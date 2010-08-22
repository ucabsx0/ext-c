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

package nz.ac.vuw.ecs.kcassell.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;

/**
 * Utility functions for finding relationships between classes and
 * their members.
 * @author Keith Cassell
 */
public class EclipseSearchUtils implements IJavaSearchConstants {

	/**
	 * Find all classes that this class accesses.
	 * @param caller the calling class
	 * @return the handles of the classes that have members
	 *  accessed by this class
	 */
	public static Set<String> calculateCalledClasses(IJavaElement caller)
			throws CoreException {
		Set<String> servers = null;
		SearchEngine engine = new SearchEngine();
		ServerClassCollector collector = new ServerClassCollector(caller);
		engine.searchDeclarationsOfReferencedTypes(caller, collector, null);
		servers = collector.getResult();
		return servers;
	}
	
	/**
	 * Find all classes that access methods or fields in this class
	 * from within the same project.
	 * @param source metrics for a particular IType
	 * @return the handles of the classes that have methods that
	 *  reference methods or fields in this class
	 */
	public static Set<String> calculateCallingClasses(IType aType)
			throws CoreException {
		Set<String> clients = null;
		SearchPattern pattern = SearchPattern.createPattern(aType,
				REFERENCES);
		IJavaSearchScope scope = createProjectSearchScope(aType);
		SearchEngine engine = new SearchEngine();
		ClientClassCollector collector = new ClientClassCollector();
		SearchParticipant[] participants = new SearchParticipant[] { SearchEngine
				.getDefaultSearchParticipant() };
		engine.search(pattern, participants, scope, collector, null);
		clients = collector.getResult();
		return clients;
	}

	/**
	 * Find all methods that this element accesses.
	 * @param caller the calling compilation unit or source type/method/field
	 * @return the methods called by this element
	 */
	public static Set<IMethod> calculateCalledMethods(IJavaElement caller)
			throws CoreException {
		Set<IMethod> called = null;

		SearchEngine engine = new SearchEngine();
		MethodCollector collector = new MethodCollector();
		engine.searchDeclarationsOfSentMessages(caller, collector, null);
		called = collector.getResults();
		return called;
	}
	
	/**
	 * Create a search scope consisting of this element's project.
	 * @param element
	 * @return the scope
	 */
	public static IJavaSearchScope createProjectSearchScope(IJavaElement element)
			throws JavaModelException {
		IJavaProject project =
			(IJavaProject) element.getAncestor(IJavaElement.JAVA_PROJECT);
	    IJavaSearchScope scope =
	    	SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
		return scope;
	}

	/**
	 * Returns the members of the specified type
	 * @param type the type/class of interest
	 * @param getInherited if true, both local and inherited members are
	 * returned. If false, then only locally declared members are returned.
	 * @return a collection of members of the type
	 * see org.eclipse.jdt.internal.ui.typehierarchy.MethodsContentProvider
	 */
	public static List<IMember> getMembers(IType type, boolean getInherited)
			throws JavaModelException {
		List<IMember> members = getDeclaredMembers(type);
		ITypeHierarchy hierarchy = type.newSupertypeHierarchy(null);

		if (getInherited) {
			ApplicationParameters params = ApplicationParameters.getSingleton();
			boolean filterObject = params.getBooleanParameter(
					ParameterConstants.FILTER_KEY, true);
			IType[] supers = hierarchy.getAllSuperclasses(type);
			// Determine whether or not to include Object's methods
			int limit = filterObject ? (supers.length - 1) : supers.length;

			for (int i = 0; i < limit; i++) {
				IType superType = supers[i];
				if (superType.exists()) {
					addDesiredMethods(superType, members);
					// addAll(superType.getInitializers(), res);
					addDesiredFields(superType, members);
				}
			}
		}
		return members;
	}

	/**
	 * Returns the methods of the specified type
	 * @param type the type/class of interest
	 * @param getInherited if true, both local and inherited methods are
	 * returned. If false, then only locally declared methods are returned.
	 * @return a collection of methods of the type
	 * see org.eclipse.jdt.internal.ui.typehierarchy.MethodsContentProvider
	 */
	public static List<IMethod> getMethods(IType type, boolean getInherited)
			throws JavaModelException {
		List<IMethod> methods = new ArrayList<IMethod>();
		ApplicationParameters params = ApplicationParameters.getSingleton();
		boolean filterObject = params.getBooleanParameter(
				ParameterConstants.FILTER_KEY, true);
		addDesiredMethods(type, methods);

		if (getInherited) {
			ITypeHierarchy hierarchy = type.newSupertypeHierarchy(null);
			IType[] supers = hierarchy.getAllSuperclasses(type);
			// Determine whether or not to include Object's methods
			int limit = filterObject ? (supers.length - 1) : supers.length;

			for (int i = 0; i < limit; i++) {
				IType superType = supers[i];
				if (superType.exists()) {
					addDesiredMethods(superType, methods);
				}
			}
		}
		return methods;
	}

