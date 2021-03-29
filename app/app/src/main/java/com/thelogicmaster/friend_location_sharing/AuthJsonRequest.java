package com.thelogicmaster.friend_location_sharing;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AuthJsonRequest extends JsonObjectRequest {

    private final String auth;
    private final boolean ignoreResponse;

    public AuthJsonRequest(int method, String url, @Nullable JSONObject jsonRequest, Response.Listener<JSONObject> listener, @Nullable Response.ErrorListener errorListener, String auth) {
        this(method, url, jsonRequest, listener, errorListener, auth, false);
    }

    public AuthJsonRequest(int method, String url, @Nullable JSONObject jsonRequest, Response.Listener<JSONObject> listener, @Nullable Response.ErrorListener errorListener, String auth, boolean ignoreResponse) {
        super(method, url, jsonRequest, listener, errorListener);
        this.auth = auth;
        this.ignoreResponse = ignoreResponse;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", auth);
        return headers;
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        if (ignoreResponse)
            return Response.success(null, null);
        return super.parseNetworkResponse(response);
    }
}
