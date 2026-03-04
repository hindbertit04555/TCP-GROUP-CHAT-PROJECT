package com.chat;

import java.io.*;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ClientHandler implements Runnable {

    private Socket socket;
    private ServerModel server;

    private BufferedReader in;
    private PrintWriter out;

    private String username;
    private boolean running = true;

    public ClientHandler(Socket socket, ServerModel server) {
        this.socket = socket;
        this.server = server;

        try {
            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            out = new PrintWriter(
                    socket.getOutputStream(), true);

        } catch (IOException e) {
            server.log("Error creating client streams: " + e.getMessage());
        }
    }

    @Override
    public void run() {

        try {

            // First message must be username
            username = in.readLine();

            if (username == null || username.isBlank()) {
                username = null;
                out.println("READ-ONLY MODE ENABLED");
                server.log("A client joined in read-only mode.");
            } else {
                server.log("User connected: " + username);
                server.notifyUserConnected(username);
            }

            String message;

            while (running && (message = in.readLine()) != null) {

                if (message.equalsIgnoreCase("bye") ||
                        message.equalsIgnoreCase("end")) {
                    break;
                }

                if (message.equalsIgnoreCase("allUsers")) {
                    out.println("Active Users:\n" +
                            server.getAllUsers());
                    continue;
                }

                // Only broadcast if not read-only
                if (username != null) {

                    String time = LocalTime.now()
                            .format(DateTimeFormatter.ofPattern("HH:mm"));

                    String formatted =
                            "[" + time + "] "
                                    + username
                                    + ": "
                                    + message;

                    // Log to server UI
                    server.log(formatted);

                    // Broadcast to other clients
                    server.broadcast(formatted, this);
                }
            }

        } catch (IOException e) {
            server.log("Connection lost: " + username);
        } finally {
            closeConnection();
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public String getUsername() {
        return username;
    }

    public void closeConnection() {

        running = false;

        try {

            server.removeClient(this);

            if (username != null) {
                server.notifyUserDisconnected(username);
                server.log("User disconnected: " + username);
            }

            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed())
                socket.close();

        } catch (IOException e) {
            server.log("Error closing connection: " + e.getMessage());
        }
    }
}