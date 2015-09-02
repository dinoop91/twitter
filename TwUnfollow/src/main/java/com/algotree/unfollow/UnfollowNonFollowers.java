package com.algotree.unfollow;

import java.util.ArrayList;
import java.util.List;

import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class UnfollowNonFollowers {
	
	public void unfollowNonActives(){
		
	}
	
	
	public static void main(String args[]) throws TwitterException{
		Configurations config=Configurations.getInstance();

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
		Twitter twitter = tf.getInstance();

		List<Long> followers=new ArrayList<Long>();
		long cursor = -1;
		IDs ids;
		System.out.println("Listing followers's ids.");
		do {
			System.out.println(config.USER_NAME);
			ids = twitter.getFollowersIDs(config.USER_NAME, cursor);
			for (long id : ids.getIDs()) {
				followers.add(id);
			}
		} while ((cursor = ids.getNextCursor()) != 0);

		List<Long> friends=new ArrayList<Long>();
		long cursor1 = -1;
		IDs ids1;
		System.out.println("Listing friends ids.");
		do {
			ids1 = twitter.getFriendsIDs(config.USER_NAME, cursor1);
			for (long id : ids1.getIDs()) {
				friends.add(id);
			}
		} while ((cursor1 = ids.getNextCursor()) != 0);

		System.out.println("friends "+friends.size());
		System.out.println("followers "+followers.size());

		for(Long friend : friends){
			if(followers.contains(friend)){
				System.out.println("is frnd");
			}
			else{
				try{
					System.out.println("unfollowing "+friend);
					twitter.destroyFriendship(friend);
				}catch(Exception e){
					e.printStackTrace();
					System.out.println("error unfollowing "+friend);
				}
			}
		}

	}
}
