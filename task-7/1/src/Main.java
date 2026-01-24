import model.*;

import model.factory.DefaultBookStoreFactory;
import model.persistence.AppState;
import model.persistence.StateSerializer;
import ui.Builder;
import ui.MenuController;
import ui.MenuFactory;
import ui.Navigator;

import java.time.LocalDate;

public class Main {

    public static void main(String[] args) {
        // Получаем синглтон инвентаря
        Inventory inventory = Inventory.getInstance();
        // Получаем синглтон фабрики
        DefaultBookStoreFactory factory = DefaultBookStoreFactory.getInstance();

        // Получаем сервис, пытаясь загрузить состояние
        BookStoreServiceImpl store = BookStoreServiceImpl.getInstance(inventory, true);

        // Если состояние не было загружено (новый запуск), добавим начальные данные
        if (StateSerializer.loadState() == null) {
            if (inventory.getCatalogMap().isEmpty()) {
                System.out.println("Инициализация начальных данных...");
                // Для демонстрации добавим несколько книг
                Book b1 = factory.createBook("Война и мир", Book.Status.AVAILABLE, 500, LocalDate.of(1869,1,1), "Эпопея Л.Н. Толстого");
                Book b2 = factory.createBook("Преступление и наказание", Book.Status.AVAILABLE, 400, LocalDate.of(1866,1,1), "Роман Ф.М. Достоевского");

                inventory.registerBook(b1, 5, 500);
                inventory.registerBook(b2, 2, 400);
            }
        }


        // Создаём меню
        MenuFactory menuFactory = new MenuFactory(store);
        Builder builder = new Builder(menuFactory);
        Navigator navigator = new Navigator();
        MenuController controller = new MenuController(builder, navigator);

        // Добавляем shutdown hook для сохранения состояния при выходе
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nСохранение состояния перед выходом...");
            StateSerializer.saveState( // Передаём фабрику
                    Inventory.getInstance(),
                    BookStoreServiceImpl.getInstance(Inventory.getInstance()).getAllOrders(),
                    BookStoreServiceImpl.getInstance(Inventory.getInstance()).getAllRequests(),
                    DefaultBookStoreFactory.getInstance()
            );
        }));

        // Запуск приложения
        controller.run();
    }
}