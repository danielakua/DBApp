package com.example.danielakua.dbapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    public static SharedPreferences sharedPref;
    public StringBuilder logger = LogsPageActivity.logger;// logger instance
    protected final String TAG = "Login Page:";// logger tag

    protected TextView errorLogin;
    protected EditText usernameLogin;
    protected EditText passwordLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPref = getSharedPreferences("com.example.danielakua.dbapp_sharedPrefs", 0);

        if(!sharedPref.contains("adminuser")){
            sharedPref.edit().putString("adminuser", "Admin")
                             .putString("adminpass", "admin").apply();
        }

//        sharedPref.edit().clear().apply();

        errorLogin = findViewById(R.id.errorLogin);
        usernameLogin = findViewById(R.id.usernameLogin);
        passwordLogin = findViewById(R.id.passwordLogin);

        Log("Entered Login Page");

        // hide keyboard on app start
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    protected void onResume() {
        super.onResume();

        sharedPref.edit().remove("username").apply();
        errorLogin.setText("");
        usernameLogin.setText("");
        passwordLogin.setText("");

        findViewById(R.id.registerLogin).setEnabled(sharedPref.contains("dburl"));
    }

    // add messages to the log
    public void Log(String msg) {
        logger.append(String.format("%s %s\n", TAG, msg));
    }

    // click for logs page
    public void LogsClick(View view) {
        Log("Moving to Logs Page");
        Intent intent = new Intent(this, LogsPageActivity.class);
        startActivity(intent);
    }

    // click for records page
    public void RecordsClick(View view) {
        Log("Moving to Records Page");
        Intent intent = new Intent(this, RecordsPageActivity.class);
        startActivity(intent);
    }

    // click for register page
    public void RegisterClick(View view) {
        Log("Moving to Register Page");
        Intent intent = new Intent(this, RegisterPage.class);
        startActivity(intent);
    }

    // click for logging in
    public void LoginClick(View view) {
        Log("Attempting Log in...");

        errorLogin.setText("");
        String adminuser = sharedPref.getString("adminuser", "");
        String adminpass = sharedPref.getString("adminpass", "");
        final String username = usernameLogin.getText().toString();
        final String password = passwordLogin.getText().toString();

        // check texts' correctness
        if (username.isEmpty()) {
            Log("Error: Username left blank");
            errorLogin.setText("Enter username");
            return;
        }
        if (password.isEmpty()) {
            Log("Error: Password left blank");
            errorLogin.setText("Enter password");
            return;
        }
        if (!(sharedPref.contains("dburl") || username.equals(adminuser))){
            Log("Error: Attempt to login without database set");
            errorLogin.setText("Admin must set database first");
            return;
        }
        if (username.equals(adminuser) && password.equals(adminpass)){
            GoToMain(adminuser);
            return;
        }
        errorLogin.setText("Attempting to communicate with the server...");
        PerformQuery query = new PerformQuery("login", new PerformQuery.AsyncResponse(){
            @Override
            public void processFinish(String response)
            {
                response = response.trim();
                errorLogin.setText(response);
                if (response.equals("login successful"))
                {
                    GoToMain(username);
                }
            }
        });
        query.execute(username, password);
    }

    void GoToMain(String username) {
        sharedPref.edit().putString("username", username).apply();
        Log(String.format("Logged successfully with user name: %s", username));
        Intent intent = new Intent(this, MainPageActivity.class);
        startActivity(intent);
    }
}
