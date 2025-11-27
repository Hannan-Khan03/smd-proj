package com.example.project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AdminAccountFragment extends Fragment {

    TextView tvFullName, tvEmail, tvRole;
    ImageView imgProfile;
    Button btnLogout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle s) {
        View v = inflater.inflate(R.layout.fragment_admin_account, container, false);

        tvFullName = v.findViewById(R.id.tvFullName);
        tvEmail = v.findViewById(R.id.tvEmail);
        tvRole = v.findViewById(R.id.tvRole);
        imgProfile = v.findViewById(R.id.imgProfile);
        btnLogout = v.findViewById(R.id.btnLogout);

        loadAdminInfo();

        btnLogout.setOnClickListener(view -> logout());

        return v;
    }

    private void loadAdminInfo() {
        String name = SupabaseSession.sessionName;
        String email = SupabaseSession.sessionEmail;

        if (name == null || name.isEmpty()) name = "Admin User";
        if (email == null || email.isEmpty()) email = "admin@supmentors.com";

        tvFullName.setText(name);
        tvEmail.setText(email);
        tvRole.setText("Role: Admin");
        imgProfile.setImageResource(R.drawable.ic_account);
    }

    private void logout() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", requireContext().MODE_PRIVATE);
        prefs.edit().clear().apply();
        SupabaseSession.sessionEmail = null;
        SupabaseSession.sessionName = null;
        Intent i = new Intent(requireContext(), LoginActivity.class);
        startActivity(i);
        requireActivity().finish();
    }
}
