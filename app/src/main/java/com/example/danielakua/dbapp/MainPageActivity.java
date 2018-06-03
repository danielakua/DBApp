package com.example.danielakua.dbapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainPageActivity extends AppCompatActivity {

    protected String name;// username
    private Spinner spinner;
    protected ArrayList<String> tables;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        name = LoginPage.sharedPref.getString("username", null);
        ((TextView) findViewById(R.id.errorMain)).setText(String.format("Hello, %s", name));

        createSpinner();
    }

    private void createSpinner() {
        final boolean isAdmin = LoginPage.sharedPref.getString("username", "").equals("admin");

        PerformQuery query = new PerformQuery(this, isAdmin ? "getTables" : "getRelevantTables", new PerformQuery.AsyncResponse() {
            @Override
            public void processFinish(String response) {
                response = response.trim();
                tables = response.isEmpty() ? new ArrayList<String>() : new ArrayList<>(Arrays.asList(response.split("\n")));
                tables.remove("main_table");
                try {
                    tables.remove("users");
                } catch (Exception e) {
                    System.out.println("no users table");
                }
                loadSpinner();
            }
        });
        query.execute(LoginPage.sharedPref.getString("username", ""));

    }

    private void loadSpinner() {
        spinner = (Spinner) findViewById(R.id.spinner);
        List<String> list = new ArrayList<String>();

        for (String table : tables) {
            list.add(table);
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }

    public void CalculateScoreClick(View view) {
        final Intent intent = new Intent(this, RecordsPageActivity.class);
        spinner = (Spinner) findViewById(R.id.spinner);
        String.valueOf(spinner.getSelectedItem());

        intent.putExtra(RecordsPageActivity.EXTRA_INFO, String.valueOf(spinner.getSelectedItem()));
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
        Intent intent = new Intent(this, UpdateInfo.class);
        intent.putExtra(UpdateInfo.EXTRA_INFO, name);
        startActivity(intent);
    }
}
