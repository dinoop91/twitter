package com.work.controller;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import twitter4j.IDs;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;


public class FollowBySearch {
	static FollowProcessor processor=new FollowProcessor();
	static public int count=0;
	int maxCount=950;
	static public ElasticSearch es=new ElasticSearch();
	static Configurations config=Configurations.getInstance();
	static Twitter twitter=null;
	static List<Long> friends=new ArrayList<Long>();
	static List<Long> followers=new ArrayList<Long>();

	private void process() throws TwitterException {
		twitterInstance();
		String s = "Hrithik Roshan,sonamakapoor,asliyoyo";
		//എ,അ,മ,സ,പ
		
		List<String> keywords = new ArrayList<String>(Arrays.asList(s.split(",")));
		
		for(String keyword : keywords){

			List<Status> tweets=search(keyword);
			System.out.println(tweets.size());
			for (Status status : tweets){
				System.out.println("=====");
				System.out.println(status.getText());
				processor.run(status);
			}
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

			processor.twitterInstance(twconfig);

			long cursor = -1;
			IDs ids;
			System.out.println("Listing friends ids.....");
			do {
				ids = twitter.getFriendsIDs(config.USER_NAME, cursor);
				for (long id : ids.getIDs()) {
					friends.add(id);
				}
			} while ((cursor = ids.getNextCursor()) != 0);
			ids=null;
			cursor = -1;
			System.out.println("Listing followers's ids.....");
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
		System.out.println("followers : "+followers.size());
	}

	public List<Status> search(String keyword) {
		List<Status> tweets=new ArrayList<Status>();
		try{
			Query query = new Query(keyword);
			query.setCount(1000);
			try{
				QueryResult qr = twitter.search(query);
				tweets = qr.getTweets();
			}catch(TwitterException tw){
				if(tw.getErrorCode()==88 && tw.getMessage().contains("message - Rate limit exceeded")){
					System.out.println("rate limit exceeded");
				}
				else{
					System.out.println("twitter exception ");
					System.out.println(tw);
				}
			}
		}catch(Exception e){
			System.out.println(e);
		}
		return tweets;
	}

	public static void main(String args[]) throws TwitterException{
		FollowBySearch folw1=new  FollowBySearch();
		folw1.process();
	}

}
