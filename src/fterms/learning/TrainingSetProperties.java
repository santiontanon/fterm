/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fterms.learning;

import fterms.FTKBase;
import fterms.FeatureTerm;
import fterms.Path;
import fterms.Sort;
import fterms.exceptions.FeatureTermException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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

        System.out.println(solutions.keySet().size() + " solution classes");
        for(String ss:solutions.keySet()) {
            System.out.println(ss + " : " + solutions.get(ss).size());
        }
    }
}
