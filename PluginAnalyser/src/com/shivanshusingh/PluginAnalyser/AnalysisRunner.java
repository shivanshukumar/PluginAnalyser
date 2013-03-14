package com.shivanshusingh.PluginAnalyser;

import java.io.IOException;
import java.io.OutputStream;

public class AnalysisRunner {

	
	/**
	 * @param args
	 */
	public static void main(final String[] args) throws IOException {
		// "/Users/singhsk/Developer/eclipse plugins/com.objfac.ant.preprocess_0.9.1/preprocessor.jar";
		// "/Users/singhsk/Developer/eclipse plugins/com.objfac.ant.preprocess_0.9.1.zip";
		// "/Users/singhsk/Developer/asm-bytecode-analysis-framework/asm-4.1/lib/all/asm-all-4.1.jar";
		// "/Users/singhsk/Developer/voldemort-0.96/dist/voldemort-0.96.jar" ;
		// "/Users/singhsk/Developer/hadoop-1.0.4/hadoop-core-1.0.4.jar";
		// "../exports/plugins/com.shivanshu.eclipse.contributor_1.0.0.201210111512.jar";
		// "/Users/singhsk/Developer/Arcmexer/arcmexer.jar";
		// "/Users/singhsk/Developer/AndroidEclipseBundleforMac/adt-bundle-mac-x86_64.zip";

		
		
		// getting features from an update  site
		
		
		//  experimenting with the eclipse update manager from command line to get features
		
	/*	String eclipseHome="/Users/singhsk/Developer/eclipse_sandbox/";//with the trailing  slash(/).
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
		
	*/		
		String destinationDirectory="/Users/singhsk/Developer/eclipse_plugins/testmirror_ganymede";
		
		// setting up the stage for all plugin analysis for the given feature
		//  iterate for all included features if necessary.
		
		
		String  pluginFolderPath=
				
				destinationDirectory+  "/plugins/";
				//"/Users/singhsk/Developer/voldemort-0.96/dist/";
				//"/Users/singhsk/Developer/AndroidEclipseBundleforMac/adt-bundle-mac-x86_64/eclipse/plugins/";
		// reading all the files (plugin jars) in the specified plugin folder
		DependencyTracker.analyseAndRecordAllInformationFromPluginFolder(pluginFolderPath);
	
		

	}

}
