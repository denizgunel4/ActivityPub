package it.polito.activitypub;
import static it.polito.activitypub.ActivityPubUtilities.fullId;

import java.io.Serializable;
import java.util.*;

public class Actor implements Serializable{
    public final String username;
    public final String displayName;
    public final String fullId;
    public final String serverName;
    public ArrayList<String> following = new ArrayList<>();
    public ArrayList<String> followsMe = new ArrayList<>();
    public ArrayList<Activity> outBox = new ArrayList<>();
    public ArrayList<Activity> inBox = new ArrayList<>();

    Actor(String username, String displayName, String serverName) {
        this.username = username;
        this.displayName = displayName;
        this.fullId = fullId(username, serverName);
        this.serverName = serverName;
    }

    public void followActor(String actorId){
        following.add(actorId);
    }

    public void getFollowed(String actorId){
        followsMe.add(actorId);
    }

    public String getFullId() {
        return fullId;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

}
