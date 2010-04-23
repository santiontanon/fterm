package fterms;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import fterms.exceptions.FeatureTermException;

public class FTUnification {

    public static int DEBUG = 0;

    static class UnificationBinding {

        public UnificationBinding(FeatureTerm f1, FeatureTerm f2, FeatureTerm u) {
            m_f1 = f1;
            m_f2 = f2;
            m_u = u;
        }

        UnificationBinding(UnificationBinding b) {
            m_f1 = b.m_f1;
            m_f2 = b.m_f2;
            m_u = b.m_u;
        }
        FeatureTerm m_f1, m_f2, m_u;
    }

    static class UnificationResult {

        public UnificationResult() {
            m_u = null;
        }

        public UnificationResult(UnificationResult r) {
            m_u = r.m_u;
            m_bindings.addAll(r.m_bindings);
            m_stack.addAll(r.m_stack);
        }

        UnificationResult clone(FTKBase dm) throws FeatureTermException {
            UnificationResult r = new UnificationResult();
            HashMap<FeatureTerm, FeatureTerm> mapping = new HashMap<FeatureTerm, FeatureTerm>();

            r.m_u = m_u.clone(dm, mapping);

            for (UnificationBinding b : m_bindings) {
                r.m_bindings.add(new UnificationBinding(b.m_f1, b.m_f2, b.m_u.clone(dm, mapping)));
            }
            for (FeatureTerm u : m_stack) {
                r.m_stack.add(u.clone(dm, mapping));
            }

            return r;
        }

        void substitute(FeatureTerm f1, FeatureTerm f2) {

            if (m_u != null) {
                m_u.substitute(f1, f2);
            }
            for (UnificationBinding b : m_bindings) {
                b.m_u.substitute(f1, f2);
            }
            for (FeatureTerm u : m_stack) {
                u.substitute(f1, f2);
            }
        }

        public String toStringNOOS(FTKBase dm) {
            String ret = "<UnificationResult>\n";
            ret += "  <m_u>\n";
            ret += "    " + m_u.toStringNOOS(dm, 4) + "\n";
            ret += "  </m_u>\n";
            ret += "  <m_bindings>\n";
            for (UnificationBinding b : m_bindings) {
                ret += "    <binding>\n";
                if (b.m_f1 != null) {
                    ret += "      " + b.m_f1.toStringNOOS(dm, 6) + "\n";
                } else {
                    ret += "      null\n";
                }
                if (b.m_f2 != null) {
                    ret += "      " + b.m_f2.toStringNOOS(dm, 6) + "\n";
                } else {
                    ret += "      null\n";
                }
                if (b.m_u != null) {
                    ret += "      " + b.m_u.toStringNOOS(dm, 6) + "\n";
                } else {
                    ret += "      null\n";
                }
                ret += "    </binding>\n";
            }
            ret += "  </m_bindings>\n";
            ret += "  <m_stack>\n";
            for (FeatureTerm t : m_stack) {
                if (t != null) {
                    ret += "    " + t.toStringNOOS(dm, 4) + "\n";
                } else {
                    ret += "    null\n";
                }
            }
            ret += "  </m_stack>\n";
            ret += "</UnificationResult>\n";
            return ret;
        }
        FeatureTerm m_u;
        List<UnificationBinding> m_bindings = new LinkedList<UnificationBinding>();
        List<FeatureTerm> m_stack = new LinkedList<FeatureTerm>();
    };

    public static FeatureTerm simpleUnification(FeatureTerm f1, FeatureTerm f2, Ontology ontology, FTKBase domain_model) throws FeatureTermException {
        List<FeatureTerm> results = unification(f1, f2, ontology, domain_model, true);
        FeatureTerm result = null;

        if (results != null) {
            if (results.size() > 0) {
                result = results.remove(0);
            }
        } // if

        return result;
    } // simple_unification

