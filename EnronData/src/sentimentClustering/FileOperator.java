/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sentimentClustering;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author jc166795
 */
public class FileOperator {
    
    /**
     * Read email dataset .txt file
     */
    public List<Email> readTxtFile(String fileName){
        File file = new File(fileName);
        BufferedReader reader = null;
        List<Email> emails = new ArrayList<>();
        try {
        reader = new BufferedReader(new FileReader(file));
        String line = "";
        
        String regEx1 = "\\<.*?\\>";
        String regEx2 = "\\[.*?\\]";
        Pattern p = Pattern.compile(regEx1);
        Pattern p2 = Pattern.compile(regEx2);
        //read each line       
        while((line = reader.readLine()) != null) {
            Email email = new Email();
            String outputStr = new String("");
            Matcher m = p.matcher(line);         
            //System.out.println("first half: " + firsthalfStr);
            while(m.find()) {
                String lineStr = m.group();
                lineStr = lineStr.replaceAll("\\<|\\>", "");
                
                //find position of [
                int position = lineStr.indexOf("[");
                
                //select sub-string before [ that are id, topic, time
                String firsthalfStr = lineStr.substring(0, position-2);
                                  
                //process first half sub-string: id, topic, time
                String[] fStr = firsthalfStr.split(",");
                if(fStr.length > 0) {
                    //id
                    String id = fStr[0].trim();
                    email.setId(id);
                    outputStr = outputStr + id + "; ";
                    //topic
                    String topic = fStr[1].trim();
                    email.setTopic(topic);
                    outputStr = outputStr + topic + "; ";
                    //time
                    String time = fStr[2].trim();
                    email.setTime(time);
                    outputStr = outputStr + time + "; ";
                }
                
                //find sentiment keywords : between [ ]
                Matcher m2 = p2.matcher(lineStr);
                while(m2.find()){
                    String sentiment = m2.group().replaceAll("\\[|\\]", "");;
                    //System.out.println("sentiment: " + sentiment);
                    List<String> sentimentKeywords = new ArrayList();
                    List<String> sentiments = new ArrayList<>();
                    if(!sentiment.isEmpty()) {
                        String[] sentStr = sentiment.split(",");
                        //outputStr = outputStr + sentStr + "\n";
                                  
                        for(int i = 0; i < sentStr.length; i++){
                            sentimentKeywords.add(sentStr[i].trim());
                            //transfer sentiment key word into positive of negtive class label
                            if(sentStr[i].trim().startsWith("-")){
                                sentiments.add("-1");
                            } else {
                                sentiments.add("+1");
                            }
                        }                   
                    }
                    email.setSentimentKeywords(sentimentKeywords);
                    email.setSentiments(sentiments);
                    outputStr += sentiments.toString();
                }
                             
                
            }
            //ignore email without any sentiment value
            if(!email.getSentiments().isEmpty()){
                emails.add(email);
                System.out.println("Add line: " + outputStr);
            }
        }
               
        } catch (IOException e){
            System.out.println("Read File error!!!");
            e.printStackTrace();
        }
        
        return emails;
    }
}
