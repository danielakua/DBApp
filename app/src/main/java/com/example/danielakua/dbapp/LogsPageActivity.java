package com.example.danielakua.dbapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

public class LogsPageActivity extends AppCompatActivity {

    public static StringBuilder logger = new StringBuilder();// logger instance
    protected final String TAG = "Logs Page:";// logger tag

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs_page);

        Log("Entered Logs Page");

        TextView logView = findViewById(R.id.lowView);
        logView.setText(logger.toString());
        logView.setMovementMethod(new ScrollingMovementMethod());
    }

    // add messages to the log
    public void Log(String msg)
    {
        logger.append(String.format("%s %s\n", TAG, msg));
    }
}
