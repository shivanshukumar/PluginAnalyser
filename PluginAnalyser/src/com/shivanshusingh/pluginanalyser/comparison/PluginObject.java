package com.shivanshusingh.pluginanalyser.comparison;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class PluginObject {
	
public String name;

/**format: class;function = no return tyoe to be added here.
 * 
 */
public Set<String> imports= new HashSet<String>();// all function signatures and all types (imported or invoked) = super set
public Set<String> exports=new HashSet<String>();// all function signatures and all types  (exported) = super set
public Map<String,Set<String>> superClassesAndInterfaces=new HashMap<String,Set<String>>();

public Map<String,Set<String>> invokationProxies  =  new HashMap<String,Set<String>>();

@Override
public   String toString()
{
	StringBuffer   sb = new StringBuffer();
	sb.append("[imports=[");
	for(String s:imports)
		sb.append(s+",");
	sb.append("],exports=[");
	for(String s:exports)
		sb.append(s+",");
	sb.append("],invokationProxies=[");
	for(Entry<String, Set<String>> s: invokationProxies.entrySet())
	{
		sb.append(s.getKey().toString()+"=[");
		for(String s1:s.getValue())
		{
			sb.append(s1+",");
		}
		sb.append("],");
	}
	sb.append("]");
	sb.append("],superClassesAndInterfaces=[");
	for(Entry<String, Set<String>> s: superClassesAndInterfaces.entrySet())
	{
		sb.append(s.getKey().toString()+"=[");
		for(String s1:s.getValue())
		{
			sb.append(s1+",");
		}
		sb.append("],");
	}
	sb.append("]");
	sb.append("]");
	return sb.toString();
}


}
