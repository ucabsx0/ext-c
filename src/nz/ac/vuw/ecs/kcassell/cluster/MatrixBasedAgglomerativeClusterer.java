/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
Copyright (c) 2010, Keith Cassell
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following 
      disclaimer in the documentation and/or other materials
      provided with the distribution.
    * Neither the name of the Victoria University of Wellington
      nor the names of its contributors may be used to endorse or
      promote products derived from this software without specific
      prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package nz.ac.vuw.ecs.kcassell.cluster;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphLink;
import nz.ac.vuw.ecs.kcassell.callgraph.JavaCallGraph;
import nz.ac.vuw.ecs.kcassell.logging.UtilLogger;
import nz.ac.vuw.ecs.kcassell.similarity.CzibulaDistanceCalculator;
import nz.ac.vuw.ecs.kcassell.similarity.DistanceCalculatorEnum;
import nz.ac.vuw.ecs.kcassell.similarity.DistanceCalculatorIfc;
import nz.ac.vuw.ecs.kcassell.similarity.DistanceMatrix;
import nz.ac.vuw.ecs.kcassell.similarity.IdentifierDistanceCalculator;
import nz.ac.vuw.ecs.kcassell.similarity.IntraClassDistanceCalculator;
import nz.ac.vuw.ecs.kcassell.similarity.LevenshteinDistanceCalculator;
import nz.ac.vuw.ecs.kcassell.similarity.SimonDistanceCalculator;
import nz.ac.vuw.ecs.kcassell.similarity.VectorSpaceModelCalculator;
import nz.ac.vuw.ecs.kcassell.utils.ApplicationParameters;
import nz.ac.vuw.ecs.kcassell.utils.EclipseUtils;
import nz.ac.vuw.ecs.kcassell.utils.ParameterConstants;
import nz.ac.vuw.ecs.kcassell.utils.RefactoringConstants;

import org.eclipse.jdt.core.JavaModelException;

import edu.uci.ics.jung.algorithms.shortestpath.MinimumSpanningForest;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;

/**
 * An agglomerative clusterer that makes use of a distance matrix to determine
 * which objects to combine/cluster.
 * @author Keith
 *
 */
public class MatrixBasedAgglomerativeClusterer implements ClustererIfc<String> {
	
	/** A constant to indicate that agglomeration is via complete link clustering.
	 * @see Jain, Murphy, and Flynn, "Data clustering: a review",
	 * ACM Computing Surveys, 1999	 */
	public static String COMPLETE_LINK = "complete";
	
	/** A constant to indicate that agglomeration is via single link clustering.
	 * @see Jain, Murphy, and Flynn, "Data clustering: a review",
	 * ACM Computing Surveys, 1999	 */
	public static String SINGLE_LINK = "single";
	
	/** Calculates distances between nodes. */
	protected DistanceCalculatorIfc<String> distanceCalculator = null;
	
	/** Keeps track of how many clustering steps have occurred. */ 
	protected int previousIteration = 0;
	
	/** The distances between the existing clusters */
	protected DistanceMatrix<String> distanceMatrix = null;
	
	/** This keeps track of the clusters that have been seen.  The key
	 * is the cluster name; the value is the cluster.  "Elements"
	 * (clusters of one) will have a handle key and a null value. */
	protected HashMap<String, MemberCluster> clusterHistory =
		new HashMap<String, MemberCluster>();

    protected static final UtilLogger logger =
    	new UtilLogger("MatrixBasedAgglomerativeClusterer");
    
    /**
     * The name of the file that contains
	 * one member per line.  The first token is the member handle, and the 
	 * remaining tokens are the stemmed words found in identifiers and comments.
     */
    private static String vectorSpaceModelInputFile = null;
    // TODO this is temporary until we create the vsm inputs at run time

    /**
     * For use by subclasses.
     */
    protected MatrixBasedAgglomerativeClusterer() {
    }
    
    /**
     * Given a list of (nonclustered) objects and a calculator to calculate
     * the distances between them, initialize the clusterer by building
     * the distance matrix.
     * @param elements a collection of things to be clustered
     * @param calc calculates the distances between objects
     */
	public MatrixBasedAgglomerativeClusterer(List<String> elements,
			DistanceCalculatorIfc<String> calc) {
		distanceCalculator = calc;
		for (String element : elements) {
			clusterHistory.put(element, null);
		}
		buildDistanceMatrix(elements);
		logger.info(distanceMatrix.toString());
	}

