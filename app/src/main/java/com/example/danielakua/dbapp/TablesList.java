package com.example.danielakua.dbapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;

public class TablesList extends AppCompatActivity {

    protected static final String MAIN_TABLE = "main_table";
    protected ArrayList<String> tables;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tables_list);
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isAdmin = LoginPage.sharedPref.getString("username", "").equals("admin");
        findViewById(R.id.addTableTableslist).setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        getTables();
    }

    protected void getTables(){
        final boolean isAdmin = LoginPage.sharedPref.getString("username", "").equals("admin");
        PerformQuery query = new PerformQuery(this, isAdmin ? "getTables" : "getRelevantTables", new PerformQuery.AsyncResponse(){
            @Override
            public void processFinish(String response)
            {
                response = response.trim();
                tables = response.isEmpty() ? new ArrayList<String>() : new ArrayList<>(Arrays.asList(response.split("\n")));
                loadTables();
            }
        });
        query.execute(LoginPage.sharedPref.getString("username", ""));
    }

    // click for create table page
    public void CreateTableClick(View view) {
        Intent intent = new Intent(this, CreateTable.class);
        startActivity(intent);
    }

    protected void loadTables() {
        ListView listView = findViewById(R.id.tables);
        TableAdapter adapter = new TableAdapter(this, tables);
        listView.setAdapter(adapter);
    }
}