	/**
	 * Returns the fields of the specified type
	 * @param type the type/class of interest
	 * @param getInherited if true, both local and inherited fields are
	 * returned. If false, then only locally declared fields are returned.
	 * @return a collection of fields of the type
	 * see org.eclipse.jdt.internal.ui.typehierarchy.MethodsContentProvider
	 */
	public static List<IField> getFields(IType type, boolean getInherited)
			throws JavaModelException {
		List<IField> fields = new ArrayList<IField>();
		addDesiredFields(type, fields);

		if (getInherited) {
			ITypeHierarchy hierarchy = type.newSupertypeHierarchy(null);
			IType[] supers = hierarchy.getAllSuperclasses(type);
			// sort from last to first: elements with same name
			// will show up in hierarchy order
			for (int i = 0; i < supers.length; i++) {
				IType superType = supers[i];
				if (superType.exists()) {
					addDesiredFields(superType, fields);
				}
			}
		}
		return fields;
	}

	/**
	 * Get the declared members (methods and fields) of the
	 * specified class.
	 * @param classHandle the Eclipse handle for the class
	 * @return a collection members
	 * @throws JavaModelException
	 */
	public static List<IMember> getDeclaredMembers(String classHandle)
			throws JavaModelException {
		List<IMember> members = new ArrayList<IMember>();
		IJavaElement element = JavaCore.create(classHandle);

		if (element == null) {
			System.err.println("  No element created from " + classHandle);
		} else if (element instanceof IType) {
			IType type = (IType) element;
			members = getDeclaredMembers(type);
		}
		return members;
	}

	/**
	 * Get the declared members (methods and fields) of the
	 * specified class.
	 * @param classHandle the Eclipse handle for the class
	 * @return a collection members
	 * @throws JavaModelException
	 */
	protected static List<IMember> getDeclaredMembers(IType type)
			throws JavaModelException {
		List<IMember> members = new ArrayList<IMember>();
		addDesiredFields(type, members);
		addDesiredMethods(type, members);
		//TODO initializers and inner classes?
		return members;
	}

	/**
	 * Adds to the list of members only those fields meeting the
	 * criteria specified by the user's parameters.
	 * @param type the type/class whose fields are to be considered
	 * @param members the accumulating results (the list of members)
	 * @throws JavaModelException
	 */
	protected static void addDesiredFields(IType type,
			List<? super IField> members) throws JavaModelException {
		IField[] fields = type.getFields();
		//TODO should filtering be here or in the display (or both?)
		ApplicationParameters params = ApplicationParameters.getSingleton();
		boolean includeStatic = params.getBooleanParameter(
				ParameterConstants.INCLUDE_STATIC_KEY, true);

		for (IField field : fields) {
			if (field != null) {
				int flags = field.getFlags();
				if (includeStatic || !Flags.isStatic(flags)) {
					members.add(field);
				}
			}
		}
	}

	/**
	 * Adds to the list of members only those methods meeting the
	 * criteria specified by the user's parameters.
	 * @param type the type/class whose methods are to be considered
	 * @param members the accumulating results (the list of members)
	 * @throws JavaModelException
	 */
	protected static void addDesiredMethods(IType type,
			List<? super IMethod> members) throws JavaModelException {
		//TODO should filtering be here or in the display (or both?)
		IMethod[] methods = type.getMethods();
		ApplicationParameters params = ApplicationParameters.getSingleton();
		boolean includeConstructors = params.getBooleanParameter(
				ParameterConstants.INCLUDE_CONSTRUCTORS_KEY, false);
		boolean includeStatic = params.getBooleanParameter(
				ParameterConstants.INCLUDE_STATIC_KEY, true);
		boolean filterObject = params.getBooleanParameter(
				ParameterConstants.FILTER_KEY, true);
		for (IMethod method : methods) {
			if (method != null) {
				String methodHandle = method.getHandleIdentifier();
				int flags = method.getFlags();
				if ((!filterObject || !EclipseUtils.isObjectMethod(methodHandle))
						&& (includeConstructors || !method.isConstructor())
						&& (includeStatic || !Flags.isStatic(flags))
						) {
					members.add(method);
				}
			}
		} // for
	}

