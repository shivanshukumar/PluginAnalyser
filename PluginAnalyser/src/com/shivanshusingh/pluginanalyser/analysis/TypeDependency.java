
package com.shivanshusingh.pluginanalyser.analysis;

import java.util.HashSet;
import java.util.Set;

/**
 * 
 * a holder of Type Dependencies (Other Classes and Interfaces)
 * @author Shivanshu Singh
 *
 */
public class TypeDependency {
	
	public String superClass="";
	public Set<String> interfaces=new HashSet<String>();
}
