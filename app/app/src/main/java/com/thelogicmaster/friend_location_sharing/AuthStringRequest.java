package com.thelogicmaster.friend_location_sharing;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class AuthStringRequest extends StringRequest {

    private final String auth;

    public AuthStringRequest(int method, String url, Response.Listener<String> listener, @Nullable Response.ErrorListener errorListener, String auth) {
        super(method, url, listener, errorListener);
        this.auth = auth;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", auth);
        return headers;
    }
}
