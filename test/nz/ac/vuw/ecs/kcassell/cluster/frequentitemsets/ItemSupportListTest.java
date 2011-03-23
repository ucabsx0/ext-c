package nz.ac.vuw.ecs.kcassell.cluster.frequentitemsets;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

public class ItemSupportListTest extends TestCase {

	@Test
	public void testSortCalledMembers() {
		ArrayList<String> keys = new ArrayList<String>();
		keys.add("a");
		keys.add("z");
		keys.add("a2");
		keys.add("n");
		keys.add("b");
		ItemSupportList supportList = new ItemSupportList("class1", keys);
		List<String> members =
			supportList.sortCalledMembers(ItemSupportList.SORT_BY.KEY);
		assertEquals(members.get(0), "a");
		assertEquals(members.get(1), "a2");
		assertEquals(members.get(2), "b");
		assertEquals(members.get(3), "n");
		assertEquals(members.get(4), "z");
		members =
			supportList.sortCalledMembers(ItemSupportList.SORT_BY.VALUE);
		// Because all the support is the same, sort by value should be
		// the same as sort by name
		assertEquals(members.get(0), "a");
		assertEquals(members.get(1), "a2");
		assertEquals(members.get(2), "b");
		assertEquals(members.get(3), "n");
		assertEquals(members.get(4), "z");
		
		supportList.setSupport("n", 3.0);
		supportList.setSupport("a2", 2.0);
//		System.out.println("supportList = " + supportList);
		members =
			supportList.sortCalledMembers(ItemSupportList.SORT_BY.KEY);
		assertEquals(members.get(0), "a");
		assertEquals(members.get(1), "a2");
		assertEquals(members.get(2), "b");
		assertEquals(members.get(3), "n");
		assertEquals(members.get(4), "z");
		members =
			supportList.sortCalledMembers(ItemSupportList.SORT_BY.VALUE);
//		System.out.println("members sorted by value = " + members);
		assertEquals(members.get(0), "n");
		assertEquals(members.get(1), "a2");
		assertEquals(members.get(2), "a");
		assertEquals(members.get(3), "b");
		assertEquals(members.get(4), "z");
	}
	
	@Test
	public void testChangeSupport() {
		ArrayList<String> keys = new ArrayList<String>();
		keys.add("a");
		keys.add("z");
		ItemSupportList supportList = new ItemSupportList("class1", keys);
		assertEquals(1.0, supportList.getSupport("a"));
		assertEquals(1.0, supportList.getSupport("z"));
		supportList.setSupport("a", 3.1);
		supportList.setSupport("z", 2.0);
		assertEquals(3.1, supportList.getSupport("a"));
		assertEquals(2.0, supportList.getSupport("z"));
		supportList.addSupport("a", 3.1);
		supportList.addSupport("z", 2.0);
		assertEquals(6.2, supportList.getSupport("a"));
		assertEquals(4.0, supportList.getSupport("z"));
		assertEquals(null, supportList.getSupport("zz"));
		supportList.addSupport("zz", 2.0);
		assertEquals(2.0, supportList.getSupport("zz"));
	}

}
