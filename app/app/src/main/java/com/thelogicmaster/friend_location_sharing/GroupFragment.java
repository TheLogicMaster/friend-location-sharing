package com.thelogicmaster.friend_location_sharing;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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

public class GroupFragment extends Fragment {

    private RequestQueue queue;
    private Timer timer;
    private String id;
    private TextView nameText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group, container, false);

        queue = Volley.newRequestQueue(requireContext());

        id = getArguments().getString("group");

        nameText = view.findViewById(R.id.group_name);

        return view;
    }

    private void refresh() {
        queue.start();

        queue.add(new AuthJsonRequest(Request.Method.GET, Helpers.BASE_URL + "group?id=" + id, null,
                groupObj -> {
                    try {
                        JSONObject usersObj = groupObj.getJSONObject("users");
                        ArrayList<Friend> users = new ArrayList<>();
                        for (Iterator<String> it = usersObj.keys(); it.hasNext(); ) {
                            String name = it.next();
                            JSONObject userObj = usersObj.getJSONObject(name);
                            ArrayList<Location> locations = new ArrayList<>();
                            JSONArray locationsArray = userObj.getJSONArray("locations");
                            for (int i = 0; i < locationsArray.length(); i++) {
                                JSONObject locationObj = locationsArray.getJSONObject(i);
                                locations.add(new Location(locationObj.getDouble("long"),
                                        locationObj.getDouble("lat"), locationObj.getLong("time")));
                            }
                            users.add(new Friend(name, Sharing.valueOf(userObj.getString("sharing")), locations));
                        }
                        // Is group object even needed here?
                        Group group = new Group(groupObj.getString("id"), groupObj.getString("name"), users);
                        nameText.setText(group.name);
                    } catch (JSONException e) {
                        Log.e("GroupParsing", "Failed to parse group", e);
                    }
                },
                error -> Log.e("GroupRequest", "Failed to get group", error),
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