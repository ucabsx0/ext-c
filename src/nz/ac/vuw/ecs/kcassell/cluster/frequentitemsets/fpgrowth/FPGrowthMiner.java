package nz.ac.vuw.ecs.kcassell.cluster.frequentitemsets.fpgrowth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import nz.ac.vuw.ecs.kcassell.cluster.frequentitemsets.ItemSupportList;
import nz.ac.vuw.ecs.kcassell.cluster.frequentitemsets.ValueComparator;

public class FPGrowthMiner {

	/** A comparator that orders items by decreasing support. */
	protected ValueComparator comparator = null;
	
	/**
	 * 
	 * @param transactions
	 *            a collection of transactions where each transaction (e.g. the
	 *            calls of a client class) is a collections of items (e.g. the
	 *            methods of the server class that the client class calls).
	 * @return
	 */
	protected FPTree buildFPTree(Collection<ItemSupportList> transactions) {
		// getFrequentItems sets the comparator as a side-effect
		ItemSupportList frequentItems = getFrequentItems(transactions);
		frequentItems.setComparator(comparator);
		FPTree fpTree = new FPTree();
		
		for (ItemSupportList transaction : transactions) {
			ItemSupportList sortedTransaction =
				sortItems(transaction);
			List<String> items = sortedTransaction.getItems();
			fpTree.insert(items, fpTree.getRoot());
		}
		fpTree.setFrequentItems(frequentItems);
		return fpTree;
	}

	/**
	 * Sort the items in the transaction based on the comparator.  Note that
	 * using a ValueComparator generated elsewhere can produce a sorting that
	 * may look unusual, e.g. when we have a "global" listing of frequencies
	 * and use that to reorder the local items.
	 * @param transaction
	 * @return
	 */
	protected ItemSupportList sortItems(ItemSupportList transaction) {
		// TODO should we copy or modify the transaction?  Presumably, we don't
		// care about the original ordering.
		transaction.setComparator(comparator);
		transaction.sortItems();
		return transaction;
	}

	/**
	 * Combines the items in all of the transaction, and returns them in
	 * decreasing order of support.  getFrequentItems sets the comparator
	 * as a side-effect.
	 * @param transactions
	 * @return the items in decreasing order of support
	 */
	protected ItemSupportList getFrequentItems(
			Collection<ItemSupportList> transactions) {
		ItemSupportList frequentItems =
			new ItemSupportList("Frequent Items", new ArrayList<String>(), comparator);
		
		for (ItemSupportList transaction : transactions) {
			List<String> transactionMembers = transaction.getItems();
			for (String item : transactionMembers) {
				Double support = transaction.getSupport(item);
				frequentItems.addSupport(item, support);
			}
		}
		comparator = new ValueComparator(frequentItems.getSupportMap());
		frequentItems.setComparator(comparator);
		frequentItems.getItems();
		return frequentItems;
	}
	
	/**
	 * The top-level call to the FPGrowth algorithm for computing frequent
	 * item sets.
	 * @param transactions the collection of all "transactions", where each
	 *   transaction contains items
	 * @param minSupport the minimum frequency of occurrence of a pattern for it
	 *   to be included in the result
	 * @return the collection of all frequent patterns (item sets)
	 */
	public Collection<ItemSupportList> mine(Collection<ItemSupportList> transactions,
			int minSupport) {
		FPTree tree = buildFPTree(transactions);
		Collection<ItemSupportList> frequentPatterns =
			new ArrayList<ItemSupportList>();
		List<String> headersDescending = tree.getHeadersDescending();
		
		// Starting with the least common item with acceptable support,
		// extract the frequent patterns for each item.
		for (int i = headersDescending.size() - 1; i >= 0; i--) {
			String headerA = headersDescending.get(i);
			ItemSupportList inputPatternA = buildInitialPatternA(headerA);
			Collection<ItemSupportList> patterns =
				fpGrowth(tree, inputPatternA, minSupport);
			frequentPatterns.addAll(patterns);
		}
		return frequentPatterns;
	}

	/**
	 * Build the initial "conditional" pattern for an item.  Since its
	 * the initial pattern, it will be empty.
	 * @param header the item whose conditional support is wanted
	 * @return an empty item support list.  The name of the "transaction"
	 * in the ItemSupportList will be the item whose conditional support is wanted
	 */
	protected ItemSupportList buildInitialPatternA(String header) {
		ArrayList<String> emptyList = new ArrayList<String>();
		ItemSupportList inputPattern =
			new ItemSupportList(header, emptyList, comparator);
		return inputPattern;
	}
	
	
	/**
	 * Recursively extract frequent patterns (item sets) from the FPTree.
	 * @param tree
	 * @param inputPatternA a pattern (item set) from the conditional pattern base
	 *   or empty if this is the initial call (no conditions)
	 * @param minSupport the minimum frequency of occurrence of a pattern for it
	 *   to be included in the result
	 * @return the collection of all frequent patterns (item sets)
	 */
	public Collection<ItemSupportList> fpGrowth(FPTree tree,
			ItemSupportList inputPatternA,
			int minSupport) {
		Collection<ItemSupportList> frequentPatterns =
			new ArrayList<ItemSupportList>();
		
		if (tree.hasOneBranch()) {
			frequentPatterns = generatePatternsForCombinations(tree);
		} else {
			String itemName = inputPatternA.getName();
			HashMap<String,ArrayList<FPTreeNode>> headerTable =
				tree.getHeaderTable();
			ArrayList<FPTreeNode> itemNodes = headerTable.get(itemName);
			
			// Traverse the node-links for an element in the header table
			for (FPTreeNode itemNode : itemNodes) {
				ItemSupportList patternB =
					generatePatternB(itemNode, inputPatternA);
//				ItemSupportList condPatternBase =
//					buildFPTree(transactions)
			}
		}
		return frequentPatterns;
	}

	protected ItemSupportList generatePatternB(FPTreeNode itemNode,
			ItemSupportList inputPatternA) {
		String itemNodeName = itemNode.getItemName();
		String patternBName = itemNodeName + inputPatternA.getName();
		List<String> patternAItems = inputPatternA.getItems();
		List<String> patternBItems = new ArrayList<String>(patternAItems);
		ItemSupportList patternB =
			new ItemSupportList(patternBName , patternBItems, comparator);
		Double support = itemNode.getSupport() * 1.0;
		for (String patternBItem : patternBItems) {
			patternB.setSupport(patternBItem, support);
		}
		patternB.addSupport(itemNodeName, support);
		return patternB;
	}

	private Collection<ItemSupportList> generatePatternsForCombinations(
			FPTree tree) {
		// TODO Auto-generated method stub
		return null;
	}
}
