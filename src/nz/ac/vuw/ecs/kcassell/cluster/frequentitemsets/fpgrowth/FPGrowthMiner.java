package nz.ac.vuw.ecs.kcassell.cluster.frequentitemsets.fpgrowth;

import java.util.ArrayList;
import java.util.Collection;
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
			fpTree.insert(items);
		}
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
}
