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
  
 package ftl.argumentation.weighted;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import ftl.argumentation.core.Argument;
import ftl.base.core.FTAntiunification;
import ftl.base.core.FTKBase;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Ontology;
import ftl.base.core.Path;
import ftl.base.utils.FeatureTermException;
import ftl.learning.core.Rule;
import ftl.learning.core.RuleHypothesis;

// TODO: Auto-generated Javadoc
/**
 * The Class WABUI.
 * 
 * @author santi
 */
public class WABUI {

	/** The DEBUG. */
	public static int DEBUG = 0;

	/**
	 * Learn concept hypothesis.
	 * 
	 * @param examples
	 *            the examples
	 * @param solution
	 *            the solution
	 * @param knownArguments
	 *            the known arguments
	 * @param dp
	 *            the dp
	 * @param sp
	 *            the sp
	 * @param o
	 *            the o
	 * @param dm
	 *            the dm
	 * @param agentName
	 *            the agent name
	 * @param language
	 *            the language
	 * @return the rule hypothesis
	 * @throws Exception
	 *             the exception
	 */
	static public RuleHypothesis learnConceptHypothesis(List<FeatureTerm> examples, FeatureTerm solution, WArgumentationFramework knownArguments, Path dp,
			Path sp, Ontology o, FTKBase dm, String agentName, int language) throws Exception {
		List<WeightedArgument> args = learnConcept(examples, solution, knownArguments, dp, sp, o, dm, agentName, language);
		RuleHypothesis h = new RuleHypothesis();

		for (WeightedArgument a : args) {
			h.addRule(a.m_a.m_rule);
		}

		return h;
	}

	/**
	 * Learn concept.
	 * 
	 * @param examples
	 *            the examples
	 * @param solution
	 *            the solution
	 * @param knownArguments
	 *            the known arguments
	 * @param dp
	 *            the dp
	 * @param sp
	 *            the sp
	 * @param o
	 *            the o
	 * @param dm
	 *            the dm
	 * @param agentName
	 *            the agent name
	 * @param language
	 *            the language
	 * @return the list
	 * @throws Exception
	 *             the exception
	 */
	static public List<WeightedArgument> learnConcept(List<FeatureTerm> examples, FeatureTerm solution, WArgumentationFramework knownArguments, Path dp,
			Path sp, Ontology o, FTKBase dm, String agentName, int language) throws Exception {
		List<WeightedArgument> args = new LinkedList<WeightedArgument>();
		FeatureTerm any = o.getSort("any").createFeatureTerm();

		if (DEBUG >= 1)
			System.out.println("WABUI.learnConcept: starting...");

		List<FeatureTerm> positive = new LinkedList<FeatureTerm>();

		for (FeatureTerm e : examples) {
			if (e.readPath(sp).equivalents(solution)) {
				positive.add(e.readPath(dp));
			}
		}

		while (positive.size() > 0) {
			WeightedArgument a = null;
			// Here we give a very negative budget (-1) to indicate that there is no limit:
			a = ABUI2(positive, examples, solution, knownArguments, -1, any, o, dm, agentName, dp, sp, language);

			if (a != null) {
				args.add(a);

				List<FeatureTerm> toDelete = new LinkedList<FeatureTerm>();
				for (FeatureTerm d : positive) {
					if (a.m_a.m_rule.pattern.subsumes(d)) {
						toDelete.add(d);
					}
				}
				positive.removeAll(toDelete);
				if (DEBUG >= 1)
					System.out.println("WABUI.learnConcept: argument covers " + toDelete.size() + " examples: " + a.m_a.m_rule.reliability);
				if (DEBUG >= 2)
					System.out.println(a.m_a.toStringNOOS(dm));
			} else {
				break;
			}
		}

		if (DEBUG >= 1)
			System.out.println("WABUI.learnConcept: done, returning " + args.size() + " arguments.");

		return args;
	}

