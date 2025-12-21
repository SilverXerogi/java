package config;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Configurator {

    public static void configure(Object target) {
        if (target == null) return;

        Class<?> clazz = target.getClass();
        String defaultConfigFile = "config.properties";

        // Сначала соберём все поля с аннотацией
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(ConfigProperty.class)) {
                ConfigProperty ann = field.getAnnotation(ConfigProperty.class);
                String configFile = !ann.configFileName().isEmpty() ? ann.configFileName() : defaultConfigFile;
                String propertyName = !ann.propertyName().isEmpty()
                        ? ann.propertyName()
                        : clazz.getSimpleName().toUpperCase() + "." + field.getName().toUpperCase();

                // Загружаем properties (кэшируем по имени файла для эффективности)
                Properties props = loadProperties(configFile);

                if (!props.containsKey(propertyName)) {
                    System.out.println("[Configurator] Свойство не найдено: " + propertyName + " в " + configFile);
                    continue;
                }

                String valueStr = props.getProperty(propertyName);
                try {
                    Object value = convertValue(valueStr, field.getType(), ann.type());
                    field.setAccessible(true);
                    field.set(target, value);
                } catch (Exception e) {
                    System.err.println("Ошибка при настройке поля " + field.getName() + ": " + e.getMessage());
                }
            }
        }
    }

    private static final Map<String, Properties> propertiesCache = new HashMap<>();

    private static Properties loadProperties(String fileName) {
        return propertiesCache.computeIfAbsent(fileName, fn -> {
            Properties props = new Properties();
            try (var in = Configurator.class.getClassLoader().getResourceAsStream(fn)) {
                if (in != null) {
                    props.load(in);
                } else {
                    System.out.println("[Configurator] Файл конфигурации не найден: " + fn);
                }
            } catch (IOException e) {
                System.err.println("Ошибка загрузки " + fn + ": " + e.getMessage());
            }
            return props;
        });
    }

    @SuppressWarnings("unchecked")
    private static Object convertValue(String value, Class<?> fieldType, ConfigType type) {
        if (type == ConfigType.AUTO) {
            if (fieldType == String.class) type = ConfigType.STRING;
            else if (fieldType == int.class || fieldType == Integer.class) type = ConfigType.INTEGER;
            else if (fieldType == long.class || fieldType == Long.class) type = ConfigType.LONG;
            else if (fieldType == double.class || fieldType == Double.class) type = ConfigType.DOUBLE;
            else if (fieldType == boolean.class || fieldType == Boolean.class) type = ConfigType.BOOLEAN;
            else if (fieldType == List.class || fieldType == ArrayList.class) type = ConfigType.INTEGER_LIST; // пример
            else if (fieldType.isArray() && fieldType.getComponentType() == String.class) type = ConfigType.STRING_ARRAY;
            else type = ConfigType.STRING;
        }

        return switch (type) {
            case STRING -> value;
            case INTEGER -> Integer.parseInt(value.trim());
            case LONG -> Long.parseLong(value.trim());
            case DOUBLE -> Double.parseDouble(value.trim());
            case BOOLEAN -> Boolean.parseBoolean(value.trim());
            case STRING_ARRAY -> Arrays.stream(value.split(","))
                    .map(String::trim)
                    .toArray(String[]::new);
            case INTEGER_LIST -> Arrays.stream(value.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            default -> value;
        };
    }
}