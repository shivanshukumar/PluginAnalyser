package com.shivanshusingh.PluginAnalyser.Analysis;

import java.util.HashSet;
import java.util.Set;

/*
 * contains the Feature meta data, feature.xml, the associated plugins and the other dependencies.
 */

public class FeatureInformation {

	// plugin format: <name>;<version>
	private Set<String> plugins = new HashSet<String>();
	private StringBuffer xml = new StringBuffer();
	private String id;
	private String label;
	private String version;
	private String versionWithoutQualifier;
	private String providerName;
	private String url;
	private String updateLabel;
	// import format: <type>;<name>;<version><;match>
	private Set<String> imports = new HashSet<String>();
	private String description;

	public String getVersionWithoutQualifier() {
		return versionWithoutQualifier;
	}

	public void setVersionWithoutQualifier(String versionWithoutQualifier) {
		this.versionWithoutQualifier = versionWithoutQualifier;
	}

	
	public String getProviderName() {
		return providerName;
	}

	public void setProviderName(String providerName) {
		this.providerName = providerName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUpdateLabel() {
		return updateLabel;
	}

	public void setUpdateLabel(String updateLabel) {
		this.updateLabel = updateLabel;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<String> getPlugins() {
		return plugins;
	}

	public void addPlugin(String plugin) {
		this.plugins.add(plugin);
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

	public Set<String> getImports() {
		return imports;
	}

	public void addImport(String imp) {
		this.imports.add(imp);
	}

	public void setImports(Set<String> imports) {
		this.imports = imports;
	}

	public FeatureInformation() {
	}

}

class ElementVersionPair {
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