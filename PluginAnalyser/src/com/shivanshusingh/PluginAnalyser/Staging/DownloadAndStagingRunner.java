package com.shivanshusingh.PluginAnalyser.Staging;



import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import com.shivanshusingh.PluginAnalyser.Analysis.DependencyTracker;
import com.shivanshusingh.PluginAnalyser.Analysis.FeatureAnalyser;
import com.shivanshusingh.PluginAnalyser.Utils.PluginAnalyserUtils;
import com.shivanshusingh.PluginAnalyser.Utils.Logging.Log;

/**
 *  to download (mirror)  an eclipse repository / update site and run the feature and plugin analysis and data recording tasks.
 * @author Shivanshu Singh
 * 
 */
public class DownloadAndStagingRunner {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		
		//  experimenting with the eclipse update manager from command line to get features
		//  http://wiki.eclipse.org/Equinox_p2_Repository_Mirroring
		String eclipseHome="/Users/singhsk/Developer/eclipse_sandbox/";//with the trailing  slash(/).
		String eclipseApp="Eclipse";
		String equinoxAppName="org.eclipse.equinox.p2.artifact.repository.mirrorApplication";
		String updateSiteURL=
//				"http://download.eclipse.org/technology/dltk/updates/"
				"https://dl-ssl.google.com/android/eclipse/"
//				"https://dl-ssl.google.com/android/eclipse/m/"//erroneous case.
//				"http://download.eclipse.org/releases/juno/"
//				"http://download.aptana.com/studio3/plugin/install"
				;//without 'site.xml'.
		String  mirrorSiteDesinationPathPrefix="/Users/singhsk/Developer/eclipse_plugins/"    ;//with a trailing slash(/)
		String mirrorSiteDesinationName=
				updateSiteURL.replace("/", "-").replace(":", "-").replace(".", "-").replaceAll("-{1,}","-");
				//"testmirror_googleandroid1"    ;
		String destinationDirectory=
				mirrorSiteDesinationPathPrefix+mirrorSiteDesinationName;//this is there the mirrired site would be available.  It automatically gets created by p2.
				//"/Users/singhsk/Developer/eclipse_sandbox";
		try {
		boolean writeModeClean=true
				, verbose=true
				, raw=true 				
				, eraseOld=true;// this way old   mirror site can be complete removed first.
		
		
		String currTimeString=("_OUTPUT_"+mirrorSiteDesinationName+"_"+PluginAnalyserUtils.getCurrentTimeString()  );
		
		if(!Log.startLogger("./"+currTimeString+"/_LOGS"))
		{
			Log.errln(  "Cannot start logging sorry. Please check the path provided or necessary permissions");
		}
		PluginAnalyserUtils.setTEMP_DIR_PATH("./"+currTimeString+"/_TMP");
		// getting features from an update  site
		
		if(!downloadAndStage(eclipseHome, eclipseApp,
				equinoxAppName, updateSiteURL, destinationDirectory,
				writeModeClean, verbose, raw, eraseOld))
		{
			Log.errln("XXXXXXX  \n  Download and staging error for: \n  "+updateSiteURL+"\n    cannot continue  with the analysis and data extraction.\n    ----\n     You may want to check the logs at  :  \n     "+eclipseHome+"configuration/  \n     ---- \nXXXXXXXXX" )    ;
			return;
		}
	    /*    starting  analysis  .... the com.shivanshusingh.PluginAnalyser.Analysis.AnalysisRunner 
	     * can also be used to do the analysis tasks on the specified source /  mirror site.
	     */
	    
		
	    String outputLocation="./"+currTimeString+"/_OUTPUT";
		// now doing the extractions from features. - feature.xml i.e.
		String featureFolderPath = destinationDirectory + "/features/";
		
			FeatureAnalyser.analyseAndRecordAllInformationFromBaseFeautreFolder(featureFolderPath, outputLocation+"/features");
		

		// reading all the files (plugin jars) in the specified plugin folder
		String pluginFolderPath = destinationDirectory + "/plugins/";
		DependencyTracker.analyseAndRecordAllInformationFromBasePluginFolder(pluginFolderPath, outputLocation+"/plugins" );

		} catch (IOException e) {
			Log.err(e.getStackTrace().toString());
			//e.printStackTrace();
		}
	
	}

	/**
	 * @param eclipseHome
	 * @param eclipseApp
	 * @param equinoxAppName
	 * @param updateSiteURL
	 * @param destinationDirectory
	 * @param writeModeClean
	 * @param verbose
	 * @param raw
	 * @param eraseOld
	 * @throws IOException 
	 */
	private static boolean downloadAndStage(String eclipseHome, String eclipseApp, String equinoxAppName,
			String updateSiteURL, String destinationDirectory,
			boolean writeModeClean, boolean verbose, boolean raw,
			boolean eraseOld) throws IOException {
		
			if (eraseOld) {
				File f = new File(destinationDirectory);
				if (f.exists()) {
					Log.outln("DELETING: " + f.getPath());

					if (!PluginAnalyserUtils.deleteFolder(f)) {
						Log.errln("XXXXXXXXX Error deleting old directory, not continuing forward");
						return false;
					}

				}
			}
			
		    String command =eclipseHome + eclipseApp	+
		    		(writeModeClean? " -writeMode clean" : "" )	+
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
				e.printStackTrace();
			}
		    
		    int exitValue=child.exitValue();
		    Log.outln("Download and Staging Process, exitvalue(0=success)="+exitValue);
		    Log.errln("Download and Staging Process, exitvalue(0=success)="+exitValue);
		    
		    
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
		    return true;
		}
	
}
