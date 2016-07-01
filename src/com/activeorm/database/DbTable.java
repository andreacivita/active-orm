package com.activeorm.database;



import com.activeorm.DbInfo;
import com.activeorm.connection.Connector;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * This class is developed for give support in creation and execution of queries, and offers
 * methods for executing DML and QL instructions.
 * It also has method for helping the use of queries.
 */

public class DbTable {


    protected String name;
    protected String primary;
    protected String sql;
    protected boolean joined;

    public DbTable() {
        name = "";
        primary = "";
        sql = "";
        joined = false;
    }

   /* DML OPERATIONS */

    /**
     * Insert data into database
     * @param attributes String of value that will be inserted into table
     */
    public void insert(String attributes) {
        //replace value of primary key  in null (in auto_increment key)
        int i = 0;
        if (attributes.substring(0, 2).contains("0,")) {
            boolean comma_isnot_found = true;
            while (comma_isnot_found) {
                i++;
                String key = attributes.substring(0, i);
                if (key.contains(","))
                    comma_isnot_found = false;

            }
            i--;
            //start replacing
            String key = attributes.substring(0, i);
            String data = attributes.substring(i);
            key = "null";
            attributes = key + data;
        }
        sql = "insert into " + name + " values (" + attributes + ")";
    }

    /**
     * Update data into table. If you not use where method, this will update <b>all records</b>
     * @param attrib Hashmap of column,value that will be updated.
     */
    public void update(HashMap attrib) {
        String attributes = process(attrib);
        sql = "update " + name + " set " + attributes;
    }


    /**
     * Delete data from table. If you not use where method, this will delete <b>all records</b>
     */
    public void delete() {
        sql = "delete from " + name;
    }


    /* QL OPERATIONS & WHERE */

    /**
     * Select all columns from current table
     */
    public void select() {
        //seleziono tutte le tuple dalla tabella
        sql = "SELECT * FROM " + name;
    }

    /**
     * Select specified columns in current table
     * @param columns String. columns that will selected
     */
    public void select(String columns) {
        sql = "SELECT " + columns + " FROM " + name;
    }

    /**
     * Perform join operation.
     * @param table is table that must be joined.
     * @param clause is join clause. E.g: t.primaryKey = d.ForeignKey
     */
    public void join(String table, String clause) {
        if (!joined) {
            sql += " JOIN " + table + " ON " + clause;
            joined = true;
        } else
            sql += "AND " + name + " JOIN " + table + " ON " + clause;
    }

    /**
     * Add where clause to your query.
     * @param clause String is where clause.
     */
    public void where(String clause) {
        sql = sql + " WHERE " + clause;
    }

    /**
     * Add order by clause
     * @param clause String. clause of order. E.g: name DESC
     */
    public void order(String clause) {

        sql = sql + " ORDER BY " + clause;
    }

    /**
     * Add group by clause
     * @param columns String. Column that will grouped E.g: col1, col2
     */
    public void group(String columns) {
        sql = sql + " GROUP BY " + columns;
    }

    /**
     * Add having clause
     * @param clause String. Having clause
     */
    public void having(String clause) {
        sql = sql + " HAVING " + clause;
    }

    /**
     * Add limit clause. It works with Mysql, Sqlite and PostgreSQL
     * @param rows number of rows. Starting point is from 0.
     */
    public void limit(int rows) {
        limit(0,rows);
    }

    /**
     * Add limit clause. It works with Mysql, Sqlite and PostgreSQL
     * @param start starting number.
     * @param number number of rows.
     */
    public void limit(int start, int number) {
        //check if DBMS is supported
        if (DbInfo.JDBC.equals("com.mysql.jdbc.Driver"))
            sql = sql + " LIMIT " + start + "," + number;
        else if (DbInfo.JDBC.equals("org.sqlite.JDBC") || DbInfo.JDBC.equals("org.postgresql.Driver"))
            sql = sql + "LIMIT " + start + " OFFSET " + number;
            //if not supported
        else
            System.out.println("Limit is not supported from your DBMS. Query will be executed without limit");
    }

