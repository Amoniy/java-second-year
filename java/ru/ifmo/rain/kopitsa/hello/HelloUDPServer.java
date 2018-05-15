package ru.ifmo.rain.kopitsa.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class HelloUDPServer implements HelloServer {

    private BlockingQueue<ServerManager> runningManagers;

    @Override
    public void start(int port, int threads) {
        try {
            ServerManager manager = new ServerManager(port, threads);
            runningManagers.add(manager);
            manager.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        runningManagers.forEach(HelloUDPServer.ServerManager::close);
    }

    public HelloUDPServer() {
        runningManagers = new LinkedBlockingQueue<>();
    }

    private static class ServerManager {

        private DatagramSocket socket;
        private ExecutorService responsePool;
        private int bufferSize;
        private int threads;

        ServerManager(int port, int threads) throws IOException {
            this.threads = threads;
            responsePool = Executors.newFixedThreadPool(threads);
            socket = new DatagramSocket(port);
            bufferSize = socket.getReceiveBufferSize();
        }

        void start() {
            List<Callable<Void>> responseWorkers = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                responseWorkers.add(createResponseWorker());
            }
            responseWorkers.forEach(task -> responsePool.submit(task));
        }

        private Callable<Void> createResponseWorker() {
            return () -> {
                DatagramPacket request = new DatagramPacket(new byte[bufferSize], bufferSize);
                while (!Thread.interrupted()) {
                    try {
                        socket.receive(request);
                        String requestString = new String(request.getData(), request.getOffset(), request.getLength());
                        String responseString = "Hello, " + requestString;

                        byte[] sendData = responseString.getBytes();
                        DatagramPacket response = new DatagramPacket(sendData, sendData.length,
                                request.getAddress(), request.getPort());
                        try {
                            socket.send(response);
                        } catch (IOException e) {
                            System.err.println("Unable to send response: " + e.getMessage());
                        }
                    } catch (IOException e) {
                        System.err.println("Unable to receive request: " + e.getMessage());
                    }
                }
                return null;
            };
        }

        void close() {
            responsePool.shutdownNow();
            socket.close();
        }
    }

    public static void main(String[] args) {
        try {
            int port = Integer.parseInt(args[0]);
            int threads = Integer.parseInt(args[1]);
            try (HelloServer server = new HelloUDPServer()) {
                server.start(port, threads);
            }
        } catch (NumberFormatException | NullPointerException | ArrayIndexOutOfBoundsException e) {
            System.err.println("Wrong arguments");
        }
    }
}
