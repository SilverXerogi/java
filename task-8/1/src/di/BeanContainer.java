package di;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class BeanContainer {
    private static final BeanContainer INSTANCE = new BeanContainer();
    private final Map<Class<?>, Object> singletonBeans = new ConcurrentHashMap<>();

    private BeanContainer() {}

    public static BeanContainer getInstance() {
        return INSTANCE;
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> clazz) {
        return (T) singletonBeans.computeIfAbsent(clazz, this::createBean);
    }

    private Object createBean(Class<?> clazz) {
        try {
            Constructor<?> ctor = clazz.getDeclaredConstructor();
            ctor.setAccessible(true);
            Object instance = ctor.newInstance();

            // Внедряем зависимости
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    field.setAccessible(true);
                    Class<?> fieldType = field.getType();
                    Object dependency = getBean(fieldType);
                    field.set(instance, dependency);
                }
            }

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка создания бина: " + clazz.getName(), e);
        }
    }

    public void registerSingleton(Class<?> clazz, Object instance) {
        singletonBeans.put(clazz, instance);
    }
}