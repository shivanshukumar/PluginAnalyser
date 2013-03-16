package com.shivanshusingh.PluginAnalyser.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class PluginAnalyserUtils {

	private static String TEMP_DIR_PATH = ".";

	/**
	 * gets the path of the current temp directory to be used by the
	 * application. All temporary files should be created in the directory
	 * represented by the path returned by this function.
	 * 
	 * @return
	 */
	public static String getTEMP_DIR_PATH() {
		return TEMP_DIR_PATH;
	}

	/**
	 * updates (and creates the directory structure if needed) the location of
	 * the temporary directory to be used by the application.
	 * 
	 * @param path
	 *            the path of the temp directory.
	 * @return {@link boolean} true or false whether it could update the
	 *         location of the temp directory.
	 */
	public static boolean setTEMP_DIR_PATH(String path) {
		if (checkAndCreateDirectory(path)) {
			TEMP_DIR_PATH = path;
			return true;
		}
		return false;

	}

	/**
	 * deletes all the files and folders recursively at the specified directory
	 * location, including the directory location.
	 * 
	 * @param folder
	 *            {@link File} handler for the directory location, which has to
	 *            be deleted along with all of its contents
	 * @return {@link boolean} true or fase depending upon whether the operation
	 *         succeeded or not.
	 */
	public static boolean deleteFolder(File folder) {
		boolean success = true;
		try {
			File[] files = folder.listFiles();
			if (files != null) { // some JVMs return null for empty dirs
				for (File f : files) {

					if (f.isDirectory()) {
						success = deleteFolder(f);
					} else {
						success = f.delete();
					}
				}
			}
			success = folder.delete();
		} catch (Exception e) {
			success = false;
		}

		return success;
	}

	/**
	 * gets a set of the absolute paths of all the files (recursively) at the
	 * specified location
	 * 
	 * @param folder
	 *            {@link File} handler for the directory for which all the path
	 *            of all the files are needed
	 * @return Set<String>
	 */
	public static Set<String> listFilesForFolder(final File folder) {
		Set<String> fileSet = new HashSet<String>();

		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				fileSet.addAll(listFilesForFolder(fileEntry));
			} else {
				fileSet.add(fileEntry.getAbsolutePath());
				// System.out.println(fileEntry.getAbsolutePath());
			}
		}
		return fileSet;
	}

	/**
	 * Starts the logging capability. It maps the System.out or stdout and
	 * System.err or stderr to respective FileOutputStream. So after calling
	 * this method, all stuff that gets written to stdout would be written to
	 * _stdout.log and that written to stderr would be written to _stderr.log
	 * files, and made available at the location represented by the path
	 * provided.
	 * 
	 * @param path
	 *            the path of the location where the logs should be made
	 *            available. Any data that already exists at the specified
	 *            location may be overwritten. the function takes care of
	 *            creating the required director structure for the path
	 *            provided, in case it does not already exist. It is assumed
	 *            that the permissions to the specified locations allow for the
	 *            method to write there.
	 * @return true or false depending whether logging could be enabled or not.
	 *         This may be false if it is not possible to access or create the
	 *         location specified in the path param.
	 */
	public static boolean startLogger(String path) {
		if (!checkAndCreateDirectory(path))
			return false;
		path = (path + "/").replaceAll("//", "/");
		try {
			FileOutputStream fout = new FileOutputStream(path + "_"
					+ "stdout.log");
			FileOutputStream ferr = new FileOutputStream(path + "_"
					+ "stderr.log");

			MultiOutputStream multiOut = new MultiOutputStream(fout);// ,
																		// System.out);
			MultiOutputStream multiErr = new MultiOutputStream(ferr);// ,
																		// System.err);

			PrintStream stdout = new PrintStream(multiOut);
			PrintStream stderr = new PrintStream(multiErr);

			System.setOut(stdout);
			System.setErr(stderr);
		} catch (FileNotFoundException ex) {
			// Could not create/open the file
		}
		return true;
	}

	/**
	 * It checks if the location represented by the path provided is a
	 * directory. the directory structure represented by the path parameter is
	 * created if it does not already exist.
	 * 
	 * @param path
	 *            the location of the directory
	 * @return {@link true} or {@link false} . false if the location specified
	 *         by the {@code path} param existed but was not a directory, true
	 *         otherwise. Also would return false if it tried to create the
	 *         directory stucture represented by the path param if it did not
	 *         already exist, and failed in doing that.
	 * 
	 */
	public static boolean checkAndCreateDirectory(String path) {
		File f = new File(path);
		if (!f.exists()) {
			// create the directory and all parent directories if needed.
			boolean success = f.mkdirs();
			if (!success)
				return success;
		} else if (!f.isDirectory()) {
			return false;
		}

		return true;
	}

	/**
	 * gets the current timestamp in the format yyyy-MM-dd-HH-mm-ss-SSSS
	 * 
	 * @return String
	 */
	public static String getCurrentTimeString() {
		java.util.Date dt = new java.util.Date();

		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
				"yyyy-MM-dd-HH-mm-ss-SSSS");

		String currentTime = sdf.format(dt);
		return currentTime;
	}

}
