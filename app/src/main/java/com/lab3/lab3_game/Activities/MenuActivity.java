package com.lab3.lab3_game.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lab3.lab3_game.CreateGameField.CreateGameFieldActivity;
import com.lab3.lab3_game.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.lab3.lab3_game.UserAccount.UserPageActivity;

import java.util.Objects;

public class MenuActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
        database = FirebaseDatabase.getInstance();
        Button signOutButton = findViewById(R.id.menu_log_out);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });
        Button startGameButton = findViewById(R.id.menu_start_game);
        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToCreation();
            }
        });
        Button joinGameButton = findViewById(R.id.menu_join_game);
        joinGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToJoining();
            }
        });
        Button statsButton = findViewById(R.id.menu_stats);
        statsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToStats();
            }
        });
        Button userButton = findViewById(R.id.menu_user_profile);
        userButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToAccount();
            }
        });
    }

    private void signOut() {
        mAuth.signOut();
        googleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        goToLogIn();
                    }
                });
    }

    private void goToLogIn()
    {
        Intent intent = new Intent(this, GoogleLoginActivity.class);
        this.startActivity(intent);
        this.finish();
    }

    private void goToCreation()
    {
        Intent intent = new Intent(this, CreateGameFieldActivity.class);
        intent.putExtra("startingGame", true);
        this.startActivity(intent);
        //this.finish();
    }

    private void goToJoining()
    {
        Intent intent = new Intent(this, CreateGameFieldActivity.class);
        intent.putExtra("startingGame", false);
        this.startActivity(intent);
    }

    private void goToStats()
    {
        Intent intent = new Intent(this, GameStatisticActivity.class);
        this.startActivity(intent);
    }

    private void goToAccount()
    {
        Intent intent = new Intent(this, UserPageActivity.class);
        this.startActivity(intent);
    }


}
