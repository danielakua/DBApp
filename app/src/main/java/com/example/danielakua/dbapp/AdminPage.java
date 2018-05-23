package com.example.danielakua.dbapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AdminPage extends AppCompatActivity {

    protected Button tablesButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_page);

        tablesButton = findViewById(R.id.tablesAdmin);
    }

    @Override
    protected void onResume(){
        super.onResume();

        byte[] bytesEncoded = Base64.encode(LoginPage.sharedPref.getString("dburl","").getBytes(), Base64.NO_WRAP | Base64.URL_SAFE);
        tablesButton.setEnabled(LoginPage.sharedPref.contains("dburl"));
        ((EditText)findViewById(R.id.encodedAdmin)).setText(new String(bytesEncoded));
    }

    // click for set DB page
    public void SetDBClick(View view) {
        Intent intent = new Intent(this, SetDBPage.class);
        startActivity(intent);
    }

    // click for tables list page
    public void TableslistClick(View view) {
        Intent intent = new Intent(this, TablesList.class);
        startActivity(intent);
    }
}
