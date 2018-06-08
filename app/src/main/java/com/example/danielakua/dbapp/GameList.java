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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GameList extends AppCompatActivity {

    static final String EXTRA_TABLE = "EXTRA_INFO_TABLE_NAME";
    static final String EXTRA_NAME = "EXTRA_INFO_USER_NAME";
    private ArrayList<String> matches, bets;
    protected ArrayList<String> presentedBets;
    protected String tableName;
    protected String userName;
    protected TextView errorGamelist;
    boolean isCurrentUser;
    Switch simpleSwitch;
    public String gamesToPresent;
    ArrayList<String> presentedMatches;
    private String userIndex;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_list);

        tableName = getIntent().getStringExtra(EXTRA_TABLE);
        userName = getIntent().getStringExtra(EXTRA_NAME);

        errorGamelist = findViewById(R.id.errorGamelist);
        simpleSwitch = findViewById(R.id.finishedSwitch);
        gamesToPresent = "all";
        simpleSwitch.setChecked(true);

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
                getAllMatches();
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

        getAllMatches();
    }

    void getAllMatches() {
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
                userIndex = response;
//                bets = response.isEmpty() || response.equals("server error")
//                        ? new ArrayList<String>() : new ArrayList<>(Arrays.asList(response.split("\n")));
                loadMatches();
            }
        });
        query.execute(tableName, userName);
    }

    void loadMatches() {
        if (matches.isEmpty()) {
            errorGamelist.setText("Nothing To Show");
        } else {
            // show users in list view
            ListView listView = findViewById(R.id.gameGamelist);
            presentedMatches = getGamesToPresent();
            sortMatchesByTime(presentedMatches);
            presentedBets = getBetsToPresent(presentedMatches);
            MatchAdapter adapter = new MatchAdapter(this, presentedMatches, presentedBets, tableName, userName, new MatchAdapter.OnDataChangeListener() {
                @Override
                public void onDataChanged(String response) {
                    recreate();
                }
            });
            listView.setAdapter(adapter);
        }
    }

    private ArrayList<String> getBetsToPresent(ArrayList<String> presentedMatches) {
        ArrayList<String> bets = new ArrayList<>();
        for (int i = 0; i < presentedMatches.size(); i++) {
            if (presentedMatches.get(i).split(",")[Integer.parseInt(userIndex)].equals("null"))
                bets.add("0");
            else bets.add(presentedMatches.get(i).split(",")[Integer.parseInt(userIndex)]);
        }
        return bets;
    }

    private ArrayList<String> getGamesToPresent() {
        ArrayList<String> presentedMatches = new ArrayList<String>();
        if (gamesToPresent.equals("all")) {
            return matches;
        } else {
            for (String item : matches) {
                String[] s = item.split(",");
                if (item.split(",")[Globals.REAL_SCORE_COLUMN_INDEX].equals("0") || item.split(",")[Globals.REAL_SCORE_COLUMN_INDEX].equals("null")) {
                    presentedMatches.add(item);
                }
            }
            return presentedMatches;
        }
    }

    private void sortMatchesByTime(ArrayList<String> presentedMatches) {
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

    public void addGameClick(View view) {
        Intent intent = new Intent(this, AddToTable.class);
        intent.putExtra(AddToTable.EXTRA_INFO, tableName);
        startActivity(intent);
    }

    public void addUsersClick(View view) {
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

    private void updateUsers(ArrayList<String> users) {
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

    public void SubmitClick(View view) {

        ArrayList<String> params = new ArrayList<>();
        params.add(tableName);
        params.add(LoginPage.sharedPref.getString("username", ""));

        if (LoginPage.sharedPref.getString("username", "").equals("admin")) {
            for (int i = 0; i < presentedMatches.size(); i++) {
                params.add(presentedMatches.get(i).split(",")[0]);
                params.add(presentedMatches.get(i).split(",")[Integer.parseInt(userIndex)]);
            }
        } else {
            for (int i = 0; i < presentedMatches.size(); i++) {
                if (presentedMatches.get(i).split(",")[Globals.LOCKED_COLUMN_INDEX].equals("0")) {
                    if (!shouldBeLocked(presentedMatches.get(i).split(",")[Globals.DATE_COLUMN_INDEX])) {
                        params.add(presentedMatches.get(i).split(",")[0]);
                        params.add(presentedBets.get(i));
                    }
                }else{
                    params.add(presentedMatches.get(i).split(",")[0]);
                    params.add(presentedMatches.get(i).split(",")[Integer.parseInt(userIndex)]);
                }
            }
        }
        for (int i = 0; i < matches.size(); i++) {
            boolean found = false;
            for (int j = 0; j < presentedMatches.size(); j++) {
                if (matches.get(i).split(",")[0].equals(presentedMatches.get(j).split(",")[0])) {
                    found = true;
                }
            }
            if (!found) {
                params.add(matches.get(i).split(",")[0]);
                params.add(matches.get(i).split(",")[Integer.parseInt(userIndex)]);
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

    private boolean shouldBeLocked(String dateString) {
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

    public void CalculateScoreClick(View view) {


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

    private void calculateScore(final ArrayList<String> users) {

        PerformQuery query1 = new PerformQuery(this, "getLockedInfo", new PerformQuery.AsyncResponse() {

            @Override
            public void processFinish(String response) {
                response = response.trim();
                calcAll(response, users);
            }
        });
        query1.execute(tableName);
    }

    private void calcAll(String response, ArrayList<String> users) {
        String[] lockedGames = response.split("\n");

        System.out.println(lockedGames);

        for (int i = 0; i < users.size(); i++) {
            Map<String, String> userBets = new HashMap<>();
            for (int j = 0; j < lockedGames.length; j++) {
                userBets.put(lockedGames[j].split(",")[0], lockedGames[j].split(",")[Globals.DATE_COLUMN_INDEX + 1 + i]);
            }
            System.out.println(users.get(i).split(" ")[0] + "\n" + userBets.toString());
            userBets.put("username", users.get(i));
            updateScore(userBets, lockedGames);
        }


    }

    private void updateScore(Map<String, String> userBets, String[] lockedGames) {
        double sum = 0;

        for (int i = 0; i < lockedGames.length; i++) {
            System.out.println(lockedGames[i]);
            String[] line = lockedGames[i].split(",");
            String id = line[0];

            if  (userBets.get(id).equals(line[Globals.REAL_SCORE_COLUMN_INDEX])) {
                double d = Double.parseDouble(line[Integer.parseInt(line[Globals.REAL_SCORE_COLUMN_INDEX]) + (Globals.HOME_TEAM_WIN_RATE_COLUMN_INDEX - 1)]);
                sum += d - 1;
            }

            // If we decide to give
//                else  if (line[5].equals("1") && !line[6].equals("null") && betCol[i].trim().equals("null")) {
//                    sum+=0.5;
//                }
        }

        PerformQuery query = new PerformQuery(this, "updateScore", new PerformQuery.AsyncResponse() {
            @Override
            public void processFinish(String response) {

            }
        });
        query.execute(UsersList.USERS_TABLE, tableName, userBets.get("username").split(" ")[0], Double.toString(sum));
    }
}
