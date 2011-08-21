package nz.ac.vuw.ecs.kcassell.similarity;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import nz.ac.vuw.ecs.kcassell.utils.EclipseUtils;
import nz.ac.vuw.ecs.kcassell.utils.ObjectPersistence;
import nz.ac.vuw.ecs.kcassell.utils.RefactoringConstants;

import org.eclipse.jdt.core.JavaModelException;

import edu.ucla.sspace.common.KACSemanticSpace;
import edu.ucla.sspace.common.Similarity;
import edu.ucla.sspace.vector.DoubleVector;
import edu.ucla.sspace.vector.Vector;
import edu.ucla.sspace.vsm.VectorSpaceModel;

/**
 * The SemanticsCalculator calculates distances between "documents"
 * based on the "terms" in those documents.  Examples:
 * (1) A "document" could be a Java identifier with its terms being the 
 * stemmed parts of the identifier.
 * (2) A "document" could be a Java class with its terms being the members 
 * it accesses.
 * @author kcassell
 *
 */
public class SemanticsCalculator 
implements DistanceCalculatorIfc<String>, RefactoringConstants, Serializable {

	public static final double MAX_CONCEPTUAL_DISTANCE = 1.0;

	private static final long serialVersionUID = 1L;
	
	/** Maps a project name to the calculator for that project. */
	protected static transient Hashtable<String, SemanticsCalculator> calculatorMap =
		new Hashtable<String, SemanticsCalculator>();
	
	/** The name of the project can be used to identify the
	 * serialized calculator. 	 */
	protected String projectName = null;
	
	/** The model maintains document vectors
	 * where class members are documents and the "words" are
	 * stemmed parts of identifiers.  This will be either a
	 * VectorSpaceModel or a LatentSemanticAnalysis object.  */
	protected KACSemanticSpace semanticSpace = null;
	
	/** Maintains a mapping from the member handle to the S-Space
	 *  document number. */
	protected Map<String, Integer> memberHandleToDocumentNumber =
		new HashMap<String, Integer>();

	/**
	 * Construct the calculator, building the semantic space
	 * based on the contents of the file provided
	 * @param fileName the name of the file that contains
	 * one member per line.  The first token is the member handle, and the 
	 * remaining tokens are the stemmed words found in identifiers and comments.
	 * @throws IOException
	 */
	protected SemanticsCalculator(String handle)
	throws IOException {
//		vsmHandle = handle;
		projectName = EclipseUtils.getProjectNameFromHandle(handle);
//		String fileName = getDataFileNameFromHandle(handle);
//		initializeVectorSpace(fileName);
		calculatorMap.put(projectName, this);
	}
	
	/**
	 * Get a SemanticsCalculator appropriate for the Eclipse handle.
	 * (This should be the calculator for the corpus/project.)
 	 * If there is already a calculator in memory, use it.
	 * Else, if there is calculator on disk, restore it.
	 * Otherwise, create a new calculator and save it to disk
	 * @param handle the Eclipse handle of the element whose
	 *   calculator we desire.
	 * @return the calculator
	 */
	public static SemanticsCalculator getCalculator(String handle) {
		String aProjectName = EclipseUtils.getProjectNameFromHandle(handle);
		// If there is already a calculator in memory, use it.
		SemanticsCalculator calculator = calculatorMap.get(aProjectName);
		
		// Else, if there is calculator on disk, restore it.
		if (calculator == null) {
			calculator = restore(aProjectName);
			
			// Otherwise, create a new calculator
			if (calculator == null) {
				try {
					calculator = new SemanticsCalculator(handle);
					String fileName = calculator.getDataFileNameFromHandle(handle);
					calculator.initializeVectorSpace(fileName);
					calculator.save(aProjectName);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if (calculator != null) {
				calculatorMap.put(aProjectName, calculator);
			}
		}
		return calculator;
	}
	
	/**
	 * Based on an Eclipse handle, retrieve the file name for the
	 * text file containing the corpus of documents.
	 * @param handle an Eclipse handle
	 * @return the file name of the corpus
	 */
	public String getDataFileNameFromHandle(String handle) {
//		String className = EclipseUtils.getNameFromHandle(handle);
		projectName = EclipseUtils.getProjectNameFromHandle(handle);
	    String memberDocumentFile = DATA_DIR +
			"MemberDocuments/" + projectName + "/" +
			projectName + "Members.txt";
//		className + "Members.txt";
	    // TODO remove debug
	    System.out.println("memberDocumentFile = " + memberDocumentFile);
	    return memberDocumentFile;
	}

	/**
	 * Save the serialized version of this calculator.
	 */
	public void save(String name) {
		String serializationFile = getSerializationFileName(name);
		try {
			ObjectPersistence.saveToFile(this, serializationFile);
		} catch (Exception e) {
			ObjectPersistence.handleSerializationException(
					"Unable to write to " + serializationFile, e);
		}
	}

	protected static String getSerializationFileName(String name) {
		String serializationFile = DATA_DIR +
			"MemberDocuments/" + name + "/" + name + ".ser";
		return serializationFile;
	}

	/**
	 * Recreates a calculator from a file containing the serialized object.
	 * @param name the name of the project to restore
	 */
	public static SemanticsCalculator restore(String name) {
		SemanticsCalculator calc = null;
		String serializationFile = getSerializationFileName(name);
		try {
			Object object = ObjectPersistence.readFromFile(serializationFile);
			calc = (SemanticsCalculator)object;
		} catch (FileNotFoundException e) {
		} catch (Exception e) {
			ObjectPersistence.handleSerializationException(
					"Unable to read SemanticsCalculator from " + serializationFile, e);
		}
		return calc;
	}

	/**
	 * Process a file that contains all of the members in a class.
	 * @param fileName the name of the file that contains
	 * one member per line.  The first token is the member handle, and the 
	 * remaining tokens are the stemmed words found in identifiers.
	 * @throws IOException
	 */
	public KACSemanticSpace initializeVectorSpace(String fileName)
	throws IOException {
		semanticSpace = new VectorSpaceModel();
		BufferedReader documentFileReader = new BufferedReader(new FileReader(
				fileName));
		int lineNum = 0;
		String line = null;
		String memberName = null;
		while ((line = documentFileReader.readLine()) != null) {
			memberName = processMemberDocument(semanticSpace, line);
			
			if (semanticSpace.getProcessedDocument()) {
				memberHandleToDocumentNumber.put(memberName, lineNum++);
			} else {
				System.out.println("processMemberDocument failed for " + line);
			}
		}
		semanticSpace.processSpace(System.getProperties());
//		int vsmColumns = vectorSpaceModel.getVectorLength();
//		int docsRead = memberHandleToDocumentNumber.size();
		return semanticSpace;
	}

	/**
	 * Process a line from a file that contains all of the members in a class.
	 * @param vsm
	 * @param line the first token is the member handle, and the 
	 * remaining tokens are the words found in identifiers and comments.
	 * @throws IOException
	 */
	protected static String processMemberDocument(KACSemanticSpace vsm,
			String line) throws IOException {
		String memberName;
		String restOfMember;
		int spaceIndex = line.indexOf(' ');
		if (spaceIndex > -1) {
			memberName = line.substring(0, spaceIndex);
			if (spaceIndex < line.length() - 2) {
				restOfMember = line.substring(spaceIndex + 1);
			} else {
				restOfMember = "";
			}
			StringReader stringReader = new StringReader(restOfMember);
			BufferedReader memberTokensReader =
				new BufferedReader(stringReader);
			vsm.processDocument(memberTokensReader);
		} else {
			memberName = line;
		}
		return memberName;
	}

//	/**
//	 * @throws IOException 
//	 * @see http://code.google.com/p/airhead-research/wiki/FrequentlyAskedQuestions#How_can_I_convert_a_.sspace_file_to_a_matrix?
//	 */
//	public void saveSemanticSpace(SemanticSpace sspace, File outputMatrixFile,
//			MatrixIO.Format fmt)
//	throws IOException {
//		Set<String> wordSet = sspace.getWords();
//		int numWords = wordSet.size();
//		DoubleVector[] wordVectors = new DoubleVector[numWords];
//		int i = 0;
//		for (String word : wordSet) {
//		    Vector<?> wordVector = sspace.getVector(word);
//			wordVectors[i] = Vectors.asDouble(wordVector);
//		    i++;
//		}                                                                               
//		Matrix wordMatrix = Matrices.asMatrix(Arrays.asList(wordVectors));
//
//		// If you then want to write the matrix out to disk, do the following
//		MatrixIO.writeMatrix(wordMatrix, outputMatrixFile, fmt);
//		// Reminder, if you still want the word-to-row mapping, write out the words array too
//	}
	
	/**
	 * Calculates the conceptual cohesion of the class, which is the average
	 * conceptual similarity of its methods.
	 * @param handle1 the Eclipse handle of a class
	 * @return the conceptual cohesion of the class
	 * @throws JavaModelException 
	 */
	public Double calculateConceptualCohesion(String classHandle)
	throws JavaModelException {
		Double cohesion = 0.0;
		List<String> methodHandles =
			EclipseUtils.getFilteredMemberHandles(classHandle);
		int numMethods = methodHandles.size();
		int numPairs = 0;
		Double total = 0.0;
		if (numMethods > 1) {
			for (int i = 0; i < numMethods; i++) {
				String handleI = methodHandles.get(i);
				for (int j = i + 1; j < numMethods; j++) {
					String handleJ = methodHandles.get(j);
					Number distance = calculateDistance(handleI, handleJ);
					double similarity = 1.0 - distance.doubleValue();
					numPairs++;
					total += similarity;
				}
			}
			cohesion = total/numPairs;
		}
		if (cohesion < 0.0) {
			cohesion = 0.0;
		}
		return cohesion;
	}
	
	/**
	 * Calculates the distance between two documents, e.g. two identifiers, based
	 * on the similarity of the words in the documents
	 * @param handle1 the Eclipse handle of a class member
	 * @param handle2 the Eclipse handle of a class member
	 * @return the distance between the documents corresponding to the handles
	 */
	public Number calculateDistance(String handle1, String handle2) {
		double distance = UNKNOWN_DISTANCE.doubleValue();
		Integer documentInt1 = memberHandleToDocumentNumber.get(handle1);
		Integer documentInt2 = memberHandleToDocumentNumber.get(handle2);
		
		if (documentInt1 != null && documentInt2 != null) {
			try {
				DoubleVector vector1 = semanticSpace.getDocumentVector(documentInt1);
				try {
					DoubleVector vector2 = semanticSpace.getDocumentVector(documentInt2);
					distance = calculateCosineDistance(vector1, vector2);
				} catch (IllegalArgumentException e) {
					System.err.println("No document vector found for " + handle2);
				}
			} catch (IllegalArgumentException e) {
				System.err.println("No document vector found for " + handle1);
			}
		}
		if (distance < 0.0) {
			distance = MAX_CONCEPTUAL_DISTANCE;
		}
		return distance;
	}

	/**
	 * Returns a number between 0 and 1 indicating how distant (dissimilar)
	 * two vectors are.
	 * @param vector1
	 * @param vector2
	 * @return the distance - 0 distance indicates maximum similarity;
	 * 1 indicates minimal similarity
	 */
	protected static double calculateCosineDistance(Vector<?> vector1,
			Vector<?> vector2) {
		double distance = 1 - Similarity.cosineSimilarity(vector1, vector2);
		if (distance < 0.0 || distance == UNKNOWN_DISTANCE.doubleValue()) {
			distance = 0.0;
		}
		return distance;
	}

	/**
	 * Calculates the distance between two terms, e.g. two class members, based
	 * on the similarity of the documents (e.g. classes) that contain them
	 * @param handle1 the Eclipse handle of a class member
	 * @param handle2 the Eclipse handle of a class member
	 * @return the distance between the documents corresponding to the handles
	 */
	public Number calculateDistanceBetweenTerms(String handle1, String handle2) {
		double distance = UNKNOWN_DISTANCE.doubleValue();
		Vector<?> vector1 = semanticSpace.getVector(handle1);
		Vector<?> vector2 = semanticSpace.getVector(handle2);
		distance = calculateCosineDistance(vector1, vector2);
		return distance;
	}

	public DistanceCalculatorEnum getType() {
		return DistanceCalculatorEnum.VectorSpaceModel;
	}
	
}
