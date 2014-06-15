/**
 * Copyright (c) 2013, Santiago Ontañón All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. Neither the name of the IIIA-CSIC nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission. THIS SOFTWARE IS PROVIDED
 * BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ftl.learning.core;

import java.util.LinkedList;
import java.util.List;

import ftl.base.core.FTKBase;
import ftl.base.core.FeatureTerm;
import ftl.base.utils.FeatureTermException;

// TODO: Auto-generated Javadoc
/**
 * The Class RuleHypothesis.
 */
public class RuleHypothesis extends Hypothesis {

    /**
     * The m_ordered.
     */
    boolean m_ordered = false;

    /**
     * The m_rules.
     */
    protected List<Rule> m_rules = new LinkedList<Rule>();

    /**
     * The m_default_solution.
     */
    private FeatureTerm m_default_solution = null;

    /**
     * Instantiates a new rule hypothesis.
     */
    public RuleHypothesis() {

    }

    /**
     * Instantiates a new rule hypothesis.
     *
     * @param ordered the ordered
     */
    public RuleHypothesis(boolean ordered) {
        m_ordered = ordered;
    }

    /**
     * Instantiates a new rule hypothesis.
     *
     * @param h the h
     */
    public RuleHypothesis(RuleHypothesis h) {
        m_ordered = h.m_ordered;
        m_rules.addAll(h.m_rules);
        setM_default_solution(h.getM_default_solution());
    }

    /**
     * Sets the ordered.
     *
     * @param ordered the new ordered
     */
    public void setOrdered(boolean ordered) {
        m_ordered = ordered;
    }

    /**
     * Checks if is ordered.
     *
     * @return true, if is ordered
     */
    public boolean isOrdered() {
        return m_ordered;
    }

    /*
     * (non-Javadoc)
     * 
     * @see csic.iiia.ftl.learning.core.Hypothesis#size()
     */
    public int size() {
        return m_rules.size();
    }

    /**
     * Copy.
     *
     * @param h the h
     * @throws Exception the exception
     */
    public void copy(RuleHypothesis h) throws Exception {
        m_ordered = h.m_ordered;
        m_rules.clear();
        m_rules.addAll(h.m_rules);
        setM_default_solution(h.getM_default_solution());
    }

    /**
     * Adds the rule.
     *
     * @param p the p
     * @param s the s
     * @param reliability the reliability
     * @param support the support
     * @throws Exception the exception
     */
    public void addRule(FeatureTerm p, FeatureTerm s, float reliability, int support) throws Exception {
        m_rules.add(new Rule(p, s, reliability, support));
    }

    /**
     * Adds the rule.
     *
     * @param r the r
     */
    public void addRule(Rule r) {
        m_rules.add(r);
    }

    /**
     * Removes the rule.
     *
     * @param r the r
     */
    public void removeRule(Rule r) {
        m_rules.remove(r);
    }

    /**
     * Gets the rules.
     *
     * @return the rules
     */
    public List<Rule> getRules() {
        return m_rules;
    }

    /**
     * Gets the default solution.
     *
     * @return the default solution
     */
    public FeatureTerm getDefaultSolution() {
        return getM_default_solution();
    }

    /**
     * Sets the default solution.
     *
     * @param s the new default solution
     */
    public void setDefaultSolution(FeatureTerm s) {
        setM_default_solution(s);
    }

