package com.thelogicmaster.friend_location_sharing;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.google.android.material.chip.Chip;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ChatFragment extends Fragment {

    private String id;
    private RecyclerView recyclerView;
    private MessageRecyclerViewAdapter adapter;
    private Timer timer;
    private LocationSharingViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        id = getArguments().getString("id");

        final EditText messageText = view.findViewById(R.id.message_send_text);
        final TextView chatName = view.findViewById(R.id.chat_name);

        viewModel = new ViewModelProvider(requireActivity()).get(LocationSharingViewModel.class);
        viewModel.getChats().observe(getViewLifecycleOwner(), chats -> {
            Chat chat = null;
            for (Chat c: chats)
                if (c.id.equals(id)) {
                    chat = c;
                    break;
                }
            if (chat == null)
                return;

            chatName.setText(chat.name);
            int prev = adapter.getItemCount();
            adapter.setMessages(chat.messages);
            if (prev != adapter.getItemCount())
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
        });

        recyclerView = view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new GridLayoutManager(view.getContext(), 1));
        adapter = new MessageRecyclerViewAdapter();
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.send_message).setOnClickListener(v -> {
            JSONObject data = new JSONObject();
            try {
                JSONObject message = new JSONObject();
                data.put("content", messageText.getText().toString());
                data.put("type", Message.MessageType.TEXT);
                data.put("id", id);
            } catch (JSONException e) {
                Log.e("AddChat", "Failed to create request data", e);
            }
            RequestQueue queue = Volley.newRequestQueue(requireContext());
            queue.add(new AuthJsonRequest(Request.Method.POST, Helpers.BASE_URL + "sendMessage", data,
                    response -> messageText.setText(""),
                    error -> {
                        Log.e("SendMessage", "Failed to send message", error);
                        Toast.makeText(requireContext(), "Failed to send message", Toast.LENGTH_SHORT).show();
                    }, Helpers.getAuth(requireContext())) {
                @Override
                protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                    return Response.success(null, null);
                }
            });
        });

        view.findViewById(R.id.send_file).setOnClickListener(v -> {

        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        viewModel.updateFriends(null);
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                viewModel.updateChat(id);
            }
        }, 0, 1000);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (timer != null)
            timer.cancel();
    }
}