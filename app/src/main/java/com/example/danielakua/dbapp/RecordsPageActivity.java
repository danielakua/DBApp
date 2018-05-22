package com.example.danielakua.dbapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class RecordsPageActivity extends AppCompatActivity {

    ArrayList<Record> records = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records_page);

        getRecords();
    }

    private void getRecords(){
        PerformQuery query = new PerformQuery("getScore", new PerformQuery.AsyncResponse(){
            @Override
            public void processFinish(String response)
            {
                response = response.trim();
                String[] scores = response.split("\n");
                for(String score : scores){
                    if(score.isEmpty()) { continue; }
                    records.add(new Record(score.split(",")[0], Double.parseDouble(score.split(",")[1])));
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
                Double o1I = o1.get_score();
                Double o2I = o2.get_score();
                return o1I > o2I ? -1 : o1I < o2I ? 1 : 0;
            }
        });

        // show scores in list view
        ListView listView = findViewById(R.id.records);
        RecordAdapter adapter = new RecordAdapter(this, records);
        listView.setAdapter(adapter);
    }
}
