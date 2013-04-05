package com.shivanshusingh.pluginanalyser.comparison;

import java.util.HashSet;
import java.util.Set;

/**
 * 
 * holder of importing and exporting elements.
 * 
 * @author Shivanshu Singh
 * 
 */
public class ImpExp {

	
	public Set<Set<String>> satisfyingPluginsSets=new HashSet<Set<String>>();
//	public Set<Set> satisfyingPluginsSets = new HashSet<Set>();
	
	private Set<String> exp = new HashSet<String>();

	private Set<String> imp = new HashSet<String>();

	public Set<String> getExp() {
		return exp;
	}

	public void addToExp(String arg0) {
		this.exp.add(arg0);
	}

	public void addToImp(String arg0) {
		this.imp.add(arg0);
	}

	public Set<String> getImp() {
		return imp;
	}

	public void setExp(Set<String> exp) {
		this.exp = exp;
	}

	public void setImp(Set<String> imp) {
		this.imp = imp;
	}
	
	/*@Override
	public String toString()
	{
		StringBuffer   sb = new StringBuffer();
		sb.append("[imp=[");
		for(Object s:imp)
			sb.append((String)s+",");
		sb.append("],exp=[");
		for(Object s:exp)
			sb.append((String)s+",");
		sb.append("],satisfyingPluginsSets=[");
		
		for(String s:satisfyingPluginsSets)
			sb.append(s+",");
		sb.append("]");
		sb.append("]");
		return sb.toString();
	}
	*/
}