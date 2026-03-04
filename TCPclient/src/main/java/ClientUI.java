package com.chat;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

public class ClientUI extends Application {

    private TextArea chatArea;
    private TextField messageField;
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;

    private boolean connected = false;

    @Override
    public void start(Stage stage) {

        stage.setTitle("Chat Client");

        chatArea = new TextArea();
        chatArea.setEditable(false);

        messageField = new TextField();
        messageField.setPromptText("Type your message...");

        Button sendButton = new Button("Send");

        sendButton.setOnAction(e -> sendMessage());
        messageField.setOnAction(e -> sendMessage());

        HBox bottomBox = new HBox(10, messageField, sendButton);

        Label statusLabel = new Label("Offline");
        statusLabel.setStyle("-fx-text-fill: red;");

        HBox topBox = new HBox(statusLabel);

        BorderPane root = new BorderPane();
        root.setTop(topBox);
        root.setCenter(chatArea);
        root.setBottom(bottomBox);

        Scene scene = new Scene(root, 500, 400);
        stage.setScene(scene);
        stage.show();

        connectToServer(statusLabel);

        stage.setOnCloseRequest(e -> disconnect());
    }

    private void connectToServer(Label statusLabel) {

        try {
            socket = new Socket("localhost", 3000);

            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            connected = true;

            statusLabel.setText("Online");
            statusLabel.setStyle("-fx-text-fill: green;");

            askUsername();

            Thread receiveThread = new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {

                        String finalMessage = message;

                        Platform.runLater(() -> {
                            chatArea.appendText(finalMessage + "\n");

                            if (finalMessage.contains("READ-ONLY")) {
                                messageField.setDisable(true);
                            }
                        });
                    }

                } catch (IOException e) {
                    Platform.runLater(() ->
                            chatArea.appendText("Disconnected from server.\n"));
                } finally {
                    disconnect();
                }
            });

            receiveThread.setDaemon(true);
            receiveThread.start();

        } catch (IOException e) {
            chatArea.appendText("Unable to connect to server.\n");
        }
    }

    private void askUsername() {

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Username");
        dialog.setHeaderText("Enter your username:");

        dialog.showAndWait().ifPresentOrElse(username -> {

            if (username.isBlank()) {
                out.println(""); // triggers read-only mode
            } else {
                out.println(username);
            }

        }, () -> out.println("")); // If user cancels
    }

    private void sendMessage() {

        if (!connected) return;

        String message = messageField.getText();

        if (!message.isBlank()) {
            out.println(message);
            messageField.clear();
        }
    }

    private void disconnect() {

        connected = false;

        try {
            if (out != null) out.println("bye");
            if (socket != null && !socket.isClosed())
                socket.close();
        } catch (IOException ignored) {
        }
    }

    public static void main(String[] args) {
        launch();
    }
}