package com.example.danielakua.dbapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

class TableAdapter extends BaseAdapter
{

    private LayoutInflater mInflater;
    private ArrayList<String> mDataSource;
    private ArrayList<String> mViewables;
    private Context context;

    TableAdapter(Context context, ArrayList<String> items, ArrayList<String> viewables) {
        mDataSource = items;
        mViewables = viewables;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount()
    {
        return mDataSource.size();
    }

    @Override
    public Object getItem(int position)
    {
        return mDataSource.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView;
        // Get view for row item
        context = parent.getContext();
        rowView = mInflater.inflate(R.layout.simple_user_view, parent, false);
        final String entry = (String) getItem(position);
        final TextView entryUser = rowView.findViewById(R.id.entryUser);
        final Button entryTable = rowView.findViewById(R.id.entryDelete);
        final Button viewable = rowView.findViewById(R.id.entryPW);

        entryUser.setText(entry);

        entryTable.setText("View");
        entryTable.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                GoToTable(entry);
            }
        });

        if(LoginPage.sharedPref.getString("username", "").equals("admin")) {
            if(entry.equals(TablesList.MAIN_TABLE) || entry.equals(UsersList.USERS_TABLE)){
                viewable.setVisibility(View.INVISIBLE);
            }
            else {
                viewable.setText("Viewable");
                viewable.setBackgroundColor(CheckViewable(entry) ? Color.GREEN : Color.RED);
                viewable.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (CheckViewable(entry)) {
                            mViewables.remove(entry);
                        } else {
                            mViewables.add(entry);
                        }
                        viewable.setBackgroundColor(CheckViewable(entry) ? Color.GREEN : Color.RED);
                        SetViewable(entry);
                    }
                });
            }
        }
        else {
            viewable.setVisibility(View.INVISIBLE);
        }
        return rowView;
    }

    private void GoToTable(String table) {
        boolean kind = table.equals(GameList.MATCH_TABLE);
        Intent intent = new Intent(context, kind ? GameList.class : UsersList.class);
        intent.putExtra(kind ? GameList.EXTRA_INFO : UsersList.EXTRA_INFO, table);
        context.startActivity(intent);
    }

    private void SetViewable(String table) {
        String action;
        String[] params;
        if(CheckViewable(table)){
            action = "addToTable";
            params = new String[] { TablesList.MAIN_TABLE, "name", table };
        }
        else{
            action = "delete";
            params = new String[] { table, TablesList.MAIN_TABLE };
        }

        PerformQuery query = new PerformQuery(action, new PerformQuery.AsyncResponse(){
            @Override
            public void processFinish(String response)
            {}
        });
        query.execute(params);
    }

    private boolean CheckViewable(String table){
        return mViewables.contains(table);
    }
}
