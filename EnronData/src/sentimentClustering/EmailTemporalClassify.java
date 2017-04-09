/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sentimentClustering;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

/**
 * Classify original email dataset into different temporal group
 * week, day of week
 * @author jc166795
 */
public class EmailTemporalClassify {
    //<week of year, <day of week, email>>
    Map<Integer,Map<Integer,List<Email>>> emailGroups = new HashMap<>();
    
    List<Email> emailsDataset = new ArrayList<>();
    
    public double timestamp;
    
    public Calendar calendar;
    public String formats = "yyyy-MM-dd HH:mm:ss";
    
    
    public EmailTemporalClassify(){
        calendar = new GregorianCalendar(TimeZone.getDefault());
    }
    
    public EmailTemporalClassify(List<Email> emails){
        this.emailsDataset.addAll(emails);
        calendar = new GregorianCalendar(TimeZone.getDefault());
    }
    
    public void process(){
        //for each email
        for(Email email : this.emailsDataset){
            String time = email.getTime();
            double timestamp = Double.valueOf(time);
            //get Calendar object
            calendar.setTimeInMillis((new Double(timestamp).longValue()));
            
            //get week of year
            int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
            //get the day of week
            //in Calendar, first day of week is Sunday
            //day 1 = Sunday, day 7 = Saturday
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            
            System.out.println("id: " + email.getId() + " week of year: " + 
                    weekOfYear + " day of week: " + dayOfWeek);
            
            //if there is no record of the weekOfYear
            //then create new subgroup for the week of year
            if(!(emailGroups.containsKey(weekOfYear))) {
                List<Email> subgroupEmails = new ArrayList<>();
                Map<Integer,List<Email>> subgroup = new HashMap<>();
                
                subgroupEmails.add(email);
                //subgroup of day of week
                subgroup.put(dayOfWeek, subgroupEmails);
                
                emailGroups.put(weekOfYear, subgroup);
            } else {//
                Map<Integer,List<Email>> subgroup = emailGroups.get(weekOfYear);
                
                if(!(subgroup.containsKey(dayOfWeek))) {
                    List<Email> subgroupEmails = new ArrayList<>();
                    subgroupEmails.add(email);
                    
                    subgroup.put(dayOfWeek, subgroupEmails);
                } else {
                    List<Email> subgroupEmails = subgroup.get(dayOfWeek);
                    subgroupEmails.add(email);
                    subgroup.put(dayOfWeek, subgroupEmails);
                }
                emailGroups.put(weekOfYear, subgroup);
            }
        }
    }
    
    public void printSummary(){
        System.out.println(" number week_of_year: " + emailGroups.size());
        
        int count = 0;
        for(Entry<Integer, Map<Integer, List<Email>>> entry : emailGroups.entrySet()){
            int week = entry.getKey();
            System.out.println("for week of year: " + week + " number day of week is " + entry.getValue().size());
            
            for(Entry<Integer,List<Email>> entry1 : entry.getValue().entrySet()){
                int day = entry1.getKey();
                count+=entry1.getValue().size();
                System.out.println("for day " + day + " emails: " + entry1.getValue().size());
            }
            
        }
        System.out.println("Total number of emails is " + count);
    }
}
