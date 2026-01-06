package com.example.ielts;

import javafx.application.Application;
import javafx.stage.Stage;

public class IELTSWritingApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("IELTS Writing Practice & Feedback");
        primaryStage.setWidth(1000);
        primaryStage.setHeight(700);
        primaryStage.setResizable(true);

        // Start with the task selection screen
        TaskSelectionScreen selectionScreen = new TaskSelectionScreen(primaryStage);
        primaryStage.setScene(selectionScreen.getScene());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
