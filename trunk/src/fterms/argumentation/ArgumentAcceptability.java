/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fterms.argumentation;

import fterms.FeatureTerm;
import fterms.Path;
import fterms.exceptions.FeatureTermException;
import java.util.Collection;
import java.util.LinkedList;

/**
 *
 * @author santi
 */
public abstract class ArgumentAcceptability {

    public static int DEBUG = 0;

    Collection<FeatureTerm> m_examples;
    Path m_dp, m_sp;

    public ArgumentAcceptability(Collection<FeatureTerm> examples, Path sp, Path dp) {
        m_examples = new LinkedList<FeatureTerm>();
        m_examples.addAll(examples);

        m_dp = dp;
        m_sp = sp;
    }

    public void updateExamples(Collection<FeatureTerm> examples) {
        m_examples.clear();
        m_examples.addAll(examples);
    }

    // boolean criterion
    public abstract boolean accepted(Argument a) throws FeatureTermException;

    // graded criterion
    public abstract float degree(Argument a) throws FeatureTermException;
}
