package nz.ac.vuw.ecs.kcassell.similarity;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import edu.ucla.sspace.common.Similarity;
import edu.ucla.sspace.vector.DoubleVector;
import edu.ucla.sspace.vsm.VectorSpaceModel;

public class SemanticAnalyzer {

	private VectorSpaceModel vectorSpaceModel = null;
	private Map<String, Integer> methodHandleToDocumentNumber = new HashMap<String, Integer>();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
			String classMethodsFileName =
				"c:/Tools/runtime-New_configuration/.metadata/.plugins/edu.wm.topicxp/CohesionTests/methods";
			semanticAnalyzer.initializeVectorSpace(classMethodsFileName);
			String getPathIteratorMethod0 =
				"=CohesionTests/src<nz.ac.vuw.ecs.kcassell.geometry{PointShape.java[PointShape~getPathIterator~QAffineTransform;";
			String getPathIteratorMethod1 =
				"=CohesionTests/src<nz.ac.vuw.ecs.kcassell.geometry{PointShape.java[PointShape~getPathIterator~QAffineTransform;~D";
			Integer getPathIteratorInt0 = semanticAnalyzer.methodHandleToDocumentNumber.get(getPathIteratorMethod0);
			Integer getPathIteratorInt1 = semanticAnalyzer.methodHandleToDocumentNumber.get(getPathIteratorMethod1);
			DoubleVector vector0 = semanticAnalyzer.vectorSpaceModel.getDocumentVector(getPathIteratorInt0);
			DoubleVector vector1 = semanticAnalyzer.vectorSpaceModel.getDocumentVector(getPathIteratorInt1);
			double similarity01 = Similarity.cosineSimilarity(vector0, vector1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Process a file that contains all of the methods in a class.
	 * @param classMethodsFileName the name of the file that contains
	 * one method per line.  The first token is the method handle, and the 
	 * remaining tokens are the words found in identifiers and comments.
	 * @throws IOException
	 */
	private VectorSpaceModel initializeVectorSpace(String classMethodsFileName)
	throws IOException {
		vectorSpaceModel = new VectorSpaceModel();
		BufferedReader documentFileReader = new BufferedReader(new FileReader(
				classMethodsFileName));
		int lineNum = 0;
		String line = null;
		String methodName = null;
		while ((line = documentFileReader.readLine()) != null) {
			methodName = processMethodDocument(vectorSpaceModel, line);
			methodHandleToDocumentNumber.put(methodName, lineNum++);
		}
		vectorSpaceModel.processSpace(System.getProperties());
		return vectorSpaceModel;
	}

	/**
	 * Process a line from a file that contains all of the methods in a class.
	 * @param vsm
	 * @param line the first token is the method handle, and the 
	 * remaining tokens are the words found in identifiers and comments.
	 * @throws IOException
	 */
	private static String processMethodDocument(VectorSpaceModel vsm,
			String line) throws IOException {
		String methodName;
		String restOfMethod;
		int spaceIndex = line.indexOf(' ');
		if (spaceIndex > -1) {
			methodName = line.substring(0, spaceIndex);
			if (spaceIndex < line.length() - 2) {
				restOfMethod = line.substring(spaceIndex + 1);
			} else {
				restOfMethod = "";
			}
			StringReader stringReader = new StringReader(restOfMethod);
			BufferedReader methodTokensReader =
				new BufferedReader(stringReader);
			vsm.processDocument(methodTokensReader);
		} else {
			methodName = line;
		}
		return methodName;
	}

}
