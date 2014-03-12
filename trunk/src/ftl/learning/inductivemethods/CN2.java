/**
 * Copyright (c) 2013, Santiago Ontañón All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution. Neither the name of
 * the IIIA-CSIC nor the names of its contributors may be used to endorse or promote products derived from this software
 * without specific prior written permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
  
 package ftl.learning.inductivemethods;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import ftl.base.core.FTKBase;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Ontology;
import ftl.base.core.Path;
import ftl.base.core.Sort;
import ftl.base.core.Symbol;
import ftl.base.core.TermFeatureTerm;
import ftl.base.utils.FeatureTermException;
import ftl.learning.core.Hypothesis;
import ftl.learning.core.InductiveLearner;
import ftl.learning.core.RuleHypothesis;

// TODO: Auto-generated Javadoc
/**
 * The Class CN2.
 */
public class CN2 extends InductiveLearner {

	/** The DEBUG. */
	static int DEBUG = 0;

	/** The BEA m_ width. */
	int BEAM_WIDTH = 3;

	/**
	 * Instantiates a new c n2.
	 * 
	 * @param bw
	 *            the bw
	 */
	public CN2(int bw) {
		BEAM_WIDTH = bw;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csic.iiia.ftl.learning.core.InductiveLearner#generateHypothesis(java.util.List,
	 * csic.iiia.ftl.base.core.Path, csic.iiia.ftl.base.core.Path, csic.iiia.ftl.base.core.Ontology,
	 * csic.iiia.ftl.base.core.FTKBase)
	 */
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
				if (DEBUG >= 2) {
					System.out.println("CN2: " + remainingExamples.size() + " remaining examples...");
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
								System.err.println("CN2: no candidate solution!");
							}

