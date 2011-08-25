package nz.ac.vuw.ecs.kcassell.utils;

import java.util.ArrayList;

public class GodClassesMM30 {

        private static final String BRULE_ENGINE = "=Jena/<com.hp.hpl.jena.reasoner.rulesys.impl.oldCode{BRuleEngine.java[BRuleEngine";
        private static final String BV_DECOMPOSE = "=wekaSVNTrunk/src\\/main\\/java<weka.classifiers{BVDecompose.java[BVDecompose";
        private static final String BV_DECOMPOSE_SEG_CV_SUB = "=wekaSVNTrunk/src\\/main\\/java<weka.classifiers{BVDecomposeSegCVSub.java[BVDecomposeSegCVSub";
        private static final String CANDIDATE_URI = "=Heritrix/<org.archive.crawler.datamodel{CandidateURI.java[CandidateURI";
        private static final String COMMAND_LINE = "=Jena/<jena.cmdline{CommandLine.java[CommandLine";
        private static final String CRAWL_CONTROLLER = "=Heritrix/<org.archive.crawler.framework{CrawlController.java[CrawlController";
        private static final String DATABASE_UTILS = "=wekaSVNTrunk/src\\/main\\/java<weka.experiment{DatabaseUtils.java[DatabaseUtils";
        private static final String EXPERIMENT = "=wekaSVNTrunk/src\\/main\\/java<weka.experiment{Experiment.java[Experiment";
        private static final String FREECOL_CLIENT = "=FreecolSVNTrunk/src<net.sf.freecol.client{FreeColClient.java[FreeColClient";
        private static final String FREECOL_OBJECT = "=FreecolSVNTrunk/src<net.sf.freecol.common.model{FreeColObject.java[FreeColObject";
        private static final String FREECOL_SERVER = "=FreecolSVNTrunk/src<net.sf.freecol.server{FreeColServer.java[FreeColServer";
        private static final String HERITRIX = "=Heritrix/<org.archive.crawler{Heritrix.java[Heritrix";
        private static final String IMAGE_LIBRARY = "=FreecolSVNTrunk/src<net.sf.freecol.client.gui{ImageLibrary.java[ImageLibrary";
        private static final String LPB_RULE_ENGINE = "=Jena/<com.hp.hpl.jena.reasoner.rulesys.impl{LPBRuleEngine.java[LPBRuleEngine";
        private static final String LP_INTERPRETER = "=Jena/<com.hp.hpl.jena.reasoner.rulesys.impl{LPInterpreter.java[LPInterpreter";
        private static final String N3_JENA_WRITER_COMMON = "=Jena/<com.hp.hpl.jena.n3{N3JenaWriterCommon.java[N3JenaWriterCommon";
        private static final String NEAREST_NEIGHBOR_SEARCH = "=wekaSVNTrunk/src\\/main\\/java<weka.core.neighboursearch{NearestNeighbourSearch.java[NearestNeighbourSearch";
        private static final String NODE_JENA = "=Jena/<com.hp.hpl.jena.graph{Node.java[Node";
        private static final String NODE_WEKA = "=wekaSVNTrunk/src\\/main\\/java<weka.gui.treevisualizer{Node.java[Node";
        private static final String PARSER_BASE = "=Jena/<com.hp.hpl.jena.n3.turtle{ParserBase.java[ParserBase";
        private static final String REG_OPTIMIZER = "=wekaSVNTrunk/src\\/main\\/java<weka.classifiers.functions.supportVector{RegOptimizer.java[RegOptimizer";
        private static final String RESULT_MATRIX = "=wekaSVNTrunk/src\\/main\\/java<weka.experiment{ResultMatrix.java[ResultMatrix";
        private static final String RULE_JENA = "=Jena/<com.hp.hpl.jena.reasoner.rulesys{Rule.java[Rule";
        private static final String RULE_WEKA = "=wekaSVNTrunk/src\\/main\\/java<weka.classifiers.trees.m5{Rule.java[Rule";
        private static final String SCRIPT = "=wekaSVNTrunk/src\\/main\\/java<weka.gui.scripting{Script.java[Script";
        private static final String SETTINGS_HANDLER = "=Heritrix/<org.archive.crawler.settings{SettingsHandler.java[SettingsHandler";
        private static final String SPECIFICATION = "=FreecolSVNTrunk/src<net.sf.freecol.common.model{Specification.java[Specification";
        private static final String TEST_INSTANCES = "=wekaSVNTrunk/src\\/main\\/java<weka.core{TestInstances.java[TestInstances";
        private static final String WORK_QUEUE = "=Heritrix/<org.archive.crawler.frontier{WorkQueue.java[WorkQueue";
        private static final String XML_DOCUMENT = "=wekaSVNTrunk/src\\/main\\/java<weka.core.xml{XMLDocument.java[XMLDocument";
        
        private ArrayList<String> freeColClasses = new ArrayList<String>();
        private ArrayList<String> heritrixClasses = new ArrayList<String>();
        private ArrayList<String> jenaClasses = new ArrayList<String>();
        private ArrayList<String> wekaClasses = new ArrayList<String>();
        private ArrayList<String> allClasses = new ArrayList<String>();

    public GodClassesMM30() {
        freeColClasses.add(FREECOL_CLIENT);
        freeColClasses.add(FREECOL_OBJECT);
        freeColClasses.add(FREECOL_SERVER);
        freeColClasses.add(IMAGE_LIBRARY);
        freeColClasses.add(SPECIFICATION);

        heritrixClasses.add(CANDIDATE_URI);
        heritrixClasses.add(CRAWL_CONTROLLER);
        heritrixClasses.add(HERITRIX);
        heritrixClasses.add(SETTINGS_HANDLER);
        heritrixClasses.add(WORK_QUEUE);

        jenaClasses.add(BRULE_ENGINE);
        jenaClasses.add(COMMAND_LINE);
        jenaClasses.add(LPB_RULE_ENGINE);
        jenaClasses.add(LP_INTERPRETER);
        jenaClasses.add(N3_JENA_WRITER_COMMON);
        jenaClasses.add(NODE_JENA);
        jenaClasses.add(PARSER_BASE);
        jenaClasses.add(RULE_JENA);

        wekaClasses.add(BV_DECOMPOSE);
        wekaClasses.add(BV_DECOMPOSE_SEG_CV_SUB);
        wekaClasses.add(DATABASE_UTILS);
        wekaClasses.add(EXPERIMENT);
        wekaClasses.add(NEAREST_NEIGHBOR_SEARCH);
        wekaClasses.add(NODE_WEKA);
        wekaClasses.add(REG_OPTIMIZER);
        wekaClasses.add(RESULT_MATRIX);
        wekaClasses.add(RULE_WEKA);
        wekaClasses.add(SCRIPT);
        wekaClasses.add(TEST_INSTANCES);
        wekaClasses.add(XML_DOCUMENT);

        allClasses.addAll(freeColClasses);
        allClasses.addAll(heritrixClasses);
        allClasses.addAll(jenaClasses);
        allClasses.addAll(wekaClasses);
    }

	public ArrayList<String> getAllClasses() {
		return allClasses;
	}

}