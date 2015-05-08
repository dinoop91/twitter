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
import twitter4j.User;
import twitter4j.conf.Configuration;

public class FollowProcessor {
	
	static public int count=0;
	int maxCount=950;
	static public ElasticSearch es=new ElasticSearch();
	Configurations config=Configurations.getInstance();
	static Twitter twitter=null;
	static List<Long> friends=new ArrayList<Long>();


	public static void twitterInstance(Configuration twconfig){
		try{
			TwitterFactory tf = new TwitterFactory(twconfig);
			twitter = tf.getInstance();

			long cursor = -1;
			IDs ids;
			System.out.println("Listing followers's ids.");
			do {
				ids = twitter.getFriendsIDs("Dinoop_nair", cursor);
				for (long id : ids.getIDs()) {
					friends.add(id);
				}
			} while ((cursor = ids.getNextCursor()) != 0);
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("friends : "+friends.size());
	}

	public int run(Status status) {
		try{
			System.out.println(status.getUser().getScreenName()+"                  "+status.getText());
			if(status.getUser().getFollowersCount() > 2000){
				System.out.println("followers "+status.getUser().getFollowersCount());
				return 1;
			}

			long idLong=status.getUser().getId();
			JSONObject json=new JSONObject();
			json.put("time", getTime());
			json.put("author", idLong);
			json.put("name", status.getUser().getScreenName());
			json.put("processed",false);
			String id=String.valueOf(status.getUser().getId());
			Map<String, Object> doc=es.getDocument(config.INDEX_NAME,config.TYPE_NAME ,id);

			if(doc==null){
				if(friends.contains(idLong)){
					System.out.println("already a friend");
				}
				else{
					count++;
					System.out.println("count---------"+count);
					if(count>=maxCount){
						System.out.println("max");
						return 0;
					}
					//es.indexDocument(config.INDEX_NAME,config.TYPE_NAME,json,id);
					//follow(idLong);
				}
			}else{
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String getTime(){
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		f.setTimeZone(TimeZone.getTimeZone("GMT"));
		return f.format(new Date());
	}

}
