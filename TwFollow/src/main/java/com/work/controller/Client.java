package com.work.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import twitter4j.JSONObject;

public class Client {
    private HttpPost post;
    public Client(){
       post = new HttpPost("http://factweavers.com:8080/find/");
   }
   public String getGender(String name){
	   String data=null;
	   try{
          List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
           nameValuePairs.add(new BasicNameValuePair("name", name));
           post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
           HttpClient client = new DefaultHttpClient();      
           HttpResponse response = client.execute(post);
           BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
           String line = "";
           while ((line = rd.readLine()) != null) {
               data=line;
           }
           JSONObject json=new JSONObject(data);
           return json.getString("gender");
	   }catch(Exception e){
		   e.printStackTrace();
	   }
           return data;
   }
   public static void main(String[] args) throws URISyntaxException{
        Client client = new Client();
        String result=client.getGender("dinoop");
        System.out.println(result);
    }
}