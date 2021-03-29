package com.thelogicmaster.friend_location_sharing;

import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class FriendFragment extends Fragment {

    private RequestQueue queue;
    private Timer timer;
    private String name;
    private TextView nameText, locationText;
    private GoogleMap map;
    private Polyline history;
    private Marker friendMarker, userMarker;
    private boolean spinnerInitialized;
    private LocationSharingViewModel viewModel;
    private SwitchCompat historySwitch;
    private User friend;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(LocationSharingViewModel.class);
        viewModel.getFriends().observe(getViewLifecycleOwner(), friends -> {
            if (!friends.containsKey(name))
                return;
            friend = friends.get(name);
            updateUI();
        });
        viewModel.getLocation().observe(getViewLifecycleOwner(), location -> updateUI());

        queue = Volley.newRequestQueue(requireContext());

        name = getArguments().getString("name");

        nameText = view.findViewById(R.id.friend_name);
        locationText = view.findViewById(R.id.location_info);

        view.findViewById(R.id.reset_zoom).setOnClickListener(v -> resetZoom());

        historySwitch = view.findViewById(R.id.show_history);
        historySwitch.setOnCheckedChangeListener((v, checked) -> {
            if (history != null)
                history.setVisible(checked);
        });

        ((Spinner) view.findViewById(R.id.sharing_spinner)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!spinnerInitialized) { // Skip first event
                    spinnerInitialized = true;
                    return;
                }
                JSONObject data = new JSONObject();
                Log.e("sharing", Sharing.values()[position].name());
                try {
                    data.put("sharing", Sharing.values()[position].name());
                    data.put("friend", name);
                } catch (JSONException e) {
                    Toast.makeText(getContext(), "Failed to update friend location sharing", Toast.LENGTH_LONG).show();
                    Log.e("FriendSharing", "Failed to create request data", e);
                    return;
                }
                queue.add(new AuthJsonRequest(Request.Method.POST,
                        Helpers.BASE_URL + "friendSharing", data, response -> {
                },
                        e -> {
                            Toast.makeText(requireContext(), "Failed to update friend location sharing", Toast.LENGTH_LONG).show();
                            Log.e("FriendSharing", "Failed to update friend location sharing", e);
                        }, Helpers.getAuth(requireContext()), true));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        view.findViewById(R.id.remove_friend_button).setOnClickListener(v ->
                queue.add(new AuthStringRequest(Request.Method.POST,
                        Helpers.BASE_URL + "deleteFriend?friend=" + name,
                        response -> Navigation.findNavController(view).navigate(R.id.action_friendFragment_to_friendsListFragment),
                        e -> {
                            Toast.makeText(requireContext(), "Failed to remove friend", Toast.LENGTH_LONG).show();
                            Log.e("FriendSharing", "Failed to delete friend", e);
                        }, Helpers.getAuth(requireContext()))));

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null)
            mapFragment.getMapAsync(googleMap -> this.map = googleMap);

        return view;
    }

    private void refresh() {
        viewModel.updateFriend(name);
    }

    private void updateUI() {
        if (map != null) {
            ArrayList<LatLng> coordinates = new ArrayList<>();
            for (Location location : friend.locations)
                coordinates.add(location.latLng);

            if (history == null) {
                history = map.addPolyline(new PolylineOptions());
                history.setColor(Color.RED);
                friendMarker = map.addMarker(new MarkerOptions()
                        .position(new LatLng(0, 0))
                        .title(friend.name)
                        .visible(false)
                );
                userMarker = map.addMarker(new MarkerOptions()
                        .title(Helpers.getUsername(requireContext()))
                        .position(new LatLng(0, 0))
                        .visible(false)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                );
            }

            if (friend.locations.size() > 0) {
                LatLng latLng = friend.locations.get(friend.locations.size() - 1).latLng;
                boolean init = friendMarker.getPosition().latitude == 0
                        && friendMarker.getPosition().longitude == 0;
                friendMarker.setPosition(latLng);
                if (init)
                    resetZoom();
                friendMarker.setVisible(true);
            }

            Location userLocation = viewModel.getLocation().getValue();
            if (userLocation != null) {
                userMarker.setPosition(viewModel.getLocation().getValue().latLng);
                userMarker.setVisible(true);
            }

            history.setVisible(historySwitch.isChecked());
            history.setPoints(coordinates);
        }

        nameText.setText(friend.name);

        if (friend.sharing == Sharing.OFF)
            locationText.setText("Location Disabled");
        else if (friend.locations.size() > 0) {
            if (Geocoder.isPresent()) {
                Location location = friend.locations.get(friend.locations.size() - 1);
                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                new Thread(() -> {
                    try {
                        List<Address> addresses = geocoder.getFromLocation(
                                location.latitude, location.longitude, 1);
                        if (addresses.size() > 0)
                            locationText.post(() ->
                                    locationText.setText(addresses.get(0).getAddressLine(0)));
                    } catch (IOException e) {
                        Log.e("FriendReverseGeocoding", "Failed to get address", e);
                    }
                }).start();
            }
        }
    }

    private void resetZoom() {
        if (map == null || (friendMarker.getPosition().latitude == 0 && friendMarker.getPosition().longitude == 0))
            return;

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(friendMarker.getPosition(), 10), 2000, null);
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