import interfaces.IAssemblyLine;
import interfaces.ILineStep;
import interfaces.IProduct;
import product.Car;
import product.CarAssemblyLine;
import steps.BodyStep;
import steps.ChassisStep;
import steps.EngineStep;

public class Main {
    public static void main(String[] args) {
        ILineStep step1 = new BodyStep();
        ILineStep step2 = new ChassisStep();
        ILineStep step3 = new EngineStep();

        IAssemblyLine line = new CarAssemblyLine(step1, step2, step3);

        IProduct car = new Car();

        car = line.assembleProduct(car);

        System.out.println("\nГотовый продукт:");
        System.out.println(car);
    }
}
