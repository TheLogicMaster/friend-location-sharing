package com.thelogicmaster.friend_location_sharing;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MessageDetailsDialog extends BottomSheetDialogFragment {

    private final Message message;

    public MessageDetailsDialog(Message message) {
        this.message = message;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_message_details, container, false);

        ((TextView)view.findViewById(R.id.message_id)).setText("ID: " + message.id);
        ((TextView)view.findViewById(R.id.message_content)).setText("Content: " + message.content);
        ((TextView)view.findViewById(R.id.message_time)).setText("Time: " +
                SimpleDateFormat.getInstance().format(new Date(message.time * 1000)));
        ((TextView)view.findViewById(R.id.message_type)).setText("Type: " + message.type);
        ((TextView)view.findViewById(R.id.message_user)).setText("User: " + message.user);

        return view;
    }
}
