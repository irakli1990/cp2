package second;

import java.util.concurrent.locks.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Random;


/**
 * The following program is a simplistic simulation of a banking system in which Bank class is
 * responsible for managing a number, N, of accounts. Every account has an associated balance,
 * equal to the amount of money deposited to that account. Additionally, every account has an
 * associated lock stored in locks array.
 * A number of concurrent accountants, implemented as Accountant class, try to execute a number
 * of transfers between pairs of randomly selected accounts (identified by numbers from 0 to N −1).
 * Each transfer attempts to withdraw a random amount of money from one account and deposit it
 * to the other account. Transfer is possible only if the balance of the first account is greater than
 * the requested amount.
 * Your task is to write a body of the bank’s transfer() method so that the transfers between
 * the accounts are correct and safe from concurrency related errors. Use the locks stored in locks
 * array to provide mutually exclusive access to the accounts.
 * Hint. Consider using tryLock() method provided by Lock interface
 */
class Bank {
    public static final int N = 10;
    private int[] balances = new int[N];
    private Lock[] locks = new Lock[N];

    public Bank() {
        for (int i = 0; i < locks.length; ++i) {
            locks[i] = new ReentrantLock();
        }
    }

    public void deposit(int accountId, int amount) {
        balances[accountId] += amount;
    }

    public int getBalance(int accountId) {
        return balances[accountId];
    }

    public boolean transfer(int fromAccount, int toAccount, int amount) {
        if ((this.getBalance(fromAccount) > amount)) {
            for (Lock lock :
                    locks) {
                if (lock.tryLock()) {
                    balances[fromAccount] = this.getBalance(fromAccount) - amount;
                    balances[toAccount] = this.getBalance(toAccount) + amount;
                    lock.unlock();
                }
                break;
            }

            return true;
        } else {
            return false;
        }

    }
}

class Accountant extends Thread {
    Bank bank;

    public Accountant(Bank bank) {
        this.bank = bank;
    }

    @Override
    public void run() {
        Random rng = ThreadLocalRandom.current();
        for (int i = 0; i < 1000; ++i) {
            int fromAccount = rng.nextInt(Bank.N - 1);
            int toAccount = rng.nextInt(Bank.N - 1);
            while (toAccount == fromAccount) { // Source should differ from
// the target
                toAccount = rng.nextInt(Bank.N - 1); // Try again
            }
            bank.transfer(fromAccount, toAccount, rng.nextInt(100));
        }
    }
}

public class Exercise2 {


    public static void main(String[] args) throws InterruptedException {
        Bank bank = new Bank();

        for (int i = 0; i < Bank.N; ++i) {
            bank.deposit(i, 100);
        }
        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; ++i) {
            threads[i] = new Accountant(bank);
        }
        for (Thread t : threads) {
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }
        int total = 0;
        for (int i = 0; i < Bank.N; ++i) {
            int b = bank.getBalance(i);
            total += b;
            System.out.printf("Account [%d] balance: %d\n", i, b);
        }
        System.out.printf("Total balance is %d\tvalid value is %d\n",
                total, Bank.N * 100);
    }
}