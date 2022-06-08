package me.xianglun.confession_app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import me.xianglun.confession_app.model.PostModel;

public class SubmitPostActivity extends AppCompatActivity {

    // declare the reference variable
    private String date, time, replyId;
    private Toolbar mToolbar;
    private CardView mProgressIndicatorCardView;
    private TextView dateAndTimeLabel;
    private LinearLayout linearLayout;
    private TextView replyPostIdText;
    private EditText contentText;
    private LinearLayoutCompat linearLayoutCompat;
    private DatabaseReference databaseReference;
    private Intent intent;
    private List<String> imagePaths;
    private long postCountInDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_post);

        // instantiate the reference variable
        mToolbar = findViewById(R.id.create_post_toolbar);
        mProgressIndicatorCardView = findViewById(R.id.create_post_progress_card_view);
        dateAndTimeLabel = findViewById(R.id.date_and_time_label);
        linearLayout = findViewById(R.id.new_post_linear_layout);
        replyPostIdText = findViewById(R.id.reply_post_id_text_view);
        contentText = findViewById(R.id.post_content_edit_text);
        linearLayoutCompat = findViewById(R.id.reply_post_id_layout);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://confession-android-app-default-rtdb.asia-southeast1.firebasedatabase.app");
        databaseReference = firebaseDatabase.getReference();
        imagePaths = new ArrayList<>();
        intent = getIntent();
        replyId = intent.getStringExtra("replyId");

        // add listener to keep track of the post count in database
        databaseReference.child("submitted_posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    postCountInDatabase = snapshot.getChildrenCount();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // set up the action bar
        mToolbar.setTitle("Create post");
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // set up date and time
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("E, dd-MMM-yyyy   hh:mm a", Locale.getDefault());
        String formattedDate = df.format(c);
        dateAndTimeLabel.setText(formattedDate);
        df = new SimpleDateFormat("E, dd-MMM-yyyy", Locale.getDefault());
        date = df.format(c);
        df = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        time = df.format(c);

        // decide to show/hide the reply id textView
        if (replyId == null) {
            // hide it
            linearLayout.removeView(linearLayoutCompat);
        } else {
            // populate it with the reply id instead
            replyPostIdText.setText("#".concat(replyId));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.create_post_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.submit_post_menu_item) {
            if (!contentText.getText().toString().isEmpty()) {
                // Disable any clicks
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                // Show progress indicator
                mProgressIndicatorCardView.setVisibility(View.VISIBLE);
                savePostToDatabase();
            } else {
                Toast.makeText(this, "You cannot submit a post without texts.", Toast.LENGTH_SHORT).show();
            }
            return true;

        } else if (item.getItemId() == R.id.add_photo_menu_item) {
            // let user selects images
            displayImagePickerDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void savePostToDatabase() {
        // get reference to firebase storage
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference();

        // get user inputs
        String content = contentText.getText().toString();

        // set up tasks list
        List<Task<UploadTask.TaskSnapshot>> storeImageOnDatabaseTaskList = new ArrayList<>();
        List<Task<Uri>> getUriTaskList = new ArrayList<>();

        // populate the image path list
        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            if (linearLayout.getChildAt(i) instanceof RelativeLayout) {
                RelativeLayout relativeLayout = (RelativeLayout) linearLayout.getChildAt(i);
                ImageView imageView = relativeLayout.findViewById(R.id.post_image);
                if (imageView.getTag() instanceof Uri) {
                    Uri uri = (Uri) imageView.getTag();
                    Task<UploadTask.TaskSnapshot> storeImageOnDatabaseTask = storageReference.child("postImages").child(uri.getLastPathSegment()).putFile(uri).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Task<Uri> getUriTask = Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(task.getResult()).getMetadata()).getReference()).getDownloadUrl().addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    String imageURL = Objects.requireNonNull(task1.getResult()).toString();
                                    imagePaths.add(imageURL);
                                }
                            }).addOnFailureListener(e -> Toast.makeText(SubmitPostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
                            getUriTaskList.add(getUriTask);
                        }
                    }).addOnFailureListener(e -> Toast.makeText(SubmitPostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
                    storeImageOnDatabaseTaskList.add(storeImageOnDatabaseTask);
                }
            }
        }

        // staging and pushing data
        Tasks.whenAllComplete(storeImageOnDatabaseTaskList).addOnCompleteListener(task -> Tasks.whenAllComplete(getUriTaskList).addOnCompleteListener(task12 -> {
            // set up current post id in String format
            String currPostId = String.format(Locale.US, "CFS%06d", postCountInDatabase);

            // get reference to database
            DatabaseReference postNode = databaseReference.child("submitted_posts").child(currPostId);

            // staging the data
            HashMap<String, Object> postMap = new HashMap<>();
            postMap.put("id", currPostId);
            postMap.put("replyId", replyId);
            postMap.put("date", date);
            postMap.put("time", time);
            postMap.put("content", content);
            postMap.put("imagePaths", imagePaths);

            // record the current post id as the children of the original post
            if (replyId != null) {
                DatabaseReference repliedPostNode = databaseReference.child("submitted_posts").child(replyId);
                repliedPostNode.get().addOnCompleteListener(dataSnapshotTask -> {
                    PostModel repliedPost = dataSnapshotTask.getResult().getValue(PostModel.class);
                    List<String> repliedBy = Objects.requireNonNull(repliedPost).getRepliedBy();
                    if (repliedBy == null) {
                        repliedBy = new ArrayList<>();
                    }
                    repliedBy.add(currPostId);
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("repliedBy", repliedBy);
                    repliedPostNode.updateChildren(hashMap);
                });
            }

            // push the staged data to the database
            postNode.updateChildren(postMap).addOnCompleteListener(voidTask -> {
                if (voidTask.isSuccessful()) {
                    // Hide progress indicator
                    mProgressIndicatorCardView.setVisibility(View.INVISIBLE);
                    Toast.makeText(SubmitPostActivity.this, "Post submitted successfully.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SubmitPostActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(SubmitPostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            });
        }));
    }

    private void displayImagePickerDialog() {
        ImagePicker.with(this).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                addDiaryImageTemplate(uri);
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    private void addDiaryImageTemplate(Uri data) {
        LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        linearParams.setMargins(convertDpToPx(getApplicationContext(), 10), convertDpToPx(getApplicationContext(), 5), convertDpToPx(getApplicationContext(), 10), convertDpToPx(getApplicationContext(), 5));

        View template = getLayoutInflater().inflate(R.layout.template_post_image, linearLayout, false);
        ImageView image = template.findViewById(R.id.post_image);
        image.setImageURI(data);
        image.setTag(data);
        FloatingActionButton deleteBtn = template.findViewById(R.id.delete_image_button);
        deleteBtn.setOnClickListener(v -> {
            ViewParent parent = deleteBtn.getParent();
            linearLayout.removeView((View) parent);
        });
        linearLayout.addView(template, linearParams);
    }

    private int convertDpToPx(Context context, float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }
}