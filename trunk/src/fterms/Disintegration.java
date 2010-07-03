package fterms;

import java.util.LinkedList;
import java.util.List;

import fterms.exceptions.FeatureTermException;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import util.Pair;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * This class implements the disintegration operation, which breaks a feature term into a set of properties as
 * described in:
 *  - "On Similarity Measures based on a Refinement Lattice" by Santiago Ontanon and Enric Plaza (ICCBR 2009)
 *  - "Similarity over Refinement Graphs" by Santiago Ontanon and Enric Plaza (Machine Learning Journal, submitted)
 * @author santi
 */
public class Disintegration {

    public static int DEBUG = 0;

    public static HashMap<FeatureTerm,List<FeatureTerm>> propertiesFormalTable = new HashMap<FeatureTerm,List<FeatureTerm>>();
    public static HashMap<FeatureTerm,List<FeatureTerm>> propertiesFastTable = new HashMap<FeatureTerm,List<FeatureTerm>>();


    public static List<FeatureTerm> disintegrate(FeatureTerm f, FTKBase dm, Ontology o) throws FeatureTermException {
        List<FeatureTerm> properties = null;
        Pair<FeatureTerm, FeatureTerm> property_rest;

        // First check cache:
        properties = propertiesFormalTable.get(f);
        if (properties!=null) return properties;

        properties = new LinkedList<FeatureTerm>();

        FeatureTerm unnamed = f.clone(dm, o);
        List<FeatureTerm> variables = FTRefinement.variables(unnamed);
        for(FeatureTerm v:variables) {
            if (!dm.contains(v)) v.setName(null);
        }
        if (DEBUG>=2) {
            System.out.println("Unnamed term to disintegrate:");
            System.out.println(unnamed.toStringNOOS(dm));
        }

        do {
            property_rest = extractProperty(unnamed, dm, o);

            if (property_rest != null) {
                if (property_rest.m_a!=null) properties.add(property_rest.m_a);
                unnamed = property_rest.m_b;

                System.out.println(properties.size() + " properties (term now has " + FTRefinement.variables(unnamed).size() + " variables");

//				System.out.println("--------------------");
//				System.out.println(property_rest.m_a.toStringNOOS(dm));
//				System.out.println(f.toStringNOOS(dm));
            }
        } while (property_rest != null);

//		System.out.println("--------------------");
//		System.out.println(unnamed.toStringNOOS(dm));

        propertiesFormalTable.put(f,properties);
        return properties;
    }


