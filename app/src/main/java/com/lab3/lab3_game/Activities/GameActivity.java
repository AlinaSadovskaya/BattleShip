package com.lab3.lab3_game.Activities;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lab3.lab3_game.CreateGameField.CurrentGameFieldMode;
import com.lab3.lab3_game.Structures.MoveResult;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import com.lab3.lab3_game.CreateGameField.GameFieldView;
import com.lab3.lab3_game.Structures.GameField;
import com.lab3.lab3_game.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Objects;

public class GameActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference player_1_field_db;
    private DatabaseReference player_2_field_db;
    private DatabaseReference player_1;
    private DatabaseReference player_2;
    private DatabaseReference player_1_score;
    private DatabaseReference player_2_score;
    private DatabaseReference currentMove;


    private ValueEventListener field_1_listener;
    private ValueEventListener field_2_listener;
    private ValueEventListener moveChangesListener;
    private ValueEventListener score_1_changedListener;
    private ValueEventListener score_2_changedListener;
    private ValueEventListener scoreView_1_listener;
    private ValueEventListener scoreView_2_listener;

    private String gameId;
    private boolean started_game;
    private boolean secondPlayerJoined = false;

    private PopupWindow mPopupWindow;
    private PopupWindow endOfTheGameWindow;
    private ProgressBar mProgressBar;
    private GameFieldView player_1_field;
    private GameFieldView player_2_field;
    private TextView player_1_name;
    private TextView player_2_name;
    private TextView player_1_name_field;
    private TextView player_2_name_field;
    private TextView player_1_scoreView;
    private TextView player_2_scoreView;

    private TextView myTurnView;
    private TextView waitForMyTurnView;

    private  int score1 = 0;
    private  int score2 = 0;
    private final int winPoints = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_layout);
        gameId = getIntent().getStringExtra("id");
        started_game = getIntent().getBooleanExtra("start", false);
        mProgressBar = findViewById(R.id.progressBarGAMEWAIT);
        mProgressBar.setVisibility(View.VISIBLE);

        myTurnView = findViewById(R.id.my_turn);
        waitForMyTurnView = findViewById(R.id.wait_for_my_turn);
        myTurnView.setVisibility(View.GONE);
        waitForMyTurnView.setVisibility(View.GONE);

        player_1_name = findViewById(R.id.player1_name);
        player_1_name_field = findViewById(R.id.player_1_field_name);
        player_1_scoreView = findViewById(R.id.score_player1);
        player_2_name = findViewById(R.id.player2_name);
        player_2_name_field = findViewById(R.id.player_2_field_name);
        player_2_scoreView = findViewById(R.id.score_player2);

        player_1_field = findViewById(R.id.player1_field);
        player_2_field = findViewById(R.id.player2_field);
        player_1_field.initializeField(CurrentGameFieldMode.PLAYER1);
        player_2_field.initializeField(CurrentGameFieldMode.PLAYER2);
        player_2_field.setFieldMode(CurrentGameFieldMode.READONLY);

        database = FirebaseDatabase.getInstance();
        DatabaseReference game = database.getReference("games").child(gameId);
        currentMove = game.child("currentMoveByPlayer");
        player_1_field_db = game.child("player_1_field");
        player_2_field_db = game.child("player_2_field");
        player_1_score = game.child("score_1");
        player_2_score = game.child("score_2");
        player_1 = game.child("player_1");
        player_2 = game.child("player_2");

        player_2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!Objects.requireNonNull(dataSnapshot.getValue(String.class)).equals("")) {
                    initFirstPlayerField();
                    initSecondPlayerField();
                    trackCurrentMove();
                    trackScore1Update();
                    trackScore2Update();
                    initStatsView();
                    mProgressBar.setVisibility(View.GONE);
                    player_2.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final FloatingActionButton showHintButton = findViewById(R.id.floatingActionButtonInfo);
        showHintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHint();
            }
        });

    }

    public void updatingMove(MoveResult moveResult)
    {
        if (moveResult == MoveResult.MISS)
        {
            if (started_game)
                currentMove.setValue("p_2_move");
            else
                currentMove.setValue("p_1_move");
        }
        else if (moveResult == MoveResult.HIT)
        {
            currentMoveStatusMessage();
            if (started_game) {
                score1++;
                player_1_score.setValue(score1);
            }
            else {
                score2++;
                player_2_score.setValue(score2);
            }
        }
        else return;
        Gson gson = new Gson();
        String jsonField1 = gson.toJson(player_1_field.getGameField());
        String jsonField2 = gson.toJson(player_2_field.getGameField());
        if (started_game)
        {
            player_1_field_db.setValue(jsonField1);
            player_2_field_db.setValue(jsonField2);
        }
        else {
            player_1_field_db.setValue(jsonField2);
            player_2_field_db.setValue(jsonField1);
        }

    }

    private void trackCurrentMove()
    {
        moveChangesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!secondPlayerJoined)
                    return;
                if (dataSnapshot.getValue(String.class) == null)
                {
                    onBackPressed();
                    return;
                }
                String value = dataSnapshot.getValue(String.class);
                if (started_game)
                {
                    if (Objects.requireNonNull(value).equals("p_1_move"))
                    {
                        currentMoveMessage(true);
                        player_2_field.setFieldMode(CurrentGameFieldMode.PLAYER2);
                    }
                    else {
                        currentMoveMessage(false);
                        player_2_field.setFieldMode(CurrentGameFieldMode.READONLY);
                    }
                }
                else {
                    if (Objects.requireNonNull(value).equals("p_2_move"))
                    {
                        currentMoveMessage(true);
                        player_2_field.setFieldMode(CurrentGameFieldMode.PLAYER2);
                    }
                    else
                    {
                        currentMoveMessage(false);
                        player_2_field.setFieldMode(CurrentGameFieldMode.READONLY);
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        currentMove.addValueEventListener(moveChangesListener);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (moveChangesListener != null)
            currentMove.removeEventListener(moveChangesListener);
        if (field_1_listener != null)
            player_1_field_db.removeEventListener(field_1_listener);
        if (field_2_listener != null)
            player_2_field_db.removeEventListener(field_2_listener);
        if (scoreView_1_listener != null)
            player_1.removeEventListener(scoreView_1_listener);
        if (scoreView_2_listener != null)
            player_2.removeEventListener(scoreView_2_listener);
        if (score_2_changedListener!= null)
            player_2_score.removeEventListener(score_2_changedListener);
        if (score_1_changedListener!= null)
            player_1_score.removeEventListener(score_1_changedListener);
    }

    private void initFirstPlayerField()
    {
        field_1_listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(String.class) == null)
                {
                    onBackPressed();
                    return;
                }
                if (Objects.requireNonNull(dataSnapshot.getValue(String.class)).isEmpty()) {
                    finish();
                    return;
                }
                Gson gson = new Gson();
                Type type = new TypeToken<GameField>() {}.getType();
                String value = dataSnapshot.getValue(String.class);
                if (started_game)
                    player_1_field.updateField((GameField) gson.fromJson(value, type));
                else
                    player_2_field.updateField((GameField) gson.fromJson(value, type));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        player_1_field_db.addValueEventListener(field_1_listener);
    }

    private void initSecondPlayerField()
    {
        field_2_listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(String.class) == null)
                {
                    onBackPressed();
                    return;
                }
                if (Objects.requireNonNull(dataSnapshot.getValue(String.class)).isEmpty() && secondPlayerJoined) {
                    finish();
                    return;
                }
                String value = dataSnapshot.getValue(String.class);
                if (!Objects.requireNonNull(value).isEmpty()) {
                    Gson gson = new Gson();
                    Type type = new TypeToken<GameField>() {
                    }.getType();
                    secondPlayerJoined = true;
                    if (started_game)
                        player_2_field.updateField((GameField) gson.fromJson(value, type));
                    else
                        player_1_field.updateField((GameField) gson.fromJson(value, type));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        player_2_field_db.addValueEventListener(field_2_listener);
        player_2_field_db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(String.class) == null)
                {
                    onBackPressed();
                    return;
                }
                String value = dataSnapshot.getValue(String.class);
                if (!Objects.requireNonNull(value).isEmpty() && started_game)
                {
                    secondPlayerJoined = true;
                    player_2_field.setFieldMode(CurrentGameFieldMode.PLAYER2);
                    player_2_field_db.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initStatsView()
    {
        scoreView_1_listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                if (value == null) {
                    onBackPressed();
                    return;
                }
                if (started_game) {
                    player_1_name.setText(value);
                    player_1_name_field.setText(value);
                }
                else {
                    player_2_name.setText(value);
                    player_2_name_field.setText(value);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        player_1.addValueEventListener(scoreView_1_listener);

        scoreView_2_listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                if (value == null) {
                    onBackPressed();
                    return;
                }
                if (started_game) {
                    player_2_name.setText(value);
                    player_2_name_field.setText(value);

                }
                else {
                    player_1_name.setText(value);
                    player_1_name_field.setText(value);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        player_2.addValueEventListener(scoreView_2_listener);
    }

    @Override
    public void onBackPressed() {
        database.getReference("games").child(gameId).removeValue();

        super.onBackPressed();
    }

    private void currentMoveMessage(boolean your)
    {
        String mes;
        if (your)
            mes = "Your move!";
        else
            mes = "Second player's move!";

        Snackbar snackbar = Snackbar.make(findViewById(R.id.game_holder), mes, BaseTransientBottomBar.LENGTH_SHORT);
        snackbar.show();
        if (your)
        {
            myTurnView.setVisibility(View.VISIBLE);
            waitForMyTurnView.setVisibility(View.GONE);
        }
        else {
            myTurnView.setVisibility(View.GONE);
            waitForMyTurnView.setVisibility(View.VISIBLE);
        }
    }

    private void currentMoveStatusMessage()
    {
        String mes = "You can hit one more time. :p";
        Snackbar snackbar = Snackbar.make(findViewById(R.id.game_holder), mes, BaseTransientBottomBar.LENGTH_SHORT);
        snackbar.show();
    }

    private void gameEnded(boolean didWin)
    {
        String mes;
        if (didWin)
            mes = "CONGRATULATIONS! YOU WIN!";
        else
            mes = "Unfortunately, you lost...";
        saveStats();
        Context mContext = getApplicationContext();
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        View customView = Objects.requireNonNull(inflater).inflate(R.layout.end_page, null);
        endOfTheGameWindow = new PopupWindow(
                customView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true
        );
        endOfTheGameWindow.setElevation(5.0f);
        findViewById(R.id.game_holder).post(new Runnable() {
            @Override
            public void run() {
                endOfTheGameWindow.showAtLocation(findViewById(R.id.game_holder), Gravity.CENTER,0,0);
            }
        });
        TextView statusView = customView.findViewById(R.id.win_status);
        statusView.setText(mes);
        ImageView kittyView = customView.findViewById(R.id.imageViewEND_OF_THE_GAME);
        if (didWin)
            kittyView.setImageResource(R.drawable.fish_small);
        else
            kittyView.setImageResource(R.drawable.fish_bad);
        Button okButton = customView.findViewById(R.id.ok_end_of_the_game);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database.getReference("games").child(gameId).removeValue();
                endOfTheGameWindow.dismiss();
                onBackPressed();
            }
        });
    }

    private void saveStats()
    {
        DatabaseReference stats = database.getReference("stats").child(gameId);
        stats.child("player_1").setValue(player_1_name.getText());
        stats.child("score_1").setValue(score1);
        stats.child("player_2").setValue(player_2_name.getText());
        stats.child("score_2").setValue(score2);
    }

    private void showHint()
    {
        Context mContext = getApplicationContext();
        // popup window for entering rss
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        View customView = Objects.requireNonNull(inflater).inflate(R.layout.rules, null);
        mPopupWindow = new PopupWindow(
                customView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true
        );
        mPopupWindow.setElevation(5.0f);
        findViewById(R.id.game_holder).post(new Runnable() {
            @Override
            public void run() {
                mPopupWindow.showAtLocation(findViewById(R.id.game_holder), Gravity.CENTER,0,0);
            }
        });
        Button okButton = customView.findViewById(R.id.rules_ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPopupWindow.dismiss();
            }
        });
    }

    private void trackScore1Update()
    {
        score_1_changedListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(int.class) == null) {
                    onBackPressed();
                    return;
                }
                int value = dataSnapshot.getValue(int.class);
                score1 = value;
                if (started_game)
                {
                    player_1_scoreView.setText(String.valueOf(score1));
                }
                else {
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    if (Objects.requireNonNull(vibrator).hasVibrator()) {
                        vibrator.vibrate(100L);
                    }
                    player_2_scoreView.setText(String.valueOf(score1));
                }
                if (value == winPoints)
                {
                    if (started_game)
                        gameEnded(true);
                    else
                        gameEnded(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        player_1_score.addValueEventListener(score_1_changedListener);
    }

    private void trackScore2Update()
    {
        score_2_changedListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(int.class) == null) {
                    onBackPressed();
                    return;
                }
                int value = dataSnapshot.getValue(int.class);
                score2 = value;
                if (started_game)
                {
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    if (Objects.requireNonNull(vibrator).hasVibrator()) {
                        vibrator.vibrate(100L);
                    }
                    player_2_scoreView.setText(String.valueOf(score2));
                }
                else {
                    player_1_scoreView.setText(String.valueOf(score2));
                }
                if (value == winPoints)
                {
                    if (!started_game)
                        gameEnded(true);
                    else
                        gameEnded(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        player_2_score.addValueEventListener(score_2_changedListener);
    }

}
