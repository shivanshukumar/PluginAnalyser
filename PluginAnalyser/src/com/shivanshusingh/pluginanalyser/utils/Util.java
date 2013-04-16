package com.shivanshusingh.pluginanalyser.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.shivanshusingh.pluginanalyser.analysis.BundleAnalyser;
import com.shivanshusingh.pluginanalyser.utils.logging.Log;
import com.shivanshusingh.pluginanalyser.utils.parsing.Constants;

/**
 * the utility functions useful in the project.
 * 
 * @author Shivanshu Singh
 * 
 */
public class Util {

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
	 * location, EXCLUDing the directory location itself.
	 * 
	 * @param folder
	 *            {@link File} handler for the directory location, all of its
	 *            contents of which have to be cleared
	 * @return {@link boolean} true or false depending upon whether the
	 *         operation succeeded or not.
	 */
	public static boolean clearFolder(File folder) {
		boolean success = true;
		try {
			File[] files = folder.listFiles();
			if (files != null) { // some JVMs return null for empty dirs
				for (File f : files) {
					success = deleteTarget(f);
					if (!success)
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
	 *            {@link File} handler for the target, which has to be deleted
	 *            along with all of its contents
	 * @return {@link boolean} true or false depending upon whether the
	 *         operation succeeded or not.
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
	 * gets the current timestamp in the format specified. * @param format
	 * String containing the format of the timestamp wanted.
	 * 
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
	public static String getFormattedTime(long milliseconds) {
		DecimalFormat form1 = new DecimalFormat("0");
		DecimalFormat form2 = new DecimalFormat("00");
		DecimalFormat form3 = new DecimalFormat("000");

		int seconds = (int) (milliseconds / 1000) % 60 ;
		int minutes = (int) ((milliseconds / (1000*60)) % 60);
		int hours   = (int) ((milliseconds / (1000*60*60)) % 24);
		int millis=(int) (milliseconds%1000);
		String formattedTime = ""
				+ form2.format(hours)  +":"
				+ form2.format(minutes) + ":"
				+ form2.format(seconds )		+ "."
				+ form3.format(millis) ;

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

	public static boolean checkDirectory(File directory, boolean isDirectory, boolean exists, boolean isReadable,
			boolean isWritable) {
		if ((isDirectory ? directory.isDirectory() : true) && (exists ? directory.exists() : true)
				&& (isReadable ? directory.canRead() : true) && (isWritable ? directory.canWrite() : true))
			return true;

		return false;

	}

	public static boolean checkFile(File file, boolean isFile, boolean exists, boolean isReadable, boolean isWritable) {
		if ((isFile ? file.isFile() : true) && (exists ? file.exists() : true) && (isReadable ? file.canRead() : true)
				&& (isWritable ? file.canWrite() : true))
			return true;

		return false;

	}

	/**
	 * 
	 * copies all the contents (files and directories recursively) of the srcDir
	 * to destDir. The destDir is created if it does not exist. example:
	 * srcDir/x and destDir. copyDirectoryContents(srcDir, destDir) would result
	 * in: srcDir/x --> destDir/x
	 * 
	 * @see FileUtils .copyDirectory(File, File)
	 * @see FileUtils .copyFile(File, File)
	 * @param srcDir
	 * @param destDir
	 * @throws IOException
	 */
	public static void copyDirectoryContents(File srcDir, File destDir) throws IOException {
		// copying all the features.
		File[] entries = srcDir.listFiles();
		if (null != entries) {
			for (File entry : entries) {
				File destFeatureFile = new File(destDir.getPath() + "/" + entry.getName());
				if (entry.isDirectory())
					FileUtils.copyDirectory(entry, destFeatureFile);
				else if (entry.isFile())
					FileUtils.copyFile(entry, destFeatureFile);
			}
		}
	}

	public static String getStackTrace(Exception e)
	{
		String separator = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n";
		String pre = "XXXX|";
		String trace = "";
		if (null != e) {
			trace += separator + pre + " " + e.getClass().getCanonicalName() + "::\"" + e.getMessage() + "\"\n";
			StackTraceElement[] elements = e.getStackTrace();
			for (StackTraceElement element : elements) {
				trace += pre + "\t@ " + element.toString() + "\n";
			}
			trace += separator;
		}
		return trace;
	}

	/**
	 * 	returns {@code true}  if  {@code spec}  is a valid URL,  {@code false  }  otherwise.  
	 * @param spec
	 * @return  {@code true}  or  {@code false  } 
	 *  
	 */
	public static boolean validURL(String spec) {
		try {
			java.net.URL url=new URL(spec  );
		} catch (MalformedURLException e) {
			return false;
		}
		return true;
	}

	/**
	 * @param fileName
	 * @param outputLocation
	 * @throws IOException
	 */
	public static void writeObjectToDisk(Object objectToWrite, String fileName, String outputLocation)
			throws IOException {
	
		fileName = fileName.trim().replace('/', '_').replace('\\', '_');
		
		outputLocation = (outputLocation + "/").trim().replaceAll("//", "/").replaceAll("\\\\", "\\");
		
		FileOutputStream fos = new FileOutputStream(outputLocation + fileName);
	
		ObjectOutputStream oos = new ObjectOutputStream(fos);
	    oos.writeObject(objectToWrite);
	    oos.close();
	    fos.close();
	    
	    // writing a text version for debugging related use.
		FileWriter fwriter = new FileWriter(outputLocation+fileName+Constants.EXTENSION_TXT);
		BufferedWriter writer = new BufferedWriter(fwriter);
	
		List<String> pluginNames=new ArrayList<String>(((Map<String, Map<String, Set<String>>>) objectToWrite).keySet());
		Collections.sort(pluginNames);
		for(String pluginName:pluginNames)
		{
			
			String toWrite=pluginName+"="+((Map<String, Map<String, Set<String>>>) objectToWrite).get(pluginName).toString();
			writer.write(toWrite+"\n");
		}
		
		// writer.write("===================================================\n");
		writer.close();
		fwriter.close();
	
	}

}
