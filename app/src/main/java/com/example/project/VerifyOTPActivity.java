package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class VerifyOTPActivity extends AppCompatActivity {

    EditText etOtp;
    Button btnVerify;
    String email, correctOtp;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_verify_otp);

        etOtp = findViewById(R.id.etOtp);
        btnVerify = findViewById(R.id.btnVerifyOtp);

        email = getIntent().getStringExtra("email");
        correctOtp = getIntent().getStringExtra("otp");

        btnVerify.setOnClickListener(v -> {
            String entered = etOtp.getText().toString().trim();

            if (entered.isEmpty()) {
                Toast.makeText(this, "Enter OTP", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!entered.equals(correctOtp)) {
                Toast.makeText(this, "Incorrect OTP", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent i = new Intent(VerifyOTPActivity.this, ResetPasswordActivity.class);
            i.putExtra("email", email);
            startActivity(i);
            finish();
        });
    }
}
