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
			Ontology o, boolean fast) throws FeatureTermException {
		super(objects, dm, o, fast);
	}
	
	
	static class PWComparator implements Comparator {

		public int compare(Object arg0, Object arg1) {
			double o0 = ((Pair<FeatureTerm,Double>)arg0).m_b;
			double o1 = ((Pair<FeatureTerm,Double>)arg1).m_b;

			if( o0 > o1 )
				return 0;
				else if( o0 < o1 )
				return -1;
				else
				return 1;		
			}
		
	}

	
	/*
	 * Only the best "cutoff" patterns will have a positve weight, the rest will have 0
	 */
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
//			System.out.println(p_w.m_a.toStringNOOS(dm) + "\n" + p_w.m_b);
		}

		Collections.sort(m_propertyWeight,new PWComparator());
		
//		for(Pair<FeatureTerm,Double> pw:m_propertyWeight) {
//			System.out.println(pw.m_b + " -> " + pw.m_a.toStringNOOS(dm));
//		}
	}
}
