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

class UserAdapter extends BaseAdapter
{
    public interface OnDataChangeListener{
        void onDataChanged(String response);
    }

    private OnDataChangeListener listener;
    private LayoutInflater mInflater;
    private ArrayList<String> mDataSource;
    private String mTable;
    private Context context;

    UserAdapter(Context context, ArrayList<String> items, String table, OnDataChangeListener listener) {
        mTable = table;
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
        View rowView;
        context = parent.getContext();
        if(mTable.equals(UsersList.USERS_TABLE))
        {
            rowView = SpecialCase(position, parent);
        }
        else
        {
            String entry = (String) getItem(position);
            rowView = mInflater.inflate(R.layout.simple_user_view, parent, false);
            final TextView entryUser = rowView.findViewById(R.id.entryUser);
            final Button entryPW = rowView.findViewById(R.id.entryPW);
            final Button entryDelete = rowView.findViewById(R.id.entryDelete);
            entryUser.setText(entry);
            entryDelete.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                    entryDelete.setVisibility(View.INVISIBLE);
                    entryUser.setPaintFlags(entryUser.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    deleteUser((String) entryUser.getText(), mTable);
                }
            });
            entryPW.setVisibility(mTable.equals(TablesList.MAIN_TABLE) ? View.INVISIBLE : View.VISIBLE);
        }
        return rowView;
    }

    private View SpecialCase(int position, ViewGroup parent){
        String entry = (String) getItem(position);
        String name = entry.split(" ")[0];
        Boolean approved = entry.split(" ")[1].equals("t");
        View rowView;
        if(approved) {
            rowView = mInflater.inflate(R.layout.simple_user_view, parent, false);
            final TextView entryUser = rowView.findViewById(R.id.entryUser);
            final Button entryPW = rowView.findViewById(R.id.entryPW);
            final Button entryDelete = rowView.findViewById(R.id.entryDelete);
            entryUser.setText(name);
            entryDelete.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    entryDelete.setVisibility(View.INVISIBLE);
                    entryUser.setPaintFlags(entryUser.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    deleteUser((String) entryUser.getText(), mTable);
                }
            });
            entryPW.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    GoToUpdateInfo(entryUser.getText().toString());
                }
            });
        }
        else {
            rowView = mInflater.inflate(R.layout.simple_application_view, parent, false);
            final TextView entryUser = rowView.findViewById(R.id.entryUser);
            final Button entryApprove = rowView.findViewById(R.id.entryApprove);
            final Button entryDelete = rowView.findViewById(R.id.entryDecline);
            entryUser.setText(name);
            entryApprove.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    entryDelete.setVisibility(View.INVISIBLE);
                    entryApprove.setVisibility(View.INVISIBLE);
                    entryUser.setPaintFlags(entryUser.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    approveUser((String) entryUser.getText());
                }
            });
            entryDelete.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    entryDelete.setVisibility(View.INVISIBLE);
                    entryApprove.setVisibility(View.INVISIBLE);
                    entryUser.setPaintFlags(entryUser.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    rejectUser((String) entryUser.getText());
                }
            });
        }
        return rowView;
    }

    private void deleteUser(String username, final String table)
    {
        PerformQuery query = new PerformQuery(null, "delete", new PerformQuery.AsyncResponse(){
            @Override
            public void processFinish(String response)
            {
                listener.onDataChanged(response);
            }
        });
        query.execute(username, table);
    }

    private void rejectUser(String username)
    {
        PerformQuery query = new PerformQuery(null, "register", new PerformQuery.AsyncResponse(){
            @Override
            public void processFinish(String response)
            {
                listener.onDataChanged(response);
            }
        });
        query.execute(username, "false");
    }

    private void approveUser(String username)
    {
        PerformQuery query = new PerformQuery(null, "register", new PerformQuery.AsyncResponse(){
            @Override
            public void processFinish(String response)
            {
                listener.onDataChanged(response);
            }
        });
        query.execute(username, "true");
    }

    private void GoToUpdateInfo(String username)
    {
        Intent intent = new Intent(context, UpdateInfo.class);
        intent.putExtra(UpdateInfo.EXTRA_INFO, username);
        context.startActivity(intent);
    }
}
