//
// Copyright (c) 2012 Mirko Nasato
//
// Permission is hereby granted, free of charge, to any person obtaining a
// copy of this software and associated documentation files (the "Software"),
// to deal in the Software without restriction, including without limitation
// the rights to use, copy, modify, merge, publish, distribute, sublicense,
// and/or sell copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included
// in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
// THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
// OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
// ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
// OTHER DEALINGS IN THE SOFTWARE.
//
package conceptopedia.Actions;

import java.util.HashMap;
import java.util.Map;

public class ProgressCounter {

	private static final int THOUSAND = 1000;
	private static final int SMALL_STEP = 1 * THOUSAND;
	private static final int BIG_STEP = 50 * THOUSAND;

	private int count = 0;
	private int countVoisins = 0;
	private int voisinsMax = 0;
	private int voisinsMin = 42;
	private Map<Integer, Integer> voisins;

	public int getCount() {
		return count;
	}

	public ProgressCounter(){
		voisins = new HashMap<Integer, Integer>();
	}
	
	public void increment(int nb) {
		count++;
		countVoisins += nb;
		if (voisins.containsKey(nb))
			voisins.put(nb, voisins.get(nb) + 1);
		else
			voisins.put(nb, 1);

		if (nb <= voisinsMin)
			voisinsMin = nb;
		if (nb >= voisinsMax)
			voisinsMax = nb;

		if (count % SMALL_STEP == 0) {
			System.out.println("count:" + count + ", voisins/noeud:"
					+ countVoisins / count + ", MinMax: " + voisinsMin + ","
					+ voisinsMax);
			displayMap();
		}
	}

	private void displayMap() {
		int lastIndex=0;
		int intervalle=5;
		int barre=0;
		for(Integer key: voisins.keySet()){
			while(key-lastIndex >= intervalle){
				System.out.println("nb elements entre "+ lastIndex + " et " + (lastIndex+intervalle + ": "+ barre));
				barre=0;
				lastIndex+=intervalle;
				if(lastIndex > 100)
					intervalle = 25;
			}
				
			if(key-lastIndex < intervalle)
				barre+= voisins.get(key);

		}
		
	}

}
