package ru.ifmo.rain.kopitsa.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ScalarIP;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class IterativeParallelism implements ScalarIP {

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        if (values.size() == 0) {
            return null;
        }
        threads = Math.min(threads, values.size());
        Thread[] threadList = new Thread[threads];

        List<T> intermediateResults = new ArrayList<>(threads);
        for (int i = 0; i < threads; i++) {
            intermediateResults.add(null);
        }

        int finalThreads = threads;
        for (int i = 0; i < threads; i++) {
            // Коля, а как вообще так, что это эффективли файнл?
            int finalI = i;
            threadList[i] = new Thread(() -> {
                intermediateResults.set(finalI, values.subList(
                        finalI * values.size() / finalThreads,
                        (finalI + 1) * values.size() / finalThreads).stream().max(comparator).orElse(null));
            });
            threadList[i].start();
        }
        for (int i = 0; i < threads; i++) {
            threadList[i].join();
        }

        return intermediateResults.stream().max(comparator).orElse(null);
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, comparator.reversed());
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        if (values.size() == 0) {
            return true;
        }
        threads = Math.min(threads, values.size());

        Boolean[] intermediateResults = new Boolean[threads];
        Thread[] threadList = new Thread[threads];
        int finalThreads = threads;
        for (int i = 0; i < threads; i++) {
            int finalI = i;
            threadList[i] = new Thread(() -> {
                intermediateResults[finalI] = values.subList(
                        finalI * values.size() / finalThreads,
                        (finalI + 1) * values.size() / finalThreads).stream().allMatch(predicate);
            });
            threadList[i].run();
        }
        for (int i = 0; i < threads; i++) {
            threadList[i].start();
        }
        return Arrays.stream(intermediateResults).allMatch(res -> res);
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return !all(threads, values, predicate.negate());
    }
}
