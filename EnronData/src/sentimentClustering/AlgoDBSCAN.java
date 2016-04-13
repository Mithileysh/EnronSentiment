/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sentimentClustering;

/**
 *
 * @author jc166795
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



/* This file is copyright (c) 2008-2015 Philippe Fournier-Viger
* 
* This file is part of the SPMF DATA MINING SOFTWARE
* (http://www.philippe-fournier-viger.com/spmf).
* 
* SPMF is free software: you can redistribute it and/or modify it under the
* terms of the GNU General Public License as published by the Free Software
* Foundation, either version 3 of the License, or (at your option) any later
* version.
* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU General Public License for more details.
* You should have received a copy of the GNU General Public License along with
* SPMF. If not, see <http://www.gnu.org/licenses/>.
*/
/**
 * An implementation of the DBSCAN algorithm (Ester et al., 1996). 
 * Note that original algorithm suggested using a R*-tree to index points 
 * to avoid having a O(n^2) complexity, but we instead used a KD-Tree.
 * The DBScan algorithm was originally published in:
 * <br/><br/>
 * 
 * Ester, Martin; Kriegel, Hans-Peter; Sander, J锟絩g; Xu, Xiaowei (1996). Simoudis, Evangelos; 
 * Han, Jiawei; Fayyad, Usama M., eds. A density-based algorithm for discovering clusters in 
 * large spatial databases with noise. Proceedings of the Second International Conference on Knowledge
 *  Discovery and Data Mining (KDD-96). AAAI Press. pp. 226锟�231.
 * 
 * @author Philippe Fournier-Viger
 */

public class AlgoDBSCAN {

	// The list of clusters generated
	protected List<Cluster> clusters = null;

	// For statistics
	protected long startTimestamp; // the start time of the latest execution
	protected long endTimestamp;  // the end time of the latest execution
	long numberOfNoiseEmails; // the number of iterations that was performed
	
	/* The distance function to be used for clustering */
	DistanceBOW distanceFunction = new DistanceBOW(); 
	
	/* This KD-Tree is used to index the data points for fast access to points in the epsilon radius*/
	//KDTree kdtree;
        List<EmailDBS> emailsDBSDataset;

	/**
	 * Default constructor
	 */
	public AlgoDBSCAN() { 
		emailsDBSDataset = new ArrayList<>();
	}
	
	/**
	 * Run the DBSCAN algorithm
	 * @param inputFile an input file path containing a list of vectors of double values
	 * @param minPts  the minimum number of points (see DBScan article)
	 * @param epsilon  the epsilon distance (see DBScan article)
	 * @param seaparator  the string that is used to separate double values on each line of the input file (default: single space)
	 * @return a list of clusters (some of them may be empty)
	 * @throws IOException exception if an error while writing the file occurs
	 */
	public List<Cluster> runAlgorithm(List<Email> emails, int minPts, double epsilon) throws NumberFormatException, IOException {
		//this.emails.addAll(emails);
                for(Email email : emails){
                    this.emailsDBSDataset.add(new EmailDBS(email));
                }
		clusters = new ArrayList<Cluster>();
		
		// For each email in the dataset
		//for(Email email: this.emails) {
                for(EmailDBS emailDBS : this.emailsDBSDataset){
			// if the node is already visited, we skip it
			//EmailDBS emailDBS = (EmailDBS) email;
                        //EmailDBS emailDBS = new EmailDBS(email);
			if(emailDBS.visited == true) {
				continue;
			}
			
			// mark the email as visited
			emailDBS.visited = true;
			
			// find the neighboors of this email
			//List<Email> neighboors = kdtree.pointsWithinRadiusOf(emailDBS, epsilon);
                        List<Email> neighboors = regionQuery(emailDBS, epsilon);
			// if it is not noise
			if(neighboors.size() >= minPts ) { 
				
				// create a new cluster
				Cluster cluster = new Cluster();
				clusters.add(cluster);
				
				// transitively add all emails that can be reached
				expandCluster(emailDBS, neighboors, cluster, epsilon, minPts);
			}else {
				// it is noise
				numberOfNoiseEmails++;
			}
			
		}
		
		//kdtree = null;
		
		
		
		// record end time
		endTimestamp =  System.currentTimeMillis();
		
		// return the clusters
		return clusters;
	}

	/**
	 * The DBScan expandCluster() method
	 * @param currentemail the current email
	 * @param neighboors the neighboors of the current email
	 * @param cluster the current cluster
	 * @param epsilon the epsilon parameter
	 * @param minPts the minPts parameter
	 */
	private void expandCluster(EmailDBS currentemail,
			List<Email> neighboors, Cluster cluster, double epsilon, int minPts) {	
		// add the current email to the cluster
		cluster.addVector(currentemail);
		
		// for each neighboor
		for(Email newemail: neighboors) {
			EmailDBS newemailDBS = (EmailDBS) newemail;
                        //EmailDBS newemailDBS = new EmailDBS(newemail);
			
			// if this email has not been visited yet
			if(newemailDBS.visited == false) {
					
				// mark the email as visited
				newemailDBS.visited = true;
				                               
				// find the neighboors of this email
				//List<Email> newNeighboors = kdtree.pointsWithinRadiusOf(newemailDBS, epsilon);
                                List<Email> newNeighboors = regionQuery(newemailDBS, epsilon);
				
				// if this email is not noise
				if(newNeighboors.size() >= minPts) { 
					expandCluster(newemailDBS, newNeighboors, cluster, epsilon, minPts);
				}else {
					// it is noise
					numberOfNoiseEmails++;
				}
			}
                        if(!cluster.contains(newemailDBS)) {
                            cluster.addVector(newemailDBS);
                        }
		}

		
	}
        
        public List<Email> regionQuery(EmailDBS emailDBS, double epsilon){
            List<Email> neighbours = new ArrayList<>();
            
            for(Email email : this.emailsDBSDataset){
                double distance = distanceFunction.distanceEmails(emailDBS, email);
                
                if(distance <= epsilon){
                    neighbours.add(email);
                }
            }
            return neighbours;
        }

	/**
	 * Save the clusters to an output file
	 * @param output the output file path
	 * @throws IOException exception if there is some writing error.
	 */
	public void saveToFile(String output) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(output));
		// for each cluster
		for(int i=0; i< clusters.size(); i++){
			// if the cluster is not empty
			if(clusters.get(i).getVectors().size() >= 1){
				// write the cluster
				writer.write(clusters.get(i).toString());
				// if not the last cluster, add a line return
				if(i < clusters.size()-1){
					writer.newLine();
				}
			}
		}
		// close the file
		writer.close();
	}
	
	/**
	 * Print statistics of the latest execution to System.out.
	 */
	public void printStatistics() {
		System.out.println("========== DBSCAN - STATS ============");
		System.out.println(" Total time ~: " + (endTimestamp - startTimestamp)
				+ " ms");
		
		System.out.println(" Number of noise emails: " + numberOfNoiseEmails);
		System.out.println("=====================================");
	}

}
