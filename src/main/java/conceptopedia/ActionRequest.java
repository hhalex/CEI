package conceptopedia;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;

import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;

import conceptopedia.Actions.ConceptCreator;
import conceptopedia.Actions.ConceptGraph;
import conceptopedia.Actions.ConceptGraphRegular;
import conceptopedia.Actions.ConceptInferer;
import conceptopedia.Actions.CrossLinkExtractor;
import conceptopedia.Actions.DataModel;
import conceptopedia.Actions.LinkExtractor;
import conceptopedia.ORM.WikiConceptGraphNode;

public class ActionRequest {
	private static final Pattern LANG_PATTERN = Pattern.compile("(?:.*?/)*(.+)wiki-latest-langlinks.sql");

	private static Map<String, Mode> ModeFromString = new HashMap<String, Mode>();

	static {
		ModeFromString.put("0", Mode.CREATE_MODEL);
		ModeFromString.put("1", Mode.EXTRACT_XML_INFO);
		ModeFromString.put("2", Mode.EXTRACT_CROSSLINKS);
		ModeFromString.put("3", Mode.BUILD_CONCEPTS);
		ModeFromString.put("4", Mode.BUILD_CONCEPT_GRAPH);
		ModeFromString.put("4bis", Mode.BUILD_CONCEPT_GRAPH_REGULAR);
		ModeFromString.put("5", Mode.INFER_TEST);

	}

	private static Map<String, Action> ActionFromString = new HashMap<String, Action>();

	static {
		ActionFromString.put("file", Action.FILE);
		ActionFromString.put("mode", Action.MODE);
		ActionFromString.put("show-manual", Action.MANUAL);
		ActionFromString.put("article", Action.ARTICLE);
		ActionFromString.put("start-row", Action.START_ROW);
	}

	public static Map<String, Integer> ActionNbParameters = new HashMap<String, Integer>();

	static {
		ActionNbParameters.put("file", 1);
		ActionNbParameters.put("mode", 1);
		ActionNbParameters.put("show-manual", 0);
		ActionNbParameters.put("article", 1);
		ActionNbParameters.put("start-row", 1);
	}

	private enum Action {
		MODE, FILE, MANUAL, ARTICLE, START_ROW
	}

	private enum Mode {
		CREATE_MODEL, EXTRACT_XML_INFO, EXTRACT_CROSSLINKS, BUILD_CONCEPTS, BUILD_CONCEPT_GRAPH, BUILD_CONCEPT_GRAPH_REGULAR, INFER_TEST
	}

	String inputFile = null;
	String inputArticle = null;
	String startRow = null;
	Mode mode = null;

	Action currentAction;

	public void setParameter(String value) throws Exception {

		switch (currentAction) {
		case MODE:
			mode = ModeFromString.get(value);
			break;
		case FILE:
			inputFile = value;
			break;
		case ARTICLE:
			inputArticle = value;
			break;
		case START_ROW:
			startRow = value;
			break;
		default:
			throw new Exception("Action not implemented");
		}
	}

	private static void displayManual() {
		System.out.println("Projet de CEI Supélec 2016");
		System.out.println("");
		System.out.println("--------------------------------------");
		System.out.println("NAME");
		System.out.println("\t Conceptopedia -- builds a concept graph from wikipedia dumps");
		System.out.println("SYNOPSIS");
		System.out.println("\t java -jar conceptopedia.jar [--qualifier argument]");

		System.out.println("DESCRIPTION");
		System.out.println(
				"Conceptopedia is a tool designed to generate a new graph from wikipedia dumps. This new data structure is easier to use when it comes to infer which article is a translation of another one.");
		System.out.println("\t --mode");
		System.out.println("\t\t 0 : creates the data model in hbase");
		System.out.println("\t\t 1 : extracts information from a xml dump");
		System.out.println("\t\t 2 : extracts cross links from a sql dump");
		System.out.println("\t\t 3 : builds concepts from crosslinks");
		System.out.println("\t\t 4 : builds graph of concepts with Map/Reduce");
		System.out.println("\t\t 4bis : builds graph of concepts without Map/Reduce (this one works!)");
		System.out.println("\t\t 5 : infer the concept of the given article");
		System.out.println("\t --file");
		System.out.println("\t\t input-file");
		System.out.println("\t --article");
		System.out.println("\t\t input-article you want to infer the concept from (mode 5)");
		System.out.println("\t --start-row");
		System.out.println("\t\t specify the row from which you want to resume the process");
		System.out.println("\t --show-manual");
		System.out.println("USAGE");
		System.out.println("à compléter");
		System.out.println("EXAMPLE");
	}

	private void buildConceptGraphRegular() throws IOException {
		System.out.println("Creating graph of concepts...");

		long startTime = System.currentTimeMillis();

		ConceptGraphRegular cgR = new ConceptGraphRegular();
		cgR.createConceptGraph(startRow);

		long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
		System.out.printf("%d seconds.\n", elapsedSeconds);

	}

