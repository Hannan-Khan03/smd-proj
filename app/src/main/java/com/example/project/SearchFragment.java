package com.example.project;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
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

public class SearchFragment extends Fragment {

    RecyclerView rvSearchCourses;
    EditText etSearch;
    ImageView btnFilter;

    ArrayList<Course> list = new ArrayList<>();
    ArrayList<Course> originalList = new ArrayList<>();
    CourseAdapter adapter;

    SupabaseClient sb;

    String activeFilter = "All";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle s) {
        View v = inflater.inflate(R.layout.fragment_search, container, false);

        sb = new SupabaseClient(requireContext());
        etSearch = v.findViewById(R.id.etSearch);
        btnFilter = v.findViewById(R.id.btnFilter);
        rvSearchCourses = v.findViewById(R.id.rvSearchCourses);

        rvSearchCourses.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CourseAdapter(requireContext(), list, "Search");
        rvSearchCourses.setAdapter(adapter);

        loadCourses();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { filter(); }
        });

        btnFilter.setOnClickListener(v1 -> openFilterDialog());

        return v;
    }

    private void openFilterDialog() {
        String[] options = {"All", "Programming", "Marketing", "Stats"};

        new AlertDialog.Builder(requireContext())
                .setTitle("Filter By Category")
                .setItems(options, (d, i) -> {
                    activeFilter = options[i];
                    filter();
                })
                .show();
    }

    private void filter() {
        String q = etSearch.getText().toString().toLowerCase().trim();
        ArrayList<Course> filtered = new ArrayList<>();

        for (Course c : originalList) {

            boolean matchesSearch =
                    c.title.toLowerCase().contains(q);

            boolean matchesCategory =
                    activeFilter.equals("All") ||
                            c.type.equalsIgnoreCase(activeFilter);

            if (matchesSearch && matchesCategory) {
                filtered.add(c);
            }
        }

        list.clear();
        list.addAll(filtered);
        adapter.updateList(list);
    }

    private void loadCourses() {
        String email = SupabaseSession.sessionEmail;

        if (email == null || email.isEmpty()) {
            sb.fetchAllCourses(new Callback() {
                @Override public void onFailure(Call call, IOException e) {}

                @Override public void onResponse(Call call, Response response) throws IOException {
                    String json = response.body().string();
                    List<Course> out = SupabaseClient.parseCoursesResponse(json);

                    requireActivity().runOnUiThread(() -> {
                        originalList.clear();
                        originalList.addAll(out);
                        list.clear();
                        list.addAll(out);
                        adapter.updateList(list);
                    });
                }
            });
            return;
        }

        sb.fetchEnrolledCourseIds(email, new Callback() {
            @Override public void onFailure(Call call, IOException e) { loadAllFallback(); }

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

                        requireActivity().runOnUiThread(() -> {
                            originalList.clear();
                            originalList.addAll(filtered);
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

                requireActivity().runOnUiThread(() -> {
                    originalList.clear();
                    originalList.addAll(out);
                    list.clear();
                    list.addAll(out);
                    adapter.updateList(list);
                });
            }
        });
    }
}
