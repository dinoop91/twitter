package com.work.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import twitter4j.IDs;
import twitter4j.JSONObject;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class FollowFriendsOfSomeone {

	
	static public int count=0;
	int maxCount=950;
	static public ElasticSearch es=new ElasticSearch();
	static Configurations config=Configurations.getInstance();
	static Twitter twitter=null;
	static List<Long> friends=new ArrayList<Long>();
	static List<Long> followers=new ArrayList<Long>();

	private void process() throws TwitterException {
		twitterInstance();
		System.out.println(friends);
		System.out.println(followers);
		List<Long> folows = getSomeonesFollowers();
		System.out.println(folows);
		for(Long idlong : folows){
			System.out.println(idlong);
			run(idlong);
		}
		
	}
		
	public static void twitterInstance(){
		try{
			
			es.createClient(config.IP,Integer.parseInt(config.PORT), config.CLUSTER_NAME);
			
			
			ConfigurationBuilder cb = new ConfigurationBuilder();
			cb.setDebugEnabled(true)
			.setUserStreamWithFollowingsEnabled(true)
			.setJSONStoreEnabled(true)
			.setIncludeEntitiesEnabled(true)
			.setIncludeMyRetweetEnabled(true)
			.setUserStreamRepliesAllEnabled(true)
			.setOAuthConsumerKey(config.CONSUMER_KEY)
			.setOAuthConsumerSecret(config.CONSUMER_KEY_SECRET)
			.setOAuthAccessToken(config.ACCESS_TOKEN)
			.setOAuthAccessTokenSecret(config.ACCESS_TOKEN_SECRET);

			Configuration twconfig=cb.build();
			TwitterFactory tf = new TwitterFactory(twconfig);
			twitter = tf.getInstance();

			long cursor = -1;
			IDs ids;
			System.out.println("Listing followers's ids.");
			do {
				ids = twitter.getFriendsIDs(config.USER_NAME, cursor);
				for (long id : ids.getIDs()) {
					friends.add(id);
				}
			} while ((cursor = ids.getNextCursor()) != 0);
			
			cursor = -1;
			System.out.println("Listing followers's ids.");
			do {
				ids = twitter.getFollowersIDs(config.USER_NAME, cursor);
				for (long id : ids.getIDs()) {
					followers.add(id);
				}
			} while ((cursor = ids.getNextCursor()) != 0);
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("friends : "+friends.size());
	}
	public List<Long> getSomeonesFollowers() throws TwitterException{
		List<Long> folwrs=new ArrayList<Long>();
		long cursor = -1;
		IDs ids;
		System.out.println("Listing followers's ids.");
		do {
			ids = twitter.getFollowersIDs("ac3c0fc50b0f4ff", cursor);
			for (long id : ids.getIDs()) {
				folwrs.add(id);
			}
		} while ((cursor = ids.getNextCursor()) != 0);
		return folwrs;
	}

	public int run(Long idLong) {
		try{
			JSONObject json=new JSONObject();
			json.put("time", getTime());
			json.put("author", idLong);
			json.put("name", String.valueOf(idLong));
			json.put("processed",false);
			String id=String.valueOf(idLong);
			Map<String, Object> doc=es.getDocument(config.INDEX_NAME,config.TYPE_NAME ,id);

			if(doc==null){
				if(friends.contains(idLong)){
					System.out.println("already a friend");
				}
				else if (followers.contains(idLong)){
					System.out.println("is my follwer");
				}
				else{
					count++;
					System.out.println("count---------"+count);
					if(count>=maxCount){
						System.out.println("max");
						return 0;
					}
					es.indexDocument(config.INDEX_NAME,config.TYPE_NAME,json,id);
					follow(idLong);
				}
			}else{
				System.out.println("already processed @@@@@@");
			}

		}catch(Exception e){
			e.printStackTrace();
		}
		return 1;
	}

	public void follow(long id){
		try {
			twitter.createFriendship(id);
		} catch (TwitterException e) {
			if(e.getStatusCode()==403){
				System.out.println("cant follow now, follow limit reached");
			}
			else
				e.printStackTrace();
		}
	}

	public static String getTime(){
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		f.setTimeZone(TimeZone.getTimeZone("GMT"));
		return f.format(new Date());
	}
	public static void main(String args[]) throws TwitterException{
		
		FollowFriendsOfSomeone fol=new FollowFriendsOfSomeone();
		fol.process();
	}

}
