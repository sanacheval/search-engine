package finalproject;

import java.util.HashMap;
import java.util.ArrayList;

public class SearchEngine {
	public HashMap<String, ArrayList<String> > wordIndex;   // this will contain a set of pairs (String, LinkedList of Strings)	
	public MyWebGraph internet;
	public XmlParser parser;

	public SearchEngine(String filename) throws Exception{
		this.wordIndex = new HashMap<String, ArrayList<String>>();
		this.internet = new MyWebGraph();
		this.parser = new XmlParser(filename);
	}
	
	/* 
	 * This does a graph traversal of the web, starting at the given url.
	 * For each new page seen, it updates the wordIndex, the web graph,
	 * and the set of visited vertices.
	 * 
	 * 	This method will fit in about 30-50 lines (or less)
	 */
	public void crawlAndIndex(String url) throws Exception {
		if (internet.getVertices().contains(url)) {
			internet.setVisited(url, true);
		}
		//visit url
		else {
			internet.addVertex(url);
			internet.setVisited(url, true);
		}
		
		for(int i=0; i<parser.getLinks(url).size(); i++) {
			if (!internet.getVertices().contains(parser.getLinks(url).get(i))) {
				internet.addVertex(parser.getLinks(url).get(i));
			}
			internet.addEdge(url, parser.getLinks(url).get(i));
		}
		
		for(int i = 0; i<parser.getContent(url).size(); i++) {
			if (wordIndex.containsKey(parser.getContent(url).get(i).toLowerCase())) {
				ArrayList<String> list = new ArrayList<String>();
				list.addAll(wordIndex.getOrDefault(parser.getContent(url).get(i).toLowerCase(), null));
				list.add(url);
				wordIndex.put(parser.getContent(url).get(i).toLowerCase(), list);
			}
			else {
				ArrayList<String> list = new ArrayList<String>();
				list.add(url);
				wordIndex.put(parser.getContent(url).get(i).toLowerCase(), list);
			}
		}
		//recursive method through urls
		for (int i = 0; i<parser.getLinks(url).size(); i++) {
			if (!internet.getVisited(parser.getLinks(url).get(i))) {
				crawlAndIndex(parser.getLinks(url).get(i));
			}
		}
	}
	
	
	
	/* 
	 * This computes the pageRanks for every vertex in the web graph.
	 * It will only be called after the graph has been constructed using
	 * crawlAndIndex(). 
	 * To implement this method, refer to the algorithm described in the 
	 * assignment pdf. 
	 * 
	 * This method will probably fit in about 30 lines.
	 */
	public void assignPageRanks(double epsilon) {
		ArrayList<String> urls = internet.getVertices();
		for (int i = 0; i<urls.size(); i++) {
			internet.setPageRank(urls.get(i), 1.0);
		}
		ArrayList<Double> ranks = computeRanks(urls);
		ArrayList<Double> temp = computeRanks(urls);
		for (int i = 0; i<ranks.size(); i++) {
			while (Math.abs(temp.get(i)-ranks.get(i))>=epsilon) {
				ranks = temp;
				temp = computeRanks(urls);
			}
		}
	}

	/*
	 * The method takes as input an ArrayList<String> representing the urls in the web graph 
	 * and returns an ArrayList<double> representing the newly computed ranks for those urls. 
	 * Note that the double in the output list is matched to the url in the input list using 
	 * their position in the list.
	 */
	public ArrayList<Double> computeRanks(ArrayList<String> vertices) {
		ArrayList<Double> ranks = new ArrayList<Double>();
		for (int i = 0; i<vertices.size(); i++) {
			Double add = 0.0;
			for (int j=0; j<vertices.size(); j++) {
				if (internet.getEdgesInto(vertices.get(i)).contains(vertices.get(j))) {
					add = add + (internet.getPageRank(vertices.get(j))/internet.getOutDegree(vertices.get(j)));
				}
			}
			Double pageRank = (0.5) + (0.5*(add));
			ranks.add(pageRank);
		}
		for (int i=0; i<vertices.size(); i++) {
			internet.setPageRank(vertices.get(i), ranks.get(i));
		}
		return ranks;
	}

	
	/* Returns a list of urls containing the query, ordered by rank
	 * Returns an empty list if no web site contains the query.
	 * 
	 * This method should take about 25 lines of code.
	 */
	public ArrayList<String> getResults(String query) {
		if (wordIndex.containsKey(query)) {
			HashMap<String, Double> url = new HashMap<String, Double>();
			ArrayList<String> links = wordIndex.get(query);
			ArrayList<Double> ranks = new ArrayList<Double>();
			ArrayList<String> output = new ArrayList<String>();
			for (int i = 0; i<links.size(); i++) {
				ranks.add(internet.getPageRank(links.get(i)));
			}
			for (int i=0; i<links.size(); i++) {
				url.put(links.get(i), ranks.get(i));
			}
			output = Sorting.fastSort(url);
			return output;
		}
		else {
			ArrayList<String> empty = null;
			return empty;
		}
	}
}
