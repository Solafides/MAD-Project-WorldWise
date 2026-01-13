package com.example.quizproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {

    private QuizDbHelper dbHelper;
    private String mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        clearSavedGameState();

        int totalQuestions = getIntent().getIntExtra(Constants.TOTAL_QUESTIONS, 0);
        int correctAnswers = getIntent().getIntExtra(Constants.CORRECT_ANSWERS, 0);
        mode = getIntent().getStringExtra(Constants.QUIZ_MODE);
        if (mode == null) mode = Constants.MODE_GENERAL;
        
        int incorrectAnswers = totalQuestions - correctAnswers;

        TextView tvScore = findViewById(R.id.tv_score);
        TextView tvCorrectAnswers = findViewById(R.id.tv_correct_answers);
        TextView tvIncorrectAnswers = findViewById(R.id.tv_incorrect_answers);
        Button btnFinish = findViewById(R.id.btn_finish);
        Button btnExit = findViewById(R.id.btn_exit);
        
        TextView tvRankStat = findViewById(R.id.tv_rank_stat);
        TextView tvRankChange = findViewById(R.id.tv_rank_change);
        TextView tvGamesPlayed = findViewById(R.id.tv_games_played);
        TextView tvBestScore = findViewById(R.id.tv_best_score);

        tvScore.setText(correctAnswers + "/" + totalQuestions);
        tvCorrectAnswers.setText("Correct Answers: " + correctAnswers);
        tvIncorrectAnswers.setText("Incorrect Answers: " + incorrectAnswers);

        dbHelper = new QuizDbHelper(this);
        saveScoreAndUpdateDashboard(correctAnswers, totalQuestions, mode, tvRankStat, tvRankChange, tvGamesPlayed, tvBestScore);

        btnFinish.setOnClickListener(v -> {
            startActivity(new Intent(ResultActivity.this, MainActivity.class));
            finish();
        });

        btnExit.setOnClickListener(v -> {
            startActivity(new Intent(ResultActivity.this, MainActivity.class));
            finish();
        });
    }

    private void clearSavedGameState() {
        SharedPreferences prefs = getSharedPreferences("QuizSaveState", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

    private void saveScoreAndUpdateDashboard(int score, int total, String mode, 
                                             TextView tvRankStat, TextView tvRankChange,
                                             TextView tvGamesPlayed, TextView tvBestScore) {
        SharedPreferences prefs = getSharedPreferences("QuizPrefs", MODE_PRIVATE);
        String username = prefs.getString("username", null);
        
        if (username != null) {
            int userId = dbHelper.getUserId(username);
            if (userId != -1) {
                int previousRank = dbHelper.getUserRank(userId, mode);
                
                dbHelper.addScore(userId, score, total, mode);
                
                int currentBest = dbHelper.getUserBestScore(userId, mode);
                int currentRank = dbHelper.getUserRank(userId, mode);
                int totalGames = dbHelper.getTotalGamesPlayed(userId, mode);
                
                tvGamesPlayed.setText("Total Games Played: " + totalGames);
                tvBestScore.setText("Best Score: " + currentBest);
                tvRankStat.setText("Current Rank: " + (currentRank > 0 ? "#" + currentRank : "Unranked"));
                
                if (previousRank > 0 && currentRank < previousRank) {
                    tvRankChange.setText("Rank Change: ▲ Up " + (previousRank - currentRank) + " places!");
                    tvRankChange.setTextColor(getResources().getColor(R.color.green_correct));
                } else if (previousRank > 0 && currentRank > previousRank) {
                    tvRankChange.setText("Rank Change: ▼ Down " + (currentRank - previousRank) + " places");
                    tvRankChange.setTextColor(getResources().getColor(R.color.red_wrong));
                } else if (previousRank <= 0 && currentRank > 0) {
                    tvRankChange.setText("Rank Change: New Entry!");
                    tvRankChange.setTextColor(getResources().getColor(R.color.primary_blue));
                } else {
                    tvRankChange.setText("Rank Change: No Change");
                    tvRankChange.setTextColor(getResources().getColor(R.color.dark_text));
                }
            }
        }
    }
}
