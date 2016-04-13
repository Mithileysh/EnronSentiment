package sentimentTools;


import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.search.similarities.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
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
import java.util.TimeZone;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.tartarus.snowball.ext.EnglishStemmer;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import sentimentTools.SentiWordList;


public class DimensionReduce {
	
		
		//Create driver and url
		static final String DB_DRIVER = "com.mysql.jdbc.Driver";
		static final String URL = "jdbc:mysql://localhost:3306/enron";	
		
		//Database credentials
		static final String USER = "root";
		static final String PASSWORD = "root";
		
		
		static double sigma;
		static double score;
		
		static ArrayList<String> tokenList;
		
		static String body;
		static ArrayList<String> bodyList;
		static ArrayList<String> bodyListNew;
		static String subject;
		
		static String POSLIST = "positive-words.txt";
		static String NEGLIST = "negative-words.txt";
		static String SWNLIST = "SentiWordNet_3.0.0_20130122.txt";
		
		static SentiWordList swl;
		static SentiWordNet swn;
		static ArrayList<String> tempLexicon;
		static Map<String, String> tempDictionary;
		
		static TFIDFSimilarity tfidf;
		static long freq =0;
		static long count = 0;
		
		static String bodyStr;
		
		static Map<String, Double> reducedSWN;
		
		public Map<String, Double> swnDictionary (String filePath) throws IOException{
			reducedSWN = new HashMap<String, Double>();
			BufferedReader reducedSWNTXT = null;
			
			try {
		        reducedSWNTXT = new BufferedReader(new FileReader(filePath));
		        int lineNumber = 0;

		        String line;
		        while ((line = reducedSWNTXT.readLine()) != null) {
		            lineNumber++;
		            
		            String[] data = line.split("\t");
		            for (String item: data){
		            	String[] itemList = item.split(",");
		            	reducedSWN.put(itemList[0], Double.parseDouble(itemList[1]));
		            }
        
		        }
		       
		    } catch (Exception e) {
		        e.printStackTrace();
		    } finally {
		        if (reducedSWNTXT != null) {
		            reducedSWNTXT.close();
		        }
		    }
			return reducedSWN;
		}
		public static void main(String[] args) throws IOException, SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
			
			Connection myConn = null;
			Statement myStmt = null;
			
			//create empty arraylist for storing words from wordlist
			
			
			try{
				
				Class.forName(DB_DRIVER).newInstance();
			
			    // Get a connection to database
				myConn = DriverManager.getConnection(URL,USER,PASSWORD);
				//System.out.println("connected");
				System.out.println("connected");
				
				// Query from database
				myStmt = myConn.createStatement();
				String sql;
				sql = "SELECT DISTINCT subject,body FROM enron.message WHERE YEAR(date) = 2001 AND MONTH(date) = 01";
				
				ResultSet rs = myStmt.executeQuery(sql);
				
				//String outputFile = "results/enronemail_owl_2001_01_idf.txt";
				String outputFile = "results/enronemail_swn_2001_01.txt";
				
				FileWriter fileWriter = null;
				//FileWriter fileWriter1 = null;
				fileWriter = new FileWriter (outputFile);
				
				//stopwords normalization
				StopAnalyzer stopAnalyzer = new StopAnalyzer();
				CharArraySet stopWords= stopAnalyzer.ENGLISH_STOP_WORDS_SET;
				
				swn = new SentiWordNet(SWNLIST);
				tempLexicon = new ArrayList<String>();
				tempDictionary = new HashMap<String, String>();
				
				for (String words: swn.wordLexicon().keySet()){
					
					tempLexicon.add(words);
				}
				//System.out.println(tempLexicon.entrySet());
				System.out.println(tempLexicon.size() + " ");
				
				tokenList = new ArrayList<String>();
				while (rs.next()){
					
					InputStream tokenmodelIn = new FileInputStream("en-token.bin");
					InputStream posmodelIn = new FileInputStream("en-pos-maxent.bin");
					
					EnglishStemmer stemmer = new EnglishStemmer();
					
					body = rs.getString("body").toLowerCase();
					subject = rs.getString("subject");
					String tokenStr = "";
					if (subject != "" && subject.contains("fw:") == false && subject != "re:"){	
					
					    bodyList = new ArrayList<String>();	
					    bodyList.add(body);
			            TokenizerModel tokenModel = new TokenizerModel(tokenmodelIn);
						TokenizerME tokenizer = new TokenizerME(tokenModel);
							  		  
						String[] tokens = tokenizer.tokenize(bodyList.get(0));
						for (String token : tokens){
							stemmer.setCurrent(token);
							stemmer.stem();
										
							tokenStr += stemmer.getCurrent() + " ";
							if (stopWords.contains(stemmer.getCurrent())){
								tokenStr.replaceFirst(stemmer.getCurrent(),"");
							}else if (stemmer.getCurrent().contains("=") | stemmer.getCurrent().contains(".")|stemmer.getCurrent().contains("/")|stemmer.getCurrent().contains(":")){
								stemmer.getCurrent().replace("=","").replace("/", "");
								stemmer.getCurrent().replace(".", "").replace(":", "");
							}
							else{
								tokenStr +="";
							}	  
									
						}
						//System.out.println( tokenStr);
						tokenList.add(tokenStr);
					}
					
					
				}
			    System.out.println(tokenList.size() + " ");
			    
			    for (String tempWord: tempLexicon){
					int frequency = 0;
					for (String tokenStr: tokenList) {
					 	for (String token: tokenStr.split(" ")){
					 		if (token.equalsIgnoreCase(tempWord)) {
					            frequency++;
					            break;   
					        }
					    }
					}
					System.out.println(tempWord + ", " + frequency);
			        if (frequency != 0){
			           	//fileWriter.append(tempTerm + ", " + Math.log10(tokenList.size() / frequency) + "\n");
			            fileWriter.append(swn.wordLexicon().get(tempWord)  + ", " + swn.lexicon().get(swn.wordLexicon().get(tempWord)) + "\n");
			        
				   }
				}
				    
			    
				stopAnalyzer.close();
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
