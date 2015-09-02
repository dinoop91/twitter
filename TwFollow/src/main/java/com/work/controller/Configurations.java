package com.work.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;



public class Configurations {
	
	private static Configurations _INSTANCE;
	private static String propertyFile = "/data/twitter.properties";
	public static Properties properties = null;
	public static boolean propertyFileAssigned=false;
	FileReader reader=new FileReader();
	
/*	public String CONSUMER_KEY="tImNqeA4fBYOpzN1Xj3UAWZmY";
	public String CONSUMER_KEY_SECRET="k930mX7wGz0UJr4r2drSKSJG6VrTOuAfEiioq35NWSjXXch8RL";
	public String ACCESS_TOKEN="269190737-J9SqFt4mVYN45KAdFL0JzGtWWbE5EKbc4zvp3245";
	public String ACCESS_TOKEN_SECRET="MhEGj1WnnTOke6fbV3vQmSfeMeJPlGqYKU2Qb6V7NVyHU";*/
	
	public String CONSUMER_KEY="";
	public String CONSUMER_KEY_SECRET="";
	public String ACCESS_TOKEN="";
	public String ACCESS_TOKEN_SECRET="";
	
	public String IP="localhost";
	public String PORT="9300";
	public String CLUSTER_NAME = "elasticsearch";
	public String INDEX_NAME = "";
	public String USER_NAME="";

	public String TYPE_NAME = "tweet";

	public  String KEYWORDS_FILE = "/data/keywords";
	public  String LANGUAGE_FILE = "/data/languages";
	public String LOCATION_FILE="/data/location";
	
	private static void readPropertyFile(){
		try{
			properties=new Properties();
			InputStream resource=null;
			if(propertyFileAssigned ==false ){
				System.out.println(properties);
				URL location = Configurations.class.getProtectionDomain().getCodeSource().getLocation();
				String file=location.getFile();
				file = file.substring(0,file.indexOf("/target"));
				propertyFile=file+propertyFile;
				System.out.println("propertyFileAssigned is false, reading from file "+propertyFile);
				resource = new FileInputStream(propertyFile);
			}
			else{
				System.out.println("propertyFileAssigned is true, reading from "+new File(propertyFile).getAbsolutePath());
				resource = new FileInputStream(propertyFile);
			}
			System.out.println(resource);
			properties.load(resource);
		}catch(Exception e){
		}
	}
	private Configurations()
	{
		String CONFIG_FILE=properties.getProperty("CONFIG_FILE");
		System.out.println(CONFIG_FILE);
		if (CONFIG_FILE!=null){
			InputStream resource=null;
			try {
				
				resource = new FileInputStream(CONFIG_FILE);
				properties.load(resource);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		ACCESS_TOKEN=properties.getProperty("ACCESS_TOKEN",ACCESS_TOKEN);
		System.out.println(ACCESS_TOKEN);
		ACCESS_TOKEN_SECRET=properties.getProperty("ACCESS_TOKEN_SECRET",ACCESS_TOKEN_SECRET);
		CONSUMER_KEY=properties.getProperty("CONSUMER_KEY",CONSUMER_KEY);
		CONSUMER_KEY_SECRET=properties.getProperty("CONSUMER_KEY_SECRET",CONSUMER_KEY_SECRET);

		INDEX_NAME=properties.getProperty("INDEX_NAME",INDEX_NAME);
		USER_NAME=properties.getProperty("USER_NAME",USER_NAME);
		System.out.println(INDEX_NAME);
		System.out.println(USER_NAME);
		
		KEYWORDS_FILE=properties.getProperty("KEYWORDS_FILE",reader.getPath(KEYWORDS_FILE));
		LANGUAGE_FILE=properties.getProperty("LANGUAGE_FILE",reader.getPath(LANGUAGE_FILE));
		LOCATION_FILE=properties.getProperty("LOCATION_FILE",reader.getPath(LOCATION_FILE));
	}
	public static Configurations getInstance() {
		if(_INSTANCE==null) {
			System.out.println("_INSTANCE =null, Creating new instance");
			readPropertyFile();
			_INSTANCE = new Configurations();
		}
		return _INSTANCE;    
	}

	public static Configurations getInstance(String propertyFilePath) {
		if(propertyFilePath != null ){
			File f = new File(propertyFilePath);
			if(f.exists() && !f.isDirectory()){
				propertyFile=propertyFilePath;
				propertyFileAssigned=true;
			}
			else{
				System.out.println("file not exist "+f+" reading from default config path ");
			}
		}
		return getInstance();
	}
}
