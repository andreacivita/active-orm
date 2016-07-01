package com.activeorm.connection;

import com.activeorm.ActiveORM;
import com.activeorm.DbInfo;

import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;

public class Connector {
    /**
     * This class provide you connection and disconnection method
     */
    private Connection conn;
    private DbInfo info;

    /**
     * connect to database.
     * @return Connection
     */

    public Connection connect()
    {
        String classname ="";
        if(!DbInfo.DBMS.equals("") && DbInfo.JDBC.equals("")) {
            classname = getClassName(DbInfo.DBMS);
            DbInfo.JDBC=classname;
        }
        if(!DbInfo.JDBC.equals(""))
            classname = DbInfo.JDBC;
        if(classname.equals("")) {
            JOptionPane.showMessageDialog(null, "You must provide a DBMS or a JDBC classname!", "ERROR", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        try{
            Class.forName(classname);
            this.conn=DriverManager.getConnection(DbInfo.URL,DbInfo.USER,DbInfo.PASSWORD);
            if(ActiveORM.DEVELOPMENT_MODE)
                JOptionPane.showMessageDialog(null,"Connection succesful!","SUCCESS",JOptionPane.INFORMATION_MESSAGE);
            return conn;
        }
        catch(ClassNotFoundException f){
            if(classname.equals("org.sqlite.JDBC"))
                JOptionPane.showMessageDialog(null,"Connection failed. Add SQLite library!","ERROR",JOptionPane.ERROR_MESSAGE);
            else if(classname.equals("com.mysql.jdbc.Driver"))
                JOptionPane.showMessageDialog(null,"Connection failed. Add MySQL library!","ERROR",JOptionPane.ERROR_MESSAGE);
            else if(classname.equals("org.postgresql.Driver"))
                JOptionPane.showMessageDialog(null,"Connection failed. Add PostgreSQL library!","ERROR",JOptionPane.ERROR_MESSAGE);
            System.out.println(f);
        }
        catch(Exception e){
            JOptionPane.showMessageDialog(null,"Connection failed. Check that your DBMS is active!","ERROR",JOptionPane.ERROR_MESSAGE);
            System.out.println(e);
        }

        return conn;
    }

    
    public boolean isConnected(){
        if (conn==null){
            return false;
        }
        return true;
    }
	/**
     * disconnect from database
     */
    public void disconnect(){
        try {
            conn.close();
            if(ActiveORM.DEVELOPMENT_MODE)
                JOptionPane.showMessageDialog(null,"Disconnessione riuscita!","SUCCESS",JOptionPane.INFORMATION_MESSAGE);
        }
        catch(Exception e) {
            System.out.println(e);
        }
        return;
    }
    public void testConnection(){
        this.connect();
        this.disconnect();
    }
    private String getClassName(String dbms){
        String mysql="mysql".toLowerCase();
        String sqlite="sqlite".toLowerCase();
        String postgre = "postgresql".toLowerCase();
        String className="";
        if(dbms.equals(mysql))
            className="com.mysql.jdbc.Driver";
        if(dbms.equals(sqlite))
            className="org.sqlite.JDBC";
        if(dbms.equals(postgre))
            className="org.postgresql.Driver";
        return className;
    }


}
