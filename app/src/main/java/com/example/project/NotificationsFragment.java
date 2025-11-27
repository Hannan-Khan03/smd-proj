package com.example.project;

import android.os.Bundle;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;

public class NotificationsFragment extends Fragment {

    RecyclerView rv;
    ArrayList<NotificationItem> list = new ArrayList<>();
    NotificationsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        View v = i.inflate(R.layout.fragment_notifications, c, false);

        rv = v.findViewById(R.id.rvNotifications);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new NotificationsAdapter(list);
        rv.setAdapter(adapter);

        loadNotifications();
        return v;
    }

    private void loadNotifications() {

        String email = SupabaseSession.sessionEmail;

        HttpUrl url = HttpUrl.parse(SupabaseClient.BASE + "/rest/v1/user_notifications")
                .newBuilder()
                .addQueryParameter(
                        "select",
                        "notification_id,notifications(id,title,message,created_at)"
                )
                .addQueryParameter("user_email", "eq." + email)
                .addQueryParameter("is_dismissed", "eq.false")
                .addQueryParameter("order", "notification_id.desc")
                .build();

        Request req = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseClient.API_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseClient.API_KEY)
                .build();

        new OkHttpClient().newCall(req).enqueue(new Callback() {

            @Override public void onFailure(Call call, IOException e) {}

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                list.clear();

                try {
                    JSONArray arr = new JSONArray(response.body().string());

                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject wrapper = arr.getJSONObject(i);
                        JSONObject o = wrapper.getJSONObject("notifications");

                        NotificationItem n = new NotificationItem();
                        n.id = o.getInt("id");
                        n.title = o.getString("title");
                        n.message = o.getString("message");
                        n.createdAt = o.getString("created_at");

                        list.add(n);
                    }

                } catch (Exception ignored) {}

                requireActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
            }
        });
    }
}
