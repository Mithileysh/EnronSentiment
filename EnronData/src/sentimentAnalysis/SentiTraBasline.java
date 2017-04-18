/*
 Copyright 2015 Sisi Liu
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
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
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


public class SentiTraBasline {
	
	//Create driver and url
	static final String DB_DRIVER = "com.mysql.jdbc.Driver";
	static final String URL = "jdbc:mysql://127.0.0.1:3306/enron";	
	
	//Database credentials
	static final String USER = "root";
	static final String PASSWORD = "root";
		
	static int id;
	static String date;
	static Date datef;
	static long datemilli;
	static double datenor;
	static String body;
	static String subject;
	static ArrayList<String> bodyList;
	static ArrayList<String> bodyArray;
	static ArrayList<String> tokenList;
	static ArrayList<String> tokenTag;
	
	static CharArraySet STOP_WORDS_SET;
	
	static DimensionReduce dr;
	static String SWNLEXICON = "revisedTR/enronemail_swn_weeklyupdate_00_02.txt";
	static Map<String, Integer> swnLexicon;
	static ArrayList<String> swnDictionary;
	
	static double sigma;
	static double score;
	
	//CREATE METHOD FOR EXTRACTING FEATURE WORDS
	public static String recursion(int mIndex, String str, ArrayList<String> strList){
				
		int index = findPosition(str, strList);
		
		if (index == -1){
			
			return null;
		}
		else{
			return str;
		}
		
		
	}
		
	public static int findPosition(String str, ArrayList<String> strList){
		
		return strList.indexOf(str);
	}
	
	//create method for converting features into longitude[-180,180] and latitude[-90,90]
	public static double normalizeLon(double value){
		ArrayList<Double> doubleList = new ArrayList<Double>();
		//doubleList.add(1.0); //training
		//doubleList.add(1087.0);
		
		doubleList.add(1.0); //business
		doubleList.add(2236.0);
				
		//doubleList.add(-0.75); //sundevil
		//doubleList.add(0.75);
		
		double norValue = 0.0;
		
		
		double divident = doubleList.get(1) - doubleList.get(0) ;
		norValue = (value - doubleList.get(0)) * 360 / divident - 180;
		return norValue;
		
	}
	
	public static double normalizeLat(double value){
		ArrayList<Double> doubleList = new ArrayList<Double>();
		
		//doubleList.add(-0.875); //training
		//doubleList.add(1.0);
		
		
		doubleList.add(-0.75); //business
		doubleList.add(0.875);
		
		
		//doubleList.add(-0.75); //sundevil
		//doubleList.add(0.75);
		
		
		double norValue = 0.0;
		
		double divident = doubleList.get(1) - doubleList.get(0) ;
		norValue = (value - doubleList.get(0)) * 180 / divident - 90;
			
		return norValue;
	}
	//create method for normalizing lon/lat into map pixel
	public static ArrayList<Double> pixelConvert(double longitude, double latitude){
		ArrayList<Double> pixelLL = new ArrayList<Double>();
		int mapWidth    = 1200;
		int mapHeight   = 900;

		// get x value
		double x = (longitude+180)*(mapWidth/360);
		
		if (latitude == 90){
			pixelLL.add(x);
			pixelLL.add(900.0);
			
		}else if (latitude == -90){
			pixelLL.add(x);
			pixelLL.add(0.0);
			
		}else{
			// convert from degrees to radians
			double latRad = latitude*(Math.PI)/180;

			// get y value
			double mercN = Math.log(Math.tan((Math.PI/4)+(latRad/2)));
			double y = (mapHeight/2)-(mapWidth*mercN/(2*(Math.PI)));
			
			pixelLL.add(x);
			pixelLL.add(y);

		}

		return pixelLL;
	}
		
	public static void main(String[] args) throws IOException {
		Connection myConn = null;
		Statement myStmt = null;
		
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
			//sql = "SELECT DISTINCT mid, subject, date, body FROM enron.message WHERE YEAR(date) = 2000 AND MONTH(date) = 01";
			//sql = "SELECT DISTINCT mid, subject, date, body FROM enron.message WHERE (YEAR(date) between 2000 and 2002) AND (subject LIKE '%sun devil%');";
			//sql = "SELECT DISTINCT mid, subject, date, body FROM enron.message WHERE (YEAR(date) between 2000 and 2002) AND (subject LIKE '%training%'OR subject LIKE '%lesson%'OR subject LIKE '%class%')";
			sql = "SELECT DISTINCT mid, subject, date, body FROM enron.message WHERE (YEAR(date) between 2000 and 2002) AND (subject LIKE '%weekly update%' OR subject LIKE '%weekly report%')";
						
			ResultSet rs = myStmt.executeQuery(sql);
			
			//String outputFile = "simpleTR/enronemail_swn_2001_01/enronemail_swn_tra_tempiv_lonlat_2001_01.txt";
			//String outputFile1 = "simpleTR/enronemail_swn_2001_01/enronemail_swn_tra_tempiv_pixel_2001_01.txt";
			//String outputFile2 = "simpleTR/enronemail_swn_2001_01/enronemail_swn_tra_tempiv_origin_2001_01.txt";
			//String outputFile3 = "simpleTR/enronemail_swn_2001_01/enronemail_swn_tra_temp_tv_pixel_2001_01.txt";
			//String outputFile = "revisedTR/enronemail_swn_00_02/enronemail_swn_sundevil_pixel.tra";
			//String outputFile = "revisedTR/enronemail_swn_00_02/enronemail_swn_training_pixel.tra";
			//String outputFile1 = "revisedTR/enronemail_swn_00_02/enronemail_swn_training_origin.txt";
			//String outputFile2 = "revisedTR/enronemail_swn_00_02/enronemail_swn_training_feature.txt";
			//String outputFile3 = "revisedTR/enronemail_swn_00_02/enronemail_swn_training_temporal_pixel.tra";
			
			String outputFile = "revisedTR/enronemail_swn_00_02/enronemail_swn_weekly_pixel.tra";
			String outputFile1 = "revisedTR/enronemail_swn_00_02/enronemail_swn_weekly_origin.txt";
			String outputFile2 = "revisedTR/enronemail_swn_00_02/enronemail_swn_weekly_feature.txt";
			String outputFile3 = "revisedTR/enronemail_swn_00_02/enronemail_swn_weekly_temporal_pixel.tra";
			
			
			
			FileWriter fileWriter = null;
			FileWriter fileWriter1 = null;
			FileWriter fileWriter2 = null;
			FileWriter fileWriter3 = null;
			
			fileWriter = new FileWriter (outputFile);
			fileWriter1 = new FileWriter (outputFile1);
			fileWriter2 = new FileWriter (outputFile2);
			fileWriter3 = new FileWriter (outputFile3);
			//fileWriter.append("id, ");
			
			StopAnalyzer stopAnalyzer = new StopAnalyzer();
			CharArraySet stopWords= stopAnalyzer.ENGLISH_STOP_WORDS_SET;
			//System.out.println(stopWords.toString());
			
			//retrieve reduced swn lexicon
			dr = new DimensionReduce();
			for(String terms: dr.swnDictionary(SWNLEXICON).keySet()){
				//swnLexicon.put(terms, dr.keyDictionary(SWNLEXICON).get(terms));
				
				//if (dr.swnDictionary(SWNLEXICON).get(terms) != 0.0){
					swnLexicon.put(terms, dr.keyDictionary(SWNLEXICON).get(terms));
					swnDictionary.add(terms);
					//fileWriter.append(terms + ", ");
					//System.out.println(terms + ": " + swnLexicon.get(terms));
				//}
				
			}
			
			System.out.println(swnLexicon.size());
			bodyArray = new ArrayList<String>();
			
			while (rs.next()){
				
				InputStream tokenmodelIn = new FileInputStream("en-token.bin");
				InputStream posmodelIn = new FileInputStream("en-pos-maxent.bin");
				
				EnglishStemmer stemmer = new EnglishStemmer();
				
				id = rs.getInt("mid");
				subject = rs.getString("subject").toLowerCase();
				date = rs.getString("date");
				body = rs.getString("body").toLowerCase();
				
		        //System.out.println(bodyList);
				
		        //if (subject != "" && subject.contains("fw:") == false && subject != "re:"){	
					try {
						
				        //System.out.println(id + ", ");
						
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
					    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
						df.setTimeZone(TimeZone.getTimeZone("UTC"));
						try{
							datef = df.parse(date);
							datemilli =  datef.getTime();
							datenor =  datemilli / 1000000;
							//System.out.println(datenor + " ");
						}catch (Exception dateex){
							dateex.printStackTrace();
						}
						
					    int size = 0;
					    for (String temp: tokenTag){
					    	
					    	if (recursion(0,temp, swnDictionary) != null)
								size++;
								
					    }
					    
					    int count = 0;
					    if (size > 1){
					    	System.out.println(id + " ");
					    	fileWriter.append(id + " " + size + " ");
					    	fileWriter1.append("<" + id + ", [" + body);
					    	fileWriter2.append("<" + id + ", " + size + ", " + datemilli + ", [");
					    	fileWriter3.append("<" + id + ", " + size + ", " + datemilli + ", [");
					    	
					    	
					    	
						    for (String tempTerm: tokenTag){
						    	count++;
						    	
						    	if (recursion(0,tempTerm, swnDictionary) != null){
						    	
						    		//fileWriter.append(new DecimalFormat("#.##").format(normalizeLon(swnLexicon.get(tempTerm))) + " " + new DecimalFormat("#.##").format(normalizeLat(dr.swnDictionary(SWNLEXICON).get(tempTerm))) + " ");
									//fileWriter.append(new DecimalFormat("#.##").format(pixelConvert(normalizeLon(datenor), normalizeLat(dr.swnDictionary(SWNLEXICON).get(tempTerm))).get(1)) + " " + new DecimalFormat("#.##").format(pixelConvert(normalizeLon(datenor), normalizeLat(dr.swnDictionary(SWNLEXICON).get(tempTerm))).get(0)) + " ");
									//fileWriter.append(dr.keyDictionary(SWNLEXICON).get(tempTerm) + " " + new DecimalFormat("#.#####").format(dr.swnDictionary(SWNLEXICON).get(tempTerm)) + " ");
						    		fileWriter.append(new DecimalFormat("#.##").format(pixelConvert(normalizeLon(count), normalizeLat(dr.swnDictionary(SWNLEXICON).get(tempTerm))).get(0)) + " " + new DecimalFormat("#.##").format(pixelConvert(normalizeLon(count), normalizeLat(dr.swnDictionary(SWNLEXICON).get(tempTerm))).get(1)) + " ");
						    		//fileWriter.append(count + " " + new DecimalFormat("#.##").format(dr.swnDictionary(SWNLEXICON).get(tempTerm)) + " ");
						    		
						    		fileWriter1.append(new DecimalFormat("#.##").format(pixelConvert(normalizeLon(swnLexicon.get(tempTerm)), normalizeLat(dr.swnDictionary(SWNLEXICON).get(tempTerm))).get(0)) + " " + new DecimalFormat("#.##").format(pixelConvert(normalizeLon(swnLexicon.get(tempTerm)), normalizeLat(dr.swnDictionary(SWNLEXICON).get(tempTerm))).get(1)) + " ");
									fileWriter2.append(tempTerm + ": " + new DecimalFormat("#.###").format(dr.swnDictionary(SWNLEXICON).get(tempTerm)) + " ");
									fileWriter3.append(new DecimalFormat("#.##").format(pixelConvert(normalizeLon(count), normalizeLat(dr.swnDictionary(SWNLEXICON).get(tempTerm))).get(0)) + " " + new DecimalFormat("#.##").format(pixelConvert(normalizeLon(count), normalizeLat(dr.swnDictionary(SWNLEXICON).get(tempTerm))).get(1)) + " ");
						    		
						    	}
							}
						    
						    
						    fileWriter.append("" + System.getProperty("line.separator"));
						    fileWriter1.append("]>" + System.getProperty("line.separator"));
						    fileWriter2.append("]>" + System.getProperty("line.separator"));
						    fileWriter3.append("]>" + System.getProperty("line.separator"));
						    
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
				//}
			}
			try{
			  fileWriter.flush();
			  fileWriter.close();
			  fileWriter1.flush();
			  fileWriter1.close();
			  fileWriter2.flush();
			  fileWriter2.close();
			  fileWriter3.flush();
			  fileWriter3.close();
			  
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
