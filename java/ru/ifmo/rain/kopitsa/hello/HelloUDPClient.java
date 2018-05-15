package ru.ifmo.rain.kopitsa.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HelloUDPClient implements HelloClient {

    private static final int RESPONSE_TIMEOUT = 100;

    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        new ClientManager(host, port, prefix, requests, threads).run();
    }

    private static class ClientManager {

        private String prefix;
        private int requests;
        private int threads;
        private SocketAddress serverAddress;
        private ExecutorService senderPool;

        ClientManager(String host, int port, String prefix, int requests, int threads) {
            this.requests = requests;
            this.threads = threads;
            this.prefix = prefix;
            serverAddress = new InetSocketAddress(host, port);
            senderPool = Executors.newFixedThreadPool(threads);
        }

        void run() {
            Collection<Callable<Void>> requestWorkers = new ArrayList<>();
            for (int threadIndex = 0; threadIndex < threads; threadIndex++) {
                requestWorkers.add(createRequestWorker(threadIndex));
            }
            try {
                senderPool.invokeAll(requestWorkers);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                senderPool.shutdown();
            }
        }

        private Callable<Void> createRequestWorker(int threadNumber) {
            return () -> {
                DatagramSocket socket = new DatagramSocket();
                socket.setSoTimeout(RESPONSE_TIMEOUT);
                int bufferSize = socket.getReceiveBufferSize();
                DatagramPacket response = new DatagramPacket(new byte[bufferSize], bufferSize);
                for (int requestIndex = 0; requestIndex < requests; requestIndex++) {
                    String query = prefix + threadNumber + "_" + requestIndex;
                    byte data[] = query.getBytes();
                    DatagramPacket request = new DatagramPacket(data, data.length, serverAddress);
                    while (!Thread.interrupted()) {
                        try {
                            socket.send(request);
                            try {
                                socket.receive(response);
                                String responseString = new String(response.getData(), response.getOffset(), response.getLength());
                                String expectedResponse = "Hello, " + query;
                                if (!expectedResponse.equals(responseString)) {
                                    continue;
                                }
                                break;
                            } catch (IOException e) {
                                System.err.println("Unable to receive packet: " + e.getMessage());
                            }
                        } catch (IOException e) {
                            System.err.println("Unable to send packet: " + e.getMessage());
                        }
                    }
                }
                socket.close();
                return null;
            };
        }
    }

    public static void main(String[] args) {
        try {
            String host = args[0];
            int port = Integer.parseInt(args[1]);
            String prefix = args[2];
            int threads = Integer.parseInt(args[3]);
            int requests = Integer.parseInt(args[4]);
            new HelloUDPClient().run(host, port, prefix, threads, requests);
        } catch (NumberFormatException | NullPointerException | ArrayIndexOutOfBoundsException e) {
            System.err.println("Wrong arguments");
        }
    }
}
