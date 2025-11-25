package com.example.project;

import java.util.ArrayList;
import java.util.List;

public class SupabaseSession {
    public static String sessionEmail = "";
    public static String sessionName = "";
    public static boolean needsRefresh = false;
    public static List<Integer> enrolledIds = new ArrayList<>();
}
