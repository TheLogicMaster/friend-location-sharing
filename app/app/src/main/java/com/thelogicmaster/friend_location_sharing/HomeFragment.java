package com.thelogicmaster.friend_location_sharing;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

public class HomeFragment extends Fragment {

    private TextView helloText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        helloText = view.findViewById(R.id.hello);

        view.findViewById(R.id.settingsButton).setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_homeFragment_to_settingsFragment, null));

        view.findViewById(R.id.friendsButton).setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_homeFragment_to_friendFragment, null));

        view.findViewById(R.id.groupsButton).setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_homeFragment_to_groupListFragment, null));

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
        if (username == null)
            NavHostFragment.findNavController(this).navigate(R.id.action_homeFragment_to_loginFragment);
        else
            helloText.setText("Welcome, " + username);
    }
}