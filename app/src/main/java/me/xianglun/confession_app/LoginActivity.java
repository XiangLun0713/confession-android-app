package me.xianglun.confession_app;

import android.content.Intent;
import android.os.Bundle;
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

            // TODO: 6/5/2022 Jiajun, the following logic is to allow us to navigate to both
            //  admin or user page at this moment, as the authentication is not yet done.
            //  Please change the below logic to let the user enter the admin page
            //  only when the admin credentials was entered.
            //  can set admin credentials as, email: admin@gmail.com; password: admin
            Intent intent;
            if (emailInput.equals("admin")) {
                // Navigate from login activity to admin main activity
                intent = new Intent(LoginActivity.this, AdminMainActivity.class);
            } else {
                // Navigate from login activity to main activity
                intent = new Intent(LoginActivity.this, MainActivity.class);
            }
            startActivity(intent);
        });
    }
}