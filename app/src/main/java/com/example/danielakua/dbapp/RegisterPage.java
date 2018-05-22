package com.example.danielakua.dbapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterPage extends AppCompatActivity {

    private final String urlPattern = ".*:\\d+/(\\w+)\\?user=\\w+&password=.*";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_page);

        LoginPage.sharedPref = getSharedPreferences("com.example.danielakua.dbapp_sharedPrefs", 0);

//        LoginPage.sharedPref.edit().clear().apply();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onResume(){
        super.onResume();
        if (LoginPage.sharedPref.contains("dburl")){
            GoToLogin(null);
        }
    }

    // click to register
    public void RegisterClick(View view)
    {
        final TextView errorRegister = findViewById(R.id.errorRegister);
        EditText urlRegister = findViewById(R.id.urlRegister);
        EditText usernameRegister = findViewById(R.id.usernameRegister);
        EditText passwordRegister = findViewById(R.id.passwordRegister);
        EditText confirmRegister = findViewById(R.id.confirmRegister);

        errorRegister.setText("");
        String dburl = urlRegister.getText().toString();
        String username = usernameRegister.getText().toString();
        String password = passwordRegister.getText().toString();
        String confirm = confirmRegister.getText().toString();

        if (dburl.isEmpty()) {
            errorRegister.setText("Enter DB URL");
            return;
        }
        if (!dburl.matches(urlPattern)){
            errorRegister.setText("Bad url format, Expecting:\n [host]:[port]/[db]?user=[_]&password=[_]");
            return;
        }
        if (username.isEmpty()) {
            errorRegister.setText("Enter username");
            return;
        }
        else if (username.equals(LoginPage.sharedPref.getString("adminuser",""))) {
            errorRegister.setText("Illegal username");
            return;
        }
        else if (username.matches("^[a-zA-Z].*$")) {
            errorRegister.setText("username must start with a letter");
            return;
        }
        else if (password.isEmpty()) {
            errorRegister.setText("Enter password");
            return;
        }
        else if (confirm.isEmpty()) {
            errorRegister.setText("Confirm password");
            return;
        }
        else if (!password.equals(confirm)) {
            errorRegister.setText("Passwords doesn't match");
            return;
        }

        updateDBURL(dburl);
        errorRegister.setText(R.string.attempting_connection);
        PerformQuery query = new PerformQuery("apply", new PerformQuery.AsyncResponse(){
            @Override
            public void processFinish(String response)
            {
                response = response.trim();
                errorRegister.setText(response);
                if (response.equals("applied successfully"))
                {
                    GoToLogin(null);
                }
            }
        });
        query.execute(username, password);
    }

    private void updateDBURL(String url){
        String dburl = String.format("jdbc:postgresql://%s", url);
        Pattern pattern = Pattern.compile(urlPattern);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find())
        {
            LoginPage.sharedPref.edit().putString("dburl", dburl)
                                       .putString("dbname", matcher.group(1)).apply();
        }
    }

    public void AdminClick(View view){
        LoginPage.sharedPref.edit().remove("dburl")
                                   .remove("dbname").apply();
        GoToLogin(view);
    }

    private void GoToLogin(View view)
    {
        Intent intent = new Intent(this, LoginPage.class);
        startActivity(intent);
        finish();
    }
}
