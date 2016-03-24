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

public class WikiConcept extends WikiHBaseORM{
	
	Set<Text> articles_ids;
	Set<WikiArticle> articles;
	static HTable table = DataModel.tables.get(DataModel.CONCEPTCREATOR_TBL);
	
	public static ResultScanner getScanner(String startRow) throws IOException {
		Scan scan;
		if (startRow != null) 
			scan = new Scan(Bytes.toBytes(startRow));
		else
			scan = new Scan();
		scan.setCaching(20);
		scan.addFamily(Bytes.toBytes("articles"));
		return table.getScanner(scan);
	}
	
	public WikiConcept(Text rowId) throws IOException{
		super(table,rowId);
	}
	
	public WikiConcept(byte[] rowId) throws IOException{
		super(table,rowId);	
	}
	
	public WikiConcept(Result rowId) throws IOException{
		super(table,rowId);	
	}
	
	public Set<Text> getArticlesIds(){
		if(articles_ids ==null ){
			System.out.println("Get articles from concept: '" + hbaseRowId + "'");
			NavigableMap<byte[], byte[]> t = result.getFamilyMap(Bytes.toBytes("articles"));
			Set<byte[]> tmp;
			if(t != null) tmp = t.keySet();
			else tmp = new HashSet<byte[]>();
			articles_ids = new HashSet<Text>();
			for (byte[] v : tmp)
				articles_ids.add(new Text(v));
		}
		return articles_ids;
	}
	
	public Set<WikiArticle> getWikiArticles() throws IOException{
		if(articles == null){
			if(articles_ids == null) getArticlesIds();
			Set<WikiArticle> articles = new HashSet<WikiArticle>();
			for (Text v : articles_ids) articles.add(new WikiArticle(v));
		}
		return articles;
	}
	
	
	
	public void appendArticle(Text articleId) throws IOException{
		Put articlePut = new Put(hbaseRowId.getBytes());
		articlePut.add(Bytes.toBytes("articles"), articleId.getBytes(), Bytes.toBytes(1));
		System.out.println("Article '"+ articleId + "' has been added to '"+ hbaseRowId + "'");
		table.put(articlePut);
	}

	public void appendArticlesList(Set<Text> articles_voisins_uniques) throws IOException {
		System.out.println(" >> Set articles list of concept: '"+ hbaseRowId + "'");
		Put articlePut = new Put(hbaseRowId.getBytes());
		for (Text article : articles_voisins_uniques) {
			// Intralang
			WikiArticle a = new WikiArticle(article);
			a.setConcept(hbaseRowId);
			// Concept Creator
			articlePut.add(Bytes.toBytes("articles"), article.getBytes(), Bytes.toBytes(1));
			System.out.println("Article '"+ article + "' has been added to '"+ hbaseRowId + "'");
		}
		table.put(articlePut);
		System.out.println(" << List of articles registered");
		
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}
}
