package fterms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import util.Pair;
import fterms.exceptions.FeatureTermException;
import fterms.exceptions.SingletonFeatureTermException;
import java.util.LinkedHashMap;
import java.util.Random;
import util.PowerSet;

public class FTRefinement {

    public static final int SORT_REFINEMENTS = 1;
    public static final int FEATURE_REFINEMENTS = 2;
    public static final int EQUALITY_REFINEMENTS = 4;
    public static final int SET_REFINEMENTS = 8;
    public static final int CONSTANT_REFINEMENTS = 16;
    public static final int ALL_REFINEMENTS = SORT_REFINEMENTS | FEATURE_REFINEMENTS | EQUALITY_REFINEMENTS | SET_REFINEMENTS | CONSTANT_REFINEMENTS;
    public static final int NO_EQUALITIES = SORT_REFINEMENTS | FEATURE_REFINEMENTS | SET_REFINEMENTS | CONSTANT_REFINEMENTS;

    public static List<FeatureTerm> getSpecializations(FeatureTerm f, FTKBase dm, int flags) throws FeatureTermException {
        List<Pair<FeatureTerm, Path>> vp = variablesWithPaths(f, dm);
        List<FeatureTerm> refinements = new LinkedList<FeatureTerm>();

        if ((flags & SORT_REFINEMENTS) != 0) {
            refinements.addAll(sortSpecialization(f, dm, vp));
        }
        if ((flags & FEATURE_REFINEMENTS) != 0) {
            refinements.addAll(featureIntroduction(f, dm, vp));
        }
        if ((flags & EQUALITY_REFINEMENTS) != 0) {
            refinements.addAll(variableEqualityAddition(f, dm, vp));
        }
        if ((flags & SET_REFINEMENTS) != 0) {
            refinements.addAll(setExpansion(f, dm, vp));
        }
        /*
        // If no set of objects is supplied, this operators might return an infinite number of refinements, so, it's not supported
        if ((flags&CONSTANT_REFINEMENTS)!=0) refinements.addAll(substitutionByConstant(f,dm));
         */
        return refinements;
    }

    public static List<FeatureTerm> getSpecializationsSubsumingAll(FeatureTerm f, FTKBase dm, Ontology o, int flags, List<FeatureTerm> objects) throws FeatureTermException {
        List<Pair<FeatureTerm, Path>> vp = variablesWithPaths(f, dm);
        List<FeatureTerm> refinements = new LinkedList<FeatureTerm>();

        if ((flags & EQUALITY_REFINEMENTS) != 0) {
            refinements.addAll(variableEqualityAdditionSubsumingAll(f, dm, vp, objects));
        }
        if ((flags & SORT_REFINEMENTS) != 0) {
            refinements.addAll(sortSpecialization(f, dm, vp));
        }
        if ((flags & FEATURE_REFINEMENTS) != 0) {
            refinements.addAll(featureIntroductionSubsumingAll(f, dm, vp, objects));
        }
        if ((flags & SET_REFINEMENTS) != 0) {
            refinements.addAll(setExpansionSubsumingAll(f, dm, vp, objects));
        }
        if ((flags & CONSTANT_REFINEMENTS) != 0) {
            refinements.addAll(substitutionByConstantSubsumingAll(f, dm, o, vp, objects));
        }

        {
            List<FeatureTerm> result = new LinkedList<FeatureTerm>();

            for (FeatureTerm r : refinements) {
                boolean candidate = true;
                for (FeatureTerm obj : objects) {
                    if (!r.subsumes(obj)) {
                        candidate = false;
                        break;
                    }
                }
                if (candidate) {
                    result.add(r);
                }
            }

            return result;
        }
    }

    public static List<FeatureTerm> getSomeSpecializationSubsumingAll(FeatureTerm f, FTKBase dm, Ontology o, int flags, List<FeatureTerm> objects) throws FeatureTermException {
        List<Pair<FeatureTerm, Path>> vp = variablesWithPaths(f, dm);
        List<FeatureTerm> refinements = null;
        List<FeatureTerm> result = new LinkedList<FeatureTerm>();

        for (int i = 0; i < 6; i++) {
            if (i == 0 && (flags & EQUALITY_REFINEMENTS) != 0) {
                refinements = variableEqualityAdditionSubsumingAll(f, dm, vp, objects);
            }
            if (i == 1 && (flags & SORT_REFINEMENTS) != 0) {
                refinements = sortSpecialization(f, dm, vp);
            }
            if (i == 4 && (flags & FEATURE_REFINEMENTS) != 0) {
                refinements = featureIntroductionSubsumingAll(f, dm, vp, objects);
            }
            if (i == 3 && (flags & SET_REFINEMENTS) != 0) {
                refinements = setExpansionSubsumingAll(f, dm, vp, objects);
            }
            if (i == 2 && (flags & CONSTANT_REFINEMENTS) != 0) {
                refinements = substitutionByConstantSubsumingAll(f, dm, o, vp, objects);
            }

// 			System.out.println("getSomeSpecializationSubsumingAll: " + i + " -> " + refinements.size());

            if (refinements!=null) {
                for (FeatureTerm r : refinements) {
                    boolean candidate = true;
                    for (FeatureTerm obj : objects) {
                        if (!r.subsumes(obj)) {
                            candidate = false;
                            break;
                        }
                    }

//					System.out.println(r.toStringNOOS(dm));
//					System.out.println(candidate);

                    if (candidate) {
//                        if (!f.subsumes(r)) {
//                            System.err.println("ops in " + i);
//                            System.err.println("Original:\n" + f.toStringNOOS(dm));
//                            System.err.println("Refinement:\n" + r.toStringNOOS(dm));
//                        }
                        result.add(r);
                    }
                }

                if (!result.isEmpty()) {
//					System.out.println(i);
                    return result;
                }
            }
        }
//		System.out.println("-");
        return result;
    }

    public static List<FeatureTerm> getSpecializationsSubsumingSome(FeatureTerm f, FTKBase dm, Ontology o, int flags, List<FeatureTerm> objects) throws FeatureTermException {
        List<Pair<FeatureTerm, Path>> vp = variablesWithPaths(f, dm);
        List<FeatureTerm> refinements = new LinkedList<FeatureTerm>();

        if ((flags & SORT_REFINEMENTS) != 0) {
            refinements.addAll(sortSpecialization(f, dm, vp));
        }
//		System.out.println("[" + refinements.size() + "]");
        if ((flags & FEATURE_REFINEMENTS) != 0) {
            refinements.addAll(featureIntroduction(f, dm, vp));
        }
//		System.out.println("[" + refinements.size() + "]");
        if ((flags & EQUALITY_REFINEMENTS) != 0) {
            refinements.addAll(variableEqualityAddition(f, dm, vp));
        }
//		System.out.println("[" + refinements.size() + "]");
        if ((flags & SET_REFINEMENTS) != 0) {
            refinements.addAll(setExpansion(f, dm, vp));
        }
//		System.out.println("[" + refinements.size() + "]");
        if ((flags & CONSTANT_REFINEMENTS) != 0) {
            refinements.addAll(substitutionByConstantSubsumingSome(f, dm, o, vp, objects));
        }
//		System.out.println("[" + refinements.size() + "]");

        {
            List<FeatureTerm> result = new LinkedList<FeatureTerm>();

            for (FeatureTerm r : refinements) {
                boolean candidate = false;
                for (FeatureTerm obj : objects) {
                    if (r.subsumes(obj)) {
                        candidate = true;
                        break;
                    }
                }
                if (candidate) {
//					if (r.subsumes(f)) {
//						System.out.println(result.size() + " does not advance... (" + refinements.indexOf(r) + ")");
//					}
                    result.add(r);
                }
            }

//			if (objects.size()==1 && refinements.size()==0 && !objects.get(0).subsumes(f)) {
//				System.err.println("There are some missing refinements here...");
//			} else {
//				System.out.println("Stats: " + objects.size() + " " + refinements.size() + " " + result.size() + " " + objects.get(0).subsumes(f));				
//			}

            return result;
        }
    }

    public static List<FeatureTerm> getGeneralizations(FeatureTerm f, FTKBase dm, Ontology o) throws FeatureTermException {
        List<Pair<FeatureTerm, Path>> vp = variablesWithPaths(f, dm);
        List<FeatureTerm> refinements = new LinkedList<FeatureTerm>();

        refinements.addAll(sortGeneralization(f, dm, o));
//		System.out.println("[" + refinements.size() + "]");
        refinements.addAll(featureElimination(f, dm, vp));
//		System.out.println("[" + refinements.size() + "]");
        refinements.addAll(variableEqualityElimination(f, dm, vp));
//		System.out.println("[" + refinements.size() + "]");
        refinements.addAll(setReduction(f, dm, o));
//		System.out.println("[" + refinements.size() + "]");
        refinements.addAll(ConstantGeneralization(f, dm));
//		System.out.println("[" + refinements.size() + "]");

//		for(FeatureTerm ft:refinements) {
//			if (f.subsumes(ft)) {
//				System.out.println(refinements.indexOf(ft) + " does not advance...");
//			}
//		}
/*
        // debug statement:
        {
        int i = 0;
        for(FeatureTerm r:refinements) {
        if (r.toStringNOOS(dm).equals(f.toStringNOOS(dm))) {
        System.err.println("ERROR in " + i + "th refinement!!!");

        System.err.println("sorts: " + sortGeneralization(f,dm,o).size());
        System.err.println("feature: " + featureElimination(f,dm,vp).size());
        System.err.println("variable: " + variableEqualityElimination(f,dm,vp).size());
        System.err.println("set: " + setReduction(f,dm,o).size());
        System.err.println("constant: " + ConstantGeneralization(f,dm).size());
        }
        i++;
        }

        }
         */
        return refinements;
    }

