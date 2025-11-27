package com.example.project;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminMainActivity extends AppCompatActivity {

    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_admin_main);

        bottomNav = findViewById(R.id.bottom_navigation_admin);

        if (s == null) {
            switchFragment(new AdminAccountFragment());
            bottomNav.setSelectedItemId(R.id.admin_nav_account);
        }

        bottomNav.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.admin_nav_account) {
                switchFragment(new AdminAccountFragment());
                return true;
            }

            if (id == R.id.admin_nav_add) {
                switchFragment(new AdminAddCourseFragment());
                return true;
            }

            if (id == R.id.admin_nav_delete) {
                switchFragment(new AdminDeleteCoursesFragment());
                return true;
            }

            return false;
        });
    }

    private void switchFragment(Fragment f) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container_admin, f)
                .commit();
    }
}
