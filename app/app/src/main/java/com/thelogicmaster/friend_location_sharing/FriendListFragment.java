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
import android.widget.EditText;
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

        final EditText usernameText = view.findViewById(R.id.username);
        view.findViewById(R.id.add_friend).setOnClickListener(v -> {
            if (!"".equals(usernameText.getText().toString()))
                queue.add(new AuthStringRequest(Request.Method.POST, Helpers.BASE_URL + "addFriend?username=" + usernameText.getText(),
                        response -> {
                            usernameText.setText("");
                            refresh();
                        },
                        error -> {
                            Log.e("FriendsRequest", "Failed to add friend", error);
                            Toast.makeText(requireContext(), "Failed to add friend", Toast.LENGTH_SHORT).show();
                        }, Helpers.getAuth(requireActivity())));
        });

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
        queue.add(new FriendsListRequest(getActivity(), friends -> {
            adapter.setFriends(friends);
            recyclerView.scrollToPosition(adapter.getItemCount() - 1);
        }, error -> {
            Log.e("FriendsRequest", "Failed to get friends", error);
            swipeRefresh.setRefreshing(false);
        }));
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