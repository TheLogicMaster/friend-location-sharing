package com.thelogicmaster.friend_location_sharing;

import android.app.Activity;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FriendsListRequest extends AuthJsonRequest {

    public FriendsListRequest(Activity context, FriendsListListener listener, Response.ErrorListener errorListener) {
        super(Method.GET, Helpers.BASE_URL + "friends", null, friendsObj -> {
            try {
                ArrayList<Friend> friends = new ArrayList<>();
                for (Iterator<String> it = friendsObj.keys(); it.hasNext(); ) {
                    String name = it.next();
                    JSONObject friendObj = friendsObj.getJSONObject(name);
                    friends.add(new Friend(name, Sharing.valueOf(friendObj.getString("sharing"))));
                }
                listener.onFriendsList(friends);
            } catch (JSONException e) {
                errorListener.onErrorResponse(new VolleyError("Failed to parse friends list", e));
            }
        }, errorListener, Helpers.getAuth(context));
    }

    public interface FriendsListListener {

        void onFriendsList(List<Friend> friends);
    }
}
