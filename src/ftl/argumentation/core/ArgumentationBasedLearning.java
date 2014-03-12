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
  
 package ftl.argumentation.core;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import ftl.base.core.FTAntiunification;
import ftl.base.core.FTKBase;
import ftl.base.core.FTRefinement;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Ontology;
import ftl.base.core.Path;
import ftl.base.utils.FeatureTermException;
import ftl.learning.core.Hypothesis;
import ftl.learning.core.Rule;
import ftl.learning.core.RuleHypothesis;

// TODO: Auto-generated Javadoc
/**
 * The Class ArgumentationBasedLearning.
 * 
 * @author santi
 */
public class ArgumentationBasedLearning {

	/** The DEBUG. */
	public static int DEBUG = 0;

	/**
	 * Uncovered examples.
	 * 
	 * @param examples
	 *            the examples
	 * @param h
	 *            the h
	 * @param arguments
	 *            the arguments
	 * @param dp
	 *            the dp
	 * @param sp
	 *            the sp
	 * @return the list
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws Exception
	 *             the exception
	 */
	public List<FeatureTerm> uncoveredExamples(Collection<FeatureTerm> examples, RuleHypothesis h, List<Argument> arguments, Path dp, Path sp)
			throws FeatureTermException, Exception {
		List<FeatureTerm> uncovered = new LinkedList<FeatureTerm>();

		for (FeatureTerm e : examples) {
			boolean covered = false;
			FeatureTerm d = e.readPath(dp);
			FeatureTerm s = e.readPath(sp);
			if (h != null) {
				for (Rule r : h.getRules()) {
					if (r.pattern.subsumes(d) && r.solution.equivalents(s)) {
						covered = true;
						break;
					}
				}
			}
			if (!covered && arguments != null) {
				for (Argument a : arguments) {
					if (a.m_rule.pattern.subsumes(d) && a.m_rule.solution.equivalents(s)) {
						covered = true;
						break;
					}
				}
			}
			if (!covered) {
				uncovered.add(e);
			}
		}

		return uncovered;
	}

	/**
	 * Generate hypothesis.
	 * 
	 * @param arguments
	 *            the arguments
	 * @param examples
	 *            the examples
	 * @param dp
	 *            the dp
	 * @param sp
	 *            the sp
	 * @return the rule hypothesis
	 * @throws Exception
	 *             the exception
	 */
	public RuleHypothesis generateHypothesis(List<Argument> arguments, Collection<FeatureTerm> examples, Path dp, Path sp) throws Exception {
		RuleHypothesis h = new RuleHypothesis(false);

		h.setDefaultSolution(Hypothesis.mostCommonSolution(examples, sp));
		for (Argument a : arguments) {
			if (a.m_type == Argument.ARGUMENT_RULE) {
				h.addRule(a.m_rule);
			}
		}

		return h;
	}

	/**
	 * Cover examples top down.
	 * 
	 * @param examples
	 *            the examples
	 * @param acceptedArguments
	 *            the accepted arguments
	 * @param aa
	 *            the aa
	 * @param dp
	 *            the dp
	 * @param sp
	 *            the sp
	 * @param o
	 *            the o
	 * @param dm
	 *            the dm
	 * @return the list
	 * @throws Exception
	 *             the exception
	 */
	public List<Argument> coverExamplesTopDown(List<FeatureTerm> examples, Collection<Argument> acceptedArguments, ArgumentAcceptability aa, Path dp, Path sp,
			Ontology o, FTKBase dm) throws Exception {
		List<Argument> rules = new LinkedList<Argument>();
		List<FeatureTerm> solutions = Hypothesis.differentSolutions(examples, sp);

		for (FeatureTerm solution : solutions) {
			List<Argument> rulesTmp = coverExamplesOneSolutionTopDown(solution, examples, acceptedArguments, aa, dp, sp, o, dm);

			rules.addAll(rulesTmp);
		}

		return rules;
	}

