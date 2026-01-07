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
    private ProgressBar wordProgressBar;

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
        root.getStyleClass().add("app-root");

        root.setTop(createTopBar());
        root.setCenter(createScrollableContent());

        scene = new Scene(root, 1100, 750);
        scene.getStylesheets().add(
                getClass().getResource("/com/example/ielts/theme.css").toExternalForm()
        );

        startTimer();
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(30);
        topBar.setAlignment(Pos.CENTER);
        topBar.getStyleClass().add("top-bar");

        Button backBtn = new Button("← Back");
        backBtn.getStyleClass().add("button-outline");
        backBtn.setOnAction(e -> goBack());

        Label taskLabel = new Label("Task " + taskNumber);
        taskLabel.getStyleClass().add("section-title");

        HBox leftBox = new HBox(12, backBtn, taskLabel);
        leftBox.setAlignment(Pos.CENTER_LEFT);

        timerLabel = new Label(formatTime(timeInSeconds));
        timerLabel.getStyleClass().add("timer-text");

        StackPane timerBox = new StackPane(timerLabel);
        timerBox.getStyleClass().add("timer-box");

        wordProgressBar = new ProgressBar(0);
        wordProgressBar.setPrefWidth(220);
        wordProgressBar.getStyleClass().addAll("word-bar", "word-bar-low");

        wordCountLabel = new Label("0 / " + minWords + " words");
        wordCountLabel.getStyleClass().add("word-low");

        HBox wordBox = new HBox(10, wordProgressBar, wordCountLabel);
        wordBox.setAlignment(Pos.CENTER_RIGHT);

        Region spacerLeft = new Region();
        Region spacerRight = new Region();
        HBox.setHgrow(spacerLeft, Priority.ALWAYS);
        HBox.setHgrow(spacerRight, Priority.ALWAYS);

        topBar.getChildren().addAll(leftBox, spacerLeft, timerBox, spacerRight, wordBox);
        return topBar;
    }

    private ScrollPane createScrollableContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setFillWidth(true); // ✅ KEY LINE

        Label questionTitle = new Label("Question");
        questionTitle.getStyleClass().add("section-title");

        questionLabel = new Label("Loading...");
        questionLabel.setWrapText(true);
        questionLabel.setMaxWidth(Double.MAX_VALUE); // ✅ ALLOW FULL WIDTH
        questionLabel.getStyleClass().add("card");

        writingArea = new TextArea();
        writingArea.setPromptText("Start writing your response here...");
        writingArea.setWrapText(true);
        writingArea.setPrefHeight(450);
        writingArea.textProperty().addListener((obs, o, n) -> updateWordCount());

        Button submit = new Button("Submit for Feedback");
        submit.getStyleClass().add("button-accent");
        submit.setPrefWidth(260);
        submit.setPrefHeight(48);
        submit.setOnAction(e -> submitAnswer());

        HBox submitBox = new HBox(submit);
        submitBox.setAlignment(Pos.CENTER);
        submitBox.setPadding(new Insets(40, 0, 20, 0));

        content.getChildren().addAll(
                questionTitle,
                questionLabel,
                writingArea,
                submitBox
        );

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.getStyleClass().add("transparent-scroll");

        return scrollPane;
    }

    private void updateWordCount() {
        String text = writingArea.getText().trim();
        int count = text.isEmpty() ? 0 : text.split("\\s+").length;

        double progress = Math.min(1.0, (double) count / minWords);
        wordProgressBar.setProgress(progress);

        wordCountLabel.setText(count + " / " + minWords + " words");

        wordProgressBar.getStyleClass().removeAll("word-bar-low", "word-bar-ok");
        wordCountLabel.getStyleClass().removeAll("word-low", "word-ok");

        if (count < minWords) {
            wordProgressBar.getStyleClass().add("word-bar-low");
            wordCountLabel.getStyleClass().add("word-low");
        } else {
            wordProgressBar.getStyleClass().add("word-bar-ok");
            wordCountLabel.getStyleClass().add("word-ok");
        }
    }

    private void startTimer() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeInSeconds--;
            timerLabel.setText(formatTime(timeInSeconds));
            if (timeInSeconds <= 0) submitAnswer();
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
        FeedbackScreen fs = new FeedbackScreen(
                primaryStage,
                taskNumber,
                currentQuestion,
                writingArea.getText()
        );
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
