package sentimentAnalysis;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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


public class TagCloud {

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
	
	static SentiWordList swl;
	static ArrayList<String> tempLexicon;
	static ArrayList<String> tempDictionary;
	
	static TFIDFSimilarity tfidf;
	static long freq =0;
	static long count = 0;
	
	static String bodyStr;
	static double idf;
	
	public static void main(String[] args) throws IOException, SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		
		Connection myConn = null;
		Statement myStmt = null;
		
		Map<String, Double> sentiDictionary = new HashMap<String,Double>();
		
		//create empty arraylist for storing words from wordlist
		tempLexicon = new ArrayList<String>();
		try{
			
			Class.forName(DB_DRIVER).newInstance();
		
		    // Get a connection to database
			myConn = DriverManager.getConnection(URL,USER,PASSWORD);
			//System.out.println("connected");
			
			// Query from database
			myStmt = myConn.createStatement();
			String sql;
			sql = "SELECT DISTINCT subject,body FROM enron.message WHERE YEAR(date) = 2001 AND MONTH(date) = 01";
			
			ResultSet rs = myStmt.executeQuery(sql);
			
			String outputFile = "data/enronemail_owl_ts_2001_01_tagcloud.csv";
			String outputFile1 = "data/enronemail_owl_ts_2001_01_tagcloud.txt";
			
			FileWriter fileWriter = null;
			FileWriter fileWriter1 = null;
			fileWriter = new FileWriter (outputFile);
			fileWriter1 = new FileWriter (outputFile1);
			fileWriter.append("Term, Frequency\n");
			//stopwords normalization
			StopAnalyzer stopAnalyzer = new StopAnalyzer();
			CharArraySet stopWords= stopAnalyzer.ENGLISH_STOP_WORDS_SET;
			
			swl = new SentiWordList();
			
			
			for (String word: swl.createList(POSLIST)){
				tempLexicon.add(word);
				
			}
			for (String words: swl.createList(NEGLIST)){
				tempLexicon.add( words);
			}
					
			bodyList = new ArrayList<String>();	
			
			while (rs.next()){
				
				InputStream tokenmodelIn = new FileInputStream("en-token.bin");
				InputStream posmodelIn = new FileInputStream("en-pos-maxent.bin");
				
				EnglishStemmer stemmer = new EnglishStemmer();
				
				body = rs.getString("body");
				subject = rs.getString("subject");
				
				if (subject != "" && subject.contains("fw:") == false && subject != "re:"){	
				    bodyStr = body.toLowerCase();
				
				    
				    bodyList.add(bodyStr);
		            
					
				}
				//System.out.println(tokenStr.length() + " ");
			  }
			  
			  for (String tempTerm : tempLexicon){
				int frequency = 0;
				for (String tokenStr: bodyList) {
				 	 for (String token: tokenStr.split(" ")){
				   		 if (token.equalsIgnoreCase(tempTerm)) {
				            frequency++;
					        fileWriter1.append(tempTerm + " ");
				                    
				          }
				     }
				 }
				    		                
		            //System.out.println(tokenList.size() + " ");
		            //System.out.println(tempTerm + " ");
		            System.out.println(frequency + ", ");
		            
		          if (frequency != 0){
		           	//fileWriter.append(tempTerm + "," + (1 + Math.log(tokenList.size() / frequency)) + "\n");
		           	fileWriter.append(tempTerm + ", " + frequency);
		          }
		            
		          fileWriter.append("\n");
		          fileWriter1.append("\n");
		        }
		
			stopAnalyzer.close();
			
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
