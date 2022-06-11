package me.xianglun.confession_app;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import java.util.LinkedList;
import java.util.Queue;

import me.xianglun.confession_app.model.PostModel;

public class App extends Application {
    // todo implement notification list in background thread
    public static final String CHANNEL_1_ID = "repliedChannel";
    public static final String CHANNEL_2_ID = "submittedChannel";
    // todo implement waiting list in background thread
    public static final Queue<PostModel> waitingList = new LinkedList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();

    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel repliedChannel = new NotificationChannel(CHANNEL_1_ID, "Someone replied your post", NotificationManager.IMPORTANCE_HIGH);
            repliedChannel.setDescription("Someone replied your post!");

            NotificationChannel submittedChannel = new NotificationChannel(CHANNEL_2_ID, "Post submitted", NotificationManager.IMPORTANCE_HIGH);
            submittedChannel.setDescription("Your post was submitted successfully!");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(repliedChannel);
            manager.createNotificationChannel(submittedChannel);
        }
    }
}
