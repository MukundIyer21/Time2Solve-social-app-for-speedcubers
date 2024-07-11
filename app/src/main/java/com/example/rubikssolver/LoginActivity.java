package com.example.rubikssolver;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private Button btnLogin;
    private EditText edtTxtEmailLogin,edtTxtPasswordLogin;
    private TextView txtViewRegister;
    private ProgressBar progressBarLogin;
    private CardView cardViewLogin;
    FirebaseAuth mAuth;
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        edtTxtEmailLogin = findViewById(R.id.edtTxttEmailLogin);
        edtTxtPasswordLogin = findViewById(R.id.edtTxtPasswordLogin);
        btnLogin = findViewById(R.id.btnLogin);
        txtViewRegister = findViewById(R.id.txtViewRegister);
        progressBarLogin = findViewById(R.id.progressBarLogin);
        cardViewLogin = findViewById(R.id.cardViewLogin);

        txtViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent  = new Intent(getApplicationContext(),RegistrationPage.class);
                startActivity(intent);
                finish();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBarLogin.setVisibility(View.VISIBLE);
                cardViewLogin.setVisibility(View.GONE);
                String email,password;
                email = edtTxtEmailLogin.getText().toString();
                password = edtTxtPasswordLogin.getText().toString();

                if(email.isEmpty() || password.isEmpty()){
                    Snackbar.make(view,"Input fields cannot be empty",Snackbar.LENGTH_SHORT).show();
                    progressBarLogin.setVisibility(View.GONE);
                    cardViewLogin.setVisibility(View.VISIBLE);
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    progressBarLogin.setVisibility(View.GONE);
                                    cardViewLogin.setVisibility(View.VISIBLE);
                                    Snackbar.make(view,"Login Successful",Snackbar.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    progressBarLogin.setVisibility(View.GONE);
                                    cardViewLogin.setVisibility(View.VISIBLE);
                                    Snackbar.make(view,"Login Failed",Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        });

            }
        });

    }
}