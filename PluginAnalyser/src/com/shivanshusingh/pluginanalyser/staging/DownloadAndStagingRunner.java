package com.shivanshusingh.pluginanalyser.staging;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.shivanshusingh.pluginanalyser.utils.Util;
import com.shivanshusingh.pluginanalyser.utils.logging.Log;

/**
 * to download (mirror) an eclipse repository / update site .
 * 
 * @author Shivanshu Singh
 * 
 */
public class DownloadAndStagingRunner {
	  private static String FEATURE_FOLDER_REL_PATH = "/features";
	  private static     String PLUGIN_FOLDER_REL_PATH = "/plugins";

	/**
	 * to download (mirror) a p2 repository / update site . Sets the stage for
	 * analysis later on. an eclipse installation with p2 is required to execute
	 * this.
	 * 
	 * @param eclipseHome
	 *            the path of eclipse home without the trailing slash(/) .
	 * @param eclipseApp
	 *            the name (or the relative path from the {@code eclipseHome}
	 *            path to) the eclipse executable application
	 * @param equinoxAppName
	 *            the name of the equinox application. generally it is
	 *            {@code org.eclipse.equinox.p2.artifact.repository.mirrorApplication}
	 * @param updateSiteURLCollection
	 *            {@link Set} collection of the URLs of the sites from where the
	 *            plugins and metadata would be fetched. This is generally a
	 *            collection of eclipse p2 repositories / update sites. Each of
	 *            the URLs in the collection must be without 'site.xml' at the
	 *            end.
	 * @param destinationDirectory
	 *            this is there the mirrored site would be available. It
	 *            automatically gets created by eclipse's equinox p2 application
	 *            if it does not exist already.
	 * @param verbose
	 * @param raw
	 * @param eraseOld
	 *            true if you want the old destinationDirectory to be erased
	 *            first. All its contents will be erased first and then the
	 *            mirroring will be done.
	 * @throws IOException
	 */
	public static boolean downloadAndStage(String eclipseHome,
			String eclipseApp, String equinoxAppName,
			Set<String> updateSiteURLCollection, String destinationDirectory,
			boolean verbose, boolean raw, boolean eraseOld) throws IOException {

		boolean flag_ErrorInSomeRepositoryDownload=false;
		if (null == updateSiteURLCollection) {
			// if the updatestitecollection   is null, return.
			Log.errln("xxxx  The updateSiteUrlCollection specified is null . Cannot download and stage anything.");
			Log.outln("xxxx  The updateSiteUrlCollection specified is null . Cannot download and stage anything.");

			return false;
		}

		if (eraseOld) {
			File f = new File(destinationDirectory);
			if (f.exists()) {
				Log.outln("DELETING: " + f.getPath());

				if (!Util.clearFolder(f)) {
					Log.errln("XXXXXXXXX Error deleting old directory, not continuing forward");
					return false;
				}

			}
		}

		int sitesDone = 0;

		int totalSites = updateSiteURLCollection.size();

		for (String updateSiteURL : updateSiteURLCollection) {
			sitesDone++;
			updateSiteURL=updateSiteURL.trim();
			// using the eclipse update manager from command line to get features  and plugin artifacts.
			// http://wiki.eclipse.org/Equinox_p2_Repository_Mirroring
			String command = eclipseHome.trim()
					+ "/"
					+ eclipseApp.trim()
					+
					// (writeModeClean? " -writeMode clean" : "" ) +
					(verbose ? " -verbose" : "") + (raw ? " -raw" : "")
					+ " -nosplash" + " -includeOptional" + " -application "
					+ equinoxAppName.trim() + " -source "
					+ updateSiteURL + " -destination " + "file:"
					+ destinationDirectory.trim();

			Log.outln("Starting Download and Staging Process of " + sitesDone + " of "
					+ totalSites +"  |  Source="  +updateSiteURL  );
			Log.errln("Starting Download and Staging Process of " + sitesDone + " of "
					+ totalSites +"  |  Source="  +updateSiteURL  );
			
			Log.outln("EXECUTING: " + command);
			Process child = Runtime.getRuntime().exec(command);

			BufferedReader br = new BufferedReader(new InputStreamReader(
					child.getInputStream()));
			int inread;
			while ((inread = br.read()) != -1) {
				Log.out((char) inread);
			}
			br.close();
			br = new BufferedReader(new InputStreamReader(
					child.getErrorStream()));
			while ((inread = br.read()) != -1) {
				Log.err((char) inread);
			}
			br.close();

			try {
				child.waitFor();
			} catch (InterruptedException e) {
				Log.err(Util.getStackTrace(e));
				// e.printStackTrace();
			}

			int exitValue = child.exitValue();
			Log.outln("Download and Staging Process of " + sitesDone + " of "
					+ totalSites + " , exitvalue(0=success)=" + exitValue +"  |  Source="  +updateSiteURL  );
			Log.errln("Download and Staging Process  " + sitesDone + " of "
					+ totalSites + " , exitvalue(0=success)=" + exitValue +"  |  Source="  +updateSiteURL  );

			child.destroy();

			if (0 != exitValue) {
				Log.outln("XXXXXXXX  \n Download and Staging Process for : "+ updateSiteURL
						+ "\n could not finish  properly, \n and the mirrored site may not be proper.  \nXXXXXXXXX");
				Log.errln("XXXXXXXX  \n Download and Staging Process for : "	+ updateSiteURL
						+ "\n could not finish  properly, \n and the mirrored site may not be proper.  \nXXXXXXXXX");
				flag_ErrorInSomeRepositoryDownload= true;

			} else {
				Log.outln("=========  \n Download and Staging Process  for : \n "
						+ updateSiteURL
						+ "\n successfully completed \n========");
			}
		}
		return   !flag_ErrorInSomeRepositoryDownload;
	}

