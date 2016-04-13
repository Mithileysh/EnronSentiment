package sentimentAnalysis;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
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
import org.apache.lucene.search.similarities.*;
import org.tartarus.snowball.ext.EnglishStemmer;

import sentimentTools.SentiWordList;
import sentimentTools.tfidfSimilarity;

public class SentiTrajectory {
	
	//Create driver and url
	static final String DB_DRIVER = "com.mysql.jdbc.Driver";
	static final String URL = "jdbc:mysql://localhost:3306/enron";	
	
	//Database credentials
	static final String USER = "root";
	static final String PASSWORD = "root";
	
	//variables for retrieving data from database
	static int id;
	static int eid;
	static String address;
	static String subject;
	static String date;
	static Date datef;
	static long datemilli;
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
	static ArrayList<String> tempLexiconPOS;
	static ArrayList<String> tempLexiconNEG;
	static ArrayList<String> tempDictionary;
	
	static tfidfSimilarity idf;
	static TFIDFSimilarity tfidf;
	
	static int size;
	//extract matching word
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
	public static double tfCalculator(ArrayList<String> totalterms, String termToCheck) {
        double count = 0;  //to count the overall occurrence of the term termToCheck
        for (String s : totalterms) {
            if (s.equalsIgnoreCase(termToCheck)) {
                count++;
            }
        }
        return count / totalterms.size();
    }

