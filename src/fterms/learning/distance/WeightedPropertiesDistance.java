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
import java.util.HashSet;

import util.Pair;

public class WeightedPropertiesDistance extends PropertiesDistance {

	public WeightedPropertiesDistance() {
	}

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
            // Create the partition induced by the property:
            List<FeatureTerm> s1 = new LinkedList<FeatureTerm>();
            List<FeatureTerm> s2 = new LinkedList<FeatureTerm>();

            for(int i = 0;i<descriptions.size();i++) {
                FeatureTerm d = descriptions.get(i);
                FeatureTerm s = solutions.get(i);

                HashSet<FeatureTerm> cache = getPropertyCache(d);
//                System.out.println("Cache has " + cache.size() + " properties.");
                if (cache.contains(p_w.m_a)) {
                    s1.add(s);
                } else {
                    s2.add(s);
                }
            }
            Pair<Float,Integer> tmp = InformationMeasurement.h_information_gain(solutions,s1,s2,different_solutions);
            p_w.m_b = (double)(tmp.m_a);
            System.out.println(p_w.m_b + "[" + s1.size() + "," + s2.size() + "]" + " -> " + p_w.m_a.toStringNOOS(dm));
		}

		
	}
}
