package com.example.project;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AccountFragment extends Fragment {

    TextView tvFullName, tvEmail, tvJoinedDate, tvBio;
    ImageView imgProfile;
    Button btnLogout, btnEditBio;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle s) {
        View v = inflater.inflate(R.layout.fragment_account, container, false);

        tvFullName = v.findViewById(R.id.tvFullName);
        tvEmail = v.findViewById(R.id.tvEmail);
        tvJoinedDate = v.findViewById(R.id.tvJoinedDate);
        tvBio = v.findViewById(R.id.tvBio);
        imgProfile = v.findViewById(R.id.imgProfile);
        btnLogout = v.findViewById(R.id.btnLogout);
        btnEditBio = v.findViewById(R.id.btnEditBio);

        loadUser();

        btnEditBio.setOnClickListener(v1 -> openBioDialog());
        btnLogout.setOnClickListener(v12 -> logout());

        return v;
    }

    private void loadUser() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", requireContext().MODE_PRIVATE);

        String fullName = prefs.getString("full_name", "");
        String email = prefs.getString("user_email", "");
        String createdAt = prefs.getString("created_at", "");
        String bio = prefs.getString("bio", "No bio added yet.");

        tvFullName.setText(fullName);
        tvEmail.setText(email);
        tvBio.setText(bio);

        String formattedDate = "--";

        try {
            if (createdAt.contains("T")) {
                SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault());
                Date d = in.parse(createdAt);
                SimpleDateFormat out = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                formattedDate = out.format(d);
            } else {
                long ts = Long.parseLong(createdAt);
                Date d = new Date(ts);
                SimpleDateFormat out = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                formattedDate = out.format(d);
            }
        } catch (Exception ignored) {}

        tvJoinedDate.setText("Joined: " + formattedDate);
        imgProfile.setImageResource(R.drawable.ic_account);
    }

    private void openBioDialog() {
        EditText input = new EditText(requireContext());
        input.setText(tvBio.getText().toString());

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Bio")
                .setView(input)
                .setPositiveButton("Save", (d, w) -> {
                    String newBio = input.getText().toString().trim();
                    SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", requireContext().MODE_PRIVATE);
                    prefs.edit().putString("bio", newBio).apply();
                    tvBio.setText(newBio);
                })
                .setNegativeButton("Cancel", null)
                .show();
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
