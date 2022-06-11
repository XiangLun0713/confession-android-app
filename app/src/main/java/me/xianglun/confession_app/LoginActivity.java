package me.xianglun.confession_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    // Declare reference variable
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private MaterialButton mLoginButton;
    private MaterialButton mRegisterButton;
    private TextView register;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // View this as the main method in java program, this method is run when this activity(page) is created
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Instantiate the reference variable
        mEmailEditText = findViewById(R.id.login_email_edit_text);
        mPasswordEditText = findViewById(R.id.login_password_edit_text);
        mLoginButton = findViewById(R.id.login_button);
        mRegisterButton = findViewById(R.id.register_button);

        mAuth = FirebaseAuth.getInstance();

        //check if user has created an account before
        if(mAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        //redirect when user press register button
        mRegisterButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        // Set on click listener on the login button
        mLoginButton.setOnClickListener(v -> {

            String email = mEmailEditText.getText().toString().trim();
            String password = mPasswordEditText.getText().toString().trim();

            if (email.isEmpty()) {
                mEmailEditText.setError("Email is required!");
                mEmailEditText.requestFocus();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                mEmailEditText.setError("Please enter a valid email!");
                mEmailEditText.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                mPasswordEditText.setError("Password is required!");
                mPasswordEditText.requestFocus();
                return;
            }

            if (password.length() < 6) {
                mPasswordEditText.setError("Min password length is 6 characters!");
                mPasswordEditText.requestFocus();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Logged in Successfully ", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                } else {
                    Toast.makeText(LoginActivity.this, "Error! User not found! ", Toast.LENGTH_SHORT).show();

                }
            });
        });
    }
}