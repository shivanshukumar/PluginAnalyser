package com.shivanshusingh.pluginanalyser.comparison;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.shivanshusingh.pluginanalyser.utils.Util;
import com.shivanshusingh.pluginanalyser.utils.Version;
import com.shivanshusingh.pluginanalyser.utils.VersionRange;
import com.shivanshusingh.pluginanalyser.utils.logging.Log;
import com.shivanshusingh.pluginanalyser.utils.parsing.Constants;
import com.shivanshusingh.pluginanalyser.utils.parsing.FuncSig;
import com.shivanshusingh.pluginanalyser.utils.parsing.ParsingUtil;
import com.shivanshusingh.pluginanalyser.utils.parsing.EclipsePlatform;

/**
 * @author Shivanshu Singh
 * 
 */
public class DependencyFinder {

	// Map for: Plugin Name (symbolic name) =>
	// pluginId(symbolicname<%version.qualifier%>) => plugin extract file names
	// set..
	private static Map<String, Map<String, Set<String>>> pluginMap = new HashMap<String, Map<String, Set<String>>>();
	private static Map<String, Map<String, Set<String>>> featureMap = new HashMap<String, Map<String, Set<String>>>();
	private static Map<String, Map<String, Set<String>>> exportedPackagesMap = new HashMap<String, Map<String, Set<String>>>();

	// sets of plugins
	static Map<String, PluginObject> plugins = new HashMap<String, PluginObject>();

	private static Map<String, ImpExp> functions = new HashMap<String, ImpExp>();
	private static Map<String, ImpExp> types = new HashMap<String, ImpExp>();

	private static Set<String> pluginExtractsIgnored = new HashSet<String>();

	// a set to collect the feature model (inter plugin and feature
	// dependencies)
	private static Set<String> dependenciesFM = new HashSet<String>();

	/**
	 * 
	 * this function build the reports of dependencies from the extract files to
	 * finally figure out what dependencies were unstated or missing. The
	 * following things get emitted : dependency report for function
	 * invokations, another one for types, feature model of inter plugin
	 * dependencies (dependencies.fm) and a constraints file (constraints.fm)
	 * for invokation impoter plugin sets implying exporter plugin set
	 * combinations.
	 * 
	 * @param pathToBasePluginExtractsDir
	 * @param pathToBaseFeatureExtractsDir
	 * @param pathToDependencyAnalysisOutputLocation
	 * @param considerBundleExportersOnly
	 *            true if only those exports to consider that are being
	 *            explicitly being specified as exported by plugins in their
	 *            plugin manifests. CAUTION: Exercise caution In case the bundle
	 *            manifest information listing exported packages is missing,
	 *            setting this to true in this case will essentially mean that
	 *            the plugin has no exports whatsoever.
	 * @param ignoreBundlesMarkedToBeIgnored
	 *            true if you want the plugins marked as to be ignored in the
	 *            Plugin Extracts, to be ignored when building the dependency,
	 *            false if you would want them to be not ignored. extracts.
	 * @param alsoConsiderInvokationSatisfactionProxies
	 *            true if the invokation proxies should be considered as well
	 *            when building the dependency set and checking if any of those
	 *            can in turn satisfy some original invokation.
	 * @param eraseOld
	 * @param ignoreVersionsInFeatureModelGeneration
	 *            true if bundle version information must be ignored when
	 *            generating the feature models and constraint files. false
	 *            otherwise.
	 * @throws IOException
	 */
	public static void buildPluginAndFeatureDependencyReports(String pathToBasePluginExtractsDir,
			String pathToBaseFeatureExtractsDir, String pathToDependencyAnalysisOutputLocation,
			boolean considerBundleExportersOnly, boolean ignoreBundlesMarkedToBeIgnored,
			boolean alsoConsiderInvokationSatisfactionProxies, boolean eraseOld,
			boolean ignoreVersionsInFeatureModelGeneration) throws IOException {

		Log.outln("==== Now Building the  Plugin and Feature Dependency Set from sources: \n  plugins: "
				+ pathToBasePluginExtractsDir + " , considerBundleExportsOnly is " + considerBundleExportersOnly
				+ "\n  features: " + pathToBaseFeatureExtractsDir + " ====");
		Log.errln("==== Now Building the  Plugin and Feature Dependency Set from sources: \n  plugins: "
				+ pathToBasePluginExtractsDir + " , considerBundleExportsOnly is " + considerBundleExportersOnly
				+ "\n  features: " + pathToBaseFeatureExtractsDir + " ====");

		long time1 = System.currentTimeMillis();

		File pluginExtractDirectory = new File(pathToBasePluginExtractsDir);
		File featureExtractDirectory = new File(pathToBaseFeatureExtractsDir);

		if (!Util.checkDirectory(pluginExtractDirectory, true, true, true, false)
				|| !Util.checkDirectory(featureExtractDirectory, true, true, true, false)) {
			// the plugin extracts source base directory is not accessible.
			Log.errln("xxxx in buildPluginDependencySuperSet \\\n the plugin dir: "
					+ pluginExtractDirectory.getAbsolutePath() + "  __OR__" + featureExtractDirectory.getAbsolutePath()
					+ "\n is(are) not a directory or not readable or   does not exist. \nxxxx");
			return;
		}

		if (!Util.checkAndCreateDirectory(pathToDependencyAnalysisOutputLocation)) {
			Log.errln("xxxx in buildPluginDependencySuperSet \\\n the output location dir: "
					+ pathToDependencyAnalysisOutputLocation + "\n is not accessible. \nxxxx");
			return;
		}

		if (eraseOld) {
			Util.clearFolder(new File(pathToDependencyAnalysisOutputLocation));
		}

		// the first order of business is to load the plugin, feature and
		// exportedpackages maps.
		// the plugin map contains the plugin name to plugin id to plugin
		// extract file name data.
		String pathToPluginMap = pathToBasePluginExtractsDir + "/" + Constants.EXTRACT_FILE_PREFIX_PLUGINMAP
				+ Constants.EXTRACT_FILE_NAME_PLUGINMAP + Constants.EXTRACT_FILE_EXTENSION_PLUGINMAP;
		File pluginMapFile = new File(pathToPluginMap);
		String pathToFeatureMap = pathToBaseFeatureExtractsDir + "/" + Constants.EXTRACT_FILE_PREFIX_FEATUREMAP
				+ Constants.EXTRACT_FILE_NAME_FEATUREMAP + Constants.EXTRACT_FILE_EXTENSION_FEATUREMAP;
		File featureMapFile = new File(pathToFeatureMap);
		String pathToExportedPackagesMap = pathToBasePluginExtractsDir + "/" + Constants.EXTRACT_FILE_PREFIX_EXPPACKAGEMAP
				+ Constants.EXTRACT_FILE_NAME_EXPPACKAGEMAP + Constants.EXTRACT_FILE_EXTENSION_EXPPACKAGEMAP;
		File exportedPackagesMapFile = new File(pathToExportedPackagesMap);

		if (!Util.checkFile(pluginMapFile, true, true, true, false)
				|| !Util.checkFile(featureMapFile, true, true, true, false)) {
			Log.errln("xxxx in buildPluginDependencySuperSet \\\n" + " NO PLUGIN  or  FEATURE MAP(s) File found at: "
					+ pluginExtractDirectory.getAbsolutePath() + "__OR__" + featureExtractDirectory.getAbsolutePath()
					+ "\n was expecting to find:" + pathToPluginMap + "__AND__" + pathToFeatureMap
					+ "  \n CANNOT CONTINUE with the Dependency Finding. \nxxxx");
			return;
		}
		if (!Util.checkFile(exportedPackagesMapFile, true, true, true, false)) {
			Log.errln("xxxx in buildPluginDependencySuperSet \\\n" + " NO ExportedPackages MAP File found at: "
					+ pluginExtractDirectory.getAbsolutePath() + "__OR__" + pathToExportedPackagesMap
					+ "  \n CANNOT CONTINUE with the Dependency Finding. \nxxxx");
			return;
		}

		// populating / loading the plugin and feature map s.
		if (!populateAllMaps(pathToPluginMap, pathToFeatureMap, pathToExportedPackagesMap)) {
			Log.errln("xxxx in buildPluginDependencySuperSet \\\n"
					+ "Error While Populating PluginMap    or    FeatureMap  "
					+ "  \n CANNOT CONTINUE with the Dependency Finding. \nxxxx");
			return;
		}

		// adding default features (plugins, features, java classes plugin,
		// platform and env parameters what have you ....to the dependencies
		// FM).
		addDefaultsToFeatureModel();

		// adding the feature -> other feature, plugin and included plugins
		// dependencies to the dependencies feature model.
		processFeatureExtracts(ignoreVersionsInFeatureModelGeneration, featureExtractDirectory);

		// now finding plugin dependencies and also building the plugin
		// dependencies FM.
		processPluginExtracts(considerBundleExportersOnly, ignoreBundlesMarkedToBeIgnored,
				alsoConsiderInvokationSatisfactionProxies, ignoreVersionsInFeatureModelGeneration, pluginExtractDirectory);

		// write out the merged file
		writeData(pathToDependencyAnalysisOutputLocation, ignoreVersionsInFeatureModelGeneration);

		long time2 = System.currentTimeMillis();
		int entriesLength = pluginExtractDirectory.listFiles().length;

		Log.outln("Dependency Set Creation for " + entriesLength + " plugin extracts, at plugin extract src  :  "
				+ pathToBasePluginExtractsDir + "  time: " + Util.getFormattedTime(time2 - time1));
		Log.errln("Dependency Set Creation for " + entriesLength + " plugin extracts, at plugin extract src  :  "
				+ pathToBasePluginExtractsDir + "  time: " + Util.getFormattedTime(time2 - time1));

	}

