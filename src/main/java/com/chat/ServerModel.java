package com.chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ServerModel {

    private int port;
    private ServerSocket serverSocket;
    private boolean running = false;

    private Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();

    // UI Callbacks
    private Consumer<String> onLog;
    private Consumer<String> onUserConnected;
    private Consumer<String> onUserDisconnected;

    public ServerModel(int port) {
        this.port = port;
    }

    // =========================
    // Callback Setters
    // =========================
    public void setOnLog(Consumer<String> onLog) {
        this.onLog = onLog;
    }

    public void setOnUserConnected(Consumer<String> onUserConnected) {
        this.onUserConnected = onUserConnected;
    }

    public void setOnUserDisconnected(Consumer<String> onUserDisconnected) {
        this.onUserDisconnected = onUserDisconnected;
    }

    // =========================
    // Start Server
    // =========================
    public void startServer() {

        try {
            serverSocket = new ServerSocket(port);
            running = true;

            log("=================================");
            log("Server Started on port " + port);
            log("Waiting for clients...");
            log("=================================");

            while (running) {

                Socket clientSocket = serverSocket.accept();

                log("New client connected: "
                        + clientSocket.getInetAddress());

                ClientHandler handler =
                        new ClientHandler(clientSocket, this);

                clients.add(handler);

                new Thread(handler).start();
            }

        } catch (IOException e) {
            log("Server error: " + e.getMessage());
        }
    }

    // =========================
    // Stop Server
    // =========================
    public void stopServer() {
        running = false;

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }

            for (ClientHandler client : clients) {
                client.closeConnection();
            }

            log("Server stopped.");

        } catch (IOException e) {
            log("Error stopping server: " + e.getMessage());
        }
    }

    // =========================
    // Logging
    // =========================
    public void log(String message) {
        if (onLog != null) {
            onLog.accept(message);
        } else {
            System.out.println(message);
        }
    }

    // =========================
    // Messaging
    // =========================
    public void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    // =========================
    // Client Management
    // =========================
    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    public void notifyUserConnected(String username) {
        log(username + " joined the chat.");
        if (onUserConnected != null) {
            onUserConnected.accept(username);
        }
    }

    public void notifyUserDisconnected(String username) {
        log(username + " left the chat.");
        if (onUserDisconnected != null) {
            onUserDisconnected.accept(username);
        }
    }

    public String getAllUsers() {
        StringBuilder sb = new StringBuilder();

        for (ClientHandler client : clients) {
            if (client.getUsername() != null) {
                sb.append(client.getUsername()).append("\n");
            }
        }

        return sb.toString();
    }
}