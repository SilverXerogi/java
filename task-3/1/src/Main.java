import java.util.Random;

public class Main {
    public static void main(String[] args) {
        int number = 100 + (new Random()).nextInt(900); // 100..999
        System.out.println("Случайное трёхзначное число: " + number);

        int hundreds = number / 100;
        int tens = (number / 10) % 10;
        int ones = number % 10;

        int maxDigit = Math.max(hundreds, Math.max(tens, ones));
        System.out.println("Цифры числа: " + hundreds + ", " + tens + ", " + ones);
        System.out.println("Наибольшая цифра: " + maxDigit);
    }
}
