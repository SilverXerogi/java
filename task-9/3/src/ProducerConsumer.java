import java.util.*;

public class ProducerConsumer {

    private static final int BUFFER_SIZE = 5;
    private static final List<Integer> buffer = new ArrayList<>();

    public static void main(String[] args) {
        Thread producer = new Thread(new Producer());
        Thread consumer = new Thread(new Consumer());

        producer.start();
        consumer.start();
    }

    static class Producer implements Runnable {
        private final Random random = new Random();

        @Override
        public void run() {
            while (true) {
                synchronized (buffer) {
                    while (buffer.size() == BUFFER_SIZE) {
                        try {
                            buffer.wait();
                        } catch (InterruptedException ignored) {}
                    }
                    int value = random.nextInt(100);
                    buffer.add(value);
                    System.out.println("Produced: " + value);
                    buffer.notifyAll();
                }
                sleep();
            }
        }
    }

    static class Consumer implements Runnable {
        @Override
        public void run() {
            while (true) {
                synchronized (buffer) {
                    while (buffer.isEmpty()) {
                        try {
                            buffer.wait();
                        } catch (InterruptedException ignored) {}
                    }
                    int value = buffer.remove(0);
                    System.out.println("Consumed: " + value);
                    buffer.notifyAll();
                }
                sleep();
            }
        }
    }

    private static void sleep() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {}
    }
}
