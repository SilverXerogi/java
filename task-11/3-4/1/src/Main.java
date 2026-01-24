import model.*;
import model.factory.DefaultBookStoreFactory;
import model.BookStoreServiceImpl;
import ui.*;

import java.time.LocalDate;

public class Main {

    public static void main(String[] args) {
        di.BeanContainer container = di.BeanContainer.getInstance();

        Inventory inventory = new Inventory();
        DefaultBookStoreFactory factory = new DefaultBookStoreFactory();
        BookStoreServiceImpl store = new BookStoreServiceImpl(inventory, factory);

        // Инициализация демо-данных, если БД пуста
        if (store.getBooksSortedByTitle().isEmpty()) {
            System.out.println("бд пустая");

        }

        container.registerSingleton(Inventory.class, inventory);
        container.registerSingleton(DefaultBookStoreFactory.class, factory);
        container.registerSingleton(BookStoreServiceImpl.class, store);

        // Передаём factory в MenuFactory
        MenuFactory menuFactory = new MenuFactory(store, inventory, factory);
        Builder builder = new Builder(menuFactory);
        Navigator navigator = new Navigator();
        MenuController controller = new MenuController(builder, navigator);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nПриложение завершено. Данные хранятся в БД.");
        }));

        controller.run();
    }
}