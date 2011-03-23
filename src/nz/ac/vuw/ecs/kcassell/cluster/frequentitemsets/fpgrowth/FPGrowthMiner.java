package nz.ac.vuw.ecs.kcassell.cluster.frequentitemsets.fpgrowth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import nz.ac.vuw.ecs.kcassell.cluster.frequentitemsets.ItemSupportList;

public class FPGrowthMiner {

	/**
	 * 
	 * @param transactions
	 *            a collection of transactions where each transaction (e.g. the
	 *            calls of a client class) is a collections of items (e.g. the
	 *            methods of the server class that the client class calls).
	 * @return
	 */
	protected FPTree buildFPTree(Collection<ItemSupportList> transactions) {
		ItemSupportList frequentItems =
			getFrequentItems(transactions);
		List<String> listFrequentItems =
			frequentItems.sortCalledMembers(ItemSupportList.SORT_BY.VALUE);
		FPTree fpTree = new FPTree();
		
		for (ItemSupportList transaction : transactions) {
			ItemSupportList sortedTransaction =
				selectAndSortTransaction(transaction);
			fpTree.insert(sortedTransaction);
		}
		return fpTree;
	}

	private ItemSupportList selectAndSortTransaction(ItemSupportList transaction) {
		// TODO Auto-generated method stub
		return null;
	}

	protected ItemSupportList getFrequentItems(
			Collection<ItemSupportList> transactions) {
		ItemSupportList frequentItems =
			new ItemSupportList("Frequent Items", new ArrayList<String>(),
					ItemSupportList.SORT_BY.VALUE);
		
		for (ItemSupportList transaction : transactions) {
			List<String> transactionMembers = transaction.getMembers();
			for (String item : transactionMembers) {
				Double support = transaction.getSupport(item);
				frequentItems.addSupport(item, support);
			}
		}
		return frequentItems;
	}
}
