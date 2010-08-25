/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fterms.learning;

import fterms.FTKBase;
import fterms.FTRefinement;
import fterms.FeatureTerm;
import fterms.Path;
import fterms.SetFeatureTerm;
import fterms.Sort;
import fterms.exceptions.FeatureTermException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author santi
 */
public class TrainingSetProperties {
    public Path description_path = null, solution_path = null;
    public Sort problem_sort = null;
    public String name = null;
    public List<FeatureTerm> cases = null;


    public List<FeatureTerm> differentSolutions() throws FeatureTermException {
        List<FeatureTerm> l = new LinkedList<FeatureTerm>();
        for(FeatureTerm c:cases) {
            FeatureTerm s = c.readPath(solution_path);
            if (!l.contains(s)) l.add(s);
        }

        return l;
    }
    

    public FeatureTerm getCaseByName(String name) {
        for(FeatureTerm c:cases) {
            if (c.getName().equals(name)) return c;
        }
        return null;
    }
    

    public void printStatistics(FTKBase dm) {
        System.out.println("Data set name: " + name);
        System.out.println(cases.size() + " cases");
        System.out.println("Description path:" + description_path);
        System.out.println("Solution path:" + solution_path);

        HashMap<String,List<FeatureTerm>> solutions = new HashMap<String,List<FeatureTerm>>();

        for(FeatureTerm c:cases) {
            try {
                FeatureTerm s = c.readPath(solution_path);
                String ss = s.toStringNOOS(dm);
                List<FeatureTerm> l = solutions.get(ss);
                if (l==null) {
                    l = new LinkedList<FeatureTerm>();
                    solutions.put(ss,l);
                }
                l.add(c);
            } catch (FeatureTermException ex) {
                ex.printStackTrace();
            }
        }

        // solutions:
        System.out.println(solutions.keySet().size() + " solution classes");
        for(String ss:solutions.keySet()) {
            System.out.println(ss + " : " + solutions.get(ss).size());
        }

        // size of the terms:
        {
            int min_size = 0, max_size = 0;
            double avg_size = 0;
            int min_set = 0, max_set = 0;
            double avg_set = 0;
            int min_set_size = 0, max_set_size = 0;
            double avg_set_size = 0;
            boolean first = true;

            for(FeatureTerm c:cases) {
                int size = FTRefinement.variables(c).size();
                List<SetFeatureTerm> sets = FTRefinement.sets(c);
                int n_sets = sets.size();
                int minss = -1, maxss = -1;
                double avgss = 0;
                for(SetFeatureTerm s:sets) {
                    if (minss==-1 || s.getSetValues().size()<minss) minss = s.getSetValues().size();
                    if (maxss==-1 || s.getSetValues().size()>maxss) maxss = s.getSetValues().size();
                    avgss+=s.getSetValues().size();
                }
                avgss/=sets.size();

                System.out.println(c.getName() + " size: " + size + " , sets: " + minss + " - " + maxss);

                if (first) {
                    min_size = max_size = size;
                    min_set = max_set = n_sets;
                    min_set_size = minss;
                    max_set_size = maxss;
                    first = false;
                } else {
                    if (size<min_size) min_size = size;
                    if (size>max_size) max_size = size;
                    if (n_sets<min_set) min_set = n_sets;
                    if (n_sets>max_set) max_set = n_sets;
                    if (minss<min_set_size) min_set_size = minss;
                    if (maxss>max_set_size) max_set_size = maxss;
                }
                avg_size+=size;
                avg_set+=n_sets;
                avg_set_size+=avgss;
            }
            avg_size/=cases.size();
            avg_set/=cases.size();
            avg_set_size/=cases.size();
            System.out.println("Size in variables: [" + min_size + "," + max_size + "] (" + avg_size + ")");
            System.out.println("number of sets: [" + min_set + "," + max_set + "] (" + avg_set + ")");
            System.out.println("size of sets: [" + min_set_size + "," + max_set_size + "] (" + avg_set_size + ")");
        }
    }
}