	/**
	 * to download (mirror) eclipse repositories / update sites along with a
	 * base eclipse installation . Sets the stage for analysis later on. an
	 * eclipse installation with p2 is required to execute this.
	 * 
	 * @param eclipseHome
	 *            the path of eclipse home without the trailing slash(/) .
	 * @param eclipseApp
	 *            the name (or the relative path from the {@code eclipseHome}
	 *            path to) the eclipse executable application
	 * @param equinoxAppName
	 *            the name of the equinox application. generally it is
	 *            {@code org.eclipse.equinox.p2.artifact.repository.mirrorApplication}
	 * @param baseEclipseInstallationHome
	 *            the path to the base eclipse installation home, without the
	 *            trailing slash(/) , in case this must also be staged for
	 *            analysis in addition to the other p2 repositories / updates
	 *            sites. The assumption is that all features and plugins will be
	 *            available in flat directories at
	 *            {@link baseEclipseInstallationHome/features} and
	 *            {@link baseEclipseInstallationHome/plugins} respectively.
	 * @param updateSiteURLCollection
	 *            {@link Set} collection of the URLs of the sites from where the
	 *            plugins and metadata would be fetched. This is generally a
	 *            collection of eclipse p2 repositories / update sites. Each of
	 *            the URLs in the collection must be without 'site.xml' at the
	 *            end.
	 * @param destinationDirectory
	 *            this is there the mirrored site would be available. It
	 *            automatically gets created by eclipse's equinox p2 application
	 *            if it does not exist already.
	 * @param verbose
	 * @param raw
	 * @param eraseOld
	 *            true if you want the old destinationDirectory to be erased
	 *            first. All its contents will be erased first and then the
	 *            mirroring will be done.
	 * 
	 * @throws IOException
	 */
	public static boolean downloadAndStageWithEclipseInstallation(
			String eclipseHome,
			String eclipseApp,
			String equinoxAppName,
			String baseEclipseInstallationHome,
			Set<String> updateSiteURLCollection,
			String destinationDirectory,
			boolean verbose,
			boolean raw,
			 boolean eraseOld

	) throws IOException {
		
		Log.outln("========  Starting  Eclipse Base Installation Staging  ==========");
		Log.errln("========  Starting  Eclipse Base Installation Staging  ==========");
		
		if (eraseOld) {
			File f = new File(destinationDirectory);
			if (f.exists()) {
				Log.outln("DELETING: " + f.getPath());

				if (!Util.clearFolder(f)) {
					Log.errln("XXXXXXXXX Error deleting old directory, not continuing forward");
					return false;
				}

			}
		}
		
		File srcDirectory = new File(baseEclipseInstallationHome);
		
		File srcFeatureDirectory = new File(baseEclipseInstallationHome
				+ FEATURE_FOLDER_REL_PATH);
		File srcPluginDirectory = new File(baseEclipseInstallationHome
				+ PLUGIN_FOLDER_REL_PATH);

		if (!Util.checkDirectory(srcDirectory, true, true, true,	false)) {

			// this means that the directory does not exist or the specified
			// file path does not pooint to a directory.
			Log.errln("XXXXX  The specified directory:"
					+ srcDirectory.getAbsolutePath()
					+ " is not a directory or does not exist or not readable.  ");
			return false;
		}

		if (!Util.checkDirectory(srcFeatureDirectory, true,	true, true, false))

		{
			// this means that the directory does not exist or the specified
			// file path does not pooint to a directory.
			Log.errln("XXXXX  The specified directory:"
					+ srcFeatureDirectory.getAbsolutePath()
					+ " is not a directory or does not exist  or not readable  ");
			return false;
		}
		if (!Util.checkDirectory(srcPluginDirectory, true,		true, true, false))

		{
			// this means that the directory does not exist or the specified
			// file path does not pooint to a directory.
			Log.errln("XXXXX  The specified directory:"
					+ srcPluginDirectory.getAbsolutePath()
					+ " is not a directory or does not exist or  not   readable. ");
			return false;
		}
		
		// checking if the destination directory already exists if not then create it,
		
		if(!Util.checkAndCreateDirectory(destinationDirectory))
		{
			Log.errln("XXXXX  The specified directory:"
					+ destinationDirectory
					+ " is not a directory or could not create it. ");
			return false;
		}
		
		// checking if the destination  features directory already exists if not then create it,

		String destFeatureDirectoryPath = destinationDirectory+FEATURE_FOLDER_REL_PATH;
		if(!Util.checkAndCreateDirectory(destFeatureDirectoryPath))
		{
			Log.errln("XXXXX  The specified directory:" + destFeatureDirectoryPath + " is not a directory or could not create it. ");
			return false;
		}
		
		// checking if the destination  plugin directory already exists if not then create it,

		String destPluginDirectoryPath = destinationDirectory+PLUGIN_FOLDER_REL_PATH;
		if(!Util.checkAndCreateDirectory(destPluginDirectoryPath))
		{
			Log.errln("XXXXX  The specified directory:" + destPluginDirectoryPath + " is not a directory or could not create it. ");
			return false;
		}
		
		try{
		File destFeatureDirectory = new File(destFeatureDirectoryPath);
		File destPluginDirectory = new File(destPluginDirectoryPath);
		// copying all the features.
		Util.copyDirectoryContents(srcFeatureDirectory, destFeatureDirectory);
		// copying all the plugin.
		Util.copyDirectoryContents(srcPluginDirectory, destPluginDirectory);
		}catch(IOException e)
		{
			Log.errln("xxxx  could not stage the eclipse base installation,  Exit.  Error in copying featues and plugins . ");
			Log.errln(e.getStackTrace().toString());
			return false;
		}
	
		Log.outln("========  Eclipse Base Installation Staging done, now Staging the Update Sites. ==========");
		Log.errln("========  Eclipse Base Installation Staging done, now Staging the Update Sites. ==========");
		
		return  downloadAndStage(eclipseHome, eclipseApp, equinoxAppName,
				updateSiteURLCollection, destinationDirectory, verbose, raw,
				false);
	}

}
