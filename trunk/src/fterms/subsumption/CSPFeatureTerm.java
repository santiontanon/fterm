/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fterms.subsumption;

import fterms.FTRefinement;
import fterms.FeatureTerm;
import fterms.Sort;
import fterms.Symbol;
import fterms.TermFeatureTerm;
import fterms.exceptions.FeatureTermException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import util.Pair;

/**
 *
 * @author santi
 */
public class CSPFeatureTerm {
    List<List<Symbol>> variables;
    HashMap<Symbol,boolean [][]> features;

    public String toString() {
        String tmp = "";
        int n = 0;
        for(List<Symbol> sorts:variables) {
            tmp += n + " - " + sorts + "\n";
            n++;
        }

        for(Symbol f:features.keySet()) {
            boolean [][]matrix = features.get(f);
            if (matrix!=null) {
                for(int i = 0;i<matrix.length;i++) {
                    for(int j = 0;j<matrix[0].length;j++) {
                        if (matrix[i][j]) tmp += i + " - " + j + " (" + f + ")\n";
                    }
                }
            }
        }
        return tmp;
    }


    public CSPFeatureTerm(FeatureTerm f) throws FeatureTermException {
        List<FeatureTerm> vl = FTRefinement.variables(f);
        HashMap<FeatureTerm, List<Pair<TermFeatureTerm, Symbol>>> vpl = FTRefinement.variablesWithAllParents(f);

        variables = new LinkedList<List<Symbol>>();
        features = new HashMap<Symbol, boolean[][]>();

        for(FeatureTerm v:vl) {
            List<Symbol> sorts = new LinkedList<Symbol>();
            if (v.isConstant()) {
                sorts.add(new Symbol(v.toStringNOOS()));
            } else if (v.getName()!=null) {
                sorts.add(v.getName());
            }
            Sort s = v.getSort();
            while(s!=null) {
                sorts.add(s.getName());
                s = s.getSuper();
            }
            variables.add(sorts);
        }

        int n1 = 0;
        int n2 = 0;
        for(FeatureTerm v:vl) {
            List<Pair<TermFeatureTerm, Symbol>> parents = vpl.get(v);
            if (parents!=null) {
                for(Pair<TermFeatureTerm, Symbol> parent:parents) {
                    if (parent!=null) {
                        n1 = vl.indexOf(parent.m_a);
                        boolean [][]matrix = features.get(parent.m_b);
                        if (matrix==null) {
                            matrix = new boolean[variables.size()][variables.size()];
                            features.put(parent.m_b,matrix);
                        }
                        matrix[n1][n2] = true;
                    }
                }
            }
            n2++;
        }
    }

}
