package me.xianglun.confession_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    // Declare reference variable
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private MaterialButton mLoginButton;
    private CardView mCardView;
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // View this as the main method in java program, this method is run when this activity(page) is created
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Instantiate the reference variable
        mCardView = findViewById(R.id.login_progress_card_view);
        mEmailEditText = findViewById(R.id.login_email_edit_text);
        mPasswordEditText = findViewById(R.id.login_password_edit_text);
        mLoginButton = findViewById(R.id.login_button);
        mFirebaseAuth = FirebaseAuth.getInstance();

        // Check if user is already logged in
        if (mFirebaseAuth.getCurrentUser() != null) {
            // If yes, navigate the user directly to the admin page
            startActivity(new Intent(getApplicationContext(), AdminMainActivity.class));
            finish();
        }

        // Set on click listener on the login button
        mLoginButton.setOnClickListener(v -> {
            // set progress bar visible
            mCardView.setVisibility(View.VISIBLE);
            // get user inputs
            String email = mEmailEditText.getText().toString().trim();
            String password = mPasswordEditText.getText().toString().trim();

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                mEmailEditText.setError("Please enter a valid email address!");
                mEmailEditText.requestFocus();
            } else if (password.isEmpty()) {
                mPasswordEditText.setError("Please enter a valid password!");
                mPasswordEditText.requestFocus();
            } else {
                mFirebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // set progress bar invisible
                        mCardView.setVisibility(View.INVISIBLE);
                        Toast.makeText(LoginActivity.this, "Log in successful. Welcome!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, AdminMainActivity.class));
                    } else {
                        // set progress bar invisible
                        mCardView.setVisibility(View.INVISIBLE);
                        Toast.makeText(LoginActivity.this, "Invalid credential.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}