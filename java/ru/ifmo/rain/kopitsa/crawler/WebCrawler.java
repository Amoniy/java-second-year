package ru.ifmo.rain.kopitsa.crawler;

import info.kgeorgiy.java.advanced.crawler.Crawler;
import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.Result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WebCrawler implements Crawler {

    private class DataHandler {

        // data
        private Queue<String> links;
        private ConcurrentSkipListSet<String> downloaded;
        private Map<String, IOException> errors;

        // synchronization utils
        private Lock lock;
        private Condition done;
        private AtomicInteger count;

        private DataHandler() {
            links = new ConcurrentLinkedQueue<>();
            downloaded = new ConcurrentSkipListSet<>();
            errors = new ConcurrentHashMap<>();
            lock = new ReentrantLock();
            done = lock.newCondition();
            count = new AtomicInteger();
        }
    }

    //    private int downloaders;
//    private int extractors;
//    private int perHost;
    private Downloader downloader;

    // возможно вынести в хендлер
    private ExecutorService downloadThreads;
    private ExecutorService extractorThreads;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
//        this.downloaders = downloaders;
//        this.extractors = extractors;
//        this.perHost = perHost;

        downloadThreads = Executors.newFixedThreadPool(downloaders);
        extractorThreads = Executors.newFixedThreadPool(extractors);
    }

    @Override
    public Result download(String url, int depth) {
        DataHandler handler = new DataHandler();
        try {
            handler.lock.lock();
            downloadThreads.submit(new DownloaderRunnable(url, 1, depth, handler));
            while (handler.count.get() > 0) {
                handler.done.await();
            }
        } catch (InterruptedException ignored) {
        } finally {
            handler.lock.unlock();
        }
        return new Result(new ArrayList<>(handler.links), handler.errors);
    }

    @Override
    public void close() {
        downloadThreads.shutdown();
        extractorThreads.shutdown();
    }

    private void decrementTasks(DataHandler handler) {
        if (handler.count.decrementAndGet() == 0) {
            try {
                handler.lock.lock();
                handler.done.signal();
            } finally {
                handler.lock.unlock();
            }
        }
    }

    private class DownloaderRunnable implements Runnable {

        private String url;
        private int depth;
        private int maxDepth;
        private DataHandler handler;

        private DownloaderRunnable(String url, int depth, int maxDepth, DataHandler handler) {
            this.url = url;
            this.depth = depth;
            this.maxDepth = maxDepth;
            this.handler = handler;
            this.handler.count.incrementAndGet();
        }

        @Override
        public void run() {
            try {
                if (handler.downloaded.add(url)) {
                    Document document = downloader.download(url);
                    if (depth < maxDepth) {
                        extractorThreads.submit(new ExtractorRunnable(document, depth + 1, maxDepth, handler));
                    }
                    handler.links.add(url);
                }
            } catch (IOException e) {
//                System.out.println("Error happened in link = " + url);
                handler.links.remove(url);
                handler.errors.put(url, e);
            } finally {
                decrementTasks(handler);
            }
        }
    }

    private class ExtractorRunnable implements Runnable {

        private Document document;
        private int depth;
        private int maxDepth;
        private DataHandler handler;

        private ExtractorRunnable(Document document, int depth, int maxDepth, DataHandler handler) {
            this.document = document;
            this.depth = depth;
            this.maxDepth = maxDepth;
            this.handler = handler;
            this.handler.count.incrementAndGet();
        }

        @Override
        public void run() {
            try {
                for (String link : document.extractLinks()) {
//                    System.out.println("Extractor: link = " + link + ", depth = " + depth + ", maxDepth = " + maxDepth);
                    downloadThreads.submit(new DownloaderRunnable(link, depth, maxDepth, handler));
                }
            } catch (IOException ignored) {
            } finally {
                decrementTasks(handler);
            }
        }
    }
}
