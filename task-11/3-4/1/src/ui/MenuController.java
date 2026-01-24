package ui;

public class MenuController {
    private final Builder builder;
    private final Navigator navigator;

    public MenuController(Builder builder, Navigator navigator) {
        this.builder = builder;
        this.navigator = navigator;
    }

    public void run() {
        navigator.setCurrentMenu(builder.getRootMenu());
        while (true) {
            navigator.printMenu();
            navigator.navigate();
        }
    }
}
