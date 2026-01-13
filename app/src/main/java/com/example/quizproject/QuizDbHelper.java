package com.example.quizproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "WorldWiseQuiz.db";
    private static final int DATABASE_VERSION = 3; // Upgrade for statistics

    // Table Users
    private static final String TABLE_USERS = "users";
    private static final String KEY_USER_ID = "id";
    private static final String KEY_USER_NAME = "username";
    private static final String KEY_USER_PASSWORD = "password";

    // Table Questions
    private static final String TABLE_QUESTIONS = "questions";
    private static final String KEY_QUES_ID = "id";
    private static final String KEY_QUES_TEXT = "question";
    private static final String KEY_OP1 = "option1";
    private static final String KEY_OP2 = "option2";
    private static final String KEY_OP3 = "option3";
    private static final String KEY_OP4 = "option4";
    private static final String KEY_ANSWER = "answer";

    // Table Scores
    private static final String TABLE_SCORES = "scores";
    private static final String KEY_SCORE_ID = "id";
    private static final String KEY_SCORE_USER_ID = "user_id";
    private static final String KEY_SCORE_VALUE = "score";
    private static final String KEY_SCORE_TOTAL = "total";
    private static final String KEY_SCORE_MODE = "mode";
    private static final String KEY_SCORE_TIMESTAMP = "timestamp";

    public QuizDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + KEY_USER_ID + " INTEGER PRIMARY KEY,"
                + KEY_USER_NAME + " TEXT,"
                + KEY_USER_PASSWORD + " TEXT" + ")";

        String CREATE_QUESTIONS_TABLE = "CREATE TABLE " + TABLE_QUESTIONS + "("
                + KEY_QUES_ID + " INTEGER PRIMARY KEY,"
                + KEY_QUES_TEXT + " TEXT,"
                + KEY_OP1 + " TEXT,"
                + KEY_OP2 + " TEXT,"
                + KEY_OP3 + " TEXT,"
                + KEY_OP4 + " TEXT,"
                + KEY_ANSWER + " INTEGER" + ")";

        String CREATE_SCORES_TABLE = "CREATE TABLE " + TABLE_SCORES + "("
                + KEY_SCORE_ID + " INTEGER PRIMARY KEY,"
                + KEY_SCORE_USER_ID + " INTEGER,"
                + KEY_SCORE_VALUE + " INTEGER,"
                + KEY_SCORE_TOTAL + " INTEGER,"
                + KEY_SCORE_MODE + " TEXT,"
                + KEY_SCORE_TIMESTAMP + " INTEGER" + ")";

        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_QUESTIONS_TABLE);
        db.execSQL(CREATE_SCORES_TABLE);

        seedQuestions(db);
    }

    private void seedQuestions(SQLiteDatabase db) {
        // Omitted for brevity... same as before
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUESTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCORES);
        onCreate(db);
    }
    
    // --- Method that was missing ---
    public ArrayList<Question> getQuestionsByIdsInOrder(List<String> ids) {
        ArrayList<Question> questionList = new ArrayList<>();
        if (ids == null || ids.isEmpty()) {
            return questionList;
        }

        String selectQuery = "SELECT * FROM " + TABLE_QUESTIONS + " WHERE " + KEY_QUES_ID + " IN (" + TextUtils.join(",", ids) + ")";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Create a temporary list to re-order later
        ArrayList<Question> tempList = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                Question question = new Question(
                        cursor.getInt(cursor.getColumnIndexOrThrow(KEY_QUES_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_QUES_TEXT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_OP1)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_OP2)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_OP3)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_OP4)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ANSWER))
                );
                tempList.add(question);
            } while (cursor.moveToNext());
        }
        cursor.close();

        // Re-order the questions to match the original saved order
        for (String id : ids) {
            for (Question q : tempList) {
                if (String.valueOf(q.getId()).equals(id)) {
                    questionList.add(q);
                    break;
                }
            }
        }
        return questionList;
    }

    // --- Other methods (registerUser, checkUser, etc.) are here... ---
    // Omitted for brevity, no changes to them
    
    // --- Question Operations ---
    public ArrayList<Question> getRandomQuestions(int limit) {
        ArrayList<Question> questionList = new ArrayList<>();
        // ORDER BY RANDOM() is efficient enough for 200 items
        String selectQuery = "SELECT * FROM " + TABLE_QUESTIONS + " ORDER BY RANDOM() LIMIT " + limit;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Question question = new Question(
                        cursor.getInt(cursor.getColumnIndexOrThrow(KEY_QUES_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_QUES_TEXT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_OP1)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_OP2)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_OP3)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_OP4)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ANSWER))
                );
                questionList.add(question);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return questionList;
    }

    // --- Score Operations (omitted for brevity) ---


    public long registerUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_USER_NAME, username);
        values.put(KEY_USER_PASSWORD, password);
        return db.insert(TABLE_USERS, null, values);
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{KEY_USER_ID}, 
                KEY_USER_NAME + "=? AND " + KEY_USER_PASSWORD + "=?", 
                new String[]{username, password}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean isUserExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{KEY_USER_ID}, 
                KEY_USER_NAME + "=?", new String[]{username}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public int getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{KEY_USER_ID}, 
                KEY_USER_NAME + "=?", new String[]{username}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            cursor.close();
            return id;
        }
        return -1;
    }

    public void addScore(int userId, int score, int total, String mode) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_SCORE_USER_ID, userId);
        values.put(KEY_SCORE_VALUE, score);
        values.put(KEY_SCORE_TOTAL, total);
        values.put(KEY_SCORE_MODE, mode);
        values.put(KEY_SCORE_TIMESTAMP, System.currentTimeMillis());
        db.insert(TABLE_SCORES, null, values);
    }

    public List<String> getLeaderboard(String mode) {
        List<String> scores = new ArrayList<>();
        String query = "SELECT u." + KEY_USER_NAME + ", s." + KEY_SCORE_VALUE + ", s." + KEY_SCORE_TOTAL
                + " FROM " + TABLE_SCORES + " s"
                + " JOIN " + TABLE_USERS + " u ON s." + KEY_SCORE_USER_ID + " = u." + KEY_USER_ID
                + " WHERE s." + KEY_SCORE_MODE + " = ?"
                + " ORDER BY s." + KEY_SCORE_VALUE + " DESC LIMIT 10";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{mode});
        
        if (cursor.moveToFirst()) {
            int rank = 1;
            do {
                String name = cursor.getString(0);
                int score = cursor.getInt(1);
                int total = cursor.getInt(2);
                scores.add(rank + ". " + name + ": " + score + " pts");
                rank++;
            } while (cursor.moveToNext());
        }
        cursor.close();
        return scores;
    }
    
    public int getUserRank(int userId, String mode) {
        int myBestScore = getUserBestScore(userId, mode);
        if (myBestScore == -1) return -1; // No score
        
        String rankQuery = "SELECT COUNT(*) FROM " + TABLE_SCORES 
                         + " WHERE " + KEY_SCORE_VALUE + " > ? AND " + KEY_SCORE_MODE + " = ?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(rankQuery, new String[]{String.valueOf(myBestScore), mode});
        int rank = 0;
        if (cursor.moveToFirst()) {
            rank = cursor.getInt(0) + 1;
        }
        cursor.close();
        return rank;
    }

    public int getUserBestScore(int userId, String mode) {
        String query = "SELECT MAX(" + KEY_SCORE_VALUE + ") FROM " + TABLE_SCORES 
                     + " WHERE " + KEY_SCORE_USER_ID + " = ? AND " + KEY_SCORE_MODE + " = ?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), mode});
        int best = -1;
        if (cursor.moveToFirst()) {
            if (!cursor.isNull(0)) {
                best = cursor.getInt(0);
            }
        }
        cursor.close();
        return best;
    }
    
    public int getTotalGamesPlayed(int userId, String mode) {
        String query = "SELECT COUNT(*) FROM " + TABLE_SCORES 
                     + " WHERE " + KEY_SCORE_USER_ID + " = ? AND " + KEY_SCORE_MODE + " = ?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), mode});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }
}