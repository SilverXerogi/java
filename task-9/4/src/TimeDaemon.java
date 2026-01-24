import java.time.LocalTime;

public class TimeDaemon {

    public static void main(String[] args) throws InterruptedException {

        Thread timeThread = new TimePrinter(2);
        timeThread.setDaemon(true);
        timeThread.start();

        Thread.sleep(10000);
        System.out.println("Main thread finished");
    }

    static class TimePrinter extends Thread {
        private final int seconds;

        public TimePrinter(int seconds) {
            this.seconds = seconds;
        }

        @Override
        public void run() {
            while (true) {
                System.out.println("Time: " + LocalTime.now());
                try {
                    Thread.sleep(seconds * 1000L);
                } catch (InterruptedException ignored) {}
            }
        }
    }
}
