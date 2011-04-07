package nz.ac.vuw.ecs.kcassell.cluster.frequentitemsets.fpgrowth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import nz.ac.vuw.ecs.kcassell.cluster.frequentitemsets.ItemSupportList;

import org.junit.Test;

public class FPGrowthMinerTest extends TestCase {
	
	// Used by setUpTransactionsM1M4
	Collection<ItemSupportList> transactions = null;
	ItemSupportList t1 = null;
	ItemSupportList t2 = null;
	ItemSupportList t3 = null;
	ItemSupportList t4 = null;

	// Used by setUpTransactionsHan
	Collection<ItemSupportList> transactionsHan = null;
	ItemSupportList t100 = null;
	ItemSupportList t200 = null;
	ItemSupportList t300 = null;
	ItemSupportList t400 = null;
	ItemSupportList t500 = null;

	private void setUpTransactionsM1M4() {
		transactions = new ArrayList<ItemSupportList>();
		t1 = new ItemSupportList("client1", null, null);
		transactions.add(t1);
		
		ArrayList<String> members2 = new ArrayList<String>();
		members2.add("m1");
		members2.add("m2");
		t2 = new ItemSupportList("client1", members2, null);
		transactions.add(t2);

		ArrayList<String> members3 = new ArrayList<String>();
		members3.add("m2");
		members3.add("m3");
		t3 = new ItemSupportList("client1", members3, null);
		transactions.add(t3);

		ArrayList<String> members4 = new ArrayList<String>();
		members4.add("m1");
		members4.add("m2");
		members4.add("m3");
		members4.add("m4");
		t4 = new ItemSupportList("client1", members4, null);
		transactions.add(t4);
	}

	/**
	 * Creates the transactions used in Han's paper.
	 */
	private void setUpTransactionsHan() {
		transactionsHan = new ArrayList<ItemSupportList>();

		ArrayList<String> members100 = new ArrayList<String>();
		members100.add("f");
		members100.add("a");
		members100.add("c");
		members100.add("d");
		members100.add("g");
		members100.add("i");
		members100.add("m");
		members100.add("p");
		t100 = new ItemSupportList("t100", members100, null);
		transactionsHan.add(t100);
		
		ArrayList<String> members200 = new ArrayList<String>();
		members200.add("a");
		members200.add("b");
		members200.add("c");
		members200.add("f");
		members200.add("l");
		members200.add("m");
		members200.add("o");
		t200 = new ItemSupportList("t200", members200, null);
		transactionsHan.add(t200);

		ArrayList<String> members300 = new ArrayList<String>();
		members300.add("b");
		members300.add("f");
		members300.add("h");
		members300.add("j");
		members300.add("o");
		t300 = new ItemSupportList("t300", members300, null);
		transactionsHan.add(t300);

		ArrayList<String> members400 = new ArrayList<String>();
		members400.add("b");
		members400.add("c");
		members400.add("k");
		members400.add("s");
		members400.add("p");
		t400 = new ItemSupportList("t400", members400, null);
		transactionsHan.add(t400);

		ArrayList<String> members500 = new ArrayList<String>();
		members500.add("a");
		members500.add("f");
		members500.add("c");
		members500.add("e");
		members500.add("l");
		members500.add("p");
		members500.add("m");
		members500.add("n");
		t500 = new ItemSupportList("t500", members500, null);
		transactionsHan.add(t500);

	}

	@Test
	public void testBuildFPTree() {
		setUpTransactionsHan();
		FPGrowthMiner miner = new FPGrowthMiner();
		FPTree tree = miner.buildFPTree(transactionsHan, 3);
		HashMap<String,ArrayList<FPTreeNode>> headerTable = tree.getHeaderTable();
		Set<String> keySet = headerTable.keySet();
		assertEquals(6, keySet.size());
		ItemSupportList frequentItems = tree.getFrequentItems();
		List<String> items = frequentItems.getItems();
		assertEquals(6, items.size());
		assertEquals(4.0, frequentItems.getSupport("f"));
		assertEquals(4.0, frequentItems.getSupport("c"));
		assertEquals(3.0, frequentItems.getSupport("a"));
		assertEquals(3.0, frequentItems.getSupport("b"));
		assertEquals(3.0, frequentItems.getSupport("m"));
		assertEquals(3.0, frequentItems.getSupport("p"));
		
		System.out.println("tree =\n" + tree);
	}

	@Test
	public void testGeneratePatternB() {
		setUpTransactionsM1M4();
		FPGrowthMiner miner = new FPGrowthMiner();
		ItemSupportList patternB1 =
			miner.generatePatternB("testGeneratePatternB1", 2.0 , t2);
		List<String> items = patternB1.getItems();
		assertEquals(3, items.size());
		assertEquals("m1", items.get(0));
		assertEquals("m2", items.get(1));
		assertEquals("testGeneratePatternB1", items.get(2));
		assertEquals(2.0, patternB1.getSupport("m1"));
		assertEquals(2.0, patternB1.getSupport("m2"));
		assertEquals(2.0, patternB1.getSupport("testGeneratePatternB1"));

		ItemSupportList patternB2 =
			miner.generatePatternB("testGeneratePatternB2", 1.0, patternB1);
		items = patternB2.getItems();
		assertEquals(4, items.size());
		assertEquals("m1", items.get(0));
		assertEquals("m2", items.get(1));
		assertEquals("testGeneratePatternB1", items.get(2));
		assertEquals("testGeneratePatternB2", items.get(3));
		assertEquals(1.0, patternB2.getSupport("m1"));
		assertEquals(1.0, patternB2.getSupport("m2"));
		assertEquals(1.0, patternB2.getSupport("testGeneratePatternB1"));
		assertEquals(1.0, patternB2.getSupport("testGeneratePatternB2"));
		System.out.println("testGeneratePatternB2 =\n" + patternB2);
	}
	
