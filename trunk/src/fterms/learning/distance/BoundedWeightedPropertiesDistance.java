/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fterms.learning.distance;

import fterms.Disintegration;
import fterms.FTKBase;
import fterms.FeatureTerm;
import fterms.Ontology;
import fterms.Path;
import fterms.exceptions.FeatureTermException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import util.Pair;

/**
 *
 * @author santi
 */
public class BoundedWeightedPropertiesDistance extends WeightedPropertiesDistance {
    int m_max_properties = 10;
    int m_max_properties_per_term = 100;

	public BoundedWeightedPropertiesDistance(List<FeatureTerm> objects, FTKBase dm,
			Ontology o, boolean fast, int max_properties, int max_properties_per_term) throws Exception {
        m_max_properties = max_properties;
        m_max_properties_per_term = max_properties_per_term;
        m_fast = fast;
        generateAllProperties(objects, dm, o);
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


    public void computeWeights(List<FeatureTerm> cases,Path sp,Path dp,FTKBase dm) throws FeatureTermException {

        super.computeWeights(cases, sp, dp, dm);

        Collections.sort(m_propertyWeight,new PWComparator());

        while(m_propertyWeight.size()>m_max_properties) {
            m_propertyWeight.remove(m_propertyWeight.size()-1);
        }
    }


    void generateAllProperties(List<FeatureTerm> objects, FTKBase dm, Ontology o) throws Exception {
        int count = 0;
        m_propertyWeight = new LinkedList<Pair<FeatureTerm, Double>>();

        // Generate all the properties
        for (FeatureTerm object : objects) {
            long start_time = System.currentTimeMillis();
            System.out.println("BoundedWeightedPropertiesDistance: processing " + object.getName() + " ("+ count + ")");
//			System.out.println(object.toStringNOOS(dm));

            List<FeatureTerm> properties_tmp = null;
            if (m_fast) {
                properties_tmp = Disintegration.disintegrateFast(object, dm, o);
                while(properties_tmp.size()>m_max_properties_per_term) properties_tmp.remove(0);
            } else {
//                properties_tmp = Disintegration.disintegrateFirstN(object, dm, o, m_max_properties_per_term, 0);
//                properties_tmp = Disintegration.disintegrateFirstN(object, dm, o, m_max_properties_per_term, 1);
//                properties_tmp = Disintegration.disintegrateFirstN(object, dm, o, m_max_properties_per_term, 2);
                properties_tmp = Disintegration.disintegrateFirstN(object, dm, o, m_max_properties_per_term, 3);
            }

            System.out.println(properties_tmp.size() + " found, now filtering... (previous total: " + m_propertyWeight.size());

            long disintegration_time = System.currentTimeMillis();

            for (FeatureTerm property : properties_tmp) {
                boolean duplicate = false;

                for (Pair<FeatureTerm, Double> p_w : m_propertyWeight) {
                    if (property.equivalents(p_w.m_a)) {
                        duplicate = true;
                        break;
                    }
                }

                if (!duplicate) {
                    m_propertyWeight.add(new Pair<FeatureTerm, Double>(property, 1.0));
                }
            }

            long time = System.currentTimeMillis();
            System.out.println("Disintegration time: " + (disintegration_time-start_time) + " filtering timw: " + (time-disintegration_time));

            count++;
        }

        // The weights will be all 1 in this distance:
        System.out.println(m_propertyWeight.size() + " properties");
//		for(Pair<FeatureTerm,Double> p_w:m_propertyWeight)
//			System.out.println(p_w.m_a.toStringNOOS(dm) + "\n" + p_w.m_b);
    }

}
