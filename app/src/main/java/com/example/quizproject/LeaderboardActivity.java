package com.example.quizproject;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {

    private ListView listView;
    private Button btnGeneral, btnTimed;
    private QuizDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        listView = findViewById(R.id.lv_leaderboard);
        Button btnBack = findViewById(R.id.btn_back);
        btnGeneral = findViewById(R.id.btn_mode_general);
        btnTimed = findViewById(R.id.btn_mode_timed);
        
        dbHelper = new QuizDbHelper(this);

        // Default load General mode
        loadLeaderboard(Constants.MODE_GENERAL);

        btnGeneral.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTabUI(true);
                loadLeaderboard(Constants.MODE_GENERAL);
            }
        });

        btnTimed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTabUI(false);
                loadLeaderboard(Constants.MODE_TIMED);
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void updateTabUI(boolean isGeneral) {
        int activeColor = ContextCompat.getColor(this, R.color.cyan_blue);
        int inactiveColor = ContextCompat.getColor(this, R.color.grey_option);
        int white = ContextCompat.getColor(this, R.color.white);
        int black = ContextCompat.getColor(this, R.color.black);

        if (isGeneral) {
            btnGeneral.setBackgroundColor(activeColor);
            btnGeneral.setTextColor(white);
            btnTimed.setBackgroundColor(inactiveColor);
            btnTimed.setTextColor(black);
        } else {
            btnGeneral.setBackgroundColor(inactiveColor);
            btnGeneral.setTextColor(black);
            btnTimed.setBackgroundColor(activeColor);
            btnTimed.setTextColor(white);
        }
    }

    private void loadLeaderboard(String mode) {
        List<String> topScores = dbHelper.getLeaderboard(mode);
        if (topScores.isEmpty()) {
            topScores = new ArrayList<>();
            topScores.add("No scores yet!");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, topScores);
        listView.setAdapter(adapter);
    }
}