package com.example.quizproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class QuizQuestionsActivity extends AppCompatActivity implements View.OnClickListener {

    private int mCurrentPosition = 1;
    private ArrayList<Question> mQuestionsList = null;
    private int mSelectedOptionPosition = 0;
    private int mCorrectAnswers = 0;
    private int mIncorrectAnswers = 0;
    private boolean mAnswered = false;
    private String mQuizMode = Constants.MODE_GENERAL;

    private CountDownTimer mTimer;
    private long mTimeLeftInMillis = 0;
    private TextView tvTimer;

    private ArrayList<String> mCurrentOptions = new ArrayList<>();
    private int mCurrentCorrectPosition = -1;

    private TextView tvQuestion;
    private TextView tvOptionOne, tvOptionTwo, tvOptionThree, tvOptionFour;
    private TextView tvProgress;
    private Button btnSubmit, btnSaveExit;
    private QuizDbHelper dbHelper;

    private SoundPool soundPool;
    private int correctSoundId, wrongSoundId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_questions);

        boolean isResumed = getIntent().getBooleanExtra("isResumed", false);

        initViews();
        initSoundPool();
        dbHelper = new QuizDbHelper(this);

        if (isResumed) {
            loadGameState();
        } else {
            startNewGame();
        }

        setQuestion();
        setClickListeners();
    }

    private void initViews() {
        tvTimer = findViewById(R.id.tv_timer);
        tvQuestion = findViewById(R.id.tv_question);
        tvOptionOne = findViewById(R.id.tv_option_one);
        tvOptionTwo = findViewById(R.id.tv_option_two);
        tvOptionThree = findViewById(R.id.tv_option_three);
        tvOptionFour = findViewById(R.id.tv_option_four);
        tvProgress = findViewById(R.id.tv_progress);
        btnSubmit = findViewById(R.id.btn_submit);
        btnSaveExit = findViewById(R.id.btn_save_exit);
    }

    private void initSoundPool() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(audioAttributes)
                .build();
        correctSoundId = soundPool.load(this, R.raw.correct_sound, 1);
        wrongSoundId = soundPool.load(this, R.raw.wrong_sound, 1);
    }

    private void setClickListeners() {
        tvOptionOne.setOnClickListener(this);
        tvOptionTwo.setOnClickListener(this);
        tvOptionThree.setOnClickListener(this);
        tvOptionFour.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);
        btnSaveExit.setOnClickListener(this);
    }

    private void setQuestion() {
        mAnswered = false;
        defaultOptionsView();

        if (mCurrentPosition > mQuestionsList.size()) {
            if (mQuizMode.equals(Constants.MODE_GENERAL)) {
                mQuestionsList.addAll(dbHelper.getRandomQuestions(100));
            } else {
                finishQuiz();
                return;
            }
        }

        Question question = mQuestionsList.get(mCurrentPosition - 1);
        tvQuestion.setText(question.getQuestion());

        mCurrentOptions.clear();
        mCurrentOptions.add(question.getOptionOne());
        mCurrentOptions.add(question.getOptionTwo());
        mCurrentOptions.add(question.getOptionThree());
        mCurrentOptions.add(question.getOptionFour());

        String correctString = "";
        switch (question.getCorrectAnswer()) {
            case 1: correctString = question.getOptionOne(); break;
            case 2: correctString = question.getOptionTwo(); break;
            case 3: correctString = question.getOptionThree(); break;
            case 4: correctString = question.getOptionFour(); break;
        }

        Collections.shuffle(mCurrentOptions);

        for (int i = 0; i < mCurrentOptions.size(); i++) {
            if (mCurrentOptions.get(i).equals(correctString)) {
                mCurrentCorrectPosition = i + 1;
                break;
            }
        }

        tvOptionOne.setText(mCurrentOptions.get(0));
        tvOptionTwo.setText(mCurrentOptions.get(1));
        tvOptionThree.setText(mCurrentOptions.get(2));
        tvOptionFour.setText(mCurrentOptions.get(3));

        updateProgressText();
        btnSubmit.setText("Next");
    }

    private void updateProgressText() {
        if (mQuizMode.equals(Constants.MODE_GENERAL)) {
            tvProgress.setText("Question " + mCurrentPosition + " | Misses: " + mIncorrectAnswers + "/5");
        } else {
            tvProgress.setText("Q: " + mCurrentPosition + "/" + mQuestionsList.size() + " | Misses: " + mIncorrectAnswers + "/3");
        }
    }

    private void finishQuiz() {
        if (mTimer != null) mTimer.cancel();
        clearSavedGameState();

        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra(Constants.CORRECT_ANSWERS, mCorrectAnswers);
        intent.putExtra(Constants.TOTAL_QUESTIONS, mCorrectAnswers + mIncorrectAnswers);
        intent.putExtra(Constants.QUIZ_MODE, mQuizMode);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_save_exit) {
            saveGameState();
            return;
        }

        if (v.getId() == R.id.btn_submit) {
            if (mAnswered) {
                mCurrentPosition++;
                setQuestion();
            } else {
                Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (!mAnswered) {
            int selectedViewId = v.getId();
            if (selectedViewId == R.id.tv_option_one) {
                mSelectedOptionPosition = 1;
            } else if (selectedViewId == R.id.tv_option_two) {
                mSelectedOptionPosition = 2;
            } else if (selectedViewId == R.id.tv_option_three) {
                mSelectedOptionPosition = 3;
            } else if (selectedViewId == R.id.tv_option_four) {
                mSelectedOptionPosition = 4;
            }
            checkAnswer();
        }
    }

    private void checkAnswer() {
        mAnswered = true;

        if (mCurrentCorrectPosition != mSelectedOptionPosition) {
            // Incorrect Answer
            answerView(mSelectedOptionPosition, R.drawable.wrong_option_border_bg);
            soundPool.play(wrongSoundId, 1, 1, 0, 0, 1);
            mIncorrectAnswers++;
            checkLossCondition();
        } else {
            // Correct Answer
            mCorrectAnswers++;
            soundPool.play(correctSoundId, 1, 1, 0, 0, 1);
        }
        answerView(mCurrentCorrectPosition, R.drawable.correct_option_border_bg);
        updateProgressText();
    }

    private void checkLossCondition() {
        if (mQuizMode.equals(Constants.MODE_GENERAL) && mIncorrectAnswers >= 5) {
            Toast.makeText(this, "Game Over! You missed 5 questions.", Toast.LENGTH_LONG).show();
            finishQuiz();
        } else if (mQuizMode.equals(Constants.MODE_TIMED) && mIncorrectAnswers >= 3) {
            Toast.makeText(this, "Game Over! You missed 3 questions.", Toast.LENGTH_LONG).show();
            finishQuiz();
        }
    }

    private void answerView(int answer, int drawableView) {
        switch (answer) {
            case 1: tvOptionOne.setBackground(ContextCompat.getDrawable(this, drawableView)); break;
            case 2: tvOptionTwo.setBackground(ContextCompat.getDrawable(this, drawableView)); break;
            case 3: tvOptionThree.setBackground(ContextCompat.getDrawable(this, drawableView)); break;
            case 4: tvOptionFour.setBackground(ContextCompat.getDrawable(this, drawableView)); break;
        }
    }

    private void defaultOptionsView() {
        ArrayList<TextView> options = new ArrayList<>();
        options.add(tvOptionOne);
        options.add(tvOptionTwo);
        options.add(tvOptionThree);
        options.add(tvOptionFour);

        for (TextView option : options) {
            option.setTextColor(Color.parseColor("#1A1A1A"));
            option.setTypeface(Typeface.DEFAULT);
            option.setBackground(ContextCompat.getDrawable(this, R.drawable.default_option_border_bg));
        }
    }

    private void startNewGame() {
        mQuizMode = getIntent().getStringExtra(Constants.QUIZ_MODE);
        if (mQuizMode == null) mQuizMode = Constants.MODE_GENERAL;

        if (mQuizMode.equals(Constants.MODE_TIMED)) {
            mQuestionsList = dbHelper.getRandomQuestions(50);
            tvTimer.setVisibility(View.VISIBLE);
            startTimer(5 * 60 * 1000);
        } else {
            mQuestionsList = dbHelper.getRandomQuestions(200);
            tvTimer.setVisibility(View.GONE);
        }

        if (mQuestionsList == null || mQuestionsList.isEmpty()) {
            Toast.makeText(this, "No questions found!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadGameState() {
        SharedPreferences prefs = getSharedPreferences("QuizSaveState", MODE_PRIVATE);
        mQuizMode = prefs.getString("mode", Constants.MODE_GENERAL);
        mCurrentPosition = prefs.getInt("currentPosition", 1);
        mCorrectAnswers = prefs.getInt("correctAnswers", 0);
        mIncorrectAnswers = prefs.getInt("incorrectAnswers", 0);
        mTimeLeftInMillis = prefs.getLong("timeLeft", 0);

        String questionIdsString = prefs.getString("questionIds", "");
        List<String> questionIds = new ArrayList<>(Arrays.asList(questionIdsString.split(",")));
        mQuestionsList = dbHelper.getQuestionsByIdsInOrder(questionIds);

        if (mQuizMode.equals(Constants.MODE_TIMED)) {
            tvTimer.setVisibility(View.VISIBLE);
            startTimer(mTimeLeftInMillis);
        } else {
            tvTimer.setVisibility(View.GONE);
        }
    }

    private void saveGameState() {
        if (mTimer != null) {
            mTimer.cancel();
        }

        SharedPreferences prefs = getSharedPreferences("QuizSaveState", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean("isGameSaved", true);
        editor.putString("mode", mQuizMode);
        editor.putInt("currentPosition", mCurrentPosition);
        editor.putInt("correctAnswers", mCorrectAnswers);
        editor.putInt("incorrectAnswers", mIncorrectAnswers);
        editor.putLong("timeLeft", mTimeLeftInMillis);

        List<String> questionIds = mQuestionsList.stream().map(q -> String.valueOf(q.getId())).collect(Collectors.toList());
        editor.putString("questionIds", TextUtils.join(",", questionIds));

        editor.apply();
        Toast.makeText(this, "Game Saved!", Toast.LENGTH_SHORT).show();
        finish();
    }
    private void clearSavedGameState() {
        SharedPreferences prefs = getSharedPreferences("QuizSaveState", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }
    private void startTimer(long durationMillis) {
        mTimeLeftInMillis = durationMillis;
        mTimer = new CountDownTimer(durationMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                long minutes = (mTimeLeftInMillis / 1000) / 60;
                long seconds = (mTimeLeftInMillis / 1000) % 60;
                tvTimer.setText(String.format("%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                tvTimer.setText("00:00");
                Toast.makeText(QuizQuestionsActivity.this, "Time's up!", Toast.LENGTH_SHORT).show();
                finishQuiz();
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        if (mTimer != null) {
            mTimer.cancel();
        }
    }
}
