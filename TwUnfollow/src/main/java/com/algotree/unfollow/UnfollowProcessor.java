package com.algotree.unfollow;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


import java.util.List;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.joda.time.DateTime;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import twitter4j.IDs;
import twitter4j.JSONObject;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class UnfollowProcessor {
	Configurations config=Configurations.getInstance();
	Twitter twitter=null;

	public void run() throws TwitterException{
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

		List<Long> followers=new ArrayList<Long>();
		long cursor = -1;
		IDs ids;
		System.out.println("Listing followers's ids.");
		do {
			ids = twitter.getFollowersIDs("Dinoop_nair", cursor);
			for (long id : ids.getIDs()) {
				followers.add(id);
				// User user = twitter.showUser(id);
				// System.out.println(user.getName());
			}
		} while ((cursor = ids.getNextCursor()) != 0);

		System.out.println("followers : "+followers.size());
		ElasticSearch es=new ElasticSearch();
		//System.exit(0);
		Client client=es.createClient(config.IP,Integer.parseInt(config.PORT), config.CLUSTER_NAME);


		try{
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			Date date = new Date();
			Date daysAgo = new DateTime(date).minusDays(1).toDate();
			SearchResponse response =null;
			System.out.println(dateFormat.format(daysAgo));
			response = client.prepareSearch(config.INDEX_NAME)
					.setTypes(config.TYPE_NAME)
					.setSize(10000)
					.setQuery(QueryBuilders.matchPhraseQuery("processed", "false"))
					.setPostFilter(
							FilterBuilders.rangeFilter("time").to(dateFormat.format(daysAgo))
							)
							.execute()
							.actionGet();
			System.out.println("hits "+response.getHits().totalHits());
			for(SearchHit hit : response.getHits()){
				Long id = Long.parseLong( hit.getSource().get("author").toString());
				boolean processed=Boolean.valueOf(hit.getSource().get("processed").toString());
				if(processed==true){
					System.out.println("processed");
					continue;
				}
				if(followers.contains(id)){
					System.out.println("is my follower----------- "+id);
				}
				else{
					unfollow(id);
				}
				JSONObject updated=new JSONObject();
				updated.put("processed", true);
				es.update(config.INDEX_NAME, config.TYPE_NAME, String.valueOf(id), updated);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public void unfollow(long id){
		try {
			twitter.destroyFriendship(id);
			System.out.println("unfollowing "+id);
		} catch (TwitterException e) {
			e.printStackTrace();
		}

	}

	public static void main(String args[]) throws TwitterException{
		UnfollowProcessor unf=new UnfollowProcessor();
		unf.run();
	}
}
