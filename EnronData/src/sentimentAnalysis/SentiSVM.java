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
		static ArrayList<String> bodyList;
		static ArrayList<String> tokenList;
		
		static CharArraySet STOP_WORDS_SET;
		
		static double sigma;
		static double score;
		
		static int index;
		static int nextIndex;
		static List<String> subList;
		static ArrayList<String> subArray;
		
		static String POSLIST = "positive-words.txt";
		static String NEGLIST = "negative-words.txt";
		
		static SentiWordList swl;
		static ArrayList<String> tempLexicon;
		static ArrayList<String> tempDictionary;
		
		static simpleKMeans skm;
		
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
		public static double normalize(int value, ArrayList<Integer> intList){
			
			double norValue = 0.0;
			Collections.sort(intList, new Comparator<Integer>() {
				
		        public int compare(Integer o1, Integer o2)
		        {

		            return  o1.compareTo(o2);
		        }
	
		    });
			if (! intList.isEmpty()){
				double divident = (intList.get(intList.size()-1) - intList.get(0));
				if (divident == 0 ){
					divident = 0.5;
					norValue = (value - intList.get(0)) / divident;
				}
				norValue = (value - intList.get(0)) / divident;	
			}else{
				return 0.0;
			}
			
			
			
			return norValue;
		}
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
				sql = "SELECT DISTINCT mid, date, body FROM enron.message WHERE subject NOT LIKE 'Re%'OR subject NOT LIKE ('FW%' OR '%FW%') LIMIT 200 ";
				ResultSet rs = myStmt.executeQuery(sql);
				count = 0;
				
				String outputFile = "enronemail_svmlight_features_kmeanslabel_#7.dat";
				
				FileWriter fileWriter = null;	
				fileWriter = new FileWriter (outputFile);
				
				StopAnalyzer stopAnalyzer = new StopAnalyzer();
				CharArraySet stopWords= stopAnalyzer.ENGLISH_STOP_WORDS_SET;
				//System.out.println(stopWords.toString());
							
				swl = new SentiWordList();
				
				//create kmeans labels
				skm = new simpleKMeans();
				
				skm.cluster("enronemail_kmeans_features_2h_normalized.csv");
				
				ArrayList<Integer> classLabel = new ArrayList<Integer>();
				for (int label: skm.classNum()){
					classLabel.add(label);
				}
				
				
				//SWN labels
				/*
				ArrayList<Integer> classLabel = new ArrayList<Integer>();
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(-1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(0);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(-1);
				classLabel.add(-1);
				classLabel.add(-1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(-1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(-1);
				classLabel.add(1);
				classLabel.add(-1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(-1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(-1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(-1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(-1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(-1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(-1);
				classLabel.add(-1);
				classLabel.add(-1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(-1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(0);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(-1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(-1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(-1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(-1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(-1);
				classLabel.add(-1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(-1);
				classLabel.add(-1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(-1);
				classLabel.add(-1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(-1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(-1);
				classLabel.add(0);
				classLabel.add(-1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(-1);
				classLabel.add(1);
				classLabel.add(-1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(-1);
				classLabel.add(-1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(-1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(-1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
				classLabel.add(1);
                */
				
				
				for (String word: swl.createList(POSLIST)){
					tempLexicon.add(word);
					
				}
				for (String words: swl.createList(NEGLIST)){
					tempLexicon.add(words);
				}
				int number = 0;
				while (rs.next()){
					number++;
					tempDictionary = new ArrayList<String>();
					InputStream tokenmodelIn = new FileInputStream("en-token.bin");
					InputStream posmodelIn = new FileInputStream("en-pos-maxent.bin");
					
					EnglishStemmer stemmer = new EnglishStemmer();
					
			
					id = rs.getInt("mid");
					date = rs.getString("date");
					body = rs.getString("body");
					
					String bodyStr = body.toLowerCase();
					
					bodyList = new ArrayList<String>();
			        bodyList.add(bodyStr);
			        
			        //System.out.println(bodyList);
			        
			        try {
			        	
							
						TokenizerModel tokenModel = new TokenizerModel(tokenmodelIn);
						TokenizerME tokenizer = new TokenizerME(tokenModel);
						  		  
						String[] tokens = tokenizer.tokenize(bodyList.get(0));
											
						POSModel posModel = new POSModel(posmodelIn);
						POSTaggerME tagger = new POSTaggerME(posModel);
						
						System.out.print(id + ", ");	
						
						//fileWriter.append(id + ", ");
						
						fileWriter.append( classLabel.get(number-1) +" ");
						
						for (String token : tokens){
							
							//for (String synTerms: sentiwordNet.lexicon().keySet()){
								
								
								tokenList= new ArrayList<String>();
								tokenList.add(token);
							
								if (stopWords.toString().contains(token)){
									tokenList.remove(token);
								}else if (token.contains("=") | token.contains(".")){
									token.replace("=","");
									token.replace(",", "");
									
								}
								else{
									stemmer.setCurrent(token);
									stemmer.stem();
																
									tokenList.add(stemmer.getCurrent());
									  
								@SuppressWarnings("deprecation")
								List<String> tags = tagger.tag(tokenList);
								
								for(String tag: tags){
									if ( tag.contains("VB")){
										tag = "v";
										tempDictionary.add( token);
									}
									else if (tag.contains("NN")){
										tag = "n";
										tempDictionary.add(token);
						  
									}
									else if (tag.contains("JJ")){
										tag = "a";
										tempDictionary.add(token);
									}
									else if (tag.contains("RB")){
										tag = "r";
										tempDictionary.add(token);
									}
									else{
										tokenList.remove(token);								  
									}	 
								}
								
								//}
						}
						//System.out.println(tempDictionary.toString());
							//sentiDictionary.put(token, score);
						
						}
						int attr = 0;
						
						ArrayList<Integer> sizeList = new ArrayList<Integer>();
					    
							for(String tempPosterm : swl.createList(POSLIST)) {
								attr++;
								HashMap<String, ArrayList<Integer>> synTerms = new HashMap<String, ArrayList<Integer>>();
														
								synTerms.put(tempPosterm, new ArrayList<Integer>(recursion(0,tempPosterm, tempDictionary)));
								int size;
								for(Entry<String, ArrayList<Integer>> entry : synTerms.entrySet()) {
									size = entry.getValue().size();
						            sizeList.add(size);
									
									
								}
								
							}
							int count = attr;
							
				            for (String tempNegterm:swl.createList(NEGLIST)){
				            	count++;
				            	HashMap<String, ArrayList<Integer>> synTerms = new HashMap<String, ArrayList<Integer>>();
								
								synTerms.put(tempNegterm, new ArrayList<Integer>(recursion(0,tempNegterm, tempDictionary)));
								int size;
								for(Entry<String, ArrayList<Integer>> entry : synTerms.entrySet()) {
									size = -entry.getValue().size();
								    sizeList.add(size);
									
									   
								}
								
							
				            }
				            //create wordlist labels
				            /*
				            int label = 0;
				            //System.out.println(sizeList.toString());
				            for (int value: sizeList){
								label += value;
								
				            }
				            if (label > 0){
				            	fileWriter.append("+1" + " ");
				            }else if (label < 0){
				            	fileWriter.append("-1" + " ");
				            }else{
				            	fileWriter.append("0" + " ");
				            }
				            */  
				   
				            int attrAG = 0;
				            for(String tempPosterm : swl.createList(POSLIST)) {
								attrAG++;
								HashMap<String, ArrayList<Integer>> synTerms = new HashMap<String, ArrayList<Integer>>();
														
								synTerms.put(tempPosterm, new ArrayList<Integer>(recursion(0,tempPosterm, tempDictionary)));
								
								int size;
								for(Entry<String, ArrayList<Integer>> entry : synTerms.entrySet()) {
									
									size = entry.getValue().size();
									
									if (size != 0 ){
										
									    fileWriter.append(attrAG + ":" + normalize(size, sizeList) + " ");
									}	//System.out.println(size);
								}
								
							}
							int countAG = attrAG;
							
				            for (String tempNegterm:swl.createList(NEGLIST)){
				            	countAG++;
				            	HashMap<String, ArrayList<Integer>> synTerms = new HashMap<String, ArrayList<Integer>>();
								
								synTerms.put(tempNegterm, new ArrayList<Integer>(recursion(0,tempNegterm, tempDictionary)));
								int size;
								for(Entry<String, ArrayList<Integer>> entry : synTerms.entrySet()) {
										size = -entry.getValue().size();
										if (size != -0){
											fileWriter.append(countAG + ":" + -normalize(size, sizeList) + " ");
										}
										
								}
								
							
				            }
						
				            
				   }
						
			
					catch (IOException e) {
					  e.printStackTrace();
					}
		            finally {
					  if (tokenmodelIn != null) {
					    try {
					      tokenmodelIn.close();
					    }
					    catch (IOException e) {
					    }
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
