package com.example.danielakua.dbapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class RecordsPageActivity extends AppCompatActivity {

    public StringBuilder logger = LogsPageActivity.logger; // logger instance
    protected final String TAG = "Records Page:";// logger tag
    protected RecordsDB db;// database instance
    ArrayList<Record> records = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records_page);

        Log("Entered Records Page");

        db = new RecordsDB(this);
        getRecords();
    }

    // add messages to the log
    public void Log(String msg)
    {
        logger.append(String.format("%s %s\n", TAG, msg));
    }

    private void getRecords(){
        PerformQuery query = new PerformQuery("getScore", new PerformQuery.AsyncResponse(){
            @Override
            public void processFinish(String response)
            {
                response = response.trim();
                String[] scores = response.split("\n");
                for(String score : scores){
                    records.add(new Record(score.split(",")[0], score.split(",")[1]));
                }
                loadRecords();
            }
        });
        query.execute(UsersList.USERS_TABLE);
    }

    // load the records
    void loadRecords()
    {
        // Sort records according to score
        Collections.sort(records, new Comparator<Record>()
        {
            public int compare(Record o1, Record o2)
            {
                int o1I = Integer.parseInt(o1.get_score());
                int o2I = Integer.parseInt(o2.get_score());
                return o1I > o2I ? -1 : o1I == o2I ? 0 : 1;
            }
        });

        // keep top 20 scores
        int limit = records.size();
        int max = 20;
        if ( limit > max )
            records.subList(max, limit).clear();

        Log(String.format("Displaying top %d scores", max));

        // show scores in list view
        ListView listView = findViewById(R.id.records);
        RecordAdapter adapter = new RecordAdapter(this, records);
        listView.setAdapter(adapter);
    }
}
