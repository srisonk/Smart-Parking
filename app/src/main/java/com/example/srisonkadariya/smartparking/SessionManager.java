package com.example.srisonkadariya.smartparking;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;


class SessionManager {
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    @SuppressLint("CommitPrefEdits")
    SessionManager(Context context){
        prefs= context.getSharedPreferences("SmartParking",Context.MODE_PRIVATE );
        editor=prefs.edit();
    }

    void setLoggedin(boolean loggedin){
        editor.putBoolean("loggedInmode",loggedin);
        editor.commit();
    }

    boolean loggedin(){
        return prefs.getBoolean("loggedInmode", false);
    }
}