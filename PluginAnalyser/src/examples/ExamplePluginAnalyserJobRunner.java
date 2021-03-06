package examples;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import com.shivanshusingh.pluginanalyser.analysis.AnalysisRunner;
import com.shivanshusingh.pluginanalyser.eclipse.marketplace.crawling.EclipseMarketplaceCrawler;
import com.shivanshusingh.pluginanalyser.job.PluginAnalyserJobRunner;
import com.shivanshusingh.pluginanalyser.staging.DownloadAndStagingRunner;
import com.shivanshusingh.pluginanalyser.staging.crawling.SourceCrawler;
import com.shivanshusingh.pluginanalyser.staging.crawling.UpdateSiteInfo;
import com.shivanshusingh.pluginanalyser.utils.Util;
import com.shivanshusingh.pluginanalyser.utils.logging.Log;

/**
 * @author Shivanshu Singh
 * 
 */
public class ExamplePluginAnalyserJobRunner {

	public static void main(String[] args) {
//
//		SourceCrawler ec = new EclipseMarketplaceCrawler();
//		 SourceCrawler ec=new EclipseMarketplaceCrawler(     158915, 180100,
//		 "./_UPDATE-SITE-DATA");
//		 ec.crawl();
//		ec.restoreFromBaseLocation("./UPDATE-SITE-DATA");
//		Set<UpdateSiteInfo> gotUpdateSites = new HashSet();
//		gotUpdateSites.addAll(ec.getSites());
//
//		for (UpdateSiteInfo updateSiteInfo : gotUpdateSites) {
//			Log.outln("name=" + updateSiteInfo.getName() + ";\t site=" + updateSiteInfo.getUpdateURL());
//
//		}
//		Log.outln("" + gotUpdateSites.size());

		 PluginAnalyserJobRunner job = new PluginAnalyserJobRunner();
		 job.run();

	}

}
