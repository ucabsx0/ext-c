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

package nz.ac.vuw.ecs.kcassell.callgraph.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphCluster;
import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphNode;
import nz.ac.vuw.ecs.kcassell.callgraph.JavaCallGraph;
import nz.ac.vuw.ecs.kcassell.cluster.BetweennessClusterer;
import nz.ac.vuw.ecs.kcassell.cluster.MatrixBasedAgglomerativeClusterer;
import nz.ac.vuw.ecs.kcassell.cluster.MemberCluster;
import nz.ac.vuw.ecs.kcassell.cluster.MixedModeClusterer;
import nz.ac.vuw.ecs.kcassell.logging.UtilLogger;
import nz.ac.vuw.ecs.kcassell.similarity.ClustererEnum;
import nz.ac.vuw.ecs.kcassell.similarity.DistanceCalculatorEnum;
import nz.ac.vuw.ecs.kcassell.similarity.DistanceCalculatorIfc;
import nz.ac.vuw.ecs.kcassell.similarity.DistanceCollector;
import nz.ac.vuw.ecs.kcassell.similarity.DistanceMatrix;
import nz.ac.vuw.ecs.kcassell.similarity.IdentifierGoogleDistanceCalculator;
import nz.ac.vuw.ecs.kcassell.similarity.IntraClassDistanceCalculator;
import nz.ac.vuw.ecs.kcassell.similarity.VectorSpaceModelCalculator;
import nz.ac.vuw.ecs.kcassell.utils.ApplicationParameters;
import nz.ac.vuw.ecs.kcassell.utils.EclipseUtils;
import nz.ac.vuw.ecs.kcassell.utils.ParameterConstants;
import nz.ac.vuw.ecs.kcassell.utils.RefactoringConstants;

import org.eclipse.jdt.core.JavaModelException;

import edu.uci.ics.jung.graph.util.EdgeType;

public class BatchOutputView implements ActionListener, ParameterConstants {
	private static final String AGGLOMERATE_BUTTON_LABEL = "Agglomerate";

	/** The label used for the button to initiate a count of the
	 * number of disconnected subgraphs.  */
	private static final String DISCONNECTED_BUTTON_LABEL =
		"Disconnected Subgraphs";

	/** The label used for the button to initiate a count of the
	 * number of disconnected subgraphs after a single split based
	 * on betweenness clustering.  */
	private static final String DISCONNECTED1_BUTTON_LABEL =
		"Disconnected Subgraphs (1 Split)";
	private static final String DISTANCES_BUTTON_LABEL = "Compute Distances";
	
	private static final Dimension BUTTON_SIZE = new Dimension(150, 60);
	private static final String  CLASS_SEPARATOR =
		"--------------------------------------\n";

	private static final String  RUN_SEPARATOR =
		"======================================\n";

	/** The main panel for this view. */
    private JSplitPane mainPanel = null;
    
    /** Where descriptive text about the clusters is written. */
    protected JTextArea textArea = null;

	/** The visualization area for agglomerative clustering. */
    private JPanel leftPanel = null;
    
    /** The enclosing application. */
    private ExtC app = null;
    
	private JLabel progressLabel = null;
    private JProgressBar progressBar = null;
    
    /** Accumulates the clustering results. */
	private StringBuffer buf = new StringBuffer(RUN_SEPARATOR);

    protected static final UtilLogger logger =
    	new UtilLogger("BatchOutputView");

	public BatchOutputView(ExtC app) {
    	this.app = app;
		setUpView();
	}


	/**
	 * @return the mainPanel
	 */
	public JSplitPane getMainPanel() {
		return mainPanel;
	}

	/**
	 * @return the clustersTextArea
	 */
	public JTextArea getTextArea() {
		return textArea;
	}

