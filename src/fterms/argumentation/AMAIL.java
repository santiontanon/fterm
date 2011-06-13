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
import java.util.HashMap;
import util.Pair;


/*
 * @author santi
 */
public class AMAIL {

    public int last_counterexamples_sent = 0;
    public int last_uncoveredexamples_sent = 0;
    public int last_skepticalexamples_sent = 0;
    public int last_rules_sent = 0;
    public boolean VISUALIZE_EVALUATION_AFTER_REVISION = false;

    public List<ArgumentationAgent> a_l = new LinkedList<ArgumentationAgent>();
    HashMap<String,ArgumentationAgent> agentNameTable = new HashMap<String,ArgumentationAgent>();
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
                 boolean credulous,
                 Path a_dp, Path a_sp, Ontology a_o, FTKBase a_dm) {

        for(int i = 0;i<l_h.size();i++) {
            ArgumentationAgent a = new ArgumentationAgent("Agent " + (i+1), l_examples.get(i), l_aa.get(i), new RuleHypothesis(l_h.get(i)), l_l.get(i), credulous);
            a_l.add(a);
            agentNameTable.put(a.m_name,a);
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


    // This constructor is only useful so that it can be swapped with the AMAIL2 one:
    // AMAIL2 assumed "credulous" agents, and so does this constructor:
    public AMAIL(RuleHypothesis h1, RuleHypothesis h2, FeatureTerm a_solution,
        Collection<FeatureTerm> examples1, Collection<FeatureTerm> examples2,
        ArgumentAcceptability aa1, ArgumentAcceptability aa2,
        ABUI l1, ABUI l2,
        Path a_dp, Path a_sp, Ontology a_o, FTKBase a_dm) {
        a_l.add(new ArgumentationAgent("Agent 1", examples1, aa1, new RuleHypothesis(h1), l1));
        a_l.add(new ArgumentationAgent("Agent 2", examples2, aa2, new RuleHypothesis(h2), l2));
        agentNameTable.put(a_l.get(0).m_name,a_l.get(0));
        agentNameTable.put(a_l.get(1).m_name,a_l.get(1));
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


    public String getNextAgent() {
        int pos = a_l.indexOf(token);
        pos++;
        if (pos>=a_l.size()) pos=0;
        return a_l.get(pos).m_name;
    }

    public String getPreviousAgent() {
        int pos = a_l.indexOf(token);
        pos--;
        if (pos<0) pos=a_l.size()-1;
        return a_l.get(pos).m_name;
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
                    ArgumentationTree at = state.getTree(r);
                    System.out.println("AA of A"+state.getRoot(r).m_ID+" (by A" + a.m_name + "): " + token.m_aa.degree(new Argument(r)) + " (Tree size: " + at.getSize() + ")");
                }
            }
        }

        // Try to defend all of the self arguments which are currently defeated:
        List<ArgumentationTree> toDefend = state.getDefeated(token.m_name);
        System.out.println("AMAIL: agent " + token.m_name + " has to defend " + toDefend.size() + " roots");
        for (ArgumentationTree at : toDefend) {
            // Defend argument:
            // Get all the arguments that have to be attacked:
            List<Argument> challengers = at.getChallengers();
            List<Pair<Argument,Argument>> attacks = new LinkedList<Pair<Argument,Argument>>();
            System.out.println("AMAIL: agent " + token.m_name + " has to attack " + challengers.size() + " arguments to defend its root");
            for (Argument a : challengers) {
                Argument b = findSingleCounterArgument(a, token, state, at, dp, sp, o, dm, a_l);
                if (b == null) {
                    at.settle(a,token.m_name);
                    attacks.clear();
                    break;
                } else {
                    attacks.add(new Pair<Argument,Argument>(a,b));
                }
            }

            System.out.println("AMAIL: agent " + token.m_name + " can send " + attacks.size() + " attacks for this root!");

            // Send the attacks!
            for(Pair<Argument,Argument> attack:attacks) {
                at.addAttack(attack.m_a, attack.m_b);
                if (attack.m_b.m_type == Argument.ARGUMENT_EXAMPLE) {
                    ArgumentationAgent other = agentNameTable.get(attack.m_a.m_agent);
                    if (other!=null) {
                        if (token.sendExample(other, attack.m_b.m_example, state))
                            last_counterexamples_sent++;
                    } else {
                        System.err.println("Couldn't find the agent corresponding to attack.m_a.m_agent!!");
                    }
                } else {
                    last_rules_sent++;
                }
                anyAttack = true;
                if (singleMessage) break;
            }
            if (singleMessage && anyAttack) break;
        }

        // Settle all the arguments of the other agent which are acceptable:
        List<Pair<Argument, ArgumentationTree>> acceptable = state.getAcceptable(token.m_name, token.m_aa, a_l);
        for (Pair<Argument, ArgumentationTree> a : acceptable) {
            a.m_b.settle(a.m_a,token.m_name);
        }

        // Find unacceptable arguments "I", and attack one:
        if (!singleMessage || !anyAttack) {
            List<Pair<Argument, ArgumentationTree>> unacceptable = state.getUnacceptable(token.m_name, token.m_aa, a_l);
            System.out.println("AMAIL: agent " + token.m_name + " finds " + unacceptable.size() + " arguments of the other agent unacceptable");
            boolean anyExampleReceived = false;
            for (Pair<Argument, ArgumentationTree> a : unacceptable) {
                // Attack argument:
                Argument b = findSingleCounterArgument(a.m_a, token, state, a.m_b, dp, sp, o, dm, a_l);
                if (b == null) {
                    if (token.m_credulous) {
                        // Credulous agents believe in arguments they cannot attack:
                        a.m_b.settle(a.m_a, token.m_name);
                        System.out.println("AMAIL: credulous agent " + token.m_name + " settling for an opponent root.");
                    } else {
                        // Skeptical agents ask for evidence for arguments they cannot attack:
                        ArgumentationAgent other = agentNameTable.get(a.m_a.m_agent);
                        // If some example has alredy been sent, maybe the argument is already acceptable, so, we have to check again:
                        if (!anyExampleReceived || !token.m_aa.accepted(a.m_a)) {
                            if (other!=null) {
                                List<FeatureTerm> examples = generateEndorsingExamples(a.m_a,other.m_examples,other.m_alreadySentExamples.get(token.m_name),dp,sp,dm,o);
                                if (examples.isEmpty()) {
                                    List<FeatureTerm> tmp = generateEndorsingExamples(a.m_a,other.m_examples,null,dp,sp,dm,o);
                                    System.err.println("AMAIL: skeptical agent " + token.m_name + " asking opponent " + a.m_a.m_agent + " for positive examples of a root, but couldn't find any!!!");
                                    System.err.println("AMAIL: " + tmp.size() + " examples available, but all were already sent.");
                                    for(FeatureTerm tmp_e:tmp) {
                                        System.err.println("AMAIL: " + tmp_e.getName() + "(" + token.m_examples.contains(tmp_e) + "," + token.m_aa.m_examples.contains(tmp_e) + ")");
                                    }
                                    System.err.println("AMAIL: argument profile for " + a.m_a);
                                    System.err.println("AMAIL: argument profile for " + token.m_name + ": " + token.coveredExamples(a.m_a, dp, sp) + " (AA: " + token.m_aa.degree(a.m_a) + " -> " + token.m_aa.accepted(a.m_a) + ")");
                                    System.err.println("AMAIL: argument profile for " + other.m_name + ": " + other.coveredExamples(a.m_a, dp, sp) + " (AA: " + other.m_aa.degree(a.m_a) + " -> " + other.m_aa.accepted(a.m_a)+ ")");
                                    System.exit(1);
                                } else {
                                    if (other.sendExample(token, examples.get(0), state))
                                        last_skepticalexamples_sent++;
                                    System.out.println("AMAIL: skeptical agent " + token.m_name + " asking opponent " + a.m_a.m_agent + " for positive examples of a root.");
                                    anyExampleReceived = true;;
                                }
                            }
                        }
                    }
                } else {
                    if (b.m_type == Argument.ARGUMENT_EXAMPLE) System.out.println("AMAIL: agent " + token.m_name + " sending an counterexample attack to " + a.m_a.m_agent + "!");
                                                          else System.out.println("AMAIL: agent " + token.m_name + " sending a rule attack to " + a.m_a.m_agent + "!");
                    a.m_b.addAttack(a.m_a, b);
                    if (b.m_type == Argument.ARGUMENT_EXAMPLE) {
                        ArgumentationAgent other = agentNameTable.get(a.m_a.m_agent);
                        if (other!=null) {
                            if (token.sendExample(other, b.m_example, state))
                                last_counterexamples_sent++;
                        } else {
                            System.err.println("Couldn't find the agent corresponding to a.m_a.m_agent!!");
                        }
                    } else {
                        last_rules_sent++;
                    }
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
                            other.m_hypothesis.coveredByAnyRule(e.readPath(dp))==null) {

                            System.out.println("AMAIL: Agent " + token.m_name + " sending uncovered example " + e.getName().get() + " to " + other.m_name);

                            if (token.sendExample(other,e, state))
                            last_uncoveredexamples_sent++;
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
            if (a==token) a.beliefRevision(state, solution, dp, sp, o, dm, true, a_l);
                     else a.beliefRevision(state, solution, dp, sp, o, dm, false, a_l);
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
                                             boolean credulous,
                                             Path dp, Path sp, Ontology o, FTKBase dm) throws Exception {

        AMAIL argumentation = new AMAIL(l_h,a_solution,l_examples,l_aa,l_l,credulous,dp,sp,o,dm);
        while(argumentation.moreRoundsP()) argumentation.round(false);

        System.out.println("Argumentation state at the end of AMAIL:");
        System.out.println(argumentation.state);

        return argumentation.result();
    }

    public static Argument findSingleCounterArgument(Argument argumentToAttack, ArgumentationAgent attacker,
        ArgumentationState state, ArgumentationTree context, Path dp, Path sp, Ontology o, FTKBase dm,
        List<ArgumentationAgent> agents) throws Exception {
        List<Argument> settled = state.getSettled(agents);

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
            List<Argument> counterArguments = generateCounterExamples(argumentToAttack, attacker.m_examples, attacker.m_alreadySentExamples.get(argumentToAttack.m_agent), dp, sp, dm, o);
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

    
    public static List<Argument> generateCounterExamples(Argument a, Collection<FeatureTerm> examples1, Collection<FeatureTerm> already_sent, Path dp, Path sp, FTKBase dm, Ontology o) throws FeatureTermException {
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


    public static List<FeatureTerm> generateEndorsingExamples(Argument a, Collection<FeatureTerm> examples1, Collection<FeatureTerm> already_sent, Path dp, Path sp, FTKBase dm, Ontology o) throws FeatureTermException {
        List<FeatureTerm> endorsingArguments = new LinkedList<FeatureTerm>();

        if (a.m_type != Argument.ARGUMENT_RULE) {
            return null;
        }

        for (FeatureTerm e : examples1) {
            if (already_sent==null || !already_sent.contains(e)) {
                FeatureTerm d = e.readPath(dp);
                FeatureTerm s = e.readPath(sp);

                if (a.m_rule.pattern.subsumes(d)) {
                    if (a.m_rule.solution.equivalents(s)) {
                        endorsingArguments.add(e);
                    }
                }
            }
        }

        return endorsingArguments;
    }
}
