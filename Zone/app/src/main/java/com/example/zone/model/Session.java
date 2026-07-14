package com.example.zone.model;

public class Session {
    private static int userID;
    private static String username;

    public static void setUserID(int id) {
        id = userID;
    }
    
    public static void setUsername(String user){
        user = username;
    }

    public static int getUserID() {
        return userID;
    }
    
    public static String getUsername() {
        return username;
    }
}
