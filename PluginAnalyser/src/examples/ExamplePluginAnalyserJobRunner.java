package examples;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.shivanshusingh.pluginanalyser.analysis.AnalysisRunner;
import com.shivanshusingh.pluginanalyser.job.PluginAnalyserJobRunner;
import com.shivanshusingh.pluginanalyser.staging.DownloadAndStagingRunner;
import com.shivanshusingh.pluginanalyser.utils.Util;
import com.shivanshusingh.pluginanalyser.utils.logging.Log;

/**
 * @author Shivanshu Singh
 * 
 */
public class ExamplePluginAnalyserJobRunner {

	public static void main(String[] args) {

		
		PluginAnalyserJobRunner job = new PluginAnalyserJobRunner();
		job.run();

	}

}