    public static List<FeatureTerm> unification(FeatureTerm f1, FeatureTerm f2, Ontology ontology, FTKBase domain_model, boolean singleton) throws FeatureTermException {

        // separability check:
        if (// false &&
            (f1 instanceof TermFeatureTerm) &&
            (f2 instanceof TermFeatureTerm)) { // separation_test
            Sort s = null;
            boolean common;

            s = f1.getSort().Unification(f2.getSort());
            
            if (s != null && s.getDataType() == Sort.DATATYPE_FEATURETERM) {
                FeatureTerm ft_tmp;
                List<FeatureTerm> objects = new LinkedList<FeatureTerm>();
                boolean separable = true;

                objects.add(f1);
                objects.add(f2);

                for (Symbol fn : s.getFeatures()) {
                    if (!s.featureSingleton(fn) || 
                        !FTAntiunification.separableFeature(fn, objects, domain_model)) {
                        separable = false;
                        break;
                    }
                } // for

                if ((f1==null || f1.isLeaf()) &&
                    (f1==null || f2.isLeaf())) separable = false;

                if (separable) {
                    List<FeatureTerm> uResults = new LinkedList<FeatureTerm>();
                    List<FeatureTerm> uResultsTmp = new LinkedList<FeatureTerm>();
                    uResults.add(s.createFeatureTerm());
                    for (Symbol fn : s.getFeatures()) {
                        FeatureTerm v1 = f1.featureValue(fn);
                        FeatureTerm v2 = f2.featureValue(fn);

                        if (v1!=null || v2!=null) {
//                            System.out.println("Recursivelly calling unification for:");
//                            System.out.println("v1:" + (v1==null ? "null":v1.toStringNOOS(domain_model)));
//                            System.out.println("v2:" + (v2==null ? "null":v2.toStringNOOS(domain_model)));
                            List<FeatureTerm> tmpResults = unification(v1,v2,ontology,domain_model, s.featureSingleton(fn));

//                            System.out.println("partial unification result for '"+ fn.get()  +"': " + uResults.size() + " - " + tmpResults.size());
//                            for(FeatureTerm tmp:tmpResults) {
//                                System.out.println(tmp.toStringNOOS(domain_model));
//                            }

                            if (tmpResults==null) return new LinkedList<FeatureTerm>();

                            // Filter results:
                            {
                                List<FeatureTerm> toDelete = new LinkedList<FeatureTerm>();
                                int l = tmpResults.size();

//                                if (l>1) {
//                                    System.out.println("Filtering unification results: " + l);
//                                    System.out.flush();
//                                }

                                for(int i = 0;i<l;i++) {
                                    FeatureTerm tmpf1 = tmpResults.get(i);
                                    if (!toDelete.contains(tmpf1)) {
                                        for(int j = i+1;j<l;j++) {
                                            FeatureTerm tmpf2 = tmpResults.get(j);
                                            if (tmpf2.subsumes(tmpf1)) {
                                                toDelete.add(tmpf1);
                                            } else {
                                                if (tmpf1.subsumes(tmpf2)) toDelete.add(tmpf2);
                                            }
                                        }
                                    }
                                }
                                tmpResults.removeAll(toDelete);

/*
                                if (tmpResults.size()>=4) {
                                    System.out.println(tmpResults.size() + " * " + uResults.size() + "(removed " + toDelete.size() + ")");
                                    System.out.println(v1.toStringNOOS(domain_model));
                                    System.out.println(v2.toStringNOOS(domain_model));
                                    System.out.println("Results:");
                                    for(FeatureTerm r:tmpResults) {
                                        System.out.println(r.toStringNOOS(domain_model));
                                    }
                                    System.out.flush();
//                                    System.exit(1);
                                }
*/
                            }


                            for(FeatureTerm t2:uResults) {
                                if (tmpResults.size()==1) {
                                    if (!s.featureSingleton(fn) &&
                                        v1!=null && v2!=null &&
                                        !(v1 instanceof SetFeatureTerm) &&
                                        !(v2 instanceof SetFeatureTerm)) {
                                        // If a set is possible in this feature, there is a chance of having a set oas the unification too:
                                        SetFeatureTerm sft = new SetFeatureTerm();
                                        sft.addSetValue(v1);
                                        sft.addSetValue(v2);
                                        if (tmpResults.get(0).subsumes(sft)) {
                                            // Only one unification is valid:
                                            ((TermFeatureTerm)t2).defineFeatureValue(fn, tmpResults.get(0));
                                            uResultsTmp.add(t2);
                                        } else {
                                            // there are 2 alternative unifications:
                                            FeatureTerm t2c = t2.clone(domain_model,ontology);
                                            ((TermFeatureTerm)t2c).defineFeatureValue(fn, tmpResults.get(0));
                                            uResultsTmp.add(t2c);
                                            t2c = t2.clone(domain_model,ontology);
                                            ((TermFeatureTerm)t2c).defineFeatureValue(fn, sft);
                                            uResultsTmp.add(t2c);
                                        }
                                    } else {
                                        ((TermFeatureTerm)t2).defineFeatureValue(fn, tmpResults.get(0));
                                        uResultsTmp.add(t2);
                                    }
                                } else {
//                                    if (!s.featureSingleton(fn) &&
//                                        v1!=null && v2!=null &&
//                                        !(v1 instanceof SetFeatureTerm) &&
//                                        !(v2 instanceof SetFeatureTerm)) {
                                        // TODO: There is a lot of combinatorics to take care in this case...
                                        // Do it!
//                                    } else {
                                        for(FeatureTerm t1:tmpResults) {
                                            FeatureTerm t2c = t2.clone(domain_model,ontology);
                                            ((TermFeatureTerm)t2c).defineFeatureValue(fn, t1);
                                            uResultsTmp.add(t2c);
                                        }
//                                    }
                                }
                            }
                            uResults.clear();
                            uResults.addAll(uResultsTmp);
                            uResultsTmp.clear();
                        }
                    }

                    return uResults;
                }
            } // if
        } // if check_separability


        List<UnificationResult> l;
        List<FeatureTerm> result = null;
        List<UnificationBinding> bindings = new LinkedList<UnificationBinding>();
        List<FeatureTerm> stack = new LinkedList<FeatureTerm>();

        if ((f1 instanceof SetFeatureTerm) ||
            (f2 instanceof SetFeatureTerm)) {
            l = set_internal_unification(f1, f2, bindings, stack, ontology, domain_model, singleton);
        } else {
            l = ft_internal_unification(f1, f2, bindings, stack, ontology, domain_model);
        }

        if (l != null && !l.isEmpty()) {
            result = new LinkedList<FeatureTerm>();
            for (UnificationResult r : l) {
                result.add(r.m_u);
            }
        } // if

        return result;
    } // unification


