package EnronDatabase;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Map.Entry;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.tartarus.snowball.ext.EnglishStemmer;

import sentimentTools.SentiWordList;

public class DataGenerator {
	
	//Create driver and url
	static final String DB_DRIVER = "com.mysql.jdbc.Driver";
	static final String URL = "jdbc:mysql://localhost:3306/enron";	
	
	//Database credentials
	static final String USER = "root";
	static final String PASSWORD = "root";
		
	//variables for retrieving data from database
	static int id;
	static String date;
	static Date datef;
	static long datemilli;
	static String subject;
		
	static String body;
	static ArrayList<String> bodyList;
	static ArrayList<String> tokenList;
	static ArrayList<String> tempList;
			
	static CharArraySet STOP_WORDS_SET;
		
		
	public static void main(String[] args) throws IOException {
		Connection myConn = null;
		Statement myStmt = null;
		Map<Integer, ArrayList<String>> dataSet = new HashMap<Integer, ArrayList<String>>();	
		try{
			Class.forName(DB_DRIVER).newInstance();
			
			// Get a connection to database
			myConn = DriverManager.getConnection(URL,USER,PASSWORD);
			System.out.println("connected");
				
			// Query from database
			myStmt = myConn.createStatement();
			String sql;
			sql = "SELECT DISTINCT mid, date, subject, body FROM enron.message WHERE YEAR(date) = 2001 AND MONTH(date) = 1 LIMIT 5";
				
			ResultSet rs = myStmt.executeQuery(sql);
				
			//generate output file directory
				
			String outputFile = "results/enronemail_data_prepocessed.txt";
				
			FileWriter fileWriter = null;	
			fileWriter = new FileWriter (outputFile);
				
			//stopwords normalization
			StopAnalyzer stopAnalyzer = new StopAnalyzer();
			CharArraySet stopWords= stopAnalyzer.ENGLISH_STOP_WORDS_SET;
				
				
			while (rs.next()){
				tempList = new ArrayList<String>();
				InputStream tokenmodelIn = new FileInputStream("en-token.bin");
				InputStream posmodelIn = new FileInputStream("en-pos-maxent.bin");	
				EnglishStemmer stemmer = new EnglishStemmer();
					
				id = rs.getInt("mid");
				date = rs.getString("date");
				body = rs.getString("body").toLowerCase();
				bodyList = new ArrayList<String>();
			    bodyList.add(body);
			        
			    //System.out.println(bodyList);
			        
			    try {
			        	
							
					TokenizerModel tokenModel = new TokenizerModel(tokenmodelIn);
					TokenizerME tokenizer = new TokenizerME(tokenModel);
					  		  
					String[] tokens = tokenizer.tokenize(bodyList.get(0));
											
					POSModel posModel = new POSModel(posmodelIn);
					POSTaggerME tagger = new POSTaggerME(posModel);
						
					System.out.print(id + ", ");	
						
						
					fileWriter.append(id + ", ");
						
					DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					df.setTimeZone(TimeZone.getTimeZone("UTC"));
					try{
						datef = df.parse(date);
						datemilli = datef.getTime();
					
					}catch (Exception dateex){
						dateex.printStackTrace();
					}
						
					//fileWriter.append( datemilli + ", ");
						
					//tokenization and stemming
					tokenList= new ArrayList<String>();
					for (String token : tokens){
						//System.out.print(token + " ");	
						stemmer.setCurrent(token);
						if (stemmer.stem() == true){
							tokenList.add(stemmer.getCurrent());
						}else{
							tokenList.add(token);
						}
															
						
						
						if (stopWords.toString().contains(token)){
							tokenList.remove(token);
						}else if (token.contains("=") | token.contains(".")|token.contains("/")|token.contains(":")){
							token.replace("=","").replace(":", "");
							token.replace(".", "").replace("/", "");
								
						}else{
							tokenList.remove(token);
							
						}
							
					}
					//System.out.println(tokenList.toString() + ", ");
					//System.out.println(tempList.toString() + ", ");
					for (String stemmedToken: tokenList){
						fileWriter.append(stemmedToken + " ");
					}
					dataSet.put(id, tokenList);			            	
				    System.out.println(dataSet.entrySet().toString());
					fileWriter.append("\n");
						
					
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
