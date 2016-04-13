package sentimentTools;

import weka.clusterers.ClusterEvaluation;
import weka.clusterers.SimpleKMeans;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class simpleKMeans {
	// initialize variables
	static ClusterEvaluation eval;
	Instances data;
	String[] options;
	SimpleKMeans skm;
	DataSource source;
	Instances newData;
	
	// create cluster method
	public String cluster (String filename)throws Exception{
		source = new DataSource(filename);
		data = new Instances(source.getDataSet());
		
		String[] removeOpt = new String[2];
		removeOpt[0] = "-R";                                    
		removeOpt[1] = "1";
		Remove remove = new Remove();                         // new instance of filter
		remove.setOptions(removeOpt);                           // set options
		remove.setInputFormat(data);                          // inform filter about dataset **AFTER** setting options
		newData = Filter.useFilter(data, remove);
		
		//System.out.println("\n--> normal");
		options = new String[8];
		options[0] = "-t";
		options[1] = filename;
		options[2] = "-N";
		options[3] = "3";
		options[4] = "-S";
		options[5] = "5000000";
		options[6] = "-I";
		options[7] = "100";
		
		return ClusterEvaluation.evaluateClusterer(new SimpleKMeans(), options);
		
	}
	//get cluster assignment for each instance
	public int[] classNum () throws Exception{	
	
		System.out.println("\n--> manual");
		skm = new SimpleKMeans();
		
		
		skm.setDontReplaceMissingValues(true);
		skm.setPreserveInstancesOrder(true);
		skm.setNumClusters(3);
		skm.setSeed(2);	
		
		skm.buildClusterer(newData);
		
		eval = new ClusterEvaluation();
		eval.setClusterer(skm);
		eval.evaluateClusterer(new Instances(newData));
		
		return skm.getAssignments();
	}	
	//get cluster numbers
	public static int numClusters(){
		
		return eval.getNumClusters();
	}
	//get sum of squared errors
	public double sse(){
		
		return skm.getSquaredError();

	}

	/*
	public static void main (String[] args) throws Exception{
		
		if (args.length == 1){
			System.out.println("usage: " + simpleKMeans.class.getName() + " <csv-file>");
			System.exit(0);
		}
		
		simpleKMeans skm =  new simpleKMeans();
		skm.cluster("enronemail_kmeans_features_2h_normalized.csv");
		for(int classNum: skm.classNum()){
			System.out.println("#" + classNum);
		}
		
		System.out.println(eval.clusterResultsToString());
		System.out.println("# SSE: " + skm.sse());
		System.out.println("# of clusters: " + numClusters());
		for(int classNum: skm.classNum()){
			System.out.println("#" + classNum);
		}
	}
	*/

}
