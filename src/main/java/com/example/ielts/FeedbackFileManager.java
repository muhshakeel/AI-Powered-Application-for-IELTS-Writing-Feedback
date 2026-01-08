package com.example.ielts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FeedbackFileManager {

    private static final String FOLDER_NAME = "IELTS";
    private static final String FILE_NAME = "FeedbackReports.txt";

    public static void saveFeedback(FeedbackData data) {
        try {
            File file = getFile();
            String report = formatFeedback(data);

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                writer.write(report);
                writer.newLine();
            }

        } catch (IOException e) {
            System.err.println("Failed to save feedback: " + e.getMessage());
        }
    }

    private static File getFile() throws IOException {
        String userHome = System.getProperty("user.home");
        File folder = new File(userHome, FOLDER_NAME);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        File file = new File(folder, FILE_NAME);
        if (!file.exists()) {
            file.createNewFile();
        }

        return file;
    }

    private static String formatFeedback(FeedbackData d) {

        StringBuilder sb = new StringBuilder();
        sb.append("==================================================\n");
        sb.append("IELTS Writing Feedback Report\n");
        sb.append("Date: ")
                .append(LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .append("\n\n");

        sb.append("Task: ").append(d.question.contains("Task 1") ? "Task 1" : "Task 2").append("\n\n");

        sb.append("QUESTION:\n").append(d.question).append("\n\n");

        sb.append("CANDIDATE ANSWER:\n").append(d.candidateAnswer).append("\n\n");

        sb.append("BAND SCORES:\n");
        sb.append("Task Achievement: ").append(d.taskAchievement).append("\n");
        sb.append("Coherence & Cohesion: ").append(d.coherenceCohesion).append("\n");
        sb.append("Lexical Resource: ").append(d.lexicalResource).append("\n");
        sb.append("Grammatical Accuracy: ").append(d.grammaticalAccuracy).append("\n");
        sb.append("Overall Band: ").append(d.overallBand).append("\n\n");

        sb.append("AREAS FOR IMPROVEMENT:\n");
        for (int i = 0; i < d.improvements.size(); i++) {
            sb.append(i + 1).append(". ").append(d.improvements.get(i)).append("\n");
        }

        sb.append("\nSAMPLE ANSWER:\n").append(d.sampleAnswer).append("\n");
        sb.append("==================================================\n");

        return sb.toString();
    }
}
