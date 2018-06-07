package com.example.danielakua.dbapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

public class LoginPage extends AppCompatActivity {

    public static SharedPreferences sharedPref;

    protected TextView errorLogin;
    protected EditText usernameLogin;
    protected EditText passwordLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        if(!sharedPref.contains("adminuser")){
            sharedPref.edit().putString("adminuser", "admin")
                             .putString("adminpass", "a").apply();
        }

        errorLogin = findViewById(R.id.errorLogin);
        usernameLogin = findViewById(R.id.usernameLogin);
        passwordLogin = findViewById(R.id.passwordLogin);

        // hide keyboard on app start
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (sharedPref.contains("username")){
            GoToMain(sharedPref.getString("username", ""));
        }

        errorLogin.setText("");
        usernameLogin.setText("");
        passwordLogin.setText("");
    }

    // click for register page
    public void RegisterClick(View view) {
        Intent intent = new Intent(this, RegisterPage.class);
        startActivity(intent);
        finish();
    }

    // click for logging in
    public void LoginClick(View view) {
        errorLogin.setText("");
        String adminuser = sharedPref.getString("adminuser", "");
        String adminpass = sharedPref.getString("adminpass", "");
        final String username = usernameLogin.getText().toString();
        final String password = passwordLogin.getText().toString();

        // check texts' correctness
        if (username.isEmpty()) {
            errorLogin.setText("Enter username");
            return;
        }
        if (password.isEmpty()) {
            errorLogin.setText("Enter password");
            return;
        }
        if (!(sharedPref.contains("dburl") || username.equals(adminuser))){
            errorLogin.setText("Admin must set database first");
            return;
        }
        if (username.equals(adminuser) && password.equals(adminpass)){
            GoToMain(adminuser);
            return;
        }
        PerformQuery query = new PerformQuery(this, "login", new PerformQuery.AsyncResponse(){
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
        Intent intent = new Intent(this, MainPageActivity.class);
        startActivity(intent);
        finish();
    }
}
