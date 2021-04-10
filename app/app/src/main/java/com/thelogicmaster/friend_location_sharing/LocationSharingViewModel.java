package com.thelogicmaster.friend_location_sharing;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class LocationSharingViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Group>> groups = new MutableLiveData<>();
    private final MutableLiveData<Map<String, User>> friends = new MutableLiveData<>();
    private final MutableLiveData<List<Chat>> chats = new MutableLiveData<>();
    private final MutableLiveData<Location> location = new MutableLiveData<>();

    private final RequestQueue queue;

    public LocationSharingViewModel(@NonNull Application application) {
        super(application);

        queue = Volley.newRequestQueue(application);
    }

    public void updateLocation(Location location) {
        this.location.setValue(location);
    }

    public void updateFriends(Response.ErrorListener errorListener) {
        queue.add(new AuthJsonRequest(Request.Method.GET, Helpers.BASE_URL + "friends", null, friendsObj -> {
            try {
                HashMap<String, User> friendList = new HashMap<>();
                for (Iterator<String> it = friendsObj.keys(); it.hasNext(); ) {
                    String name = it.next();
                    JSONObject friendObj = friendsObj.getJSONObject(name);
                    friendList.put(name, new User(name, Sharing.valueOf(friendObj.getString("sharing"))));
                }
                friends.setValue(friendList);
            } catch (JSONException e) {
                errorListener.onErrorResponse(new VolleyError("Failed to parse friends", e));
            }
        }, errorListener, Helpers.getAuth(getApplication().getApplicationContext())));
    }

    public void updateFriend(String name) {
        queue.add(new AuthJsonRequest(Request.Method.GET, Helpers.BASE_URL + "friend?name=" + name, null,
                friendObj -> {
                    try {
                        ArrayList<com.thelogicmaster.friend_location_sharing.Location> locations = new ArrayList<>();
                        JSONArray locationsArray = friendObj.getJSONArray("locations");
                        for (int i = 0; i < locationsArray.length(); i++) {
                            JSONObject locationObj = locationsArray.getJSONObject(i);
                            locations.add(new com.thelogicmaster.friend_location_sharing.Location(locationObj.getDouble("long"),
                                    locationObj.getDouble("lat"), locationObj.getLong("time")));
                        }
                        HashMap<String, User> newUsers = new HashMap<>(friends.getValue());
                        newUsers.put(name, new User(name, Sharing.valueOf(friendObj.getString("sharing")), locations));
                        friends.setValue(newUsers);
                    } catch (JSONException e) {
                        Log.e("FriendParsing", "Failed to parse friend", e);
                    }
                },
                error -> Log.e("FriendRequest", "Failed to get friend", error),
                Helpers.getAuth(getApplication().getApplicationContext())));
    }

    public void updateGroups(Response.ErrorListener errorListener) {
        queue.add(new AuthJsonRequest(Request.Method.GET, Helpers.BASE_URL + "groups", null,
                response -> {
                    try {
                        JSONArray groupArray = response.getJSONArray("groups");
                        ArrayList<Group> groups = new ArrayList<>();
                        for (int i = 0; i < groupArray.length(); i++) {
                            JSONObject groupObj = groupArray.getJSONObject(i);
                            JSONObject usersObj = groupObj.getJSONObject("users");
                            ArrayList<User> users = new ArrayList<>();
                            for (Iterator<String> it = usersObj.keys(); it.hasNext(); ) {
                                String name = it.next();
                                JSONObject userObj = usersObj.getJSONObject(name);
                                users.add(new User(name, Sharing.valueOf(userObj.getString("sharing"))));
                            }
                            groups.add(new Group(groupObj.getString("id"), groupObj.getString("name"), users));
                        }
                        this.groups.setValue(groups);
                    } catch (JSONException e) {
                        errorListener.onErrorResponse(new VolleyError("Failed to parse groups", e));
                    }
                },
                errorListener, Helpers.getAuth(getApplication().getApplicationContext())));
    }

    public void updateGroup(String id) {
        queue.add(new AuthJsonRequest(Request.Method.GET, Helpers.BASE_URL + "group?id=" + id, null,
                groupObj -> {
                    try {
                        JSONObject usersObj = groupObj.getJSONObject("users");
                        ArrayList<User> users = new ArrayList<>();
                        for (Iterator<String> it = usersObj.keys(); it.hasNext(); ) {
                            String name = it.next();
                            JSONObject userObj = usersObj.getJSONObject(name);
                            ArrayList<Location> locations = new ArrayList<>();
                            JSONArray locationsArray = userObj.getJSONArray("locations");
                            for (int i = 0; i < locationsArray.length(); i++) {
                                JSONObject locationObj = locationsArray.getJSONObject(i);
                                locations.add(new Location(locationObj.getDouble("long"),
                                        locationObj.getDouble("lat"), locationObj.getLong("time")));
                            }
                            users.add(new User(name, Sharing.valueOf(userObj.getString("sharing")), locations));
                        }
                        ArrayList<Group> newGroups = new ArrayList<>(groups.getValue());
                        Group oldGroup = null;
                        for (Group g : newGroups)
                            if (g.id.equals(id)) {
                                oldGroup = g;
                                break;
                            }
                        if (oldGroup != null)
                            newGroups.remove(oldGroup);
                        newGroups.add(new Group(id, groupObj.getString("name"), users));
                        groups.setValue(newGroups);
                    } catch (JSONException e) {
                        Log.e("GroupParsing", "Failed to parse group", e);
                    }
                },
                error -> Log.e("GroupRequest", "Failed to get group", error),
                Helpers.getAuth(getApplication().getApplicationContext())));
    }

    public void updateChats(Response.ErrorListener errorListener) {
        queue.add(new AuthJsonRequest(Request.Method.GET, Helpers.BASE_URL + "chats", null,
                response -> {
                    try {
                        JSONArray chatArray = response.getJSONArray("chats");
                        ArrayList<Chat> chats = new ArrayList<>();
                        for (int i = 0; i < chatArray.length(); i++) {
                            JSONObject chatObj = chatArray.getJSONObject(i);
                            JSONArray usersArray = chatObj.getJSONArray("users");
                            ArrayList<User> users = new ArrayList<>();
                            for (Object name : users)
                                users.add(new User((String)name));
                            chats.add(new Chat(chatObj.getString("id"), chatObj.getString("name"), users));
                        }
                        this.chats.setValue(chats);
                    } catch (JSONException e) {
                        errorListener.onErrorResponse(new VolleyError("Failed to parse chats", e));
                    }
                },
                errorListener, Helpers.getAuth(getApplication().getApplicationContext())));
    }

    public void updateChat(String id) {
        queue.add(new AuthJsonRequest(Request.Method.GET, Helpers.BASE_URL + "chat?id=" + id, null,
                chatObj -> {
                    try {
                        JSONArray usersArray = chatObj.getJSONArray("users");
                        ArrayList<User> users = new ArrayList<>();
                        for (Object name : users)
                            users.add(new User((String)name));

                        JSONArray messagesArray = chatObj.getJSONArray("messages");
                        ArrayList<Message> messages = new ArrayList<>();
                        for (int i = 0; i < messagesArray.length(); i++) {
                            JSONObject messageObj = messagesArray.getJSONObject(i);
                            messages.add(new Message(
                                    messageObj.getString("id"),
                                    messageObj.getString("user"),
                                    Message.MessageType.valueOf(messageObj.getString("type")),
                                    messageObj.getString("content"),
                                    messageObj.getLong("time")
                            ));
                        }

                        ArrayList<Chat> newChats = new ArrayList<>(chats.getValue());
                        Chat oldChat = null;
                        for (Chat c : newChats)
                            if (c.id.equals(id)) {
                                oldChat = c;
                                break;
                            }
                        if (oldChat != null)
                            newChats.remove(oldChat);
                        newChats.add(new Chat(id, chatObj.getString("name"), users, messages));
                        chats.setValue(newChats);
                    } catch (JSONException | IllegalArgumentException e) {
                        Log.e("ChatParsing", "Failed to parse chat", e);
                    }
                },
                error -> Log.e("ChatRequest", "Failed to get chat", error),
                Helpers.getAuth(getApplication().getApplicationContext())));
    }

    public MutableLiveData<List<Chat>> getChats() {
        return chats;
    }

    public MutableLiveData<List<Group>> getGroups() {
        return groups;
    }

    public MutableLiveData<Map<String, User>> getFriends() {
        return friends;
    }

    public MutableLiveData<Location> getLocation() {
        return location;
    }
}
