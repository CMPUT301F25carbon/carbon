package com.example.carbon;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.VH> {

    private List<User> users = new ArrayList<>();

    public void updateList(List<User> newList) {
        users.clear();
        users.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int i) {
        User u = users.get(i);
        h.tvName.setText(u.getFirstName() + " " + u.getLastName());
        h.tvEmail.setText(u.getEmail());
        h.tvPhone.setText(u.getPhoneNo());
        h.tvRole.setText(u.getRole());
    }

    @Override public int getItemCount() { return users.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvPhone, tvRole;
        VH(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_name);
            tvEmail = v.findViewById(R.id.tv_email);
            tvPhone = v.findViewById(R.id.tv_phone);
            tvRole = v.findViewById(R.id.tv_role);
        }
    }
}
