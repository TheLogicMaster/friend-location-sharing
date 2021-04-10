package com.thelogicmaster.friend_location_sharing;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.provider.MediaStore;
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
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.Volley;
import com.google.android.material.chip.Chip;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static android.app.Activity.RESULT_OK;

public class ChatFragment extends Fragment {

    private String id;
    private RecyclerView recyclerView;
    private MessageRecyclerViewAdapter adapter;
    private Timer timer;
    private LocationSharingViewModel viewModel;
    private RequestQueue queue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        queue = Volley.newRequestQueue(getContext());

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
        adapter = new MessageRecyclerViewAdapter(Helpers.getUsername(getContext()));
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
            // Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //startActivityForResult(takePicture, 69);//zero can be replaced with any action code (called requestCode)

            if ((ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED))
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            else {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto , 6969);
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            FileUploadRequest volleyMultipartRequest = new FileUploadRequest(Request.Method.POST, Helpers.BASE_URL + "sendFile?id=" + id + "&type=IMAGE",
                    response -> {},
                    error -> {
                        Toast.makeText(getContext(), "Failed to send image", Toast.LENGTH_LONG).show();
                        Log.e("SendImage","Failed to send image", error);
                    }, Helpers.getAuth(getContext())) {

                @Override
                protected Map<String, DataPart> getByteData() {
                    Map<String, DataPart> params = new HashMap<>();
                    try {
                        ByteArrayOutputStream output = new ByteArrayOutputStream();
                        FileInputStream input = new FileInputStream(getPath(data.getData()));
                        byte[] buffer = new byte[1024];
                        int n;
                        while (-1 != (n = input.read(buffer)))
                            output.write(buffer, 0, n);
                        byte[] bytes = output.toByteArray();
                        params.put("file", new DataPart("" + System.currentTimeMillis(), bytes));
                    } catch (IOException e) {
                        Log.e("SendImage","Failed to send image", e);
                    }

                    return params;
                }

                @Override
                protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
                    return Response.success(null, null);
                }
            };

            queue.add(volleyMultipartRequest);
        }
    }

    // Source: https://stackoverflow.com/questions/2169649/get-pick-an-image-from-androids-built-in-gallery-app-programmatically
    public String getPath(Uri uri) {
        // just some safety built in
        if( uri == null ) {
            // TODO perform some logging or show user feedback
            return null;
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = { MediaStore.Images.Media.DATA };

        Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        // this is our fallback here
        return uri.getPath();
    }

    @Override
    public void onResume() {
        super.onResume();

        queue.start();
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

        queue.stop();
        if (timer != null)
            timer.cancel();
    }
}