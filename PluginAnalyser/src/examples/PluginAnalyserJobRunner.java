/**
 *  examples of how to use the pluginanalyser   system.
 */
package examples;

import java.io.IOException;

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
		String updateSiteURL =
		// "http://download.eclipse.org/technology/dltk/updates/"
		"https://dl-ssl.google.com/android/eclipse/"
		// "https://dl-ssl.google.com/android/eclipse/m/"//erroneous case.
		// "http://download.eclipse.org/releases/juno/"
		// "http://download.aptana.com/studio3/plugin/install"
		;// without 'site.xml'.
		String mirrorSiteDesinationPathPrefix = "/Users/singhsk/Developer/eclipse_plugins/";// with
																							// a
																							// trailing
																							// slash(/)
		String mirrorSiteDesinationName = updateSiteURL.replace("/", "-")
				.replace(":", "-").replace(".", "-").replaceAll("-{1,}", "-")
		// +PluginAnalyserUtils.getCurrentTimeString()
		;
		// "testmirror_googleandroid1" ;
		String destinationDirectory = mirrorSiteDesinationPathPrefix
				+ mirrorSiteDesinationName;// this is there the mirrired site
											// would be available. It
											// automatically gets created by p2.
		// "/Users/singhsk/Developer/eclipse_sandbox";
		try {
			boolean writeModeClean = true, verbose = true, raw = true, eraseOld = true;// this
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
					eclipseApp, equinoxAppName, updateSiteURL,
					destinationDirectory, writeModeClean, verbose, raw,
					eraseOld))

			{
				Log.errln("XXXXXXX  " +
						"\n  Download and staging error for: " +
						"\n  "  + updateSiteURL	+
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
