package com.example.danielakua.dbapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

public class RegisterPage extends AppCompatActivity {

    public StringBuilder logger = LogsPageActivity.logger;// logger instance
    protected final String TAG = "Register Page:";// logger tag

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_page);

        Log("Entered Register Page");

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    // add messages to the log
    public void Log(String msg)
    {
        logger.append(String.format("%s %s\n", TAG, msg));
    }

    // click to register
    public void RegisterClick(View view)
    {
        Log("Attempting register...");

//        Intent intent = new Intent(this, MainActivity.class);
        final TextView errorRegister = findViewById(R.id.errorRegister);
        EditText usernameRegister = findViewById(R.id.usernameRegister);
        EditText passwordRegister = findViewById(R.id.passwordRegister);
        EditText confirmRegister = findViewById(R.id.confirmRegister);

        errorRegister.setText("");
        String username = usernameRegister.getText().toString();
        String password = passwordRegister.getText().toString();
        String confirm = confirmRegister.getText().toString();

        if (!(MainActivity.sharedPref.contains("dburl"))){
            Log("Error: Attempt to register without database set");
            errorRegister.setText("Admin must set database first");
            return;
        }
        if (username.isEmpty())
        {
            Log("Error: Username left blank");
            errorRegister.setText("Enter username");
            return;
        }
        else if (username.equals(MainActivity.sharedPref.getString("adminuser","")))
        {
            Log("Error: Username Admin");
            errorRegister.setText("Illegal username");
            return;
        }
        else if (password.isEmpty())
        {
            Log("Error: Password left blank");
            errorRegister.setText("Enter password");
            return;
        }
        else if (confirm.isEmpty())
        {
            Log("Error: Confirm password left blank");
            errorRegister.setText("Confirm password");
            return;
        }
        else if (!password.equals(confirm))
        {
            Log("Error: Passwords doesn't match");
            errorRegister.setText("Passwords doesn't match");
            return;
        }

        errorRegister.setText(R.string.attempting_connection);
        PerformQuery query = new PerformQuery("apply", new PerformQuery.AsyncResponse(){
            @Override
            public void processFinish(String response)
            {
                response = response.trim();
                errorRegister.setText(response);
                if (response.equals("applied successfully"))
                {
                    Log("Applied successfully");
                    CancelClick(null);
                }
            }
        });
        query.execute(username, password);
    }

    // click to cancel
    public void CancelClick(View view)
    {
        Log("Moving to Login Page");
        finish();
    }
}
