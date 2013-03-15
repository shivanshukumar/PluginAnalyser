package com.shivanshusingh.PluginAnalyser.Analysis;

import java.util.HashSet;
import java.util.Set;

/*
 * contains the Feature meta data, feature.xml, the associated plugins and the other dependencies.
 */

public class FeatureInformation {
	
	

	private  Set<String> plugins = new HashSet<String>();
	private  StringBuffer xml = new StringBuffer();
	private  String version;
	private Set<Import> imports =  new HashSet<Import>();
	private String description;
	
	public String getDescription() {
		return description;
	}



	public void setDescription(String description) {
		this.description = description;
	}



	public Set<String> getPlugins() {
		return plugins;
	}



	public void setPlugins(Set<String> plugins) {
		this.plugins = plugins;
	}



	public String getXml() {
		return xml.toString();
	}



	public void setXml(String xml) {
		this.xml = new StringBuffer(xml);
	}
	public void appendXml(StringBuffer sb) {
		this.xml.append(sb);
	}



	public String getVersion() {
		return version;
	}



	public void setVersion(String version) {
		this.version = version;
	}



	public Set<Import> getImports() {
		return imports;
	}



	public void setImports(Set<Import> imports) {
		this.imports = imports;
	}


	
	
	public FeatureInformation() {	
	}


}
class Import{
	private String element;
	public String getElement() {
		return element;
	}
	public void setElement(String element) {
		this.element = element;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	private String version;
	
	
}