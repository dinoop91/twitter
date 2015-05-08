package com.work.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReadLists {
	Configurations config=Configurations.getInstance();
	
	public String[] readKeywords() {
		try{
			List<String> keywordList=new ArrayList<String>();
			File file=new File(config.KEYWORDS_FILE);
			BufferedReader reader=new BufferedReader(new FileReader(file));
			String line=null;
			while((line=reader.readLine())!=null){
				System.out.println(line);
				if(!keywordList.contains(line))
					keywordList.add(line);
			}
			String keywords[] = keywordList.toArray(new String[keywordList.size()]);
			return keywords;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	public double[][] readLocations(){
		try{
			File file=new File(config.LOCATION_FILE);
			BufferedReader reader=new BufferedReader(new FileReader(file));
			String data=null;
			while((data=reader.readLine())!=null){
				String[] rows =data.split("&");

				String[][] matrix = new String[rows.length][]; 
				int r = 0;
				for (String row : rows) {
					matrix[r++] = row.trim().split(",");
				}

				int tableStringLength=matrix.length;
				double[][] tableDouble = new double[tableStringLength][tableStringLength];

				for(int i=0; i<tableStringLength; i++) {
					for(int j=0; j<tableStringLength; j++) {
						tableDouble[i][j]= Double.valueOf(matrix[i][j]);	      
					}
				}
				System.out.println(Arrays.deepToString(matrix));
				System.out.println(Arrays.deepToString(tableDouble));
				return tableDouble;
			}
			return null;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	public String[] readLanguages() {
		try{
			List<String> languageList=new ArrayList<String>();
			File file=new File(config.LANGUAGE_FILE);
			BufferedReader reader=new BufferedReader(new FileReader(file));
			String line=null;
			while((line=reader.readLine())!=null){
				System.out.println(line);
				if(!languageList.contains(line))
					languageList.add(line);
			}
			String languages[] = languageList.toArray(new String[languageList.size()]);
			return languages;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

}
