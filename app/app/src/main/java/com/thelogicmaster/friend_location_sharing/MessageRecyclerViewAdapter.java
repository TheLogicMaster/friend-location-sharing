package com.thelogicmaster.friend_location_sharing;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MessageRecyclerViewAdapter extends RecyclerView.Adapter<MessageRecyclerViewAdapter.ViewHolder> {

    private List<Message> messages = new ArrayList<>();

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_message_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.message = messages.get(position);
        holder.userView.setText(messages.get(position).user);
        if (messages.get(position).type == Message.MessageType.TEXT)
            holder.textView.setText(messages.get(position).content);
        holder.textView.setVisibility(messages.get(position).type == Message.MessageType.TEXT ? View.VISIBLE : View.GONE);
        if (messages.get(position).type == Message.MessageType.IMAGE)
            Picasso.get().load(messages.get(position).content).into(holder.imageView);
        holder.imageView.setVisibility(messages.get(position).type == Message.MessageType.IMAGE ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView userView;
        public final TextView textView;
        public final ImageView imageView;
        public Message message;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            userView = view.findViewById(R.id.message_user);
            textView = view.findViewById(R.id.message_text);
            imageView = view.findViewById(R.id.message_image);
        }

        @NotNull
        @Override
        public String toString() {
            return super.toString() + " '" + textView.getText() + "'";
        }
    }
}