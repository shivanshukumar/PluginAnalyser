/**
 * 
 */
package com.shivanshusingh.pluginanalyser.comparison;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import com.shivanshusingh.pluginanalyser.analysis.BundleInformation;
import com.shivanshusingh.pluginanalyser.utils.Util;
import com.shivanshusingh.pluginanalyser.utils.logging.Log;

/**
 * @author Shivanshu Singh
 * 
 */
public class DependencyFinder {

	Map<String,ImpExp> functions=new HashMap<String,ImpExp>();
	Map<String,ImpExp> types=new HashMap<String,ImpExp>();
	
	/**
	 * @param pathToPluginExtractDir
	 * @param pathToDependencyAnalysisDestinationDir
	 * @param eraseOld
	 */
	public  static void buildPluginDependencySuperSet(String pathToPluginExtractDir, String pathToDependencyAnalysisDestinationDir, boolean eraseOld)
	{
		
		
		
		File pluginExtractDirectory= new File(pathToPluginExtractDir);
		
//		if(!Util.checkDirectory(pluginExtractDirectory,true,true,true,false))
//		{
//			Log.errln("xxxx in buildPluginDependencySuperSet \\\n the plugin dir: "+pluginExtractDirectory.getAbsolutePath()+"\n is not a directory or not readable or   does not exist. \nxxxx");
//			return;
//		}
		File[] entries=pluginExtractDirectory.listFiles();
		for (File entry: entries)
		{
			/**
			 *  this is the name of the PluginExtract file, without the  extension. 
			 *  this  is for obtaining the fully qualified plugin name with version and qualifier, even if that information  is not in the pluginExtract file,  cause because of an originally missing manifest.mf file for thus plugin.
			 */  
			if(Util.checkFile(entry, true, true, true, false))
			{
				String thisPluginExtractName=entry.getName().trim();
				int startingIndex=thisPluginExtractName.indexOf("PLUGIN-EXTRACT-")+"PLUGIN-EXTRACT-".length();
				thisPluginExtractName=thisPluginExtractName.substring(startingIndex);
				if(thisPluginExtractName.toLowerCase().endsWith(".txt"))
					thisPluginExtractName=thisPluginExtractName.substring(0,thisPluginExtractName.length()-4)    ;
				System.out.println(thisPluginExtractName)    ;
				
				//TODO:  write out the whole process for restoring information from  plugin extracts and feature extracts  and then convert that into a list of   <element,  importers,  exporters  > tuples.  2013-03-19 1:50 PM EDT.
				

				
				BundleInformation bundleInformation = restoreBundleInformation(entry);
			}
		}
	}

	private  static BundleInformation restoreBundleInformation(File entry) {
		// TODO Auto-generated method stub
		return null;
	}
}


class ImpExp
{
	Set<String> imp=new HashSet<String>();
	Set<String> exp=new HashSet<String>();
}
