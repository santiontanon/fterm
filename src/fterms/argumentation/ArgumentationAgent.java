/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fterms.argumentation;

import fterms.FTKBase;
import fterms.FeatureTerm;
import fterms.Ontology;
import fterms.Path;
import fterms.exceptions.FeatureTermException;
import fterms.learning.Rule;
import fterms.learning.RuleHypothesis;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author santi
 */
public class ArgumentationAgent {
    public String m_name;
    public List<FeatureTerm> m_examples;
    public HashMap<String,List<FeatureTerm>> m_alreadySentExamples;
    public ArgumentAcceptability m_aa;
    public RuleHypothesis m_hypothesis;
    public ArgumentationBasedLearning m_learning;
    public boolean m_rationalist = true;

    public ArgumentationAgent(String name,Collection<FeatureTerm> examples, ArgumentAcceptability aa, RuleHypothesis h, ArgumentationBasedLearning l) {
        m_name = name;
        m_examples = new LinkedList<FeatureTerm>();
        m_examples.addAll(examples);
        m_alreadySentExamples = new HashMap<String,List<FeatureTerm>>();
        m_aa = aa;
        m_aa.updateExamples(m_examples);
        m_hypothesis = h;
        m_learning = l;
        m_rationalist = true;
    }

    public ArgumentationAgent(String name,Collection<FeatureTerm> examples, ArgumentAcceptability aa, RuleHypothesis h, ArgumentationBasedLearning l, boolean credulous) {
        m_name = name;
        m_examples = new LinkedList<FeatureTerm>();
        m_examples.addAll(examples);
        m_alreadySentExamples = new HashMap<String,List<FeatureTerm>>();
        m_aa = aa;
        m_aa.updateExamples(m_examples);
        m_hypothesis = h;
        m_learning = l;
        m_rationalist = credulous;
    }

    public boolean sendExample(ArgumentationAgent other,FeatureTerm example, ArgumentationState state) {
        if (AMAIL.DEBUG>=2) System.out.println("sendExample: " + m_name + " -> " + other.m_name + " (" + example.getName() + ")");

        List<FeatureTerm> l = m_alreadySentExamples.get(other.m_name);
        if (l==null) {
            l = new LinkedList<FeatureTerm>();
            m_alreadySentExamples.put(other.m_name,l);
        }
        if (l.contains(example)) {
            System.err.println("ArgumentationAgent.sendExample: example had already been sent to this agent!!!!");
        } else {
            l.add(example);
        }

        if (other.m_examples.contains(example)) return false;
        other.m_examples.add(example);
        other.m_aa.updateExamples(other.m_examples);

        state.unSettle(other.m_name);
        return true;
    }


    public String coveredExamples(Argument a, Path dp, Path sp) throws FeatureTermException {
        if (a.m_type == Argument.ARGUMENT_RULE) {
            FeatureTerm pattern = a.m_rule.pattern;
            FeatureTerm solution = a.m_rule.solution;
            int np = 0;
            int nn = 0;
            for(FeatureTerm example:m_examples) {
                FeatureTerm d = example.readPath(dp);
                FeatureTerm s = example.readPath(sp);
                if (pattern.subsumes(d)) {
                    if (solution.equivalents(s)) np++;
                                            else nn++;
                }
            }
            return "(" + np + "," + nn + ")";
        } else {
            return "";
        }
    }
    
    public void beliefRevision(ArgumentationState state, FeatureTerm solution, Path dp, Path sp, Ontology o, FTKBase dm,boolean recover, List<ArgumentationAgent> agents) throws Exception {
        if (AMAIL.DEBUG>=2) System.out.println("beliefRevision: " + m_name);
        List<Rule> toDelete = new LinkedList<Rule>();
        boolean anyNewExample = false;

        for(FeatureTerm e:state.getExamplesSentToAgent(m_name)) {
            if (!m_examples.contains(e)) {
                m_examples.add(e);
                anyNewExample = true;
            }
        }

        if (anyNewExample) {
            m_aa.updateExamples(m_examples);
            state.unSettle(m_name);
        }
        
        // Remove any not acceptable rule:
        for(Rule r:m_hypothesis.getRules()) {
            if (!m_aa.accepted(new Argument(r))) toDelete.add(r);
        }
        if (AMAIL.DEBUG>=2) 
            if (toDelete.size()>0) System.out.println("Removed " + toDelete.size() + " rules due to not meeting acceptance criterion.");

        for(Rule r:toDelete) {
            m_hypothesis.removeRule(r);

            // remove them from the state:
            for(Argument a:state.getRoots(m_name)) {
                if (a.m_rule == r) {
                    state.retractRoot(a);
                }
            }
        }
        toDelete.clear();

        // Remove all rules that have been defeated:
        {
            List<ArgumentationTree> toDefend = state.getDefeated(m_name);
            for (ArgumentationTree at : toDefend) {
                List<Argument> challengers = at.getChallengers();
                for(Argument challenger:challengers) {
                    if (at.settledP(challenger, m_name)) {
                        toDelete.add(at.getRoot().m_rule);
                        break;
                    }
                }
            }
        }
        if (AMAIL.DEBUG>=2) 
            if (toDelete.size()>0) System.out.println("Removed " + toDelete.size() + " rules due to being defeated.");

        for(Rule r:toDelete) {
            m_hypothesis.removeRule(r);

            // remove them from the state:
            for(Argument a:state.getRoots(m_name)) {
                if (a.m_rule == r) {
                    state.retractRoot(a);
                }
            }
        }
        int removedRoots = state.retractUnacceptable(m_name, m_aa);
        if (AMAIL.DEBUG>=2) 
            if (removedRoots>0) System.out.println("Removed " + removedRoots + " additional roots from the state due propagation of rules not meeting acceptance criterion.");

        // Recover uncovered examples:
        if (recover) {
//            List<Argument> acceptedArguments = state.getSettled(agents);
            List<Argument> acceptedArguments = state.getUndefeatedArguments();
            m_hypothesis = m_learning.coverUncoveredExamples(m_examples, solution, m_hypothesis, acceptedArguments, m_aa, dp, sp, o, dm);

            for(Rule r:m_hypothesis.getRules()) {
                boolean found = false;
                for(Argument a:state.getRoots(m_name)) {
                    if (a.m_rule == r) {
                        found = true;
                        break;
                    }
                }
                if (!found) state.addNewRoot(m_name, new Argument(r, m_name));
            }
        }
    }
    
}
