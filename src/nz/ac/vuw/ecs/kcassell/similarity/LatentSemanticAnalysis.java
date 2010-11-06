package nz.ac.vuw.ecs.kcassell.similarity;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import edu.ucla.sspace.vsm.VectorSpaceModel;

public class LatentSemanticAnalysis {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		VectorSpaceModel vsm;
		try {
			vsm = new VectorSpaceModel();
			String classMethodsFileName =
				"c:/Tools/runtime-New_configuration/.metadata/.plugins/edu.wm.topicxp/CohesionTests/methods";
			BufferedReader documentFileReader = new BufferedReader(new FileReader(
					classMethodsFileName));
			Map<String, Integer> methodToDocNum = new HashMap<String, Integer>();
			int lineNum = 0;
			String line = null;
			String methodName = null;
			while ((line = documentFileReader.readLine()) != null) {
				methodName = processMethodDocument(vsm, line);
				methodToDocNum.put(methodName, lineNum++);
			}
			vsm.processSpace(System.getProperties());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

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
