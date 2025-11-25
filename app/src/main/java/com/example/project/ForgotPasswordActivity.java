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
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ForgotPasswordActivity extends AppCompatActivity {

    EditText etEmailForgot;
    Button btnSendOtp;
    SupabaseClient sb;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_forgot_password);

        sb = new SupabaseClient(this);

        etEmailForgot = findViewById(R.id.etEmailForgot);
        btnSendOtp = findViewById(R.id.btnSendOtp);

        btnSendOtp.setOnClickListener(v -> {
            String email = etEmailForgot.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

               sb.getUserByEmail(email, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(ForgotPasswordActivity.this, "Error checking email", Toast.LENGTH_SHORT).show()
                    );
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String body = response.body().string();
                    List<Map<String, Object>> users = SupabaseClient.parseGeneric(body);

                    if (users == null || users.isEmpty()) {
                        runOnUiThread(() ->
                                Toast.makeText(ForgotPasswordActivity.this, "Email not found", Toast.LENGTH_SHORT).show()
                        );
                        return;
                    }


                    String otp = String.format("%06d", new Random().nextInt(1000000));

                    runOnUiThread(() -> {
                        Toast.makeText(ForgotPasswordActivity.this, "OTP sent: " + otp, Toast.LENGTH_LONG).show();

                        Intent i = new Intent(ForgotPasswordActivity.this, VerifyOTPActivity.class);
                        i.putExtra("email", email);
                        i.putExtra("otp", otp);
                        startActivity(i);
                    });
                }
            });
        });
    }
}
