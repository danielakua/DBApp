package com.example.danielakua.dbapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class GameList extends AppCompatActivity {

    static final String EXTRA_TABLE = "EXTRA_INFO_TABLE_NAME";
    static final String EXTRA_NAME = "EXTRA_INFO_USER_NAME";
    private ArrayList<String> matches, bets;
    protected String tableName;
    protected String userName;
    protected TextView errorGamelist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_list);

        tableName = getIntent().getStringExtra(EXTRA_TABLE);
        userName = getIntent().getStringExtra(EXTRA_NAME);

        errorGamelist = findViewById(R.id.errorGamelist);
    }

    @Override
    protected void onResume(){
        super.onResume();
        boolean isAdmin = LoginPage.sharedPref.getString("username", "").equals("admin") && userName.equals("admin");
        boolean isUser = LoginPage.sharedPref.getString("username", "").equals(userName);

        ((Button) findViewById(R.id.scoreGamelist)).setText(String.format(getString(R.string.button_calculate_score), isAdmin ? "Calculate" : "Show"));
        findViewById(R.id.addGameGamelist).setVisibility(isAdmin ? View.VISIBLE : View.INVISIBLE);
        findViewById(R.id.addUserGamelist).setVisibility(isAdmin ? View.VISIBLE : View.INVISIBLE);
        findViewById(R.id.scoreGamelist).setVisibility(isUser ? View.VISIBLE : View.INVISIBLE);
        findViewById(R.id.submitGamelist).setVisibility(isUser ? View.VISIBLE : View.INVISIBLE);

        getAllMatches();
    }

    void getAllMatches(){
        ((TextView) findViewById(R.id.titleGamelist)).setText(tableName);
        errorGamelist.setText("");
        PerformQuery query = new PerformQuery(this, "getAllInfo", new PerformQuery.AsyncResponse() {
            @Override
            public void processFinish(String response) {
                response = response.trim();
                matches = response.isEmpty() ? new ArrayList<String>() : new ArrayList<>(Arrays.asList(response.split("\n")));
                getRelevantUser();
            }
        });
        query.execute(tableName);
    }

    void getRelevantUser(){
        errorGamelist.setText("");
        PerformQuery query = new PerformQuery(this, "getColumns", new PerformQuery.AsyncResponse() {
            @Override
            public void processFinish(String response) {
                response = response.trim();
                bets = response.isEmpty() || response.equals("server error")
                        ? new ArrayList<String>() : new ArrayList<>(Arrays.asList(response.split("\n")));
                loadMatches();
            }
        });
        query.execute(tableName, userName);
    }

    // load the records
    void loadMatches(){
        if(bets.isEmpty()){
            errorGamelist.setText("Not a participant");
        }
        else {
            // show users in list view
            ListView listView = findViewById(R.id.gameGamelist);
            MatchAdapter adapter = new MatchAdapter(this, matches, bets, tableName, userName, new MatchAdapter.OnDataChangeListener() {
                @Override
                public void onDataChanged(String response) {
                    recreate();
                }
            });
            listView.setAdapter(adapter);
        }
    }

    public void addGameClick(View view){
        Intent intent = new Intent(this, AddToTable.class);
        intent.putExtra(AddToTable.EXTRA_INFO, tableName);
        startActivity(intent);
    }

    public void addUsersClick(View view){
        PerformQuery query = new PerformQuery(this, "getAllUsers", new PerformQuery.AsyncResponse() {
            @Override
            public void processFinish(String response) {
                response = response.trim();
                ArrayList<String> users = response.isEmpty() ? new ArrayList<String>() : new ArrayList<>(Arrays.asList(response.split(",")));
                updateUsers(users);
            }
        });
        query.execute(UsersList.USERS_TABLE);
    }

    private void updateUsers(ArrayList<String> users){
        ArrayList<String> columns = new ArrayList<>();
        columns.add(tableName);
        for(String user : users){
            if(user.split(" ")[1].equals("t")) {
                columns.add(user.split(" ")[0]);
                columns.add("int4");
            }
        }

        String[] colsArr = new String[columns.size()];
        colsArr = columns.toArray(colsArr);

        PerformQuery query = new PerformQuery(this, "addColumns", new PerformQuery.AsyncResponse(){
            @Override
            public void processFinish(String response)
            {
                errorGamelist.setText(response.trim());
            }
        });
        query.execute(colsArr);
    }

    public void SubmitClick(View view){
        ArrayList<String> params = new ArrayList<>();
        params.add(tableName);
        params.add(LoginPage.sharedPref.getString("username", ""));
        for(int i = 0; i < bets.size(); i++){
            params.add(matches.get(i).split(",")[0]);
            params.add(bets.get(i));
        }

        String[] paramsArr = new String[params.size()];
        paramsArr = params.toArray(paramsArr);

        PerformQuery query = new PerformQuery(this, "updateColumn", new PerformQuery.AsyncResponse() {
            @Override
            public void processFinish(String response) {
                response = response.trim();
                errorGamelist.setText(response);
            }
        });
        query.execute(paramsArr);
    }

    public void CalculateScoreClick(View view) {
        final Intent intent = new Intent(this, RecordsPageActivity.class);
        intent.putExtra(RecordsPageActivity.EXTRA_INFO, tableName);

        if(!LoginPage.sharedPref.getString("username","").equals("admin")){
            startActivity(intent);
        }
        PerformQuery query = new PerformQuery(this, "getAllUsers", new PerformQuery.AsyncResponse() {
            @Override
            public void processFinish(String response) {
                response = response.trim();
                ArrayList<String> users = response.isEmpty() ? new ArrayList<String>() : new ArrayList<>(Arrays.asList(response.split(",")));
                calculateScore(users);
                errorGamelist.setText("");
                startActivity(intent);
            }
        });
        query.execute(UsersList.USERS_TABLE);
    }

    private void calculateScore(ArrayList<String> users) {
        for(String user : users){
            if(user.split(" ")[1].equals("f")){
                System.out.println("Skipping " + user);
                continue;
            }
            final String name = user.split(" ")[0];
            PerformQuery query = new PerformQuery(this, "getColumns", new PerformQuery.AsyncResponse() {
                @Override
                public void processFinish(String response) {
                    response = response.trim();
                    updateScore(name, response.split("\n"));
                }
            });
            query.execute(tableName, name);
        }
    }

    private void updateScore(String name, String[] betCol) {
        double sum = 0;
        if(matches.size() == betCol.length) {
            for (int i = 0; i < matches.size(); i++) {
                System.out.println(matches.get(i));
                String[] line = matches.get(i).split(",");
                if (line[5].equals("1") && line[6].equals(betCol[i].trim())) {
                    sum += Double.parseDouble(line[Integer.parseInt(line[6]) + 1]);
                }
            }
        }
        PerformQuery query = new PerformQuery(this, "updateScore", new PerformQuery.AsyncResponse() {
            @Override
            public void processFinish(String response) { }
        });
        query.execute(UsersList.USERS_TABLE, name, Double.toString(sum));
    }
}
