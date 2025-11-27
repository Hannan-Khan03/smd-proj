package com.example.project;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SupabaseClient {

    public static final String BASE = "https://ypsrgzksxbxujrcbilog.supabase.co";
    public static final String API_KEY = "sb_publishable_4FB5a2Danm75PflhdkpLUQ_1rnZ_TaP";

    public final OkHttpClient client;
    private final Gson gson = new Gson();

    public SupabaseClient(Context ctx) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        client = new OkHttpClient.Builder().addInterceptor(logging).build();
    }

    private Request.Builder baseReq(String url) {
        return new Request.Builder()
                .url(url)
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Accept", "application/json");
    }

    public void fetchAllCourses(Callback cb) {
        String url = BASE + "/rest/v1/courses?select=*&order=created_at.desc";
        Request req = baseReq(url).get().build();
        client.newCall(req).enqueue(cb);
    }

    public void fetchEnrolledCourseIds(String userEmail, Callback cb) {
        HttpUrl url = HttpUrl.parse(BASE + "/rest/v1/user_enrollments").newBuilder()
                .addQueryParameter("select", "course_id")
                .addQueryParameter("user_email", "eq." + userEmail)
                .build();
        Request req = baseReq(url.toString()).get().build();
        client.newCall(req).enqueue(cb);
    }

    public void fetchCoursesByIds(List<Integer> ids, Callback cb) {
        if (ids == null || ids.isEmpty()) {
            Response r = new Response.Builder()
                    .request(new Request.Builder().url(BASE).build())
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(ResponseBody.create("[]", MediaType.get("application/json")))
                    .build();
            try { cb.onResponse(null, r); } catch (IOException ignored) {}
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(ids.get(i));
        }

        HttpUrl url = HttpUrl.parse(BASE + "/rest/v1/courses").newBuilder()
                .addQueryParameter("select", "*")
                .addQueryParameter("id", "in.(" + sb.toString() + ")")
                .build();

        Request req = baseReq(url.toString()).get().build();
        client.newCall(req).enqueue(cb);
    }

    public void addEnrollment(String userEmail, int courseId, Callback cb) {
        String url = BASE + "/rest/v1/user_enrollments";
        String json = "{\"user_email\":\"" + userEmail + "\",\"course_id\":" + courseId + "}";

        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));

        Request req = baseReq(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .post(body).build();

        client.newCall(req).enqueue(cb);
    }

    public void deleteEnrollment(String userEmail, int courseId, Callback cb) {
        HttpUrl url = HttpUrl.parse(BASE + "/rest/v1/user_enrollments").newBuilder()
                .addQueryParameter("user_email", "eq." + userEmail)
                .addQueryParameter("course_id", "eq." + courseId)
                .build();

        Request req = baseReq(url.toString()).delete().build();
        client.newCall(req).enqueue(cb);
    }

    public void upsertUser(String email, String fullName, String password, Callback cb) {
        String url = BASE + "/rest/v1/app_users";
        String json = "{\"email\":\"" + email + "\",\"full_name\":\"" + fullName + "\",\"password\":\"" + password + "\"}";

        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));

        Request req = baseReq(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(body).build();

        client.newCall(req).enqueue(cb);
    }

    public void getUserByEmail(String email, Callback cb) {
        HttpUrl url = HttpUrl.parse(BASE + "/rest/v1/app_users").newBuilder()
                .addQueryParameter("select", "*")
                .addQueryParameter("email", "eq." + email)
                .build();

        Request req = baseReq(url.toString()).get().build();
        client.newCall(req).enqueue(cb);
    }

    public void updatePassword(String email, String newPassword, Callback cb) {
        String url = BASE + "/rest/v1/app_users?email=eq." + email;
        String json = "{\"password\":\"" + newPassword + "\"}";

        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));

        Request req = baseReq(url)
                .addHeader("Content-Type", "application/json")
                .patch(body).build();

        client.newCall(req).enqueue(cb);
    }

    public void fetchAllNotifications(Callback cb) {
        String url = BASE + "/rest/v1/notifications?select=*&order=created_at.desc";
        Request req = baseReq(url).get().build();
        client.newCall(req).enqueue(cb);
    }

    public void fetchUserNotifications(String email, Callback cb) {
        HttpUrl url = HttpUrl.parse(BASE + "/rest/v1/user_notifications").newBuilder()
                .addQueryParameter("select", "*")
                .addQueryParameter("user_email", "eq." + email)
                .addQueryParameter("is_dismissed", "eq.false")
                .build();

        Request req = baseReq(url.toString()).get().build();
        client.newCall(req).enqueue(cb);
    }

    public void dismissUserNotification(int notificationId, String email, Callback cb) {
        HttpUrl url = HttpUrl.parse(BASE + "/rest/v1/user_notifications").newBuilder()
                .addQueryParameter("notification_id", "eq." + notificationId)
                .addQueryParameter("user_email", "eq." + email)
                .build();

        Request req = baseReq(url.toString()).delete().build();
        client.newCall(req).enqueue(cb);
    }

    public static ArrayList<NotificationItem> parseNotifications(String json) {
        ArrayList<NotificationItem> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                NotificationItem n = new NotificationItem();
                n.id = o.getInt("id");
                n.title = o.getString("title");
                n.message = o.getString("message");
                n.createdAt = o.getString("created_at");
                list.add(n);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<Map<String, Object>> parseGeneric(String json) {
        Gson gson = new Gson();
        try {
            Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
            List<Map<String,Object>> list = gson.fromJson(json, listType);
            if (list != null) return list;
        } catch (Exception ignored) {}

        try {
            Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> obj = gson.fromJson(json, mapType);
            if (obj != null) {
                List<Map<String,Object>> list = new ArrayList<>();
                list.add(obj);
                return list;
            }
        } catch (Exception ignored) {}

        return new ArrayList<>();
    }

    public static List<Course> parseCoursesResponse(String json) {
        List<Map<String, Object>> rows = parseGeneric(json);
        List<Course> out = new ArrayList<>();

        for (Map<String, Object> r : rows) {
            Course c = new Course();

            c.id = ((Number) r.get("id")).intValue();
            c.title = String.valueOf(r.get("title"));
            c.price = String.valueOf(r.get("price"));
            c.rating = String.valueOf(r.get("rating"));
            c.imageName = String.valueOf(r.get("image_name"));

            Object t = r.get("type");
            c.type = t == null ? "programming" : String.valueOf(t);

            out.add(c);
        }

        return out;
    }


    public static List<Integer> parseCourseIdResponse(String json) {
        List<Map<String, Object>> rows = parseGeneric(json);
        List<Integer> out = new ArrayList<>();

        for (Map<String, Object> r : rows) {
            Object id = r.get("course_id");
            if (id instanceof Number) out.add(((Number) id).intValue());
        }

        return out;
    }
    public void dismissNotification(int userNotifId, Callback cb) {
        String url = BASE + "/rest/v1/user_notifications?id=eq." + userNotifId;
        String json = "{\"is_dismissed\":true}";
        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));

        Request req = baseReq(url)
                .addHeader("Content-Type", "application/json")
                .patch(body)
                .build();

        client.newCall(req).enqueue(cb);
    }

}
