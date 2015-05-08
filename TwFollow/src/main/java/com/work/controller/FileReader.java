package com.work.controller;


import java.io.BufferedReader;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;




public class FileReader {


	public String getPath(String fileName) {
		System.out.println("input file is "+fileName);
		if(fileName==null) return null;
		URL location = FileReader.class.getProtectionDomain().getCodeSource().getLocation();
		String file=location.getFile();
		//System.out.println("file "+file);
		if(file.contains("target"))
			file = file.substring(0,file.indexOf("/target"));
		else{
			if(file.contains(".jar")){
				file=file.substring(0, file.indexOf(".jar")+4);
				file = file.substring(0, file.lastIndexOf("/"));
			}
		}
		fileName=file+fileName;
		System.out.println("output file is "+fileName);
		return fileName;
	}
	
	public List<String> readList(String path){
		List<String> list=new ArrayList<String>();
		try{
			File file=new File(path);
			BufferedReader reader=new BufferedReader(new java.io.FileReader(file));
			String line=null;
			while((line=reader.readLine())!=null){
				if(!list.contains(line))
					list.add(line);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return list;
	}
}
