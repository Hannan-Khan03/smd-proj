package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import java.io.IOException;

public class ResetPasswordActivity extends AppCompatActivity {

    EditText etNewPass;
    Button btnReset;
    String email;
    SupabaseClient sb;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_reset_password);

        sb = new SupabaseClient(this);
        etNewPass = findViewById(R.id.etNewPassword);
        btnReset = findViewById(R.id.btnResetPassword);

        email = getIntent().getStringExtra("email");

        btnReset.setOnClickListener(v -> {
            String newp = etNewPass.getText().toString().trim();

            if (newp.isEmpty()) {
                Toast.makeText(this, "Enter new password", Toast.LENGTH_SHORT).show();
                return;
            }

            sb.updatePassword(email, newp, new Callback() {
                @Override public void onFailure(Call call, IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(ResetPasswordActivity.this, "Failed to update password", Toast.LENGTH_SHORT).show()
                    );
                }

                @Override public void onResponse(Call call, Response response) {
                    runOnUiThread(() -> {
                        Toast.makeText(ResetPasswordActivity.this, "Password updated", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ResetPasswordActivity.this, LoginActivity.class));
                        finish();
                    });
                }
            });
        });
    }
}
