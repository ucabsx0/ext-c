package nz.ac.vuw.ecs.kcassell.similarity;

/*
Retrieved from http://www.google.com/codesearch/p?hl=en#LjJS3knaY94/trunk/src/metrics/GoogleDistance.java&q=googledistance%20lang:java&sa=N&cd=1&ct=rc
	http://reviewclustering.googlecode.com/svn
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
	static int counter = 0;

	protected final static double logM = Math.log(8058044651.0); // http://ryouready.wordpress.com/2009/01/12/r-normalized-google-distance-ngd-in-r-part-ii/

	protected static final String cacheFilename = "google.cache";

	Map<String, Integer> cache;
	Map<String, Integer> newCache; // Holds the new terms we entered (these are
									// also in the cache)

	public GoogleDistance() throws NumberFormatException, IOException {
		cache = setupCache(cacheFilename);
		newCache = new HashMap<String, Integer>();
	}

	protected Map<String, Integer> setupCache(String filename)
			throws NumberFormatException, IOException {
		Map<String, Integer> cache = new HashMap<String, Integer>();

		BufferedReader br = new BufferedReader(new FileReader(filename));

		String line;
		while ((line = br.readLine()) != null) {
			int lastSpaceIdx = line.lastIndexOf(' ');
			cache.put(line.substring(0, lastSpaceIdx), Integer.parseInt(line
					.substring(lastSpaceIdx + 1)));
		}

		br.close();

		return cache;
	}

	protected void outputCache(String filename) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(filename, true));

		for (Map.Entry<String, Integer> e : newCache.entrySet())
			bw.append(e.getKey() + " " + e.getValue() + "\n");

		bw.close();

		newCache = new HashMap<String, Integer>();
	}

	protected int numResults(String term) {
		if (cache.containsKey(term))
			return cache.get(term);

		try {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}

			URL url = new URL(
					"http://ajax.googleapis.com/ajax/services/search/web?v=1.0&"
							+ "q=" + term.replaceAll(" ", "+") + " ");
			URLConnection connection = url.openConnection();
			InputStream is = connection.getInputStream();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(is));

			JSONObject json = new JSONObject(new JSONTokener(reader));
			int count = json.getJSONObject("responseData").getJSONObject(
					"cursor").getInt("estimatedResultCount");
			
			/* A search for meriweather yielded:
			 * {"responseData": {"results":[{"GsearchResultClass":"GwebSearch","unescapedUrl":"http://www.meriweather.com/","url":"http://www.meriweather.com/","visibleUrl":"www.meriweather.com","cacheUrl":"http://www.google.com/search?q\u003dcache:QCPH_oVFGocJ:www.meriweather.com","title":"\u003cb\u003eMeriweather\u0026#39;s\u003c/b\u003e Photo Gallery","titleNoFormatting":"Meriweather\u0026#39;s Photo Gallery","content":"Jerome \u003cb\u003eMeriweather\u0026#39;s\u003c/b\u003e Photo Gallery. Just a few pictures of my work and comments   as a United Airlines Photographer."},{"GsearchResultClass":"GwebSearch","unescapedUrl":"http://www.merriweathermusic.com/","url":"http://www.merriweathermusic.com/","visibleUrl":"www.merriweathermusic.com","cacheUrl":"http://www.google.com/search?q\u003dcache:MwSkYM8nm8IJ:www.merriweathermusic.com","title":"Merriweather Post Pavilion","titleNoFormatting":"Merriweather Post Pavilion","content":"At Merriweather - HFStival 2010 • Third Eye Blind • Billy Idol • Everclear • Ed   Kowalczyk from Live • Presidents of the United States of America • Fuel \u003cb\u003e...\u003c/b\u003e"},{"GsearchResultClass":"GwebSearch","unescapedUrl":"http://www.myspace.com/meriwether","url":"http://www.myspace.com/meriwether","visibleUrl":"www.myspace.com","cacheUrl":"http://www.google.com/search?q\u003dcache:mg7JvIOSUQcJ:www.myspace.com","title":"Meriwether [twitter @meriwether1] on MySpace Music - Free \u003cb\u003e...\u003c/b\u003e","titleNoFormatting":"Meriwether [twitter @meriwether1] on MySpace Music - Free ...","content":"MySpace Music profile for Meriwether [twitter @meriwether1]. Download Meriwether   [twitter @meriwether1] Rock / Alternative / Soul music singles, \u003cb\u003e...\u003c/b\u003e"},{"GsearchResultClass":"GwebSearch","unescapedUrl":"http://en.wikipedia.org/wiki/Brandon_Meriweather","url":"http://en.wikipedia.org/wiki/Brandon_Meriweather","visibleUrl":"en.wikipedia.org","cacheUrl":"http://www.google.com/search?q\u003dcache:lcfTHafxuYMJ:en.wikipedia.org","title":"Brandon \u003cb\u003eMeriweather\u003c/b\u003e - Wikipedia, the free encyclopedia","titleNoFormatting":"Brandon Meriweather - Wikipedia, the free encyclopedia","content":"Brandon \u003cb\u003eMeriweather\u003c/b\u003e (born January 14, 1984 in Apopka, Florida) is an American   football safety for the New England Patriots of the National Football League.   \u003cb\u003e...\u003c/b\u003e"}],"cursor":{"pages":[{"start":"0","label":1},{"start":"4","label":2},{"start":"8","label":3},{"start":"12","label":4},{"start":"16","label":5},{"start":"20","label":6},{"start":"24","label":7},{"start":"28","label":8}],"estimatedResultCount":"13700","currentPageIndex":0,"moreResultsUrl":"http://www.google.com/search?oe\u003dutf8\u0026ie\u003dutf8\u0026source\u003duds\u0026start\u003d0\u0026hl\u003den\u0026q\u003dmeriweather"}}, "responseDetails": null, "responseStatus": 200}
			 */

			is.close();

			cache.put(term, count);
			newCache.put(term, count);

			if (counter++ == 20) {
				System.out.println("Outputted!");
				outputCache(cacheFilename);
				counter = 0;
			}

			return count;
		} catch (IOException ioe) {
		} catch (JSONException e) {
		}

		throw new IllegalArgumentException("Soemthign bad");
	}

	public double termDist(String term, String against) {
		System.out.println("scoring " + term + " and " + against);

		int min = numResults(term);
		int max = numResults(against);
		int both = numResults(term + " " + against);

		if (min < max) {
			int temp = max;
			max = min;
			min = temp;
		}

		if (min == 0.0 || both == 0.0)
			return 0.0;

		return (Math.log(max) - Math.log(both)) / (logM - Math.log(min));
	}

//	public double score(Review r, Mean m) {
//		double sum = 0.0;
//		for (String s : r.words) {
//			for (String against : m.prob.keySet()) {
//				double score = termDist(s, against);
//				sum += score;
//			}
//		}
//		return sum;
//	}

}
