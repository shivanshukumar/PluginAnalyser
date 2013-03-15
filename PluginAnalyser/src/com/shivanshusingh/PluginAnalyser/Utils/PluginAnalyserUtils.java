package com.shivanshusingh.PluginAnalyser.Utils;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class PluginAnalyserUtils {
	public static boolean deleteFolder(File folder) {
		boolean success=true;
		try{
	    File[] files = folder.listFiles();
	    if(files!=null) { //some JVMs return null for empty dirs
	        for(File f: files) {
	        	
	            if(f.isDirectory()) {
	              success=  deleteFolder(f);
	            } else {
	               success= f.delete();
	            }
	        }
	    }
	   success= folder.delete();
		}
		catch(Exception e)
		{
			success=false;
		}
		
		return success;
	}
	public  static  Set<String>  listFilesForFolder(final File folder) {
	   Set<String> fileSet= new HashSet<String>();
	   
		for (final File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	           fileSet.addAll(listFilesForFolder(fileEntry));
	        } else {
	        	fileSet.add(fileEntry.getAbsolutePath());
	        	//System.out.println(fileEntry.getAbsolutePath());
	        }
	    }
		return fileSet;
	}
}
