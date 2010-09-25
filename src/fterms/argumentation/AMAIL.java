/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fterms.argumentation;

import fterms.FTKBase;
import fterms.FeatureTerm;
import fterms.Ontology;
import fterms.Path;
import fterms.argumentation.Argument;
import fterms.argumentation.ArgumentAcceptability;
import fterms.exceptions.FeatureTermException;
import fterms.learning.Rule;
import fterms.learning.RuleHypothesis;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import fterms.argumentation.ArgumentationAgent;
import fterms.argumentation.ArgumentationState;
import fterms.argumentation.ArgumentationTree;
import util.Pair;


/*
 * @author santi
 */
public class AMAIL {

    public int last_ce_sent = 0;
    public int last_rules_sent = 0;
    public boolean VISUALIZE_EVALUATION_AFTER_REVISION = false;

    public List<ArgumentationAgent> a_l = new LinkedList<ArgumentationAgent>();
    ArgumentationAgent token = null;
    public ArgumentationState state = null;
    Path dp = null, sp = null;
    Ontology o = null;
    FTKBase dm = null;
    FeatureTerm solution = null;
    int round = 0;
    int anotherRound = 2;

    public AMAIL(List<RuleHypothesis> l_h, FeatureTerm a_solution,
                 List<List<FeatureTerm>> l_examples,
                 List<ArgumentAcceptability> l_aa,
                 List<ArgumentationBasedLearning> l_l,
                 Path a_dp, Path a_sp, Ontology a_o, FTKBase a_dm) {

        for(int i = 0;i<l_h.size();i++) {
            ArgumentationAgent a = new ArgumentationAgent("Agent " + (i+1), l_examples.get(i), l_aa.get(i), new RuleHypothesis(l_h.get(i)), l_l.get(i));
            a_l.add(a);
        }
        token = a_l.get(0);

        // Initial state of argumentation:
        state = new ArgumentationState();
        for(ArgumentationAgent a:a_l) {
            for (Rule r : a.m_hypothesis.getRules()) {
                state.addNewRoot(a.m_name, new Argument(r, a.m_name));
            }
        }

        dp = a_dp;
        sp = a_sp;
        o = a_o;
        dm = a_dm;
        solution = a_solution;
    }

    public String getToken() {
        return token.m_name;
    }

    public boolean moreRoundsP() {
        return anotherRound>0;
    }