	/**
	 * Cover examples one solution top down.
	 * 
	 * @param solution
	 *            the solution
	 * @param examples
	 *            the examples
	 * @param acceptedArguments
	 *            the accepted arguments
	 * @param aa
	 *            the aa
	 * @param dp
	 *            the dp
	 * @param sp
	 *            the sp
	 * @param o
	 *            the o
	 * @param dm
	 *            the dm
	 * @return the list
	 * @throws Exception
	 *             the exception
	 */
	public List<Argument> coverExamplesOneSolutionTopDown(FeatureTerm solution, List<FeatureTerm> examples, Collection<Argument> acceptedArguments,
			ArgumentAcceptability aa, Path dp, Path sp, Ontology o, FTKBase dm) throws Exception {
		List<Argument> rules = new LinkedList<Argument>();

		List<FeatureTerm> all = new LinkedList<FeatureTerm>();
		List<FeatureTerm> positives = new LinkedList<FeatureTerm>();
		List<FeatureTerm> negatives = new LinkedList<FeatureTerm>();
		List<FeatureTerm> positivesCovered = new LinkedList<FeatureTerm>();
		List<FeatureTerm> negativesCovered = new LinkedList<FeatureTerm>();

		for (FeatureTerm e : examples) {
			FeatureTerm d = e.readPath(dp);
			FeatureTerm s = e.readPath(sp);

			if (solution.equivalents(s)) {
				positives.add(d);
			} else {
				negatives.add(d);
			}
			all.add(d);
		}

		FeatureTerm pattern = null;

		do {
			FeatureTerm learnedPattern = null;

			if (pattern == null) {
				pattern = FTAntiunification.simpleAntiunification(all, o, dm);
				positivesCovered.clear();
				positivesCovered.addAll(positives);
				negativesCovered.clear();
				negativesCovered.addAll(negatives);
			}

			// Check if we are done:
			if (!negativesCovered.isEmpty()) {
				List<FeatureTerm> refinements = FTRefinement.getSpecializationsSubsumingSome(pattern, dm, o, FTRefinement.ALL_REFINEMENTS, positives);

				if (!refinements.isEmpty()) {
					// Rank refinements (making sure they agree with accepted arguments):
					int nrefinements = refinements.size();
					float[] heuristics = new float[nrefinements];
					int[] coverage = new int[nrefinements];
					int i = 0;

					/* Compute heuristic: */
					for (FeatureTerm refinement : refinements) {
						float before_p = 0, before_n = 0;
						float after_p = 0, after_n = 0;
						float before_i, after_i;
						double LOG2E = Math.log(2.0);

						for (FeatureTerm f : positives) {
							if (pattern.subsumes(f)) {
								before_p++;
							}
							if (refinement.subsumes(f)) {
								after_p++;
							}
						} /* if */
						for (FeatureTerm f : negatives) {
							if (pattern.subsumes(f)) {
								before_n++;
							}
							if (refinement.subsumes(f)) {
								after_n++;
							}
						} /* if */

						before_i = (float) (-(Math.log(before_p / (before_p + before_n)) / LOG2E));
						if (after_p + after_n == 0) {
							after_i = 0;
						} else {
							after_i = (float) (-(Math.log(after_p / (after_p + after_n)) / LOG2E));
						}

						heuristics[i] = (after_p) * (before_i - after_i);
						coverage[i] = (int) after_p;
					}

					float maximum = 0;
					int selected = -1;

					for (i = 0; i < nrefinements; i++) {
						if (coverage[i] > 0 && (selected == -1 || heuristics[i] > maximum)) {

							maximum = heuristics[i];
							selected = i;
						}
					}

					if (selected == -1) {
						learnedPattern = pattern;
					} else {
						pattern = refinements.get(selected);
					}
				} else {
					learnedPattern = pattern;
				}
			} else {
				learnedPattern = pattern;
			}

			// New pattern learned
			if (learnedPattern != null) {
				List<FeatureTerm> toDelete = new LinkedList<FeatureTerm>();
				float p = 0;
				float n = 0;
				for (FeatureTerm e : positives) {
					if (learnedPattern.subsumes(e)) {
						p++;
						toDelete.add(e);
					}
				}
				for (FeatureTerm e : negatives) {
					if (pattern.subsumes(e)) {
						n++;
					}
				}
				rules.add(new Argument(new Rule(learnedPattern, solution, (p + 1) / (p + n + 2), (int) p)));
				pattern = null;
				all.removeAll(toDelete);
				positives.removeAll(toDelete);

			}
		} while (!positives.isEmpty());

		// Filter all the rules that are not acceptble:
		List<Argument> acceptedRules = new LinkedList<Argument>();

		if (DEBUG >= 1) {
			System.out.println("coverExamplesOneSolutionTopDown: found " + rules.size() + " arguments.");
		}
		for (Argument a : rules) {
			if (aa.accepted(a)) {
				boolean conflict = false;
				for (Argument a2 : acceptedArguments) {
					if (Attack.attacksAcceptedP(a2, a, aa, o, dm)) {
						conflict = true;
						if (DEBUG >= 1) {
							System.out.println("rule in conflict with a previously accepted rule...");
						}
						break;
					}
				}
				if (!conflict) {
					acceptedRules.add(a);
				}
			} else {
				if (DEBUG >= 1) {
					System.out.println("not acceptable rules...");
				}
			}
		}
		if (DEBUG >= 1) {
			System.out.println("coverExamplesOneSolutionTopDown: found " + acceptedRules.size() + " arguments after filtering.");
		}

		return acceptedRules;
	}

