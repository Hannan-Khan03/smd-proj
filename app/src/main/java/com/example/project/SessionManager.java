package com.example.project;

import android.content.Context;
import android.content.Intent;

public class SessionManager {
    public static void logout(Context ctx) {
        SupabaseSession.sessionEmail = "";
        SupabaseSession.sessionName = "";
        SupabaseSession.needsRefresh = false;
        Intent i = new Intent(ctx, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(i);
    }
}
