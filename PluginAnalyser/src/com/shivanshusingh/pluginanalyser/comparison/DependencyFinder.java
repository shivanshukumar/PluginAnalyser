/**
 * 
 */
package com.shivanshusingh.pluginanalyser.comparison;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.objectweb.asm.util.CheckAnnotationAdapter;

import com.shivanshusingh.pluginanalyser.analysis.BundleInformation;
import com.shivanshusingh.pluginanalyser.utils.Util;
import com.shivanshusingh.pluginanalyser.utils.logging.Log;
import com.shivanshusingh.pluginanalyser.utils.parsing.Constants;

/**
 * @author Shivanshu Singh
 * 
 */
public class DependencyFinder {

	static Map<String, ImpExp> functions = new HashMap<String, ImpExp>();
	static Map<String, ImpExp> types = new HashMap<String, ImpExp>();

	/**
	 * 
	 * this function build the superset of dependencies from the extract files
	 * to finally figure out what dependencies were unstated or missing.
	 * 
	 * @param pathToBasePluginExtractsDir
	 * @param pathToPluginDependencyAnalysisOutputLocation
	 * @param eraseOld
	 * @throws IOException
	 */
	public static void buildPluginDependencySuperSet(String pathToBasePluginExtractsDir, String pathToPluginDependencyAnalysisOutputLocation, boolean eraseOld)
			throws IOException {

		Log.outln("==== Now Building the  Plugin Dependency  Set from source: "+pathToBasePluginExtractsDir+" ====");
		Log.errln("==== Now Building the  Plugin Dependency  Set from source: "+pathToBasePluginExtractsDir+" ====");
		
		long time1=System.currentTimeMillis();
		
		
		File pluginExtractDirectory = new File(pathToBasePluginExtractsDir);

		if (!Util.checkDirectory(pluginExtractDirectory, true, true, true, false)) {
			// the plugin extracts source base directory is not accessible.
			Log.errln("xxxx in buildPluginDependencySuperSet \\\n the plugin dir: " + pluginExtractDirectory.getAbsolutePath()
					+ "\n is not a directory or not readable or   does not exist. \nxxxx");
			return;
		}
		if (!Util.checkAndCreateDirectory(pathToPluginDependencyAnalysisOutputLocation)) {
			Log.errln("xxxx in buildPluginDependencySuperSet \\\n the output location dir: " + pathToPluginDependencyAnalysisOutputLocation
					+ "\n is not accessible. \nxxxx");
			return;
		}
		
		if(eraseOld)
		{
			Util.clearFolder(new File(pathToPluginDependencyAnalysisOutputLocation));
		}
		File[] entries = pluginExtractDirectory.listFiles();
		if (entries.length < 2) {

			Log.errln("XXXX  \n " + pluginExtractDirectory.getAbsolutePath()
					+ "\n has has than 2 extracts, cannot compare.\n The location must have at least 2 extract files.  \nXXXX  ");
		}
		
		long pluginExtractsDone=0;

		for (File entry : entries) {
			pluginExtractsDone++;
			if (Util.checkFile(entry, true, true, true, false)) {
				/**
				 * this is the name of the PluginExtract file, without the
				 * extension. this is for obtaining the fully qualified plugin name
				 * with version and qualifier, even if that information is not in
				 * the pluginExtract file, cause because of an originally missing
				 * manifest.mf file for thus plugin.
				 */
				String thisPluginExtractName = entry.getName().trim();
				int startingIndex = thisPluginExtractName.indexOf(Constants.EXTRACT_FILE_PREFIX_PLUGIN) + Constants.EXTRACT_FILE_PREFIX_PLUGIN.length();
				thisPluginExtractName = thisPluginExtractName.substring(startingIndex);
				if (thisPluginExtractName.toLowerCase().endsWith(Constants.EXTRACT_FILE_EXTENSION_PLUGIN))
					thisPluginExtractName = thisPluginExtractName.substring(0, thisPluginExtractName.length() - Constants.EXTRACT_FILE_EXTENSION_PLUGIN.length());
				Log.outln("==== Adding to DependencySet,  plugin "+pluginExtractsDone  +" of "+entries.length+" : "+thisPluginExtractName+"====");

				// restoring the functions information from the file.
				Set<String> myMethodExports = restorePropertyFromExtract(entry, Constants.PLUGIN_ALL_MY_METHODS_PUBLIC);
				Set<String> myMethodImports = restorePropertyFromExtract(entry, Constants.PLUGIN_ALL_MY_METHOD_CALLS_EXTERNAL_AND_NON_JAVA);
				Set<String> myTypeExports = restorePropertyFromExtract(entry, Constants.PLUGIN_ALL_MY_TYPES_PUBLIC);
				Set<String> myTypeImports = restorePropertyFromExtract(entry, Constants.PLUGIN_ALL_TYPES_DETECTED_EXTERNAL_AND_NON_JAVA);

				// merging functions
				for (String s : myMethodExports) {
					ImpExp impexp = new ImpExp();
					if (functions.containsKey(s))
						impexp = functions.get(s);
					impexp.exp.add(thisPluginExtractName);
					functions.put(s, impexp);
				}
				

				for (String s : myMethodImports) {
					ImpExp impexp = new ImpExp();
					if (functions.containsKey(s))
						impexp = functions.get(s);
					impexp.imp.add(thisPluginExtractName);
					functions.put(s, impexp);
				}
				//System.out.println(functions.keySet().toString());

				// merging types (classes)
				for (String s : myTypeExports) {
					ImpExp impexp = new ImpExp();
					if (types.containsKey(s))
						impexp = types.get(s);
					impexp.exp.add(thisPluginExtractName);
					types.put(s, impexp);
				}

				for (String s : myTypeImports) {
					ImpExp impexp = new ImpExp();
					if (types.containsKey(s))
						impexp = types.get(s);
					impexp.imp.add(thisPluginExtractName);
					types.put(s, impexp);
				}
				//System.out.println(types.keySet().toString());
			}
			
		}
		// write out the merged file

					writeData(pathToPluginDependencyAnalysisOutputLocation );
					
					
					long time2=System.currentTimeMillis();
					Log.outln("Dependency Set Creation for "+entries.length+" plugin extracts, at plugin extract src  :  "+pathToBasePluginExtractsDir +"  time: "+Util.getFormattedTime(time2-time1));
					Log.errln("Dependency Set Creation for "+entries.length+" plugin extracts, at plugin extract src  :  "+pathToBasePluginExtractsDir +"  time: "+Util.getFormattedTime(time2-time1));

	}

