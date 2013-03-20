package com.shivanshusingh.pluginanalyser.job;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.shivanshusingh.pluginanalyser.analysis.AnalysisRunner;
import com.shivanshusingh.pluginanalyser.comparison.DependencyFinder;
import com.shivanshusingh.pluginanalyser.staging.DownloadAndStagingRunner;
import com.shivanshusingh.pluginanalyser.utils.Util;
import com.shivanshusingh.pluginanalyser.utils.logging.Log;

/**
 * @author Shivanshu Singh
 * 
 */
public class PluginAnalyserJobRunner {

	public void run() {
		long time1 = System.currentTimeMillis();

		long stage1 = System.currentTimeMillis();
		String eclipseHome = "/Users/singhsk/Developer/eclipse_sandbox";// without
																		// the
																		// trailing
																		// slash(/).
		String eclipseApp = "Eclipse";
		String equinoxAppName = "org.eclipse.equinox.p2.artifact.repository.mirrorApplication";

		// all update sites are added to the update site collection from
		// which all artifacts will be downloaded.
		// each update site parameter should be the URL to the eclipse product
		// update site without the 'site.xml' at end.
		Set<String> updateSiteURLCollection = new HashSet<String>();

		updateSiteURLCollection.add("http://download.eclipse.org/technology/dltk/updates/");
		updateSiteURLCollection.add("https://dl-ssl.google.com/android/eclipse");
		updateSiteURLCollection.add("https://dl-ssl.google.com/android/eclipse/m/");// erroneous
																					// case.
		// updateSiteURLCollection.add("http://download.aptana.com/studio3/plugin/install");
		updateSiteURLCollection.add("http://download.eclipse.org/egit/github/updates");
		updateSiteURLCollection.add("http://download.eclipse.org/technology/m2e/releases/1.3/1.3.1.20130219-1424");
		/*
		 * // add an eclipse site to club plugins with a base eclipse product.
		 * updateSiteURLCollection
		 * .add("http://download.eclipse.org/releases/juno/");
		 */
		String mirrorSiteDesinationPathPrefix = "/Users/singhsk/Developer/eclipse_plugins";

		String mirrorSiteDesinationName = "new_site" + Util.getCurrentTimeString();
		mirrorSiteDesinationName = mirrorSiteDesinationName.replace("/", "-").replace(":", "-").replace(".", "-").replaceAll("-{1,}", "-");

		// this is there the mirrored site would be available. It automatically
		// gets created by p2.
		String destinationDirectory = mirrorSiteDesinationPathPrefix + "/" + mirrorSiteDesinationName;
		try {
			boolean verbose = false, raw = true;

			// this way old mirror site can be complete removed first.
			boolean eraseOld = true;

			String currOutpurDir = "_OUTPUT_" + mirrorSiteDesinationName
			// + "_" + Util.getCurrentTimeString()
			;

			if (!Log.startLogger("./" + currOutpurDir + "/_LOGS")) {
				Log.errln("Cannot start logging sorry. Please check the path provided or necessary permissions");
			}
			Util.setTEMP_DIR_PATH("./" + currOutpurDir + "/_TMP");
			// getting features from an update site
			String baseEclipseInstallationHome = eclipseHome;

			if (!DownloadAndStagingRunner.downloadAndStageWithEclipseInstallation(eclipseHome, eclipseApp, equinoxAppName, baseEclipseInstallationHome,
					updateSiteURLCollection, destinationDirectory, verbose, raw, eraseOld))

			/*
			 * if (!DownloadAndStagingRunner.downloadAndStage(eclipseHome,
			 * eclipseApp, equinoxAppName, updateSiteURLCollection,
			 * destinationDirectory, verbose, raw, eraseOld))
			 */
			{
				Log.errln("XXXXXXX  " + "\n  Download and staging error for: " + "\n  " + updateSiteURLCollection
						+ "\n    cannot continue  with the analysis and data extraction." + "\n    ----" + "\n     You may want to check the logs at  :  "
						+ "\n     " + eclipseHome + "configuration/  " + "\n    ---- " + "\nXXXXXXX");
				Log.errln("But still  any  way   going on with the analysis for now ..............");
			}
			long stage2 = System.currentTimeMillis();
			Log.outln("Downloading and Staging for site configuration at  :  " + destinationDirectory + "  time: " + Util.getFormattedTime(stage2 - stage1));
			Log.errln("Downloading and Staging for site configuration at  :  " + destinationDirectory + "  time: " + Util.getFormattedTime(stage2 - stage1));

			/*
			 * starting analysis ....
			 */

			String outputLocation = "./" + currOutpurDir + "/_OUTPUT";
			AnalysisRunner.analyseAndRecord(destinationDirectory, outputLocation, true);

			// ////////////////////////////////////////////////////// 
			// the dependency finder.

			// currOutpurDir="_OUTPUT_new_site2013-03-19-16-33-07-731";
			outputLocation = "./" + currOutpurDir + "/_OUTPUT";

			String pluginExtractsLocation = outputLocation + "/plugins";
			String pluginextractAnalysisDestLocation = outputLocation + "/_DEPENDENCY_SET"; // without
																							// the
																							// trailing
																							// slash(/)
			DependencyFinder.buildPluginDependencySuperSet(pluginExtractsLocation, pluginextractAnalysisDestLocation, true);
			// ///////////////////////////////////////////////////////////////////////////////////////////////
			long time2 = System.currentTimeMillis();
			Log.outln("Current Job for site configuration at  :  " + destinationDirectory + "  time: " + Util.getFormattedTime(time2 - time1));
			Log.errln("Current Job for site configuration at  :  " + destinationDirectory + "  time: " + Util.getFormattedTime(time2 - time1));

		} catch (Exception e) {

			Log.err(e.getStackTrace().toString());
			e.printStackTrace();
		}

	}

}
