package com.example.danielakua.dbapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;

public class GameList extends AppCompatActivity {

    static final String EXTRA_TABLE = "EXTRA_INFO_TABLE_NAME";
    static final String EXTRA_NAME = "EXTRA_INFO_USER_NAME";
    private ArrayList<String> matches, bets;
    protected String tableName;
    protected String userName;
    protected TextView errorGamelist;
    boolean isCurrentUser;
    Switch simpleSwitch;
    public String gamesToPresent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_list);

        tableName = getIntent().getStringExtra(EXTRA_TABLE);
        userName = getIntent().getStringExtra(EXTRA_NAME);

        errorGamelist = findViewById(R.id.errorGamelist);
        simpleSwitch = findViewById(R.id.finishedSwitch);
        gamesToPresent = "open";
        simpleSwitch.setChecked(false);

        simpleSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (simpleSwitch.isChecked()) {
                    gamesToPresent = "all";
                    simpleSwitch.setChecked(true);
                } else {
                    gamesToPresent = "open";
                    simpleSwitch.setChecked(false);
                }
                getAllMatches(gamesToPresent);
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isAdmin = LoginPage.sharedPref.getString("username", "").equals("admin") && userName.equals("admin");
        isCurrentUser = LoginPage.sharedPref.getString("username", "").equals(userName);

        ((Button) findViewById(R.id.scoreGamelist)).setText(String.format(getString(R.string.button_calculate_score), isAdmin ? "Calculate" : "Show"));
        findViewById(R.id.addGameGamelist).setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        findViewById(R.id.addUserGamelist).setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        findViewById(R.id.scoreGamelist).setVisibility(isCurrentUser ? View.VISIBLE : View.GONE);
        findViewById(R.id.submitGamelist).setVisibility(isCurrentUser ? View.VISIBLE : View.GONE);

        getAllMatches(gamesToPresent);
    }

    void getAllMatches(String gamesToPresent) {
        ((TextView) findViewById(R.id.titleGamelist)).setText(tableName);
        errorGamelist.setText("");
        String method = isCurrentUser ? "getAllInfo" : "getLockedInfo";
        PerformQuery query = new PerformQuery(this, method, new PerformQuery.AsyncResponse() {
            @Override
            public void processFinish(String response) {
                response = response.trim();
                matches = response.isEmpty() ? new ArrayList<String>() : new ArrayList<>(Arrays.asList(response.split("\n")));
                getRelevantUser();
            }
        });
        query.execute(tableName);
    }

    void getRelevantUser() {
        errorGamelist.setText("");
        PerformQuery query = new PerformQuery(this, "performGetColumnIndexForUser", new PerformQuery.AsyncResponse() {
            @Override
            public void processFinish(String response) {
                response = response.trim();
//                bets = response.isEmpty() || response.equals("server error")
//                        ? new ArrayList<String>() : new ArrayList<>(Arrays.asList(response.split("\n")));
                loadMatches(response);
            }
        });
        query.execute(tableName, userName);
    }

    void loadMatches(String userIndex) {
        if (matches.isEmpty()) {
            errorGamelist.setText("Nothing To Show");
        } else {
            // show users in list view
            ListView listView = findViewById(R.id.gameGamelist);
            ArrayList<String> presentedMatches = getGamesToPresent();
            sortMatchesByTime(presentedMatches);
            ArrayList<String> presentedBets = getBetsToPresent(presentedMatches,userIndex);
            bets = presentedBets;
            MatchAdapter adapter = new MatchAdapter(this, presentedMatches, presentedBets, tableName, userName, new MatchAdapter.OnDataChangeListener() {
                @Override
                public void onDataChanged(String response) {
                    recreate();
                }
            });
            listView.setAdapter(adapter);
        }
    }

    private ArrayList<String> getBetsToPresent(ArrayList<String> presentedMatches, String userIndex) {
        ArrayList<String> bets = new ArrayList<>();
        for (int i = 0; i < presentedMatches.size(); i++) {
            bets.add(presentedMatches.get(i).split(",")[Integer.parseInt(userIndex)]);
        }
        return bets;
    }

    private ArrayList<String> getGamesToPresent() {
        ArrayList<String> presentedMatches = new  ArrayList<String>();
        if (gamesToPresent.equals("all")) {
            return matches;
        } else {
            for (String item : matches) {
                String[] s= item.split(",");
                if (item.split(",")[Globals.REAL_SCORE_COLUMN_INDEX].equals("0")) {
                    presentedMatches.add(item);
                }
            }
            return presentedMatches;
        }
    }
        private void sortMatchesByTime (ArrayList<String> presentedMatches) {
            final SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Collections.sort(presentedMatches, new Comparator<String>() {
                public int compare(String o1, String o2) {
                    try {
                        if (dt.parse(o1.split(",")[Globals.DATE_COLUMN_INDEX]) == null || dt.parse(o2.split(",")[Globals.DATE_COLUMN_INDEX]) == null)
                            return 0;
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    try {
                        return dt.parse(o1.split(",")[Globals.DATE_COLUMN_INDEX]).compareTo(dt.parse(o2.split(",")[Globals.DATE_COLUMN_INDEX]));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return 0;
                }
            });
        }

        public void addGameClick (View view){
            Intent intent = new Intent(this, AddToTable.class);
            intent.putExtra(AddToTable.EXTRA_INFO, tableName);
            startActivity(intent);
        }

        public void addUsersClick (View view){
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

        private void updateUsers (ArrayList < String > users) {
            ArrayList<String> columns = new ArrayList<>();
            columns.add(tableName);
            for (String user : users) {
                if (user.split(" ")[1].equals("t")) {
                    columns.add(user.split(" ")[0]);
                    columns.add("integer");
                }
            }

            String[] colsArr = new String[columns.size()];
            colsArr = columns.toArray(colsArr);

            PerformQuery query = new PerformQuery(this, "addColumns", new PerformQuery.AsyncResponse() {
                @Override
                public void processFinish(String response) {
                    errorGamelist.setText(response.trim());
                }
            });
            query.execute(colsArr);
        }

        public void SubmitClick (View view){

            ArrayList<String> params = new ArrayList<>();
            params.add(tableName);
            params.add(LoginPage.sharedPref.getString("username", ""));

            if (LoginPage.sharedPref.getString("username", "").equals("admin")) {
                for (int i = 0; i < bets.size(); i++) {
                    params.add(matches.get(i).split(",")[0]);
                    params.add(bets.get(i));
                }
            } else {
                for (int i = 0; i < bets.size(); i++) {
                    if (matches.get(i).split(",")[Globals.LOCKED_COLUMN_INDEX].equals("0")) {
                        if (!shouldBeLocked(matches.get(i).split(",")[0], matches.get(i).split(",")[Globals.DATE_COLUMN_INDEX])) {
                            params.add(matches.get(i).split(",")[0]);
                            params.add(bets.get(i));
                        }
                    }
                }
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

        private boolean shouldBeLocked (String id, String dateString){
            SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date date = null;
            try {
                date = dt.parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Date now = new Date(System.currentTimeMillis() + (1000 * 60 * 5));
            System.out.println(date.compareTo(now));
            if (date.compareTo(now) < 0) {
                return true;
            }
            return false;
        }

        public void CalculateScoreClick (View view){


            PerformQuery query = new PerformQuery(this, "getAllUsers", new PerformQuery.AsyncResponse() {
                @Override
                public void processFinish(String response) {
                    response = response.trim();
                    ArrayList<String> users = response.isEmpty() ? new ArrayList<String>() : new ArrayList<>(Arrays.asList(response.split(",")));
                    calculateScore(users);
                    errorGamelist.setText("");
                    Intent intent = new Intent(GameList.this, RecordsPageActivity.class);
                    intent.putExtra(RecordsPageActivity.EXTRA_INFO, tableName);
                    startActivity(intent);
                }
            });
            query.execute(UsersList.USERS_TABLE);

        }

        private void calculateScore ( final ArrayList<String> users){

            PerformQuery query1 = new PerformQuery(this, "getLockedInfo", new PerformQuery.AsyncResponse() {

                @Override
                public void processFinish(String response) {
                    response = response.trim();
                    calcAll(response, users);
                }
            });
            query1.execute(tableName);
        }

        private void calcAll (String response, ArrayList < String > users){
            String[] rows = response.split("\n");

            System.out.println(rows);

            for (int i = 0; i < users.size(); i++) {
                String[] userBets = new String[rows.length];
                for (int j = 0; j < rows.length; j++) {
                    userBets[j] = rows[j].split(",")[Globals.DATE_COLUMN_INDEX + 1 + i];
                }
                System.out.println(users.get(i).split(" ")[0] + "\n" + userBets.toString());
                updateScore(users.get(i), userBets);
            }


        }

        private void updateScore (String name, String[]betCol){
            double sum = 0;
            if (matches.size() == betCol.length) {
                for (int i = 0; i < matches.size(); i++) {
                    System.out.println(matches.get(i));
                    String[] line = matches.get(i).split(",");

                    if (line[Globals.LOCKED_COLUMN_INDEX].equals("1") && line[Globals.REAL_SCORE_COLUMN_INDEX].equals(betCol[i].trim())) {
                        double d = Double.parseDouble(line[Integer.parseInt(line[Globals.REAL_SCORE_COLUMN_INDEX]) + (Globals.HOME_TEAM_WIN_RATE_COLUMN_INDEX - 1)]);
                        sum += d - 1;
                    }

                    // If we decide to give
//                else  if (line[5].equals("1") && !line[6].equals("null") && betCol[i].trim().equals("null")) {
//                    sum+=0.5;
//                }
                }
            }
            PerformQuery query = new PerformQuery(this, "updateScore", new PerformQuery.AsyncResponse() {
                @Override
                public void processFinish(String response) {

                }
            });
            query.execute(UsersList.USERS_TABLE, tableName, name.split(" ")[0], Double.toString(sum));
        }
    }
