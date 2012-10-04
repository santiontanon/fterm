/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fterms.subsumption;

import fterms.FTKBase;
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import util.Pair;

/**
 *
 * @author santi
 */
public class CSPFeatureTerm {    
    public List<FeatureTerm> originalVariables = null; // only set if the term was constructed from a FeatureTerm, if it was loaded from disk, it will be null
    public List<List<Object>> variables;    // it might contain either constants (terms) or sorts
    public HashMap<Symbol,boolean [][]> features;

    public String toString() {
        String tmp = "";
        int n = 0;
        for(List<Object> sorts:variables) {
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
        originalVariables = FTRefinement.variables(f);
        HashMap<FeatureTerm, List<Pair<TermFeatureTerm, Symbol>>> vpl = FTRefinement.variablesWithAllParents(f);

        variables = new LinkedList<List<Object>>();
        features = new HashMap<Symbol, boolean[][]>();

        for(FeatureTerm v:originalVariables) {
            List<Object> sorts = new LinkedList<Object>();
            if (v.isConstant()) {
                sorts.add(v);
            } else if (v.getName()!=null && v.isLeaf()) {
                // assume it's part of the domain model:
                sorts.add(v);
            }
                    
            Sort s = v.getSort();
            while(s!=null) {
                sorts.add(s);
                s = s.getSuper();
            }
            variables.add(sorts);
        }

        int n1 = 0;
        int n2 = 0;
        for(FeatureTerm v:originalVariables) {
            List<Pair<TermFeatureTerm, Symbol>> parents = vpl.get(v);
            if (parents!=null) {
                for(Pair<TermFeatureTerm, Symbol> parent:parents) {
                    if (parent!=null) {
                        n1 = originalVariables.indexOf(parent.m_a);
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


    public CSPFeatureTerm(FeatureTerm f, FTKBase dm) throws FeatureTermException {
        originalVariables = FTRefinement.variables(f);
        HashMap<FeatureTerm, List<Pair<TermFeatureTerm, Symbol>>> vpl = FTRefinement.variablesWithAllParents(f);


//        System.out.println(f.toStringNOOS(dm));
        
        variables = new LinkedList<List<Object>>();
        features = new HashMap<Symbol, boolean[][]>();

        for(FeatureTerm v:originalVariables) {
            List<Object> sorts = new LinkedList<Object>();
            if (v.isConstant()) {
                sorts.add(v);
            } else if (dm.contains(v)) {
                sorts.add(v);
            }
            Sort s = v.getSort();
            while(s!=null) {
                sorts.add(s);
                s = s.getSuper();
            }
            variables.add(sorts);
        }

        int n1 = 0;
        int n2 = 0;
        for(FeatureTerm v:originalVariables) {
            List<Pair<TermFeatureTerm, Symbol>> parents = vpl.get(v);
            if (parents!=null) {
                for(Pair<TermFeatureTerm, Symbol> parent:parents) {
                    if (parent!=null) {
                        n1 = originalVariables.indexOf(parent.m_a);
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

 
    public CSPFeatureTerm(String fileName) throws IOException, FeatureTermException {
        FileReader fr = new FileReader(fileName);
        BufferedReader br = new BufferedReader(fr);

        variables = new LinkedList<List<Object>>();
        features = new HashMap<Symbol, boolean[][]>();

        int state = 0;
        String line = br.readLine();
        while(line!=null) {

            switch(state) {
                case 0:
                    {
                        StringTokenizer st = new StringTokenizer(line);
                        String token = st.nextToken();
                        int node = Integer.parseInt(token);
                        if (node==0) {
                            state = 1;
                        } else {
                            st.nextToken();
                            List<Object> sorts = new LinkedList<Object>();
                            while(st.hasMoreTokens()) {
                                token = st.nextToken();
                                sorts.add(new Symbol(token));
                            }
                            variables.add(sorts);
                        }
                    }
                    break;
                 case 1:
                    {
                        StringTokenizer st = new StringTokenizer(line);
                        int n1 = Integer.parseInt(st.nextToken());
                        int n2 = Integer.parseInt(st.nextToken());
                        Symbol f = new Symbol(st.nextToken());

                        boolean [][]matrix  = features.get(f);
                        if (matrix==null) {
                            matrix = new boolean[variables.size()][variables.size()];
                            features.put(f, matrix);
                        }
                        matrix[n1-1][n2-1] = true;
                    }
                    break;
            }

            line = br.readLine();
        }
    }


    public FeatureTerm toFeatureTerm() throws FeatureTermException {
        List<FeatureTerm> vars = new LinkedList<FeatureTerm>();

        for(List<Object> l:variables) {
            FeatureTerm variable = null;
            Object o = l.get(0);
            if (o instanceof FeatureTerm) {
                variable = (FeatureTerm)o;
            } else if (o instanceof Sort) {
                variable = ((Sort)o).createFeatureTerm();
            } else {
                throw new FeatureTermException("CSPFeatureTerm: most specific sort is not term nor sort... " + o);
            }
            vars.add(variable);
        }

        for(Symbol f:features.keySet()) {
            boolean [][]matrix = features.get(f);
            for(int n1 = 0; n1<vars.size();n1++) {
                for(int n2 = 0; n2<vars.size();n2++) {
                    if (matrix[n1][n2]) ((TermFeatureTerm)vars.get(n1)).addFeatureValue(f, vars.get(n2));
                }
            }
        }

        return vars.get(0);
    }


    public int variablesInSets() {
        HashSet<Integer> inSets = new HashSet<Integer>();

        for(Symbol f:features.keySet()) {
            boolean [][]links = features.get(f);
            for(int i = 0;i<links.length;i++) {
                int v = -1;
                for(int j = 0;j<links[i].length;j++) {
                    if (links[i][j]) {
                        if (v==-1) {
                            v = j;
                        } else {
                            inSets.add(v);
                            inSets.add(j);
                        }
                    }
                }
            }
        }

        return inSets.size();
    }

     public int largestSet() {
        int largestSet = 0;

        for(Symbol f:features.keySet()) {
            boolean [][]links = features.get(f);
            for(int i = 0;i<links.length;i++) {
                int v = 0;
                for(int j = 0;j<links[i].length;j++) {
                    if (links[i][j]) v++;
                }
                if (v>largestSet) largestSet = v;
            }
        }

        return largestSet;
    }

}
