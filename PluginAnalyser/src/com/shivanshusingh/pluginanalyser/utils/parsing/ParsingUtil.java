package com.shivanshusingh.pluginanalyser.utils.parsing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * has the utility and helper functions that help in parsing extract and other files in the system.
 * @author Shivanshu Singh
 *
 */
public class ParsingUtil {

	/**
	 * @param entry
	 * @param property
	 * @return {@link Set}
	 * @throws IOException
	 */
	public static Set<String> restorePropertyFromExtract(File entry, String property) throws IOException {
	
		property = property.trim();
		Set<String> result = new HashSet<String>();
		BufferedReader br = new BufferedReader(new FileReader(entry));
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
		return result;
	}

}
