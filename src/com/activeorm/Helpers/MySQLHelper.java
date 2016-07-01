package com.activeorm.Helpers;

/**
 * Created by casa on 18/06/2016.
 */
public class MySQLHelper {

    /**
     * Escape string for prevent sqlinjection and error in data with " ' " char
     * @param str string escaped
     * @return String
     */
    public static String eScapeString(String str) {
        String data = null;
        if (str != null && str.length() > 0) {
            str = str.replace("'", "\\'");
            data = str;
        }
        return data;
    }

    /**
     * refactor all data that contains " ' ". Very useful in italian localization.
     * @param str string refactored
     * @return String
     */
    public static String refactorString(String str){
        String data = null;
        if (str != null && str.length() > 0) {
            str = str.replace("\\'", "'");
            data = str;
        }
        return data;
    }


}
