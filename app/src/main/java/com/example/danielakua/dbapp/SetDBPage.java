package com.example.danielakua.dbapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class SetDBPage extends AppCompatActivity {

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

    // click to set DB
    public void SetDBClick(View view)
    {
        errorSetdb.setText("Connecting...");

        String host = hostSetdb.getText().toString();
        String port = portSetdb.getText().toString();
        String dbname = dbSetdb.getText().toString();
        String dbuser = usernamedb.getText().toString();
        String dbpass = passworddb.getText().toString();

        if(host.isEmpty())
        {
            errorSetdb.setText("Enter host name or ip");
            return;
        }
        if(port.isEmpty())
        {
            errorSetdb.setText("Enter port number");
            return;
        }
        if(!port.matches("^\\d{4,5}$"))
        {
            errorSetdb.setText("Invalid port number");
            return;
        }
        if(dbname.isEmpty())
        {
            errorSetdb.setText("Enter DB name");
            return;
        }
        if(dbuser.isEmpty())
        {
            errorSetdb.setText("Enter DB username");
            return;
        }
        if(dbpass.isEmpty())
        {
            errorSetdb.setText("Enter DB password");
            return;
        }

        final String oldUrl = LoginPage.sharedPref.getString("dburl", null);
        final String oldName = LoginPage.sharedPref.getString("dbname", null);


        String dburl = String.format("jdbc:postgresql://%s:%s/%s?user=%s&password=%s", host, port, dbname, dbuser, dbpass);
        LoginPage.sharedPref.edit().putString("dburl", dburl)
                                      .putString("dbname", dbname).apply();

        PerformQuery query = new PerformQuery("create", new PerformQuery.AsyncResponse(){
            @Override
            public void processFinish(String response)
            {
                response = response.trim();
                errorSetdb.setText(response);
                if(response.equals("server error") || response.equals("one or more of the DB credentials are wrong"))
                {
                    LoginPage.sharedPref.edit().putString("dburl", oldUrl)
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
        finish();
    }
}
