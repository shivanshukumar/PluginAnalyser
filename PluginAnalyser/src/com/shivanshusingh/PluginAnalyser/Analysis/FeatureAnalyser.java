package com.shivanshusingh.PluginAnalyser.Analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
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

public class FeatureAnalyser {

	public static void analyseAndRecordAllInformationFromFeautreFolder(
			String featureFolderPath) throws IOException {

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
		long featureFolderFileCounter = 0;
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				String featureJarName = listOfFiles[i].getName();
				if (featureJarName.toLowerCase().endsWith(".jar")) {
					// this means that this is a feature jar (it is assumed that
					// this would be a feature jar if it is at this location)
					featureFolderFileCounter++;
					analyseAndRecordAllInformationFromFeatureJar(
							featureFolderPath, featureJarName);
				}

			}/*
			 * else if (listOfFiles[i].isDirectory()) {
			 * System.out.println("Directory " + listOfFiles[i].getName()); }
			 */
		}
		long l2 = System.currentTimeMillis();
		System.out.println(featureFolderFileCounter
				+ " feature  jars have been analyzed");

		System.err.println("for  source:" + featureFolderPath + "  time: "
				+ (l2 - l1) / 1000f + " seconds. \n");

	}

	/**
	 * @throws IOException
	 */
	public static void analyseAndRecordAllInformationFromFeatureJar(
			String pathPrefix, String featureJarName) throws IOException {
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

		writeDataToFile(featureInfo, featureJarName);

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
		String TEMPFileName = "feature-analyser-temp-" + Math.random() + (
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
					featureInfo.setVersion(eElement.getAttribute("version").trim());
					StringTokenizer versionTokens=new StringTokenizer(eElement.getAttribute("version").trim(),".");
					StringBuffer versionWithoutQualifier = new StringBuffer(versionTokens.nextToken());
					for(int x=0; x<2  && versionTokens.hasMoreElements();x++)
					{
						versionWithoutQualifier.append("."+versionTokens.nextToken().trim());
					}
					featureInfo.setVersionWithoutQualifier( versionWithoutQualifier.toString());
					
					featureInfo.setProviderName(eElement
							.getAttribute("provider-name").trim());

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

					featureInfo.setDescription(eElement.getTextContent().trim());

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
					featureInfo.setUpdateLabel(eElement.getAttribute("label").trim());

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
			String featureJarFileName) throws IOException {

		FileWriter fwriter = new FileWriter("FEATURE-EXTRACT-"
				+ featureJarFileName.replace('/', '_') + ".txt");
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
