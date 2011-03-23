package nz.ac.vuw.ecs.kcassell.cluster.frequentitemsets.fpgrowth;

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.TestCase;
import nz.ac.vuw.ecs.kcassell.cluster.frequentitemsets.ItemSupportList;

import org.junit.Test;

public class FPGrowthMinerTest extends TestCase {

	@Test
	public void testBuildFPTree() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetFrequentItems() {
		try {
		Collection<ItemSupportList> transactions = new ArrayList<ItemSupportList>();
		ItemSupportList t1 = new ItemSupportList("client1", null);
		transactions.add(t1);
		
		ArrayList<String> members2 = new ArrayList<String>();
		members2.add("m1");
		members2.add("m2");
		ItemSupportList t2 = new ItemSupportList("client1", members2);
		transactions.add(t2);

		ArrayList<String> members3 = new ArrayList<String>();
		members3.add("m1");
		members3.add("m3");
		ItemSupportList t3 = new ItemSupportList("client1", members3);
		transactions.add(t3);

		ArrayList<String> members4 = new ArrayList<String>();
		members4.add("m1");
		members2.add("m2");
		members2.add("m3");
		members4.add("m4");
		ItemSupportList t4 = new ItemSupportList("client1", members4);
		transactions.add(t4);

		FPGrowthMiner miner = new FPGrowthMiner();
		ItemSupportList frequentItems = miner.getFrequentItems(transactions);
		System.out.println("frequentItems = " + frequentItems);
		} catch (StackOverflowError e) {
			StackTraceElement[] trace = e.getStackTrace();
//			Arrays.copyOfRange()
		}
	}

}
