/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fterms.argumentation;

import fterms.FeatureTerm;
import fterms.Path;
import fterms.exceptions.FeatureTermException;
import java.util.Collection;

/**
 *
 * @author santi
 */
public class CountArgumentAcceptability extends ArgumentAcceptability {

    int m_min_count = 2;

    public CountArgumentAcceptability(Collection<FeatureTerm> examples, Path sp, Path dp, int min_count) {
        super(examples,sp,dp);

        m_min_count = min_count;

    }

    public boolean accepted(Argument a) throws FeatureTermException {
        if (degree(a)>=m_min_count) return true;
        return false;
    }


    public float degree(Argument a) throws FeatureTermException {
        if (a.m_type==Argument.ARGUMENT_EXAMPLE) return 1.0f;
        if (a.m_type==Argument.ARGUMENT_RULE) {
            float P = 0;
            float N = 0;

            for(FeatureTerm e:m_examples) {
                FeatureTerm d = e.readPath(m_dp);

                if (a.m_rule.pattern.subsumes(d)) {
                    FeatureTerm s = e.readPath(m_sp);
                    if (s.equivalents(a.m_rule.solution)) P++;
                                                   else N++;
                }
            }

            float b = 0;

            if (N>0) {
                b = 0;
            } else {
                b = P;
            }

            if (DEBUG>=1) {
                System.out.println("CountArgumentAcceptability.accepted " + b + "(" + P + "," + N + ") with " + m_examples.size() + " examples");
                if (DEBUG>=2) {
                    for(FeatureTerm e:m_examples) {
                        FeatureTerm d = e.readPath(m_dp);

                        if (a.m_rule.pattern.subsumes(d)) {
                            FeatureTerm s = e.readPath(m_sp);
                            System.out.print(e.getName().get() + " ");
                        }
                    }
                    System.out.println("");
                }
            }

            return b;
        }
        return 0.0f;
    }

}
