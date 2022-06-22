package me.xianglun.confession_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import me.xianglun.confession_app.R;
import me.xianglun.confession_app.model.PostModel;

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
        holder.setIsRecyclable(false);
        holder.imageView.setImageDrawable(null);
        holder.id.setText("#".concat(post.getId()));
        holder.content.setText(post.getContent());
        holder.time.setText(post.getTime());
        holder.date.setText(post.getDate());
        holder.imageMenuButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, v);
            popupMenu.inflate(R.menu.reported_post_menu);
            popupMenu.getMenu().findItem(R.id.delete_post_button).setOnMenuItemClickListener(item -> {
                // ask for delete post confirmation
                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
                builder.setTitle("Delete Post");
                builder.setMessage("Are you sure you want to delete this post? All of its replying post will also be deleted.");
                builder.setPositiveButton("Delete", (dialog, id) -> batchRemoval(post.getId(), post.getReplyId()));
                builder.setNegativeButton(android.R.string.cancel, (dialog, id) -> {
                });
                builder.show();
                return true;
            });
            popupMenu.getMenu().findItem(R.id.remove_from_reported_button).setOnMenuItemClickListener(item -> {
                // remove the post from reported posts collection
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://confession-android-app-default-rtdb.asia-southeast1.firebasedatabase.app");
                firebaseDatabase.getReference().child("reported_posts").child(post.getId()).removeValue().addOnCompleteListener(task -> Toast.makeText(context, "Post successfully removed from the reported post list.", Toast.LENGTH_SHORT).show());
                return true;
            });
            popupMenu.show();
        });

        // load image if there is any
        if (post.getImagePaths() != null && post.getImagePaths().size() > 0) {
            String path = post.getImagePaths().get(0);
            if (!path.isEmpty()) {
                // add image to the image view collection
                Glide.with(context)
                        .asBitmap()
                        .load(path)
                        .into(holder.imageView);
            }

            holder.imageView.setVisibility(View.VISIBLE);

        } else {
            // if there is no images, remove the previous image
            Glide.with(context).clear(holder.imageView);
        }

        if (post.getReplyId() == null) {
            // if this post doesn't reply to any other post, hide the corresponding text view
            TextView replyToText = holder.replyToText;
            ViewParent viewParent = replyToText.getParent();
            if (viewParent instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) viewParent;
                viewGroup.removeView(replyToText);
                TextView replyId = holder.replyId;
                viewGroup.removeView(replyId);
            }
        } else {
            // otherwise, show the corresponding reply id
            holder.replyId.setText("#".concat(post.getReplyId()));
        }
    }

    /**
     * Batch removing the targeted post and its children using DFS
     */
    private void batchRemoval(String id, String replyId) {
        // access database & storage reference
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://confession-android-app-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference postNode = firebaseDatabase.getReference().child("submitted_posts");
        DatabaseReference reportedPostNode = firebaseDatabase.getReference().child("reported_posts");
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        // perform deletion using dfs to delete itself and all of its children
        dfsDelete(id, postNode, reportedPostNode, firebaseStorage);
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
        private final ImageView imageView;

        public ReportedViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            date = itemView.findViewById(R.id.adapter_reported_post_date);
            time = itemView.findViewById(R.id.adapter_reported_post_time);
            id = itemView.findViewById(R.id.adapter_reported_post_id);
            replyToText = itemView.findViewById(R.id.adapter_reported_reply_to_text);
            replyId = itemView.findViewById(R.id.adapter_reported_reply_post_id);
            content = itemView.findViewById(R.id.adapter_reported_post_text);
            imageMenuButton = itemView.findViewById(R.id.adapter_reported_post_option_button);
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
