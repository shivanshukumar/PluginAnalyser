/**
 *  examples of how to use the pluginanalyser   system.
 */
package examples;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.shivanshusingh.pluginanalyser.analysis.AnalysisRunner;
import com.shivanshusingh.pluginanalyser.staging.DownloadAndStagingRunner;
import com.shivanshusingh.pluginanalyser.utils.PluginAnalyserUtils;
import com.shivanshusingh.pluginanalyser.utils.logging.Log;

/**
 * @author Shivanshu Singh
 * 
 */
public class PluginAnalyserJobRunner {

	public static void main(String[] args) {

		// experimenting with the eclipse update manager from command line to
		// get features
		// http://wiki.eclipse.org/Equinox_p2_Repository_Mirroring
		String eclipseHome = "/Users/singhsk/Developer/eclipse_sandbox/";// with
																			// the
																			// trailing
																			// slash(/).
		String eclipseApp = "Eclipse";
		String equinoxAppName = "org.eclipse.equinox.p2.artifact.repository.mirrorApplication";
		
		// all update sites that are added to the update site collection from
		// which all artifacts will be downloaded.
		// each update site parameter should be the URL to the eclipse product
		// update site without the 'site.xml' at end.
		Set<String> updateSiteURLCollection = new HashSet<String>();

		// "http://download.eclipse.org/technology/dltk/updates/"

		updateSiteURLCollection				.add("https://dl-ssl.google.com/android/eclipse/");
		// "https://dl-ssl.google.com/android/eclipse/m/"//erroneous case.
		// "http://download.aptana.com/studio3/plugin/install"
		updateSiteURLCollection
				.add("http://download.eclipse.org/egit/github/updates");
//		updateSiteURLCollection  			.add("http://download.eclipse.org/releases/juno/");

		
		String mirrorSiteDesinationPathPrefix = "/Users/singhsk/Developer/eclipse_plugins/";
		
		String mirrorSiteDesinationName = "new_site"+Math.random();
				mirrorSiteDesinationName=mirrorSiteDesinationName.replace("/", "-")
				.replace(":", "-").replace(".", "-").replaceAll("-{1,}", "-")
		;
		
		// this is there the mirrored site would be available. It automatically gets created by p2.
		String destinationDirectory = mirrorSiteDesinationPathPrefix
				+ mirrorSiteDesinationName;
		// "/Users/singhsk/Developer/eclipse_sandbox";
		try {
			boolean verbose = true, raw = true, eraseOld = true;// this
																						// way
																						// old
																						// mirror
																						// site
																						// can
																						// be
																						// complete
																						// removed
																						// first.

			String currTimeString = "_OUTPUT_" + mirrorSiteDesinationName
			// + "_" + PluginAnalyserUtils.getCurrentTimeString()
			;

			if (!Log.startLogger("./" + currTimeString + "/_LOGS")) {
				Log.errln("Cannot start logging sorry. Please check the path provided or necessary permissions");
			}
			PluginAnalyserUtils.setTEMP_DIR_PATH("./" + currTimeString
					+ "/_TMP");
			// getting features from an update site

			if (!DownloadAndStagingRunner.downloadAndStage(eclipseHome,
					eclipseApp, equinoxAppName, updateSiteURLCollection,
					destinationDirectory, verbose, raw,
					eraseOld))

			{
				Log.errln("XXXXXXX  " +
						"\n  Download and staging error for: " +
						"\n  "  + updateSiteURLCollection	+
						"\n    cannot continue  with the analysis and data extraction."  + 
						"\n    ----" + 
						"\n     You may want to check the logs at  :  " + 
						"\n     "	+ eclipseHome  + "configuration/  "  + 
						"\n    ---- " +
						"\nXXXXXXX"
						);
				return;
			}

			/*
			 * starting analysis ....
			 */

			String outputLocation = "./" + currTimeString + "/_OUTPUT";
			AnalysisRunner.analyseAndRecord(destinationDirectory,
					outputLocation);

		} catch (IOException e) {
			Log.err(e.getStackTrace().toString());
			// e.printStackTrace();
		}

	}

}
