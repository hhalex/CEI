package conceptopedia.ORM;

import java.io.IOException;

import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.io.Text;

public abstract class WikiHBaseORM {
	protected boolean newEntity;
	protected HTable table;
	protected Text hbaseRowId;
	protected Result result;
	
	protected WikiHBaseORM(HTable table ,byte[] key) throws IOException {
		this(table,new Text(key));
	}
	
	protected WikiHBaseORM(HTable table ,Text key) throws IOException {
		hbaseRowId = key;
		Get get = new Get(hbaseRowId.getBytes());
		result = table.get(get);
		this.table=table;
		newEntity = result == null;
	}

	protected WikiHBaseORM(HTable table ,Result res) throws IOException {
		result = res;
		hbaseRowId = new Text(res.getRow());
		this.table=table;
		newEntity = result == null;
	}
	
	public Text getId(){
		return hbaseRowId;
	}
	
	public abstract String getDescription();
	
	public void remove() throws IOException {
		if (!newEntity)
			table.delete(new Delete(hbaseRowId.getBytes()));
	}
}
