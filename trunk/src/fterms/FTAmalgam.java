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

    public static Pair<FeatureTerm,Integer> amalgamProperties(FeatureTerm f1, FeatureTerm f2, Ontology ontology, FTKBase domain_model) throws FeatureTermException, Exception {

//            public static List<FeatureTerm> disintegrate(FeatureTerm object, FTKBase dm, Ontology o, boolean cache, boolean fast) throws Exception {

        List<FeatureTerm> propertiesF1 = Disintegration.disintegrate(f1, domain_model, ontology, true, true);
        List<FeatureTerm> propertiesF2 = Disintegration.disintegrate(f2, domain_model, ontology, true, true);
        List<FeatureTerm> common = new LinkedList<FeatureTerm>();
        List<FeatureTerm> toAdd = new LinkedList<FeatureTerm>();
        List<FeatureTerm> left = new LinkedList<FeatureTerm>();
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
            if (FTUnification.simpleUnification(p,f2,domain_model)!=null) {
                toAdd.add(p);
            }
        }
        for(FeatureTerm p:propertiesF2) {
            if (FTUnification.simpleUnification(p,f1,domain_model)!=null) {
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
                FeatureTerm tmp_result = null;
                List<FeatureTerm> tmpl = FTUnification.unification(result, p, domain_model);
                if (tmpl==null) {
                    System.err.println("amalgamProperties, Inconsistency!!!");
                    System.err.println("could not unify result:");
                    System.err.println(result.toStringNOOS(domain_model));
                    System.err.println("with property:");
                    System.err.println(p.toStringNOOS(domain_model));
                } else {
                    for(FeatureTerm tmp:tmpl) {
                        if (tmp.subsumes(f1) && tmp.subsumes(f2)) {
                            tmp_result = tmp;
                            break;
                        }
                    }
                    if (tmp_result==null) {
                        System.err.println("Amalgam: cannot add a property without nonsubsumming the terms...");
                        left.add(p);
                    } else {
                        result = tmp_result;
                    }
                }
            }
        }
        common.addAll(toAdd);
        toAdd.clear();


        // Try to add the rest of properties one by one, sorted by a heuristic
        // Heuristic: potential number of new refinements that can be added if this one is (i.e. number of refinements left subsumed)
        {
            HashMap<FeatureTerm,Integer> heuristic = new HashMap<FeatureTerm,Integer>();
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
                System.out.println("Amalgam: " + left.size() + " properties left");

                FeatureTerm next = null;
                int h = 0;
                for(FeatureTerm p:left) {
                    if (next==null || heuristic.get(p)>h) {
                        next = p;
                        h = heuristic.get(p);
                    }
                }
                left.remove(next);

                FeatureTerm tmp = FTUnification.simpleUnification(result, next, domain_model);
                if (tmp!=null) {
                    result = tmp;
                    common.add(next);
                }

                System.out.println("Amalgam:");
                System.out.println(result.toStringNOOS(domain_model));;

            }
        }
        propertiesF1.removeAll(common);
        propertiesF2.removeAll(common);

        if (DEBUG>=1) System.out.println("amalgamProperties, after heuristic test: " + common.size() + " in common, and " + propertiesF1.size() + ", " + propertiesF2.size() + " left.");

        return new Pair<FeatureTerm,Integer>(result,propertiesF1.size()+propertiesF2.size());
    }

}
