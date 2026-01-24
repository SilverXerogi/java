import model.*;
import model.factory.DefaultBookStoreFactory;
import model.persistence.AppState;
import model.persistence.StateSerializer;
import model.BookStoreServiceImpl;
import ui.*;

import java.time.LocalDate;

public class Main {

    public static void main(String[] args) {
        di.BeanContainer container = di.BeanContainer.getInstance();

        Inventory inventory = new Inventory();
        DefaultBookStoreFactory factory = new DefaultBookStoreFactory();
        BookStoreServiceImpl store = new BookStoreServiceImpl(inventory, factory);

        // Загружаем состояние, если оно есть
        AppState loaded = StateSerializer.loadState();
        if (loaded != null) {
            inventory.restoreFromState(loaded);
            factory.restoreCounters(loaded.getBookCounter(), loaded.getOrderCounter(), loaded.getRequestCounter());
            store.restoreFromState(loaded);
        } else {
            // Инициализация демо-данных
            if (inventory.getCatalogMap().isEmpty()) {
                System.out.println("Инициализация начальных данных...");
                Book b1 = factory.createBook("Война и мир", Book.Status.AVAILABLE, 500, LocalDate.of(1869,1,1), "Эпопея Л.Н. Толстого");
                Book b2 = factory.createBook("Преступление и наказание", Book.Status.AVAILABLE, 400, LocalDate.of(1866,1,1), "Роман Ф.М. Достоевского");

                inventory.registerBook(b1, 5, 500);
                inventory.registerBook(b2, 2, 400);
            }
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
            System.out.println("\nСохранение состояния перед выходом...");
            StateSerializer.saveState(
                    inventory,
                    store.getAllOrders(),
                    store.getAllRequests(),
                    factory
            );
        }));

        controller.run();
    }
}