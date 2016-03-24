package conceptopedia.Actions;

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;

import conceptopedia.ORM.WikiArticle;
import conceptopedia.ORM.WikiConceptGraphNode;

public class ConceptInferer {
	private WikiArticle article;
	
	public ConceptInferer(String articleStr) throws IOException{ // infere le concept d'un article
		article = new WikiArticle( Bytes.toBytes(articleStr));
	}
	
	public boolean infereConcept() throws IOException{ //True si le concept inferer est bon
		
		//on recupere les concepts des voisins de l'article
		Set<WikiArticle> voisins_articles = article.getWikiArticlesVoisins();
		Set<Text> concepts_voisins_ids = new HashSet<Text>();
		Set<WikiArticle> candidate_articles = new HashSet<WikiArticle>();
		for (WikiArticle a : voisins_articles) {
			concepts_voisins_ids.add(a.getConceptId());
			candidate_articles.addAll(a.getWikiArticlesVoisins());
		}
		
		Set<WikiConceptGraphNode> candidate_concepts = new HashSet<WikiConceptGraphNode>();
		
		for (WikiArticle candidate : candidate_articles) {
			System.out.println("neighbors found : " + candidate.getId());
			try{
				WikiConceptGraphNode newVoisin = new WikiConceptGraphNode(candidate.getConceptId());
				candidate_concepts.add(newVoisin);
			}catch(NullPointerException e){
				e.printStackTrace();
				System.out.println("Concept not created yet: '"+candidate.getConceptId()+"'");
			}
		}
		
		//on teste les candidats
		int maxScore = -1;
		Text bestCandidate = null;
		Map<Text,Integer> candidatesScore =  new HashMap<Text, Integer>();
		
		for (WikiConceptGraphNode candidat : candidate_concepts) {
			Map<Text,Integer> liensCandidat = candidat.getLinks();
			int score = 0;
			for (Text noeudvoisin : liensCandidat.keySet()) {
				if(concepts_voisins_ids.contains(noeudvoisin))
					score += liensCandidat.get(noeudvoisin)^2;
			}
			if(score >= maxScore){
				maxScore = score;
				bestCandidate = candidat.getId();
			}
			System.out.println(candidat.getId().toString() + " score : " + score);
			candidatesScore.put(candidat.getId(),score);
		}
		
		//Ranking
		Map<Integer, List<Text>> tri_casier = new HashMap<Integer, List<Text>>();
		
		for (Text cKey : candidatesScore.keySet()) {
			if(tri_casier.containsKey(cKey))
				tri_casier.get(candidatesScore.get(cKey)).add(cKey);
			else{
				List<Text> newList = new LinkedList<Text>();
				newList.add(cKey);
 				tri_casier.put(candidatesScore.get(cKey), newList);
			}
		}
		System.out.println("-----------");
		System.out.println("Ranking :");
		System.out.println("-----------");
		//Affiche les 20 premiers
		int j=10;
		for(int i = maxScore; i>0 && j > 0; i--){
			if(tri_casier.containsKey(i)){
				Iterator<Text> it = tri_casier.get(i).iterator();
				while(it.hasNext() && j > 0){
					j--;
					System.out.println(i + " : " + it.next());
				}
			}
				
		}
		System.out.println(bestCandidate.toString() + " with a score of : "+ maxScore);
		return bestCandidate.equals(article.getConceptId());
	}
	
}
