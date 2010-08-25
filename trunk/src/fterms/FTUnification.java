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
public class FTUnification {

    public static int DEBUG = 0;

    static class Restriction {
        FeatureTerm f1;
        FeatureTerm f2;
        boolean soft;
        
        public Restriction(FeatureTerm a_f1, FeatureTerm a_f2,boolean a_soft) {
            f1 = a_f1;
            f2 = a_f2;
            soft = a_soft;  // soft restrictions can be broken (e.g. for sets)
        }
    }

    static class Feature {
        FeatureTerm f1;
        FeatureTerm f2;
        Symbol f;
        boolean function;   // whether it admits sets or not (functions do not admit sets)
        int size_restriction;   // minimum size of the set in this feature
        
        public Feature(FeatureTerm a_f1, FeatureTerm a_f2, Symbol a_f, boolean a_function, int sr) {
            f1 = a_f1;
            f2 = a_f2;
            f = a_f;
            function = a_function;
            size_restriction = sr;
        }
    }

    static class UnificationNode {
        FeatureTerm root;
        List<FeatureTerm> variables = new LinkedList<FeatureTerm>();
        List<Feature> features = new LinkedList<Feature>();
        List<Restriction> restrictions = new LinkedList<Restriction>(); // yet to be applied

        public UnificationNode(UnificationNode n) {
            root = n.root;
            variables.addAll(n.variables);
            for(Feature f:n.features) {
                features.add(new Feature(f.f1,f.f2,f.f,f.function,f.size_restriction));
            }
            for(Restriction r:n.restrictions) {
                restrictions.add(new Restriction(r.f1,r.f2,r.soft));
            }
        }

        public UnificationNode(FeatureTerm f1,FeatureTerm f2, FTKBase dm) throws FeatureTermException {
            HashMap<FeatureTerm, List<Pair<TermFeatureTerm, Symbol>>> v1 = FTRefinement.variablesWithAllParents(f1);
            HashMap<FeatureTerm, List<Pair<TermFeatureTerm, Symbol>>> v2 = FTRefinement.variablesWithAllParents(f2);

            for(FeatureTerm v:v1.keySet()) {
                variables.add(v);
                for(Pair<TermFeatureTerm, Symbol> p:v1.get(v)) {
                    if (p!=null && p.m_a!=null) {
                        FeatureTerm tmp = p.m_a.featureValue(p.m_b);
                        int n = 1;
                        if (tmp instanceof SetFeatureTerm) {
                            n = ((SetFeatureTerm)tmp).getSetValues().size();
                        }
                        features.add(new Feature(p.m_a,v,p.m_b,p.m_a.getSort().featureSingleton(p.m_b),n));
                    }
                }
            }
            for(FeatureTerm v:v2.keySet()) {
                variables.add(v);
                for(Pair<TermFeatureTerm, Symbol> p:v2.get(v)) {
                    if (p!=null && p.m_a!=null) {
                        FeatureTerm tmp = p.m_a.featureValue(p.m_b);
                        int n = 1;
                        if (tmp instanceof SetFeatureTerm) {
                            n = ((SetFeatureTerm)tmp).getSetValues().size();
                        }
                        features.add(new Feature(p.m_a,v,p.m_b,p.m_a.getSort().featureSingleton(p.m_b),n));
                    }
                }
            }
            root = f1;
            restrictions.add(new Restriction(f1, f2, false));    // the first restriction cannot be broken
        }

        public void print(FTKBase dm) {
            System.out.print("V:[ ");
            for(FeatureTerm v:variables) {
                System.out.print("X" + variables.indexOf(v) + ":" + (v.isConstant() || v.getName()!=null ? v.toStringNOOS(dm):v.getSort().get()) + " ");
            }
            System.out.println("]");
            System.out.print("F:[ ");
            for(Feature f:features) {
                System.out.print("X" + variables.indexOf(f.f1) + "." + f.f.get() + "=X" + variables.indexOf(f.f2) + " ");
            }
            System.out.println("]");
            System.out.print("R:[ ");
            for(Restriction r:restrictions) {
                if (r.soft) System.out.print("{");
                System.out.print("X" + variables.indexOf(r.f1) + " = X" + variables.indexOf(r.f2));
                if (r.soft) System.out.print("} ");
                else System.out.print(" ");
            }
            System.out.println("]");
        }