    // Generates a new unification result, making sure it is not subsumed by any other unification:
    static void addUnificationResult(List<UnificationResult> lr,
                                     FeatureTerm u,
                                     List<UnificationBinding> bindings,
                                     UnificationBinding extraBinding,
                                     List<FeatureTerm> stack,
                                     FeatureTerm extraStack) throws FeatureTermException {
        // subsumption test:
        List<UnificationResult> toDelete = new LinkedList<UnificationResult>();
        for(UnificationResult r2:lr) {
            if (r2.m_u.subsumes(u)) {
                System.out.println("addUnificationResult: not added!");
                return;
            }
            if (u.subsumes(r2.m_u)) toDelete.add(r2);
        }
        lr.removeAll(toDelete);
        if (toDelete.size()>0) System.out.println("addUnificationResult: " + toDelete.size() + " removed");
        if (lr.size()>0) System.out.println("addUnificationResult: " + lr.size() + " kept");

        UnificationResult r = new UnificationResult();
        r.m_u = u;
        r.m_bindings.addAll(bindings);
        if (extraBinding!=null) r.m_bindings.add(extraBinding);
        r.m_stack.addAll(stack);
        if (extraStack!=null) r.m_stack.add(0, extraStack);
        lr.add(r);
    }

    
    static List<UnificationResult> ft_internal_unification(FeatureTerm f1, FeatureTerm f2, List<UnificationBinding> bindings, List<FeatureTerm> stack, Ontology ontology, FTKBase domain_model) throws FeatureTermException {
        List<UnificationResult> lr;
        FeatureTerm u = null;

        if (DEBUG >= 1) {
            System.out.println("ft_internal_unification: bindings " + bindings.size());
        }
        if (DEBUG >= 2) {
            System.out.println("f1:\n" + (f1 == null ? "null" : f1.toStringNOOS(domain_model)));
        }
        if (DEBUG >= 2) {
            System.out.println("f2:\n" + (f2 == null ? "null" : f2.toStringNOOS(domain_model)));
        }

        // Check bindings:
        {
            for (UnificationBinding b : bindings) {
                if ((b.m_f1 == null || !(b.m_f1 instanceof SetFeatureTerm)) &&
                    (b.m_f2 == null || !(b.m_f2 instanceof SetFeatureTerm))) {
                    // It is important not to use "equals" for feature terms here, since we want to find equality of pointers
                    if (f1 != null && f2 != null &&
                        b.m_f1 != null && b.m_f2 != null &&
                        b.m_f1 == f1 && b.m_f2 == f2) {
                        lr = new LinkedList<UnificationResult>();
                        addUnificationResult(lr,b.m_u,bindings,null,stack,null);

                        if (DEBUG >= 1) {
                            System.out.println("ft_internal_unification: succeeded[BF] and returned " + lr.size() + " results.");
                        }
                        if (DEBUG >= 2) {
                            for (UnificationResult r2 : lr) {
                                System.out.println(r2.toStringNOOS(domain_model));
                            }
                        }
                        return lr;
                    } // if

                    // It is important not to use "equals" for feature terms here, since we want to find equality of pointers
                    if (f1 != null && b.m_f1 != null && b.m_f1 == f1) {
                        if (f2 == null || f2.subsumes(b.m_u)) {
                            lr = new LinkedList<UnificationResult>();
                            addUnificationResult(lr,b.m_u,bindings,new UnificationBinding(f1, f2, b.m_u),stack,null);
 
                            if (DEBUG >= 1) {
                                System.out.println("ft_internal_unification: succeeded[B<f1>F] and returned " + lr.size() + " results.");
                            }
                            if (DEBUG >= 2) {
                                for (UnificationResult r2 : lr) {
                                    System.out.println(r2.toStringNOOS(domain_model));
                                }
                            }
                            return lr;
                        } else {
                            if (DEBUG >= 1) {
                                System.out.println("ft_internal_unification: found a binding<f1>, but it requires further refinement...");
                            }
                            {
                                int pos = bindings.indexOf(b);
                                UnificationBinding b2;
                                FeatureTerm old_ft, new_ft;

                                if (b.m_u.equals(f1)) {
                                    if (DEBUG >= 1) {
                                        System.out.println("ft_internal_unification: returning null");
                                    }
                                    return null;
                                }
                                lr = ft_internal_unification(b.m_u, f2, bindings, stack, ontology, domain_model);
                                if (lr == null) {
                                    if (DEBUG >= 1) {
                                        System.out.println("ft_internal_unification: returning null");
                                    }
                                    return null;
                                }

                                for (UnificationResult r3 : lr) {
                                    b2 = r3.m_bindings.get(pos);
                                    old_ft = b2.m_u;
                                    new_ft = r3.m_u;

                                    r3.substitute(old_ft, new_ft);
                                } // while

                                if (DEBUG >= 1) {
                                    System.out.println("ft_internal_unification: refinement finished...");
                                }
                                if (DEBUG >= 2) {
                                    for (UnificationResult r2 : lr) {
                                        System.out.println(r2.toStringNOOS(domain_model));
                                    }
                                }
                                return lr;
                            }
                        } // if
                    } // if

                    if (f2 != null && b.m_f2 != null && b.m_f2 == f2) {
                        if (f1 == null || f1.subsumes(b.m_u)) {
                            lr = new LinkedList<UnificationResult>();
                            addUnificationResult(lr, b.m_u, bindings,
                                                 new UnificationBinding(f1, f2, b.m_u), stack,null);

                            if (DEBUG >= 1) {
                                System.out.println("ft_internal_unification: succeeded[B<f2>F] and returned " + lr.size() + " results.");
                            }
                            if (DEBUG >= 2) {
                                for (UnificationResult r2 : lr) {
                                    System.out.println(r2.toStringNOOS(domain_model));
                                }
                            }
                            return lr;
                        } else {
                            if (DEBUG >= 1) {
                                System.out.println("ft_internal_unification: found a binding<f2>, but it requires further refinement...");
                            }
                            {
                                int pos = bindings.indexOf(b);
                                UnificationBinding b2;
                                FeatureTerm old_ft, new_ft;

                                if (b.m_u.equals(f2)) {
                                    if (DEBUG >= 1) {
                                        System.out.println("ft_internal_unification: returning null");
                                    }
                                    return null;
                                }
                                lr = ft_internal_unification(f1, b.m_u, bindings, stack, ontology, domain_model);
                                if (lr == null) {
                                    if (DEBUG >= 1) {
                                        System.out.println("ft_internal_unification: returning null");
                                    }
                                    return null;
                                }

                                for (UnificationResult r3 : lr) {
                                    b2 = r3.m_bindings.get(pos);
                                    old_ft = b2.m_u;
                                    new_ft = r3.m_u;
                                    r3.substitute(old_ft, new_ft);
                                } // for

                                if (DEBUG >= 1) {
                                    System.out.println("ft_internal_unification: refinement finished...");
                                }
                                if (DEBUG >= 2) {
                                    for (UnificationResult r2 : lr) {
                                        System.out.println(r2.toStringNOOS(domain_model));
                                    }
                                }
                                return lr;
                            }
                        } // if
                    } // if
                } // if
            } // while
        }

        {
            Sort s = null;

            if (f1 != null && f1 instanceof SpecialFeatureTerm) {
                System.err.println("Unification with SpecialTerms not yet supported!");
                return null;
            } // if

            if (f2 != null && f2 instanceof SpecialFeatureTerm) {
                System.err.println("Unification with SpecialTerms not yet supported!");
                return null;
            } // if

            if (f1 == null) {
                s = f2.getSort();
            }
            if (f2 == null) {
                s = f1.getSort();
            }
            if (s == null) {
                s = f1.getSort().Unification(f2.getSort());
            }

            if (s != null) {
                if (DEBUG >= 1) {
                    System.out.println("ft_internal_unification: unification sort will be " + s.get());
                }

                u = s.createFeatureTerm();

                if ((f1 != null && f1.getName() != null) || (f2 != null && f2.getName() != null)) {

                    if (f1!=null && domain_model.contains(f1)) {
                        if (f2==null || f2.subsumes(f1)) {
                            u = f1;
                            lr = new LinkedList<UnificationResult>();
                            addUnificationResult(lr, u, bindings,
                                                 new UnificationBinding(f1, f2, u), stack,null);

                            if (DEBUG >= 1) {
                                System.out.println("ft_internal_unification: succeeded (domain) and returned " + lr.size() + " results");
                            }
                            if (DEBUG >= 2) {
                                for (UnificationResult r2 : lr) {
                                    System.out.println(r2.toStringNOOS(domain_model));
                                }
                            }
                            return lr;
                        }
                    }
                    if (f2!=null && domain_model.contains(f2)) {
                        if (f1==null || f1.subsumes(f2)) {
                            u = f2;
                            lr = new LinkedList<UnificationResult>();
                            addUnificationResult(lr, u, bindings,
                                                 new UnificationBinding(f1, f2, u), stack,null);

                            if (DEBUG >= 1) {
                                System.out.println("ft_internal_unification: succeeded (domain) and returned " + lr.size() + " results");
                            }
                            if (DEBUG >= 2) {
                                for (UnificationResult r2 : lr) {
                                    System.out.println(r2.toStringNOOS(domain_model));
                                }
                            }
                            return lr;
                        }
                    }

                    if ((f1 != null && f1.getName() != null) && (f2 != null && f2.getName() != null)) {
                        if (f1.getName().equals(f2.getName())) {
                            u.setName(new Symbol(f1.getName()));
                        } else {
                            u = null;
                        } // if
                    } else {
                        if (f1 != null && f1.getName() != null) {
                            u.setName(new Symbol(f1.getName()));
                        } else {
                            u.setName(new Symbol(f2.getName()));
                        } // if
                    } // if
                } // if

                if (u != null) {
                    if (u instanceof IntegerFeatureTerm) {
                        if ((f1 != null && !(f1 instanceof IntegerFeatureTerm)) ||
                            (f2 != null && !(f2 instanceof IntegerFeatureTerm))) {
                            // This might happen when unifying an abstract number (e.g. (define (number)) with an integer
                            u = integer_unification(((f1 != null && !(f1 instanceof IntegerFeatureTerm)) ? null : (IntegerFeatureTerm) f1),
                                ((f2 != null && !(f2 instanceof IntegerFeatureTerm)) ? null : (IntegerFeatureTerm) f2), (IntegerFeatureTerm) u, ontology);
                        } else {
                            u = integer_unification((IntegerFeatureTerm) f1, (IntegerFeatureTerm) f2, (IntegerFeatureTerm) u, ontology);
                        }
                        if (u != null) {
                            lr = new LinkedList<UnificationResult>();

                            addUnificationResult(lr, u, bindings,
                                                 new UnificationBinding(f1, f2, u), stack,null);

                            if (DEBUG >= 1) {
                                System.out.println("ft_internal_unification: succeeded (integer) and returned " + lr.size() + " results");
                            }
                            if (DEBUG >= 2) {
                                for (UnificationResult r2 : lr) {
                                    System.out.println(r2.toStringNOOS(domain_model));
                                }
                            }
                            return lr;
                        } // if
                    } else if (u instanceof FloatFeatureTerm) {
                        if ((f1 != null && !(f1 instanceof FloatFeatureTerm)) ||
                            (f2 != null && !(f2 instanceof FloatFeatureTerm))) {
                            // This might happen when unifying an abstract number (e.g. (define (number)) with a float
                            u = float_unification(((f1 != null && !(f1 instanceof FloatFeatureTerm)) ? null : (FloatFeatureTerm) f1),
                                ((f2 != null && !(f2 instanceof FloatFeatureTerm)) ? null : (FloatFeatureTerm) f2), (FloatFeatureTerm) u, ontology);
                        } else {
                            u = float_unification((FloatFeatureTerm) f1, (FloatFeatureTerm) f2, (FloatFeatureTerm) u, ontology);
                        }
                        if (u != null) {
                            lr = new LinkedList<UnificationResult>();
                            addUnificationResult(lr, u, bindings,
                                                 new UnificationBinding(f1, f2, u), stack,null);

                            if (DEBUG >= 1) {
                                System.out.println("ft_internal_unification: succeeded (float) and returned " + lr.size() + " results");
                            }
                            if (DEBUG >= 2) {
                                for (UnificationResult r2 : lr) {
                                    System.out.println(r2.toStringNOOS(domain_model));
                                }
                            }
                            return lr;
                        } // if
                    } else if (u instanceof SymbolFeatureTerm) {
                        if (f1 == null || f1.getDataType() == -1 || ((SymbolFeatureTerm) f1).getValue() == null) {
                            u = f2.clone(domain_model, ontology);
                            lr = new LinkedList<UnificationResult>();
                            if (f1!=null) {
                                addUnificationResult(lr, u, bindings,
                                                     new UnificationBinding(f1, f2, u), stack,null);
                            } else {
                                addUnificationResult(lr, u, bindings,
                                                     null, stack,null);
                            }

                            if (DEBUG >= 1) {
                                System.out.println("ft_internal_unification: succeeded (null symbols) and returned " + lr.size() + " results");
                            }
                            if (DEBUG >= 2) {
                                for (UnificationResult r2 : lr) {
                                    System.out.println(r2.toStringNOOS(domain_model));
                                }
                            }
                            return lr;
                        } // if

                        if (f2 == null || f2.getDataType() == -1 || ((SymbolFeatureTerm) f2).getValue() == null) {
                            u = f1.clone(domain_model, ontology);
                            lr = new LinkedList<UnificationResult>();
                            if (f2!=null) {
                                addUnificationResult(lr, u, bindings,
                                                     new UnificationBinding(f1, f2, u), stack,null);
                            } else {
                                addUnificationResult(lr, u, bindings,
                                                     null, stack,null);
                            }

                            if (DEBUG >= 1) {
                                System.out.println("ft_internal_unification: succeeded (null or abstract) and returned " + lr.size() + " results");
                            }
                            if (DEBUG >= 2) {
                                for (UnificationResult r2 : lr) {
                                    System.out.println(r2.toStringNOOS(domain_model));
                                }
                            }
                            return lr;
                        } // if

                        if (((SymbolFeatureTerm) f1).getValue().equals(((SymbolFeatureTerm) f2).getValue())) {
                            u = new SymbolFeatureTerm(((SymbolFeatureTerm) f1).getValue(), ontology);
                            u.setSort(s);
                            lr = new LinkedList<UnificationResult>();
                            addUnificationResult(lr, u, bindings,
                                                 new UnificationBinding(f1, f2, u), stack,null);

                            if (DEBUG >= 1) {
                                System.out.println("ft_internal_unification: succeeded (symbol) and returned " + lr.size() + " results");
                            }
                            if (DEBUG >= 2) {
                                for (UnificationResult r2 : lr) {
                                    System.out.println(r2.toStringNOOS(domain_model));
                                }
                            }
                            return lr;
                        } // if
                    } else if (u instanceof TermFeatureTerm) {
                        List<UnificationResult> lr2, lr3;
                        List<Symbol> features = new LinkedList<Symbol>();
                        int i;

                        {
                            lr = new LinkedList<UnificationResult>();
                            addUnificationResult(lr, u,
                                                 bindings, new UnificationBinding(f1, f2, u),
                                                 stack, u);
                        }

                        {
                            List<Symbol> l = s.getFeatures();

                            for (Symbol f : l) {
                                if ((f1 != null && f1.featureValue(f) != null) ||
                                    (f2 != null && f2.featureValue(f) != null)) {
                                    features.add(f);
                                } // if
                            } // while
                        }

                        if (DEBUG >= 1) {
                            System.out.println("ft_internal_unification: considering TermFeatureTerm... " + s.get() + " with features " + features);
                        }

                        for (Symbol f : features) {
                            lr3 = new LinkedList<UnificationResult>();

                            i = 0;
                            for (UnificationResult result : lr) {
                                if (DEBUG >= 1) {
                                    System.out.println("ft_internal_unification: considering feature " + f.get() + " (with bindings " + (i + 1) + "/" + lr.size() + ")");
                                }
                                i++;

                                lr2 = set_internal_unification((f1 == null ? null : f1.featureValue(f)),
                                    (f2 == null ? null : f2.featureValue(f)), (result.m_bindings), (result.m_stack), ontology, domain_model,
                                    s.featureSingleton(f));
                                if (lr2 != null) {
                                    for (UnificationResult r2 : lr2) {
                                        // look for the proper clone of "r.m_u":
                                        {
                                            FeatureTerm u2;
//											if (r2.m_stack.get(0).getDataType()==Sort.DATATYPE_SET) r2.m_stack.remove(0);
                                            u2 = r2.m_stack.get(0);

                                            ((TermFeatureTerm) u2).defineFeatureValue(f, r2.m_u);
                                            r2.m_u = u2;
                                        }

                                        lr3.add(r2);
                                    } // while
                                } // if
                            } // while
                            lr = lr3;

                        } // for

                        // Filter the results for not having repeated ones:
                        {
                            List<UnificationResult> to_delete = new LinkedList<UnificationResult>();
                            List<UnificationResult> good_ones = new LinkedList<UnificationResult>();

                            for (UnificationResult r : lr) {
                                r.m_stack.remove(0); // pop the TermFeatureTerm from the stack.
                            }
                            for (UnificationResult r : lr) {
                                if (!to_delete.contains(r)) {
                                    // If the stack is empty, that means we are in the root, so we should filter the unifications that
                                    // actually are not unifications:
                                    if (!r.m_stack.isEmpty() || ((f1 == null || f1.subsumes(r.m_u)) && (f2 == null || f2.subsumes(r.m_u)))) {
                                        for (UnificationResult r2 : lr) {
                                            if (r != r2 && !to_delete.contains(r2)) {
                                                if (r.m_u.subsumes(r2.m_u)) {
                                                    to_delete.add(r2);
                                                } else {
                                                    if (r2.m_u.subsumes(r.m_u)) {
                                                        if (!to_delete.contains(r)) {
                                                            if (!r.m_stack.isEmpty() || ((f1 == null || f1.subsumes(r2.m_u)) && (f2 == null || f2.subsumes(r2.m_u)))) {
                                                                to_delete.add(r);
                                                            }
                                                        }
                                                    }
                                                } // if
                                            }
                                        } // while
                                    } else {
                                        if (!to_delete.contains(r)) {
                                            to_delete.add(r);
                                        }
                                    }
                                }
                            }
                            for (UnificationResult r : lr) {
                                if (!to_delete.contains(r)) {
                                    good_ones.add(r);
                                }
                            }

                            lr.clear();

                            if (DEBUG >= 2) {
                                System.out.println("Removed the following unifications:");
                                for (UnificationResult r2 : to_delete) {
                                    System.out.println(r2.m_u.toStringNOOS(domain_model));
                                }
                                System.out.println("The following unifications were kept:");
                                for (UnificationResult r2 : good_ones) {
                                    System.out.println(r2.m_u.toStringNOOS(domain_model));
                                }
                            }

                            lr.addAll(good_ones);
                        }

                        if (DEBUG >= 1) {
                            System.out.println("ft_internal_unification: succeeded (term) and returned " + lr.size() + " results");
                        }
                        if (DEBUG >= 2) {
                            for (UnificationResult r2 : lr) {
                                System.out.println(r2.toStringNOOS(domain_model));
                            }
                        }
                        return lr;
                    } else if (u.getDataType() == Sort.DATATYPE_ABSTRACT) {
//						List<Symbol> features;

                        lr = new LinkedList<UnificationResult>();
                        addUnificationResult(lr, u, bindings,
                                             new UnificationBinding(f1, f2, u), stack,null);

                        if (DEBUG >= 1) {
                            System.out.println("ft_internal_unification: succeeded (abstract) and returned " + lr.size() + " results");
                        }
                        if (DEBUG >= 2) {
                            for (UnificationResult r2 : lr) {
                                System.out.println(r2.toStringNOOS(domain_model));
                            }
                        }
                        return lr;
                    } // if

                } // if (u!=0)
            } else {
                if (DEBUG >= 1) {
                    System.out.println("ft_internal_unification: sort is null!!!!");
                }
            }
        }

        if (DEBUG >= 1) {
            System.out.println("ft_internal_unification: unification failed....");
        }
        return null;
    } // ft_internal_unification

