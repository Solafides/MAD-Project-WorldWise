package com.example.quizproject;

import java.util.ArrayList;

public class Constants {

    public static final String USER_NAME = "user_name";
    public static final String TOTAL_QUESTIONS = "total_questions";
    public static final String CORRECT_ANSWERS = "correct_answers";
    
    // New constants for modes
    public static final String QUIZ_MODE = "quiz_mode";
    public static final String MODE_GENERAL = "General";
    public static final String MODE_TIMED = "Timed";

    // Legacy method kept for fallback
    public static ArrayList<Question> getQuestions() {
        // This is now handled by the database
        return new ArrayList<>();
    }
}