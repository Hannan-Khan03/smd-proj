package com.example.project;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {

    private final Context context;
    private ArrayList<Course> courseList;
    private final String source;

    public CourseAdapter(Context context, ArrayList<Course> courseList, String source) {
        this.context = context;
        this.courseList = courseList;
        this.source = source;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_course, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Course c = courseList.get(position);

        holder.tvCourseTitle.setText(c.title);
        holder.tvCoursePrice.setText(c.price);
        holder.tvCourseRatings.setText(c.rating);

        int resId = context.getResources().getIdentifier(c.imageName, "drawable", context.getPackageName());
        if (resId == 0) resId = R.drawable.ic_placeholder;
        holder.imgCourse.setImageResource(resId);

        holder.itemView.setOnClickListener(v -> {

            if ("Tasks".equals(source)) {

                StatsFragment statsFragment = new StatsFragment();
                Bundle b = new Bundle();

                b.putInt("courseId", c.id);
                b.putString("title", c.title);
                b.putString("price", c.price);
                b.putString("rating", c.rating);
                b.putString("imageName", c.imageName);

                statsFragment.setArguments(b);

                if (context instanceof MainActivity) {
                    MainActivity a = (MainActivity) context;
                    a.getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, statsFragment)
                            .addToBackStack(null)
                            .commit();
                }
            }

            else {
                Intent i = new Intent(context, CourseDetailsActivity.class);
                i.putExtra("courseId", c.id);
                i.putExtra("title", c.title);
                i.putExtra("price", c.price);
                i.putExtra("rating", c.rating);
                i.putExtra("imageName", c.imageName);
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public void updateList(ArrayList<Course> filtered) {
        this.courseList = filtered;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCourse;
        TextView tvCourseTitle, tvCoursePrice, tvCourseRatings;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCourse = itemView.findViewById(R.id.imgCourse);
            tvCourseTitle = itemView.findViewById(R.id.tvCourseTitle);
            tvCoursePrice = itemView.findViewById(R.id.tvCoursePrice);
            tvCourseRatings = itemView.findViewById(R.id.tvCourseRatings);
        }
    }
}
