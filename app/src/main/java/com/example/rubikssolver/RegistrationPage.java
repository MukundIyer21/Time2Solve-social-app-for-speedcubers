package com.example.rubikssolver;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class RegistrationPage extends AppCompatActivity {
    private EditText edtTxtEmailRegister,edtTxtPasswordRegister,edtTxtCPasswordRegister,edtTxtUsernameRegister;
    private Button btnRegister;
    private TextView txtViewLogin;
    FirebaseAuth mAuth;
    private ProgressBar progressBarRegister;
    private CardView cardViewRegister;
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
        setContentView(R.layout.activity_registration_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mAuth = FirebaseAuth.getInstance();
        edtTxtEmailRegister = findViewById(R.id.edtTxttEmailRegister);
        edtTxtPasswordRegister = findViewById(R.id.edtTxtPasswordRegister);
        edtTxtUsernameRegister = findViewById(R.id.edtTxtUsername);
        edtTxtCPasswordRegister = findViewById(R.id.edtTxtCPasswordRegister);
        btnRegister = findViewById(R.id.btnRegister);
        txtViewLogin = findViewById(R.id.txtViewLogin);
        progressBarRegister = findViewById(R.id.progressBarRegister);
        cardViewRegister = findViewById(R.id.cardViewRegister);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBarRegister.setVisibility(View.VISIBLE);
                cardViewRegister.setVisibility(View.GONE);
                String email,password,Cpassword,username;

                email = edtTxtEmailRegister.getText().toString();
                password = edtTxtPasswordRegister.getText().toString();
                Cpassword =edtTxtCPasswordRegister.getText().toString();
                username = edtTxtUsernameRegister.getText().toString();

                if(email.isEmpty() || password.isEmpty() || username.isEmpty() || Cpassword.isEmpty()){
                    Snackbar.make(view,"Input fields cannot be empty!",Snackbar.LENGTH_SHORT).show();
                    progressBarRegister.setVisibility(View.GONE);
                    cardViewRegister.setVisibility(View.VISIBLE);
                    return;
                }
                else if(!password.equals(Cpassword)){
                    edtTxtCPasswordRegister.setError("Password does not match");
                    edtTxtPasswordRegister.setError("Password does not match");
                    Snackbar.make(view,"Password does not match",Snackbar.LENGTH_SHORT).show();
                    progressBarRegister.setVisibility(View.GONE);
                    cardViewRegister.setVisibility(View.VISIBLE);
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    progressBarRegister.setVisibility(View.GONE);
                                    cardViewRegister.setVisibility(View.VISIBLE);
                                    edtTxtEmailRegister.setText("");
                                    edtTxtPasswordRegister.setText("");
                                    edtTxtUsernameRegister.setText("");
                                    edtTxtCPasswordRegister.setText("");

                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    List<Cuber> friends = new ArrayList<>();
                                    Cuber cuber = new Cuber(user.getUid(),username,friends);

                                    FirebaseFirestore.getInstance().collection("profiles")
                                            .document(user.getUid())
                                            .set(cuber)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    // User profile created successfully
                                                    Snackbar.make(view,"Registration Successful",Snackbar.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // Error creating user profile
                                                    Snackbar.make(view,"Registration Failed",Snackbar.LENGTH_SHORT).show();
                                                }
                                            });



                                } else {
                                    // If sign in fails, display a message to the user.
                                    progressBarRegister.setVisibility(View.GONE);
                                    cardViewRegister.setVisibility(View.VISIBLE);
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                    Snackbar.make(view,"Registration Failed",Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        });

            }
        });

        txtViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
}