	/**
	 * Cover examples bottom up.
	 * 
	 * @param examples
	 *            the examples
	 * @param acceptedArguments
	 *            the accepted arguments
	 * @param aa
	 *            the aa
	 * @param dp
	 *            the dp
	 * @param sp
	 *            the sp
	 * @param o
	 *            the o
	 * @param dm
	 *            the dm
	 * @return the list
	 * @throws Exception
	 *             the exception
	 */
	public List<Argument> coverExamplesBottomUp(List<FeatureTerm> examples, Collection<Argument> acceptedArguments, ArgumentAcceptability aa, Path dp, Path sp,
			Ontology o, FTKBase dm) throws Exception {
		List<Argument> rules = new LinkedList<Argument>();
		List<FeatureTerm> remainingSeeds = new LinkedList<FeatureTerm>();

		remainingSeeds.addAll(examples);

		while (remainingSeeds.size() > 0) {
			FeatureTerm seed = remainingSeeds.get(0);
			Argument a = coverExampleBottomUp(examples, seed, acceptedArguments, aa, dp, sp, o, dm);

			remainingSeeds.remove(seed);

			if (a != null) {
				// remove all the covered seeds:
				List<FeatureTerm> toDelete = new LinkedList<FeatureTerm>();

				for (FeatureTerm e : remainingSeeds) {
					FeatureTerm d = e.readPath(dp);
					FeatureTerm s = e.readPath(sp);

					if (a.m_rule.pattern.subsumes(d) && a.m_rule.solution.equivalents(s)) {
						toDelete.add(e);
					}
				}
				remainingSeeds.removeAll(toDelete);

				rules.add(a);
			}
		}

		// Filter all the rules that are not acceptble:
		List<Argument> acceptedRules = new LinkedList<Argument>();

		if (DEBUG >= 1) {
			System.out.println("coverExamplesBottomUp: found " + rules.size() + " arguments.");
		}
		for (Argument a : rules) {
			if (aa.accepted(a)) {
				acceptedRules.add(a);
			} else {
				if (DEBUG >= 1) {
					System.out.println("not acceptable rule...");
				}
			}
		}
		if (DEBUG >= 1) {
			System.out.println("coverExamplesBottomUp: found " + acceptedRules.size() + " arguments after filtering.");
		}

		return acceptedRules;
	}

