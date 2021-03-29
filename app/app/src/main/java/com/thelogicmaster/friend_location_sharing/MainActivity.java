package com.thelogicmaster.friend_location_sharing;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import android.os.Looper;
import android.util.Log;

import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private RequestQueue queue;
    private boolean autoLocation;
    private String sharing;
    private SharedPreferences.OnSharedPreferenceChangeListener prefsListener;
    private FusedLocationProviderClient locationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private Location location;
    private LocationSharingViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(LocationSharingViewModel.class);

        queue = Volley.newRequestQueue(this);
        locationClient = LocationServices.getFusedLocationProviderClient(this);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        autoLocation = prefs.getBoolean("autoLocation", false);
        sharing = prefs.getString("sharing", "OFF");

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(3000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                updateLocation(false);
                location = locationResult.getLastLocation();
                viewModel.updateLocation(new com.thelogicmaster.friend_location_sharing.Location(
                        location.getLongitude(), location.getLatitude(), 0
                ));
            }
        };

        prefsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if ("autoLocation".equals(key))
                    autoLocation = sharedPreferences.getBoolean("autoLocation", false);
                else if ("sharing".equals(key)) {
                    sharing = sharedPreferences.getString("sharing", "OFF");
                    updateSharing();
                }
            }
        };

        findViewById(R.id.fab).setOnClickListener(view -> updateLocation(true));
    }

    @Override
    public void onPause() {
        super.onPause();
        queue.stop();

        locationClient.removeLocationUpdates(locationCallback);

        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(prefsListener);
    }

    @Override
    public void onResume() {
        super.onResume();

        queue.start();

        locationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(prefsListener);
    }

    private void updateLocation(boolean manual) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (manual)
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 6969);
            return;
        }

        if (!manual && !autoLocation)
            return;

        locationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location == null) {
                if (manual)
                    Toast.makeText(MainActivity.this, "Failed to get location", Toast.LENGTH_SHORT).show();
                return;
            }

            JSONObject data = new JSONObject();
            try {
                if (location != null) {
                    data.put("long", location.getLongitude());
                    data.put("lat", location.getLatitude());
                }
                data.put("sharing", sharing);
            } catch (JSONException e) {
                Log.e("UpdateLocation", "Failed to create location data object", e);
                return;
            }

            sendLocationData(data, !manual);
        });
    }

    private void updateSharing() {
        JSONObject data = new JSONObject();
        try {
            data.put("sharing", sharing);
        } catch (JSONException e) {
            Log.e("UpdateLocation", "Failed to create sharing data object", e);
            return;
        }
        sendLocationData(data, false);
    }

    private void sendLocationData(JSONObject data, boolean silent) {
        queue.add(new AuthJsonRequest(Request.Method.POST, Helpers.BASE_URL + "updateLocation", data,
                response -> {
                    if (!silent)
                        Toast.makeText(this, "Updated location", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    if (!silent)
                        Toast.makeText(this, "Failed to update location", Toast.LENGTH_SHORT).show();
                    Log.e("UpdateLocation", "Failed", error);
                }, Helpers.getAuth(this), true));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != 6969)
            return;

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            updateLocation(true);
        else
            Toast.makeText(this, "This app can't function without location permissions", Toast.LENGTH_SHORT).show();
    }
}