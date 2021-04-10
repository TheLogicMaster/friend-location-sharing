package com.thelogicmaster.friend_location_sharing;

public class Message {

    public final String id;
    public final String user;
    public final MessageType type;
    public final String content;
    public final long time;

    public Message(String id, String user, MessageType type, String content, long time) {
        this.id = id;
        this.user = user;
        this.type = type;
        this.content = content;
        this.time = time;
    }

    public enum MessageType {
        TEXT,
        IMAGE,
        FILE
    }
}