	private void buildConceptGraph() throws IOException, ClassNotFoundException, InterruptedException {
		Job job = Job.getInstance(DataModel.conf);
		job.setJobName("BuildConceptGraph");

		job.setJarByClass(ConceptGraph.class); // class that contains mapper

		Scan scan = new Scan(Bytes.toBytes("C-af-1555"), Bytes.toBytes("C-af-1982"));
		// Scan scan = new Scan(Bytes.toBytes("C-az-"));
		// scan.setMaxResultSize(50);
		scan.setCaching(200); // 1 is the default in Scan, which will be bad for
								// MapReduce jobs
		scan.setCacheBlocks(false); // don't set to true for MR jobs
		// set other scan attrs

		TableMapReduceUtil.initTableMapperJob(DataModel.CONCEPTCREATOR_TBL, // input
																			// HBase
																			// table
																			// name
				scan, // Scan instance to control CF and attribute selection
				ConceptGraph.ConceptMapper.class, // mapper
				ImmutableBytesWritable.class, // mapper output key
				Text.class, // mapper output value
				job);

		TableMapReduceUtil.initTableReducerJob(DataModel.CONCEPTGRAPH_TBL, // output
																			// table
				ConceptGraph.ConceptReducer.class, // reducer class
				job);

		job.setNumReduceTasks(1); // at least one, adjust as required

		boolean b = job.waitForCompletion(true);
		if (!b) {
			throw new IOException("error with job!");
		}
	}

	private void buildConcepts() throws IOException {
		System.out.println("Creating concepts...");

		long startTime = System.currentTimeMillis();

		ConceptCreator conceptCreator = new ConceptCreator();
		conceptCreator.createConcepts(startRow);

		long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
		System.out.printf("%d seconds.\n", elapsedSeconds);
	}

	private void createModel() throws IOException {
		System.out.println("Creating tables...");

		long startTime = System.currentTimeMillis();

		DataModel.create();

		long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
		System.out.printf("%d seconds.\n", elapsedSeconds);
	}

	private void extractLinks(String inputFile) throws IOException, XMLStreamException {
		System.out.println("Parsing pages and extracting links...");

		long startTime = System.currentTimeMillis();

		LinkExtractor linkExtractor = new LinkExtractor();
		linkExtractor.parse(inputFile);

		long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
		System.out.printf("\n%d pages parsed in %d seconds.\n", linkExtractor.getPageCount(), elapsedSeconds);
	}

	private void extractCrossLinks(String inputFile) throws IOException, XMLStreamException {
		System.out.println("Extracting cross links...");

		long startTime = System.currentTimeMillis();

		// Langue par défaut = français
		String lang = "fr";
		// On récupère la langue dans le nom du fichier
		Matcher matcher = LANG_PATTERN.matcher(inputFile);
		if (matcher.find())
			lang = matcher.group(1);

		System.out.println("Langue: " + lang);

		CrossLinkExtractor linkExtractor = new CrossLinkExtractor(lang, null);
		linkExtractor.parse(inputFile);

		long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
		System.out.printf("\n%d pages parsed in %d seconds.\n", linkExtractor.getPageCount(), elapsedSeconds);
	}

	private void infereArticle(String inputArticle) throws IOException {
		System.out.println("Infering article : " + inputArticle);

		long startTime = System.currentTimeMillis();

		ConceptInferer Inferer = new ConceptInferer(inputArticle);
		System.out.println(Inferer.infereConcept());

		long elapsedmilliSeconds = System.currentTimeMillis() - startTime;
		System.out.printf("infered in %d milliseconds.\n", elapsedmilliSeconds);

	}

	public void call() throws IOException, ClassNotFoundException, InterruptedException, XMLStreamException {
		//
		if (currentAction != null) {
			switch (currentAction) {
			case MANUAL:
				displayManual();
				break;
			
			default:
				break;
			}
		}
		
		if (mode != null) {
			switch (mode) {
			case CREATE_MODEL:
				createModel();
				break;
			case EXTRACT_XML_INFO:
				extractLinks(inputFile);
				break;
			case EXTRACT_CROSSLINKS:
				extractCrossLinks(inputFile);
				break;
			case BUILD_CONCEPTS:
				buildConcepts();
				break;
			case BUILD_CONCEPT_GRAPH:
				buildConceptGraph();
				break;
			case BUILD_CONCEPT_GRAPH_REGULAR:
				buildConceptGraphRegular();
				break;
			case INFER_TEST:
				infereArticle(inputArticle);
			default:
				break;
			}
		}
	}

	public void setAction(String group) throws Exception {
		if (ActionFromString.containsKey(group))
			currentAction = ActionFromString.get(group);
		else
			throw new Exception("Unknown argument qualifier, please see manual");

	}
}
