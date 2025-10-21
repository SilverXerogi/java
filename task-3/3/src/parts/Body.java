package parts;

import interfaces.IProductPart;

public class Body implements IProductPart {
    public Body() {
        System.out.println("Создана часть: кузов");
    }

    @Override
    public String toString() {
        return "Кузов";
    }
}

