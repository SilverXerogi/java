import java.util.ArrayList;
import java.util.List;

public class Bouquet {
    private List<Flower> flowers = new ArrayList<>();

    public void addFlower(Flower flower) {
        flowers.add(flower);
        System.out.println("Добавлен цветок: " + flower.getName());
    }

    public double getTotalPrice() {
        double sum = 0;
        for (Flower f : flowers) {
            sum += f.getPrice();
        }
        return sum;
    }

    public void showBouquet() {
        System.out.println("\nСостав букета:");
        for (Flower f : flowers) {
            System.out.println("- " + f);
        }
        System.out.println("Общая стоимость: " + getTotalPrice() + " руб.");
    }
}
