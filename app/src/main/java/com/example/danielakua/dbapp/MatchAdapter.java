package com.example.danielakua.dbapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

class MatchAdapter extends BaseAdapter
{
    public interface OnDataChangeListener{
        void onDataChanged(String response);
    }

    private OnDataChangeListener listener;
    private LayoutInflater mInflater;
    private ArrayList<String> mDataSource;
    private ArrayList<String> mBets;
    private String mTable;
    private Context context;

    MatchAdapter(Context context, ArrayList<String> items, ArrayList<String> bets, String table, OnDataChangeListener listener) {
        mTable = table;
        mDataSource = items;
        mBets = bets;
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView;
        final boolean isAdmin = MainActivity.sharedPref.getString("username", "").equals("admin");
        context = parent.getContext();
        final String[] entry = ((String) getItem(position)).split(",");
        boolean enabled = Integer.parseInt(entry[5]) == 0;
        rowView = mInflater.inflate(R.layout.game_row_view, parent, false);
        final TextView entryMatch = rowView.findViewById(R.id.entryMatch);
        final Button entryLock = rowView.findViewById(R.id.entryLock);
        final Button entryLeftWin = rowView.findViewById(R.id.entryLeftWin);
        final Button entryTie = rowView.findViewById(R.id.entryTie);
        final Button entryRightWin = rowView.findViewById(R.id.entryRightWin);

        entryMatch.setText(entry[1]);
        entryLeftWin.setText(entry[2]);
        entryTie.setText(entry[3]);
        entryRightWin.setText(entry[4]);

        if(!isAdmin){
            entryLeftWin.setEnabled(enabled);
            entryTie.setEnabled(enabled);
            entryRightWin.setEnabled(enabled);
        }

        int bet = mBets.get(position).equals("null") ? 0 : Integer.parseInt(mBets.get(position));
        switch(bet){
            case 1:
                entryLeftWin.setTextColor(Color.RED); break;
            case 2:
                entryTie.setTextColor(Color.RED); break;
            case 3:
                entryRightWin.setTextColor(Color.RED); break;
            default:
                mBets.set(position, "0");
        }

        entryLock.setVisibility(isAdmin ? View.VISIBLE : View.INVISIBLE);
        entryLock.setText(Integer.parseInt(entry[5]) == 0 ? "Lock" : "Unlock");
        entryLock.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                entry[5] = Integer.parseInt(entry[5]) == 0 ? "1" : "0";
                entryLock.setText(Integer.parseInt(entry[5]) == 0 ? "Lock" : "Unlock");
                updateLock(entry);
            }
        });
        entryLeftWin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                entryLeftWin.setTextColor(Color.RED);
                entryTie.setTextColor(Color.BLACK);
                entryRightWin.setTextColor(Color.BLACK);
                mBets.set(position,"1");
            }
        });
        entryTie.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                entryLeftWin.setTextColor(Color.BLACK);
                entryTie.setTextColor(Color.RED);
                entryRightWin.setTextColor(Color.BLACK);
                mBets.set(position,"2");
            }
        });
        entryRightWin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                entryLeftWin.setTextColor(Color.BLACK);
                entryTie.setTextColor(Color.BLACK);
                entryRightWin.setTextColor(Color.RED);
                mBets.set(position,"3");
            }
        });
        return rowView;
    }

    private void updateLock(String[] entry){
        PerformQuery query = new PerformQuery("update", new PerformQuery.AsyncResponse(){
            @Override
            public void processFinish(String response)
            {
                listener.onDataChanged(response);
            }
        });
        query.execute(mTable, entry[0], "locked", entry[5]);
    }
}
