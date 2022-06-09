package me.xianglun.confession_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

import me.xianglun.confession_app.adapter.ReportedPostAdapter;
import me.xianglun.confession_app.model.PostModel;

public class AdminMainActivity extends AppCompatActivity {

    // declare reference variables
    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private ReportedPostAdapter reportedPostAdapter;
    private ArrayList<PostModel> reportedPostList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        // Instantiate reference variables
        mToolbar = findViewById(R.id.admin_toolbar);
        mRecyclerView = findViewById(R.id.admin_post_recycler_view);

        loadReportedPost();

        // Using the reference variable to set title on the toolbar
        mToolbar.setTitle(Html.fromHtml("<font color=\"#444444\">&nbsp;&nbsp Admin </font>"));
        // Set this toolbar as the action bar of the app
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_star_icon_24);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.admin_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.admin_logout_menu_item) {
            // TODO: 6/9/2022 Jiajun insert your code here to log the admin out of their account
            Toast.makeText(this, "Logging out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AdminMainActivity.this, LoginActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadReportedPost() {
        reportedPostList = new ArrayList<>();
        reportedPostAdapter = new ReportedPostAdapter(this, reportedPostList);
        mRecyclerView.setAdapter(reportedPostAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reportedPostAdapter.setOnItemClickListener(position -> {
            PostModel post = reportedPostList.get(position);
            Intent intent = new Intent(AdminMainActivity.this, SubmitPostActivity.class);
            intent.putExtra("postId", post.getId());
            intent.putExtra("replyId", post.getReplyId());
            intent.putExtra("content", post.getContent());
            intent.putExtra("date", post.getDate());
            intent.putExtra("time", post.getTime());
            intent.putStringArrayListExtra("imagePaths", (ArrayList<String>) post.getImagePaths());
            startActivity(intent);
        });
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://confession-android-app-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference databaseReference = firebaseDatabase.getReference().child("reported_posts");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reportedPostList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        PostModel reportedPost = snap.getValue(PostModel.class);
                        reportedPostList.add(reportedPost);
                    }
                }
                reportedPostAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}