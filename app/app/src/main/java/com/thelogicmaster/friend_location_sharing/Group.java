package com.thelogicmaster.friend_location_sharing;

import java.util.List;

public class Group {

    public final String id;
    public final String name;
    public final List<User> users;

    public Group(String id, String name, List<User> users) {
        this.id = id;
        this.name = name;
        this.users = users;
    }
}