	/**
	 * Attack.
	 * 
	 * @param arg
	 *            the arg
	 * @param knownArguments
	 *            the known arguments
	 * @param examples
	 *            the examples
	 * @param dp
	 *            the dp
	 * @param sp
	 *            the sp
	 * @param o
	 *            the o
	 * @param dm
	 *            the dm
	 * @param agentName
	 *            the agent name
	 * @param language
	 *            the language
	 * @return the list
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws Exception
	 *             the exception
	 */
	static public List<WeightedArgument> attack(WeightedArgument arg, WArgumentationFramework knownArguments, List<FeatureTerm> examples, Path dp, Path sp,
			Ontology o, FTKBase dm, String agentName, int language) throws FeatureTermException, Exception {
		List<WeightedArgument> attacks = new LinkedList<WeightedArgument>();
		List<FeatureTerm> coveredExamples = new LinkedList<FeatureTerm>();
		FeatureTerm pattern = arg.m_a.m_rule.pattern;
		FeatureTerm solution = arg.m_a.m_rule.solution;

		if (DEBUG >= 1)
			System.out.println("WABUI.attack: starting...");

		HashMap<FeatureTerm, List<FeatureTerm>> differentClassExamples = new HashMap<FeatureTerm, List<FeatureTerm>>();

		for (FeatureTerm e : examples) {
			FeatureTerm s = e.readPath(sp);
			FeatureTerm d = e.readPath(dp);
			if (pattern.subsumes(d)) {
				coveredExamples.add(e);
				List<FeatureTerm> l = differentClassExamples.get(s);
				if (l == null) {
					l = new LinkedList<FeatureTerm>();
					differentClassExamples.put(s, l);
				}
				l.add(d);
			}
		}

		if (DEBUG >= 0) {
			System.out.println("WABUI.attack: starting with the following examples:");
			for (FeatureTerm s : differentClassExamples.keySet()) {
				System.out.println(s.toStringNOOS(dm) + ": " + differentClassExamples.get(s).size());
			}
		}

		for (FeatureTerm s : differentClassExamples.keySet()) {
			if (!s.equivalents(solution)) {
				List<FeatureTerm> positive = new LinkedList<FeatureTerm>();
				positive.addAll(differentClassExamples.get(s));
				while (positive.size() > 0) {
					WeightedArgument a = null;
					// Here we give a very negative budget (-1) to indicate that there is no limit:
					a = ABUI2(positive, coveredExamples, s, knownArguments, -1, pattern, o, dm, agentName, dp, sp, language);

					if (a != null) {
						attacks.add(a);

						List<FeatureTerm> toDelete = new LinkedList<FeatureTerm>();
						for (FeatureTerm d : positive) {
							if (a.m_a.m_rule.pattern.subsumes(d)) {
								toDelete.add(d);
							}
						}
						positive.removeAll(toDelete);
						if (DEBUG >= 1)
							System.out.println("WABUI.attack: argument covers " + toDelete.size() + " examples: " + a.m_a.m_rule.reliability);
						if (DEBUG >= 2)
							System.out.println(a.m_a.toStringNOOS(dm));
					} else {
						break;
					}
				}
			}
		}

		if (DEBUG >= 1) {
			System.out.println("WABUI.attack: done, returning " + attacks.size() + " arguments.");
		}

		return attacks;
	}

	/**
	 * Replacement.
	 * 
	 * @param arg
	 *            the arg
	 * @param pattern
	 *            the pattern
	 * @param knownArguments
	 *            the known arguments
	 * @param budgetToImprove
	 *            the budget to improve
	 * @param examples
	 *            the examples
	 * @param dp
	 *            the dp
	 * @param sp
	 *            the sp
	 * @param o
	 *            the o
	 * @param dm
	 *            the dm
	 * @param agentName
	 *            the agent name
	 * @param language
	 *            the language
	 * @return the list
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws Exception
	 *             the exception
	 */
	static public List<WeightedArgument> replacement(WeightedArgument arg, FeatureTerm pattern, WArgumentationFramework knownArguments, float budgetToImprove,
			List<FeatureTerm> examples, Path dp, Path sp, Ontology o, FTKBase dm, String agentName, int language) throws FeatureTermException, Exception {
		List<WeightedArgument> args = new LinkedList<WeightedArgument>();
		FeatureTerm solution = arg.m_a.m_rule.solution;
		if (pattern == null)
			pattern = o.getSort("any").createFeatureTerm();

		if (DEBUG >= 1)
			System.out.println("WABUI.replacement: starting...");

		List<FeatureTerm> positive = new LinkedList<FeatureTerm>();
		List<FeatureTerm> coveredExamples = new LinkedList<FeatureTerm>();

		for (FeatureTerm e : examples) {
			FeatureTerm d = e.readPath(dp);
			if (pattern.subsumes(d)) {
				coveredExamples.add(e);
				if (e.readPath(sp).equivalents(solution)) {
					if (arg.m_a.m_rule.pattern.subsumes(d)) {
						positive.add(e.readPath(dp));
					}
				}
			}
		}

		while (positive.size() > 0) {
			WeightedArgument a = null;
			if (DEBUG >= 1)
				System.out.println("WABUI.replacement: calling ABUI2 with " + positive.size() + " positive uncovered");
			a = ABUI2(positive, coveredExamples, solution, knownArguments, budgetToImprove, pattern, o, dm, agentName, dp, sp, language);

			if (a != null) {
				args.add(a);

				List<FeatureTerm> toDelete = new LinkedList<FeatureTerm>();
				for (FeatureTerm d : positive) {
					if (a.m_a.m_rule.pattern.subsumes(d)) {
						toDelete.add(d);
					}
				}
				positive.removeAll(toDelete);
				if (DEBUG >= 1)
					System.out.println("WABUI.replacement: argument covers " + toDelete.size() + " examples: " + a.m_a.m_rule.reliability);
				if (DEBUG >= 2)
					System.out.println(a.m_a.toStringNOOS(dm));
			} else {
				break;
			}
		}

		if (DEBUG >= 1)
			System.out.println("WABUI.replacement: done, returning " + args.size() + " arguments.");

		return args;
	}

