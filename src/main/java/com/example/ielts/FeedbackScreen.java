package com.example.ielts;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.net.URL;

public class FeedbackScreen {

    private final Scene scene;
    private final VBox content = new VBox();
    private final Stage primaryStage;

    public FeedbackScreen(Stage stage, int taskNumber, String question, String answer) {
        this.primaryStage = stage;

        /* ================= ROOT ================= */

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.getStyleClass().add("root");

        /* ================= TITLE ================= */

        Label title = new Label("IELTS Writing Task " + taskNumber + " – Feedback Report");
        title.getStyleClass().add("feedback-title");
        BorderPane.setAlignment(title, Pos.CENTER);
        root.setTop(title);

        /* ================= CENTER ================= */

        content.setSpacing(20);
        content.getStyleClass().add("feedback-root");

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        root.setCenter(scrollPane);

        Label loading = new Label("Loading feedback...");
        loading.getStyleClass().add("subtitle");
        content.getChildren().add(loading);

        /* ================= BOTTOM ================= */

        HBox bottomBar = new HBox(20);
        bottomBar.getStyleClass().add("feedback-bottom-bar");

        Button homeBtn = new Button("Home");
        homeBtn.getStyleClass().add("button-primary");
        homeBtn.setOnAction(e ->
                primaryStage.setScene(
                        new TaskSelectionScreen(primaryStage).getScene()
                )
        );

        Button exitBtn = new Button("Exit");
        exitBtn.getStyleClass().add("button-danger");
        exitBtn.setOnAction(e -> primaryStage.close());

        bottomBar.getChildren().addAll(homeBtn, exitBtn);
        root.setBottom(bottomBar);

        /* ================= SCENE ================= */

        scene = new Scene(root, 1100, 750);

        /* ================= CSS ================= */

        URL css = FeedbackScreen.class.getResource(
                "/com/example/ielts/theme.css"
        );

        if (css == null) {
            System.err.println("❌ theme.css NOT FOUND");
        } else {
            scene.getStylesheets().add(css.toExternalForm());
        }

        /* ================= LOAD FEEDBACK ================= */

        new Thread(() -> {
            try {
                FeedbackData data =
                        AIService.getStructuredFeedback(taskNumber, question, answer);

                // ✅ SAVE FEEDBACK REPORT (BACKGROUND THREAD)
                FeedbackFileManager.saveFeedback(data);

                Platform.runLater(() -> renderFeedback(data));

            } catch (Exception ex) {
                Platform.runLater(() -> {
                    content.getChildren().clear();
                    Label err = new Label("Error loading feedback:\n" + ex.getMessage());
                    err.getStyleClass().add("subtitle");
                    content.getChildren().add(err);
                });
            }
        }).start();
    }

    /* =====================================================
                           RENDER UI
       ===================================================== */

    private void renderFeedback(FeedbackData d) {
        content.getChildren().clear();

        /* ===== OVERALL BAND ===== */

        Label bandTitle = new Label("Overall Band");
        bandTitle.getStyleClass().add("section-title");

        Label bandScore = new Label(String.format("%.1f", d.overallBand));
        bandScore.getStyleClass().add("overall-band-score");

        VBox overallBox = new VBox(10, bandTitle, bandScore);
        overallBox.getStyleClass().addAll("feedback-section", "overall-band");

        content.getChildren().add(overallBox);

        /* ===== CRITERIA ===== */

        content.getChildren().add(
                bandRow("Task Achievement / Response",
                        d.taskAchievement, "feedback-task"));

        content.getChildren().add(
                bandRow("Coherence & Cohesion",
                        d.coherenceCohesion, "feedback-coherence"));

        content.getChildren().add(
                bandRow("Lexical Resource",
                        d.lexicalResource, "feedback-lexical"));

        content.getChildren().add(
                bandRow("Grammatical Range & Accuracy",
                        d.grammaticalAccuracy, "feedback-grammar"));

        /* ===== QUESTION ===== */

        content.getChildren().add(
                contentSection("Question", d.question));

        /* ===== USER ANSWER ===== */

        content.getChildren().add(
                contentSection("Your Answer", d.candidateAnswer));

        /* ===== SAMPLE ANSWER ===== */

        content.getChildren().add(
                contentSection("Sample Answer (Band 8–9)", d.sampleAnswer));

        /* ===== IMPROVEMENTS ===== */

        VBox improvementsList = new VBox(6);
        for (String s : d.improvements) {
            Label l = new Label("• " + s);
            l.getStyleClass().add("improvement-item");
            improvementsList.getChildren().add(l);
        }

        Label impTitle = new Label("Areas for Improvement");
        impTitle.getStyleClass().add("section-title");

        VBox improvementBox = new VBox(10, impTitle, improvementsList);
        improvementBox.getStyleClass().addAll("feedback-section", "feedback-sample");

        content.getChildren().add(improvementBox);
    }

    /* =====================================================
                           HELPERS
       ===================================================== */

    private VBox bandRow(String title, double score, String colorClass) {
        Label label = new Label(title);
        label.getStyleClass().add("band-label");

        Label scoreLabel = new Label(String.format("%.1f", score));
        scoreLabel.getStyleClass().add("band-score");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(10, label, spacer, scoreLabel);
        row.getStyleClass().addAll("band-row", "feedback-section", colorClass);

        return new VBox(row);
    }

    private VBox contentSection(String titleText, String body) {
        Label title = new Label(titleText);
        title.getStyleClass().add("section-title");

        Label text = new Label(body);
        text.getStyleClass().add("feedback-content-text");
        text.setWrapText(true);

        VBox box = new VBox(10, title, text);
        box.getStyleClass().addAll("feedback-content", "feedback-section");

        return box;
    }

    public Scene getScene() {
        return scene;
    }
}
