package nz.ac.vuw.ecs.kcassell.cluster.frequentitemsets.fpgrowth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;
import nz.ac.vuw.ecs.kcassell.cluster.frequentitemsets.ItemSupportList;

import org.junit.Test;

public class FPGrowthMinerTest extends TestCase {
	
	Collection<ItemSupportList> transactions = null;
	ItemSupportList t1 = null;
	ItemSupportList t2 = null;
	ItemSupportList t3 = null;
	ItemSupportList t4 = null;

	public void setUp() {
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

	@Test
	public void testBuildFPTree() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetFrequentItems() {
		FPGrowthMiner miner = new FPGrowthMiner();
		ItemSupportList frequentItems = miner.getFrequentItems(transactions);
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
	
	public void testSortItems() {
		FPGrowthMiner miner = new FPGrowthMiner();
		ItemSupportList frequentItems = miner.getFrequentItems(transactions);
/*		frequentItems = ItemSupportList
			m2:	3.0
			m1:	2.0
			m3:	2.0
			m4:	1.0		*/
		miner.sortItems(t2);
		List<String> members2 = t2.getItems();
		assertEquals("m2", members2.get(0));
		assertEquals("m1", members2.get(1));
		
		miner.sortItems(t3);
		List<String> members3 = t3.getItems();
		assertEquals("m2", members3.get(0));
		assertEquals("m3", members3.get(1));
		
		miner.sortItems(t4);
		List<String> members4 = t4.getItems();
		assertEquals("m2", members4.get(0));
		assertEquals("m1", members4.get(1));
		assertEquals("m3", members4.get(2));
		assertEquals("m4", members4.get(3));
	}

}
