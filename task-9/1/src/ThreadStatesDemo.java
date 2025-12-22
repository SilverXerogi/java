public class ThreadStatesDemo {

    private static final Object lock = new Object();

    public static void main(String[] args) throws Exception {

        Thread t = new Thread(() -> {
            try {
                // TIMED_WAITING
                Thread.sleep(500);

                synchronized (lock) {
                    // WAITING
                    lock.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        // NEW
        System.out.println("State after creation: " + t.getState());

        t.start();
        Thread.sleep(100);

        // RUNNABLE
        System.out.println("State after start: " + t.getState());

        synchronized (lock) {
            Thread blocker = new Thread(() -> {
                synchronized (lock) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {}
                }
            });

            blocker.start();
            Thread.sleep(100);

            // BLOCKED
            System.out.println("State BLOCKED: " + t.getState());
        }

        Thread.sleep(600);

        // WAITING
        System.out.println("State WAITING: " + t.getState());

        synchronized (lock) {
            lock.notify();
        }

        Thread.sleep(500);

        // TERMINATED
        System.out.println("State TERMINATED: " + t.getState());
    }
}
