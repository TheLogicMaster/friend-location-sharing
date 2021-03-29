package com.thelogicmaster.friend_location_sharing;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

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

public class GroupFragment extends Fragment {

    private RequestQueue queue;
    private Timer timer;
    private String id;
    private TextView nameText;
    private LocationSharingViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(LocationSharingViewModel.class);
        viewModel.getGroups().observe(getViewLifecycleOwner(), groups -> {
            Group group = null;
            for (Group g: groups)
                if (g.id.equals(id)) {
                    group = g;
                    break;
                }
            if (group == null)
                return;
            nameText.setText(group.name);
        });

        queue = Volley.newRequestQueue(requireContext());

        id = getArguments().getString("group");

        nameText = view.findViewById(R.id.group_name);

        return view;
    }

    private void refresh() {
        viewModel.updateGroup(id);
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