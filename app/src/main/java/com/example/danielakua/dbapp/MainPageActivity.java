package com.example.danielakua.dbapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainPageActivity extends AppCompatActivity {

    protected String name;// username

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        name = LoginPage.sharedPref.getString("username", null);
        ((TextView) findViewById(R.id.errorMain)).setText(String.format("Hello, %s", name));
    }

    @Override
    public void onResume() {
        super.onResume();
        findViewById(R.id.adminMain).setVisibility(name.equals("admin") ? View.VISIBLE : View.INVISIBLE);
    }

    // click for records page
    public void RecordsClick(View view) {
        Intent intent = new Intent(this, RecordsPageActivity.class);
        startActivity(intent);
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
