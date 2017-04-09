package sentimentTools;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;


public class AttGenerator {

	public static void main(String[] args) {
		FastVector atts;
		FastVector attVals;
		Instances data;
		Instances dataRel;
		double[] vals;
		double[] valsRel;
		int i;
		
		atts = new FastVector();
		atts.addElement(new Attribute ("id"));
		
		attVals = new FastVector();
		for (i = 0; i < 4; i++)
			attVals.addElement("val" + i);
		atts.addElement(new Attribute("class", attVals));
		

	}

}
