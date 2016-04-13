/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sentimentClustering;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jc166795
 */
public class Email {
    
    String id;
    String sender; //sender id
    String receiver; //receiver id
    String topic;
    String time;
    
    List<String> sentimentKeywords;
    List<String> sentiments;
    
    public Email() {
        id = new String();
        sender = new String();
        receiver = new String();
        topic = new String();
        time = new String();
        sentimentKeywords = new ArrayList<>();
        sentiments = new ArrayList<>();
    }
    
    public Email(Email e){
        id = new String();
        id = e.getId();
        
        sender = new String();
        sender = e.getSender();
        
        receiver = new String();
        receiver = e.getReceiver();
        
        topic = new String();
        topic = e.getTopic();
        
        time = new String();
        time = e.getTime();
        
        sentimentKeywords = new ArrayList<>();
        sentimentKeywords.addAll(e.getSentimentKeywords());
        
        sentiments = new ArrayList<>();
        if (e.getSentiments().size() > 0)
        sentiments.addAll(e.getSentiments());
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public List<String> getSentiments() {
        return sentiments;
    }

    public void setSentiments(List<String> sentiments) {
        this.sentiments = sentiments;
    }

    public List<String> getSentimentKeywords() {
        return sentimentKeywords;
    }

    public void setSentimentKeywords(List<String> sentimentKeywords) {
        this.sentimentKeywords = sentimentKeywords;
    }
    
    @Override
    public boolean equals(Object object2){
        if(object2 instanceof Email) {
            Email email2 = (Email) object2;
        return this.id.equals(email2.getId());
    }
        return super.equals(object2);
    }
    
}