    public static List<FeatureTerm> getGeneralizationsAggressive(FeatureTerm f, FTKBase dm, Ontology o) throws FeatureTermException {
        List<Pair<FeatureTerm, Path>> vp = variablesWithPaths(f, dm);
        List<FeatureTerm> refinements = new LinkedList<FeatureTerm>();

        refinements.addAll(sortGeneralization(f, dm, o));
//		System.out.println("[" + refinements.size() + "]");
        refinements.addAll(featureElimination(f, dm, vp));
//		System.out.println("[" + refinements.size() + "]");
        refinements.addAll(setReduction(f, dm, o));
//		System.out.println("[" + refinements.size() + "]");
        refinements.addAll(ConstantGeneralization(f, dm));
//		System.out.println("[" + refinements.size() + "]");
        refinements.addAll(variableEqualityEliminationAggressive(f, dm));
//		System.out.println("[" + refinements.size() + "]");

//		for(FeatureTerm ft:refinements) {
//			if (f.subsumes(ft)) {
//				System.out.println(refinements.indexOf(ft) + " does not advance...");
//			}
//		}
/*
        // debug statement:
        {
        int i = 0;
        for(FeatureTerm r:refinements) {
        if (r.toStringNOOS(dm).equals(f.toStringNOOS(dm))) {
        System.err.println("ERROR in " + i + "th refinement!!!");

        System.err.println("sorts: " + sortGeneralization(f,dm,o).size());
        System.err.println("feature: " + featureElimination(f,dm,vp).size());
        System.err.println("variable: " + variableEqualityElimination(f,dm,vp).size());
        System.err.println("set: " + setReduction(f,dm,o).size());
        System.err.println("constant: " + ConstantGeneralization(f,dm).size());
        }
        i++;
        }

        }
         */
        return refinements;
    }

    /*
     * If there is any loop in the variable equalities, this function will never manage to reach the (any) term, since the chain to
     * reach it is infinite: there are infinite terms in between (any) and any term with a loop
     */
    public static List<FeatureTerm> getSomeGeneralizations(FeatureTerm f, FTKBase dm, Ontology o) throws FeatureTermException {
        List<Pair<FeatureTerm, Path>> vp = variablesWithPaths(f, dm);
        List<FeatureTerm> refinements = null;

        for (int i = 0; i < 5; i++) {
            if (i == 0) {
                refinements = sortGeneralization(f, dm, o);
            }
            if (i == 1) {
                refinements = featureElimination(f, dm, vp);
            }
            if (i == 2) {
                refinements = setReduction(f, dm, o);
            }
            if (i == 3) {
                refinements = ConstantGeneralization(f, dm);
            }
            if (i == 4) {
                refinements = variableEqualityElimination(f, dm, vp);
            }

            if (!refinements.isEmpty()) {
                return refinements;
            }
        } // for

        return new LinkedList<FeatureTerm>();
    }

    /*
     * This method is like "getSomeGeneralizations", but uses the aggresive version of the removal of variable equalities
     */
    public static List<FeatureTerm> getSomeGeneralizationsAggressive(FeatureTerm f, FTKBase dm, Ontology o) throws FeatureTermException {
        List<Pair<FeatureTerm, Path>> vp = variablesWithPaths(f, dm);
        List<FeatureTerm> refinements = null;

        for (int i = 0; i < 5; i++) {
            if (i == 0) refinements = sortGeneralization(f, dm, o);
            if (i == 1) refinements = featureElimination(f, dm, vp);
            if (i == 2) refinements = setReduction(f, dm, o);
            if (i == 3) refinements = ConstantGeneralization(f, dm);
            if (i == 4) refinements = variableEqualityEliminationAggressive(f, dm);

            if (!refinements.isEmpty()) {
                return refinements;
            }
        } // for

        return new LinkedList<FeatureTerm>();
    }


    public static List<FeatureTerm> getSomeRandomGeneralizationsAggressive(FeatureTerm f, FTKBase dm, Ontology o) throws FeatureTermException {
        List<Pair<FeatureTerm, Path>> vp = variablesWithPaths(f, dm);
        List<FeatureTerm> refinements = null;
        List<Integer> left = new LinkedList<Integer>();
        left.add(0);
        left.add(1);
        left.add(2);
        left.add(3);
        left.add(4);
        Random r = new Random();

        while(!left.isEmpty()) {
            int i = left.remove(r.nextInt(left.size()));
            if (i == 0) refinements = sortGeneralization(f, dm, o);
            if (i == 1) refinements = featureElimination(f, dm, vp);
            if (i == 2) refinements = setReduction(f, dm, o);
            if (i == 3) refinements = ConstantGeneralization(f, dm);
            if (i == 4) refinements = variableEqualityEliminationAggressive(f, dm);

            if (!refinements.isEmpty()) {
                return refinements;
            }
        } // for

        return new LinkedList<FeatureTerm>();
    }

    public static List<FeatureTerm> ConstantGeneralization(FeatureTerm f, FTKBase dm) throws FeatureTermException {
        List<FeatureTerm> refinements = new LinkedList<FeatureTerm>();

        HashSet<FeatureTerm> visited = new HashSet<FeatureTerm>();
        List<FeatureTerm> open_nodes = new LinkedList<FeatureTerm>();
        FeatureTerm node;

        visited.add(f);
        open_nodes.add(f);

        while (!open_nodes.isEmpty()) {
            node = open_nodes.remove(0);

            if (node.getDataType() == Sort.DATATYPE_FEATURETERM) {
//				int l = ((TermFeatureTerm)node).getFeatureNames().size();
//				for(int i = 0;i<l;i++) {
//					FeatureTerm ft2 = ((TermFeatureTerm)node).getFeatureValues().get(i); 
//					Symbol fname = ((TermFeatureTerm)node).getFeatureNames().get(i); 
                for (Entry<Symbol, FeatureTerm> feature : ((TermFeatureTerm) node).getFeatures()) {
                    FeatureTerm ft2 = feature.getValue();

                    if (!visited.contains(ft2)) {
                        if (ft2.isConstant() || dm.contains(ft2)) {
                            HashMap<FeatureTerm, FeatureTerm> correspondences = new HashMap<FeatureTerm, FeatureTerm>();
                            FeatureTerm clone = f.clone(dm, correspondences);

                            ((TermFeatureTerm) correspondences.get(node)).defineFeatureValue(feature.getKey(), ft2.getSort().createFeatureTerm());

                            refinements.add(clone);
                        } else {
                            visited.add(ft2);
                            open_nodes.add(ft2);
                        }
                    }
                }
            } // if

            if (node instanceof SetFeatureTerm) {
                int l = ((SetFeatureTerm) node).getSetValues().size();
                for (int i = 0; i < l; i++) {
                    FeatureTerm ft2 = ((SetFeatureTerm) node).getSetValues().get(i);
                    if (!visited.contains(ft2)) {
                        if (ft2.isConstant() || dm.contains(ft2)) {
                            HashMap<FeatureTerm, FeatureTerm> correspondences = new HashMap<FeatureTerm, FeatureTerm>();
                            FeatureTerm clone = f.clone(dm, correspondences);

                            ((SetFeatureTerm) correspondences.get(node)).substituteSetValue(i, ft2.getSort().createFeatureTerm());

                            refinements.add(clone);
                        } else {
                            visited.add(ft2);
                            open_nodes.add(ft2);
                        }
                    }
                } // for
            } // if/
        } // while

        return refinements;
    }

