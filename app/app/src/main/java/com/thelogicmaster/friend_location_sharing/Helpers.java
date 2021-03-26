package com.thelogicmaster.friend_location_sharing;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

public class Helpers {

    public static final String BASE_URL = "https://example.thelogicmaster.com/";

    public static String getAuth(Activity activity) {
        SharedPreferences prefs = activity.getPreferences(Context.MODE_PRIVATE);
        return getAuth(prefs.getString("username", ""), prefs.getString("password", ""));
    }

    public static String getAuth(String username, String password) {
        return "basic " + Base64.encodeToString((username + ":" + password).getBytes(), Base64.NO_WRAP);
    }

    public static String getUsername(Activity activity) {
        return activity.getPreferences(Context.MODE_PRIVATE).getString("username", null);
    }

    public static void setCredentials(Activity activity, String username, String password) {
        SharedPreferences.Editor editor = activity.getPreferences(Context.MODE_PRIVATE).edit();
        editor.putString("username", username);
        editor.putString("password", password);
        editor.apply();
    }
}
