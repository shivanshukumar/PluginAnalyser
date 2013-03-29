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

	
	public Set<Set<String>> exporterSets=new HashSet<Set<String>>();
	//public Set<Set> exporterSets = new HashSet<Set>();
	
	private Set exp = new HashSet();

	private Set imp = new HashSet();

	public Set getExp() {
		return exp;
	}

	public void addToExp(Object arg0) {
		this.exp.add(arg0);
	}

	public void addToImp(Object arg0) {
		this.imp.add(arg0);
	}

	public Set getImp() {
		return imp;
	}

	public void setExp(Set exp) {
		this.exp = exp;
	}

	public void setImp(Set imp) {
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
		sb.append("],exporterSets=[");
		
		for(String s:exporterSets)
			sb.append(s+",");
		sb.append("]");
		sb.append("]");
		return sb.toString();
	}
	*/
}