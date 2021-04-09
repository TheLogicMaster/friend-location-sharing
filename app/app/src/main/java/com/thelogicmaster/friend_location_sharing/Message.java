package com.thelogicmaster.friend_location_sharing;

public class Message {

    public final String id;
    public final String user;
    public final MessageType type;
    public final String content;

    public Message(String id, String user, MessageType type, String content) {
        this.id = id;
        this.user = user;
        this.type = type;
        this.content = content;
    }

    public enum MessageType {
        TEXT,
        IMAGE,
        FILE
    }
}
