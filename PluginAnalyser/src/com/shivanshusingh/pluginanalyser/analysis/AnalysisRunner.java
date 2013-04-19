package com.shivanshusingh.pluginanalyser.analysis;

import java.io.File;
import java.io.IOException;

import com.shivanshusingh.pluginanalyser.utils.Util;

/**
 * to start the analysis and recording process for featues and plugins of
 * product/configuration mirror site.
 * 
 * @author Shivanshu Singh
 * 
 */
public class AnalysisRunner {
	/**
	 * starts the analysis and recording process for a given features / plugins
	 * folder. The naming convention is this: mirrorSiteLocation/features/
	 * contains all the feature related jars and dirs and
	 * mirrorSiteLocation/plugins/ contains all the plugin jars and dirs that
	 * need to be analysed / deployed together.
	 * 
	 * @author Shivanshu Singh
	 * 
	 * @param mirrorSiteLocation
	 * @param outputLocation
	 * 
	 * @param eraseOld
	 * @param pathToJavaClasses 
	 * @throws IOException
	 */
	public static void analyseAndRecord(String mirrorSiteLocation,
			String outputLocation, String pathToJavaClasses, boolean eraseOld) throws IOException {
		
		if(eraseOld)
		{
			Util.checkAndCreateDirectory(outputLocation);
			Util.clearFolder(new File(outputLocation));
		}
		// now doing the extractions from features. - feature.xml i.e.
		String featureFolderPath = mirrorSiteLocation + "/features/";

		FeatureAnalyser.analyseAndRecordAllInformationFromBaseFeautreFolder(
				featureFolderPath, outputLocation + "/features", eraseOld);

		// reading all the files (plugin jars) in the specified plugin folder
		String pluginFolderPath = mirrorSiteLocation + "/plugins/";
		BundleAnalyser.analyseAndRecordAllInformationFromBasePluginFolder(
				pluginFolderPath, outputLocation + "/plugins", pathToJavaClasses, eraseOld);
	}

}
