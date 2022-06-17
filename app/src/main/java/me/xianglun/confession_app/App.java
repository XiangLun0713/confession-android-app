package me.xianglun.confession_app;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import me.xianglun.confession_app.model.PostModel;

public class App extends Application {
    // create notification channel
    public static final String CHANNEL_1_ID = "publishChannel";
    private NotificationManagerCompat notificationManager;
    // App level post waiting list
    public static final Queue<PostModel> waitingList = new LinkedList<>();

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannels();
        notificationManager = NotificationManagerCompat.from(this);

        // create background thread for waiting list
        BackgroundRunnable backgroundRunnable = new BackgroundRunnable();
        new Thread(backgroundRunnable).start();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel publishChannel = new NotificationChannel(CHANNEL_1_ID, "Post published", NotificationManager.IMPORTANCE_HIGH);
            publishChannel.setDescription("You will be notify if your post was published successfully.");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(publishChannel);
        }
    }

    class BackgroundRunnable implements Runnable {
        @Override
        public void run() {
            Timer timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                long elapsedSec = 0;

                @Override
                public void run() {
                    int waitingListSize = waitingList.size();
                    if (waitingListSize == 0) {
                        elapsedSec = 0;
                    } else if (waitingListSize > 10) {
                        // wait for 5 min (300 sec)
                        if (elapsedSec >= 5) {
                            // if elapse time is 5 min or more
                            PostModel post = waitingList.poll();
                            if (post != null) {
                                savePostToDatabase(post);
                            }
                            elapsedSec = 0;
                        }
                    } else if (waitingListSize > 5) {
                        // wait for 10 min (600 sec)
                        if (elapsedSec >= 10) {
                            // if elapse time is 10 min or more
                            PostModel post = waitingList.poll();
                            if (post != null) {
                                savePostToDatabase(post);
                            }
                            elapsedSec = 0;
                        }
                    } else {
                        // wait for 15 min (900 sec)
                        if (elapsedSec >= 15) {
                            // if elapse time is 15 min or more
                            PostModel post = waitingList.poll();
                            if (post != null) {
                                savePostToDatabase(post);
                            }
                            elapsedSec = 0;
                        }
                    }
                    // increase elapse time for each loop
                    elapsedSec++;
                }
            };
            // call timerTask every 1 sec
            timer.scheduleAtFixedRate(timerTask, 0, 1000);
        }

    }

    private void savePostToDatabase(PostModel post) {
        // get reference to database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://confession-android-app-default-rtdb.asia-southeast1.firebasedatabase.app").getReference();
        DatabaseReference postNode = databaseReference.child("submitted_posts").child(post.getId());

        // staging data before pushing it to database
        HashMap<String, Object> postMap = new HashMap<>();
        postMap.put("id", post.getId());
        postMap.put("replyId", post.getReplyId());
        postMap.put("date", post.getDate());
        postMap.put("time", post.getTime());
        postMap.put("content", post.getContent());
        postMap.put("imagePaths", post.getImagePaths());

        // record the current post id as the children of the original post
        if (post.getReplyId() != null) {
            DatabaseReference repliedPostNode = databaseReference.child("submitted_posts").child(post.getReplyId());
            repliedPostNode.get().addOnCompleteListener(dataSnapshotTask -> {
                PostModel repliedPost = dataSnapshotTask.getResult().getValue(PostModel.class);
                List<String> repliedBy = Objects.requireNonNull(repliedPost).getRepliedBy();
                if (repliedBy == null) {
                    repliedBy = new ArrayList<>();
                }
                repliedBy.add(post.getId());
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("repliedBy", repliedBy);
                repliedPostNode.updateChildren(hashMap);
            });
        }

        // push the staged data to the database
        postNode.updateChildren(postMap).addOnCompleteListener(voidTask -> {
            if (voidTask.isSuccessful()) {
                // notify the user that their post is published
                Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                        .setSmallIcon(R.drawable.ic_message_icon_24)
                        .setContentTitle("Post Published")
                        .setContentText("Your post " + post.getId() + " was published successfully!")
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .build();

                notificationManager.notify(1, notification);
            }
        }).addOnFailureListener(e -> {
            // notify the user that their post is failed to publish
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                    .setSmallIcon(R.drawable.ic_message_icon_24)
                    .setContentTitle("Post Publish Failed")
                    .setContentText("Oh no! Your post failed to publish.")
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .build();

            notificationManager.notify(2, notification);
        });
    }
}