    public static List<FeatureTerm> setReduction(FeatureTerm f, FTKBase dm, Ontology o) throws FeatureTermException {
        List<FeatureTerm> refinements = new LinkedList<FeatureTerm>();
        HashMap<SetFeatureTerm, Set<Pair<TermFeatureTerm, Symbol>>> sp = setsWithAllParents(f);
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
                    refinements.add(clone);
                }
            }
        }

        return refinements;
    }

    /*
     * This method is like "variableEqualityElimination", but when a variable Equality is eliminated, one of the two variables
     * is left empty (just a term of the appropriate sort without any feature). This is useful when trying to generalize a term
     * all the way up to "(any)", since otherwise we can get into an infinite loop.
     */
    public static List<FeatureTerm> variableEqualityEliminationAggressive(FeatureTerm f, FTKBase dm) throws FeatureTermException {
        List<FeatureTerm> refinements = new LinkedList<FeatureTerm>();

        HashSet<FeatureTerm> visited = new HashSet<FeatureTerm>();
        HashMap<FeatureTerm,Pair<FeatureTerm,Symbol>> firstOccurrence = new HashMap<FeatureTerm,Pair<FeatureTerm,Symbol>>();
        List<FeatureTerm> open_nodes = new LinkedList<FeatureTerm>();
        FeatureTerm node;

        visited.add(f);
        open_nodes.add(f);

        while (!open_nodes.isEmpty()) {
            node = open_nodes.remove(0);
            if (node.getDataType() == Sort.DATATYPE_FEATURETERM) {
                for (Entry<Symbol, FeatureTerm> feature : ((TermFeatureTerm) node).getFeatures()) {
                    FeatureTerm ft2 = feature.getValue();
                    if (!visited.contains(ft2)) {
                        visited.add(ft2);
                        open_nodes.add(ft2);
                        firstOccurrence.put(ft2, new Pair<FeatureTerm,Symbol>(node,feature.getKey()));
                    } else {
                        if (!dm.contains(ft2) && !ft2.isConstant() && !(ft2 instanceof SetFeatureTerm)) {
                            // Variable equality:
                            HashMap<FeatureTerm, FeatureTerm> correspondences = new HashMap<FeatureTerm, FeatureTerm>();
                            FeatureTerm clone = f.clone(dm, correspondences);

                            {
                                if (ft2 == null) {
                                    System.err.println("ft2 was null in variableEqualityEliminationAggressive!");
                                    System.err.println("from the feature '" + feature.getKey().get() + "' in the term:" + node.toStringNOOS(dm));
                                    System.err.flush();
                                }
                                if (ft2.getSort() == null) {
                                    System.err.println("ft2.getSort() was null in variableEqualityEliminationAggressive!");
                                    System.err.println("from the feature '" + feature.getKey().get() + "' in the term:" + node.toStringNOOS(dm));
                                    System.err.flush();
                                }
                                FeatureTerm ft2Clone = ft2.getSort().createFeatureTerm();
                                ((TermFeatureTerm) (correspondences.get(node))).defineFeatureValue(feature.getKey(), ft2Clone);
                            }

                            refinements.add(clone);

                            if (firstOccurrence.get(ft2)!=null) {
                                HashMap<FeatureTerm, FeatureTerm> correspondences2 = new HashMap<FeatureTerm, FeatureTerm>();
                                Pair<FeatureTerm,Symbol> tmp = firstOccurrence.get(ft2);
                                FeatureTerm clone2 = f.clone(dm, correspondences2);
                                FeatureTerm ft2Clone = ft2.getSort().createFeatureTerm();
                                if (tmp.m_b==null) {
                                    ((SetFeatureTerm) (correspondences2.get(tmp.m_a))).removeSetValue(correspondences2.get(ft2));
                                    ((SetFeatureTerm) (correspondences2.get(tmp.m_a))).addSetValue(ft2Clone);
                                } else {
                                    ((TermFeatureTerm) (correspondences2.get(tmp.m_a))).defineFeatureValue(tmp.m_b, ft2Clone);
                                }
                                // prevent the case where replacing the value cuts the access to the other instances of the variable:
                                if (variables(clone2).contains(correspondences2.get(ft2))) {
                                    refinements.add(clone2);
                                }
                                firstOccurrence.remove(ft2);
                            }
                        }
                    }
                }
            } // if

            if (node instanceof SetFeatureTerm) {
                for (FeatureTerm ft2 : ((SetFeatureTerm) node).getSetValues()) {
                    if (!visited.contains(ft2)) {
                        visited.add(ft2);
                        open_nodes.add(ft2);
                        firstOccurrence.put(ft2, new Pair<FeatureTerm,Symbol>(node,null));
                    } else {
                        if (!dm.contains(ft2) && !ft2.isConstant() && !(ft2 instanceof SetFeatureTerm)) {
                            // Variable equality:
                            HashMap<FeatureTerm, FeatureTerm> correspondences = new HashMap<FeatureTerm, FeatureTerm>();
                            FeatureTerm clone = f.clone(dm, correspondences);

                            {
                                ((SetFeatureTerm) (correspondences.get(node))).removeSetValue(correspondences.get(ft2));
//								correspondences.remove(ft2);
//								FeatureTerm ft2Clone = ft2.clone(correspondences);
                                if (ft2 == null) {
                                    System.err.println("ft2 was null in variableEqualityEliminationAggressive!");
                                    System.err.println("from the set:" + node.toStringNOOS(dm));
                                    System.err.flush();
                                }
                                if (ft2.getSort() == null) {
                                    System.err.println("ft2.getSort() was null in variableEqualityEliminationAggressive!");
                                    System.err.println("from the set:" + node.toStringNOOS(dm));
                                    System.err.flush();
                                }
                                FeatureTerm ft2Clone = ft2.getSort().createFeatureTerm();
                                ((SetFeatureTerm) (correspondences.get(node))).removeSetValue(correspondences.get(ft2));
                                ((SetFeatureTerm) (correspondences.get(node))).addSetValue(ft2Clone);
                            }

                            refinements.add(clone);

                            if (firstOccurrence.get(ft2)!=null) {
                                Pair<FeatureTerm,Symbol> tmp = firstOccurrence.get(ft2);
                                HashMap<FeatureTerm, FeatureTerm> correspondences2 = new HashMap<FeatureTerm, FeatureTerm>();
                                FeatureTerm clone2 = f.clone(dm, correspondences2);
                                FeatureTerm ft2Clone = ft2.getSort().createFeatureTerm();
                                if (tmp.m_b==null) {
                                    ((SetFeatureTerm) (correspondences2.get(tmp.m_a))).removeSetValue(correspondences2.get(ft2));
                                    ((SetFeatureTerm) (correspondences2.get(tmp.m_a))).addSetValue(ft2Clone);
                                } else {
                                    ((TermFeatureTerm) (correspondences2.get(tmp.m_a))).defineFeatureValue(tmp.m_b, ft2Clone);
                                }
                                // prevent the case where replacing the value cuts the access to the other instances of the variable:
                                if (variables(clone2).contains(correspondences2.get(ft2))) {
                                    refinements.add(clone2);
                                }
                                firstOccurrence.remove(ft2);
                            }
                        }
                    }
                } // for
            } // if/
        } // while

        return refinements;
    }

    public static List<FeatureTerm> variableEqualityElimination(FeatureTerm f, FTKBase dm, List<Pair<FeatureTerm, Path>> vp) throws FeatureTermException {
        List<FeatureTerm> refinements = new LinkedList<FeatureTerm>();

        HashSet<FeatureTerm> visited = new HashSet<FeatureTerm>();
        List<FeatureTerm> open_nodes = new LinkedList<FeatureTerm>();
        FeatureTerm node;

        visited.add(f);
        open_nodes.add(f);

        while (!open_nodes.isEmpty()) {
            node = open_nodes.remove(0);
            if (node.getDataType() == Sort.DATATYPE_FEATURETERM) {
                for (Entry<Symbol, FeatureTerm> feature : ((TermFeatureTerm) node).getFeatures()) {
                    FeatureTerm ft2 = feature.getValue();
                    if (!visited.contains(ft2)) {
                        visited.add(ft2);
                        open_nodes.add(ft2);
                    } else {
                        if (!dm.contains(ft2) && !ft2.isConstant() && !(ft2 instanceof SetFeatureTerm)) {
                            // Variable equality:
                            HashMap<FeatureTerm, FeatureTerm> correspondences = new HashMap<FeatureTerm, FeatureTerm>();
                            FeatureTerm clone = f.clone(dm, correspondences);

                            {
                                correspondences.remove(ft2);
                                FeatureTerm ft2Clone = ft2.clone(correspondences);
                                ((TermFeatureTerm) (correspondences.get(node))).defineFeatureValue(feature.getKey(), ft2Clone);
                            }

                            refinements.add(clone);
                        }
                    }
                }
            } // if

            if (node instanceof SetFeatureTerm) {
                for (FeatureTerm ft2 : ((SetFeatureTerm) node).getSetValues()) {
                    if (!visited.contains(ft2)) {
                        visited.add(ft2);
                        open_nodes.add(ft2);
                    } else {
                        if (!dm.contains(ft2) && !ft2.isConstant() && !(ft2 instanceof SetFeatureTerm)) {
                            // Variable equality:
                            HashMap<FeatureTerm, FeatureTerm> correspondences = new HashMap<FeatureTerm, FeatureTerm>();
                            FeatureTerm clone = f.clone(dm, correspondences);

                            {
                                ((SetFeatureTerm) (correspondences.get(node))).removeSetValue(correspondences.get(ft2));
                                correspondences.remove(ft2);
                                FeatureTerm ft2Clone = ft2.clone(correspondences);
                                ((SetFeatureTerm) (correspondences.get(node))).addSetValue(ft2Clone);
                            }

                            refinements.add(clone);
                        }
                    }
                } // for
            } // if/
        } // while

        return refinements;
    }

    public static List<FeatureTerm> featureElimination(FeatureTerm f, FTKBase dm, List<Pair<FeatureTerm, Path>> vp) throws FeatureTermException {
        List<FeatureTerm> refinements = new LinkedList<FeatureTerm>();

        if (vp == null) {
            vp = variablesWithPaths(f, dm);
        }

        for (Pair<FeatureTerm, Path> p : vp) {
            FeatureTerm X = p.m_a;
            if (X.getDataType() == Sort.DATATYPE_FEATURETERM) {
                TermFeatureTerm TX = (TermFeatureTerm) X;

                for (Symbol fname : TX.getFeatureNames()) {
                    FeatureTerm fvalue = TX.featureValue(fname);

                    if ((fvalue instanceof SetFeatureTerm &&
                         ((SetFeatureTerm)fvalue).getSetValues().size()==0) ||
                        (fvalue.getSort() == X.getSort().featureSort(fname) && fvalue.isLeaf() && !fvalue.isConstant() && !dm.contains(fvalue))) {
                        HashMap<FeatureTerm, FeatureTerm> correspondences = new HashMap<FeatureTerm, FeatureTerm>();
                        FeatureTerm clone = f.clone(dm, correspondences);

                        TermFeatureTerm f1 = (TermFeatureTerm) correspondences.get(X);
                        f1.removeFeatureValue(fname);

                        refinements.add(clone);
                    }
                }
            }
        }

        return refinements;
    }

    public static List<FeatureTerm> sortGeneralization(FeatureTerm f, FTKBase dm, Ontology o) throws FeatureTermException {
        List<FeatureTerm> refinements = new LinkedList<FeatureTerm>();

        HashMap<FeatureTerm, List<Pair<TermFeatureTerm, Symbol>>> vp = variablesWithAllParents(f);

        for (FeatureTerm X : vp.keySet()) {
            if (!X.isConstant() && !dm.contains(X)) {
                Sort most_general = o.getSort("any");
                Sort to_generalize_to = X.getSort().getSuper();

                if (to_generalize_to != null) {
                    for (Pair<TermFeatureTerm, Symbol> parent : vp.get(X)) {
                        if (parent != null) {
                            Sort s = parent.m_a.getSort().featureSort(parent.m_b);
                            if (most_general.isSubsort(s)) {
                                most_general = s;
                            }
                        }
                    }

                    if (to_generalize_to.is_a(most_general)) {
                        boolean canGeneralize = true;

                        if (X.getDataType() == Sort.DATATYPE_FEATURETERM) {
                            for (Symbol fn : ((TermFeatureTerm) X).getFeatureNames()) {
                                if (!to_generalize_to.hasFeature(fn)) {
                                    canGeneralize = false;
                                    break;
                                }
                            }
                        }

                        if (canGeneralize) {
                            refinements.add(sortSubstitution(f, X, to_generalize_to, dm));
                        }
                    }
                }
            }
        }

        return refinements;
    }

    public static List<FeatureTerm> sortSpecialization(FeatureTerm f, FTKBase dm, List<Pair<FeatureTerm, Path>> vp) throws FeatureTermException {
        List<FeatureTerm> refinements = new LinkedList<FeatureTerm>();

        if (vp == null) {
            vp = variablesWithPaths(f, dm);
        }

        for (Pair<FeatureTerm, Path> p : vp) {
            FeatureTerm X = p.m_a;
            if (!dm.contains(X)) {
                for (Sort s : X.getSort().getSubSorts()) {
                    refinements.add(sortSubstitution(f, X, s, dm));
                }
            }
        }

        return refinements;
    }

    public static List<FeatureTerm> featureIntroduction(FeatureTerm f, FTKBase dm, List<Pair<FeatureTerm, Path>> vp) throws FeatureTermException {
        List<FeatureTerm> refinements = new LinkedList<FeatureTerm>();

        if (vp == null) {
            vp = variablesWithPaths(f, dm);
        }

        for (Pair<FeatureTerm, Path> p : vp) {
            FeatureTerm X = p.m_a;
            if (X.getDataType() == Sort.DATATYPE_FEATURETERM && !dm.contains(X)) {
                for (Symbol feature : X.getSort().getFeatures()) {
                    if (X.featureValue(feature) == null) {
                        refinements.add(featureDefinition(f, X, feature, X.getSort().featureSort(feature).createFeatureTerm(), dm));
                    }
                }
            }
        }

        return refinements;
    }

    public static List<FeatureTerm> featureIntroductionSubsumingAll(FeatureTerm f, FTKBase dm, List<Pair<FeatureTerm, Path>> vp, List<FeatureTerm> objects) throws FeatureTermException {
        List<FeatureTerm> refinements = new LinkedList<FeatureTerm>();

        if (vp == null) {
            vp = variablesWithPaths(f, dm);
        }

        for (Pair<FeatureTerm, Path> p : vp) {
            FeatureTerm X = p.m_a;
            if (!dm.contains(X)) {
                for (Symbol feature : X.getSort().getFeatures()) {
                    if (X.featureValue(feature) == null) {
                        boolean allHaveIt = true;

                        for (FeatureTerm object : objects) {
                            FeatureTerm X2 = object.readPath(p.m_b);
                            if (X2.getDataType() == Sort.DATATYPE_SET) {
                                allHaveIt = false;
                                for (FeatureTerm X2_element : ((SetFeatureTerm) X2).getSetValues()) {
                                    if ((X2_element.getDataType() == Sort.DATATYPE_FEATURETERM) && X2.featureValue(feature) != null) {
                                        allHaveIt = true;
                                        break;
                                    }
                                }
                            } else {
                                if ((!(X2.getDataType() == Sort.DATATYPE_FEATURETERM)) || X2.featureValue(feature) == null) {
                                    allHaveIt = false;
                                    break;
                                }
                            }
                        }

                        if (allHaveIt) {
                            refinements.add(featureDefinition(f, X, feature, X.getSort().featureSort(feature).createFeatureTerm(), dm));
                        }
                    }
                }
            }
        }

        return refinements;
    }

    public static List<FeatureTerm> variableEqualityAddition(FeatureTerm f, FTKBase dm, List<Pair<FeatureTerm, Path>> vp) throws FeatureTermException {
        List<FeatureTerm> refinements = new LinkedList<FeatureTerm>();
        List<SetFeatureTerm> sets = sets(f);

        if (vp == null) {
            vp = variablesWithPaths(f, dm);
        }

        for (Pair<FeatureTerm, Path> p : vp) {
            FeatureTerm X = p.m_a;
            if (!dm.contains(X)) {
                for (Pair<FeatureTerm, Path> p2 : vp) {
                    FeatureTerm Y = p2.m_a;
                    if (Y != X && X.getSort().isSubsort(Y.getSort()) && !dm.contains(Y)) {
                        if (!dm.contains(X) && Y.getSort().isSubsort(X.getSort())) {
                            if (vp.indexOf(p)<vp.indexOf(p2)) continue; // prevent duplicated refinements
                        }

                        if (!appearTogetherInASet(f,X,Y,sets)) {
                            List<FeatureTerm> tmpl = variableEquality(f,X,Y,dm);
                            for(FeatureTerm tmp:tmpl) {
                                if (f.subsumes(tmp)) refinements.add(tmp);
                            }
                        }
                    }
                }
            }
        }

//		System.out.println("{" + refinements.size() + "}");

        return refinements;
    }

    public static List<FeatureTerm> variableEqualityAdditionSubsumingAll(FeatureTerm f, FTKBase dm, List<Pair<FeatureTerm, Path>> vp, List<FeatureTerm> objects) throws FeatureTermException {
        List<FeatureTerm> refinements = new LinkedList<FeatureTerm>();
        List<SetFeatureTerm> sets = sets(f);

        if (vp == null) {
            vp = variablesWithPaths(f, dm);
        }

        for (Pair<FeatureTerm, Path> p : vp) {
            FeatureTerm X = p.m_a;
            if (!X.isConstant() && !dm.contains(X)) {
                for (Pair<FeatureTerm, Path> p2 : vp) {
                    FeatureTerm Y = p2.m_a;
                    if (Y != X && !Y.isConstant() && X.getSort().isSubsort(Y.getSort()) && !dm.contains(Y)) {
                        if (!X.isConstant() && !dm.contains(X) && Y.getSort().isSubsort(X.getSort())) {
                            if (vp.indexOf(p)<vp.indexOf(p2)) continue; // prevent duplicated refinements
                        }
                        boolean appear_together = false;
                        boolean appear_in_all_objects = true;

                        for (FeatureTerm object : objects) {
                            FeatureTerm X2 = object.readPath(p.m_b);
                            FeatureTerm Y2 = object.readPath(p2.m_b);

                            if (!intersect(X2, Y2)) {
                                appear_in_all_objects = false;
                                break;
                            }
                        }

                        if (appear_in_all_objects) {
                            for (SetFeatureTerm set : sets) {
                                if (set.getSetValues().contains(X) &&
                                    set.getSetValues().contains(Y)) {
                                    appear_together = true;
                                    break;
                                }
                            }

                            if (!appear_together) {
                                List<FeatureTerm> tmpl = variableEquality(f,X,Y,dm);
                                for(FeatureTerm tmp:tmpl) {
                                    if (f.subsumes(tmp)) refinements.add(tmp);
                                }
                            }
                        }
                    }
                }
            }
        }

//		System.out.println("{" + refinements.size() + "}");

        return refinements;
    }

    public static List<FeatureTerm> variableEquality(FeatureTerm f, FeatureTerm X, FeatureTerm Y, FTKBase dm) throws FeatureTermException {
        return variableEquality(f,X,Y,dm,new LinkedList<Pair<FeatureTerm,FeatureTerm>>());
    }

    public static List<FeatureTerm> variableEquality(FeatureTerm f, FeatureTerm X, FeatureTerm Y, FTKBase dm,
                                                     List<Pair<FeatureTerm,FeatureTerm>> pendingEqualitiesInput) throws FeatureTermException {
        HashMap<FeatureTerm, FeatureTerm> correspondences = new HashMap<FeatureTerm, FeatureTerm>();
        FeatureTerm clone = f.clone(dm, correspondences);
        FeatureTerm NX = correspondences.get(X);
        FeatureTerm NY = correspondences.get(Y);
        List<FeatureTerm> results = new LinkedList<FeatureTerm>();

        // clone the pending equalities list:
        List<Pair<FeatureTerm,FeatureTerm>> pendingEqualities = new LinkedList<Pair<FeatureTerm,FeatureTerm>>();
        for(Pair<FeatureTerm,FeatureTerm> pe:pendingEqualitiesInput) {
            pendingEqualities.add(new Pair<FeatureTerm,FeatureTerm>(pe.m_a,pe.m_b));
        }

        // Create a clone node which has all the features of X and Y, and the most specific sort:
        Sort s = null;
        if (X.getSort().isSubsort(Y.getSort())) {
            s = Y.getSort();
        } else if (X.getSort().isSubsort(Y.getSort())) {
            s = X.getSort();
        } else {
            return results;
        }

        FeatureTerm newNode = s.createFeatureTerm();
        try {
            for(Symbol feature:s.getFeatures()) {
                List<FeatureTerm> vl1 = X.featureValues(feature);
                List<FeatureTerm> vl2 = Y.featureValues(feature);
                for(FeatureTerm v:vl1) {
                    if (v==X || v==Y) {
                        ((TermFeatureTerm)newNode).addFeatureValue(feature, newNode);
                    } else {
                        ((TermFeatureTerm)newNode).addFeatureValue(feature, correspondences.get(v));
                    }
                }
                for(FeatureTerm v:vl2) {
                    if (v==X || v==Y) {
                        ((TermFeatureTerm)newNode).addFeatureValue(feature, newNode);
                    } else {
                        ((TermFeatureTerm)newNode).addFeatureValue(feature, correspondences.get(v));
                    }
                }
                // check for any recursive variable equalities that might be needed:
                for(FeatureTerm V1:vl1) {
                    for(FeatureTerm V2:vl2) {
                        if (V1!=V2) {
                            if (!appearTogetherInASet(f, V1, V2)) {
                                // Found a pair that needs to be treated recursively:
                                pendingEqualities.add(new Pair<FeatureTerm,FeatureTerm>(V1,V2));
                            }
                        }
                    }
                }
            }
        } catch (SingletonFeatureTermException e) {
            return results;
        }

        for(Pair<FeatureTerm,FeatureTerm> pe:pendingEqualities) {
            if (pe.m_a == X || pe.m_a == Y) pe.m_a = newNode;
                                       else pe.m_a = correspondences.get(pe.m_a);
            if (pe.m_b == X || pe.m_b == Y) pe.m_b = newNode;
                                       else pe.m_b = correspondences.get(pe.m_b);
        }


//        System.out.println("Creating a variable equality in:\n" + clone.toStringNOOS(dm));
//        System.out.println("X:\n" + X.toStringNOOS(dm));
//        System.out.println("Y:\n" + Y.toStringNOOS(dm));
//        System.out.println("newNode:\n" + newNode.toStringNOOS(dm));
        clone.substitute(correspondences.get(X), newNode);
//        System.out.println("After doing X -> newNode:\n" + clone.toStringNOOS(dm));
        clone.substitute(correspondences.get(Y), newNode);
//        System.out.println("After doing Y -> newNode:\n" + clone.toStringNOOS(dm));
        FeatureTerm base = clone;
        if (f==X || f==Y) base = newNode;
        results.add(base);

        if (!pendingEqualities.isEmpty()) {
            List<Pair<FeatureTerm,FeatureTerm>> toDelete = new LinkedList<Pair<FeatureTerm,FeatureTerm>>();

            // replace appearances of X and Y:
            for(Pair<FeatureTerm,FeatureTerm> p:pendingEqualities) {
                if (p.m_a==NX || p.m_a==NY) p.m_a = newNode;
                if (p.m_b==NX || p.m_b==NY) p.m_b = newNode;
                if (p.m_a == p.m_b) toDelete.add(p);
            }
            pendingEqualities.removeAll(toDelete);
            toDelete.clear();

            // filter duplicates:
            int n = pendingEqualities.size();
            for(int i = 0;i<n;i++) {
                for(int j = i+1;j<n;j++) {
                    Pair<FeatureTerm,FeatureTerm> p1 = pendingEqualities.get(i);
                    Pair<FeatureTerm,FeatureTerm> p2 = pendingEqualities.get(j);
                    if (p1.m_a == p2.m_a && p1.m_b == p2.m_b) toDelete.add(p2);
                    if (p1.m_a == p2.m_b && p1.m_b == p2.m_a) toDelete.add(p2);
                }
            }
            pendingEqualities.removeAll(toDelete);


//            List<FeatureTerm> variables1 = FTRefinement.variables(f);
//            List<FeatureTerm> variables2 = FTRefinement.variables(base);
//            System.err.println("Recursive variable equality needed with " + pendingEqualities.size() + " pairs!!! (" + variables1.size() + " -> " + variables2.size() + ")");
/*
            String n1 = "X" + (variables1.indexOf(X) + 1);
            String n2 = "X" + (variables1.indexOf(Y) + 1);
            String n3 = "Y" + (variables2.indexOf(newNode) + 1);
            System.err.println("Original equality was " + n1 + "==" + n2 + " -> " + n3);
            System.err.println("Resulting in\n" + base.toStringNOOS(dm));
            for(Pair<FeatureTerm,FeatureTerm> p:recursiveEqualitiesToConsider) {
                n1 = "?";
                n2 = "?";
                int index = variables1.indexOf(p.m_a);
                if (index==-1) {
                    index = variables2.indexOf(p.m_a);
                    if (index!=-1) n1 = "Y" + (index + 1);
                } else {
                    n1 = "X" + (index + 1);
                }
                index = variables1.indexOf(p.m_b);
                if (index==-1) {
                    index = variables2.indexOf(p.m_b);
                    if (index!=-1) n2 = "Y" + (index + 1);
                } else {
                    n2 = "X" + (index + 1);
                }
                System.err.println(n1 + "==" + n2);
            }
*/
            while(!pendingEqualities.isEmpty()) {
                Pair<FeatureTerm,FeatureTerm> first = pendingEqualities.remove(0);
                results.addAll(variableEquality(base,first.m_a,first.m_b,dm,pendingEqualities));
            }
        }

        return results;
    }
    

    public static List<FeatureTerm> setExpansion(FeatureTerm f, FTKBase dm, List<Pair<FeatureTerm, Path>> vp) throws FeatureTermException {
        List<FeatureTerm> refinements = new LinkedList<FeatureTerm>();

        if (vp == null) {
            vp = variablesWithPaths(f, dm);
        }

        for (Pair<FeatureTerm, Path> p : vp) {
            FeatureTerm X = p.m_a;
            if (X.getDataType() == Sort.DATATYPE_FEATURETERM) {
                for (Symbol feature : X.getSort().getFeatures()) {
                    FeatureTerm v = X.featureValue(feature);
                    if (!X.getSort().featureSingleton(feature) && v != null) {
                        if (v instanceof SetFeatureTerm) {
                            HashMap<FeatureTerm, FeatureTerm> correspondences = new HashMap<FeatureTerm, FeatureTerm>();
                            FeatureTerm clone = f.clone(dm, correspondences);
                            ((SetFeatureTerm) correspondences.get(v)).addSetValue(X.getSort().featureSort(feature).createFeatureTerm());
                            refinements.add(clone);
                        } else {
                            HashMap<FeatureTerm, FeatureTerm> correspondences = new HashMap<FeatureTerm, FeatureTerm>();
                            FeatureTerm clone = f.clone(dm, correspondences);
                            SetFeatureTerm newSet = new SetFeatureTerm();
                            FeatureTerm new_v = correspondences.get(v);
                            if (new_v == null) {
                                // This means that "v" was part of the domain model
                                newSet.addSetValue(v);
                            } else {
                                newSet.addSetValue(new_v);
                            }
                            newSet.addSetValue(X.getSort().featureSort(feature).createFeatureTerm());
                            ((TermFeatureTerm) correspondences.get(X)).defineFeatureValue(feature, newSet);
                            refinements.add(clone);
                        }
                    }
                }
            }
        }
        return refinements;
    }

    public static List<FeatureTerm> setExpansionSubsumingAll(FeatureTerm f, FTKBase dm, List<Pair<FeatureTerm, Path>> vp, List<FeatureTerm> objects) throws FeatureTermException {
        List<FeatureTerm> refinements = new LinkedList<FeatureTerm>();

        if (vp == null) {
            vp = variablesWithPaths(f, dm);
        }
        
        for (Pair<FeatureTerm, Path> p : vp) {
            FeatureTerm X = p.m_a;
            if (X.getDataType() == Sort.DATATYPE_FEATURETERM) {
                for (Symbol feature : X.getSort().getFeatures()) {
                    FeatureTerm v = X.featureValue(feature);
                    if (!X.getSort().featureSingleton(feature) && v != null) {
                        int maximum_size, current_size;

                        if (v instanceof SetFeatureTerm) {
                            current_size = ((SetFeatureTerm) v).getSetValues().size();
                        } else {
                            current_size = 1;
                        }

                        maximum_size = current_size + 1;

                        for (FeatureTerm object : objects) {
                            FeatureTerm X2 = object.readPath(p.m_b);
                            List<FeatureTerm> lv2 = new LinkedList<FeatureTerm>();
                            int local_max_size = 0;
                            if (X2 instanceof TermFeatureTerm) {
                                lv2.add(X2.featureValue(feature));
                            } else if (X2 instanceof SetFeatureTerm) {
                                for(FeatureTerm X2v:((SetFeatureTerm)X2).getSetValues()) {
                                    if (X2v instanceof TermFeatureTerm)
                                        lv2.add(X2v.featureValue(feature));
                                }
                            }

                            for(FeatureTerm v2:lv2) {
                                if (v2 instanceof SetFeatureTerm) {
                                    local_max_size = Math.max(((SetFeatureTerm) v2).getSetValues().size(), local_max_size);
                                } else {
                                    local_max_size = Math.max(1, local_max_size);
                                }                                
                            }

                            maximum_size = Math.min(maximum_size,local_max_size);

                            if (maximum_size <= current_size) {
                                break;
                            }
                        }

                        if (current_size < maximum_size) {
                            if (v instanceof SetFeatureTerm) {
                                HashMap<FeatureTerm, FeatureTerm> correspondences = new HashMap<FeatureTerm, FeatureTerm>();
                                FeatureTerm clone = f.clone(dm, correspondences);
                                ((SetFeatureTerm) correspondences.get(v)).addSetValue(X.getSort().featureSort(feature).createFeatureTerm());
                                refinements.add(clone);
                            } else {
                                HashMap<FeatureTerm, FeatureTerm> correspondences = new HashMap<FeatureTerm, FeatureTerm>();
                                FeatureTerm clone = f.clone(dm, correspondences);
                                SetFeatureTerm newSet = new SetFeatureTerm();
                                FeatureTerm new_v = correspondences.get(v);
                                if (new_v == null) {
                                    // This means that "v" was part of the domain model
                                    newSet.addSetValue(v);
                                } else {
                                    newSet.addSetValue(new_v);
                                }
                                newSet.addSetValue(X.getSort().featureSort(feature).createFeatureTerm());
                                ((TermFeatureTerm) correspondences.get(X)).defineFeatureValue(feature, newSet);
                                refinements.add(clone);
                            }
                        }
                    }
                }
            }
        }
        return refinements;
    }

    public static List<FeatureTerm> substitutionByConstantSubsumingSome(FeatureTerm f, FTKBase dm, Ontology ontology, List<Pair<FeatureTerm, Path>> vp, List<FeatureTerm> objects) throws FeatureTermException {
        List<FeatureTerm> refinements = new LinkedList<FeatureTerm>();

        if (vp == null) {
            vp = variablesWithPaths(f, dm);
        }

        for (Pair<FeatureTerm, Path> node : vp) {
            List<FeatureTerm> constants = new LinkedList<FeatureTerm>();
            for (FeatureTerm o : objects) {
                FeatureTerm c = o.readPath(node.m_b);

                if (c != null) {
                    if (c instanceof SetFeatureTerm) {
                        for (FeatureTerm c2 : ((SetFeatureTerm) c).getSetValues()) {
                            if ((c2.isConstant() || dm.contains(c2)) && node.m_a.subsumes(c2) && !c2.subsumes(node.m_a) && !constants.contains(c2)) {
                                constants.add(c2);
                            }
                        }
                    } else {
                        //					if (c==null) System.out.println("c is null");
                        //					if (dm==null) System.out.println("dm is null");
                        //					if (node==null) System.out.println("node is null");
                        //					if (node.m_a==null) System.out.println("node.m_a is null");
                        //					if (constants==null) System.out.println("constants is null");
                        if ((c.isConstant() || dm.contains(c)) && node.m_a.subsumes(c) && !c.subsumes(node.m_a) && !constants.contains(c)) {
                            constants.add(c);
//						} else {
//							System.out.println(c.toStringNOOS(dm) + " discarded");
                        }
                    }
                }
            }

//			System.out.print("{");
            for (FeatureTerm c : constants) {
//				System.out.print(c.toStringNOOS(dm) + " ");
                refinements.add(substitute(f, node.m_a, c.clone(dm, ontology), dm));
            }
//			System.out.println("}");
        }

        return refinements;
    }

    public static List<FeatureTerm> substitutionByConstantSubsumingAll(FeatureTerm f, FTKBase dm, Ontology ontology, List<Pair<FeatureTerm, Path>> vp, List<FeatureTerm> objects) throws FeatureTermException {
        List<FeatureTerm> refinements = new LinkedList<FeatureTerm>();

        if (vp == null) {
            vp = variablesWithPaths(f, dm);
        }

        for (Pair<FeatureTerm, Path> node : vp) {
            List<FeatureTerm> constants = new LinkedList<FeatureTerm>();
            boolean first = true;
            if (!node.m_a.isConstant() && !dm.contains(node.m_a)) {
                for (FeatureTerm o : objects) {
                    FeatureTerm c = o.readPath(node.m_b);

                    if (first) {
                        if (c!=null) {
                            if (c instanceof SetFeatureTerm) {
                                for (FeatureTerm c2 : ((SetFeatureTerm) c).getSetValues()) {
                                    if ((c2.isConstant() || dm.contains(c2)) && node.m_a.subsumes(c2)) {
                                        constants.add(c2);
                                    }
                                }
                            } else {
                                if ((c.isConstant() || dm.contains(c)) && node.m_a.subsumes(c)) {
                                    constants.add(c);
                                }
                            }
                        }
                        first = false;
                    } else {
                        if (c==null) {
                            constants.clear();
                        } else if (c instanceof SetFeatureTerm) {
                            boolean found = false;
                            List<FeatureTerm> todelete = new LinkedList<FeatureTerm>();
                            for (FeatureTerm c3 : constants) {
                                for (FeatureTerm c2 : ((SetFeatureTerm) c).getSetValues()) {
                                    if (c3.equals(c2)) {
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found) {
                                    todelete.add(c3);
                                }
                            }
                            while (!todelete.isEmpty()) {
                                constants.remove(todelete.remove(0));
                            }
                        } else {
                            List<FeatureTerm> todelete = new LinkedList<FeatureTerm>();
                            for (FeatureTerm c3 : constants) {
                                if (c3 != c) {
                                    todelete.add(c3);
                                }
                            }
                            while (!todelete.isEmpty()) {
                                constants.remove(todelete.remove(0));
                            }
                        }

                        if (constants.isEmpty()) {
                            break;
                        }
                    }
                }
            }
            for (FeatureTerm c : constants) {
                refinements.add(substitute(f, node.m_a, c.clone(dm, ontology), dm));
            }
        }

        return refinements;
    }



    public static List<FeatureTerm> variables(FeatureTerm ft) {
        HashSet<FeatureTerm> visited = new HashSet<FeatureTerm>();
        List<FeatureTerm> variables = new LinkedList<FeatureTerm>();
        List<FeatureTerm> open_nodes = new LinkedList<FeatureTerm>();
        FeatureTerm node;

        visited.add(ft);
        open_nodes.add(ft);

        while (!open_nodes.isEmpty()) {
            node = open_nodes.remove(0);
            if (!(node instanceof SetFeatureTerm)) {
                variables.add(node);
            }

            if (node.getDataType() == Sort.DATATYPE_FEATURETERM) {
                for (FeatureTerm ft2 : ((TermFeatureTerm) node).getFeatureValues()) {
                    if (!visited.contains(ft2)) {
                        visited.add(ft2);
                        open_nodes.add(ft2);
                    }
                }
            } // if

            if (node instanceof SetFeatureTerm) {
                for (FeatureTerm ft2 : ((SetFeatureTerm) node).getSetValues()) {
                    if (!visited.contains(ft2)) {
                        visited.add(ft2);
                        open_nodes.add(ft2);
                    }
                } // for
            } // if/
        } // while

        return variables;
    } // variables

    public static List<Pair<FeatureTerm, Path>> variablesWithPaths(FeatureTerm ft, FTKBase dm) {
        HashSet<FeatureTerm> visited = new HashSet<FeatureTerm>();
        List<Pair<FeatureTerm, Path>> variablesPaths = new LinkedList<Pair<FeatureTerm, Path>>();
        List<Pair<FeatureTerm, Path>> open_nodes = new LinkedList<Pair<FeatureTerm, Path>>();
        Pair<FeatureTerm, Path> node;

        visited.add(ft);
        open_nodes.add(new Pair<FeatureTerm, Path>(ft, new Path()));

        while (!open_nodes.isEmpty()) {
            node = open_nodes.remove(0);
            if (!(node.m_a instanceof SetFeatureTerm)) {
                variablesPaths.add(node);
            }

            if (node.m_a instanceof TermFeatureTerm) {
                for (Entry<Symbol, FeatureTerm> feature : ((TermFeatureTerm) node.m_a).getFeatures()) {
                    FeatureTerm ft2 = feature.getValue();
                    if (ft2.isConstant() || dm.contains(ft2) || !visited.contains(ft2)) {
                        visited.add(ft2);
                        Path p = new Path(node.m_b);
                        p.features.add(feature.getKey());
                        open_nodes.add(new Pair<FeatureTerm, Path>(ft2, p));
                    }
                }
            } // if

            if (node.m_a instanceof SetFeatureTerm) {
                for (FeatureTerm ft2 : ((SetFeatureTerm) node.m_a).getSetValues()) {
                    if (ft2.isConstant() || dm.contains(ft2) || !visited.contains(ft2)) {
                        visited.add(ft2);
                        open_nodes.add(new Pair<FeatureTerm, Path>(ft2, node.m_b));
                    }
                } // for
            } // if/
        } // while

        return variablesPaths;
    } // variables with paths

    public static HashMap<FeatureTerm, AnnotatedPath> variablesWithAnnotatedPaths(FeatureTerm ft) {
        HashSet<FeatureTerm> visited = new HashSet<FeatureTerm>();
        HashMap<FeatureTerm, AnnotatedPath> variablesPaths = new LinkedHashMap<FeatureTerm, AnnotatedPath>();
        List<Pair<FeatureTerm, AnnotatedPath>> open_nodes = new LinkedList<Pair<FeatureTerm, AnnotatedPath>>();
        Pair<FeatureTerm, AnnotatedPath> node;

        visited.add(ft);
        open_nodes.add(new Pair<FeatureTerm, AnnotatedPath>(ft, new AnnotatedPath()));

        while (!open_nodes.isEmpty()) {
            node = open_nodes.remove(0);
            if (!(node.m_a instanceof SetFeatureTerm)) {
                variablesPaths.put(node.m_a, node.m_b);
            }

            if (node.m_a.getDataType() == Sort.DATATYPE_FEATURETERM) {
                for (Entry<Symbol, FeatureTerm> feature : ((TermFeatureTerm) node.m_a).getFeatures()) {
                    FeatureTerm ft2 = feature.getValue();
                    if (!visited.contains(ft2)) {
                        visited.add(ft2);
                        AnnotatedPath p = new AnnotatedPath(node.m_b);
                        p.features.add(new Pair<FeatureTerm, Symbol>(node.m_a, feature.getKey()));
                        open_nodes.add(new Pair<FeatureTerm, AnnotatedPath>(ft2, p));
                    }
                }
            } // if

            if (node.m_a instanceof SetFeatureTerm) {
                for (FeatureTerm ft2 : ((SetFeatureTerm) node.m_a).getSetValues()) {
                    if (!visited.contains(ft2)) {
                        visited.add(ft2);
                        open_nodes.add(new Pair<FeatureTerm, AnnotatedPath>(ft2, node.m_b));
                    }
                } // for
            } // if/
        } // while

        return variablesPaths;
    } // variables with annotated paths


    public static HashMap<FeatureTerm, List<Pair<TermFeatureTerm, Symbol>>> variablesWithAllParents(FeatureTerm ft) throws FeatureTermException {
        HashMap<FeatureTerm, List<Pair<TermFeatureTerm, Symbol>>> variablesParents = new LinkedHashMap<FeatureTerm, List<Pair<TermFeatureTerm, Symbol>>>();
        List<Pair<FeatureTerm, Pair<TermFeatureTerm, Symbol>>> open_nodes = new LinkedList<Pair<FeatureTerm, Pair<TermFeatureTerm, Symbol>>>();
        Pair<FeatureTerm, Pair<TermFeatureTerm, Symbol>> node;

        open_nodes.add(new Pair<FeatureTerm, Pair<TermFeatureTerm, Symbol>>(ft, null));

        while (!open_nodes.isEmpty()) {
            node = open_nodes.remove(0);

            if (!(node.m_a instanceof SetFeatureTerm)) {
                List<Pair<TermFeatureTerm, Symbol>> parents = variablesParents.get(node.m_a);
                if (parents == null) {
                    LinkedList<Pair<TermFeatureTerm, Symbol>> s = new LinkedList<Pair<TermFeatureTerm, Symbol>>();
                    s.add(node.m_b);
                    if (node.m_a==null) {
                        System.err.println("variablesWithAllParents: Variable is null!!!");
                        System.err.println(ft.toStringNOOS());
                        throw new FeatureTermException("variablesWithAllParents: Variable is null!!!");
                    }
                    variablesParents.put(node.m_a, s);
                } else {
                    boolean already_there = false;
                    for (Pair<TermFeatureTerm, Symbol> parent : parents) {
                        if (parent.m_a.equals(node.m_b.m_a) && parent.m_b.equals(node.m_b.m_b)) {
                            already_there = true;
                            break;
                        }
                    }
                    if (!already_there) {
                        parents.add(node.m_b);
                    }
                }
            }

            if (node.m_a instanceof TermFeatureTerm) {
                for (Entry<Symbol, FeatureTerm> feature : ((TermFeatureTerm) node.m_a).getFeatures()) {
                    FeatureTerm ft2 = feature.getValue();
                    if (variablesParents.get(ft2) == null) {
                        open_nodes.add(new Pair<FeatureTerm, Pair<TermFeatureTerm, Symbol>>(ft2,
                                new Pair<TermFeatureTerm, Symbol>((TermFeatureTerm) node.m_a, feature.getKey())));
                    } else {
                        List<Pair<TermFeatureTerm, Symbol>> parents = variablesParents.get(ft2);
                        boolean already_there = false;
                        for (Pair<TermFeatureTerm, Symbol> parent : parents) {
                            if (parent != null && parent.m_a.equals(node.m_a) && parent.m_b.equals(node.m_b.m_b)) {
                                already_there = true;
                                break;
                            }
                        }
                        if (!already_there) {
                            parents.add(new Pair<TermFeatureTerm, Symbol>((TermFeatureTerm) node.m_a, feature.getKey()));
                        }
                    }
                }
            } // if

            if (node.m_a instanceof SetFeatureTerm) {
                for (FeatureTerm ft2 : ((SetFeatureTerm) node.m_a).getSetValues()) {
                    if (variablesParents.get(ft2) == null) {
                        open_nodes.add(new Pair<FeatureTerm, Pair<TermFeatureTerm, Symbol>>(ft2, node.m_b));
                    } else {
                        List<Pair<TermFeatureTerm, Symbol>> parents = variablesParents.get(ft2);
                        boolean already_there = false;
                        for (Pair<TermFeatureTerm, Symbol> parent : parents) {
                            if (parent != null && parent.m_a.equals(node.m_b.m_a) && parent.m_b.equals(node.m_b.m_b)) {
                                already_there = true;
                                break;
                            }
                        }
                        if (!already_there) {
                            parents.add(node.m_b);
                        }
                    }
                } // for
            } // if/
        } // while

        return variablesParents;
    } // variables with paths


    public static List<SetFeatureTerm> sets(FeatureTerm ft) {
        HashSet<FeatureTerm> visited = new HashSet<FeatureTerm>();
        List<SetFeatureTerm> sets = new LinkedList<SetFeatureTerm>();
        List<FeatureTerm> open_nodes = new LinkedList<FeatureTerm>();
        FeatureTerm node;

        visited.add(ft);
        open_nodes.add(ft);

        while (!open_nodes.isEmpty()) {
            node = open_nodes.remove(0);
            if (node instanceof SetFeatureTerm) {
                sets.add((SetFeatureTerm) node);
            }

            if (node.getDataType() == Sort.DATATYPE_FEATURETERM) {
                for (FeatureTerm ft2 : ((TermFeatureTerm) node).getFeatureValues()) {
                    if (!visited.contains(ft2)) {
                        visited.add(ft2);
                        open_nodes.add(ft2);
                    }
                }
            } // if

            if (node instanceof SetFeatureTerm) {
                for (FeatureTerm ft2 : ((SetFeatureTerm) node).getSetValues()) {
                    if (!visited.contains(ft2)) {
                        visited.add(ft2);
                        open_nodes.add(ft2);
                    }
                } // for
            } // if/
        } // while

        return sets;
    } // sets

    public static HashMap<SetFeatureTerm, Set<Pair<TermFeatureTerm, Symbol>>> setsWithAllParents(FeatureTerm ft) {
        HashSet<FeatureTerm> visited = new HashSet<FeatureTerm>();
        HashMap<SetFeatureTerm, Set<Pair<TermFeatureTerm, Symbol>>> setsParents = new HashMap<SetFeatureTerm, Set<Pair<TermFeatureTerm, Symbol>>>();
        List<Pair<FeatureTerm, Pair<TermFeatureTerm, Symbol>>> open_nodes = new LinkedList<Pair<FeatureTerm, Pair<TermFeatureTerm, Symbol>>>();
        Pair<FeatureTerm, Pair<TermFeatureTerm, Symbol>> node;

        visited.add(ft);
        open_nodes.add(new Pair<FeatureTerm, Pair<TermFeatureTerm, Symbol>>(ft, null));

        while (!open_nodes.isEmpty()) {
            node = open_nodes.remove(0);

            if (node.m_a instanceof SetFeatureTerm) {
                Set<Pair<TermFeatureTerm, Symbol>> parents = setsParents.get(node.m_a);
                if (parents == null) {
                    HashSet<Pair<TermFeatureTerm, Symbol>> s = new HashSet<Pair<TermFeatureTerm, Symbol>>();
                    if (node.m_b != null) {
                        s.add(node.m_b);
                    }
                    setsParents.put((SetFeatureTerm) node.m_a, s);
                } else {
                    parents.add(node.m_b);
                }
            }

            if (node.m_a.getDataType() == Sort.DATATYPE_FEATURETERM) {
                for (Entry<Symbol, FeatureTerm> feature : ((TermFeatureTerm) node.m_a).getFeatures()) {
                    FeatureTerm ft2 = feature.getValue();
                    if (!visited.contains(ft2)) {
                        visited.add(ft2);
                        open_nodes.add(new Pair<FeatureTerm, Pair<TermFeatureTerm, Symbol>>(ft2,
                                new Pair<TermFeatureTerm, Symbol>((TermFeatureTerm) node.m_a, feature.getKey())));
                    }
                }
            } // if

            if (node.m_a instanceof SetFeatureTerm) {
                for (FeatureTerm ft2 : ((SetFeatureTerm) node.m_a).getSetValues()) {
                    if (!visited.contains(ft2)) {
                        visited.add(ft2);
                        open_nodes.add(new Pair<FeatureTerm, Pair<TermFeatureTerm, Symbol>>(ft2, node.m_b));
                    }
                } // for
            } // if/
        } // while

        return setsParents;
    } // sets with paths

    public static FeatureTerm substitute(FeatureTerm original, FeatureTerm old_node, FeatureTerm new_node, FTKBase dm) throws FeatureTermException {
        HashMap<FeatureTerm, FeatureTerm> correspondences = new HashMap<FeatureTerm, FeatureTerm>();

        correspondences.put(old_node, new_node);

        return original.clone(dm, correspondences);
    }

    public static FeatureTerm featureDefinition(FeatureTerm original, FeatureTerm old_node, Symbol feature, FeatureTerm feature_value, FTKBase dm) throws FeatureTermException {
        HashMap<FeatureTerm, FeatureTerm> correspondences = new HashMap<FeatureTerm, FeatureTerm>();
        FeatureTerm clone, new_node;

        clone = original.clone(dm, correspondences);

        new_node = correspondences.get(old_node);
        if (!(new_node.getDataType() == Sort.DATATYPE_FEATURETERM)) {
            throw new FeatureTermException("FTAtomicModifiers.featureDefinition of a non TermFeatureTerm! " + new_node.getSort().get());
        }

        if (feature_value == null) {
            ((TermFeatureTerm) new_node).removeFeatureValue(feature);
        } else {
            ((TermFeatureTerm) new_node).defineFeatureValue(feature, feature_value);
        }

        return clone;
    }

    public static FeatureTerm sortSubstitution(FeatureTerm original, FeatureTerm old_node, Sort new_sort, FTKBase dm) throws FeatureTermException {
        HashMap<FeatureTerm, FeatureTerm> correspondences = new HashMap<FeatureTerm, FeatureTerm>();
        FeatureTerm clone, new_node;

        if (old_node.getDataType() != new_sort.getDataType()) {
            correspondences.put(old_node, new_sort.createFeatureTerm());
            clone = original.clone(dm, correspondences);
        } else {
            clone = original.clone(dm, correspondences);

            new_node = correspondences.get(old_node);

            new_node.setSort(new_sort);
        }

//		System.out.println(old_node.getSort().get() + " -> " + new_sort.get());
//		System.out.println(old_node.toStringNOOS(dm) + " -> " + new_node.toStringNOOS(dm));
//		System.out.flush();

        return clone;
    }

    /*
     * This method returns true if:
     * - f1 and f2 are the same, or
     * - if f1 is a set and contains f2
     * - if f2 is a set and contains f1
     * - if f1 and f2 are sets and they have a non empty intersection
     *
     */
    public static boolean intersect(FeatureTerm f1, FeatureTerm f2) {
        if (f1.equals(f2)) {
            return true;
        }

        if (f1 instanceof SetFeatureTerm) {
            if (f2 instanceof SetFeatureTerm) {
                for (FeatureTerm f1e : ((SetFeatureTerm) f1).getSetValues()) {
                    for (FeatureTerm f2e : ((SetFeatureTerm) f2).getSetValues()) {
                        if (f1e.equals(f2e)) {
                            return true;
                        }
                    }
                }
            } else {
                for (FeatureTerm f : ((SetFeatureTerm) f1).getSetValues()) {
                    if (f.equals(f2)) {
                        return true;
                    }
                }
            }
        } else {
            if (f2 instanceof SetFeatureTerm) {
                for (FeatureTerm f : ((SetFeatureTerm) f2).getSetValues()) {
                    if (f.equals(f1)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    // returns the depth in the refinemen graph:
    public static int depth(FeatureTerm t, FTKBase dm, Ontology o) throws FeatureTermException {
        int depth = 0;
        List<FeatureTerm> refinements;

        while (t!=null) {
            refinements = getGeneralizationsAggressive(t, dm, o);
            if (refinements == null || refinements.size() == 0) {
                return depth;
            }
            t = refinements.remove(0);
            depth++;
        };
        return depth;
    }


    // this method assumes that f1 subsumes f2
    public static List<FeatureTerm> refinementPath(FeatureTerm f1,FeatureTerm f2, Ontology o, FTKBase dm) throws FeatureTermException {
        List<FeatureTerm> l = new LinkedList<FeatureTerm>();

        l.add(f2);
        
        FeatureTerm next = f2;
        do{
            List<FeatureTerm> refinements = FTRefinement.getGeneralizationsAggressive(next, dm,o);
            next = null;
            for(FeatureTerm r:refinements) {
                if (f1.subsumes(r)) {
                    next = r;
                    l.add(0,next);
                    break;
                }
            }
        }while(next!=null);
        
        return l;
    }


    // returns true, if variables X and Y appear in together in the same set in f:
    public static boolean appearTogetherInASet(FeatureTerm f, FeatureTerm X, FeatureTerm Y) {
        List<SetFeatureTerm> sets = sets(f);

        for (SetFeatureTerm set : sets) {
            if (set.getSetValues().contains(X) &&
                set.getSetValues().contains(Y)) {
                return true;
            }
        }
        return false;
    }

    // returns true, if variables X and Y appear in together in the same set in f:
    public static boolean appearTogetherInASet(FeatureTerm f, FeatureTerm X, FeatureTerm Y, List<SetFeatureTerm> sets) {
        for (SetFeatureTerm set : sets) {
            if (set.getSetValues().contains(X) &&
                set.getSetValues().contains(Y)) {
                return true;
            }
        }
        return false;

    }

}
