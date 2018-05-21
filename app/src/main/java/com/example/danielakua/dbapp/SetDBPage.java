package com.example.danielakua.dbapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class SetDBPage extends AppCompatActivity {

    public StringBuilder logger = LogsPageActivity.logger;// logger instance
    protected final String TAG = "Set DB Page:";// logger tag
    private EditText hostSetdb;
    private EditText portSetdb;
    private EditText dbSetdb;
    private EditText usernamedb;
    private EditText passworddb;
    private TextView errorSetdb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_dbpage);

        hostSetdb = findViewById(R.id.hostSetdb);
        portSetdb = findViewById(R.id.portSetdb);
        dbSetdb = findViewById(R.id.dbSetdb);
        usernamedb = findViewById(R.id.usernameSetdb);
        passworddb = findViewById(R.id.passwordSetdb);
        errorSetdb = findViewById(R.id.errorSetdb);

        portSetdb.setInputType(InputType.TYPE_CLASS_NUMBER);
    }

    // add messages to the log
    public void Log(String msg)
    {
        logger.append(String.format("%s %s\n", TAG, msg));
    }

    // click to set DB
    public void SetDBClick(View view)
    {
        Log("Trying to set DB");
        errorSetdb.setText("Connecting...");

        String host = hostSetdb.getText().toString();
        String port = portSetdb.getText().toString();
        String dbname = dbSetdb.getText().toString();
        String dbuser = usernamedb.getText().toString();
        String dbpass = passworddb.getText().toString();

        if(host.isEmpty())
        {
            Log("Host name empty");
            errorSetdb.setText("Enter host name or ip");
            return;
        }
        if(port.isEmpty())
        {
            Log("Port empty");
            errorSetdb.setText("Enter port number");
            return;
        }
        if(!port.matches("^\\d{4,5}$"))
        {
            Log("Invalid port number");
            errorSetdb.setText("Invalid port number");
            return;
        }
        if(dbname.isEmpty())
        {
            Log("DB name empty");
            errorSetdb.setText("Enter DB name");
            return;
        }
        if(dbuser.isEmpty())
        {
            Log("DB username name empty");
            errorSetdb.setText("Enter DB username");
            return;
        }
        if(dbpass.isEmpty())
        {
            Log("DB password empty");
            errorSetdb.setText("Enter DB password");
            return;
        }

        final String oldUrl = MainActivity.sharedPref.getString("dburl", null);
        final String oldName = MainActivity.sharedPref.getString("dbname", null);


        String dburl = String.format("jdbc:postgresql://%s:%s/%s?user=%s&password=%s", host, port, dbname, dbuser, dbpass);
        MainActivity.sharedPref.edit().putString("dburl", dburl)
                                      .putString("dbname", dbname).apply();

        PerformQuery query = new PerformQuery("create", new PerformQuery.AsyncResponse(){
            @Override
            public void processFinish(String response)
            {
                response = response.trim();
                errorSetdb.setText(response);
                if(response.equals("server error") || response.equals("one or more of the credentials is wrong"))
                {
                    MainActivity.sharedPref.edit().putString("dburl", oldUrl)
                                                  .putString("dbname", oldName).apply();
                }
                else
                {
                    findViewById(R.id.confirmSetdb).setEnabled(false);
                }
            }
        });
        query.execute();
    }

    // click to cancel
    public void CancelClick(View view)
    {
        Log("Moving to Main Page");
        finish();
    }
}
