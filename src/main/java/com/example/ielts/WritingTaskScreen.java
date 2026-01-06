package com.example.ielts;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

public class WritingTaskScreen {

    private Scene scene;
    private Stage primaryStage;
    private int taskNumber;

    private TextArea writingArea;
    private Label timerLabel;
    private Label wordCountLabel;
    private Label questionLabel;

    private Timeline timeline;
    private int timeInSeconds;
    private int minWords;
    private String currentQuestion = "";

    public WritingTaskScreen(Stage primaryStage, int taskNumber) {
        this.primaryStage = primaryStage;
        this.taskNumber = taskNumber;
        this.timeInSeconds = taskNumber == 1 ? 1200 : 2400;
        this.minWords = taskNumber == 1 ? 150 : 250;

        createScene();
        generateQuestion();
    }

    private void createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f6f8;");

        root.setTop(createTopBar());
        root.setCenter(createCenterSection());
        root.setBottom(createBottomSection());

        scene = new Scene(root, 1100, 750);
        startTimer();
    }

    /* ================= TOP BAR ================= */

    private HBox createTopBar() {
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0;");

        Button backBtn = new Button("← Back");
        backBtn.setStyle(
                "-fx-font-family: 'Times New Roman';" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-color: transparent;" +
                        "-fx-cursor: hand;"
        );
        backBtn.setOnAction(e -> goBack());

        Label taskLabel = new Label("Task " + taskNumber);
        taskLabel.setStyle(
                "-fx-font-family: 'Times New Roman';" +
                        "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;"
        );

        wordCountLabel = new Label("0 / " + minWords + " words");
        wordCountLabel.setStyle(
                "-fx-font-family: 'Times New Roman';" +
                        "-fx-font-size: 15px;" +
                        "-fx-text-fill: #e67e22;"
        );

        timerLabel = new Label(formatTime(timeInSeconds));
        timerLabel.setStyle(
                "-fx-font-family: 'Times New Roman';" +
                        "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #27ae60;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(backBtn, taskLabel, wordCountLabel, spacer, timerLabel);
        return topBar;
    }

    /* ================= CENTER ================= */

    private VBox createCenterSection() {
        VBox center = new VBox(15);
        center.setPadding(new Insets(20));

        Label questionTitle = new Label("Question");
        questionTitle.setStyle(
                "-fx-font-family: 'Times New Roman';" +
                        "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;"
        );

        questionLabel = new Label("Loading...");
        questionLabel.setWrapText(true);
        questionLabel.setStyle(
                "-fx-font-family: 'Times New Roman';" +
                        "-fx-font-size: 15px;" +
                        "-fx-background-color: white;" +
                        "-fx-padding: 15;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;"
        );

        ScrollPane questionPane = new ScrollPane(questionLabel);
        questionPane.setFitToWidth(true);
        questionPane.setPrefHeight(160);
        questionPane.setStyle("-fx-background-color: transparent;");

        writingArea = new TextArea();
        writingArea.setPromptText("Start writing your response here...");
        writingArea.setWrapText(true);
        writingArea.setStyle(
                "-fx-font-family: 'Times New Roman';" +
                        "-fx-font-size: 15px;" +
                        "-fx-background-color: white;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-radius: 8;"
        );

        writingArea.textProperty().addListener((obs, o, n) -> updateWordCount());

        VBox.setVgrow(writingArea, Priority.ALWAYS);
        center.getChildren().addAll(questionTitle, questionPane, writingArea);
        return center;
    }

    /* ================= BOTTOM ================= */

    private HBox createBottomSection() {
        HBox bottom = new HBox();
        bottom.setPadding(new Insets(15));
        bottom.setAlignment(Pos.CENTER_RIGHT);
        bottom.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0;");

        Button submit = new Button("Submit for Feedback");
        submit.setStyle(
                "-fx-font-family: 'Times New Roman';" +
                        "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-color: #f5c16c;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;"
        );
        submit.setOnAction(e -> submitAnswer());

        bottom.getChildren().add(submit);
        return bottom;
    }

    /* ================= LOGIC ================= */

    private void updateWordCount() {
        String text = writingArea.getText().trim();
        int count = text.isEmpty() ? 0 : text.split("\\s+").length;

        wordCountLabel.setText(count + " / " + minWords + " words");

        if (count < minWords) {
            wordCountLabel.setStyle("-fx-font-family: 'Times New Roman'; -fx-font-size: 15px; -fx-text-fill: #e74c3c;");
        } else {
            wordCountLabel.setStyle("-fx-font-family: 'Times New Roman'; -fx-font-size: 15px; -fx-text-fill: #27ae60;");
        }
    }

    private void startTimer() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeInSeconds--;
            timerLabel.setText(formatTime(timeInSeconds));

            if (timeInSeconds <= 60) {
                timerLabel.setStyle("-fx-font-family: 'Times New Roman'; -fx-font-size: 18px; -fx-text-fill: red;");
            }

            if (timeInSeconds <= 0) {
                timeline.stop();
                submitAnswer();
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private String formatTime(int s) {
        return String.format("%02d:%02d", s / 60, s % 60);
    }

    private void generateQuestion() {
        new Thread(() -> {
            try {
                String q = AIService.generateQuestion(taskNumber);
                currentQuestion = q;
                javafx.application.Platform.runLater(() -> questionLabel.setText(q));
            } catch (Exception e) {
                javafx.application.Platform.runLater(() ->
                        questionLabel.setText("Failed to load question."));
            }
        }).start();
    }

    private void submitAnswer() {
        timeline.stop();
        FeedbackScreen fs = new FeedbackScreen(primaryStage, taskNumber, currentQuestion, writingArea.getText());
        primaryStage.setScene(fs.getScene());
    }

    private void goBack() {
        timeline.stop();
        primaryStage.setScene(new TaskSelectionScreen(primaryStage).getScene());
    }

    public Scene getScene() {
        return scene;
    }
}
