package steps;

import interfaces.ILineStep;
import interfaces.IProductPart;
import parts.Engine;

public class EngineStep implements ILineStep {
    @Override
    public IProductPart buildProductPart() {
        System.out.println("Шаг 3: создаем двигатель...");
        return new Engine();
    }
}
