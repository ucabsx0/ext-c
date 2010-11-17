package nz.ac.vuw.ecs.kcassell.similarity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.ucla.sspace.common.SemanticSpace;
import edu.ucla.sspace.common.Similarity;
import edu.ucla.sspace.matrix.Matrices;
import edu.ucla.sspace.matrix.Matrix;
import edu.ucla.sspace.matrix.MatrixIO;
import edu.ucla.sspace.vector.DoubleVector;
import edu.ucla.sspace.vector.Vector;
import edu.ucla.sspace.vector.Vectors;
import edu.ucla.sspace.vsm.VectorSpaceModel;

public class VectorSpaceModelCalculator 
implements DistanceCalculatorIfc<String> {

	/** The vector space model maintains document vectors
	 * where class members are documents and the "words" are
	 * stemmed parts of identifiers and stemmed words from comments. */
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
	public VectorSpaceModelCalculator(String fileName)
	throws IOException {
		initializeVectorSpace(fileName);
	}
	
	
	/**
	 * Process a file that contains all of the members in a class.
	 * @param classMembersFileName the name of the file that contains
	 * one member per line.  The first token is the member handle, and the 
	 * remaining tokens are the stemmed words found in identifiers and comments.
	 * @throws IOException
	 */
	public VectorSpaceModel initializeVectorSpace(String classMembersFileName)
	throws IOException {
		vectorSpaceModel = new VectorSpaceModel();
		BufferedReader documentFileReader = new BufferedReader(new FileReader(
				classMembersFileName));
		int lineNum = 0;
		String line = null;
		String memberName = null;
		while ((line = documentFileReader.readLine()) != null) {
			memberName = processMemberDocument(vectorSpaceModel, line);
			memberHandleToDocumentNumber.put(memberName, lineNum++);
		}
		vectorSpaceModel.processSpace(System.getProperties());
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

	/**
	 * @throws IOException 
	 * @see http://code.google.com/p/airhead-research/wiki/FrequentlyAskedQuestions#How_can_I_convert_a_.sspace_file_to_a_matrix?
	 */
	public void saveSemanticSpace(SemanticSpace sspace, File outputMatrixFile,
			MatrixIO.Format fmt)
	throws IOException {
		Set<String> wordSet = sspace.getWords();
		int numWords = wordSet.size();
		DoubleVector[] wordVectors = new DoubleVector[numWords];
		int i = 0;
		for (String word : wordSet) {
		    Vector<?> wordVector = sspace.getVector(word);
			wordVectors[i] = Vectors.asDouble(wordVector);
		    i++;
		}                                                                               
		Matrix wordMatrix = Matrices.asMatrix(Arrays.asList(wordVectors));

		// If you then want to write the matrix out to disk, do the following
		MatrixIO.writeMatrix(wordMatrix, outputMatrixFile, fmt);
		// Reminder, if you still want the word-to-row mapping, write out the words array too
	}

	public Number calculateDistance(String handle1, String handle2) {
		Integer documentInt1 = memberHandleToDocumentNumber.get(handle1);
		Integer documentInt2 = memberHandleToDocumentNumber.get(handle2);
		DoubleVector vector1 = vectorSpaceModel.getDocumentVector(documentInt1);
		DoubleVector vector2 = vectorSpaceModel.getDocumentVector(documentInt2);
		double distance = 1 - Similarity.cosineSimilarity(vector1, vector2);
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
				new VectorSpaceModelCalculator("c:/Tools/runtime-New_configuration/.metadata/.plugins/edu.wm.topicxp/FreecolSVNTrunk/methods");
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
