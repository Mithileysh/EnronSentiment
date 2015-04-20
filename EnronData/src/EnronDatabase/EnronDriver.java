/*
 * Version 1.5.3
 * Copyright © , The Apache Software Foundation
 * License and Disclaimer.  The ASF licenses this documentation to you under the Apache License, 
 * Version 2.0 (the "License"); you may not use this documentation except in compliance with the License. 
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, this documentation and its contents are 
 * distributed under the License on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the License for the specific language governing permissions and limitations 
 * under the License.
 */
package EnronDatabase;


import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;


public class EnronDriver {
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
	
	private static final String COMMA_DELIMITER = ",";
	private static final String NEW_LINE_SEPARATOR = "\n";
	//CSV file header
	private static final String FILE_HEADER = "id,,lastName,gender,age";

	
	public static void main(String[] args) throws IOException {
		Connection myConn = null;
		Statement myStmt = null;
		
		
		try{
			
			Class.forName(DB_DRIVER).newInstance();
		
		    // Get a connection to database
			myConn = DriverManager.getConnection(URL,USER,PASSWORD);
			System.out.println("connected");
			
			
			myStmt = myConn.createStatement();
			String sql;
			sql = "SELECT DISTINCT mid, date, body FROM enron.message WHERE subject NOT LIKE 'Re%'OR subject NOT LIKE 'FW%' LIMIT 100 ";
			ResultSet rs = myStmt.executeQuery(sql);
			count = 0;
			
			
			while (rs.next()){
				InputStream tokenmodelIn = new FileInputStream("en-token.bin");
				InputStream posmodelIn = new FileInputStream("en-pos-maxent.bin");
				String pathToSWN = "SentiWordNet_3.0.0_20130122.txt";
				SentiWordNet sentiwordnet = new SentiWordNet(pathToSWN);
				
				int attrCount = 0;
				id = rs.getInt("mid");
				date = rs.getString("date");
				body = rs.getString("body");
				
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
					  
					  
					  for (String token : tokens){
						  
						  
						  
						  tokenList= new ArrayList<String>();
						  tokenList.add(token);
						  
						  
						  List<String> tags = tagger.tag(tokenList);
						  for(String tag: tags){
							  
							  if ( tag.contains("VB")){
								  
								  tag = "v";
								  if (sentiwordnet.extract(token, "v") == 0.0){
									  tokenList.remove(token);
								  }else{
									  System.out.print(token + "/ "+ tag + ": " + sentiwordnet.extract(token, "v") + ", ");
							      }
							  }
						          
							  else if (tag.contains("NN")){
								  tag = "n";
								  if (sentiwordnet.extract(token, "n") == 0.0){
									  tokenList.remove(token);
								  }else{
									  System.out.print(token + "/ "+ tag + ": " + sentiwordnet.extract(token, "n") +", ");
								  }
								  
								  
							  }
							  else if (tag.contains("JJ")){
								  tag = "a";
								  if (sentiwordnet.extract(token, "a") == 0.0){
									  tokenList.remove(token);
								  }else{
									  System.out.print(token + "/ "+ tag + ": " + sentiwordnet.extract(token, "a") +", ");
								  }
								  
								  
							  }
							  else if (tag.contains("RB")){
								  tag = "r";
								  if (sentiwordnet.extract(token, "r") == 0.0){
									  tokenList.remove(token);
								  }else{
									  System.out.print(token + "/ "+ tag + ": " + sentiwordnet.extract(token, "r") +", ");
								  }
								  
								  
							  }
							  else{
								  tokenList.remove(token);
								  
							  }
							 
							  
						  }
						  
					  }
						  
					  System.out.println("");
				  
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
				
				count ++;
				
				//System.out.print(tokenList.toString()  );
				
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
