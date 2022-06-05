package me.xianglun.confession_app;

import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private FloatingActionButton mFloatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = findViewById(R.id.main_toolbar);
        mFloatingActionButton = findViewById(R.id.floating_action_button);

        mToolbar.setTitle(Html.fromHtml("<font color=\"#444444\"> Home </font>"));
        setSupportActionBar(mToolbar);

        mFloatingActionButton.setOnClickListener(v -> Toast.makeText(this, "Create New Post", Toast.LENGTH_SHORT).show());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.log_out_menu_item) {
            Toast.makeText(this, "Logging out", Toast.LENGTH_SHORT).show();
            // TODO: 6/4/2022 Jiajun please put the log out function here to log the user out of their account
        } else if (item.getItemId() == R.id.settings_menu_item) {
            // TODO: 6/5/2022 Navigate the user to the settings page
        }
        return super.onOptionsItemSelected(item);
    }
}