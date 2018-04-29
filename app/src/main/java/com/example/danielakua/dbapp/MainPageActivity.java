package com.example.danielakua.dbapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainPageActivity extends AppCompatActivity {

    public StringBuilder logger = LogsPageActivity.logger;// logger instance
    protected final String TAG = "Main Page:";// logger tag
    protected String name;// username
    protected Button adminMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        name = MainActivity.sharedPref.getString("username", null);
        adminMain = findViewById(R.id.adminMain);

        TextView textView = findViewById(R.id.errorMain);
        textView.setText(String.format("Hello, %s", name));
    }

    // add messages to the log
    public void Log(String msg)
    {
        logger.append(String.format("%s %s\n", TAG, msg));
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Log("Entered Main Page");
        if(!name.equals("Admin"))
        {
            adminMain.setVisibility(View.INVISIBLE);
        }
    }

    // click for logs page
    public void LogsClick(View view)
    {
        Log("Moving to Logs Page");
        Intent intent = new Intent(this, LogsPageActivity.class);
        startActivity(intent);
    }

    // click for records page
    public void RecordsClick(View view)
    {
        Log("Moving to Records Page");
        Intent intent = new Intent(this, RecordsPageActivity.class);
        startActivity(intent);
    }

    // click for game page
    public void GameClick(View view)
    {
        Log("Moving to Game Page");
        Intent intent = new Intent(this, GamePage.class);
        startActivity(intent);
    }

    // click for admin page
    public void AdminClick(View view)
    {
        Log("Moving to Game Page");
        Intent intent = new Intent(this, AdminPage.class);
        startActivity(intent);
    }

    // click for change password page
    public void UpdateInfoClick(View view)
    {
        Log("Moving to change User Info Page");
        Intent intent = new Intent(this,UpdateInfo.class);
        intent.putExtra(UpdateInfo.EXTRA_INFO, name);
        startActivity(intent);
    }
}
