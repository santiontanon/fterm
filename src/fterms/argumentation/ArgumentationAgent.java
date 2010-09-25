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

    
    public void beliefRevision(ArgumentationState state, FeatureTerm solution, Path dp, Path sp, Ontology o, FTKBase dm,boolean recover) throws Exception {
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
        System.out.println("Removed " + toDelete.size() + " rules due to not meeting acceptance criterion.");

        // Remove all rules that have been defeated:
        {
            for(ArgumentationTree at:state.getTrees(m_name)) {
                if (at.settledP(at.getRoot()) && at.defeatedP()) {
                    toDelete.add(at.getRoot().m_rule);
                }
            }
        }
        System.out.println("Removed " + toDelete.size() + " rules due to being defeated.");

        for(Rule r:toDelete) {
            m_hypothesis.removeRule(r);

            // remove them from the state:
            for(Argument a:state.getRoots(m_name)) {
                if (a.m_rule == r) {
                    state.retractRoot(a);
                }
            }
        }
        state.retractUnacceptable(m_name, m_aa);

        // Recover uncovered examples:
        if (recover) {
            m_hypothesis = m_learning.coverUncoveredExamples(m_examples, solution, m_hypothesis, state.getSettled(), m_aa, dp, sp, o, dm);

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
