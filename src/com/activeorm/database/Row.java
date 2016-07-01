package com.activeorm.database;

import com.activeorm.ActiveORM;
import com.activeorm.Helpers.MySQLHelper;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.HashMap;

/**
 * Created by casa on 18/06/2016.
 */
public class Row {

    protected DbTable dbtable;
    protected boolean existing;
    protected String primary;

    /**
     * Populate Row attributes with data from ResultSet.
     * All column of ResultSet must correspond to a proprerty of Row Object.
     * With this, you can use result as an object (without use try catch and abstrating more from relational logic)
     * @param result ResultSet from fetch()
     */
    public void populate(ResultSet result){
        existing=true;
        Class<?> classe = this.getClass();
        String column = "";
        String type="";
        try{
            result.next();
            for(Field field : classe.getDeclaredFields()){
                column=field.getName();
                field.setAccessible(true);
                type=field.getType().toString();
                if(type.equals("int"))
                    field.set(this, result.getInt(column));

                else if (type.equals("boolean"))
                    field.set(this,result.getBoolean(column));
                else
                    field.set(this,result.getString(column));
                if(field.get(this).toString().contains("\\'"))
                    field.set(this, MySQLHelper.refactorString(field.get(this).toString()));
            }
        }
        catch(Exception e){
            System.out.println(e);
        }


    }

    /**
     * Save current row into db. If row exist, it will update row. If row doesn't exist, it will insert new row.
     * @return boolean.
     */
    public boolean save(){
        boolean execution = false;
        if(existing){
            Class<?> classe = this.getClass();
            String valuePrimary = getValue(classe,primary,this);
            dbtable.update(this.valueToMap());
            dbtable.where(dbtable.primary + " = " + valuePrimary);
            if(ActiveORM.DEVELOPMENT_MODE)
                dbtable.printSql();
            execution = dbtable.execute();

        }
        else{
            dbtable.insert(this.valueToString());
            if(ActiveORM.DEVELOPMENT_MODE)
                dbtable.printSql();
            execution = dbtable.execute();

        }
        return execution;
    }

    /**
     * Delete row from database.
     */
    public void destroy(){
        boolean execution = false;
        if(existing){
            Class<?> classe = this.getClass();
            String valuePrimary = getValue(classe,primary,this);
            dbtable.delete();
            dbtable.where(dbtable.primary + " = " + valuePrimary);
            if(ActiveORM.DEVELOPMENT_MODE)
                dbtable.printSql();
            execution = dbtable.execute();
        }
    }

    /**
     * Create a String from Object attributes value.
     * @return String
     */

    protected String valueToString(){

        Class<?> classe = this.getClass();
        String temp_name = "";
        String temp_value="";
        String total="";
        int i=0;
        for(Field field : classe.getDeclaredFields()) {
            temp_name=field.getName();
            temp_value=this.getValue(classe,temp_name,this);
            if(temp_value.contains("'"))
                temp_value=MySQLHelper.eScapeString(temp_value);
            if (i==0){
                total="'"+temp_value+"'";
            }
            else{
                total = total + ", '" + temp_value+"'";
            }
            i++;
        }
        return total;

    }

    /**
     * Create a map from Object. Map key is Object attribute, Map value is attribute value
     * @return HashMap
     */
    protected HashMap valueToMap(){
        Class<?> classe = this.getClass();
        String key = "";
        String value="";
        HashMap total = new HashMap();
        for(Field field : classe.getDeclaredFields()) {
            key=field.getName();
            value=this.getValue(classe,key,this);
            if(value.contains("'"))
                value=MySQLHelper.eScapeString(value);
            total.put(key, value);
        }
        return total;
    }

    protected String getValue(Class<?> classe,String key, Object o){
        String value="";
        try{
            Field f = classe.getDeclaredField(key);
            f.setAccessible(true);
            value = f.get(o).toString();
        }
        catch(Exception f){
            value="VOID";
        }
        return value;
    }




}
