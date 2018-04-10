package ru.ifmo.rain.kopitsa.mapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class ParallelMapperImpl implements ParallelMapper {

    private int threads;
    private Thread[] threadsArray;
    private final List<Runnable> tasks;

    public ParallelMapperImpl(int threads) {
        this.threads = threads;
        threadsArray = new Thread[threads];
        tasks = new LinkedList<>();

        for (int i = 0; i < threads; i++) {
            threadsArray[i] = new Thread(() -> {
                Runnable currentRunnable;
                while (!Thread.currentThread().isInterrupted()) {
                    synchronized (tasks) {
                        while (tasks.isEmpty()) {
                            try {
                                tasks.wait();
                            } catch (InterruptedException e) {
                                return;
                            }
                        }
                        currentRunnable = tasks.remove(0);
                    }
                    currentRunnable.run();
                }
            });
            threadsArray[i].start();
        }
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        List<R> results = new ArrayList<>(args.size());
        for (int i = 0; i < args.size(); i++) {
            results.add(null);
        }

        final Counter counter = new Counter();
        for (int i = 0; i < args.size(); i++) {
            int finalI = i;
            Runnable runnable = () -> {
                results.set(finalI, f.apply(args.get(finalI)));
                synchronized (counter) {
                    counter.increment();
                    if (counter.getCount() == args.size()) {
                        counter.notify();
                    }
                }
            };
            synchronized (tasks) {
                tasks.add(runnable);
                tasks.notify();
            }
        }
        synchronized (counter) {
            while (counter.getCount() < args.size()) {
                counter.wait();
            }
        }
        return results;
    }

    @Override
    public void close() {
        for (int i = 0; i < threads; i++) {
            threadsArray[i].interrupt();
        }
        for (int i = 0; i < threads; i++) {
            try {
                threadsArray[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class Counter {
        private int count = 0;

        private int getCount() {
            return count;
        }

        private void increment() {
            count++;
        }
    }
}