    /* MISC OPERATION */

    /**
     * find data on table from key
     * @param key primary key provided
     * @return ResultSet
     */
    public ResultSet find(String key) {
        select();
        where(primary = primary + "=" + key);
        return fetch();
    }

    /**
     * Search data on table
     * @param attribute column used for searching
     * @param search search clause
     * @return ResultSet
     */
    public ResultSet search(String attribute, String search) {
        //works with MySQL and SQLite
        select();
        where(attribute + " LIKE %" + search + "%");
        return fetch();
    }

    /**
     * Check if table is empty.
     * @return boolean
     */
    public boolean isEmpty() {
        select();
        int counts = count(fetch());
        if (counts == 0)
            return true;
        return false;
    }

    /**
     * Check if element exist
     * @param key key of element
     * @return boolean
     */
    public boolean exist(String key) {
        int counts = count(find(key));
        if (counts == 1)
            return true;
        return false;

    }

    /**
     * Count how many rows in ResultSet are stored.
     * @param result resultset for counting
     * @return int
     */
    public int count(ResultSet result) {
        int i = 0;
        try {
            while (result.next()) {
                i++;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return i;
    }

    /**
     * print current sql instruction. Useful for debug
     */
    public void printSql() {
        System.out.println(sql);
    }


    protected String process(HashMap map) {
        String data = "";
        Set keys = map.keySet();
        int counter = 1;
        Iterator i = keys.iterator();
        while (i.hasNext()) {
            String key = (String) i.next();
            String value = (String) map.get(key);
            data = data + key + "='" + value + "'";
            //if is not last item add ,
            if (counter < map.size())
                data = data + ", ";
            //increase counter
            counter++;
        }
        return data;
    }

    public String getName(){
        return name;
    }

    public String getPrimary() {
        return primary;
    }

    /* EXECUTING OPERATIONS */

    /**
     * Fetch data from database
     * @return ResultSet
     */
    public ResultSet fetch() {
        //created resultset and start connection
        ResultSet result = null;
        Connector connector = new Connector();
        Connection connection = connector.connect();

        //if there's no query, all rows
        if (sql.equals("")) {
            select();
        }
        //added ; to sql
        sql = sql + ";";
        //try to execute query
        try {
            Statement query = connection.createStatement();
            result = query.executeQuery(sql);
        } catch (Exception e) {
            System.out.println(e);
        }
        return result;
    }

    /**
     * fetch data from database
     * @param instruction sql instruction
     * @return ResultSet
     */
    public ResultSet fetch(String instruction) {
        //created resultset and start connection
        ResultSet result = null;
        Connector connector = new Connector();
        Connection connection = connector.connect();
        //try to execute query
        try {
            Statement query = connection.createStatement();
            result = query.executeQuery(instruction);

        } catch (Exception e) {
            System.out.println(e);
        }
        return result;
    }

    /**
     * execute DML query
     * @return boolean
     */
    public boolean execute() {


        Connector connector = new Connector();
        Connection connection = connector.connect();
        boolean check = false;
        if (sql.equals("")) {
            JOptionPane.showMessageDialog(null, "There is no query to execute!", "ERROR", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        sql = sql + ";";
        try {
            PreparedStatement query = connection.prepareStatement(sql);
            query.executeUpdate();
            check = true;
        } catch (Exception e) {
            System.out.println(e);
            check = false;
        }
        connector.disconnect();
        return check;
    }

    /**
     * execute DML query
     * @param instruction query executed
     * @return boolean
     */
    public boolean execute(String instruction) {


        Connector connector = new Connector();
        Connection connection = connector.connect();
        boolean check = false;
        try {
            PreparedStatement query = connection.prepareStatement(instruction);
            query.executeUpdate();
            check = true;
        } catch (Exception e) {
            System.out.println(e);
        }
        connector.disconnect();
        return check;
    }




}
