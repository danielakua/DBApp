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
import java.util.Arrays;

class UserAdapter extends BaseAdapter {
    public interface OnDataChangeListener {
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
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView;
        context = parent.getContext();
        if (mTable.equals(UsersList.USERS_TABLE)) {
            rowView = SpecialCase(position, parent);
        } else {
            String entry = (String) getItem(position);
            rowView = mInflater.inflate(R.layout.simple_user_view, parent, false);
            final TextView entryUser = rowView.findViewById(R.id.entryUser);
            final Button entryPW = rowView.findViewById(R.id.entryPW);
            final Button entryDelete = rowView.findViewById(R.id.entryDelete);
            entryUser.setText(entry);
            entryDelete.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    deleteUser((String) entryUser.getText(), mTable);
                }
            });
            entryPW.setVisibility(mTable.equals(TablesList.MAIN_TABLE) ? View.GONE : View.VISIBLE);
        }
        return rowView;
    }

    private View SpecialCase(int position, ViewGroup parent) {
        String entry = (String) getItem(position);
        String name = entry.split(" ")[0];
        Boolean approved = entry.split(" ")[1].equals("t");
        View rowView;
        if (approved) {
            rowView = mInflater.inflate(R.layout.simple_user_view, parent, false);
            final TextView entryUser = rowView.findViewById(R.id.entryUser);
            final Button entryPW = rowView.findViewById(R.id.entryPW);
            final Button entryDelete = rowView.findViewById(R.id.entryDelete);
            entryUser.setText(name);
            entryDelete.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    deleteUser((String) entryUser.getText(), mTable);
                }
            });
            entryPW.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    GoToUpdateInfo(entryUser.getText().toString());
                }
            });
        } else {
            rowView = mInflater.inflate(R.layout.simple_application_view, parent, false);
            final TextView entryUser = rowView.findViewById(R.id.entryUser);
            final Button entryApprove = rowView.findViewById(R.id.entryApprove);
            final Button entryDelete = rowView.findViewById(R.id.entryDecline);
            entryUser.setText(name);
            entryApprove.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    approveUser((String) entryUser.getText());
                }
            });
            entryDelete.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    rejectUser((String) entryUser.getText());
                }
            });
        }
        return rowView;
    }

    private void deleteUser(final String username, final String table) {
        PerformQuery query = new PerformQuery(null, "delete", new PerformQuery.AsyncResponse() {
            @Override
            public void processFinish(String response) {
                listener.onDataChanged(response);
                deleteFromGamesTables(username);
            }
        });
        query.execute(username, table);
    }

    private void deleteFromGamesTables(final String username) {
        PerformQuery query = new PerformQuery(null, "getTables", new PerformQuery.AsyncResponse() {
            @Override
            public void processFinish(String response) {
                response = response.trim();
                ArrayList<String> tables = response.isEmpty() ? new ArrayList<String>() : new ArrayList<>(Arrays.asList(response.split("\n")));
                tables.remove("main_table");
                try {
                    tables.remove("users");
                } catch (Exception e) {
                    System.out.println("no users table");
                }
                for (String table : tables) {
                    deleteUserFromTable(table, username);
                }
            }
        });
        query.execute("admin");

    }

    private void deleteUserFromTable(String table, String username) {
        PerformQuery query = new PerformQuery(null, "performDeleteColumn", new PerformQuery.AsyncResponse() {
            @Override
            public void processFinish(String response) {
                listener.onDataChanged(response);

            }
        });
        query.execute(table, username);
    }

    private void rejectUser(String username) {
        PerformQuery query = new PerformQuery(null, "register", new PerformQuery.AsyncResponse() {
            @Override
            public void processFinish(String response) {
                listener.onDataChanged(response);
            }
        });
        query.execute(username, "false");
    }

    private void approveUser(String username) {
        PerformQuery query = new PerformQuery(null, "register", new PerformQuery.AsyncResponse() {
            @Override
            public void processFinish(String response) {
                listener.onDataChanged(response);
            }
        });
        query.execute(username, "true");

        query = new PerformQuery(null, "addUserToAllTables", new PerformQuery.AsyncResponse() {
            @Override
            public void processFinish(String response) {
                listener.onDataChanged(response);
            }
        });
        query.execute(username, "true");
    }

    private void GoToUpdateInfo(String username) {
        Intent intent = new Intent(context, UpdateInfo.class);
        intent.putExtra(UpdateInfo.EXTRA_INFO, username);
        context.startActivity(intent);
    }
}
