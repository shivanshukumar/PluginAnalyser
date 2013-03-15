package com.shivanshusingh.PluginAnalyser.Staging;



import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import com.shivanshusingh.PluginAnalyser.Analysis.DependencyTracker;
import com.shivanshusingh.PluginAnalyser.Analysis.FeatureAnalyser;


public class DownloadAndStagingRunner {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		

		// getting features from an update  site
		
		
		//  experimenting with the eclipse update manager from command line to get features
		//  http://wiki.eclipse.org/Equinox_p2_Repository_Mirroring
		String eclipseHome="/Users/singhsk/Developer/eclipse_sandbox/";//with the trailing  slash(/).
		String eclipseApp="Eclipse.app/Contents/MacOS/eclipse";
		String equinoxAppName="org.eclipse.equinox.p2.artifact.repository.mirrorApplication";
		String updateSiteURL=
				//"http://download.eclipse.org/technology/dltk/updates/"
				"https://dl-ssl.google.com/android/eclipse/"
				//"http://download.aptana.com/studio3/plugin/install"
				;//without 'site.xml'.
		String destinationDirectory="/Users/singhsk/Developer/eclipse_plugins/testmirror_googleandroid1";//this is there the mirrired site would be available.
		boolean writeModeClean=true, verbose=true, raw=true;
		
		String removeCommand=" rm -r ";boolean eraseOld=true;// this way old   mirror site can be complete removed first.
		
		boolean previousCommandExecutedWithSuccess=true;
		try {
		    // Execute command
			if(eraseOld)
			{
						File f = new File(destinationDirectory);
						if(f.exists())
						{
							String command=removeCommand+" "+destinationDirectory+"/";
							System.out.println("EXECUTING:"+command);
							Process child = Runtime.getRuntime().exec(command);
							try {
								child.waitFor();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							if(0!=child.exitValue())
								previousCommandExecutedWithSuccess=false;
							child.destroy();
						}
			}
			if(!previousCommandExecutedWithSuccess)
			{
				System.err.println("Error deleting old directory, not continuing forward");
				return;
			}
		    String command =eclipseHome + eclipseApp	+
		    		(writeModeClean? " -writeMode clean" : "" )	+
		    		(verbose?" -verbose":"")		+
		    		(raw?" -raw":"")	+
		    		" -nosplash"		+
		    		" -application "+equinoxAppName	+
		    		" -source "+ updateSiteURL		+  
		    		" -destination "  +"file:"+destinationDirectory;
		    
		    System.out.println("EXECUTING:"+command );
		    Process child = Runtime.getRuntime().exec(command);
		    
		    BufferedReader br= new  BufferedReader(new InputStreamReader(child.getInputStream()));
		    int inread;
		    while ((inread = br.read()) != -1)
		    {
		    	System.out.print((char)inread);
		    }
		    br= new  BufferedReader(new InputStreamReader(child.getErrorStream()));
		    while ((inread = br.read()) != -1)
		    {
		    	System.out.print((char)inread);
		    }
		    
		    System.out.println("exitvalue="+child.exitValue());
		    
		    try {
				child.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		    child.destroy();
		    
		    
		    
		    /*    starting  analysis  .... the com.shivanshusingh.PluginAnalyser.Analysis.AnalysisRunner 
		     * can also be used to do the analysis tasks on the specified source /  mirror site.
		     */
		    
		    
		    // now doing the extractions from features. - feature.xml i.e.
//			String  featureFolderPath=	destinationDirectory+  "/features/";
//			FeatureAnalyser.analyseAndRecordAllInformationFromFeautreFolder(featureFolderPath);
			
			// reading all the files (plugin jars) in the specified plugin folder
//			String  pluginFolderPath=	destinationDirectory+  "/plugins/";
//			DependencyTracker.analyseAndRecordAllInformationFromPluginFolder(pluginFolderPath);
		
		    

		} catch (IOException e) {
		}
		
	
	}

}
