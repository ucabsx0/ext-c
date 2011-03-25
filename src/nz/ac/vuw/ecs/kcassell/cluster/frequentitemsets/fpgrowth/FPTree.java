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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class FPTree {
	/** The name of the root of the FPTree. */
	public static final String ROOT_NAME = "RootNode";
	
	/**  The key is the item name.  The value is the first node in
	 *  the FPTree labeled with itemName. */
	protected HashMap<String, ArrayList<FPTreeNode>> headerTable =
		new HashMap<String, ArrayList<FPTreeNode>>();
	
	protected FPTreeNode root =
		new FPTreeNode(ROOT_NAME, 0, null);
	
	private static final String SPACES =
		"                                                                        ";
	private static final String EOLN = System.getProperty("line.separator");
	
	public FPTree() {
		addToHeaderTable(ROOT_NAME, root);
	}

	public FPTreeNode getRoot() {
		return root;
	}

	public HashMap<String, ArrayList<FPTreeNode>> getHeaderTable() {
		return headerTable;
	}

	/**
	 * Implements the "insert_tree" function described in Han's paper for
	 * inserting the items of a transaction into the FPTree.
	 * @param items the frequent items for a transaction, in decreasing
	 * frequency
	 * @param parent the node representing the previous item in the sequence
	 */
	public void insert(List<String> items, FPTreeNode parent) {
		if (items.size() > 0) {
			String item = items.get(0);
			FPTreeNode child = parent.getChild(item);
			
			// New child - previously unseen sequence
			if (child == null) {
				child = new FPTreeNode(item, 1, parent);
				parent.addChild(child);
				addToHeaderTable(item, child);
			} else { // Existing node - increment count
				child.incrementCount();
			}
			// recurse on tail
			insert(items.subList(1, items.size()), child);
		}
	}

	private void addToHeaderTable(String item, FPTreeNode node) {
		if (!headerTable.containsKey(item)) {
			ArrayList<FPTreeNode> list = new ArrayList<FPTreeNode>();
			list.add(node);
			headerTable.put(item, list);
		}
	}
	
	@Override
	public String toString() {
		String result = "FPTree [headerTable = " + headerTable + EOLN;
		result += treeToString(root, 0);
		return result;
	}

	private String treeToString(FPTreeNode node, int indent) {
		StringBuffer buf = new StringBuffer();
		String spaces = SPACES;
		if (indent < SPACES.length() / 2) {
			spaces = SPACES.substring(0, 2 * indent + 1);
		}
		buf.append(spaces).append(node).append(EOLN);
		Collection<FPTreeNode> children = node.getChildren();
		if (children != null) {
			for (FPTreeNode child : children) {
				treeToString(child, ++indent);
			}
		}
		return buf.toString();
	}

}
