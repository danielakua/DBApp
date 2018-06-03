package com.example.danielakua.dbapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

class MatchAdapter extends BaseAdapter {
    public interface OnDataChangeListener {
        void onDataChanged(String response);
    }

    private OnDataChangeListener listener;
    private LayoutInflater mInflater;
    private ArrayList<String> mDataSource;
    private ArrayList<String> mBets;
    private String mName;
    private String mTable;
    private final int defColor = -570425344;
    private final int defDisabled = android.R.color.secondary_text_dark;

    MatchAdapter(Context context, ArrayList<String> matches, ArrayList<String> bets, String table, String name, OnDataChangeListener listener) {
        mTable = table;
        mName = name;
        mDataSource = matches;
        mBets = bets;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return mDataSource.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataSource.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView;
        final boolean isAdmin = LoginPage.sharedPref.getString("username", "").equals("admin");
        final boolean isUser = LoginPage.sharedPref.getString("username", "").equals(mName);

        final String[] entry = ((String) getItem(position)).split(",");
        boolean enabled = Integer.parseInt(entry[5]) == 0;
        int realScore = Integer.parseInt(entry[6]);
        rowView = mInflater.inflate(R.layout.game_row_view, parent, false);

        final TextView entryMatch = rowView.findViewById(R.id.entryMatch);
        final Button entryLock = rowView.findViewById(R.id.entryLock);
        final Button entryDelgame = rowView.findViewById(R.id.entryDelgame);
        final Button entryLeftWin = rowView.findViewById(R.id.entryLeftWin);
        final Button entryTie = rowView.findViewById(R.id.entryTie);
        final Button entryRightWin = rowView.findViewById(R.id.entryRightWin);

        entryMatch.setText(entry[1]);
        entryLeftWin.setText(entry[2]);
        entryTie.setText(entry[3]);
        entryRightWin.setText(entry[4]);

        switch (realScore) {
            case 0: {
                break;
            }
            case 1: {
                entryLeftWin.setBackgroundColor(Color.GREEN);
                break;
            }
            case 2: {
                entryTie.setBackgroundColor(Color.GREEN);
                break;
            }
            case 3: {
                entryRightWin.setBackgroundColor(Color.GREEN);
                break;
            }
        }

        if (!isAdmin) {
            entryLeftWin.setEnabled(enabled);
            entryTie.setEnabled(enabled);
            entryRightWin.setEnabled(enabled);
        }

        if (!isUser) {
            entryLeftWin.setEnabled(false);
            entryTie.setEnabled(false);
            entryRightWin.setEnabled(false);
        }

        int bet = mBets.get(position).equals("null") ? 0 : Integer.parseInt(mBets.get(position));
        switch (bet) {
            case 1:
                entryLeftWin.setTextColor(Color.RED);
                break;
            case 2:
                entryTie.setTextColor(Color.RED);
                break;
            case 3:
                entryRightWin.setTextColor(Color.RED);
                break;
            default:
                mBets.set(position, "0");
        }

        entryLock.setVisibility(isAdmin && isUser ? View.VISIBLE : View.GONE);
        entryLock.setText(Integer.parseInt(entry[5]) == 0 ? "Lock" : "Unlock");
        entryLock.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                entry[5] = Integer.parseInt(entry[5]) == 0 ? "1" : "0";
                entryLock.setText(Integer.parseInt(entry[5]) == 0 ? "Lock" : "Unlock");
                updateLock(entry);
            }
        });
        entryDelgame.setVisibility(isAdmin && isUser ? View.VISIBLE : View.GONE);
        entryDelgame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteMatch(entry[0]);
            }
        });
        entryLeftWin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                boolean colorB = entryLeftWin.getTextColors().getDefaultColor() == defColor;
                entryLeftWin.setTextColor(colorB ? Color.RED : defColor);
                entryTie.setTextColor(defColor);
                entryRightWin.setTextColor(defColor);
                mBets.set(position, colorB ? "1" : "0");
            }
        });
        entryTie.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                boolean colorB = entryTie.getTextColors().getDefaultColor() == defColor;
                entryLeftWin.setTextColor(defColor);
                entryTie.setTextColor(colorB ? Color.RED : defColor);
                entryRightWin.setTextColor(defColor);
                mBets.set(position, colorB ? "2" : "0");
            }
        });
        entryRightWin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                boolean colorB = entryRightWin.getTextColors().getDefaultColor() == defColor;
                entryLeftWin.setTextColor(defColor);
                entryTie.setTextColor(defColor);
                entryRightWin.setTextColor(colorB ? Color.RED : defColor);
                mBets.set(position, colorB ? "3" : "0");
            }
        });
        return rowView;
    }

    private void deleteMatch(String match) {
        PerformQuery query = new PerformQuery(null, "delete", new PerformQuery.AsyncResponse() {
            @Override
            public void processFinish(String response) {
                listener.onDataChanged(response);
            }
        });
        query.execute(match, mTable);
    }

    private void updateLock(String[] entry) {
        PerformQuery query = new PerformQuery(null, "update", new PerformQuery.AsyncResponse() {
            @Override
            public void processFinish(String response) {
                listener.onDataChanged(response);
            }
        });
        query.execute(mTable, entry[0], "locked", entry[5]);
    }
}
