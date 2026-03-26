package bookstore.ui;

import java.util.Scanner;
import java.util.Stack;

public class Navigator {
    private Menu currentMenu;
    private final Scanner scanner = new Scanner(System.in);
    private final Stack<Menu> previousMenus = new Stack<>();

    public void setCurrentMenu(Menu menu) {
        this.currentMenu = menu;
    }

    public void printMenu() {
        System.out.println("\n=== " + currentMenu.getName() + " ===");
        int i = 1;
        for (MenuItem item : currentMenu.getItems()) {
            System.out.println(i++ + ". " + item.getTitle());
        }
        System.out.println("0. Назад");
    }

    public void navigate() {
        System.out.print("Выберите пункт: ");
        int choice = -1;
        try {
            choice = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Введите корректное число!");
            return;
        }

        if (choice == 0) {
            if (!previousMenus.isEmpty()) {
                currentMenu = previousMenus.pop();
            } else {
                System.out.println("Вы находитесь в главном меню.");
            }
            return;
        }

        if (choice < 0 || choice > currentMenu.getItems().size()) {
            System.out.println("Неверный пункт меню!");
            return;
        }

        MenuItem selected = currentMenu.getItems().get(choice - 1);
        selected.doAction();

        if (selected.getNextMenu() != null) {
            previousMenus.push(currentMenu); // добавляем текущее меню в стек
            currentMenu = selected.getNextMenu();
        }
    }
}
