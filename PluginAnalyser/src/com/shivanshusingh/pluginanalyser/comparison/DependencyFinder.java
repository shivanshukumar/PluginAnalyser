package com.shivanshusingh.pluginanalyser.comparison;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.shivanshusingh.pluginanalyser.utils.Util;
import com.shivanshusingh.pluginanalyser.utils.logging.Log;
import com.shivanshusingh.pluginanalyser.utils.parsing.Constants;
import com.shivanshusingh.pluginanalyser.utils.parsing.ParsingUtil;

/**
 * @author Shivanshu Singh
 * 
 */
public class DependencyFinder {

	static Map<String, ImpExp> functions = new HashMap<String, ImpExp>();
	static  Set<String>  pluginExtractsIgnored = new   HashSet  <String>    ();
	static Map<String, ImpExp> types = new HashMap<String, ImpExp>();

	/**
	 * 
	 * this function build the superset of dependencies from the extract files
	 * to finally figure out what dependencies were unstated or missing.
	 * 
	 * @param pathToBasePluginExtractsDir
	 * @param pathToPluginDependencyAnalysisOutputLocation
	 * @param considerBundleExportersOnly
	 *            true if only those exports to consider that are being
	 *            explicitly being specified as exported by plugins in their
	 *            plugin manifests. CAUTION: Exercise caution In case the bundle
	 *            manifest information listing exported packages is missing,
	 *            setting this to true in this case will essentially mean that
	 *            the plugin has no exports whatsoever.
	 * @param eraseOld
	 * @throws IOException
	 */
	public static void buildPluginDependencySuperSet(String pathToBasePluginExtractsDir,
			String pathToPluginDependencyAnalysisOutputLocation, boolean considerBundleExportersOnly, boolean eraseOld)
			throws IOException {

		Log.outln("==== Now Building the  Plugin Dependency  Set from source: " + pathToBasePluginExtractsDir
				+ " , considerBundleExportsOnly is " + considerBundleExportersOnly + " ====");
		Log.errln("==== Now Building the  Plugin Dependency  Set from source: " + pathToBasePluginExtractsDir
				+ " , considerBundleExportsOnly is " + considerBundleExportersOnly + " ====");

		long time1 = System.currentTimeMillis();

		File pluginExtractDirectory = new File(pathToBasePluginExtractsDir);

		if (!Util.checkDirectory(pluginExtractDirectory, true, true, true, false)) {
			// the plugin extracts source base directory is not accessible.
			Log.errln("xxxx in buildPluginDependencySuperSet \\\n the plugin dir: "
					+ pluginExtractDirectory.getAbsolutePath()
					+ "\n is not a directory or not readable or   does not exist. \nxxxx");
			return;
		}
		if (!Util.checkAndCreateDirectory(pathToPluginDependencyAnalysisOutputLocation)) {
			Log.errln("xxxx in buildPluginDependencySuperSet \\\n the output location dir: "
					+ pathToPluginDependencyAnalysisOutputLocation + "\n is not accessible. \nxxxx");
			return;
		}

		if (eraseOld) {
			Util.clearFolder(new File(pathToPluginDependencyAnalysisOutputLocation));
		}
		File[] pluginExtractEntries = pluginExtractDirectory.listFiles();
		int entriesLength = pluginExtractEntries.length;
		if (entriesLength < 2) {

			Log.errln("XXXX  \n "
					+ pluginExtractDirectory.getAbsolutePath()
					+ "\n has has than 2 extracts, cannot compare.\n The location must have at least 2 extract files.  \nXXXX  ");
		}

		long pluginExtractsDone = 0;

		for (File pluginExtract : pluginExtractEntries) {
			pluginExtractsDone++;
			if (Util.checkFile(pluginExtract, true, true, true, false)) {
				/**
				 * this is the name of the PluginExtract file, without the
				 * extension. this is for obtaining the fully qualified plugin
				 * name with version and qualifier, even if that information is
				 * not in the pluginExtract file, cause because of an originally
				 * missing manifest.mf file for thus plugin.
				 */
				String thisPluginExtractName = pluginExtract.getName().trim();
				int startingIndex = thisPluginExtractName.indexOf(Constants.EXTRACT_FILE_PREFIX_PLUGIN)
						+ Constants.EXTRACT_FILE_PREFIX_PLUGIN.length();
				thisPluginExtractName = thisPluginExtractName.substring(startingIndex);
				if (thisPluginExtractName.toLowerCase().endsWith(Constants.EXTRACT_FILE_EXTENSION_PLUGIN))
					thisPluginExtractName = thisPluginExtractName.substring(0, thisPluginExtractName.length()
							- Constants.EXTRACT_FILE_EXTENSION_PLUGIN.length());
				// Log.outln("==== Adding to DependencySet,  plugin " +
				// pluginExtractsDone + " of " + entriesLength + " : "
				// + thisPluginExtractName + "====");

				// restoring the functions information from the file.
				
				boolean ignorePluginExtract = false;
				Set<String> ignoreBundleProperty = ParsingUtil.restorePropertyFromExtract(pluginExtract,
						Constants.BUNDLE_IGNORE);
				if (null != ignoreBundleProperty && 1 == ignoreBundleProperty.size())
					ignorePluginExtract = ignoreBundleProperty.toString().toLowerCase().trim().contains("true");

					//System.out.println("==== ignoreBundleProperty= "+ignoreBundleProperty.toString());
				if(ignorePluginExtract)
				{
					Log.outln("==== "+thisPluginExtractName+" must be IGNORED");
					Log.errln("==== "+thisPluginExtractName+" must be IGNORED");
					pluginExtractsIgnored.add(thisPluginExtractName);
					continue;
				
				}
				
				Set<String> bundleExports = new HashSet<String>();
				if (considerBundleExportersOnly) {
					bundleExports = ParsingUtil.restorePropertyFromExtract(pluginExtract, Constants.BUNDLE_EXPORTS);
				}

				Set<String> myMethodExports = ParsingUtil.restorePropertyFromExtract(pluginExtract,
						Constants.PLUGIN_ALL_MY_METHODS_PUBLIC);
				Set<String> myMethodImports = ParsingUtil.restorePropertyFromExtract(pluginExtract,
						Constants.PLUGIN_ALL_MY_METHOD_CALLS_EXTERNAL_AND_NON_JAVA);
				Set<String> myTypeExports = ParsingUtil.restorePropertyFromExtract(pluginExtract,
						Constants.PLUGIN_ALL_MY_TYPES_PUBLIC);
				Set<String> myTypeImports = ParsingUtil.restorePropertyFromExtract(pluginExtract,
						Constants.PLUGIN_ALL_TYPES_DETECTED_EXTERNAL_AND_NON_JAVA);

				// merging functions
				for (String s : myMethodExports) {
					s = s.trim();
					if (1 <= s.length()) {
						ImpExp impexp = new ImpExp();

						if (functions.containsKey(s))
							impexp = functions.get(s);

						boolean flag_isExported = true;

						if (considerBundleExportersOnly) {
							flag_isExported = false;
							String funcWithoutReturnType = s.split(" ")[1].trim();
							for (String a : bundleExports) {
								a = a.trim();
								if (1 <= a.length()) {
									// getting the class / package name from the
									// bundle export entry.
									// getting split on ";" first.
									String b = a;
									a = a.split(";")[0].trim();
									a = a.split(Constants.BUNDLE_DEPDENDENCY_KEYWORD_OPTIONAL)[0].trim();
									if (1 < a.length()) {
										// Log.outln("******** Checking if "+funcWithoutReturnType+" starts with "+a.trim() + ".\n  |  original invokation:"+s+"    original bundleExportEntry:"+b);
										if (funcWithoutReturnType.startsWith(a.trim() + ".")) {
											// Log.outln("************* YES DOES.");
											flag_isExported = true;
											break;
										}
									}
								}
							}
						}
						if (flag_isExported) {
							// Log.outln("*********** adding to exports:"+thisPluginExtractName+
							// "    for invokation: "+s);
							impexp.addToExp(thisPluginExtractName);
							functions.put(s, impexp);
						}
					}
				}

				for (String s : myMethodImports) {
					ImpExp impexp = new ImpExp();
					if (functions.containsKey(s))
						impexp = functions.get(s);

					impexp.addToImp(thisPluginExtractName);
					functions.put(s, impexp);
				}

				// merging types (classes)
				for (String s : myTypeExports) {
					ImpExp impexp = new ImpExp();
					if (types.containsKey(s))
						impexp = types.get(s);
					impexp.addToExp(thisPluginExtractName);
					types.put(s, impexp);
				}

				for (String s : myTypeImports) {
					ImpExp impexp = new ImpExp();
					if (types.containsKey(s))
						impexp = types.get(s);
					impexp.addToImp(thisPluginExtractName);
					types.put(s, impexp);
				}
				// System.out.println(types.keySet().toString());

				if (pluginExtractsDone % 200 == 0 || pluginExtractsDone == entriesLength) {
					Log.outln("#### PluginExtractsMerged \t= " + pluginExtractsDone);

					Log.outln("#### functions \tobjectSize= " + (double) (functions.toString().length() / (1024 * 1024))
							+ "MB");
					Log.outln("#### types \t\tobjectSize= " + (types.toString().length() / (1024 * 1024)) + "MB");
				}

			}

		}
		// write out the merged file

		writeData(pathToPluginDependencyAnalysisOutputLocation);

		long time2 = System.currentTimeMillis();
		Log.outln("Dependency Set Creation for " + entriesLength + " plugin extracts, at plugin extract src  :  "
				+ pathToBasePluginExtractsDir + "  time: " + Util.getFormattedTime(time2 - time1));
		Log.errln("Dependency Set Creation for " + entriesLength + " plugin extracts, at plugin extract src  :  "
				+ pathToBasePluginExtractsDir + "  time: " + Util.getFormattedTime(time2 - time1));

	}

