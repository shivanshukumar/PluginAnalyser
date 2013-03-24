package com.shivanshusingh.pluginanalyser.eclipse.marketplace.crawling;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.shivanshusingh.pluginanalyser.eclipse.marketplace.EclipseUpdateSiteInformation;
import com.shivanshusingh.pluginanalyser.eclipse.marketplace.parsing.Constants;
import com.shivanshusingh.pluginanalyser.staging.crawling.SourceCrawler;
import com.shivanshusingh.pluginanalyser.utils.Util;
import com.shivanshusingh.pluginanalyser.utils.http.HttpClient;
import com.shivanshusingh.pluginanalyser.utils.logging.Log;

/**
 * 
 * eclipse marketplace api client that gets a number of update sites from the
 * eclipse marketplace.
 * 
 * @author Shivanshu Singh
 * 
 */
public class EclipseMarketplaceCrawler implements SourceCrawler {

	private static final String MARKETS_CATEGORIES_URI = "/api/p";

	private static final String MIME = "text/xml";

	private static final String NODE_URI = "/node/[node id]/api/p";

	private static final String NODE_URI_ID_ELEM = "[node id]";

	private static final String PROTOCOL = "http://";

	private static final String SOLR_SEARCH_URI = "/api/p/search/apachesolr_search";
	
	private static final String UPDATE_SITE_FILE_EXTENTION = ".xml";

	private static final String SOURCES[] = {
		"marketplace.eclipse.org",
//		"market.eclipsesource.com/yoxos",
		
		};

	private String DEST_DIR_PATH = null;

	private int endId = 1000;

	private Map<String, EclipseUpdateSiteInformation> siteURLs;// =new
	// HashSet<String>();

	private int startId = 1;

	public EclipseMarketplaceCrawler() {
		initialize();

	}

	/**
	 * 
	 * with range of eclipse marketplace nodeIDs
	 * 
	 * @param startId
	 *            starting nodeId
	 * @param endId
	 *            ending nodeId
	 */
	public EclipseMarketplaceCrawler(int startId, int endId) {
		inititialize(startId, endId);
		initialize();
	}

	public EclipseMarketplaceCrawler(int startId, int endId, String destinationDirPath) {
		inititialize(startId, endId);
		initialize(destinationDirPath);
		initialize();
	}

	/**
	 * 
	 * with the option of recording the information at a given location.
	 * 
	 * @param destinationDirPath
	 */
	public EclipseMarketplaceCrawler(String destinationDirPath) {
		initialize(destinationDirPath);
		initialize();
	}