	/**
	 * 
	 * @param aType
	 * @return all the methods that are required by a superclass or an interface
	 * @throws JavaModelException
	 */
	private static List<IMethod> getRequiredMethods(IType aType)
			throws JavaModelException {
		List<IMethod> requiredMethods = new ArrayList<IMethod>();
		IMethod[] theseMethods = aType.getMethods();

		ITypeHierarchy hierarchy = aType.newSupertypeHierarchy(null);
		IType[] supertypes = hierarchy.getAllSupertypes(aType);
		HashSet<IMethod> allSuperMethods = new HashSet<IMethod>();

		for (IType supertype : supertypes) {
			IMethod[] methods = supertype.getMethods();
			List<IMethod> superMethods = Arrays.asList(methods);
			allSuperMethods.addAll(superMethods);
		}

		// Check each method to see if it is declared elsewhere
		for (IMethod method : theseMethods) {
			for (IMethod superMethod : allSuperMethods) {
				if (method.isSimilar(superMethod)) {
					requiredMethods.add(superMethod);
					break;
				}
			}
		}
		return requiredMethods;
	}

	/**
	 * @param type the type whose superclasses are desired
	 * @return all supertypes, in bottom up order
	 * @throws JavaModelException
	 */
	public static IType[] getSupertypes(IType type) throws JavaModelException {
		ITypeHierarchy typeHierarchy = type.newSupertypeHierarchy(null);
		IType[] supertypes = typeHierarchy.getAllSupertypes(type);
		return supertypes;
	}
	
	/**
	 * @param child
	 * @param parent
	 * @return true if parent is a supertype of child; false otherwise
	 * @throws JavaModelException
	 */
	public static boolean hasSupertype(IType child, IType parent)
	throws JavaModelException {
		IType[] supertypes = getSupertypes(child);
		boolean hasSuper = false;
		int i = 0;
		while (!hasSuper && i < supertypes.length) {
			hasSuper = parent.equals(supertypes[i]);
			i++;
		}
		return hasSuper;
	}
	
