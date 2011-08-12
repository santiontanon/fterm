/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fterms.subsumption;

import fterms.FeatureTerm;
import fterms.SetFeatureTerm;
import fterms.Symbol;
import fterms.exceptions.FeatureTermException;
import java.util.LinkedList;
import java.util.List;
import jp.ac.kobe_u.cs.cream.*;

/**
 *
 * @author santi
 */


public class CSPSubsumption {

    public static boolean subsumes(FeatureTerm f1, FeatureTerm f2) throws FeatureTermException {
        if (f1 instanceof SetFeatureTerm || f2 instanceof SetFeatureTerm) return FTSubsumption.subsumes(f1,f2);
        return subsumes(new CSPFeatureTerm(f1), new CSPFeatureTerm(f2));
    }

    public static boolean subsumes(CSPFeatureTerm t1, CSPFeatureTerm t2) {
//        System.out.println(t1.toString());
//        System.out.println(t2.toString());

        int n1 = t1.variables.size();
        int n2 = t2.variables.size();
		Network net = new Network();
		IntVariable[] q = new IntVariable[n1];
        q[0] = new IntVariable(net,0,0);
        for(int i = 1;i<n1;i++) q[i] = new IntVariable(net,0,n2-1);

        // Sort constraint:
        for(int i = 0;i<n1;i++) {
            for(int j = 0;j<n2;j++) {
                Object s1 = t1.variables.get(i).get(0);
                if (!t2.variables.get(j).contains(s1)) {
                    ((IntDomain)(q[i].getDomain())).remove(j);
                }
            }
        }

        // feature constraints:
        for(int i1 = 0;i1<n1;i1++) {
            for(int j1 = 0;j1<n1;j1++) {
                List<Symbol> minimumRequirements = new LinkedList<Symbol>();
                for(Symbol f:t1.features.keySet()) {
                    boolean [][]matrix1 = t1.features.get(f);
                    if (matrix1!=null && matrix1[i1][j1]) minimumRequirements.add(f);
                }
                if (!minimumRequirements.isEmpty()) {
//                    System.out.println(i1 + " - " + j1);
//                    System.out.println("MR: " + minimumRequirements);
                    boolean [][]relation = new boolean [n2][n2];
                    for(int i2 = 0;i2<n2;i2++) {
                        for(int j2 = 0;j2<n2;j2++) {
                            boolean satisfies = true;
                            for(Symbol f:minimumRequirements) {
                                boolean [][]matrix2 = t2.features.get(f);
                                if (matrix2==null || !matrix2[i2][j2]) {
                                    satisfies = false;
                                    break;
                                }
                            }
                            if (satisfies) relation[i2][j2] = true;

//                            System.out.print((relation[i2][j2] ? "1":"0"));
                        }
//                        System.out.println("");
                    }
                    new Relation(net,q[i1],relation,q[j1]);
                }
            }
        }

        for(Symbol f:t1.features.keySet()) {
            for(int i1 = 0;i1<n1;i1++) {
                boolean [][]matrix1 = t1.features.get(f);
                if (matrix1!=null) {
                    List<Integer> siblings = new LinkedList<Integer>();
                    for(int j1 = 0;j1<n1;j1++) {
                        if (matrix1[i1][j1]) siblings.add(j1);
                    }
                    if (siblings.size()>1) {
                        Variable v[] = new Variable[siblings.size()];
                        for(int i = 0;i<siblings.size();i++) v[i]=q[siblings.get(i)];
//                        System.out.println("siblings: " + siblings);
                        new NotEquals(net,v);
                    }
                }
            }
        }

        Solver solver = new DefaultSolver(net);
		solver.start();
        solver.waitNext();
        Solution solution = solver.getSolution();
		solver.stop();
        if (solution==null) return false;
        for (int i = 0; i < n1; i++) {
            if (solution.getDomain(q[i]).size()==0) return false;
//            int j = solution.getIntValue(q[i]);
//            System.out.print(j + " ");
        }
//        System.out.println();

        return true;
    }


}
