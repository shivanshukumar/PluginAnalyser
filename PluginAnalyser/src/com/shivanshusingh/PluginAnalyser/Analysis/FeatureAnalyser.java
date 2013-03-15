package com.shivanshusingh.PluginAnalyser.Analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
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

		System.out.println("=======Analysing  Feature Source:"+featureFolderPath);
		File folder = new File(featureFolderPath);
		if(null==folder)
		{
			System.out.println("==== nothing here.");
			return;
		}
		File[] listOfFiles = folder.listFiles();
		long featureFolderFileCounter=0;
		    for (int i = 0; i < listOfFiles.length; i++) {
		      if (listOfFiles[i].isFile()) {
		    	  String featureJarName=listOfFiles[i].getName();
		    	  if(featureJarName.toLowerCase().endsWith(".jar"))
		    	  {
		    		  // this means that this is a feature jar (it is assumed that this would be a feature jar if it is at this location)
		    		  featureFolderFileCounter++;
		    		  analyseAndRecordAllInformationFromFeatureJar(featureFolderPath,featureJarName );
		    	  }

		      }/* else if (listOfFiles[i].isDirectory()) {
		        System.out.println("Directory " + listOfFiles[i].getName());
		      }*/
		    }
		    long l2 = System.currentTimeMillis();
		    System.out.println(featureFolderFileCounter+" feature  jars have been analyzed");
		    
			System.err.println("for  source:"+featureFolderPath+"  time: " + (l2 - l1) / 1000f + " seconds. \n");
		
	}

	/**
	 * @throws IOException
	 */
	public static void analyseAndRecordAllInformationFromFeatureJar(String pathPrefix, String featureJarName) throws IOException {
		// ////////  feature archive/////////////////////////////////
		String jarFileNameWithPathFull =pathPrefix+  featureJarName;
		
		long l1 = System.currentTimeMillis();

		// ZipFile f = new ZipFile(jarFileNameWithPathFull);
		JarFile f = new JarFile(jarFileNameWithPathFull);

		// Actual part of getting the meta data from the
		// current jar file.////
		
		System.out.println("now starting the  feature dependency  extraction");
		
		FeatureInformation featureInfo=	extractFeatureMetaDataFromFeatureJar( f);
		

		long l2 = System.currentTimeMillis();

		writeDataToFile(featureInfo, featureJarName);

		System.err.println("time: " + (l2 - l1) / 1000f + " seconds. \n");
	}
	
	
	public static FeatureInformation extractFeatureMetaDataFromFeatureJar(JarFile jarfileinstance)
	{
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
	
	
	private static FeatureInformation extractFeatureInformation(InputStream inputStream) {

		FeatureInformation featureInfo=new FeatureInformation();
		String TEMPFileName = "feature-analyser-temp-" + Math.random() + (
		// jarfileinstance.getName()
		// + "_" +
				"feature.xml").replaceAll("/", "_").replace(" ", "_");
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					inputStream));
			String ss;

			BufferedReader bufferedTempReader = new BufferedReader(
					new InputStreamReader(inputStream));

			BufferedWriter bufferedTempWriter;

			bufferedTempWriter = new BufferedWriter(
					new FileWriter(TEMPFileName));

			int inread;
			while ((inread = bufferedTempReader.read()) != -1) {
				// System.out.print((char)inread);
				bufferedTempWriter.write(inread);
				//capturing the full xml text of the feature.xml
				featureInfo.appendXml(new StringBuffer(""+(char)inread));
			}
			bufferedTempWriter.close();
			bufferedTempReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			File f = new File(TEMPFileName);
			System.out.println(	featureInfo.getXml()       );
			// now the file (xml) is ready for analysis.
			Document doc = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().parse(f);
			// System.out.println(doc.toString());
			// System.out.println("Root element :" +
			// doc.getDocumentElement().getNodeName());

			/*
			 * NodeList nList = doc.getElementsByTagName("import"); for (int
			 * temp = 0; temp < nList.getLength(); temp++) {
			 * 
			 * Node nNode = nList.item(temp);
			 * 
			 * System.out.println("\nCurrent Element :" + nNode.getNodeName());
			 * 
			 * if (nNode.getNodeType() == Node.ELEMENT_NODE) {
			 * 
			 * Element eElement = (Element) nNode;
			 * 
			 * System.out.println(" feature : " +
			 * eElement.getAttribute("feature"));
			 * System.out.println(" plugin : " +
			 * eElement.getAttribute("plugin"));
			 * System.out.println(" version : " +
			 * eElement.getAttribute("version")); System.out.println(" match : "
			 * + eElement.getAttribute("match")); } }
			 */
			NodeList nList = doc.getElementsByTagName("plugin");
			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);

				System.out.println("\nCurrent Element :" + nNode.getNodeName());

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;

					System.out.println(" id : "
							+ eElement.getAttribute("id")
							+ (" , version : " + eElement
									.getAttribute("version")));
				}
			}

			new File(TEMPFileName).delete();

		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return featureInfo;

	}
	
	
	

	private static void writeDataToFile(FeatureInformation  featureInfo, 		String featureJarFileName) throws IOException {
		/*FileWriter fwriter = new FileWriter("FEATURE-EXTRACT-"
				+ featureJarFileName.replace('/', '_') + ".txt");
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
		
		
		
		///////////// BUNDLE MANIFEST    ///////////////////
		
		
		// this is the set of other plugins that this plugin would depend on.
		// bundleinfo.getRequires() and bundleinfo.getImports() eventually point
		// to bundleinfo.getRequirements() without any differences.
		if(null!=bundleinfo)
		{
			writer.write("Bundle Requirements ========= \n");
			for(Object s:bundleinfo.getRequirements())
				writer.write(  s.toString()  + "\n");
			System.out.println("Bundle Requirements = "
					+ bundleinfo.getRequirements().toString()); // Require-Bundle
			writer.write("---------------------------------------- \n");
			writer.write("Bundle Exports ========= \n");
			for(Object s:bundleinfo.getExports())
				writer.write(  s.toString()  + "\n");
			System.out.println("Bundle Exports = " + bundleinfo.getExports().toString()); // Export-Package
			System.out.println("Name Symbolic = "
					+ bundleinfo.getSymbolicName().toString());
			System.out.println("Version = " + bundleinfo.getVersion().toString());
			System.out.println("Version without qualifier  = "
					+ bundleinfo.getVersion().withoutQualifier().toString());
			System.out.println("Bundle Imports  = "
					+ bundleinfo.getImports().toString());
			System.out.println("Bundle ClassPathEntries  = "
					+ bundleinfo.getClasspathEntries().toString());
			System.out.println("Bundle hashcode  = "
					+ bundleinfo.hashCode()    );
			writer.write("---------------------------------------- \n");
			
		}
		
		////////////////////////////////////////////////////////
		
			

		writer.write("=================All My Classes (Types)  ==================================\n");

		for (String s : allMyClasses) {
			writer.write(s + "\n");
		}
		writer.write("---------------------------------------- \n");
		writer.write(allMyClasses.size() + "," + " own classes (types).\n");
		System.out.println(allMyClasses.size() + "," + " own classes (types).");

		writer.write("=================All My Public Classes (Types) ==================================\n");

		for (String s : allMyPublicClasses) {
			writer.write(s + "\n");
		}
		writer.write("---------------------------------------- \n");
		writer.write(allMyPublicClasses.size() + ","
				+ " own public classes (types).\n");
		System.out.println(allMyPublicClasses.size() + ","
				+ " own public classes (types).");

		writer.write("=================All My Methods ==================================\n");

		for (String s : allMyMethods) {
			writer.write(s + "\n");
		}
		writer.write("---------------------------------------- \n");
		writer.write(allMyMethods.size() + "," + " internal methods.\n");
		System.out.println(allMyMethods.size() + "," + " internal methods.");

		writer.write("=================All My Public Methods ==================================\n");

		for (String s : allMyPublicMethods) {
			writer.write(s + "\n");
		}
		writer.write("---------------------------------------- \n");
		writer.write(allMyPublicMethods.size() + ","
				+ " internal public methods.\n");
		System.out.println(allMyPublicMethods.size() + ","
				+ " internal public methods.");

		writer.write("=================All   Invokations ==================================\n");

		for (String s : allInvokations) {
			writer.write(s + "\n");
		}
		writer.write("---------------------------------------- \n");
		writer.write(allInvokations.size() + ","
				+ " method invokations (intrnal and external).\n");
		System.out.println(allInvokations.size() + ","
				+ " method invokations (intrnal and external).");

		writer.write("=================All  External  Invokations ==================================\n");

		for (String s : externalInvokations) {
			writer.write(s + "\n");
		}
		writer.write("---------------------------------------- \n");
		writer.write(externalInvokations.size() + ","
				+ " method invokations (external).\n");
		System.out.println(externalInvokations.size() + ","
				+ " method invokations (external).");

		writer.write("=================All  External and non Excluded  Invokations ==================================\n");

		for (String s : externalNonJavaInvokations) {
			writer.write(s + "\n");
		}
		writer.write("---------------------------------------- \n");
		writer.write(externalNonJavaInvokations.size() + ","
				+ " method invokations (external and non excluded).\n");
		System.out.println(externalNonJavaInvokations.size() + ","
				+ " method invokations (external and non excluded).");

		writer.write("=================All Detected Types==================================\n");
		for (String s : allDetectedTypes) {
			writer.write(s + "\n");
		}
		writer.write("---------------------------------------- \n");
		writer.write(allDetectedTypes.size() + ","
				+ " types (internal and external).\n");
		System.out.println(allDetectedTypes.size() + ","
				+ " types (internal and external).");

		writer.write("=================All External Detected Types==================================\n");
		for (String s : allExternalDetectedTypes) {
			writer.write(s + "\n");
		}
		writer.write("---------------------------------------- \n");
		writer.write(allExternalDetectedTypes.size() + ","
				+ " types (external).\n");
		System.out.println(allExternalDetectedTypes.size() + ","
				+ " types (external).");

		writer.write("=================All External Non Java Detected Types==================================\n");
		for (String s : allExternalNonJavaDetectedTypes) {
			writer.write(s + "\n");
		}
		writer.write("---------------------------------------- \n");
		writer.write(allExternalNonJavaDetectedTypes.size() + ","
				+ " types (external non excluded).\n");
		System.out.println(allExternalNonJavaDetectedTypes.size() + ","
				+ " types (external Non excluded).");

		writer.write("=================All Jar Packages ==================================\n");

		for (String s : jarPackages) {
			writer.write(s + "\n");
		}
		writer.write("---------------------------------------- \n");
		writer.write(jarPackages.size() + "," + " jar packages.\n");

		writer.write("=================All  Class packages ==================================\n");

		for (String s : classPackages) {
			writer.write(s + "\n");
		}
		writer.write("---------------------------------------- \n");
		writer.write(classPackages.size() + "," + " class packages.\n");
		writer.write("=================All My Deprecated Methods ==================================\n");

		for (String s : allMyDeprecatedMethods) {
			writer.write(s + "\n");
		}
		writer.write("---------------------------------------- \n");
		writer.write(allMyDeprecatedMethods.size() + ","
				+ " deprecated methods.\n");
		System.out.println(allMyDeprecatedMethods.size() + ","
				+ " deprecated methods.");

		writer.write("=================All My Deprecated Classes ==================================\n");

		for (String s : allMyDeprecatedClasses) {
			writer.write(s + "\n");
		}
		writer.write("---------------------------------------- \n");
		writer.write(allMyDeprecatedClasses.size() + ","
				+ " deprecated   classes. \n");
		System.out.println(allMyDeprecatedClasses.size() + ","
				+ " deprecated   classes.");

	if(null!=bundleinfo){
		writer.write("=================Bundle Plugin.xml ==================================\n");
		writer.write(bundleinfo.getPluginXml() + "\n");
		writer.write("---------------------------------------- \n");
	}
		writer.write("===================================================\n");
		writer.close();
		fwriter.close();
	*/
	}


}
