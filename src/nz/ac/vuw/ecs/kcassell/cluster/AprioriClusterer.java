/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
Copyright (c) 2011, Keith Cassell
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Based on the Apriori algorithm described in
 * "Fast Algorithms for Mining Association Rules", R. Agrawal, R. Srikant, 1994.
 * 
 * @author kcassell
 * 
 */
public class AprioriClusterer<T> {

	protected Set<ItemSet<T>> generateCandidates(Set<ItemSet<T>> priorItemSets) {
		Set<ItemSet<T>> candidates = joinPriorItemSets(priorItemSets);
		candidates = pruneCandidates(candidates, priorItemSets);		
		return candidates;
	}

	/**
	 * Combines those item sets that are the same except for their last members.
	 * @param priorItemSets item sets with k-1 members
	 * @return candidate items sets with k members
	 */
	@SuppressWarnings("unchecked")
	protected Set<ItemSet<T>> joinPriorItemSets(Set<ItemSet<T>> priorItemSets) {
		Set<ItemSet<T>> candidates = new HashSet<ItemSet<T>>();
		
		for (ItemSet<T> itemSet1 : priorItemSets) {
			T last1 = itemSet1.last();
			SortedSet<T> allButLast1 = itemSet1.allButLast();
			
			for (ItemSet<T> itemSet2 : priorItemSets) {
				T last2 = itemSet2.last();
				SortedSet<T> allButLast2 = itemSet2.allButLast();
				
				if (allButLast1.equals(allButLast2) && 
						((Comparable<T>)last1).compareTo(last2) < 0) {
					// TODO increase efficiency by adding to existing item set
					// instead of creating new objects?
					TreeSet<T> candidateItems = new TreeSet<T>(itemSet1.getItems());
					candidateItems.add(last2);
					ItemSet<T> candidate = new ItemSet<T>(candidateItems);
					candidates.add(candidate);
				}
			}
		}
		return candidates;
	}
	
	protected Set<ItemSet<T>> pruneCandidates(Set<ItemSet<T>> candidates,
			Set<ItemSet<T>> priorItemSets) {
		Iterator<ItemSet<T>> iterator = candidates.iterator();
		while (iterator.hasNext()) {
			ItemSet<T> itemSet = iterator.next();
			if (!passesSubsetTest(itemSet, priorItemSets)) {
				iterator.remove();
			}
		}
		return candidates;
	}

	private boolean passesSubsetTest(ItemSet<T> itemSet,
			Set<ItemSet<T>> priorItemSets) {
		// TODO Auto-generated method stub
		return false;
	}

    /** Returns all combinations of items of the specified size from the source
     *  list. Order is preserved, such as if a is always before b in the source,
     *  there will not be a combination where a is after b. Unlike permutations,
     *  order is not significant such that [a,b] is equivalent to the combination
     *  [b,a] and therefore latter will not be returned unless a or b appears
     *  more than once in the source list. */
    public static <T> List<List<T>> getCombinations(List<T> source, int size) {
        if (size < 0 || size > source.size())
            throw new IllegalArgumentException("Combination size must be greater " +
                "than or equal to 0 and less than or equal to the size of the " +
                "source list (" + source.size() + "). Current size of " + size +
                " is invalid.");

        List<List<T>> combinations = new ArrayList<List<T>>();
        if (size == 0)
            combinations.add(new ArrayList<T>());
            
        else if (size == source.size())
            combinations.add(new ArrayList<T>(source));

        else
            for (int i = 0; i < source.size() - size + 1; i++) {
                T head = source.get(i);
                List<T> subList = source.subList(i + 1, source.size());
                for (List<T> subCombination: getCombinations(subList, size - 1)) {
                    subCombination.add(0, head);
                    combinations.add(subCombination);
                }
            }
        
        return combinations;
    }

}