	public static void main(String[] args) throws IOException {
		Connection myConn = null;
		Statement myStmt = null;
		Statement stmt = null;
		
		
		Map<String, Double> sentiDictionary = new HashMap<String,Double>();
		
		//create empty arraylist for storing words from wordlist
		tempLexiconPOS = new ArrayList<String>();
		tempLexiconNEG = new ArrayList<String>();
		
		try{
			
			Class.forName(DB_DRIVER).newInstance();
		
		    // Get a connection to database
			myConn = DriverManager.getConnection(URL,USER,PASSWORD);
			System.out.println("connected");
			
			// Query from database
			myStmt = myConn.createStatement();
			String sql;
			sql = "SELECT DISTINCT mid,subject, date, body FROM enron.message WHERE YEAR(date) = 2001 AND MONTH(date) BETWEEN 06 AND 07";
			//sql = "SELECT DISTINCT mid, date, body FROM enron.message WHERE subject LIKE '%weekly update%' LIMIT 300";
			//sql = "SELECT DISTINCT mid, date, body FROM enron.message WHERE subject LIKE '%enron kids%' LIMIT 300";
			//sql = "SELECT DISTINCT mid, sender, date, body FROM enron.message WHERE subject LIKE '%training%' LIMIT 300";
			
			//sql = "SELECT DISTINCT mid, date, body FROM enron.message WHERE sender = 'kevin.hyatt@enron.com' LIMIT 200 ";
			//sql = "SELECT DISTINCT mid, date, body FROM enron.message WHERE sender = 'lorna.brennan@enron.com' LIMIT 200 ";
			//sql = "SELECT DISTINCT mid, date, body FROM enron.message WHERE sender = 'christi.nicolay@enron.com' LIMIT 200 ";
			
			ResultSet rs = myStmt.executeQuery(sql);
			
			
			//generate output file directory
			
			//String outputFile = "data/enronemail_kmeans_set1_features_tm.csv";
			//String outputFile = "data/enronemail_kmeans_set1_features_normalized.csv";
			//String outputFile = "data/enronemail_kmeans_set2_features.csv";
			//String outputFile = "data/enronemail_kmeans_set3_features.csv";
			
			String outputFile = "data/enronemail_owl_ts_2001_06_07_tfidf.txt";
			
			//String outputFile = "data/enronemail_owl_ts_enronkidsprogram.txt";
			//String outputFile = "data/enronemail_owl_ts_weeklyupdate.txt";
			//String outputFile = "data/enronemail_owl_ts_training.txt";
			
			FileWriter fileWriter = null;	
			fileWriter = new FileWriter (outputFile);
			
			//fileWriter.append("[id][date]");
			//fileWriter.append("2, lunch, ");
			
			
			//stopwords normalization
			StopAnalyzer stopAnalyzer = new StopAnalyzer();
			CharArraySet stopWords= stopAnalyzer.ENGLISH_STOP_WORDS_SET;
			
			//
			swl = new SentiWordList();
			
			for (String word: swl.createList(POSLIST)){
				tempLexiconPOS.add(word);
			}
			
			for (String words: swl.createList(NEGLIST)){
				tempLexiconNEG.add(words);
			}
		
			/*
			int attrCount = 0;
			for (String term:tempLexicon){
				attrCount++;
				fileWriter.append("attr_" + attrCount + ",");
			}
			fileWriter.append("\n");
			*/
			while (rs.next()){
				idf = new tfidfSimilarity();
				tempDictionary = new ArrayList<String>();
				InputStream tokenmodelIn = new FileInputStream("en-token.bin");
				InputStream posmodelIn = new FileInputStream("en-pos-maxent.bin");
				
				EnglishStemmer stemmer = new EnglishStemmer();
				
		
				id = rs.getInt("mid");
				date = rs.getString("date");
				subject = rs.getString("subject").toLowerCase();
				body = rs.getString("body");
				//address = rs.getString("sender");
				
				
				String bodyStr = body.toLowerCase();
				
				
				
				bodyList = new ArrayList<String>();
		        
				bodyList.add(bodyStr);
		        
		        //System.out.println(bodyList);
		        
		        try {
		        	
					if (subject != "" && subject.contains("fw:") == false && subject != "re:"){	
					TokenizerModel tokenModel = new TokenizerModel(tokenmodelIn);
					TokenizerME tokenizer = new TokenizerME(tokenModel);
					  		  
					String[] tokens = tokenizer.tokenize(bodyList.get(0));
										
					POSModel posModel = new POSModel(posmodelIn);
					POSTaggerME tagger = new POSTaggerME(posModel);
					
					System.out.print(id + ", ");	
					
					
					fileWriter.append("<" + id + ", ");
					/*
					stmt = myConn.createStatement();
					
					String newSql;
					newSql = "SELECT employeelist.eid FROM enron.employeelist, enron.message Where message.sender = employeelist.Email_id OR message.sender = employeelist.Email2 OR message.sender = employeelist.Email3 AND message.sender = '" + address + "'LIMIT 1"; 
					
					ResultSet newRs = myStmt.executeQuery(newSql);
					
					if (newRs.wasNull()){
						eid = 0;
					}
					eid = newRs.getInt("employeelist.eid");
					
					fileWriter.append(eid + ", " + address + ", " + "training, [");
					*/
					if (subject.contains("girl")|subject.contains("trip")|subject.contains("golf")|subject.contains("lunch") | subject.contains("daddy") |subject.contains("i m") |subject.contains("gift") | subject.contains("us") | subject.contains("private") | subject.contains("vacation") | subject.contains("birthday")| subject.contains("dinner") | subject.contains("?")){
						fileWriter.append("Private Issue, ");
					}else if (subject.contains("arrival")|subject.contains("hi")|subject.contains("good") | subject.contains("congrat") | subject.contains("i am here") | subject.contains("farewell") ){
						fileWriter.append("Daily Greeting, ");
					}else if (subject.contains("training") | subject.contains("ethink")|subject.contains("lesson") | subject.contains("class")){
						fileWriter.append("Employee Training, ");
					}else if (subject.contains("follow")|subject.contains("discussion")|subject.contains("draft")|subject.contains("update")|subject.contains("language")|subject.contains("plan")|subject.contains("weekly") |subject.contains("presentation") |subject.contains("urgent") |subject.contains("worksheet") |subject.contains("notice") | subject.contains("notice") | subject.contains("update") |  subject.contains("review") |subject.contains("decision") |subject.contains("schedule") |subject.contains("meeting") | subject.contains("follow up") ||subject.contains("visit") | subject.contains("assignment") | subject.contains("work") |subject.contains("update")){
						fileWriter.append("General Operation, ");
					}else if (subject.contains("minute")|subject.contains("petition")|subject.contains("note")|subject.contains("document") |subject.contains("performance") |subject.contains("<<") |subject.contains("accomplishment") | subject.contains("approval") | subject.contains("section") | subject.contains("summary") | subject.contains("draft") |subject.contains("report") ){
						fileWriter.append("Business Document, ");
					}else if (subject.contains("announce")|subject.contains("goal")|subject.contains("enron") |subject.contains("risk") |subject.contains("analytics") |subject.contains("organization") | subject.contains("action") | subject.contains("goal") | subject.contains("executive") | subject.contains("summary") | subject.contains("agreement") | subject.contains("confidential") | subject.contains("proposal") | subject.contains("financial") | subject.contains("data") |subject.contains("analysis")){
						fileWriter.append("Company Strategy, ");
					}else if (subject.contains("pseg")|subject.contains("bank")|subject.contains("negotiation") |subject.contains("trade")|subject.contains("stock") |subject.contains("contract") | subject.contains("gas") |subject.contains("bank") | subject.contains("cash") | subject.contains("stock") | subject.contains("market") | subject.contains("loan")){
						fileWriter.append("Business Investment, ");
					}else if (subject.contains("enron kids") | subject.contains("kids") ){
						fileWriter.append("Enron Kids Program, ");
					}else if (subject.contains("project") |subject.contains("panda")|subject.contains("gas")| subject.contains("co op")|subject.contains("oil") |subject.contains("energy") |subject.contains("california") | subject.contains("sun devil") | subject.contains("alaskan") | subject.contains("alaska")){
						fileWriter.append("Company Project, ");
					}else if (subject.contains("publish")|subject.contains("press") | subject.contains("bulletin") | subject.contains("release") | subject.contains("media") | subject.contains("conference") | subject.contains("news")){
						fileWriter.append("News/Press/Media,  ");
					}else if (subject.contains("space")|subject.contains("car")|subject.contains("storage")|subject.contains("call") | subject.contains("purchase") |subject.contains("delivery") | subject.contains("issue") | subject.contains("office") | subject.contains("power") | subject.contains("electric") | subject.contains("product") |subject.contains("letter") |subject.contains("credit") | subject.contains("stage") | subject.contains("shipping") | subject.contains("respond") | subject.contains("response") | subject.contains("password") | subject.contains("cost") | subject.contains("contact")){
						fileWriter.append("Logistic Issue, ");
					}else if (subject.contains("xms")|subject.contains("cpu")|subject.contains("disk")|subject.contains("outlook")|subject.contains("alert") |subject.contains("security") |subject.contains("interconnect") | subject.contains("esource") | subject.contains("monitor") | subject.contains("virus") | subject.contains("computer") | subject.contains("wrong") | subject.contains("online") | subject.contains("request")){
						fileWriter.append("Technical Issue, ");
					}else if (subject.contains("confirmation") | subject.contains("promotion") | subject.contains("resume") | subject.contains("analyst") | subject.contains("interview") | subject.contains("leave") | subject.contains("employ") | subject.contains("job")){
						fileWriter.append("Employment Arrangement, ");
					}else if (subject.contains("win")|subject.contains("msn")|subject.contains("game")|subject.startsWith("the")|subject.contains("!") | subject.contains(".com") | subject.contains("welcome") | subject.contains("friend")| subject.contains("$") | subject.contains("shop")){
						fileWriter.append("Commercial/Advertising, ");
					}
					else{
						fileWriter.append("Other, ");
					}
					
					DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
					df.setTimeZone(TimeZone.getTimeZone("UTC"));
					try{
						datef = df.parse(date);
						datemilli = datef.getTime();
						
					}catch (Exception dateex){
						dateex.printStackTrace();
					}
					
					fileWriter.append(datemilli + ", [");
					
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
							}	  
							
					}
			            for(String tempTerm : tokenList) {
			            	
							if (recursion(0,tempTerm, tempLexiconPOS) != null){
								
								
								fileWriter.append(recursion(0,tempTerm,tempLexiconPOS) + ":" + tfCalculator(bodyList,tempTerm)*idf.idfDictionary("enronemail_owl_ts_2001,idf.txt").get(tempTerm) + ", ");
							}else if (recursion(0,tempTerm, tempLexiconNEG) != null){
                                
								
								fileWriter.append(recursion(0,tempTerm, tempLexiconNEG) + ":" + -tfCalculator(bodyList,tempTerm)*idf.idfDictionary("enronemail_owl_ts_2001,idf.txt").get(tempTerm) + ", ");
								
							}
								
								
						}
			            
			            
		            	
					//System.out.println("");
					fileWriter.append("]>\n");
					}
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
