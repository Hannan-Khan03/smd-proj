package com.example.project;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.io.IOException;

public class AdminAddCourseActivity extends AppCompatActivity {

    EditText etTitle, etPrice;
    Spinner spinnerType;
    Button btnAdd;
    OkHttpClient client;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_admin_add_course);

        client = new OkHttpClient();

        etTitle = findViewById(R.id.etTitle);
        etPrice = findViewById(R.id.etPrice);
        spinnerType = findViewById(R.id.spinnerType);
        btnAdd = findViewById(R.id.btnAdd);

        String[] types = {"programming", "marketing", "stats"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item,
                types
        );

        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinnerType.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> {

            String title = etTitle.getText().toString().trim();
            String priceRaw = etPrice.getText().toString().trim();
            String type = spinnerType.getSelectedItem().toString();

            if (title.isEmpty() || priceRaw.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String price = "$" + priceRaw;

            String json =
                    "{"
                            + "\"title\":\"" + title + "\","
                            + "\"price\":\"" + price + "\","
                            + "\"rating\":\"NEW\","
                            + "\"image_name\":\"ic_placeholder\","
                            + "\"type\":\"" + type + "\""
                            + "}";

            RequestBody body = RequestBody.create(json, MediaType.get("application/json"));

            Request request = new Request.Builder()
                    .url(SupabaseClient.BASE + "/rest/v1/courses")
                    .addHeader("apikey", SupabaseClient.API_KEY)
                    .addHeader("Authorization", "Bearer " + SupabaseClient.API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "return=representation")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(AdminAddCourseActivity.this, "Upload Failed", Toast.LENGTH_SHORT).show()
                    );
                }

                @Override
                public void onResponse(Call call, Response response) {
                    runOnUiThread(() -> {
                        Toast.makeText(AdminAddCourseActivity.this, "Course Published", Toast.LENGTH_SHORT).show();
                        etTitle.setText("");
                        etPrice.setText("");
                        spinnerType.setSelection(0);
                    });
                }
            });

        });
    }
}
