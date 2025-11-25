package com.example.project;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {
    private static final String NAME = "supmentors_prefs";
    private static final String KEY_LOGGED = "logged_in";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_PENDING = "pending_email";

    private static SharedPreferences get(Context c) {
        return c.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public static void setLoggedIn(Context c, boolean v) {
        get(c).edit().putBoolean(KEY_LOGGED, v).apply();
    }
    public static boolean isLoggedIn(Context c) {
        return get(c).getBoolean(KEY_LOGGED, false);
    }

    public static void setUserEmail(Context c, String e) {
        get(c).edit().putString(KEY_EMAIL, e).apply();
    }
    public static String getUserEmail(Context c) {
        return get(c).getString(KEY_EMAIL, null);
    }

    public static void setPendingEmail(Context c, String e) {
        get(c).edit().putString(KEY_PENDING, e).apply();
    }
    public static String getPendingEmail(Context c) {
        return get(c).getString(KEY_PENDING, null);
    }
}
