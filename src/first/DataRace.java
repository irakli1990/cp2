package first;

class Counter {
    static final int Iter = 1_000;  // Read-only, just for testing purposes
    int value = 0;

    public void increment() {
        ++value;
    }
}

class Worker implements Runnable {
    Counter counter = null;

    public Worker(Counter c) {
        counter = c;
    }

    @Override
    public void run() {
        for (int i = 0; i < Counter.Iter; ++i) {
            counter.increment();  // All threads increment the same (shared)
            // counter simultaneously (unsafe!)
        }
    }
}

// Main class of the application:
public class DataRace {
    public static void main(String [] args) throws InterruptedException {
        Counter counter = new Counter();  // Single counter

        Thread [] threads = new Thread[10];
        for (int i = 0; i < threads.length; ++i) {
            // All worker threads share the same counter
            threads[i] = new Thread(new Worker(counter));
        }

        long startTime = System.nanoTime();

        for (Thread t : threads) {
            t.start();
        }

        // Now we wait for all the created threads
        for (Thread t : threads) {
            t.join();
        }

        long endTime = System.nanoTime();

        // Now only the main thread prints the final value:
        System.out.println("Counter value is: " + counter.value);
        System.out.println("The expected value is: " + (threads.length * Counter.Iter));
        System.out.printf("The execution took: %.2f ms\n", (endTime - startTime) * 1e-6);
    }
}