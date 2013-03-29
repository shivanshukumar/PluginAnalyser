package com.shivanshusingh.pluginanalyser.comparison;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PluginObject {
	
public String name;

/**format: class;function = no return tyoe to be added here.
 * 
 */
public Set<String> imports= new HashSet<String>();// all function signatures and all types (imported or invoked) = super set
public Set<String> exports=new HashSet<String>();// all function signatures and all types  (exported) = super set
public Map<String,Set<String>> superClassesAndInterfaces=new HashMap<String,Set<String>>();


}
