package ru.ifmo.rain.kopitsa.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ScalarIP;

import java.util.ArrayList;
import java.util.Collections;
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
        List<Thread> threadList = new ArrayList<>();

        ArrayList<T> intermediateResults = new ArrayList<>(threads);
        for (int i = 0; i < threads; i++) {
            intermediateResults.add(null);
        }

        int finalThreads = threads;
        for (int i = 0; i < threads; i++) {
            // Коля, а как вообще так, что это эффективли файнл?
            int finalI = i;
            threadList.add(new Thread(() -> {
                intermediateResults.set(finalI, Collections.max(values.subList(
                        finalI * values.size() / finalThreads,
                        (finalI + 1) * values.size() / finalThreads), comparator));
            }));
            threadList.get(i).run();
        }
        for (int i = 0; i < threads; i++) {
            threadList.get(i).join();
        }

        return Collections.max(intermediateResults, comparator);
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

        boolean[] intermediateResults = new boolean[threads];
        List<Thread> threadList = new ArrayList<>();
        int finalThreads = threads;
        for (int i = 0; i < threads; i++) {
            int finalI = i;
            threadList.add(new Thread(() -> {
                intermediateResults[finalI] = values.subList(
                        finalI * values.size() / finalThreads,
                        (finalI + 1) * values.size() / finalThreads).stream().allMatch(predicate);
            }));
            threadList.get(i).run();
        }
        for (int i = 0; i < threads; i++) {
            threadList.get(i).join();
        }
        for (int i = 0; i < threads; i++) {
            if (!intermediateResults[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        if (values.size() == 0) {
            return false;
        }
        threads = Math.min(threads, values.size());

        boolean[] intermediateResults = new boolean[threads];
        List<Thread> threadList = new ArrayList<>();
        int finalThreads = threads;
        for (int i = 0; i < threads; i++) {
            int finalI = i;
            threadList.add(new Thread(() -> {
                intermediateResults[finalI] = values.subList(
                        finalI * values.size() / finalThreads,
                        (finalI + 1) * values.size() / finalThreads).stream().anyMatch(predicate);
            }));
            threadList.get(i).run();
        }
        for (int i = 0; i < threads; i++) {
            threadList.get(i).join();
        }
        for (int i = 0; i < threads; i++) {
            if (intermediateResults[i]) {
                return true;
            }
        }
        return false;
    }
}
