package com.example.danielakua.dbapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class AdminPage extends AppCompatActivity {

    public StringBuilder logger = LogsPageActivity.logger;// logger instance
    protected final String TAG = "Admin Page:";// logger tag
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
        tablesButton.setEnabled(MainActivity.sharedPref.contains("dburl"));
    }

    // add messages to the log
    public void Log(String msg)
    {
        logger.append(String.format("%s %s\n", TAG, msg));
    }

    // click for set DB page
    public void SetDBClick(View view)
    {
        Log("Moving to Set DB Page");
        Intent intent = new Intent(this, SetDBPage.class);
        startActivity(intent);
    }

    // click for tables list page
    public void TableslistClick(View view)
    {
        Log("Moving to Tables List Page");
        Intent intent = new Intent(this, TablesList.class);
        startActivity(intent);
    }
}
