package com.example.ielts;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

public class AIService {
    private static final String API_KEY = "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";
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    // Using a more reliable model that's available on Groq
    private static final String MODEL = "llama-3.3-70b-versatile";

    public static String generateQuestion(int taskNumber) throws Exception {
        String prompt;

        if (taskNumber == 1) {
            prompt = "Generate a realistic IELTS Writing Task 1 question. " +
                    "It should describe a chart, graph, table, diagram, or process that needs to be summarized. " +
                    "Format it exactly as it would appear in an IELTS exam. " +
                    "Include specific data points and clear instructions. " +
                    "Start with 'You should spend about 20 minutes on this task.' " +
                    "Make it challenging but fair for an IELTS candidate.";
        } else {
            prompt = "Generate a realistic IELTS Writing Task 2 essay question. " +
                    "It should be an opinion, discussion, advantage/disadvantage, or problem-solution type question. " +
                    "Format it exactly as it would appear in an IELTS exam. " +
                    "Start with 'You should spend about 40 minutes on this task.' " +
                    "The topic should be relevant, thought-provoking, and appropriate for academic writing. " +
                    "Include clear instructions about what the candidate should discuss.";
        }

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", MODEL);

        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", prompt);
        messages.put(message);

        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 500);

        return makeAPIRequest(requestBody);
    }

    public static String getFeedback(int taskNumber, String question, String answer) throws Exception {
        String taskType = taskNumber == 1 ? "Task 1" : "Task 2";
        int minWords = taskNumber == 1 ? 150 : 250;

        String prompt = String.format(
                "You are an expert IELTS examiner. Evaluate this IELTS Writing %s response according to official IELTS band descriptors.\n\n" +
                        "QUESTION:\n%s\n\n" +
                        "CANDIDATE'S ANSWER:\n%s\n\n" +
                        "Provide detailed feedback in the following format:\n\n" +
                        "OVERALL BAND SCORE: [X.X]\n\n" +
                        "TASK ACHIEVEMENT/RESPONSE: [Band X.X]\n" +
                        "- Detailed evaluation based on official descriptors\n" +
                        "- Specific strengths and weaknesses\n\n" +
                        "COHERENCE & COHESION: [Band X.X]\n" +
                        "- Evaluation of organization and linking\n" +
                        "- Comment on paragraph structure and flow\n\n" +
                        "LEXICAL RESOURCE: [Band X.X]\n" +
                        "- Evaluation of vocabulary range and accuracy\n" +
                        "- Comment on word choice and collocations\n\n" +
                        "GRAMMATICAL RANGE & ACCURACY: [Band X.X]\n" +
                        "- Evaluation of sentence structures\n" +
                        "- Comment on grammar accuracy and variety\n\n" +
                        "AREAS FOR IMPROVEMENT:\n" +
                        "1. Specific actionable suggestion\n" +
                        "2. Specific actionable suggestion\n" +
                        "3. Specific actionable suggestion\n\n" +
                        "SAMPLE ANSWER (Band 8-9 level):\n" +
                        "Provide a model answer that demonstrates excellent use of all criteria. Write a complete, well-structured response that would achieve Band 8-9.\n\n" +
                        "Word count requirement: minimum %d words. Actual word count: %d words.\n" +
                        "Be specific, constructive, and reference the official IELTS band descriptors in your evaluation.",
                taskType, question, answer, minWords, countWords(answer)
        );

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", MODEL);

        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", prompt);
        messages.put(message);

        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.3);
        requestBody.put("max_tokens", 3000);

        return makeAPIRequest(requestBody);
    }

    private static String makeAPIRequest(JSONObject requestBody) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
        conn.setDoOutput(true);
        conn.setConnectTimeout(30000); // 30 seconds
        conn.setReadTimeout(30000);

        // Send request
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // Read response
        int responseCode = conn.getResponseCode();

        if (responseCode != 200) {
            // Read error response
            StringBuilder errorResponse = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream(), "utf-8"))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    errorResponse.append(responseLine.trim());
                }
            } catch (Exception e) {
                // If error stream is not available
            }

            String errorMsg = "API request failed with code: " + responseCode;
            if (errorResponse.length() > 0) {
                errorMsg += "\nDetails: " + errorResponse.toString();
            }
            throw new Exception(errorMsg);
        }

        // Read success response
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "utf-8"))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }

        // Parse response
        JSONObject jsonResponse = new JSONObject(response.toString());

        // Check if response has the expected structure
        if (!jsonResponse.has("choices")) {
            throw new Exception("Invalid API response: missing 'choices' field");
        }

        JSONArray choices = jsonResponse.getJSONArray("choices");
        if (choices.length() == 0) {
            throw new Exception("Invalid API response: empty choices array");
        }

        JSONObject firstChoice = choices.getJSONObject(0);
        JSONObject message = firstChoice.getJSONObject("message");
        String content = message.getString("content");

        return content;
    }

    private static int countWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        return text.trim().split("\\s+").length;
    }
}
