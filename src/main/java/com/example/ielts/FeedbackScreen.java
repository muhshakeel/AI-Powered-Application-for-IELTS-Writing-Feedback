package com.example.ielts;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class FeedbackScreen {
    private Scene scene;
    private Stage primaryStage;
    private int taskNumber;
    private String question;
    private String answer;
    private ScrollPane scrollPane;
    private VBox contentBox;
    private Label loadingLabel;

    public FeedbackScreen(Stage primaryStage, int taskNumber, String question, String answer) {
        this.primaryStage = primaryStage;
        this.taskNumber = taskNumber;
        this.question = question;
        this.answer = answer;
        createScene();
        getFeedback();
    }

    private void createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #ecf0f1;");

        // Top Section
        VBox topSection = createTopSection();
        root.setTop(topSection);

        // Center Section - Scrollable Content
        contentBox = new VBox(20);
        contentBox.setPadding(new Insets(20));
        contentBox.setAlignment(Pos.TOP_CENTER);

        loadingLabel = new Label("Analyzing your writing...\nPlease wait, this may take a moment.");
        loadingLabel.setStyle(
                "-fx-font-size: 18px; " +
                        "-fx-text-fill: #7f8c8d; " +
                        "-fx-text-alignment: center;"
        );

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(80, 80);

        VBox loadingBox = new VBox(20, progressIndicator, loadingLabel);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPadding(new Insets(100));

        contentBox.getChildren().add(loadingBox);

        scrollPane = new ScrollPane(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        root.setCenter(scrollPane);

        // Bottom Section
        HBox bottomSection = createBottomSection();
        root.setBottom(bottomSection);

        scene = new Scene(root, 1000, 700);
    }

    private VBox createTopSection() {
        VBox topBox = new VBox(10);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(15));
        topBox.setStyle("-fx-background-color: #34495e;");

        Label titleLabel = new Label("IELTS Writing Task " + taskNumber + " - Feedback Report");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        topBox.getChildren().add(titleLabel);
        return topBox;
    }

    private HBox createBottomSection() {
        HBox bottomBox = new HBox(20);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(15));
        bottomBox.setStyle("-fx-background-color: #34495e;");

        Button newTaskButton = new Button("Try Another Task");
        newTaskButton.setPrefWidth(200);
        newTaskButton.setPrefHeight(40);
        newTaskButton.setStyle(
                "-fx-background-color: #27ae60; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 5; " +
                        "-fx-cursor: hand;"
        );
        newTaskButton.setOnAction(e -> goToSelection());

        Button exitButton = new Button("Exit");
        exitButton.setPrefWidth(120);
        exitButton.setPrefHeight(40);
        exitButton.setStyle(
                "-fx-background-color: #e74c3c; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 5; " +
                        "-fx-cursor: hand;"
        );
        exitButton.setOnAction(e -> primaryStage.close());

        bottomBox.getChildren().addAll(newTaskButton, exitButton);
        return bottomBox;
    }

    private void getFeedback() {
        new Thread(() -> {
            try {
                String feedback = AIService.getFeedback(taskNumber, question, answer);

                javafx.application.Platform.runLater(() -> {
                    displayFeedback(feedback);
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    showError("Failed to get feedback: " + e.getMessage());
                });
            }
        }).start();
    }

    private void displayFeedback(String feedback) {
        contentBox.getChildren().clear();

        // Parse the feedback and create sections
        VBox feedbackContent = new VBox(15);
        feedbackContent.setPadding(new Insets(20));
        feedbackContent.setMaxWidth(900);

        // Original Question
        feedbackContent.getChildren().add(createSection("Original Question", question, "#3498db"));

        // Your Answer
        feedbackContent.getChildren().add(createSection("Your Answer", answer, "#95a5a6"));

        // AI Feedback - parse and display
        String[] sections = feedback.split("\n\n");
        for (String section : sections) {
            if (!section.trim().isEmpty()) {
                // Try to identify section type
                if (section.contains("BAND SCORE") || section.contains("Band Score")) {
                    feedbackContent.getChildren().add(createSection("Band Score", section, "#e74c3c"));
                } else if (section.contains("TASK ACHIEVEMENT") || section.contains("Task Achievement") ||
                        section.contains("TASK RESPONSE") || section.contains("Task Response")) {
                    feedbackContent.getChildren().add(createSection("Task Achievement/Response", section, "#9b59b6"));
                } else if (section.contains("COHERENCE") || section.contains("Coherence")) {
                    feedbackContent.getChildren().add(createSection("Coherence & Cohesion", section, "#3498db"));
                } else if (section.contains("LEXICAL") || section.contains("Lexical")) {
                    feedbackContent.getChildren().add(createSection("Lexical Resource", section, "#f39c12"));
                } else if (section.contains("GRAMMATICAL") || section.contains("Grammatical")) {
                    feedbackContent.getChildren().add(createSection("Grammatical Range & Accuracy", section, "#1abc9c"));
                } else if (section.contains("IMPROVEMENT") || section.contains("Improvement")) {
                    feedbackContent.getChildren().add(createSection("Areas for Improvement", section, "#e67e22"));
                } else if (section.contains("SAMPLE") || section.contains("Sample")) {
                    feedbackContent.getChildren().add(createSection("Sample Answer", section, "#27ae60"));
                } else {
                    feedbackContent.getChildren().add(createSection("Feedback", section, "#7f8c8d"));
                }
            }
        }

        contentBox.getChildren().add(feedbackContent);
    }

    private VBox createSection(String title, String content, String color) {
        VBox section = new VBox(10);
        section.setStyle(
                "-fx-background-color: white; " +
                        "-fx-padding: 20; " +
                        "-fx-border-color: " + color + "; " +
                        "-fx-border-width: 0 0 0 5; " +
                        "-fx-background-radius: 5; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
        );

        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-font-size: 18px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: " + color + ";"
        );

        Label contentLabel = new Label(content);
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(850);
        contentLabel.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-text-fill: #2c3e50; " +
                        "-fx-line-spacing: 2px;"
        );

        section.getChildren().addAll(titleLabel, contentLabel);
        return section;
    }

    private void goToSelection() {
        TaskSelectionScreen selectionScreen = new TaskSelectionScreen(primaryStage);
        primaryStage.setScene(selectionScreen.getScene());
    }

    private void showError(String message) {
        contentBox.getChildren().clear();

        Label errorLabel = new Label("Error: " + message);
        errorLabel.setStyle(
                "-fx-font-size: 16px; " +
                        "-fx-text-fill: #e74c3c; " +
                        "-fx-padding: 50;"
        );
        errorLabel.setWrapText(true);

        contentBox.getChildren().add(errorLabel);
    }

    public Scene getScene() {
        return scene;
    }
}
