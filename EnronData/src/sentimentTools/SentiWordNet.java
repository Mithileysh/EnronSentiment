
/**
 * Created by Sisi on 9/03/2015.
 */
package sentimentTools;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.TreeMap;

    public class SentiWordNet {

        private Map<String, Double> sentiDictionary;
        private Map<String,Double> sentiLexicon;
        private Map<String, String> wordList;
        private ArrayList<String> synList;
        static HashMap<String, HashMap<Integer, Double>> tempDictionary;
        
        private double sigma;

        public SentiWordNet(String filePath) throws IOException {
            // This is our main dictionary representation
            sentiDictionary = new HashMap<String, Double>();

            // From String to list of doubles.
            tempDictionary = new HashMap<String, HashMap<Integer, Double>>();
            

            BufferedReader sentiCSV = null;
            try {
                sentiCSV = new BufferedReader(new FileReader(filePath));
                int lineNumber = 0;

                String line;
                while ((line = sentiCSV.readLine()) != null) {
                    lineNumber++;

                    // If it's a comment, skip this line.
                    if (!line.trim().startsWith("#")) {
                        // We use tab separation
                        String[] data = line.split("\t");
                        String wordTypeMarker = data[0];

                        // Example line:
                        // POS ID PosS NegS SynsetTerm#sensenumber Desc
                        // a 00009618 0.5 0.25 spartan#4 austere#3 ascetical#2
                        // ascetic#2 practicing great self-denial;...etc

                        // Is it a valid line? Otherwise, through exception.
                        if (data.length <= 5 ) {
                            throw new IllegalArgumentException(
                                    "Incorrect tabulation format in file, line: "
                                            + lineNumber);
                        }

                        // Calculate synset score as score = PosS - NegS
                        Double synsetScore = Double.parseDouble(data[2])
                                - Double.parseDouble(data[3]);

                        // Get all Synset terms
                        String[] synTermsSplit = data[4].split(" ");

                        // Go through all terms of current synset.
                        for (String synTermSplit : synTermsSplit) {
                            // Get synterm and synterm rank
                            String[] synTermAndRank = synTermSplit.split("#");
                            String synTerm = synTermAndRank[0] + "#"
                                    + wordTypeMarker;

                            int synTermRank = Integer.parseInt(synTermAndRank[1]);
                            // What we get here is a map of the type:
                            // term -> {score of synset#1, score of synset#2...}

                            // Add map to term if it doesn't have one
                            if (!tempDictionary.containsKey(synTerm)) {
                                tempDictionary.put(synTerm,
                                        new HashMap<Integer, Double>());
                            }

                            // Add synset link to synterm
                            tempDictionary.get(synTerm).put(synTermRank,
                                    synsetScore);
                            
                        }
                    }
                }

                // Go through all the terms.
                for (Map.Entry<String, HashMap<Integer, Double>> entry : tempDictionary
                        .entrySet()) {
                    String word = entry.getKey();
                    Map<Integer, Double> synSetScoreMap = entry.getValue();
                    
                    // Calculate weighted average. Weigh the synsets according to
                    // their rank.
                    // Score= 1/2*first + 1/3*second + 1/4*third ..... etc.
                    // Sum = 1/1 + 1/2 + 1/3 ...
                    double score = 0.0;
                    double sum = 0.0;
                    for (Map.Entry<Integer, Double> setScore : synSetScoreMap
                            .entrySet()) {
                        score += setScore.getValue() / (double) setScore.getKey();
                        sum += 1.0 / (double) setScore.getKey();
                    }
                    score /= sum;

                    sentiDictionary.put(word, score);
                    //System.out.println(tempDictionary.keySet().toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (sentiCSV != null) {
                    sentiCSV.close();
                }
            }
        }
        public double extract (String synTerm){
        	return sentiDictionary.get(synTerm.replace(" ", ""));
        }

        public double extract(String word, String pos) {
        	
        	double score = 0;
        	String wordLow = word.toLowerCase();
        	String[] termList = tempDictionary.keySet().toString().split(",");
        	
        	for (String terms: termList){
        		String[] wordList = terms.split("#");
        		
        		for (String keyWord: wordList){
        			
        			if ((terms.contains((wordLow + "#" + pos ))) && (keyWord.equals(" " + wordLow))){
                		
                		score = sentiDictionary.get(wordLow + "#" + pos);
                	    return score;
                	}else {
                	    score = 0;
                	    
                	}
        		}
        	         			
        	}
                return 0;
      
        }
        
        //Create sentiment lexicon based on scores + to -
        public Map<String, Double> lexicon() {
        	sigma = 0.0001;
        	double score = 0;
        	
            String[] termDict = tempDictionary.keySet().toString().split(",");
            
            synList = new ArrayList<String>();
        	for (String terms: termDict){
                //System.out.println(terms);
        		synList.add(terms);        
        	}
        	sentiLexicon = new HashMap<String, Double>();
        	
        	
        	//System.out.println(synList.get(0).replace("[", "") );
        	sentiLexicon.put(synList.get(0).replace("[", ""), sentiDictionary.get(synList.get(0).replace("[", "")));
        	
        	for (int i = 1; i< synList.size()-2;i++){
    			score = sentiDictionary.get(synList.get(i).replace(" ", ""));
    			//if (score > Math.abs(sigma))
                //System.out.println(synList.get(i) + " " + score);    		
        	    sentiLexicon.put(synList.get(i).replace(" ", ""), score);
        	}
        	sentiLexicon.put(synList.get(synList.size()-1).replace("]", ""), sentiDictionary.get(synList.get(synList.size()-1).replace("]", "")));
        	//System.out.println(sentiLexicon.entrySet());
        	
        	return sentiLexicon;
        	
        }
        
        public Map<String, String> wordLexicon(){
        	Map<String, String> swnLexicon = new HashMap<String, String>();
        	
        	for (String terms: lexicon().keySet()){
        		String[] termList = terms.split("#");
        		swnLexicon.put(termList[0], terms);
        		
        	}
        	return swnLexicon;
        }

        
		

       

}
