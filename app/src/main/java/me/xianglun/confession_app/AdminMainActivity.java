package me.xianglun.confession_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.Html;

public class AdminMainActivity extends AppCompatActivity {

    // declare reference variable to the toolbar
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // View this as the main method in java program, this method is run when this activity(page) is created
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        // Instantiate the reference variable
        mToolbar = findViewById(R.id.admin_toolbar);

        // Using the reference variable to set title on the toolbar
        mToolbar.setTitle(Html.fromHtml("<font color=\"#444444\"> Admin </font>"));
        // Set this toolbar as the action bar of the app
        setSupportActionBar(mToolbar);
    }
}