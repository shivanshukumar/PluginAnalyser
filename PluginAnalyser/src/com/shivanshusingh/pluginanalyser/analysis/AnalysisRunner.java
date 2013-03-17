package com.shivanshusingh.pluginanalyser.analysis;

import java.io.IOException;
import java.sql.Date;
import java.util.Calendar;

import com.shivanshusingh.pluginanalyser.utils.PluginAnalyserUtils;
import com.shivanshusingh.pluginanalyser.utils.logging.Log;

/**
 * to start the analysis and recording process for featues and plugins of
 * product/configuration mirror site.
 * 
 * @author singhsk
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
	 * @throws IOException
	 */
	public static void analyseAndRecord(String mirrorSiteLocation,
			String outputLocation) throws IOException {
		// now doing the extractions from features. - feature.xml i.e.
		String featureFolderPath = mirrorSiteLocation + "/features/";

		FeatureAnalyser.analyseAndRecordAllInformationFromBaseFeautreFolder(
				featureFolderPath, outputLocation + "/features");

		// reading all the files (plugin jars) in the specified plugin folder
		String pluginFolderPath = mirrorSiteLocation + "/plugins/";
		DependencyTracker.analyseAndRecordAllInformationFromBasePluginFolder(
				pluginFolderPath, outputLocation + "/plugins");
	}

}
