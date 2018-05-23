package com.example.danielakua.dbapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class UsersList extends AppCompatActivity {

    static final String USERS_TABLE = "users";
    static final String EXTRA_INFO = "EXTRA_INFO_TABLE_NAME";

    private ArrayList<String> users;
    protected String tableName = USERS_TABLE;
    protected TextView errorUserslist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        tableName = getIntent().getStringExtra(EXTRA_INFO);
        errorUserslist = findViewById(R.id.errorUserslist);
    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean isMain = tableName.equals(TablesList.MAIN_TABLE);
        findViewById(R.id.addUserUserslist).setVisibility(isMain ? View.INVISIBLE : View.VISIBLE);
        getAllUsers();
    }

    void getAllUsers() {
        ((TextView) findViewById(R.id.titleUserslist)).setText(tableName);
        errorUserslist.setText("");
        PerformQuery query = new PerformQuery(this, "getAllUsers", new PerformQuery.AsyncResponse() {
            @Override
            public void processFinish(String response) {
                response = response.trim();
                users = response.isEmpty() ? new ArrayList<String>() : new ArrayList<>(Arrays.asList(response.split(",")));
                loadUsers();
            }
        });
        query.execute(tableName);
    }

    // load the records
    void loadUsers() {
        // show users in list view
        ListView listView = findViewById(R.id.users);
        UserAdapter adapter = new UserAdapter(this, users, tableName, new UserAdapter.OnDataChangeListener() {
            @Override
            public void onDataChanged(String response) {
                errorUserslist.setText(response);
            }
        });
        listView.setAdapter(adapter);
    }

    public void addUserClick(View view){
        Intent intent = new Intent(this, AddToTable.class);
        intent.putExtra(AddToTable.EXTRA_INFO, tableName);
        startActivity(intent);
    }
}
