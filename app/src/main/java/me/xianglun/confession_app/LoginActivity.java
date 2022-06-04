package me.xianglun.confession_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class LoginActivity extends AppCompatActivity {

    // Declare reference variable
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private MaterialButton mLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // View this as the main method in java program, this method is run when this activity(page) is created
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Instantiate the reference variable
        mEmailEditText = findViewById(R.id.login_email_edit_text);
        mPasswordEditText = findViewById(R.id.login_password_edit_text);
        mLoginButton = findViewById(R.id.login_button);
        
        // Set on click listener on the login button
        mLoginButton.setOnClickListener(v -> {
            // Get the inputs from the edit texts
            String emailInput = mEmailEditText.getText().toString();
            String passwordInput = mPasswordEditText.getText().toString();

            // TODO: 6/4/2022 Jiajun, use these inputs entered by the user
            //  to log the user into their account, please proceed...

            // Navigate from login activity to main activity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }
}