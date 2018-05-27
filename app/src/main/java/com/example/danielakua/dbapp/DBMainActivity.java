package com.example.danielakua.dbapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DBMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dbmain);

        LoginPage.sharedPref = getSharedPreferences("com.example.danielakua.dbapp_sharedPrefs", 0);

//        LoginPage.sharedPref.edit().clear().apply();
    }

    @Override
    public void onResume(){
        super.onResume();
        if (LoginPage.sharedPref.contains("dburl")) {
            GoToRegister();
        }
    }

    public void SetDBClick(View view) {
        final TextView errorDBMain = findViewById(R.id.errorDBMain);
        EditText urlRegister = findViewById(R.id.urlDBMain);
        String dburl = urlRegister.getText().toString();

        if (dburl.isEmpty()) {
            errorDBMain.setText("Enter DB URL");
            return;
        }

        updateDBURL(dburl);

        PerformQuery query = new PerformQuery(this, "ping", new PerformQuery.AsyncResponse(){
            @Override
            public void processFinish(String response)
            {
                response = response.trim();
                if (response.isEmpty()) {
                    GoToRegister();
                }
                else {
                    errorDBMain.setText("Bad URL code");
                }
            }
        });
        query.execute();
    }

    private void updateDBURL(String url) {
        byte[] valueDecoded = Base64.decode(url.getBytes(), Base64.NO_WRAP | Base64.URL_SAFE);
        System.out.println(new String(valueDecoded));
        String dburl = new String(valueDecoded);
        Pattern pattern = Pattern.compile(".*:\\d+/(\\w+)\\?user=\\w+&password=.*");
        Matcher matcher = pattern.matcher(dburl);
        if (matcher.find()) {
            LoginPage.sharedPref.edit().putString("dburl", dburl)
                    .putString("dbname", matcher.group(1)).apply();
        }
    }

    public void AdminClick(View view) {
        LoginPage.sharedPref.edit().remove("dburl")
                .remove("dbname").apply();
        GoToLogin();
    }

    private void GoToRegister() {
        Intent intent = new Intent(this, RegisterPage.class);
        startActivity(intent);
        finish();
    }

    private void GoToLogin() {
        Intent intent = new Intent(this, LoginPage.class);
        startActivity(intent);
        finish();
    }
}
