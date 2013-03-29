package com.shivanshusingh.pluginanalyser.comparison;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import javax.media.j3d.SpotLight;

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
	static Set<String> pluginExtractsIgnored = new HashSet<String>();
	static Map<String, ImpExp> types = new HashMap<String, ImpExp>();

	// sets of plugins
	static Map<String, PluginObject> plugins = new HashMap<String, PluginObject>();

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
	 * @param ignoreBundlesMarkedToBeIgnored TODO
	 * @param eraseOld
	 * @throws IOException
	 */
	public static void buildPluginDependencySuperSet(String pathToBasePluginExtractsDir,
			String pathToPluginDependencyAnalysisOutputLocation, boolean considerBundleExportersOnly, boolean ignoreBundlesMarkedToBeIgnored, boolean eraseOld)
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
				if(ignoreBundlesMarkedToBeIgnored)
				{
					if (null != ignoreBundleProperty && 1 == ignoreBundleProperty.size())
						ignorePluginExtract = ignoreBundleProperty.toString().toLowerCase().trim().contains("true");
				}
				// System.out.println("==== ignoreBundleProperty= "+ignoreBundleProperty.toString());
				if (ignorePluginExtract) {
					Log.outln("==== " + thisPluginExtractName + " must be IGNORED");
					Log.errln("==== " + thisPluginExtractName + " must be IGNORED");
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
				Set<String> superClassesAndInterfacesSuperSet = ParsingUtil.restorePropertyFromExtract(pluginExtract,
						Constants.PLUGIN_ALL_INHERITANCE_AND_INTERFACE_PAIRS);

				// //////////////////////////////////////////
				// building DependencyFinder.plugins object
				// /////////////////////////////////////////

				PluginObject po = new PluginObject();
				if (plugins.containsKey(thisPluginExtractName))
					po = plugins.get(thisPluginExtractName);

				po.name = thisPluginExtractName;

				// building the superclassAndInterface SuperSet
				for (String pair : superClassesAndInterfacesSuperSet) {
					String[] pairElements = pair.split(Constants.DELIM_PLUGIN_ELEMENT_SUPERCLASS_INTERFACE);
					String baseType = pairElements[0].trim();
					String superType = pairElements[1].trim();

					Set<String> depTypes = new HashSet<String>();
					if (po.superClassesAndInterfaces.containsKey(baseType))
						depTypes = po.superClassesAndInterfaces.get(baseType);
					depTypes.add(superType);
					po.superClassesAndInterfaces.put(baseType, depTypes);

				}

				// building the plugins object for imports. (combining type and
				// function information together)

				po.imports.addAll(myMethodImports);
				po.imports.addAll(myTypeImports);

				// merging functions
				for (String myMethodExport : myMethodExports) {
					myMethodExport = myMethodExport.trim();
					if (1 <= myMethodExport.length()) {
						ImpExp impexp = new ImpExp();

						if (functions.containsKey(myMethodExport))
							impexp = functions.get(myMethodExport);

						boolean flag_isExported = true;

						if (considerBundleExportersOnly) {
							flag_isExported = false;

							// String funcClassName = s.split(" ")[1].trim();
							String[] funcNameElements = separateFuncNameElements(myMethodExport);

							String funcRetType = funcNameElements[0];
							String funcClassName = funcNameElements[1];
							String funcName = funcNameElements[2];

							for (String bundleExport : bundleExports) {
								bundleExport = bundleExport.trim();
								if (1 <= bundleExport.length()) {

									bundleExport = getBundleExportNameFromBundleExportEntry(bundleExport);
									if (1 < bundleExport.length()) {
										// Log.outln("******** Checking if "+funcWithoutReturnType+" starts with "+a.trim()
										// +
										// ".\n  |  original invokation:"+s+"    original bundleExportEntry:"+b);
										if (funcClassName.startsWith(bundleExport.trim() + ".")) {
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
							// "    for invokation: "+ myMethodExport);
							impexp.addToExp(thisPluginExtractName);
							functions.put(myMethodExport, impexp);

							// building the plugins object for exports.
							po.exports.add(myMethodExport.trim());

						}
					}
				}

				for (String s : myMethodImports) {
					s = s.trim();
					if (1 <= s.length()) {

						ImpExp impexp = new ImpExp();
						if (functions.containsKey(s))
							impexp = functions.get(s);

						impexp.addToImp(thisPluginExtractName);
						functions.put(s, impexp);

					}
				}

				// merging types (classes)
				for (String myTypeExport : myTypeExports) {
					myTypeExport = myTypeExport.trim();
					if (1 <= myTypeExport.length()) {
						ImpExp impexp = new ImpExp();
						if (types.containsKey(myTypeExport))
							impexp = types.get(myTypeExport);
						boolean flag_isExported = true;

						if (considerBundleExportersOnly) {
							flag_isExported = false;

							for (String bundleExport : bundleExports) {
								bundleExport = bundleExport.trim();
								if (1 <= bundleExport.length()) {

									bundleExport = getBundleExportNameFromBundleExportEntry(bundleExport);
									if (1 < bundleExport.length()) {

										if (myTypeExport.startsWith(bundleExport.trim() + ".")) {
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
							// "    for type: "+myTypeExport);
							impexp.addToExp(thisPluginExtractName);
							types.put(myTypeExport, impexp);

							// building the plugins object for exports.
							po.exports.add(myTypeExport.trim());
						}
					}
				}

				for (String s : myTypeImports) {
					s = s.trim();
					if (1 <= s.length()) {
						ImpExp impexp = new ImpExp();
						if (types.containsKey(s))
							impexp = types.get(s);
						impexp.addToImp(thisPluginExtractName);
						types.put(s, impexp);
					}
				}
				// System.out.println(types.keySet().toString());

				if (pluginExtractsDone % 250 == 0 || pluginExtractsDone == entriesLength) {
					Log.outln("#### PluginExtractsMerged \t= " + pluginExtractsDone);

					int functionsMemSize = functions.toString().length() / (1024 * 1024);
					int typesMemSize = types.toString().length() / (1024 * 1024);
					int pluginsMemSize = (plugins.toString().length() + po.toString().length()) / (1024 * 1024);

					Log.outln("## functions \tobjectSize\t= " + functionsMemSize + "MB");
					Log.outln("## types \t\tobjectSize\t= " + typesMemSize + "MB");
					Log.outln("## plugins \tobjectSize\t= " + pluginsMemSize + "MB");
					Log.outln("## TOTAL 3 \tobjectSize\t= " + (functionsMemSize + typesMemSize + pluginsMemSize) + "MB");
					Log.outln("## time (merging) so far \t= "
							+ Util.getFormattedTime(System.currentTimeMillis() - time1));
				}

				// adding the constructed object to the DependencyFinder.plugins
				// object.
				plugins.put(thisPluginExtractName, po);

				// ///////////////// done with adding the current plugin
				// information to the DependencyFinder.plugins object /////////
			}
		}

		// ///////// now doing the circular inter plugin dependency analysis.

		Set<Entry<String, PluginObject>> pluginEntrySet = plugins.entrySet();
		for (Entry<String, PluginObject> entry : pluginEntrySet) {
			PluginObject pluginObj = entry.getValue();
			for (String imp : pluginObj.imports) {
				String exporterSequencesString = "";
				exporterSequencesString = findExporters(imp);

				// now if there were any transitive exporters, check for that
				// and mark the import as satisfied.
				Set<String> finalExporters = new HashSet<String>();
				finalExporters = getFinalExporterInSeq(exporterSequencesString);

				Set<String> exporterSequences = new HashSet<String>();
				exporterSequences = getExporterSequences(exporterSequencesString);

				// recording the data in the functions or types objects.
				if (functions.containsKey(imp)) {
					ImpExp impexp = functions.get(imp);

					impexp.exporterSequences.addAll(exporterSequences);

					for (String s : finalExporters) {
						impexp.addToExp(s);
					}
					functions.put(imp, impexp);
				} else if (types.containsKey(imp)) {
					ImpExp impexp = types.get(imp);

					impexp.exporterSequences.addAll(exporterSequences);

					for (String s : finalExporters) {
						impexp.addToExp(s);
					}
					functions.put(imp, impexp);

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

	/**
	 * 
	 * gets a set of exporter sequences from the main exporterSequencesString
	 * 
	 * @param exporterSequencesString
	 * @return {@link Set<{@link String}>}
	 */
	private static Set<String> getExporterSequences(String exporterSequencesString) {
		Set<String> sequences = new HashSet<String>();
		String[] exporterSequences = exporterSequencesString.split(Constants.DELIM_PLUGIN_DEPENDENCY_TRANSITIVE_TYPE_NEW);
		if (null != exporterSequences && 1 <= exporterSequences.length) {
			for (String s : exporterSequences) {
				if (null != s && 1 <= s.length()) {
					s = s.trim();
					String s1 = s.replaceAll("[" + Constants.DELIM_PLUGIN_DEPENDENCY_TRANSITIVE_TYPE_END + "]+", "");
					if (null != s1 && 1 <= s1.length() && null != s && 1 <= s.length())
						sequences.add(s);
				}
			}
		}
		return sequences;
	}

	/**
	 * gets the final elements in the exporter sequences, which were deemed to
	 * satisfy an import. e.g. a====>b====>c;d====>e====>f====>g;h====>i====>
	 * will result in the output being:[c, g] in the Set being returned, the
	 * rest will be ignored.
	 */
	private static Set<String> getFinalExporterInSeq(String exportersSequences) {

		Set<String> finalExporters = new HashSet<String>();
		if (null != exportersSequences) {
			exportersSequences = exportersSequences.replaceAll("[" + Constants.DELIM_PLUGIN_DEPENDENCY_TRANSITIVE_TYPE_NEW
					+ "]+", "");
			exportersSequences = exportersSequences.replaceAll("[" + Constants.DELIM_PLUGIN_DEPENDENCY_TRANSITIVE_TYPE_END
					+ "]+", "");
			if (1 <= exportersSequences.length()) {
				String[] exporterSequences = exportersSequences
						.split(Constants.DELIM_PLUGIN_DEPENDENCY_TRANSITIVE_TYPE_SUCCESS);
				if (!exportersSequences.equals(exporterSequences[0])) {
					// this means that there were some finals.
					for (int x = 0; x < exporterSequences.length; x++) {
						String[] exporterElements = exporterSequences[x]
								.split(Constants.DELIM_PLUGIN_DEPENDENCY_TRANSITIVE_TYPE_INTERIM);
						String finalExporter = exporterElements[exporterElements.length - 1];
						if (!finalExporter.contains(Constants.DELIM_PLUGIN_DEPENDENCY_TRANSITIVE_TYPE_INTERIM))
							finalExporters.add(finalExporter.trim());
					}
				}
			}
		}
		return finalExporters;
	}

	/**
	 * return 1 is the input is a function entry , 2 in case of a type entry and
	 * 0 otherwise.
	 * 
	 * @param imp
	 * @return
	 */
	private static int getEntryType(String str) {
		if (null == str || "".equalsIgnoreCase(str.trim()))
			return 0;
		str = str.trim();
		String[] splits = str.split(" ");
		if (splits.length >= 2)
			return 1;
		else

			return 2;
	}

	/**
	 * helper to store already traversed imports, helping DependencyFinder.
	 * findExporters
	 */
	private static Map<String, Set<Set<String>>> exporterSetsCache = new HashMap<String, Set<Set<String>>>();

	/**
	 * recursively finds the exporter sets of the given imports.
	 * 
	 * @param imp
	 * @return
	 */
	private static String findExporters(String imp) {
		// if(exporterSetsCache.containsKey(imp))
		// return exporterSetsCache.get(imp);

		String result = "";
		imp = imp.trim();
		int impType = getEntryType(imp);
		if (0 != impType) {
			String classname = imp;

			/*
			 * val targetPlugins: Set[Plugin] =
			 * typesDefined.getOrElse(classname, Set())
			 */
			if (1 == impType) {
				// it is a function, so get the class name.
				String[] funcElements = separateFuncNameElements(imp);
				classname = funcElements[1];// class name.

			}
			Set<PluginObject> targetPlugins = new HashSet<PluginObject>();
			targetPlugins = getTargetPlugins(classname);
			for (PluginObject targetPlugin : targetPlugins) {
				if (null != targetPlugin) {
					if (null != targetPlugin.exports && targetPlugin.exports.contains(imp)) {

						result += targetPlugin.name + Constants.DELIM_PLUGIN_DEPENDENCY_TRANSITIVE_TYPE_SUCCESS;
						// System.out.println("result: "+result);

					} else {
						if (null != targetPlugin.superClassesAndInterfaces) {
							Set<String> superclasses = targetPlugin.superClassesAndInterfaces.get(classname);
							for (String superclass : superclasses) {
								String newImp = imp.replace(classname, superclass);
								result += targetPlugin.name + Constants.DELIM_PLUGIN_DEPENDENCY_TRANSITIVE_TYPE_INTERIM
										+ findExporters(newImp);
								// System.out.println("result_branch: "+result);
							}
							/*
							 * val superclasses =
							 * targetPlugin.inheritance.getOrElse(classname,
							 * Set())
							 * 
							 * for (superclass <- superclasses) result = result
							 * ++ findExports((superclass, funname)).map(_ +
							 * targetPlugin)
							 */
						}
					}
				}
			}
		}

		return (result + Constants.DELIM_PLUGIN_DEPENDENCY_TRANSITIVE_TYPE_END + Constants.DELIM_PLUGIN_DEPENDENCY_TRANSITIVE_TYPE_NEW)
				.replaceAll("[" + Constants.DELIM_PLUGIN_DEPENDENCY_TRANSITIVE_TYPE_NEW + "]+",
						Constants.DELIM_PLUGIN_DEPENDENCY_TRANSITIVE_TYPE_NEW).replaceAll(
						"[" + Constants.DELIM_PLUGIN_DEPENDENCY_TRANSITIVE_TYPE_END + "]+",
						Constants.DELIM_PLUGIN_DEPENDENCY_TRANSITIVE_TYPE_END);

	}

	/**
	 * gets a set of PluginsObject for the plugins that export the said
	 * classname.
	 * 
	 * @param classname
	 * @return
	 */
	private static Set<PluginObject> getTargetPlugins(String classname) {
		Set<PluginObject> result = new HashSet<PluginObject>();

		// getting the list of plugin names that export this type.
		if (types.containsKey(classname)) {
			Set allPluginExporters = types.get(classname).getExp();
			for (Object pluginName : allPluginExporters) {
				// getting the PluginObjects for all the plugin names obtained.
				PluginObject pobj = plugins.get((String) pluginName);
				result.add(pobj);
			}

		}
		return result;
	}

	/**
	 * @param bundleExport
	 * @return
	 */
	private static String getBundleExportNameFromBundleExportEntry(String bundleExport) {
		// getting the class / package name from the
		// bundle export entry.
		// getting split on ";" first.
		bundleExport = bundleExport.split(";")[0].trim();
		bundleExport = bundleExport.split(Constants.BUNDLE_DEPDENDENCY_KEYWORD_OPTIONAL)[0].trim();
		return bundleExport;
	}

	/**
	 * separates the function class from function name. e.g. if the input is:
	 * org.s.G com.x.A.foo () returned: classAndFuncName[0]=org.s.G
	 * classAndFuncName[1]=com.x.A classAndFuncName[3]=foo ()
	 * 
	 * @param funcSignature
	 * @return
	 */
	private static String[] separateFuncNameElements(String funcSignature) {
		String[] classAndFuncName = new String[3];
		String[] spaceSplits = funcSignature.split(" ");

		// returnType
		classAndFuncName[0] = spaceSplits[0].trim();

		String[] dotSplits = spaceSplits[1].trim().split("\\.");

		// function name and parameters
		classAndFuncName[2] = dotSplits[dotSplits.length - 1].trim() + " " + spaceSplits[2].trim();

		// class name
		classAndFuncName[1] = dotSplits[0].trim();
		for (int x = 1; x < dotSplits.length - 1; x++)
			classAndFuncName[1] += "." + dotSplits[x].trim();

		return classAndFuncName;
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
		Log.outln(Constants.PLUGIN_DEPENDENCY_ALL_FUNCTIONS + "\n");

		long counter=0;
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

			writer.write(Constants.PLUGIN_DEPENDENCY_EXPORTERS_SETS + "\n");
			for (String s : impexp.exporterSequences) {
				writer.write((String) s + "\n");
			}
			writer.write(Constants.MARKER_TERMINATOR + "\n");

			// terminating the key ( function name )
			writer.write(Constants.MARKER_TERMINATOR + "\n");
			counter++;
			if(counter%500==0)
				Log.outln("## functions written: "+counter);
		}
		writer.write(Constants.PLUGIN_DEPENDENCY_ALL_UNMATCHED_FUNCTION_IMPORTS + "\n");
		Log.outln(Constants.PLUGIN_DEPENDENCY_ALL_UNMATCHED_FUNCTION_IMPORTS + "\n");
	for (String s : unmatchedFunctionImports)
			writer.write(s + "\n");
		writer.write("COUNT=" + unmatchedFunctionImports.size() + "\n");
		writer.write(Constants.MARKER_TERMINATOR + "\n");

		writer.write(Constants.PLUGIN_DEPENDENCY_ALL_IGNORED_PLUGINS + "\n");
		Log.outln(Constants.PLUGIN_DEPENDENCY_ALL_IGNORED_PLUGINS + "\n");

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
		
		Log.outln(Constants.PLUGIN_DEPENDENCY_ALL_TYPES + "\n");

		 counter=0;

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

			writer.write(Constants.PLUGIN_DEPENDENCY_EXPORTERS_SETS + "\n");
			for (String s : impexp.exporterSequences) {
				writer.write((String) s + "\n");
			}
			writer.write(Constants.MARKER_TERMINATOR + "\n");

			// terminating the key ( type /class name )
			writer.write(Constants.MARKER_TERMINATOR + "\n");
			
			counter++;
			if(counter%500==0)
				Log.outln("## types written: "+counter);
		
		}
		
		writer.write(Constants.PLUGIN_DEPENDENCY_ALL_UNMATCHED_TYPE_IMPORTS + "\n");
		Log.outln(Constants.PLUGIN_DEPENDENCY_ALL_UNMATCHED_TYPE_IMPORTS + "\n");

		for (String s : unmatchedTypeImports)
			writer.write(s + "\n");
		writer.write("COUNT=" + unmatchedTypeImports.size() + "\n");

		writer.write(Constants.MARKER_TERMINATOR + "\n");

		writer.write(Constants.PLUGIN_DEPENDENCY_ALL_IGNORED_PLUGINS + "\n");
		Log.outln(Constants.PLUGIN_DEPENDENCY_ALL_IGNORED_PLUGINS + "\n");

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
