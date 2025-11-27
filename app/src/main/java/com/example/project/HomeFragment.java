package com.example.project;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import okhttp3.*;
import java.io.IOException;
import java.util.*;

public class HomeFragment extends Fragment {

    RecyclerView rv;
    CourseAdapter adapter;
    ArrayList<Course> list = new ArrayList<>();
    EditText etSearch;
    TextView tvUsername;
    ImageView imgAvatar, imgBell;
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
        imgBell = v.findViewById(R.id.imgBell);

        tvUsername.setText(SupabaseSession.sessionName);
        imgAvatar.setImageResource(R.drawable.ic_account);

        imgBell.setOnClickListener(v1 -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new NotificationsFragment())
                    .addToBackStack(null)
                    .commit();
        });

        adapter = new CourseAdapter(requireContext(), list, "Home");
        rv.setAdapter(adapter);

        loadCourses();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { filter(s.toString()); }
        });

        return v;
    }

    private void loadCourses() {
        sb.fetchAllCourses(new Callback() {
            @Override public void onFailure(Call call, IOException e) {}

            @Override public void onResponse(Call call, Response response) throws IOException {
                List<Course> out = SupabaseClient.parseCoursesResponse(response.body().string());
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
