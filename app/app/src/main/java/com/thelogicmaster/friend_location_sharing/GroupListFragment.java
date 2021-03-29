package com.thelogicmaster.friend_location_sharing;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

public class GroupListFragment extends Fragment {

    private RequestQueue queue;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private GroupRecyclerViewAdapter adapter;
    private Timer timer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_list, container, false);

        LocationSharingViewModel viewModel = new ViewModelProvider(requireActivity()).get(LocationSharingViewModel.class);

        queue = Volley.newRequestQueue(requireContext());

        swipeRefresh = view.findViewById(R.id.refresh);
        swipeRefresh.setOnRefreshListener(this::refresh);

        view.findViewById(R.id.add_group).setOnClickListener(
                v -> (new AddGroupDialog(new ArrayList<>(viewModel.getFriends().getValue().values()))).show(getChildFragmentManager(), "AddGroup"));

        recyclerView = view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new GridLayoutManager(view.getContext(), 1));
        adapter = new GroupRecyclerViewAdapter(group -> {
            Bundle params = new Bundle();
            params.putString("group", group.id);
            Navigation.findNavController(view).navigate(R.id.action_groupListFragment_to_groupFragment, params);
        });
        recyclerView.setAdapter(adapter);

        return view;
    }

    private void refresh() {
        queue.start();
        queue.add(new AuthJsonRequest(Request.Method.GET, Helpers.BASE_URL + "groups", null,
                response -> {
                    try {
                        JSONArray groupArray = response.getJSONArray("groups");
                        ArrayList<Group> groups = new ArrayList<>();
                        for (int i = 0; i < groupArray.length(); i++) {
                            JSONObject groupObj = groupArray.getJSONObject(i);
                            JSONObject usersObj = groupObj.getJSONObject("users");
                            ArrayList<User> users = new ArrayList<>();
                            for (Iterator<String> it = usersObj.keys(); it.hasNext(); ) {
                                String name = it.next();
                                JSONObject userObj = usersObj.getJSONObject(name);
                                users.add(new User(name, Sharing.valueOf(userObj.getString("sharing"))));
                            }
                            groups.add(new Group(groupObj.getString("id"), groupObj.getString("name"), users));
                        }
                        adapter.setGroups(groups);
                        recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                    } catch (JSONException e) {
                        Log.e("GroupsParsing", "Failed to parse groups", e);
                    }
                    swipeRefresh.setRefreshing(false);
                },
                error -> {
                    Log.e("GroupsRequest", "Failed to get groups", error);
                    swipeRefresh.setRefreshing(false);
                }, Helpers.getAuth(requireContext())));
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