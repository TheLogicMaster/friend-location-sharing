package com.thelogicmaster.friend_location_sharing;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ChatRecyclerViewAdapter extends RecyclerView.Adapter<ChatRecyclerViewAdapter.ViewHolder> {

    private List<Chat> chats = new ArrayList<>();
    private final ChatClickListener listener;

    public ChatRecyclerViewAdapter(ChatClickListener listener) {
        this.listener = listener;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_chat_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.chat = chats.get(position);
        holder.nameView.setText(chats.get(position).name);
        holder.view.setOnClickListener(view -> listener.onClick(holder.chat));
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    public void setGroups(List<Chat> chats) {
        this.chats = chats;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView nameView;
        public Chat chat;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            nameView = view.findViewById(R.id.chat_name);
        }

        @NotNull
        @Override
        public String toString() {
            return super.toString() + " '" + nameView.getText() + "'";
        }
    }

    public interface ChatClickListener {

        void onClick(Chat chat);
    }
}