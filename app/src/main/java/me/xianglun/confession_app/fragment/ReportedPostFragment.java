package me.xianglun.confession_app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import me.xianglun.confession_app.R;
import me.xianglun.confession_app.SubmitPostActivity;
import me.xianglun.confession_app.adapter.ReportedPostAdapter;
import me.xianglun.confession_app.model.PostModel;

public class ReportedPostFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private ReportedPostAdapter reportedPostAdapter;
    private ArrayList<PostModel> reportedPostList;
    private View view;

    public ReportedPostFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_reported_post, container, false);
        mRecyclerView = view.findViewById(R.id.admin_post_recycler_view);

        // load all reported post
        loadReportedPost();

        return view;
    }

    private void loadReportedPost() {
        reportedPostList = new ArrayList<>();
        reportedPostAdapter = new ReportedPostAdapter(getContext(), reportedPostList);
        mRecyclerView.setAdapter(reportedPostAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        reportedPostAdapter.setOnItemClickListener(position -> {
            PostModel post = reportedPostList.get(position);
            Intent intent = new Intent(getContext(), SubmitPostActivity.class);
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