	/**
	 * Cover example bottom up.
	 * 
	 * @param examples
	 *            the examples
	 * @param example
	 *            the example
	 * @param acceptedArguments
	 *            the accepted arguments
	 * @param aa
	 *            the aa
	 * @param dp
	 *            the dp
	 * @param sp
	 *            the sp
	 * @param o
	 *            the o
	 * @param dm
	 *            the dm
	 * @return the argument
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public Argument coverExampleBottomUp(List<FeatureTerm> examples, FeatureTerm example, Collection<Argument> acceptedArguments, ArgumentAcceptability aa,
			Path dp, Path sp, Ontology o, FTKBase dm) throws FeatureTermException {
		FeatureTerm startingPoint_d = example.readPath(dp).clone(dm, o);
		FeatureTerm startingPoint_s = example.readPath(sp);

		if (startingPoint_d.getName() != null) {
			startingPoint_d.setName(null);
		}

		List<FeatureTerm> examplesToAvoid = new LinkedList<FeatureTerm>();

		for (FeatureTerm e : examples) {
			if (!startingPoint_s.equivalents(e.readPath(sp))) {
				examplesToAvoid.add(e.readPath(dp));
			}
		}

		// Generalize the starting point making sure that:
		// c1: the generalization still subsumes starting point (just for debugging purposes)
		// c2: none of the examples to avoid is covered
		// c3: there is no unification with any of already accepted arguments for other classes
		// c4: it has to be locally valid

		{
			List<FeatureTerm> l = null;
			boolean end, c1, c2, c3, c4;
			FeatureTerm pattern = null;

			do {
				end = true;
				if (pattern == null) {
					l = FTRefinement.getGeneralizations(startingPoint_d, dm, o);
				} else {
					l = FTRefinement.getGeneralizations(pattern, dm, o);
				}

				for (FeatureTerm f2 : l) {
					c1 = true;
					c2 = true;
					c3 = true;
					if (!f2.subsumes(startingPoint_d)) {
						c1 = false;
					}
					if (c1) {
						for (FeatureTerm d : examplesToAvoid) {
							if (f2.subsumes(d)) {
								c2 = false;
								break;
							}
						}
					}
					if (c1 && c2) {
						if (acceptedArguments != null) {
							for (Argument a : acceptedArguments) {
								Argument tmpArg = new Argument(new Rule(f2, startingPoint_s, 0.0f, 0));
								if (Attack.attacksAcceptedP(a, tmpArg, aa, o, dm)) {
									c3 = false;
									break;
								}
							}
						}
					}

					if (c1 && c2 && c3) {

						pattern = f2;
						end = false;
						break;
					}
				}
			} while (!end);

			if (pattern != null) {
				float p = 0;
				float n = 0;
				for (FeatureTerm e : examples) {
					FeatureTerm d = e.readPath(dp);
					if (pattern.subsumes(d)) {
						if (startingPoint_s.equivalents(e.readPath(sp))) {
							p++;
						} else {
							n++;
						}
					}
				}

				Argument tmp = new Argument(new Rule(pattern, startingPoint_s, (p + 1) / (p + n + 2), (int) p));
				if (DEBUG >= 1) {
					System.out.println("coverExampleBottomUp found: " + tmp.toStringNOOS(dm));
				}
				return tmp;
			}
		}

		if (DEBUG >= 1) {
			System.out.println("coverExampleBottomUp no valid argument found...");
		}
		return null;
	}

	/**
	 * Cover uncovered examples.
	 * 
	 * @param examples
	 *            the examples
	 * @param solution
	 *            the solution
	 * @param h
	 *            the h
	 * @param acceptedArguments
	 *            the accepted arguments
	 * @param aa
	 *            the aa
	 * @param dp
	 *            the dp
	 * @param sp
	 *            the sp
	 * @param o
	 *            the o
	 * @param dm
	 *            the dm
	 * @return the rule hypothesis
	 * @throws Exception
	 *             the exception
	 */
	public RuleHypothesis coverUncoveredExamples(List<FeatureTerm> examples, FeatureTerm solution, RuleHypothesis h, List<Argument> acceptedArguments,
			ArgumentAcceptability aa, Path dp, Path sp, Ontology o, FTKBase dm) throws Exception {
		List<FeatureTerm> uncovered = uncoveredExamples(examples, h, acceptedArguments, dp, sp);
		List<Argument> newArguments = coverExamplesBottomUp(uncovered, acceptedArguments, aa, dp, sp, o, dm);
		System.out.println("coverUncoveredExamples with " + acceptedArguments.size() + " accepted arguments...");
		System.out.println("Uncovered examples " + uncovered.size() + " covered by " + newArguments.size() + " new rules.");
		for (Rule r : h.getRules())
			newArguments.add(new Argument(r));
		return generateHypothesis(newArguments, examples, dp, sp);
	}
}
