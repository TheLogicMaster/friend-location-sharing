package com.thelogicmaster.friend_location_sharing;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AuthJsonRequest extends JsonObjectRequest {

    private final String auth;

    public AuthJsonRequest(int method, String url, @Nullable JSONObject jsonRequest, Response.Listener<JSONObject> listener, @Nullable Response.ErrorListener errorListener, String auth) {
        super(method, url, jsonRequest, listener, errorListener);
        this.auth = auth;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", auth);
        return headers;
    }
}
