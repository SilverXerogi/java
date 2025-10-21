package steps;

import interfaces.ILineStep;
import interfaces.IProductPart;
import parts.Body;

public class BodyStep implements ILineStep {
    @Override
    public IProductPart buildProductPart() {
        System.out.println("Шаг 1: создаем кузов...");
        return new Body();
    }
}

