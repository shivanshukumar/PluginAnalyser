package com.shivanshusingh.PluginAnalyser.Analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

import com.shivanshusingh.PluginAnalyser.Utils.PluginAnalyserUtils;

/**
 * 
 * @author singhsk
 * 
 */
public class DependencyTracker extends ManifestParser {

	/**
	 * @param pluginFolderPath
	 * @throws IOException
	 */
	public static void analyseAndRecordAllInformationFromBasePluginFolder(
			String pluginFolderPath, String outputLocation) throws IOException {
		if(!PluginAnalyserUtils.checkAndCreateDirectory(outputLocation))
		{
			System.err.println("Error Accessing/Creating Output Directory for Plugin Analysis Output at: "+outputLocation+"\n Cannot continue with the analysis." );
			return;
		}
		// reading all the files (plugin jars) in the specified plugin folder
		long l1 = System.currentTimeMillis();

		System.out.println("=======Analysing Source:" + pluginFolderPath);
		File folder = new File(pluginFolderPath);
		if (null == folder) {
			System.out.println("==== nothing here.");
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
				System.out.println("Directory " + listOfFiles[i].getName());
			}
		}
		long l2 = System.currentTimeMillis();
		System.out.println(pluginAnalysedCounter + " plugin have been analyzed");
		System.err.println(pluginAnalysedCounter + " plugin have been analyzed");
		System.out.println("for  source:" + pluginFolderPath + "  time: "
				+ (l2 - l1) / 1000f + " seconds. \n");
		System.err.println("for  source:" + pluginFolderPath + "  time: "
				+ (l2 - l1) / 1000f + " seconds. \n");
		// String pluginJarName
		// ="com.android.ide.eclipse.adt_21.0.1.2012-12-6-2-58.jar";
	}

	public static void analyseAndRecordAllInformationFromPluginDir(
			String pathPrefix, String pluginDirName, String outputLocation) throws IOException {

		try {
			DependencyVisitor v = new DependencyVisitor();
			BundleInformation bundleInformation = new BundleInformation();
			String dirNameWithPathFull = pathPrefix + pluginDirName;

			long l1 = System.currentTimeMillis();

			File folder = new File(dirNameWithPathFull);
			if (null == folder || !folder.isDirectory()) {
				System.out.println("==== ==== nothing here.");
				return;
			}

			bundleInformation = getBundleManifestAndMetaInformationFromDir(folder);
			System.out
					.println("now starting the plugin_from_dir dependency  extraction for  : "
							+ folder.getPath());
			extractDependenciesAndExportsFromDir(v, bundleInformation, folder);

			long l2 = System.currentTimeMillis();

			writeDataToFile(v, bundleInformation, folder.getName(), outputLocation);

			System.err.println("time: " + (l2 - l1) / 1000f + " seconds. \n");
		} catch (Exception e) {
			System.err.println("ERROR WHILE ANALYSING PLUGIN Folder : "
					+ pathPrefix + pluginDirName);
			e.printStackTrace();
		}

	}

	private static BundleInformation getBundleManifestAndMetaInformationFromDir(
			File folder) {
		// recursively constructing a set of paths of all files in this plugin
		// folder.
		Set<String> dirFileList = PluginAnalyserUtils
				.listFilesForFolder(folder);
		BundleInformation bundleInformation = new BundleInformation();
		// getting plugin meta-inf/manifest.mf manifest information

		Iterator<String> en = dirFileList.iterator();
		boolean flag_manifestFound = false, flag_pluginxmlFound = false;
		StringBuffer pluginxmlText = new StringBuffer();
		while (en.hasNext() && (!flag_manifestFound || !flag_pluginxmlFound)) {

			File e = new File(en.next().toString());
			String name = e.getName();
			//System.out.println("==== handling :" + name);

			if (!flag_manifestFound
					&& name.toLowerCase().endsWith("meta-inf/manifest.mf")) {
				// getting the manifest.
				flag_manifestFound = true;
				try {

					System.out
							.println("==== ==== ====  manifest try: file name = "
									+ folder.getPath() + ">" + name);

					bundleInformation = extractManifestInformation(new FileInputStream(
							e));

				} catch (Exception exception) {
				}
			} else if (!flag_pluginxmlFound
					&& name.toLowerCase().endsWith("plugin.xml")) {
				flag_pluginxmlFound = true;
				try {
					System.out.println("==== ==== ====  plugin.xml capture: file name = "+folder.getPath() + ">" + name);
					BufferedReader br;

					br = new BufferedReader(new FileReader(e));

					String ss;
					while ((ss = br.readLine()) != null) {
						pluginxmlText.append(ss);
					}
					br.close();
					//System.out.println(pluginxmlText);
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
	 * @throws IOException
	 */
	public static void analyseAndRecordAllInformationFromPluginJar(
			String pathPrefix, String pluginJarName, String outputLocation) throws IOException {
		try {
			DependencyVisitor v = new DependencyVisitor();
			BundleInformation bundleInformation = new BundleInformation();
			// ////////archive/////////////////////////////////
			String jarFileNameWithPathFull = pathPrefix + pluginJarName;

			long l1 = System.currentTimeMillis();

			// ZipFile f = new ZipFile(jarFileNameWithPathFull);
			JarFile f = new JarFile(jarFileNameWithPathFull);

			// Actual part of getting the dependecies and offerrings from the
			// current jar file.////

			bundleInformation = getBundleManifestAndMetaInformationFromJar(f);
			System.out
					.println("now starting the  plugin dependency  extraction");
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

			long l2 = System.currentTimeMillis();

			writeDataToFile(v, bundleInformation, pluginJarName, outputLocation);

			System.err.println("time: " + (l2 - l1) / 1000f + " seconds. \n");
		} catch (Exception e) {
			System.err.println("ERROR WHILE ANALYSING PLUGIN Jar : "
					+ pathPrefix + pluginJarName);
			e.printStackTrace();
		}
	}

	private static long internalFileCounter = 0;

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

					System.out
							.println("==== ==== ====  manifest try: file name = "
									+ jarfileinstance.getName() + ">" + name);

					/*
					 * Here printing out the detected Manifest file just for
					 * debugging purposes.
					 */
					/*
					 * if (name.toLowerCase().endsWith("manifest.mf")) {
					 * System.out.println("====== indicated manifest entry");
					 * BufferedReader br = new BufferedReader(new
					 * InputStreamReader( jarfileinstance.getInputStream(e)));
					 * String ss; while ((ss = br.readLine()) != null) {
					 * System.out.println(ss + "/////////"); }
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
				System.out.println("==== ==== ====  plugin.xml capture. : file name = "+jarfileinstance.getName() + ">" + name);
				BufferedReader br = new BufferedReader(new InputStreamReader(
						jarfileinstance.getInputStream(e)));
				String ss;
				while ((ss = br.readLine()) != null) {
					pluginxmlText.append(ss);
				}
				br.close();
				//System.out.println(pluginxmlText);
			}
		}
		if (null != bundleInformation)
			bundleInformation.setPluginXml(pluginxmlText.toString());
		return bundleInformation;

	}

	private static void extractDependenciesAndExportsFromJarFile(
			DependencyVisitor v, BundleInformation bundleInformation,
			JarFile jarfileinstance) throws IOException {

		// // for zip files reading ///////////////
		// ZipFile f = new ZipFile(jarFileNameWithPathFull.trim());
		// Enumeration<? extends ZipEntry> en = f.entries();
		// ///////////////////////////////////////////
		long thisInterArchiveFileNumber = internalFileCounter;

		System.out.println("==== ====  Starting the Archive 1."
				+ internalFileCounter + " : " + jarfileinstance.getName()
				+ " analysis ===================");

		Enumeration<? extends JarEntry> en = jarfileinstance.entries();
		int classesAnalyzedCounter = 0;

		while (en.hasMoreElements()) {
			JarEntry e = en.nextElement();

			String name = e.getName();
			// System.out.println(name);

			if (name.toLowerCase().endsWith(".class")) {

				classesAnalyzedCounter++;
				new ClassReader(jarfileinstance.getInputStream(e)).accept(v, 0);

			} else if (name.toLowerCase().endsWith(".jar")) {
				// nested jar.

				System.out.println(name + " found");
				// System.out.println(bundleInformation.getClasspathEntries().toString());

				// now check if this nested jar file is one of the Bundle
				// classpath dependencies (lib jars)
				if (null != bundleInformation) {
					for (String libJarNameEnding : bundleInformation
							.getClasspathEntries()) {
						// System.out.println("CHECKING:"+name+": ends with:"+libJarNameEnding);
						if (name.toLowerCase().endsWith(
								libJarNameEnding.toLowerCase())) {
							// good news, the jar is present in the bundle
							// manifest jar file entries' list.
							System.out
									.println("now analysing internal lib jar:"
											+ name);

							String TEMPFileName = (PluginAnalyserUtils.getTEMP_DIR_PATH()+"/pa-sks-plugin-tmp-").replace("//", "/")
									+ Math.random() + ( // jarfileinstance.getName()
														// // + "_" +
									name).replaceAll("/", "_")
											.replace(" ", "_")
											;

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
							System.out.println("==== created : " + TEMPFileName
									+ "============ ");
							internalFileCounter++;
							extractDependenciesAndExportsFromJarFile(v,
									bundleInformation,
									new JarFile(TEMPFileName));
							System.out.println("==== delete = "
									+ new File(TEMPFileName).delete() + " : "
									+ TEMPFileName + "============ ");
							System.out.println("==== ==== ==== ==== ");

							break;// get out when found and analysed.
						}

					}
				}

			}

		}
		jarfileinstance.close();
		System.out.println(classesAnalyzedCounter + " Class Files read.");
		System.out.println(internalFileCounter + " internal Jar Files read.");
		System.out.println("==== ==== Ending the Archive 1."
				+ thisInterArchiveFileNumber + " analysis ===================");

	}

	private static void extractDependenciesAndExportsFromDir(
			DependencyVisitor v, BundleInformation bundleInformation,
			File folder) throws IOException {

		long thisInterArchiveFileNumber = internalFileCounter;

		System.out.println("==== ====  Starting the Plugin_from_Dir 1."
				+ internalFileCounter + " : " + folder.getName()
				+ " analysis ===================");

		// getting a recursive list of all files contained in this plugin dir.
		Set<String> dirFileList = PluginAnalyserUtils
				.listFilesForFolder(folder);

		Iterator<String> en = dirFileList.iterator();

		int classesAnalyzedCounter = 0;

		while (en.hasNext()) {
			File e = new File(en.next());

			String name = e.getName();
			// System.out.println(name);

			if (name.toLowerCase().endsWith(".class")) {

				classesAnalyzedCounter++;
				new ClassReader(new FileInputStream(e)).accept(v, 0);

			} else if (name.toLowerCase().endsWith(".jar")) {
				// nested jar.

				System.out.println(name + " found");
				// System.out.println(bundleInformation.getClasspathEntries().toString());

				// now check if this nested jar file is one of the Bundle
				// classpath dependencies (lib jars)
				if (null != bundleInformation) {
					for (String libJarNameEnding : bundleInformation
							.getClasspathEntries()) {
						// System.out.println("CHECKING:"+name+": ends with:"+libJarNameEnding);
						if (name.toLowerCase().endsWith(
								libJarNameEnding.toLowerCase())) {
							// good news, the jar is present in the bundle
							// manifest jar file entries' list.
							System.out
									.println("now analysing internal lib jar:"
											+ name);

							String TEMPFileName = (PluginAnalyserUtils.getTEMP_DIR_PATH()+"/pa-sks-plugin-tmp-").replace("//", "/")
									+ Math.random() 
									+ (
											// jarfileinstance.getName()
														// // + "_" +
									name
									)
									.replaceAll("/", "_")
									.replace(" ", "_")
									;

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
							System.out.println("==== created : " + TEMPFileName
									+ "============ ");
							internalFileCounter++;
							extractDependenciesAndExportsFromJarFile(v,
									bundleInformation,
									new JarFile(TEMPFileName));
							System.out.println("==== delete = "
									+ new File(TEMPFileName).delete() + " : "
									+ TEMPFileName + "============ ");
							System.out.println("==== ==== ==== ==== ");

							break;// get out when found and analysed.
						}

					}
				}

			}

		}

		System.out.println(classesAnalyzedCounter + " Class Files read.");
		System.out.println(internalFileCounter + " internal Jar Files read.");
		System.out.println("==== ==== Ending the Plungin_from_Dir 1."
				+ thisInterArchiveFileNumber + " analysis ===================");

	}

	private static BundleInformation extractManifestInformation(
			Manifest manifest) {
		BundleInformation bundleinfo = null;
		try {
			bundleinfo = new BundleInformation(manifest);
			// extractBundleInfo(bundleinfo);

		} catch (ParseException e) {
			System.out
					.println("  NO Manifest found here or cannot parse that  or maybe theres nothing to parse inthis.  ");
			// e.printStackTrace();
		}
		return bundleinfo;
	}

	private static BundleInformation extractManifestInformation(
			InputStream manifestStream) {
		BundleInformation bundleInformation = null;

		try {

			bundleInformation = new BundleInformation(manifestStream);

			// extractBundleInfo(bundleInformation);

		} catch (ParseException e) {
			System.out
					.println("  NO Manifest found here or cannot parse that  or maybe theres nothing to parse inthis.    ");

			// e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bundleInformation;
	}

	private static void writeDataToFile(DependencyVisitor v,
			BundleInformation bundleinfo, String pluginFileName, String outputLocation)
			throws IOException {

		
		pluginFileName = pluginFileName.toLowerCase().trim();
		if (pluginFileName.endsWith(".jar") || pluginFileName.endsWith(".zip"))
			pluginFileName = pluginFileName.substring(0,
					pluginFileName.length() - 4);

		outputLocation=(outputLocation+"/").trim().replaceAll("//", "/");
		
		FileWriter fwriter = new FileWriter(outputLocation+"PLUGIN-EXTRACT-"
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
		
		boolean flag_bundleInfoExists=true;
		if(null==bundleinfo  ||  null==bundleinfo.getBundleInfo() )
			flag_bundleInfoExists=false;
		
			
			writer.write("Bundle Requirements ======== \n");
			if(flag_bundleInfoExists)
			{
				
				for(Object s:bundleinfo.getRequirements())
					writer.write(  s.toString()  + "\n");
				//System.out.println("Bundle Requirements = "  + bundleinfo.getRequirements().toString()+"\n"+bundleinfo.getRequirements().size() +" , Bundle Requirements"); // Require-Bundle
			}
			writer.write("--------\n");
			writer.write("Bundle Exports ======== \n");
			if(flag_bundleInfoExists)
			{
				for(Object s:bundleinfo.getExports())
					writer.write(  s.toString()  + "\n");
				//System.out.println("Bundle Exports = " + bundleinfo.getExports().toString()); // Export-Package
			}
			writer.write("--------\n");
			writer.write("Symbolic Name ======== \n");
			if(flag_bundleInfoExists)
			{
			writer.write(null!= bundleinfo.getSymbolicName()? bundleinfo.getSymbolicName().toString()+"\n" :"" );
			//System.out.println("Symbolic Name = "+ bundleinfo.getSymbolicName().toString());
			}
			writer.write("--------\n");
			writer.write("Version ======== \n");
			if(flag_bundleInfoExists)
			{
			writer.write(null!=bundleinfo.getVersion()? bundleinfo.getVersion().toString()+"\n" : ""  );
			//System.out.println("Version = " + bundleinfo.getVersion().toString());
			}
			writer.write("--------\n");
			writer.write("Version without qualifier ========\n");
			if(flag_bundleInfoExists)
			{
			writer.write(null!=bundleinfo.getVersion()? bundleinfo.getVersion().withoutQualifier().toString()  +"\n" :"" );
			//System.out.println("Version without qualifier  = "		+ bundleinfo.getVersion().withoutQualifier().toString());
			}
			writer.write("--------\n");
			writer.write("Bundle Imports ========\n ");
			if(flag_bundleInfoExists)
			{
			for(Object s:bundleinfo.getImports())
				writer.write(  s.toString()  + "\n");
			//System.out.println("Bundle Imports = "		+ bundleinfo.getImports().toString());
			}
			writer.write("--------\n");
			writer.write("Bundle ClassPathEntries ========\n");
			if(flag_bundleInfoExists)
			{
			for(Object s:bundleinfo.getClasspathEntries())
				writer.write(  s.toString()  + "\n");
			//System.out.println("Bundle ClassPathEntries  = "		+ bundleinfo.getClasspathEntries().toString());
			}
			writer.write("--------\n");
			//System.out.println("Bundle hashcode  = "		+ bundleinfo.hashCode()    );
			
		
		
		////////////////////////////////////////////////////////
		
		writer.write("All My Classes (Types)  ========\n");

		for (String s : allMyClasses) {
			writer.write(s + "\n");
		}
		writer.write("--------\n");
		// writer.write(allMyClasses.size() + "," + " own classes (types).\n");
		System.out.println(allMyClasses.size() + "," + " own classes (types).");

		writer.write("All My Public Classes (Types) ========\n");

		for (String s : allMyPublicClasses) {
			writer.write(s + "\n");
		}
		writer.write("--------\n");
		// writer.write(allMyPublicClasses.size() + ","+
		// " own public classes (types).\n");
		System.out.println(allMyPublicClasses.size() + ","
				+ " own public classes (types).");

		writer.write("All My Methods ========\n");

		for (String s : allMyMethods) {
			writer.write(s + "\n");
		}
		writer.write("--------\n");
		// writer.write(allMyMethods.size() + "," + " internal methods.\n");
		System.out.println(allMyMethods.size() + "," + " internal methods.");

		writer.write("All My Public Methods ========\n");

		for (String s : allMyPublicMethods) {
			writer.write(s + "\n");
		}
		writer.write("--------\n");
		// writer.write(allMyPublicMethods.size() + ","+
		// " internal public methods.\n");
		System.out.println(allMyPublicMethods.size() + ","
				+ " internal public methods.");

		writer.write("All Invokations ========\n");

		for (String s : allInvokations) {
			writer.write(s + "\n");
		}
		writer.write("--------\n");
		// writer.write(allInvokations.size() + "," +
		// " method invokations (intrnal and external).\n");
		System.out.println(allInvokations.size() + ","
				+ " method invokations (intrnal and external).");

		writer.write("All External Invokations ========\n");

		for (String s : externalInvokations) {
			writer.write(s + "\n");
		}
		writer.write("--------\n");
		// writer.write(externalInvokations.size() + ","+
		// " method invokations (external).\n");
		System.out.println(externalInvokations.size() + ","
				+ " method invokations (external).");

		writer.write("All External and non Excluded Invokations ========\n");

		for (String s : externalNonJavaInvokations) {
			writer.write(s + "\n");
		}
		writer.write("--------\n");
		// writer.write(externalNonJavaInvokations.size() + "," +
		// " method invokations (external and non excluded).\n");
		System.out.println(externalNonJavaInvokations.size() + ","
				+ " method invokations (external and non excluded).");

		writer.write("All Detected Types ========\n");
		for (String s : allDetectedTypes) {
			writer.write(s + "\n");
		}
		writer.write("--------\n");
		// writer.write(allDetectedTypes.size() + "," +
		// " types (internal and external).\n");
		System.out.println(allDetectedTypes.size() + ","
				+ " types (internal and external).");

		writer.write("All External Detected Types ========\n");
		for (String s : allExternalDetectedTypes) {
			writer.write(s + "\n");
		}
		writer.write("--------\n");
		// writer.write(allExternalDetectedTypes.size() + "," +
		// " types (external).\n");
		System.out.println(allExternalDetectedTypes.size() + ","
				+ " types (external).");

		writer.write("All External Non Java Detected Types ========\n");
		for (String s : allExternalNonJavaDetectedTypes) {
			writer.write(s + "\n");
		}
		writer.write("--------\n");
		// writer.write(allExternalNonJavaDetectedTypes.size() + "," +
		// " types (external non excluded).\n");
		System.out.println(allExternalNonJavaDetectedTypes.size() + ","
				+ " types (external Non excluded).");

		writer.write("All Jar Packages ========\n");

		for (String s : jarPackages) {
			writer.write(s + "\n");
		}
		writer.write("--------\n");
		// writer.write(jarPackages.size() + "," + " jar packages.\n");

		writer.write("All  Class packages ========\n");

		for (String s : classPackages) {
			writer.write(s + "\n");
		}
		writer.write("--------\n");
		// writer.write(classPackages.size() + "," + " class packages.\n");
		writer.write("All My Deprecated Methods ========\n");

		for (String s : allMyDeprecatedMethods) {
			writer.write(s + "\n");
		}
		writer.write("--------\n");
		// writer.write(allMyDeprecatedMethods.size() + "," +
		// " deprecated methods.\n");
		System.out.println(allMyDeprecatedMethods.size() + ","
				+ " deprecated methods.");

		writer.write("All My Deprecated Classes ========\n");

		for (String s : allMyDeprecatedClasses) {
			writer.write(s + "\n");
		}
		writer.write("--------\n");
		// writer.write(allMyDeprecatedClasses.size() + "," +
		// " deprecated   classes. \n");
		System.out.println(allMyDeprecatedClasses.size() + ","
				+ " deprecated   classes.");

		writer.write("Bundle Plugin.xml ========\n");
		if (null != bundleinfo) {// this means that there won't be any
									// plugin.xml available.
			writer.write(bundleinfo.getPluginXml() + "\n");
		}
		writer.write("--------\n");

		// writer.write("===================================================\n");
		writer.close();
		fwriter.close();
	}

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
		System.out.println("Bundle Requirements = "
				+ bundleinfo.getRequirements().toString()); // Require-Bundle
		// //////
		System.out.println("Bundle Exports = " + bundleExports.toString()); // Export-Package
		System.out.println("Name Symbolic = "
				+ bundleinfo.getSymbolicName().toString());
		System.out.println("Version = " + bundleinfo.getVersion().toString());
		System.out.println("Version without qualifier  = "
				+ bundleinfo.getVersion().withoutQualifier().toString());
		System.out.println("Bundle Imports  = "
				+ bundleinfo.getImports().toString());
		System.out.println("Bundle ClassPathEntries  = "
				+ bundleinfo.getClasspathEntries().toString());
		System.out.println("Bundle hashcode  = " + bundleinfo.hashCode());

	}

}