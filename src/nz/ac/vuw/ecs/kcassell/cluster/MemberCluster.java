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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import nz.ac.vuw.ecs.kcassell.utils.EclipseUtils;
import nz.ac.vuw.ecs.kcassell.utils.StringUtils;

/**
 * This represents clusters of a class's members. A cluster consists of some
 * combination of 0 or more String identifiers for Eclipse member handles and 0
 * or more subclusters.
 * 
 * @author kcassell
 */
public class MemberCluster implements ClusterIfc<String> {

	/** The subcomponents are either string elements or MemberClusters */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Set children = new TreeSet(new ClusterComparator());

	/**
	 * The number of elements are in the cluster (including those in
	 * subclusters).
	 */
	protected int elementCount = 0;

	/** A user comment. It can be anything, e.g. a distance. */
	protected String comment = "";

	protected String clusterName = "";

	/** The distance between the subclusters. */
	protected Double distance = Double.MIN_VALUE;

	/**
	 * @return the clusterName
	 */
	public String getClusterName() {
		return clusterName;
	}

	/**
	 * @param clusterName
	 *            the clusterName to set
	 */
	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@SuppressWarnings("unchecked")
	public void addElement(String element) {
		children.add(element);
		elementCount++;
	}

	@SuppressWarnings("unchecked")
	public void addElements(Collection<String> elements) {
		children.addAll(elements);
		elementCount += elements.size();
	}

	@SuppressWarnings("unchecked")
	public void addCluster(MemberCluster cluster) {
		children.add(cluster);
		elementCount += cluster.getElementCount();
	}

	/**
	 * @return the number of elements in the cluster (including those in
	 *         subclusters.
	 */
	public int getElementCount() {
		return elementCount;
	}

	/** @return the elements in the cluster (including those in subclusters. */
	public Set<String> getElements() {
		Set<String> elements = new HashSet<String>();

		for (Object obj : children) {
			if (obj instanceof String) {
				elements.add((String) obj);
			} else if (obj instanceof MemberCluster) {
				MemberCluster cluster = (MemberCluster) obj;
				Set<String> elements2 = cluster.getElements();
				elements.addAll(elements2);
			}
		}
		return elements;
	}

	/**
	 * Using cluster IDs and a hash table mapping ids to clusters, identify all
	 * the elements in the cluster
	 * 
	 * @param clusterHistory
	 *            maps String ids to MemberClusters. Ids that don't represent
	 *            clusters map to null.
	 * @return the elements in the cluster (including those in subclusters.
	 */
	public Set<String> getElements(HashMap<String, MemberCluster> clusterHistory) {
		Set<String> elements = new HashSet<String>();

		for (Object obj : children) {
			if (obj instanceof String) {
				String sObj = (String) obj;
				MemberCluster cluster = clusterHistory.get(sObj);
				if (cluster == null) {
					elements.add(sObj);
				} else {
					Set<String> elements2 = cluster.getElements();
					elements.addAll(elements2);
				}
			} else if (obj instanceof MemberCluster) {
				MemberCluster cluster = (MemberCluster) obj;
				Set<String> elements2 = cluster.getElements();
				elements.addAll(elements2);
			}
		}
		return elements;
	}

	/**
	 * @return the children
	 */
	public Set<?> getChildren() {
		return children;
	}

