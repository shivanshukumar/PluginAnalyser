package com.shivanshusingh.pluginanalyser.utils.parsing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * 
 * extracts related constants used for recording extracted information and also for parsing during restoration from extracts.
 * @author Shivanshu Singh
 * 
 */
public interface Constants {

	public static final String CONFIG_ 													= "CONFIG_";
	public static final String _FE_ 													= "_FE:__";

	public static final String MARKER_ADDON 											= "==";
	public static final String MARKER_INTERIM 											= "====";
	public static final String MARKER_TERMINATOR 										= "----";
	
	public static final String BUNDLE_DEPDENDENCY_KEYWORD_OPTIONAL 						= "(optional)";

	public static final String BUNDLE_CLASSPATHENTRIES 									= MARKER_ADDON +  "Bundle ClassPathEntries"  			  +  MARKER_ADDON ;
	public static final String BUNDLE_EXPORTS 											= MARKER_ADDON + "Bundle Exports"  						  +  MARKER_ADDON ;
	public static final String BUNDLE_FRAGMENT_HOST 									= MARKER_ADDON + "If FRAGMENT then HOSTBUNDLE"  			 +  MARKER_ADDON ;
	public static final String BUNDLE_IGNORE 											= MARKER_ADDON + "IGNORE BUNDLE COZ MANIFEST OR SOME PROPERTY OF IT WAS MALFORMED " 			+ MARKER_ADDON;
	public static final String BUNDLE_PACKAGE_IMPORTS 									= MARKER_ADDON + "Bundle Package Imports"  				  +  MARKER_ADDON ;
	public static final String BUNDLE_PLUGIN_XML 										= MARKER_ADDON + "Bundle Plugin.xml"  					  +  MARKER_ADDON ;
	
	public static final String PROPERTY_VALUE_UNKNOWN_LITERAL							= "PAUNKNOWN";
	
	public static final String BUNDLE_REQUIREMENTS 										= MARKER_ADDON + "Bundle Requirements"  				  +  MARKER_ADDON ;
	public static final String BUNDLE_OTHER_BUNDLE_IMPORTS 								= MARKER_ADDON + "Bundle Other BUNDLE Imports"  			+ MARKER_ADDON ;
	public static final String BUNDLE_SYMBOLICNAME 										= MARKER_ADDON + "Symbolic Name"  						  +  MARKER_ADDON ;
	public static final String BUNDLE_VERSION 											= MARKER_ADDON + "Version"  							  +  MARKER_ADDON ;
	public static final String BUNDLE_VERSION_WITHOUT_QUALIFIER 						= MARKER_ADDON + "Version without qualifier"  			  +  MARKER_ADDON ;

	public static final String BUNDLE_VERSION_UNKNOWN_WITHOUT_QUALIFIER_LITERAL 		= PROPERTY_VALUE_UNKNOWN_LITERAL + "." + PROPERTY_VALUE_UNKNOWN_LITERAL + "." + PROPERTY_VALUE_UNKNOWN_LITERAL;
	public static final String BUNDLE_VERSION_UNKNOWN_LITERAL 							= BUNDLE_VERSION_UNKNOWN_WITHOUT_QUALIFIER_LITERAL  + "." + PROPERTY_VALUE_UNKNOWN_LITERAL;

	public static final String DELIM_VERSION_STRING_CLOSE 						= "%>";
	public static final String DELIM_VERSION_STRING_OPEN 						= "<%";
	public static final String DELIM_PLUGIN_DEPENDENCY_ELEMENT_SET  					= "####";
	public static final String DELIM_PLUGIN_ELEMENT_SUPERCLASS_INTERFACE 				= " => ";

	public static final String FM_CONSTRAINTS_FILE_EXTENSION 							= ".fm";
	public static final String FM_CONSTRAINTS_FILE_NAME 								= "constraints";
	public static final String FM_CONSTRAINTS_FILE_PREFIX 								= "";
	public static final String FM_PLUGIN_DEPENDENCIES_FILE_PREFIX 						= "";
	public static final String FM_PLUGIN_DEPENDENCIES_FILE_NAME 						= "dependencies";
	public static final String FM_PLUGIN_DEPENDENCIES_FILE_EXTENSION 					= ".fm";
	
	public static final String DEPENDENCY_SET_FILE_EXTENSION_PLUGIN 					= ".dpst";
	public static final String DEPENDENCY_SET_FILE_PREFIX_PLUGIN 						= "DEPENDENCY-SET-PLUGIN-";
	
	public static final String EXCEPTION_MSG_EARLY_END_OF_A_PARAMETER 					= "Early end of a parameter";

