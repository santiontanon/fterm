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

    static boolean s_cache = true;
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
        Integer max_properties = null;
        Integer min_properties = null;
        m_propertyWeight = new LinkedList<Pair<FeatureTerm, Double>>();

        // Generate all the properties
        for (FeatureTerm object : objects) {

            System.out.println("processing " + object.getName() + " ("+ count + ")");
            List<FeatureTerm> properties_tmp = disintegrate(object,dm,o, s_cache);
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
            System.out.println("Filtering time: " + (time-start_time));

            count++;

//            if (count>=10) break;
        }

        // The weights will be all 1 in this distance:
        System.out.println("Properties per term: [" + min_properties + " - " + max_properties + "]");
        System.out.println(m_propertyWeight.size() + " properties");
//		for(Pair<FeatureTerm,Double> p_w:m_propertyWeight) {
//			System.out.println(p_w.m_a.toStringNOOS(dm) + "\n" + p_w.m_b);
//		}
    }

    public List<FeatureTerm> disintegrate(FeatureTerm object, FTKBase dm, Ontology o, boolean cache) throws Exception {
        long start_time = System.currentTimeMillis();
        List<FeatureTerm> properties_tmp = null;
        if (object.getName()!=null && cache) {
            String fname;
            String fname_state;
            FeatureTerm current_state = null;
            if (m_fast) fname = "disintegration-cache/fast-"+object.getName();
                   else fname = "disintegration-cache/formal-" + object.getName();
            if (m_fast) fname_state = "disintegration-cache/fast-"+object.getName()+"-state";
                   else fname_state = "disintegration-cache/formal-" + object.getName()+"-state";
            File tmp_state = new File(fname_state);
            File tmp = new File(fname);
            // set up the current state:
            if (tmp_state.exists() && tmp.exists()) {
                // disintegration was abandoned in the middle
                // load properties
                FTKBase tmpBase = new FTKBase();
                tmpBase.uses(dm);
                tmpBase.ImportNOOS(fname, o);
                properties_tmp = new LinkedList<FeatureTerm>();
                properties_tmp.addAll(tmpBase.getAllTerms());
                // load the last state:
                FTKBase tmpBase_state = new FTKBase();
                tmpBase_state.uses(dm);
                tmpBase_state.ImportNOOS(fname_state, o);
                current_state = tmpBase_state.getAllTerms().get(0);
                System.out.println(properties_tmp.size() + " properties were already extracted. Continuing...");
            } else {
                // disintegration didn't start or it's complete:
                if (tmp.exists()) {
                    // load properties
                    FTKBase tmpBase = new FTKBase();
                    tmpBase.uses(dm);
                    tmpBase.ImportNOOS(fname, o);
                    properties_tmp = new LinkedList<FeatureTerm>();
                    properties_tmp.addAll(tmpBase.getAllTerms());
                    System.out.println(properties_tmp.size() + " properties were already extracted. Complete.");
                } else {
                    properties_tmp = new LinkedList<FeatureTerm>();
                    current_state = object.clone(dm, o);
                    List<FeatureTerm> variables = FTRefinement.variables(current_state);
                    for(FeatureTerm v:variables) {
                        if (!dm.contains(v)) v.setName(null);
                    }
                    System.out.println("Disintegrating from scratch...");
                }
            }

            while(current_state!=null) {
                // extract a property:
                Pair<FeatureTerm, FeatureTerm> property_rest;
                if (m_fast) {
                    property_rest = Disintegration.extractPropertyFast(current_state, dm, o);
                } else {
                    property_rest = Disintegration.extractProperty(current_state, dm, o);
                }
                if (property_rest!=null) {
                    current_state = property_rest.m_b;
                    properties_tmp.add(property_rest.m_a);

                    System.out.println(properties_tmp.size() + " properties (term now has " + FTRefinement.variables(current_state).size() + " variables)");

                    // save the property
                    {
                        FileWriter fw = new FileWriter(fname,true);
                        fw.write(property_rest.m_a.toStringNOOS(dm)+"\n");
                        fw.flush();
                        fw.close();
                    }
                    // save the state
                    if (current_state!=null) {
                        FileWriter fw = new FileWriter(fname_state);
                        fw.write(current_state.toStringNOOS(dm)+"\n");
                        fw.flush();
                        fw.close();
                    } else {
                        tmp_state.delete();
                    }
                } else {
                    current_state = null;
                    tmp_state.delete();
                }

                
            }
        } else {
            if (m_fast) properties_tmp = Disintegration.disintegrateFast(object, dm, o);
                   else properties_tmp = Disintegration.disintegrate(object, dm, o);
        }

        System.out.println(properties_tmp.size() + " found, now filtering... (previous total: " + m_propertyWeight.size() + ")");

        long disintegration_time = System.currentTimeMillis();
        System.out.println("Disintegration time: " + (disintegration_time-start_time));
        return properties_tmp;
    }


    public HashSet<FeatureTerm> getPropertyCache(FeatureTerm f1) throws FeatureTermException {
        HashSet<FeatureTerm> cache1 = property_cache.get(f1);

        if (cache1==null) {
            System.out.println("getPropertyCache: new case, testinc against " + m_propertyWeight.size() + " properties.");
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

		System.out.println("PD: " + shared + " - " + f1_not_shared + " - " + f2_not_shared + " -> " + distance);
		System.out.flush();
        return distance;
    }
}
