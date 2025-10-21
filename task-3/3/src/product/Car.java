package product;

import interfaces.IProduct;
import interfaces.IProductPart;

public class Car implements IProduct {
    private IProductPart body;
    private IProductPart chassis;
    private IProductPart engine;

    @Override
    public void installFirstPart(IProductPart part) {
        this.body = part;
        System.out.println("Установлена первая часть: " + part);
    }

    @Override
    public void installSecondPart(IProductPart part) {
        this.chassis = part;
        System.out.println("Установлена вторая часть: " + part);
    }

    @Override
    public void installThirdPart(IProductPart part) {
        this.engine = part;
        System.out.println("Установлена третья часть: " + part);
    }

    @Override
    public String toString() {
        return "Автомобиль [" +
                "кузов=" + body + ", " +
                "шасси=" + chassis + ", " +
                "двигатель=" + engine + "]";
    }
}