    // - This method generates a path from f to 'any', and only generates properties for the last 'N' refinements
    // - It is useful when dealing with large terms, since the last 'n' refinements will deal with the smallest terms
    //   and thus will not involve subsumption operations with large terms
    // - when 'method' = 0, the path is selected at random from the term to any (generlization)
    // - when 'method' = 1, the path is selected as the first path from the term to any (generalization)
    // - when 'method' = 2, the path is selected as the first path from any to the term (specialization)
    public static List<FeatureTerm> disintegrateFirstN(FeatureTerm f, FTKBase dm, Ontology o,int N,int method) throws FeatureTermException {
        List<FeatureTerm> properties = null;
        List<FeatureTerm> refinementPath = new LinkedList<FeatureTerm>();
        Random r = new Random();

        // First check cache:
        properties = propertiesFormalTable.get(f);
        if (properties!=null) return properties;

        properties = new LinkedList<FeatureTerm>();

        FeatureTerm unnamed = null;
        if (method==0 || method==1) {
            unnamed = f.clone(dm, o);
            List<FeatureTerm> variables = FTRefinement.variables(unnamed);
            for(FeatureTerm v:variables) {
                if (!dm.contains(v)) v.setName(null);
            }
            if (DEBUG>=2) {
                System.out.println("Unnamed term to disintegrate:");
                System.out.println(unnamed.toStringNOOS(dm));
            }
        } else {
            unnamed = o.getSort("any").createFeatureTerm();
        }
        // generate the refinement path:
        refinementPath.add(unnamed);
        do{
//            System.out.println(refinementPath.size() + "\n" + unnamed.toStringNOOS(dm));
            switch(method) {
                // generalization, random
                case 0:{
                    List<FeatureTerm> refinements = FTRefinement.getSomeRandomGeneralizationsAggressive(unnamed, dm, o);
                    if (!refinements.isEmpty()) {
                        unnamed = refinements.get(r.nextInt(refinements.size()));
                        refinementPath.add(unnamed);
                    } else {
                        unnamed = null;
                    }
                }
                break;
                // generalization, deterministic
                case 1:{
                    List<FeatureTerm> refinements = FTRefinement.getSomeGeneralizationsAggressive(unnamed, dm, o);
                    if (!refinements.isEmpty()) {
                        unnamed = refinements.get(0);
                        refinementPath.add(unnamed);
                    } else {
                        unnamed = null;
                    }
                }
                break;
                // specialization, deterministic
                case 2:{
                    if (refinementPath.size()>=N) {
                        unnamed = null;
                    } else {
                        List<FeatureTerm> tmp = new LinkedList<FeatureTerm>();
                        tmp.add(f);
                        List<FeatureTerm> refinements = FTRefinement.getSomeSpecializationSubsumingAll(unnamed, dm, o, FTRefinement.ALL_REFINEMENTS, tmp);
                        if (!refinements.isEmpty()) {
                            unnamed = refinements.get(0);
                            refinementPath.add(0,unnamed);
                        } else {
                            unnamed = null;
                        }
                    }
                }
                // specialization, random
                case 3:{
                    if (refinementPath.size()>N) {
                        unnamed = null;
                    } else {
                        List<FeatureTerm> tmp = new LinkedList<FeatureTerm>();
                        tmp.add(f);
                        List<FeatureTerm> refinements = FTRefinement.getSomeSpecializationSubsumingAll(unnamed, dm, o, FTRefinement.ALL_REFINEMENTS, tmp);
                        if (!refinements.isEmpty()) {
                            unnamed = refinements.get(r.nextInt(refinements.size()));
                            refinementPath.add(0,unnamed);
                        } else {
                            unnamed = null;
                        }
                    }
                }
                break;

            }
        }while(unnamed!=null);


        System.out.println("Refinement path is of length: " + refinementPath.size());
        // reduce the path to the desired length:
        while(refinementPath.size()>(N+1)) refinementPath.remove(0);

        // generate the properties:
        for(int i = 0;i<refinementPath.size()-1;i++) {
            FeatureTerm t1 = refinementPath.get(i);
            FeatureTerm t2 = refinementPath.get(i+1);
            FeatureTerm property = remainderFastAssumingRefinement(t1, t2, dm, o);
            properties.add(property);
//            System.out.println("t1: -----------------------------------");
//            System.out.println(t1.toStringNOOS(dm));
//            System.out.println("Property");
//            System.out.println(property.toStringNOOS(dm));
            if (DEBUG>=1) System.out.println("extractPropertyFormal finished...");
        }

        return properties;
    }


    /*
     * This method is like the one below, but follows the exat formulation used in out journal paper
     */
    public static Pair<FeatureTerm, FeatureTerm> extractProperty(FeatureTerm f, FTKBase dm, Ontology o) throws FeatureTermException {

	if (DEBUG>=1) System.out.println("extractPropertyFormal started...");
        if (DEBUG>=2) {
            System.out.println("Original term:");
            System.out.println(f.toStringNOOS(dm));
        }

        List<FeatureTerm> refinements = FTRefinement.getSomeGeneralizationsAggressive(f, dm, o);

        if (refinements.size() > 0) {
            FeatureTerm refinement = refinements.get(0);
            Pair<FeatureTerm, FeatureTerm> tmp = new Pair<FeatureTerm, FeatureTerm>(remainderFastAssumingRefinement(f, refinement, dm, o), refinement);

            if (DEBUG>=2) {
                System.out.println("Property:");
                System.out.println(tmp.m_a.toStringNOOS(dm));
            }
            if (DEBUG>=1) System.out.println("extractPropertyFormal finished...");
            return tmp;
        } else {
            if (DEBUG>=1) System.out.println("extractPropertyFormal finished... (null)");
            return null;
        }
    }

