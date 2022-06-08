package me.xianglun.confession_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import me.xianglun.confession_app.adapter.PostAdapter;
import me.xianglun.confession_app.model.PostModel;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private FloatingActionButton mFloatingActionButton;
    private RecyclerView mRecyclerView;
    private PostAdapter postAdapter;
    private ArrayList<PostModel> postList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = findViewById(R.id.main_toolbar);
        mFloatingActionButton = findViewById(R.id.floating_action_button);
        mRecyclerView = findViewById(R.id.post_recycler_view);


        loadPosts();

        mToolbar.setTitle(Html.fromHtml("<font color=\"#444444\"> Home </font>"));
        setSupportActionBar(mToolbar);

        mFloatingActionButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SubmitPostActivity.class);
            startActivity(intent);
        });
    }

    private void loadPosts() {
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(this, postList);
        mRecyclerView.setAdapter(postAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://confession-android-app-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference databaseReference = firebaseDatabase.getReference().child("submitted_posts");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        PostModel post = snap.getValue(PostModel.class);
                        postList.add(0, post);
                    }
                }
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // set up option menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        // provide functionality for the search menu item
        SearchView searchView = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
        searchView.setQueryHint("Search Post...");
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                search(newText);
                return false;
            }
        });
        return true;
    }

    private void search(String str) {
        ArrayList<PostModel> filteredList = new ArrayList<>();
        for (PostModel post : postList) {
            if ("#".concat(post.getId()).toLowerCase().contains(str.toLowerCase().trim())) {
                filteredList.add(post);
            }
        }
        PostAdapter postAdapter = new PostAdapter(this, filteredList);
        mRecyclerView.setAdapter(postAdapter);
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