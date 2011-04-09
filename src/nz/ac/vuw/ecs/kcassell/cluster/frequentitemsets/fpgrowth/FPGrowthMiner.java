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
	 * Builds a frequent pattern tree (FPTree) based on the frequently
	 * occurring items in a transaction.
	 * @param transactions
	 *            a collection of transactions where each transaction (e.g. the
	 *            calls of a client class) is a collections of items (e.g. the
	 *            methods of the server class that the client class calls).
	 * @param minSupport
	 *            the minimum total support that each item must have to be
	 *            included in the tree
	 * @return the frequent pattern tree
	 */
	protected FPTree buildFPTree(Collection<ItemSupportList> transactions,
			int minSupport) {
		ItemSupportList frequentItems =
			getFrequentItems(transactions, minSupport);
		frequentItems.setComparator(comparator);
		FPTree fpTree = buildFPTreeFromFrequentItems(transactions,
				frequentItems);
		return fpTree;
	}

	/**
	 * Builds a frequent pattern tree (FPTree) based on the frequently
	 * occurring items in a transaction.
	 * @param transactions
	 *            a collection of transactions where each transaction (e.g. the
	 *            calls of a client class) is a collections of items (e.g. the
	 *            methods of the server class that the client class calls).
	 * @param frequentItems the frequently occurring items across all
	 * transactions
	 * @return the frequent pattern tree
	 */
	protected FPTree buildFPTreeFromFrequentItems(
			Collection<ItemSupportList> transactions,
			ItemSupportList frequentItems) {
		FPTree fpTree = new FPTree();
		fpTree.setFrequentItems(frequentItems);
		
		for (ItemSupportList transaction : transactions) {
			ItemSupportList sortedTransaction =
				pruneAndSortItems(transaction, frequentItems);
			List<String> items = sortedTransaction.getItems();
			fpTree.insert(items, fpTree.getRoot());
		}
		return fpTree;
	}

	/**
	 * Sort the items in the transaction based on the comparator after
	 * removing those items with insufficient support to be frequent.
	 * @param transaction
	 * @return a revised transaction that has frequent items sorted
	 */
	protected ItemSupportList pruneAndSortItems(ItemSupportList transaction,
			ItemSupportList frequentItems) {
		List<String> items = transaction.getItems();
		List<String> revisedItems = new ArrayList<String>(items);
		
		if (frequentItems != null) {
			for (int i = revisedItems.size() - 1; i >= 0; i--) {
				String item = revisedItems.get(i);
				Double support = frequentItems.getSupport(item);
				if (support == null) {
					revisedItems.remove(i);
				}
			}
		}
		ValueComparator revisedComparator =
			new ValueComparator(frequentItems.getSupportMap());
		ItemSupportList revisedTransaction =
			new ItemSupportList(transaction.getName() + "pruned",
					revisedItems, revisedComparator);
		// Set the supports for the revised transaction the same
		// as the original transaction
		for (String item : revisedItems) {
			Double support = transaction.getSupport(item);
			revisedTransaction.setSupport(item, support);
		}
		revisedTransaction.sortItems();
		return revisedTransaction;
	}

	protected ItemSupportList getFrequentItems(
			Collection<ItemSupportList> transactions, int minSupport) {
		ItemSupportList itemsDecreasing =
			getItemsDecreasingFrequency(transactions);
		List<String> items = itemsDecreasing.getItems();
		int itemIndex = items.size() - 1;
		boolean tooSmall = true;
		
		while(tooSmall && itemIndex >= 0) {
			String item = items.get(itemIndex);
			Double support = itemsDecreasing.getSupport(item);
			tooSmall = support < minSupport;
			if (tooSmall) {
				items.remove(itemIndex);
				itemsDecreasing.setSupport(item, null);
			}
			itemIndex--;
		}
		return itemsDecreasing;
	}

	/**
	 * Combines the items in all of the transaction, and returns them in
	 * decreasing order of support.  getFrequentItems sets the comparator
	 * as a side-effect.
	 * @param transactions
	 * @return the items in decreasing order of support
	 */
	protected ItemSupportList getItemsDecreasingFrequency(
			Collection<ItemSupportList> transactions) {
		ItemSupportList sortedItems =
			new ItemSupportList("Frequent Items", new ArrayList<String>(), comparator);
		
		for (ItemSupportList transaction : transactions) {
			List<String> transactionMembers = transaction.getItems();
			for (String item : transactionMembers) {
				Double support = transaction.getSupport(item);
				sortedItems.addSupport(item, support);
			}
		}
		comparator = new ValueComparator(sortedItems.getSupportMap());
		sortedItems.setComparator(comparator);
		sortedItems.getItems();
		return sortedItems;
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
		FPTree tree = buildFPTree(transactions, minSupport);
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
//			ArrayList<FPTreeNode> itemNodes = headerTable.get(itemName);
			
			ItemSupportList frequentItems = tree.getFrequentItems();
			
			// Starting with the least frequently occurring item, build
			// frequent item sets containing that item
			List<String> items = frequentItems.getItems();
			for (int i = items.size() - 1; i >= 0; i--) {
				String itemA = items.get(i);
				Double supportA = frequentItems.getSupport(itemA);
				ItemSupportList patternB =
					generatePatternB(itemA, supportA, inputPatternA);
				Collection<ItemSupportList> conditionalPatternBase =
					constructConditionalPatternBase(tree, patternB);
			}

		}
		return frequentPatterns;
	}

	protected Collection<ItemSupportList> constructConditionalPatternBase(
			FPTree tree, ItemSupportList patternB) {
		Collection<ItemSupportList> conditionalPatternBase =
			new ArrayList<ItemSupportList>();
		// The most recently processed node (towards the leaves)
		List<String> bItems = patternB.getItems();
		String bItem0 = bItems.get(0);
		HashMap<String,ArrayList<FPTreeNode>> headerTable = tree.getHeaderTable();
		ArrayList<FPTreeNode> itemNodes = headerTable.get(bItem0);

		// Traverse the node-links for an element in the header table -
		// moving "sideways" through the nodes for a given item
		for (FPTreeNode itemNode : itemNodes) {
			ItemSupportList pattern = constructConditionalPattern(itemNode);
			if (pattern != null) {
				conditionalPatternBase.add(pattern);
			}
		}
		//TODO build conditionalPatternBase
		return conditionalPatternBase;
	}

	protected ItemSupportList constructConditionalPattern(FPTreeNode itemNode) {
		FPTreeNode parentNode = itemNode.getParentNode();
		String parentName = parentNode.getItemName();
		Double support = itemNode.getSupport() * 1.0;
		ArrayList<String> items = new ArrayList<String>();
		String name = "";
		
		// Climb up (towards the root of the tree)
		while (!FPTree.ROOT_NAME.equals(parentName)) {
			items.add(parentName);
			name += parentName + ",";
			parentNode = parentNode.getParentNode();
			parentName = parentNode.getItemName();
		}
		ItemSupportList condPattern = new ItemSupportList(name, items, comparator);
		for (String item : items) {
			condPattern.setSupport(item, support);
		}
		return condPattern;
	}

	/**
	 * Generates the pattern on which the conditional pattern base
	 * and conditional FPTree will be based.
	 * @param itemName the name of the item
	 * @param support the support for itemName
	 * @param inputPatternA the pattern built from less frequently
	 * occurring items
	 * @return a pattern joining itemNode with inputPaternA, where the
	 * support for each item is set to that of itemNode
	 */
	protected ItemSupportList generatePatternB(String itemName,
			Double support,
			ItemSupportList inputPatternA) {
		String patternBName = itemName + inputPatternA.getName();
		List<String> patternAItems = inputPatternA.getItems();
		List<String> patternBItems = new ArrayList<String>(patternAItems);
		ItemSupportList patternB =
			new ItemSupportList(patternBName , patternBItems, comparator);
		for (String patternBItem : patternBItems) {
			patternB.setSupport(patternBItem, support);
		}
		patternB.addSupport(itemName, support);
		return patternB;
	}

	/**
	 * Find all the paths through the tree that terminate at item
	 * @param item the least frequently occurring item in the patterns
	 * to be returned
	 * @param tree the tree being searched
	 * @return the patterns
	 */
	public Collection<ItemSupportList> getPatternsEndingWithItem(String item,
			FPTree tree) {
		return tree.getPatternsEndingWithItem(item, comparator);
	}

	/**
	 * Given a single branch, generate patterns representing all combinations of
	 * the items on that branch
	 * @param tree
	 * @return the patterns
	 */
	protected Collection<ItemSupportList> generatePatternsForCombinations(
			FPTree tree) {
		// TODO Auto-generated method stub
		return null;
	}
}
