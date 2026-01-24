package ui;

public class Builder {
    private final Menu rootMenu;

    public Builder(MenuFactory factory) {
        this.rootMenu = factory.createMenu(MenuType.MAIN);
    }

    public Menu getRootMenu() {
        return rootMenu;
    }
}
