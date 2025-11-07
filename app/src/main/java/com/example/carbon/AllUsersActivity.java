package com.example.carbon;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * Activity that displays a list of all users that are in the Firestore database.
 * Each user can be banned by pressing a button. If a user is banned,
 * a label saying "BANNED" will be shown instead of the button.
 */
public class AllUsersActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ArrayList<WaitlistEntrant> userList = new ArrayList<>();
    private AllUsersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        // Set up shared UI elements (header & bottom menu)
        UIHelper.setupHeaderAndMenu(this);

        RecyclerView recyclerView = findViewById(R.id.recycler_all_users);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AllUsersAdapter(userList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadAllUsers();
    }

    /**
     * Fetches all the users from the database and updates the RecyclerView.
     */
    private void loadAllUsers() {
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        userList.clear();
                        for (DocumentSnapshot doc : task.getResult()) {
                            WaitlistEntrant entrant = new WaitlistEntrant();
                            // Fetch user data
                            entrant.fetchUserFromDB(new WaitlistEntrant.UserCallback() {
                                @Override
                                public void onUserFetched(User user) {
                                    adapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onError(Exception e) {
                                    Log.e("AllUsers", "Error fetching user: " + e.getMessage());
                                }
                            });
                            // Assign userId manually (reflection because it's a private field)
                            try {
                                java.lang.reflect.Field field = entrant.getClass().getDeclaredField("userId");
                                field.setAccessible(true);
                                field.set(entrant, doc.getId());
                            } catch (Exception e) {
                                Log.e("AllUsers", "Reflection error: " + e.getMessage());
                            }

                            userList.add(entrant);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Failed to load users.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
