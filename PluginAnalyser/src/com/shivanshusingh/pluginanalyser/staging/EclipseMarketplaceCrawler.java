/**
 * 
 */
package com.shivanshusingh.pluginanalyser.staging;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpResponse;

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

	private Set<String> siteURLs;// =new HashSet<String>();
	private static final String source="http://marketplace.eclipse.org";
	private static final String marketplaceMarketsCategoriesURL="/api/p/search/apachesolr_search";

	public EclipseMarketplaceCrawler() {
		siteURLs = new HashSet<String>();
	}

	@Override
	public void crawl() {

		
		try {
		HttpResponse httpResponse = HttpClient.get(source,marketplaceMarketsCategoriesURL, null);
		HttpClient.handleResponse_print(httpResponse);				
			
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		// TODO implement the process of getting the URLs from the eclipse
		// marketplace.

	}

	@Override
	public Set<String> getSiteURLs() {
		return siteURLs;
	}

}
