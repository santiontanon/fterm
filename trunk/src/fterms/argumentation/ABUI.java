/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fterms.argumentation;

import fterms.FTAntiunification;
import fterms.FTKBase;
import fterms.FTRefinement;
import fterms.FTUnification;
import fterms.FeatureTerm;
import fterms.Ontology;
import fterms.Path;
import fterms.exceptions.FeatureTermException;
import fterms.learning.Hypothesis;
import fterms.learning.Rule;
import fterms.learning.RuleHypothesis;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author santi
 */
public class ABUI extends ArgumentationBasedLearning {

    public static int DEBUG = 0;

    public static int ABUI_call_count = 0;
    public static int ABUI_VERSION = 1;
    public static boolean GREEDY_ABUI = true; // If this is set to true, ABUI will stop as soon as it finds the first acceptable rule
                                         // otherwise, it will continue trying all the seeds and then select the best one found.

    public static Argument generateBestCounterArgumentABUI(Argument a, Collection<FeatureTerm> examples, Collection<Argument> acceptedArguments,
            ArgumentAcceptability aa, Path dp, Path sp, FTKBase dm, Ontology o) throws FeatureTermException, Exception {
        List<Argument> args = new LinkedList<Argument>();
        List<FeatureTerm> allSolutions = Hypothesis.differentSolutions(examples, sp);
        ABUI abui = new ABUI();

        for (FeatureTerm solution : allSolutions) {

            if (!solution.equivalents(a.m_rule.solution)) {
                List<FeatureTerm> positive = new LinkedList<FeatureTerm>();
                List<FeatureTerm> negative = new LinkedList<FeatureTerm>();

                for (FeatureTerm e : examples) {
                    if (e.readPath(sp).equivalents(solution)) {
                        positive.add(e.readPath(dp));
                    } else {
                        negative.add(e.readPath(dp));
                    }
                }

                Argument beta = null;
                switch(ABUI_VERSION) {
                    case 1:
                        beta = abui.ABUI(positive, negative, solution, acceptedArguments, a.m_rule.pattern, aa, o, dm);
                        break;
                    case 2:
                        beta = abui.ABUI2(positive, negative, positive, solution, acceptedArguments, a.m_rule.pattern, aa, o, dm);
                        break;
                }
                if (beta != null) {
                    args.add(beta);
                }
            }
        }

        {
            Argument best_beta = null;
            float best_conf = 0.0f;

            for (Argument beta : args) {
                float conf = beta.m_rule.reliability;
                if (best_beta == null || conf > best_conf) {
                    best_beta = beta;
                    best_conf = conf;
                }
            }

            return best_beta;
        }

    }

    public RuleHypothesis learnConceptABUI(Collection<FeatureTerm> examples, FeatureTerm solution, Collection<Argument> acceptedArguments, ArgumentAcceptability aa, Path dp, Path sp, Ontology o, FTKBase dm) throws Exception {
        List<Argument> args = new LinkedList<Argument>();
        FeatureTerm any = o.getSort("any").createFeatureTerm();

        if (DEBUG >= 1) {
            System.out.println("learnConceptABUI: starting...");
        }

        List<FeatureTerm> allPositive = new LinkedList<FeatureTerm>();
        List<FeatureTerm> positive = new LinkedList<FeatureTerm>();
        List<FeatureTerm> negative = new LinkedList<FeatureTerm>();

        for (FeatureTerm e : examples) {
            if (e.readPath(sp).equivalents(solution)) {
                positive.add(e.readPath(dp));
                allPositive.add(e.readPath(dp));
            } else {
                negative.add(e.readPath(dp));
            }
        }

        if (DEBUG >= 1) {
            System.out.println("learnConceptABUI: starting with class " + solution.toStringNOOS(dm) + " with " + positive.size() + "/" + negative.size() + " examples.");
        }

        while (positive.size() > 0) {
            Argument a = null;
            switch(ABUI_VERSION) {
                case 1:
                    a = ABUI(positive, negative, solution, acceptedArguments, any, aa, o, dm);
                    break;
                case 2:
                    a = ABUI2(positive, negative, allPositive, solution, acceptedArguments, any, aa, o, dm);
                    break;
            }

            if (a != null) {
                args.add(a);

                List<FeatureTerm> toDelete = new LinkedList<FeatureTerm>();
                for (FeatureTerm d : positive) {
                    if (a.m_rule.pattern.subsumes(d)) {
                        toDelete.add(d);
                    }
                }
                positive.removeAll(toDelete);
                if (DEBUG >= 1) {
                    System.out.println("learnConceptABUI: argument covers " + toDelete.size() + " examples: " + a.m_rule.reliability);
                    System.out.println(a.toStringNOOS(dm));
                }
            } else {
                break;
            }
        }

        if (DEBUG >= 1) {
            System.out.println("learnConceptABUI: done, returning " + args.size() + " arguments.");
        }

        RuleHypothesis h = new RuleHypothesis();
        for (Argument a : args) {
            h.addRule(a.m_rule);
        }

        return h;
    }

