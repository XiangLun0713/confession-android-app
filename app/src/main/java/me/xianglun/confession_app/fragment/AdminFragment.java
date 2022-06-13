package me.xianglun.confession_app.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import me.xianglun.confession_app.R;
import me.xianglun.confession_app.adapter.AdminAdapter;
import me.xianglun.confession_app.model.AdminModel;

public class AdminFragment extends Fragment {

    private View view;
    private FloatingActionButton mFab;
    private CardView mCardView;
    private RecyclerView mRecyclerView;
    private AdminAdapter adminAdapter;
    private ArrayList<AdminModel> adminList;
    private EditText usernameEditText, emailEditText, passwordEditText;
    private FirebaseAuth mFirebaseAuth;

    public AdminFragment() {
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
        view = inflater.inflate(R.layout.fragment_admin, container, false);
        mRecyclerView = view.findViewById(R.id.admin_list_recycler_view);
        mCardView = view.findViewById(R.id.add_admin_member_progress_card_view);
        mFab = view.findViewById(R.id.add_admin_member_button);
        mFirebaseAuth = FirebaseAuth.getInstance();

        mFab.setOnClickListener(v -> {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme);
            dialog.setTitle("Adding Admin Member");
            LinearLayout layout = new LinearLayout(getContext());
            layout.setOrientation(LinearLayout.VERTICAL);

            usernameEditText = new EditText(getContext());
            usernameEditText.setHint("Username");
            usernameEditText.setInputType(InputType.TYPE_CLASS_TEXT);
            layout.addView(usernameEditText);

            emailEditText = new EditText(getContext());
            emailEditText.setHint("Email address");
            emailEditText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            layout.addView(emailEditText);

            passwordEditText = new EditText(getContext());
            passwordEditText.setHint("Password");
            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            layout.addView(passwordEditText);
            layout.setPadding(50, 0, 50, 0);
            dialog.setView(layout);

            dialog.setPositiveButton("Add", (dialog1, which) -> registerAdmin()).setNegativeButton("Cancel", (dialog12, which) -> dialog12.cancel());

            dialog.show();
        });

        // load all admin member
        loadAdminList();

        return view;
    }

    private void loadAdminList() {
        adminList = new ArrayList<>();
        adminAdapter = new AdminAdapter(getContext(), adminList);
        mRecyclerView.setAdapter(adminAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://confession-android-app-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference databaseReference = firebaseDatabase.getReference().child("Admin");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                adminList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        AdminModel admin = snap.getValue(AdminModel.class);
                        adminList.add(admin);
                    }
                }
                System.out.println(adminList);
                adminAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void registerAdmin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(getContext(), "Register failed. Please provide a valid email address.", Toast.LENGTH_SHORT).show();
        } else if (password.isEmpty()) {
            Toast.makeText(getContext(), "Register failed. Please provide a password.", Toast.LENGTH_SHORT).show();
        } else if (password.length() < 6) {
            Toast.makeText(getContext(), "Register failed. Please provide a password with a minimum length of 6 characters.", Toast.LENGTH_SHORT).show();
        } else if (username.isEmpty()) {
            Toast.makeText(getContext(), "Register failed. Please provide a username.", Toast.LENGTH_SHORT).show();
        } else {
            // creating new user through firebase
            mCardView.setVisibility(View.VISIBLE);
            mFirebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DatabaseReference adminNode = FirebaseDatabase.getInstance("https://confession-android-app-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("Admin").push();
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("id", adminNode.getKey());
                    hashMap.put("username", username);
                    hashMap.put("email", email);
                    hashMap.put("password", password);
                    adminNode.updateChildren(hashMap).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            mCardView.setVisibility(View.INVISIBLE);
                            Toast.makeText(getContext(), "Admin added successfully!", Toast.LENGTH_LONG).show();

                        } else {
                            Toast.makeText(getContext(), "Failed to register the admin. Please try again.", Toast.LENGTH_LONG).show();
                            mCardView.setVisibility(View.GONE);
                        }
                    });
                } else {
                    Toast.makeText(getContext(), Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                    mCardView.setVisibility(View.GONE);
                }
            });
        }
    }
}