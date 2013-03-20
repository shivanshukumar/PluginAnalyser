package com.shivanshusingh.pluginanalyser.utils.logging;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import com.shivanshusingh.pluginanalyser.utils.Util;
import com.sun.tools.corba.se.idl.constExpr.Equal;

/**
 * Logger to log errors and other info.
 * @author Shivanshu Singh
 *
 */
public class Log {
	
	public static final String STD_ERR_LOG_FILENAME = "stderr.log";

	public  static final String STD_OUT_LOG_FILENAME = "stdout.log";

	private static  boolean outnewline=true;
	private static  boolean errnewline=true;
	public static void outln(String entry)
	{
		boolean oldOutNewLineFlag=outnewline;
		outnewline=false;
		out(outaddon()+entry);
		System.out.print("\n");
		outnewline=oldOutNewLineFlag;
	}
	public static void out(String entry)
	{

		int newLineIndex=0;
		String toWrite="";
		
		

		while (newLineIndex >= 0) {
			
			if(outnewline &&  !"".equals(entry))
			{
				toWrite += outaddon();
				outnewline=false;
			}

			newLineIndex = entry.indexOf("\n");
			if (newLineIndex >= 0) {
				toWrite += entry.substring(0, newLineIndex + 1);
				entry =  entry.substring(newLineIndex + 1);
				outnewline=true;
			}
			
		}

		toWrite += entry;

		System.out.print(toWrite);
	}

	public static void errln(String entry)
	{
		boolean oldErrNewLine=errnewline;
		errnewline=false;
		err(erraddon()+entry);
		System.err.print("\n");
		errnewline=oldErrNewLine;	
	}
	public static void err(String entry)
	{

		int newLineIndex=0;
		String toWrite="";
		
		while(newLineIndex>=0)
			{
			
			if(errnewline &&  !"".equals(entry))
			{
				toWrite += erraddon();
				errnewline=false;
			}

			newLineIndex = entry.indexOf("\n");
			if (newLineIndex >= 0) {
				toWrite += entry.substring(0, newLineIndex + 1);
				entry =  entry.substring(newLineIndex + 1);
				errnewline=true;
			}
			  
			}
		toWrite+=entry;
		
		System.err.print(toWrite);
	}
	public static void errln(char entry)
	{
		errln(""+entry);
	}
	public static void err(char entry)
	{
		err(""+entry);
	}
	public static void outln(char entry)
	{
		outln(""+entry);
	}
	public static void out(char entry)
	{
		out(""+entry);
	}
	private static String addon()
	{
		java.util.Date dt = new java.util.Date();

		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy/MMM/dd hh:mm:ss");//yyyy-MM-dd hh:mm:ss:SSS

		String currentTime = sdf.format(dt);
		
		return currentTime;
	}
	private static String erraddon()
	{
		return "["+addon()+" ERR] ";
	}
	private static String outaddon()
	{
		return "["+addon()+" OUT] ";
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
		if (!Util.checkAndCreateDirectory(path))
			return false;
		path = (path + "/").replaceAll("//", "/");
		try {
			FileOutputStream fout = new FileOutputStream(path + "_"
					+ Log.STD_OUT_LOG_FILENAME);
			FileOutputStream ferr = new FileOutputStream(path + "_"
					+ Log.STD_ERR_LOG_FILENAME);
	
			MultiOutputStream multiOut = new MultiOutputStream(fout  , System.out);
			MultiOutputStream multiErr = new MultiOutputStream(ferr  , System.err);
	
			PrintStream stdout = new PrintStream(multiOut);
			PrintStream stderr = new PrintStream(multiErr);
	
			System.setOut(stdout);
			System.setErr(stderr);
		} catch (FileNotFoundException ex) {
			// Could not create/open the file
		}
		return true;
	}
}
