import model.*;

import model.factory.DefaultBookStoreFactory;
import ui.Builder;
import ui.MenuController;
import ui.MenuFactory;
import ui.Navigator;

import java.time.LocalDate;

public class Main {

    public static void main(String[] args) {
        // Получаем синглтон инвентаря и магазина
        Inventory inventory = Inventory.getInstance();
        BookStoreServiceImpl store = BookStoreServiceImpl.getInstance(inventory);

        // Получаем фабрику
        DefaultBookStoreFactory factory = DefaultBookStoreFactory.getInstance();

        // Для демонстрации добавим несколько книг (новый синтаксис фабрики)
        Book b1 = factory.createBook("Война и мир", Book.Status.AVAILABLE, 500, LocalDate.of(1869,1,1), "Эпопея Л.Н. Толстого");
        Book b2 = factory.createBook("Преступление и наказание", Book.Status.AVAILABLE, 400, LocalDate.of(1866,1,1), "Роман Ф.М. Достоевского");

        // Регистрируем книги в инвентаре
        inventory.registerBook(b1, 5, 500);
        inventory.registerBook(b2, 2, 400);

        // Создаём меню
        MenuFactory menuFactory = new MenuFactory(store);
        Builder builder = new Builder(menuFactory);
        Navigator navigator = new Navigator();
        MenuController controller = new MenuController(builder, navigator);

        // Запуск приложения
        controller.run();
    }
}
