package com.example.project;

import java.util.ArrayList;

public class SupabaseSession {
    public static String sessionEmail = null;
    public static String sessionName = null;
    public static String userRole = "user";
    public static boolean needsRefresh = false;
    public static ArrayList<Integer> enrolledIds = new ArrayList<>();
}
