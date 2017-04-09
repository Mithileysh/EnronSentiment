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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.*;
import javax.swing.JOptionPane;
/**
 *
 * @author jc166795
 */
public class SentimentClustering {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws NumberFormatException, IOException {
        // TODO code application logic here
        List<Email> emails = new ArrayList<>();
        FileOperator fop = new FileOperator();
        String inputfileName = "enronemail_owl_ts_2001_01_05";
        String fileName = "enronemail_owl_ts_2001_01_05.txt";
        
        int minPts = 5;
        double epsilon = 0.2;
        
        String outfilePath = "results" + File.separator + "minpts " + minPts + 
                "_eps " + epsilon + "_" + inputfileName + "_" + 
                (System.currentTimeMillis()) + File.separator;      
        boolean ret = new File(outfilePath).mkdir();
        if (ret == false) {     
             System.out.println("Error: unable to mkdir" + new File(outfilePath).getAbsolutePath());
            return;
        }
        
        emails = fop.readTxtFile(fileName);
        
        System.out.println("number of emails: " + emails.size());
        
        EmailTemporalClassify emailClassify = new EmailTemporalClassify(emails);
        emailClassify.process();
        
        emailClassify.printSummary();
        /*      
        //for each temporal group, apply algoDBSCAN on its dataset
        int week;
        int day;
        String outfileName = "";
        List<Email> subgroupEmails = new ArrayList<>();
        for(Map.Entry<Integer, Map<Integer, List<Email>>> entry : emailClassify.emailGroups.entrySet()){
            week = entry.getKey();
            System.out.println("Week of year: " + week + " number day of week is " + entry.getValue().size());

            for(Map.Entry<Integer,List<Email>> entry1 : entry.getValue().entrySet()){
                day = entry1.getKey();
                System.out.println("for day " + day + " emails: " + entry1.getValue().size());
                
                subgroupEmails.clear();
                subgroupEmails.addAll(entry1.getValue());
                //Start Clustering - DBSCAN algorithm
                AlgoDBSCAN algoDBSCAN = new AlgoDBSCAN();
                algoDBSCAN.runAlgorithm(subgroupEmails, minPts, epsilon);
                outfileName = outfilePath + "week" + week + " day " + (day-1) + ".txt";
                algoDBSCAN.saveToFile(outfileName);
            }
            
        }
        */
    }
    
    
}
