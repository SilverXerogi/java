package parts;

import interfaces.IProductPart;

public class Engine implements IProductPart {
    public Engine() {
        System.out.println("Создана часть: двигатель");
    }

    @Override
    public String toString() {
        return "Двигатель";
    }
}
