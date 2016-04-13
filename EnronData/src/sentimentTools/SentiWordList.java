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
	
	public static void main (String[] args) throws IOException{
		SentiWordList swl = new SentiWordList();
		ArrayList<String> list = new ArrayList<String>();
		ArrayList<String> bodylist = new ArrayList<String>();
		
		for (String wl: swl.createList("negative-words.txt")){
			list.add(wl);
		}
		String words = "a b c d bleak. e f c ds , bleed";
		String[] newword = words.split(" ");
		//String[] words = {"a","b","c","d","bleak", "e", "f", "g", "bleed"};
		
		
		for (String word: newword){
			System.out.println(word +", ");
			System.out.println(recursion(0,word, list));
		}
		System.out.println(bodylist.toString());
		
	}
    
}



