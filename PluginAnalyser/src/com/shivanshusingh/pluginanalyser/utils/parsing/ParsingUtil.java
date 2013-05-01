package com.shivanshusingh.pluginanalyser.utils.parsing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * 
 * has the utility and helper functions that help in parsing extract and other files in the system.
 * @author Shivanshu Singh
 *
 */
public class ParsingUtil {

	/**
	 * @param extractFile
	 * @param property
	 * @return {@link Set}
	 * @throws IOException
	 */
	public static Set<String> restorePropertyFromExtract(File extractFile, String property) throws IOException {
	
		property = property.trim();
		Set<String> result = new HashSet<String>();
		BufferedReader br = new BufferedReader(new FileReader(extractFile));
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
		br.close();
		return result;
	}

	/**
	 * get the bundle property name from a bundle  dependency  property entry by stripping any version or other information e.g. (optional).
	 * If the filterOptionals flag is set to  false, the entry stripped of version but including the "(optional)" keyword is returned.
	 * 
	 * @param bundleDependencyEntry
	 * @param removeVersionInfo true if any existing version information (version or version range) should be stripped from the result being returned.
	 * @param removeOptionalIndicator true if any existing "(optional)" keyword should be removed from the name being returned. it is included if this is set to false.
	 * @return
	 */
	public static String getNameFromBundleDependencyEntry(String bundleDependencyEntry, boolean removeVersionInfo, boolean removeOptionalIndicator) {
		// getting the class / package name from the
		// bundle export entry.
		// getting split on ";" first.
		String newEntry="";
		if(removeVersionInfo)
			newEntry = bundleDependencyEntry.split(";")[0].trim();
		newEntry = newEntry.replace(Constants.BUNDLE_DEPDENDENCY_KEYWORD_OPTIONAL,"").trim();
		if(!removeOptionalIndicator&& bundleDependencyEntry.toLowerCase().contains(Constants.BUNDLE_DEPDENDENCY_KEYWORD_OPTIONAL))
			newEntry+=" "+Constants.BUNDLE_DEPDENDENCY_KEYWORD_OPTIONAL;
		return newEntry;
	}
	
	/**
	 * get the bundle property version string from a bundle dependency property entry by stripping any version or other information e.g. (optional).
	 * If the filterOptionals flag is set to  false, the entry stripped of version but including the "(optional)" keyword is returned.
	 * 
	 * @param bundleDependencyEntry
	 * @return
	 */
	public static String getVersionStrFromBundleDependencyEntry(String bundleDependencyEntry) {
		// getting the version from the
		// bundle export entry.
		// getting split on ";" first.
		String newEntry="";
		String  [] splits=bundleDependencyEntry.split(";");
		if(null!=splits && 2<=splits.length)
		{
			
			//this means that there is some version information present.
			newEntry =    splits [1].trim().replace(Constants.BUNDLE_DEPDENDENCY_KEYWORD_OPTIONAL,"");
		}
		return newEntry;
	}
	
	/**
	 * return if the bundle Dependency Entry is marked as optional, false otherwise. 
	 * @param bundleDependencyEntry
	 * @return
	 */
	public static boolean isOptional_BundleDependencyEntry(String bundleDependencyEntry) {
		// getting the version from the
		// bundle export entry.
		// getting split on ";" first.
		return bundleDependencyEntry.toLowerCase().contains(Constants.BUNDLE_DEPDENDENCY_KEYWORD_OPTIONAL);
	}


	/**
	 * return 1 is the input is a function entry , 2 in case of a type entry and
	 * 0 otherwise.
	 * 
	 * @param imp
	 * @return
	 */
	public static int getEntryType(String str) {
		//System.out.println("splitting:"+str);
		if (null != str && 0 < str.trim().length()) {
			try {
				str = str.trim();
				//String[] splits = str.split(" ");
				StringTokenizer splits=new StringTokenizer(str," ");
				if (null != splits  &&  0!=splits.countTokens())
				{
					if (splits.countTokens() >= 3)
					{
						
						return 1;
					} else {
						
						return 2;
					}
				}
				
			} catch (StackOverflowError e) {
				System.err.println("finding the entry type for:" + str.toString());
				throw e;

			}

		} 
			return 0;
	}

	/**
	 * 
	 * gets the plugin ID from the plugin extract
	 * e.g. if the bundle SymbolicName = A and the Bundle version = b.c.d.qualifier then the result returned will be: A[b.c.d.qualifier]
	 * 
	 * @param pluginExtract
	 * @return
	 */
	public static String getPluginIdFromExtract(File pluginExtract) {
		StringBuffer pluginId=new StringBuffer();
		try {
			Set<String> pluginSymbolicNameSet=restorePropertyFromExtract(pluginExtract,  Constants.BUNDLE_SYMBOLICNAME  );
			Set<String> pluginVersionSet=restorePropertyFromExtract(pluginExtract,  Constants.BUNDLE_VERSION  );
			for(String s: pluginSymbolicNameSet)
				pluginId.append(s.trim());
			pluginId.append(Constants.DELIM_VERSION_STRING_OPEN);
			for(String s: pluginVersionSet)
				pluginId.append(s.trim());
			pluginId.append(Constants.DELIM_VERSION_STRING_CLOSE);
		} catch (IOException e) {
			pluginId=new StringBuffer();
			e.printStackTrace();
		}
		return pluginId.toString();
	}
	
	/**
	 * 
	 * gets the feature ID from the feature extract
	 * 
	 * @param featureExtract
	 * @return
	 */
	public static String getFeatureIdFromExtract(File featureExtract) {
		StringBuffer featureId=new StringBuffer();
		try {
			Set<String> featureIdOrName=restorePropertyFromExtract(featureExtract,  Constants.FEATURE_ID  );
			Set<String> pluginVersionSet=restorePropertyFromExtract(featureExtract,  Constants.FEATURE_VERSION  );
			for(String s: featureIdOrName)
				featureId.append(s.trim());
			featureId.append(Constants.DELIM_VERSION_STRING_OPEN);
			for(String s: pluginVersionSet)
				featureId.append(s.trim());
			featureId.append(Constants.DELIM_VERSION_STRING_CLOSE);
		} catch (IOException e) {
			featureId=new StringBuffer();
			e.printStackTrace();
		}
		return featureId.toString();
	}

	/**
	 * build a ID containing name and version info (for use in plugin and package maps and later for dependency feature model and constraints). E.g. if name = A.B.C and versionStr= 1.2.0.whatever and the opening and closing delimiters for versionStr are <% and %> respectively, the result would be A.B.C<%1.2.0.whatever%> 
	 * @param name
	 * @param versionStr
	 * @return
	 */
	public static String buildIdentifier(String name, String versionStr) {
		String pluginId=name+Constants.DELIM_VERSION_STRING_OPEN+versionStr+Constants.DELIM_VERSION_STRING_CLOSE;
		return pluginId;
	}

}
