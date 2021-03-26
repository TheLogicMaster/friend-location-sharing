package com.thelogicmaster.friend_location_sharing;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

public class FriendFragment extends Fragment {

    private RequestQueue queue;
    private Timer timer;
    private String name;
    private TextView nameText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend, container, false);

        queue = Volley.newRequestQueue(requireContext());

        name = getArguments().getString("name");

        nameText = view.findViewById(R.id.friend_name);

        return view;
    }

    private void refresh() {
        queue.start();

        queue.add(new AuthJsonRequest(Request.Method.GET, Helpers.BASE_URL + "friend?name=" + name, null,
                friendObj -> {
                    try {
                        ArrayList<Location> locations = new ArrayList<>();
                        JSONArray locationsArray = friendObj.getJSONArray("locations");
                        for (int i = 0; i < locationsArray.length(); i++) {
                            JSONObject locationObj = locationsArray.getJSONObject(i);
                            locations.add(new Location(locationObj.getDouble("long"),
                                    locationObj.getDouble("lat"), locationObj.getLong("time")));
                        }
                        Friend friend = new Friend(friendObj.getString("name"),
                                Sharing.valueOf(friendObj.getString("sharing")), locations);
                        nameText.setText(friend.name);
                    } catch (JSONException e) {
                        Log.e("FriendParsing", "Failed to parse friend", e);
                    }
                },
                error -> Log.e("FriendRequest", "Failed to get friend", error),
                Helpers.getAuth(requireActivity())));
    }

    @Override
    public void onResume() {
        super.onResume();
        queue.start();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                refresh();
            }
        }, 0, 4000);
    }

    @Override
    public void onPause() {
        super.onPause();
        queue.stop();
        if (timer != null)
            timer.cancel();
    }
}