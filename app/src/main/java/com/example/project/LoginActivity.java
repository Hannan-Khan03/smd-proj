package com.example.project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin;
    TextView tvSignup, tvForgot;
    SupabaseClient sb;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_login);

        sb = new SupabaseClient(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignup = findViewById(R.id.tvSignup);
        tvForgot = findViewById(R.id.tvForgot);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Enter email & password", Toast.LENGTH_SHORT).show();
                return;
            }

            sb.getUserByEmail(email, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String body = response.body().string();
                    List<Map<String, Object>> users = SupabaseClient.parseGeneric(body);

                    if (users == null || users.isEmpty()) {
                        runOnUiThread(() -> Toast.makeText(LoginActivity.this, "User not found", Toast.LENGTH_SHORT).show());
                        return;
                    }

                    Map<String, Object> u = users.get(0);

                    String storedPass = u.get("password") == null ? "" : String.valueOf(u.get("password"));
                    String fullName = u.get("full_name") == null ? "" : String.valueOf(u.get("full_name"));
                    String createdAt = u.get("created_at") == null ? "" : String.valueOf(u.get("created_at"));

                    if (!storedPass.equals(pass)) {
                        runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show());
                        return;
                    }

                    SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    prefs.edit()
                            .putString("full_name", fullName)
                            .putString("user_email", email)
                            .putString("created_at", createdAt)
                            .apply();

                    SupabaseSession.sessionEmail = email;
                    SupabaseSession.sessionName = fullName;
                    SupabaseSession.needsRefresh = true;

                    runOnUiThread(() -> {
                        Intent i = new Intent(LoginActivity.this, MainActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                        finish();
                    });
                }
            });
        });

        tvSignup.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, SignUpActivity.class)));
        tvForgot.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));
    }
}
