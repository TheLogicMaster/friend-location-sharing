package com.thelogicmaster.friend_location_sharing;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class LoginFragment extends Fragment {

    private RequestQueue queue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        final EditText usernameEdit = view.findViewById(R.id.username);
        final EditText passwordEdit = view.findViewById(R.id.password);

        queue = Volley.newRequestQueue(requireContext());

        view.findViewById(R.id.login).setOnClickListener(v -> {
            queue.add(new AuthStringRequest(Request.Method.GET, Helpers.BASE_URL + "login",
                    response -> {
                        Toast.makeText(getContext(), response, Toast.LENGTH_LONG).show();
                        login(view, usernameEdit.getText().toString(), passwordEdit.getText().toString());
                    },
                    error -> {
                        if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                            Toast.makeText(getContext(), "Incorrect credentials (Hint: try \"user:1234\")", Toast.LENGTH_LONG).show();
                            return;
                        }
                        Log.e("Login", "Failed to login", error);
                        Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_LONG).show();
                    }, Helpers.getAuth(usernameEdit.getText().toString(), passwordEdit.getText().toString())));
        });

        view.findViewById(R.id.signup).setOnClickListener(v -> {
            queue.add(new AuthStringRequest(Request.Method.GET, Helpers.BASE_URL +
                    "signup?username=" + usernameEdit.getText() + "&password=" + passwordEdit.getText(),
                    response -> {
                        Toast.makeText(getContext(), response, Toast.LENGTH_LONG).show();
                        if ("Successfully created account!".equals(response))
                            login(view, usernameEdit.getText().toString(), passwordEdit.getText().toString());
                    },
                    error -> {
                        Log.e("Signup", "Failed to signup", error);
                        Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_LONG).show();
                    }, Helpers.getAuth(requireActivity())));
        });

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        queue.stop();
    }

    @Override
    public void onResume() {
        super.onResume();
        queue.start();
    }

    private void login(View view, String username, String password) {
        Helpers.setCredentials(requireActivity(), username, password);
        Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_homeFragment);
    }
}