package nz.ac.vuw.ecs.kcassell.cluster.frequentitemsets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * This class associates a transaction's (client class's calls) items (class members)
 * with a number representing its count (or support, or ...).
 * @author Keith Cassell
 */
public class ItemSupportList {
	
	/** Different ways to order the item list - by key/name, by value/support. */
	public static enum SORT_BY {
		UNSORTED,
		KEY,
		VALUE
	}
	
	/** The client class (transaction) */
	protected String classHandle = null;
	
	/** Indicates how the hash map should be ordered. */
	protected SORT_BY sortBy = SORT_BY.UNSORTED;
	
	/** Keeps track of the support for each item (called method).
	 * The key is the client method's handle; the value is its support. */
	protected Map<String, Double> supportMap = new HashMap<String, Double>();
	
	/** Indicates whether the members list has been updated since the suportMap
	 * has changed.
	 */
	protected boolean isDirty = true;

	/**
	 * This is an ordered list of the items. It serves as a cache of items in
	 * the support Map and is dependent on the isDirty flag, so it is not guaranteed
	 * to be up to date.
	 */
	protected List<String> members = new ArrayList<String>();

	/**
	 * @param classHandle
	 * @param calledMembers
	 */
	public ItemSupportList(String classHandle, Collection<String> calledMembers) {
		super();
		this.classHandle = classHandle;
//		supportMap = new HashMap<String, Double>();

		if (calledMembers != null) {
			for (String handle : calledMembers) {
				supportMap.put(handle, 1.0);
			}
		}
		isDirty = true;
	}
	
	/**
	 * @param classHandle
	 * @param calledMembers
	 */
	public ItemSupportList(String classHandle, List<String> calledMembers,
			SORT_BY sortBy) {
		super();
		this.classHandle = classHandle;
		this.sortBy = sortBy;
		
//		if (sortBy == SORT_BY.UNSORTED) {
//			supportMap = new HashMap<String, Double>();
//		} else if (sortBy == SORT_BY.KEY) {
//			supportMap = new TreeMap<String, Double>();
//		} else if (sortBy == SORT_BY.VALUE) {
//			supportMap = new TreeMap<String, Double>(new ValueComparator());
//		}
		
		for (String handle : calledMembers) {
			supportMap.put(handle, 1.0);
		}
		isDirty = true;
	}
	
	public Double addSupport(String handle, Double newSupport) {
		Double support = newSupport;
		if (supportMap.containsKey(handle)) {
			Double oldSupport = supportMap.get(handle);
			if (oldSupport != null) {
				support = newSupport.doubleValue() + oldSupport.doubleValue();
			}
		}
		supportMap.put(handle, support);
		isDirty = true;
		return support;
	}
	
	public void setSupport(String handle, Double newSupport) {
		supportMap.put(handle, newSupport);
		isDirty = true;
	}
	
	public Double getSupport(String handle) {
		return supportMap.get(handle);
	}
	
	/**
	 * 
	 * @param byWhat indicates whether the keys, values, or neither determines the
	 * ordering of the results
	 * @return the keys ordered based on the byWhat value
	 */
	public List<String> sortCalledMembers(SORT_BY byWhat) {
		sortBy = byWhat;
		members = new ArrayList<String>(supportMap.keySet());
		if (SORT_BY.VALUE.equals(byWhat)) {
			Collections.sort(members, new ValueComparator());
		} else if (SORT_BY.KEY.equals(byWhat)) {
			Collections.sort(members);
		}
		isDirty = false;
		return members;
	}

	public List<String> getMembers() {
		if (isDirty) {
			sortCalledMembers(sortBy);
		}
		return members;
	}
	
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer(
				"ItemSupportList [classHandle=" + classHandle + 
				", sortBy=" + sortBy +
				", isDirty=" + isDirty +
				", supportMap=\n");
		Set<Entry<String,Double>> entrySet = supportMap.entrySet();
		for (Entry<String,Double> entry : entrySet) {
			buf.append("\t").append(entry.getKey());
			buf.append(":\t").append(entry.getValue()).append("\n");
		}
		return buf.toString();
	}


	protected class ValueComparator implements Comparator<String> {

		/**
		 * We want numerically decreasing values in our list, so this reverses
		 * the usual meaning of compare for numbers.
		 */
		public int compare(String a, String b) {
			int result = 0;
			Double aValue = supportMap.get(a);
			Double bValue = supportMap.get(b);
			
			if (aValue != null) {
				result = -1 * aValue.compareTo(bValue);
				
				// If the values are the same, then compare the keys
				if (result == 0) {
					result = a.compareTo(b);
				}
			} else if (bValue != null) {
				result = -1;
			}
			return result;
		}
	}

}
