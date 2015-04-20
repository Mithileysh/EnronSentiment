package EnronDatabase;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class DataExtraction {

	public static void main(String[] args) {
		try {
		      ZipFile rawFolder = new ZipFile("kenneth.zip");
		      Enumeration rawFile = rawFolder.entries();

		      BufferedReader input = new BufferedReader(new InputStreamReader(
		          System.in));
		      while (rawFile.hasMoreElements()) {
		        ZipEntry rawData = (ZipEntry) rawFile.nextElement();
		        System.out.println("Read " + rawData.getName() + "?");
		        String inputLine = input.readLine();
		        if (inputLine.equalsIgnoreCase("yes")) {
		          long size = rawData.getSize();
		          if (size > 0) {
		            System.out.println("Length is " + size);
		            BufferedReader exData = new BufferedReader(
		                new InputStreamReader(rawFolder.getInputStream(rawData)));
		            String line;
		            while ((line = exData.readLine()) != null) {
		            	
		            	System.out.println(line);
		            	/*
		            	ArrayList<String> date= new ArrayList<String>();
		            	date.add(line);
		            	System.out.println(date.toString());
		            	*/
		            	try{
		            		
		            	
		            	BufferedWriter txtFile = new BufferedWriter ( new FileWriter("kenneth.txt"));
		            	txtFile.write(line + "\n");
		            	txtFile.close();
		            	}
		            	catch (IOException ex){
		            		ex.printStackTrace();
		            	}
		            }
		            exData.close();
		          }
		          
		        }
		      }
		    } catch (IOException e) {
		      e.printStackTrace();
		    }
		  
		
		
		// TODO Auto-generated method stub

	}

}
