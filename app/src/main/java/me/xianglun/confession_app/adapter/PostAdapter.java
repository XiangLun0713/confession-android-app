package me.xianglun.confession_app.adapter;

import android.content.Context;
import android.content.Intent;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

import me.xianglun.confession_app.R;
import me.xianglun.confession_app.SubmitPostActivity;
import me.xianglun.confession_app.model.PostModel;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private final Context context;
    private final ArrayList<PostModel> postList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public PostAdapter(Context context, ArrayList<PostModel> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public PostAdapter.PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate the view holder
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.template_post, parent, false);
        return new PostViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.PostViewHolder holder, int position) {
        // setting up the view holder
        holder.setIsRecyclable(false);
        PostModel post = postList.get(position);
        holder.imageView.setImageDrawable(null);
        holder.id.setText("#".concat(post.getId()));
        holder.content.setText(post.getContent());
        holder.time.setText(post.getTime());
        holder.date.setText(post.getDate());
        holder.imageMenuButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, v);
            popupMenu.inflate(R.menu.post_menu);
            popupMenu.getMenu().findItem(R.id.post_reply_menu).setOnMenuItemClickListener(item -> {
                // when reply is clicked, navigate user to create new page
                Intent intent = new Intent(context, SubmitPostActivity.class);
                intent.putExtra("replyId", post.getId());
                v.getContext().startActivity(intent);
                return true;
            });
            popupMenu.getMenu().findItem(R.id.post_report_menu).setOnMenuItemClickListener(item -> {
                // ask for report post confirmation
                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
                builder.setTitle("Report Post");
                builder.setMessage("Are you sure you want to report this post?");
                builder.setPositiveButton("Report", (dialog, id) -> reportPost(post));
                builder.setNegativeButton(android.R.string.cancel, (dialog, id) -> {
                });
                builder.show();
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

    private void reportPost(PostModel post) {
        // record the current post at database reported posts collection
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://confession-android-app-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference databaseReference = firebaseDatabase.getReference().child("reported_posts");
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", post.getId());
        hashMap.put("content", post.getContent());
        hashMap.put("date", post.getDate());
        hashMap.put("time", post.getTime());
        hashMap.put("replyId", post.getReplyId());
        hashMap.put("repliedBy", post.getRepliedBy());
        hashMap.put("imagePaths", post.getImagePaths());
        databaseReference.child(post.getId()).updateChildren(hashMap);

        // display post reported successfully
        Toast.makeText(context, "Post reported successfully.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        private final TextView date;
        private final TextView time;
        private final TextView id;
        private final TextView replyToText;
        private final TextView replyId;
        private final TextView content;
        private final ImageButton imageMenuButton;
        private final ImageView imageView;

        public PostViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            date = itemView.findViewById(R.id.adapter_post_date);
            time = itemView.findViewById(R.id.adapter_post_time);
            id = itemView.findViewById(R.id.adapter_post_id);
            replyToText = itemView.findViewById(R.id.adapter_reply_to_text);
            replyId = itemView.findViewById(R.id.adapter_reply_post_id);
            content = itemView.findViewById(R.id.adapter_post_text);
            imageMenuButton = itemView.findViewById(R.id.adapter_post_menu_button);
            imageView = itemView.findViewById(R.id.adapter_post_image);

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