	/**
	 * adds the default constraints and implications etc to the FeatureModel
	 * 
	 */
	private static void addDefaultsToFeatureModel() {
		// adding the java sdk to dependenciesFM.
		if (pluginMap.containsKey(Constants.EXTRACT_FILE_NAME_JAVA_CLASSES_SDK))
			for (String javaClassesPlugin : pluginMap.get(Constants.EXTRACT_FILE_NAME_JAVA_CLASSES_SDK).keySet())
				dependenciesFM.add(javaClassesPlugin);

		// adding the platform parameter constraint to the feature model.
		String params = "";
		for (String s : EclipsePlatform._list_OS)
			params += Constants.PLATFORM_PARAM_OS + s + ",";
		params = params.substring(0, params.length() - ",".length());
		dependenciesFM.add(params.length() > 0 ? "oneOf(" + params + ")" : "");
		params = "";
		for (String s : EclipsePlatform._list_ARCH)
			params += Constants.PLATFORM_PARAM_OS + s + ",";
		params = params.substring(0, params.length() - ",".length());
		dependenciesFM.add(params.length() > 0 ? "oneOf(" + params + ")" : "");
		params = "";
		for (String s : EclipsePlatform._list_WS)
			params += Constants.PLATFORM_PARAM_OS + s + ",";
		params = params.substring(0, params.length() - ",".length());
		dependenciesFM.add(params.length() > 0 ? "oneOf(" + params + ")" : "");

	}

	/**
	 * the function name explains the functionality provided.
	 * 
	 * @param ignoreVersionsInFeatureModelGeneration
	 * @param featureExtractDirectory
	 * @throws IOException
	 */
	private static void processFeatureExtracts(boolean ignoreVersionsInFeatureModelGeneration, File featureExtractDirectory)
			throws IOException {
		long time1 = System.currentTimeMillis();
		File[] featureExtractEntries = featureExtractDirectory.listFiles();

		int entriesLength = featureExtractEntries.length;

		long featureExtractsDone = 0;

		for (File featureExtract : featureExtractEntries) {
			// System.out.println(featureExtract.getAbsolutePath());
			featureExtractsDone++;
			if (0 == featureExtractsDone % 50 || featureExtractsDone >= entriesLength) {
				Log.outln("#### FeatureExtractsMerged = " + featureExtractsDone + " of " + entriesLength + " ("
						+ (featureExtractsDone * 100 / entriesLength) + "%)");

				Log.outln("## time (merging) so far \t= " + Util.getFormattedTime(System.currentTimeMillis() - time1));
			}
			if (Util.checkFile(featureExtract, true, true, true, false)) {
				/**
				 * this is the name of the PluginExtract file, without the
				 * extension. this is for obtaining the fully qualified plugin
				 * name with version and qualifier, even if that information is
				 * not in the pluginExtract file, cause because of an originally
				 * missing manifest.mf file for thus plugin.
				 */
				String thisFeatureExtractName = featureExtract.getName().trim();

				// ignoring the ones that are not feature extracts
				if (!(thisFeatureExtractName.endsWith(Constants.EXTRACT_FILE_EXTENSION_FEATURE) && thisFeatureExtractName
						.startsWith(Constants.EXTRACT_FILE_PREFIX_FEATURE)))
					continue;

				// constructing this feature extract feature id.
				String thisFeatureId = ParsingUtil.getFeatureIdFromExtract(featureExtract);

				// construncting the platform conditional set (OS ARCH WS) for
				// this feature.
				String featureOS = ParsingUtil.restorePropertyFromExtract(featureExtract, Constants.FEATURE_OS).toString()
						.replace("[", "").replace("]", "");
				String featureARCH = ParsingUtil.restorePropertyFromExtract(featureExtract, Constants.FEATURE_ARCH)
						.toString().replace("[", "").replace("]", "");
				String featureWS = ParsingUtil.restorePropertyFromExtract(featureExtract, Constants.FEATURE_WS).toString()
						.replace("[", "").replace("]", "");

				String conditionalElement = buildPlatformConditionalElement(featureOS, featureARCH, featureWS);

				// processing the included plugins:
				addFeatureIncludedPluginsToFeatureModel(ignoreVersionsInFeatureModelGeneration, thisFeatureId,
						conditionalElement, featureExtract);

				// processing imports of the feature.
				addFeatureImportsToFeatureModel(ignoreVersionsInFeatureModelGeneration, thisFeatureId, conditionalElement,
						featureExtract);
			}
		}
	}

