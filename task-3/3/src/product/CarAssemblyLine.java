package product;

import interfaces.IAssemblyLine;
import interfaces.ILineStep;
import interfaces.IProduct;
import interfaces.IProductPart;

public class CarAssemblyLine implements IAssemblyLine {
    private ILineStep step1;
    private ILineStep step2;
    private ILineStep step3;

    public CarAssemblyLine(ILineStep step1, ILineStep step2, ILineStep step3) {
        this.step1 = step1;
        this.step2 = step2;
        this.step3 = step3;
    }

    @Override
    public IProduct assembleProduct(IProduct product) {
        System.out.println("=== Начало сборки автомобиля ===");

        IProductPart part1 = step1.buildProductPart();
        product.installFirstPart(part1);

        IProductPart part2 = step2.buildProductPart();
        product.installSecondPart(part2);

        IProductPart part3 = step3.buildProductPart();
        product.installThirdPart(part3);

        System.out.println("=== Сборка завершена ===");
        return product;
    }
}
