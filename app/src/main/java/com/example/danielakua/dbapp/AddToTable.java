package com.example.danielakua.dbapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class AddToTable extends AppCompatActivity {

    public static final String EXTRA_INFO = "EXTRA_INFO_TABLE_NAME";
    protected String tableName;
    protected ArrayList<Column> columns;
    protected TextView errorAdd;
    protected TextView titleAdd;
    protected ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_to_table);

        tableName = getIntent().getStringExtra(EXTRA_INFO);
        titleAdd = findViewById(R.id.titleAdd);
        titleAdd.setText(String.format(getString(R.string.add_to_table_title), tableName));
        errorAdd = findViewById(R.id.errorAdd);
        listView = findViewById(R.id.infoAdd);
        listView.setItemsCanFocus(true);

        ClearClick(null);
    }

    public void AddClick(View view){
        if(!checkColumns()){
            return;
        }
        String[] params = getParamsList();
        errorAdd.setText("Attempting to communicate with the server...");
        PerformQuery query = new PerformQuery("addToTable", new PerformQuery.AsyncResponse(){
            @Override
            public void processFinish(String response)
            {
                response = response.trim();
                errorAdd.setText(response);
            }
        });
        query.execute(params);
    }

    public void ClearClick(View view){
        columns = new ArrayList<>();
        getColumns();
    }

    protected void loadColumns(){
        AddAdapter adapter = new AddAdapter(this, columns);
        listView.setAdapter(adapter);
    }

    private void getColumns(){
        errorAdd.setText("Attempting to communicate with the server...");
        PerformQuery query = new PerformQuery("columns", new PerformQuery.AsyncResponse(){
            @Override
            public void processFinish(String response)
            {
                response = response.trim();
                buildColumnsList(response);
                errorAdd.setText("");
            }
        });
        query.execute(tableName);
    }

    private void buildColumnsList(String response){
        String name, type;
        ArrayList<String> cols = new ArrayList<>(Arrays.asList(response.split("\n")));
        for(String col : cols){
            ArrayList<String> list = new ArrayList<>(Arrays.asList(col.split(" ")));
            switch(list.remove(list.size() - 1)){
                case "text":
                    type = "TEXT"; break;
                case "int4":
                    type = "INTEGER"; break;
                case "bool":
                    type = "BOOLEAN"; break;
                case "float8":
                    type = "FLOAT"; break;
                default:
                    type = "";
            }
            name = joinList(list);
            columns.add(new Column(name, type));
        }
        loadColumns();
    }

    private String joinList(ArrayList<String> list){
        String delim = "";
        StringBuilder buf = new StringBuilder("");
        for(String element : list){
            buf.append(delim);
            delim = " ";
            buf.append(element);
        }
        return buf.toString();
    }

    private boolean checkColumns(){
        for(Column col : columns){
            if(col.get_value().isEmpty()){
                errorAdd.setText(String.format("Enter %s", col.get_name()));
                return false;
            }
            switch(col.get_type()){
                case "INTEGER":
                    if (!col.get_value().matches("^\\d+$")){
                        errorAdd.setText(String.format("%s must contain only numbers", col.get_name()));
                        return false;
                    }
                    break;
                case "FLOAT":
                    if (!col.get_value().matches("^\\d+\\.?\\d+$")){
                        errorAdd.setText(String.format("%s must contain only numbers and a dot", col.get_name()));
                        return false;
                    }
                    break;
                case "BOOLEAN":
                    if(!(col.get_value().equals("TRUE") || (col.get_value().equals("FALSE")))){
                        errorAdd.setText(String.format("%s must be TRUE or FALSE", col.get_name()));
                        return false;
                    }
                    break;
            }
        }
        return true;
    }

    private String[] getParamsList(){
        ArrayList<String> params = new ArrayList<>();
        params.add(tableName);
        for(Column column : columns){
            params.add(column.get_name());
            params.add(column.get_value());
        }
        return params.toArray(new String[params.size()]);
    }
}
