/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fterms.learning;

import fterms.Disintegration;
import fterms.FTKBase;
import fterms.FTUnification;
import fterms.FeatureTerm;
import fterms.Ontology;
import fterms.Path;
import fterms.Sort;
import fterms.exceptions.FeatureTermException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author santi
 */
public class PropertiesCN2 extends InductiveLearner {

    public static int DEBUG = 1;
    int BEAM_WIDTH = 3;
    boolean fast = false;

    public PropertiesCN2(int bw, boolean a_fast) {
        BEAM_WIDTH = bw;
        fast = a_fast;
    }

    public Hypothesis generateHypothesis(List<FeatureTerm> examples, Path dp, Path sp, Ontology o, FTKBase dm) throws Exception {
        RuleHypothesis h = new RuleHypothesis(true);
        Sort descriptionSort = null;
        List<FeatureTerm> differentSolutions = Hypothesis.differentSolutions(examples, sp);

        // Compute the default solution:
        {
            HashMap<FeatureTerm, Integer> classifiedExamples = new HashMap<FeatureTerm, Integer>();
            FeatureTerm best = null;
            int bestCount = 0;

            for (FeatureTerm example : examples) {
                FeatureTerm s = example.readPath(sp);
                Integer count = classifiedExamples.get(s);

                if (descriptionSort == null) {
                    FeatureTerm d = example.readPath(dp);
                    if (d != null) {
                        descriptionSort = d.getSort();
                    }
                }

                if (count == null) {
                    count = 0;
                }
                classifiedExamples.put(s, count + 1);

                if (best == null || count > bestCount) {
                    best = s;
                    bestCount = count;
                }
            }
            h.setDefaultSolution(best);
        }

        // CN2:
        {
            List<Example> remainingExamples = new LinkedList<Example>();
            for (FeatureTerm e : examples) {
                remainingExamples.add(new Example(e, dp, sp));
            }
            List<Selector> selectors = computeAllSelectors(remainingExamples, descriptionSort, dm);
            Rule rule = null;
            do {
                if (DEBUG >= 1) {
                    System.out.println("PropertiesCN2: " + remainingExamples.size() + " remaining examples...");
                }

                rule = findBestRule(remainingExamples, selectors, o, dm, descriptionSort, differentSolutions);
                if (rule != null) {
                    {
                        List<Example> covered = new LinkedList<Example>();
                        HashMap<FeatureTerm, List<Example>> classifiedCovered = new HashMap<FeatureTerm, List<Example>>();

                        // Find the examples covered by the rule:
                        for (Example e : rule.examplesCovered) {
                            covered.add(e);

                            List<Example> l = classifiedCovered.get(e.solution);
                            if (l == null) {
                                l = new LinkedList<Example>();
                                classifiedCovered.put(e.solution, l);
                            }
                            l.add(e);
                        }

                        // Remove them from the remainingExamples:
                        remainingExamples.removeAll(covered);

                        // Find the most common class:
                        {
                            FeatureTerm candidate = null;
                            int max = 0;

                            for (FeatureTerm s : classifiedCovered.keySet()) {
                                if (candidate == null || classifiedCovered.get(s).size() > max) {
                                    candidate = s;
                                    max = classifiedCovered.get(s).size();
                                }
                            }

                            if (max != covered.size()) {
                                System.out.println("Incorrect: " + (covered.size() - max) + " / " + covered.size());
                            }

                            // Create a new rule:
                            if (candidate != null) {
                                h.addRule(rule.pattern, candidate, ((float) (max + 1)) / ((float) covered.size() + 2), max);
                            } else {
                                System.err.println("PropertiesCN2: no candidate solution!");
                            }

                            if (DEBUG >= 1) {
                                System.out.println("PropertiesCN2.newRule: " + rule.pattern.toStringNOOS(dm) + "\n" + candidate.toStringNOOS(dm) + "\n" + ((float) (max + 1)) / ((float) covered.size() + 2));
                            }
                        }
                    }
                }
            } while (!(rule == null || remainingExamples.isEmpty()));

            return h;
        }
    }

