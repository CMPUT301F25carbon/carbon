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

public class AllUsersActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ArrayList<WaitlistEntrant> userList = new ArrayList<>();
    private AllUsersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        UIHelper.setupHeaderAndMenu(this);

        RecyclerView recyclerView = findViewById(R.id.recycler_all_users);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AllUsersAdapter(userList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadAllUsers();
    }

    private void loadAllUsers() {
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        userList.clear();
                        for (DocumentSnapshot doc : task.getResult()) {
                            WaitlistEntrant entrant = new WaitlistEntrant();
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
