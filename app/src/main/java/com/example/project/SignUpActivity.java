package com.example.project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import java.io.IOException;

public class SignUpActivity extends AppCompatActivity {

    EditText etFullName, etEmail, etPassword;
    Button btnSignUp;
    SupabaseClient sb;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_signup);

        sb = new SupabaseClient(this);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignUp = findViewById(R.id.btnSignUp);

        btnSignUp.setOnClickListener(v -> {
            String name = etFullName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!name.matches("[a-zA-Z ]+")) {
                Toast.makeText(this, "Name must contain letters only", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!email.contains("@") || !email.endsWith(".com")) {
                Toast.makeText(this, "Enter a valid email", Toast.LENGTH_SHORT).show();
                return;
            }

            sb.upsertUser(email, name, pass, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(SignUpActivity.this, "Signup failed", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        runOnUiThread(() -> Toast.makeText(SignUpActivity.this, "Signup failed", Toast.LENGTH_SHORT).show());
                        return;
                    }

                    long now = System.currentTimeMillis();

                    SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    prefs.edit()
                            .putString("full_name", name)
                            .putString("user_email", email)
                            .putString("created_at", String.valueOf(now))
                            .apply();

                    SupabaseSession.sessionEmail = email;
                    SupabaseSession.sessionName = name;
                    SupabaseSession.needsRefresh = true;

                    runOnUiThread(() -> {
                        Intent i = new Intent(SignUpActivity.this, MainActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                        finish();
                    });
                }
            });
        });
    }
}
