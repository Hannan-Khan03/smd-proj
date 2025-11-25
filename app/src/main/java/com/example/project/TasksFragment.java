package com.example.project;

import android.os.Bundle;
import android.view.View;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TasksFragment extends Fragment {

    RecyclerView rv;
    CourseAdapter adapter;
    ArrayList<Course> list = new ArrayList<>();
    SupabaseClient sb;

    LinearLayout containerDailyTasks;

    String[] taskTitles = {
            "Answer using English",
            "Describe the image",
            "Record an audio message",
            "Practice pronunciation",
            "Summarize a paragraph",
            "Solve a quick quiz"
    };

    int[] maxCounts = { 5, 2, 3, 4, 3, 6 };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup c, @Nullable Bundle s) {

        View v = inflater.inflate(R.layout.fragment_tasks, c, false);

        sb = new SupabaseClient(requireContext());

        rv = v.findViewById(R.id.rvTasks);
        containerDailyTasks = v.findViewById(R.id.containerDailyTasks);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CourseAdapter(requireContext(), list, "Tasks");
        rv.setAdapter(adapter);

        generateDailyTasks();
        loadEnrolled();

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SupabaseSession.needsRefresh) {
            SupabaseSession.needsRefresh = false;
            loadEnrolled();
        }
    }

    // ----------------------------------------------------------
    // 🎯  R A N D O M   D A I L Y   T A S K S
    // ----------------------------------------------------------
    private void generateDailyTasks() {
        containerDailyTasks.removeAllViews();
        Random r = new Random();

        for (int i = 0; i < 3; i++) {

            String title = taskTitles[r.nextInt(taskTitles.length)];
            int max = maxCounts[r.nextInt(maxCounts.length)];
            int progress = r.nextInt(max + 1);

            View card = createTaskCard(title, progress, max);
            containerDailyTasks.addView(card);
        }
    }

    // Creates one small task box programmatically
    private View createTaskCard(String title, int progress, int total) {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 24, 24, 24);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);

        params.setMarginEnd(12);
        layout.setLayoutParams(params);

        // Style box
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#E8F5E9"));
        bg.setCornerRadius(20);
        bg.setStroke(2, Color.parseColor("#009688"));
        layout.setBackground(bg);

        // Title Text
        TextView tv = new TextView(requireContext());
        tv.setText(title);
        tv.setTextColor(Color.parseColor("#000000"));
        tv.setTextSize(14f);
        tv.setPadding(0, 0, 0, 8);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        layout.addView(tv);

        // Progress
        TextView tv2 = new TextView(requireContext());
        tv2.setText(progress + "/" + total);
        tv2.setTextColor(Color.parseColor("#444444"));
        tv2.setTextSize(13f);
        layout.addView(tv2);

        return layout;
    }

    // ----------------------------------------------------------
    // 📚  L O A D   E N R O L L E D   C O U R S E S
    // ----------------------------------------------------------
    private void loadEnrolled() {
        String email = SupabaseSession.sessionEmail;
        if (email == null || email.isEmpty()) {
            adapter.updateList(new ArrayList<>());
            return;
        }

        sb.fetchEnrolledCourseIds(email, new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() -> adapter.updateList(new ArrayList<>()));
            }

            @Override public void onResponse(Call call, Response response) throws IOException {

                String body = response.body().string();
                List<Integer> ids = SupabaseClient.parseCourseIdResponse(body);

                if (ids.isEmpty()) {
                    requireActivity().runOnUiThread(() -> adapter.updateList(new ArrayList<>()));
                    return;
                }

                sb.fetchCoursesByIds(ids, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        requireActivity().runOnUiThread(() -> adapter.updateList(new ArrayList<>()));
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String b = response.body().string();
                        List<Course> courses = SupabaseClient.parseCoursesResponse(b);
                        requireActivity().runOnUiThread(() -> {
                            list.clear();
                            list.addAll(courses);
                            adapter.updateList(new ArrayList<>(list));
                        });
                    }
                });
            }
        });
    }
}
