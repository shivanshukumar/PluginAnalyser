package com.shivanshusingh.PluginAnalyser.Analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.shivanshusingh.PluginAnalyser.Utils.PluginAnalyserUtils;

/**
 * this is the class that analyses features (dir or jar).
 * 
 * @author Shivanshu Singh
 * 
 */
public class FeatureAnalyser {

	/**
	 * analyses and records in output files, all the metada for the features in
	 * the given directory.
	 * 
	 * @param featureFolderPath
	 *            is the path of the directory where the features to be analysed
	 *            are available.
	 * @param outputLocation
	 *            is the path of the directory where the output files containing
	 *            the information extracted from features will be stored. The
	 *            location should be available, however this method takes care
	 *            of creating the directory structure if it does not already
	 *            exist. If it already exists, any data available at the may be
	 *            over written.
	 */
	public static void analyseAndRecordAllInformationFromBaseFeautreFolder(
			String featureFolderPath, String outputLocation) throws IOException {

		if (!PluginAnalyserUtils.checkAndCreateDirectory(outputLocation)) {
			System.err
					.println("Error Accessing/Creating Output Directory for  Feature  Analysis Output at: "
							+ outputLocation
							+ "\n Cannot continue with the analysis.");
			return;
		}
		// reading all the files (feature jars) in the specified feature folder
		long l1 = System.currentTimeMillis();

		System.out.println("=======Analysing  Feature Source:"
				+ featureFolderPath);
		File folder = new File(featureFolderPath);
		if (null == folder) {
			System.out.println("==== nothing here.");
			return;
		}
		File[] listOfFiles = folder.listFiles();
		long featureAnlalysedCounter = 0;
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				// if this is a (jar) file.
				String featureJarName = listOfFiles[i].getName();
				if (featureJarName.toLowerCase().endsWith(".jar")) {
					// this means that this is a feature jar (it is assumed that
					// this would be a feature jar if it is at this location)
					featureAnlalysedCounter++;
					analyseAndRecordAllInformationFromFeatureJar(
							featureFolderPath, featureJarName, outputLocation);
				}

			} else if (listOfFiles[i].isDirectory()) {

				// some of the features may exist as directories instead of jar
				// files, so check the directories.
				featureAnlalysedCounter++;
				analyseAndRecordAllInformationFromFeatureDir(featureFolderPath,
						listOfFiles[i].getName(), outputLocation);
				// System.out.println("Directory " + listOfFiles[i].getName());
			}

		}
		long l2 = System.currentTimeMillis();
		System.out.println(featureAnlalysedCounter
				+ " features have been analyzed");
		System.err.println(featureAnlalysedCounter
				+ " features have been analyzed");
		System.out.println("for  source:" + featureFolderPath + "  time: "
				+ (l2 - l1) / 1000f + " seconds. \n");

		System.err.println("for  source:" + featureFolderPath + "  time: "
				+ (l2 - l1) / 1000f + " seconds. \n");

	}

	public static void analyseAndRecordAllInformationFromFeatureDir(
			String pathPrefix, String featureDirName, String outputLocation)
			throws IOException {
		FeatureInformation featureInfo = new FeatureInformation();

		String dirNameWithPathFull = pathPrefix + featureDirName;
		File folder = new File(dirNameWithPathFull);
		if (null == folder || !folder.isDirectory()) {
			System.out.println("==== ==== nothing here.");
			return;
		}
		File[] listOfFiles = folder.listFiles();

		for (File e : listOfFiles) {

			String name = e.getName();
			if (name.toLowerCase().endsWith("feature.xml")) {
				try {
					System.out
							.println("==== ==== ====  feature.xml :  enclosing  dir or such file name  =    "
									+ folder.getPath() + ">" + name);
					featureInfo = extractFeatureInformation(new FileInputStream(
							e));

				} catch (Exception exception) {
					exception.printStackTrace();
				}
				break;
			}
		}
		writeDataToFile(featureInfo, folder.getName(), outputLocation);
	}

	/**
	 * @throws IOException
	 */
	public static void analyseAndRecordAllInformationFromFeatureJar(
			String pathPrefix, String featureJarName, String outputLocation)
			throws IOException {
		// //////// feature archive/////////////////////////////////
		String jarFileNameWithPathFull = pathPrefix + featureJarName;

		long l1 = System.currentTimeMillis();

		// ZipFile f = new ZipFile(jarFileNameWithPathFull);
		JarFile f = new JarFile(jarFileNameWithPathFull);

		// Actual part of getting the meta data from the
		// current jar file.////

		System.out.println("now starting the  feature dependency  extraction");

		FeatureInformation featureInfo = extractFeatureMetaDataFromFeatureJar(f);

		long l2 = System.currentTimeMillis();

		writeDataToFile(featureInfo, featureJarName, outputLocation);

		System.err.println("time: " + (l2 - l1) / 1000f + " seconds. \n");
	}

	public static FeatureInformation extractFeatureMetaDataFromFeatureJar(
			JarFile jarfileinstance) {
		FeatureInformation featureinfo = new FeatureInformation();

		Enumeration<? extends JarEntry> en = jarfileinstance.entries();

		while (en.hasMoreElements()) {
			JarEntry e = en.nextElement();

			String name = e.getName();
			if (name.toLowerCase().endsWith("feature.xml")) {
				try {
					System.out
							.println("==== ==== ====  feature.xml :  enclosing  jar or such file name  =    "
									+ jarfileinstance.getName() + ">" + name);
					featureinfo = extractFeatureInformation(jarfileinstance
							.getInputStream(e));

				} catch (Exception exception) {
					exception.printStackTrace();
				}
			}
		}
		return featureinfo;

	}

	private static FeatureInformation extractFeatureInformation(
			InputStream inputStream) {

		FeatureInformation featureInfo = new FeatureInformation();
		String TEMPFileName = (PluginAnalyserUtils.getTEMP_DIR_PATH() + "/pa-sks-feature-tmp-")
				.replace("//", "/") + Math.random() + (
		// jarfileinstance.getName()
		// + "_" +
				"feature.xml").replaceAll("/", "_").replace(" ", "_");
		try {

			BufferedReader bufferedTempReader = new BufferedReader(
					new InputStreamReader(inputStream));

			BufferedWriter bufferedTempWriter;

			bufferedTempWriter = new BufferedWriter(
					new FileWriter(TEMPFileName));

			int inread;
			while ((inread = bufferedTempReader.read()) != -1) {
				// System.out.print((char)inread);
				bufferedTempWriter.write(inread);
				// capturing the full xml text of the feature.xml
				featureInfo.appendXml(new StringBuffer("" + (char) inread));
			}
			bufferedTempWriter.close();
			bufferedTempReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			File f = new File(TEMPFileName);
			// System.out.println(featureInfo.getXml());
			// now the file (xml) is ready for analysis.
			Document doc = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().parse(f);
			// System.out.println(doc.toString());
			// System.out.println("Root element :" +
			// doc.getDocumentElement().getNodeName());

			NodeList nList = doc.getElementsByTagName("import");
			for (int p = 0; p < nList.getLength(); p++) {

				Node nNode = nList.item(p);

				// System.out.println("\nCurrent Element :" +
				// nNode.getNodeName());

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;

					String importedFeature = eElement.getAttribute("feature");
					String importedPlugin = eElement.getAttribute("plugin");
					String importedElementVersion = eElement
							.getAttribute("version");
					String importedElementMatch = eElement
							.getAttribute("match");
					// System.out.println(" feature : " + importedFeature);
					// System.out.println(" plugin : " + importedPlugin);
					// System.out.println(" version : " +
					// importedElementVersion);
					// System.out.println(" match : " + importedElementMatch);

					// adding the import element to featureinfo;
					String importElement = (null != importedFeature
							&& !"".equals(importedFeature) ? "feature;"
							+ importedFeature.trim().toLowerCase() + ";"
							: (null != importedPlugin
									&& !"".equals(importedPlugin) ? "plugin;"
									+ importedPlugin.trim().toLowerCase() + ";"
									: ""));
					importElement += (null != importElement
							&& !"".equals(importElement) ? importedElementVersion
							.trim() + ";" + importedElementMatch + ";"
							: "");
					if (null != importElement && !"".equals(importElement))
						featureInfo.addImport(importElement);

				}
			}

			nList = doc.getElementsByTagName("plugin");
			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);

				// System.out.println("\nCurrent Element :" +
				// nNode.getNodeName());

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;

					// System.out.println(" id : "
					// + eElement.getAttribute("id")
					// + (" , version : " + eElement
					// .getAttribute("version")));

					// adding the plugin that this feature is made up of to the
					// featureinformation.
					featureInfo.addPlugin(eElement.getAttribute("id").trim()
							.toLowerCase()
							+ ";" + eElement.getAttribute("version").trim());
				}
			}

			nList = doc.getElementsByTagName("feature");
			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);

				// System.out.println("\nCurrent Element :" +
				// nNode.getNodeName());

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;

					featureInfo.setId(eElement.getAttribute("id").trim());
					featureInfo.setLabel(eElement.getAttribute("label").trim());
					featureInfo.setVersion(eElement.getAttribute("version")
							.trim());
					StringTokenizer versionTokens = new StringTokenizer(
							eElement.getAttribute("version").trim(), ".");
					StringBuffer versionWithoutQualifier = new StringBuffer(
							versionTokens.nextToken());
					for (int x = 0; x < 2 && versionTokens.hasMoreElements(); x++) {
						versionWithoutQualifier.append("."
								+ versionTokens.nextToken().trim());
					}
					featureInfo
							.setVersionWithoutQualifier(versionWithoutQualifier
									.toString());

					featureInfo.setProviderName(eElement.getAttribute(
							"provider-name").trim());

					// System.out.println("  For \"Feature\" id : " +
					// eElement.getAttribute("id")
					// + "\n label : " + eElement.getAttribute("label")
					// + "\n version : "+eElement.getAttribute("version")
					// +
					// "\n providerName : "+eElement.getAttribute("provider-name")
					// );

				}
			}

			nList = doc.getElementsByTagName("description");
			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);

				// System.out.println("\nCurrent Element :" +
				// nNode.getNodeName());

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;

					featureInfo
							.setDescription(eElement.getTextContent().trim());

					// System.out.println(" description : "+eElement.getTextContent());
				}
			}
			nList = doc.getElementsByTagName("update");
			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);

				// System.out.println("\nCurrent Element :" +
				// nNode.getNodeName());

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;

					featureInfo.setUrl(eElement.getAttribute("url").trim());
					featureInfo.setUpdateLabel(eElement.getAttribute("label")
							.trim());

					// System.out.println(" updatelabel : "+eElement.getAttribute("label")
					// + "\n           url : "+ eElement.getAttribute("url")
					// );
				}
			}

			new File(TEMPFileName).delete();

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return featureInfo;

	}

	private static void writeDataToFile(FeatureInformation featureInfo,
			String featureFileName, String outputLocation) throws IOException {

		outputLocation = (outputLocation + "/").trim().replaceAll("//", "/");

		featureFileName = featureFileName.toLowerCase().trim();
		if (featureFileName.endsWith(".jar")
				|| featureFileName.endsWith(".zip"))
			featureFileName = featureFileName.substring(0,
					featureFileName.length() - 4);

		FileWriter fwriter = new FileWriter(outputLocation + "FEATURE-EXTRACT-"
				+ featureFileName.replace('/', '_') + ".txt");
		BufferedWriter writer = new BufferedWriter(fwriter);
		writer.write("Id ========\n");
		writer.write(featureInfo.getId() + "\n");
		System.out.println(featureInfo.getId() + "=========");
		writer.write("--------\n");
		writer.write("Label ========\n");
		writer.write(featureInfo.getLabel() + "\n");
		writer.write("--------\n");
		writer.write("Version ========\n");
		writer.write(featureInfo.getVersion() + "\n");
		writer.write("--------\n");
		writer.write("Version  Without Qualifier  ========\n");
		writer.write(featureInfo.getVersionWithoutQualifier() + "\n");
		writer.write("--------\n");
		writer.write("ProviderName ========\n");
		writer.write(featureInfo.getProviderName() + "\n");
		writer.write("--------\n");
		writer.write("URL ========\n");
		writer.write(featureInfo.getUrl() + "\n");
		writer.write("--------\n");
		writer.write("UpdateLabel ========\n");
		writer.write(featureInfo.getUpdateLabel() + "\n");
		writer.write("--------\n");
		writer.write("Description ========\n");
		writer.write(featureInfo.getDescription() + "\n");
		writer.write("--------\n");

		writer.write("Plugins ========\n");
		for (String s : featureInfo.getPlugins()) {
			writer.write(s + "\n");
		}
		System.out.println(featureInfo.getPlugins().size() + "," + " plugins.");

		writer.write("--------\n");
		writer.write("Imports ========\n");
		for (String s : featureInfo.getImports()) {
			writer.write(s + "\n");
		}
		System.out.println(featureInfo.getImports().size() + "," + " imports.");

		writer.write("--------\n");
		writer.write("Feature.xml ========\n");
		writer.write(featureInfo.getXml().trim() + "\n");
		writer.write("--------\n");

		writer.close();
		fwriter.close();

	}

}