	public static final String EXTRACT_FILE_EXTENSION_FEATURE							= ".extr";
	public static final String EXTRACT_FILE_EXTENSION_FEATUREMAP 						= ".obj";
	public static final String EXTRACT_FILE_EXTENSION_PLUGIN 							= ".extr";
	public static final String EXTRACT_FILE_EXTENSION_PLUGINMAP 						= ".obj";
	public static final String EXTRACT_FILE_NAME_JAVA_CLASSES_SDK 						= "JAVA.CLASSES.SDK";
	public static final String EXTRACT_FILE_NAME_FEATUREMAP 							= "FEATURE-MAP-OBJECT";
	public static final String EXTRACT_FILE_NAME_PLUGINMAP 								= "PLUGIN-MAP-OBJECT";
	public static final String EXTRACT_FILE_PREFIX_FEATURE 								= "EXTRACT-FEATURE-";
	public static final String EXTRACT_FILE_PREFIX_FEATUREMAP 							= "EXTRACT-FEATUREMAP-";
	public static final String EXTRACT_FILE_PREFIX_PLUGIN 								= "EXTRACT-PLUGIN-";
	public static final String EXTRACT_FILE_PREFIX_PLUGINMAP 							= "EXTRACT-PLUGINMAP-";
	public static final String EXTENSION_TXT 											= ".txt";
	
	public static final String JAR_FILE_EXTENSION 										= ".jar";

	public static final String FEATURE_FEATURE_XML 										= MARKER_ADDON + "Feature.xml " 				+ MARKER_ADDON;
	public static final String FEATURE_LABEL 											= MARKER_ADDON + "Label " 						+ MARKER_ADDON;
	public static final String FEATURE_IMPORTS 											= MARKER_ADDON + "Imports " 					+ MARKER_ADDON;
	public static final String FEATURE_PROVIDED_PLUGINS 								= MARKER_ADDON + "Provided Plugins " 				+ MARKER_ADDON;
	public static final String FEATURE_DESCRIPTION 										= MARKER_ADDON + "Description " 				+ MARKER_ADDON;
	public static final String FEATURE_UPDATE_LABEL 									= MARKER_ADDON + "UpdateLabel " 				+ MARKER_ADDON;
	public static final String FEATURE_URL 												= MARKER_ADDON + "URL " 								+ MARKER_ADDON;
	public static final String FEATURE_PROVIDER_NAME 									= MARKER_ADDON + "ProviderName " 				+ MARKER_ADDON;
	public static final String FEATURE_VERSION_WITHOUT_QUALIFIER 						= MARKER_ADDON + "Version  Without Qualifier  "  		+ MARKER_ADDON;
	public static final String FEATURE_VERSION 											= MARKER_ADDON + "Version " 							+ MARKER_ADDON;
	public static final String FEATURE_ID 												= MARKER_ADDON + "Id   " 						+ MARKER_ADDON;

