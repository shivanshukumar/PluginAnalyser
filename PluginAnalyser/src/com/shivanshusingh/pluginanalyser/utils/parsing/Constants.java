package com.shivanshusingh.pluginanalyser.utils.parsing;

/**
 * 
 * extracts related constants used for recording extracted information and also for parsing during restoration from extracts.
 * @author Shivanshu Singh
 * 
 */
public interface Constants {

	public static final String DEPENDENCY_SET_FILE_EXTENSION_PLUGIN 					= ".dpst";
	public static final String DEPENDENCY_SET_FILE_PREFIX_PLUGIN 						= "DEPENDENCY-SET-PLUGIN-";

	public static final String EXTRACT_FILE_EXTENSION_FEATURE							= ".extr";
	public static final String EXTRACT_FILE_EXTENSION_PLUGIN 							= ".extr";
	public static final String EXTRACT_FILE_PREFIX_FEATURE 								= "EXTRACT-FEATURE-";
	public static final String EXTRACT_FILE_PREFIX_PLUGIN 								= "EXTRACT-PLUGIN-";

	public static final String JAR_FILE_EXTENSION 										= ".jar";

	public static final String MARKER_ADDON 											= "==";
	public static final String MARKER_INTERIM 											= "====";
	public static final String MARKER_TERMINATOR 										= "----";

	public static final String PLUGIN_ALL_CLASS_PACKAGES 								= MARKER_ADDON + "All Class packages" 					+ MARKER_ADDON;
	public static final String PLUGIN_ALL_INTERFACES_IMPLEMENTED_BY_ME 					 = MARKER_ADDON + "Interfaces Implemented "+ MARKER_ADDON;
	public static final String PLUGIN_ALL_JAR_PACKAGES 									= MARKER_ADDON + "All Jar Packages" 						+ MARKER_ADDON;
	public static final String PLUGIN_ALL_MY_METHOD_CALLS 								= MARKER_ADDON + "All Invokations" 						+ MARKER_ADDON;
	public static final String PLUGIN_ALL_MY_METHOD_CALLS_EXTERNAL 						= MARKER_ADDON + "All External Invokations" 				+ MARKER_ADDON;
	public static final String PLUGIN_ALL_MY_METHOD_CALLS_EXTERNAL_AND_NON_JAVA 		= MARKER_ADDON + "All External and non Java Invokations" 	+ MARKER_ADDON;
	public static final String PLUGIN_ALL_MY_METHODS 									= MARKER_ADDON + "All My Methods" 						+ MARKER_ADDON;
	public static final String PLUGIN_ALL_MY_METHODS_DEPRECATED 						= MARKER_ADDON + "All My Deprecated Methods" 				+ MARKER_ADDON;
	public static final String PLUGIN_ALL_MY_METHODS_PUBLIC 							= MARKER_ADDON + "All My Public Methods" 					+ MARKER_ADDON;
	public static final String PLUGIN_ALL_MY_TYPES 										= MARKER_ADDON + "All My Classes (Types)" 				+ MARKER_ADDON;
	public static final String PLUGIN_ALL_MY_TYPES_DEPRECATED 							= MARKER_ADDON + "All My Deprecated Classes" 				+ MARKER_ADDON;
	public static final String PLUGIN_ALL_MY_TYPES_PUBLIC 								= MARKER_ADDON + "All My Public Classes (Types)" 			+ MARKER_ADDON;
	public static final String PLUGIN_ALL_SUPERCLASS_EXTENDED_BY_ME 					= MARKER_ADDON + "Super Class(es)  Extended"+ MARKER_ADDON;
	public static final String PLUGIN_ALL_TYPES_DETECTED 								= MARKER_ADDON + "All Detected Types" 					+ MARKER_ADDON;

	public static final String PLUGIN_ALL_TYPES_DETECTED_EXTERNAL 						= MARKER_ADDON + "All External Detected Types" 			+ MARKER_ADDON;
	public static final String PLUGIN_ALL_TYPES_DETECTED_EXTERNAL_AND_NON_JAVA 			= MARKER_ADDON + "All External Non Java Detected Types" 	+ MARKER_ADDON;
	public static final String PLUGIN_DEPENDENCY_ALL_FUNCTIONS  						= MARKER_ADDON + "All functions " 			+ MARKER_ADDON;
	public static final String PLUGIN_DEPENDENCY_ALL_TYPES  							= MARKER_ADDON + "All types (classes) " 		+ MARKER_ADDON;
	public static final String PLUGIN_DEPENDENCY_ELEMENT_SET_DELIM  					= "####";
	public static final String PLUGIN_DEPENDENCY_EXPORTERS  							= MARKER_ADDON + "Exporters " 				+ MARKER_ADDON;
	public static final String PLUGIN_DEPENDENCY_EXPORTERS_UNSATISFIED  				= MARKER_ADDON + "Generous EXPORTERS  UnSatisfied " 	+ MARKER_ADDON;
	public static final String PLUGIN_DEPENDENCY_IMPORTERS  							= MARKER_ADDON + "Importers " 				+ MARKER_ADDON;
	public static final String PLUGIN_DEPENDENCY_IMPORTERS_UNSATISFIED  				= MARKER_ADDON + "Poor Importers  UnSatisfied " 	+ MARKER_ADDON;
	public static final String PLUGIN_ELEMENT_SUPERCLASS_INTERFACE_DELIM 				= " => ";

	
	//TODO Add support for features.
}
