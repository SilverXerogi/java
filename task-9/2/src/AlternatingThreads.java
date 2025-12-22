public class AlternatingThreads {

    private static final Object lock = new Object();
    private static boolean turn = true;

    public static void main(String[] args) {

        Thread t1 = new Thread(() -> print("Thread-1", true));
        Thread t2 = new Thread(() -> print("Thread-2", false));

        t1.start();
        t2.start();
    }

    private static void print(String name, boolean myTurn) {
        for (int i = 0; i < 5; i++) {
            synchronized (lock) {
                while (turn != myTurn) {
                    try {
                        lock.wait();
                    } catch (InterruptedException ignored) {}
                }
                System.out.println(name);
                turn = !turn;
                lock.notifyAll();
            }
        }
    }
}
