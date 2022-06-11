package me.xianglun.confession_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;

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

import me.xianglun.confession_app.adapter.PostAdapter;
import me.xianglun.confession_app.model.PostModel;

public class ViewChildActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private PostAdapter postAdapter;
    private ArrayList<PostModel> postList;
    private ArrayList<String> repliedBy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_child);

        mToolbar = findViewById(R.id.view_child_toolbar);
        mRecyclerView = findViewById(R.id.post_recycler_view);
        repliedBy = getIntent().getStringArrayListExtra("repliedBy");

        loadAllChildPost();

        mToolbar.setTitle(Html.fromHtml("<font color=\"#444444\"> Replying posts </font>"));
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void loadAllChildPost() {
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(this, postList);
        mRecyclerView.setAdapter(postAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        postAdapter.setOnItemClickListener(position -> {
            PostModel post = postList.get(position);
            Intent intent = new Intent(ViewChildActivity.this, SubmitPostActivity.class);
            intent.putExtra("postId", post.getId());
            intent.putExtra("replyId", post.getReplyId());
            intent.putExtra("content", post.getContent());
            intent.putExtra("date", post.getDate());
            intent.putExtra("time", post.getTime());
            intent.putStringArrayListExtra("imagePaths", (ArrayList<String>) post.getImagePaths());
            intent.putStringArrayListExtra("repliedBy", (ArrayList<String>) post.getRepliedBy());
            startActivity(intent);
        });
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://confession-android-app-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference databaseReference = firebaseDatabase.getReference().child("submitted_posts");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        PostModel post = snap.getValue(PostModel.class);
                        if (post != null && repliedBy.contains(post.getId()))
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
}