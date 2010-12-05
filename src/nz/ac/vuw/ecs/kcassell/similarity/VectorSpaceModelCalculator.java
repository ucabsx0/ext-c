package nz.ac.vuw.ecs.kcassell.similarity;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import nz.ac.vuw.ecs.kcassell.utils.EclipseUtils;
import nz.ac.vuw.ecs.kcassell.utils.ObjectPersistence;
import nz.ac.vuw.ecs.kcassell.utils.RefactoringConstants;
import edu.ucla.sspace.common.Similarity;
import edu.ucla.sspace.vector.DoubleVector;
import edu.ucla.sspace.vsm.VectorSpaceModel;

public class VectorSpaceModelCalculator 
implements DistanceCalculatorIfc<String>, Serializable {

	private static final long serialVersionUID = 1L;
	
	private static transient Hashtable<String, VectorSpaceModelCalculator> calculatorMap =
		new Hashtable<String, VectorSpaceModelCalculator>();
	
	/** The name of the project can be used to identify the
	 * serialized calculator. 	 */
	private String projectName = null;

	/** The vector space model maintains document vectors
	 * where class members are documents and the "words" are
	 * stemmed parts of identifiers. */
	private VectorSpaceModel vectorSpaceModel = null;
	
	/** Maintains a mapping from the member handle to the S-Space
	 * VectorSpaceModel's document number. */
	private Map<String, Integer> memberHandleToDocumentNumber =
		new HashMap<String, Integer>();

	/**
	 * Construct the calculator, building the vector space model
	 * based on the contents of the file provided
	 * @param fileName the name of the file that contains
	 * one member per line.  The first token is the member handle, and the 
	 * remaining tokens are the stemmed words found in identifiers and comments.
	 * @throws IOException
	 */
	protected VectorSpaceModelCalculator(String handle)
	throws IOException {
		String fileName = getMemberDataFileFromHandle(handle);
		initializeVectorSpace(fileName);
		calculatorMap.put(projectName, this);
	}
	
	/**
	 * Get a VectorSpaceModelCalculator appropriate for the Eclipse handle.
	 * (This should be the calculator for the corpus/project.)
 	 * If there is already a calculator in memory, use it.
	 * Else, if there is calculator on disk, restore it.
	 * Otherwise, create a new calculator and save it to disk
	 * @param handle the Eclipse handle of the element whose
	 *   calculator we desire.
	 * @return the calculator
	 */
	public static VectorSpaceModelCalculator getCalculator(String handle) {
		String aProjectName = EclipseUtils.getProjectNameFromHandle(handle);
		// If there is already a calculator in memory, use it.
		VectorSpaceModelCalculator calculator = calculatorMap.get(aProjectName);
		
		// Else, if there is calculator on disk, restore it.
		if (calculator == null) {
			calculator = restore(aProjectName);
			
			// Otherwise, create a new calculator
			if (calculator == null) {
				try {
					calculator = new VectorSpaceModelCalculator(handle);
					calculator.save();
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
	private String getMemberDataFileFromHandle(String handle) {
//		String className = EclipseUtils.getNameFromHandle(handle);
		projectName = EclipseUtils.getProjectNameFromHandle(handle);
	    String memberDocumentFile = RefactoringConstants.DATA_DIR +
			"MemberDocuments/" + projectName + "Members.txt";
//		"MemberDocuments/" + projectName + "/" +
//		className + "Members.txt";
	    return memberDocumentFile;
	}

	/**
	 * Save the serialized version of this calculator.
	 */
	public void save() {
		String serializationFile = RefactoringConstants.DATA_DIR +
			"MemberDocuments/" + projectName + ".ser";
		try {
			ObjectPersistence.saveToFile(this, serializationFile);
		} catch (Exception e) {
			ObjectPersistence.handleSerializationException(
					"Unable to write to " + serializationFile, e);
		}
	}

	/**
	 * Recreates a calculator from a file containing the serialized object.
	 * @param name the name of the project to restore
	 */
	public static VectorSpaceModelCalculator restore(String name) {
		VectorSpaceModelCalculator calc = null;
		String serializationFile = RefactoringConstants.DATA_DIR +
			"MemberDocuments/" + name + ".ser";
		try {
			Object object = ObjectPersistence.readFromFile(serializationFile);
			calc = (VectorSpaceModelCalculator)object;
		} catch (Exception e) {
			ObjectPersistence.handleSerializationException(
					"Unable to read from " + serializationFile, e);
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
	private VectorSpaceModel initializeVectorSpace(String fileName)
	throws IOException {
		vectorSpaceModel = new VectorSpaceModel();
		BufferedReader documentFileReader = new BufferedReader(new FileReader(
				fileName));
		int lineNum = 0;
		String line = null;
		String memberName = null;
		while ((line = documentFileReader.readLine()) != null) {
			memberName = processMemberDocument(vectorSpaceModel, line);
			
			if (vectorSpaceModel.processedDocument) {
				memberHandleToDocumentNumber.put(memberName, lineNum++);
			} else {
				System.out.println("processMemberDocument failed for " + line);
			}
		}
		vectorSpaceModel.processSpace(System.getProperties());
//		int vsmColumns = vectorSpaceModel.getVectorLength();
//		int docsRead = memberHandleToDocumentNumber.size();
		return vectorSpaceModel;
	}

	/**
	 * Process a line from a file that contains all of the members in a class.
	 * @param vsm
	 * @param line the first token is the member handle, and the 
	 * remaining tokens are the words found in identifiers and comments.
	 * @throws IOException
	 */
	private static String processMemberDocument(VectorSpaceModel vsm,
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

	public Number calculateDistance(String handle1, String handle2) {
		double distance = RefactoringConstants.UNKNOWN_DISTANCE.doubleValue();
		Integer documentInt1 = memberHandleToDocumentNumber.get(handle1);
		Integer documentInt2 = memberHandleToDocumentNumber.get(handle2);
		
		if (documentInt1 != null && documentInt2 != null) {
			DoubleVector vector1 = vectorSpaceModel.getDocumentVector(documentInt1);
			DoubleVector vector2 = vectorSpaceModel.getDocumentVector(documentInt2);
			distance = 1 - Similarity.cosineSimilarity(vector1, vector2);
			if (distance < 0.0 && distance != RefactoringConstants.UNKNOWN_DISTANCE.doubleValue()) {
				distance = 0.0;
			}
		}
		return distance;
	}

	public DistanceCalculatorEnum getType() {
		return DistanceCalculatorEnum.VectorSpaceModel;
	}
	
	/**
	 * A simple test
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			VectorSpaceModelCalculator calculator =
				new VectorSpaceModelCalculator("=CohesionTests/src<nz.ac.vuw.ecs.kcassell.geometry{PointShape.java[PointShape~document~QAffineTransform;");
			//testCohesionTests(semanticAnalyzer);
			calculator.testFreecol();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

//	private static void testCohesionTests(VectorSpaceModelCalculator vectorSpaceModelCalculator)
//			throws IOException {
//		String classMembersFileName =
//			"c:/Tools/runtime-New_configuration/.metadata/.plugins/edu.wm.topicxp/CohesionTests/methods";
//		vectorSpaceModelCalculator.initializeVectorSpace(classMembersFileName);
//		String documentMember0 =
//			"=CohesionTests/src<nz.ac.vuw.ecs.kcassell.geometry{PointShape.java[PointShape~document~QAffineTransform;";
//		String documentMember1 =
//			"=CohesionTests/src<nz.ac.vuw.ecs.kcassell.geometry{PointShape.java[PointShape~document~QAffineTransform;~D";
//		String documentMember2 =
//			"=CohesionTests/src<nz.ac.vuw.ecs.kcassell.geometry{PointShape.java[PointShape~getBounds";
//		String documentMember3 =
//			"=CohesionTests/src<nz.ac.vuw.ecs.kcassell.geometry{PointShape.java[PointShape~intersects~D~D~D~D";
//		Integer documentInt0 = vectorSpaceModelCalculator.memberHandleToDocumentNumber.get(documentMember0);
//		Integer documentInt1 = vectorSpaceModelCalculator.memberHandleToDocumentNumber.get(documentMember1);
//		Integer documentInt2 = vectorSpaceModelCalculator.memberHandleToDocumentNumber.get(documentMember2);
//		Integer documentInt3 = vectorSpaceModelCalculator.memberHandleToDocumentNumber.get(documentMember3);
//		DoubleVector vector0 = vectorSpaceModelCalculator.vectorSpaceModel.getDocumentVector(documentInt0);
//		DoubleVector vector1 = vectorSpaceModelCalculator.vectorSpaceModel.getDocumentVector(documentInt1);
//		DoubleVector vector2 = vectorSpaceModelCalculator.vectorSpaceModel.getDocumentVector(documentInt2);
//		DoubleVector vector3 = vectorSpaceModelCalculator.vectorSpaceModel.getDocumentVector(documentInt3);
//		double similarity01 = Similarity.cosineSimilarity(vector0, vector1);
//		double similarity12 = Similarity.cosineSimilarity(vector1, vector2);
//		double similarity23 = Similarity.cosineSimilarity(vector2, vector3);
//		double similarity13 = Similarity.cosineSimilarity(vector1, vector3);
//		System.out.println("similarity01: " + similarity01);
//		System.out.println("similarity12: " + similarity12);
//		System.out.println("similarity23: " + similarity23);
//		System.out.println("similarity13: " + similarity13);
//	}

	private void testFreecol() throws IOException {
		String documentMember0 = "=FreecolSVNTrunk/src<net.sf.freecol.client{FreeColClient.java[FreeColClient~loadClientOptions~QFile;";
		String documentMember1 = "=FreecolSVNTrunk/src<net.sf.freecol.client{FreeColClient.java[FreeColClient~saveClientOptions";
		String documentMember2 = "=FreecolSVNTrunk/src<net.sf.freecol.client{FreeColClient.java[FreeColClient~setMapEditor~Z";
		String documentMember3 = "=FreecolSVNTrunk/src<net.sf.freecol.client{FreeColClient.java[FreeColClient~canSaveCurrentGame";
		Integer documentInt0 = memberHandleToDocumentNumber
				.get(documentMember0);
		Integer documentInt1 = memberHandleToDocumentNumber
				.get(documentMember1);
		Integer documentInt2 = memberHandleToDocumentNumber
				.get(documentMember2);
		Integer documentInt3 = memberHandleToDocumentNumber
				.get(documentMember3);
		DoubleVector vector0 = vectorSpaceModel
				.getDocumentVector(documentInt0);
		DoubleVector vector1 = vectorSpaceModel
				.getDocumentVector(documentInt1);
		DoubleVector vector2 = vectorSpaceModel
				.getDocumentVector(documentInt2);
		DoubleVector vector3 = vectorSpaceModel
				.getDocumentVector(documentInt3);
		double similarity01 = Similarity.cosineSimilarity(vector0, vector1);
		System.out.println("similarity01: " + similarity01);
		double similarity12 = Similarity.cosineSimilarity(vector1, vector2);
		System.out.println("similarity12: " + similarity12);
		double similarity23 = Similarity.cosineSimilarity(vector2, vector3);
		System.out.println("similarity23: " + similarity23);
		double similarity13 = Similarity.cosineSimilarity(vector1, vector3);
		System.out.println("similarity13: " + similarity13);
		double similarity03 = Similarity.cosineSimilarity(vector0, vector3);
		System.out.println("similarity03: " + similarity03);
	}


}
