package me.xianglun.confession_app.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import me.xianglun.confession_app.R;
import me.xianglun.confession_app.model.PostModel;

public class DeletePostFragment extends Fragment {

    private View view;
    private MaterialButton deletePostButton;
    private EditText deletePostIdEditText;
    private Context context;

    public DeletePostFragment() {
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
        view = inflater.inflate(R.layout.fragment_delete_post, container, false);
        context = getContext();
        deletePostIdEditText = view.findViewById(R.id.delete_post_id_text_view);
        deletePostButton = view.findViewById(R.id.admin_delete_post_button);

        deletePostButton.setOnClickListener(v -> {
            String inputId = deletePostIdEditText.getText().toString();
            if (inputId.isEmpty()) {
                Toast.makeText(context, "Please enter the post id.", Toast.LENGTH_SHORT).show();
            } else {
                // ask for delete post confirmation
                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
                builder.setTitle("Delete Post");
                builder.setMessage("Are you sure you want to delete this post? All of its replying post will also be deleted.");
                builder.setPositiveButton("Delete", (dialog, id) -> batchRemoval(inputId));
                builder.setNegativeButton(android.R.string.cancel, (dialog, id) -> {
                });
                builder.show();
            }
        });
        return view;
    }

    /**
     * Batch removing the targeted post and its children using DFS
     */
    private void batchRemoval(String id) {
        // if id entered with leading #, return
        if (id.charAt(0) == '#') {
            Toast.makeText(getContext(), "Batch remove unsuccessful. Please enter a valid ID.", Toast.LENGTH_SHORT).show();
            return;
        }

        // access database & storage reference
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://confession-android-app-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference postNode = firebaseDatabase.getReference().child("submitted_posts");
        DatabaseReference reportedPostNode = firebaseDatabase.getReference().child("reported_posts");
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

        // search for reply id
        final String[] replyId = {null};
        postNode.child(id).get().addOnCompleteListener(task -> {
            DataSnapshot snapshot = task.getResult();
            if (snapshot.exists()) {
                PostModel postModel = snapshot.getValue(PostModel.class);
                replyId[0] = Objects.requireNonNull(postModel).getReplyId();
            }
            // perform deletion using dfs to delete itself and all of its children
            dfsDelete(id, postNode, reportedPostNode, firebaseStorage);
        });
        // remove its presence in its parent repliedBy list
        if (replyId[0] != null) {
            postNode.child(replyId[0]).get().addOnCompleteListener(task -> {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    PostModel postModel = snapshot.getValue(PostModel.class);
                    List<String> newRepliedByList = Objects.requireNonNull(postModel).getRepliedBy();
                    newRepliedByList.remove(id);
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("repliedBy", newRepliedByList);
                    postNode.child(replyId[0]).updateChildren(hashMap);
                }
            });
        }
        Toast.makeText(getContext(), "Batch remove successfully!", Toast.LENGTH_SHORT).show();
    }

    private void dfsDelete(String id, DatabaseReference postNode, DatabaseReference reportedPostNode, FirebaseStorage firebaseStorage) {
        postNode.child(id).get().addOnCompleteListener(task -> {
            DataSnapshot snapshot = task.getResult();
            if (snapshot.exists()) {
                PostModel currPost = snapshot.getValue(PostModel.class);
                // delete current post from post collection
                postNode.child(Objects.requireNonNull(currPost).getId()).removeValue();
                // delete current post from reported post collection
                reportedPostNode.child(currPost.getId()).removeValue();
                // remove all current post's images from firebase storage
                List<String> imagePaths = Objects.requireNonNull(currPost).getImagePaths();
                if (imagePaths != null) {
                    for (String path : imagePaths) {
                        firebaseStorage.getReferenceFromUrl(path).delete();
                    }
                }
                // get access to current post's children list and call dfsDelete on each child posts
                List<String> repliedByList = Objects.requireNonNull(currPost).getRepliedBy();
                if (repliedByList != null) {
                    for (String replyingId : repliedByList) {
                        dfsDelete(replyingId, postNode, reportedPostNode, firebaseStorage);
                    }
                }
            }
        });
    }

}