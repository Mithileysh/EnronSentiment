package sentimentTools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MinMaxNorm{
	
	public static void main(String[] args){
		List<Double>  fruits= new ArrayList<Double>();

		fruits.add(4.63);
		fruits.add(-5.3323);
		fruits.add(2.5234);
		fruits.add(-9.1234);
		fruits.add(3.2434);
		fruits.add(4.63);
		fruits.add(-5.3323);
		fruits.add(-2.5234);
		fruits.add(-9.1234);
		fruits.add(-3.2434);

		//Sorting
		Collections.sort(fruits, new Comparator<Double>() {
			
		        public int compare(Double o1, Double o2)
		        {

		            return  o1.compareTo(o2);
		        }

				
				
		    });
		double value = -4.23;
		double norValue = -(value - fruits.get(0)) / (fruits.get(fruits.size()-1) - fruits.get(0));
		
		System.out.println(norValue);
	}
	

}
