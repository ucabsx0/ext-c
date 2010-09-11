package nz.ac.vuw.ecs.kcassell.similarity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphNode;
import nz.ac.vuw.ecs.kcassell.callgraph.JavaCallGraph;

public class DistanceCollector {
	
	HashMap<DistanceCalculatorIfc<String>, DistanceMatrix<String>> allDistances =
		new HashMap<DistanceCalculatorIfc<String>, DistanceMatrix<String>>();
//	DistanceMatrix<String> czibulaDistances = null;
//	DistanceMatrix<String> googleDistances = null;
//	DistanceMatrix<String> intraClassDistances = null;
//	DistanceMatrix<String> simonDistances = null;

	public static void main(String[] args) {
		JavaCallGraph callGraph = null;
		DistanceCollector collector = new DistanceCollector();
		collector.collectDistances(callGraph);
	}

	public void collectDistances(JavaCallGraph callGraph) {
		ArrayList<DistanceCalculatorIfc<String>> calculators =
			initializeCalculators(callGraph);
		List<CallGraphNode> nodes = callGraph.getNodes();
		List<String> memberNames = getMemberNames(nodes);
		
		for (DistanceCalculatorIfc<String> calc : calculators) {
			DistanceMatrix<String> matrix =
				new DistanceMatrix<String>(memberNames);
			matrix.fillMatrix(calc);
			allDistances.put(calc, matrix);
		}
	}

	private ArrayList<DistanceCalculatorIfc<String>> initializeCalculators(
			JavaCallGraph callGraph) {
		ArrayList<DistanceCalculatorIfc<String>> calculators =
			new ArrayList<DistanceCalculatorIfc<String>>();
		CzibulaDistanceCalculator czibulaCalculator =
			new CzibulaDistanceCalculator(callGraph);
		calculators.add(czibulaCalculator);
		IdentifierGoogleDistanceCalculator googleCalculator = null;
		try {
			googleCalculator = new IdentifierGoogleDistanceCalculator();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		calculators.add(googleCalculator);
		IntraClassDistanceCalculator intraCalculator =
			new IntraClassDistanceCalculator(callGraph);
		calculators.add(intraCalculator);
		SimonDistanceCalculator simonCalculator =
			new SimonDistanceCalculator(callGraph);
		calculators.add(simonCalculator);
		return calculators;
	}

	/**
	 * Collects the member names from the graph's nodes.
	 * @param nodes
	 * @return the list of names
	 */
	protected List<String> getMemberNames(List<CallGraphNode> nodes) {
		List<String> memberNames = new ArrayList<String>();
		
		for (CallGraphNode node : nodes) {
			memberNames.add(node.getSimpleName());
		}
		return memberNames;
	}
}
