package com.example.ielts;

import java.util.List;

public class FeedbackData {

    public double taskAchievement;
    public double coherenceCohesion;
    public double lexicalResource;
    public double grammaticalAccuracy;

    public double overallBand;

    public String question;
    public String candidateAnswer;
    public String sampleAnswer;

    public List<String> improvements;

    public void calculateOverallBand() {
        overallBand = Math.round(
                ((taskAchievement +
                        coherenceCohesion +
                        lexicalResource +
                        grammaticalAccuracy) / 4.0) * 2
        ) / 2.0;
    }
}
