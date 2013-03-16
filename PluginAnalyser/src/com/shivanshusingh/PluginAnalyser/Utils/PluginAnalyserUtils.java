package com.shivanshusingh.PluginAnalyser.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class PluginAnalyserUtils {
	
	private static String TEMP_DIR_PATH=".";
	
	public static String getTEMP_DIR_PATH() {
		return TEMP_DIR_PATH;
	}
	public static boolean setTEMP_DIR_PATH(String path) {
		if(checkAndCreateDirectory(path))
		{
			TEMP_DIR_PATH = path;
			return   true;
		}
		return false;
		
	}
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
	
	public static boolean startLogger(String path)
	{
		if(!checkAndCreateDirectory(path)  )  
			return false;
		path=(path+"/").replaceAll("//", "/");
		try
		{
			FileOutputStream fout= new FileOutputStream(path+"_"+"stdout.log");
			FileOutputStream ferr= new FileOutputStream(path+"_"+"stderr.log");
			
			MultiOutputStream multiOut= new MultiOutputStream(fout  );// , System.out);
			MultiOutputStream multiErr= new MultiOutputStream(ferr  );//  , System.err);
			
			PrintStream stdout= new PrintStream(multiOut);
			PrintStream stderr= new PrintStream(multiErr);
			
			System.setOut(stdout);
			System.setErr(stderr);
		}
		catch (FileNotFoundException ex)
		{
			//Could not create/open the file
		}
		return true;
	}
	/**
	 * @param path
	 */
	public static boolean checkAndCreateDirectory(String path) {
		File f=new File(path);
		if(!f.exists())
		{
			// create  the directory and all parent directories if needed.
			boolean success=f.mkdirs();
			if(!success)
				return success;
		}
		else if(!f.isDirectory())
		{
			return false;
		}
		
		return true;
	}
	public static String getCurrentTimeString() {
		java.util.Date dt = new java.util.Date();
	
		java.text.SimpleDateFormat sdf = 
		     new java.text.SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSSS");
	
		String currentTime = sdf.format(dt);
		return currentTime;
	}
	
	
}
