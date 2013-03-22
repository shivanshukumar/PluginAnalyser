/**
 * 
 */
package com.shivanshusingh.pluginanalyser.staging;

import java.util.HashSet;
import java.util.Set;

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

	public EclipseMarketplaceCrawler() {
		siteURLs = new HashSet<String>();
	}

	@Override
	public void crawl() {
		// TODO implement the process of getting the URLs from the eclipse
		// marketplace.

	}

	@Override
	public Set<String> getSiteURLs() {
		return siteURLs;
	}

}
