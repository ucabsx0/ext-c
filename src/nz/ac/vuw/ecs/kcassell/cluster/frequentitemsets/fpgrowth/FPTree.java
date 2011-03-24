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

package nz.ac.vuw.ecs.kcassell.cluster.frequentitemsets.fpgrowth;

import java.util.HashMap;
import java.util.List;

public class FPTree {
	/** The name of the root of the FPTree. */
	public static final String ROOT_NAME = "RootNode";
	
	/**  The key is the item name.  The value is the first node in
	 *  the FPTree labeled with itemName. */
	protected HashMap<String, ItemPrefixSubtreeNode> headerTable =
		new HashMap<String, ItemPrefixSubtreeNode>();
	
	protected ItemPrefixSubtreeNode root =
		new ItemPrefixSubtreeNode(ROOT_NAME, 0);
	
	public FPTree() {
		headerTable.put(ROOT_NAME, root);
	}

	public void insert(List<String> items) {
//		List<String> items = sortedTransaction.getItems();
		if (items.size() > 0) {
			String item = items.get(0);
			if (headerTable.containsKey(item)) {
				ItemPrefixSubtreeNode node = headerTable.get(item);
				node.incrementCount();
			} else {
				ItemPrefixSubtreeNode node =
					new ItemPrefixSubtreeNode(item, 1);
				headerTable.put(item, node);
				// TODO node's parent link to the Tree(?)
				// TODO node's node-link be linked to the nodes with the same
				// item-name via the node-link structure
			}
			// recurse on tail
			insert(items.subList(1, items.size()));
		}
		
	}
}
