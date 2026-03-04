package com.chat;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ServerUI extends Application {

    private ObservableList<String> userList = FXCollections.observableArrayList();
    private TextArea logArea;

    @Override
    public void start(Stage stage) {

        stage.setTitle("Server Dashboard");

        logArea = new TextArea();
        logArea.setEditable(false);

        ListView<String> usersView = new ListView<>(userList);

        VBox rightPanel = new VBox(usersView);
        rightPanel.setPrefWidth(150);

        BorderPane root = new BorderPane();
        root.setCenter(logArea);
        root.setRight(rightPanel);

        Scene scene = new Scene(root, 600, 400);
        stage.setScene(scene);
        stage.show();

        startServer();
    }

    private void startServer() {

        new Thread(() -> {

            ServerModel server = new ServerModel(3000);

            server.setOnLog(message ->
                    Platform.runLater(() ->
                            logArea.appendText(message + "\n")));

            server.setOnUserConnected(username ->
                    Platform.runLater(() ->
                            userList.add(username)));

            server.setOnUserDisconnected(username ->
                    Platform.runLater(() ->
                            userList.remove(username)));

            server.startServer();

        }).start();
    }

    public static void main(String[] args) {
        launch();
    }
}