	/**
	 * @param ignoreVersionsInFeatureModelGeneration
	 * @param thisFeatureId
	 * @param featureConditionalElem
	 * @param featureExtract
	 * @throws IOException
	 */
	private static void addFeatureImportsToFeatureModel(boolean ignoreVersionsInFeatureModelGeneration,
			String thisFeatureId, String featureConditionalElem, File featureExtract) throws IOException {
		Set<String> myImports = ParsingUtil.restorePropertyFromExtract(featureExtract, Constants.FEATURE_IMPORTS);
		for (String myImport : myImports) {
			String[] importProps;
			String importType = "", importName = "", importVersionStr = "", importMatch = "", importPatch = "";
			importProps = myImport.split(";");
			for (int x = 0; x < importProps.length; x++) {
				switch (x) {
				case 0:
					importType = importProps[x].trim();
					break;
				case 1:
					importName = importProps[x].trim();
					break;
				case 2:
					importVersionStr = importProps[x].trim();
					break;
				case 3:
					importMatch = importProps[x].trim();
					break;
				case 4:
					importPatch = importProps[x].trim();

				default:
					break;
				}
			}

			String importNamePrefix = "";
			Map<String, Map<String, Set<String>>> targetMap;
			if (0 == "feature".compareToIgnoreCase(importType)) {
				// assigning the right kind of map to look up depending upon the
				// type of the import, feature or plugin.

				targetMap = featureMap;
				importNamePrefix = Constants._FE_;
			} else if (0 == "plugin".compareToIgnoreCase(importType))
				targetMap = pluginMap;
			else
				continue;
			if (targetMap.containsKey(importName)) {
				if (!ignoreVersionsInFeatureModelGeneration) {
					Set<String> candidateImportIds = targetMap.get(importName).keySet();
					for (String candidateImportId : candidateImportIds) {
						try {

							String candidateImportVersionStr = parseDepIdForVersion(candidateImportId);
							Version candidateImportVersion = new Version(candidateImportVersionStr);
							boolean flag_candidateImportMatches = false;

							if (importVersionStr.trim().length() >= 1) {
								// version is provided

								Version importVersion = new Version(importVersionStr);

								// the candidate import to be added to feature
								// model if its version matches
								// as per the match type to the import specified
								// by the feature.
								if (0 == "perfect".compareToIgnoreCase(importMatch)) {
									if (0 == candidateImportVersion.compareTo(importVersion))
										flag_candidateImportMatches = true;
								}

								else if (0 == "equivalent".compareToIgnoreCase(importMatch)) {
									// the candidate version must be at
									// least the version specified or at
									// a higher service level, i.e.
									// major and minor version levels
									// must be equal and the third param
									// must be equal or higher for the
									// candidate.
									if (candidateImportVersion.equivalentTo(importVersion))
										flag_candidateImportMatches = true;
								}

								else if (0 == "compatible".compareToIgnoreCase(importMatch)) {
									// the candidate version must be at
									// a higher or equal service or
									// minor level, the major level must
									// be the same.
									if (candidateImportVersion.compatibleTo(importVersion))
										flag_candidateImportMatches = true;
								}

								else if (0 == "greaterOrEqual".compareToIgnoreCase(importMatch)) {
									// the candidate version must be
									// either equal or greater then the
									// specified version, as specified
									// through the major, minor and
									// service levels.
									if (0 <= candidateImportVersion.compareUnqualified(importVersion))
										flag_candidateImportMatches = true;

								} else {

									// no match is present. so just do
									// the unqualified comparison.
									if (0 == candidateImportVersion.compareUnqualified(importVersion))
										flag_candidateImportMatches = true;
								}
							}

							else {
								// version is not present. go for all
								// possible
								// values.
								flag_candidateImportMatches = true;
							}
							if (flag_candidateImportMatches) {
								String LHS = Constants._FE_ + thisFeatureId;
								if (null != featureConditionalElem && featureConditionalElem.length() > 0)
									LHS = "(" + featureConditionalElem + Constants.IMPLIES_RIGHT + thisFeatureId + ")";

								dependenciesFM.add(LHS + Constants.IMPLIES_RIGHT + importNamePrefix + candidateImportId);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

					}

				} else {

					// the versions were to be ignored completely.
					String LHS = Constants._FE_ + thisFeatureId.trim();
					if (null != featureConditionalElem && featureConditionalElem.length() > 0)
						LHS = "(" + featureConditionalElem + Constants.IMPLIES_RIGHT + LHS + ")";

					dependenciesFM.add((LHS + Constants.IMPLIES_RIGHT + importNamePrefix + importName).replaceAll(
							"<(.*?)>", ""));
				}
			}
		}
	}

	/**
	 * Adds the plugins provided by the feature to the Dependencies Feature
	 * Model. e.g. __feature => (plugina && pluginb)
	 * 
	 * @param ignoreVersionsInFeatureModelGeneration
	 * @param thisFeatureId
	 * @param featureConditionalElem
	 *            the conditional element (Plarform parameters) on the feature
	 *            itself .
	 * @param featureExtract
	 * @throws IOException
	 */
	private static void addFeatureIncludedPluginsToFeatureModel(boolean ignoreVersionsInFeatureModelGeneration,
			String thisFeatureId, String featureConditionalElem, File featureExtract) throws IOException {
		Set<String> myPlugins = ParsingUtil.restorePropertyFromExtract(featureExtract, Constants.FEATURE_PROVIDED_PLUGINS);

		Set<String> includedPluginsSet = new LinkedHashSet<String>();
		for (String myPlugin : myPlugins) {
			String[] pluginProps;
			String pluginName = "", pluginVersionStr = "", pluginFragment = "", pluginOS = "", pluginARCH = "", pluginWS = "";
			pluginProps = myPlugin.split(";");
			for (int x = 0; x < pluginProps.length; x++) {
				switch (x) {
				case 0:
					pluginName = pluginProps[x].trim();
					break;
				case 1:
					pluginVersionStr = pluginProps[x].trim();
					break;
				case 2:
					pluginFragment = pluginProps[x].trim();
					break;
				case 3:
					pluginOS = pluginProps[x].trim();
					break;
				case 4:
					pluginARCH = pluginProps[x].trim();
					break;
				case 5:
					pluginWS = pluginProps[x].trim();
					break;
				default:
					break;
				}

				String conditionalElement = buildPlatformConditionalElement(pluginOS, pluginARCH, pluginWS);

				try {
					VersionRange versionRange = new VersionRange(pluginVersionStr);

					// now getting all Plugin IDs for the plugin name of
					// the dependency entry from the pluginmap.
					if (pluginMap.containsKey(pluginName)) {
						if (!ignoreVersionsInFeatureModelGeneration) {
							Set<String> candidatePluginIds = pluginMap.get(pluginName).keySet();
							for (String candidatePluginId : candidatePluginIds) {
								String candidatePluginVersionStr = parseDepIdForVersion(candidatePluginId);
								Version candidateVersion = new Version(candidatePluginVersionStr);
								if (versionRange.containsQualified(candidateVersion)) // versionRange.includes(candidateVersion))
								{
									if (conditionalElement.length() > 0)
										candidatePluginId = "(" + conditionalElement + Constants.IMPLIES_RIGHT
												+ candidatePluginId + ")";
									includedPluginsSet.add(candidatePluginId.trim());
								}
							}
						} else {
							if (conditionalElement.length() > 0)
								pluginName = "(" + conditionalElement + Constants.IMPLIES_RIGHT + pluginName + ")";
							includedPluginsSet.add(pluginName.trim());
						}
					}

				} catch (ParseException e) {
					e.printStackTrace();
				}

			}
		}

		String includedPluginsRHS = "";
		for (String s : includedPluginsSet) {
			if (!"".equalsIgnoreCase(s.trim()))
				includedPluginsRHS += s + Constants._AND_;
		}
		includedPluginsSet.clear();

		// removing the trailing &&
		if (Constants._AND_.length() <= includedPluginsRHS.length())
			includedPluginsRHS = includedPluginsRHS.substring(0, includedPluginsRHS.length() - Constants._AND_.length());

		String LHS = Constants._FE_ + thisFeatureId.trim();
		if (null != featureConditionalElem && featureConditionalElem.length() > 0)
			LHS = "(" + featureConditionalElem + Constants.IMPLIES_RIGHT + LHS + ")";

		if (!"".equalsIgnoreCase(includedPluginsRHS.trim())) {
			String pluginDepFMEntry = LHS + Constants.IMPLIES_RIGHT + "(" + includedPluginsRHS.trim() + ")";

			if (ignoreVersionsInFeatureModelGeneration) {
				pluginDepFMEntry = pluginDepFMEntry.replaceAll("<(.*?)>", "");
			}
			dependenciesFM.add(pluginDepFMEntry.trim());
		}
	}

	/**
	 * 
	 * builds the platform parameter string from the provided parameter values.
	 * The parameter values that are blank or null are ignored.
	 * 
	 * @param pluginOS
	 * @param pluginARCH
	 * @param pluginWS
	 * @return
	 */
	private static String buildPlatformConditionalElement(String pluginOS, String pluginARCH, String pluginWS) {
		String conditionalElement = "";

		conditionalElement += null != pluginOS && pluginOS.trim().length() > 0 ? Constants.PLATFORM_PARAM_OS + pluginOS
				+ Constants._AND_ : "";
		conditionalElement += null != pluginARCH && pluginARCH.trim().length() > 0 ? Constants.PLATFORM_PARAM_ARCH
				+ pluginARCH + Constants._AND_ : "";
		conditionalElement += null != pluginWS && pluginWS.trim().length() > 0 ? Constants.PLATFORM_PARAM_WS + pluginWS
				+ Constants._AND_ : "";

		if (conditionalElement.endsWith(Constants._AND_))
			conditionalElement = conditionalElement.substring(0, conditionalElement.length() - Constants._AND_.length());
		if (conditionalElement.length() > 0)
			conditionalElement = "(" + conditionalElement + ")";
		return conditionalElement;
	}

	
	/**
	 *     maintains the current depth of calls in the findExporters (circular dependency analysis )  function.
	 */
	private static long findExportersRecursiveCallDepth=0;
		
	/**
	 * @param considerBundleExportersOnly
	 * @param ignoreBundlesMarkedToBeIgnored
	 * @param alsoConsiderInvokationSatisfactionProxies
	 * @param ignoreVersionsInFeatureModelGeneration
	 * @param pluginExtractDirectory
	 * @throws IOException
	 */
	private static void processPluginExtracts(boolean considerBundleExportersOnly, boolean ignoreBundlesMarkedToBeIgnored,
			boolean alsoConsiderInvokationSatisfactionProxies, boolean ignoreVersionsInFeatureModelGeneration,
			File pluginExtractDirectory) throws IOException {
		long time1 = System.currentTimeMillis();
		File[] pluginExtractEntries = pluginExtractDirectory.listFiles();

		int entriesLength = pluginExtractEntries.length;

		long pluginExtractsDone = 0;

		for (File pluginExtract : pluginExtractEntries) {
			pluginExtractsDone++;
			if (pluginExtractsDone % 250 == 0 || pluginExtractsDone >= entriesLength) {
				Log.outln("#### PluginExtractsMerged = " + pluginExtractsDone + " of " + entriesLength + " ("
						+ (pluginExtractsDone * 100 / entriesLength) + "%)");

				Log.outln("## time (merging) so far \t= " + Util.getFormattedTime(System.currentTimeMillis() - time1));
			}
			if (Util.checkFile(pluginExtract, true, true, true, false)) {
				/**
				 * this is the name of the PluginExtract file, without the
				 * extension. this is for obtaining the fully qualified plugin
				 * name with version and qualifier, even if that information is
				 * not in the pluginExtract file, cause because of an originally
				 * missing manifest.mf file for thus plugin.
				 */
				String thisPluginExtractName = pluginExtract.getName().trim();

				// ignoring the ones that are not plugin extracts
				if (!(thisPluginExtractName.endsWith(Constants.EXTRACT_FILE_EXTENSION_PLUGIN) && thisPluginExtractName
						.startsWith(Constants.EXTRACT_FILE_PREFIX_PLUGIN)))
					continue;

				// constructing this plugin extract plugin id.
				String thisPluginId = ParsingUtil.getPluginIdFromExtract(pluginExtract);

				boolean ignorePluginExtract = false;
				Set<String> ignoreBundleProperty = ParsingUtil.restorePropertyFromExtract(pluginExtract,
						Constants.BUNDLE_IGNORE);

				if (ignoreBundlesMarkedToBeIgnored) {
					if (null != ignoreBundleProperty && 1 == ignoreBundleProperty.size())
						ignorePluginExtract = ignoreBundleProperty.toString().toLowerCase().trim().contains("true");
				}

				if (ignorePluginExtract) {
					Log.outln("==== " + thisPluginId + " must be IGNORED");
					Log.errln("==== " + thisPluginId + " must be IGNORED");
					pluginExtractsIgnored.add(thisPluginId);
					continue;
				}

				Set<String> bundleExports = new HashSet<String>();
				if (considerBundleExportersOnly) {
					bundleExports = ParsingUtil.restorePropertyFromExtract(pluginExtract, Constants.BUNDLE_EXPORTS);
				}

				// restoring the plugin information from the plugin extract
				// file.
				Set<String> myHosts = ParsingUtil.restorePropertyFromExtract(pluginExtract, Constants.BUNDLE_FRAGMENT_HOST);
				Set<String> myPackageImports = ParsingUtil.restorePropertyFromExtract(pluginExtract,
						Constants.BUNDLE_PACKAGE_IMPORTS);
				Set<String> myOtherBundleDependencies = ParsingUtil.restorePropertyFromExtract(pluginExtract,
						Constants.BUNDLE_OTHER_BUNDLE_IMPORTS);
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
				Set<String> myInvokationProxyPairs = ParsingUtil.restorePropertyFromExtract(pluginExtract,
						Constants.PLUGIN_ALL_INVOKATION_PROXY_PAIRS);

				// building the dependenciesFM Set

				// adding the host plugin bundle dependency to the feature model
				addPluginDependenciesToFeatureModel(thisPluginId, myHosts, pluginMap, Constants.IMPLIES_BIDIRECTIONAL,
						"fragmentHost", ignoreVersionsInFeatureModelGeneration);

				// adding the bundle dependencies (other bundles) to the feature
				// model.
				addPluginDependenciesToFeatureModel(thisPluginId, myOtherBundleDependencies, pluginMap,
						Constants.IMPLIES_RIGHT, "pluginImp", ignoreVersionsInFeatureModelGeneration);

				// adding package based bundle dependencies
				addPackageDependenciesToFeatureModel(thisPluginId, myPackageImports, " pkgdep",
						ignoreVersionsInFeatureModelGeneration);

				// //////////////////////////////////////////
				// building DependencyFinder.plugins object
				// /////////////////////////////////////////

				PluginObject po = new PluginObject();
				if (plugins.containsKey(thisPluginId))
					po = plugins.get(thisPluginId);

				po.name = thisPluginId;

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

				// building the interfaceProxies Set
				for (String pair : myInvokationProxyPairs) {
					String[] pairElements = pair.split(Constants.DELIM_PLUGIN_ELEMENT_SUPERCLASS_INTERFACE);
					String invokationBase = pairElements[0].trim();
					String invokationProxy = pairElements[1].trim();

					Set<String> invokationProxies = new HashSet<String>();
					if (po.invokationProxies.containsKey(invokationBase))
						invokationProxies = po.invokationProxies.get(invokationBase);
					invokationProxies.add(invokationProxy);
					po.invokationProxies.put(invokationBase, invokationProxies);

				}

				// merging functions

				boolean flag_hasOptionalPluginImport = hasOptional(myOtherBundleDependencies);

				for (String myMethodExport : myMethodExports) {
					myMethodExport = myMethodExport.trim();
					if (1 <= myMethodExport.length()) {

						boolean flag_isExported = true;

						if (considerBundleExportersOnly) {
							flag_isExported = false;

							// String funcClassName = s.split(" ")[1].trim();
							FuncSig funcNameElements= new FuncSig(myMethodExport);
							String funcClassName = funcNameElements.getClassName();

							for (String bundleExport : bundleExports) {
								bundleExport = bundleExport.trim();
								if (1 <= bundleExport.length()) {

									bundleExport = ParsingUtil.getNameFromBundleDependencyEntry(bundleExport, true, true);
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
							// building the plugins object for exports.
							po.exports.add(myMethodExport.trim());
						}
					}
				}

				if (!(flag_hasOptionalPluginImport || Constants.BLACKLISTED_PLUGINS.contains(thisPluginId))) {
					for (String s : myMethodImports) {
						s = s.trim();
						if (1 <= s.length()) {
							// check if this import comes from an optional
							// package import of this bundle for which no
							// exporter is available. If so, then this import
							// must be ignored.
							boolean doNotCare = false;
							doNotCare = checkIfImportFromOptionalOrphanPkgImport(s, myPackageImports, exportedPackagesMap);

							if (!doNotCare) {
								ImpExp impexp = new ImpExp();
								if (functions.containsKey(s))
									impexp = functions.get(s);

								impexp.addToImp(thisPluginId);
								functions.put(s, impexp);

								// building the plugins object for imports.
								// (combining type and
								// function information together) .
								// This is the functions part.
								po.imports.add(s);

							} else {
								// System.out.println(s +
								// " : do not care because of  ioptional package import in plugin: "
								// + thisPluginId);
							}
						}
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

									bundleExport = ParsingUtil.getNameFromBundleDependencyEntry(bundleExport, true, true);
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
							impexp.addToExp(thisPluginId);
							types.put(myTypeExport, impexp);
							// building the plugins object for exports.
							po.exports.add(myTypeExport.trim());
						}
					}
				}

				if (!(flag_hasOptionalPluginImport || Constants.BLACKLISTED_PLUGINS.contains(thisPluginId))) {
					// ignore all imports from plugins that either have optional
					// plugin imports are are blacklisted.
					for (String s : myTypeImports) {
						s = s.trim();
						if (1 <= s.length()) {
							// check if this import comes from an optional
							// package import of this bundle for which no
							// exporter is
							// available. If so, then this import must be
							// ignored.
							boolean doNotCare = false;
							doNotCare = checkIfImportFromOptionalOrphanPkgImport(s, myPackageImports, exportedPackagesMap);

							if (!doNotCare) {
								ImpExp impexp = new ImpExp();
								if (types.containsKey(s))
									impexp = types.get(s);
								impexp.addToImp(thisPluginId);
								types.put(s, impexp);

								// building the plugins object for imports.
								// (combining type and
								// function information together).
								// This is the types part.
								po.imports.add(s);

							} else {
//								System.out.println(s + " : do not care because of  ioptional package import in plugin: "
//										+ thisPluginId);
							}
						}
					}
				}
				// adding the constructed object to the DependencyFinder.plugins
				// object.
				plugins.put(thisPluginId, po);

				// ///////////////// done with adding the current plugin
				// information to the DependencyFinder.plugins object /////////
			}
		}
				
		Log.outln(" ///////// now doing the circular inter plugin dependency analysis    /////////    ");

		Set<Entry<String, PluginObject>> pluginEntrySet = plugins.entrySet();

		long totalnumberofplugins = pluginEntrySet.size(), counter = 0, indirectDepAnalysisTime1 = System
				.currentTimeMillis();

		for (Entry<String, PluginObject> entry : pluginEntrySet) {
			counter++;
			PluginObject pluginObj = entry.getValue();
			Log.outln("== plugin  " + counter + " : " + pluginObj.name);
			Log.errln("== plugin  " + counter + " : " + pluginObj.name);
			
			for (String imp : pluginObj.imports) {
				
				findExportersRecursiveCallDepth=0;
				Set<Set<String>> exporterPluginSets = new LinkedHashSet<Set<String>>();
				// if the alsoConsiderInvokationSatisfactionProxies flag
				// parameter is true, then build a set of all invokations to be
				// considered: all its proxy invokations if they exists else
				// check the original invokation.
				if (alsoConsiderInvokationSatisfactionProxies) {
					Set<String> invokationProxies = pluginObj.invokationProxies.get(imp);
					if (null != invokationProxies && 1 <= invokationProxies.size()) {
						// check only the proxies, as we know that if it has not
						// been satisfied till now at the plugin level ( i.e. a
						// proxy exists) then if it has to be satisfied , it
						// will be satisfied through the proxy only.
						exporterPluginSets = fetchExporters(imp, invokationProxies, pluginObj.name);
					} else {
						exporterPluginSets = fetchExporters(imp);
					}
				} else {

					exporterPluginSets = fetchExporters(imp);
				}

				// now if there were any indirect / transitive exporters, check
				// for that and mark the import as satisfied.

				// recording the data in the functions or types objects.
				if (functions.containsKey(imp) && 1 <= exporterPluginSets.size()) {
					ImpExp impexp = functions.get(imp);
					impexp.addAllToExp(exporterPluginSets);
					functions.put(imp, impexp);
				} else if (types.containsKey(imp) && 1 <= exporterPluginSets.size()) {
					ImpExp impexp = types.get(imp);
					impexp.addAllToExp(exporterPluginSets);
					types.put(imp, impexp);
				}
			}
			if (counter % 250 == 0 || counter >= totalnumberofplugins) {

				Log.outln("#### indirect dep analysis done for " + (counter) + " of " + totalnumberofplugins + " ("
						+ (counter * 100 / totalnumberofplugins) + "%)");
				Log.outln("## time (indirect dep analysis) so far \t= "
						+ Util.getFormattedTime(System.currentTimeMillis() - indirectDepAnalysisTime1));
			}
		}
	}

	

	private static Set<Set<String>> fetchExporters(String imp, Set<String> importEntryProxies, String ownerPluginName) {

		Set<Set<String>> result = new LinkedHashSet<Set<String>>();

		// first see if the import can be satisfied on its own
		// this is required as there might be some interface implementations
		// that may satisfy the invokation indirectly.
		Set<Set<String>> interimResult = new LinkedHashSet<Set<String>>();
		interimResult = fetchExporters(imp);
		if (null != interimResult && interimResult.size() > 0)
			result.addAll(interimResult);
		else {

			// otherwise, try the proxies but then this means that we would need
			// to
			// include the plugin that owns the import entry in the result as
			// the
			// proxy is generated to go outside of the owner plugin from inside
			// of
			// the owner plugin and the dependency cannot be satisfied without
			// the
			// owner plugin in the mix.

			for (String importEntryProxy : importEntryProxies) {
				interimResult = new LinkedHashSet<Set<String>>();

				interimResult = fetchExporters(importEntryProxy);
				if (interimResult.size() > 0)// null!=interimResult &&
					for (Set<String> set : interimResult) {
						if (set.size() > 0)
							set.add(ownerPluginName);
					}
				result.addAll(interimResult);
			}
		}
		return result;
	}

	/**
	 * get the set of exporting plugin combinations either from the cache, if
	 * available or else do a fresh search.
	 * 
	 * @param imp
	 * @return
	 */
	private static Set<Set<String>> fetchExporters(String imp) {
		Set<Set<String>> result = new LinkedHashSet<Set<String>>();

		if (exporterSetsCache.containsKey(imp)) {
			result = exporterSetsCache.get(imp);
		} else {
			Set<Set<String>> interimresult = findExporters(imp, new HashSet<String>());
			if (null != interimresult && 1 <= interimresult.size()) {
				exporterSetsCache.put(imp, interimresult);
				result.addAll(interimresult);
			}
		}
		return result;
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
	 *            the import signature (function or type)
	 * @return Set<Set<String>> a set of sets of plugin combinations that
	 *         satisfy the import
	 */
	private static Set<Set<String>> findExporters(String imp, Set<String> seenClasses) {

		findExportersRecursiveCallDepth++;
		if (findExportersRecursiveCallDepth % 500000 == 0)
			System.err.println(findExportersRecursiveCallDepth + ":" + imp);

		Set<Set<String>> result = new LinkedHashSet<Set<String>>();
		imp = imp.trim();
		int impType = ParsingUtil.getEntryType(imp);
		if (0 != impType) {
			String classname = imp;
			if (1 == impType) {
				/* it is a function, so get the class name. */
				FuncSig funcElements = new FuncSig(imp);
				classname = funcElements.getClassName();// class name.
			}
			Set<PluginObject> targetPlugins = new HashSet<PluginObject>();
			targetPlugins = getTargetPlugins(classname);

			for (PluginObject targetPlugin : targetPlugins) {

				if (null != targetPlugin) {
					if (null != targetPlugin.exports && targetPlugin.exports.contains(imp)) {
						Set<String> newSet = new LinkedHashSet<String>();
						newSet.add(targetPlugin.name);
						result.add(newSet);
						// System.out.println("result: "+result);
					} else {
						if (null != targetPlugin.superClassesAndInterfaces) {
							Set<String> superclasses = targetPlugin.superClassesAndInterfaces.get(classname);
							if (null != superclasses) {
								/*
								 * iteration level cache of classes visited in
								 * the current tree of inheritance hierarchy, so
								 * that a loop can be avoided.
								 */
								Set<String> newSeenClasses = new HashSet<String>(seenClasses);
								newSeenClasses.add(classname);
								for (String superclass : superclasses) {
									if (!seenClasses.contains(superclass)
											&& 0 != Constants.JAVA_LANG_OBJECT.compareToIgnoreCase(superclass))
									/*
									 * if this class has been visited already in
									 * the current iteration and the current
									 * hierarchy i.e., stop as this is a loop.
									 * __&&__  
									 *  ignoring cases where the
									 * superclass is java.lang.Object as pruning
									 * for this was done earlier already.
									 */
									{
										String newImp = imp.replace(classname, superclass);
										/*
										 * if imp is a functioninvokation, then  replace just the class name with superclass name
										 */
										boolean impTypeIsFunction = (1 == ParsingUtil.getEntryType(imp)) ? true : false;

										if (impTypeIsFunction) {
											// it is a function, so get the
											// class
											// name.

											FuncSig funcElements = new FuncSig(imp);

											// replace the class name.
											funcElements.setClassName(superclass);
											// recnstruct
											newImp = funcElements.getSignature();
										}

										Set<Set<String>> setOfSets = findExporters(newImp, newSeenClasses);
										if (setOfSets.size() > 0)
											for (Set<String> set : setOfSets) {
												if (set.size() > 0)
													set.add(targetPlugin.name);
											}
										result.addAll(setOfSets);
										// System.out.println("result_branch: "+result);
									}
								}
							}
						}
					}
				}
			}
		}
		return result;
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
			Set<String> allPluginExporters = types.get(classname).getExp();
			for (Object pluginName : allPluginExporters) {
				// getting the PluginObjects for all the plugin names obtained.
				PluginObject pobj = plugins.get((String) pluginName);
				result.add(pobj);
			}

		}
		return result;
	}
	/**
	 * is the importEntry coming from a packageImport that was optional and for
	 * which no exporter is present?
	 * 
	 * @param importEntry
	 * @param myPackageImports
	 * @param exportedPkgsMap
	 * @return
	 */
	private static boolean checkIfImportFromOptionalOrphanPkgImport(String importEntry, Set<String> myPackageImports,
			Map<String, Map<String, Set<String>>> exportedPkgsMap) {
		boolean result = false;
		String methodImportClassName = importEntry;

		if (1 == ParsingUtil.getEntryType(importEntry)) {
		FuncSig	myMethodImportElems=    new FuncSig(importEntry);
			methodImportClassName = myMethodImportElems.getClassName();
		}
		for (String pkgImpEntry : myPackageImports) {
			String pkgImpName = ParsingUtil.getNameFromBundleDependencyEntry(pkgImpEntry, true, false);
			boolean flag_pkgImpEntry_isOptional = pkgImpName.contains(Constants.BUNDLE_DEPDENDENCY_KEYWORD_OPTIONAL);

			if (flag_pkgImpEntry_isOptional) {
				pkgImpName = pkgImpName.replace(Constants.BUNDLE_DEPDENDENCY_KEYWORD_OPTIONAL, "").trim();
				if (!exportedPkgsMap.containsKey(pkgImpName) && methodImportClassName.startsWith(pkgImpName + ".")) {
					result = true;
					break;
				}
			}
		}
		return result;
	}

	private static boolean hasOptional(Set<String> s) {
		for (String a : s)
			if (a.toLowerCase().contains(Constants.BUNDLE_DEPDENDENCY_KEYWORD_OPTIONAL))
				return true;
		return false;
	}

	/**
	 * 
	 * adds package dependencies to the feature model.
	 * 
	 * @param thisPluginId
	 * @param pkgImports
	 * @param comment
	 * @param ignoreVersionsInFeatureModelGeneration
	 */
	private static void addPackageDependenciesToFeatureModel(String thisPluginId, Set<String> pkgImports, String comment,
			boolean ignoreVersionsInFeatureModelGeneration) {
		// adding package based bundle dependencies
		for (String packageImport : pkgImports) {
			String packageImportName = ParsingUtil.getNameFromBundleDependencyEntry(packageImport, true, false);
			if (null != packageImportName && !"".equalsIgnoreCase(packageImportName.trim())) 
			// this means that there is some name to it.
			{
				packageImportName = packageImportName.trim();
				/*
				 * check if this is optional. If yes this will not be included.
				 */
				if (!packageImportName.contains(Constants.BUNDLE_DEPDENDENCY_KEYWORD_OPTIONAL))
				/*
				 * now getting all Plugin IDs for the plugin name of the
				 * dependency entry from the pluginmap.
				 */
				{
					if (exportedPackagesMap.containsKey(packageImportName)) {
						Map<String, Set<String>> candidatePkgMap = exportedPackagesMap.get(packageImportName);
						Set<String> candidateExpotedPackageIds = candidatePkgMap.keySet();

						if (!ignoreVersionsInFeatureModelGeneration) {

							String versionRangeStr = ParsingUtil.getVersionStrFromBundleDependencyEntry(packageImport);

							try {
								VersionRange versionRange = new VersionRange(versionRangeStr);

								for (String candidateExpPkgId : candidateExpotedPackageIds) {
									String candidateExpPkgVersionStr = parseDepIdForVersion(candidateExpPkgId);
									Version candidateVersion = new Version(candidateExpPkgVersionStr);
									if (versionRange.containsQualified(candidateVersion)) // versionRange.includes(candidateVersion))
									{
										for (String candidatePluginId : candidatePkgMap.get(candidateExpPkgId)) {
											String pluginDepFMEntry = thisPluginId.trim() + Constants.IMPLIES_RIGHT
													+ candidatePluginId.trim() + Constants.DELIM_COMMENT + comment.trim();

											dependenciesFM.add(pluginDepFMEntry);
										}
									}
								}
							} catch (ParseException e) {
								e.printStackTrace();
							}
						} else {
							// not considering the version ranges at
							// all, so add all plugin ids that export any
							// versions of the package.

							for (String candidateExpPkgId : candidateExpotedPackageIds)
								for (String candidatePluginId : candidatePkgMap.get(candidateExpPkgId)) {
									String pluginDepFMEntry = thisPluginId.trim() + Constants.IMPLIES_RIGHT
											+ candidatePluginId.trim() + Constants.DELIM_COMMENT + "pkgdep";
									pluginDepFMEntry = pluginDepFMEntry.replaceAll("<(.*?)>", "");
									dependenciesFM.add(pluginDepFMEntry);
								}
						}
					}

				}
			}
		}
	}

	/**
	 * 
	 * adds the provided dependencies as implications from the specified plugin
	 * in the Dependencies Feature Model.
	 * 
	 * @param thisPluginId
	 *            this plugin's id.
	 * @param dependencies
	 *            the {@link Set} of dependencies on other plugins
	 * @param map
	 *            the {@link Map<k, Map<k, Set>>} of the metadata for the
	 *            dependencies (dependency name to ID Set and so on ....) e.g.
	 *            pluginMap.
	 * @param implies
	 *            the implication indicator e.g. => or <=> or <=
	 * @param comment
	 *            the comment to be added at the end of each dependency entry
	 * @param ignoreVersionsInFeatureModelGeneration
	 */
	private static void addPluginDependenciesToFeatureModel(String thisPluginId, Set<String> dependencies,
			Map<String, Map<String, Set<String>>> map, String implies, String comment,
			boolean ignoreVersionsInFeatureModelGeneration) {

		for (String otherPluginDepEntry : dependencies) {

			String otherPluginName = ParsingUtil.getNameFromBundleDependencyEntry(otherPluginDepEntry, true, false);
			if (null != otherPluginName && !"".equalsIgnoreCase(otherPluginName.trim())) {
				// this means that there is some name to it.
				otherPluginName = otherPluginName.trim();

				// check if this is optional. If yes this will not be
				// included.
				if (!otherPluginName.contains(Constants.BUNDLE_DEPDENDENCY_KEYWORD_OPTIONAL)) {
					if (!ignoreVersionsInFeatureModelGeneration) {
						// now doing the check for whether there exists
						// some bundle that falls in the version range
						// specified by the otherPluginDepEntry.
						String versionRangeStr = ParsingUtil.getVersionStrFromBundleDependencyEntry(otherPluginDepEntry);

						try {
							VersionRange versionRange = new VersionRange(versionRangeStr);

							// now getting all Plugin IDs for the plugin name of
							// the dependency entry from the pluginmap.
							if (map.containsKey(otherPluginName)) {
								Set<String> candidatePluginIds = map.get(otherPluginName).keySet();
								for (String candidatePluginId : candidatePluginIds) {
									String candidatePluginVersionStr = parseDepIdForVersion(candidatePluginId);
									Version candidateVersion = new Version(candidatePluginVersionStr);
									if (versionRange.containsQualified(candidateVersion)) // versionRange.includes(candidateVersion))
									{
										String pluginDepFMEntry = thisPluginId.trim() + implies + candidatePluginId.trim()
												+ Constants.DELIM_COMMENT + comment.trim();
										dependenciesFM.add(pluginDepFMEntry);
									}
								}
							}
						} catch (ParseException e) {
							e.printStackTrace();
						}
					} else {
						// not considering the version ranges at all, so
						// skipping the check of whether a plugin exists in the
						// sepcified range.
						String pluginDepFMEntry = thisPluginId.trim() + implies + otherPluginName.trim()
								+ Constants.DELIM_COMMENT + comment.trim();
						pluginDepFMEntry = pluginDepFMEntry.replaceAll("<(.*?)>", "");
						dependenciesFM.add(pluginDepFMEntry);
					}
				}
			}
		}
	}

	/**
	 * parses the pluginId or FeatureId got from the plugin / feature Map to
	 * return the version information. e.g. pluginA<version.a.b.qual> will
	 * return version.a.b.qual an empty string is returned if there is no
	 * version information available.
	 * 
	 * @param PluginOrFeatureId
	 */
	private static String parseDepIdForVersion(String PluginOrFeatureId) {
		String version = "";
		String[] splits = PluginOrFeatureId.split(Constants.DELIM_VERSION_STRING_OPEN);
		if (null != splits && 2 <= splits.length) {// this means that there was
													// some version information,
													// else there is no version
													// information.
			version = splits[1];
			version = version.replace(Constants.DELIM_VERSION_STRING_OPEN, "")
					.replace(Constants.DELIM_VERSION_STRING_CLOSE, "").trim();
		}
		return version;
	}

	/**
	 * @param pathToPluginMapFile
	 * @param pathToFeatureMapFile
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static boolean populateAllMaps(String pathToPluginMapFile, String pathToFeatureMapFile,
			String pathToExportedPackagesMapFile) {
		try {
			ObjectInputStream ois;

			ois = new ObjectInputStream(new FileInputStream(pathToPluginMapFile));
			pluginMap = (Map<String, Map<String, Set<String>>>) ois.readObject();
			ois.close();

			ois = new ObjectInputStream(new FileInputStream(pathToFeatureMapFile));
			featureMap = (Map<String, Map<String, Set<String>>>) ois.readObject();
			ois.close();

			ois = new ObjectInputStream(new FileInputStream(pathToExportedPackagesMapFile));
			exportedPackagesMap = (Map<String, Map<String, Set<String>>>) ois.readObject();
			ois.close();

		} catch (Exception e) {
			Log.errln(Util.getStackTrace(e));
			return false;
		}
		return true;
	}
	private static void writeData(String pluginDependencySetOutputLocationPath,
			boolean ignoreVersionsInFeatureModelGeneration) throws IOException {

		Set<String> unmatchedFunctionImports = new LinkedHashSet<String>();
		Set<String> unmatchedTypeImports = new LinkedHashSet<String>();

		if (!Util.checkAndCreateDirectory(pluginDependencySetOutputLocationPath)) {
			Log.errln("xxxx\n in writeData() in   DependencyFinder, the output location: "
					+ pluginDependencySetOutputLocationPath + "  \n is not accessible, cannot record data.  \nxxxx");
			return;
		}

		File functionFile = new File(pluginDependencySetOutputLocationPath + "/" + Constants.DEPENDENCY_SET_FILE_PREFIX
				+ "functions" + Constants.DEPENDENCY_SET_FILE_EXTENSION);
		File typeFile = new File(pluginDependencySetOutputLocationPath + "/" + Constants.DEPENDENCY_SET_FILE_PREFIX
				+ "types" + Constants.DEPENDENCY_SET_FILE_EXTENSION);
		File constraintsFMFile = new File(pluginDependencySetOutputLocationPath + "/"
				+ Constants.FM_CONSTRAINTS_FILE_PREFIX + Constants.FM_CONSTRAINTS_FILE_NAME
				+ Constants.FM_CONSTRAINTS_FILE_EXTENSION);
		File pluginDependenciesFMFile = new File(pluginDependencySetOutputLocationPath + "/"
				+ Constants.FM_PLUGIN_DEPENDENCIES_FILE_PREFIX + Constants.FM_PLUGIN_DEPENDENCIES_FILE_NAME
				+ Constants.FM_PLUGIN_DEPENDENCIES_FILE_EXTENSION);

		// writing out the dependenciesFM

		FileWriter pluginDepFMfilewriter = new FileWriter(pluginDependenciesFMFile);
		BufferedWriter pluginDepFMWriter = new BufferedWriter(pluginDepFMfilewriter);

		List<String> pluginDepFM_List = new ArrayList<String>(dependenciesFM);
		Collections.sort(pluginDepFM_List);
		for (String s : pluginDepFM_List) {
			pluginDepFMWriter.write(s.replace(".", "_").replace("-", "_")
					.replace(Constants.DELIM_VERSION_STRING_OPEN, "__").replace(Constants.DELIM_VERSION_STRING_CLOSE, "__")
					+ "\n");
		}
		pluginDepFMWriter.close();
		pluginDepFMfilewriter.close();

		// writing functions set.

		FileWriter filewriter = new FileWriter(functionFile);
		BufferedWriter writer = new BufferedWriter(filewriter);

		// the writers for the constraints file.
		FileWriter constraintsFMfilewriter = new FileWriter(constraintsFMFile);
		BufferedWriter constraintsFMWriter = new BufferedWriter(constraintsFMfilewriter);
		Map<String, Set<String>> constraintsFM = new HashMap<String, Set<String>>();

		writer.write(Constants.PLUGIN_DEPENDENCY_ALL_FUNCTIONS + "\n");
		Log.outln(Constants.PLUGIN_DEPENDENCY_ALL_FUNCTIONS + "\n");

		long counter = 0;
		long functionsLength = functions.keySet().size();
		for (String funcSign : functions.keySet()) {

			writer.write(Constants.DELIM_PLUGIN_DEPENDENCY_ELEMENT_SET + "\n");

			// writing the function signature.
			writer.write(funcSign + "\n");

			ImpExp impexp = functions.get(funcSign);

			Set<String> imp = impexp.getImp();
			Set<String> exp = impexp.getExp();

			// all importers
			writer.write(Constants.PLUGIN_DEPENDENCY_IMPORTERS + "\n");
			for (String s : imp) {
				writer.write(s + "\n");
			}
			writer.write(Constants.MARKER_TERMINATOR + "\n");

			// building the exporters part of the constraint as well.
			String constraintsExporters = "";

			// all exporters
			writer.write(Constants.PLUGIN_DEPENDENCY_EXPORTERS + "\n");
			for (String s : exp) {
				writer.write(s + "\n");

				// collecting the exporters set .. to add in the constraints
				// file.
				String ces = s.trim().replace("[", "(" + Constants.CONFIG_).replace("]", ")")
						.replace(", ", Constants._AND_ + Constants.CONFIG_);
				if (null != ces && 1 <= ces.trim().length())
					constraintsExporters += ces + Constants._OR_;

			}
			writer.write(Constants.MARKER_TERMINATOR + "\n");

			if (4 <= constraintsExporters.length())
				constraintsExporters = constraintsExporters.substring(0,
						constraintsExporters.length() - Constants._OR_.length());

			// collecting the constraint entry to the constraintsSet.
			if (1 <= constraintsExporters.length()) {
				for (String importer : imp) {
					importer = importer.trim();
					if ("".equalsIgnoreCase(importer))
						continue;
					String constraint = Constants.CONFIG_ + importer + Constants.IMPLIES_RIGHT + constraintsExporters;
					if (ignoreVersionsInFeatureModelGeneration)
						constraint = constraint.replaceAll("<(.*?)>", "");
					Set<String> funcs = new HashSet<String>();
					if (constraintsFM.containsKey(constraint))
						funcs = constraintsFM.get(constraint);
				//	funcs.add( funcSign);
					constraintsFM.put(constraint, funcs);
				}
			}

			// all importers not satisfied
			writer.write(Constants.PLUGIN_DEPENDENCY_IMPORTERS_UNSATISFIED + "\n");
			writer.write((0 == exp.size() ? true + ", " + imp.size() + " importer(s) hungry " : false + ", " + exp.size()
					+ (1 < exp.size() ? " exporters!! PLURAL??" : " exporter") + " available ")
					+ "\n");
			writer.write(Constants.MARKER_TERMINATOR + "\n");

			// add to the unmatchedFunctionImports Set.
			if (0 == exp.size())
				unmatchedFunctionImports.add(funcSign);

			// all exporters whose exports were not needed by anyone
			writer.write(Constants.PLUGIN_DEPENDENCY_EXPORTERS_UNSATISFIED + "\n");
			writer.write((0 == imp.size() ? true + ", " + exp.size()
					+ (1 < exp.size() ? " exporters!! PLURAL??" : " exporter") + " eagerly available " : false + ", "
					+ imp.size() + " importer(s) hungry ")
					+ "\n");
			writer.write(Constants.MARKER_TERMINATOR + "\n");

			// writer.write(Constants.PLUGIN_DEPENDENCY_SATISFYING_PLUGINS_SETS
			// + "\n");
			// for (Set<String> s : impexp.satisfyingPluginsSets) {
			// writer.write(s + "\n");
			// }
			// writer.write(Constants.MARKER_TERMINATOR + "\n");

			// terminating the key ( function name )
			writer.write(Constants.MARKER_TERMINATOR + "\n");

			counter++;
			if (counter % 1000 == 0 || counter == functionsLength)
				Log.outln("## functions written to disk: " + counter + " of " + functionsLength + " ("
						+ (counter * 100 / functionsLength) + "%)");
		}

		// writing the constraints file.
		List<String> constraintsSet_List = new ArrayList<String>(constraintsFM.keySet());
		Collections.sort(constraintsSet_List);
		for (String constraint : constraintsSet_List) {
			String comment="";
			for (String f : constraintsFM.get(constraint))
				comment += f.trim()+", ";
			
			constraintsFMWriter.write((constraint+(comment.length()>0?Constants.DELIM_COMMENT+comment:"")).replace(".", "_").replace("-", "_")
					.replace(Constants.DELIM_VERSION_STRING_OPEN, "__").replace(Constants.DELIM_VERSION_STRING_CLOSE, "__")
					+ "\n");
		}
		constraintsFMWriter.close();
		constraintsFMfilewriter.close();

		writer.write(Constants.PLUGIN_DEPENDENCY_ALL_UNMATCHED_FUNCTION_IMPORTS + "\n");
		Log.outln(Constants.PLUGIN_DEPENDENCY_ALL_UNMATCHED_FUNCTION_IMPORTS + "\n");
		List<String> unmatchedFunctionImports_List = new ArrayList<String>(unmatchedFunctionImports);
		Collections.sort(unmatchedFunctionImports_List);
		for (String s : unmatchedFunctionImports_List)
			writer.write(s + "\n");
		writer.write("COUNT=" + unmatchedFunctionImports.size() + "\n");
		Log.outln("COUNT=" + unmatchedFunctionImports.size() + "\n");
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

		counter = 0;
		long typesLength = types.keySet().size();

		for (String key : types.keySet()) {

			writer.write(Constants.DELIM_PLUGIN_DEPENDENCY_ELEMENT_SET + "\n");

			// writing the type signature.
			writer.write(key + "\n");

			ImpExp impexp = types.get(key);

			Set<String> imp = impexp.getImp();
			Set<String> exp = impexp.getExp();

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

			// writer.write(Constants.PLUGIN_DEPENDENCY_SATISFYING_PLUGINS_SETS
			// + "\n");
			// for (Set<String> s : impexp.satisfyingPluginsSets) {
			// writer.write(s + "\n");
			// }
			// writer.write(Constants.MARKER_TERMINATOR + "\n");

			// terminating the key ( type /class name )
			writer.write(Constants.MARKER_TERMINATOR + "\n");

			counter++;
			if (counter % 1000 == 0 || counter == typesLength)
				Log.outln("## types written: " + counter + " of " + typesLength + " (" + (counter * 100 / typesLength)
						+ "%)");

		}

		writer.write(Constants.PLUGIN_DEPENDENCY_ALL_UNMATCHED_TYPE_IMPORTS + "\n");
		Log.outln(Constants.PLUGIN_DEPENDENCY_ALL_UNMATCHED_TYPE_IMPORTS + "\n");
		ArrayList<String> unmatchedTypeImports_List = new ArrayList<String>(unmatchedTypeImports);
		Collections.sort(unmatchedTypeImports_List);
		for (String s : unmatchedTypeImports_List)
			writer.write(s + "\n");
		writer.write("COUNT=" + unmatchedTypeImports.size() + "\n");
		Log.outln("COUNT=" + unmatchedTypeImports.size() + "\n");

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
