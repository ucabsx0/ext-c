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

import java.util.Collection;
import java.util.HashMap;

public class FPTreeNode {
	
	/** The item identifier. */
	protected String itemName;
	
	/** The support count - the number of transactions represented by
	 * the portion of the path reaching this node. */
	protected int itemCount;
	
	/** The link to the previous node in one or more transaction sequences.  */
	protected FPTreeNode parentNode = null;

	/** The links to the next nodes in one or more transaction sequences.  */
	protected HashMap<String, FPTreeNode> childLinks = null;

//	/** The forward link to the next node in a linked list of nodes with same
//	 * itemName.  */
//	Not needed - info is in the list maintained by the headerTable
//	protected FPTreeNode nodeLink = null;

	
	/**
	 * Three argument constructor.
	 * 
	 * @param name
	 *            the itemset identifier.
	 * @param support
	 *            the support value for the itemset.
	 * @param backRef
	 *            the backward link to the parent node.
	 */

	public FPTreeNode(String name, int support, FPTreeNode parentNode) {
		itemName = name;
		itemCount = support;
		this.parentNode = parentNode;
	}
	
	public void incrementCount() {
		itemCount++;
	}

	public void addChild(FPTreeNode node) {
		if (childLinks == null) {
			childLinks = new HashMap<String, FPTreeNode>();
		}
		childLinks.put(node.itemName, node);
	}
	
	public FPTreeNode getChild(String name) {
		FPTreeNode child = null;
		if (childLinks != null) {
			child = childLinks.get(name);
		}
		return child;
	}
	
	public Collection<FPTreeNode> getChildren() {
		Collection<FPTreeNode> result = null;
		if (childLinks != null) {
			result = childLinks.values();
		}
		return result;
	}

	@Override
	public String toString() {
		String result =
		 "FPTreeNode [itemName=" + itemName + ":" + itemCount;
		if (parentNode != null) {
			result += ", parentNode=" + parentNode.itemName + ":" + parentNode.itemCount;
		}
		if (childLinks != null && childLinks.size() > 0) {
			result += ", children: (";
			for (FPTreeNode child : childLinks.values()) {
				result += child.itemName + ":" + child.itemCount + " ";
			}
			result += ")";
		}
		return result;
	}
}
