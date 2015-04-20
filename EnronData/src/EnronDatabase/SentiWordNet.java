package EnronDatabase;

/**
 * Created by Sisi on 9/03/2015.
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

    public class SentiWordNet {

        private Map<String, Double> dictionary;
        static HashMap<String, HashMap<Integer, Double>> tempDictionary;

        public SentiWordNet(String pathToSWN) throws IOException {
            // This is our main dictionary representation
            dictionary = new HashMap<String, Double>();

            // From String to list of doubles.
            tempDictionary = new HashMap<String, HashMap<Integer, Double>>();
            

            BufferedReader csv = null;
            try {
                csv = new BufferedReader(new FileReader(pathToSWN));
                int lineNumber = 0;

                String line;
                while ((line = csv.readLine()) != null) {
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
                        if (data.length != 6) {
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

                    dictionary.put(word, score);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (csv != null) {
                    csv.close();
                }
            }
        }

        public double extract(String word, String pos) {
        	double score = 0;
        	String wordLow = word.toLowerCase();
        	String[] termList = tempDictionary.keySet().toString().split(",");
        	
        	for (String terms: termList){
        		String[] wordList = terms.split("#");
        		
        		for (String keyWord: wordList){
        			//System.out.print(keyWord + ", ");
        			if ((terms.contains((wordLow + "#" + pos ))) && (keyWord.equals(" " + wordLow))){
                		
                		score = dictionary.get(wordLow + "#" + pos);
                	    return score;
                	}else {
                	    score = 0;
                	    
                	}
        		}
        		
        		         			
        	}
                return 0;
      
        }
        public static void main (String[] args) throws IOException{
        	String pathToSWN = "SentiWordNet_3.0.0_20130122.txt";
    		SentiWordNet sentiwordnet = new SentiWordNet(pathToSWN);
    		
    		System.out.println(sentiwordnet.extract("good", "a"));
    		System.out.println(sentiwordnet.extract("congratulations", "v"));
    		System.out.println(sentiwordnet.extract("skills", "n"));
        	
        }
        
		

       

}
