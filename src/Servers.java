package it.polito.activitypub;

import java.util.*;
import java.util.stream.Stream;

public class Servers implements Server, NamingService{
    protected String serverName;
    protected HashMap<String, Actor> actorsMap = new HashMap<>();

    public Servers(String name){
        serverName = name;
    }

    public String getName(){
        return serverName;
    }

    public Actor createActor(String a, String b) throws ActivityPubException{
        String str ="@" +a+ "@" + serverName;
        if (actorsMap.containsKey(str)){
            throw new ActivityPubException("Actor exists in server");
        }
        Actor currActor = new Actor(a, b, serverName);
        actorsMap.put(str, currActor);
        allActors.put(a, currActor);
        return currActor;
    }

    public List<Actor> listAllActors(){
        List<Actor> actorList = actorsMap.values().stream()
                                .toList();
        return actorList;
    }

    public Optional<Actor> getActor(String username){
        if (actorsMap.containsKey(username)){
            return Optional.of(actorsMap.get(username));
        }
        else if (actorsMap.containsKey("@" +username+ "@" +serverName)){
            return Optional.of(actorsMap.get("@" +username+ "@" +serverName));
        }
        return Optional.empty();
    }

	@Override
	public boolean deleteActor(String username) {
        if (actorsMap.containsKey("@" +username+ "@" +serverName)){
			allActors.values().forEach(a->{a.following.remove(username); a.followsMe.remove(username);});
            actorsMap.remove("@"+username+"@"+serverName);
            allActors.remove("@" +username+ "@" +serverName);
            return true;
        }
		
        return false;
	}

	@Override
	public boolean follow(String followerId, String targetId) throws ActivityPubException {
		String f=followerId.contains("@")?followerId:"@"+followerId+"@"+serverName;
    	String t=targetId.contains("@")?targetId.split("@")[1]:targetId;
    	if (!actorsMap.containsKey(f) || !allActors.containsKey(t)) throw new ActivityPubException("can't follow");
    	if (actorsMap.get(f).following.contains(t)) return false;
    	actorsMap.get(f).followActor(t);
    	allActors.get(t).getFollowed(f.split("@")[1]);
    	return true;
	}

	@Override
	public boolean addFollower(String followerId, String targetId) throws ActivityPubException {
		String f = (followerId.contains("@"))?followerId:"@"+followerId+"@"+serverName;
		String t = (targetId.contains("@"))?targetId.split("@")[1]:targetId;
		if (!(actorsMap.containsKey(f)) || !(allActors.containsKey(t))) throw new ActivityPubException("can't add follower");
		if (actorsMap.get(f).followsMe.contains(t)) return false;
		actorsMap.get(f).followsMe.add(t);
		allActors.get(t).following.add(f.split("@")[1]);
		return true;
	}

	@Override
	public boolean unfollow(String followerId, String targetId) throws ActivityPubException {
		String f=(followerId.contains("@"))?followerId:"@"+followerId+"@"+serverName;
		String t=(targetId.contains("@"))?targetId.split("@")[1]:targetId;
		if(!(actorsMap.containsKey(f)) || !(allActors.containsKey(t))) throw new ActivityPubException("can't unfollow");
		if (!(actorsMap.get(f).following.contains(t))) return false;
		actorsMap.get(f).following.remove(t);
		allActors.get(t).followsMe.remove(f.split("@")[1]);
		return true;
	}

	@Override
	public boolean removeFollower(String followerId, String targetId) throws ActivityPubException {
        return unfollow(followerId, targetId);
	}

	@Override
	public List<Actor> getFollowers(String fullId) {
		return ((fullId.contains("@"))?actorsMap.get(fullId).followsMe.stream().map(p -> (Actor)allActors.get(p)).toList():actorsMap.get("@"+fullId+"@"+serverName).followsMe.stream().map(p -> (Actor)allActors.get(p)).toList());
	}

	@Override
	public List<Actor> getFollowing(String fullId) {
		return ((fullId.contains("@"))?actorsMap.get(fullId).following.stream().map(p -> (Actor)allActors.get(p)).toList():actorsMap.get("@"+fullId+"@"+serverName).following.stream().map(p -> (Actor)allActors.get(p)).toList());
	}

	@Override
	public boolean isFollowing(String followerId, String targetId) {
		String f=(followerId.contains("@"))?followerId:"@"+followerId+"@"+serverName;
		String t=(targetId.contains("@"))?targetId.split("@")[1]:targetId;
		return actorsMap.containsKey(f) && actorsMap.get(f).following.contains(t);
	}

	@Override
	public Activity createActivity(String actorId, ActivityType type, String content) {
		Activity act = new Activity(type, actorId, content);
		String str = (actorId.contains("@"))?actorId:"@"+actorId+"@"+serverName;
		actorsMap.get(str).outBox.add(act);
		receiveActivity(act, actorsMap.get(str).followsMe.toArray(String[]::new));
		return act;
	}

	@Override
	public Stream<Activity> getAllActivities() {
		return actorsMap.values().stream().flatMap(a -> a.outBox.stream());
	}

	@Override
	public Stream<Activity> getInbox(String actorId) {
		return actorsMap.get((actorId.contains("@"))?actorId:"@"+actorId+"@"+serverName).inBox.stream();
	}

	@Override
	public Stream<Activity> getOutbox(String actorId) {
		return actorsMap.get((actorId.contains("@"))?actorId:"@"+actorId+"@"+serverName).outBox.stream();
	}

	@Override
	public void receiveActivity(Activity activity, String... targetId) {
		for (String string:targetId){allActors.get(string).inBox.add(activity);}
	}

	@Override
	public void registerServer(String name, Server server) {
		InMemoryNamingService naming = new InMemoryNamingService();
        naming.registerServer(name, server);
	}

	@Override
	public Optional<Server> resolveServer(String name) {
		InMemoryNamingService naming = new InMemoryNamingService();
        Optional<Server> server = naming.resolveServer(name);
        return server;
	}

}
