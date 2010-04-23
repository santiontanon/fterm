package fterms.learning.activelearning;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import util.Pair;

import fterms.FTKBase;
import fterms.FeatureTerm;
import fterms.Ontology;
import fterms.Path;
import fterms.exceptions.FeatureTermException;

public class RandomActiveLearning extends ActiveLearning {
	
	Random r = new Random();
	
	public String toString() {
		return "RandomActiveLearning";
	}
	
	public List<Pair<FeatureTerm,Double>> examplesUtility(List<FeatureTerm> allTraining,
			List<FeatureTerm> examples,List<FeatureTerm> differentSolutions, Path dp, Path sp, Ontology o, FTKBase dm) throws FeatureTermException {
		
		List<Pair<FeatureTerm,Double>> ret = new LinkedList<Pair<FeatureTerm,Double>>();

		for(FeatureTerm example:examples) {
			ret.add(new Pair<FeatureTerm,Double>(example,r.nextDouble()));
		}
		return ret;
	}
}
