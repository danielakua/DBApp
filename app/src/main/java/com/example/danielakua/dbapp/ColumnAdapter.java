package com.example.danielakua.dbapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

class ColumnAdapter extends BaseAdapter
{
    public interface OnDataChangeListener{
        void onDataChanged(Column column);
    }

    private OnDataChangeListener listener;
    private LayoutInflater mInflater;
    private ArrayList<Column> mDataSource;

    ColumnAdapter(Context context, ArrayList<Column> items, OnDataChangeListener listener) {
        mDataSource = items;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.listener = listener;
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
        View rowView = mInflater.inflate(R.layout.simple_column_view, parent, false);
        final Column entry = (Column) getItem(position);
        final TextView entryName = rowView.findViewById(R.id.entryName);
        final TextView entryType = rowView.findViewById(R.id.entryType);
        final Button entryDelete = rowView.findViewById(R.id.entryDelete);
        entryName.setText(entry.get_name());
        entryType.setText(entry.get_type());
        entryDelete.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    listener.onDataChanged(entry);
            }
        });
        return rowView;
    }
}