    public List<Argument> coverExamplesABUI(List<FeatureTerm> examples, Collection<Argument> acceptedArguments, ArgumentAcceptability aa, Path dp, Path sp, Ontology o, FTKBase dm) throws Exception {
        List<Argument> args = new LinkedList<Argument>();
        FeatureTerm any = o.getSort("any").createFeatureTerm();

        if (DEBUG >= 1) {
            System.out.println("coverExamplesABUI: starting...");
        }

        List<FeatureTerm> allSolutions = Hypothesis.differentSolutions(examples, sp);

        for (FeatureTerm solution : allSolutions) {
            List<FeatureTerm> allPositive = new LinkedList<FeatureTerm>();
            List<FeatureTerm> positive = new LinkedList<FeatureTerm>();
            List<FeatureTerm> negative = new LinkedList<FeatureTerm>();

            for (FeatureTerm e : examples) {
                if (e.readPath(sp).equivalents(solution)) {
                    positive.add(e.readPath(dp));
                    allPositive.add(e.readPath(dp));
                } else {
                    negative.add(e.readPath(dp));
                }
            }

            if (DEBUG >= 1) {
                System.out.println("coverExamplesABUI: starting with class " + solution.toStringNOOS(dm) + " with " + positive.size() + "/" + negative.size() + " examples.");
            }

            while (positive.size() > 0) {
                Argument a = null;
                switch(ABUI_VERSION) {
                    case 1:
                        a = ABUI(positive, negative, solution, acceptedArguments, any, aa, o, dm);
                        break;
                    case 2:
                        a = ABUI2(positive, negative, allPositive, solution, acceptedArguments, any, aa, o, dm);
                        break;
                }

                if (a != null) {
                    args.add(a);

                    List<FeatureTerm> toDelete = new LinkedList<FeatureTerm>();
                    for (FeatureTerm d : positive) {
                        if (a.m_rule.pattern.subsumes(d)) {
                            toDelete.add(d);
                        }
                    }
                    positive.removeAll(toDelete);
                    if (DEBUG >= 1) {
                        System.out.println("coverExamplesABUI: argument covers " + toDelete.size() + " examples: " + a.m_rule.reliability);
                    }
                } else {
                    break;
                }
            }

            if (DEBUG >= 1) {
                System.out.println("coverExamplesABUI: done with class " + solution.toStringNOOS(dm) + " leaving  " + positive.size() + " uncovered.");
            }
        }

        if (DEBUG >= 1) {
            System.out.println("coverExamplesABUI: done, returning " + args.size() + " arguments.");
        }

        return args;
    }

    public RuleHypothesis coverUncoveredExamples(List<FeatureTerm> examples, FeatureTerm solution, RuleHypothesis h, List<Argument> acceptedArguments, ArgumentAcceptability aa, Path dp, Path sp, Ontology o, FTKBase dm) throws Exception {
        List<Argument> args = new LinkedList<Argument>();
        FeatureTerm any = o.getSort("any").createFeatureTerm();

        if (DEBUG >= 1) {
            System.out.println("coverUncoveredExamplesABUI: starting...");
        }

        List<FeatureTerm> allPositive = new LinkedList<FeatureTerm>();
        List<FeatureTerm> positive = new LinkedList<FeatureTerm>();
        List<FeatureTerm> negative = new LinkedList<FeatureTerm>();

        for (FeatureTerm e : examples) {
            if (e.readPath(sp).equivalents(solution)) {
                boolean covered = false;
                FeatureTerm d = e.readPath(dp);
                FeatureTerm s = e.readPath(sp);
                if (h != null) {
                    for (Rule r : h.getRules()) {
                        if (r.pattern.subsumes(d) &&
                            r.solution.equivalents(s)) {
                            covered = true;
                            break;
                        }
                    }
                }

                if (!covered) {
                    positive.add(e.readPath(dp));
                }
                allPositive.add(e.readPath(dp));
            } else {
                negative.add(e.readPath(dp));
            }
        }

        if (DEBUG >= 1) {
            System.out.println("coverUncoveredExamplesABUI: starting with class " + solution.toStringNOOS(dm) + " with " + positive.size() + "/" + negative.size() + " examples and " + acceptedArguments.size() + " accepted arguments.");
            System.out.println("coverUncoveredExamplesABUI: aa has " + aa.m_examples.size() + " examples.");
        }

        while (positive.size() > 0) {
            Argument a = null;
            switch(ABUI_VERSION) {
                case 1:
                    a = ABUI(positive, negative, solution, acceptedArguments, any, aa, o, dm);
                    break;
                case 2:
                    a = ABUI2(positive, negative, allPositive, solution, acceptedArguments, any, aa, o, dm);
                    break;
            }

            if (a != null) {
                args.add(a);

                List<FeatureTerm> toDelete = new LinkedList<FeatureTerm>();
                for (FeatureTerm d : positive) {
                    if (a.m_rule.pattern.subsumes(d)) {
                        toDelete.add(d);
                    }
                }
                positive.removeAll(toDelete);
                if (DEBUG >= 1) {
                    System.out.println("coverUncoveredExamplesABUI: argument covers " + toDelete.size() + " examples: " + a.m_rule.reliability);
                }
            } else {
                break;
            }
        }


        if (DEBUG >= 1) {
            System.out.println("coverUncoveredExamplesABUI: done with class " + solution.toStringNOOS(dm) + " leaving  " + positive.size() + " uncovered.");
        }

        if (DEBUG >= 1) {
            System.out.println("coverUncoveredExamplesABUI: done, returning " + args.size() + " arguments.");
        }

        for (Argument a : args) {
            h.addRule(a.m_rule);
        }

        return h;
    }


