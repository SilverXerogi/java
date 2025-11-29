package ui;

import java.util.List;

public class Menu {
    private final String name;
    private final List<MenuItem> menuItems;

    public Menu(String name, List<MenuItem> menuItems) {
        this.name = name;
        this.menuItems = menuItems;
    }

    public String getName() {
        return name;
    }

    public List<MenuItem> getItems() {
        return menuItems;
    }
}
