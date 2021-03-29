package com.thelogicmaster.friend_location_sharing;

import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
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
import com.google.android.gms.maps.CameraUpdate;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class GroupFragment extends Fragment {

    private RequestQueue queue;
    private Timer timer;
    private String id, username;
    private TextView nameText, locationText;
    private LocationSharingViewModel viewModel;
    private Polyline history;
    private Spinner sharingSpinner;
    private SwitchCompat historySwitch;
    private GoogleMap map;
    private final HashMap<String, Marker> markers = new HashMap<>();
    private Marker userMarker, selected;
    private final HashMap<String, User> users = new HashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group, container, false);

        username = Helpers.getUsername(requireContext());
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

            for (User user: group.users)
                users.put(user.name, user);

            updateMap();

            if (markers.size() > 0)
                for (Marker marker: markers.values()) {
                    User user = users.get(marker.getTitle());
                    if (user.locations.size() == 0)
                        markers.get(user.name).setVisible(false);
                    else {
                        Location current = user.locations.get(user.locations.size() - 1);
                        markers.get(user.name).setPosition(current.latLng);
                        markers.get(user.name).setVisible(user.sharing != Sharing.OFF);
                    }
                }

            User user = null;
            for (User u: group.users)
                if (u.name.equals(username))
                    user = u;
            sharingSpinner.setSelection(user.sharing.ordinal(), false);
            sharingSpinner.setEnabled(true);

            nameText.setText(group.name);
        });
        viewModel.getLocation().observe(getViewLifecycleOwner(), location -> updateMap());

        queue = Volley.newRequestQueue(requireContext());

        id = getArguments().getString("group");

        nameText = view.findViewById(R.id.group_name);
        locationText = view.findViewById(R.id.location_info);

        view.findViewById(R.id.reset_zoom).setOnClickListener(v -> resetZoom());

        historySwitch = view.findViewById(R.id.show_history);
        historySwitch.setOnCheckedChangeListener((v, checked) -> {
            if (history != null)
                history.setVisible(checked);
        });

        sharingSpinner = view.findViewById(R.id.sharing_spinner);
        sharingSpinner.setEnabled(false);
        sharingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!sharingSpinner.isEnabled())
                    return;
                JSONObject data = new JSONObject();
                try {
                    data.put("sharing", Sharing.values()[position].name());
                    data.put("id", GroupFragment.this.id);
                } catch (JSONException e) {
                    Toast.makeText(getContext(), "Failed to update group location sharing", Toast.LENGTH_LONG).show();
                    Log.e("GroupSharing", "Failed to create request data", e);
                    return;
                }
                queue.add(new AuthJsonRequest(Request.Method.POST,
                        Helpers.BASE_URL + "groupSharing", data, response -> {
                },
                        e -> {
                            Toast.makeText(requireContext(), "Failed to update group location sharing", Toast.LENGTH_LONG).show();
                            Log.e("GroupSharing", "Failed to update group location sharing", e);
                        }, Helpers.getAuth(requireContext()), true));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        view.findViewById(R.id.delete_group_button).setOnClickListener(v ->
                queue.add(new AuthStringRequest(Request.Method.POST,
                        Helpers.BASE_URL + "deleteGroup?id=" + id,
                        response -> Navigation.findNavController(view).navigate(R.id.action_groupFragment_to_groupsListFragment),
                        e -> {
                            Toast.makeText(requireContext(), "Failed to delete group", Toast.LENGTH_LONG).show();
                            Log.e("DeleteGroup", "Failed to delete group", e);
                        }, Helpers.getAuth(requireContext()))));

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null)
            mapFragment.getMapAsync(googleMap -> {
                map = googleMap;
                map.setOnMarkerClickListener(marker -> {
                    selected = marker;
                    updateMap();
                    User user = users.get(marker.getTitle());
                    if (user.sharing == Sharing.OFF)
                        locationText.setText("Location Disabled");
                    else if (user.locations.size() > 0) {
                        if (Geocoder.isPresent()) {
                            Location location = user.locations.get(user.locations.size() - 1);
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
                    return false;
                });
            });

        return view;
    }

    private void refresh() {
        viewModel.updateGroup(id);
    }

    private void updateMap() {
        if (map == null)
            return;

        if (history == null) {
            history = map.addPolyline(new PolylineOptions());
            history.setColor(Color.RED);
            userMarker = map.addMarker(new MarkerOptions()
                    .title(username)
                    .position(new LatLng(0, 0))
                    .visible(false)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            );
        }

        if (markers.size() == 0 && users.size() > 0) {
            for (User user: users.values())
                if (!user.name.equals(username)) {
                    markers.put(user.name, map.addMarker(new MarkerOptions()
                            .title(user.name)
                            .position(new LatLng(0, 0))
                            .visible(false)
                    ));
                }
            new Handler(Looper.getMainLooper()).postDelayed(this::resetZoom, 1000);
        }

        if (selected != null && users.size() > 0) {
            ArrayList<LatLng> locations = new ArrayList<>();
            for (Location location: users.get(selected.getTitle()).locations)
                locations.add(location.latLng);
            history.setPoints(locations);
        }

        Location userLocation = viewModel.getLocation().getValue();
        if (userLocation != null) {
            userMarker.setPosition(viewModel.getLocation().getValue().latLng);
            userMarker.setVisible(true);
        }

        history.setVisible(historySwitch.isChecked());
    }

    private void resetZoom() {
        if (map == null || markers.size() == 0 || getContext() == null)
            return;

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(userMarker.getPosition());
        for (Marker marker: markers.values())
            builder.include(marker.getPosition());
        LatLngBounds bounds = builder.build();
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,
                getResources().getDisplayMetrics().widthPixels / 6)
        );
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