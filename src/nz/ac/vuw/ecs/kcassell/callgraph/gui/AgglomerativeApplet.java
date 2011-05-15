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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.SortedSet;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphCluster;
import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphLink;
import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphNode;
import nz.ac.vuw.ecs.kcassell.callgraph.JavaCallGraph;
import nz.ac.vuw.ecs.kcassell.cluster.ClusterCombinationEnum;
import nz.ac.vuw.ecs.kcassell.cluster.ClusterTextFormatEnum;
import nz.ac.vuw.ecs.kcassell.cluster.ClustererIfc;
import nz.ac.vuw.ecs.kcassell.similarity.ClustererEnum;
import nz.ac.vuw.ecs.kcassell.similarity.DistanceCalculatorEnum;
import nz.ac.vuw.ecs.kcassell.utils.ParameterConstants;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;

public class AgglomerativeApplet extends ClusteringGraphApplet
implements ClusterUIConstants, ParameterConstants {
	
	public static final String CALCULATOR_COMBO = "CalculatorCombo";
	public static final String CLUSTERER_COMBO = "ClustererCombo";
	public static final String CLUSTER_TEXT_FORMAT_COMBO = "ClusterTextFormatCombo";
	public static final String LINK_COMBO = "LinkCombo";

	protected static final long serialVersionUID = 1L;
	protected Layout<CallGraphNode, CallGraphLink> layout = null;

	/** Where the user selects the distance calculator. */
	protected JComboBox calculatorBox = null;

	/** Where the user selects the clusterer. */
	protected JComboBox clustererBox = null;

	/** Where the user selects the group linkage type. */
	protected JComboBox groupLinkageBox = null;

	/** Where the user selects the text format. */
	protected JComboBox textFormatBox = null;

	JPanel southPanel = null;

	// protected HashMap<CallGraphNode, Point2D> nodePositions =
	// new HashMap<CallGraphNode, Point2D>();

	public AgglomerativeApplet() {
		sliderLabelBase = "Iteration: ";
	}

	/**
	 * Shows the cluster text
	 */
	public void setUpView(final JavaCallGraph callGraph) throws IOException {
		graph = callGraph;
		Graph<CallGraphNode, CallGraphLink> jungGraph =
			callGraph.getJungGraph();
		setUpVisualizerAndLayout(jungGraph);
		// recordPositions(jungGraph);

		Container content = getContentPane();
		content.removeAll();
		content.add(new GraphZoomScrollPane(visualizer));
		JPanel southPanel = setUpSouthPanel();
		content.add(southPanel, BorderLayout.SOUTH);
		displayGraphClusterText();
		visualizer.validate();
		visualizer.repaint();
	}

	private JComboBox createLinkageCombo() {
		Vector<String> menuItems = new Vector<String>();
		for (ClusterCombinationEnum linkage : ClusterCombinationEnum.values()) {
			menuItems.add(linkage.toString());
		}
		JComboBox linkageBox = new JComboBox(menuItems);
		String sLink = parameters.getParameter(
				LINKAGE_KEY,
				ClusterCombinationEnum.AVERAGE_LINK.toString());
		linkageBox.setSelectedItem(sLink);
		linkageBox.setName(LINK_COMBO);
		return linkageBox;
	}

	private JComboBox createCalculatorCombo() {
		Vector<String> menuItems = new Vector<String>();
		for (DistanceCalculatorEnum calc : DistanceCalculatorEnum.values()) {
			// TODO reincorporate GoogleDistance when code to handle its
			// long run-time is in place
			if ( calc != DistanceCalculatorEnum.GoogleDistance) {
				menuItems.add(calc.toString());
			}
		}
		JComboBox calculatorBox = new JComboBox(menuItems);
		String sCalc = parameters.getParameter(
				CALCULATOR_KEY,
				DistanceCalculatorEnum.IntraClass.toString());
		calculatorBox.setSelectedItem(sCalc);
		calculatorBox.setName(CALCULATOR_COMBO);
		return calculatorBox;
	}

	private JComboBox createClusterTextFormatCombo() {
		Vector<String> menuItems = new Vector<String>();
		for (ClusterTextFormatEnum format : ClusterTextFormatEnum.values()) {
			menuItems.add(format.toString());
		}
		JComboBox clustererBox = new JComboBox(menuItems);
		String sFormat = parameters.getParameter(
				CLUSTER_TEXT_FORMAT_KEY,
				ClusterTextFormatEnum.NEWICK.toString());
		clustererBox.setSelectedItem(sFormat);
		clustererBox.setName(CLUSTER_TEXT_FORMAT_COMBO);
		return clustererBox;
	}

	private JComboBox createClustererCombo() {
		Vector<String> menuItems = new Vector<String>();
		for (ClustererEnum calc : ClustererEnum.values()) {
			menuItems.add(calc.toString());
		}
		JComboBox clustererBox = new JComboBox(menuItems);
		String sClusterer = parameters.getParameter(
				CLUSTERER_KEY,
				ClustererEnum.AGGLOMERATIVE.toString());
		clustererBox.setSelectedItem(sClusterer);
		clustererBox.setName(CLUSTERER_COMBO);
		return clustererBox;
	}

	/*
	 * Records where all the nodes have been laid out.
	 * 
	 * @param jungGraph
	 */
	// protected void recordPositions(Graph<CallGraphNode, CallGraphLink>
	// jungGraph) {
	// Collection<CallGraphNode> vertices = jungGraph.getVertices();
	// for (CallGraphNode node : vertices) {
	// Point2D point = layout.transform(node);
	// nodePositions.put(node, point);
	// }
	// }

	/**
	 * Create a simple layout frame with the Fruchterman-Rheingold layout
	 * algorithm
	 * 
	 * @param graph
	 */
	protected void setUpVisualizerAndLayout(
			final Graph<CallGraphNode, CallGraphLink> graph) {
		String sLayout = parameters.getParameter(GRAPH_LAYOUT_KEY,
				GraphLayoutEnum.FRLayout.toString());
		layout = GraphView.getLayout(sLayout, graph);
		visualizer =
			new VisualizationViewer<CallGraphNode, CallGraphLink>(
				layout);
		setUpMouseMode();
		// visualizer.setBackground(Color.white);
		// Tell the renderer to use our own customized color rendering
		setRenderContextTransformers();
	}

	protected JPanel setUpSouthPanel() {
		southPanel = new JPanel();
		GridLayout gridLayout = new GridLayout(2, 5);
		JPanel grid = new JPanel(gridLayout);
		gridLayout.setHgap(15);
		
		JLabel clustererLabel = new JLabel("Clusterer: ");
		grid.add(clustererLabel);
		JLabel calcLabel = new JLabel("Distance Calculator: ");
		grid.add(calcLabel);
		JLabel groupLinkageLabel = new JLabel("Group Linkage: ");
		grid.add(groupLinkageLabel);
		JLabel iterationLabel = new JLabel("Visualize Agglomeration");
		grid.add(iterationLabel);
		JLabel formatLabel = new JLabel("Text Format: ");
		grid.add(formatLabel);

		clustererBox = createClustererCombo();
		clustererBox.addActionListener(view);
		grid.add(clustererBox);

		calculatorBox = createCalculatorCombo();
		calculatorBox.addActionListener(view);
		grid.add(calculatorBox);

		groupLinkageBox = createLinkageCombo();
		groupLinkageBox.addActionListener(view);
		grid.add(groupLinkageBox);

		TitledBorder sliderBorder = setUpSliderPanel();
		addChangeListenerToSlider(sliderBorder);
		grid.add(sliderPanel);
		southPanel.add(grid);

		textFormatBox = createClusterTextFormatCombo();
		textFormatBox.addActionListener(view);
		grid.add(textFormatBox);

		setUpMouseMode();
		southPanel.validate();
		return southPanel;
	}

	protected void addChangeListenerToSlider(final TitledBorder sliderBorder) {
		iterationSlider.addChangeListener(new ChangeListener() {
			/**
			 * Changes the graph display after the slider is moved.
			 */
			public void stateChanged(ChangeEvent event) {
				JSlider source = (JSlider) event.getSource();
				if (!source.getValueIsAdjusting()) {
					int iteration = source.getValue();
					
					String sCalc = parameters.getParameter(CALCULATOR_KEY,
							DistanceCalculatorEnum.Identifier.toString());
					
					// TODO visual display for identifier clustering
					if (DistanceCalculatorEnum.Identifier.toString().equals(sCalc)
							|| DistanceCalculatorEnum.GoogleDistance.toString().equals(sCalc)) {
					} else {
						redrawGraph(iteration);
						displayGraphClusterText();
					}
					sliderBorder.setTitle(sliderLabelBase
							+ iterationSlider.getValue());
					sliderPanel.repaint();
				}
			}
		});
	}

	/**
	 * Changes the graph display according to the number of clustering steps
	 * 
	 * @param iteration
	 *            the number of clustering steps to be completed
	 */
	protected void redrawGraph(int iteration) {
		// aggregateLayout.removeAll();
		Collection<CallGraphNode> clusters = clusterer.cluster(iteration);
		relocateClusters(clusters);
		visualizer.validate();
		visualizer.repaint();
	}

	/**
	 * Displays text corresponding to the clusters in the text area.
	 */
	protected void displayGraphClusterText() {
		Graph<CallGraphNode, CallGraphLink> graph = layout.getGraph();
		Collection<CallGraphNode> clusters = graph.getVertices();
		ArrayList<CallGraphNode> clusterList = new ArrayList<CallGraphNode>(
				clusters);
		Collections.sort(clusterList, nodeNameComparator);

		if (clustersTextArea != null) {
			StringBuffer buf = new StringBuffer();
			groupsToString(buf, clusters);
			String clustersString = buf.toString();
			clustersTextArea.setText(clustersString);
			clustersTextArea.setCaretPosition(0);
			clustersTextArea.repaint();
		}
	}

	/**
	 * Redisplays the nodes that are CallGraphClusters
	 * 
	 * @param nodes
	 */
	protected void relocateClusters(Collection<CallGraphNode> nodes) {
		for (CallGraphNode node : nodes) {
			if (node instanceof CallGraphCluster) {
				CallGraphCluster cluster = (CallGraphCluster) node;
				setClusterLocation(cluster);
			}
			// else {
			// Point2D point1 = layout.transform(node);
			//
			// }
		}
	}

	/**
	 * Set the location of a cluster to be that of the subnode nearest to the
	 * center.
	 * 
	 * @param cluster
	 *            the new cluster
	 */
	protected void setClusterLocation(CallGraphCluster cluster) {
		SortedSet<CallGraphNode> subNodes = cluster.getElements();
		// For agglomeration, two subnodes per cluster
		CallGraphNode node1 = subNodes.first();
		CallGraphNode node2 = subNodes.last();
		Dimension dimension = layout.getSize();
		Point2D center = new Point(dimension.width / 2, dimension.height / 2);
		Point2D point1 = layout.transform(node1);
		Point2D point2 = layout.transform(node2);
		double distance1 = center.distance(point1);
		double distance2 = center.distance(point2);
		if (distance1 < distance2) {
			layout.setLocation(cluster, point1);
		} else {
			layout.setLocation(cluster, point2);
		}
	}

	/**
	 * @param clusterer
	 *            the clusterer to set
	 */
	public void setClusterer(ClustererIfc<CallGraphNode> clusterer) {
		this.clusterer = clusterer;
	}

	/**
	 * @return the clustersTextArea
	 */
	public JTextArea getClustersTextArea() {
		return clustersTextArea;
	}


}
