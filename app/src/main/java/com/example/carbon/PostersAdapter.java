package com.example.carbon;

/**
 * RecyclerView adapter for event posters, supporting deletion and preview taps.
 * Outstanding issues: assumes external logic handles storage cleanup.
 */

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

public class PostersAdapter extends RecyclerView.Adapter<PostersAdapter.ViewHolder> {

    private final List<Poster> posters = new ArrayList<>();
    private OnDeleteClickListener deleteListener;
    private boolean isEditMode = false;

    public interface OnDeleteClickListener {
        void onDelete(Poster poster, int position);
    }

    public void setDeleteListener(OnDeleteClickListener listener) {
        this.deleteListener = listener;
    }

    public void setEditMode(boolean editMode) {
        isEditMode = editMode;
        notifyDataSetChanged();
    }

    public void updateList(List<Poster> newList) {
        posters.clear();
        if (newList != null) posters.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_poster, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Poster poster = posters.get(position);

        h.progress.setVisibility(View.VISIBLE);

        Glide.with(h.itemView.getContext())
                .load(poster.getImageUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .timeout(15000)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(h.img);

        h.progress.setVisibility(View.GONE);

        // Show delete button only in admin/edit mode
        h.btnDelete.setVisibility(isEditMode ? View.VISIBLE : View.GONE);

        h.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(poster, h.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return posters.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        ImageButton btnDelete;
        ProgressBar progress;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img_poster);
            btnDelete = itemView.findViewById(R.id.btn_delete_poster);
            progress = itemView.findViewById(R.id.progress_poster);
        }
    }
}
