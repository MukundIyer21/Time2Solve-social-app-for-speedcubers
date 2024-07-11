package com.example.rubikssolver;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.rubikssolver.databinding.ActivityMainBinding;
import com.example.rubikssolver.ui.home.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.security.Permission;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications,R.id.navigation_profile)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);

        getPermission();

    }

    void getPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            String[] p = new String[1];
            p[0] = Manifest.permission.CAMERA;
            requestPermissions(p,101);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults[0]!=PackageManager.PERMISSION_GRANTED){
            Toast.makeText(getApplicationContext(),"Camera permission required",Toast.LENGTH_SHORT).show();
            getPermission();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("MainActivity", "onActivityResult: requestCode = " + requestCode + ", resultCode = " + resultCode + ", data = " + (data != null ? "not null" : "null"));

        if (requestCode == 1001) {
            if (resultCode == RESULT_OK && data != null) {
                boolean isVerified = data.getBooleanExtra("isVerified", false);
                String cubeSolve = data.getStringExtra("cubeSolve");
                Log.d("MainActivity", "Received result: isVerified = " + isVerified + ", cubeSolve = " + cubeSolve);

                // Get the NavController
                NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

                // Get the current destination
                NavDestination currentDestination = navController.getCurrentDestination();

                if (currentDestination != null && currentDestination.getId() == R.id.navigation_home) {
                    // We are on the HomeFragment
                    HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.nav_host_fragment_activity_main)
                            .getChildFragmentManager()
                            .getPrimaryNavigationFragment();

                    if (homeFragment != null) {
                        homeFragment.handleCameraResult(isVerified, cubeSolve);
                        Log.d("MainActivity", "Result passed to HomeFragment");
                    } else {
                        Log.e("MainActivity", "HomeFragment is null");
                    }
                } else {
                    // We are not on the HomeFragment, store the result to pass later
                    Log.d("MainActivity", "Not on HomeFragment, storing result");
                    getIntent().putExtra("isVerified", isVerified);
                    getIntent().putExtra("cubeSolve", cubeSolve);
                }
            } else {
                Log.d("MainActivity", "Result not OK or data is null for CAMERA_PAGE_REQUEST_CODE");
            }
        } else {
            Log.d("MainActivity", "Unhandled request code: " + requestCode);
        }
    }
}