    public void round(boolean singleMessage) throws Exception
    {
        boolean anyAttack = false;
        anotherRound--;

        System.out.println("");
        System.out.println("AMAIL: agent " + token.m_name + " has the token in round " + round);


        for(ArgumentationAgent a:a_l) {
            if (a!=token) {
                for(Rule r:a.m_hypothesis.getRules()) {
                    System.out.println("AA of A"+state.getRoot(r).m_ID+": " + token.m_aa.degree(new Argument(r)));
                }
            }
        }

        // Try to defend all of the self arguments which are currently defeated:
        List<ArgumentationTree> toDefend = state.getDefeatedUnsettled(token.m_name);
        System.out.println("AMAIL: agent " + token.m_name + " has to defend " + toDefend.size() + " roots");
        for (ArgumentationTree at : toDefend) {
            // Defend argument:
            // Get all the arguments that have to be attacked:
            List<Argument> challengers = at.getChallengers();
            List<Pair<Argument,Argument>> attacks = new LinkedList<Pair<Argument,Argument>>();
            System.out.println("AMAIL: agent " + token.m_name + " has to attack " + challengers.size() + " arguments to defend it's root");
            for (Argument a : challengers) {
                Argument b = findSingleCounterArgument(a, token, state, at, dp, sp, o, dm);
                if (b == null) {
                    at.settle(a);
                    attacks.clear();
                    break;
                } else {
                    attacks.add(new Pair<Argument,Argument>(a,b));
                }
            }
            if (attacks.size()==0) at.settle(at.getRoot());

            System.out.println("AMAIL: agent " + token.m_name + " cand send " + attacks.size() + " attacks for this root!");

            // Send the attacks!
            for(Pair<Argument,Argument> attack:attacks) {
                at.addAttack(attack.m_a, attack.m_b);
                last_rules_sent++;
                anyAttack = true;
                if (singleMessage) break;
            }
            if (singleMessage && anyAttack) break;
        }

        // Settle all the arguments of the other agent which are acceptable:
        List<Pair<Argument, ArgumentationTree>> acceptable = state.getAcceptable(token.m_name, token.m_aa);
        for (Pair<Argument, ArgumentationTree> a : acceptable) {
            a.m_b.settle(a.m_a);
        }

        // Find unacceptable arguments "I", and attack one:
        if (!singleMessage || !anyAttack) {
            List<Pair<Argument, ArgumentationTree>> unacceptable = state.getUnacceptable(token.m_name, token.m_aa);
            System.out.println("AMAIL: agent " + token.m_name + " finds " + unacceptable.size() + " arguments of the other agent unacceptable");
            for (Pair<Argument, ArgumentationTree> a : unacceptable) {
                // Attack argument:
                Argument b = findSingleCounterArgument(a.m_a, token, state, a.m_b, dp, sp, o, dm);
                if (b == null) {
                    a.m_b.settle(a.m_a);
                    System.out.println("AMAIL: agent " + token.m_name + " settling for an opponent root!");
                } else {
                    System.out.println("AMAIL: agent " + token.m_name + " sending an attack!");
                    a.m_b.addAttack(a.m_a, b);
                    if (b.m_type == Argument.ARGUMENT_EXAMPLE) {
                        ArgumentationAgent other = null;
                        for(ArgumentationAgent a_tmp:a_l) {
                            if (a_tmp.m_name.equals(a.m_a.m_agent)) {
                                other = a_tmp;
                                break;
                            }
                        }
                        if (other!=null) {
                            token.sendExample(other, b.m_example, state);
                        } else {
                            System.err.println("Couldn't find the agent corresponding to a.m_a.m_agent!!");
                        }
                    }
                    last_rules_sent++;
                    anyAttack = true;
                    break;
                }
            }
        }

        if (!anyAttack) {
            // Check for uncovered:
            for(FeatureTerm e:token.m_examples) {
                if (e.readPath(sp).equivalents(solution)) {
                    for(ArgumentationAgent other:a_l) {
                        if (other!=token &&
                            !other.m_examples.contains(e) &&
                            !other.m_hypothesis.coveredByAnyRule(e.readPath(dp))) {

                            System.out.println("AMAIL: Agent " + token.m_name + " sending uncovered example " + e.getName().get() + " to " + other.m_name);

                            token.sendExample(other,e, state);
                            last_ce_sent++;
                            anyAttack = true;
                            break;
                        }
                    }
                    if (anyAttack) break;
                }
            }
        }

        // Belief Revision:
        for(ArgumentationAgent a:a_l) {
            if (a==token) a.beliefRevision(state, solution, dp, sp, o, dm, true);
                     else a.beliefRevision(state, solution, dp, sp, o, dm, false);
        }

        if (anyAttack) {
            anotherRound = a_l.size();
        }

        // pass the token:
        int pos = a_l.indexOf(token);
        pos++;
        if (pos>=a_l.size()) pos=0;
        token = a_l.get(pos);
        round++;
    }

    public List<RuleHypothesis> result() {
        List<RuleHypothesis> ret = new LinkedList<RuleHypothesis>();
        for(ArgumentationAgent a:a_l) {
            ret.add(a.m_hypothesis);
        }

        return ret;
    }


    public static List<RuleHypothesis> argue(List<RuleHypothesis> l_h, FeatureTerm a_solution,
                                             List<List<FeatureTerm>> l_examples,
                                             List<ArgumentAcceptability> l_aa,
                                             List<ArgumentationBasedLearning> l_l,
                                             Path dp, Path sp, Ontology o, FTKBase dm) throws Exception {

        AMAIL argumentation = new AMAIL(l_h,a_solution,l_examples,l_aa,l_l,dp,sp,o,dm);
        while(argumentation.moreRoundsP()) argumentation.round(false);

        System.out.println("Argumentation state at the end of AMAIL:");
        System.out.println(argumentation.state);

        return argumentation.result();
    }