	/**
	 * Use the distance calculator to fill in the distance matrix
	 * for the elements provided.
	 * @param elements usually the handles for the class members
	 */
	protected void buildDistanceMatrix(List<String> elements) {
		distanceMatrix = new DistanceMatrix<String>(elements);
		for (int row = 0; row < elements.size(); row++) {
			String obj1 = elements.get(row);
//			clusterHistory.put(obj1, null);	// "Cluster" of one element
			for (int col = 0; col <= row; col++) {
				String obj2 = elements.get(col);
				Number distance = calculateDistance(obj1, obj2);
				distanceMatrix.setDistance(obj1, obj2, distance);
			}
		}
	}

	public DistanceMatrix<String> getDistanceMatrix() {
		return distanceMatrix;
	}

	/**
	 * @return the identifiers of the clusters
	 */
	public Collection<String> getClusters() {
		return distanceMatrix.getHeaders();
	}

	public static String getVectorSpaceModelInputFile() {
		return vectorSpaceModelInputFile;
	}

	public static void setVectorSpaceModelInputFile(String vectorSpaceModelInputFile) {
		MatrixBasedAgglomerativeClusterer.vectorSpaceModelInputFile = vectorSpaceModelInputFile;
	}

	/**
	 * @return the clusters
	 */
	public Collection<MemberCluster> getMemberClusters() {
		List<String> headers = distanceMatrix.getHeaders();
		ArrayList<MemberCluster> memberClusters = new ArrayList<MemberCluster>();
		
		for (String header : headers) {
			MemberCluster cluster = clusterHistory.get(header);
			
			// If a single element, convert it into a cluster
			if (cluster == null) {
				cluster = new MemberCluster();
				cluster.addElement(header);
			}
			memberClusters.add(cluster);
		}
		return memberClusters;
	}

    /**
	 * @return the clusterHistory
	 */
	public HashMap<String, MemberCluster> getClusterHistory() {
		return clusterHistory;
	}

