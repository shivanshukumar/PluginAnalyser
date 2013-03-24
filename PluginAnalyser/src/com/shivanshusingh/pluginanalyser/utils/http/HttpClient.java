package com.shivanshusingh.pluginanalyser.utils.http;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.shivanshusingh.pluginanalyser.utils.Util;
import com.shivanshusingh.pluginanalyser.utils.logging.Log;


public class HttpClient {
	
	
	private static   String MIME_DEFAULT="text/plain";
	
	public static HttpResponse get(String site, String suffix, Map<String, String> params) throws URISyntaxException {

		return	get( site,  suffix,  params, null) ;

	}
	public static HttpResponse get(String site, String suffix, Map<String, String> params, String mime) throws URISyntaxException {

		Log.outln("GET=" + getBaseURI(site, suffix, params));
		HttpGet request = new HttpGet(getBaseURI(site, suffix, params));
		HttpResponse response = null;
		response =makeRequest(request, mime) ;
		//handleResponse_print(response  );
		return   response;
		

	}
	public static boolean connect(String site) {

		
		try {
			
			HttpResponse response = null;
			response = get(site, null, null);

			if (null == response) {

				return false;
			}
			if (!(response.getStatusLine().getStatusCode() == 200)) {
				// Log.outln("could not get a response");
				return false;
			}
			HttpEntity httpEntity = response.getEntity();
			// Get the response
			BufferedReader rd;
			
			
				rd = new BufferedReader(new InputStreamReader(
						httpEntity.getContent()));

				String line = "";
				while ((line = rd.readLine()) != null) {
					Log.outln(line);
				}
		} catch (NoHttpResponseException e2) {
			// e2.printStackTrace();
			return false;

		} catch (IOException e2) {
			// e2.printStackTrace();
			return false;

		} catch (Exception e1) {
			// e1.printStackTrace();
			return false;
		}

		return true;

	}

	
	public static HttpResponse post(String site, String suffix, Map<String, String> getParams,
			Map<String, String> postParams) throws URISyntaxException {
		Log.outln("POST=" + getBaseURI(site, suffix, getParams));

		HttpPost request = new HttpPost(getBaseURI(site, suffix, getParams));

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

		if (null != postParams) {

			Set<String> keySet = postParams.keySet();
			for (String param : keySet)
				nameValuePairs.add(new BasicNameValuePair(param, postParams
						.get(param)));
		}

		try {
			request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}
		HttpResponse response = null;
		response =makeRequest(request) ;
		//handleResponse_print(response  );
		return   response;
	}

	
	/**
	 * @param request
	 *  @param  mime
	 * @return  {@link HttpResponse }
	 */
	private static HttpResponse makeRequest(HttpUriRequest request,  String mime) {
		org.apache.http.client.HttpClient client = new DefaultHttpClient();
		if(null==mime  ||  "".equalsIgnoreCase(mime))
			mime=MIME_DEFAULT;
		request.addHeader("Accept",  mime );
		HttpResponse response = null;
		try {
			response = client.execute(request);
		} catch (ClientProtocolException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		return response;
		
	}
	/**
	 * @param request
	 * @return  {@link HttpResponse }
	 */
	private static HttpResponse makeRequest(HttpUriRequest request) {
		org.apache.http.client.HttpClient client = new DefaultHttpClient();
		request.addHeader("Accept", MIME_DEFAULT);
		HttpResponse response = null;
		try {
			response = client.execute(request);
		} catch (ClientProtocolException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		return response;
		
	}

	/**
	 * @param response
	 */
	public static void handleResponse_print(HttpResponse response) {
		
		Log.outln(handleResponse_makeString(response));
	}

	private static URI getBaseURI(String site, String suffix, Map<String, String> params) throws URISyntaxException {

		if(null==site || "".equalsIgnoreCase(site))
		{
			//nothing to be formed ,  the site parameter is blank.
			
			return null;
		}
		String s = "?";
		if (null != params)
		{

			Set<String> keySet = params.keySet();
			for (String param : keySet)
				s += param + "=" + params.get(param) + "&";
		}
		else
			{
				s="";
			}
		return new URIBuilder(site + (null!=suffix? suffix:"") + s).build();
	}
	
	
	public static File handleResponse_makeFile(HttpResponse response) {
		String fileName = (
					Util.getTEMP_DIR_PATH() +
					"/pa-sks-tmp-" +
					("" + Math.random()).replaceAll("\\.", "-")
				).replaceAll("//", "/");
		
		File file = null;

		if (null == response) {
			Log.outln("could not get a response");
			return file;
		}
		if (!(response.getStatusLine().getStatusCode() == 200)) {
			Log.outln("status of response = not ok = " + response.getStatusLine().getStatusCode());
			return file;
		}

		HttpEntity httpEntity = response.getEntity();
		// put the response in a file and return the file handler.
		file = new File(fileName);

		try {

			BufferedWriter bufferedTempWriter = new BufferedWriter(new FileWriter(file));

			BufferedReader bufferedTempReader = new BufferedReader(new InputStreamReader(httpEntity.getContent()));

			int inread;
			while ((inread = bufferedTempReader.read()) != -1) {
				bufferedTempWriter.write(inread);
			}
			bufferedTempReader.close();
			bufferedTempWriter.close();

		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return file;
	}
	
	
	public static String handleResponse_makeString(HttpResponse response) {
		String output = "";
		if (null == response) {
			Log.outln("could not get a response");
			return  output;
		}
		if (!(response.getStatusLine().getStatusCode() == 200)) {
			Log.outln("status of response = not ok = "        +response.getStatusLine().getStatusCode()  );
			return  output;
		}
		HttpEntity httpEntity = response.getEntity();
		// Get the response
		BufferedReader rd;
		
		try {
			rd = new BufferedReader(new InputStreamReader(
					httpEntity.getContent()));

			String line = "";
			while ((line = rd.readLine()) != null) {
				output += line;
				// Log.outln(line);
			}
			
			

		} 
		 catch (IllegalStateException e) {
			
			e.printStackTrace();
			 return"";
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
		return output;
	}

}