							if (DEBUG >= 1) {
								System.out.println("CN2.newRule: " + rule.pattern.toStringNOOS(dm) + "\n" + candidate.toStringNOOS(dm) + "\n"
										+ ((float) (max + 1)) / ((float) covered.size() + 2));
							}
						}
					}
				}
			} while (!(rule == null || remainingExamples.isEmpty()));

			return h;
		}
	}

	/**
	 * Compute all selectors.
	 * 
	 * @param examples
	 *            the examples
	 * @param descriptionSort
	 *            the description sort
	 * @param dm
	 *            the dm
	 * @return the list
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	private static List<Selector> computeAllSelectors(List<Example> examples, Sort descriptionSort, FTKBase dm) throws FeatureTermException {
		List<Selector> selectors = new LinkedList<Selector>();
		List<Symbol> features = descriptionSort.getFeatures();
		for (Example e : examples) {
			for (Symbol f : features) {
				FeatureTerm v = e.description.featureValue(f);
				if (v != null) {
					TermFeatureTerm pattern = (TermFeatureTerm) descriptionSort.createFeatureTerm();
					pattern.defineFeatureValue(f, v);

					{
						boolean alreadyPresent = false;
						for (Selector s : selectors) {
							if (s.pattern.equivalents(pattern)) {
								alreadyPresent = true;
								break;
							}
						}
						if (!alreadyPresent) {
							selectors.add(new Selector(pattern, examples));
						}
					}
				}
			}
		}

		// System.out.println(selectors.size() + " selectors found:");
		// for(Selector s:selectors) {
		// System.out.println(s.pattern.toStringNOOS(dm));
		// }

		return selectors;
	}

	/**
	 * Find best rule.
	 * 
	 * @param remainingExamples
	 *            the remaining examples
	 * @param selectors
	 *            the selectors
	 * @param o
	 *            the o
	 * @param dm
	 *            the dm
	 * @param descriptionSort
	 *            the description sort
	 * @param differentSolutions
	 *            the different solutions
	 * @return the rule
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	private Rule findBestRule(List<Example> remainingExamples, List<Selector> selectors, Ontology o, FTKBase dm, Sort descriptionSort,
			List<FeatureTerm> differentSolutions) throws FeatureTermException {

		List<Rule> star = new LinkedList<Rule>();
		star.add(new Rule((TermFeatureTerm) descriptionSort.createFeatureTerm(), remainingExamples, null, differentSolutions));
		Rule bestRule = null;
		float bestRuleHeuristic = 0;

		// selectors = computeAllSelectors(remainingExamples,descriptionSort,dp,dm);

		if (DEBUG >= 2) {
			System.out.println("CN2.findBestRule: " + selectors.size() + " selectors...");
		}

		while (!star.isEmpty()) {

			// Refine Rules:
			List<Rule> newStar = new LinkedList<Rule>();
			for (Rule rule : star) {
				for (Selector selector : selectors) {
					Symbol f = selector.pattern.getFeatureNames().iterator().next();
					if (rule.pattern.featureValue(f) == null) {
						Rule newRule = new Rule(null, rule.examplesCovered, selector.examplesCovered, differentSolutions);
						if (newRule.examplesCovered.size() > 0) {
							TermFeatureTerm newRulePattern = (TermFeatureTerm) rule.pattern.clone(dm, o);
							newRulePattern.defineFeatureValue(f, selector.pattern.featureValue(f));
							newRule.pattern = newRulePattern;
							newStar.add(newRule);

							// DEBUG:
							// for(FeatureTerm ex:remainingExamples) {
							// FeatureTerm d = ex.readPath(dp);
							// if (newRule.examplesCovered.contains(ex)) {
							// if (!newRule.pattern.subsumes(d)) System.err.println("inconsistency1");
							// } else {
							// if (newRule.pattern.subsumes(d)) System.err.println("inconsistency2");
							// }
							// }
						}
					}
				}
			}

			// if (DEBUG>=2)
			// System.out.println("CN2.findBestRule: " + newStar.size() + " new rules...");

			// Check for a new best rule:
			for (Rule rule : newStar) {
				// float s = ruleStatisticallySignificant(rule,remainingExamples,dp,sp,differentSolutions);
				// if (s>1.0f) {
				if (bestRule == null || rule.heuristic < bestRuleHeuristic) {
					bestRule = rule;
					bestRuleHeuristic = rule.heuristic;
				}
				// }
			}

			// if (DEBUG>=1)
			// System.out.println("Best rule heuristic: " + bestRuleHeuristic);
			// System.out.println("Best rule: " + bestRule.pattern.toStringNOOS(dm));

			// if (DEBUG>=2) System.out.println("CN2.findBestRule: " + newStar.size() + " new rules...");

			// Keep only the best rules:
			{
				boolean keepSorting = true;
				int l = newStar.size();

				while (keepSorting) {
					keepSorting = false;
					for (int i = 0; i < l - 1; i++) {
						Rule r1 = newStar.get(i);
						Rule r2 = newStar.get(i + 1);

						if (r1.heuristic > r2.heuristic) {
							keepSorting = true;
							newStar.set(i, r2);
							newStar.set(i + 1, r1);
						}
					}
				}
				while (newStar.size() > BEAM_WIDTH) {
					newStar.remove(0);
				}
			}

			star = newStar;

			// System.out.println("star:");
			// for(Rule r:star) {
			// System.out.println("rule covers: " + r.examplesCovered.size());
			// }
		}

		// DEBUG:
		// if (bestRuleHeuristic>0) {
		// System.out.println("CN2 could not find a good rule for classifying these " + bestRule.examplesCovered.size()
		// + " examples:");
		// for(FeatureTerm ex:bestRule.examplesCovered) {
		// System.out.println(ex.toStringNOOS(dm));
		// }
		// System.out.println("This is thebest I could find:");
		// System.out.println(bestRule.pattern.toStringNOOS(dm));
		// System.exit(0);
		// }

		return bestRule;
	}

	/**
	 * Rule heuristic subsumed only.
	 * 
	 * @param rule
	 *            the rule
	 * @param examplesSubsumedOnly
	 *            the examples subsumed only
	 * @param solutions
	 *            the solutions
	 * @return the float
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	private static float ruleHeuristicSubsumedOnly(FeatureTerm rule, Collection<Example> examplesSubsumedOnly, List<FeatureTerm> solutions)
			throws FeatureTermException {
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
				System.err.println("CN2.ruleHeuristic: Weird solution!");
			}
			f[index]++;
			covered++;
		}

		for (i = 0; i < solutions.size(); i++) {
			f[i] /= covered;
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

	/*
	 * private static float ruleStatisticallySignificant(FeatureTerm rule,List<FeatureTerm> examples, Path dp,Path
	 * sp,List<FeatureTerm> solutions) throws FeatureTermException { float e[] = null, f[] = null; float covered = 0;
	 * int i;
	 * 
	 * e = new float[solutions.size()]; f = new float[solutions.size()];
	 * 
	 * for(i = 0;i<solutions.size();i++) { e[i]=0; f[i]=0; }
	 * 
	 * for(FeatureTerm ex:examples) { FeatureTerm d = ex.readPath(dp); FeatureTerm s = ex.readPath(sp); int index =
	 * solutions.indexOf(s); if (index==-1) System.err.println("CN2.ruleStatisticallySignificant: Weird solution!");
	 * e[index]++; if (rule.subsumes(d)) { f[index]++; covered ++; } }
	 * 
	 * for(i = 0;i<solutions.size();i++) e[i]/=((float)examples.size())/covered;
	 * 
	 * float result = 0;
	 * 
	 * for(i = 0;i<solutions.size();i++) { if (f[i]!=0) { result += f[i] * Math.log(f[i]/e[i]); } } result *= 2;
	 * 
	 * if (DEBUG>=2) { System.out.println("|examples,covered| = " + examples.size() + " " + covered);
	 * System.out.print("f = [ "); for(i = 0;i<f.length;i++) System.out.print(f[i] + " "); System.out.println("]");
	 * System.out.print("e = [ "); for(i = 0;i<e.length;i++) System.out.print(e[i] + " "); System.out.println("]");
	 * System.out.println("s = " + result); }
	 * 
	 * return result; }
	 */
	/**
	 * The Class Selector.
	 */
	public static class Selector {

		/** The pattern. */
		public TermFeatureTerm pattern;

		/** The examples covered. */
		public HashSet<Example> examplesCovered;

		/**
		 * Instantiates a new selector.
		 * 
		 * @param p
		 *            the p
		 * @param examples
		 *            the examples
		 * @throws FeatureTermException
		 *             the feature term exception
		 */
		public Selector(TermFeatureTerm p, Collection<Example> examples) throws FeatureTermException {
			pattern = p;
			examplesCovered = new HashSet<Example>();
			for (Example example : examples) {
				if (pattern.subsumes(example.description)) {
					examplesCovered.add(example);
				}
			}
		}
	}

	/**
	 * The Class Rule.
	 */
	static class Rule {

		/** The pattern. */
		public TermFeatureTerm pattern;

		/** The examples covered. */
		public HashSet<Example> examplesCovered;

		/** The heuristic. */
		float heuristic;

		/**
		 * Instantiates a new rule.
		 * 
		 * @param p
		 *            the p
		 * @param examples1
		 *            the examples1
		 * @param examples2
		 *            the examples2
		 * @param differentSolutions
		 *            the different solutions
		 * @throws FeatureTermException
		 *             the feature term exception
		 */
		public Rule(TermFeatureTerm p, Collection<Example> examples1, Collection<Example> examples2, List<FeatureTerm> differentSolutions)
				throws FeatureTermException {
			pattern = p;
			examplesCovered = new HashSet<Example>();
			for (Example example : examples1) {
				if (examples2 == null || examples2.contains(example)) {
					examplesCovered.add(example);
				}
			}
			heuristic = ruleHeuristicSubsumedOnly(pattern, examplesCovered, differentSolutions);
		}
	}

	/**
	 * The Class Example.
	 */
	static class Example {

		/** The solution. */
		public FeatureTerm example, description, solution;

		/**
		 * Instantiates a new example.
		 * 
		 * @param e
		 *            the e
		 * @param dp
		 *            the dp
		 * @param sp
		 *            the sp
		 * @throws FeatureTermException
		 *             the feature term exception
		 */
		public Example(FeatureTerm e, Path dp, Path sp) throws FeatureTermException {
			example = e;
			description = e.readPath(dp);
			solution = e.readPath(sp);
		}
	}
}