	/**
	 * Collects all types that are defined within the specified one -
	 * inner classes, anonymous inner classes, ...
	 * @param type
	 * @return a collection of types
	 * @throws CoreException
	 */
	public static Set<IType> getEmbeddedTypes(IType type)
			throws CoreException {
		SearchEngine searchEngine = new SearchEngine();
		SearchParticipant[] participants =
			new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() };
		String namePattern = "*" + type.getElementName() + "*";
//		String namePattern = type.getFullyQualifiedName('$') + "*";
		SearchPattern pattern = SearchPattern.createPattern(namePattern,
				TYPE, DECLARATIONS, SearchPattern.R_PATTERN_MATCH);
//		SearchPattern pattern = SearchPattern.createPattern(
//				type, DECLARATIONS);
		IJavaSearchScope scope =
			SearchEngine.createJavaSearchScope(new IJavaElement[] {type});
		EmbeddedClassCollector collector = new EmbeddedClassCollector(type);
		searchEngine.search(pattern, participants, scope,
				collector, null);
		Set<IType> embedded = collector.getResult();
		return embedded;
	}

	/**
	 * Collects the methods that access the specified member
	 * @param member the field or method whose accessors are being determined
	 * @param scope the elements being examined, e.g. this class or this package
	 * @return the collection of methods that access the indicated member
	 */
	public static Set<IMethod> getCallingMethods(
			IMember member,
			IJavaSearchScope scope)
			throws CoreException {
		MethodCollector methodCollector = new MethodCollector();
		SearchEngine searchEngine = new SearchEngine();
		SearchParticipant[] participants =
			new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() };
		SearchPattern callingMethodPattern = SearchPattern.createPattern(
				member, REFERENCES);
		searchEngine.search(callingMethodPattern, participants, scope,
				methodCollector, null);
		Set<IMethod> callers = methodCollector.getResults();
		return callers;
	}

	
	/**
	 * Uses the JDT SearchEngine to collect all ITypes
	 * that directly depend on members in the specified IType
	 */
	private static class ClientClassCollector extends SearchRequestor {

		/** The set of handles of ITypes (the calling classes). */
		private Set<String> results = null;

		public ClientClassCollector() {
		}

		/** @return The set of handles of ITypes (the calling classes). */
		public Set<String> getResult() {
			return results;
		}

		/**
		 * Sets up an empty collection to contain the results
		 * @see org.eclipse.jdt.core.search.SearchRequestor#beginReporting()
		 */
		@Override
		public void beginReporting() {
			results = new HashSet<String>();
		}

		/**
		 * Adds the handle of the IType that contains this element to the results.
		 * @see org.eclipse.jdt.core.search.SearchRequestor#acceptSearchMatch(org
		 * .eclipse.jdt.core.search.SearchMatch)
		 */
		@Override
		public void acceptSearchMatch(SearchMatch match) throws CoreException {
			IJavaElement enclosingElement = (IJavaElement) match.getElement();
			if (enclosingElement != null) {
				IJavaElement element =
					enclosingElement.getAncestor(IJavaElement.TYPE);
				if (element != null) {
					results.add(element.getHandleIdentifier());
				}
			}
		}

	}	// class ClientCollector
	

	/**
	 * Collects all ITypes (from the same project) that the specified element
	 *  directly depends on.
	 */
	private static class ServerClassCollector extends SearchRequestor
	// implements IJavaSearchResultCollector
	{
		/** The project of the IType. */
		IJavaProject callerProject = null;

		/** The handles of the classes that are depended on. */
		Set<String> results = null;

		public ServerClassCollector(IJavaElement callerElement) {
			callerProject = callerElement.getJavaProject();
		}

		/** @return The set of handles of ITypes (the called classes). */
		public Set<String> getResult() {
			return results;
		}

		/**
		 * Sets up an empty collection to contain the results
		 * @see org.eclipse.jdt.core.search.SearchRequestor#beginReporting()
		 */
		@Override
		public void beginReporting() {
			results = new HashSet<String>();
		}

		/**
		 * Adds the handle of the IType that contains this element to the results.
		 * @see org.eclipse.jdt.core.search.SearchRequestor#acceptSearchMatch(org
		 * .eclipse.jdt.core.search.SearchMatch)
		 */
		@Override
		public void acceptSearchMatch(SearchMatch match) throws CoreException {
			IJavaElement enclosingElement = (IJavaElement) match.getElement();
			if (enclosingElement != null) {
				IJavaElement typeElement =
					enclosingElement.getAncestor(IJavaElement.TYPE);

				if (typeElement != null) {
					IJavaProject calleeProject = typeElement.getJavaProject();

					// Only add Types from the same project
					if (callerProject != null
							&& callerProject.equals(calleeProject)) {
						results.add(typeElement.getHandleIdentifier());
					}
				}
			}
		}

	}	// class ServerCollector


	/**
	 * Collects all ITypes (from the same project) that are defined within
	 * the specified element.
	 */
	private static class EmbeddedClassCollector extends SearchRequestor
	{
		/** The embedded classes. */
		Set<IType> results = null;
		
		/** The element whose scope encompasses the embedded classes. */
		IJavaElement containingElement = null;

		public EmbeddedClassCollector(IJavaElement containingElement) {
			this.containingElement = containingElement;
		}

		/** @return The set of embedded classes. */
		public Set<IType> getResult() {
			return results;
		}

		/**
		 * Sets up an empty collection to contain the results
		 * @see org.eclipse.jdt.core.search.SearchRequestor#beginReporting()
		 */
		@Override
		public void beginReporting() {
			results = new HashSet<IType>();
		}

		/**
		 * Adds the embedded IType.
		 * @see org.eclipse.jdt.core.search.SearchRequestor#acceptSearchMatch(org
		 * .eclipse.jdt.core.search.SearchMatch)
		 */
		@Override
		public void acceptSearchMatch(SearchMatch match) throws CoreException {
			IType embeddedElement = (IType)match.getElement();
			
			if (!containingElement.equals(embeddedElement)) {
				results.add(embeddedElement);
			}
		}

	}	// class EmbeddedClassCollector


	/**
	 * Collect all methods (except those filtered out by keepStaticMethods)
	 *  that call the specified method
	 */
	private static class MethodCollector extends SearchRequestor {
		/** Flag to determine whether information about static methods should be
		 * kept. */
		//TODO parameters
		protected static boolean keepStaticMethods = true;

		protected Set<IMethod> results = null;

		public MethodCollector() {
		}

		public Set<IMethod> getResults() {
			return results;
		}

		/**
		 * @see org.eclipse.jdt.core.search.SearchRequestor#beginReporting()
		 */
		public void beginReporting() {
			results = new HashSet<IMethod>();
		}

		/** 
		 * @see
		 * org.eclipse.jdt.core.search.SearchRequestor#acceptSearchMatch(org
		 * .eclipse.jdt.core.search.SearchMatch)
		 */
		public void acceptSearchMatch(SearchMatch match) throws CoreException {
			Object matchingElement = match.getElement();

			if (matchingElement instanceof IMethod) {
				IMethod method = (IMethod) matchingElement;
				int flags = method.getFlags();
				if (keepStaticMethods || !Flags.isStatic(flags)) {
					results.add(method);
				}
			}
		}

		/**
		 * @see org.eclipse.jdt.core.search.SearchRequestor#endReporting()
		 */
		public void endReporting() {
		}

	}	// MethodCollector


}
