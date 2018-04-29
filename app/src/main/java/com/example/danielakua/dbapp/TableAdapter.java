package com.example.danielakua.dbapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
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
    private Context context;

    TableAdapter(Context context, ArrayList<String> items) {
        mDataSource = items;
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
        rowView = mInflater.inflate(R.layout.simple_column_view, parent, false);
        final String entry = (String) getItem(position);
        final TextView entryName = rowView.findViewById(R.id.entryName);
        final Button entryTable = rowView.findViewById(R.id.entryDelete);
        entryName.setText(entry);
        entryTable.setText("View");
        entryTable.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                GoToTable(entry);
            }
        });
        return rowView;
    }

    private void GoToTable(String table)
    {
        Intent intent = new Intent(context, UsersList.class);
        intent.putExtra(UsersList.EXTRA_INFO, table);
        context.startActivity(intent);
    }
}
