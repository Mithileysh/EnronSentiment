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


	public class SentiPolarity {
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
		
		static tfidfSimilarity tfidf;
		static String POSIDFLIST = "results/enronemail_owl_2001_01_idf.txt";
		static String NEGIDFLIST = "results/enronemail_owl_2001_01_idf_neg.txt";
		//static String POSIDFLIST = "results/enronemail_owl_dataset3_idf.txt";
		//static String NEGIDFLIST = "results/enronemail_owl_dataset3_idf_neg.txt";
		
		static ArrayList<String> tempLexicon;
		static ArrayList<String> tempDictionary;
		
		
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
		/*
		public static double normalize(int value){
			ArrayList<Integer> intList = new ArrayList<Integer>();
			intList.add(-30);
			intList.add(17);
			double norValue = 0.0;
			
			if (! intList.isEmpty()){
				double divident = (intList.get(intList.size()-1) - intList.get(0));
				if (divident == 0 ){
					norValue =  0.5;
				}
				norValue = ((value - intList.get(0)) / divident) ;	//scale standard normalized value to [-1,1]
			}else{
				return 0.0;
			}
			
			return norValue;
		}
		*/
		public static void main(String[] args) throws IOException {
			Connection myConn = null;
			Statement myStmt = null;
			
			Map<String, Double> sentiDictionary = new HashMap<String,Double>();
			tempLexicon = new ArrayList<String>();
			try{
				
				Class.forName(DB_DRIVER).newInstance();
			
			    // Get a connection to database
				myConn = DriverManager.getConnection(URL,USER,PASSWORD);
				System.out.println("connected");
				
				
				myStmt = myConn.createStatement();
				String sql;
				
				sql = "SELECT DISTINCT mid, date, subject, body FROM enron.message WHERE YEAR(date) = 2001 AND MONTH(date) = 01";
				//sql = "SELECT DISTINCT mid, subject, date, body FROM enron.message WHERE sender = 'kevin.hyatt@enron.com' LIMIT 200 ";
				//sql = "SELECT DISTINCT mid, subject, date, body FROM enron.message WHERE sender = 'lorna.brennan@enron.com' LIMIT 200 ";
				//sql = "SELECT DISTINCT mid, subject, date, body FROM enron.message WHERE sender = 'christi.nicolay@enron.com' LIMIT 200 ";
				
				
				ResultSet rs = myStmt.executeQuery(sql);
				count = 0;
				
				//String outputFile = "results/enronemail_owl_tfidf_polarity_label_2001_01.dat";
				//String outputFile1 = "results/enronemail_owl_bow_polarity_label_2001_01.csv";
				//String outputFile = "results/enronemail_owl_tp_polarity_label_2001_01.dat";				
				
				//String outputFile = "results/polarity_dat_files/enronemail_owl_tp_polarity_dataset1.dat";
				//String outputFile = "results/polarity_dat_files/enronemail_owl_bow_polarity_dataset2.dat";
				//String outputFile = "results/polarity_dat_files/enronemail_owl_tfidf_polarity_dataset3.dat";
				//String outputFile = "results/polarity_dat_files/enronemail_owl_tp_polarity_dataset2.dat";
				//String outputFile = "results/polarity_dat_files/enronemail_owl_bow_polarity_dataset3.dat";
				//String outputFile = "results/polarity_dat_files/enronemail_owl_tfidf_polarity_dataset1.dat";
				//String outputFile = "results/polarity_dat_files/enronemail_owl_tp_polarity_dataset3.dat";
				//String outputFile = "results/polarity_dat_files/enronemail_owl_bow_polarity_dataset1.dat";
				//String outputFile = "results/polarity_dat_files/enronemail_owl_tfidf_polarity_dataset2.dat";
				
				//String outputFile = "results/results_enronemail_2001_01/dat_files/enronemail_owl_tfidf_polarity_wtnoise.dat";
				//String outputFile = "results/results_enronemail_2001_01/dat_files/enronemail_owl_bow_polarity_wtnoise.dat";
				String outputFile = "results/results_enronemail_2001_01/dat_files/enronemail_owl_tp_polarity_wtnoise.dat";
				
				FileWriter fileWriter = null;
				//FileWriter fileWriter1 = null;
				fileWriter = new FileWriter (outputFile);
				//fileWriter1 = new FileWriter (outputFile1);
				
				//fileWriter1.append("class, ");
				
				StopAnalyzer stopAnalyzer = new StopAnalyzer();
				CharArraySet stopWords= stopAnalyzer.ENGLISH_STOP_WORDS_SET;
				
				//create owl lexicon from reduced wordlist
				tfidf = new tfidfSimilarity();
				
				for (String word: tfidf.idfDictionary(POSIDFLIST).keySet()){
					tempLexicon.add(word);
					//fileWriter1.append(word + ", ");
				}
				for (String words: tfidf.idfDictionary(NEGIDFLIST).keySet()){
					tempLexicon.add( words);
					//fileWriter1.append(words + ", ");
				}
				//fileWriter1.append("\n");
				
				int number = 0;
				while (rs.next()){
					number++;

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
						
						    int attr = 0;
						
						    ArrayList<Double> sizeList = new ArrayList<Double>();
					        ArrayList<Integer> sizeListInt = new ArrayList<Integer>();
					        
							for(String tempPosterm : tfidf.idfDictionary(POSIDFLIST).keySet()) {
								attr++;
								HashMap<String, ArrayList<Integer>> synTerms = new HashMap<String, ArrayList<Integer>>();
														
								synTerms.put(tempPosterm, new ArrayList<Integer>(recursion(0,tempPosterm, tokenList)));
								int size;
								for(Entry<String, ArrayList<Integer>> entry : synTerms.entrySet()) {
									size = entry.getValue().size();
						            
									if (size != 0){
										sizeListInt.add(1);
										//sizeListInt.add(size);
										//sizeList.add(size * tfidf.idfDictionary(POSIDFLIST).get(tempPosterm));
									}
								}
								
							}
							int count = attr;
							
				            for (String tempNegterm:tfidf.idfDictionary(NEGIDFLIST).keySet()){
				            	count++;
				            	HashMap<String, ArrayList<Integer>> synTerms = new HashMap<String, ArrayList<Integer>>();
								
								synTerms.put(tempNegterm, new ArrayList<Integer>(recursion(0,tempNegterm, tokenList)));
								int size;
								for(Entry<String, ArrayList<Integer>> entry : synTerms.entrySet()) {
									size = -entry.getValue().size();
								    
									if(size != -0){
										sizeListInt.add(-1);
										//sizeListInt.add(size);
										//sizeList.add(size*tfidf.idfDictionary(NEGIDFLIST).get(tempNegterm));
										
									}
								}
								
							
				            }
				            
				            if (sizeListInt.size() != 0){
				            	System.out.println(id + ", ");
				            	
				            	//create wordlist labels
					            
					            double label = 0.0;
					            //System.out.println(sizeList.toString());
					            for (double value: sizeListInt){
									label += value;
									
					            }
					            if (label > 0.0){
					            	fileWriter.append("1" + " ");
					            	//fileWriter1.append("1" + ", ");
					            }else if (label < 0.0){
					            	fileWriter.append("-1" + " ");
					            	//fileWriter1.append("-1" + ", ");
					            }else{
					            	fileWriter.append("0" + " ");
					            	//fileWriter1.append("0" + ", ");
					            }
					            
					            int attrAG = 0;
					            for(String tempPosterm : tfidf.idfDictionary(POSIDFLIST).keySet()) {
									attrAG++;
									HashMap<String, ArrayList<Integer>> synTerms = new HashMap<String, ArrayList<Integer>>();
															
									synTerms.put(tempPosterm, new ArrayList<Integer>(recursion(0,tempPosterm, tokenList)));
									
									int size;
									for(Entry<String, ArrayList<Integer>> entry : synTerms.entrySet()) {
										
										size = entry.getValue().size();
										
										if (size !=0){
											//fileWriter.append(attrAG + ":" + size * tfidf.idfDictionary(POSIDFLIST).get(tempPosterm) + " ");
											fileWriter.append(attrAG+ ":1 ");
											//fileWriter.append(attrAG + ":" + size + " ");
											//fileWriter1.append(size + ", ");
										}
										//System.out.println(size);
									}
									
								}
								int countAG = attrAG;
								
					            for (String tempNegterm:tfidf.idfDictionary(NEGIDFLIST).keySet()){
					            	countAG++;
					            	HashMap<String, ArrayList<Integer>> synTerms = new HashMap<String, ArrayList<Integer>>();
									
									synTerms.put(tempNegterm, new ArrayList<Integer>(recursion(0,tempNegterm, tokenList)));
									int size;
									for(Entry<String, ArrayList<Integer>> entry : synTerms.entrySet()) {
											size = -entry.getValue().size();
											
											if (size != -0){
												fileWriter.append(countAG + ":-1 ");
												//fileWriter.append(countAG + ":"  + size * tfidf.idfDictionary(NEGIDFLIST).get(tempNegterm) + " ");
												//fileWriter.append(countAG + ":" + size + " ");
												//fileWriter1.append(size + ", ");
											}	
									}
								
					            }
					            fileWriter.append(System.getProperty("line.separator"));
					            
				            }
				   
				            
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
