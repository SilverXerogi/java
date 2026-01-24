package bookstore;

import bookstore.model.Inventory;
import bookstore.model.BookStoreServiceImpl;
import bookstore.model.factory.DefaultBookStoreFactory;
import bookstore.ui.MenuFactory;
import bookstore.ui.Builder;
import bookstore.ui.Navigator;
import bookstore.ui.MenuController;
import bookstore.di.BeanContainer;

public class Main {
    public static void main(String[] args) {
        BeanContainer container = BeanContainer.getInstance();

        Inventory inventory = new Inventory();
        DefaultBookStoreFactory factory = new DefaultBookStoreFactory();
        BookStoreServiceImpl store = new BookStoreServiceImpl(inventory, factory);

        if (store.getBooksSortedByTitle().isEmpty()) {
            System.out.println("БД пуста");
        }

        container.registerSingleton(Inventory.class, inventory);
        container.registerSingleton(DefaultBookStoreFactory.class, factory);
        container.registerSingleton(BookStoreServiceImpl.class, store);

        MenuFactory menuFactory = new MenuFactory(store, inventory, factory);
        Builder builder = new Builder(menuFactory);
        Navigator navigator = new Navigator();
        MenuController controller = new MenuController(builder, navigator);

        Runtime.getRuntime().addShutdownHook(new Thread(() ->
                System.out.println("\nПриложение завершено. Данные хранятся в БД.")
        ));

        controller.run();
    }
}