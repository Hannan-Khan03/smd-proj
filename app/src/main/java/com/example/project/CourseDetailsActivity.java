package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class CourseDetailsActivity extends AppCompatActivity {

    TextView tvTitle, tvPrice, tvRating, tvDescription;
    ImageView imgCourse;
    Button btnEnroll;
    int courseId;
    String title;
    String price;
    String rating;
    String imageName;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_course_details);
        tvTitle = findViewById(R.id.tvCourseTitle);
        tvPrice = findViewById(R.id.tvCoursePrice);
        tvRating = findViewById(R.id.tvCourseRating);
        tvDescription = findViewById(R.id.tvCourseDescription);
        imgCourse = findViewById(R.id.imgCourseDetail);
        btnEnroll = findViewById(R.id.btnEnroll);
        courseId = getIntent().getIntExtra("courseId", -1);
        title = getIntent().getStringExtra("title");
        price = getIntent().getStringExtra("price");
        rating = getIntent().getStringExtra("rating");
        imageName = getIntent().getStringExtra("imageName");
        tvTitle.setText(title == null ? "Untitled" : title);
        tvPrice.setText(price == null ? "Free" : price);
        tvRating.setText(rating == null ? "★ 4.0" : rating);
        int res = getResources().getIdentifier(imageName, "drawable", getPackageName());
        if (res == 0) res = R.drawable.ic_placeholder;
        imgCourse.setImageResource(res);
        btnEnroll.setOnClickListener(v -> {
            Intent i = new Intent(CourseDetailsActivity.this, PaymentActivity.class);
            i.putExtra("courseId", courseId);
            i.putExtra("title", title);
            i.putExtra("price", price);
            startActivity(i);
        });
    }
}
