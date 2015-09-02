package com.work.controller;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

import twitter4j.FilterQuery;
import twitter4j.JSONException;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;


public class FollowController {

	static Logger logger=null;
	static String twitterPropertyFile = null;
	static Configurations config;
	int i=0;
	static{
		logger=Logger.getLogger(FollowController.class.getName());
		twitterPropertyFile = "/data/twitter.properties";
	}
	static ElasticSearch es=new ElasticSearch();
	Twitter twitter=null;

	public void run(){
		try{
			es.createClient(config.IP,Integer.parseInt(config.PORT), config.CLUSTER_NAME);
			
			if(es.createIndex(config.INDEX_NAME)==false){
				logger.info("creating index failed ,exiting");
				return;
			}
			FollowProcessor.es=es;
			
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

			Configuration config=cb.build();
			FollowProcessor.twitterInstance(config);
			
			TwitterStream twitterStream = new TwitterStreamFactory(config).getInstance();

			StatusListener listener = new StatusListener() {

				public void onException(Exception arg0) {
					logger.info("on exception "+arg0.getMessage());
				}

				public void onDeletionNotice(StatusDeletionNotice arg0) {
					logger.info("onDeletionNotice "+arg0);
				}

				public void onScrubGeo(long arg0, long arg1) {
					logger.info("onScrubGeo "+arg1);
				}

				public void onStatus(Status status) {
					FollowProcessor process=new FollowProcessor();
					int val=process.run(status);
					if(val==0){
						System.out.println("max limit");
						try {
							Thread.sleep(86400*1000);
							FollowProcessor.count=0;
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

				public void onTrackLimitationNotice(int arg0) {
					logger.info("onTrackLimitationNotice "+arg0);

				}

				public void onStallWarning(StallWarning warning) {
					logger.info("onStallWarning "+warning.getMessage());
				}

			};

			TwitterFactory tf = new TwitterFactory(config);
			twitter = tf.getInstance();
			ReadLists read=new ReadLists();
			String keywords[] = read.readKeywords();
			//keywords[0]="a";
			
			String languages[] = read.readLanguages();
			//keywords[2]="SRK12Million";
			FilterQuery fq = new FilterQuery();
			fq.track(keywords).language(languages).locations(read.readLocations());
			twitterStream.addListener(listener);
			twitterStream.filter(fq);
		}catch(Exception e){
			e.printStackTrace();
			
		}
	}


	public static void main(String []args) throws IOException, JSONException, TwitterException{
		if(args.length>0){
			twitterPropertyFile=args[0];
		}
		else{
			URL location = FollowController.class.getProtectionDomain().getCodeSource().getLocation();
			String file=location.getFile();
			if(file.contains("target"))
				file = file.substring(0,file.indexOf("/target"));
			twitterPropertyFile=file+twitterPropertyFile;
		}
		logger.info("property file : "+twitterPropertyFile);
		config=Configurations.getInstance(twitterPropertyFile);
		FollowController controller = new FollowController();
		controller.run();
	}
}