    /*
     * This is the ABUI induction algorithm as explained in our "Concept Convergence" paper submitted to AAMAS 2010:
     * - returns an argument which is more specific than "g", and which is not attacked by any argument in "acceptedArguments"
     *
     */

    public Argument ABUI(List<FeatureTerm> positiveDescriptions, List<FeatureTerm> negativeDescriptions, FeatureTerm solution,
            Collection<Argument> acceptedArguments, FeatureTerm g,
            ArgumentAcceptability aa, Ontology o, FTKBase dm) throws Exception {
        List<Argument> H = new LinkedList<Argument>();  // This list will contain the candidate arguments

        ABUI_call_count++;

        for (FeatureTerm e : positiveDescriptions) {
            if (DEBUG >= 2) {
                System.out.println("New seed...");
            }
            if (g.subsumes(e)) {
                FeatureTerm c = e.clone(dm, o);
                if (c.getName() != null) {
                    c.setName(null);
                }
                while (c != null) {

                    Argument a = new Argument(new Rule(c, solution, 0.0f, 0));
                    if (DEBUG >= 2) {
                        System.out.println(c.toStringNOOS(dm));
                    }
                    if (DEBUG >= 2) {
                        System.out.println("Generalization confidence: " + aa.degree(a));
                    }
                    if (aa.accepted(a)) {
                        a.m_rule.reliability = aa.degree(a);
                        H.add(a);
                    }

                    List<FeatureTerm> G = FTRefinement.getGeneralizations(c, dm, o);
                    List<FeatureTerm> G2 = new LinkedList<FeatureTerm>();

                    if (DEBUG >= 2) {
                        System.out.println(G.size() + " generalizations.");
                    }

                    for (FeatureTerm h : G) {
                        if (g.subsumes(h) && !h.subsumes(g)) {
                            boolean attacked = false;
                            for (Argument a2 : acceptedArguments) {
                                if (!a2.m_rule.solution.equivalents(solution) &&
                                    h.subsumes(a2.m_rule.pattern)) {
                                    attacked = true;
                                    break;
                                }
                            }
                            if (!attacked) {
                                G2.add(h);
                            }
                        }
                    }

                    if (DEBUG >= 2) {
                        System.out.println(G2.size() + " filtered generalizations.");
                    }

                    c = null;
                    {
                        FeatureTerm best_c = null;
                        float best_conf = 0.0f;
                        float max_c = 0.0f;
                        float min_c = 1.0f;

                        for (FeatureTerm c2 : G2) {
                            Argument a2 = new Argument(new Rule(c2, solution, 0.0f, 0));
                            float conf = aa.degree(a2);
                            if (best_c == null || conf > best_conf) {
                                best_c = c2;
                                best_conf = conf;
                            }

                            if (conf>max_c) max_c = conf;
                            if (conf<min_c) min_c = conf;
                        }

                        c = best_c;
                        if (DEBUG >= 2) {
                            System.out.println("Generalization confidence: " + best_conf +
                                               " the range was [" + min_c + "-" + max_c + "] (with " + G2.size() + ")");
                        }
                    }
                }
                
                if (GREEDY_ABUI && !H.isEmpty()) break;
            }
        }

        {
            Argument best_a = null;
            float best_conf = 0.0f;

            for (Argument a : H) {
                float conf = a.m_rule.reliability;
                if (best_a == null || conf > best_conf) {
                    best_a = a;
                    best_conf = conf;
                }
            }

            return best_a;
        }
    }