    /*
     * This method computes the remainder by using the "unification" method. It returns the correct result, but it's very, very slow.
     * It's here just because it's implemented in the exact way in which the operation is defined in our submissino to the Machine Learning
     * Journal.
     */
    public static FeatureTerm remainder(FeatureTerm f, FeatureTerm refinement, FTKBase dm, Ontology o) throws FeatureTermException {
        FeatureTerm oldRemainder = null;
        FeatureTerm remainder = f;
        do {
            oldRemainder = remainder;
            if (DEBUG>=2) {
                System.out.println("remainder: cycle starts");
            }
            if (DEBUG>=3) {
                System.out.println("refinement: ");
                System.out.println(remainder.toStringNOOS(dm));
            }
            List<FeatureTerm> refinements = FTRefinement.getGeneralizationsAggressive(remainder, dm, o);
            if (DEBUG>=3) System.out.println("remainder: " + refinements.size() + " refinements.");

            remainder = null;
            for (FeatureTerm r : refinements) {
                List<FeatureTerm> unifications = FTUnification.unification(refinement, r, o, dm, true);
                if (unifications==null) {
                    if (DEBUG>=3) System.out.println("remainder: 0 unifications");
                    continue;
                }
                if (DEBUG>=3) System.out.println("remainder: " + unifications.size() + " unifications");

                for (FeatureTerm u : unifications) {
                    if (u.equivalents(f)) {
                        remainder = r;
                        break;
                    }
                }

                if (remainder != null) break;
            }
        } while (remainder != null);

//		System.out.println("f: ");
//		System.out.println(f.toStringNOOS(dm));
//		System.out.println("refinement: ");
//		System.out.println(refinement.toStringNOOS(dm));
//		System.out.println("Remainder: ");
//		System.out.println(oldRemainder.toStringNOOS(dm));

        return oldRemainder;
    }

    

    /*
     * This method computes the remainder also in an exact way, but avoiding any call to unification. It is faster.
     */
    public static FeatureTerm remainderFast(FeatureTerm f, FeatureTerm refinement, FTKBase dm, Ontology o) throws FeatureTermException {
        FeatureTerm oldRemainder = null;
        FeatureTerm remainder = f;

        // If any of these terms are candidate unifications, then the property cannot recover the original term:
        List<FeatureTerm> originalGeneralizations = FTRefinement.getGeneralizationsAggressive(f, dm, o);

        do {
            oldRemainder = remainder;
            if (DEBUG>=3) {
                System.out.println("remainder: cycle starts");
                System.out.println("refinement: ");
                System.out.println(remainder.toStringNOOS(dm));
            }
            List<FeatureTerm> refinements = FTRefinement.getGeneralizationsAggressive(remainder, dm, o);
            // if (DEBUG>=3)
            System.out.println("remainder: " + refinements.size() + " refinements.");

            remainder = null;
            for (FeatureTerm r : refinements) {
                boolean canRecover = true;

                // Sanity test:
/*                if (!(refinement.subsumes(f) && r.subsumes(f))) {
                    System.err.println("remainderFast: sanity check failed!");
                    System.exit(1);
                }
*/
                canRecover = true;
                for(FeatureTerm candidate:originalGeneralizations) {
                    if (refinement.subsumes(candidate) && r.subsumes(candidate)) {
                        canRecover = false;
                    }
                }

                if (canRecover) {
                    remainder = r;
                    break;
                }
            }
        } while (remainder != null);

//		System.out.println("f: ");
//		System.out.println(f.toStringNOOS(dm));
//		System.out.println("refinement: ");
//		System.out.println(refinement.toStringNOOS(dm));
//		System.out.println("Remainder: ");
//		System.out.println(oldRemainder.toStringNOOS(dm));

        return oldRemainder;
    }

    // This method assumes that 'refinement' is a direct generlization refinement of 'f', it's even faster:
    public static FeatureTerm remainderFastAssumingRefinement(FeatureTerm f, FeatureTerm refinement, FTKBase dm, Ontology o) throws FeatureTermException {
        FeatureTerm oldRemainder = null;
        FeatureTerm remainder = f;

        do {
            oldRemainder = remainder;
            if (DEBUG>=3) {
                System.out.println("remainderFastAssumingRefinement: cycle starts");
                System.out.println("refinement: ");
                System.out.println(remainder.toStringNOOS(dm));
            }
            List<FeatureTerm> refinements = FTRefinement.getGeneralizationsAggressive(remainder, dm, o);
            // if (DEBUG>=3)
//            System.out.println("remainder: " + refinements.size() + " refinements.");

            remainder = null;
            for (FeatureTerm r : refinements) {
//                System.out.print(".");
                if (!r.subsumes(refinement)) {
                    remainder = r;
                    break;
                }
            }
        } while (remainder != null);

//		System.out.println("f: ");
//		System.out.println(f.toStringNOOS(dm));
//		System.out.println("refinement: ");
//		System.out.println(refinement.toStringNOOS(dm));
//		System.out.println("Remainder: ");
//		System.out.println(oldRemainder.toStringNOOS(dm));

        return oldRemainder;
    }