    private List<Selector> computeAllSelectors(List<Example> examples, Sort descriptionSort, FTKBase dm) throws FeatureTermException, Exception {
        List<Selector> selectors = new LinkedList<Selector>();
        List<FeatureTerm> properties = new LinkedList<FeatureTerm>();

        if (DEBUG>=1) System.out.println("PropertiexCN2: computing selectors from " + examples.size() + " examples.");

        for (Example e : examples) {
            List<FeatureTerm> properties_tmp;
            if (DEBUG>=1) System.out.println("PropertiexCN2: computing selectors... " + examples.indexOf(e) + "/" + examples.size());
            properties_tmp = Disintegration.disintegrate(e.description, dm, e.description.getSort().getOntology(), true, fast);

            for (FeatureTerm property : properties_tmp) {
                boolean duplicate = false;
                for (FeatureTerm p : properties) {
                    if (property.equivalents(p)) {
                        duplicate = true;
                        break;
                    }
                }
                if (!duplicate) {
                    properties.add(property);
                }
            }
        }
        for (FeatureTerm property : properties) {
            selectors.add(new Selector((FeatureTerm) property, examples));
        }

        if (DEBUG>=1) System.out.println("PropertiexCN2: " + selectors.size() + " selectors found.");

        return selectors;
    }

    private Rule findBestRule(List<Example> remainingExamples, List<Selector> selectors, Ontology o, FTKBase dm, Sort descriptionSort, List<FeatureTerm> differentSolutions) throws FeatureTermException {

        List<Rule> star = new LinkedList<Rule>();
        star.add(new Rule(o.getSort("any").createFeatureTerm(), null, remainingExamples, null, differentSolutions));
        Rule bestRule = null;

        if (DEBUG >= 1) {
            System.out.println("PropertiesCN2.findBestRule: " + selectors.size() + " selectors...");
        }

        while (!star.isEmpty()) {

            // Refine Rules:
            List<Rule> newStar = new LinkedList<Rule>();
            for (Rule rule : star) {
                for (Selector selector : selectors) {
                    if (!rule.selectors.contains(selector)) {
                        Rule newRule = new Rule(rule, selector, differentSolutions, o, dm);
                        if (newRule.pattern != null && newRule.examplesCovered.size() > 0) {
                            newStar.add(newRule);
                        }
                    }
                }
            }

            if (DEBUG >= 1) {
                System.out.println("PropertiesCN2.findBestRule: " + newStar.size() + " new rules...");
            }

            // Check for a new best rule:
            for (Rule rule : newStar) {
//				float s = ruleStatisticallySignificant(rule,remainingExamples,dp,sp,differentSolutions);
//				if (s>1.0f) {
                if (bestRule == null || rule.heuristic < bestRule.heuristic ||
                    (rule.heuristic == bestRule.heuristic && rule.examplesCovered.size()>bestRule.examplesCovered.size())) {
                    bestRule = rule;
                }
//				}
            }

            if (bestRule.coversSingleClass()) return bestRule;

            
            // Keep only the best rules:
            {
                boolean keepSorting = true;
                int l = newStar.size();

                while (keepSorting) {
                    keepSorting = false;
                    for (int i = 0; i < l - 1; i++) {
                        Rule r1 = newStar.get(i);
                        Rule r2 = newStar.get(i + 1);

                        if (r1.heuristic < r2.heuristic ||
                            (r1.heuristic == r2.heuristic && r1.examplesCovered.size()>r2.examplesCovered.size())) {
                            keepSorting = true;
                            newStar.set(i, r2);
                            newStar.set(i + 1, r1);
                        }
                    }
                }
                while (newStar.size() > BEAM_WIDTH) {
                    newStar.remove(0);
                }

                if (DEBUG >= 1) {
                    int i = 1;
                    for(Rule r:newStar) {
                        System.out.println("Best rule " + i + " heuristic: " + r.heuristic);
                        System.out.println("Distribution " + i + ": " + r.distributionToString());
                        System.out.println("Best rule " + i + ": " + r.pattern.toStringNOOS(dm));
                        i++;
                    }
                }

            }

            star = newStar;
        }

        return bestRule;
    }

