package me.xianglun.confession_app.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import me.xianglun.confession_app.R;
import me.xianglun.confession_app.model.PostModel;
import pereira.agnaldo.previewimgcol.ImageCollectionView;

public class ReportedPostAdapter extends RecyclerView.Adapter<ReportedPostAdapter.ReportedViewHolder> {

    private final Context context;
    private final ArrayList<PostModel> reportedPostList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);

    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public ReportedPostAdapter(Context context, ArrayList<PostModel> postList) {
        this.context = context;
        this.reportedPostList = postList;
    }

    @NonNull
    @Override
    public ReportedPostAdapter.ReportedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate the view holder
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.template_reported_post, parent, false);
        return new ReportedViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportedPostAdapter.ReportedViewHolder holder, int position) {
        // setting up the view holder
        PostModel post = reportedPostList.get(position);
        holder.id.setText("#".concat(post.getId()));
        holder.content.setText(post.getContent());
        holder.time.setText(post.getTime());
        holder.date.setText(post.getDate());
        holder.imageMenuButton.setOnClickListener(v -> {
            // ask for delete post confirmation
            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
            builder.setTitle("Delete Post");
            builder.setMessage("Are you sure you want to delete this post? All of its replying post will also be deleted.");
            builder.setPositiveButton("Delete", (dialog, id) -> batchRemoval(post.getId(), post.getReplyId()));
            builder.setNegativeButton(android.R.string.cancel, (dialog, id) -> {
            });
            builder.show();
        });

        // load image if there is any
        if (post.getImagePaths() != null && post.getImagePaths().size() > 0) {
            for (String path : post.getImagePaths()) {
                if (!path.isEmpty()) {
                    // add image to the image view collection
                    Glide.with(context)
                            .asBitmap()
                            .load(path)
                            .into(new CustomTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    holder.imageView.addImage(resource);
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {

                                }
                            });
                }
            }
            holder.imageView.setVisibility(View.VISIBLE);
        }

        if (post.getReplyId() == null) {
            // if this post doesn't reply to any other post, hide the corresponding text view
            TextView replyToText = holder.replyToText;
            ((ViewGroup) replyToText.getParent()).removeView(replyToText);
            TextView replyId = holder.replyId;
            ((ViewGroup) replyId.getParent()).removeView(replyId);
        } else {
            // otherwise, show the corresponding reply id
            holder.replyId.setText("#".concat(post.getReplyId()));
        }
    }

    /**
     * Batch removing the targeted post and its children using DFS
     */
    private void batchRemoval(String id, String replyId) {
        // access database reference
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://confession-android-app-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference postNode = firebaseDatabase.getReference().child("submitted_posts");
        DatabaseReference reportedPostNode = firebaseDatabase.getReference().child("reported_posts");
        // perform deletion using dfs to delete itself and all of its children
        dfsDelete(id, postNode, reportedPostNode);
        // remove its presence in its parent repliedBy list
        if (replyId != null) {
            postNode.child(replyId).get().addOnCompleteListener(task -> {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    PostModel postModel = snapshot.getValue(PostModel.class);
                    List<String> newRepliedByList = Objects.requireNonNull(postModel).getRepliedBy();
                    newRepliedByList.remove(id);
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("repliedBy", newRepliedByList);
                    postNode.child(replyId).updateChildren(hashMap);
                }
            });
        }
        Toast.makeText(context, "Batch remove successfully!", Toast.LENGTH_SHORT).show();
    }

    private void dfsDelete(String id, DatabaseReference postNode, DatabaseReference reportedPostNode) {
        postNode.child(id).get().addOnCompleteListener(task -> {
            DataSnapshot snapshot = task.getResult();
            if (snapshot.exists()) {
                PostModel currPost = snapshot.getValue(PostModel.class);
                List<String> repliedByList = Objects.requireNonNull(currPost).getRepliedBy();
                postNode.child(currPost.getId()).removeValue();
                reportedPostNode.child(currPost.getId()).removeValue();
                if (repliedByList != null) {
                    for (String replyingId : repliedByList) {
                        dfsDelete(replyingId, postNode, reportedPostNode);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return reportedPostList.size();
    }

    public static class ReportedViewHolder extends RecyclerView.ViewHolder {
        private final TextView date;
        private final TextView time;
        private final TextView id;
        private final TextView replyToText;
        private final TextView replyId;
        private final TextView content;
        private final ImageButton imageMenuButton;
        private final ImageCollectionView imageView;

        public ReportedViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            date = itemView.findViewById(R.id.adapter_reported_post_date);
            time = itemView.findViewById(R.id.adapter_reported_post_time);
            id = itemView.findViewById(R.id.adapter_reported_post_id);
            replyToText = itemView.findViewById(R.id.adapter_reported_reply_to_text);
            replyId = itemView.findViewById(R.id.adapter_reported_reply_post_id);
            content = itemView.findViewById(R.id.adapter_reported_post_text);
            imageMenuButton = itemView.findViewById(R.id.adapter_reported_post_delete_button);
            imageView = itemView.findViewById(R.id.adapter_reported_post_image);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });
        }
    }
}
