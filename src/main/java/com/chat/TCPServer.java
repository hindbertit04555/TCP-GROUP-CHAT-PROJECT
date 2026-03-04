package com.chat;

import java.io.InputStream;
import java.util.Properties;

public class TCPServer {

    public static void main(String[] args) {

        try {
            // Load properties file
            Properties properties = new Properties();

            InputStream input =
                    TCPServer.class.getClassLoader()
                            .getResourceAsStream("server.properties");

            if (input == null) {
                System.out.println("server.properties file not found!");
                return;
            }

            properties.load(input);

            // Read port from properties
            int port = Integer.parseInt(
                    properties.getProperty("server.port")
            );

            System.out.println("Loaded port from config file: " + port);

            ServerModel server = new ServerModel(port);
            server.startServer();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}