	/**
	 * Creates the agglomeration applet and starts it
	 */
	protected void setUpView() {
		leftPanel = new JPanel();
		leftPanel.setLayout(new GridLayout(12, 1));
		JButton aggButton = new JButton(AGGLOMERATE_BUTTON_LABEL);
		aggButton.setPreferredSize(BUTTON_SIZE);
		aggButton.addActionListener(this);
		leftPanel.add(aggButton);
		JButton subgraphButton = new JButton(DISCONNECTED_BUTTON_LABEL);
		subgraphButton.setPreferredSize(BUTTON_SIZE);
		subgraphButton.addActionListener(this);
		leftPanel.add(subgraphButton);
		JButton subgraph1Button = new JButton(DISCONNECTED1_BUTTON_LABEL);
		subgraph1Button.setPreferredSize(BUTTON_SIZE);
		subgraph1Button.addActionListener(this);
		leftPanel.add(subgraph1Button);
		JButton distancesButton = new JButton(DISTANCES_BUTTON_LABEL);
		distancesButton.setPreferredSize(BUTTON_SIZE);
		distancesButton.addActionListener(this);
		leftPanel.add(distancesButton);
		progressLabel = new JLabel("Progress:");
		progressLabel.setVisible(false);
		progressLabel.setPreferredSize(BUTTON_SIZE);
		leftPanel.add(progressLabel);

		textArea = new JTextArea();
		textArea.setEditable(false);
		JScrollPane textScroller = new JScrollPane(textArea);
		mainPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                leftPanel, textScroller);
		mainPanel.validate();
		mainPanel.repaint();
	}

	/**
	 * Reacts to the various push buttons
	 */
	public void actionPerformed(ActionEvent event) {
		String command = event.getActionCommand();
		if (AGGLOMERATE_BUTTON_LABEL.equals(command)) {
			clusterAllSelections(mainPanel);
		}
		else if (DISCONNECTED_BUTTON_LABEL.equals(command)) {
			countAllDisconnectedSubgraphs(mainPanel);
		}
		else if (DISCONNECTED1_BUTTON_LABEL.equals(command)) {
			clusterAllSelections(mainPanel);
		}
		else if (DISTANCES_BUTTON_LABEL.equals(command)){
			collectDistances(mainPanel);
		}
		textArea.repaint();
	}

	/**
	 * Clusters the members of all classes in the metrics view.
	 * @return
	 */
	protected StringBuffer clusterSelections() {
		ApplicationParameters params = ApplicationParameters.getSingleton();
		String sClusterer = params.getParameter(
				CLUSTERER_KEY, ClustererEnum.MIXED_MODE.toString());
		String sCalc = params.getParameter(
				ParameterConstants.CALCULATOR_KEY,
				DistanceCalculatorEnum.IntraClass.toString());
		logger.info("Aggregating using " + sClusterer + " and " + sCalc);
		MetricsView metricsView = app.getMetricsView();
		String[] classHandles = metricsView.getClassHandles();
		// "=Weka/<weka.classifiers.meta{MultiClassClassifier.java[MultiClassClassifier";

		int iterations = classHandles.length; // Math.min(20, classHandles.length);
		activateProgressBar(iterations);
		for (int i = 0;  i < iterations; i++) {
			progressBar.setValue(i);
			String handle = classHandles[i];
			clusterOneSelection(sClusterer, sCalc, handle);
		}
		inactivateProgressBar();
		return buf;
	}


	protected void clusterOneSelection(String sClusterer, String sCalc,
			String handle) {
		buf = new StringBuffer(RUN_SEPARATOR);
		long start = System.currentTimeMillis();
		try {
			JavaCallGraph callGraph = getGraphFromHandle(handle);
			// TODO other calculators for all clusterers
			if (ClustererEnum.AGGLOMERATIVE.toString().equalsIgnoreCase(
					sClusterer)) {
				if (DistanceCalculatorEnum.IntraClass.toString()
						.equalsIgnoreCase(sCalc)) {
					DistanceCalculatorIfc<String> calc =
						new IntraClassDistanceCalculator(callGraph);
					List<String> memberHandles = EclipseUtils
							.getFilteredMemberHandles(handle);
					MatrixBasedAgglomerativeClusterer clusterer =
						new MatrixBasedAgglomerativeClusterer(
							memberHandles, calc);
					MemberCluster cluster = clusterer.getSingleCluster();
					buf.append("Final cluster:\n"
							+ cluster.toNestedString());
				} else if (DistanceCalculatorEnum.GoogleDistance.toString()
						.equalsIgnoreCase(sCalc)) {
					try {
						DistanceCalculatorIfc<String> calc =
							new IdentifierGoogleDistanceCalculator();
						List<String> memberHandles =
							EclipseUtils.getFilteredMemberNames(handle);
//							EclipseUtils.getMemberHandles(handle);
						MatrixBasedAgglomerativeClusterer clusterer =
							new MatrixBasedAgglomerativeClusterer(memberHandles, calc);
						MemberCluster cluster = clusterer.getSingleCluster();
						buf.append("Final cluster:\n"
								+ cluster.toNestedString());
					} catch (Exception e) {
						String msg = "Unable to calculate distances.  (No web access?)";
						JOptionPane.showMessageDialog(mainPanel, msg,
							"Error Clustering", JOptionPane.WARNING_MESSAGE);
					}
				} else if (DistanceCalculatorEnum.VectorSpaceModel.toString()
						.equalsIgnoreCase(sCalc)) {
					try {
						String className = EclipseUtils.getNameFromHandle(handle);
					    //TODO remove hard-code of the file in which the member "document" data is stored
					    String memberDocumentFile = RefactoringConstants.DATA_DIR +
							"MemberDocuments/FreeCol/" + className;
					    DistanceCalculatorIfc<String> calc =
					    	new VectorSpaceModelCalculator(memberDocumentFile);
						List<String> memberHandles =
							EclipseUtils.getFilteredMemberNames(handle);
						MatrixBasedAgglomerativeClusterer clusterer =
							new MatrixBasedAgglomerativeClusterer(memberHandles, calc);
						MemberCluster cluster = clusterer.getSingleCluster();
						buf.append("Final cluster:\n"
								+ cluster.toNestedString());
					} catch (Exception e) {
						String msg = "Problem with the VectorSpaceModelCalculator: " + e;
						JOptionPane.showMessageDialog(mainPanel, msg,
							"Error Clustering", JOptionPane.WARNING_MESSAGE);
					}
				}
			} else if (ClustererEnum.BETWEENNESS.toString()
					.equalsIgnoreCase(sClusterer)) {
				Collection<CallGraphNode> clusters =
					clusterUsingBetweenness(callGraph);
				buf.append("Final clusters for " + callGraph.getName());
				appendClusterSizes(clusters);
				String sClusters = toOutputString(clusters);
				buf.append(":\n" + sClusters);
			} else if (ClustererEnum.MIXED_MODE.toString()
					.equalsIgnoreCase(sClusterer)) {
				Collection<CallGraphNode> clusters =
					clusterUsingMixedMode(callGraph);
				buf.append("Final clusters for " + callGraph.getName());
				appendClusterSizes(clusters);
				String sClusters = toOutputString(clusters);
				buf.append(":\n" + sClusters);
			}
			textArea.append(buf.toString());
		} catch (JavaModelException e) {
			buf.append(e.toString());
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		buf.append("Clustering above took " + (end - start) + " millis");
		buf.append(CLASS_SEPARATOR);
	}


	/**
	 * Append the sizes of the clusters to the output buffer.
	 * @param clusters
	 */
	private void appendClusterSizes(Collection<CallGraphNode> clusters) {
		buf.append(" (");
		for (CallGraphNode node : clusters) {
			if (node instanceof CallGraphCluster) {
				CallGraphCluster cluster = (CallGraphCluster)node;
				buf.append(cluster.getElementCount());
			} else {
				buf.append("1");
			}
			buf.append(", ");
		}
		buf.delete(buf.length()-2, buf.length()); // omit last ", "
		buf.append(")");
	}

	/**
	 * Cluster using the BetweennessClusterer.
	 * @param callGraph
	 * @throws JavaModelException
	 */
	public static Collection<CallGraphNode> clusterUsingBetweenness(JavaCallGraph callGraph)
	throws JavaModelException {
		JavaCallGraph undirectedGraph =
			JavaCallGraph.toUndirectedGraph(callGraph);
		BetweennessClusterer clusterer = new BetweennessClusterer(undirectedGraph);

		// Get intial group sizes, without clustering
		Collection<CallGraphNode> clusters = clusterer.cluster(0);
		List<Integer> sizes =
			CallGraphCluster.getClusterSizes(clusters);
		Collections.sort(sizes);
		int clusterCount = sizes.size();
		
		// If there are disconnected groups before clustering, check their
		// sizes
		if (clusterCount > 1) {
			Integer largest2 = sizes.get(clusterCount - 2);
			
			// If the second largest cluster is above the threshold for new
			// class size, do no more.  
			if (largest2 < 7) { 
				clusters = clusterer.cluster();
			}
		} else { // Break up the single group
			clusters = clusterer.cluster();
		}
		ArrayList<CallGraphNode> nodeClusters =
			new ArrayList<CallGraphNode>(clusters);
		Collections.sort(nodeClusters, BetweennessClusterer.getSizeComparator());
		return nodeClusters;
	}

	/**
	 * Cluster using the MixedModeClusterer.
	 * @param callGraph
	 * @throws JavaModelException
	 */
	public static Collection<CallGraphNode> clusterUsingMixedMode(JavaCallGraph callGraph)
	throws JavaModelException {
		JavaCallGraph undirectedGraph =
			JavaCallGraph.toUndirectedGraph(callGraph);
		MixedModeClusterer clusterer = new MixedModeClusterer(undirectedGraph);
		Collection<CallGraphNode> clusters = clusterer.cluster();
		return clusters;
	}


	private static String toOutputString(Collection<CallGraphNode> clusters) {
		StringBuffer buf = new StringBuffer();
		for (CallGraphNode node: clusters) {
			buf.append(node.toNestedString());
			buf.append("\n");
		}
		String clusterString = buf.toString();
		return clusterString;
	}

	protected void activateProgressBar(int limit) {
		if (progressBar != null) {
			leftPanel.remove(progressBar);
		}
		progressBar = new JProgressBar(0, limit);
		progressBar.setPreferredSize(BUTTON_SIZE);
		leftPanel.add(progressBar);
		progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressLabel.setVisible(true);
		leftPanel.validate();
		leftPanel.repaint();
	}

	protected void inactivateProgressBar() {
		if (progressBar != null) {
			leftPanel.remove(progressBar);
		}
		progressBar = null;
        progressLabel.setVisible(false);
		leftPanel.validate();
		leftPanel.repaint();
	}

	/**
	 * Counts the number of members in each disconnected subgraph for each
	 * of the class handles obtained from the metrics view.
	 * condensed
	 */
	protected void countDisconnectedSubgraphs() {
		textArea.append(RUN_SEPARATOR);
		MetricsView metricsView = app.getMetricsView();
		String[] classHandles = metricsView.getClassHandles();
		
		if ((classHandles == null)
				|| ((classHandles.length == 1)
						&& classHandles[0].contains("none found"))) {
			String msg = "Nothing (from the metrics view) to count";
			JOptionPane.showMessageDialog(mainPanel, msg,
				"Error Clustering", JOptionPane.WARNING_MESSAGE);
		} else {
			ArrayList<String> candidates = new ArrayList<String>();
			activateProgressBar(classHandles.length);

			for (int i = 0; i < classHandles.length; i++) {
				long start = System.currentTimeMillis();

				try {
					JavaCallGraph callGraph =
						getGraphFromHandle(classHandles[i]);
					BetweennessClusterer calc = new BetweennessClusterer(callGraph);
					Collection<CallGraphNode> clusters = calc.cluster(0);
					List<Integer> sizes =
						CallGraphCluster.getClusterSizes(clusters);
					Collections.sort(sizes);
					int clusterCount = sizes.size();
					if (clusterCount > 1) {
						Integer largest1 = sizes.get(clusterCount - 1);
						Integer largest2 = sizes.get(clusterCount - 2);
						ApplicationParameters parameters =
							app.getApplicationParameters();
						Integer maxMembers =
							parameters.getIntParameter(MAX_MEMBERS_KEY, 20);

						if ((largest1 + largest2) > maxMembers) {
							candidates.add(classHandles[i]);
						}
					}
					textArea.append("Initial cluster sizes for " +
							classHandles[i] + ": " + sizes);
					progressBar.setValue(i);
				} catch (JavaModelException e) {
					textArea.append(e.toString());
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				long end = System.currentTimeMillis();
				textArea.append("Clustering above took " + (end - start) + " millis\n");
			}
			textArea.append("Extract class candidates: (" + 
					candidates.size() + "/" + classHandles.length + ")\n\t" + 
					candidates + "\n");
			inactivateProgressBar();
		}
	}

	/**
	 * Get the call graph corresponding to the handle
	 * @param classHandle the handle indicating the class
	 * @return a java call graph
	 * @throws JavaModelException
	 */
	protected JavaCallGraph getGraphFromHandle(
			String classHandle) throws JavaModelException {
		// A temporary graph to use for calling getAltGraphUsingParams
		JavaCallGraph callGraph = new JavaCallGraph();
//			new JavaCallGraph(classHandle, EdgeType.DIRECTED);
		callGraph.setHandle(classHandle);
		callGraph.setDefaultEdgeType(EdgeType.DIRECTED);
		callGraph = callGraph.getAltGraphUsingParams();
		return callGraph;
	}

	/**
	 * Run agglomerative clustering on all classes in the metric view and
	 * report the results in the text area.
	 * @param mainPane the component on which to put the wait cursor
	 */
	public void clusterAllSelections(final Component mainPane) {
		System.out.println("clustering...");

		Thread worker = new Thread("BatchClusterThread") {

			public void run() {

				try {
					try {
						textArea.setText("");
						mainPane.setCursor(RefactoringConstants.WAIT_CURSOR);
						clusterSelections();
					} finally {
						mainPane.setCursor(RefactoringConstants.DEFAULT_CURSOR);
					}
				} catch (Exception e) {
					String msg = "Problem while clustering: "
							+ e.getMessage();
					JOptionPane.showMessageDialog(mainPane, msg,
							"Error Clustering", JOptionPane.WARNING_MESSAGE);
				}
			}
		}; // Thread worker

		worker.start(); // So we don't hold up the dispatch thread.
	}

	/**
	 * Determine the disconnected subgraphs for each class and print the sizes
	 * of them.
	 * @param component the component on which to put the wait cursor
	 */
	public void countAllDisconnectedSubgraphs(final Component component) {
		System.out.println("counting subgraphs...");

		Thread worker = new Thread("SubgraphsThread") {

			public void run() {

				try {
					try {
						component.setCursor(RefactoringConstants.WAIT_CURSOR);
						countDisconnectedSubgraphs();
					} finally {
						component.setCursor(RefactoringConstants.DEFAULT_CURSOR);
					}
				} catch (Exception e) {
					String msg = "Problem while counting subgraphs: "
							+ e.getMessage();
					JOptionPane.showMessageDialog(component, msg,
							"Error Clustering", JOptionPane.WARNING_MESSAGE);
				}
			}
		}; // Thread worker

		worker.start(); // So we don't hold up the dispatch thread.
	}

	/**
	 * Determine the distances between the members for each class.
	 * @param mainPane the component on which to put the wait cursor
	 */
	public void collectDistances(final Component mainPane) {
		System.out.println("collecting distances...");

		Thread worker = new Thread("CollectDistancesThread") {

			public void run() {

				try {
					try {
						mainPane.setCursor(RefactoringConstants.WAIT_CURSOR);
						collectDistances();
					} finally {
						mainPane.setCursor(RefactoringConstants.DEFAULT_CURSOR);
					}
				} catch (Exception e) {
					String msg = "Problem while collecting distances: "
							+ e.getMessage();
					JOptionPane.showMessageDialog(mainPane, msg,
							"Error Collecting Distances", JOptionPane.WARNING_MESSAGE);
				}
			}
		}; // Thread worker

		worker.start(); // So we don't hold up the dispatch thread.
	}

	/**
	 * Collects distance measurements between the members of the
	 * class visible in the graph view.
	 */
	protected void collectDistances() {
		textArea.append(RUN_SEPARATOR);
		GraphView graphView = app.getGraphView();
		JavaCallGraph callGraph = graphView.getGraph();

		if (callGraph == null) {
			String msg = "Choose a class.";
			JOptionPane.showMessageDialog(mainPanel, msg,
				"No class chosen", JOptionPane.WARNING_MESSAGE);
		} else {
			List<CallGraphNode> nodes = callGraph.getNodes();
			List<String> memberNames = getMemberNames(nodes);
			ArrayList<DistanceCalculatorIfc<String>> calculators =
				initializeCalculators(callGraph);
			DistanceCollector collector = new DistanceCollector(calculators);
			activateProgressBar(calculators.size());
			int i = 0;

			for (DistanceCalculatorIfc<String> calc : calculators) {
				long start = System.currentTimeMillis();

				DistanceMatrix<String> matrix =
					collector.collectDistances(memberNames, calc);
				textArea.append(calc.getType().toString() +
						" distances for " + callGraph.getName() + ":\n");
				textArea.append(matrix.toString());
				progressBar.setValue(i++);
				long end = System.currentTimeMillis();
				textArea.append("Distance calculation above took " + (end - start) + " millis\n");
			}
			inactivateProgressBar();
		}
	}

	/**
	 * @param callGraph a dependency graph between class members
	 * @return a list of calculators that will calculate distances
	 * between the nodes of the supplied graph
	 */
	private ArrayList<DistanceCalculatorIfc<String>> initializeCalculators(
			JavaCallGraph callGraph) {
		ArrayList<DistanceCalculatorIfc<String>> calculators =
			new ArrayList<DistanceCalculatorIfc<String>>();
		
//		CzibulaDistanceCalculator czibulaCalculator =
//			new CzibulaDistanceCalculator(callGraph);
//		calculators.add(czibulaCalculator);
		
		IdentifierGoogleDistanceCalculator googleCalculator = null;
		try {
			googleCalculator = new IdentifierGoogleDistanceCalculator();
			googleCalculator.clearCache();
			calculators.add(googleCalculator);
		} catch (Exception e) {
			String msg = "Unable to initialize GoogleDistanceCalculator:\n" + e;
			JOptionPane.showMessageDialog(mainPanel, msg,
				"Error Initializing", JOptionPane.WARNING_MESSAGE);
		}
		
//		IntraClassDistanceCalculator intraCalculator =
//			new IntraClassDistanceCalculator(callGraph);
//		calculators.add(intraCalculator);
//		
//		SimonDistanceCalculator simonCalculator =
//			new SimonDistanceCalculator(callGraph);
//		calculators.add(simonCalculator);
		return calculators;
	}

	/**
	 * Collects the member names from the graph's nodes.
	 * @param nodes
	 * @return the list of names
	 */
	private List<String> getMemberNames(List<CallGraphNode> nodes) {
		List<String> memberNames = new ArrayList<String>();
		
		for (CallGraphNode node : nodes) {
			memberNames.add(node.getSimpleName());
		}
		Collections.sort(memberNames);
		return memberNames;
	}


}