	private static void writeData(String pluginDependencySetOutputLocationPath) throws IOException {
		if (!Util.checkAndCreateDirectory(pluginDependencySetOutputLocationPath)) {
			Log.errln("xxxx\n in writeData() in   DependencyFinder, the output location: " + pluginDependencySetOutputLocationPath
					+ "  \n is not accessible, cannot record data.  \nxxxx");
			return;
		}
		
		
		File functionFile = new File(pluginDependencySetOutputLocationPath + "/" + Constants.DEPENDENCY_SET_FILE_PREFIX_PLUGIN + "functions"+Constants.DEPENDENCY_SET_FILE_EXTENSION_PLUGIN);
		File typeFile = new File(pluginDependencySetOutputLocationPath + "/" + Constants.DEPENDENCY_SET_FILE_PREFIX_PLUGIN + "types"+Constants.DEPENDENCY_SET_FILE_EXTENSION_PLUGIN);

		// writing functions set.

		FileWriter filewriter = new FileWriter(functionFile);
		BufferedWriter writer = new BufferedWriter(filewriter);

		writer.write(Constants.PLUGIN_DEPENDENCY_ALL_FUNCTIONS + "\n");

		for (String key : functions.keySet()) {

			writer.write(Constants.PLUGIN_DEPENDENCY_ELEMENT_SET_DELIM + "\n");

			// writing the function signature.
			writer.write(key + "\n");

			ImpExp impexp = functions.get(key);
			
			Set<String> imp = impexp.imp;
			Set<String> exp = impexp.exp;

			// all importers
			writer.write(Constants.PLUGIN_DEPENDENCY_IMPORTERS + "\n");
			for (String s : imp) {
				writer.write(s + "\n");
			}
			writer.write(Constants.MARKER_TERMINATOR + "\n");

			// all exporters
			writer.write(Constants.PLUGIN_DEPENDENCY_EXPORTERS + "\n");
			for (String s : exp) {
				writer.write(s + "\n");
			}
			writer.write(Constants.MARKER_TERMINATOR + "\n");

			// all importers not satisfied
			writer.write(Constants.PLUGIN_DEPENDENCY_IMPORTERS_UNSATISFIED + "\n");
			writer.write((0==exp.size()? true+", "+imp.size()+" importer(s) hungry ":false+", "+exp.size()+ (1<exp.size()? " exporters!! PLURAL??":" exporter")+" available ")+"\n");
			writer.write(Constants.MARKER_TERMINATOR + "\n");

			// all exporters   whose exports were not needed by anyone
			writer.write(Constants.PLUGIN_DEPENDENCY_EXPORTERS_UNSATISFIED + "\n");
			writer.write((0==imp.size()? true+", "+exp.size()+ (1<exp.size()? " exporters!! PLURAL??":" exporter")+" eagerly available ":false+", "+imp.size()+" importer(s) hungry ")+"\n");
			writer.write(Constants.MARKER_TERMINATOR + "\n");

			// terminating the key ( function name )
			writer.write(Constants.MARKER_TERMINATOR + "\n");
		}

		// terminating the functions set
		writer.write(Constants.MARKER_TERMINATOR + "\n");

		writer.close();
		filewriter.close();

		// writing types set.

		filewriter = new FileWriter(typeFile);
		writer = new BufferedWriter(filewriter);

		writer.write(Constants.PLUGIN_DEPENDENCY_ALL_TYPES + "\n");

		for (String key : types.keySet()) {

			writer.write(Constants.PLUGIN_DEPENDENCY_ELEMENT_SET_DELIM + "\n");

			// writing the type signature.
			writer.write(key + "\n");

			ImpExp impexp = types.get(key);
			Set<String> imp = impexp.imp;
			Set<String> exp = impexp.exp;

			
			// all importers
			writer.write(Constants.PLUGIN_DEPENDENCY_IMPORTERS + "\n");
			for (String s : imp) {
				writer.write(s + "\n");
			}
			writer.write(Constants.MARKER_TERMINATOR + "\n");

			// all exporters
			writer.write(Constants.PLUGIN_DEPENDENCY_EXPORTERS + "\n");
			for (String s : exp) {
				writer.write(s + "\n");
			}
			writer.write(Constants.MARKER_TERMINATOR + "\n");

			// all importers not satisfied
						writer.write(Constants.PLUGIN_DEPENDENCY_IMPORTERS_UNSATISFIED + "\n");
						writer.write((0==exp.size()? true+", "+imp.size()+" importer(s) hungry ":false+", "+exp.size()+ (1<exp.size()? " exporters!! PLURAL??":" exporter")+" available ")+"\n");
						writer.write(Constants.MARKER_TERMINATOR + "\n");

						// all exporters   whose exports were not needed by anyone.
						writer.write(Constants.PLUGIN_DEPENDENCY_EXPORTERS_UNSATISFIED + "\n");
						writer.write((0==imp.size()? true+", "+exp.size()+ (1<exp.size()? " exporters!! PLURAL??":" exporter")+" eagerly available ":false+", "+imp.size()+" importer(s) hungry ")+"\n");
						writer.write(Constants.MARKER_TERMINATOR + "\n");

			// terminating the key ( type /class name )
			writer.write(Constants.MARKER_TERMINATOR + "\n");
		}

		// terminating the types set
		writer.write(Constants.MARKER_TERMINATOR + "\n");
		writer.close();
		filewriter.close();

	}

	/**
	 * @param entry
	 * @param property
	 * @return  {@link Set}
	 * @throws IOException
	 */
	private static Set<String> restorePropertyFromExtract(File entry, String property) throws IOException {

		property = property.trim();
		Set<String> result = new HashSet<String>();
		BufferedReader br = new BufferedReader(new FileReader(entry));
		String line = "";
		while (null != (line = br.readLine())) {

			line = line.replace("\n", "").trim();
			if (property.equalsIgnoreCase(line)) {
				// found the property.
				// now push all the entries into the set.
				while (null != (line = br.readLine())) {
					line = line.replace("\n", "").trim();
					if (Constants.MARKER_TERMINATOR.equalsIgnoreCase(line)) {
						break;
					}
					result.add(line);
				}
				break;
			}
		}
		return result;
	}
}

class ImpExp {
	protected Set<String> imp = new HashSet<String>();
	protected Set<String> exp = new HashSet<String>();
}
