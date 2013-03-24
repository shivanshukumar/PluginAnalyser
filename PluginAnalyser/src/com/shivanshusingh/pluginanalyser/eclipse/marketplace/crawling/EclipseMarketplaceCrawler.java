/**
 * 
 */
package com.shivanshusingh.pluginanalyser.eclipse.marketplace.crawling;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.shivanshusingh.pluginanalyser.eclipse.marketplace.EclipseUpdateSiteInformation;
import com.shivanshusingh.pluginanalyser.staging.crawling.SourceCrawler;
import com.shivanshusingh.pluginanalyser.utils.http.HttpClient;

/**
 * 
 * eclipse marketplace api client that gets a number of update sites from the
 * eclipse marketplace.
 * 
 * @author Shivanshu Singh
 * 
 */
public class EclipseMarketplaceCrawler implements SourceCrawler {

	private Set<EclipseUpdateSiteInformation> siteURLs;// =new HashSet<String>();
	private static final String source = "http://marketplace.eclipse.org";
	private static final String marketplaceMarketsCategoriesURL = "/api/p/search/apachesolr_search";

	public EclipseMarketplaceCrawler() {
		siteURLs = new HashSet<EclipseUpdateSiteInformation>();
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
		 * **********************************************
		 * HOWEVER, ther is a HACKY WAY to get a bunch of URLs, if just a bunch
		 * of update URLs if tahst all that we need. take the api:
		 * http://marketplace.eclipse.org/node/[node id]/api/p
		 * and iterate over [node id] and try to see if any update API is available.
		 * *********************************************
		 */

		try {
			HttpResponse httpResponse = HttpClient.get(source, marketplaceMarketsCategoriesURL, null);
			File file = HttpClient.handleResponse_makeFile(httpResponse);
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
			doc.normalizeDocument();
			NodeList nodeList = doc.getElementsByTagName("import");
			// TODO implement the process of getting the URLs from the eclipse
			// marketplace.
			
			
			
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

	}

	@Override
	public Set<EclipseUpdateSiteInformation> getSites() {
		return siteURLs;
	}

}
