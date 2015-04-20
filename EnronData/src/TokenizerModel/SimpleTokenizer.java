/*
	 * Licensed to the Apache Software Foundation (ASF) under one or more
	 * contributor license agreements.  See the NOTICE file distributed with
	 * this work for additional information regarding copyright ownership.
	 * The ASF licenses this file to You under the Apache License, Version 2.0
	 * (the "License"); you may not use this file except in compliance with
	 * the License. You may obtain a copy of the License at
	 *
	 *     http://www.apache.org/licenses/LICENSE-2.0
	 *
	 * Unless required by applicable law or agreed to in writing, software
	 * distributed under the License is distributed on an "AS IS" BASIS,
	 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	 * See the License for the specific language governing permissions and
	 * limitations under the License.
	 */

package TokenizerModel;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import opennlp.uima.tokenize.AbstractTokenizer;


public class SimpleTokenizer {
	

	public static void main (String[] args) throws FileNotFoundException{
		InputStream modelIn = new FileInputStream("en-token.bin");

		try {
		  TokenizerModel model = new TokenizerModel(modelIn);
		  TokenizerME tokenizer = new TokenizerME(model);
		  String tokens[] = tokenizer.tokenize("An input sample sentence.");
		  Span tokenSpans[] = tokenizer.tokenizePos("An input sample sentence.");
		  for (String token : tokens){
			  System.out.println(token + ", ");
		  }
		  
		}
		catch (IOException e) {
		  e.printStackTrace();
		}
		finally {
		  if (modelIn != null) {
		    try {
		      modelIn.close();
		    }
		    catch (IOException e) {
		    }
		  }
		}
		
	}
	
	
}
	
	




	