	@Test
	public void testGetItemsDecreasingFrequency() {
		setUpTransactionsM1M4();
		FPGrowthMiner miner = new FPGrowthMiner();
		ItemSupportList frequentItems = miner.getItemsDecreasingFrequency(transactions);
		System.out.println("frequentItems = " + frequentItems);
		List<String> members = frequentItems.getItems();
		assertEquals("m2", members.get(0));
		assertEquals(3.0, frequentItems.getSupport("m2"), 0.00001);
		assertEquals("m1", members.get(1));
		assertEquals(2.0, frequentItems.getSupport("m1"), 0.00001);
		assertEquals("m3", members.get(2));
		assertEquals(2.0, frequentItems.getSupport("m3"), 0.00001);
		assertEquals("m4", members.get(3));
		assertEquals(1.0, frequentItems.getSupport("m4"), 0.00001);
	}
	
	@Test
	public void testGetFrequentItems() {
		setUpTransactionsM1M4();
		FPGrowthMiner miner = new FPGrowthMiner();
		ItemSupportList frequentItems =
			miner.getFrequentItems(transactions, 1);
		System.out.println("frequentItems = " + frequentItems);
		List<String> members = frequentItems.getItems();
		assertEquals(4, members.size());
		assertEquals("m2", members.get(0));
		assertEquals(3.0, frequentItems.getSupport("m2"), 0.00001);
		assertEquals("m1", members.get(1));
		assertEquals(2.0, frequentItems.getSupport("m1"), 0.00001);
		assertEquals("m3", members.get(2));
		assertEquals(2.0, frequentItems.getSupport("m3"), 0.00001);
		assertEquals("m4", members.get(3));
		assertEquals(1.0, frequentItems.getSupport("m4"), 0.00001);

		frequentItems = miner.getFrequentItems(transactions, 3);
		System.out.println("frequentItems = " + frequentItems);
		members = frequentItems.getItems();
		assertEquals(1, members.size());
		assertEquals("m2", members.get(0));

		frequentItems = miner.getFrequentItems(transactions, 2);
		System.out.println("frequentItems = " + frequentItems);
		members = frequentItems.getItems();
		assertEquals(3, members.size());
		assertEquals("m2", members.get(0));
		assertEquals(3.0, frequentItems.getSupport("m2"), 0.00001);
		assertEquals("m1", members.get(1));
		assertEquals(2.0, frequentItems.getSupport("m1"), 0.00001);
		assertEquals("m3", members.get(2));
		assertEquals(2.0, frequentItems.getSupport("m3"), 0.00001);
	}
	
	@Test
	public void testConstructConditionalPattern() {
		
	}
	
	public void testPruneAndSortItems() {
		setUpTransactionsM1M4();
		FPGrowthMiner miner = new FPGrowthMiner();
		// Establish the ordering based on the frequencies
		// of occurrence of items
		miner.getItemsDecreasingFrequency(transactions);
/*		frequentItems = ItemSupportList
			m2:	3.0
			m1:	2.0
			m3:	2.0
			m4:	1.0		*/
		ItemSupportList frequentItems = miner.getFrequentItems(transactions, 0);
		ItemSupportList revisedT2 = miner.pruneAndSortItems(t2, frequentItems);
		List<String> members2 = revisedT2.getItems();
		assertEquals("m2", members2.get(0));
		assertEquals("m1", members2.get(1));
		
		ItemSupportList revisedT3 = miner.pruneAndSortItems(t3, frequentItems);
		List<String> members3 = revisedT3.getItems();
		assertEquals("m2", members3.get(0));
		assertEquals("m3", members3.get(1));
		
		ItemSupportList revisedT4 = miner.pruneAndSortItems(t4, frequentItems);
		List<String> members4 = revisedT4.getItems();
		assertEquals("m2", members4.get(0));
		assertEquals("m1", members4.get(1));
		assertEquals("m3", members4.get(2));
		assertEquals("m4", members4.get(3));

		frequentItems = miner.getFrequentItems(transactions, 3);
		revisedT2 = miner.pruneAndSortItems(t2, frequentItems);
		members2 = revisedT2.getItems();
		assertEquals(1, members2.size());
		assertEquals("m2", members2.get(0));
		
		revisedT3 = miner.pruneAndSortItems(t3, frequentItems);
		members3 = revisedT3.getItems();
		assertEquals(1, members3.size());
		assertEquals("m2", members3.get(0));
		
		revisedT4 = miner.pruneAndSortItems(t4, frequentItems);
		members4 = revisedT4.getItems();
		assertEquals(1, members4.size());
		assertEquals("m2", members4.get(0));
	}

}
