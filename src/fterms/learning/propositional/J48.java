package fterms.learning.propositional;

import java.util.List;

import weka.classifiers.Classifier;
import weka.core.Instances;
import fterms.FTKBase;
import fterms.FeatureTerm;
import fterms.Ontology;
import fterms.Path;
import fterms.learning.Hypothesis;
import fterms.learning.InductiveLearner;

/*
 * This class just wraps weka's J48 into a class usable in the AWorld project
 */

public class J48 extends InductiveLearner {
	NOOSToWeka.ConversionRecord record = null;
	
	public J48(NOOSToWeka.ConversionRecord r) {
		record = r;
	}
	
	public Hypothesis generateHypothesis(List<FeatureTerm> examples,Path dp,Path sp,Ontology o,FTKBase dm) throws Exception {
		WekaJ48Hypothesis h  = null;
		
		Instances wekaTrainingSet = NOOSToWeka.translateSubset(examples,record.allCases,record.allWekaCases);
		
		Classifier c = new weka.classifiers.trees.J48();
		try {					
			c.buildClassifier(wekaTrainingSet);
//			System.out.println(c.toString());
			h = new WekaJ48Hypothesis(c, record,true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return h;
	}
}
