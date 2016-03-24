package conceptopedia.ORM;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;

import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;

import conceptopedia.Actions.DataModel;

public class WikiConceptGraphNode extends WikiHBaseORM{
	
	Set<Text> voisins_ids;
	Set<WikiConceptGraphNode> voisins;
	Map<Text, Integer> liensvoisins;
	static HTable table = DataModel.tables.get(DataModel.CONCEPTGRAPH_TBL);
	
	
	public WikiConceptGraphNode(Text rowId) throws IOException{
		super(table,rowId);
	}
	
	public WikiConceptGraphNode(byte[] rowId) throws IOException{
		super(table,rowId);	
	}
	
	public WikiConceptGraphNode(Result rowId) throws IOException{
		super(table,rowId);	
	}
	
	public Set<Text> getVoisinsIds(){
		if( voisins_ids == null){
			if(liensvoisins == null) 
				getLinks();
			System.out.println("vid" + liensvoisins.size());
			voisins_ids=liensvoisins.keySet();
		}
		return voisins_ids;
	}
	
	public Set<WikiConceptGraphNode> getWikiVoisins() throws IOException{
		if( voisins == null){
			if(voisins_ids == null) 
				getVoisinsIds();
			voisins = new HashSet<WikiConceptGraphNode>();
			for (Text v : voisins_ids){
				voisins.add(new WikiConceptGraphNode(v));
			}
		}
		return voisins;
	}
	
	public Map<Text, Integer> getLinks(){
		if(liensvoisins ==null ){
			System.out.println("Get links of concept: '" + hbaseRowId + "'");
			NavigableMap<byte[], byte[]> t = result.getFamilyMap(Bytes.toBytes("voisins"));
			Set<byte[]> tmp;
			if(t != null) tmp = t.keySet();
			else tmp = new HashSet<byte[]>();
			liensvoisins = new HashMap<Text,Integer>();
			for (byte[] v : tmp)
				liensvoisins.put(new Text(v), Bytes.toInt(t.get(v)));
		}
		return liensvoisins;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}
}