/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sentimentClustering;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *Distance function based on bag of words model
 * transform sentiment key words into bag of words model 
 * to calculate distance between two email.
 * @author jc166795
 */
public class DistanceBOW {
    double[] vector1;
    double[] vector2;
    
    public DistanceBOW(){
        
    }
    
    /**
     * 
     * @param e1 email 1
     * @param e2 email 2
     * @return distance between email 1 and email 2
     */
    public double distanceEmails(Email e1, Email e2){
        double distance = Double.POSITIVE_INFINITY;
        
        //when topics of two email are same
        if(e1.getTopic().equalsIgnoreCase(e2.getTopic())){
            distance = distance(e1.getSentiments(),e2.getSentiments());
        }
        
        return distance;
    }
    /**
     * calculate distance between sentiment bag of words
    */
    public double distance(List<String> sentiment1, List<String> sentiment2){
        double distance = Double.POSITIVE_INFINITY;
        if(sentiment1.size()==0 && sentiment2.size()==0)
            return 0;
        createBOW(sentiment1,sentiment2);
        
        //get weight of each word in BOW that used to distance calculation
        double[] weightVec1 = new double[vector1.length];
        double[] weightVec2 = new double[vector2.length];
        //System.out.println("size of words Sen1: " + sentiment1.size());
        //System.out.println("size of words Sen2: " + sentiment2.size());
        for(int i = 0; i < vector1.length; i++) {
            weightVec1[i] = (double)vector1[i]/sentiment1.size();
            //System.out.println("Sen1 - frequency : " + vector1[i] + " Weight:" + weightVec1[i]);
            weightVec2[i] = (double)vector2[i]/sentiment2.size();
           // System.out.println("Sen2 - frequency : " + vector2[i] + " Weight:" + weightVec2[i]);
        }
        
        //distance = calculateEuclidianDistance(vector1,vector2);//BOW distance without weight value
        distance = calculateEuclidianDistance(weightVec1,weightVec2); //distance with weight value
        
        return distance;
    }
    
    /**
     * Create bag of words for each sentiment keywords list
     * @param sentiment1
     * @param sentiment2 
     */
    public void createBOW(List<String> sentiment1, List<String> sentiment2){ 
        Set<String> uniqueWords = new HashSet<>();       
        //System.out.println("size of sentiment2: " + sentiment2.size());
        if(!sentiment1.isEmpty())
            uniqueWords.addAll(sentiment1);
        if(!sentiment2.isEmpty())
            uniqueWords.addAll(sentiment2);
        
        int length = uniqueWords.size();
        vector1 = new double[length];
        vector2 = new double[length];
        
        int i = 0;
        for(String word : uniqueWords){
            vector1[i] = Collections.frequency(sentiment1, word);
            vector2[i] = Collections.frequency(sentiment2, word);
            i++;
        }
        
    }
    
    /**
     * Euclidian distance
     * @param vector1
     * @param vector2
     * @return 
     */
    public double calculateEuclidianDistance(double[] vector1, double[] vector2) {
	double sum =0;	
	for(int i=0; i< vector1.length; i++){
		sum += Math.pow(vector1[i] - vector2[i], 2);
	}
	return Math.sqrt(sum);
	}
}
