package com.example.danielakua.dbapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

class RecordAdapter extends BaseAdapter
{
    private LayoutInflater mInflater;
    private ArrayList<Record> mDataSource;

    public RecordAdapter(Context context, ArrayList<Record> items) {
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
        // Get view for row item
        View rowView = mInflater.inflate(R.layout.simple_list_view, parent, false);

        Record entry = (Record) getItem(position);
        TextView entryName = rowView.findViewById(R.id.entryName);
        TextView entryScore = rowView.findViewById(R.id.entryScore);
        entryName.setText(entry.get_name());
        entryScore.setText(String.format("%.2f", entry.get_score()));

        return rowView;
    }
}
