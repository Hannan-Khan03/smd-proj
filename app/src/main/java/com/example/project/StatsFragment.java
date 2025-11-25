package com.example.project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import android.content.SharedPreferences;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import java.io.IOException;

public class StatsFragment extends Fragment {

    int courseId;
    String title, price, rating, imageName;

    ImageView imgCourse;
    TextView tvTitle, tvPrice, tvRating;
    Button btnDrop;
    ProgressBar p1, p2, p3;

    TextView dayMon, dayTue, dayWed, dayThu, dayFri, daySat, daySun;

    SupabaseClient sb;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup c, @Nullable Bundle s) {

        View v = inflater.inflate(R.layout.fragment_stats, c, false);

        sb = new SupabaseClient(requireContext());

        imgCourse = v.findViewById(R.id.imgStatsCourse);
        tvTitle = v.findViewById(R.id.tvStatsTitle);
        tvPrice = v.findViewById(R.id.tvStatsPrice);
        tvRating = v.findViewById(R.id.tvStatsRating);
        btnDrop = v.findViewById(R.id.btnDropCourse);

        p1 = v.findViewById(R.id.progressLesson);
        p2 = v.findViewById(R.id.progressQuiz);
        p3 = v.findViewById(R.id.progressProject);

        dayMon = v.findViewById(R.id.dayMon);
        dayTue = v.findViewById(R.id.dayTue);
        dayWed = v.findViewById(R.id.dayWed);
        dayThu = v.findViewById(R.id.dayThu);
        dayFri = v.findViewById(R.id.dayFri);
        daySat = v.findViewById(R.id.daySat);
        daySun = v.findViewById(R.id.daySun);

        Bundle args = getArguments();
        if (args == null) return v;

        courseId = args.getInt("courseId", -1);
        title = args.getString("title", "");
        price = args.getString("price", "");
        rating = args.getString("rating", "");
        imageName = args.getString("imageName", "");

        tvTitle.setText(title);
        tvPrice.setText(price);
        tvRating.setText(rating);

        int res = getResources().getIdentifier(imageName, "drawable", requireContext().getPackageName());
        if (res == 0) res = R.drawable.ic_placeholder;
        imgCourse.setImageResource(res);

        p1.setProgress(60);
        p2.setProgress(45);
        p3.setProgress(30);

        loadDayStates();

        setupToggle(dayMon, "Mon");
        setupToggle(dayTue, "Tue");
        setupToggle(dayWed, "Wed");
        setupToggle(dayThu, "Thu");
        setupToggle(dayFri, "Fri");
        setupToggle(daySat, "Sat");
        setupToggle(daySun, "Sun");

        btnDrop.setOnClickListener(v1 -> {
            AlertDialog.Builder b = new AlertDialog.Builder(requireContext());
            b.setTitle("Drop Course");
            b.setMessage("Are you sure you want to drop this course?");
            b.setPositiveButton("YES", (d, w) -> {

                sb.deleteEnrollment(SupabaseSession.sessionEmail, courseId, new Callback() {
                    @Override public void onFailure(Call call, IOException e) {}

                    @Override public void onResponse(Call call, Response response) throws IOException {
                        requireActivity().runOnUiThread(() ->
                                requireActivity().getSupportFragmentManager().popBackStack()
                        );
                    }
                });

            });
            b.setNegativeButton("NO", (d, w) -> d.dismiss());
            b.show();
        });

        return v;
    }

    private void setupToggle(TextView tv, String day) {
        tv.setOnClickListener(v -> {
            boolean selected = isDaySelected(day);
            if (selected) {
                tv.setBackgroundResource(R.drawable.rounded_field);
                tv.setTextColor(getResources().getColor(R.color.text_primary));
                saveDayState(day, false);
            } else {
                tv.setBackgroundResource(R.drawable.selected_outline);
                tv.setTextColor(getResources().getColor(R.color.text_muted));
                saveDayState(day, true);
            }
        });
    }

    private void saveDayState(String day, boolean state) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("StudyDays", requireContext().MODE_PRIVATE);
        prefs.edit().putBoolean(day, state).apply();
    }

    private boolean isDaySelected(String day) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("StudyDays", requireContext().MODE_PRIVATE);
        return prefs.getBoolean(day, false);
    }

    private void loadDayStates() {
        restoreState(dayMon, "Mon");
        restoreState(dayTue, "Tue");
        restoreState(dayWed, "Wed");
        restoreState(dayThu, "Thu");
        restoreState(dayFri, "Fri");
        restoreState(daySat, "Sat");
        restoreState(daySun, "Sun");
    }

    private void restoreState(TextView tv, String day) {
        boolean selected = isDaySelected(day);
        if (selected) {
            tv.setBackgroundResource(R.drawable.selected_outline);
            tv.setTextColor(getResources().getColor(R.color.text_muted));
        } else {
            tv.setBackgroundResource(R.drawable.rounded_field);
            tv.setTextColor(getResources().getColor(R.color.text_primary));
        }
    }
}
