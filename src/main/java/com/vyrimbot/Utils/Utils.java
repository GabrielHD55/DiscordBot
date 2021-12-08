package com.vyrimbot.Utils;

public class Utils {

    public static boolean isLong(String s) {
        try {
            Long.parseLong(s);
        } catch (NumberFormatException ex) {
            return false;
        }
        return true;
    }

    public static boolean isInt(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            return false;
        }
        return true;
    }
}
