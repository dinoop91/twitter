package com.algotree.unfollow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.mapper.MapperParsingException;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import twitter4j.JSONObject;


public class ElasticSearch {



	public static Client client = null;
	public static Map<String,String> mapping=null;
	Logger logger = Logger.getLogger(ElasticSearch.class.getName());


	public Client createClient(String ip, int port, String cluster){
		logger.info("creating new client : "+ip+"  "+port+"  "+cluster);
		Settings settings = ImmutableSettings.settingsBuilder()
				.put("client.transport.sniff", true)
				.put("cluster.name",cluster)
				.put("node.client",true)
				.build();
		client = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress(ip,port));
		return client;
	}

	public Client getClient(){
		return client;
	}

	public boolean isIndexExist(String index) {
		try{
			return client.admin().indices().prepareExists(index).execute().actionGet().isExists();
		}catch(Exception e){
			return false;
		}
	}		
	public boolean createIndex(String index) {
		try{
			if(isIndexExist(index) == false){
				client.admin().indices().prepareCreate(index).execute().actionGet();
				logger.info("created index "+index);
			}
			return true;
		}catch(Exception e){
			return false;
		}
	}
	public boolean createIndex(String index,String type,String mapping){
		try{
			if(mapping==null || mapping=="")
				createIndex(index);
			if(isIndexExist(index) == false){
				client.admin().indices().prepareCreate(index).addMapping(type, mapping).execute().actionGet();
				logger.info("created index "+index+" with mapping: "+mapping);
			}
			return true;
		}catch(Exception e){
			return false;
		}
	}
	public boolean indexDocument(String index, String type,JSONObject data,String id){
		try{
			client.prepareIndex(index,type,id)
			.setSource(data.toString())
			.setRefresh(true)
			.execute()
			.actionGet();
			return true;
		}catch(MapperParsingException map){
		}catch(Exception e){
		}
		return false;
	}
	
	public boolean update(String index,String type,String id, JSONObject data) {
		try{
			client.prepareUpdate(index,type, id)
			.setDocAsUpsert(true)
			.setRefresh(true)
			.setDoc(data.toString())
			.execute()
			.actionGet();	
			return true;
		}
		catch(MapperParsingException map){
		}catch(Exception e){
		}
		return false;
	}
	public List<Map<String, Object>> getAllDocs(String index,String type,String query){
        int scrollSize = 1000;
        List<Map<String,Object>> esData = new ArrayList<Map<String,Object>>();
        SearchResponse response = null;
        int i = 0;
        while( response == null || response.getHits().hits().length != 0){
            response = client.prepareSearch(index)
                    .setTypes(type)
                       .setQuery(query)
                       .setSize(scrollSize)
                       .setFrom(i * scrollSize)
                    .execute()
                    .actionGet();
            for(SearchHit hit : response.getHits()){
                esData.add(hit.getSource());
            }
            i++;
        }
        return esData;
}
	public Map<String, String> getMapping(String index,String type){
		try{
			ClusterState cs = client.admin().cluster().prepareState().setIndices(index).execute().actionGet().getState();
			IndexMetaData imd = cs.getMetaData().index(index);
			MappingMetaData mdd = imd.mapping(type);
			Map<String, Object> map=(Map<String, Object>) mdd.getSourceAsMap().get("properties");
			mapping=new HashMap<String, String>();
			for(String key : map.keySet()){
				Map<String,String> datatype=(Map<String, String>) map.get(key);
				mapping.put(key, datatype.get("type"));
			}
			return mapping;
		}catch(Exception e){
			return null;
		}
	}
	public Map<String,Object> getDocument(String index, String type,String id){
		try{
			GetResponse response = client.prepareGet(index, type, id).execute().actionGet();
			return response.getSource();
		}catch(Exception e){
			return null;
		}
	}
	



}
