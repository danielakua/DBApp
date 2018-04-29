package com.example.danielakua.dbapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class RecordsDB extends SQLiteOpenHelper {

    // All Static variables

    private static final int DATABASE_VERSION = 1;// Database Version
    private static final String DATABASE_NAME = "recordsManager";// Database Name
    private static final String TABLE_RECORDS = "records";// Records table name

    // Records table columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_SCORE = "score";

    public RecordsDB(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_RECORDS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_SCORE + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECORDS);

        // Create tables again
        onCreate(db);
    }

    // Adding new record
    void addRecord(Record record)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, record.get_name()); // Record Name
        values.put(KEY_SCORE, record.get_score()); // Record Score

        // Inserting Row
        db.insert(TABLE_RECORDS, null, values);
        db.close(); // Closing database connection
    }

    // Getting single record
    Record getRecord(int id)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_RECORDS, new String[] { KEY_ID,
                        KEY_NAME, KEY_SCORE }, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);

        if (cursor != null)
        {
            cursor.moveToFirst();
        }

        Record record = new Record(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2));
        cursor.close();

        // return record
        return record;
    }

    // Getting All records
    public ArrayList<Record> getAllRecords() {
        ArrayList<Record> recordList = new ArrayList<Record>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_RECORDS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Record contact = new Record();
                contact.set_id(Integer.parseInt(cursor.getString(0)));
                contact.set_name(cursor.getString(1));
                contact.set_score(cursor.getString(2));
                // Adding contact to list
                recordList.add(contact);
            } while (cursor.moveToNext());
        }
        cursor.close();

        // return record list
        return recordList;
    }

    // Updating single record
    public int updateRecord(Record record) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, record.get_name());
        values.put(KEY_SCORE, record.get_score());

        // updating row
        return db.update(TABLE_RECORDS, values, KEY_ID + " = ?",
                new String[] { String.valueOf(record.get_id()) });
    }

    // Deleting single record
    public void deleteRecord(Record record) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_RECORDS, KEY_ID + " = ?",
                new String[] { String.valueOf(record.get_id()) });
        db.close();
    }


    // Getting records Count
    public int getRecordsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_RECORDS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }
}
