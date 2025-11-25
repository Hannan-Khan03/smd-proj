package com.example.project;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
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
        String url = BASE + "/rest/v1/courses?select=*&order=title.asc";
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
        Request req = baseReq(url).addHeader("Content-Type", "application/json").addHeader("Prefer", "return=representation")
                .post(body).build();
        client.newCall(req).enqueue(cb);
    }

    public void deleteEnrollment(String userEmail, int courseId, Callback cb) {
        HttpUrl url = HttpUrl.parse(BASE + "/rest/v1/user_enrollments").newBuilder()
                .addQueryParameter("user_email", "eq." + userEmail)
                .addQueryParameter("course_id", "eq." + String.valueOf(courseId))
                .build();
        Request req = baseReq(url.toString()).delete().build();
        client.newCall(req).enqueue(cb);
    }

    public void upsertUser(String email, String fullName, String password, Callback cb) {
        String url = BASE + "/rest/v1/app_users";
        String json = "{\"email\":\"" + email + "\",\"full_name\":\"" + fullName + "\",\"password\":\"" + password + "\"}";
        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
        Request req = baseReq(url).addHeader("Content-Type", "application/json").addHeader("Prefer", "resolution=merge-duplicates")
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
        Request req = baseReq(url).addHeader("Content-Type", "application/json").addHeader("Prefer", "return=minimal")
                .patch(body).build();
        client.newCall(req).enqueue(cb);
    }

    public void saveOtp(String email, String code, long ts, Callback cb) {
        String url = BASE + "/rest/v1/otps";
        String json = "{\"user_email\":\"" + email + "\",\"code\":\"" + code + "\",\"ts\":" + ts + "}";
        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
        Request req = baseReq(url).addHeader("Content-Type", "application/json").addHeader("Prefer", "return=representation")
                .post(body).build();
        client.newCall(req).enqueue(cb);
    }

    public void getLatestOtp(String email, Callback cb) {
        HttpUrl url = HttpUrl.parse(BASE + "/rest/v1/otps").newBuilder()
                .addQueryParameter("select", "code,ts")
                .addQueryParameter("user_email", "eq." + email)
                .addQueryParameter("order", "ts.desc")
                .addQueryParameter("limit", "1")
                .build();
        Request req = baseReq(url.toString()).get().build();
        client.newCall(req).enqueue(cb);
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
            Object id = r.get("id");
            Object t = r.get("title");
            Object p = r.get("price");
            Object rt = r.get("rating");
            Object im = r.get("image_name");
            if (id instanceof Number) c.id = ((Number) id).intValue(); else c.id = Integer.parseInt(String.valueOf(id));
            c.title = t == null ? "Untitled Course" : String.valueOf(t);
            c.price = p == null ? "Free" : String.valueOf(p);
            c.rating = rt == null ? "★ 4.0" : String.valueOf(rt);
            c.imageName = im == null ? "ic_placeholder" : String.valueOf(im);
            out.add(c);
        }
        return out;
    }

    public static List<Integer> parseCourseIdResponse(String json) {
        List<Map<String, Object>> rows = parseGeneric(json);
        List<Integer> out = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            Object id = r.get("course_id");
            if (id instanceof Number) out.add(((Number) id).intValue()); else out.add(Integer.parseInt(String.valueOf(id)));
        }
        return out;
    }
}
