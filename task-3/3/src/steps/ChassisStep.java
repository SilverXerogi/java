package steps;

import interfaces.ILineStep;
import interfaces.IProductPart;
import parts.Chassis;

public class ChassisStep implements ILineStep {
    @Override
    public IProductPart buildProductPart() {
        System.out.println("Шаг 2: создаем шасси...");
        return new Chassis();
    }
}
