/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fterms;

import fterms.exceptions.FeatureTermException;
import java.util.Collections;
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

    
    public static List<Pair<FeatureTerm,Integer>> fastAmalgamRefinements(FeatureTerm f1, FeatureTerm f2, Ontology o, FTKBase dm) throws FeatureTermException, Exception {
        FeatureTerm au = FTAntiunification.simpleAntiunification(f1, f2, o, dm);

        // Compute the refinement path from "au" to "f1":
        List<FeatureTerm> rpath1 = FTRefinement.refinementPath(au,f1,o,dm);
        List<FeatureTerm> rpath2 = FTRefinement.refinementPath(au,f2,o,dm);

        int L1 = rpath1.size(), L2 = rpath2.size();
        System.out.println("au -> f1: " + L1);
        System.out.println("au -> f2: " + L2);

        // compute the LUGs:
        int LUG1N = 0, LUG2N = 0;
        FeatureTerm LUG1 = null;
        FeatureTerm LUG2 = null;
        {
            int min = 0,max = rpath1.size()-1;

            while(min!=max) {
                int mid = (min+max)/2;
                if (mid==min) mid++;
                FeatureTerm midTerm = rpath1.get(mid);
                FeatureTerm u = FTUnification.simpleUnification(midTerm, f2, dm);
                if (u!=null) {
                    min = mid;
                } else {
//                    System.out.println("Term does not unify:" + mid);
//                    System.out.println(midTerm.toStringNOOS(dm));

                    if (max!=mid) max = mid;
                             else max = min;
                }
            }
            LUG1N = min;
            LUG1 = rpath1.get(min);
            System.out.println("LUG1 is in position: " + min);
            System.out.println(LUG1.toStringNOOS(dm));

            min = 0;
            max = rpath2.size()-1;
            while(min!=max) {
                int mid = (min+max)/2;
                if (mid==min) mid++;
                FeatureTerm midTerm = rpath2.get(mid);
                FeatureTerm u = FTUnification.simpleUnification(midTerm, f1, dm);
                if (u!=null) {
                    min = mid;
                } else {
                    if (max!=mid) max = mid;
                             else max = min;
                }
            }
            LUG2N = min;
            LUG2 = rpath2.get(min);
            System.out.println("LUG2 is in position: " + min);
            System.out.println(LUG2.toStringNOOS(dm));
        }

        // compute the amalgam:
        List<FeatureTerm> amalgams = FTUnification.unification(LUG1, LUG2, dm);
        List<Pair<FeatureTerm,Integer>> results = new LinkedList<Pair<FeatureTerm,Integer>>();
        for(FeatureTerm amalgam:amalgams) {
            results.add(new Pair<FeatureTerm,Integer>(amalgam,(L1+L2-2)-(LUG1N+LUG2N)));
        }

        return results;
    }


    // LUG between f1 and f2. "AU" represents the starting point to start refining until reaching f1 if possible
    public static Pair<FeatureTerm,Integer> LUG(FeatureTerm au,FeatureTerm f1,FeatureTerm f2,FTKBase dm,Ontology o) throws FeatureTermException {
        int LUGN = 0;
        FeatureTerm LUG = null;

        FeatureTerm next = null;
        List<FeatureTerm> objects = new LinkedList<FeatureTerm>();
        objects.add(f1);
        next = au;
        do {
            LUG = next;
            List<FeatureTerm> l = FTRefinement.getSpecializationsSubsumingAll(next, dm, o, FTRefinement.ALL_REFINEMENTS, objects);
//            System.out.println(l.size());
            next = null;
            for(FeatureTerm t:l) {
                if (t.subsumes(f1)) {
                    FeatureTerm u = FTUnification.simpleUnification(t, f2, dm);
                    if (u!=null) {
                        next = t;
                        LUGN++;
                        break;
                    }
                }
            }
        }while(next!=null);

        return new Pair<FeatureTerm,Integer>(LUG,LUGN);
    }


    public static List<Pair<FeatureTerm,Integer>> amalgamRefinements(FeatureTerm f1, FeatureTerm f2, Ontology o, FTKBase dm) throws FeatureTermException, Exception {
        FeatureTerm au = FTAntiunification.simpleAntiunification(f1, f2, o, dm);

        // compute the LUGs:
        Pair<FeatureTerm,Integer> LUG1P = LUG(au,f1,f2,dm,o);
        System.out.println("LUG1 is in position: " + LUG1P.m_b);
        System.out.println(LUG1P.m_a.toStringNOOS(dm));

        Pair<FeatureTerm,Integer> LUG2P = LUG(au,f2,f1,dm,o);
        System.out.println("LUG2 is in position: " + LUG2P.m_b);
        System.out.println(LUG2P.m_a.toStringNOOS(dm));

        List<FeatureTerm> rpath1 = FTRefinement.refinementPath(LUG1P.m_a,f1,o,dm);
        List<FeatureTerm> rpath2 = FTRefinement.refinementPath(LUG2P.m_a,f2,o,dm);

        int cost = (rpath1.size()-1)+(rpath2.size()-1);

        System.out.println("Paths from LUG1 and LUG2 to terms:" + rpath1.size() + " " + rpath2.size());

        // compute the amalgam:
        List<FeatureTerm> amalgams = FTUnification.unification(LUG1P.m_a, LUG2P.m_a, dm);
        List<Pair<FeatureTerm,Integer>> results = new LinkedList<Pair<FeatureTerm,Integer>>();
        for(FeatureTerm amalgam:amalgams) {
            results.add(new Pair<FeatureTerm,Integer>(amalgam,cost));
        }

        return results;
    }


    public static List<Pair<FeatureTerm,Integer>> maximalAmalgamRefinements(FeatureTerm f1, FeatureTerm f2, Ontology o, FTKBase dm) throws FeatureTermException, Exception {
        FeatureTerm au = FTAntiunification.simpleAntiunification(f1, f2, o, dm);

        // compute the LUGs:
        Pair<FeatureTerm,Integer> LUG1P = LUG(au,f1,f2,dm,o);
        if (DEBUG>=1) System.out.println("LUG1 is in position: " + LUG1P.m_b);
        if (DEBUG>=1) System.out.println(LUG1P.m_a.toStringNOOS(dm));

        Pair<FeatureTerm,Integer> LUG2P = LUG(au,f2,f1,dm,o);
        if (DEBUG>=1) System.out.println("LUG2 is in position: " + LUG2P.m_b);
        if (DEBUG>=1) System.out.println(LUG2P.m_a.toStringNOOS(dm));

        // make the Amalgam Maximal: RESULT = unification(A,B), A = LUG(f1,LUG2), B = LUG(f2,A)
        Pair<FeatureTerm,Integer> AP = LUG(LUG1P.m_a,f1,LUG2P.m_a,dm,o);
        Pair<FeatureTerm,Integer> BP = LUG(LUG2P.m_a,f2,AP.m_a,dm,o);

        List<FeatureTerm> rpath1 = FTRefinement.refinementPath(AP.m_a,f1,o,dm);
        List<FeatureTerm> rpath2 = FTRefinement.refinementPath(BP.m_a,f2,o,dm);

        int cost = (rpath1.size()-1)+(rpath2.size()-1);

        if (DEBUG>=1) System.out.println("Paths from A and B to terms:" + (rpath1.size()-1) + " " + (rpath2.size()-1));

        // compute the amalgam:
        List<FeatureTerm> amalgams = FTUnification.unification(AP.m_a, BP.m_a, dm);
        List<Pair<FeatureTerm,Integer>> results = new LinkedList<Pair<FeatureTerm,Integer>>();
        for(FeatureTerm amalgam:amalgams) {
            results.add(new Pair<FeatureTerm,Integer>(amalgam,cost));
        }

        return results;
    }
    
    
    public static List<Pair<FeatureTerm,Integer>> assimetricAmalgamRefinements(FeatureTerm source, FeatureTerm target, Ontology o, FTKBase dm) throws FeatureTermException, Exception {
        FeatureTerm au = FTAntiunification.simpleAntiunification(source, target, o, dm);

        // compute the LUGs:
        Pair<FeatureTerm,Integer> LUG1P = LUG(au,source,target,dm,o);
        if (DEBUG>=1) System.out.println("LUG1 is in position: " + LUG1P.m_b);
        if (DEBUG>=1) System.out.println(LUG1P.m_a.toStringNOOS(dm));

        List<FeatureTerm> rpath1 = FTRefinement.refinementPath(LUG1P.m_a,source,o,dm);

        int cost = (rpath1.size()-1);

        if (DEBUG>=1) System.out.println("Path from LUG1 to source:" + rpath1.size());

        // compute the amalgam:
        List<FeatureTerm> amalgams = FTUnification.unification(LUG1P.m_a, target, dm);
        List<Pair<FeatureTerm,Integer>> results = new LinkedList<Pair<FeatureTerm,Integer>>();
        for(FeatureTerm amalgam:amalgams) {
            results.add(new Pair<FeatureTerm,Integer>(amalgam,cost));
        }

        return results;
    }    

 
}
