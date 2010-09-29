/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fterms.argumentation;

import fterms.FTKBase;
import fterms.FeatureTerm;
import fterms.Ontology;
import fterms.Path;
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

    public ArgumentationAgent(String name,Collection<FeatureTerm> examples, ArgumentAcceptability aa, RuleHypothesis h, ArgumentationBasedLearning l) {
        m_name = name;
        m_examples = new LinkedList<FeatureTerm>();
        m_examples.addAll(examples);
        m_alreadySentExamples = new HashMap<String,List<FeatureTerm>>();
        m_aa = aa;
        m_hypothesis = h;
        m_learning = l;
    }


    public void sendExample(ArgumentationAgent other,FeatureTerm example, ArgumentationState state) {
        other.m_examples.add(example);
        other.m_aa.updateExamples(other.m_examples);

        List<FeatureTerm> l = m_alreadySentExamples.get(other);
        if (l==null) {
            l = new LinkedList<FeatureTerm>();
            m_alreadySentExamples.put(other.m_name,l);
        } else {
            if (l.contains(example)) {
                System.err.println("ArgumentationAgent.sendExample: example had already been sent to this agent!!!!");
            }
        }
        l.add(example);
        state.unSettle(other.m_name);
    }

    
    public void beliefRevision(ArgumentationState state, FeatureTerm solution, Path dp, Path sp, Ontology o, FTKBase dm,boolean recover, List<ArgumentationAgent> agents) throws Exception {
        System.out.println("beliefRevision: " + m_name);
        List<Rule> toDelete = new LinkedList<Rule>();
        boolean anyNewExample = false;

        for(FeatureTerm e:state.getExamples()) {
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
