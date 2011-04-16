package fterms.learning.distance;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import fterms.FTKBase;
import fterms.FeatureTerm;
import fterms.Ontology;
import fterms.exceptions.FeatureTermException;

import fterms.Disintegration;
import fterms.FTRefinement;
import fterms.Path;
import java.io.File;
import java.io.FileWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import util.Pair;

public class PropertiesDistance extends Distance {
    
    public static int DEBUG = 0;

    static boolean s_cache = false;
    boolean m_fast = false;
    List<FeatureTerm> descriptions = new LinkedList<FeatureTerm>();
    protected List<Pair<FeatureTerm, Double>> m_propertyWeight = null;;
    HashMap<FeatureTerm,HashSet<FeatureTerm>> property_cache = new HashMap<FeatureTerm,HashSet<FeatureTerm>>();

    
    public PropertiesDistance() {
        m_propertyWeight = new LinkedList<Pair<FeatureTerm, Double>>();
	}
    
    public PropertiesDistance(Collection<FeatureTerm> objects, FTKBase dm, Ontology o, Path dp, boolean fast) throws Exception {
        m_fast = fast;
        for (FeatureTerm obj : objects) {
            descriptions.add(obj.readPath(dp));
        }
        generateAllProperties(descriptions, dm, o);
    }

    public PropertiesDistance(List<FeatureTerm> objects, FTKBase dm, Ontology o, boolean fast) throws Exception {
        m_fast = fast;
        generateAllProperties(objects, dm, o);
    }

    void generateAllProperties(List<FeatureTerm> objects, FTKBase dm, Ontology o) throws Exception {
        int count = 0;
        long start = System.currentTimeMillis();
        Integer max_properties = null;
        Integer min_properties = null;
        m_propertyWeight = new LinkedList<Pair<FeatureTerm, Double>>();

        // Generate all the properties
        for (FeatureTerm object : objects) {

            if (DEBUG>=1) System.out.println("processing " + object.getName() + " ("+ count + ")");
            List<FeatureTerm> properties_tmp = Disintegration.disintegrate(object,dm,o, s_cache, m_fast);
            long start_time = System.currentTimeMillis();

            if (max_properties==null || properties_tmp.size()>max_properties) max_properties = properties_tmp.size();
            if (min_properties==null || properties_tmp.size()<min_properties) min_properties = properties_tmp.size();

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
            if (DEBUG>=1) System.out.println("Filtering time: " + (time-start_time));

            count++;

//            if (count>=10) break;
        }

        // The weights will be all 1 in this distance:
        // if (DEBUG>=1)
            System.out.println("Properties per term: [" + min_properties + " - " + max_properties + "]");
        // if (DEBUG>=1)
            System.out.println(m_propertyWeight.size() + " properties (in " + (System.currentTimeMillis()-start) + "ms)");
//		for(Pair<FeatureTerm,Double> p_w:m_propertyWeight) {
//			System.out.println(p_w.m_a.toStringNOOS(dm) + "\n" + p_w.m_b);
//		}
    }


    public HashSet<FeatureTerm> getPropertyCache(FeatureTerm f1) throws FeatureTermException {
        HashSet<FeatureTerm> cache1 = property_cache.get(f1);

        if (cache1==null) {
//            System.out.println("getPropertyCache: new case, testinc against " + m_propertyWeight.size() + " properties.");
            cache1 = new HashSet<FeatureTerm>();
            for (Pair<FeatureTerm, Double> p_w : m_propertyWeight)
                if (p_w.m_a.subsumes(f1))
                    cache1.add(p_w.m_a);
            property_cache.put(f1,cache1);
        }

        return cache1;
    }

    public double distance(FeatureTerm f1, FeatureTerm f2, Ontology o, FTKBase dm) throws Exception {
        double shared = 0;
        double f1_not_shared = 0;
        double f2_not_shared = 0;

        if (m_propertyWeight==null || m_propertyWeight.size()==0) {
            generateAllProperties(descriptions, dm, o);
        }

        HashSet<FeatureTerm> cache1 = getPropertyCache(f1);
        HashSet<FeatureTerm> cache2 = getPropertyCache(f2);

        for (Pair<FeatureTerm, Double> p_w : m_propertyWeight) {
            if (p_w.m_b > 0) {
                if (cache1.contains(p_w.m_a)) {
                    if (cache2.contains(p_w.m_a)) {
                        shared += p_w.m_b;
                    } else {
                        f1_not_shared += p_w.m_b;
                    }
                } else {
                    if (cache2.contains(p_w.m_a)) {
                        f2_not_shared += p_w.m_b;
                    } else {
                        // none of them have it!
                        // should we count it as a similarity???
                    }
                }
            }
        }

        double tmp = ((double) (shared * 2 + f1_not_shared + f2_not_shared));
        double distance = (tmp>0 ? 1.0f - (((double) (shared * 2)) / tmp):1.0);
//		double distance = 1.0f-(((double)(shared))/((double)(shared+f1_not_shared+f2_not_shared)));

//		System.out.println("PD: " + shared + " - " + f1_not_shared + " - " + f2_not_shared + " -> " + distance);
//		System.out.flush();
        return distance;
    }
}
