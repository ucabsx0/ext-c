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

package nz.ac.vuw.ecs.kcassell.cluster;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

public class AprioriClustererTest extends TestCase {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testGenerateCandidates() {
		fail("Not yet implemented");
	}

	@Test
	public void testJoinPriorItemSets() {
		Set<ItemSet<Integer>> candidates = new HashSet<ItemSet<Integer>>();
		
		SortedSet<Integer> candidateItems123 = new TreeSet<Integer>();
		candidateItems123.add(1);
		candidateItems123.add(2);
		candidateItems123.add(3);
		ItemSet<Integer> candidate123 = new ItemSet<Integer>(candidateItems123 );
		candidates.add(candidate123);
		
		SortedSet<Integer> candidateItems124 = new TreeSet<Integer>();
		candidateItems124.add(1);
		candidateItems124.add(2);
		candidateItems124.add(4);
		ItemSet<Integer> candidate124 = new ItemSet<Integer>(candidateItems124 );
		candidates.add(candidate124);
		
		SortedSet<Integer> candidateItems134 = new TreeSet<Integer>();
		candidateItems134.add(1);
		candidateItems134.add(3);
		candidateItems134.add(4);
		ItemSet<Integer> candidate134 = new ItemSet<Integer>(candidateItems134 );
		candidates.add(candidate134);
		
		SortedSet<Integer> candidateItems135 = new TreeSet<Integer>();
		candidateItems135.add(1);
		candidateItems135.add(3);
		candidateItems135.add(5);
		ItemSet<Integer> candidate135 = new ItemSet<Integer>(candidateItems135 );
		candidates.add(candidate135);

		SortedSet<Integer> candidateItems234 = new TreeSet<Integer>();
		candidateItems234.add(2);
		candidateItems234.add(3);
		candidateItems234.add(4);
		ItemSet<Integer> candidate234 = new ItemSet<Integer>(candidateItems234 );
		candidates.add(candidate234);
		
		AprioriClusterer<Integer> apriori = new AprioriClusterer<Integer>();
		Set<ItemSet<Integer>> itemSets = apriori.joinPriorItemSets(candidates);
//		System.out.println("itemSets = " + itemSets);
		assertEquals(2, itemSets.size()); // {1,2,3,4} and {1,3,4,5}
		Iterator<ItemSet<Integer>> iterator = itemSets.iterator();
		while (iterator.hasNext()) {
			ItemSet<Integer> itemSet = iterator.next();
			SortedSet<Integer> items = itemSet.getItems();
			assertEquals((Integer)1, (Integer)items.first());
			assertTrue(items.contains((Integer)3));
			assertTrue(items.contains((Integer)4));
			if (!items.contains((Integer)5)) {
				assertTrue(items.contains((Integer)2));
			}
			if (!items.contains((Integer)2)) {
				assertTrue(items.contains((Integer)5));
			}
		}
}

}
