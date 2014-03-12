/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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

import ftl.base.core.FTAntiunification;
import ftl.base.core.FTKBase;
import ftl.base.core.FTRefinement;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Ontology;
import ftl.base.core.Path;
import ftl.base.core.Sort;
import ftl.base.utils.FeatureTermException;
import ftl.learning.core.Hypothesis;
import ftl.learning.core.InductiveLearner;
import ftl.learning.core.RuleHypothesis;

// TODO: Auto-generated Javadoc
/**
 * The Class RefinementCN2.
 * 
 * @author santi
 */
public class RefinementCN2 extends InductiveLearner {

	/** The DEBUG. */
	public static int DEBUG = 1;

	/** The BEA m_ width. */
	int BEAM_WIDTH = 3;

	/**
	 * Instantiates a new refinement cn2.
	 * 
	 * @param bw
	 *            the bw
	 */
	public RefinementCN2(int bw) {
		BEAM_WIDTH = bw;
	}

	public RefinementCN2() {
		// TODO Auto-generated constructor stub
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
			List<CN2Example> remainingExamples = new LinkedList<CN2Example>();
			for (FeatureTerm e : examples) {
				remainingExamples.add(new CN2Example(e, dp, sp));
			}
			CN2Rule rule = null;
			FeatureTerm startingTerm = o.getSort("any").createFeatureTerm();

			do {
				if (DEBUG >= 1) {
					System.out.println("RefinementCN2: " + remainingExamples.size() + " remaining examples...");
				}
				rule = findBestRule(remainingExamples, startingTerm, o, dm, descriptionSort, differentSolutions);
				if (rule != null) {
					{
						List<CN2Example> covered = new LinkedList<CN2Example>();
						HashMap<FeatureTerm, List<CN2Example>> classifiedCovered = new HashMap<FeatureTerm, List<CN2Example>>();

						if (DEBUG >= 1)
							System.out.println("RefinementCN2.newRule: figuring out its solution... (1/3)");

						// Find the examples covered by the rule:
						for (CN2Example e : rule.examplesCovered) {
							covered.add(e);

							List<CN2Example> l = classifiedCovered.get(e.solution);
							if (l == null) {
								l = new LinkedList<CN2Example>();
								classifiedCovered.put(e.solution, l);
							}
							l.add(e);
						}

						if (DEBUG >= 1)
							System.out.println("RefinementCN2.newRule: figuring out its solution... (2/3)");

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
								FeatureTerm generalPattern = rule.pattern;
								// generalize rule:
								{
									List<FeatureTerm> positive = new LinkedList<FeatureTerm>();
									List<FeatureTerm> negative = new LinkedList<FeatureTerm>();
									for (CN2Example e : rule.examplesCovered) {
										if (e.solution.equivalents(candidate)) {
											positive.add(e.description);
										}
									}
									for (CN2Example e : remainingExamples) {
										if (!e.solution.equivalents(candidate)) {
											negative.add(e.description);
										}
									}
									generalPattern = Hypothesis.generalizePattern(rule.pattern, positive, negative, o, dm);
								}

								if (DEBUG >= 1)
									System.out.println("RefinementCN2.newRule: figuring out its solution... (3/3)");

								h.addRule(generalPattern, candidate, ((float) (max + 1)) / ((float) covered.size() + 2), max);
								if (DEBUG >= 1)
									System.out.println("RefinementCN2.newRule: " + generalPattern.toStringNOOS(dm) + "\n" + candidate.toStringNOOS(dm) + "\n"
											+ ((float) (max + 1)) / ((float) covered.size() + 2));
							} else {
								System.err.println("RefinementCN2: no candidate solution!");
							}
						}
					}
				}
			} while (!(rule == null || remainingExamples.isEmpty()));

			return h;
		}
	}

	/**
	 * Find best rule.
	 * 
	 * @param remainingExamples
	 *            the remaining examples
	 * @param au
	 *            the au
	 * @param o
	 *            the o
	 * @param dm
	 *            the dm
	 * @param descriptionSort
	 *            the description sort
	 * @param differentSolutions
	 *            the different solutions
	 * @return the c n2 rule
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	private CN2Rule findBestRule(List<CN2Example> remainingExamples, FeatureTerm au, Ontology o, FTKBase dm, Sort descriptionSort,
			List<FeatureTerm> differentSolutions) throws FeatureTermException {

		List<CN2Rule> star = new LinkedList<CN2Rule>();
		CN2Rule initial = new CN2Rule(au, null, remainingExamples, null, differentSolutions, o, dm);
		star.add(initial);
		CN2Rule bestRule = initial;

		while (!star.isEmpty()) {

			// Refine Rules:
			List<CN2Rule> newStar = new LinkedList<CN2Rule>();
			List<FeatureTerm> alreadyGenerated = new LinkedList<FeatureTerm>();
			for (CN2Rule rule : star) {
				// generate new rules:
				List<FeatureTerm> problems = new LinkedList<FeatureTerm>();
				for (CN2Example e : rule.examplesCovered)
					problems.add(e.description);
				List<FeatureTerm> l = FTRefinement.getSpecializationsSubsumingSome(rule.pattern, dm, o, FTRefinement.ALL_REFINEMENTS, problems);

				for (FeatureTerm pattern : l) {
					boolean found = false;
					for (FeatureTerm p2 : alreadyGenerated) {
						if (pattern.equivalents(p2)) {
							found = true;
							break;
						}
					}
					if (!found) {
						alreadyGenerated.add(pattern);
						CN2Rule newRule = new CN2Rule(rule, pattern, differentSolutions, o, dm);
						if (newRule.examplesCovered.size() > 0) {
							newStar.add(newRule);
						}
					}
				}
			}

			if (DEBUG >= 1)
				System.out.println("RefinementCN2.findBestRule: " + newStar.size() + " new rules...");

			// Check for a new best rule:
			for (CN2Rule rule : newStar) {
				if (bestRule == null || rule.heuristic < bestRule.heuristic
						|| (rule.heuristic == bestRule.heuristic && rule.examplesCovered.size() > bestRule.examplesCovered.size())) {
					bestRule = rule;
				}
			}

			if (DEBUG >= 1)
				System.out.println("RefinementCN2.findBestRule: chacking for termination...");

			if (bestRule.coversSingleClass())
				return bestRule;

			if (DEBUG >= 1) {
				System.out.println("Best rule heuristic: " + bestRule.heuristic);
				System.out.println("Distribution: " + bestRule.distributionToString());
				System.out.println("Best rule: " + bestRule.pattern.toStringNOOS(dm));
			}

			// Keep only the best rules:
			{
				boolean keepSorting = true;
				int l = newStar.size();

				while (keepSorting) {
					keepSorting = false;
					for (int i = 0; i < l - 1; i++) {
						CN2Rule r1 = newStar.get(i);
						CN2Rule r2 = newStar.get(i + 1);

						if (r1.heuristic < r2.heuristic || (r1.heuristic == r2.heuristic && r1.examplesCovered.size() > r2.examplesCovered.size())) {
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
		}

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
	private static float ruleHeuristicSubsumedOnly(FeatureTerm rule, Collection<CN2Example> examplesSubsumedOnly, List<FeatureTerm> solutions)
			throws FeatureTermException {
		float f[] = null;
		float covered = 0;
		int i;

		f = new float[solutions.size()];

		for (i = 0; i < solutions.size(); i++) {
			f[i] = 0;
		}

		for (CN2Example ex : examplesSubsumedOnly) {
			int index = solutions.indexOf(ex.solution);
			if (index == -1) {
				System.err.println("PropertiesCN2.ruleHeuristic: Weird solution!");
			}
			f[index]++;
			covered++;
		}

		if (covered > 0) {
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

	/**
	 * The Class CN2Rule.
	 */
	static class CN2Rule {

		/** The pattern. */
		public FeatureTerm pattern;

		/** The examples covered. */
		public HashSet<CN2Example> examplesCovered;

		/** The heuristic. */
		float heuristic;

		/**
		 * Instantiates a new c n2 rule.
		 * 
		 * @param p
		 *            the p
		 * @param s
		 *            the s
		 * @param examples1
		 *            the examples1
		 * @param examples2
		 *            the examples2
		 * @param differentSolutions
		 *            the different solutions
		 * @param o
		 *            the o
		 * @param dm
		 *            the dm
		 * @throws FeatureTermException
		 *             the feature term exception
		 */
		public CN2Rule(FeatureTerm p, FeatureTerm s, Collection<CN2Example> examples1, Collection<CN2Example> examples2, List<FeatureTerm> differentSolutions,
				Ontology o, FTKBase dm) throws FeatureTermException {
			if (p != null) {
				pattern = p;
			} else {
				pattern = o.getSort("any").createFeatureTerm();
			}
			examplesCovered = new HashSet<CN2Example>();
			for (CN2Example example : examples1) {
				if (examples2 == null || examples2.contains(example)) {
					examplesCovered.add(example);
				}
			}

			// specialize the pattern as much as possible before continuing:
			{
				List<FeatureTerm> l = new LinkedList<FeatureTerm>();
				List<FeatureTerm> l2 = new LinkedList<FeatureTerm>();
				for (CN2Example e : examplesCovered)
					l.add(e.description);
				l2.add(pattern);
				List<FeatureTerm> aul = FTAntiunification.antiunification(l, FTRefinement.ALL_REFINEMENTS, l2, o, dm, true, FTAntiunification.VERSION_FAST);
				pattern = aul.get(0);
			}

			heuristic = ruleHeuristicSubsumedOnly(pattern, examplesCovered, differentSolutions);
		}

		/**
		 * Instantiates a new c n2 rule.
		 * 
		 * @param r
		 *            the r
		 * @param p
		 *            the p
		 * @param differentSolutions
		 *            the different solutions
		 * @param o
		 *            the o
		 * @param dm
		 *            the dm
		 * @throws FeatureTermException
		 *             the feature term exception
		 */
		public CN2Rule(CN2Rule r, FeatureTerm p, List<FeatureTerm> differentSolutions, Ontology o, FTKBase dm) throws FeatureTermException {
			if (p != null) {
				pattern = p;
			} else {
				pattern = o.getSort("any").createFeatureTerm();
			}
			examplesCovered = new HashSet<CN2Example>();
			for (CN2Example example : r.examplesCovered) {
				if (p.subsumes(example.description)) {
					examplesCovered.add(example);
				}
			}

			// specialize the pattern as much as possible before continuing:
			{
				List<FeatureTerm> l = new LinkedList<FeatureTerm>();
				List<FeatureTerm> l2 = new LinkedList<FeatureTerm>();
				for (CN2Example e : examplesCovered)
					l.add(e.description);
				l2.add(pattern);
				List<FeatureTerm> aul = FTAntiunification.antiunification(l, FTRefinement.ALL_REFINEMENTS, l2, o, dm, true, FTAntiunification.VERSION_FAST);
				pattern = aul.get(0);
			}

			heuristic = ruleHeuristicSubsumedOnly(pattern, examplesCovered, differentSolutions);
		}

		/**
		 * Distribution to string.
		 * 
		 * @return the string
		 */
		public String distributionToString() {
			HashMap<FeatureTerm, Integer> solutionCount = new HashMap<FeatureTerm, Integer>();

			for (CN2Example e : examplesCovered) {
				Integer count = solutionCount.get(e.solution);
				if (count == null) {
					solutionCount.put(e.solution, 1);
				} else {
					solutionCount.put(e.solution, count + 1);
				}
			}

			String result = "[ ";

			for (FeatureTerm s : solutionCount.keySet()) {
				result += solutionCount.get(s) + " ";
			}

			return result + "]";
		}

		/**
		 * Covers single class.
		 * 
		 * @return true, if successful
		 */
		public boolean coversSingleClass() {
			HashSet<FeatureTerm> solutions = new HashSet<FeatureTerm>();

			for (CN2Example e : examplesCovered) {
				if (!solutions.contains(e.solution)) {
					solutions.add(e.solution);
				}
			}

			if (solutions.size() == 1)
				return true;
			return false;
		}
	}

	/**
	 * The Class CN2Example.
	 */
	static class CN2Example {

		/** The solution. */
		public FeatureTerm example, description, solution;

		/**
		 * Instantiates a new c n2 example.
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
		public CN2Example(FeatureTerm e, Path dp, Path sp) throws FeatureTermException {
			example = e;
			description = e.readPath(dp);
			solution = e.readPath(sp);
		}
	}
}
