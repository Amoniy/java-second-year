package ru.ifmo.rain.kopitsa.crawler;

import info.kgeorgiy.java.advanced.crawler.CachingDownloader;

import java.io.IOException;

public class Main {

    private static final int DEFAULT_COUNT = 1;
    private static final int DEPTH = 1;

    public static void main(String[] args) {
        if (args == null || args.length < 2 || args.length > 5) {
            System.err.println("Wrong arguments.");
            return;
        }
        for (String argument : args) {
            if (argument == null) {
                System.err.println("Null argument.");
                return;
            }
        }
        if (!args[0].equals("WebCrawler")) {
            System.err.println("First argument should be \"WebCrawler\".");
            return;
        }
        String url = args[1];
        int downloads = DEFAULT_COUNT;
        int extractors = DEFAULT_COUNT;
        int perHost = DEFAULT_COUNT;
        try {
            if (args.length > 2) {
                downloads = Integer.parseInt(args[2]);
            }
            if (args.length > 3) {
                extractors = Integer.parseInt(args[3]);
            }
            if (args.length > 4) {
                perHost = Integer.parseInt(args[4]);
            }
        } catch (NumberFormatException e) {
            System.err.println("Arguments with indexes 2-4 are supposed to be integers.");
            return;
        }
        try {
            WebCrawler webCrawler = new WebCrawler(new CachingDownloader(), downloads, extractors, perHost);
            webCrawler.download(url, DEPTH);
        } catch (IOException e) {
            System.err.println("Downloader could have not been initialized.");
        }
    }
}
