package conceptopedia.ORM;

import java.io.IOException;
import java.util.HashSet;
import java.util.NavigableMap;
import java.util.Set;

import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;

import conceptopedia.Actions.DataModel;

public class WikiArticle extends WikiHBaseORM {

	public static ResultScanner getScannerVoisinsInter(String startRow) throws IOException {
		Scan scan;
		if (startRow != null) 
			scan = new Scan(Bytes.toBytes(startRow));
		else
			scan = new Scan();
		scan.setCaching(20);
		scan.addFamily(Bytes.toBytes("voisins_inter"));
		scan.addFamily(Bytes.toBytes("attributs"));
		scan.addFamily(Bytes.toBytes("voisins"));
		return table.getScanner(scan);
	}

	public WikiArticle(byte[] key) throws IOException {
		super(table,key);
		// TODO Auto-generated constructor stub
	}

	public WikiArticle(Text key) throws IOException {
		super(table,key);
		// TODO Auto-generated constructor stub
	}

	public WikiArticle(Result key) throws IOException {
		super(table,key);
		// TODO Auto-generated constructor stub
	}

	private WikiConcept concept;
	private Set<WikiArticle> voisins;
	private Set<Text> voisins_ids;
	private Set<WikiArticle> voisins_inter;
	private Set<Text> voisins_inter_ids;
	static HTable table = DataModel.tables.get(DataModel.INTRALANG_TBL);
	

	// Return null if this article doesn't belong to a concept yet
	public Text getConceptId() {
		byte[] conceptRowKey = result.getValue(Bytes.toBytes("attributs"), Bytes.toBytes("concept"));
		if (conceptRowKey != null)
			return new Text(conceptRowKey);
		else
			return null;
	}

	public boolean hasConcept() {
		return getConceptId() != null;
	}

	public WikiConcept getWikiConcept() throws IOException {
		if (concept == null) {
			Text conceptId = getConceptId();
			if (conceptId != null)
				concept = new WikiConcept(conceptId);
		}
		return concept;
	}

	public Set<Text> getArticlesVoisinsIds() throws IOException {
		if (voisins_ids == null) {
			System.out.println("Get neighbors articles from article: '" + hbaseRowId + "'");
			Set<byte[]> tmp;
			NavigableMap<byte[], byte[]> t = result.getFamilyMap(Bytes.toBytes("voisins"));
			if(t != null) tmp = t.keySet();
			else tmp = new HashSet<byte[]>();
			voisins_ids = new HashSet<Text>();
			for (byte[] v : tmp)
				voisins_ids.add(new Text(v));
		}
		return voisins_ids;
	}

	public Set<WikiArticle> getWikiArticlesVoisins() throws IOException {
		if (voisins == null) {
			if (voisins_ids == null)
				getArticlesVoisinsIds();
			voisins = new HashSet<WikiArticle>();
			for (Text v : voisins_ids)
				voisins.add(new WikiArticle(v));
		}
		return voisins;
	}
	
	public Set<Text> getArticlesVoisinsInterIds() throws IOException {
		if (voisins_inter_ids == null) {
			System.out.println("Get crooslink articles from article: '" + hbaseRowId + "'");
			Set<byte[]> tmp;
			NavigableMap<byte[], byte[]> t = result.getFamilyMap(Bytes.toBytes("voisins_inter"));
			if(t != null) tmp = t.keySet();
			else tmp = new HashSet<byte[]>();
			voisins_inter_ids = new HashSet<Text>();
			for (byte[] v : tmp)
				voisins_inter_ids.add(new Text(v));
		}
		return voisins_inter_ids;
	}

	public Set<WikiArticle> getWikiArticlesVoisinsInter() throws IOException {
		if (voisins_inter == null) {
			if (voisins_inter_ids == null)
				getArticlesVoisinsInterIds();
			voisins_inter = new HashSet<WikiArticle>();
			for (Text v : voisins_inter_ids)
				voisins_inter.add(new WikiArticle(v));
		}
		return voisins_inter;
	}

	public void setConcept(Text conceptId) throws IOException {
		// Intralang column "concept"
		Put conceptPut = new Put(hbaseRowId.getBytes());
		conceptPut.add(Bytes.toBytes("attributs"), Bytes.toBytes("concept"), conceptId.getBytes());
		WikiArticle.table.put(conceptPut);
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

}
