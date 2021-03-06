package ru.ifmo.rain.kopitsa.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class IterativeParallelism implements ListIP {

    private ParallelMapper mapper;

    public IterativeParallelism() {

    }

    public IterativeParallelism(ParallelMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        List<String> ans = applyFunctionConcurrently(threads, values, ts ->
                ts.stream().map(Object::toString).collect(Collectors.joining()));
        assert ans != null;
        return ans.stream().map(Object::toString).collect(Collectors.joining());
    }

    @Override
    public <T> List<T> filter(final int threads, final List<? extends T> values, final Predicate<? super T> predicate)
            throws InterruptedException {
        List<List<T>> ans = applyFunctionConcurrently(threads, values, ts -> ts.stream().filter(predicate)
                .collect(Collectors.toList()));
        assert ans != null;
        return ans.stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Override
    public <T, U> List<U> map(final int threads, final List<? extends T> values, final Function<? super T, ? extends U> f)
            throws InterruptedException {
        List<List<U>> ans = applyFunctionConcurrently(threads, values, ts -> ts.stream().map(f)
                .collect(Collectors.toList()));
        assert ans != null;
        return ans.stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator)
            throws InterruptedException {
        List<T> ans = applyFunctionConcurrently(threads, values, listTFunction -> listTFunction.stream().max(comparator)
                .orElse(null));
        assert ans != null;
        return ans.stream().max(comparator).orElse(null);
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator)
            throws InterruptedException {
        return maximum(threads, values, comparator.reversed());
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate)
            throws InterruptedException {
        List<Boolean> ans = applyFunctionConcurrently(threads, values, listBooleanFunction -> listBooleanFunction
                .stream().allMatch(predicate));
        assert ans != null;
        return ans.stream().allMatch(intermediateResult -> intermediateResult);
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate)
            throws InterruptedException {
        return !all(threads, values, predicate.negate());
    }

    private <T> List<List<? extends T>> splitList(int threads, List<? extends T> values) {
        List<List<? extends T>> splitList = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            splitList.add(values.subList(i * values.size() / threads, (i + 1) * values.size() / threads));
        }
        return splitList;
    }

    private <T, R> List<R> applyFunctionConcurrently(int threads, List<? extends T> values,
                                                     Function<List<? extends T>, R> function)
            throws InterruptedException {
        if (values.size() == 0) {
            return null;
        }
        threads = Math.min(threads, values.size());
        List<List<? extends T>> arguments = splitList(threads, values);

        if (mapper != null) {
            return mapper.map(function, arguments);
        }

        List<R> intermediateResults = new ArrayList<>(threads);
        for (int i = 0; i < threads; i++) {
            intermediateResults.add(null);
        }

        Thread[] threadList = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            int finalI = i;
            threadList[i] = new Thread(() -> {
                intermediateResults.set(finalI, function.apply(arguments.get(finalI)));
            });
            threadList[i].start();
        }
        InterruptedException interruptedException = null;
        for (int i = 0; i < threads; i++) {
            try {
                threadList[i].join();
            } catch (InterruptedException e) {
                if (interruptedException == null) {
                    interruptedException = e;
                    i--;
                    continue;
                }
                i--;
                interruptedException.addSuppressed(e);
            }
        }
        if (interruptedException != null) {
            throw interruptedException;
        }

        return intermediateResults;
    }
}
