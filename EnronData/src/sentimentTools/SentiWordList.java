package sentimentTools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SentiWordList{
	private ArrayList<String> wordList;
	
	public ArrayList<String> createList(String filePath) throws IOException  {
		
		wordList = new ArrayList<String>();
		BufferedReader wordListCSV = null;
		
		try {
	        wordListCSV = new BufferedReader(new FileReader(filePath));
	        int lineNumber = 0;

	        String line;
	        while ((line = wordListCSV.readLine()) != null) {
	            lineNumber++;

	            // If it's a comment, skip this line.
	            if (!line.trim().contains(";")) {
	                
	            	wordList.add(line.replace(" ", ""));
	            	
	            }    
	        }
	        wordList.remove(0);
	        
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        if (wordListCSV != null) {
	            wordListCSV.close();
	        }
	    }
		return wordList;
	}
	/*
	public static void main (String[] args) throws IOException{
		SentiWordList swl = new SentiWordList();
		
		System.out.println(swl.createList("negative-words.txt").toString());
		
	}
    */
}



