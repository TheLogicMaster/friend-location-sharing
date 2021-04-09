package com.thelogicmaster.friend_location_sharing;

import java.util.ArrayList;
import java.util.List;

public class User {

    public final String name;
    public final Sharing sharing;
    public final List<Location> locations;

    public User(String name) {
        this(name, Sharing.OFF, new ArrayList<>());
    }

    public User(String name, Sharing sharing) {
        this(name, sharing, new ArrayList<>());
    }

    public User(String name, Sharing sharing, List<Location> locations) {
        this.sharing = sharing;
        this.name = name;
        this.locations = locations;
    }
}