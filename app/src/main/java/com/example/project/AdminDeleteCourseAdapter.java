package com.example.project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class AdminDeleteCourseAdapter extends RecyclerView.Adapter<AdminDeleteCourseAdapter.Holder> {

    public interface OnCourseClickListener {
        void onCourseClick(Course c, int position);
    }

    ArrayList<Course> list;
    OnCourseClickListener listener;

    public AdminDeleteCourseAdapter(ArrayList<Course> list, OnCourseClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Course c = list.get(position);
        h.tvTitle.setText(c.title);
        h.tvPrice.setText(c.price);
        h.tvRating.setText(c.rating);
        int resId = h.itemView.getContext().getResources()
                .getIdentifier(c.imageName, "drawable", h.itemView.getContext().getPackageName());
        if (resId == 0) resId = R.drawable.ic_placeholder;
        h.imgCourse.setImageResource(resId);

        h.itemView.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            listener.onCourseClick(list.get(pos), pos);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        ImageView imgCourse;
        TextView tvTitle, tvPrice, tvRating;
        Holder(View v) {
            super(v);
            imgCourse = v.findViewById(R.id.imgCourse);
            tvTitle = v.findViewById(R.id.tvCourseTitle);
            tvPrice = v.findViewById(R.id.tvCoursePrice);
            tvRating = v.findViewById(R.id.tvCourseRatings);
        }
    }
}
