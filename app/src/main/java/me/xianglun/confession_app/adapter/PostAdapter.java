package me.xianglun.confession_app.adapter;

import android.content.Context;
import android.content.Intent;
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
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import me.xianglun.confession_app.R;
import me.xianglun.confession_app.SubmitPostActivity;
import me.xianglun.confession_app.model.PostModel;
import pereira.agnaldo.previewimgcol.ImageCollectionView;

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
        PostModel post = postList.get(position);
        holder.postId.setText("#".concat(post.getId()));
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
                // todo send post to reported posts collection at database
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://confession-android-app-default-rtdb.asia-southeast1.firebasedatabase.app");
                DatabaseReference databaseReference = firebaseDatabase.getReference().child("reported_posts");

                Toast.makeText(context, "Post reported successfully.", Toast.LENGTH_SHORT).show();
                return true;
            });
            popupMenu.show();
        });
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

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        private final TextView date;
        private final TextView time;
        private final TextView postId;
        private final TextView replyToText;
        private final TextView replyId;
        private final TextView content;
        private final ImageButton imageMenuButton;
        private final ImageCollectionView imageView;

        public PostViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            date = itemView.findViewById(R.id.adapter_post_date);
            time = itemView.findViewById(R.id.adapter_post_time);
            postId = itemView.findViewById(R.id.adapter_post_id);
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
