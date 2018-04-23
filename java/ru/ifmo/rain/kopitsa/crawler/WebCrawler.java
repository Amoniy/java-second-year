package ru.ifmo.rain.kopitsa.crawler;

import info.kgeorgiy.java.advanced.crawler.Crawler;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.Result;

public class WebCrawler implements Crawler {

    private Downloader downloader;
    private int downloaders;
    private int extractors;
    private int perHost;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.downloaders = downloaders;
        this.extractors = extractors;
        this.perHost = perHost;
    }

    @Override
    public Result download(String url, int depth) {
        return null;
    }

    @Override
    public void close() {

    }
}
