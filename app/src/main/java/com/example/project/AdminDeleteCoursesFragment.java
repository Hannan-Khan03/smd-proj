package com.example.project;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AdminDeleteCoursesFragment extends Fragment {

    RecyclerView rv;
    EditText etSearch;
    ArrayList<Course> list = new ArrayList<>();
    ArrayList<Course> originalList = new ArrayList<>();
    AdminDeleteCourseAdapter adapter;
    SupabaseClient sb;
    OkHttpClient client;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle s) {
        View v = inflater.inflate(R.layout.fragment_admin_delete_courses, container, false);

        sb = new SupabaseClient(requireContext());
        client = new OkHttpClient();

        rv = v.findViewById(R.id.rvAdminDeleteCourses);
        etSearch = v.findViewById(R.id.etAdminSearch);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AdminDeleteCourseAdapter(list, (course, pos) -> confirmDelete(course, pos));
        rv.setAdapter(adapter);

        loadCourses();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { filter(); }
        });

        return v;
    }

    private void loadCourses() {
        sb.fetchAllCourses(new Callback() {
            @Override public void onFailure(Call call, IOException e) {}

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                List<Course> out = SupabaseClient.parseCoursesResponse(json);
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    originalList.clear();
                    originalList.addAll(out);
                    list.clear();
                    list.addAll(out);
                    adapter.notifyDataSetChanged();
                });
            }
        });
    }

    private void filter() {
        String q = etSearch.getText().toString().toLowerCase().trim();
        ArrayList<Course> filtered = new ArrayList<>();
        for (Course c : originalList) {
            if (c.title.toLowerCase().contains(q)) filtered.add(c);
        }
        list.clear();
        list.addAll(filtered);
        adapter.notifyDataSetChanged();
    }

    private void confirmDelete(Course c, int pos) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Course")
                .setMessage("Delete \"" + c.title + "\" for all users?")
                .setPositiveButton("Delete", (d, w) -> deleteCourse(c, pos))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteCourse(Course c, int pos) {
        HttpUrl url = HttpUrl.parse(SupabaseClient.BASE + "/rest/v1/courses").newBuilder()
                .addQueryParameter("id", "eq." + c.id)
                .build();

        Request req = new Request.Builder()
                .url(url)
                .delete()
                .addHeader("apikey", SupabaseClient.API_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseClient.API_KEY)
                .addHeader("Prefer", "return=minimal")
                .build();

        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {}

            @Override
            public void onResponse(Call call, Response response) {
                sendDeleteNotification(c.title);
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (pos >= 0 && pos < list.size()) {
                        Course removed = list.remove(pos);
                        originalList.remove(removed);
                        adapter.notifyItemRemoved(pos);
                        adapter.notifyItemRangeChanged(pos, list.size());
                    }
                });
            }
        });
    }

    private void sendDeleteNotification(String courseTitle) {
        try {
            String notifJson =
                    "{"
                            + "\"title\":\"Course Removed\","
                            + "\"message\":\"The course \\\"" + courseTitle + "\\\" has been removed from the catalog\""
                            + "}";

            RequestBody notifBody = RequestBody.create(notifJson, MediaType.get("application/json"));

            Request notifReq = new Request.Builder()
                    .url(SupabaseClient.BASE + "/rest/v1/notifications")
                    .addHeader("apikey", SupabaseClient.API_KEY)
                    .addHeader("Authorization", "Bearer " + SupabaseClient.API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "return=representation")
                    .post(notifBody)
                    .build();

            client.newCall(notifReq).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) {}

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String body = response.body().string();
                        JSONArray arr = new JSONArray(body);
                        if (arr.length() == 0) return;
                        int notificationId = arr.getJSONObject(0).getInt("id");
                        assignNotificationToAllUsers(notificationId);
                    } catch (Exception ignored) {}
                }
            });
        } catch (Exception ignored) {}
    }

    private void assignNotificationToAllUsers(int notificationId) {
        Request getUsers = new Request.Builder()
                .url(SupabaseClient.BASE + "/rest/v1/app_users?select=email")
                .addHeader("apikey", SupabaseClient.API_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseClient.API_KEY)
                .get()
                .build();

        client.newCall(getUsers).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {}

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String body = response.body().string();
                    JSONArray usersArr = new JSONArray(body);
                    for (int i = 0; i < usersArr.length(); i++) {
                        String email = usersArr.getJSONObject(i).getString("email");
                        String json =
                                "{"
                                        + "\"user_email\":\"" + email + "\","
                                        + "\"notification_id\":" + notificationId
                                        + "}";
                        RequestBody bodyInsert = RequestBody.create(json, MediaType.get("application/json"));
                        Request req = new Request.Builder()
                                .url(SupabaseClient.BASE + "/rest/v1/user_notifications")
                                .addHeader("apikey", SupabaseClient.API_KEY)
                                .addHeader("Authorization", "Bearer " + SupabaseClient.API_KEY)
                                .addHeader("Content-Type", "application/json")
                                .post(bodyInsert)
                                .build();
                        client.newCall(req).enqueue(new Callback() {
                            @Override public void onFailure(Call call, IOException e) {}
                            @Override public void onResponse(Call call, Response response) {}
                        });
                    }
                } catch (Exception ignored) {}
            }
        });
    }
}