    static List<UnificationResult> set_internal_unification(FeatureTerm f1, FeatureTerm f2, List<UnificationBinding> bindings, List<FeatureTerm> stack, Ontology ontology, FTKBase domain_model, boolean singleton) throws FeatureTermException {
        int i, j;
        int size;
        List<FeatureTerm> v1 = new LinkedList<FeatureTerm>(), v2 = new LinkedList<FeatureTerm>();
        int assignments1[], assignments2[];
        int assignments2b[];
        int max_size = 0;

        List<UnificationResult> initial_lr, lr, lr2, lr3, final_lr;

        if (DEBUG >= 1) {
            System.out.println("set_internal_unification: bindings " + bindings.size());
        }

        // Check bindings:
        {
            for (UnificationBinding b : bindings) {
                if (f1 != null && f2 != null &&
                    b.m_f1 != null && b.m_f2 != null &&
                    b.m_f1 == f1 && b.m_f2 == f2) {
                    lr = new LinkedList<UnificationResult>();
                    addUnificationResult(lr, b.m_u, bindings, null, stack,null);

                    if (DEBUG >= 1) {
                        System.out.println("set_internal_unification: succeeded[BF] and returned " + lr.size() + " results.");
                    }
                    if (DEBUG >= 2) {
                        for (UnificationResult r2 : lr) {
                            System.out.println(r2.toStringNOOS(domain_model));
                        }
                    }
                    return lr;
                } // if

                if (f1 != null && b.m_f1 != null && (b.m_f1 instanceof SetFeatureTerm) && b.m_f1 == f1) {
                    if (f2 == null || f2.subsumes(b.m_u)) {
                        lr = new LinkedList<UnificationResult>();
                        addUnificationResult(lr, b.m_u, bindings, new UnificationBinding(f1, f2, b.m_u), stack,null);

                        if (DEBUG >= 1) {
                            System.out.println("set_internal_unification: succeeded[B<f1>F] and returned " + lr.size() + " results.");
                        }
                        if (DEBUG >= 2) {
                            for (UnificationResult r2 : lr) {
                                System.out.println(r2.toStringNOOS(domain_model));
                            }
                        }
                        return lr;
                    } else {
                        if (DEBUG >= 1) {
                            System.out.println("set_internal_unification: found a binding<f1>, but it requires further refinement...");
                        }
                        {
                            int pos = bindings.indexOf(b);
                            UnificationBinding b2;
                            FeatureTerm old_ft, new_ft;

                            if (b.m_u.equals(f1)) {
                                if (DEBUG >= 1) {
                                    System.out.println("set_internal_unification: returning null");
                                }
                                return null;
                            }
                            lr = set_internal_unification(b.m_u, f2, bindings, stack, ontology, domain_model, singleton);
                            if (lr == null) {
                                if (DEBUG >= 1) {
                                    System.out.println("set_internal_unification: returning null");
                                }
                                return null;
                            }

                            for (UnificationResult r : lr) {
                                b2 = r.m_bindings.get(pos);
                                old_ft = b2.m_u;
                                new_ft = r.m_u;
                                r.substitute(old_ft, new_ft);

                            } // while

                            if (DEBUG >= 1) {
                                System.out.println("set_internal_unification: refinement finished...");
                            }
                            if (DEBUG >= 2) {
                                for (UnificationResult r2 : lr) {
                                    System.out.println(r2.toStringNOOS(domain_model));
                                }
                            }
                            return lr;
                        }
                    } // if
                } // if
                if (f2 != null && b.m_f2 != null && (b.m_f2 instanceof SetFeatureTerm) && b.m_f2 == f2) {
                    if (f1 == null || f1.subsumes(b.m_u)) {
                        lr = new LinkedList<UnificationResult>();
                        addUnificationResult(lr, b.m_u, bindings, new UnificationBinding(f1, f2, b.m_u), stack,null);

                        if (DEBUG >= 1) {
                            System.out.println("set_internal_unification: succeeded[B<f2>F] and returned " + lr.size() + " results.");
                        }
                        if (DEBUG >= 2) {
                            for (UnificationResult r2 : lr) {
                                System.out.println(r2.toStringNOOS(domain_model));
                            }
                        }
                        return lr;
                    } else {
                        if (DEBUG >= 1) {
                            System.out.println("set_internal_unification: found a binding<f2>, but it requires further refinement...");
                        }
                        {
                            int pos = bindings.indexOf(b);
                            UnificationBinding b2;
                            FeatureTerm old_ft, new_ft;

                            if (b.m_u.equals(f2)) {
                                if (DEBUG >= 1) {
                                    System.out.println("set_internal_unification: returning null");
                                }
                                return null;
                            }
                            lr = set_internal_unification(f1, b.m_u, bindings, stack, ontology, domain_model, singleton);
                            if (lr == null) {
                                if (DEBUG >= 1) {
                                    System.out.println("set_internal_unification: returning null");
                                }
                                return null;
                            }

                            for (UnificationResult r : lr) {
                                b2 = r.m_bindings.get(pos);
                                old_ft = b2.m_u;
                                new_ft = r.m_u;
                                r.substitute(old_ft, new_ft);

                            } // while

                            if (DEBUG >= 1) {
                                System.out.println("set_internal_unification: refinement finished...");
                            }
                            if (DEBUG >= 2) {
                                for (UnificationResult r2 : lr) {
                                    System.out.println(r2.toStringNOOS(domain_model));
                                }
                            }
                            return lr;
                        }
                    } // if
                } // if
            } // while
        }

        initial_lr = new LinkedList<UnificationResult>();
        final_lr = new LinkedList<UnificationResult>();
        {
            if ((f1 != null && (f1 instanceof SetFeatureTerm)) || (f2 != null && (f2 instanceof SetFeatureTerm))) {
                FeatureTerm tmp = new SetFeatureTerm();
                addUnificationResult(initial_lr, tmp, bindings, new UnificationBinding(f1, f2, tmp), stack, tmp);
            } else {
                FeatureTerm tmp = new SetFeatureTerm();
                addUnificationResult(initial_lr, tmp, bindings, null, stack, tmp);
            }
        }

        if (f1 != null) {
            if (f1 instanceof SetFeatureTerm) {
                v1.addAll(((SetFeatureTerm) f1).getSetValues());
            } else {
                v1.add(f1);
            } // if
        } // if
        if (f2 != null) {
            if (f2 instanceof SetFeatureTerm) {
                v2.addAll(((SetFeatureTerm) f2).getSetValues());
            } else {
                v2.add(f2);
            } // if
        } // if

        if (DEBUG >= 1) {
            System.out.println("set_internal_unification: [" + v1.size() + "," + v2.size() + "]");
        }

        if (v2.size() > v1.size()) {
            size = v2.size();
        } else {
            size = v1.size();
        }

        if (v2.size() > 0) {
            assignments2b = new int[v2.size()];
        } else {
            assignments2b = null;
        }

        max_size = v1.size() + v2.size();

        // Set max_size to 1 when the feature is NOT a relation
        if (singleton) {
            max_size = 1;
        }

        for (; size <= max_size; size++) {
            boolean more_mappings = true;

            if (DEBUG >= 1) {
                System.out.println("set_internal_unification: trying with set size " + size + "/" + max_size);
            }

            assignments1 = new int[size];
            assignments2 = new int[size];

            // The first set is mapped directly to the element of the resulting set:
            for (i = 0; i < v1.size(); i++) {
                assignments1[i] = i;
            }
            for (; i < size; i++) {
                assignments1[i] = -1;
            }

            // Start with the initial direct mapping, and iterate though all the possible mappings:
            for (i = 0; i < v2.size(); i++) {
                assignments2[i] = i;
                assignments2b[i] = i;
            } //  if
            for (; i < size; i++) {
                assignments2[i] = -1;
            }

            do {
                // Test if the assignment is valid:
                {
                    boolean valid = true;

                    if (DEBUG >= 1) {
                        System.out.println("set_internal_unification: trying assignment:");
                        System.out.print("assignments1: ");
                        for (int k = 0; k < assignments1.length; k++) {
                            System.out.print(assignments1[k] + " ");
                        }
                        System.out.println("");
                        System.out.print("assignments2: ");
                        for (int k = 0; k < assignments2.length; k++) {
                            System.out.print(assignments2[k] + " ");
                        }
                        System.out.println("");
                        if (assignments2b != null) {
                            System.out.print("assignments2b: ");
                            for (int k = 0; k < assignments2b.length; k++) {
                                System.out.print(assignments2b[k] + " ");
                            }
                            System.out.println("");
                        }
                    }


                    // test1, each new element has at least one element assigned:
                    if (valid) {
                        for (i = 0; i < size && valid; i++) {
                            if (assignments1[i] == -1 && assignments2[i] == -1) {
                                valid = false;
                            }
                        }
                    }
                    if (valid) {
                        for (i = 0; i < v2.size() && valid; i++) {
                            for (j = 0; j < v2.size() && valid; j++) {
                                if (i != j && assignments2b[i] == assignments2b[j]) {
                                    valid = false;
                                }
                            } /// for
                        } /// for
                    } // if

                    if (valid) {

                        // Unify each element of the set:
                        lr = new LinkedList<UnificationResult>();
                        for (UnificationResult r : initial_lr) {
                            lr.add(r.clone(domain_model));
                        }

                        for (j = 0; j < size; j++) {
                            FeatureTerm tmp1, tmp2;

                            lr3 = new LinkedList<UnificationResult>();

                            i = 0;
                            for (UnificationResult r : lr) {
                                if (DEBUG >= 1) {
                                    System.out.println("set_internal_unification: Considering new element " + j + " (with bindings " + (i + 1) + "/" + lr.size() + ")");
                                }
                                i++;

                                tmp1 = null;
                                if (assignments1[j] != -1) {
                                    tmp1 = v1.get(assignments1[j]);
                                }
                                tmp2 = null;
                                if (assignments2[j] != -1) {
                                    tmp2 = v2.get(assignments2[j]);
                                }

                                lr2 = ft_internal_unification(tmp1, tmp2, r.m_bindings, r.m_stack, ontology, domain_model);
                                if (lr2 != null) {
                                    while (!lr2.isEmpty()) {
                                        UnificationResult r2 = lr2.remove(0);

                                        // look for the proper clone of "r.m_u":
                                        {
                                            SetFeatureTerm u;
//											if (r2.m_stack.get(0).getDataType()!=Sort.DATATYPE_SET) r2.m_stack.remove(0);
                                            u = (SetFeatureTerm) r2.m_stack.get(0);

                                            if (u == r2.m_u) {
                                                System.err.println(f1.toStringNOOS(domain_model));
                                                System.err.println(f2.toStringNOOS(domain_model));
                                                System.err.println(r2.toStringNOOS(domain_model));
                                                throw new Error("set_internal_unification: internal bug: trying to add a set to itself!");
                                            }

                                            u.addSetValue(r2.m_u);
                                            r2.m_u = u;
                                        }

                                        lr3.add(r2);
                                    } // while
                                } // if
                            } // while
                            lr = lr3;

                        } // for

                        while (!lr.isEmpty()) {
                            UnificationResult r = lr.remove(0);
                            if ((r.m_u instanceof SetFeatureTerm) && (((SetFeatureTerm) (r.m_u)).getSetValues()).size() == 1) {
                                r.m_u = ((SetFeatureTerm) r.m_u).getSetValues().get(0);
                            } // if
                            r.m_stack.remove(0); // Pop the set from the stack

                            // Check if the new result has to be added to the list of final results:
                            {
                                boolean add = true;
                                List<UnificationResult> toDelete = new LinkedList<UnificationResult>();
                                for(UnificationResult r2:final_lr) {
                                    if (r2.m_u.subsumes(r.m_u)) {
                                        add = false;
//                                        System.out.println("final_lr: not adding!");
                                        break;
                                    }
                                    if (r.m_u.subsumes(r2.m_u)) toDelete.add(r2);
                                }
                                if (add) {
                                    final_lr.removeAll(toDelete);
//                                    if (toDelete.size()>0) System.out.println("final_lr: " + toDelete.size() + " removed");
//                                    if (final_lr.size()>0) System.out.println("final_lr: " + final_lr.size() + " kept");
                                    final_lr.add(r);
                                }
                            }


                        } // while
                        lr = null;
                    } else {
                        if (DEBUG >= 1) {
                            System.out.println("set_internal_unification: invalid assignment!");
                        }
                    } // if
                }

                // generate the next mapping:
                if (assignments2b != null) {
                    boolean end = false;

                    i = v2.size() - 1;

                    do {
                        assignments2b[i]++;
                        if (assignments2b[i] >= size) {
                            assignments2b[i] = 0;
                            i--;
                        } else {
                            end = true;
                        } // if
                    } while (!end && i >= 0);
                    if (i < 0) {
                        more_mappings = false;
                    } else {
                        for (i = 0; i < size; i++) {
                            assignments2[i] = -1;
                        }
                        for (i = 0; i < v2.size(); i++) {
                            assignments2[assignments2b[i]] = i;
                        }
                    } // if
                } else {
                    more_mappings = false;
                } // if

            } while (more_mappings);

        } // for

        if (DEBUG >= 1) {
            System.out.println("set_internal_unification: succeeded and returned " + final_lr.size() + " results.");
        }
        if (DEBUG >= 2) {
            for (UnificationResult r2 : final_lr) {
                System.out.println(r2.toStringNOOS(domain_model));
            }
        }

        if (final_lr.isEmpty()) {
            return null;
        }

        return final_lr;
    } // set_internal_unification