	/**
	 * ABU i2.
	 * 
	 * @param uncoveredPositiveDescriptions
	 *            the uncovered positive descriptions
	 * @param examples
	 *            the examples
	 * @param solution
	 *            the solution
	 * @param knownArguments
	 *            the known arguments
	 * @param maxBudget
	 *            the max budget
	 * @param g
	 *            the g
	 * @param o
	 *            the o
	 * @param dm
	 *            the dm
	 * @param agentName
	 *            the agent name
	 * @param dp
	 *            the dp
	 * @param sp
	 *            the sp
	 * @param language
	 *            the language
	 * @return the weighted argument
	 * @throws Exception
	 *             the exception
	 */
	static public WeightedArgument ABUI2(List<FeatureTerm> uncoveredPositiveDescriptions, List<FeatureTerm> examples, FeatureTerm solution,
			WArgumentationFramework knownArguments, float maxBudget, FeatureTerm g, Ontology o, FTKBase dm, String agentName, Path dp, Path sp, int language)
			throws Exception {

		List<FeatureTerm> positiveDescriptions = new LinkedList<FeatureTerm>();
		List<FeatureTerm> negativeDescriptions = new LinkedList<FeatureTerm>();
		List<Argument> H = new LinkedList<Argument>(); // This list will contain the candidate arguments
		if (knownArguments == null)
			knownArguments = new WArgumentationFramework();

		for (FeatureTerm e : examples) {
			FeatureTerm s = e.readPath(sp);
			if (s.equivalents(solution)) {
				positiveDescriptions.add(e.readPath(dp));
			} else {
				negativeDescriptions.add(e.readPath(dp));
			}
		}

		if (DEBUG >= 1)
			System.out.println("WABUI: UPD: " + uncoveredPositiveDescriptions.size() + " P/N:" + positiveDescriptions.size() + "/"
					+ negativeDescriptions.size());

		for (FeatureTerm e : uncoveredPositiveDescriptions) {
			List<FeatureTerm> notYetCoveredPositiveDescriptions = new LinkedList<FeatureTerm>();
			notYetCoveredPositiveDescriptions.addAll(positiveDescriptions);

			if (DEBUG >= 1)
				System.out.println("WABUI: New seed...");
			if (g.subsumes(e)) {
				FeatureTerm c = e.clone(dm, o);
				if (c.getName() != null)
					c.setName(null);
				while (c != null) {
					// remove all the already covered:
					{
						List<FeatureTerm> toDelete = new LinkedList<FeatureTerm>();
						for (FeatureTerm d : notYetCoveredPositiveDescriptions)
							if (c.subsumes(d)) {
								toDelete.add(d);
							}
						notYetCoveredPositiveDescriptions.removeAll(toDelete);
					}

					Argument a = new Argument(new Rule(c, solution, 0.0f, 0), agentName);
					if (DEBUG >= 2)
						System.out.println(c.toStringNOOS(dm));

					a.m_rule.reliability = confidence(a.m_rule, positiveDescriptions, negativeDescriptions);
					H.add(a);

					// DEBUG:
					/*
					 * { WeightedArgument tmpArg = new WeightedArgument(a); WeightedArgument tmpArg2 =
					 * knownArguments.addArgument(tmpArg); if (tmpArg2!=tmpArg) {
					 * System.out.println("Repeated argument..."); } else { knownArguments.removeArgument(tmpArg); }
					 * 
					 * }
					 */

					// System.out.println("B(h) = " + a.m_rule.reliability);

					List<FeatureTerm> G = new LinkedList<FeatureTerm>();

					for (FeatureTerm d : notYetCoveredPositiveDescriptions) {
						// System.out.print("(");System.out.flush();
						FeatureTerm au = FTAntiunification.simpleAntiunification(c, d, o, dm, language);
						// System.out.print(")");System.out.flush();
						if (au != null)
							G.add(au);
					}
					List<FeatureTerm> G2 = new LinkedList<FeatureTerm>();

					if (DEBUG >= 2)
						System.out.println("WABUI: " + G.size() + " generalizations.");

					for (FeatureTerm h : G) {
						if (g.subsumes(h) && !h.subsumes(g)) {
							if (maxBudget >= 0) {
								// compute budget according to "knownArguments":
								WeightedArgument tmpArg = new WeightedArgument(new Argument(new Rule(h, solution), agentName));
								tmpArg.addExaminationRecord(new ArgumentExaminationRecord(tmpArg, agentName, examples, dp, sp));
								WeightedArgument tmpArg2 = knownArguments.addArgument(tmpArg);

								// estimate the examination of the other agents:
								if (tmpArg2 == tmpArg) {
									List<ArgumentExaminationRecord> estimatedExaminations = knownArguments.examinationEstimation(tmpArg);
									for (ArgumentExaminationRecord aer : estimatedExaminations) {
										if (!aer.m_agent_name.equals(agentName))
											tmpArg.addExaminationRecord(aer);
									}
								} else {
									// List<ArgumentExaminationRecord> estimatedExaminations =
									// knownArguments.examinationEstimation(tmpArg);
									// System.out.println("Estimated Examination records:");
									// for(ArgumentExaminationRecord aer:estimatedExaminations) System.out.println(aer);
								}

								float budget = knownArguments.inconsistencyBudget(tmpArg2);

								// if (budget>0) System.out.println("budget(h) = " + budget + " (max = " + maxBudget +
								// ")");

								knownArguments.removeArgument(tmpArg);

								if (budget < maxBudget)
									G2.add(h);
							} else {
								// when "maxBudget" is negative, there is no filter:
								G2.add(h);
							}
						}
					}

					if (DEBUG >= 2)
						System.out.println("ABUI2: " + G.size() + " -> " + G2.size() + " filtered generalizations.");

					c = null;
					{
						FeatureTerm best_c = null;
						float best_conf = 0.0f;
						float max_c = 0.0f;
						float min_c = 1.0f;

						for (FeatureTerm c2 : G2) {
							Argument a2 = new Argument(new Rule(c2, solution, 0.0f, 0), agentName);
							float conf = confidence(a2.m_rule, positiveDescriptions, negativeDescriptions);
							if (best_c == null || conf > best_conf) {
								best_c = c2;
								best_conf = conf;
							}

							if (conf > max_c)
								max_c = conf;
							if (conf < min_c)
								min_c = conf;
						}

						c = best_c;
						if (DEBUG >= 2)
							System.out.println("ABUI2: Generalization confidence: " + best_conf + " the range was [" + min_c + "-" + max_c + "] (with "
									+ G2.size() + ")");
					}
				}
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

			// System.out.println("|H! = " + H.size());

			if (best_a == null)
				return null;
			return new WeightedArgument(best_a);
		}
	}

	/**
	 * Confidence.
	 * 
	 * @param r
	 *            the r
	 * @param positive
	 *            the positive
	 * @param negative
	 *            the negative
	 * @return the float
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	static float confidence(Rule r, List<FeatureTerm> positive, List<FeatureTerm> negative) throws FeatureTermException {
		int np = 0;
		int nn = 0;

		for (FeatureTerm d : positive)
			if (r.pattern.subsumes(d))
				np++;
		for (FeatureTerm d : negative)
			if (r.pattern.subsumes(d))
				nn++;

		return (((float) np) + 1) / (np + nn + 2);
	}
}
