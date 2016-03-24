package conceptopedia.Actions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;

import conceptopedia.ORM.WikiArticle;
import conceptopedia.ORM.WikiConcept;

public class ConceptGraphRegular {

	public void createConceptGraph(String startRow) throws IOException {
		ResultScanner scanner = WikiConcept.getScanner(startRow);
		int i = 0;
		Map<Text, Map<Text, Integer>> map = new HashMap<Text, Map<Text, Integer>>();
		Integer nbConcepts = 1;
		for (Result result = scanner.next(); (result != null); result = scanner.next()) {
			// On réinitialise le HashMap si nécessaire
			if (i+1 % nbConcepts == 0){
				System.out.println("New HashMap : "+nbConcepts+" concepts to be processed");
				map = new HashMap<Text,Map<Text, Integer>>();
			}
			
			WikiConcept thisConcept = new WikiConcept(result);
			
			Map<Text, Integer> currentMap = new HashMap<Text, Integer>();
			map.put(thisConcept.getId(), currentMap);

			Set<Text> articles_uniq = thisConcept.getArticlesIds();
			System.out.println("Current concept ("+i+"): '"+thisConcept.getId()+"' with "+articles_uniq.size()+" articles");
			for (Text article : articles_uniq) {
				WikiArticle a = new WikiArticle(article);
				Set<WikiArticle> voisins_uniq = a.getWikiArticlesVoisins();
				System.out.println("Article '"+a.getId()+"' with "+voisins_uniq.size()+" neighbours");
				for (WikiArticle voisin : voisins_uniq) {
					if (voisin.hasConcept()){
						//System.out.println("linked to '"+voisin.getConceptId()+"'");
						if(currentMap.containsKey(voisin.getConceptId()))
							currentMap.put(voisin.getConceptId(), currentMap.get(voisin.getConceptId()) + 1);
						else
							currentMap.put(voisin.getConceptId(), 1);
					}
				}
			}
			// On enregistre dans Hbase
			if (i % nbConcepts == 0){
				for(Text conceptSource : map.keySet()){
					Map<Text, Integer> tmpMap = map.get(conceptSource);
					Put tmpPut = new Put(conceptSource.getBytes());
					for(Text conceptCible: tmpMap.keySet()){
						tmpPut.add(Bytes.toBytes("voisins"), conceptCible.getBytes(), Bytes.toBytes(tmpMap.get(conceptCible)));
					}
					if(!tmpPut.isEmpty())
						DataModel.tables.get(DataModel.CONCEPTGRAPH_TBL).put(tmpPut);
				}
				System.out.println(nbConcepts+" concepts registered");
			}
			i++;
		}
		
	}
}
