package com.thelogicmaster.friend_location_sharing;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class FriendListFragment extends Fragment {

    private RequestQueue queue;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private FriendRecyclerViewAdapter adapter;
    private Timer timer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_list, container, false);

        queue = Volley.newRequestQueue(requireContext());

        swipeRefresh = view.findViewById(R.id.refresh);
        swipeRefresh.setOnRefreshListener(this::refresh);

        recyclerView = view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new GridLayoutManager(view.getContext(), 1));
        adapter = new FriendRecyclerViewAdapter(friend -> {
            Bundle params = new Bundle();
            params.putString("name", friend.name);
            Navigation.findNavController(view).navigate(R.id.action_friendsListFragment_to_friendFragment, params);
        });
        recyclerView.setAdapter(adapter);

        return view;
    }

    private void refresh() {
        queue.start();
        queue.add(new AuthJsonRequest(Request.Method.GET, Helpers.BASE_URL + "friends", null,
                friendsObj -> {
                    try {
                        ArrayList<Friend> friends = new ArrayList<>();
                        for (Iterator<String> it = friendsObj.keys(); it.hasNext(); ) {
                            String name = it.next();
                            JSONObject friendObj = friendsObj.getJSONObject(name);
                            friends.add(new Friend(name, Sharing.valueOf(friendObj.getString("sharing"))));
                        }
                        adapter.setFriends(friends);
                        recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                    } catch (JSONException e) {
                        Log.e("FriendsParsing", "Failed to parse friends", e);
                    }
                    swipeRefresh.setRefreshing(false);
                },
                error -> {
                    Log.e("FriendsRequest", "Failed to get friends", error);
                    swipeRefresh.setRefreshing(false);
                }, Helpers.getAuth(requireActivity())));
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
        }, 0, 5000);
    }

    @Override
    public void onPause() {
        super.onPause();
        queue.stop();
        if (timer != null)
            timer.cancel();
    }
}