        // applies the first restriction:
        public List<UnificationNode> applyRestriction(FTKBase dm) throws FeatureTermException {
            Restriction r = restrictions.get(0);
            List<UnificationNode> results = new LinkedList<UnificationNode>();

            if (variables.contains(r.f1) && variables.contains(r.f2)) {
                if (r.soft) {
                    // clone the node:
                    UnificationNode n = new UnificationNode(this);
                    n.restrictions.remove(0);
                    results.add(n);
                }

                // clone the node:
                UnificationNode n = new UnificationNode(this);
                n.restrictions.remove(0);
                
                // figure out the new restrictions
                for(Feature f:n.features) {
                    if (f.f1==r.f1) {
                        for(Feature f2:n.features) {
                            if (f2.f1==r.f2 && f2.f.equals(f.f) && f.f2!=f2.f2) {
                                n.restrictions.add(new Restriction(f.f2,f2.f2,!f.f1.getSort().featureSingleton(f.f) ||
                                                                              !f2.f1.getSort().featureSingleton(f.f)));
                            }
                        }
                    }
                }


                // compute the merged variable (unify sort and constants):
                FeatureTerm u = variableUnification(r.f1,r.f2,dm);

                if (u!=null) {
                    // remove the second variable:
                    n.variables.remove(r.f1);
                    n.variables.remove(r.f2);
                    n.variables.add(u);

                    if (n.root==r.f1) n.root = u;
                    if (n.root==r.f2) n.root = u;

                    // substitute all the appearances of f1 and f2 by their unification:
                    for(Feature f:n.features) {
                        if (f.f1==r.f1) f.f1=u;
                        if (f.f2==r.f1) f.f2=u;
                        if (f.f1==r.f2) f.f1=u;
                        if (f.f2==r.f2) f.f2=u;
                    }
                    for(Restriction r2:n.restrictions) {
                        if (r2.f1==r.f1) r2.f1=u;
                        if (r2.f2==r.f1) r2.f2=u;
                        if (r2.f1==r.f2) r2.f1=u;
                        if (r2.f2==r.f2) r2.f2=u;
                    }

                    // remove repeated feature restrictions:
                    List<Feature> toDelete = new LinkedList<Feature>();
                    for(Feature f1:n.features) {
                        for(Feature f2:n.features) {
                            if (f1!=f2) {
                                if (f1.f1==f2.f1 &&
                                    f1.f2==f2.f2 &&
                                    f1.f.equals(f2.f)) {
                                    f1.function = f1.function && f2.function;
                                    f1.size_restriction = Math.max(f1.size_restriction, f2.size_restriction);
                                    toDelete.add(f2);
                                }
                            } else {
                                break;
                            }
                        }
                    }
                    n.features.removeAll(toDelete);

                    // check for consistency:
                    boolean consistent = true;
                    for(Feature f1:n.features) {
                        int num1 = f1.size_restriction;
                        int num2 = 0;
                        for(Feature f2:n.features) {
                            if (f1.f1==f2.f1 && f1.f.equals(f2.f)) num2++;
                        }
                        if (num2<num1) {
                            // restrictions violated!!!
                            if (DEBUG>=1) {
                                System.out.println("Number restriction violated for X" + n.variables.indexOf(f1.f1) + "." + f1.f.get());
                                System.out.println("It should ne at least " + num1 + " and it's " + num2);
                            }
                            consistent = false;
                            break;
                        }
                        if (!consistent) break;
                    }

                    if (consistent) results.add(n);
                }

            } else {
                results.add(this);
            }
            return results;
        }


        // checks for consistency:
        public boolean consistent() {

            return true;
        }

        public FeatureTerm generateResult(FTKBase dm) throws FeatureTermException {
            HashMap<FeatureTerm,FeatureTerm> variablesMap = new HashMap<FeatureTerm,FeatureTerm>();

            // recreate variables:
            for(FeatureTerm v:variables) {
                if (v.isConstant() || dm.contains(v)) {
                    variablesMap.put(v,v);
                } else {
                    variablesMap.put(v,v.getSort().createFeatureTerm());
                }
            }

            // add features:
            for(Feature f:features) {
                TermFeatureTerm v1 = (TermFeatureTerm)variablesMap.get(f.f1);
                FeatureTerm v2 = variablesMap.get(f.f2);
                if (v1==null || v2==null) {
                    System.err.println("FTUnification2.generateResult: variable mapping does not exist!!");
                }
                v1.addFeatureValue(f.f, v2);
            }

            return variablesMap.get(root);
        }
    }


