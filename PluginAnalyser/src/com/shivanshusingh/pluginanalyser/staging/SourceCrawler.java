package com.shivanshusingh.pluginanalyser.staging;

import java.util.HashSet;
import java.util.Set;

/**
 * 
 * interface for the SourceCrawler: any crawler that crawls some source and
 * returns a {@link Set} of eclipse update site URLs.
 * 
 * @author Shivanshu Singh
 * 
 */
public interface SourceCrawler {

	/**
	 * returns a {@link Set}<{@link String}> of update site URLs fetched by
	 * crawling the source
	 * 
	 * @return {@link Set}<{@link String}>
	 */
	public Set<String> getSiteURLs();

	/**
	 * crawls the source to refresh the {@link Set}<{@link String}> siteURLs
	 */
	public void crawl();

}
