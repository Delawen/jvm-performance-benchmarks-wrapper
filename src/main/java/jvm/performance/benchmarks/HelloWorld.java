package jvm.performance.benchmarks;

import com.ionutbalosin.jvm.performance.benchmarks.miscellaneous.fibonacci.recursive.RecursiveFormula;
import com.ionutbalosin.jvm.performance.benchmarks.miscellaneous.nqueens.backtracking.NQueensBacktracking;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.Random;
import java.util.concurrent.*;

public class HelloWorld {
    private static final Random random = new Random(16384);

    public static int conditional_dead_loop() {
        int result = random.nextInt(2, 64) << 1;
        boolean useLoopResult = false;

        int i = 0, dummy = 0;
        for (; i < 20_000; i++) {
            dummy = Math.max(dummy + i, i);
        }

        // because "useLoopResult" is always false, this branch is never taken
        if (useLoopResult) {
            result += dummy;
        }

        return result;
    }


    public static BigInteger fibonacci() {
        var number = random.nextLong(1, 100);
        return RecursiveFormula.fibonacci(number, BigInteger.ZERO, BigInteger.ONE);
    }

    public static byte[][] nqueens() throws ExecutionException, InterruptedException {
        var number = random.nextInt(2, 12);
        var startingRow = random.nextInt(number);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<byte[][]> future = executor.submit(() -> NQueensBacktracking.placeNQueens(number, startingRow));

        try {
            return future.get(50, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new InterruptedException(e.getMessage());
        } finally {
            executor.shutdown();
        }

    }
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        var start = System.nanoTime();
        var time = start;
        var formatter = new DecimalFormat("###,###,###");

        System.out.println("Looping over a conditional dead loop!");

        for (int i = 0; i < 100; i++) {
            conditional_dead_loop();
        }

        System.out.println("That took " + formatter.format(System.nanoTime() - time) + " ns.");

        time = System.nanoTime();
        System.out.println("Generating Fibonacci");
        for (int i = 0; i < 100; i++) {
            fibonacci();
        }

        System.out.println("That took " + formatter.format(System.nanoTime() - time) + " ns.");

        time = System.nanoTime();
        System.out.println("Generating NQueens");
        for (int i = 0; i < 500; i++) {
            nqueens();
        }

        System.out.println("That took " + formatter.format(System.nanoTime() - time) + " ns.");

        System.out.println("The whole program took " + formatter.format(System.nanoTime() - time) + " ns.");
    }
}