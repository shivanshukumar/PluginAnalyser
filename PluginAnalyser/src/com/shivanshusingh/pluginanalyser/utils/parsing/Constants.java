/**
 * 
 */
package com.shivanshusingh.pluginanalyser.utils.parsing;

/**
 * @author singhsk
 *
 */
public interface Constants {
	
	public  static final String INTERIM_MARKER="========";
	public  static final String TERMINATOR_MARKER="########";
	public  static final String PLUGIN_ALL_MY_TYPES=INTERIM_MARKER+"All My Classes (Types)"+INTERIM_MARKER;
	public  static final String PLUGIN_ALL_MY_TYPES_PUBLIC=INTERIM_MARKER+"All My Public Classes (Types)"+INTERIM_MARKER;
	public  static final String PLUGIN_ALL_MY_METHODS=INTERIM_MARKER+"All My Methods"+INTERIM_MARKER;
	public  static final String PLUGIN_ALL_MY_METHODS_PUBLIC=INTERIM_MARKER+"All My Public Methods"+INTERIM_MARKER;
	public  static final String PLUGIN_ALL_MY_METHOD_CALLS=INTERIM_MARKER+"All Invokations"+INTERIM_MARKER;
	public  static final String PLUGIN_ALL_MY_METHOD_CALLS_EXTERNAL=INTERIM_MARKER+"All External Invokations"+INTERIM_MARKER;
	public  static final String PLUGIN_ALL_MY_METHOD_CALLS_EXTERNAL_AND_NON_JAVA=INTERIM_MARKER+"All External and non Java Invokations"+INTERIM_MARKER;
	public  static final String PLUGIN_ALL_TYPES_DETECTED=INTERIM_MARKER+"All Detected Types"+INTERIM_MARKER;
	public  static final String PLUGIN_ALL_TYPES_DETECTED_EXTERNAL=INTERIM_MARKER+"All External Detected Types"+INTERIM_MARKER;
	public  static final String PLUGIN_ALL_TYPES_DETECTED_EXTERNAL_AND_NON_JAVA=INTERIM_MARKER+"All External Non Java Detected Types"+INTERIM_MARKER;
	public  static final String PLUGIN_ALL_JAR_PACKAGES=INTERIM_MARKER+"All Jar Packages"+INTERIM_MARKER;
	public  static final String PLUGIN_ALL_CLASS_PACKAGES=INTERIM_MARKER+"All Class packages"+INTERIM_MARKER;
	public  static final String PLUGIN_ALL_MY_METHODS_DEPRECATED=INTERIM_MARKER+"All My Deprecated Methods"+INTERIM_MARKER;
	public  static final String PLUGIN_ALL_MY_TYPES_DEPRECATED=INTERIM_MARKER+"All My Deprecated Classes"+INTERIM_MARKER;
	public static final String PLUGIN_EXTRACT_FILE_PREFIX = "PLUGIN-EXTRACT-";
	

}
