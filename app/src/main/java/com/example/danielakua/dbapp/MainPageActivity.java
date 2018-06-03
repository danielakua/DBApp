package com.example.danielakua.dbapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainPageActivity extends AppCompatActivity {

    protected String name;// username

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        name = LoginPage.sharedPref.getString("username", null);
        ((TextView) findViewById(R.id.errorMain)).setText(String.format("Hello, %s", name));


    }
    public void CalculateScoreClick(View view) {
        final Intent intent = new Intent(this, RecordsPageActivity.class);
        intent.putExtra(RecordsPageActivity.EXTRA_INFO, "alpha");
        startActivity(intent);

    }
    @Override
    public void onResume() {
        super.onResume();
        findViewById(R.id.adminMain).setVisibility(name.equals("admin") ? View.VISIBLE : View.GONE);
    }

    // click for game page
    public void GameClick(View view) {
        Intent intent = new Intent(this, TablesList.class);
        startActivity(intent);
    }

    // click for admin page
    public void AdminClick(View view) {
        Intent intent = new Intent(this, AdminPage.class);
        startActivity(intent);
    }

    public void LogoutClick(View view) {
        LoginPage.sharedPref.edit().remove("username").apply();
        Intent intent = new Intent(this, LoginPage.class);
        startActivity(intent);
        finish();
    }

    // click for change password page
    public void UpdateInfoClick(View view) {
        Intent intent = new Intent(this,UpdateInfo.class);
        intent.putExtra(UpdateInfo.EXTRA_INFO, name);
        startActivity(intent);
    }
}
