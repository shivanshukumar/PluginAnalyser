package com.shivanshusingh.pluginanalyser.eclipse.marketplace;

import com.shivanshusingh.pluginanalyser.staging.crawling.UpdateSiteInfo;

/**
 * holds information about an eclipse update site
 * 
 * @author Shivanshu Singh
 * 
 */
public class EclipseUpdateSiteInformation implements UpdateSiteInfo {

	private String nodeId;
	private String nodeListingXML;
	private String nodeName;
	private String updateURL;

	public String getNodeId() {
		return nodeId;
	}

	public String getNodeListingXML() {
		return nodeListingXML;
	}

	public String getNodeName() {
		return nodeName;
	}

	
	
	public String getUpdateURL() {
		return updateURL;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public void setNodeListingXML(String nodeListingXML) {
		this.nodeListingXML = nodeListingXML;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public void setUpdateURL(String updateURL) {
		this.updateURL = updateURL;
	}

	@Override
	public String getName() {
		return nodeName;
	}

	@Override
	public String getId() {
		return nodeId;
	}
}