    public static Argument findSingleCounterArgument(Argument argumentToAttack, ArgumentationAgent attacker,
        ArgumentationState state, ArgumentationTree context, Path dp, Path sp, Ontology o, FTKBase dm) throws Exception {
        List<Argument> settled = state.getSettled();

//        System.out.println("findSingleCounterArgument: attacking argument " + argumentToAttack.toStringNOOS(dm));
//        System.out.println("findSingleCounterArgument: " + settled.size() + " settled arguments.");

        try {
            System.out.println("findSingleCounterArgument: looking for counterarguments...");
            Argument counterArgument = ABUI.generateBestCounterArgumentABUI(argumentToAttack, attacker.m_examples, settled, attacker.m_aa, dp, sp, dm, o);

            if (counterArgument != null && !context.containsEquivalent(counterArgument)) {
                counterArgument.m_agent = attacker.m_name;
                return counterArgument;
            }


            System.out.println("findSingleCounterArgument: no counterargument found, looking for counterexamples...");
            if (argumentToAttack.m_agent==null) {
                System.err.println("Argument agent generator is null!!!");
            }
            List<Argument> counterArguments = generateCounterExamples(argumentToAttack, attacker.m_examples, attacker.m_alreadySentExamples.get(argumentToAttack.m_agent), settled, attacker.m_aa, dp, sp, dm, o);
            if (counterArguments != null && counterArguments.size() > 0) {
                counterArguments.get(0).m_agent = attacker.m_name;
                return counterArguments.get(0);
            }

            System.out.println("findSingleCounterArgument: no counterexamples found either");

            return null;
        } catch (FeatureTermException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static void evaluateRule(Rule r, List<FeatureTerm> examples, Path dp, Path sp) throws FeatureTermException {
        int correct = 0;
        int incorrect = 0;
        int total = 0;

        for (FeatureTerm e : examples) {
            FeatureTerm d = e.readPath(dp);

            if (r.pattern.subsumes(d)) {
                FeatureTerm s = e.readPath(sp);
                if (r.solution.equivalents(s)) {
                    correct++;
                } else {
                    incorrect++;
                }
            }

            total++;
        }

        System.out.println("evaluateRule: +" + correct + " -" + incorrect + " ("
            + (correct + incorrect > 0 ? ((100.0f * correct) / (correct + incorrect)) + "%" : "-") + ") covering "
            + ((100.0f * (correct + incorrect)) / total) + "%");
    }

    public static void evaluateRuleSet(RuleHypothesis h_raw, List<Argument> acceptedArguments, List<FeatureTerm> examples1, List<FeatureTerm> examples2, Path dp, Path sp, FTKBase dm) throws FeatureTermException, Exception {
        List<FeatureTerm> examples = new LinkedList<FeatureTerm>();
        examples.addAll(examples1);
        for (FeatureTerm e : examples2) {
            if (!examples.contains(e)) {
                examples.add(e);
            }
        }

        RuleHypothesis h = new RuleHypothesis(h_raw);
        if (acceptedArguments != null) {
            for (Argument a : acceptedArguments) {
                h.addRule(a.m_rule);
            }
        }

        System.out.println("/* rule set evaluation ***************************************\\");
        for (Rule r : h.getRules()) {
            evaluateRule(r, examples, dp, sp);
        }
        System.out.println("\\*************************************************************/");
    }

    
    public static List<Argument> generateCounterExamples(Argument a, Collection<FeatureTerm> examples1, Collection<FeatureTerm> already_sent, Collection<Argument> acceptedArguments, ArgumentAcceptability aa, Path dp, Path sp, FTKBase dm, Ontology o) throws FeatureTermException {

        List<Argument> counterArguments = new LinkedList<Argument>();

        if (a.m_type != Argument.ARGUMENT_RULE) {
            return null;
        }

        for (FeatureTerm e : examples1) {
            if (already_sent==null || !already_sent.contains(e)) {
                FeatureTerm d = e.readPath(dp);
                FeatureTerm s = e.readPath(sp);

                if (a.m_rule.pattern.subsumes(d)) {
                    if (!a.m_rule.solution.equivalents(s)) {
                        counterArguments.add(new Argument(e));
                    }
                }
            }
        }

        return counterArguments;
    }
}
