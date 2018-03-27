package ru.ifmo.rain.kopitsa.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ScalarIP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

public class IterativeParallelism implements ScalarIP {

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        if (values.size() == 0) {
            return null;
        }
        threads = Math.min(threads, values.size());
        List<Thread> threadList = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            threadList.add(new Thread(String.valueOf(i)));
        }
        int finalThreads = threads;
        return Collections.max(
                threadList.stream().map(thread -> Collections.max(values.subList(
                        Integer.parseInt(thread.getName()) * values.size() / finalThreads,
                        (Integer.parseInt(thread.getName()) + 1) * values.size() / finalThreads)
                        , comparator)).collect(toList())
                , comparator);
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
        List<Thread> threadList = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            threadList.add(new Thread(String.valueOf(i)));
        }
        int finalThreads = threads;
        return threadList.stream().allMatch(thread -> values.subList(
                Integer.parseInt(thread.getName()) * values.size() / finalThreads,
                (Integer.parseInt(thread.getName()) + 1) * values.size() / finalThreads).stream().allMatch(predicate));
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        if (values.size() == 0) {
            return false;
        }
        threads = Math.min(threads, values.size());
        List<Thread> threadList = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            threadList.add(new Thread(String.valueOf(i)));
        }
        int finalThreads = threads;
        return threadList.stream().anyMatch(thread -> values.subList(
                Integer.parseInt(thread.getName()) * values.size() / finalThreads,
                (Integer.parseInt(thread.getName()) + 1) * values.size() / finalThreads).stream().anyMatch(predicate));
    }
}
