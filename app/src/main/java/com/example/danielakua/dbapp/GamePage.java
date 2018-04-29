package com.example.danielakua.dbapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Random;

public class GamePage extends AppCompatActivity {

    public StringBuilder logger = LogsPageActivity.logger;// logger instance
    protected final String TAG = "Game Page:";// logger tag
    protected final int countDownInterval = 10;// time between timer updates in msec
    protected int counter;// click counter
    protected String name;// username
    protected long millisInFuture;// game time length in msec
    protected CountDownTimer timer;// count down timer

    protected TextView clickView;
    protected TextView timerView;
    protected Button click;
    protected RecordsDB db;// database instance
    protected int clkW, clkH;// button dimensions

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_page);

        Log("Entered Game Page");

        name = MainActivity.sharedPref.getString("username", null);

        // initialize game variables
        counter = 0;
        millisInFuture = 3000;
        db = new RecordsDB(this);

        clickView = findViewById(R.id.clickCount);
        timerView = findViewById(R.id.timerText);
        click = findViewById(R.id.clicker);

        clickView.setText(String.format("Clicks: %d", counter));
        timerView.setText("Time remaining: ETERNITY");
    }

    // reset timer after click
    private void ResetTimer()
    {
        timer = new CountDownTimer(millisInFuture, countDownInterval)
        {
            public void onTick(long millisUntilFinished)
            {
                timerView.setText(String.format("Time remaining: %s", millisUntilFinished / 1000.0));
            }

            public void onFinish()
            {
                Log(String.format("Game ended, final score is %d", counter));
                click.setEnabled(false);

                // save user record in database
                saveRecord();

                // alert the user with final score
                PopAlert();
            }
        };
        // shorten next click by 5%
        millisInFuture -= millisInFuture * 5 / 100;
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

        // initialize game variables
        counter = 0;
        millisInFuture = 3000;
        click.setText("Start");
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if(timer != null)
        {
            timer.cancel();
        }
    }

    // save game score and username in database
    void saveRecord()
    {
        PerformQuery query = new PerformQuery("updateScore", new PerformQuery.AsyncResponse(){
            @Override
            public void processFinish(String response) { }
        });
        query.execute(UsersList.USERS_TABLE, name, Integer.toString(counter));
        Record record = new Record(name, Integer.toString(counter));
        db.addRecord(record);
    }

    // click on game's main button
    public void GameClick(View view)
    {
        if(timer != null)
        {
            // click detected, update counter
            timer.cancel();
            counter++;
            Log(String.format("Click detected, %d consecutive clicks", counter));
        }
        else
        {
            // first click is start game
            Log("Game started");
        }

        // reset and start the timer
        click.setText("");
        clickView.setText(String.format("Clicks: %d", counter));
        ResetTimer();
        timer.start();

        // reposition the button randomly
        RelativeLayout RL = findViewById(R.id.RL1);

        clkW = click.getWidth();
        clkH = click.getHeight();

        int width = RL.getWidth() - clkW;
        int height = RL.getHeight() - clkH;

        clkW = clkW - (int)(clkW * 5 / 100.0);
        clkH = clkH - (int)(clkH * 5 / 100.0);

        ViewGroup.LayoutParams params = click.getLayoutParams();
        //Button new width
        params.width = clkW;
        params.height = clkH;

        click.setLayoutParams(params);

        Random r = new Random();

        click.setX(r.nextInt(width));
        click.setY(r.nextInt(height));
    }

    // alert the user on final score
    public void PopAlert()
    {
        // build alert screen
        AlertDialog.Builder alertB = new AlertDialog.Builder(this);
        alertB.setMessage(String.format("Game ended, your final score is %d\nWhats your next move?", counter));
        alertB.setCancelable(false);

        // add play again button to alert
        alertB.setPositiveButton(
                "Play Again",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.cancel();
                        recreate();
                    }
                });

        // add go to records button to alert
        alertB.setNegativeButton(
                "Records",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.cancel();
                        RecordsClick();
                    }
                });

        // show alert screen
        AlertDialog alertD = alertB.create();
        alertD.show();
    }

    // go to records page
    public void RecordsClick()
    {
        Log("Moving to Records Page");
        Intent intent = new Intent(this, RecordsPageActivity.class);
        startActivity(intent);
        finish();
    }
}
