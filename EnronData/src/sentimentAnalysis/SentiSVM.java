package sentimentAnalysis;

	/*
	 * Copyright 2015 Sisi Liu
	 * Licensed under the Apache License, Version 2.0 (the "License");
	 * you may not use this file except in compliance with the License.
	 * You may obtain a copy of the License at
	    http://www.apache.org/licenses/LICENSE-2.0
	 * Unless required by applicable law or agreed to in writing, software
	 * distributed under the License is distributed on an "AS IS" BASIS,
	 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	 * See the License for the specific language governing permissions and
	 * limitations under the License.
	 */

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.tartarus.snowball.ext.EnglishStemmer;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import sentimentTools.SentiWordList;
import sentimentTools.simpleKMeans;
import sentimentTools.tfidfSimilarity;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

	public class SentiSVM {
		static int count;
		//Create driver and url
		static final String DB_DRIVER = "com.mysql.jdbc.Driver";
		static final String URL = "jdbc:mysql://localhost:3306/enron";	
		
		//Database credentials
		static final String USER = "root";
		static final String PASSWORD = "root";
		
		static int id;
		static String date;
		static String body;
		static String subject;
		static ArrayList<String> bodyList;
		static ArrayList<String> tokenList;
		
		static CharArraySet STOP_WORDS_SET;
		
		static double sigma;
		static double score;
		
		static int index;
		static int nextIndex;
		static List<String> subList;
		static ArrayList<String> subArray;
		
		static String POSIDFLIST = "results/enronemail_owl_2001_01_idf.txt";
		static String NEGIDFLIST = "results/enronemail_owl_2001_01_idf_neg.txt";
		//static String POSIDFLIST = "results/enronemail_owl_dataset3_idf.txt";
		//static String NEGIDFLIST = "results/enronemail_owl_dataset3_idf_neg.txt";
			
		static tfidfSimilarity tfidf;
		static ArrayList<String> tempLexicon;
		static ArrayList<String> tempDictionary;
		
		static simpleKMeans skm;
		static ArrayList<Integer>classLabel;
		
	    public static ArrayList<Integer> recursion(int mIndex, String str, ArrayList<String> strList){
			
			ArrayList<Integer> hs = new ArrayList<Integer>();
			
			ArrayList<String> subArray = new ArrayList<String>();
			int index = findPosition(str, strList);
			
			if (index == -1){
				
				return hs;
			}
			
			int nextIndex = 0;
			
			nextIndex += index;
			
			hs.add(mIndex + index);
			
			nextIndex += 1;
			
			if(index >= strList.size()-1) 
				return hs;
			
		    List<String> subList = strList.subList(index+1,(strList.size()));
		    
		    
		    if(subList.isEmpty())
		    	return hs;
		    for (String subItem: subList){
		    	subArray.add(subItem);
		    }
		   	
		    ArrayList<Integer> aa = recursion(mIndex+nextIndex,str,subArray);
		    if(aa.isEmpty())
		    	return hs;
		    hs.addAll(aa);
			return hs;
		}
		
		public static int findPosition(String str, ArrayList<String> strList){
			
			return strList.indexOf(str);
		}
		
		public static void main(String[] args) throws IOException {
			Connection myConn = null;
			Statement myStmt = null;
			
			Map<String, Double> sentiDictionary = new HashMap<String,Double>();
			
			//create empty arraylist for storing words from wordlist
			tempLexicon = new ArrayList<String>();
			try{
				
				Class.forName(DB_DRIVER).newInstance();
			
			    // Get a connection to database
				myConn = DriverManager.getConnection(URL,USER,PASSWORD);
				System.out.println("connected");
				
				// Query from database
				myStmt = myConn.createStatement();
				String sql;
				sql = "SELECT DISTINCT mid, date, subject, body FROM enron.message WHERE YEAR(date) = 2001 AND MONTH(date) = 01";
				//sql = "SELECT DISTINCT mid, subject, date, body FROM enron.message WHERE sender = 'kevin.hyatt@enron.com' LIMIT 200 ";
				//sql = "SELECT DISTINCT mid, subject, date, body FROM enron.message WHERE sender = 'lorna.brennan@enron.com' LIMIT 200 ";
				//sql = "SELECT DISTINCT mid, subject, date, body FROM enron.message WHERE sender = 'christi.nicolay@enron.com' LIMIT 200 ";
				
				ResultSet rs = myStmt.executeQuery(sql);
				count = 0;
				
				//generate output file directory
									
				//String outputFile = "results/enronemail_owl_bow_kmeans_label_2001_01.csv";
				//String outputFile = "results/results_enronemail_2001_01/dat_files/enronemail_owl_tfidf_kmeans_wtnoise_label_2001_01.dat";
				//String outputFile = "results/results_enronemail_2001_01/dat_files/enronemail_owl_bow_kmeans_wtnoise_label_2001_01.dat";
				//String outputFile = "results/results_enronemail_2001_01/dat_files/enronemail_owl_tp_kmeans_wtnoise_label_2001_01.dat";
				
				String outputFile = "results/results_enronemail_2001_01/dat_files/enronemail_owl_tfidf'_kmeans_label#7_2001_01.dat";
				//String outputFile = "results/results_enronemail_2001_01/dat_files/enronemail_owl_tfidf_kmeans_label#5_2001_01.dat";
				//String outputFile = "results/results_enronemail_2001_01/dat_files/enronemail_owl_tfidf_kmeans_label#6_2001_01.dat";
				//String outputFile = "results/results_enronemail_2001_01/dat_files/enronemail_owl_tfidf_kmeans_label#7_2001_01.dat";
				
				//String outputFile1 = "results/enronemail_owl_bow_kmeans_label_2001_01.dat";
				//String outputFile1 = "results/enronemail_owl_bow_kmeans_2001_01.txt";
				//String outputFile = "results/enronemail_owl_bow_kmeans_label_2001_01.csv";
				//String outputFile = "results/kmeans_results/enronemail_owl_tp_kmeans_label_dataset1.dat";
				//String outputFile1 = "results/enronemail_owl_bow_kmeans_2001_01.txt";
				//String outputFile = "results/kmeans_results/enronemail_owl_bow_kmeans_label_dataset2.dat";
				//String outputFile = "results/kmeans_results/enronemail_owl_tp_kmeans_label_dataset3.dat";
				//String outputFile = "results/kmeans_results/enronemail_owl_tfidf_kmeans_label#7_dataset1.dat";
				//String outputFile = "results/kmeans_results/enronemail_owl_tfidf_kmeans_label#4_dataset2.dat";
				//String outputFile = "results/kmeans_results/enronemail_owl_tfidf_kmeans_label#7_dataset3.dat";
				
				//String outputFile = "results/kmeans_results/enronemail_owl_tp_kmeans_label_2001_01.dat";
				//String outputFile = "results/kmeans_results/enronemail_owl_tfidf_kmeans_label_2001_01.dat";
				//String outputFile = "results/kmeans_results/enronemail_owl_bow_kmeans_label_2001_01.dat";
				
				
				FileWriter fileWriter = null;
				fileWriter = new FileWriter (outputFile);
				
				
				//fileWriter.append("class, ");
				//fileWriter.append("[id][date]");
				
				//stopwords normalization
				StopAnalyzer stopAnalyzer = new StopAnalyzer();
				CharArraySet stopWords= stopAnalyzer.ENGLISH_STOP_WORDS_SET;
				
				//create kmeans labels
				skm = new simpleKMeans();
				
				//skm.cluster("data/enronemail_kmeans_set1_features.csv");
				//skm.cluster("data/enronemail_kmeans_set2_features.csv");
				//skm.cluster("data/enronemail_kmeans_set3_features.csv");
				
				//skm.cluster("results/kmeans_csv_files/enronemail_owl_tp_kmeans_2001_01.csv");
				skm.cluster("results/results_enronemail_2001_01/kmeans_csv_files/enronemail_owl_tfidf_kmeans_2001_01.csv");
				//skm.cluster("results/kmeans_csv_files/enronemail_owl_bow_kmeans_2001_01.csv");
				
				//skm.cluster("results/results_enronemail_2001_01/kmeans_csv_files/enronemail_owl_tfidf_kmeans_withoutnoise_2001_01.csv");
				//skm.cluster("results/results_enronemail_2001_01/kmeans_csv_files/enronemail_owl_bow_kmeans_wtnoise_2001_01.csv");
				//skm.cluster("results/results_enronemail_2001_01/kmeans_csv_files/enronemail_owl_tp_kmeans_wtnoise_2001_01.csv");
				
				//skm.cluster("results/kmeans_csv_files/enronemail_owl_tfidf_kmeans_dataset3.csv");
				//skm.cluster("results/kmeans_csv_files/enronemail_owl_tfidf_kmeans_dataset2.csv");
				//skm.cluster("results/kmeans_csv_files/enronemail_owl_tp_kmeans_dataset3.csv");
				
				classLabel = new ArrayList<Integer>();
				for (int label: skm.classNum()){
					classLabel.add(label);
				}
				System.out.println(classLabel.size());	
				//
				//swl = new SentiWordList();
				tfidf = new tfidfSimilarity();
				
				for (String word: tfidf.idfDictionary(POSIDFLIST).keySet()){
					tempLexicon.add(word);
					//fileWriter.append(word + ", ");
				}
				for (String words: tfidf.idfDictionary(NEGIDFLIST).keySet()){
					tempLexicon.add( words);
					//fileWriter.append(words + ", ");
				}
				
				//fileWriter.append("\n");
				int count = 0;
				//System.out.println(tempLexicon.size());
				while (rs.next()){
					
					InputStream tokenmodelIn = new FileInputStream("en-token.bin");
					InputStream posmodelIn = new FileInputStream("en-pos-maxent.bin");
					
					EnglishStemmer stemmer = new EnglishStemmer();
					
			
					id = rs.getInt("mid");
					
					date = rs.getString("date");
					body = rs.getString("body").toLowerCase();
					subject = rs.getString("subject").toLowerCase();
					bodyList = new ArrayList<String>();
			        bodyList.add(body);
			        
			        
			        //System.out.println(bodyList);
			        if (subject != "" && subject.contains("fw:") == false && subject != "re:"){	
						try {
							
							bodyList = new ArrayList<String>();	
					        bodyList.add(body);
			                TokenizerModel tokenModel = new TokenizerModel(tokenmodelIn);
						    TokenizerME tokenizer = new TokenizerME(tokenModel);
							  		  
						    String[] tokens = tokenizer.tokenize(bodyList.get(0));
						    tokenList = new ArrayList<String>();
						    for (String token : tokens){
							    
						    	if (token.contains("=") | token.contains(".")|token.contains("/")|token.contains(":")){ 
						    	    token.replaceAll(".", "").replaceAll("/", "").replaceAll("=", "").replaceAll(":", "");
						    		stemmer.setCurrent(token);
							        stemmer.stem();
										
							        tokenList.add(stemmer.getCurrent());
							        
							        if (stopWords.contains(stemmer.getCurrent())){
								       tokenList.remove(stemmer.getCurrent());
							        }
							
						    	}else{
								    stemmer.setCurrent(token);
						    		tokenList.add(stemmer.getCurrent());
							    }	  
									
						    }
							   
						    ArrayList<Integer> sizeList = new ArrayList<Integer>();
						    
							for(String tempPosterm : tfidf.idfDictionary(POSIDFLIST).keySet()) {
								
								HashMap<String, ArrayList<Integer>> synTerms = new HashMap<String, ArrayList<Integer>>();
														
								synTerms.put(tempPosterm, new ArrayList<Integer>(recursion(0,tempPosterm, tokenList)));
								int size;
								for(Entry<String, ArrayList<Integer>> entry : synTerms.entrySet()) {
									size = entry.getValue().size();
									if (size != 0 )
										sizeList.add(size);
								}
								
							}
							
				            for (String tempNegterm:tfidf.idfDictionary(NEGIDFLIST).keySet()){
				            	
				            	HashMap<String, ArrayList<Integer>> synTerms = new HashMap<String, ArrayList<Integer>>();
								
								synTerms.put(tempNegterm, new ArrayList<Integer>(recursion(0,tempNegterm, tokenList)));
								int size;
								for(Entry<String, ArrayList<Integer>> entry : synTerms.entrySet()) {
									size = -entry.getValue().size();
									if (size != -0)
										sizeList.add(size);   
								}
							
				            }
				            //if (sizeList.size() != 0){
				            	if (count < classLabel.size()){
									
								    fileWriter.append(classLabel.get(count) + " ");
								    count++;
								}    
								
						        System.out.println(id + ", ");
				            	int attCount = 0;
							    for(String tempPosterm : tfidf.idfDictionary(POSIDFLIST).keySet()) {
									attCount++;
									HashMap<String, ArrayList<Integer>> synTerms = new HashMap<String, ArrayList<Integer>>();
															
									synTerms.put(tempPosterm, new ArrayList<Integer>(recursion(0,tempPosterm, tokenList)));
									
									int size;
									for(Entry<String, ArrayList<Integer>> entry : synTerms.entrySet()) {
										
										size = entry.getValue().size();
										if (size != 0){
											//fileWriter.append(attCount + ":1 ");
											//fileWriter1.append(tempPosterm + ":" + normalize(size) + " ");
											fileWriter.append(attCount + ":" + size * tfidf.idfDictionary(POSIDFLIST).get(tempPosterm) + " ");
											//fileWriter.append(attCount + ":" + size + " ");
										}
									}
									
								}
								
					            for (String tempNegterm:tfidf.idfDictionary(NEGIDFLIST).keySet()){
					            	attCount++;
					            	HashMap<String, ArrayList<Integer>> synTerms = new HashMap<String, ArrayList<Integer>>();
									
									synTerms.put(tempNegterm, new ArrayList<Integer>(recursion(0,tempNegterm, tokenList)));
									int size;
									for(Entry<String, ArrayList<Integer>> entry : synTerms.entrySet()) {
										size = -entry.getValue().size();
										//fileWriter.append(size + ",");
										if (size != -0 ){
											//fileWriter.append(attCount + ":-1 ");
											//fileWriter1.append(tempNegterm + ":" + normalize(size) + " ");
											fileWriter.append(attCount + ":" + size * tfidf.idfDictionary(NEGIDFLIST).get(tempNegterm) + " ");
											//fileWriter.append(attCount + ":" + size + " ");
										}	
									}
								}
				            
				            	fileWriter.append(System.getProperty("line.separator"));
							
				            
				            //}
						    
						//}
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			    finally {
			    	if (tokenmodelIn != null) {
			        }
					try {
						 tokenmodelIn.close();
					}catch (IOException e) {
					}
				}
				if (posmodelIn != null) {
					try {
					   posmodelIn.close();
					}
				    catch (IOException e) {
					}
				}
			}
		}
		try{
		  fileWriter.flush();
		  fileWriter.close();
		  stopAnalyzer.close();
		}catch (IOException e) {
		  e.printStackTrace();
		}
				
		rs.close();
		myStmt.close();
		myConn.close();
		}catch (SQLException se){
				se.printStackTrace();
			}catch (Exception e){
				e.printStackTrace();
			}finally{
				
				try{
					if (myStmt != null){
						myStmt.close();
					}
				}catch (SQLException sse){
						sse.printStackTrace();
				}
				try{
					if (myConn != null)
						myConn.close();
				}catch (SQLException sqe){
					sqe.printStackTrace();
				}
			
			}
			

		}
	

}
