package conceptopedia.Actions;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;

import conceptopedia.ORM.WikiArticle;
import conceptopedia.ORM.WikiConcept;

public class ConceptCreator {

	public void createConcepts(String startRow) throws IOException {
		
		ResultScanner scanner = WikiArticle.getScannerVoisinsInter(startRow);

		for (Result result = scanner.next(); (result != null); result = scanner.next()) {
			WikiArticle currentWikiArticle = new WikiArticle(result);
			System.out.println("[Article traité: '"+currentWikiArticle.getId() +"']");
			
			if(currentWikiArticle.hasConcept()) continue;
			
			Set<WikiArticle> voisins = currentWikiArticle.getWikiArticlesVoisinsInter();
			
			Set<Text> crosslinkKeysConcepts = new HashSet<Text>();

			for (WikiArticle crosslink : voisins) {
				if (crosslink.hasConcept())
					crosslinkKeysConcepts.add(crosslink.getConceptId());
			}

			// On crée un set pour éliminer les doublons de concepts récupérés
			// depuis les crosslinks

			if (crosslinkKeysConcepts.isEmpty()) {
				String conceptName = "C-" + currentWikiArticle.getId();
				System.out.println("Concept créé: '"+ conceptName + "'");
				currentWikiArticle.setConcept(new Text(conceptName));
				WikiConcept conceptreceptor = new WikiConcept(new Text(conceptName));
				conceptreceptor.appendArticle(currentWikiArticle.getId());
			} else if (crosslinkKeysConcepts.size() == 1) {
				Text conceptByteName = crosslinkKeysConcepts.iterator().next();
				System.out.println("Concept agrégé: '" + conceptByteName + "', article: '"+ currentWikiArticle.getId() + "'");
				currentWikiArticle.setConcept(new Text(conceptByteName));
				WikiConcept conceptreceptor = new WikiConcept(new Text(conceptByteName));
				conceptreceptor.appendArticle(currentWikiArticle.getId());
			} else {
				int length = crosslinkKeysConcepts.size();
				System.out.println("Gros concept, nb concepts déjà présents: " + length);
				Text[] conceptByteNames = new Text[length];
				int j = 0;
				for (Text bs : crosslinkKeysConcepts) {
					conceptByteNames[j] = bs;
					System.out.println(" - concept "+j+": " + Bytes.toString(bs.getBytes()));
					j++;
				}

				// On fusionne les concepts des articles crosslink voisins dans
				// le premier concept de la liste(récepteur)
				Set<Text> articles_voisins_uniques = new HashSet<Text>();
				// On ajoute les articles de chaque concept à un ensemble, pour pouvoir tous les ajouter au conceptreceptor
				WikiConcept conceptreceptor = new WikiConcept(conceptByteNames[0]);
				for (int i = 1; i < conceptByteNames.length; i++) {
					WikiConcept c = new WikiConcept(conceptByteNames[i]);
					articles_voisins_uniques.addAll(c.getArticlesIds());	
					c.remove();
				}

				articles_voisins_uniques.add(currentWikiArticle.getId());
				conceptreceptor.appendArticlesList(articles_voisins_uniques);
				
			}
		}
	}
}
