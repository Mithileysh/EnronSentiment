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
package EnronDatabase;


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

import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.tartarus.snowball.ext.EnglishStemmer;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;


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
	static ArrayList<String> bodyList;
	static ArrayList<String> tokenList;
	
	static CharArraySet STOP_WORDS_SET;
	
	static double sigma;
	static double score;
	
	
	public static void main(String[] args) throws IOException {
		Connection myConn = null;
		Statement myStmt = null;
		
		Map<String, Double> sentiDictionary = new HashMap<String,Double>();
		
		try{
			
			Class.forName(DB_DRIVER).newInstance();
		
		    // Get a connection to database
			myConn = DriverManager.getConnection(URL,USER,PASSWORD);
			System.out.println("connected");
			
			
			myStmt = myConn.createStatement();
			String sql;
			sql = "SELECT DISTINCT mid, date, body FROM enron.message WHERE subject NOT LIKE 'Re%'OR subject NOT LIKE ('FW%' OR '%FW%') LIMIT 100 ";
			ResultSet rs = myStmt.executeQuery(sql);
			count = 0;
			String outputFile = "enronemail_svmlight_features_1w.csv";
			
			FileWriter fileWriter = null;	
			fileWriter = new FileWriter (outputFile);
			StopAnalyzer stopAnalyzer = new StopAnalyzer();
			CharArraySet stopWords= stopAnalyzer.ENGLISH_STOP_WORDS_SET;
			//System.out.println(stopWords.toString());
			
			while (rs.next()){
				
				InputStream tokenmodelIn = new FileInputStream("en-token.bin");
				InputStream posmodelIn = new FileInputStream("en-pos-maxent.bin");
				String pathToSWN = "SentiWordNet_3.0.0_20130122.txt";
				SentiWordNet sentiwordnet = new SentiWordNet(pathToSWN);
				
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
					double tokenProbs[] = tokenizer.getTokenProbabilities();
					
					POSModel posModel = new POSModel(posmodelIn);
					POSTaggerME tagger = new POSTaggerME(posModel);
					
					System.out.print(id + ", ");	
					
					
					fileWriter.append(id + ", ");
					
					for (String token : tokens){
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
								if (sentiwordnet.extract(token, "v") == 0.0){
									tokenList.remove(token);
								}
								else{
									//System.out.print(token + "/ "+ tag + ": " + sentiwordnet.extract(token, "v") + ", ");
							        fileWriter.append(sentiwordnet.extract(token, "v") + ", " );
							        score = sentiwordnet.extract(token, "v");
									sentiDictionary.put(token, sentiwordnet.extract(token, "v"));
								}
							}
							else if (tag.contains("NN")){
								tag = "n";
								if (sentiwordnet.extract(token, "n") == 0.0){
									tokenList.remove(token);
								}else{
									//System.out.print(token + "/ "+ tag + ": " + sentiwordnet.extract(token, "n") +", ");
									fileWriter.append(sentiwordnet.extract(token, "n") + ", " );
									score = sentiwordnet.extract(token, "n");
									sentiDictionary.put(token, sentiwordnet.extract(token, "n"));
								}
				  
							}
							else if (tag.contains("JJ")){
								tag = "a";
								if (sentiwordnet.extract(token, "a") == 0.0){
									tokenList.remove(token);
								}else{
									//System.out.print(token + "/ "+ tag + ": " + sentiwordnet.extract(token, "a") +", ");
									fileWriter.append(sentiwordnet.extract(token, "a") + ", " );
									score = sentiwordnet.extract(token, "a");
									sentiDictionary.put(token, sentiwordnet.extract(token, "a"));
								} 
							}
							else if (tag.contains("RB")){
								tag = "r";
								if (sentiwordnet.extract(token, "r") == 0.0){
									tokenList.remove(token);
								}else{
									//System.out.print(token + "/ "+ tag + ": " + sentiwordnet.extract(token, "r") +", ");
									fileWriter.append(sentiwordnet.extract(token, "r") + ", " );
									score = sentiwordnet.extract(token, "r");
									sentiDictionary.put(token, sentiwordnet.extract(token, "r"));
								}  
							}
							else{
								tokenList.remove(token);								  
							}	 
						}
						
						}
						//sentiDictionary.put(token, score);
					}
					//System.out.println("");
					fileWriter.append("\n");
					
					//}
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
