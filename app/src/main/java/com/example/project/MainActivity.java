package com.example.project;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment sel = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) sel = new HomeFragment();
            else if (itemId == R.id.nav_search) sel = new SearchFragment();
            else if (itemId == R.id.nav_tasks) sel = new TasksFragment();
            else if (itemId == R.id.nav_profile) sel = new AccountFragment();
            if (sel != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, sel).commit();
                return true;
            }
            return false;
        });
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }

    public void setActiveNavItem(int id) {
        bottomNavigationView.setSelectedItemId(id);
    }
}
