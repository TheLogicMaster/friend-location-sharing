package com.thelogicmaster.friend_location_sharing;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import androidx.preference.PreferenceManager;

public class Helpers {

    public static final String BASE_URL = "https://example.thelogicmaster.com/";

    public static String getAuth(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return getAuth(prefs.getString("username", ""), prefs.getString("password", ""));
    }

    public static String getAuth(String username, String password) {
        return "basic " + Base64.encodeToString((username + ":" + password).getBytes(), Base64.NO_WRAP);
    }

    public static String getUsername(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString("username", null);
    }

    public static void setCredentials(Context context, String username, String password) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString("username", username);
        editor.putString("password", password);
        editor.apply();
    }
}
