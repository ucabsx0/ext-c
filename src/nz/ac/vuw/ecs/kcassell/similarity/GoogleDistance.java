package nz.ac.vuw.ecs.kcassell.similarity;

/*
 * The code here is based on code retrieved from 
 * http://www.google.com/codesearch/p?hl=en#LjJS3knaY94/trunk/src/metrics/GoogleDistance.java&q=googledistance%20lang:java&sa=N&cd=1&ct=rc
  (http://reviewclustering.googlecode.com/svn)
*/

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class GoogleDistance {
	private static final String SEARCH_SITE_PREFIX =
		"http://ajax.googleapis.com/ajax/services/search/web?v=1.0&";
	protected static final String CACHE_FILE_NAME = "google.cache";

	static int counter = 0;

	/** The logarithm of a number that is (hopefully) greater than or equal
	 *  to the (unpublished) indexed number of Google documents.
	 *  http://googleblog.blogspot.com/2008/07/we-knew-web-was-big.html
	 *  puts this at a trillion or more.  */
	protected final static double logN = Math.log(1.0e12);

	Map<String, Integer> cache;
	
	/** Holds the new terms we entered (these are also in the cache) */
	Map<String, Integer> newCache = new HashMap<String, Integer>();

	public GoogleDistance() throws NumberFormatException, IOException {
		cache = setupCache(CACHE_FILE_NAME);
	}

	protected Map<String, Integer> setupCache(String filename)
			throws NumberFormatException, IOException {
		Map<String, Integer> cache = new HashMap<String, Integer>();
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String line;

		while ((line = reader.readLine()) != null) {
			int lastSpaceIndex = line.lastIndexOf(' ');
			String token = line.substring(0, lastSpaceIndex);
			int count = Integer.parseInt(line.substring(lastSpaceIndex + 1));
			cache.put(token, count);
		}

		reader.close();
		return cache;
	}

	/**
	 * Adds the contents of newCache to the specified file
	 * @param filename
	 */
	protected void updateCache(String filename) {

		if (counter++ >= 20) {
			BufferedWriter writer = null;
			try {
				writer = new BufferedWriter(new FileWriter(filename, true));

				for (Map.Entry<String, Integer> entry : newCache.entrySet()) {
					writer.append(entry.getKey() + " " + entry.getValue() + "\n");
				}
				newCache = new HashMap<String, Integer>();
				counter = 0;
			} catch (IOException e) {
				// Things will just take longer
			} finally {
				if (writer != null) {
					try {
						writer.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	protected int numResultsFromWeb(String term)
	throws JSONException, IOException {
		int result = 0;
		
		if (cache.containsKey(term)) {
			result = cache.get(term);
		} else {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}

			String searchTerm = term.replaceAll(" ", "+");
			URL url = null;
			InputStream stream = null;
			try {
				url = new URL(SEARCH_SITE_PREFIX + "q=" + searchTerm + " ");
				URLConnection connection = url.openConnection();
				stream = connection.getInputStream();
				InputStreamReader inputReader = new InputStreamReader(stream);
				BufferedReader bufferedReader = new BufferedReader(inputReader);

				JSONObject json = new JSONObject(new JSONTokener(bufferedReader));
				JSONObject responseData = json.getJSONObject("responseData");
				JSONObject cursor = responseData.getJSONObject("cursor");
				int count = cursor.getInt("estimatedResultCount");
				
				/* A search for meriweather yielded:
				 * {"responseData": {"results":[{"GsearchResultClass":"GwebSearch","unescapedUrl":"http://www.meriweather.com/","url":"http://www.meriweather.com/","visibleUrl":"www.meriweather.com","cacheUrl":"http://www.google.com/search?q\u003dcache:QCPH_oVFGocJ:www.meriweather.com","title":"\u003cb\u003eMeriweather\u0026#39;s\u003c/b\u003e Photo Gallery","titleNoFormatting":"Meriweather\u0026#39;s Photo Gallery","content":"Jerome \u003cb\u003eMeriweather\u0026#39;s\u003c/b\u003e Photo Gallery. Just a few pictures of my work and comments   as a United Airlines Photographer."},{"GsearchResultClass":"GwebSearch","unescapedUrl":"http://www.merriweathermusic.com/","url":"http://www.merriweathermusic.com/","visibleUrl":"www.merriweathermusic.com","cacheUrl":"http://www.google.com/search?q\u003dcache:MwSkYM8nm8IJ:www.merriweathermusic.com","title":"Merriweather Post Pavilion","titleNoFormatting":"Merriweather Post Pavilion","content":"At Merriweather - HFStival 2010 • Third Eye Blind • Billy Idol • Everclear • Ed   Kowalczyk from Live • Presidents of the United States of America • Fuel \u003cb\u003e...\u003c/b\u003e"},{"GsearchResultClass":"GwebSearch","unescapedUrl":"http://www.myspace.com/meriwether","url":"http://www.myspace.com/meriwether","visibleUrl":"www.myspace.com","cacheUrl":"http://www.google.com/search?q\u003dcache:mg7JvIOSUQcJ:www.myspace.com","title":"Meriwether [twitter @meriwether1] on MySpace Music - Free \u003cb\u003e...\u003c/b\u003e","titleNoFormatting":"Meriwether [twitter @meriwether1] on MySpace Music - Free ...","content":"MySpace Music profile for Meriwether [twitter @meriwether1]. Download Meriwether   [twitter @meriwether1] Rock / Alternative / Soul music singles, \u003cb\u003e...\u003c/b\u003e"},{"GsearchResultClass":"GwebSearch","unescapedUrl":"http://en.wikipedia.org/wiki/Brandon_Meriweather","url":"http://en.wikipedia.org/wiki/Brandon_Meriweather","visibleUrl":"en.wikipedia.org","cacheUrl":"http://www.google.com/search?q\u003dcache:lcfTHafxuYMJ:en.wikipedia.org","title":"Brandon \u003cb\u003eMeriweather\u003c/b\u003e - Wikipedia, the free encyclopedia","titleNoFormatting":"Brandon Meriweather - Wikipedia, the free encyclopedia","content":"Brandon \u003cb\u003eMeriweather\u003c/b\u003e (born January 14, 1984 in Apopka, Florida) is an American   football safety for the New England Patriots of the National Football League.   \u003cb\u003e...\u003c/b\u003e"}],"cursor":{"pages":[{"start":"0","label":1},{"start":"4","label":2},{"start":"8","label":3},{"start":"12","label":4},{"start":"16","label":5},{"start":"20","label":6},{"start":"24","label":7},{"start":"28","label":8}],"estimatedResultCount":"13700","currentPageIndex":0,"moreResultsUrl":"http://www.google.com/search?oe\u003dutf8\u0026ie\u003dutf8\u0026source\u003duds\u0026start\u003d0\u0026hl\u003den\u0026q\u003dmeriweather"}}, "responseDetails": null, "responseStatus": 200}
				 */
				cache.put(term, count);
				newCache.put(term, count);
				updateCache(CACHE_FILE_NAME);
				result = count;
			}
			finally {
				if (stream != null) {
					try {
						stream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		return result;
	}

	public double normalizedGoogleDistance(String term1, String term2) throws Exception {
		System.out.println("scoring " + term1 + " and " + term2);

		int min = numResultsFromWeb(term1);
		int max = numResultsFromWeb(term2);
		int both = numResultsFromWeb(term1 + " " + term2);

		// if necessary, swap the min and max
		if (min < max) {
			int temp = max;
			max = min;
			min = temp;
		}

		double distance = 0.0;
		if (min != 0.0 && both != 0.0) {
			distance =
				(Math.log(max) - Math.log(both)) / (logN - Math.log(min));
		}
		return distance;
	}

//	public double score(List<String> words, List<String> keys) {
////	public double score(Review r, Mean m) {
//		double sum = 0.0;
//		for (String s : words) {
//			for (String against : keys) {
//				double score = normalizedGoogleDistance(s, against);
//				sum += score;
//			}
//		}
//		return sum;
//	}

}
