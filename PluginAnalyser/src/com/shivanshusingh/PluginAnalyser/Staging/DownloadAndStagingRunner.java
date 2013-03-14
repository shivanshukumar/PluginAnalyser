package com.shivanshusingh.PluginAnalyser.Staging;



import java.io.IOException;


public class DownloadAndStagingRunner {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		

		// getting features from an update  site
		
		
		//  experimenting with the eclipse update manager from command line to get features
		
		String eclipseHome="/Users/singhsk/Developer/eclipse_sandbox/";//with the trailing  slash(/).
		String eclipseAppName="Eclipse";
		String equinoxAppName="org.eclipse.equinox.p2.artifact.repository.mirrorApplication";
		String updateSiteURL="https://dl-ssl.google.com/android/eclipse/";//without 'site.xml'.
		String destinationDirectory="/Users/singhsk/Developer/eclipse_plugins/testmirror_googleadt";
		
		try {
		    // Execute command
		    String command = eclipseHome+eclipseAppName  +  " -application "+equinoxAppName+" -source "+ updateSiteURL+  " -destination "  +"file:"+destinationDirectory;		
		    System.out.println("EXECUTING:"+command );
		    Process child = Runtime.getRuntime().exec(command);

		} catch (IOException e) {
		}
		
	
	}

}
