package com.example.danielakua.dbapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class AdminPage extends AppCompatActivity {

    protected Button tablesButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_page);

        tablesButton = findViewById(R.id.tablesAdmin);
    }

    @Override
    protected void onResume(){
        super.onResume();
        tablesButton.setEnabled(LoginPage.sharedPref.contains("dburl"));
    }

    // click for set DB page
    public void SetDBClick(View view) {
        Intent intent = new Intent(this, SetDBPage.class);
        startActivity(intent);
    }

    // click for tables list page
    public void TableslistClick(View view) {
        Intent intent = new Intent(this, TablesList.class);
        startActivity(intent);
    }
}
