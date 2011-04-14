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
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;

import nz.ac.vuw.ecs.kcassell.cluster.frequentitemsets.ItemSupportList;
import nz.ac.vuw.ecs.kcassell.utils.EclipseSearchUtils;
import nz.ac.vuw.ecs.kcassell.utils.EclipseUtils;

public class FrequentMethodsMiner {

	public Collection<ItemSupportList> getFrequentFrequentlyUsedMethods(String handle) throws CoreException {
		IJavaElement type = EclipseUtils.getTypeFromHandle(handle);
		IJavaProject project = type.getJavaProject();
		IJavaSearchScope scope =
			SearchEngine.createJavaSearchScope(new IJavaElement[] { project });
		Set<IMethod> callingMethods =
			EclipseSearchUtils.calculateCallingMethods(type , scope);
		
		HashMap<String, Set<String>> clientCallers =
			associateMethodsWithClient(callingMethods);
		ArrayList<ItemSupportList> itemSupportLists = createItemSupportLists(clientCallers);
		FPGrowthMiner fpMiner = new FPGrowthMiner();
		Collection<ItemSupportList> frequentMethods =
			fpMiner.mine(itemSupportLists, 4);
		return frequentMethods;
	}

	/**
	 * Creates item support lists for each server class
	 * @param clientCallers a map whose keys are the class identifier and whose values
	 * are the methods in that class that call client methods
	 * @return a collection of item support lists for each client class
	 * where the items in the support lists are the calling methods from
	 * the class.
	 */
	protected ArrayList<ItemSupportList> createItemSupportLists(
			HashMap<String, Set<String>> clientCallers) {
		ArrayList<ItemSupportList> itemSupportLists =
			new ArrayList<ItemSupportList>();
		Set<Entry<String,Set<String>>> entrySet = clientCallers.entrySet();
		for (Entry<String,Set<String>> entry : entrySet) {
			String className = entry.getKey();
			Set<String> classMethods = entry.getValue();
			ItemSupportList itemSupportList =
				new ItemSupportList(className, classMethods, null);
			itemSupportLists.add(itemSupportList);
		}
		return itemSupportLists;
	}

	/**
	 * Associate all of the calling methods with their classes using a hash map
	 * @param callingMethods all of the methods that call the client class
	 * @return a map whose keys are the class identifier and whose values
	 * are the methods in that class that call client methods
	 */
	protected HashMap<String, Set<String>> associateMethodsWithClient(
			Set<IMethod> callingMethods) {
		// 
		HashMap<String, Set<String>> clientCallers =
			new HashMap<String, Set<String>>();
		for (IMethod method : callingMethods) {
			IType declaringType = method.getDeclaringType();
			String typeName = declaringType.getElementName();
			// TODO String typeHandle = declaringType.getHandleIdentifier();
			String methodName = method.getElementName();
			// TODO use handle instead
			Set<String> callerSet = clientCallers.get(typeName);
			if (callerSet == null) {
				callerSet = new HashSet<String>();
				clientCallers.put(typeName, callerSet);
			}
			callerSet.add(methodName);
		}
		return clientCallers;
	}


}
