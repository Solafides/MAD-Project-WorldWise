package com.example.quizproject;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences saveStatePrefs;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences userPrefs = getSharedPreferences("QuizPrefs", MODE_PRIVATE);
        saveStatePrefs = getSharedPreferences("QuizSaveState", MODE_PRIVATE);
        username = userPrefs.getString("username", "Guest");

        TextView tvWelcome = findViewById(R.id.tv_welcome);
        tvWelcome.setText("Welcome " + username + "!");

        setupButtonListeners();
    }

    private void setupButtonListeners() {
        Button btnGeneral = findViewById(R.id.btn_general_mode);
        btnGeneral.setOnClickListener(v -> handleModeSelection(Constants.MODE_GENERAL));

        Button btnTimed = findViewById(R.id.btn_timed_mode);
        btnTimed.setOnClickListener(v -> handleModeSelection(Constants.MODE_TIMED));

        Button btnLogout = findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor userEditor = getSharedPreferences("QuizPrefs", MODE_PRIVATE).edit();
            userEditor.clear();
            userEditor.apply();
            clearSavedGame();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });

        Button btnLeaderboard = findViewById(R.id.btn_leaderboard);
        btnLeaderboard.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, LeaderboardActivity.class)));
    }

    private void handleModeSelection(String selectedMode) {
        boolean isGameSaved = saveStatePrefs.getBoolean("isGameSaved", false);
        String savedMode = saveStatePrefs.getString("mode", "");

        if (isGameSaved && savedMode.equals(selectedMode)) {
            showResumeOrNewGameDialog(selectedMode);
        } else {
            startNewGame(selectedMode);
        }
    }

    private void showResumeOrNewGameDialog(String mode) {
        new AlertDialog.Builder(this)
                .setTitle("Saved Game Found")
                .setMessage("Do you want to continue your saved game or start a new one?")
                .setPositiveButton("Continue", (dialog, which) -> resumeGame())
                .setNegativeButton("New Game", (dialog, which) -> startNewGame(mode))
                .setNeutralButton("Cancel", null)
                .show();
    }

    private void startNewGame(String mode) {
        clearSavedGame();
        Intent intent = new Intent(MainActivity.this, QuizQuestionsActivity.class);
        intent.putExtra(Constants.USER_NAME, username);
        intent.putExtra(Constants.QUIZ_MODE, mode);
        startActivity(intent);
    }

    private void resumeGame() {
        Intent intent = new Intent(MainActivity.this, QuizQuestionsActivity.class);
        intent.putExtra(Constants.USER_NAME, username);
        intent.putExtra("isResumed", true);
        startActivity(intent);
    }

    private void clearSavedGame() {
        SharedPreferences.Editor saveEditor = saveStatePrefs.edit();
        saveEditor.clear();
        saveEditor.apply();
    }
}
