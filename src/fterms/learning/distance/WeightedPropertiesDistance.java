package fterms.learning.distance;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import fterms.FTKBase;
import fterms.FeatureTerm;
import fterms.Ontology;
import fterms.Path;
import fterms.exceptions.FeatureTermException;
import fterms.learning.InformationMeasurement;

import util.Pair;

public class WeightedPropertiesDistance extends PropertiesDistance {

	public WeightedPropertiesDistance(List<FeatureTerm> objects, FTKBase dm,
			Ontology o, boolean fast) throws Exception {
		super(objects, dm, o, fast);
	}

    public WeightedPropertiesDistance(List<FeatureTerm> objects, FTKBase dm,
			Ontology o, Path dp, boolean fast) throws Exception {
		super(objects, dm, o, dp , fast);
	}
	
	public void computeWeights(List<FeatureTerm> cases,Path sp,Path dp,FTKBase dm) throws FeatureTermException {

		List<FeatureTerm> descriptions = new LinkedList<FeatureTerm>();
		List<FeatureTerm> solutions = new LinkedList<FeatureTerm>();
		List<FeatureTerm> different_solutions = new LinkedList<FeatureTerm>();
		
		for(FeatureTerm c:cases) {
			FeatureTerm solution = c.readPath(sp);
			if (!different_solutions.contains(solution)) different_solutions.add(solution);
			
			descriptions.add(c.readPath(dp));
			solutions.add(solution);
		}
				
		for(Pair<FeatureTerm,Double> p_w:m_propertyWeight) {
//			Pair<Float,Integer> tmp = InformationMeasurement.h_rldm(descriptions,solutions,different_solutions,p_w.m_a);
			Pair<Float,Integer> tmp = InformationMeasurement.h_information_gain(descriptions,solutions,different_solutions,p_w.m_a);	
//			p_w.m_b = 1-(double)(tmp.m_a);
			p_w.m_b = (double)(tmp.m_a);
//			p_w.m_b = Math.pow(p_w.m_b,64);
			System.out.println(p_w.m_a.toStringNOOS(dm) + "\n" + p_w.m_b);
		}

		
//		for(Pair<FeatureTerm,Double> pw:m_propertyWeight) {
//			System.out.println(pw.m_b + " -> " + pw.m_a.toStringNOOS(dm));
//		}
	}
}
