package com.example.project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;

public class AdminAddCourseFragment extends Fragment {

    EditText etTitle, etPrice;
    Spinner spinnerType;
    Button btnAdd;
    OkHttpClient client;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {

        View v = i.inflate(R.layout.fragment_admin_add_course, c, false);

        client = new OkHttpClient();

        etTitle = v.findViewById(R.id.etTitle);
        etPrice = v.findViewById(R.id.etPrice);
        spinnerType = v.findViewById(R.id.spinnerType);
        btnAdd = v.findViewById(R.id.btnAdd);

        String[] types = {"programming", "marketing", "stats"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.spinner_item,
                types
        );

        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinnerType.setAdapter(adapter);

        btnAdd.setOnClickListener(v1 -> publishCourse());

        return v;
    }

    private void publishCourse() {

        String title = etTitle.getText().toString().trim();
        String priceRaw = etPrice.getText().toString().trim();
        String type = spinnerType.getSelectedItem().toString();

        if (title.isEmpty() || priceRaw.isEmpty()) {
            Toast.makeText(requireContext(), "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String price = "$" + priceRaw;

        String json =
                "{"
                        + "\"title\":\"" + title + "\","
                        + "\"price\":\"" + price + "\","
                        + "\"rating\":\"NEW\","
                        + "\"image_name\":\"ic_placeholder\","
                        + "\"type\":\"" + type + "\""
                        + "}";

        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url(SupabaseClient.BASE + "/rest/v1/courses")
                .addHeader("apikey", SupabaseClient.API_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseClient.API_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Upload Failed", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String body = response.body().string();

                try {
                    JSONArray arr = new JSONArray(body);
                    int courseId = arr.getJSONObject(0).getInt("id");

                    createNotification(title);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void createNotification(String courseTitle) {

        try {

            String notifJson =
                    "{"
                            + "\"title\":\"New Course Uploaded\","
                            + "\"message\":\"A new course \\\"" + courseTitle + "\\\" is now available\""
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

                @Override
                public void onFailure(Call call, IOException e) {}

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    String body = response.body().string();

                    try {
                        JSONArray arr = new JSONArray(body);
                        int notificationId = arr.getJSONObject(0).getInt("id");
                        assignNotificationToUsers(notificationId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void assignNotificationToUsers(int notificationId) {

        Request getUsers = new Request.Builder()
                .url(SupabaseClient.BASE + "/rest/v1/app_users?select=email")
                .addHeader("apikey", SupabaseClient.API_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseClient.API_KEY)
                .get()
                .build();

        client.newCall(getUsers).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {}

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String body = response.body().string();

                try {
                    JSONArray usersArr = new JSONArray(body);

                    for (int i = 0; i < usersArr.length(); i++) {

                        String email = usersArr.getJSONObject(i).getString("email");

                        String json =
                                "{"
                                        + "\"user_email\":\"" + email + "\","
                                        + "\"notification_id\":" + notificationId
                                        + "}";

                        RequestBody bodyReq = RequestBody.create(json, MediaType.get("application/json"));

                        Request req = new Request.Builder()
                                .url(SupabaseClient.BASE + "/rest/v1/user_notifications")
                                .addHeader("apikey", SupabaseClient.API_KEY)
                                .addHeader("Authorization", "Bearer " + SupabaseClient.API_KEY)
                                .addHeader("Content-Type", "application/json")
                                .post(bodyReq)
                                .build();

                        client.newCall(req).enqueue(new Callback() {
                            @Override public void onFailure(Call call, IOException e) {}
                            @Override public void onResponse(Call call, Response response) {}
                        });
                    }

                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Course + Notifications Sent", Toast.LENGTH_SHORT).show();
                        etTitle.setText("");
                        etPrice.setText("");
                        spinnerType.setSelection(0);
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