    public static List<FeatureTerm> disintegrateFast(FeatureTerm f, FTKBase dm, Ontology o) throws FeatureTermException {
        List<FeatureTerm> properties = null;
        Pair<FeatureTerm, FeatureTerm> property_rest;

        // First check cache:
        properties = propertiesFastTable.get(f);
        if (properties!=null) return properties;
        properties = new LinkedList<FeatureTerm>();

        FeatureTerm unnamed = f.clone(dm, o);
        for(FeatureTerm f2:FTRefinement.variables(unnamed)) {
            if (!dm.contains(f2)) f2.setName(null);
        }

        do {
            property_rest = extractPropertyFast(unnamed, dm, o);

            if (property_rest != null) {
                properties.add(property_rest.m_a);
                unnamed = property_rest.m_b;

//				System.out.println("-------------------- " + properties.size());
//				System.out.println(property_rest.m_a.toStringNOOS(dm));
//				System.out.println(f.toStringNOOS(dm));
            }
        } while (property_rest != null);

//		System.out.println("--------------------");
//		System.out.println(unnamed.toStringNOOS(dm));

        propertiesFastTable.put(f,properties);
        return properties;
    }

    /*
     * This method takes a feature term and generates a property and generalizes the term to remove that property from it
     * The unification of the property and the generalized term should result exactly in the original term
     */
    public static Pair<FeatureTerm, FeatureTerm> extractPropertyFast(FeatureTerm f, FTKBase dm, Ontology o) throws FeatureTermException {
        HashMap<FeatureTerm, AnnotatedPath> vp = FTRefinement.variablesWithAnnotatedPaths(f);
        HashMap<FeatureTerm, List<Pair<TermFeatureTerm, Symbol>>> vap = FTRefinement.variablesWithAllParents(f);

        // Check for a sort property:
        for (FeatureTerm X : vap.keySet()) {
            if (!X.isConstant() && !dm.contains(X)) {
                Sort most_general = o.getSort("any");
                Sort to_generalize_to = X.getSort().getSuper();

                if (to_generalize_to != null) {
                    for (Pair<TermFeatureTerm, Symbol> parent : vap.get(X)) {
                        if (parent != null) {
                            Sort s = parent.m_a.getSort().featureSort(parent.m_b);
                            if (most_general.isSubsort(s)) {
                                most_general = s;
                            }
                        }
                    }

                    if (to_generalize_to.is_a(most_general)) {
                        boolean canGeneralize = true;

                        if (X instanceof TermFeatureTerm) {
                            for (Symbol fn : ((TermFeatureTerm) X).getFeatureNames()) {
                                if (!to_generalize_to.hasFeature(fn)) {
                                    canGeneralize = false;
                                    break;
                                }
                            }
                        }

                        if (canGeneralize) {
                            // Build Property:
                            FeatureTerm property = null;
                            {
                                AnnotatedPath ap = vp.get(X);
                                FeatureTerm lastNode = null, tmp;
                                Symbol lastFeature = null;

                                // Reconstruct the path:
                                for (Pair<FeatureTerm, Symbol> n_f : ap.features) {
                                    if (lastNode == null) {
                                        Sort s = n_f.m_a.getSort();
                                        while (s.getSuper().hasFeature(n_f.m_b)) {
                                            s = s.getSuper();
                                        }

                                        property = lastNode = s.createFeatureTerm();
                                    } else {
                                        Sort s = n_f.m_a.getSort();
                                        while (s.getSuper().hasFeature(n_f.m_b)) {
                                            s = s.getSuper();
                                        }

                                        tmp = s.createFeatureTerm();
                                        ((TermFeatureTerm) lastNode).defineFeatureValue(lastFeature, tmp);
                                        lastNode = tmp;
                                    }
                                    lastFeature = n_f.m_b;
                                }

                                if (lastNode == null) {
                                    property = lastNode = X.getSort().createFeatureTerm();
                                } else {
                                    tmp = X.getSort().createFeatureTerm();
                                    ((TermFeatureTerm) lastNode).defineFeatureValue(lastFeature, tmp);
                                }
                            }


                            // Generalize:
                            FeatureTerm generalization = FTRefinement.sortSubstitution(f, X, to_generalize_to, dm);

                            return new Pair<FeatureTerm, FeatureTerm>(property, generalization);
                        }
                    }
                }
            }
        }

        // Otherwise check for a constant property:
        {
            for (FeatureTerm node : vap.keySet()) {
                if (node.isConstant() || dm.contains(node)) {
                    List<Pair<TermFeatureTerm, Symbol>> parents = vap.get(node);

                    for (Pair<TermFeatureTerm, Symbol> p_f : parents) {
                        FeatureTerm container = p_f.m_a.featureValue(p_f.m_b);

                        FeatureTerm property = null;
                        {
                            AnnotatedPath ap = vp.get(node);
                            FeatureTerm lastNode = null, tmp;
                            Symbol lastFeature = null;

                            // Reconstruct the path:
                            for (Pair<FeatureTerm, Symbol> n_f : ap.features) {
                                if (lastNode == null) {
                                    Sort s = n_f.m_a.getSort();
                                    while (s.getSuper().hasFeature(n_f.m_b)) {
                                        s = s.getSuper();
                                    }

                                    property = lastNode = s.createFeatureTerm();
                                } else {
                                    Sort s = n_f.m_a.getSort();
                                    while (s.getSuper().hasFeature(n_f.m_b)) {
                                        s = s.getSuper();
                                    }

                                    tmp = s.createFeatureTerm();
                                    ((TermFeatureTerm) lastNode).defineFeatureValue(lastFeature, tmp);
                                    lastNode = tmp;
                                }
                                lastFeature = n_f.m_b;
                            }

                            if (lastNode == null) {
                                property = lastNode = node;
                            } else {
                                ((TermFeatureTerm) lastNode).defineFeatureValue(lastFeature, node);
                            }
                        }

                        if (container.equals(node)) {
                            HashMap<FeatureTerm, FeatureTerm> correspondences = new HashMap<FeatureTerm, FeatureTerm>();
                            FeatureTerm clone = f.clone(dm, correspondences);
                            ((TermFeatureTerm) correspondences.get(p_f.m_a)).defineFeatureValue(p_f.m_b, node.getSort().createFeatureTerm());

/*
                            if (clone.equivalents(f)) {
                                System.err.println("ERROR 2!!!");
                                System.err.println(f.toStringNOOS(dm));
                                System.err.println(clone.toStringNOOS(dm));
                                System.err.println(property.toStringNOOS(dm));
                                System.exit(1);
                            }
*/
                            return new Pair<FeatureTerm, FeatureTerm>(property, clone);
                        } else {
                            int l = ((SetFeatureTerm) container).getSetValues().size();
                            for (int i = 0; i < l; i++) {
                                FeatureTerm ft2 = ((SetFeatureTerm) container).getSetValues().get(i);
                                if (ft2.equals(node)) {
                                    HashMap<FeatureTerm, FeatureTerm> correspondences = new HashMap<FeatureTerm, FeatureTerm>();
                                    FeatureTerm clone = f.clone(dm, correspondences);
                                    ((SetFeatureTerm) correspondences.get(container)).substituteSetValue(i, ft2.getSort().createFeatureTerm());

                                    return new Pair<FeatureTerm, FeatureTerm>(property, clone);
                                }
                            } // for
                        }
                    }
                }
            }
        }

        // Otherwise check for a set property:
        {
            HashMap<SetFeatureTerm, Set<Pair<TermFeatureTerm, Symbol>>> sp = FTRefinement.setsWithAllParents(f);
            for (SetFeatureTerm S : sp.keySet()) {
                Sort most_general = o.getSort("any");

                for (Pair<TermFeatureTerm, Symbol> parent : sp.get(S)) {
                    Sort s = parent.m_a.getSort().featureSort(parent.m_b);
                    if (most_general.isSubsort(s)) {
                        most_general = s;
                    }
                }

                for (FeatureTerm X : S.getSetValues()) {
                    if (X.getSort() == most_general && X.isLeaf()) {
                        HashMap<FeatureTerm, FeatureTerm> correspondences = new HashMap<FeatureTerm, FeatureTerm>();
                        FeatureTerm clone = f.clone(dm, correspondences);

                        if (S.getSetValues().size() == 2) {
                            SetFeatureTerm setClone = (SetFeatureTerm) correspondences.get(S);
                            setClone.removeSetValue(correspondences.get(X));
                            FeatureTerm toRemoveClone = setClone.getSetValues().get(0);
                            clone.substitute(correspondences.get(S), toRemoveClone);
                        } else {
                            FeatureTerm Xclone = correspondences.get(X);
                            if (Xclone != null) {
                                ((SetFeatureTerm) correspondences.get(S)).removeSetValue(Xclone);
                            } else {
                                // X is in the domain model
                                ((SetFeatureTerm) correspondences.get(S)).removeSetValue(X);
                            }
                        }

                        FeatureTerm property = null;
                        {
                            AnnotatedPath ap = vp.get(X);
                            FeatureTerm lastNode = null, tmp;
                            Symbol lastFeature = null;

                            // Reconstruct the path:
                            for (Pair<FeatureTerm, Symbol> n_f : ap.features) {
                                if (lastNode == null) {
                                    Sort s = n_f.m_a.getSort();
                                    while (s.getSuper().hasFeature(n_f.m_b)) {
                                        s = s.getSuper();
                                    }

                                    property = lastNode = s.createFeatureTerm();
                                } else {
                                    Sort s = n_f.m_a.getSort();
                                    while (s.getSuper().hasFeature(n_f.m_b)) {
                                        s = s.getSuper();
                                    }

                                    tmp = s.createFeatureTerm();
                                    ((TermFeatureTerm) lastNode).defineFeatureValue(lastFeature, tmp);
                                    lastNode = tmp;
                                }
                                lastFeature = n_f.m_b;
                            }

                            tmp = new SetFeatureTerm();
                            for (int i = 0; i < S.getSetValues().size(); i++) {
                                ((SetFeatureTerm) tmp).addSetValue(most_general.createFeatureTerm());
                            }

                            if (lastNode == null) {
                                property = lastNode = tmp;
                            } else {
                                ((TermFeatureTerm) lastNode).defineFeatureValue(lastFeature, tmp);
                            }
                        }

                        return new Pair<FeatureTerm, FeatureTerm>(property, clone);
                    }
                }
            }

        }

        // Otherwise check for a feature property:
        for (FeatureTerm X : vap.keySet()) {
            if (X instanceof TermFeatureTerm) {
                TermFeatureTerm TX = (TermFeatureTerm) X;

                for (Symbol fname : TX.getFeatureNames()) {
                    FeatureTerm fvalue = TX.featureValue(fname);

                    if (fvalue.getSort() == X.getSort().featureSort(fname) && fvalue.isLeaf() && !fvalue.isConstant() && !dm.contains(fvalue)) {
                        FeatureTerm property = null;

                        HashMap<FeatureTerm, FeatureTerm> correspondences = new HashMap<FeatureTerm, FeatureTerm>();
                        FeatureTerm clone = f.clone(dm, correspondences);

                        TermFeatureTerm f1 = (TermFeatureTerm) correspondences.get(X);
                        f1.removeFeatureValue(fname);

                        {
                            AnnotatedPath ap = vp.get(fvalue);
                            FeatureTerm lastNode = null, tmp;
                            Symbol lastFeature = null;

                            // Reconstruct the path:
                            for (Pair<FeatureTerm, Symbol> n_f : ap.features) {
                                if (lastNode == null) {
                                    Sort s = n_f.m_a.getSort();
                                    while (s.getSuper().hasFeature(n_f.m_b)) {
                                        s = s.getSuper();
                                    }

                                    property = lastNode = s.createFeatureTerm();
                                } else {
                                    Sort s = n_f.m_a.getSort();
                                    while (s.getSuper().hasFeature(n_f.m_b)) {
                                        s = s.getSuper();
                                    }

                                    tmp = s.createFeatureTerm();
                                    ((TermFeatureTerm) lastNode).defineFeatureValue(lastFeature, tmp);
                                    lastNode = tmp;
                                }
                                lastFeature = n_f.m_b;
                            }


                            if (lastNode == null) {
                                property = lastNode = fvalue.getSort().createFeatureTerm();
                            } else {
                                ((TermFeatureTerm) lastNode).defineFeatureValue(lastFeature, fvalue.getSort().createFeatureTerm());
                            }
                        }

                        return new Pair<FeatureTerm, FeatureTerm>(property, clone);
                    }
                }
            }
        }

        // Otherwise check for a variable equality property:
        {
            for (FeatureTerm X : vap.keySet()) {
                List<Pair<TermFeatureTerm, Symbol>> parents = vap.get(X);
                /*
                System.out.println("Parents of X:");
                for(Pair<TermFeatureTerm,Symbol> parent:parents) {
                if (parent==null) {
                System.out.println("null");
                } else {
                System.out.println(parent.m_a.getSort().get() + "." + parent.m_b.get());
                }
                }
                 */
                if (parents.size() >= 2) {
                    FeatureTerm property = null;
                    Pair<TermFeatureTerm, Symbol> parent1 = null;
                    Pair<TermFeatureTerm, Symbol> parent2 = null;
                    boolean first = true;

                    // Get 2 parents of the set:
                    for (Pair<TermFeatureTerm, Symbol> parent : parents) {
                        if (first) {
                            parent1 = parent;
                            first = false;
                        } else {
                            parent2 = parent;
                            break;
                        }
                    }

                    if (parent2 == null) {
                        parent2 = parent1;
                        parent1 = null;
                    }
                    /*
                    if (parent1==null) {
                    System.out.println("parent1: null");
                    } else {
                    System.out.println("parent1: " + parent1.m_a.getSort().get() + "." + parent1.m_b.get());
                    }
                    if (parent2==null) {
                    System.out.println("parent2: null");
                    } else {
                    System.out.println("parent2: " + parent2.m_a.getSort().get() + "." + parent2.m_b.get());
                    }
                     */

                    HashMap<FeatureTerm, FeatureTerm> correspondences = new HashMap<FeatureTerm, FeatureTerm>();
                    FeatureTerm clone = f.clone(dm, correspondences);
                    if (parent1 == null) {
                        FeatureTerm ft2Clone = X.getSort().createFeatureTerm();
                        ((TermFeatureTerm) (correspondences.get(parent2.m_a))).defineFeatureValue(parent2.m_b, ft2Clone);
                    } else {
                        FeatureTerm ft2Clone = X.getSort().createFeatureTerm();
                        ((TermFeatureTerm) (correspondences.get(parent1.m_a))).defineFeatureValue(parent1.m_b, ft2Clone);
                    }

                    // Create property:
                    {
                        AnnotatedPath ap1 = (parent1 == null ? new AnnotatedPath() : vp.get(parent1.m_a));
                        AnnotatedPath ap2 = vp.get(parent2.m_a);

                        FeatureTerm lastNode1 = null, lastNode2 = null, tmp, trail = null;
                        Symbol lastFeature1 = null, lastFeature2 = null;

                        // Reconstruct the first path:
                        for (Pair<FeatureTerm, Symbol> n_f : ap1.features) {
                            if (lastNode1 == null) {
                                Sort s = n_f.m_a.getSort();
                                while (s.getSuper().hasFeature(n_f.m_b)) {
                                    s = s.getSuper();
                                }

                                property = lastNode1 = s.createFeatureTerm();
                            } else {
                                Sort s = n_f.m_a.getSort();
                                while (s.getSuper().hasFeature(n_f.m_b)) {
                                    s = s.getSuper();
                                }

                                tmp = s.createFeatureTerm();
                                ((TermFeatureTerm) lastNode1).defineFeatureValue(lastFeature1, tmp);
                                lastNode1 = tmp;
                            }
                            lastFeature1 = n_f.m_b;
                        }

                        // Reconstruct the second path:
                        trail = property;
                        for (Pair<FeatureTerm, Symbol> n_f : ap2.features) {
                            if (lastNode2 == null) {
                                Sort s = n_f.m_a.getSort();
                                while (s.getSuper().hasFeature(n_f.m_b)) {
                                    s = s.getSuper();
                                }

                                if (trail == null) {
                                    property = lastNode2 = s.createFeatureTerm();
                                } else {
                                    if (!s.inSort(trail)) {
                                        trail.setSort(s);
                                    }
                                    lastNode2 = trail;
                                    trail = trail.featureValue(n_f.m_b);
                                }
                            } else {
                                Sort s = n_f.m_a.getSort();
                                while (s.getSuper().hasFeature(n_f.m_b)) {
                                    s = s.getSuper();
                                }

                                if (trail == null) {
                                    tmp = s.createFeatureTerm();
                                    ((TermFeatureTerm) lastNode2).defineFeatureValue(lastFeature2, tmp);
                                    lastNode2 = tmp;
                                } else {
                                    if (!s.inSort(trail)) {
                                        trail.setSort(s);
                                    }
                                    lastNode2 = trail;
                                    trail = trail.featureValue(n_f.m_b);
                                }
                            }
                            lastFeature2 = n_f.m_b;
                        }

                        // Add the parents and the common node
                        {
                            if (lastNode1 == null) {
                                if (parent1 == null) {
                                    // the variable equality refers to the root
                                } else {
                                    Sort s = parent1.m_a.getSort();
                                    while (s.getSuper().hasFeature(parent1.m_b)) {
                                        s = s.getSuper();
                                    }

                                    if (property == null) {
                                        // both paths refer to the root
                                        lastNode1 = property = s.createFeatureTerm();
                                    } else {
                                        if (!s.inSort(property)) {
                                            property.setSort(s);
                                        }
                                        lastNode1 = property;
                                    }
                                }
                            } else {
                                Sort s = parent1.m_a.getSort();
                                while (s.getSuper().hasFeature(parent1.m_b)) {
                                    s = s.getSuper();
                                }

                                tmp = lastNode1.featureValue(lastFeature1);
                                if (tmp == null) {
                                    tmp = s.createFeatureTerm();
                                    ((TermFeatureTerm) lastNode1).defineFeatureValue(lastFeature1, tmp);
                                    lastNode1 = tmp;
                                } else {
                                    if (!s.inSort(tmp)) {
                                        tmp.setSort(s);
                                    }
                                    lastNode1 = tmp;
                                }
                            }

                            if (lastNode2 == null) {
                                Sort s = parent2.m_a.getSort();
                                while (s.getSuper().hasFeature(parent2.m_b)) {
                                    s = s.getSuper();
                                }

                                if (property == null) {
                                    // both paths refer to the root
                                    lastNode2 = property = s.createFeatureTerm();
                                } else {
                                    if (!s.inSort(property)) {
                                        property.setSort(s);
                                    }
                                    lastNode2 = property;
                                }
                            } else {
                                Sort s = parent2.m_a.getSort();
                                while (s.getSuper().hasFeature(parent2.m_b)) {
                                    s = s.getSuper();
                                }

                                tmp = lastNode2.featureValue(lastFeature2);
                                if (tmp == null) {
                                    tmp = s.createFeatureTerm();
                                    ((TermFeatureTerm) lastNode2).defineFeatureValue(lastFeature2, tmp);
                                    lastNode2 = tmp;
                                } else {
                                    if (!s.inSort(tmp)) {
                                        tmp.setSort(s);
                                    }
                                    lastNode2 = tmp;
                                }
                            }
                        }

//						System.out.println(" -------------------------- ");
//						System.out.println(ap1 + " -> " + parent1.m_b);
//						System.out.println(ap2 + " -> " + parent2.m_b);
//						System.out.println(property.toStringNOOS(dm));

                        // Add the common term:
                        {
                            Sort s = X.getSort();
                            FeatureTerm common = null;

                            if (parent1 == null) {
                                // parent1 is the root
                                ((TermFeatureTerm) lastNode2).defineFeatureValue(parent2.m_b, property);
                            } else {
                                common = lastNode1.featureValue(parent1.m_b);
                                if (common == null) {
                                    common = lastNode2.featureValue(parent2.m_b);
                                    if (common == null) {
                                        common = s.createFeatureTerm();
                                    }
                                }

                                tmp = lastNode1.featureValue(parent1.m_b);
                                if (tmp == null) {
                                    ((TermFeatureTerm) lastNode1).defineFeatureValue(parent1.m_b, common);
                                } else {
                                    common = tmp;
                                    if (!s.inSort(common)) {
                                        tmp.setSort(s);
                                    }
                                }

                                tmp = lastNode2.featureValue(parent2.m_b);
                                if (tmp == null) {
                                    ((TermFeatureTerm) lastNode2).defineFeatureValue(parent2.m_b, common);
                                } else {
                                    if (!s.inSort(common)) {
                                        tmp.setSort(s);
                                    }
                                }
                            }
                        }

//						System.out.println(property.toStringNOOS(dm));

                    }

                    return new Pair<FeatureTerm, FeatureTerm>(property, clone);
                }
            }
        }

        return null;
    }
}