    /*
     * This is a modification on the original ABUI algorithm. Instead of generalizing by generating generalization refinements,
     * it generalizes by unifying the current term with each of the uncovered positive examples
     * - returns an argument which is more specific than "g", and which is not attacked by any argument in "acceptedArguments"
     *
     */
    public Argument ABUI2(List<FeatureTerm> uncoveredPositiveDescriptions, List<FeatureTerm> negativeDescriptions, List<FeatureTerm> positiveDescriptions, FeatureTerm solution,
            Collection<Argument> acceptedArguments, FeatureTerm g,
            ArgumentAcceptability aa, Ontology o, FTKBase dm) throws Exception {

        List<Argument> H = new LinkedList<Argument>();  // This list will contain the candidate arguments
        if (acceptedArguments==null) acceptedArguments = new LinkedList<Argument>();

        ABUI_call_count++;

        if (DEBUG >= 1) {
            System.out.println("ABUI2: UPD: " + uncoveredPositiveDescriptions.size() + " P/N:" + positiveDescriptions.size() + "/" + negativeDescriptions.size());
        }

        for (FeatureTerm e : uncoveredPositiveDescriptions) {
            List<FeatureTerm> notYetCoveredPositiveDescriptions = new LinkedList<FeatureTerm>();
            notYetCoveredPositiveDescriptions.addAll(positiveDescriptions);

            if (DEBUG >= 2) {
                System.out.println("ABUI2: New seed...");
            }
            if (g.subsumes(e)) {
                FeatureTerm c = e.clone(dm, o);
                if (c.getName() != null) {
                    c.setName(null);
                }
                while (c != null) {
                    // remove all the already covered:
                    {
                        List<FeatureTerm> toDelete = new LinkedList<FeatureTerm>();
                        for(FeatureTerm d:notYetCoveredPositiveDescriptions)
                            if (c.subsumes(d)) {
                                toDelete.add(d);
                            }
                        notYetCoveredPositiveDescriptions.removeAll(toDelete);
                    }

                    Argument a = new Argument(new Rule(c, solution, 0.0f, 0));
                    if (DEBUG >= 2) {
                        System.out.println(c.toStringNOOS(dm));
                    }
                    if (DEBUG >= 2) {
                        System.out.println("ABUI2: Generalization confidence: " + aa.degree(a));
                    }
                    if (aa.accepted(a)) {
                        a.m_rule.reliability = aa.degree(a);
                        H.add(a);
                    }

                    List<FeatureTerm> G = new LinkedList<FeatureTerm>();

                    for(FeatureTerm d:notYetCoveredPositiveDescriptions) {
                        FeatureTerm au = FTAntiunification.simpleAntiunification(c,d, o, dm);
                        if (au!=null) G.add(au);
                    }
                    List<FeatureTerm> G2 = new LinkedList<FeatureTerm>();

                    if (DEBUG >= 2) {
                        System.out.println("ABUI2: " + G.size() + " generalizations.");
                    }

                    for (FeatureTerm h : G) {
                        if (g.subsumes(h) && !h.subsumes(g)) {
                            boolean attacked = false;
                            for (Argument a2 : acceptedArguments) {
                                if (!a2.m_rule.solution.equivalents(solution) &&
                                    h.subsumes(a2.m_rule.pattern)) {
                                    attacked = true;
                                    break;
                                }
                            }
                            if (!attacked) {
                                G2.add(h);
                            }
                        }
                    }

                    if (DEBUG >= 2) {
                        System.out.println("ABUI2: " + G2.size() + " filtered generalizations.");
                    }

                    c = null;
                    {
                        FeatureTerm best_c = null;
                        float best_conf = 0.0f;
                        float max_c = 0.0f;
                        float min_c = 1.0f;

                        for (FeatureTerm c2 : G2) {
                            Argument a2 = new Argument(new Rule(c2, solution, 0.0f, 0));
                            float conf = aa.degree(a2);
                            if (best_c == null || conf > best_conf) {
                                best_c = c2;
                                best_conf = conf;
                            }

                            if (conf>max_c) max_c = conf;
                            if (conf<min_c) min_c = conf;
                        }

                        c = best_c;
                        if (DEBUG >= 2) {
                            System.out.println("ABUI2: Generalization confidence: " + best_conf +
                                               " the range was [" + min_c + "-" + max_c + "] (with " + G2.size() + ")");
                        }
                    }
                }
            }
            if (GREEDY_ABUI && !H.isEmpty()) break;            
        }

        {
            Argument best_a = null;
            float best_conf = 0.0f;

            for (Argument a : H) {
                float conf = a.m_rule.reliability;
                if (best_a == null || conf > best_conf) {
                    best_a = a;
                    best_conf = conf;
                }
            }

            return best_a;
        }
    }

}
