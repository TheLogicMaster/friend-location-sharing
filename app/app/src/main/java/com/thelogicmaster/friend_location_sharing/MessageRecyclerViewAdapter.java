package com.thelogicmaster.friend_location_sharing;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MessageRecyclerViewAdapter extends RecyclerView.Adapter<MessageRecyclerViewAdapter.ViewHolder> {

    private List<Message> messages = new ArrayList<>();
    private final String username;
    private final MessageLongClickListener listener;

    public MessageRecyclerViewAdapter(String username, MessageLongClickListener listener) {
        this.username = username;
        this.listener = listener;
    }

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
        holder.cardView.setOnLongClickListener(v -> {
            listener.onLongClick(holder.message);
            return true;
        });
        holder.timeView.setText(new PrettyTime().format(new Date(holder.message.time * 1000)));
        if (holder.message.user.equals(username))
            holder.cardView.setCardBackgroundColor(Color.CYAN);
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
        public final CardView cardView;
        public final TextView userView;
        public final TextView textView;
        public final ImageView imageView;
        public final TextView timeView;
        public Message message;

        public ViewHolder(View view) {
            super(view);
            this.cardView = (CardView) view;
            userView = view.findViewById(R.id.message_user);
            timeView = view.findViewById(R.id.message_time_relative);
            textView = view.findViewById(R.id.message_text);
            imageView = view.findViewById(R.id.message_image);
        }

        @NotNull
        @Override
        public String toString() {
            return super.toString() + " '" + textView.getText() + "'";
        }
    }

    public interface MessageLongClickListener {

        void onLongClick(Message message);
    }
}