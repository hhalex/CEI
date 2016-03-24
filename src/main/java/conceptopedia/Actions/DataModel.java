package conceptopedia.Actions;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;

public class DataModel {
	private static HBaseAdmin admin;
	public static Configuration conf = HBaseConfiguration.create();
	static{
		conf.set("hbase.master", "localhost:9503");
		try {
			admin = new HBaseAdmin(conf);
			tables = new HashMap<String, HTable>() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				{
					put(INTRALANG_TBL, new HTable(conf, INTRALANG_TBL));
					put(CONCEPTGRAPH_TBL, new HTable(conf, CONCEPTGRAPH_TBL));
					put(CONCEPTCREATOR_TBL, new HTable(conf, CONCEPTCREATOR_TBL));
					put(INTRALANG_MATCHING_IDS_TBL, new HTable(conf, INTRALANG_MATCHING_IDS_TBL));

				}
			};

		} catch (MasterNotRunningException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ZooKeeperConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static Map<String, HTable> tables;
	
	public static final String INTRALANG_TBL = "intralang";
	public static final String CONCEPTGRAPH_TBL = "conceptgraph";
	public static final String CONCEPTCREATOR_TBL = "conceptcreator";
	public static final String INTRALANG_MATCHING_IDS_TBL = "intralang_matching_ids";

	private static void cleanTable(String tableName, HBaseAdmin admin) throws IOException {
		if (admin.tableExists(tableName)) {
			if (!admin.isTableDisabled(tableName))
				admin.disableTable(tableName);

			admin.deleteTable(tableName);
		}
	}

	public static void create() throws IOException {
		// Instantiating configuration class
		Configuration con = HBaseConfiguration.create();
		con.set("hbase.master", "localhost:9503");

		// Instantiating HbaseAdmin class
		HBaseAdmin admin = new HBaseAdmin(con);

		// disabling table named emp
		try {
			cleanTable(INTRALANG_TBL, admin);
			cleanTable(CONCEPTGRAPH_TBL, admin);
			cleanTable(CONCEPTCREATOR_TBL, admin);
			cleanTable(INTRALANG_MATCHING_IDS_TBL, admin);

		} catch (IOException e) {
			System.out.println("Something went wrong while cleaning a table: '" + e.getLocalizedMessage() + "'");
		}

		// Instantiating table for intralang links
		HTableDescriptor tableIntraLang = new HTableDescriptor(TableName.valueOf(INTRALANG_TBL));
		tableIntraLang.addFamily(new HColumnDescriptor("voisins"));
		tableIntraLang.addFamily(new HColumnDescriptor("attributs"));
		tableIntraLang.addFamily(new HColumnDescriptor("voisins_inter"));
		admin.createTable(tableIntraLang);
		System.out.println("Table '" + INTRALANG_TBL + "' created");

		// Instantiating table for intralang matching ids
		HTableDescriptor tableIntraLangMatchingIds = new HTableDescriptor(TableName.valueOf(INTRALANG_MATCHING_IDS_TBL));
		tableIntraLangMatchingIds.addFamily(new HColumnDescriptor("match"));
		admin.createTable(tableIntraLangMatchingIds);
		System.out.println("Table '" + INTRALANG_MATCHING_IDS_TBL + "' created");

		// Links between concepts
		HTableDescriptor tableConceptGraph = new HTableDescriptor(TableName.valueOf(CONCEPTGRAPH_TBL));
		tableConceptGraph.addFamily(new HColumnDescriptor("voisins"));
		admin.createTable(tableConceptGraph);
		System.out.println("Table '" + CONCEPTGRAPH_TBL + "' created");

		// Incremental table for concept creation
		HTableDescriptor tableConceptCreator = new HTableDescriptor(TableName.valueOf(CONCEPTCREATOR_TBL));
		tableConceptCreator.addFamily(new HColumnDescriptor("articles"));
		admin.createTable(tableConceptCreator);
		System.out.println("Table '" + CONCEPTCREATOR_TBL + "' created");
	}
}
