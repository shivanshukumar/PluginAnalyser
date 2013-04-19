package com.shivanshusingh.pluginanalyser.analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.shivanshusingh.pluginanalyser.utils.Util;
import com.shivanshusingh.pluginanalyser.utils.logging.Log;
import com.shivanshusingh.pluginanalyser.utils.parsing.Constants;

/**
 * this is the class that analyses features (dir or jar).
 * 
 * @author Shivanshu Singh
 * 
 */
public class FeatureAnalyser {

	// Map for: Feature Name (featureidOrName)  ->   featureId(featureIdOrName<%version.qualifier%>)   ->   feature extract file names
		// set..
		private static Map<String, Map<String, Set<String>>> featureMap = new HashMap<String, Map<String, Set<String>>>();
		private static long UNKNOWN_FeatureId_Counter = 0;

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
	 * @param eraseOld
	 */

	public static void analyseAndRecordAllInformationFromBaseFeautreFolder(String featureFolderPath, String outputLocation,
			boolean eraseOld) throws IOException {
		if (!Util.checkDirectory(new File(featureFolderPath), true, true, true, false)) {
			Log.errln("xxxx Error Accessing Feature source base Directory for Feature Analysis : " + featureFolderPath
					+ "\n Cannot continue with the fetaure analysis.");
			return;
		}
		if (!Util.checkAndCreateDirectory(outputLocation)) {
			Log.errln("Error Accessing/Creating Output Directory for  Feature  Analysis Output at: " + outputLocation
					+ "\n Cannot continue with the feature analysis.");
			return;
		}

		if (eraseOld)
			Util.clearFolder(new File(outputLocation));

		// reading all the files (feature jars) in the specified feature folder
		long l1 = System.currentTimeMillis();

		Log.outln("=======Analysing  Feature Source:" + featureFolderPath);
		File folder = new File(featureFolderPath);
		/*if (null == folder) {
			Log.outln("==== nothing here.");
			return;
		}*/
		File[] listOfFiles = folder.listFiles();
		long featureAnlalysedCounter = 0;
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				// if this is a (jar) file.
				String featureJarName = listOfFiles[i].getName();
				if (featureJarName.toLowerCase().endsWith(Constants.JAR_FILE_EXTENSION)) {
					// this means that this is a feature jar (it is assumed that
					// this would be a feature jar if it is at this location)
					featureAnlalysedCounter++;
					analyseAndRecordAllInformationFromFeatureJar(featureFolderPath, featureJarName, outputLocation);
				}

			} else if (listOfFiles[i].isDirectory()) {

				// some of the features may exist as directories instead of jar
				// files, so check the directories.
				featureAnlalysedCounter++;
				analyseAndRecordAllInformationFromFeatureDir(featureFolderPath, listOfFiles[i].getName(), outputLocation);
				// Log.outln("Directory " + listOfFiles[i].getName());
			}

		}
		//writing the plugin map to disk.
		Util.writeObjectToDisk(featureMap,Constants.EXTRACT_FILE_PREFIX_FEATUREMAP
				+Constants.EXTRACT_FILE_NAME_FEATUREMAP + Constants.EXTRACT_FILE_EXTENSION_FEATUREMAP, outputLocation);
			
		long l2 = System.currentTimeMillis();
		Log.outln(featureAnlalysedCounter + " features have been analyzed");
		Log.errln(featureAnlalysedCounter + " features have been analyzed");
		Log.outln("for  source:" + featureFolderPath + "  time: " + Util.getFormattedTime(l2 - l1));

		Log.errln("for  source:" + featureFolderPath + "  time: " + Util.getFormattedTime(l2 - l1));

	}

	public static void analyseAndRecordAllInformationFromFeatureDir(String pathPrefix, String featureDirName,
			String outputLocation) throws IOException {
		long l1 = System.currentTimeMillis();

		FeatureInformation featureInfo = new FeatureInformation();

		String dirNameWithPathFull = pathPrefix + featureDirName;

		File folder = new File(dirNameWithPathFull);
		if (null == folder || !folder.isDirectory()) {
			Log.outln("==== ==== nothing here.");
			return;
		}
		File[] listOfFiles = folder.listFiles();

		for (File e : listOfFiles) {

			String name = e.getName();
			if (name.toLowerCase().endsWith("feature.xml")) {
				try {
					Log.outln("==== ==== ====  feature.xml :  enclosing  dir or such file name  =    " + folder.getPath()
							+ ">" + name);
					featureInfo = extractFeatureInformation(new FileInputStream(e));

				} catch (Exception exception) {
					exception.printStackTrace();
				}
				break;
			}
		}
		writeData(featureInfo, folder.getName(), outputLocation);
		long l2 = System.currentTimeMillis();

		Log.errln("==== analysed:  \n " + dirNameWithPathFull + "\n time: " + Util.getFormattedTime(l2 - l1));

	}

	/**
	 * @param pathPrefix
	 * @param featureJarName
	 * @param outputLocation
	 * @throws IOException
	 */
	public static void analyseAndRecordAllInformationFromFeatureJar(String pathPrefix, String featureJarName,
			String outputLocation) throws IOException {
		long l1 = System.currentTimeMillis();

		// //////// feature archive/////////////////////////////////
		String jarFileNameWithPathFull = pathPrefix + featureJarName;

		// ZipFile f = new ZipFile(jarFileNameWithPathFull);
		JarFile f = new JarFile(jarFileNameWithPathFull);

		// Actual part of getting the meta data from the
		// current jar file.////

		Log.outln("now starting the  feature dependency  extraction");

		FeatureInformation featureInfo = extractFeatureMetaDataFromFeatureJar(f);

		writeData(featureInfo, featureJarName, outputLocation);
		long l2 = System.currentTimeMillis();

		Log.errln("==== analysed:  \n " + jarFileNameWithPathFull + "\n time: " + Util.getFormattedTime(l2 - l1));
	}

	/**
	 * @param jarfileinstance
	 * @return
	 */
	public static FeatureInformation extractFeatureMetaDataFromFeatureJar(JarFile jarfileinstance) {
		FeatureInformation featureinfo = new FeatureInformation();

		Enumeration<? extends JarEntry> en = jarfileinstance.entries();

		while (en.hasMoreElements()) {
			JarEntry e = en.nextElement();

			String name = e.getName();
			if (name.toLowerCase().endsWith("feature.xml")) {
				try {
					Log.outln("==== ==== ====  feature.xml :  enclosing  jar or such file name  =    "
							+ jarfileinstance.getName() + ">" + name);
					featureinfo = extractFeatureInformation(jarfileinstance.getInputStream(e));

				} catch (Exception exception) {
					exception.printStackTrace();
				}
			}
		}
		return featureinfo;

	}

	/**
	 * extracts the feature information for the feature.xml or such inputStream
	 * 
	 * @param inputStream
	 *            {@link InputStream} for the feature.xml file or such source
	 *            that contains the feature information / metadata.
	 * @return {@link FeatureInformation] extracted .
	 */
	private static FeatureInformation extractFeatureInformation(InputStream inputStream) {

		FeatureInformation featureInfo = new FeatureInformation();
		String TEMPFileName = (Util.getTEMP_DIR_PATH() + "/pa-sks-feature-tmp-").replace("//", "/") + Math.random() + (
		// jarfileinstance.getName()
		// + "_" +
				"feature.xml").replaceAll("/", "_").replace(" ", "_");
		try {

			BufferedReader bufferedTempReader = new BufferedReader(new InputStreamReader(inputStream));

			BufferedWriter bufferedTempWriter;

			bufferedTempWriter = new BufferedWriter(new FileWriter(TEMPFileName));

			int inread;
			while ((inread = bufferedTempReader.read()) != -1) {
				// Log.out((char)inread);
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
			// Log.outln(featureInfo.getXml());
			// now the file (xml) is ready for analysis.
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(f);
			// Log.outln(doc.toString());
			// Log.outln("Root element :" +
			// doc.getDocumentElement().getNodeName());

			NodeList nList = doc.getElementsByTagName("import");
			for (int p = 0; p < nList.getLength(); p++) {

				Node nNode = nList.item(p);

				// Log.outln("\nCurrent Element :" +
				// nNode.getNodeName());

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;

					String importedFeature = eElement.getAttribute("feature");
					String importedPlugin = eElement.getAttribute("plugin");
					String importedElementVersion = eElement.getAttribute("version");
					String importedElementMatch = eElement.getAttribute("match");
					String importedElementPatch = eElement.getAttribute("patch");
					// Log.outln(" feature : " + importedFeature);
					// Log.outln(" plugin : " + importedPlugin);
					// Log.outln(" version : " + importedElementVersion+" match : " + importedElementMatch);

					// adding the import element to featureinfo;
					String importElement = (null != importedFeature && !"".equals(importedFeature) ? "feature;"
							+ importedFeature.trim() + ";" : (null != importedPlugin
							&& !"".equals(importedPlugin) ? "plugin;" + importedPlugin.trim() + ";" : ""));
					importElement += (null != importElement && !"".equals(importElement) ? importedElementVersion.trim()
							+ ";" + importedElementMatch + ";"+ importedElementPatch  : "");
					if (null != importElement && !"".equals(importElement))
						featureInfo.addImport(importElement);

				}
			}

			nList = doc.getElementsByTagName("plugin");
			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);

				// Log.outln("\nCurrent Element :" +
				// nNode.getNodeName());

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;

					// Log.outln(" id : "
					// + eElement.getAttribute("id")
					// + (" , version : " + eElement
					// .getAttribute("version")));

					// adding the plugin that this feature is made up of to the
					// featureinformation.
					featureInfo.addPlugin(
							eElement.getAttribute("id").trim() 
							+ ";" 	+ eElement.getAttribute("version").trim()
							+ ";" 	+ eElement.getAttribute("fragment").trim()
							+ ";" 	+ eElement.getAttribute("os").trim()
							+ ";" 	+ eElement.getAttribute("arch").trim()
							+ ";" 	+ eElement.getAttribute("ws").trim()

							);
				}
			}

			nList = doc.getElementsByTagName("feature");
			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);

				// Log.outln("\nCurrent Element :" +
				// nNode.getNodeName());

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;

					featureInfo.setId(eElement.getAttribute("id").trim());
					featureInfo.setLabel(eElement.getAttribute("label").trim());
					featureInfo.setVersion(eElement.getAttribute("version").trim());
					featureInfo.setProviderName(eElement.getAttribute("provider-name").trim());
					featureInfo.setOS(eElement.getAttribute("os").trim());
					featureInfo.setARCH(eElement.getAttribute("arch").trim());
					featureInfo.setWS(eElement.getAttribute("ws").trim());

					// Log.outln("  For \"Feature\" id : " +
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

				// Log.outln("\nCurrent Element :" +
				// nNode.getNodeName());

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;

					featureInfo.setDescription(eElement.getTextContent().trim());

					// Log.outln(" description : "+eElement.getTextContent());
				}
			}
			nList = doc.getElementsByTagName("update");
			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);

				// Log.outln("\nCurrent Element :" +
				// nNode.getNodeName());

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;

					featureInfo.setUrl(eElement.getAttribute("url").trim());
					featureInfo.setUpdateLabel(eElement.getAttribute("label").trim());

					// Log.outln(" updatelabel : "+eElement.getAttribute("label")
					// + "\n           url : "+ eElement.getAttribute("url")
					// );
				}
			}

			Log.outln("== delete = " + new File(TEMPFileName).delete() + " : " + TEMPFileName + "====");

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return featureInfo;

	}

	/**
	 * @param featureInfo
	 * @param featureFileName
	 * @param outputLocation
	 * @throws IOException
	 */
	private static void writeData(FeatureInformation featureInfo, String featureFileName, String outputLocation)
			throws IOException {
		// TODO switch extract generation to largely or completely Constants
		// based approach.
		outputLocation = (outputLocation + "/").trim().replaceAll("//", "/");

		featureFileName = featureFileName.trim();
		if (featureFileName.endsWith(Constants.JAR_FILE_EXTENSION))
			featureFileName = featureFileName
					.substring(0, featureFileName.length() - Constants.JAR_FILE_EXTENSION.length());

		FileWriter fwriter = new FileWriter(outputLocation + Constants.EXTRACT_FILE_PREFIX_FEATURE
				+ featureFileName.replace('/', '_') + Constants.EXTRACT_FILE_EXTENSION_FEATURE);
		BufferedWriter writer = new BufferedWriter(fwriter);
		writer.write(Constants.FEATURE_ID+"\n");
		String featureId=Constants.PROPERTY_VALUE_UNKNOWN_LITERAL+"_"+UNKNOWN_FeatureId_Counter++;
		featureId=((null != featureInfo.getId() && !"".equalsIgnoreCase(featureInfo.getId().trim() ) ) ? featureInfo.getId().trim() : featureId);
		writer.write(featureInfo.getId() + "\n");
		writer.write(Constants.MARKER_TERMINATOR + "\n");
		writer.write(Constants.FEATURE_LABEL+"\n");
		writer.write(featureInfo.getLabel() + "\n");
		writer.write(Constants.MARKER_TERMINATOR + "\n");
		writer.write(Constants.FEATURE_VERSION+"\n");
		writer.write(featureInfo.getVersion().toString() + "\n");
		writer.write(Constants.MARKER_TERMINATOR + "\n");
		
		addToFeatureMap(featureFileName, featureId, featureInfo.getVersion().toString());
		
		writer.write(Constants.FEATURE_OS+"\n");
		writer.write(featureInfo.getOS().toString() + "\n");
		writer.write(Constants.MARKER_TERMINATOR + "\n");
		writer.write(Constants.FEATURE_ARCH+"\n");
		writer.write(featureInfo.getARCH().toString() + "\n");
		writer.write(Constants.MARKER_TERMINATOR + "\n");
		writer.write(Constants.FEATURE_WS+"\n");
		writer.write(featureInfo.getWS().toString() + "\n");
		writer.write(Constants.MARKER_TERMINATOR + "\n");
		writer.write(Constants.FEATURE_VERSION_WITHOUT_QUALIFIER+"\n");
		writer.write(featureInfo.getVersion().withoutQualifier() + "\n");
		writer.write(Constants.MARKER_TERMINATOR + "\n");
		writer.write(Constants.FEATURE_PROVIDER_NAME+"\n");
		writer.write(featureInfo.getProviderName() + "\n");
		writer.write(Constants.MARKER_TERMINATOR + "\n");
		writer.write(Constants.FEATURE_URL+"\n");
		writer.write(featureInfo.getUrl() + "\n");
		writer.write(Constants.MARKER_TERMINATOR + "\n");
		writer.write(Constants.FEATURE_UPDATE_LABEL+"\n");
		writer.write(featureInfo.getUpdateLabel() + "\n");
		writer.write(Constants.MARKER_TERMINATOR + "\n");
		writer.write(Constants.FEATURE_DESCRIPTION+"\n");
		writer.write(featureInfo.getDescription() + "\n");
		writer.write(Constants.MARKER_TERMINATOR + "\n");

		writer.write(Constants.FEATURE_PROVIDED_PLUGINS+"\n");
		for (String s : featureInfo.getPlugins()) {
			writer.write(s + "\n");
		}
		// Log.outln(featureInfo.getPlugins().size() + "," + " plugins.");

		writer.write(Constants.MARKER_TERMINATOR + "\n");
		writer.write(Constants.FEATURE_IMPORTS+"\n");
		for (String s : featureInfo.getImports()) {
			writer.write(s + "\n");
		}
		// Log.outln(featureInfo.getImports().size() + "," + " imports.");

		writer.write(Constants.MARKER_TERMINATOR + "\n");
		writer.write(Constants.FEATURE_FEATURE_XML+"\n");
		writer.write(featureInfo.getXml().trim() + "\n");
		writer.write(Constants.MARKER_TERMINATOR + "\n");

		writer.close();
		fwriter.close();

	}
	/**
	 * @param featureFileName
	 * @param featureIdOrName
	 * @param featureVersionWithQualifier
	 */
	private static void addToFeatureMap(String featureFileName, String featureIdOrName, String featureVersionWithQualifier) {
		// adding the  feature ID and extract file name to the feature map.
		Map<String,Set<String>> interimFeatureIdMap=new HashMap<String, Set<String>>();
		if(featureMap.containsKey(featureIdOrName))
			interimFeatureIdMap=featureMap.get(featureIdOrName);
		String featureId=featureIdOrName+Constants.DELIM_VERSION_STRING_OPEN+featureVersionWithQualifier+Constants.DELIM_VERSION_STRING_CLOSE;
		Set<String> interimFeatureFileSet=new HashSet<String>();
		if(interimFeatureIdMap.containsKey(featureId))
			interimFeatureFileSet=interimFeatureIdMap.get(featureId);
		interimFeatureFileSet.add(featureFileName);
		//adding back to the Map and outer map.
		interimFeatureIdMap.put(featureId, interimFeatureFileSet);
		featureMap.put(featureIdOrName, interimFeatureIdMap);
	}
}
