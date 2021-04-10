package com.thelogicmaster.friend_location_sharing;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.navigation.Navigation;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ManageChatDialog extends BottomSheetDialogFragment {

    private final Chat chat;
    private final Runnable deleteCallback;

    public ManageChatDialog(Chat chat, Runnable deleteCallback) {
        this.chat = chat;
        this.deleteCallback = deleteCallback;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_manage_chat, container, false);

        ((TextView)view.findViewById(R.id.manage_chat_name)).setText(chat.name);

        view.findViewById(R.id.delete_chat).setOnClickListener(v -> {
            RequestQueue queue = Volley.newRequestQueue(requireContext());
            queue.add(new AuthStringRequest(Request.Method.POST, Helpers.BASE_URL + "deleteChat?id=" + chat.id,
                    response -> deleteCallback.run(),
                    error -> {
                        Log.e("DeleteChat", "Failed to delete chat", error);
                        Toast.makeText(requireContext(), "Failed to delete chat", Toast.LENGTH_SHORT).show();
                    },
                    Helpers.getAuth(getContext())));
        });

        return view;
    }
}