	@Override
	public void crawl() {

		// the REAL LENGTHY AND STRUCTURED, process to build your own 'catalog'
		// is to:
		/*
		 * In terms of building your own catalog with the Marketplace API you
		 * would follow a three step process.
		 * 
		 * 1.Query the Base URL for a listing of Markets and their categories.
		 * Retrieving A listing of Markets and Categories
		 * http://marketplace.eclipse.org/api/p
		 * 
		 * > This will return a listing of Markets and Categories, it includes
		 * URLs for each category, as well number of listings in each category.
		 * 
		 * 2.Iterate through the result to record the <category id, market id>
		 * aka catTuple pairs. Then use the following API to get a list of all
		 * nodes for this category(category id) in this market (market id):
		 * 
		 * http://marketplace.eclipse.org/taxonomy/term/[catTuple]/api/p
		 * 
		 * 3.Finally iterate through each of the nodes gotten above through the
		 * api:
		 * 
		 * http://marketplace.eclipse.org/node/[node id]/api/p
		 * 
		 * form this a bunch of imformation including any eclipse update URL if
		 * available, can be extracted.
		 * 
		 * 
		 * 
		 * Also, there is a solr search API as well for more structured focussed
		 * queries which can be used later on. See
		 * http://wiki.eclipse.org/Marketplace/REST#Building_a_Catalog
		 */

		/*
		 * Continuing from the above, ............
		 * ********************************************** HOWEVER, ther is a
		 * HACKY WAY to get a bunch of URLs, if just a bunch of update URLs are
		 * all that we need. take the api :
		 * http://marketplace.eclipse.org/node/[node id]/api/p and iterate over
		 * [node id] and try to see if any update API is available.
		 * *********************************************
		 */
	for(String SOURCE:SOURCES)
		for (int nodeId = startId; nodeId <= endId; nodeId++) {

			boolean flag_wasRecorded = false;
			String source = SOURCE + NODE_URI.replace(NODE_URI_ID_ELEM, "" + nodeId);
			HttpResponse httpResponse;
			try {
				httpResponse = HttpClient.get(PROTOCOL, source, null, MIME);
			} catch (URISyntaxException e1) {

				e1.printStackTrace();

				continue;
			}
			File file = HttpClient.handleResponse_makeFile(httpResponse);
			try {
				if (null == file) {
					// this means that there was no response from the server.
					continue;
				}
				Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
				doc.normalizeDocument();

				// checking if the entry that we got has any information in it,
				// i.e. if any information for the said [node id] exists at all.
				NodeList nodeList = doc.getElementsByTagName(Constants.NODE_ELEM);
				if (null != nodeList && 1 <= nodeList.getLength()) {
					// this means that there is something available and that a
					// node for the given nodeId exists in the marketplace.

					// there is only one node element, for sure as we asked for
					// only one :)
					Node nodeItem = nodeList.item(0);

					if (null != nodeItem) {
						Element nodeElem = (Element) nodeItem;

						if (null != nodeElem.getTextContent() && 1 <= nodeElem.getTextContent().trim().length()) {

							// Ahhhh, we finally have soemthing at hand, lets
							// extract infotmation now.

							// but still, lets check if there is any update site
							// information present.
							NodeList updateURLList = doc.getElementsByTagName(Constants.NODE_ELEM_UPDATE_URL);
							if (null != updateURLList && 1 <= updateURLList.getLength()) {
								Node nodeURLItem = updateURLList.item(0);
								if (null != nodeURLItem) {
									String updateSiteURL = ((Element) nodeURLItem).getTextContent();
									// System.out.println("updateSiteURL="+updateSiteURL);
									if (null != updateSiteURL && 1 <= updateSiteURL.trim().replace(PROTOCOL, "").length()
											&& Util.validURL(updateSiteURL)) {
										String gotNodeId = nodeElem.getAttribute(Constants.NODE_ELEM_NODE_ID).trim();
										EclipseUpdateSiteInformation eusi = new EclipseUpdateSiteInformation();
										if (this.siteURLs.containsKey(gotNodeId))
											eusi = this.siteURLs.get(gotNodeId);
										eusi.setNodeId(nodeElem.getAttribute(Constants.NODE_ELEM_NODE_ID).trim());
										eusi.setNodeName(nodeElem.getAttribute(Constants.NODE_ELEM_NODE_NAME).trim());
										eusi.setUpdateURL(updateSiteURL.trim());
										this.siteURLs.put(gotNodeId, eusi);
										flag_wasRecorded = true;
										// System.out.println("OUTPUT+++++++========="
										// + eusi.nodeId+eusi.nodeName +
										// eusi.updateURL );
									}
								}
							}
						}
					}
				}

			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
			if (null != file) {
				if (null != this.DEST_DIR_PATH && flag_wasRecorded) {
					File newFile = new File(this.DEST_DIR_PATH + "/" +SOURCE.replaceAll("/", "-")+"/"+ source.replaceAll("/", "-")+UPDATE_SITE_FILE_EXTENTION);
					if (newFile.exists())
						newFile.delete();
					try {
						FileUtils.moveFile(file, newFile);
						Log.outln(file.getAbsolutePath() + "  |  movedTo=" + newFile.getAbsolutePath());

					} catch (IOException e) {

						e.printStackTrace();
					}

				} else {
					Log.outln(file.getAbsolutePath() + "  |  delete=" + file.delete());
				}
			}
		}

	}

	@Override
	public Collection<EclipseUpdateSiteInformation> getSites() {
		return siteURLs.values();
	}

	private void initialize() {
		siteURLs = new HashMap<String, EclipseUpdateSiteInformation>();

	}

	/**
	 * @param destinationDirPath
	 */
	private void initialize(String destinationDirPath) {
		siteURLs = new HashMap<String, EclipseUpdateSiteInformation>();
		if (null != destinationDirPath && 1 <= destinationDirPath.trim().length())
			this.DEST_DIR_PATH = destinationDirPath.trim();
	}

	/**
	 * @param startId
	 * @param endId
	 */
	private void inititialize(int startId, int endId) {
		this.startId = startId;
		this.endId = endId;
	}

}
