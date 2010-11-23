package nz.ac.vuw.ecs.kcassell.similarity;

import java.util.Set;

import nz.ac.vuw.ecs.kcassell.cluster.MemberCluster;
import nz.ac.vuw.ecs.kcassell.utils.RefactoringConstants;

/**
 * A collection of utilities to calculate distances between groups.
 * In most cases, it will be more efficient to calculate distances
 * once and store them for later access rather than to recalculate
 * using these utilities.
 * @author Keith Cassell
 */
public class ClusterDistanceUtils {

	/**
	 * Returns the smallest distance between any element in cluster1
	 * and any element in cluster2
	 * @param cluster1 a collection of elements
	 * @param cluster2 a collection of elements
	 * @param calc calculates the distance between elements
	 * @return the smallest distance
	 */
	public static double singleLinkDistance(MemberCluster cluster1,
			MemberCluster cluster2,
			DistanceCalculatorIfc<String> calc) {
		double min = RefactoringConstants.MAX_DISTANCE.doubleValue();
		Set<String> elements1 = cluster1.getElements();
		Set<String> elements2 = cluster2.getElements();
		
		for (String element1 : elements1) {
			for (String element2 : elements2) {
				Number distance = calc.calculateDistance(element1, element2);
				min = Math.min(min, distance.doubleValue());
			}
		}
		return min;
	}

	/**
	 * Returns the smallest distance between any element in cluster1
	 * and any element in cluster2
	 * @param cluster1 a collection of elements
	 * @param cluster2 a collection of elements
	 * @param matrix contains the distances between elements
	 * @return the smallest distance
	 */
	public static double singleLinkDistance(MemberCluster cluster1,
			MemberCluster cluster2,
			DistanceMatrix<String> matrix) {
		double min = RefactoringConstants.MAX_DISTANCE.doubleValue();
		Set<String> elements1 = cluster1.getElements();
		Set<String> elements2 = cluster2.getElements();
		
		for (String element1 : elements1) {
			for (String element2 : elements2) {
				Number distance = matrix.getDistance(element1, element2);
				min = Math.min(min, distance.doubleValue());
			}
		}
		return min;
	}

	/**
	 * Returns the largest distance between any element in cluster1
	 * and any element in cluster2
	 * @param cluster1 a collection of elements
	 * @param cluster2 a collection of elements
	 * @param calc calculates the distance between elements
	 * @return the largest distance
	 */
	public static double completeLinkDistance(MemberCluster cluster1,
			MemberCluster cluster2,
			DistanceCalculatorIfc<String> calc) {
		double max = 0.0;
		Set<String> elements1 = cluster1.getElements();
		Set<String> elements2 = cluster2.getElements();
		
		for (String element1 : elements1) {
			for (String element2 : elements2) {
				Number distance = calc.calculateDistance(element1, element2);
				max = Math.max(max, distance.doubleValue());
			}
		}
		return max;
	}

	/**
	 * Returns the largest distance between any element in cluster1
	 * and any element in cluster2
	 * @param cluster1 a collection of elements
	 * @param cluster2 a collection of elements
	 * @param matrix contains the distances between elements
	 * @return the largest distance
	 */
	public static double completeLinkDistance(MemberCluster cluster1,
			MemberCluster cluster2,
			DistanceMatrix<String> matrix) {
		double max = 0.0;
		Set<String> elements1 = cluster1.getElements();
		Set<String> elements2 = cluster2.getElements();
		
		for (String element1 : elements1) {
			for (String element2 : elements2) {
				Number distance = matrix.getDistance(element1, element2);
				max = Math.max(max, distance.doubleValue());
			}
		}
		return max;
	}

	/**
	 * Returns the average distance between the elements in cluster1
	 * and the elements in cluster2
	 * @param cluster1 a collection of elements
	 * @param cluster2 a collection of elements
	 * @param calc calculates the distance between elements
	 * @return the average distance
	 */
	public static double averageLinkDistance(MemberCluster cluster1,
			MemberCluster cluster2,
			DistanceCalculatorIfc<String> calc) {
		double sum = 0.0;
		int i = 0;
		Set<String> elements1 = cluster1.getElements();
		Set<String> elements2 = cluster2.getElements();
		
		for (String element1 : elements1) {
			for (String element2 : elements2) {
				Number distance = calc.calculateDistance(element1, element2);
				sum += distance.doubleValue();
				i++;
			}
		}
		double average = sum/i;
		return average;
	}

	/**
	 * Returns the average distance between the elements in cluster1
	 * and the elements in cluster2
	 * @param cluster1 a collection of elements
	 * @param cluster2 a collection of elements
	 * @param matrix contains the distances between elements
	 * @return the average distance
	 */
	public static double averageLinkDistance(MemberCluster cluster1,
			MemberCluster cluster2,
			DistanceMatrix<String> matrix) {
		double sum = 0.0;
		int i = 0;
		Set<String> elements1 = cluster1.getElements();
		Set<String> elements2 = cluster2.getElements();
		
		for (String element1 : elements1) {
			for (String element2 : elements2) {
				Number distance = matrix.getDistance(element1, element2);
				sum += distance.doubleValue();
				i++;
			}
		}
		double average = sum/i;
		return average;
	}
}
