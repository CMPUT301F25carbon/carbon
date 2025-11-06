package com.example.carbon;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class AllUsersAdapter extends RecyclerView.Adapter<AllUsersAdapter.ViewHolder> {
    private List<WaitlistEntrant> entrants;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Runnable reloadCallback; // om refresh mogelijk te maken

    public AllUsersAdapter(List<WaitlistEntrant> entrants) {
        this.entrants = entrants;
        this.reloadCallback = reloadCallback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WaitlistEntrant entrant = entrants.get(position);

        entrant.fetchUserFromDB(new WaitlistEntrant.UserCallback() {
            @Override
            public void onUserFetched(User user) {
                holder.name.setText(user.getFirstName() + " " + user.getLastName());
                holder.email.setText(user.getEmail());
                holder.role.setText("Role: " + user.getRole());

                if (user.isBanned()) {
                    holder.bannedLabel.setVisibility(View.VISIBLE);
                    holder.banButton.setVisibility(View.GONE);
                } else {
                    holder.bannedLabel.setVisibility(View.GONE);
                    holder.banButton.setVisibility(View.VISIBLE);

                    holder.banButton.setOnClickListener(v -> {
                        db.collection("users").document(entrant.getUserId())
                                .update("banned", true)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(v.getContext(), "User banned", Toast.LENGTH_SHORT).show();
                                    if (reloadCallback != null) {
                                        reloadCallback.run(); // herlaad lijst
                                    }
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(v.getContext(), "Error banning user", Toast.LENGTH_SHORT).show());
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("AllUsersAdapter", "Failed to fetch user data: " + e.getMessage());
            }
        });
    }

    @Override
    public int getItemCount() {
        return entrants.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, email, role, bannedLabel;
        Button banButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.user_name);
            email = itemView.findViewById(R.id.user_email);
            role = itemView.findViewById(R.id.user_role);
            banButton = itemView.findViewById(R.id.ban_button);
            bannedLabel = itemView.findViewById(R.id.banned_label);
        }
    }
}
