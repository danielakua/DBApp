package com.example.danielakua.dbapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class PerformQuery extends AsyncTask<String, String, String> {
    public interface AsyncResponse {
        void processFinish(String response);
    }

    private AsyncResponse delegate = null;
    private String action;
    private Connection c = null;
    private Statement stmt = null;
    private ProgressDialog progressDialog;
    private boolean dismiss = false;

    PerformQuery(Context context, String action, AsyncResponse delegate) {
        this.delegate = delegate;
        this.action = action;
        if (context != null) {
            dismiss = true;
            progressDialog = new ProgressDialog(context);
        }
    }

    @Override
    protected void onPreExecute() {
        if(dismiss) {
            progressDialog.setMessage("Please Wait");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (dismiss) {
            progressDialog.dismiss();
        }
        delegate.processFinish(result);
    }

    @Override
    protected void onProgressUpdate(String... text) {}

    @Override
    protected void onCancelled()
    {
        if (dismiss) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected String doInBackground(String... params) {
        String response = "";


        try {
            Class.forName("org.postgresql.Driver");

            String dburl = LoginPage.sharedPref.getString("dburl", null);

            c = DriverManager.getConnection(dburl);
            c.setAutoCommit(false);
        }
        catch (Exception e) {
            e.printStackTrace();
            return "one or more of the DB credentials are wrong";
        }

        switch (action) {
            case "ping":
                response = "";
                break;
            case "create":
                response = performCreate();
                break;
            case "customTable":
                response = performCustom(params);
                break;
            case "columns":
                response = performGetColumns(params);
                break;
            case "getColumns":
                response = performGetColumnValue(params);
                break;
            case "addColumn":
                response = performAddColumn(params);
                break;
            case "addColumns":
                response = performAddColumns(params);
                break;
            case "updateColumn":
                response = performUpdateColumn(params);
                break;
            case "checkApply":
                response = String.valueOf(checkApplied(params));
                break;
            case "addToTable":
                response = performAddToTable(params);
                break;
            case "getTables":
                response = performGetTables();
                break;
            case "getRelevantTables":
                response = performGetRelevantTables(params);
                break;
            case "delete":
                response = performDelete(params);
                break;
            case "login":
                response = performLogin(params);
                break;
            case "register":
                response = performRegister(params);
                break;
            case "apply":
                response = performApply(params);
                break;
            case "update":
                response = performUpdate(params);
                break;
            case "updateScore":
                response = updateScore(params);
                break;
            case "getValue":
                response = getValue(params);
                break;
            case "getScore":
                response = getScore(params);
                break;
            case "getAllInfo":
                response = performAllFromTable(params);
                break;
            case "getAllUsers":
                response = performKeysFromTable(params);
                break;
        }

        try {
            if (stmt != null) {
                stmt.close();
            }
            c.commit();
            c.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            response = "Can't connect to the server";
        }
        return response;
    }

    private String performGetRelevantTables(String... params){
        StringBuilder response = new StringBuilder("");
        String column = params[0];
        String delim = "";
        try {
            String query = String.format("SELECT table_name FROM information_schema.columns WHERE column_name = '%s'", column);
            System.out.println(query);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()) {
                response.append(delim);
                delim = "\n";
                response.append(rs.getString("table_name"));
            }
        }
        catch (Exception e){
            response.append("server error");
            e.printStackTrace();
        }
        return response.toString();
    }

    private String performGetTables(){
        StringBuilder response = new StringBuilder("");
        String delim = "";
        try {
            String query = "SELECT table_name FROM information_schema.tables WHERE table_schema='public' " +
                           "AND table_type='BASE TABLE' AND table_catalog='" + LoginPage.sharedPref.getString("dbname", "dotgamedb") + "';";
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()) {
                response.append(delim);
                delim = "\n";
                response.append(rs.getString("table_name"));
            }
        }
        catch (Exception e){
            response.append("server error");
            e.printStackTrace();
        }
        return response.toString();

    }

    private String performAddColumn(String... params) {
        String response;
        String table = params[0];
        String col = params[1];
        String type = params[2];
        try{
            String query = String.format("ALTER TABLE %s ADD COLUMN %s %s;", table, col, type);
            System.out.println(query);
            stmt = c.createStatement();
            stmt.executeUpdate(query);
            response = String.format("added column %s to table %s", col, table);
        }
        catch (Exception e){
            e.printStackTrace();
            response = "server error";
        }
        return response;
    }

    private String performAddColumns(String... params){
        String response;
        String table = params[0];
        try {
            String columns = performGetColumns(table);
            for (int i = 1; i < params.length; i++) {
                if(columns.contains(params[i] + " " + params[i + 1])){
                    i++;
                    continue;
                }
                performAddColumn(table, params[i++], params[i]);
            }
            response = "Added successfully";
        }
        catch (Exception e){
            e.printStackTrace();
            response = "server error";
        }
        return response;
    }

    private String performUpdateColumn(String... params){
        String response;
        String table = params[0];
        String col = params[1];
        String pkey = getPrimaryKey(table);
        StringBuilder query = new StringBuilder(String.format("UPDATE %s SET %s = CASE %s ", table, col, pkey));
        for(int i = 2; i < params.length; i++){
            query.append(String.format("WHEN '%s' THEN %s ", params[i++], params[i]));
        }
        query.append("END");
        try {
            System.out.println(query.toString());
            stmt = c.createStatement();
            stmt.executeUpdate(query.toString());
            response = "updated successfully";
        }
        catch (Exception e){
            e.printStackTrace();
            response = "server error";
        }
        return response;
    }

    private String performGetColumns(String... params){
        StringBuilder response = new StringBuilder("");
        String table = params[0];
        try{
            String query = String.format("SELECT column_name, data_type FROM information_schema.columns WHERE table_name='%s';", table);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()) {
                String name = rs.getString("column_name");
                String type = rs.getString("data_type");
                response.append(String.format("%s %s%n", name, type));
            }
            rs.close();
            System.out.println(response.toString());
        }
        catch (Exception e){
            e.printStackTrace();
            response.append("server error");
        }
        return response.toString();
    }

    private String performGetColumnValue(String... params){
        StringBuilder response = new StringBuilder("");
        String table = params[0];
        String pkey = getPrimaryKey(table);
        StringBuilder query = new StringBuilder("SELECT ");
        String delim = "", maindelim = "";
        for(int i = 1; i < params.length; i++){
            query.append(delim);
            delim = ",";
            query.append(params[i]);
        }
        query.append(String.format(" FROM %s ORDER BY %s DESC", table, pkey));
        try{
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(query.toString());
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            while ( rs.next() ) {
                response.append(maindelim);
                maindelim = "\n";
                delim = "";
                for (int i = 1; i <= columnCount; i++) {
                    response.append(delim);
                    delim = ",";
                    response.append(rs.getObject(i));
                }
            }
            rs.close();
        }
        catch (Exception e){
            e.printStackTrace();
            response.append("server error");
        }
        return response.toString();
    }

    private String getValue(String... params) {
        Object response;
        String table = params[0];
        String row = params[1];
        String column = params[2];
        String primaryKey = getPrimaryKey(table);
        try {
            String query = String.format("SELECT %s FROM %s WHERE %s='%s';", column, table, primaryKey, row);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            rs.next();
            response = rs.getObject(column);
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
            response = "server error";
        }
        return response.toString();
    }

    private String getScore(String... params){
        StringBuilder response = new StringBuilder("");
        String delim = "";
        String table = params[0];
        String score = params[1];
        String primaryKey = getPrimaryKey(table);
        try{
            String query = String.format("SELECT %s,%s FROM %s;", primaryKey, score, table);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while ( rs.next() ) {
                response.append(delim);
                delim = "\n";
                response.append(String.format("%s,%s", rs.getString(primaryKey), rs.getFloat(score)));
            }
            rs.close();
        }
        catch (Exception e){
            e.printStackTrace();
            response.append("server error");
        }
        return response.toString();
    }

    private String updateScore(String... params){
        String response;
        String table = params[0];
        String score = params[1];
        String primaryKey = getPrimaryKey(table);
        String primaryValue = params[2];
        String newScore = params[3];
        try{
            String query = String.format("UPDATE %s SET %s=%s WHERE %s='%s';", table, score, newScore, primaryKey, primaryValue);
            stmt = c.createStatement();
            stmt.executeUpdate(query);
            response = String.format("%s updated", score);
        }
        catch (Exception e){
            e.printStackTrace();
            response = "server error";
        }
        return response;
    }

    private String performUpdate(String... params){
        String response;
        String table = params[0];
        String primaryKey = getPrimaryKey(table);
        String primaryValue = params[1];
        String updateKey = params[2];
        String updateValue = params[3];
        try{
            String query = String.format("UPDATE %s SET %s='%s' WHERE %s='%s';", table, updateKey, updateValue, primaryKey, primaryValue);
            stmt = c.createStatement();
            stmt.executeUpdate(query);
            response = String.format("%s updated", updateKey);
        }
        catch (Exception e){
            e.printStackTrace();
            response = "server error";
        }
        return response;
    }

    private String performCustom(String... params) {
        String response;
        String delim = ",";
        String table = params[0];
        StringBuilder query = new StringBuilder(String.format("CREATE TABLE %s (id SERIAL PRIMARY KEY", table));

        for(int i = 1; i < params.length; i++){
            query.append(delim);
            query.append(String.format("%s %s", params[i++], params[i]));
        }
        query.append(")");
        System.out.println(query.toString());
        try {
            stmt = c.createStatement();
            stmt.executeUpdate(query.toString());
            response = String.format("created table: %s", table);
        }
        catch (Exception e){
            e.printStackTrace();
            response = String.format("table %s already exists", table);
        }
        return response;
    }

    private String performCreate() {
        String response;
        String query;
        try {
            query = String.format("CREATE TABLE IF NOT EXISTS %s (name TEXT PRIMARY KEY, columns INTEGER)", TablesList.MAIN_TABLE);
            System.out.println(query);
            stmt = c.createStatement();
            stmt.executeUpdate(query);

            query = String.format("CREATE TABLE IF NOT EXISTS %s (username TEXT PRIMARY KEY, password TEXT, approved BOOLEAN)", UsersList.USERS_TABLE);
            System.out.println(query);
            stmt.executeUpdate(query);

            if(!checkInTable(TablesList.MAIN_TABLE, UsersList.USERS_TABLE)) {
                performAddToTable(TablesList.MAIN_TABLE, "name", UsersList.USERS_TABLE, "columns", "3");
            }

            response = String.format("created tables: %s, %s", UsersList.USERS_TABLE, TablesList.MAIN_TABLE);
        }
        catch (Exception e) {
            e.printStackTrace();
            response = "server error";
        }
        return response;
    }

    private String performDelete(String... params) {
        String response;
        String username = params[0];
        String table = params[1];
        String key = getPrimaryKey(table);
        try
        {
            String query = String.format("DELETE FROM %s WHERE %s='%s';", table, key, username);
            stmt = c.createStatement();
            stmt.executeUpdate(query);
            response = "user deleted";
        }
        catch (Exception e)
        {
            e.printStackTrace();
            response = "server error";
        }
        return response;
    }

    private String performApply(String... params) {
        String response;
        String username = params[0];
        String password = params[1];
        try {
            if (checkApplied(params)) {
                response = "already applied";
            } else if (checkRegister(params)) {
                response = "user exists";
            } else {
                String query = String.format("INSERT INTO %s (username, password, approved) VALUES ('%s', '%s', FALSE);", UsersList.USERS_TABLE, username, password);
                stmt = c.createStatement();
                stmt.executeUpdate(query);
//                String query = "INSERT INTO ? (username, password, approved) VALUES (?, ?, FALSE);";
//                PreparedStatement stmt = c.prepareStatement(query);
//                stmt.setString(1, UsersList.USERS_TABLE);
//                stmt.setString(2, username);
//                stmt.setString(3, password);
//                stmt.executeUpdate();
//                stmt.close();
                response = "applied successfully";
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            response = "server error";
        }
        return response;
    }

    private boolean checkInTable(String table, String... params) {
        boolean response = false;
        String key = getPrimaryKey(table);
        String primaryValue = params.length < 3 ? params[0] : params[2];
        String query = String.format("SELECT COUNT(*) AS rowcount FROM %s WHERE %s='%s';", table, key, primaryValue);
        System.out.println(query);
        try {
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            rs.next();
            int count = rs.getInt("rowcount");
            response = count == 1;
            rs.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    private String performAddToTable(String... params) {
        String response;
        String table = params[0];
        String delim = "";
        StringBuilder keys = new StringBuilder("");
        StringBuilder values = new StringBuilder("");
        for(int i = 1; i < params.length ; i++){
            keys.append(delim);
            values.append(delim);
            delim = ",";
            keys.append(params[i++]);
            values.append(String.format("'%s'", params[i]));
        }

        try{
            if(table.equals(UsersList.USERS_TABLE) && checkInTable(table, params)){
                response = "already exists";
            }
            else {
                String query = String.format("INSERT INTO %s (%s) VALUES (%s)", table, keys.toString(), values.toString());
                System.out.println(query);
                stmt = c.createStatement();
                stmt.executeUpdate(query);
                response = "added";
            }
        }
        catch (Exception e){
            response = "server error";
            e.printStackTrace();
        }
        return response;
    }

    private boolean checkRegister(String... params) {
        if(!checkInTable(UsersList.USERS_TABLE, params)){
            return false;
        }
        String query = String.format("SELECT approved FROM %s WHERE username='%s';", UsersList.USERS_TABLE, params[0]);
        try {
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            rs.next();
            boolean approved = rs.getBoolean("approved");
            rs.close();
            return approved;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    private boolean checkApplied(String... params) {
        if(!checkInTable(UsersList.USERS_TABLE, params)){
            return false;
        }
        String query = String.format("SELECT approved FROM %s WHERE username='%s';", UsersList.USERS_TABLE, params[0]);
        try {
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            rs.next();
            boolean approved = rs.getBoolean("approved");
            rs.close();
            return !approved;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    private String performLogin(String... params) {
        String response;
        if (checkApplied(params)) {
            response = "user awaiting approval";
        }
        else if (checkRegister(params) && getPassword(params[0], UsersList.USERS_TABLE).equals(params[1])) {
            response = "login successful";
        }
        else {
            response = "wrong username or password";
        }
        return response;
    }

    private String performRegister(String... params) {
        String response;
        String username = params[0];
        boolean approved = Boolean.parseBoolean(params[1]);
        String query;
        try {
            if (approved) {
                query = String.format("UPDATE %s SET approved=TRUE WHERE username='%s';", UsersList.USERS_TABLE, username);
                response = "user added";
            }
            else {
                query = String.format("DELETE FROM %s WHERE username='%s';", UsersList.USERS_TABLE, username);
                response = "user rejected";
            }
            stmt = c.createStatement();
            stmt.executeUpdate(query);
        }
        catch (Exception e) {
            e.printStackTrace();
            response = "server error";
        }
        return response;
    }

    private String getPassword(String username, String table) {
        String password = "";
        String query = String.format("SELECT password FROM %s WHERE username='%s';", table, username);
        try {
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            rs.next();
            password = rs.getString("password").trim();
            rs.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return password;
    }

    private String performKeysFromTable(String... params) {
        StringBuilder response = new StringBuilder("");
        String delim = "";
        String table = params[0];
        String pkey = getPrimaryKey(table);
        String query = String.format("SELECT %s%s FROM %s ORDER BY %s DESC", pkey, table.equals(UsersList.USERS_TABLE) ? ",approved" : "", table, pkey);
        try {
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while ( rs.next() ) {
                response.append(delim);
                delim = ",";
                if(table.equals(UsersList.USERS_TABLE)) {
                    response.append(String.format("%s %s", rs.getString(pkey.trim()), rs.getString("approved")));
                }
                else{
                    response.append(rs.getString(pkey));
                }
            }
            rs.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return response.toString();
    }

    private String performAllFromTable(String... params) {
        StringBuilder response = new StringBuilder("");
        String maindelim = "";
        String table = params[0];
        String pkey = getPrimaryKey(table);
        String query = String.format("SELECT * FROM %s ORDER BY %s DESC", table, pkey);
        try {
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            ResultSetMetaData rsmt = rs.getMetaData();
            int columnCount = rsmt.getColumnCount();

            while ( rs.next() ) {
                response.append(maindelim);
                maindelim = "\n";
                String delim = "";
                for (int i = 1; i <= columnCount; i++) {
                    response.append(delim);
                    delim = ",";
                    response.append(rs.getObject(i));
                }
            }
            rs.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return response.toString();
    }

    private String getPrimaryKey(String table){
        String columns = performGetColumns(table);
        return columns.split("\n")[0].split(" ")[0];
    }
}
