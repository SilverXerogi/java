package bookstore;

import bookstore.config.SpringConfig;
import bookstore.model.BookStoreServiceImpl;
import bookstore.model.factory.DefaultBookStoreFactory;
import bookstore.ui.MenuFactory;
import bookstore.ui.Builder;
import bookstore.ui.Navigator;
import bookstore.ui.MenuController;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String[] args) {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(SpringConfig.class);

        BookStoreServiceImpl store = ctx.getBean(BookStoreServiceImpl.class);

        if (store.getBooksSortedByTitle().isEmpty()) {
            System.out.println("БД пуста");
        }

        // Получаем остальные бины через Spring
        DefaultBookStoreFactory factory = ctx.getBean(DefaultBookStoreFactory.class);

        MenuFactory menuFactory = new MenuFactory(store, store.getInventory(), factory);
        Builder builder = new Builder(menuFactory);
        Navigator navigator = new Navigator();
        MenuController controller = new MenuController(builder, navigator);

        Runtime.getRuntime().addShutdownHook(new Thread(() ->
                System.out.println("\nПриложение завершено. Данные хранятся в БД.")
        ));

        controller.run();
    }
}