	public static final String PLUGIN_ALL_CLASS_PACKAGES 								= MARKER_ADDON + "All Class packages " 						+ MARKER_ADDON;
	public static final String PLUGIN_ALL_INHERITANCE_AND_INTERFACE_PAIRS   			= MARKER_ADDON + "All Inheritance AND Interface Impl Pairs Super Set "			+ MARKER_ADDON;
	public static final String PLUGIN_ALL_INHERITANCE_HIERARCHIES 						= MARKER_ADDON + "All Inheritance Hierarchies "  	  		+ MARKER_ADDON;
	public static final String PLUGIN_ALL_INHERITANCE_PAIRS 							= MARKER_ADDON + "All My Inheritance Pairs  "				+ MARKER_ADDON;
	public static final String PLUGIN_ALL_INTERFACE_IMPLEMENTATION_LISTS 				= MARKER_ADDON + "All Interfaces Implementation Lists "		+ MARKER_ADDON;
	public static final String PLUGIN_ALL_INTERFACE_IMPLEMENTATION_PAIRS 				= MARKER_ADDON + "All Interfaces Implementation Pairs "		+ MARKER_ADDON;
	public static final String PLUGIN_ALL_INVOKATION_PROXY_PAIRS 						= MARKER_ADDON + "All Invokation Satisfaction Potential Proxy Pairs"			+ MARKER_ADDON;
	public static final String PLUGIN_ALL_JAR_PACKAGES 									= MARKER_ADDON + "All Jar Packages" 						+ MARKER_ADDON;
	public static final String PLUGIN_ALL_MY_METHOD_CALLS 								= MARKER_ADDON + "All Invokations" 							+ MARKER_ADDON;
	public static final String PLUGIN_ALL_MY_METHOD_CALLS_EXTERNAL 						= MARKER_ADDON + "All External Invokations" 				+ MARKER_ADDON;
	public static final String PLUGIN_ALL_MY_METHOD_CALLS_EXTERNAL_AND_NON_JAVA 		= MARKER_ADDON + "All External and non Java Invokations" 	+ MARKER_ADDON;
	public static final String PLUGIN_ALL_MY_METHODS 									= MARKER_ADDON + "All My Methods" 							+ MARKER_ADDON;
	public static final String PLUGIN_ALL_MY_METHODS_DEPRECATED 						= MARKER_ADDON + "All My Deprecated Methods" 				+ MARKER_ADDON;
	public static final String PLUGIN_ALL_MY_METHODS_PUBLIC 							= MARKER_ADDON + "All My Public Methods" 					+ MARKER_ADDON;
	public static final String PLUGIN_ALL_MY_TYPES 										= MARKER_ADDON + "All My Classes (Types)" 					+ MARKER_ADDON;
	public static final String PLUGIN_ALL_MY_TYPES_DEPRECATED 							= MARKER_ADDON + "All My Deprecated Classes" 				+ MARKER_ADDON;
	public static final String PLUGIN_ALL_MY_TYPES_PUBLIC 								= MARKER_ADDON + "All My Public Classes (Types)" 			+ MARKER_ADDON;
	public static final String PLUGIN_ALL_TYPES_DETECTED 								= MARKER_ADDON + "All Detected Types" 						+ MARKER_ADDON;
	public static final String PLUGIN_ALL_TYPES_DETECTED_EXTERNAL 						= MARKER_ADDON + "All External Detected Types" 				+ MARKER_ADDON;
	public static final String PLUGIN_ALL_TYPES_DETECTED_EXTERNAL_AND_NON_JAVA 			= MARKER_ADDON + "All External Non Java Detected Types" 	+ MARKER_ADDON;
	
	public static final String PLUGIN_DEPENDENCY_ALL_FUNCTIONS  						= MARKER_ADDON + "All functions " 							+ MARKER_ADDON;
	public static final String PLUGIN_DEPENDENCY_ALL_IGNORED_PLUGINS 					= MARKER_ADDON + "Plugins IGNORED  " 						+ MARKER_ADDON;
	public static final String PLUGIN_DEPENDENCY_ALL_TYPES  							= MARKER_ADDON + "All types (classes) " 					+ MARKER_ADDON;
	public static final String PLUGIN_DEPENDENCY_ALL_UNMATCHED_FUNCTION_IMPORTS 		= MARKER_ADDON + "All UnMatched Invokations" 				+ MARKER_ADDON;
	public static final String PLUGIN_DEPENDENCY_ALL_UNMATCHED_TYPE_IMPORTS 			= MARKER_ADDON + "All UnMatched Types" 						+ MARKER_ADDON;
	public static final String PLUGIN_DEPENDENCY_EXPORTERS  							= MARKER_ADDON + "Exporters " 								+ MARKER_ADDON;
	public static final String PLUGIN_DEPENDENCY_EXPORTERS_UNSATISFIED  				= MARKER_ADDON + "Exporters  UnSatisfied " 					+ MARKER_ADDON;
	public static final String PLUGIN_DEPENDENCY_IMPORTERS  							= MARKER_ADDON + "Importers " 								+ MARKER_ADDON;
	public static final String PLUGIN_DEPENDENCY_IMPORTERS_UNSATISFIED  				= MARKER_ADDON + "Importers  UnSatisfied " 					+ MARKER_ADDON;
	public static final String PLUGIN_DEPENDENCY_SATISFYING_PLUGINS_SETS 				= MARKER_ADDON + "Satisfying Plugins SETS"					+ MARKER_ADDON;

	public static final String JAVA_LANG_OBJECT 									  	= "java.lang.Object" ;

	public static final List<String> JAVA_LANG_OBJECT_FUNCTIONS = new ArrayList<String>(Arrays.asList(
			"boolean java.lang.Object.equals (java.lang.Object,)",
			"int java.lang.Object.hashCode ()",
			"java.lang.Class java.lang.Object.getClass ()",
			"java.lang.Object java.lang.Object.clone ()",
			"java.lang.String java.lang.Object.toString ()",
			"void java.lang.Object.finalize ()",
			"void java.lang.Object.notify ()",
			"void java.lang.Object.notifyAll ()",
			"void java.lang.Object.wait ()",
			"void java.lang.Object.wait (long,)",
			"void java.lang.Object.wait (long,int,)"
			));




}
