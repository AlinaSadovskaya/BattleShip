package com.lab3.lab3_game.CreateGameField;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.lab3.lab3_game.Activities.GameActivity;
import com.lab3.lab3_game.Structures.GameField;
import com.lab3.lab3_game.Structures.GameStatus;
import com.lab3.lab3_game.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.Objects;
import java.util.UUID;

public class CreateGameFieldActivity extends AppCompatActivity {

    private GameFieldView fieldView;
    private PopupWindow mPopupWindow;
    private PopupWindow idPopupWindow;
    private ProgressBar mProgressBar;
    private String id;

    private boolean startingGame;

    private FirebaseDatabase database;
    private DatabaseReference game;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_field);
        startingGame = getIntent().getBooleanExtra("startingGame", false);

        mProgressBar = findViewById(R.id.progressBarJoinGame2);
        mProgressBar.setVisibility(View.GONE);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        fieldView = findViewById(R.id.player_field);
        if (savedInstanceState == null)
            fieldView.createField();
        else {
            GameField field = (GameField) savedInstanceState.getSerializable("field");
            fieldView.setField(field, CurrentGameFieldMode.CREATION);
        }
        FloatingActionButton fab = findViewById(R.id.floatingActionButtonInfo2);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHint();
            }
        });
        Button goToGameButton = findViewById(R.id.get_started_field_created);
        goToGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!fieldView.finalCheck())
                    ShipsPlacesError();
                else {
                    if (startingGame)
                        getGameId();
                    else
                        enterGameId();
                }
            }
        });
    }

    private void showHint()
    {
        Context mContext = getApplicationContext();
        // popup window for entering rss
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        View customView = Objects.requireNonNull(inflater).inflate(R.layout.rules, null);
        mPopupWindow = new PopupWindow(
                customView,
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT,
                true
        );
        mPopupWindow.setElevation(5.0f);
        findViewById(R.id.create_field_layout).post(new Runnable() {
            @Override
            public void run() {
                mPopupWindow.showAtLocation(findViewById(R.id.create_field_layout), Gravity.CENTER,0,0);
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

    private void getGameId()
    {
        Context mContext = getApplicationContext();
        // popup window for entering rss
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        View customView = Objects.requireNonNull(inflater).inflate(R.layout.personal_game, null);
        idPopupWindow = new PopupWindow(
                customView,
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT,
                true
        );
        idPopupWindow.setElevation(5.0f);
        findViewById(R.id.create_field_layout).post(new Runnable() {
            @Override
            public void run() {
                idPopupWindow.showAtLocation(findViewById(R.id.create_field_layout), Gravity.CENTER,0,0);
            }
        });

        final TextView idView = customView.findViewById(R.id.game_id_view);
        UUID U_id = UUID.randomUUID();
        id = U_id.toString().replace("-", "");
        idView.setText(id);

        Button okButton = customView.findViewById(R.id.game_id_getting_ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressBar.setVisibility(View.VISIBLE);
                connect(true);
                idPopupWindow.dismiss();
            }
        });


        Button copyButton = customView.findViewById(R.id.copy_to_clipboard);
        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("simple text", idView.getText());
                Objects.requireNonNull(clipboard).setPrimaryClip(clip);
                Toast.makeText(getApplicationContext(), "Id copied to Clipboard",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void enterGameId()
    {
        Context mContext = getApplicationContext();
        // popup window for entering rss
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        View customView = Objects.requireNonNull(inflater).inflate(R.layout.join_game, null);
        idPopupWindow = new PopupWindow(
                customView,
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT,
                true
        );
        idPopupWindow.setElevation(5.0f);
        findViewById(R.id.create_field_layout).post(new Runnable() {
            @Override
            public void run() {
                idPopupWindow.showAtLocation(findViewById(R.id.create_field_layout), Gravity.CENTER,0,0);
            }
        });
        final EditText idInput = customView.findViewById(R.id.id_input);
        Button okButton = customView.findViewById(R.id.game_id_joining_ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressBar.setVisibility(View.VISIBLE);
                id = idInput.getText().toString();
                connect(false);
                idPopupWindow.dismiss();
            }
        });
    }

    private void goToGame(Boolean start)
    {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("id", id);
        intent.putExtra("start", start);
        this.startActivity(intent);
        this.finish();
    }

    private void connect(Boolean start)
    {
        database = FirebaseDatabase.getInstance();
        game = database.getReference("games").child(id);
        ValueEventListener checkListener;

        if (start) {
            checkListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        Gson gson = new Gson();
                        String jsonGridCreated = gson.toJson(fieldView.getGameField());
                        String jsonGridCreated2 = gson.toJson(new GameField());
                        game.removeEventListener(this);
                        game.setValue(new GameStatus(currentUser.getDisplayName(), "", jsonGridCreated, jsonGridCreated2));
                        goToGame(true);
                    }
                    else {
                        ErrorToJoinGame();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
        }
        else {
            checkListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Gson gson = new Gson();
                        String jsonGridCreated = gson.toJson(fieldView.getGameField());
                        game.removeEventListener(this);
                        game.child("player_2").setValue(currentUser.getDisplayName());
                        game.child("player_2_field").setValue(jsonGridCreated);
                        goToGame(false);
                    }
                    else ErrorToJoinGame();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
        }
        game.addValueEventListener(checkListener);

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("field", fieldView.getGameField());
    }



    private void ErrorToJoinGame()
    {
        Toast toast = Toast.makeText(this,
                "Incorrect id.",
                Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        LinearLayout toastContainer = (LinearLayout) toast.getView();
        ImageView catImageView = new ImageView(this);
        catImageView.setImageResource(R.drawable.ariel_happy);
        toastContainer.addView(catImageView, 0);
        toast.show();
    }

    private void ShipsPlacesError()
    {
        Toast toast = Toast.makeText(this,
                "Incorrect placement for ships.",
                Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        LinearLayout toastContainer = (LinearLayout) toast.getView();
        ImageView catImageView = new ImageView(this);
        catImageView.setImageResource(R.drawable.ariel_happy);
        toastContainer.addView(catImageView, 0);
        toast.show();
    }

    @Override
    public void onBackPressed() {
        if (database != null)
            database.getReference("games").child(id).removeValue();
        super.onBackPressed();
    }
}
