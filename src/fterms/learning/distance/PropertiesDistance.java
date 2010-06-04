package fterms.learning.distance;

import java.util.LinkedList;
import java.util.List;

import fterms.FTKBase;
import fterms.FeatureTerm;
import fterms.Ontology;
import fterms.exceptions.FeatureTermException;

import fterms.Disintegration;
import fterms.Path;
import java.util.Collection;
import util.Pair;

public class PropertiesDistance extends Distance {

    boolean m_fast = false;
    List<FeatureTerm> descriptions = new LinkedList<FeatureTerm>();
    protected List<Pair<FeatureTerm, Double>> m_propertyWeight = null;;

    public PropertiesDistance(Collection<FeatureTerm> objects, FTKBase dm, Ontology o, Path dp, boolean fast) throws FeatureTermException {
        m_fast = fast;
        for (FeatureTerm obj : objects) {
            descriptions.add(obj.readPath(dp));
        }
        generateAllProperties(descriptions, dm, o);
    }

    public PropertiesDistance(List<FeatureTerm> objects, FTKBase dm, Ontology o, boolean fast) throws FeatureTermException {
        m_fast = fast;
        generateAllProperties(objects, dm, o);
    }

    private void generateAllProperties(List<FeatureTerm> objects, FTKBase dm, Ontology o) throws FeatureTermException {
        int count = 0;
        m_propertyWeight = new LinkedList<Pair<FeatureTerm, Double>>();

        // Generate all the properties
        for (FeatureTerm object : objects) {
            System.out.println("processing " + count);
//			System.out.println(object.toStringNOOS(dm));

            List<FeatureTerm> properties_tmp = null;
            if (m_fast) {
                properties_tmp = Disintegration.disintegrateFast(object, dm, o);
            } else {
                properties_tmp = Disintegration.disintegrate(object, dm, o);
            }

            System.out.println(properties_tmp.size() + " found, now filtering... (previous total: " + m_propertyWeight.size());

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

            count++;
        }

        // The weights will be all 1 in this distance:
        System.out.println(m_propertyWeight.size() + " properties");
//		for(Pair<FeatureTerm,Double> p_w:m_propertyWeight) {
//			System.out.println(p_w.m_a.toStringNOOS(dm) + "\n" + p_w.m_b);
//		}
        }

    public double distance(FeatureTerm f1, FeatureTerm f2, Ontology o, FTKBase dm) throws FeatureTermException {
        double shared = 0;
        double f1_not_shared = 0;
        double f2_not_shared = 0;

        if (m_propertyWeight==null) {
            generateAllProperties(descriptions, dm, o);
        }

        for (Pair<FeatureTerm, Double> p_w : m_propertyWeight) {
            if (p_w.m_b > 0) {
                if (p_w.m_a.subsumes(f1)) {
                    if (p_w.m_a.subsumes(f2)) {
                        shared += p_w.m_b;
                    } else {
                        f1_not_shared += p_w.m_b;
                    }
                } else {
                    if (p_w.m_a.subsumes(f2)) {
                        f2_not_shared += p_w.m_b;
                    } else {
                        // none of them have it!
                        // should we count it as a similarity???
                    }
                }
            }
        }

        double distance = 1.0f - (((double) (shared * 2)) / ((double) (shared * 2 + f1_not_shared + f2_not_shared)));
//		double distance = 1.0f-(((double)(shared))/((double)(shared+f1_not_shared+f2_not_shared)));

		System.out.println("PD: " + shared + " - " + f1_not_shared + " - " + f2_not_shared + " -> " + distance);
		System.out.flush();
        return distance;
    }
}
