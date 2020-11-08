package first;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implement the Test-and-set–based mutual exclusion algorithm. The pseudocode of the algorithm
 * was presented during the lecture. Compare the performance of your implementation to the speed
 * of the built-in implementation provided in ReentrantLock class.
 * Hint. The shared register, occupied, should be implemented as an instance of AtomicBoolean
 * class, while the Test-and-set atomic instruction should be replaced with compareAndSet() method
 * of AtomicBoolean class.
 * The skeleton of the implementation is shown below.
 */
class TestAndSet {
    private AtomicBoolean state;


    public TestAndSet(AtomicBoolean state) {
        this.state = state;
    }

    public void lock() {
        while (state.getAndSet(true)) {
        }
    }

    public void unlock() {
        state.set(false);
    }
}

class Counter {
    static final int Iter = 1_000_000;
    AtomicBoolean state = new AtomicBoolean();
    final TestAndSet lock = new TestAndSet(state);

    int value = 0;

    public void increment() {
        lock.lock(); // Entry protocol
        try { // Critical section:
            ++value;
        } finally {
            lock.unlock(); // Exit protocol
        }
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
            counter.increment();
        }
    }
}

public class Exercise1 {
    public static void main(String[] args) throws InterruptedException {
        Counter counter = new Counter(); // Single counter
        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; ++i) {
            threads[i] = new Thread(new Worker(counter));
        }
        long startTime = System.nanoTime();
        for (Thread t : threads) {
            t.start();
        }
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