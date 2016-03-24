package conceptopedia.Actions;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.io.*;
import org.apache.hadoop.hbase.mapreduce.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.*;

import conceptopedia.ORM.WikiArticle;
import conceptopedia.ORM.WikiConcept;

public class ConceptGraph {


	public static class ConceptMapper extends TableMapper<ImmutableBytesWritable, Text> {

		public void map(ImmutableBytesWritable conceptKey, Result articleList, Context context)
				throws InterruptedException, IOException {
			WikiConcept thisConcept = new WikiConcept(articleList);
			Set<Text> articles_uniq = thisConcept.getArticlesIds();

			for (Text article : articles_uniq) {
				WikiArticle a = new WikiArticle(article);
				Set<WikiArticle> voisins_uniq = a.getWikiArticlesVoisins();
				
				for (WikiArticle voisin : voisins_uniq) {
					if (voisin.hasConcept())
						context.write(conceptKey, voisin.getConceptId());
				}
			}
		}
	}

	public static class ConceptReducer extends TableReducer<ImmutableBytesWritable, Text, ImmutableBytesWritable> {

		public void reduce(ImmutableBytesWritable key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {

			Map<Text, Integer> count = new HashMap<Text, Integer>();
			for (Text val : values) {
				if (count.containsKey(val))
					count.put(val, count.get(val) + 1);
				else
					count.put(val, 1);
			}

			Put put = new Put(key.get());
			for (Text targetConcept : count.keySet()) {
				put.add(Bytes.toBytes("voisins"), targetConcept.getBytes(), Bytes.toBytes(count.get(targetConcept)));
			}
			context.write(key, put);
		}
	}

}