    public static FeatureTerm variableUnification(FeatureTerm f1,FeatureTerm f2,FTKBase dm) throws FeatureTermException {
        Sort s1 = f1.getSort();
        Sort s2 = f2.getSort();

        if (s1==null || s2==null) return null;

        Sort su = s1.Unification(s2);

        if (su==null) return null;

        FeatureTerm u = su.createFeatureTerm();

        if (f1.isConstant() || dm.contains(f1)) {
            if (f2.isConstant() || dm.contains(f2)) {
                if (f1.equivalents(f2)) return f1;
                return null;
            } else {
                return f1;
            }
        } else {
            if (f2.isConstant() || dm.contains(f2)) {
                return f2;
            }
        }

        return u;
    }


    public static List<FeatureTerm> unificationRemovingDuplicates(FeatureTerm f1,FeatureTerm f2,FTKBase dm) throws FeatureTermException {
        List<FeatureTerm> unifications = unification(f1,f2,dm);
        List<FeatureTerm> unificationsFiltered = new LinkedList<FeatureTerm>();

        for(FeatureTerm u:unifications) {
            boolean found = false;
            for(FeatureTerm u2:unificationsFiltered) {
                if (u.equivalents(u2)) {
                    found = true;
                    break;
                }
            }
            if (!found) unificationsFiltered.add(u);
        }

        return unificationsFiltered;
    }

    public static FeatureTerm simpleUnification(FeatureTerm f1,FeatureTerm f2,FTKBase dm) throws FeatureTermException {
       UnificationNode start = new UnificationNode(f1, f2, dm);
        List<UnificationNode> stack = new LinkedList<UnificationNode>();
        stack.add(start);

        while(!stack.isEmpty()) {
            UnificationNode n = stack.remove(0);
            if (DEBUG>=1) {
                System.out.println("Current:");
                n.print(dm);
            }

            List<UnificationNode> r = n.applyRestriction(dm);
            List<UnificationNode> toDelete = new LinkedList<UnificationNode>();

            // check for result:
            for(UnificationNode n2:r) {
                if (n2.restrictions.isEmpty()) {
                    // it's a result!!!
                    FeatureTerm res = n2.generateResult(dm);
                    toDelete.add(n2);

                    if (DEBUG>=1) {
                        System.out.println("We've got a result:");
                        n2.print(dm);
                        System.out.println(res.toStringNOOS(dm));
                   }

                   return res;
                }
            }
            r.removeAll(toDelete);

            if (r!=null) {
                stack.addAll(r);
            }
        }
        
        return null;
    }


    public static List<FeatureTerm> unification(FeatureTerm f1,FeatureTerm f2,FTKBase dm) throws FeatureTermException {
        List<FeatureTerm> results = new LinkedList<FeatureTerm>();
        UnificationNode start = new UnificationNode(f1, f2, dm);
        List<UnificationNode> stack = new LinkedList<UnificationNode>();
        stack.add(start);

        while(!stack.isEmpty()) {
            UnificationNode n = stack.remove(0);
            if (DEBUG>=1) {
                System.out.println("Current:");
                n.print(dm);
            }

            List<UnificationNode> r = n.applyRestriction(dm);
            List<UnificationNode> toDelete = new LinkedList<UnificationNode>();

            // check for result:
            for(UnificationNode n2:r) {
                if (n2.restrictions.isEmpty()) {
                    // it's a result!!!
                    FeatureTerm res = n2.generateResult(dm);
                    results.add(res);
                    toDelete.add(n2);

                    if (DEBUG>=1) {
                        System.out.println("We've got a result:");
                        n2.print(dm);
                        System.out.println(res.toStringNOOS(dm));
                   }
                }
            }
            r.removeAll(toDelete);

            if (r!=null) {
                stack.addAll(r);
            }
        }

        return results;
    }


    // Note: this method has the problem that the generalization operator is not complete,
    //       and thus, it might not be accurate....
    public static boolean isUnification(FeatureTerm u,FeatureTerm f1,FeatureTerm f2, FTKBase dm, Ontology o) throws FeatureTermException {
        if (!f1.subsumes(u)) return false;
        if (!f2.subsumes(u)) return false;

        List<FeatureTerm> gs = FTRefinement.getGeneralizationsAggressive(u, dm, o);
        for(FeatureTerm g:gs) {
            if (u.subsumes(g)) {
                System.err.println("isUnification: term subsumes generalization refinement!!!!!");
            } else {
                if (f1.subsumes(g) && f2.subsumes(g)) return false;
            }
        }
        return true;
    }
}
