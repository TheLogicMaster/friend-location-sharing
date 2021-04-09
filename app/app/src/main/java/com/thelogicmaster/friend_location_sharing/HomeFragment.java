package com.thelogicmaster.friend_location_sharing;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class HomeFragment extends Fragment {

    private TextView helloText;
    private RequestQueue queue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        queue = Volley.newRequestQueue(requireContext());

        helloText = view.findViewById(R.id.hello);

        view.findViewById(R.id.settingsButton).setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_homeFragment_to_settingsFragment, null));

        view.findViewById(R.id.friendsButton).setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_homeFragment_to_friendFragment, null));

        view.findViewById(R.id.groupsButton).setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_homeFragment_to_groupListFragment, null));

        view.findViewById(R.id.chatsButton).setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_homeFragment_to_chatListFragment, null));

        view.findViewById(R.id.logoutButton).setOnClickListener(v -> {
            Helpers.setCredentials(requireActivity(), null, null);
            Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_loginFragment);
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        String username = Helpers.getUsername(requireActivity());
        if (username == null) {
            NavHostFragment.findNavController(this).navigate(R.id.action_homeFragment_to_loginFragment);
            return;
        } else
            helloText.setText("Welcome, " + username);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        queue.add(new AuthStringRequest(Request.Method.GET, Helpers.BASE_URL + "sharing",
                sharing -> prefs.edit().putString("sharing", sharing).apply(),
                e -> Log.e("UpdateSharing", "Failed to retrieve sharing settings", e),
                Helpers.getAuth(requireContext())));

        new ViewModelProvider(this).get(LocationSharingViewModel.class)
                .updateFriends(null);
    }
}