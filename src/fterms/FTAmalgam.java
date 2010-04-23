/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fterms;

import fterms.exceptions.FeatureTermException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import util.Pair;

/**
 *
 * @author santi
 */
public class FTAmalgam {

    public static final int DEBUG = 0;

    public static Pair<FeatureTerm,Integer> amalgamProperties(FeatureTerm f1, FeatureTerm f2, Ontology ontology, FTKBase domain_model) throws FeatureTermException {

        List<FeatureTerm> propertiesF1 = Disintegration.disintegrate(f1, domain_model, ontology);
        List<FeatureTerm> propertiesF2 = Disintegration.disintegrate(f2, domain_model, ontology);
        List<FeatureTerm> common = new LinkedList<FeatureTerm>();
        List<FeatureTerm> toAdd = new LinkedList<FeatureTerm>();
        FeatureTerm result = null;

        if (DEBUG>=1) System.out.println("amalgamProperties, start with " + propertiesF1.size() + ", " + propertiesF2.size() + " porperties.");

        // Find all the properties composing the antiunification:
        for(FeatureTerm p:propertiesF1) {
            if (p.subsumes(f2)) {
                toAdd.add(p);
            }
        }
        for(FeatureTerm p:propertiesF2) {
            if (p.subsumes(f1)) {
                toAdd.add(p);
            }
        }
        propertiesF1.removeAll(toAdd);
        propertiesF2.removeAll(toAdd);
        if (DEBUG>=1) System.out.println("amalgamProperties, after subsumption test: " + common.size() + " in common, and " + propertiesF1.size() + ", " + propertiesF2.size() + " left.");

        // Generate the temporary answer:
        common.addAll(toAdd);
        toAdd.clear();
        result = FTAntiunification.simpleAntiunification(f1,f2, ontology, domain_model);

        // Find all the properties which unify without problems:
        for(FeatureTerm p:propertiesF1) {
            if (FTUnification.simpleUnification(p,f2,ontology,domain_model)!=null) {
                toAdd.add(p);
            }
        }
        for(FeatureTerm p:propertiesF2) {
            if (FTUnification.simpleUnification(p,f1,ontology,domain_model)!=null) {
                toAdd.add(p);
            }
        }
        propertiesF1.removeAll(toAdd);
        propertiesF2.removeAll(toAdd);
        if (DEBUG>=1) System.out.println("amalgamProperties, after unification test: " + common.size() + " in common, and " + propertiesF1.size() + ", " + propertiesF2.size() + " left.");

        // Generate the temporary answer:
        for(FeatureTerm p:toAdd) {
            if (DEBUG>=2) {
                System.out.println("amalgamProperties, Adding property:");
                System.out.println(p.toStringNOOS(domain_model));
            }
            if (result==null) {
                result = p;
            } else {
                FeatureTerm tmp = FTUnification.simpleUnification(result, p, ontology, domain_model);
                if (tmp==null) {
                    System.err.println("amalgamProperties, Inconsistency!!!");
                    System.err.println("could not unify result:");
                    System.err.println(result.toStringNOOS(domain_model));
                    System.err.println("with property:");
                    System.err.println(p.toStringNOOS(domain_model));
                }
                result = tmp;
            }
        }
        common.addAll(toAdd);
        toAdd.clear();


        // Try to add the rest of properties one by one, sorted by a heuristic
        // Heuristic: potential number of new refinements that can be added if this one is (i.e. number of refinements left subsumed)
        {
            HashMap<FeatureTerm,Integer> heuristic = new HashMap<FeatureTerm,Integer>();
            List<FeatureTerm> left = new LinkedList<FeatureTerm>();
            left.addAll(propertiesF1);
            left.addAll(propertiesF2);
            for(FeatureTerm p:left) {
                int count = 0;
                for(FeatureTerm p2:left) {
                    if (p.subsumes(p2)) count++;
                }
                heuristic.put(p, count);
            }
            while(!left.isEmpty()) {
                FeatureTerm next = null;
                int h = 0;
                for(FeatureTerm p:left) {
                    if (next==null || heuristic.get(p)>h) {
                        next = p;
                        h = heuristic.get(p);
                    }
                }
                left.remove(next);

                FeatureTerm tmp = FTUnification.simpleUnification(result, next, ontology, domain_model);
                if (tmp!=null) {
                    result = tmp;
                    common.add(next);
                }
            }
        }
        propertiesF1.removeAll(common);
        propertiesF2.removeAll(common);

        if (DEBUG>=1) System.out.println("amalgamProperties, after heuristic test: " + common.size() + " in common, and " + propertiesF1.size() + ", " + propertiesF2.size() + " left.");

        return new Pair<FeatureTerm,Integer>(result,propertiesF1.size()+propertiesF2.size());
    }

}
