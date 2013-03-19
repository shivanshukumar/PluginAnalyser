package com.shivanshusingh.pluginanalyser.analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.ivy.osgi.core.ManifestParser;
import org.objectweb.asm.ClassReader;

import com.shivanshusingh.pluginanalyser.utils.Util;
import com.shivanshusingh.pluginanalyser.utils.logging.Log;
import com.shivanshusingh.pluginanalyser.utils.parsing.Constants;

/**
 * Analyzes the plugin / bundles.
 * @author Shivanshu Singh
 * 
 */
public class BundleAnalyser extends ManifestParser {

	
	/**
	 * @param pluginFolderPath
	 * @param outputLocation
	 * @throws IOException
	 */
	public static void analyseAndRecordAllInformationFromBasePluginFolder(
			String pluginFolderPath, String outputLocation) throws IOException {
		if (!Util.checkAndCreateDirectory(outputLocation)) {
			  Log.errln("xxxx Error Accessing/Creating Output Directory for Plugin Analysis Output at: "
							+ outputLocation
							+ "\n Cannot continue with the analysis.");
			return;
		}
		// reading all the files (plugin jars) in the specified plugin folder
		long l1 = System.currentTimeMillis();

		Log.outln("==== Analysing Source:" + pluginFolderPath);
		File folder = new File(pluginFolderPath);
		if (null == folder) {
			Log.outln("==== nothing here.");
			return;
		}
		File[] listOfFiles = folder.listFiles();
		long pluginAnalysedCounter = 0;
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				String pluginJarName = listOfFiles[i].getName();
				if (pluginJarName.toLowerCase().endsWith(".jar")) {
					// this means that this is a plugin jar (it is assumed that
					// this would be a plugin jar if it is at this location)
					pluginAnalysedCounter++;
					analyseAndRecordAllInformationFromPluginJar(
							pluginFolderPath, pluginJarName, outputLocation);

				}

			} else if (listOfFiles[i].isDirectory()) {

				// some plugins may be unpacked and so exist as directories
				// instead of jars.
				pluginAnalysedCounter++;
				analyseAndRecordAllInformationFromPluginDir(pluginFolderPath,
						listOfFiles[i].getName(), outputLocation);
				// Log.outln("Directory " + listOfFiles[i].getName());
			}
		}
		long l2 = System.currentTimeMillis();
		  Log.outln(pluginAnalysedCounter + " plugin have been analyzed");
	  Log.errln(pluginAnalysedCounter + " plugin have been analyzed");
		Log.outln("for source:" + pluginFolderPath + " time: "
				+ Util.getFormattedTime(l2 - l1));
		Log.errln("for source:" + pluginFolderPath + " time: "
				+ Util.getFormattedTime(l2 - l1));
		// String pluginJarName
		// ="com.android.ide.eclipse.adt_21.0.1.2012-12-6-2-58.jar";
	}

	/**
	 * @param pathPrefix
	 * @param pluginDirName
	 * @param outputLocation
	 * @throws IOException
	 */
	public static void analyseAndRecordAllInformationFromPluginDir(
			String pathPrefix, String pluginDirName, String outputLocation)
			throws IOException {
		long l1 = System.currentTimeMillis();

		try {
			DependencyVisitor v = new DependencyVisitor();
			BundleInformation bundleInformation = new BundleInformation();
			String dirNameWithPathFull = pathPrefix + pluginDirName;

			File folder = new File(dirNameWithPathFull);
			if (null == folder || !folder.isDirectory()) {
				Log.outln("==== ==== nothing here.");
				return;
			}

			bundleInformation = getBundleManifestAndMetaInformationFromDir(folder);
			Log.outln("now starting the plugin_from_dir dependency  extraction for  : "
							+ folder.getPath());
			extractDependenciesAndExportsFromDir(v, bundleInformation, folder);

			writeData(v, bundleInformation, folder.getName(),
					outputLocation);
			long l2 = System.currentTimeMillis();

			Log.errln("==== analysed:  \n " + dirNameWithPathFull
					+ "\n time: "
					+ Util.getFormattedTime(l2 - l1));
		} catch (Exception e) {
			Log.errln("xxxx ERROR WHILE ANALYSING PLUGIN Folder : "
					+ pathPrefix + pluginDirName);
			e.printStackTrace();
		}

	}

	/**
	 * @param folder
	 * @return
	 */
	private static BundleInformation getBundleManifestAndMetaInformationFromDir(
			File folder) {
		// recursively constructing a set of paths of all files in this plugin
		// folder.
		Set<String> dirFileList = Util
				.listFilesForFolder(folder);
		BundleInformation bundleInformation = new BundleInformation();
		// getting plugin meta-inf/manifest.mf manifest information

		Iterator<String> en = dirFileList.iterator();
		boolean flag_manifestFound = false, flag_pluginxmlFound = false;
		StringBuffer pluginxmlText = new StringBuffer();
		while (en.hasNext() && (!flag_manifestFound || !flag_pluginxmlFound)) {

			File e = new File(en.next().toString());
			String name = e.getName();
			// Log.outln("==== handling :" + name);

			if (!flag_manifestFound
					&& name.toLowerCase().endsWith("meta-inf/manifest.mf")) {
				// getting the manifest.
				flag_manifestFound = true;
				try {

					Log.outln("== manifest try: file name = "
									+ folder.getPath() + ">" + name);

					bundleInformation = extractManifestInformation(new FileInputStream(
							e));

				} catch (Exception exception) {
				}
			} else if (!flag_pluginxmlFound
					&& name.toLowerCase().endsWith("plugin.xml")) {
				flag_pluginxmlFound = true;
				try {
					Log.outln("== plugin.xml capture: file name = "
									+ folder.getPath() + ">" + name);
					BufferedReader br;

					br = new BufferedReader(new FileReader(e));

					String ss;
					while ((ss = br.readLine()) != null) {
						pluginxmlText.append(ss);
					}
					br.close();
					// Log.outln(pluginxmlText);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
		// if(null!=bundleInformation)
		bundleInformation.setPluginXml(pluginxmlText.toString());
		return bundleInformation;

	}

	
	/**
	 * @param pathPrefix
	 * @param pluginJarName
	 * @param outputLocation
	 * @throws IOException
	 */
	public static void analyseAndRecordAllInformationFromPluginJar(
			String pathPrefix, String pluginJarName, String outputLocation)
			throws IOException {
		long l1 = System.currentTimeMillis();

		try {
			DependencyVisitor v = new DependencyVisitor();
			BundleInformation bundleInformation = new BundleInformation();
			// ////////archive/////////////////////////////////
			String jarFileNameWithPathFull = pathPrefix + pluginJarName;

			// ZipFile f = new ZipFile(jarFileNameWithPathFull);
			JarFile f = new JarFile(jarFileNameWithPathFull);

			// Actual part of getting the dependecies and offerrings from the
			// current jar file.////

			bundleInformation = getBundleManifestAndMetaInformationFromJar(f);
			Log.outln("== now starting the  plugin dependency  extraction");
			extractDependenciesAndExportsFromJarFile(v, bundleInformation, f);

			// ////////////////////////////////////////

			// ///////// //sing le class reading try //////////////

			/*
			 * jarFileNameWithPathFull=
			 * "./bin/com/shivanshusingh/PluginAnalyser_OLD/DUMMYFORTESTClassSignatureExtractor.class"
			 * ; File f = new File(jarFileNameWithPathFull); InputStream in= new
			 * FileInputStream(f); new ClassReader(in).accept(v, 0);
			 */
			// /////////////////////////// ////////////////////////

			writeData(v, bundleInformation, pluginJarName, outputLocation);
			long l2 = System.currentTimeMillis();

			Log.errln("==== analysed:  \n " + jarFileNameWithPathFull
					+ "\n time: "
					+ Util.getFormattedTime(l2 - l1));

		} catch (Exception e) {
			Log.errln("xxxx ERROR WHILE ANALYSING PLUGIN Jar : "
					+ pathPrefix + pluginJarName);
			e.printStackTrace();
		}
	}

	private static long internalFileCounter = 0;

	/**
	 * @param jarfileinstance
	 * @return
	 * @throws IOException
	 */
	private static BundleInformation getBundleManifestAndMetaInformationFromJar(
			JarFile jarfileinstance) throws IOException {

		BundleInformation bundleInformation = new BundleInformation();
		// getting plugin meta-inf/manifest.mf manifest information
		Enumeration<? extends JarEntry> en = jarfileinstance.entries();

		boolean flag_manifestFound = false, flag_pluginxmlFound = false;
		StringBuffer pluginxmlText = new StringBuffer();
		while (en.hasMoreElements()
				&& (!flag_manifestFound || !flag_pluginxmlFound)) {

			JarEntry e = en.nextElement();
			String name = e.getName();

			if (!flag_manifestFound
					&& name.toLowerCase().endsWith("meta-inf/manifest.mf")) {
				// getting the manifest.
				flag_manifestFound = true;
				try {

					Log.outln("== manifest try: file name = "
									+ jarfileinstance.getName() + ">" + name);

					/*
					 * Here printing out the detected Manifest file just for
					 * debugging purposes.
					 */
					/*
					 * if (name.toLowerCase().endsWith("manifest.mf")) {
					 * Log.outln("====== indicated manifest entry");
					 * BufferedReader br = new BufferedReader(new
					 * InputStreamReader( jarfileinstance.getInputStream(e)));
					 * String ss; while ((ss = br.readLine()) != null) {
					 * Log.outln(ss + "/////////"); }
					 * 
					 * extractManifestInformation(jarfileinstance.getInputStream(
					 * e));
					 * 
					 * }
					 */
					/* /////////////////////////////////// */
					bundleInformation = extractManifestInformation(jarfileinstance
							.getInputStream(e));
					// extractManifestInformation(jarfileinstance.getManifest());

				} catch (Exception exception) {
				}
			} else if (!flag_pluginxmlFound
					&& name.toLowerCase().endsWith("plugin.xml")) {
				flag_pluginxmlFound = true;
				Log.outln("== plugin.xml capture. : file name = "
								+ jarfileinstance.getName() + ">" + name);
				BufferedReader br = new BufferedReader(new InputStreamReader(
						jarfileinstance.getInputStream(e)));
				String ss;
				while ((ss = br.readLine()) != null) {
					pluginxmlText.append(ss);
				}
				br.close();
				// Log.outln(pluginxmlText);
			}
		}
		if (null != bundleInformation)
			bundleInformation.setPluginXml(pluginxmlText.toString());
		return bundleInformation;

	}

	/**
	 * @param v  {@link DependencyVisitor}
	 * @param bundleInformation  {@link BundleInformation}
	 * @param jarfileinstance
	 * @throws IOException
	 */
	private static void extractDependenciesAndExportsFromJarFile(
			DependencyVisitor v, BundleInformation bundleInformation,
			JarFile jarfileinstance) throws IOException {

		// // for zip files reading ///////////////
		// ZipFile f = new ZipFile(jarFileNameWithPathFull.trim());
		// Enumeration<? extends ZipEntry> en = f.entries();
		// ///////////////////////////////////////////

		Log.outln("==== Starting the Archive : "
				+ jarfileinstance.getName() + " analysis ====");

		Enumeration<? extends JarEntry> en = jarfileinstance.entries();
		int classesAnalyzedCounter = 0;

		while (en.hasMoreElements()) {
			JarEntry e = en.nextElement();

			String name = e.getName();
			// Log.outln(name);

			if (name.toLowerCase().endsWith(".class")) {

				classesAnalyzedCounter++;
				new ClassReader(jarfileinstance.getInputStream(e)).accept(v, 0);

			} else if (name.toLowerCase().endsWith(".jar")) {
				// nested jar.

				Log.outln("====> "+name + " found");
				// Log.outln(bundleInformation.getClasspathEntries().toString());

				// now check if this nested jar file is one of the Bundle
				// classpath dependencies (lib jars)
				if (null != bundleInformation) {
					for (String libJarNameEnding : bundleInformation
							.getClasspathEntries()) {
						// Log.outln("CHECKING:"+name+": ends with:"+libJarNameEnding);
						if (name.toLowerCase().endsWith(
								libJarNameEnding.toLowerCase())) {
							// good news, the jar is present in the bundle
							// manifest jar file entries' list.
							Log.outln("====> now analysing internal lib jar:"
											+ name);

							String TEMPFileName = (Util
									.getTEMP_DIR_PATH() + "/pa-sks-plugin-tmp-")
									.replace("//", "/")
									+ Math.random() + ( // jarfileinstance.getName()
														// // + "_" +
									name).replaceAll("/", "_")
											.replace(" ", "_");

							BufferedReader bufferedTempReader = new BufferedReader(
									new InputStreamReader(
											jarfileinstance.getInputStream(e)));

							BufferedWriter bufferedTempWriter = new BufferedWriter(
									new FileWriter(TEMPFileName));
							int inread;
							while ((inread = bufferedTempReader.read()) != -1) {
								bufferedTempWriter.write(inread);
							}
							bufferedTempWriter.close();
							bufferedTempReader.close();
							Log.outln("==== created : " + TEMPFileName
									+ "==== ");
							internalFileCounter++;
							extractDependenciesAndExportsFromJarFile(v,
									bundleInformation,
									new JarFile(TEMPFileName));
							Log.outln("==== delete = "
									+ new File(TEMPFileName).delete() + " : "
									+ TEMPFileName + "====");
							Log.outln("==== ==== ==== ==== ");

							break;// get out when found and analysed.
						}

					}
				}

			}

		}
		Log.outln("==== ==== "+classesAnalyzedCounter + " Class Files read.");
		Log.outln("==== Ending the Archive : "
				+ jarfileinstance.getName() + " analysis ====");
		jarfileinstance.close();
	}

	/**
	 * @param v {@link DependencyVisitor}
	 * @param bundleInformation
	 * @param folder
	 * @throws IOException
	 */
	private static void extractDependenciesAndExportsFromDir(
			DependencyVisitor v, BundleInformation bundleInformation,
			File folder) throws IOException {

		Log.outln("==== Starting the Plugin_from_Dir : "
				+ folder.getCanonicalPath() + " analysis ====");

		// getting a recursive list of all files contained in this plugin dir.
		Set<String> dirFileList = Util
				.listFilesForFolder(folder);

		Iterator<String> en = dirFileList.iterator();

		int classesAnalyzedCounter = 0;

		while (en.hasNext()) {
			File e = new File(en.next());

			String name = e.getName();
			// Log.outln(name);

			if (name.toLowerCase().endsWith(".class")) {

				classesAnalyzedCounter++;
				new ClassReader(new FileInputStream(e)).accept(v, 0);

			} else if (name.toLowerCase().endsWith(".jar")) {
				// nested jar.

				Log.outln("====> "+name + " found");
				// Log.outln(bundleInformation.getClasspathEntries().toString());

				// now check if this nested jar file is one of the Bundle
				// classpath dependencies (lib jars)
				if (null != bundleInformation) {
					for (String libJarNameEnding : bundleInformation
							.getClasspathEntries()) {
						// Log.outln("CHECKING:"+name+": ends with:"+libJarNameEnding);
						if (name.toLowerCase().endsWith(
								libJarNameEnding.toLowerCase())) {
							// good news, the jar is present in the bundle
							// manifest jar file entries' list.
							Log.outln("====> now analysing internal lib jar:"
											+ name);

							String TEMPFileName = (Util
									.getTEMP_DIR_PATH() + "/pa-sks-plugin-tmp-")
									.replace("//", "/")
									+ Math.random() + (
									// jarfileinstance.getName()
									// // + "_" +
									name).replaceAll("/", "_")
											.replace(" ", "_");

							BufferedReader bufferedTempReader = new BufferedReader(
									new FileReader(e));

							BufferedWriter bufferedTempWriter = new BufferedWriter(
									new FileWriter(TEMPFileName));
							int inread;
							while ((inread = bufferedTempReader.read()) != -1) {
								bufferedTempWriter.write(inread);
							}
							bufferedTempWriter.close();
							bufferedTempReader.close();
							Log.outln("==== created : " + TEMPFileName
									+ "====");
							internalFileCounter++;
							extractDependenciesAndExportsFromJarFile(v,
									bundleInformation,
									new JarFile(TEMPFileName));
							Log.outln("==== delete = "
									+ new File(TEMPFileName).delete() + " : "
									+ TEMPFileName + "====");
							Log.outln("==== ==== ==== ==== ");

							break;// get out when found and analysed.
						}

					}
				}

			}

		}
		Log.outln(classesAnalyzedCounter + " Class Files read.");
		Log.outln("==== Ending the Plungin_from_Dir :  "
				+ folder.getCanonicalPath() + " analysis =====");

	}

	/**
	 * @param manifest
	 * @return
	 */
	private static BundleInformation extractManifestInformation(
			Manifest manifest) {
		BundleInformation bundleinfo = null;
		try {
			bundleinfo = new BundleInformation(manifest);
			// extractBundleInfo(bundleinfo);

		} catch (ParseException e) {
			Log.outln("xxxx  NO Manifest found here or cannot parse that  or maybe theres nothing to parse inthis.  ");
			// e.printStackTrace();
		}
		return bundleinfo;
	}

	/**
	 * @param manifestStream
	 * @return
	 */
	private static BundleInformation extractManifestInformation(
			InputStream manifestStream) {
		BundleInformation bundleInformation = null;

		try {

			bundleInformation = new BundleInformation(manifestStream);

			// extractBundleInfo(bundleInformation);

		} catch (ParseException e) {
			Log.outln("xxxx  NO Manifest found here or cannot parse that  or maybe theres nothing to parse inthis.    ");

			// e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bundleInformation;
	}

	/**
	 * @param v
	 * @param bundleinfo
	 * @param pluginFileName
	 * @param outputLocation
	 * @throws IOException
	 */
	private static void writeData(DependencyVisitor v,
			BundleInformation bundleinfo, String pluginFileName,
			String outputLocation) throws IOException {

		pluginFileName = pluginFileName.toLowerCase().trim();
		if (pluginFileName.endsWith(".jar") || pluginFileName.endsWith(".zip"))
			pluginFileName = pluginFileName.substring(0,
					pluginFileName.length() - 4);

		outputLocation = (outputLocation + "/").trim().replaceAll("//", "/");

		FileWriter fwriter = new FileWriter(outputLocation + Constants.PLUGIN_EXTRACT_FILE_PREFIX
				+ pluginFileName.replace('/', '_') + ".txt");
		BufferedWriter writer = new BufferedWriter(fwriter);

		List<String> allDetectedTypes = new ArrayList<String>(
				v.getAllDetectedTypes());
		List<String> allExternalDetectedTypes = new ArrayList<String>(
				v.getAllExternalDetectedTypes());
		List<String> allExternalNonJavaDetectedTypes = new ArrayList<String>(
				v.getAllExternalNonJavaDetectedTypes());

		List<String> allMyMethods = new ArrayList<String>(v.getAllMyMethods());
		List<String> allMyDeprecatedMethods = new ArrayList<String>(
				v.getAllMyDeprecatedMethods());
		List<String> allMyDeprecatedPublicMethods = new ArrayList<String>(
				v.getAllMyDeprecatedPublicMethods());
		List<String> allMyPublicMethods = new ArrayList<String>(
				v.getAllMyPublicMethods());

		List<String> allInvokations = new ArrayList<String>(
				v.getAllInvokations());
		List<String> allMyClasses = new ArrayList<String>(v.getAllMyClasses());
		List<String> allMyDeprecatedClasses = new ArrayList<String>(
				v.getAllMyDeprecatedClasses());
		List<String> allMyDeprecatedPublicClasses = new ArrayList<String>(
				v.getAllMyDeprecatedPublicClasses());
		List<String> allMyPublicClasses = new ArrayList<String>(
				v.getAllMyPublicClasses());

		Map<String, Map<String, Integer>> globals = v.getGlobals();
		List<String> jarPackages = new ArrayList<String>(globals.keySet());
		List<String> classPackages = new ArrayList<String>(v.getPackages());
		List<String> externalInvokations = new ArrayList<String>(
				v.getAllExternalMethodInvokations());
		List<String> externalNonJavaInvokations = new ArrayList<String>(
				v.getAllExternalNonJavaMethodInvokations());

		java.util.Collections.sort(allDetectedTypes);
		java.util.Collections.sort(allMyMethods);
		java.util.Collections.sort(allMyPublicMethods);
		java.util.Collections.sort(allMyDeprecatedMethods);
		java.util.Collections.sort(allMyDeprecatedPublicMethods);
		java.util.Collections.sort(allInvokations);
		java.util.Collections.sort(allMyClasses);
		java.util.Collections.sort(jarPackages);
		java.util.Collections.sort(classPackages);
		java.util.Collections.sort(externalInvokations);
		java.util.Collections.sort(externalNonJavaInvokations);
		java.util.Collections.sort(allExternalDetectedTypes);
		java.util.Collections.sort(allExternalNonJavaDetectedTypes);
		java.util.Collections.sort(allMyPublicClasses);
		java.util.Collections.sort(allMyDeprecatedClasses);
		java.util.Collections.sort(allMyDeprecatedPublicClasses);

		// /////////// BUNDLE MANIFEST ///////////////////
		// java.util.Collections.sort(bundleRequirements);
		// this is the set of other plugins that this plugin would depend on.
		// bundleinfo.getRequires() and bundleinfo.getImports() eventually point
		// to bundleinfo.getRequirements() without any differences.

		boolean flag_bundleInfoExists = true;
		if (null == bundleinfo || null == bundleinfo.getBundleInfo())
			flag_bundleInfoExists = false;

		writer.write("Bundle Requirements ======== \n");
		if (flag_bundleInfoExists) {

			for (Object s : bundleinfo.getRequirements())
				writer.write(s.toString() + "\n");
			// Log.outln("Bundle Requirements = " +
			// bundleinfo.getRequirements().toString()+"\n"+bundleinfo.getRequirements().size()
			// +" , Bundle Requirements"); // Require-Bundle
		}
		writer.write(Constants.TERMINATOR_MARKER+"\n");
		writer.write("Bundle Exports ======== \n");
		if (flag_bundleInfoExists) {
			for (Object s : bundleinfo.getExports())
				writer.write(s.toString() + "\n");
			// Log.outln("Bundle Exports = " +
			// bundleinfo.getExports().toString()); // Export-Package
		}
		writer.write(Constants.TERMINATOR_MARKER+"\n");
		writer.write("Symbolic Name ======== \n");
		if (flag_bundleInfoExists) {
			writer.write(null != bundleinfo.getSymbolicName() ? bundleinfo
					.getSymbolicName().toString() + "\n" : "");
			// Log.outln("Symbolic Name = "+
			// bundleinfo.getSymbolicName().toString());
		}
		writer.write(Constants.TERMINATOR_MARKER+"\n");
		writer.write("Version ======== \n");
		if (flag_bundleInfoExists) {
			writer.write(null != bundleinfo.getVersion() ? bundleinfo
					.getVersion().toString() + "\n" : "");
			// Log.outln("Version = " +
			// bundleinfo.getVersion().toString());
		}
		writer.write(Constants.TERMINATOR_MARKER+"\n");
		writer.write("Version without qualifier ========\n");
		if (flag_bundleInfoExists) {
			writer.write(null != bundleinfo.getVersion() ? bundleinfo
					.getVersion().withoutQualifier().toString()
					+ "\n" : "");
			// Log.outln("Version without qualifier  = " +
			// bundleinfo.getVersion().withoutQualifier().toString());
		}
		writer.write(Constants.TERMINATOR_MARKER+"\n");
		writer.write("Bundle Imports ========\n ");
		if (flag_bundleInfoExists) {
			for (Object s : bundleinfo.getImports())
				writer.write(s.toString() + "\n");
			// Log.outln("Bundle Imports = " +
			// bundleinfo.getImports().toString());
		}
		writer.write(Constants.TERMINATOR_MARKER+"\n");
		writer.write("Bundle ClassPathEntries ========\n");
		if (flag_bundleInfoExists) {
			for (Object s : bundleinfo.getClasspathEntries())
				writer.write(s.toString() + "\n");
			// Log.outln("Bundle ClassPathEntries  = " +
			// bundleinfo.getClasspathEntries().toString());
		}
		writer.write(Constants.TERMINATOR_MARKER+"\n");
		// Log.outln("Bundle hashcode  = " + bundleinfo.hashCode() );

		// //////////////////////////////////////////////////////

		writer.write(Constants.PLUGIN_ALL_MY_TYPES+"\n");
		//"All My Classes (Types)  ========\n");

		for (String s : allMyClasses) {
			writer.write(s + "\n");
		}
		writer.write(Constants.TERMINATOR_MARKER+"\n");
		// writer.write(allMyClasses.size() + "," + " own classes (types).\n");
		// Log.outln(allMyClasses.size() + "," +
		// " own classes (types).");

		writer.write(Constants.PLUGIN_ALL_MY_TYPES_PUBLIC+"\n");
		//"All My Public Classes (Types) ========\n");

		for (String s : allMyPublicClasses) {
			writer.write(s + "\n");
		}
		writer.write(Constants.TERMINATOR_MARKER+"\n");
		// writer.write(allMyPublicClasses.size() + ","+
		// " own public classes (types).\n");
		// Log.outln(allMyPublicClasses.size() + "," +
		// " own public classes (types).");

		writer.write(Constants.PLUGIN_ALL_MY_METHODS+"\n");
		//"All My Methods ========\n");

		for (String s : allMyMethods) {
			writer.write(s + "\n");
		}
		writer.write(Constants.TERMINATOR_MARKER+"\n");
		// writer.write(allMyMethods.size() + "," + " internal methods.\n");
		// Log.outln(allMyMethods.size() + "," + " internal methods.");

		writer.write(Constants.PLUGIN_ALL_MY_METHODS_PUBLIC+"\n");
		//"All My Public Methods ========\n");

		for (String s : allMyPublicMethods) {
			writer.write(s + "\n");
		}
		writer.write(Constants.TERMINATOR_MARKER+"\n");
		// writer.write(allMyPublicMethods.size() + ","+
		// " internal public methods.\n");
		// Log.outln(allMyPublicMethods.size() + "," +
		// " internal public methods.");

		writer.write(Constants.PLUGIN_ALL_MY_METHOD_CALLS+"\n");
		//"All Invokations ========\n");

		for (String s : allInvokations) {
			writer.write(s + "\n");
		}
		writer.write(Constants.TERMINATOR_MARKER+"\n");
		// writer.write(allInvokations.size() + "," +
		// " method invokations (intrnal and external).\n");
		// Log.outln(allInvokations.size() + "," +
		// " method invokations (intrnal and external).");

		writer.write(Constants.PLUGIN_ALL_MY_METHOD_CALLS_EXTERNAL+"\n");
		//"All External Invokations ========\n");

		for (String s : externalInvokations) {
			writer.write(s + "\n");
		}
		writer.write(Constants.TERMINATOR_MARKER+"\n");
		// writer.write(externalInvokations.size() + ","+
		// " method invokations (external).\n");
		// Log.outln(externalInvokations.size() + "," +
		// " method invokations (external).");

		writer.write(Constants.PLUGIN_ALL_MY_METHOD_CALLS_EXTERNAL_AND_NON_JAVA+"\n");
		//"All External and non Excluded Invokations ========\n");

		for (String s : externalNonJavaInvokations) {
			writer.write(s + "\n");
		}
		writer.write(Constants.TERMINATOR_MARKER+"\n");
		// writer.write(externalNonJavaInvokations.size() + "," +
		// " method invokations (external and non excluded).\n");
		// Log.outln(externalNonJavaInvokations.size() + ","
		// + " method invokations (external and non excluded).");

		writer.write(Constants.PLUGIN_ALL_TYPES_DETECTED+"\n");
		//"All Detected Types ========\n");
		for (String s : allDetectedTypes) {
			writer.write(s + "\n");
		}
		writer.write(Constants.TERMINATOR_MARKER+"\n");
		// writer.write(allDetectedTypes.size() + "," +
		// " types (internal and external).\n");
		// Log.outln(allDetectedTypes.size() + ","
		// + " types (internal and external).");

		writer.write(Constants.PLUGIN_ALL_TYPES_DETECTED_EXTERNAL+"\n");
		//"All External Detected Types ========\n");
		for (String s : allExternalDetectedTypes) {
			writer.write(s + "\n");
		}
		writer.write(Constants.TERMINATOR_MARKER+"\n");
		// writer.write(allExternalDetectedTypes.size() + "," +
		// " types (external).\n");
		// Log.outln(allExternalDetectedTypes.size() + ","
		// + " types (external).");

		writer.write(Constants.PLUGIN_ALL_TYPES_DETECTED_EXTERNAL_AND_NON_JAVA+"\n");
		//"All External Non Java Detected Types ========\n");
		for (String s : allExternalNonJavaDetectedTypes) {
			writer.write(s + "\n");
		}
		writer.write(Constants.TERMINATOR_MARKER+"\n");
		// writer.write(allExternalNonJavaDetectedTypes.size() + "," +
		// " types (external non excluded).\n");
		// Log.outln(allExternalNonJavaDetectedTypes.size() + ","
		// + " types (external Non excluded).");

		writer.write(Constants.PLUGIN_ALL_JAR_PACKAGES+"\n");
		//"All Jar Packages ========\n");

		for (String s : jarPackages) {
			writer.write(s + "\n");
		}
		writer.write(Constants.TERMINATOR_MARKER+"\n");
		// writer.write(jarPackages.size() + "," + " jar packages.\n");

		writer.write(Constants.PLUGIN_ALL_CLASS_PACKAGES+"\n");
		//"All  Class packages ========\n");

		for (String s : classPackages) {
			writer.write(s + "\n");
		}
		writer.write(Constants.TERMINATOR_MARKER+"\n");
		// writer.write(classPackages.size() + "," + " class packages.\n");
		writer.write(Constants.PLUGIN_ALL_MY_METHODS_DEPRECATED+"\n");
		//"All My Deprecated Methods ========\n");

		for (String s : allMyDeprecatedMethods) {
			writer.write(s + "\n");
		}
		writer.write(Constants.TERMINATOR_MARKER+"\n");
		// writer.write(allMyDeprecatedMethods.size() + "," +
		// " deprecated methods.\n");
		// .println(allMyDeprecatedMethods.size() + ","
		// + " deprecated methods.");

		writer.write(Constants.PLUGIN_ALL_MY_TYPES_DEPRECATED+"\n");
		//"All My Deprecated Classes ========\n");

		for (String s : allMyDeprecatedClasses) {
			writer.write(s + "\n");
		}
		writer.write(Constants.TERMINATOR_MARKER+"\n");
		
		// writer.write(allMyDeprecatedClasses.size() + "," +
		// " deprecated   classes. \n");
		// Log.outln(allMyDeprecatedClasses.size() + ","
		// + " deprecated   classes.");

		writer.write("Bundle Plugin.xml ========\n");
		if (null != bundleinfo) {// this means that there won't be any
									// plugin.xml available.
			writer.write(bundleinfo.getPluginXml() + "\n");
		}
		writer.write(Constants.TERMINATOR_MARKER+"\n");

		// writer.write("===================================================\n");
		writer.close();
		fwriter.close();
	}

	/**
	 * @deprecated Do not use this.
	 * @param bundleinfo
	 */
	@SuppressWarnings({ "unused", "unchecked" })
	private static void extractBundleInfo(BundleInformation bundleinfo) {
		Set<String> bundleExports = new LinkedHashSet<String>(
				bundleinfo.getExports());
		Set<String> bundleRequires = new LinkedHashSet<String>(
				bundleinfo.getRequires());

		// Java version so no use
		List<String> bundleExecEnv = new ArrayList<String>(
				bundleinfo.getExecutionEnvironments());

		// this is the set of other plugins that this plugin would depend on.
		// bundleinfo.getRequires() and bundleinfo.getImports() eventually point
		// to bundleinfo.getRequirements() without any differences.
		Log.outln("Bundle Requirements = "
				+ bundleinfo.getRequirements().toString()); // Require-Bundle
		// //////
		Log.outln("Bundle Exports = " + bundleExports.toString()); // Export-Package
		Log.outln("Name Symbolic = "
				+ bundleinfo.getSymbolicName().toString());
		Log.outln("Version = " + bundleinfo.getVersion().toString());
		Log.outln("Version without qualifier  = "
				+ bundleinfo.getVersion().withoutQualifier().toString());
		Log.outln("Bundle Imports  = "
				+ bundleinfo.getImports().toString());
		Log.outln("Bundle ClassPathEntries  = "
				+ bundleinfo.getClasspathEntries().toString());
		Log.outln("Bundle hashcode  = " + bundleinfo.hashCode());

	}

}