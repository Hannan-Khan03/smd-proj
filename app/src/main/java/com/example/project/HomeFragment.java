package com.example.project;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeFragment extends Fragment {

    RecyclerView rv;
    CourseAdapter adapter;
    ArrayList<Course> list = new ArrayList<>();
    EditText etSearch;
    TextView tvUsername;
    ImageView imgAvatar;
    SupabaseClient sb;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle s) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        sb = new SupabaseClient(requireContext());

        rv = v.findViewById(R.id.rvCourses);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        etSearch = v.findViewById(R.id.etHomeSearch);
        tvUsername = v.findViewById(R.id.tvUsername);
        imgAvatar = v.findViewById(R.id.imgAvatar);

        tvUsername.setText("Welcome back, " + SupabaseSession.sessionName);
        imgAvatar.setImageResource(R.drawable.ic_account);

        adapter = new CourseAdapter(requireContext(), list, "Home");
        rv.setAdapter(adapter);

        loadCourses();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { filter(s.toString()); }
        });

        return v;
    }

    private void loadCourses() {
        String email = SupabaseSession.sessionEmail;

        if (email == null || email.isEmpty()) {
            loadAllFallback();
            return;
        }

        sb.fetchEnrolledCourseIds(email, new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                loadAllFallback();
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                List<Integer> enrolledIds = SupabaseClient.parseCourseIdResponse(body);
                Set<Integer> set = new HashSet<>(enrolledIds);

                sb.fetchAllCourses(new Callback() {
                    @Override public void onFailure(Call call, IOException e) { loadAllFallback(); }

                    @Override public void onResponse(Call call, Response response) throws IOException {
                        String json = response.body().string();
                        List<Course> out = SupabaseClient.parseCoursesResponse(json);

                        List<Course> filtered = new ArrayList<>();
                        for (Course c : out) {
                            if (!set.contains(c.id)) filtered.add(c);
                        }

                        if (!isAdded()) return;

                        requireActivity().runOnUiThread(() -> {
                            list.clear();
                            list.addAll(filtered);
                            adapter.updateList(list);
                        });
                    }
                });
            }
        });
    }

    private void loadAllFallback() {
        sb.fetchAllCourses(new Callback() {
            @Override public void onFailure(Call call, IOException e) {}

            @Override public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                List<Course> out = SupabaseClient.parseCoursesResponse(json);

                if (!isAdded()) return;

                requireActivity().runOnUiThread(() -> {
                    list.clear();
                    list.addAll(out);
                    adapter.updateList(list);
                });
            }
        });
    }

    private void filter(String q) {
        ArrayList<Course> filtered = new ArrayList<>();
        for (Course c : list) {
            if (c.title.toLowerCase().contains(q.toLowerCase())) filtered.add(c);
        }
        adapter.updateList(filtered);
    }
}
