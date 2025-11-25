package com.example.project;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import java.io.IOException;

public class PaymentActivity extends AppCompatActivity {

    EditText etCardName, etCardNumber, etExpiry, etCVV;
    Button btnPayNow;
    TextView tvCourseTitle, tvPrice, tvTax, tvTotal;
    ImageView imgVisa, imgMaster;
    SupabaseClient sb;

    int courseId;
    String courseTitle, coursePrice;
    int selectedCard = 0;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_payment);

        sb = new SupabaseClient(this);

        imgVisa = findViewById(R.id.imgVisa);
        imgMaster = findViewById(R.id.imgMastercard);
        etCardName = findViewById(R.id.etCardName);
        etCardNumber = findViewById(R.id.etCardNumber);
        etExpiry = findViewById(R.id.etExpiry);
        etCVV = findViewById(R.id.etCVV);
        btnPayNow = findViewById(R.id.btnPayNow);

        tvCourseTitle = findViewById(R.id.tvCourseTitle);
        tvPrice = findViewById(R.id.tvPrice);
        tvTax = findViewById(R.id.tvTax);
        tvTotal = findViewById(R.id.tvTotal);

        // FIX: Read courseId as INT
        courseId = getIntent().getIntExtra("courseId", -1);
        courseTitle = getIntent().getStringExtra("title");
        coursePrice = getIntent().getStringExtra("price");

        if (courseId == -1) {
            Toast.makeText(this, "Error: Missing Course ID", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        tvCourseTitle.setText(courseTitle);

        double priceValue = 0;
        if (coursePrice.startsWith("$")) {
            priceValue = Double.parseDouble(coursePrice.substring(1));
        }

        double tax = priceValue * 0.05;
        double total = priceValue + tax;

        tvPrice.setText("$" + priceValue);
        tvTax.setText(String.format("$%.2f", tax));
        tvTotal.setText(String.format("$%.2f", total));

        imgVisa.setOnClickListener(v -> selectCard(1));
        imgMaster.setOnClickListener(v -> selectCard(2));

        btnPayNow.setOnClickListener(v -> {

            if (selectedCard == 0) {
                Toast.makeText(this, "Select a card type", Toast.LENGTH_SHORT).show();
                return;
            }

            String name = etCardName.getText().toString().trim();
            String number = etCardNumber.getText().toString().trim();
            String exp = etExpiry.getText().toString().trim();
            String cvv = etCVV.getText().toString().trim();

            if (name.isEmpty() || number.length() != 16 || exp.length() != 4 || cvv.length() != 3) {
                Toast.makeText(this, "Invalid card details", Toast.LENGTH_SHORT).show();
                return;
            }

            int mm = Integer.parseInt(exp.substring(0, 2));
            int yy = Integer.parseInt(exp.substring(2, 4));

            if (mm < 1 || mm > 12 || yy < 25) {
                Toast.makeText(this, "Invalid expiry", Toast.LENGTH_SHORT).show();
                return;
            }

            String email = SupabaseSession.sessionEmail;

            sb.addEnrollment(email, courseId, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(PaymentActivity.this, "Enrollment failed", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    SupabaseSession.needsRefresh = true;

                    runOnUiThread(() -> {
                        Toast.makeText(PaymentActivity.this, "Payment Successful", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            });
        });
    }

    private void selectCard(int c) {
        selectedCard = c;

        imgVisa.setBackgroundColor(Color.TRANSPARENT);
        imgMaster.setBackgroundColor(Color.TRANSPARENT);

        GradientDrawable outline = new GradientDrawable();
        outline.setStroke(5, Color.parseColor("#007AFF"));
        outline.setCornerRadius(16);

        if (c == 1) imgVisa.setBackground(outline);
        else imgMaster.setBackground(outline);
    }
}
