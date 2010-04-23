/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fterms.learning;

import fterms.FeatureTerm;
import fterms.Path;
import fterms.Sort;
import java.util.List;

/**
 *
 * @author santi
 */
public class TrainingSetProperties {
    public Path description_path = null, solution_path = null;
    public Sort problem_sort = null;
    public String name = null;
    public List<FeatureTerm> cases = null;
}
