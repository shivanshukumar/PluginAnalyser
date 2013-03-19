package com.shivanshusingh.pluginanalyser.utils;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * the utility functions   useful in the project.
 * 
 * @author Shivanshu Singh
 *
 */
public class PluginAnalyserUtils {

	private static String TEMP_DIR_PATH = ".";

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
			return success;
		} else if (!f.isDirectory()) {
			return false;
		}

		return true;
	}

	/**
	 * deletes all the files and folders recursively at the specified directory
	 * location, EXCLUDing the directory location  itself.
	 * 
	 * @param folder
	 *            {@link File} handler for the directory location, all of its contents of  which  have to
	 *            be  cleared   
	 * @return {@link boolean} true or false depending upon whether the operation
	 *         succeeded or not.
	 */
	public static boolean clearFolder(File folder) {
		boolean success = true;
		try {
			File[] files = folder.listFiles();
			if (files != null) { // some JVMs return null for empty dirs
				for (File f : files) {
					success = deleteTarget(f);
					if(!success)
						return false;
				}
			}
		} catch (Exception e) {
			success = false;
		}

		return success;
	}

	/**
	 * deletes all the files and folders recursively at the specified directory
	 * location, including the directory location.
	 * 
	 * @param handler
	 *            {@link File} handler for the target, which has to
	 *            be deleted along with all of its contents
	 * @return {@link boolean} true or false depending upon whether the operation
	 *         succeeded or not. 
	 */
	public static boolean deleteTarget(File handler) {
		boolean success = true;
		try {
			File[] files = handler.listFiles();
			if (files != null) { // some JVMs return null for empty dirs
				for (File f : files) {

					if (f.isDirectory()) {
						success = deleteTarget(f);
					} else {
						success = f.delete();
					}
				}
			}
			success = handler.delete();
		} catch (Exception e) {
			success = false;
		}

		return success;
	}
	/**
	 * gets the current timestamp in the format yyyy-MM-dd-HH-mm-ss-SSS
	 * 
	 * @return String
	 */
	public static String getCurrentTimeString() {
		
		return getCurrentTimeString("yyyy-MM-dd-HH-mm-ss-SSS");
	}

	/**
	 * gets the current timestamp in the format  specified.
	 * * @param format String containing the format of the timestamp wanted.
	 * @return String
	 */
	public static String getCurrentTimeString(String format) {
		java.util.Date dt = new java.util.Date();

		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(format);

		String currentTime = sdf.format(dt);
		return currentTime;
	}

	/**
	 * gets the formatted miliseconds string.
	 * 
	 * @return String
	 */
	public static String getFormattedTime(long  miliseconds ) {
		
		DecimalFormat form1 = new DecimalFormat("0");
		DecimalFormat form2 = new DecimalFormat("00");
		DecimalFormat form3 = new DecimalFormat("000");
	      
		String   formattedTime=""
				//+	form2.format((long) miliseconds / (1000 * 60 * 60))+ "h:"
				//+	form2.format((long) miliseconds / (1000 * 60))+"m:"
				+	form2.format((long) miliseconds / 1000)+"s:"
				+	form3.format((long) miliseconds % 1000)+"ms"
				;
		
		
		
		
		return formattedTime;
	}

	/**
	 * gets the path of the current temp directory to be used by the
	 * application. All temporary files should be created in the directory
	 * represented by the path returned by this function.
	 * 
	 * @return {@link String} the temporary working directory path.
	 */
	public static String getTEMP_DIR_PATH() {
		return TEMP_DIR_PATH;
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
				// Log.outln(fileEntry.getAbsolutePath());
			}
		}
		return fileSet;
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

}
