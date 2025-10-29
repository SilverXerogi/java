public class Main {
    public static void main(String[] args) {
        System.out.println("=== Сбор букета ===");

        Bouquet bouquet = new Bouquet();

        bouquet.addFlower(new Rose());
        bouquet.addFlower(new Rose());
        bouquet.addFlower(new Tulip());
        bouquet.addFlower(new Chamomile());

        bouquet.showBouquet();
    }
}