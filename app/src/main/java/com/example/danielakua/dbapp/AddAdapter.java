package com.example.danielakua.dbapp;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

class AddAdapter extends BaseAdapter
{
    private LayoutInflater mInflater;
    private ArrayList<Column> mDataSource;

    AddAdapter(Context context, ArrayList<Column> items) {
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
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return ((Column) getItem(position)).get_type().equals("BOOLEAN") ? 0 : 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView;
        int type = getItemViewType(position);
        final Column entry = (Column) getItem(position);
        final TextView entryCol;
        final TextView entryType;
        if(type == 0){
            rowView = mInflater.inflate(R.layout.bool_add_user, parent, false);
            entryCol = rowView.findViewById(R.id.entryBoolCol);
            entryType = rowView.findViewById(R.id.entryBoolType);
            final Spinner entryBool = rowView.findViewById(R.id.entryBoolean);
            final String[] items = new String[]{ "TRUE", "FALSE" };
            ArrayAdapter<String> adapter = new ArrayAdapter<>(parent.getContext(), android.R.layout.simple_spinner_dropdown_item, items);
            entryBool.setAdapter(adapter);
            entryBool.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    entry.set_value((String) parent.getItemAtPosition(pos));
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    entry.set_value(items[0]);
                }
            });
        }
        else {
            rowView = mInflater.inflate(R.layout.value_add_user, parent, false);
            entryCol = rowView.findViewById(R.id.entryValueCol);
            entryType = rowView.findViewById(R.id.entryValueType);
            EditText entryValue = rowView.findViewById(R.id.entryValue);
            if(entry.get_type().equals("INTEGER")){
                entryValue.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
            entryValue.setText(entry.get_value());
            entryValue.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                @Override
                public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                    entry.set_value(s.toString());
                }

                @Override
                public void afterTextChanged(Editable editable) { }
            });
        }

        entryCol.setText(entry.get_name());
        entryType.setText(entry.get_type());

        return rowView;
    }
}
