package com.thelogicmaster.friend_location_sharing;

import java.util.ArrayList;
import java.util.List;

public class Chat extends Group {

    public final List<Message> messages;

    public Chat(String id, String name, List<User> users, List<Message> messages) {
        super(id, name, users);

        this.messages = messages;
    }

    public Chat(String id, String name, List<User> users) {
        this(id, name, users, new ArrayList<>());
    }
}
