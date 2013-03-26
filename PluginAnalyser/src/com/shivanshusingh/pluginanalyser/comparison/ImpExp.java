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
}