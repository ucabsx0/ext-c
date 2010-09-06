package nz.ac.vuw.ecs.kcassell.similarity;

/*
 * The code here is based on code retrieved from 
 * http://reviewclustering.googlecode.com/svn
*/

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import nz.ac.vuw.ecs.kcassell.utils.RefactoringConstants;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * This implements the Normalized Google Distance (NGD) as described in
 * R.L. Cilibrasi and P.M.B. Vitanyi, "The Google Similarity Distance",
 * IEEE Trans. Knowledge and Data Engineering, 19:3(2007), 370 - 383
 */

public class GoogleDistanceCalculator
implements DistanceCalculatorIfc<String> {
	
	/** A site that will return the number of matches, among other things. */
	private static final String SEARCH_SITE_PREFIX =
		"http://ajax.googleapis.com/ajax/services/search/web?v=1.0&";
	
	protected static final String CACHE_FILE_NAME = "google.cache";

	static int counter = 0;

	/** The logarithm of a number that is (hopefully) greater than or equal
	 *  to the (unpublished) indexed number of Google documents.
	 *  http://googleblog.blogspot.com/2008/07/we-knew-web-was-big.html
	 *  puts this at a trillion or more.  */
	protected final static double logN = Math.log(1.0e12);

	Map<String, Integer> cache = new HashMap<String, Integer>();
	
	/** Holds the new terms we entered (these are also in the cache) */
	Map<String, Integer> newCache = new HashMap<String, Integer>();

	public GoogleDistanceCalculator() throws NumberFormatException, IOException {
		cache = setupCache(CACHE_FILE_NAME);
	}

	protected Map<String, Integer> setupCache(String filename)
			throws NumberFormatException, IOException {

		File cacheFile = new File(filename);

		if (cacheFile.canRead()) {
			BufferedReader reader = new BufferedReader(new FileReader(filename));

			Map<String, Integer> cache = new HashMap<String, Integer>();
			String line;

			while ((line = reader.readLine()) != null) {
				int lastSpaceIndex = line.lastIndexOf(' ');
				String token = line.substring(0, lastSpaceIndex);
				int count = Integer
						.parseInt(line.substring(lastSpaceIndex + 1));
				cache.put(token, count);
			}

			reader.close();
		}
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
//			try {
//				// TODO why sleep?
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//			}

			String searchTerm = term.replaceAll(" ", "+");
			URL url = null;
			InputStream stream = null;
			try {
				String urlString = SEARCH_SITE_PREFIX + "q=" + searchTerm + " ";
				/*
				 * Example queries:
					cassell: q=cassell
					keith cassell: q=keith+cassell
					"keith cassell": q=%22keith+cassell%22
					"keith cassell" betweenness: q=%22keith+cassell%22+betweenness
				 */
				url = new URL(urlString);
				URLConnection connection = url.openConnection();
				connection.setConnectTimeout(2000);
				stream = connection.getInputStream();
				InputStreamReader inputReader = new InputStreamReader(stream);
				BufferedReader bufferedReader = new BufferedReader(inputReader);

				JSONObject json = new JSONObject(new JSONTokener(bufferedReader));
				JSONObject responseData = json.getJSONObject("responseData");
				JSONObject cursor = responseData.getJSONObject("cursor");
				int count = 0;
				
				try {
					count = cursor.getInt("estimatedResultCount");
				} catch (JSONException e) {
					// exception will be thrown when no matches are found
					count = 0;
				}
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

	/**
	 * Calculates the normalized Google Distance (NGD) between the two terms
	 * specified.  NOTE: this number can change between runs, because it is
	 * based on the number of web pages found by Google, which changes.
	 * @return a number from 0 (minimally distant) to 1 (maximally distant),
	 *   unless an exception occurs in which case, it is negative
	 *   (RefactoringConstants.UNKNOWN_DISTANCE)
	 */
	public Double calculateDistance(String term1, String term2) {
		// System.out.println("scoring " + term1 + " and " + term2);
		double distance = RefactoringConstants.UNKNOWN_DISTANCE.doubleValue();

		try {
			int min = numResultsFromWeb(term1);
			int max = numResultsFromWeb(term2);
			int both = numResultsFromWeb(term1 + "+" + term2);

			// if necessary, swap the min and max
			if (max < min) {
				int temp = max;
				max = min;
				min = temp;
			}

			if (min > 0.0 && both > 0.0) {
				distance =
					(Math.log(max) - Math.log(both)) / (logN - Math.log(min));
			} else {
				distance = 1.0;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
