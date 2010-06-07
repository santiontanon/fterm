/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fterms.learning.distance;

import fterms.FTKBase;
import fterms.FeatureTerm;
import fterms.Ontology;
import fterms.Path;
import fterms.exceptions.FeatureTermException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import util.Pair;

/**
 *
 * @author santi
 */
public class BoundedWeightedPropertiesDistance extends WeightedPropertiesDistance {
    int m_max_properties = 10;

	public BoundedWeightedPropertiesDistance(List<FeatureTerm> objects, FTKBase dm,
			Ontology o, boolean fast, int max_properties) throws Exception {
		super(objects, dm, o, fast);
        m_max_properties = max_properties;
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

}
