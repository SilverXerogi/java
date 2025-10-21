package parts;

import interfaces.IProductPart;

public class Chassis implements IProductPart {
    public Chassis() {
        System.out.println("Создана часть: шасси");
    }

    @Override
    public String toString() {
        return "Шасси";
    }
}