	private static void writeData(String pluginDependencySetOutputLocationPath) throws IOException {

		Set<String> unmatchedFunctionImports = new HashSet<String>();
		Set<String> unmatchedTypeImports = new HashSet<String>();

		if (!Util.checkAndCreateDirectory(pluginDependencySetOutputLocationPath)) {
			Log.errln("xxxx\n in writeData() in   DependencyFinder, the output location: "
					+ pluginDependencySetOutputLocationPath + "  \n is not accessible, cannot record data.  \nxxxx");
			return;
		}

		File functionFile = new File(pluginDependencySetOutputLocationPath + "/"
				+ Constants.DEPENDENCY_SET_FILE_PREFIX_PLUGIN + "functions"
				+ Constants.DEPENDENCY_SET_FILE_EXTENSION_PLUGIN);
		File typeFile = new File(pluginDependencySetOutputLocationPath + "/" + Constants.DEPENDENCY_SET_FILE_PREFIX_PLUGIN
				+ "types" + Constants.DEPENDENCY_SET_FILE_EXTENSION_PLUGIN);

		// writing functions set.

		FileWriter filewriter = new FileWriter(functionFile);
		BufferedWriter writer = new BufferedWriter(filewriter);

		writer.write(Constants.PLUGIN_DEPENDENCY_ALL_FUNCTIONS + "\n");

		for (String key : functions.keySet()) {

			writer.write(Constants.DELIM_PLUGIN_DEPENDENCY_ELEMENT_SET + "\n");

			// writing the function signature.
			writer.write(key + "\n");

			ImpExp impexp = functions.get(key);

			Set imp = impexp.getImp();
			Set exp = impexp.getExp();

			// all importers
			writer.write(Constants.PLUGIN_DEPENDENCY_IMPORTERS + "\n");
			for (Object s : imp) {
				writer.write((String) s + "\n");
			}
			writer.write(Constants.MARKER_TERMINATOR + "\n");

			// all exporters
			writer.write(Constants.PLUGIN_DEPENDENCY_EXPORTERS + "\n");
			for (Object s : exp) {
				writer.write((String) s + "\n");
			}
			writer.write(Constants.MARKER_TERMINATOR + "\n");

			// all importers not satisfied
			writer.write(Constants.PLUGIN_DEPENDENCY_IMPORTERS_UNSATISFIED + "\n");
			writer.write((0 == exp.size() ? true + ", " + imp.size() + " importer(s) hungry " : false + ", " + exp.size()
					+ (1 < exp.size() ? " exporters!! PLURAL??" : " exporter") + " available ")
					+ "\n");
			writer.write(Constants.MARKER_TERMINATOR + "\n");

			// add to the unmatchedFunctionImports Set.
			if (0 == exp.size())
				unmatchedFunctionImports.add(key);

			// all exporters whose exports were not needed by anyone
			writer.write(Constants.PLUGIN_DEPENDENCY_EXPORTERS_UNSATISFIED + "\n");
			writer.write((0 == imp.size() ? true + ", " + exp.size()
					+ (1 < exp.size() ? " exporters!! PLURAL??" : " exporter") + " eagerly available " : false + ", "
					+ imp.size() + " importer(s) hungry ")
					+ "\n");
			writer.write(Constants.MARKER_TERMINATOR + "\n");

			// terminating the key ( function name )
			writer.write(Constants.MARKER_TERMINATOR + "\n");
		}
		writer.write(Constants.PLUGIN_DEPENDENCY_ALL_UNMATCHED_FUNCTION_IMPORTS + "\n");
		for (String s : unmatchedFunctionImports)
			writer.write(s + "\n");
		writer.write("COUNT=" + unmatchedFunctionImports.size() + "\n");
		writer.write(Constants.MARKER_TERMINATOR + "\n");
		
		writer.write(Constants.PLUGIN_DEPENDENCY_ALL_IGNORED_PLUGINS + "\n");
		for (String s : pluginExtractsIgnored)
			writer.write(s + "\n");
		writer.write("COUNT=" + pluginExtractsIgnored.size() + "\n");
		writer.write(Constants.MARKER_TERMINATOR + "\n");

		// terminating the functions set
		writer.write(Constants.MARKER_TERMINATOR + "\n");

		writer.close();
		filewriter.close();

		// writing types set.

		filewriter = new FileWriter(typeFile);
		writer = new BufferedWriter(filewriter);

		writer.write(Constants.PLUGIN_DEPENDENCY_ALL_TYPES + "\n");

		for (String key : types.keySet()) {

			writer.write(Constants.DELIM_PLUGIN_DEPENDENCY_ELEMENT_SET + "\n");

			// writing the type signature.
			writer.write(key + "\n");

			ImpExp impexp = types.get(key);

			Set imp = impexp.getImp();
			Set exp = impexp.getExp();

			// all importers
			writer.write(Constants.PLUGIN_DEPENDENCY_IMPORTERS + "\n");
			for (Object s : imp) {
				writer.write((String) s + "\n");
			}
			writer.write(Constants.MARKER_TERMINATOR + "\n");

			// all exporters
			writer.write(Constants.PLUGIN_DEPENDENCY_EXPORTERS + "\n");
			for (Object s : exp) {
				writer.write((String) s + "\n");
			}
			writer.write(Constants.MARKER_TERMINATOR + "\n");

			// all importers not satisfied
			writer.write(Constants.PLUGIN_DEPENDENCY_IMPORTERS_UNSATISFIED + "\n");
			writer.write((0 == exp.size() ? true + ", " + imp.size() + " importer(s) hungry " : false + ", " + exp.size()
					+ (1 < exp.size() ? " exporters!! PLURAL??" : " exporter") + " available ")
					+ "\n");
			writer.write(Constants.MARKER_TERMINATOR + "\n");

			// add to the unmatchedTypeImports Set.
			if (0 == exp.size())
				unmatchedTypeImports.add(key);

			// all exporters whose exports were not needed by anyone.
			writer.write(Constants.PLUGIN_DEPENDENCY_EXPORTERS_UNSATISFIED + "\n");
			writer.write((0 == imp.size() ? true + ", " + exp.size()
					+ (1 < exp.size() ? " exporters!! PLURAL??" : " exporter") + " eagerly available " : false + ", "
					+ imp.size() + " importer(s) hungry ")
					+ "\n");
			writer.write(Constants.MARKER_TERMINATOR + "\n");

			// terminating the key ( type /class name )
			writer.write(Constants.MARKER_TERMINATOR + "\n");
		}
		writer.write(Constants.PLUGIN_DEPENDENCY_ALL_UNMATCHED_TYPE_IMPORTS + "\n");
		for (String s : unmatchedTypeImports)
			writer.write(s + "\n");
		writer.write("COUNT=" + unmatchedTypeImports.size() + "\n");
		
		writer.write(Constants.MARKER_TERMINATOR + "\n");
		
		writer.write(Constants.PLUGIN_DEPENDENCY_ALL_IGNORED_PLUGINS + "\n");
		for (String s : pluginExtractsIgnored)
			writer.write(s + "\n");
		writer.write("COUNT=" + pluginExtractsIgnored.size() + "\n");
		writer.write(Constants.MARKER_TERMINATOR + "\n");

		// terminating the types set
		writer.write(Constants.MARKER_TERMINATOR + "\n");
		writer.close();
		filewriter.close();

	}
}
