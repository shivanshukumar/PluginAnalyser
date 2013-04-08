package com.shivanshusingh.pluginanalyser.job;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.shivanshusingh.pluginanalyser.analysis.AnalysisRunner;
import com.shivanshusingh.pluginanalyser.comparison.DependencyFinder;
import com.shivanshusingh.pluginanalyser.eclipse.marketplace.crawling.EclipseMarketplaceCrawler;
import com.shivanshusingh.pluginanalyser.staging.DownloadAndStagingRunner;
import com.shivanshusingh.pluginanalyser.staging.crawling.SourceCrawler;
import com.shivanshusingh.pluginanalyser.staging.crawling.UpdateSiteInfo;
import com.shivanshusingh.pluginanalyser.utils.Util;
import com.shivanshusingh.pluginanalyser.utils.logging.Log;

/**
 * 
 * runs a complete job of getting update site URLs, downloading and staging,
 * analysis and extract generation of the plugin and feature meta data and then
 * dependency analysis and extract generation.
 * 
 * @author Shivanshu Singh
 * 
 */
public class PluginAnalyserJobRunner {

	public void run() {
		long time1 = System.currentTimeMillis();

		long stage1 = System.currentTimeMillis();

		String mirrorSiteDesinationPathPrefix = "/Users/singhsk/Developer/eclipse_plugins";

		String mirrorSiteDesinationName =
//		 "new_site2_____";
//		 "new_site3_____";
//		"__WorkingEclipseSite_features_and_plugins__";
//		 "_plugin_sandbox";
				"_Base_Eclipse_TestingVersion";
//		 "eclipse_juno_p2_mirror_site";
		// + Util.getCurrentTimeString();

		mirrorSiteDesinationName = mirrorSiteDesinationName.replace("/", "-").replace(":", "-").replace(".", "-")
				.replaceAll("-{1,}", "-");
		String currOutpurDir = "_OUTPUT_" + mirrorSiteDesinationName
		// + "_" + Util.getCurrentTimeString()
		;

		if (!Log.startLogger("./" + currOutpurDir + "/_LOGS")) {
			Log.errln("Cannot start logging sorry. Please check the path provided or necessary permissions");
		}
		Util.setTEMP_DIR_PATH("./" + currOutpurDir + "/_TMP");

		// ///////////////////////////////////////////////////
		// //// getting the update site set from eclipse marketplace
		SourceCrawler ec = new EclipseMarketplaceCrawler();
		// SourceCrawler ec=new EclipseMarketplaceCrawler(0,
		// 1000,"./_UPDATESITEDATA");
		// ec.crawl();
		Set<UpdateSiteInfo> gotUpdateSites = new HashSet();
		// gotUpdateSites.addAll(ec.getSites());
		// //////////////////////////////////////////////////////

		// without the trailing slash(/).
		String eclipseHome =
		// "/Users/singhsk/Developer/eclipse";
		"/Users/singhsk/Developer/eclipse_sandbox";

		String eclipseApp = "Eclipse";
		String equinoxAppName = "org.eclipse.equinox.p2.artifact.repository.mirrorApplication";

		// all update sites are added to the update site collection from
		// which all artifacts will be downloaded.
		// each update site parameter should be the URL to the eclipse product
		// update site without the 'site.xml' at end.
		Set<String> updateSiteCollection = new HashSet<String>();

		updateSiteCollection.add("http://download.eclipse.org/technology/dltk/updates/");
		updateSiteCollection.add("https://dl-ssl.google.com/android/eclipse");
		/*
		 * updateSiteURLCollection.add(
		 * "https://dl-ssl.google.com/android/eclipse/m/");// erroneous case.
		 */
		// updateSiteCollection.add("http://download.aptana.com/studio3/plugin/install");
		updateSiteCollection.add("http://download.eclipse.org/egit/github/updates");
		updateSiteCollection.add("http://download.eclipse.org/technology/m2e/releases/1.3/1.3.1.20130219-1424");
		updateSiteCollection.add("http://www.apache.org/dist/ant/ivyde/updatesite");
		updateSiteCollection.add("http://dist.springsource.org/release/GRECLIPSE/e4.2/");
		updateSiteCollection.add("http://goldenhammers.com/merclipse/update ");
		updateSiteCollection.add("http://download.eclipse.org/technology/subversive/1.0/update-site-1.0.1/	");
		updateSiteCollection.add("http://jd.benow.ca/jd-eclipse/update");
		// updateSiteCollection.add("http://wwwiti.cs.uni-magdeburg.de/iti_db/research/featureide/deploy/");
		// updateSiteCollection.add("http://openextern.googlecode.com/svn/trunk/openextern_update/");
		// updateSiteCollection.add("http://update.eclemma.org/");
		// updateSiteCollection.add("http://andrei.gmxhome.de/eclipse/");
		// updateSiteCollection.add("http://download.eclipse.org/technology/m2e/milestones/1.4 ");
		// updateSiteCollection.add("http://pmd.sf.net/eclipse");
		// updateSiteCollection.add("http://findbugs.cs.umd.edu/eclipse/");
		updateSiteCollection.add("						 http://eclipse-cs.sourceforge.net/update");
		updateSiteCollection.add("						 http://download.eclipse.org/tools/pdt/updates/release");
		updateSiteCollection.add("					 http://www.ne.jp/asahi/zigen/home/plugin/dbviewer/");
		// add an eclipse site to club plugins with a base eclipse product.
		// updateSiteCollection
		// .add("http://download.eclipse.org/releases/juno/");

		// ///////////////////////////////////////////////////
		// // building the updateSiteURLSet from updatesite extracts.
		 updateSiteCollection = new HashSet<String>();
		// List<String> xxxx = new ArrayList<String>();
		//
		// ec.restoreFromBaseLocation("./UPDATE-SITE-DATA");
		// gotUpdateSites = new HashSet();
		// gotUpdateSites.addAll(ec.getSites());
		// for (UpdateSiteInfo updateSiteInfo : gotUpdateSites) {
		// // removing the trailing slash(/);
		// String updateSiteURL = updateSiteInfo.getUpdateURL();
		// if
		// ("/".equalsIgnoreCase(updateSiteURL.substring(updateSiteURL.length()
		// - 1)))
		// updateSiteURL = updateSiteURL.substring(0, updateSiteURL.length() -
		// 1);
		//
		// Log.outln("name=" + updateSiteInfo.getName() + ";\t site=" +
		// updateSiteURL);
		// updateSiteCollection.add(updateSiteURL);
		//
		// }
		//
		// Log.outln("====Number of Update Sites=" +
		// updateSiteCollection.size());
		// // //////////////////////////////////////////////////////////

		// this is there the mirrored site would be available. It automatically
		// gets created by p2.
		List<String> updateSiteURLCollection_list = new ArrayList<String>(updateSiteCollection);
		Collections.sort(updateSiteURLCollection_list);

		String destinationDirectory = mirrorSiteDesinationPathPrefix + "/" + mirrorSiteDesinationName;
		try {
			boolean verbose = false, raw = true;

			// this way old mirror site can be complete removed first.
			boolean eraseOld = false;

			// this way the base eclipse installation gets copied to the staging
			// location
			boolean copyBaseEclipseSite = false;
			
//			// getting features from update sites
//			//
//			// String baseEclipseP2SiteLocation =
//			// "/Users/singhsk/Developer/eclipse_plugins/eclipse_juno_p2_mirror_site";
//			
//			// if (!DownloadAndStagingRunner
//			// .downloadAndStageAgainstBaseEclipseSite(eclipseHome, eclipseApp,
//			// equinoxAppName, baseEclipseP2SiteLocation, copyBaseEclipseSite ,
//			// updateSiteURLCollection_list, destinationDirectory, verbose, raw,
//			// eraseOld))
//			//
//			
//			 if (!DownloadAndStagingRunner.downloadAndStage(eclipseHome,
//			 eclipseApp, equinoxAppName, updateSiteURLCollection_list,
//			 destinationDirectory, verbose, raw, eraseOld))
//			
//			 {
//			 Log.errln("XXXXXXX  " + "\n  Download and staging error for: " +
//			 "\n  " + destinationDirectory
//			 + "\n    cannot continue  with the analysis and data extraction."
//			 + "\n    ----"
//			 + "\n     You may want to check the logs at  :  " + "\n     " +
//			 eclipseHome + "configuration/  "
//			 + "\n    ---- " + "\nXXXXXXX");
//			 Log.errln("But still  any  way   going on with the analysis for now ..............");
//			 }
			long stage2 = System.currentTimeMillis();
			Log.outln("Downloading and Staging for site configuration at  :  " + destinationDirectory + "  time: "
					+ Util.getFormattedTime(stage2 - stage1));
			Log.errln("Downloading and Staging for site configuration at  :  " + destinationDirectory + "  time: "
					+ Util.getFormattedTime(stage2 - stage1));

			/*
			 * starting analysis ....
			 */

			String outputLocation = "./" + currOutpurDir + "/_OUTPUT";
			String pathToJavaClasses="/Users/singhsk/Developer/java_classes";
			boolean eraseOldExtracts = true;
		
//			AnalysisRunner.analyseAndRecord(destinationDirectory, outputLocation, pathToJavaClasses, eraseOldExtracts);

			// //////////////////////////////////////////////////////
			// the dependency finder.

			outputLocation = "./" + currOutpurDir + "/_OUTPUT";

			String pluginExtractsLocation = outputLocation + "/plugins";
			// without the trailing slash(/)
			String pluginextractAnalysisDestLocation = outputLocation + "/_DEPENDENCY_SET";

			boolean eraseoldDependenctSetExtrcats = true;
			boolean considerBundleExportsOnly = false;
			boolean ignoreBundlesMarkedToBeIgnored = false;
			boolean considerInvokationSatisfactionProxies = true;
			boolean ignoreVersionInFeatureModelGeneration = false;
			DependencyFinder.buildPluginDependencyReports(pluginExtractsLocation, pluginextractAnalysisDestLocation,
					considerBundleExportsOnly, ignoreBundlesMarkedToBeIgnored, considerInvokationSatisfactionProxies,
					eraseoldDependenctSetExtrcats, ignoreVersionInFeatureModelGeneration);
			 ///////////////////////////////////////////////////////////////////////////////////////////////
			long time2 = System.currentTimeMillis();
			Log.outln("Current Job for site configuration at  :  " + destinationDirectory + "  time: "
					+ Util.getFormattedTime(time2 - time1));
			Log.errln("Current Job for site configuration at  :  " + destinationDirectory + "  time: "
					+ Util.getFormattedTime(time2 - time1));

		} catch (Exception e) {

			Log.err(e.getStackTrace().toString());
			e.printStackTrace();
		}

	}

}