    private static float ruleHeuristicSubsumedOnly(FeatureTerm rule, Collection<Example> examplesSubsumedOnly, List<FeatureTerm> solutions) throws FeatureTermException {
        float f[] = null;
        float covered = 0;
        int i;

        f = new float[solutions.size()];

        for (i = 0; i < solutions.size(); i++) {
            f[i] = 0;
        }

        for (Example ex : examplesSubsumedOnly) {
            int index = solutions.indexOf(ex.solution);
            if (index == -1) {
                System.err.println("PropertiesCN2.ruleHeuristic: Weird solution!");
            }
            f[index]++;
            covered++;
        }

        if (covered>0) {
            for (i = 0; i < solutions.size(); i++) {
                f[i] /= covered;
            }
        }

        float result = 0;

        for (i = 0; i < solutions.size(); i++) {
            if (f[i] != 0) {
                result += f[i] * Math.log(f[i]);
            }
        }
        result = -result;

        if (DEBUG >= 2) {
            System.out.println("covered = " + covered);
            System.out.print("f = [ ");
            for (i = 0; i < f.length; i++) {
                System.out.print(f[i] + " ");
            }
            System.out.println("]");
            System.out.println("h = " + result);
        }

        return result;
    }

    public static class Selector {

        public FeatureTerm pattern;
        public HashSet<Example> examplesCovered;

        public Selector(FeatureTerm p, Collection<Example> examples) throws FeatureTermException {
            pattern = p;
            examplesCovered = new HashSet<Example>();
            for (Example example : examples) {
                if (pattern.subsumes(example.description)) {
                    examplesCovered.add(example);
                }
            }
        }
    }

    static class Rule {

        List<Selector> selectors = new LinkedList<Selector>();
        public FeatureTerm pattern;
        public HashSet<Example> examplesCovered;
        float heuristic;

        public Rule(FeatureTerm p, List<Selector> s, Collection<Example> examples1, Collection<Example> examples2, List<FeatureTerm> differentSolutions) throws FeatureTermException {
            pattern = p;
            if (s != null) {
                selectors.addAll(s);
            }
            examplesCovered = new HashSet<Example>();
            for (Example example : examples1) {
                if (examples2 == null || examples2.contains(example)) {
                    examplesCovered.add(example);
                }
            }
            heuristic = ruleHeuristicSubsumedOnly(pattern, examplesCovered, differentSolutions);
        }

        public Rule(Rule r, Selector s, List<FeatureTerm> differentSolutions, Ontology o, FTKBase dm) throws FeatureTermException {
            pattern = FTUnification.simpleUnification(r.pattern, s.pattern, dm);
            if (pattern == null) return;
            selectors.addAll(r.selectors);
            selectors.add(s);
            examplesCovered = new HashSet<Example>();
            for (Example example : r.examplesCovered) {
                if (s.examplesCovered.contains(example)) {
                    if (pattern.subsumes(example.description)) {
                        examplesCovered.add(example);
                    }
                }
            }
            heuristic = ruleHeuristicSubsumedOnly(pattern, examplesCovered, differentSolutions);
        }

        public String distributionToString() {
            HashMap<FeatureTerm,Integer> solutionCount = new HashMap<FeatureTerm,Integer>();

            for(Example e:examplesCovered) {
                Integer count = solutionCount.get(e.solution);
                if (count==null) {
                    solutionCount.put(e.solution,1);
                } else {
                    solutionCount.put(e.solution,count+1);
                }
            }

            String result = "[ ";

            for(FeatureTerm s:solutionCount.keySet()) {
                result += solutionCount.get(s) + " ";
            }

            return result + "]";
        }

        public boolean coversSingleClass() {
            HashSet<FeatureTerm> solutions = new HashSet<FeatureTerm>();

            for(Example e:examplesCovered) {
                if (!solutions.contains(e.solution)) {
                    solutions.add(e.solution);
                }
            }

            if (solutions.size()==1) return true;
            return false;
        }
    }

    static class Example {

        public FeatureTerm example, description, solution;

        public Example(FeatureTerm e, Path dp, Path sp) throws FeatureTermException {
            example = e;
            description = e.readPath(dp);
            solution = e.readPath(sp);
        }
    }
}
