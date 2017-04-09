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
package sentimentAnalysis;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
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
import opennlp.tools.util.Span;
import sentimentTools.DimensionReduce;
import sentimentTools.SentiWordNet;


public class SentiBaseline {
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
	static ArrayList<String> tokenTag;
	
	static CharArraySet STOP_WORDS_SET;
	
	static DimensionReduce dr;
	static String SWNLEXICON = "SimpleTR/enronemail_swn_2001_01.txt";
	static Map<String, Integer> swnLexicon;
	static ArrayList<String> swnDictionary;
	
	static double sigma;
	static double score;
	
	public static String recursion(int mIndex, String str, ArrayList<String> strList){
		
		
		int index = findPosition(str, strList);
		
		if (index == -1){
			
			return null;
		}
		else{
			return str;
		}
		
		
	}
	
	public static ArrayList<Integer> recursionFreq(int mIndex, String str, ArrayList<String> strList){
		
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
	   	
	    ArrayList<Integer> aa = recursionFreq(mIndex+nextIndex,str,subArray);
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
		swnLexicon = new HashMap<String, Integer>();
		swnDictionary = new ArrayList<String>();
		try{
			
			Class.forName(DB_DRIVER).newInstance();
		
		    // Get a connection to database
			myConn = DriverManager.getConnection(URL,USER,PASSWORD);
			System.out.println("connected");
			
			
			myStmt = myConn.createStatement();
			String sql;
			// Select from database
			sql = "SELECT DISTINCT mid, subject, date, body FROM enron.message WHERE YEAR(date) = 2001 AND MONTH(date) = 01";
			
			
			ResultSet rs = myStmt.executeQuery(sql);
			
			String outputFile = "SimpleTR/results/enronemail_swn_ts_2001_01.tra";
			
			FileWriter fileWriter = null;
			
			fileWriter = new FileWriter (outputFile);
			
			//fileWriter.append("id, ");
			
			StopAnalyzer stopAnalyzer = new StopAnalyzer();
			CharArraySet stopWords= stopAnalyzer.ENGLISH_STOP_WORDS_SET;
			//System.out.println(stopWords.toString());
			
			//retrieve reduced swn lexicon
			dr = new DimensionReduce();
			for(String terms: dr.swnDictionary(SWNLEXICON).keySet()){
				if (dr.swnDictionary(SWNLEXICON).get(terms) != 0.0){
					swnLexicon.put(terms, dr.keyDictionary(SWNLEXICON).get(terms));
					//fileWriter.append(terms + ", ");
				
				}
			}
			for(String words: swnLexicon.keySet()){
				swnDictionary.add(words);
			}
			System.out.println(swnLexicon.size());
			while (rs.next()){
				
				InputStream tokenmodelIn = new FileInputStream("en-token.bin");
				InputStream posmodelIn = new FileInputStream("en-pos-maxent.bin");
				
				EnglishStemmer stemmer = new EnglishStemmer();
				
				id = rs.getInt("mid");
				subject = rs.getString("subject").toLowerCase();
				date = rs.getString("date");
				body = rs.getString("body").toLowerCase();
				
		        //System.out.println(bodyList);
		        if (subject != "" && subject.contains("fw:") == false && subject != "re:"){	
					try {
						//fileWriter.append(id + ", ");
						
				        System.out.println(id + ", ");
						bodyList = new ArrayList<String>();	
				        bodyList.add(body);
		                TokenizerModel tokenModel = new TokenizerModel(tokenmodelIn);
					    TokenizerME tokenizer = new TokenizerME(tokenModel);
						  		  
					    POSModel posModel = new POSModel(posmodelIn);
						POSTaggerME tagger = new POSTaggerME(posModel);
						
					    String[] tokens = tokenizer.tokenize(bodyList.get(0));
					    tokenList = new ArrayList<String>();
					    tokenTag = new ArrayList<String>();
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
					    //System.out.println(tokenList.size());
					    @SuppressWarnings("deprecation")
					    List<String> tags = tagger.tag(tokenList);
				        //System.out.println(tags.size());
					    for (int i = 0; i< tokenList.size(); i++){
					        //System.out.println(tokenList.get(i) + "/" + tags.get(i) + " ");
							if ( tags.get(i).contains("VB")){
								tokenTag.add(tokenList.get(i) + "#v");
								
							    
							}
							else if (tags.get(i).contains("NN")){
								tokenTag.add(tokenList.get(i) + "#n");
								//tokenList.remove(i);
							    
							}
							else if (tags.get(i).contains("JJ")){
								tokenTag.add(tokenList.get(i) + "#a");
								//tokenList.remove(i);
							    
							}
							else if (tags.get(i).contains("RB")){
								tokenTag.add(tokenList.get(i) + "#r");
								//tokenList.remove(i);
							    
							}else{
								tokenList.remove(i);
							}
							    
							
					    }
					    //System.out.println(tokenTag.toString() );
					    /*
					    ArrayList<Integer> sizeList = new ArrayList<Integer>();
					    for(String tempTerm : swnLexicon) {
							
						    HashMap<String, ArrayList<Integer>> synTerms = new HashMap<String, ArrayList<Integer>>();
							synTerms.put(tempTerm, new ArrayList<Integer>(recursionFreq(0,tempTerm, tokenTag)));
					        int size;
						    for(Entry<String, ArrayList<Integer>> entry : synTerms.entrySet()) {
							
							    size = entry.getValue().size();
							    
							    if (size != 0){
							    	//double value = size *(dr.swnDictionary(SWNLEXICON).get(tempTerm));
								    sizeList.add(size);
							    }
							}
						}
					    
					    
					    double label = 0.0;
			            //System.out.println(sizeList.toString());
			            for (double value: sizeList){
							label += value;
							
			            }
			            if (label > 0){
			            	fileWriter.append("1" + " ");
			            }else if (label < 0){
			            	fileWriter.append("-1" + " ");
			            }else{
			            	fileWriter.append("0" + " ");
			            }
			            
			            int count = 0;
					    for(String tempTerm : swnLexicon) {
						    count++;
						    HashMap<String, ArrayList<Integer>> synTerms = new HashMap<String, ArrayList<Integer>>();
							synTerms.put(tempTerm, new ArrayList<Integer>(recursion(0,tempTerm, tokenTag)));
					        int size;
						    for(Entry<String, ArrayList<Integer>> entry : synTerms.entrySet()) {
							
							    size = entry.getValue().size();
							    if (size != 0){
								    fileWriter.append(count + ":" + size * (dr.swnDictionary(SWNLEXICON).get(tempTerm))+ " ");
								    //fileWriter1.append(tempPosterm + ":" + size + ", ");
							    }
							}
						}
						*/
					    int sizeList = 0;
					    for (String temp: tokenTag){
					    	
					    	if (recursion(0,temp, swnDictionary) != null)
								sizeList++;
								
					    }
					    if (sizeList != 0){
					    	fileWriter.append(id + " " + sizeList + " ");
						    for (String tempTerm: tokenTag){
						    	if (recursion(0,tempTerm, swnDictionary) != null){
						    		fileWriter.append( swnLexicon.get(tempTerm)+ " " + dr.swnDictionary(SWNLEXICON).get(tempTerm)+ " ");
									//fileWriter1.append(tempPosterm + ":" + size + ", ");
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
