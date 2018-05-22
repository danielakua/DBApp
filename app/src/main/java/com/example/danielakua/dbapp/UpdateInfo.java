package com.example.danielakua.dbapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

public class UpdateInfo extends AppCompatActivity {

    public static final String EXTRA_INFO = "EXTRA_INFO_USERNAME";

    private boolean isAdmin;
    private String username;
    private TextView errorInfo;
    private EditText oldInfo;
    private EditText newInfo;
    private EditText conewInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_info);

        username = getIntent().getStringExtra(EXTRA_INFO);
        TextView titleInfo = findViewById(R.id.titleInfo);
        errorInfo = findViewById(R.id.errorInfo);
        oldInfo = findViewById(R.id.oldInfo);
        newInfo = findViewById(R.id.newInfo);
        conewInfo = findViewById(R.id.conewInfo);

        titleInfo.setText(String.format(getString(R.string.info_title), username));
        isAdmin = LoginPage.sharedPref.getString("username", "")
                 .equals(LoginPage.sharedPref.getString("adminuser", "admin")) &&
                 !username.equals(LoginPage.sharedPref.getString("username", ""));

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(isAdmin){
            oldInfo.setVisibility(View.INVISIBLE);
        }
    }

    // click to cancel
    public void CancelClick(View view){
        finish();
    }

    // click to change password
    public void ChangePWClick(View view) {
        String oldpass = oldInfo.getText().toString();
        String newpass = newInfo.getText().toString();
        String conewpass = conewInfo.getText().toString();

        if(oldpass.isEmpty() && !isAdmin){
            errorInfo.setText("Enter password");
            return;
        }
        if(newpass.isEmpty()){
            errorInfo.setText("Enter new password");
            return;
        }
        if(conewpass.isEmpty()){
            errorInfo.setText("Enter confirm password");
            return;
        }
        if(!newpass.equals(conewpass)){
            errorInfo.setText("Passwords doesn't match");
            return;
        }

        if(newpass.equals(oldpass) && !isAdmin){
            errorInfo.setText("New password can't be the old password");
            return;
        }
        if(username.equals(LoginPage.sharedPref.getString("adminuser", "admin"))){
            LoginPage.sharedPref.edit().putString("adminpass", newpass).apply();
            errorInfo.setText("password updated");
        }
        else {
            errorInfo.setText(R.string.attempting_connection);
            PerformQuery query = new PerformQuery("update", new PerformQuery.AsyncResponse(){
                @Override
                public void processFinish(String response)
                {
                    response = response.trim();
                    errorInfo.setText(response);
                }
            });
            query.execute(UsersList.USERS_TABLE, username, "password", newpass);
        }
    }
}
