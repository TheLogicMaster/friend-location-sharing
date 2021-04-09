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

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ChatListFragment extends Fragment {

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private ChatRecyclerViewAdapter adapter;
    private Timer timer;
    private LocationSharingViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(LocationSharingViewModel.class);
        viewModel.getChats().observe(getViewLifecycleOwner(), chats -> {
            adapter.setGroups(chats);
            recyclerView.scrollToPosition(adapter.getItemCount() - 1);
            swipeRefresh.setRefreshing(false);
        });

        swipeRefresh = view.findViewById(R.id.refresh);
        swipeRefresh.setOnRefreshListener(this::refresh);

        view.findViewById(R.id.add_chat).setOnClickListener(
                v -> {
                    if (viewModel.getFriends().getValue() == null)
                        return;
                    (new AddChatDialog(new ArrayList<>(viewModel.getFriends().getValue().values()))).show(getChildFragmentManager(), "AddChat");
                });

        recyclerView = view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new GridLayoutManager(view.getContext(), 1));
        adapter = new ChatRecyclerViewAdapter(chat -> {
            Bundle params = new Bundle();
            params.putString("id", chat.id);
            Navigation.findNavController(view).navigate(R.id.action_chatListFragment_to_chatFragment, params);
        });
        recyclerView.setAdapter(adapter);

        return view;
    }

    private void refresh() {
        viewModel.updateFriends(null);
        viewModel.updateChats(e -> {
            Log.e("ChatsRequest", "Failed to get update chats", e);
            swipeRefresh.setRefreshing(false);
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        viewModel.updateFriends(null);
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                refresh();
            }
        }, 0, 5000);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (timer != null)
            timer.cancel();
    }
}