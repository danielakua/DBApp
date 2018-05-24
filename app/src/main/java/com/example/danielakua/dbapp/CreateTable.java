package com.example.danielakua.dbapp;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class CreateTable extends AppCompatActivity {

    protected LinearLayout newColumnL;
    protected Spinner columnType;
    protected TextView errorTable;
    protected EditText columnName;
    protected ArrayList<Column> columns = new ArrayList<>();
    protected String currType;
    protected String tableName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_table);

        newColumnL = findViewById(R.id.addLayoutTable);
        columnType = findViewById(R.id.columnTypeTable);
        columnName = findViewById(R.id.columnNameTable);
        errorTable = findViewById(R.id.errorTable);

        final String[] items = new String[]{ "TEXT", "FLOAT", "INTEGER", "BOOLEAN" };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        columnType.setAdapter(adapter);
        columnType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                currType = (String) parent.getItemAtPosition(pos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                currType = items[0];
            }
        });

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    protected void onResume(){
        super.onResume();
        newColumnL.setVisibility(View.GONE);
    }

    public void AddColumnClick(View view){
        errorTable.setText("");
        newColumnL.setVisibility(View.VISIBLE);
    }

    public void CreateClick(View view){
        tableName = ((EditText) findViewById(R.id.tableName)).getText().toString();
        if(tableName.isEmpty()){
            errorTable.setText("Enter table name");
            return;
        }
        if(columns.isEmpty()){
            errorTable.setText("Table must have at least one column");
            return;
        }
        PerformQuery query = new PerformQuery(this, "customTable", new PerformQuery.AsyncResponse(){
            @Override
            public void processFinish(String response)
            {
                response = response.trim();
                errorTable.setText(response);
                if(response.startsWith("created table")) {
                    addScoreColumn();
                }
            }
        });
        query.execute(getParamsList());
    }

    protected void addScoreColumn() {
        PerformQuery query = new PerformQuery(this, "addColumn", new PerformQuery.AsyncResponse(){
            @Override
            public void processFinish(String response) { }
        });
        query.execute(UsersList.USERS_TABLE, tableName, "FLOAT");
    }

    protected void loadColumns(){
        ListView listView = findViewById(R.id.columnListTable);
        ColumnAdapter adapter = new ColumnAdapter(this, columns, new ColumnAdapter.OnDataChangeListener() {
            @Override
            public void onDataChanged(Column column) {
                columns.remove(column);
                loadColumns();
            }
        });
        listView.setAdapter(adapter);
    }

    public void confirmColumnClick(View view){
        String name = columnName.getText().toString().trim();
        if(name.isEmpty()){
            errorTable.setText("Enter column name");
            return;
        }
        if(checkName(name)){
            errorTable.setText(String.format("Column %s already exists", name));
            return;
        }
        columns.add(new Column(name, currType));
        columnName.setText("");
        columnName.clearFocus();
        errorTable.setText("");

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (view != null && imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        loadColumns();
        newColumnL.setVisibility(View.INVISIBLE);
    }

    private boolean checkName(String name){
        if (name.equals("id")) { return true; }
        for(Column column : columns){
            if(column.get_name().equals(name)){
                return true;
            }
        }
        return false;
    }

    private String[] getParamsList(){
        ArrayList<String> params = new ArrayList<>();
        params.add(tableName);
        for(Column column : columns){
            params.add(column.get_name());
            params.add(column.get_type());
        }
        return params.toArray(new String[params.size()]);
    }
}