    /**
     * Subsumed by rule.
     *
     * @param problem the problem
     * @param r the r
     * @return true, if successful
     * @throws FeatureTermException the feature term exception
     * @throws Exception the exception
     */
    public boolean subsumedByRule(FeatureTerm problem, Rule r) throws FeatureTermException, Exception {

        if (m_ordered) {
            int pos = m_rules.indexOf(r);
            if (pos == -1) {
                System.err.println("That rule is not part of this hypothesis!");
                return false;
            }

            // A rule can only subsume a pattern when m_ordered = true if all the previous rules do not:
            for (Rule r2 : m_rules) {
                if (r2 == r) {
                    return r.pattern.subsumes(problem);
                } else {
                    if (r2.pattern.subsumes(problem)) {
                        return false;
                    }
                }
            }
        } else {
            return r.pattern.subsumes(problem);
        }

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see csic.iiia.ftl.learning.core.Hypothesis#generatePrediction(csic.iiia.ftl.base.core.FeatureTerm,
     * csic.iiia.ftl.base.core.FTKBase, boolean)
     */
    public Prediction generatePrediction(FeatureTerm problem, FTKBase dm, boolean debug) throws FeatureTermException, Exception {
        Prediction p;
        Rule candidate = null;
        int fired = 0;

        if (m_ordered) {
            for (Rule rule : m_rules) {
                if (rule.pattern.subsumes(problem)) {
                    if (debug) {
                        System.out.println("Fired rule " + m_rules.indexOf(rule) + " -> " + rule.solution.toStringNOOS(dm));
                    }
                    candidate = rule;
                    fired++;
                    break;
                } // if
            } // while
        } else {
            for (Rule rule : m_rules) {
                if (rule.pattern.subsumes(problem)) {
                    if (debug) {
                        System.out.println("Fired rule " + m_rules.indexOf(rule) + " -> " + rule.solution.toStringNOOS(dm));
                    }
                    if ((candidate == null || (candidate.pattern.subsumes(rule.pattern) && !rule.pattern.subsumes(candidate.pattern)) || (!candidate.pattern
                            .subsumes(rule.pattern) && rule.reliability > candidate.reliability))) {
                        candidate = rule;
                        if (debug) {
                            System.out.println("Candidate rule " + m_rules.indexOf(rule));
                        }
                        fired++;
                    } // if
                } // if
            } // while
        }

		// System.out.println("Fired " + fired + " rules with ordered " + m_ordered);
        if (candidate != null) {
            if (debug) {
                System.out.println("Hypothesis: " + candidate.solution.toStringNOOS(dm) + " with reliability " + candidate.reliability);
            }

            p = new Prediction();
            p.problem = problem;
            p.solutions.add(candidate.solution);
            p.justifications.put(candidate.solution, candidate.pattern);
            p.support.put(candidate.solution, candidate.support);
            return p;
        } // if

        if (getM_default_solution() != null) {
            if (debug) {
                System.out.println("Hypothesis: not covered by any pattern. Using default solution...");
            }
            if (debug) {
                System.out.println("Hypothesis: " + getM_default_solution().toStringNOOS(dm));
            }

            p = new Prediction();
            p.problem = problem;
            p.solutions.add(getM_default_solution());
            p.justifications.put(getM_default_solution(), problem.getSort().createFeatureTerm());
            p.support.put(getM_default_solution(), 1);
            return p;
        } else {
            if (debug) {
                System.out.println("Hypothesis: not covered by any pattern and no default solution...");
            }
            return new Prediction();
        } // if
    } // Hypothesis::generate_prediction

    /**
     * Covered by any rule.
     *
     * @param problem the problem
     * @return the rule
     * @throws FeatureTermException the feature term exception
     */
    public Rule coveredByAnyRule(FeatureTerm problem) throws FeatureTermException {
        for (Rule rule : m_rules) {
            if (rule.pattern.subsumes(problem)) {
                return rule;
            }
        } // while
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see csic.iiia.ftl.learning.core.Hypothesis#toString(csic.iiia.ftl.base.core.FTKBase)
     */
    public String toString(FTKBase dm) {
        String tmp;

        tmp = ("Hypothesis: rules learned (" + m_rules.size() + "): ----------------------\n");
        if (getM_default_solution() != null) {
            tmp += "Default Solution: " + getM_default_solution().toStringNOOS(dm) + "\n";
        } // if

        for (Rule rule : m_rules) {
            tmp += m_rules.indexOf(rule) + " - Rule for " + rule.solution.toStringNOOS(dm) + " " + rule.reliability + "\n";
            tmp += rule.pattern.toStringNOOS(dm) + "\n";
        } // while

        return tmp;
    } // Hypothesis::show_rule_set

    /*
     * (non-Javadoc)
     * 
     * @see csic.iiia.ftl.learning.core.Hypothesis#toCompactString(csic.iiia.ftl.base.core.FTKBase)
     */
    public String toCompactString(FTKBase dm) {
        String tmp;

        tmp = ("Hypothesis: rules learned (" + m_rules.size() + "): ----------------------\n");
        if (getM_default_solution() != null) {
            tmp += "Default Solution: " + getM_default_solution().toStringNOOS(dm) + "\n";
        } // if

        for (Rule rule : m_rules) {
            tmp += "- Rule for " + rule.solution.toStringNOOS(dm) + " - " + rule.reliability + "\n";
        } // while

        return tmp;
    } // Hypothesis::show_rule_set

    /**
     * Gets the m_default_solution.
     *
     * @return the m_default_solution
     */
    public FeatureTerm getM_default_solution() {
        return m_default_solution;
    }

    /**
     * Sets the m_default_solution.
     *
     * @param m_default_solution the new m_default_solution
     */
    public void setM_default_solution(FeatureTerm m_default_solution) {
        this.m_default_solution = m_default_solution;
    }
}
