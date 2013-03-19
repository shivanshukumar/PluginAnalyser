package com.shivanshusingh.pluginanalyser.staging;



import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;

import com.shivanshusingh.pluginanalyser.utils.PluginAnalyserUtils;
import com.shivanshusingh.pluginanalyser.utils.logging.Log;

/**
 *  to download (mirror)  an eclipse repository / update site .
 * @author Shivanshu Singh
 * 
 */
public class DownloadAndStagingRunner {

	/**
	 * to download (mirror) a  p2 repository / update site . Sets the stage for   analysis later on. an eclipse installation with p2 is required  to execute this.
	 * @param eclipseHome 	the path of eclipse home  with the trailing  slash(/) .
	 * @param eclipseApp 	the name (or the relative  path from the  {@code eclipseHome} path to) the eclipse executable application
	 * @param equinoxAppName 	the name of the equinox application. generally it is  {@code org.eclipse.equinox.p2.artifact.repository.mirrorApplication}
	 * @param updateSiteURLCollection {@link Set}  collection of the URLs of the sites from where the plugins and metadata would be fetched.  This is generally a collection of eclipse  p2 repositories / update sites.  Each of the URLs in the collection must be without 'site.xml' at the end.
	 * @param destinationDirectory 	this is there the mirrored site would be available.  It automatically gets created by  eclipse's equinox p2  application if it does not exist already.
	 * @param verbose
	 * @param raw
	 * @param eraseOld 	true if you want the old destinationDirectory to be erased first. All its contents will be erased first and then the mirroring will be done.
	 * @throws IOException 
	 */
	public static boolean downloadAndStage(String eclipseHome, String eclipseApp, String equinoxAppName,
			Set<String> updateSiteURLCollection, String destinationDirectory,
			boolean verbose, boolean raw,
			boolean eraseOld) throws IOException {
		
			if(null==updateSiteURLCollection  ||  updateSiteURLCollection.size()<1  )
			{
				// if the updatestitecollection has   nothing in it.  return.
				Log.errln("xxxx  The updateSiteUrlCollection specified is null or empty. Cannot download and stage anything.");
				Log.outln("xxxx  The updateSiteUrlCollection specified is null or empty. Cannot download and stage anything.");
				
				return false;
			}
			if (eraseOld) {
				File f = new File(destinationDirectory);
				if (f.exists()) {
					Log.outln("DELETING: " + f.getPath());

					if (!PluginAnalyserUtils.clearFolder(f)) {
						Log.errln("XXXXXXXXX Error deleting old directory, not continuing forward");
						return false;
					}

				}
			}
			
			
		  int sitesDone=0;
		  
		  int totalSites=updateSiteURLCollection.size();
			
			for(String  updateSiteURL:updateSiteURLCollection )
			{
				sitesDone++;
		    String command =eclipseHome + eclipseApp	+
		    		//(writeModeClean? " -writeMode clean" : "" )	+
		    		(verbose?" -verbose":"")		+
		    		(raw?" -raw":"")	+
		    		" -nosplash"		+
		    		" -includeOptional"		+
		    		" -application "+equinoxAppName	+
		    		" -source "+ updateSiteURL		+  
		    		" -destination "  +"file:"+destinationDirectory;
		    
		    Log.outln("EXECUTING: "+command );
		    Process child = Runtime.getRuntime().exec(command);
		    
		    BufferedReader br= new  BufferedReader(new InputStreamReader(child.getInputStream()));
		    int inread;
		    while ((inread = br.read()) != -1)
		    {
		    	Log.out((char)inread);
		    }
		    br.close();
		    br= new  BufferedReader(new InputStreamReader(child.getErrorStream()));
		    while ((inread = br.read()) != -1)
		    {
		    	Log.err((char)inread);
		    }
		    br.close();
		    
		    try {
				child.waitFor();
			} catch (InterruptedException e) {
				Log.err(e.getStackTrace().toString());
			//	e.printStackTrace();
			}
		    
		    int exitValue=child.exitValue();
		    Log.outln("Download and Staging Process  "+sitesDone+" of "+  totalSites  +" , exitvalue(0=success)="+exitValue);
		    Log.errln("Download and Staging Process  "+sitesDone+" of "+  totalSites  +" , exitvalue(0=success)="+exitValue);
		    
		    
		    child.destroy();
		    
		    if(0!=exitValue)
		    {
		    	Log.outln("XXXXXXXX  \n Download and Staging Process for :\n "+  updateSiteURL+"\n could not finish  properly, \n and the mirrored site may not be proper.  \nXXXXXXXXX");
			    Log.errln("XXXXXXXX  \n Download and Staging Process for :\n "+  updateSiteURL+"\n could not finish  properly, \n and the mirrored site may not be proper.  \nXXXXXXXXX");
			    return false;
			    
		    }
		    else
		    {
		    	Log.outln("=========  \n Download and Staging Process  for : \n "+  updateSiteURL+"\n successfully completed \n========");
		    }
			}
		    return true;
		}
	
	/**
	 * to download (mirror) eclipse repositories / update sites along with a base eclipse installation . Sets the stage for analysis later on.   an eclipse installation with p2 is required  to execute this.
	 * @param eclipseHome 	the path of eclipse home  with the trailing  slash(/) .
	 * @param eclipseApp 	the name (or the relative  path from the  {@code eclipseHome} path to) the eclipse executable application
	 * @param equinoxAppName 	the name of the equinox application. generally it is  {@code org.eclipse.equinox.p2.artifact.repository.mirrorApplication}
	 * @param updateSiteURLCollection {@link Set}  collection of the URLs of the sites from where the plugins and metadata would be fetched.  This is generally a collection of eclipse  p2 repositories / update sites.  Each of the URLs in the collection must be without 'site.xml' at the end.
	 * @param destinationDirectory 	this is there the mirrored site would be available.  It automatically gets created by  eclipse's equinox p2  application if it does not exist already.
	 * @param verbose
	 * @param raw
	 * @param eraseOld 	true if you want the old destinationDirectory to be erased first. All its contents will be erased first and then the mirroring will be done.
	 * @param baseEclipseInstallationHome the path to the base eclipse installation  home,   in case this must also be staged  for analysis in addition to   the other p2 repositories / updates sites. The assumption is that all features  and plugins  will be available  in flat directories at  {@link  baseEclipseInstallationHome/features}  and    {@link  baseEclipseInstallationHome/plugins}    respectively.
	 * @throws IOException 
	 */
	public static boolean downloadAndStageEclipseApplication(
			String eclipseHome,
			String eclipseApp,
			String equinoxAppName,
			Set<String> updateSiteURLCollection,
			String destinationDirectory,
			boolean writeModeClean,
			boolean verbose,
			boolean raw,
			String baseEclipseInstallationHome,
			boolean eraseOld
			
			) throws IOException {
		
		return downloadAndStage(eclipseHome, eclipseApp, equinoxAppName, updateSiteURLCollection, destinationDirectory, verbose, raw, eraseOld);
		//TODO:  write the process of copying all the   		baseEclipseInstallationHome    artifacts   to the staging destination directory.
	}
		
	
}