	/**
     * Form clusters by combining nodes.  Quit when there is a single cluster
     * @return the cluster of everything.
     */
	public MemberCluster getSingleCluster() {
		MemberCluster cluster = null;
		try {
			while (continueClustering()) {
				cluster = clusterOnce();
				previousIteration++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cluster;
	}

    /**
     * Form clusters by combining nodes.  Two objects should be combined
     * for each iteration.  The total number of cluster steps that should be
     * performed is determined by a user preference/parameter.  By default,
     * everything will be put into a single cluster.
     * @return a collection of all clusters (some will be single objects)
     */
	public Collection<String> cluster() {
		Collection<String> clusters = null;
		List<String> headers = distanceMatrix.getHeaders();
		int numHeaders = headers.size();
		
		if (numHeaders > 0) {
			ApplicationParameters params =
				ApplicationParameters.getSingleton();
			int iterations = params.getIntParameter(
					ParameterConstants.AGGLOMERATION_CLUSTERS_KEY,
					numHeaders - 1);
			clusters = cluster(iterations);
		}
		return clusters;
	}

    /**
     * Form clusters by combining nodes.  Two nodes should be combined
     * for each iteration.
     * @param iteration the total number of cluster steps that should be
     * performed
     * @return a collection of all clusters (some will be single nodes)
     */
	public Collection<String> cluster(int iteration) {
		int numIterations = iteration - previousIteration;

		for (int i = 0; i < numIterations && continueClustering(); i++) {
			clusterOnce();
			previousIteration++;
		}	// for
		return getClusters();
	}

	/**
	 * Add a new level of clustering
	 */
	protected MemberCluster clusterOnce() {
		Distance<String> nearest = distanceMatrix.findNearest();
		MemberCluster cluster = createCluster(nearest);
		modifyMatrix(cluster);
		return cluster;
	}

	/**
	 * Creates a cluster from two identifiers (each of which may represent
	 * one or more elements).
	 * @param neighbors the two objects
	 * @return the new cluster
	 */
	protected MemberCluster createCluster(Distance<String> neighbors) {
		MemberCluster cluster = new MemberCluster();
		String near1 = neighbors.getFirst();
		String near2 = neighbors.getSecond();
		logger.info("createCluster from " + near1 + ", " + near2);
		addChildToCluster(cluster, near1);
		addChildToCluster(cluster, near2);
		String clusterName =
			nameCluster(cluster, (near1.compareTo(near2) < 0) ? near1 : near2);
		clusterHistory.put(clusterName, cluster);
		return cluster;
	}

	/**
	 * Adds to a cluster based on an identifier (which may represent
	 * one or more elements).
	 * @param cluster the existing cluster
	 * @param childName the identifier of the addition
	 * @return the enlarged cluster
	 */
	protected void addChildToCluster(MemberCluster cluster, String childName) {
		MemberCluster childCluster = clusterHistory.get(childName);
		if (childCluster == null) {
			cluster.addElement(childName);
		} else {
			cluster.addCluster(childCluster);
		}
	}

	/**
	 * Create a name for the new cluster by combining the provided name
	 * with the iteration number
	 * @param cluster the cluster to be named
	 * @param name the name to use as the basis of the new name
	 */
	protected String nameCluster(MemberCluster cluster, String name) {
		// Make the name show the iteration the cluster was made
		// Parse the old name to remove the old number - avoid "+26+27"
		int indexPlus = name.indexOf("+");
		if (indexPlus >= 0) {
			name = name.substring(0, indexPlus);
		}
		name += "+" + (previousIteration + 1);
		cluster.setClusterName(name);
		return name;
	}

	/**
	 * Revises the distanceMatrix after a clustering step by removing the rows
	 * and columns for the elements that were merged, and creating a row for
	 * the newly formed cluster
	 * @param cluster
	 * @return
	 */
	protected DistanceMatrix<String> modifyMatrix(MemberCluster cluster) {
		String clusterName = cluster.getClusterName();
		List<String> headers = getNewHeaders(cluster);
		DistanceMatrix<String> newMatrix = new DistanceMatrix<String>(headers);
		
		// Copy over old matrix values, except for last row, which is new.
		int numElements = headers.size();
		for (int row = 0; row < numElements - 1; row++) {
			String obj1 = headers.get(row);
			for (int col = 0; col <= row; col++) {
				String obj2 = headers.get(col);
				Number distance = distanceMatrix.getDistance(obj1, obj2);
				newMatrix.setDistance(obj1, obj2, distance);
			}
		}
		// Fill in the values for the newly formed cluster
		for (int col = 0; col < numElements; col++) {
			String obj2 = headers.get(col);
			Number distance = calculateDistance(clusterName, obj2);
			newMatrix.setDistance(clusterName, obj2, distance);
		}
		distanceMatrix = newMatrix;
		return distanceMatrix;
	}

	/**
	 * Returns the names of the current clusters
	 * @param cluster the most recently created cluster
	 * @return the names of the current clusters
	 */
	protected List<String> getNewHeaders(MemberCluster cluster) {
		String clusterName = cluster.getClusterName();
		Set<?> children = cluster.getChildren();
		List<String> headers =
			new ArrayList<String>(distanceMatrix.getHeaders());
		
		// Remove recently clustered objects from distance matrix
		for (Object child: children) {
			if (child instanceof MemberCluster) {
				MemberCluster childCluster = (MemberCluster)child;
				headers.remove(childCluster.getClusterName());
			} else {
				headers.remove(child);
			}
		}
		headers.add(clusterName);
		return headers;
	}

	protected boolean continueClustering() {
		// TODO write cohesion-based stopping criterion;
		// ultimately have a user-supplied command object
		List<String> headers = distanceMatrix.getHeaders();
		return headers.size() > 1;
	}
	
	/**
	 * Clusters the members of the indicated class using the distance
	 * calculator specified in the user preferences/parameters.
	 * @param classHandle the Eclipse handle of the class to cluster
	 * @return the fully agglomerated cluster
	 */
	public static MemberCluster cluster(String classHandle) {
		MemberCluster cluster = null;
		try {
			DistanceCalculatorIfc<String> calc =
				setUpSpecifiedCalculator(classHandle);
			List<String> memberHandles =
				EclipseUtils.getFilteredMemberHandles(classHandle);
			MatrixBasedAgglomerativeClusterer clusterer =
				new MatrixBasedAgglomerativeClusterer(memberHandles, calc);

			cluster = clusterer.getSingleCluster();
			System.out.println("Final cluster:\n" + cluster.toNestedString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cluster;
	}
	
	/**
	 * Calculates the distance between the objects corresponding
	 * to the identifiers.  When an identifier corresponds to a cluster,
	 * we calculate all distances for the objects within that cluster
	 * and return the smallest.  TODO make configurable (single link, etc.).
	 * We ignore the case of the letters in the comparison.
	 * @return between 0 (identical) and 1 (completely different)
	 */
	protected Number calculateDistance(String s1, String s2) {
		Double result = 1.0;

		if (s1 != null && s2 != null) {
			MemberCluster cluster1 = clusterHistory.get(s1);
			MemberCluster cluster2 = clusterHistory.get(s2);
			
			// Cluster1 is a single element (handle)
			if (cluster1 == null) {
				// Cluster1 and cluster2 are single elements (handles)
				if (cluster2 == null) {
					String name1 = EclipseUtils.getNameFromHandle(s1);
					String name2 = EclipseUtils.getNameFromHandle(s2);
					Number nDistance = distanceCalculator.calculateDistance(name1, name2);
					if (nDistance != null) {
						result = nDistance.doubleValue();
					} else {
						System.err.println("s1 =" + s1);
					}
				} else { // cluster2 is a true cluster
					Set<String> ids = cluster2.getElements();
					result = getSmallestDistanceToGroup(s1, ids, result);
				}
			}	// if (cluster1 == null)
			// Cluster1 is a true cluster
			else {
				// cluster2 is a single element
				if (cluster2 == null) {
					Set<String> ids = cluster1.getElements();
					result = getSmallestDistanceToGroup(s2, ids, result);
				}
				// Both s1 and s2 are clusters
				else {
					Set<String> ids1 = cluster1.getElements();
					for (String id1 : ids1) {
						Set<String> ids2 = cluster2.getElements();
						Double distance =
							getSmallestDistanceToGroup(id1, ids2, result);
						if (distance.compareTo(result) < 0) {
							result = distance;
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * Get the smallest distance found from the element specified
	 * to any member of the group.
	 * @param s1 the single element
	 * @param ids the group
	 * @param result the smallest distance so far
	 * @return the smallest distance
	 */
	protected Double getSmallestDistanceToGroup(String s1,
			Collection<String> ids, Double result) {
		for (String id : ids) {
			if (id != null) {
				String name1 = EclipseUtils.getNameFromHandle(s1);
				String name2 = EclipseUtils.getNameFromHandle(id);
				Double distance =
					distanceCalculator.calculateDistance(name1, name2)
						.doubleValue();
				if (distance.compareTo(result) < 0) {
					result = distance;
				}
			}
		}
		return result;
	}



	protected static DistanceCalculatorIfc<String> setUpSpecifiedCalculator(
			String classHandle) throws Exception {
		ApplicationParameters parameters =
			ApplicationParameters.getSingleton();
		String sCalc =
			parameters.getParameter(ParameterConstants.CALCULATOR_KEY,
					      DistanceCalculatorEnum.Levenshtein.toString());
		
		DistanceCalculatorIfc<String> calc = null;
		if (DistanceCalculatorEnum.Czibula.toString().equals(sCalc)) {
			calc = setUpCzibulaClustering(classHandle);
		} else if (DistanceCalculatorEnum.Identifier.toString().equals(sCalc)) {
			calc = setUpIdentifierClustering();
		} else if (DistanceCalculatorEnum.IntraClass.toString().equals(sCalc)) {
			calc = setUpIntraClassDistanceClustering(classHandle);
		} else if (DistanceCalculatorEnum.Levenshtein.toString().equals(sCalc)) {
			calc = setUpLevenshteinClustering();
		} else if (DistanceCalculatorEnum.Simon.toString().equals(sCalc)) {
			calc = setUpSimonClustering(classHandle);
		} else if (DistanceCalculatorEnum.VectorSpaceModel.toString().equals(sCalc)) {
			calc = setUpVectorSpaceModelClustering(classHandle);
		}
		return calc;
	}
	
	private static DistanceCalculatorIfc<String> setUpVectorSpaceModelClustering(
			String classHandle) throws IOException {
		VectorSpaceModelCalculator calc =
			new VectorSpaceModelCalculator(vectorSpaceModelInputFile);
		return null;
	}

	/**
	 * Set up a IdentifierDistanceCalculator
	 * @return the calculator
	 */
	protected static DistanceCalculatorIfc<String> setUpIdentifierClustering() {
		DistanceCalculatorIfc<String> calc =
			new IdentifierDistanceCalculator();
		return calc;
	}

	/**
	 * Set up a LevenshteinDistanceCalculator
	 * @return the calculator
	 */
	protected static DistanceCalculatorIfc<String> setUpLevenshteinClustering() {
		DistanceCalculatorIfc<String> calc =
			new LevenshteinDistanceCalculator();
		return calc;
	}

	/**
	 * Set up an IntraClassDistanceCalculator
	 * @param classHandle the Eclipse handle for the class to be clustered
	 * @return the calculator
	 * @throws JavaModelException
	 */
	protected static DistanceCalculatorIfc<String>
	setUpIntraClassDistanceClustering(String classHandle)
			throws JavaModelException {
		JavaCallGraph callGraph =
			new JavaCallGraph(classHandle, EdgeType.UNDIRECTED);
		DistanceCalculatorIfc<String> calc =
			new IntraClassDistanceCalculator(callGraph);
		return calc;
	}

	/**
	 * Set up a CzibulaDistanceCalculator
	 * @param classHandle the Eclipse handle for the class to be clustered
	 * @return the calculator
	 * @throws JavaModelException
	 */
	protected static DistanceCalculatorIfc<String>
	setUpCzibulaClustering(String classHandle)
			throws JavaModelException {
		JavaCallGraph callGraph =
			new JavaCallGraph(classHandle, EdgeType.UNDIRECTED);
		DistanceCalculatorIfc<String> calc =
			new CzibulaDistanceCalculator(callGraph);
		return calc;
	}
	
	/**
	 * Set up a SimonDistanceCalculator
	 * @param classHandle the Eclipse handle for the class to be clustered
	 * @return the calculator
	 * @throws JavaModelException
	 */
	protected static DistanceCalculatorIfc<String>
	setUpSimonClustering(String classHandle)
			throws JavaModelException {
		JavaCallGraph callGraph =
			new JavaCallGraph(classHandle, EdgeType.UNDIRECTED);
		DistanceCalculatorIfc<String> calc =
			new SimonDistanceCalculator(callGraph );
		return calc;
	}
	
	/**
	 * Creates a minimum spanning forest from the matrix.
	 * @return the forest
	 */
	public Forest<String, CallGraphLink> createMinimumSpanningForest() {
	    Graph<String, CallGraphLink> graph =
	    	new SparseMultigraph<String, CallGraphLink>();
	    List<String> headers = distanceMatrix.getHeaders();
	    int size = headers.size();
	    
	    // Create all vertices
		for (int i = 0; i < size; i++) {
			String member1 = headers.get(i);
			graph.addVertex(member1);
		}
		
		CallGraphLink.CallGraphLinkFactory linkFactory =
			new CallGraphLink.CallGraphLinkFactory();
		HashMap<CallGraphLink, Double> edgeWeights =
			new HashMap<CallGraphLink, Double>();

		// Create all edges
		for (int i = 0; i < size; i++) {
			String member1 = headers.get(i);
			for (int j = 0; j < i; j++) {
				String member2 = headers.get(j);
				Number distance = distanceMatrix.getDistance(member1, member2);
				
				if (!RefactoringConstants.UNKNOWN_DISTANCE.equals(distance)
						&& !distance.equals(1.0)) {
					//TODO parameterize - here, assuming 1.0 is the max distance
					// (as for Jaccard distance)
					CallGraphLink link = linkFactory.create();
					edgeWeights.put(link, distance.doubleValue());
					link.setWeight(distance);
					graph.addEdge(link, member1, member2);
				}
			}
		}	// create edges
		
        MinimumSpanningForest<String, CallGraphLink> forest = 
        	new MinimumSpanningForest<String, CallGraphLink>(graph,
        		new DelegateForest<String, CallGraphLink>(),
        		null,
        		edgeWeights);
        
        Forest<String, CallGraphLink> minSpanningForest = forest.getForest();
        return minSpanningForest;
	}

}