    static FeatureTerm integer_unification(IntegerFeatureTerm f1, IntegerFeatureTerm f2, IntegerFeatureTerm u, Ontology o) throws FeatureTermException {
        Sort s = u.getSort();

        if (f1 == null) {
            return f2.clone(o);
        }
        if (f2 == null) {
            return f1.clone(o);
        }

        if (f1.getValue() != null) {
            // specific value:
            if (f2.getValue() != null) {
                // specific value:
                if (!f2.getValue().equals(f1.getValue())) {
                    return null;
                } // if

                u = new IntegerFeatureTerm(f1.getValue(), o);
                u.setSort(s);
                return u;
            } else {
                // undefined value:
                u = new IntegerFeatureTerm(f1.getValue(), o);
                u.setSort(s);
                return u;
            } // if
        } else {
            // undefined value:
            if (f2.getValue() != null) {
                // specific value:
                u = new IntegerFeatureTerm(f2.getValue(), o);
                u.setSort(s);
                return u;
            } else {
                // undefined value:
                return u;
            } // if
        } // if
    } // integer_unification
    static FeatureTerm float_unification(FloatFeatureTerm f1, FloatFeatureTerm f2, FloatFeatureTerm u, Ontology o) throws FeatureTermException {
        Sort s = u.getSort();

        if (f1 == null) {
            return f2.clone(o);
        }
        if (f2 == null) {
            return f1.clone(o);
        }

        if (f1.getValue() != null) {
            // specific value:
            if (f2.getValue() != null) {
                // specific value:
                if (!f2.getValue().equals(f1.getValue())) {
                    return null;
                } // if

                u = new FloatFeatureTerm(f1.getValue(), o);
                u.setSort(s);
                return u;
            } else {
                u = new FloatFeatureTerm(f1.getValue(), o);
                u.setSort(s);
                return u;
            } // if
        } else {
            // undefined value:
            if (f2.getValue() != null) {
                u = new FloatFeatureTerm(f2.getValue(), o);
                u.setSort(s);
                return u;
            } else {
                return u;
            } // if
        } // if
    } // float_unification

}
