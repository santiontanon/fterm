/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fterms.subsumption;

import fterms.FTRefinement;
import fterms.FeatureTerm;
import fterms.FloatFeatureTerm;
import fterms.IntegerFeatureTerm;
import fterms.SetFeatureTerm;
import fterms.Symbol;
import fterms.SymbolFeatureTerm;
import fterms.TermFeatureTerm;
import fterms.exceptions.FeatureTermException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import util.FTSubsumptionRecord;

/**
 *
 * @author santi
 */
public class FTSubsumption {

    public static boolean subsumptionWithBindings(FeatureTerm t1, FeatureTerm t2,List<FeatureTerm> bindings_a,List<FeatureTerm> bindings_b) throws FeatureTermException {

        List<SubsumptionStackNode> stack = new LinkedList<SubsumptionStackNode>();
        SubsumptionStackNode stack_node;
        List<SubsumptionBackTrackNode> stack_backtracking = new LinkedList<SubsumptionBackTrackNode>();
        FeatureTerm f1 = null, f2 = null;
        FeatureTerm set1[] = null, set2[] = null;
        int set1l = 0, set2l = 0;
        int[] assignment = null;
        int assignment_pos = 0;
        int state;	/*	0 - normal,
        1 - already known to be false,
        2 - already known to be true,
        3 - continuing a previous started subsumption, so no initial tests needed */
        boolean res = true;
        int i;
        long start = System.currentTimeMillis();

        if (t1 == t2) {
            return true;
        }
        if (t1 == null || t2 == null) {
            return false;
        }

//        if (true) return CSPSubsumption.subsumes(t1, t2);
//        if (FTRefinement.variables(t1).size()>20) return CSPSubsumption.subsumes(t1, t2);
//        if (FTRefinement.variables(t1).size()>20 && FTRefinement.sets(t1).size()>0) return CSPSubsumption.subsumes(t1, t2);


        stack.add(0, new SubsumptionStackNode(t1, t2, 0, null, null, 0, null, 0, -1));
        while (!stack.isEmpty()) {

            stack_node = stack.remove(0);
            f1 = stack_node.m_f1;
            f2 = stack_node.m_f2;
            state = stack_node.m_state;
            assignment = stack_node.m_assignment;
            set1 = stack_node.m_set1;
            set2 = stack_node.m_set2;
            set1l = stack_node.m_set1l;
            set2l = stack_node.m_set2l;
            assignment_pos = stack_node.m_assignment_pos;

            // Initial subsumption tests: name,sort, etc.
            //			System.out.println("Subsumption: (state = " + state + ") (" + stack.size() + " remaining) (" + stack_backtracking.size() + " backtrack points)");
            //			System.out.println("f1: " + f1.toStringNOOS());
            //			System.out.println("f2: " + f2.toStringNOOS());

            if (state == 0) {
                // Test for path equalties:
//				if (f1==null || f2==null) {
//					System.err.println("WTF! f1: " + f1 + " , f2: " + f2);
//					System.err.println(this.toStringNOOS());
//				}
                if (f2 != null &&
                    (f1.getDataType() == f2.getDataType())) {
                    int pos;

                    pos = bindings_a.indexOf(f1);
                    if (pos != -1) {
                        if (bindings_b.get(pos).equals(f2)) {
                            // already known to be true:
                            state = 2;
                        } else {
                            FeatureTerm ftc = bindings_b.get(pos);
                            if (f1 instanceof IntegerFeatureTerm && f2 instanceof IntegerFeatureTerm) {
                                if (ftc instanceof IntegerFeatureTerm &&
                                    ((IntegerFeatureTerm) f2).getValue() != null &&
                                    ((IntegerFeatureTerm) f2).getValue() == ((IntegerFeatureTerm) f1).getValue()) {
                                    state = 2;
                                } else {
                                    state = 1;
                                } // if
                            } else {
                                if (f1 instanceof FloatFeatureTerm && f2 instanceof FloatFeatureTerm) {
                                    if (ftc instanceof FloatFeatureTerm &&
                                        ((FloatFeatureTerm) f2).getValue() != null &&
                                        ((FloatFeatureTerm) f2).getValue() == ((FloatFeatureTerm) f1).getValue()) {
                                        state = 2;
                                    } else {
                                        state = 1;
                                    } // if
                                } else {
                                    state = 1;
                                }
                            }
                        } // if
                    } // if
                } // if
                //				System.out.println("path test completed...");
            } // if

            if (state == 0) {
                // Test names:
                if (f1.getName() != null) {
                    if (f2.getName() == null) {
                        if (!(f2 instanceof SetFeatureTerm)) {
                            state = 1;
                        } // if
                    } else {
                        if (f2.getName().equals(f1.getName())) {
                            state = 2;
                        } else {
                            state = 1;
                        }
                    } // if
                } // if
                //				System.out.println("name test completed...");
            } // if

            if (state == 0) {
                // Test sorts:
                if (!(f1 instanceof SetFeatureTerm) && !(f2 instanceof SetFeatureTerm) &&
                    f1.getSort() != null && f2.getSort() != null &&
                    !f1.getSort().isSubsort(f2.getSort())) {
                    state = 1;
                }
                //				System.out.println("sort test completed...");
            } // if

            if (state == 0 || state == 2) {
                if (f2 != null &&
                    (f1.getDataType() == f2.getDataType())) {
                    bindings_a.add(0, f1);
                    bindings_b.add(0, f2);
                } // if
            } // if

            if (state == 3) {
                state = 0;
            }

            // Subsumption:
            if (state == 0) {
                if (f1 instanceof SetFeatureTerm || f2 instanceof SetFeatureTerm) {
                    boolean interrupted = false;

                    //						System.out.println("Set subsumption...");

                    // Prepare the sets:
                    if (set1 == null || set2 == null) {
                        if (f1 instanceof SetFeatureTerm) {
                            if (((SetFeatureTerm) f1).getSetValues().size() > 0) {
                                set1 = new FeatureTerm[((SetFeatureTerm) f1).getSetValues().size()];
                            } else {
                                set1 = null;
                            } // if
                            set1l = 0;
                            for (FeatureTerm ft : ((SetFeatureTerm) f1).getSetValues()) {
                                set1[set1l++] = ft;
                            }
                        } else {
                            set1 = new FeatureTerm[1];
                            set1l = 0;
                            set1[set1l++] = f1;
                        } // if
                        if (f2 instanceof SetFeatureTerm) {
                            if (((SetFeatureTerm) f2).getSetValues().size() > 0) {
                                set2 = new FeatureTerm[((SetFeatureTerm) f2).getSetValues().size()];
                            } else {
                                set2 = null;
                            } // if
                            set2l = 0;
                            for (FeatureTerm ft : ((SetFeatureTerm) f2).getSetValues()) {
                                set2[set2l++] = ft;
                            }
                        } else {
                            set2 = new FeatureTerm[1];
                            set2l = 0;
                            set2[set2l++] = f2;
                        } // if

                        //							System.out.println("Set1: " + set1l + "\nSet2: " + set2l);

                        if (set1l == 0) {
                            state = 2;
                        }
                        if (set1l > set2l) {
                            state = 1;
                        }
                    } // if

                    // Assign an element of the set2 to each element of the set1:
                    if (state == 0) {

                        if (assignment == null) {
                            assignment = new int[set1l];
                            for (i = 0; i < set1l; i++) {
                                assignment[i] = i;
                            }
                            assignment_pos = 0;
                        } // if

                        if (assignment_pos >= set1l) {
                            state = 2;
                        } else {
                            // create a backtracking node:
                            //								System.out.println("Backtrack node created at set position: " + assignment_pos);
                            {
                                SubsumptionBackTrackNode b_node;

                                b_node = new SubsumptionBackTrackNode();
                                b_node.n_bindings = bindings_a.size();
                                {
                                    int assignment_copy[] = new int[set1l];
                                    for (i = 0; i < set1l; i++) {
                                        assignment_copy[i] = assignment[i];
                                    }
                                    b_node.m_node = new SubsumptionStackNode(f1, f2, 3, assignment_copy, set1, set1l, set2, set2l, assignment_pos);
                                }
                                b_node.m_stack = new LinkedList<SubsumptionStackNode>();

                                for (SubsumptionStackNode s_node : stack) {
                                    if (s_node.m_assignment != null) {
                                        int assignment_copy[] = new int[s_node.m_set1l];
                                        for (i = 0; i < s_node.m_set1l; i++) {
                                            assignment_copy[i] = s_node.m_assignment[i];
                                        }
                                        b_node.m_stack.add(new SubsumptionStackNode(s_node.m_f1, s_node.m_f2, s_node.m_state,
                                            assignment_copy, set1, s_node.m_set1l, set2, s_node.m_set2l, s_node.m_assignment_pos));
                                    } else {
                                        b_node.m_stack.add(new SubsumptionStackNode(s_node.m_f1, s_node.m_f2, s_node.m_state,
                                            null, null, 0, null, 0, -1));
                                    } // if
                                } // while

                                stack_backtracking.add(0, b_node);
                            }

                            stack.add(0, new SubsumptionStackNode(f1, f2, 3, assignment, set1, set1l, set2, set2l, assignment_pos + 1));
                            stack.add(0, new SubsumptionStackNode(set1[assignment_pos], set2[assignment[assignment_pos]], 0, null, null, 0, null, 0, -1));
                            interrupted = true;
                        } // if
                    } // if

                    if (!interrupted) {
                        set1 = null;
                        set2 = null;
                    } // if
                } else {
                    // Single object subsumption:
                    // System.out.println("Single object subsumption...");

                    if (!f1.hasValue()) {
                        if (!f1.getSort().isSubsort(f2.getSort())) {
                            state = 1;
                        }
                    } else {
                        if (f1 instanceof IntegerFeatureTerm) {
                            if (((IntegerFeatureTerm) f1).getValue() == null) {
                                if (!(f2 instanceof IntegerFeatureTerm)) {
                                    state = 1;
                                }
                            } else {
                                if (!(f2 instanceof IntegerFeatureTerm) ||
                                    ((IntegerFeatureTerm) f2).getValue() == null ||
                                    !((IntegerFeatureTerm) f2).getValue().equals(((IntegerFeatureTerm) f1).getValue())) {
                                    state = 1;
                                }
                            }
                        } else if (f1 instanceof FloatFeatureTerm) {
                            Float f_value = null;

                            if (f2 instanceof IntegerFeatureTerm) {
                                f_value = new Float((float) ((IntegerFeatureTerm) f2).getValue());
                            } // if
                            if (f2 instanceof FloatFeatureTerm) {
                                f_value = ((FloatFeatureTerm) f2).getValue();
                            } // if

                            if (((FloatFeatureTerm) f1).getValue() == null) {
                            } else {
                                if (f_value == null ||
                                    !f_value.equals(((FloatFeatureTerm) f1).getValue())) {
                                    state = 1;
                                }
                            }
                        } else if (f1 instanceof SymbolFeatureTerm) {
                            if (((SymbolFeatureTerm) f1).getValue() != null) {
                                if (!(f2 instanceof SymbolFeatureTerm) ||
                                    ((SymbolFeatureTerm) f2).getValue() == null) {
                                    state = 1;
                                } else {
                                    if (!((SymbolFeatureTerm) f2).getValue().equals(((SymbolFeatureTerm) f1).getValue())) {
                                        state = 1;
                                    }
                                } // if
                            } // if
                        } else if (f1 instanceof TermFeatureTerm) {
                            //								System.out.println("FeatureTerm Subsumption (current feature " + feature_pos + ")");
                            for (Entry<Symbol, FeatureTerm> feature : ((TermFeatureTerm) f1).getFeatures()) {
                                FeatureTerm fv = feature.getValue();
                                Symbol fn = feature.getKey();
                                FeatureTerm fv2 = null;
                                if (f2 instanceof TermFeatureTerm) fv2 = f2.featureValue(fn);
                                if (fv2 == null) {
                                    if (!(fv instanceof SetFeatureTerm) || ((SetFeatureTerm)fv).getSetValues().size()!=0) {
                                        state = 1;
                                        break;
                                    }
                                } // if
                                stack.add(0, new SubsumptionStackNode(fv, fv2, 0, null, null, 0, null, 0, -1));
                            }
                        } else {
                            System.err.println("Warning! weird FeatureTerm class '" + f1.getClass().getName() + "' in subsumption");
                        }
                    } // if
                } // if

            } // if

            if (state == 1) {
                //					System.out.println("Fail");

                if (!stack_backtracking.isEmpty()) {
                    // backtracking:
                    SubsumptionBackTrackNode b_node;
                    SubsumptionStackNode s_node;
                    boolean found = false;

                    //						System.out.println("Backtracking... (" + stack_backtracking.size() + "nodes)\n");

                    do {
                        b_node = stack_backtracking.remove(0);
                        s_node = b_node.m_node;
                        //							printf("bt: [%p,%p] ",s_node.m_f1,s_node.m_f2);
                        if (get_set_assignment(s_node.m_set1l, s_node.m_set2l, s_node.m_assignment, s_node.m_assignment_pos)) {
                            found = true;
                        }

                    } while (!found && !stack_backtracking.isEmpty());

                    if (found) {
                        // Reconstruct the stack and bindings:
                        // stack:
                        stack.clear();

                        for (SubsumptionStackNode s_node2 : b_node.m_stack) {
                            if (s_node2.m_assignment != null) {
                                int assignment_copy[] = new int[s_node2.m_set1l];
                                for (i = 0; i < s_node2.m_set1l; i++) {
                                    assignment_copy[i] = s_node2.m_assignment[i];
                                }
                                stack.add(new SubsumptionStackNode(s_node2.m_f1, s_node2.m_f2, s_node2.m_state,
                                    assignment_copy, set1, s_node2.m_set1l, set2, s_node2.m_set2l, s_node2.m_assignment_pos));
                            } else {
                                stack.add(new SubsumptionStackNode(s_node2.m_f1, s_node2.m_f2, s_node2.m_state,
                                    null, null, 0, null, 0, -1));
                            } // if
                        } // while
                        if (b_node.m_node.m_assignment != null) {
                            int assignment_copy[] = new int[b_node.m_node.m_set1l];
                            for (i = 0; i < b_node.m_node.m_set1l; i++) {
                                assignment_copy[i] = b_node.m_node.m_assignment[i];
                            }
                            stack.add(0, new SubsumptionStackNode(b_node.m_node.m_f1, b_node.m_node.m_f2, b_node.m_node.m_state,
                                assignment_copy, set1, b_node.m_node.m_set1l, set2, b_node.m_node.m_set2l, b_node.m_node.m_assignment_pos));
                        } else {
                            stack.add(0, new SubsumptionStackNode(b_node.m_node.m_f1, b_node.m_node.m_f2, b_node.m_node.m_state,
                                null, null, 0, null, 0, -1));
                        } // if


                        // bindings:
                        while (bindings_a.size() > b_node.n_bindings) {
                            bindings_a.remove(0);
                            bindings_b.remove(0);
                        } // while
                    } else {
                        stack.clear();
                        res = false;
                    } // if
                } else {
                    stack.clear();
                    res = false;
                } // if
            } // if

            //				if (state==2) System.out.println("Ok");
        } // while
        //		System.out.println("*---- End Subsumption: " + (res ? "true":"false") + " ----*\n");

        /*
        long end = System.currentTimeMillis();
        boolean res2 = CSPSubsumption.subsumes(t1, t2);
        long end2 = System.currentTimeMillis();
        
        FTSubsumptionRecord.register(f1, f2, (end-start), (end2-end), res);

        if (res!=res2) {
            System.err.println("CSPsubsumption has a different result: " + res + " - " + res2);
            System.err.println(t1.toStringNOOS());
            System.err.println(t2.toStringNOOS());
        }
        */
        
        return res;
    }


    static boolean get_set_assignment(int l1, int l2, int assignment[], int failure_position) {
        int i = l1 - 1, j;
        int start_pos = 0;
        boolean collision;

        if (failure_position >= 0 && failure_position <= l1 - 1) {
            i = failure_position;
            start_pos = failure_position;
        } /* if */

        //		printf("<%i!> ",failure_position);

        for (j = failure_position + 1; j < l1; j++) {
            assignment[j] = -1;
        }
        do {
            do {
                assignment[i]++;
                collision = false;
                for (j = 0; j < i; j++) {
                    if (assignment[i] == assignment[j]) {
                        collision = true;
                    }
                } // for
            } while (collision && assignment[i] < l2);
            if (assignment[i] < l2) {
                i++;
            } else {
                if (i < l1) {
                    assignment[i] = -1;
                }
                i--;
            } // if

            if (i == l1) {
                /*
                printf("{%i,%i} ",l1,l2);
                for(i=0;i<l1;i++) printf("%i ",assignment[i]);
                printf("\n");
                 */
                return true;
            } /* if */
        } while (i >= start_pos);

        //		printf("fail \n");

        return false;
    } /* get_set_assignment */

}
