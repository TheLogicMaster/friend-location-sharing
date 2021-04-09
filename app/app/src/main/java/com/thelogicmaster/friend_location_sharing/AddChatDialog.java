package com.thelogicmaster.friend_location_sharing;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AddChatDialog extends BottomSheetDialogFragment {

    private final List<User> friends;

    public AddChatDialog(List<User> friends) {
        this.friends = friends;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_add_chat, container, false);

        ChipGroup chipGroup = view.findViewById(R.id.friends);
        final ArrayList<Chip> chips = new ArrayList<>();
        for (User friend: friends) {
            Chip chip = new Chip(getContext());
            chip.setText(friend.name);
            chip.setCheckable(true);
            chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.design_default_color_secondary)));
            chips.add(chip);
            chipGroup.addView(chip);
        }

        EditText chatName = view.findViewById(R.id.add_chat_name);

        view.findViewById(R.id.create_chat).setOnClickListener(v -> {
            JSONObject data = new JSONObject();
            try {
                JSONArray users = new JSONArray();
                for (Chip chip: chips)
                    if (chip.isChecked())
                        users.put(chip.getText());
                if (users.length() == 0 || "".equals(chatName.getText().toString()))
                    return;
                data.put("name", chatName.getText().toString());
                data.put("users", users);
            } catch (JSONException e) {
                Log.e("AddChat", "Failed to create request data", e);
            }
            RequestQueue queue = Volley.newRequestQueue(requireContext());
            queue.add(new AuthJsonRequest(Request.Method.POST, Helpers.BASE_URL + "createChat", data,
                    response -> {},
                    error -> {
                        Log.e("CreateChat", "Failed to create chat", error);
                        Toast.makeText(requireContext(), "Failed to create chat", Toast.LENGTH_SHORT).show();
                    }, Helpers.getAuth(requireContext())) {
                @Override
                protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                    return Response.success(null, null);
                }
            });
        });

        return view;

    }
}