	protected Double getClusterIteration() {
		double it = 0.5;
		String name = getClusterName();
		// Makes use of a naming convention oneName+OtherCount
		int indexPlus = name.lastIndexOf("+");
		// If the name of the cluster matches the "+ convention",
		// extract the iteration number
		if (indexPlus >= 0) {
			String sit = name.substring(indexPlus + 1);
			try {
				it = Double.parseDouble(sit);
			} catch (Exception e) {
				// ignore
			}
		}
		return it;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nz.ac.vuw.ecs.kcassell.callgraph.ClusterIfc#toNestedString()
	 */
	public String toNestedString() {
		StringBuffer buf = new StringBuffer();
		toNestedString(0, buf);
		String nestedString = buf.toString();
		return nestedString;
	}

	protected void toNestedString(int indentLevel, StringBuffer buf) {
		// If this is a top level (visible) node, print its name
		if (indentLevel == 0) {
			buf.append(clusterName).append("\n");
		}
		indentLevel++;
		String leadSpaces = StringUtils.SPACES140.substring(0, 2 * indentLevel);

		for (Object component : children) {
			if (component instanceof MemberCluster) {
				buf.append(leadSpaces);
				MemberCluster cluster = (MemberCluster) component;
				String name = cluster.getClusterName();
				// Makes use of a naming convention oneName+OtherCount
				int indexPlus = name.lastIndexOf("+");
				// If the name of the cluster matches the "+ convention",
				// extract the iteration number
				if (indexPlus >= 0) {
					String it = name.substring(indexPlus + 1);
					buf.append("|+" + it + " (" + cluster.comment + ")\n");
				} else {
					buf.append("|+" + name + "\n");
				}
				cluster.toNestedString(indentLevel, buf);
			} else if (component instanceof String) { // element
				buf.append(leadSpaces).append("|-");
				String name = EclipseUtils.getNameFromHandle(component
						.toString());
				buf.append(name).append("\n");
			}
		}
	}

	/**
	 * Generates a string representation of the tree using the Newick/New
	 * Hampshire format. Several software packages can create dendrograms based
	 * on this representation.
	 * 
	 * @see http://evolution.genetics.washington.edu/phylip/newicktree.html
	 */
	public String toNewickString() {
		StringBuffer buf = new StringBuffer();
		HashSet<String> namesSeen = new HashSet<String>();
		toNewickString(0, buf, namesSeen, 1.0);
		buf.append(";\n");
		String nestedString = buf.toString();
		return nestedString;
	}

	protected void toNewickString(int indentLevel, StringBuffer buf,
			HashSet<String> namesSeen, Double parentDistance) {
		String leadSpaces = StringUtils.SPACES140.substring(0, 2 * indentLevel);
		buf.append(leadSpaces).append("(");
		int nextIndent = indentLevel + 1;

		for (Object component : children) {
			buf.append("\n");
			if (component instanceof MemberCluster) {
				MemberCluster cluster = (MemberCluster) component;
				cluster.toNewickString(nextIndent, buf, namesSeen, distance);
			} else if (component instanceof String) { // element
				String name = EclipseUtils.getNameFromHandle(component
						.toString());
				if (namesSeen.contains(name)) {
					name += "_" + namesSeen.size();
					namesSeen.add(name);
				}
				buf.append(leadSpaces).append("  ").append(name);
				// add Newick branch length
				buf.append(":").append(String.format("%.2f", distance));
			}
			buf.append(",");
		}
		int length = buf.length();
		buf.delete(buf.lastIndexOf(","), length); // eliminate the last ","
		buf.append("\n");
		buf.append(leadSpaces).append(") ");
		appendInternalNewickNodeName(buf);
		appendNewickBranchLength(buf, parentDistance);
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(Double distance) {
		this.distance = distance;
	}

	private void appendNewickBranchLength(StringBuffer buf, Double parentDistance) {
		// Double clusteringIteration = getClusterIteration();
		// // We make the branch length proportional to the iteration, although
		// // proportional to the distance measure might be better
		// buf.append(":").append(clusteringIteration);
//		double maxChildDistance = getMaxChildDistance();
		double branchLength = parentDistance - distance;
		branchLength = Math.max(0.01, branchLength);
		buf.append(":").append(String.format("%.2f", branchLength));
	}

	/**
	 * Label the internal node with the distance (appended with the iteration).
	 * Appending the iteration is necessary to make the label unique (to satisfy
	 * Matlab). Spaces are replaced with underscores.
	 */
	private void appendInternalNewickNodeName(StringBuffer buf) {
		Double clusteringIteration = getClusterIteration();
		// String refinedComment = comment.replaceAll(" ", "_");
		// buf.append(refinedComment);
		buf.append("it").append(clusteringIteration.intValue());
		buf.append("-").append(String.format("%.2f", distance));
	}

	public String toString() {
		// return clusterName + " has " + elementCount + " elements";
		return toNestedString();
	}

}
