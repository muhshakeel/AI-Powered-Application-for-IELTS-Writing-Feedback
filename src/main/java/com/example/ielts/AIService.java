package com.example.ielts;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class AIService {

    private static final String API_KEY = System.getenv("GROQ_API_KEY");
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.3-70b-versatile";

    /* =========================================================
       QUESTION GENERATION
       ========================================================= */

    public static String generateQuestion(int taskNumber) throws Exception {

        String prompt = (taskNumber == 1)
                ? """
                Generate a realistic IELTS Writing Task 1 question.
                Use official IELTS wording.
                Start with: "You should spend about 20 minutes on this task."
                """
                : """
                Generate a realistic IELTS Writing Task 2 question.
                Use official IELTS wording.
                Start with: "You should spend about 40 minutes on this task."
                """;

        JSONObject body = new JSONObject();
        body.put("model", MODEL);
        body.put("temperature", 0.7);
        body.put("max_tokens", 500);

        JSONArray messages = new JSONArray();
        messages.put(new JSONObject()
                .put("role", "user")
                .put("content", prompt));

        body.put("messages", messages);

        return callAPI(body);
    }

    /* =========================================================
       STRUCTURED FEEDBACK + SAMPLE ANSWER
       ========================================================= */

    public static FeedbackData getStructuredFeedback(
            int taskNumber,
            String question,
            String answer
    ) throws Exception {

        String taskType = (taskNumber == 1) ? "Task 1" : "Task 2";

        String sampleInstruction = (taskNumber == 1)
                ? "Write a Band 8–9 IELTS Task 1 model answer (minimum 150 words)."
                : "Write a Band 8–9 IELTS Task 2 model essay (minimum 250 words).";

        String prompt = """
        You are an official IELTS examiner.

        Evaluate the following IELTS Writing %s response.

        VERY IMPORTANT RULES:
        - Output MUST be valid JSON only
        - Do NOT include explanations
        - Do NOT include markdown
        - Do NOT include extra text

        JSON FORMAT (STRICT):

        {
          "taskAchievement": 0.0,
          "coherenceCohesion": 0.0,
          "lexicalResource": 0.0,
          "grammaticalAccuracy": 0.0,
          "sampleAnswer": "",
          "areasOfImprovement": [
            "",
            "",
            ""
          ]
        }

        SCORING:
        - Use IELTS band descriptors
        - Scores must be in 0.5 increments (0.0–9.0)

        SAMPLE ANSWER REQUIREMENT:
        %s

        QUESTION:
        %s

        CANDIDATE ANSWER:
        %s
        """.formatted(taskType, sampleInstruction, question, answer);

        JSONObject body = new JSONObject();
        body.put("model", MODEL);
        body.put("temperature", 0.2);
        body.put("max_tokens", 3000);

        JSONArray messages = new JSONArray();
        messages.put(new JSONObject()
                .put("role", "user")
                .put("content", prompt));

        body.put("messages", messages);

        String response = callAPI(body);

        // --- Parse strict JSON ---
        JSONObject json = new JSONObject(response);

        FeedbackData data = new FeedbackData();
        data.taskAchievement = json.getDouble("taskAchievement");
        data.coherenceCohesion = json.getDouble("coherenceCohesion");
        data.lexicalResource = json.getDouble("lexicalResource");
        data.grammaticalAccuracy = json.getDouble("grammaticalAccuracy");
        data.sampleAnswer = json.getString("sampleAnswer");

        JSONArray imp = json.getJSONArray("areasOfImprovement");
        data.improvements = new ArrayList<>();
        for (int i = 0; i < imp.length(); i++) {
            data.improvements.add(imp.getString(i));
        }

        data.question = question;
        data.candidateAnswer = answer;
        data.calculateOverallBand();

        return data;
    }

    /* =========================================================
       LOW-LEVEL API CALL
       ========================================================= */

    private static String callAPI(JSONObject body) throws Exception {

        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.toString().getBytes());
        }

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("API error: " + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream())
        );

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);

        JSONObject result = new JSONObject(sb.toString());
        return result.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");
    }
}
