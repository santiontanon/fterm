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

import ftl.base.core.FTKBase;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Ontology;
import ftl.base.core.Path;
import ftl.base.utils.FeatureTermException;
import ftl.learning.core.Rule;
import ftl.learning.core.RuleHypothesis;

// TODO: Auto-generated Javadoc
/**
 * The Class WAMAIL.
 * 
 * @author santi
 */
public class WAMAIL {

	/** The DEBUG. */
	public static int DEBUG = 1;

	/** The Constant INDIVIDUAL_CORRECTED. */
	public static final int INDIVIDUAL_CORRECTED = 0; // each agent creates a hypothesis that covers its own examples

	/** The Constant INDIVIDUAL_EXPANDED. */
	public static final int INDIVIDUAL_EXPANDED = 1; // each agent creates a hypothesis that covers its own examples,
														// and then some additional to cover the examples of the other

	/** The Constant UNIFIED. */
	public static final int UNIFIED = 2; // union of the two individual-corrected hypotheses

	// Statistics:
	/** The max_af_size. */
	public static int max_af_size = 0;

	/** The max_n_rounds. */
	public static int max_n_rounds = 0;

	/** The max_af_depth. */
	public static int max_af_depth = 0;

	/** The last_af_size. */
	public static int last_af_size = 0;

	/** The last_n_rounds. */
	public static int last_n_rounds = 0;

	/** The last_af_depth. */
	public static int last_af_depth = 0;

	/**
	 * Clear statistics.
	 */
	public static void clearStatistics() {
		max_af_size = 0;
		max_n_rounds = 0;
		max_af_depth = 0;

		last_af_size = 0;
		last_n_rounds = 0;
		last_af_depth = 0;
	}

	/**
	 * WAMAIL.
	 * 
	 * @param trainingSets
	 *            the training sets
	 * @param solution
	 *            the solution
	 * @param o
	 *            the o
	 * @param dm
	 *            the dm
	 * @param dp
	 *            the dp
	 * @param sp
	 *            the sp
	 * @param mode
	 *            the mode
	 * @param language
	 *            the language
	 * @return the list
	 * @throws Exception
	 *             the exception
	 */
	public static List<RuleHypothesis> WAMAIL(List<List<FeatureTerm>> trainingSets, FeatureTerm solution, Ontology o, FTKBase dm, Path dp, Path sp, int mode,
			int language) throws Exception {
		List<WArgumentationAgent> agents = WAMAIL(trainingSets, solution, o, dm, dp, sp, language);
		return getAgentsHypotheses(agents, solution, dp, sp, mode);
	}

	/**
	 * WAMAIL.
	 * 
	 * @param trainingSets
	 *            the training sets
	 * @param solution
	 *            the solution
	 * @param o
	 *            the o
	 * @param dm
	 *            the dm
	 * @param dp
	 *            the dp
	 * @param sp
	 *            the sp
	 * @param language
	 *            the language
	 * @return the list
	 * @throws Exception
	 *             the exception
	 */
	public static List<WArgumentationAgent> WAMAIL(List<List<FeatureTerm>> trainingSets, FeatureTerm solution, Ontology o, FTKBase dm, Path dp, Path sp,
			int language) throws Exception {
		// This table keeps track of which arguments were generated to attack which others:
		HashMap<WeightedArgument, WeightedArgument> generatedToAttack = new HashMap<WeightedArgument, WeightedArgument>();

		// STEP 1: Create the agents and their initial hypotheses:
		if (DEBUG >= 1)
			System.out.println("WAMAIL: creating agents... (" + trainingSets.size() + ")");
		List<WArgumentationAgent> agents = new LinkedList<WArgumentationAgent>();
		int i = 1;
		for (List<FeatureTerm> ts : trainingSets) {
			WArgumentationAgent a = new WArgumentationAgent("Ag" + i, ts, solution, dp, sp, o, dm, language);
			agents.add(a);

			System.out.println("Initial hypothesis of agent Ag" + i + ":");
			for (WeightedArgument arg : a.m_hypothesis) {
				System.out.print(arg.getID() + " ");
			}
			System.out.println("");

			i++;
		}

		// STEP 2: create the initial argumentation framework:
		if (DEBUG >= 1)
			System.out.println("WAMAIL: creating global argumentation framework...");
		WArgumentationFramework global_af = new WArgumentationFramework();
		for (WArgumentationAgent a : agents) {
			for (WeightedArgument arg : a.m_af.getArguments()) {
				for (WArgumentationAgent a2 : agents)
					a2.examine(arg);
				global_af.addArgument(arg);
			}
		}
		for (WArgumentationAgent a : agents)
			a.cacheInconsistencyBudget();

		// STEP 2: Token passing mechanism:
		int tokeni = 0;
		int remainingRounds = agents.size();
		int cycle = 0;
		do {
			if (DEBUG >= 1)
				System.out.println("WAMAIL: ---- cycle " + cycle + " starting ----");
			WArgumentationAgent token = agents.get(tokeni);
			remainingRounds--;

			// Agent's turn:
			// 1) COMMUNICATION: Check for any new arguments in the global_af
			List<WeightedArgument> newArguments = new LinkedList<WeightedArgument>();
			for (WeightedArgument arg : global_af.getArguments())
				if (!token.m_af.contains(arg)) {
					token.examine(arg);
					newArguments.add(arg);
				}
			if (DEBUG >= 1)
				System.out.println("WAMAIL:" + newArguments.size() + " new arguments.");

			// 2) COMMUNICATION: Incorporate those arguments into the local af
			for (WeightedArgument arg : newArguments)
				token.m_af.addArgument(arg);

			// 3) BELIEF REVISION: Try to attack all of the new arguments:
			if (DEBUG >= 1)
				System.out.println("WAMAIL: Belief revision... (attack all new argumetnts)");
			for (WeightedArgument arg : newArguments) {
				List<WeightedArgument> attacks = WABUI.attack(arg, global_af, token.m_examples, dp, sp, o, dm, token.m_name, language);
				if (DEBUG >= 1)
					System.out.println("WAMAIL: " + attacks.size() + " attacks found.");
				for (WeightedArgument a : attacks) {
					if (DEBUG >= 2)
						System.out.println(a.toStringNOOS(dm));
					generatedToAttack.put(a, arg);
					token.examine(a);
					token.m_af.addArgument(a);
				}
			}

			// 4) BELIEF REVISION: If the budget of any of the arguments is increased, use ABUI to find a better
			// alternative
			if (DEBUG >= 1)
				System.out.println("WAMAIL: Belief revision... (check for increased budgets)");
			HashMap<WeightedArgument, Float> newBudgets = new HashMap<WeightedArgument, Float>();
			List<WeightedArgument> toFindReplacements = new LinkedList<WeightedArgument>();
			if (!newArguments.isEmpty()) {
				for (WeightedArgument arg : token.m_af.getArguments(token.m_name)) {
					Float previous = token.budgetCache.get(arg);
					float budget = token.m_af.inconsistencyBudget(arg);
					if (previous != null) {
						if (budget > previous) {
							if (DEBUG >= 1)
								System.out.println("WAMAIL: agent " + token.m_name + " got the budget of argument " + arg.getID() + " increased (" + previous
										+ "->" + budget + "), looking for a replacement...");
							toFindReplacements.add(arg);
						}
					}
					newBudgets.put(arg, budget);
				}
			}
			token.budgetCache.putAll(newBudgets);
			if (DEBUG >= 1)
				System.out.println("WAMAIL: Belief revision, " + toFindReplacements.size() + " arguments to find replacements for...");
			for (WeightedArgument arg : toFindReplacements) {
				// Find a replacement:
				List<WeightedArgument> replacements = null;
				WeightedArgument tmp = generatedToAttack.get(arg);
				if (tmp == null) {
					replacements = WABUI.replacement(arg, null, global_af, token.budgetCache.get(arg), token.m_examples, dp, sp, o, dm, token.m_name, language);
				} else {
					replacements = WABUI.replacement(arg, tmp.m_a.m_rule.pattern, global_af, token.budgetCache.get(arg), token.m_examples, dp, sp, o, dm,
							token.m_name, language);
				}
				if (DEBUG >= 1) {
					System.out.print("WAMAIL: " + replacements.size() + " replacements found: ");
					for (WeightedArgument arg2 : replacements)
						System.out.print(arg2.getID() + " ");
					System.out.println("");
				}
				for (WeightedArgument arg2 : replacements) {
					token.examine(arg2);
					if (arg2 == token.m_af.addArgument(arg2) && // This means, if "arg2" is not equivalent to any old
																// argument
							token.m_hypothesis.contains(arg)) {
						token.m_hypothesis.add(arg2);
						global_af.addArgument(arg2);
					}
				}
			}

			// 5) ATTACK: For each of the arguments of the other agents for which the local budget is higher than in
			// global, send one of the possible attacks
			if (DEBUG >= 1)
				System.out.println("WAMAIL: Attack...");
			List<WeightedArgument> attacks_to_send = new LinkedList<WeightedArgument>();
			for (WeightedArgument arg : global_af.getArguments()) {
				if (!arg.getAgent().equals(token.m_name)) {
					float gbudget = global_af.inconsistencyBudget(arg);
					float lbudget = token.m_af.inconsistencyBudget(arg);

					if (lbudget > gbudget) {
						// Look for the strongest acceptable attack not present in the global af:
						if (DEBUG >= 1)
							System.out.println("WAMAIL: local budget or argument " + arg.getID() + " for " + token.m_name + " is higher than global: "
									+ lbudget + "/" + gbudget);
						float best_s = 0;
						WeightedArgument best = null;
						for (WeightedArgument attack : token.m_af.getAttacksOf(arg)) {
							if (!global_af.contains(attack)) {
								float s = attack.attackStrength(arg);
								if (best == null || s > best_s) {
									best = attack;
									best_s = s;
								}
							}
						}
						if (best != null) {
							if (!attacks_to_send.contains(best))
								attacks_to_send.add(best);
						}
					}
				}
			}
			for (WeightedArgument attack : attacks_to_send) {
				if (DEBUG >= 1)
					System.out.println("Sending Attack: " + attack.getID());
				global_af.addArgument(attack);
				remainingRounds = agents.size(); // new messages, protocol continuing...
			}

			tokeni = (tokeni + 1) % agents.size();
			cycle++;

			// remove this after it's been properly debugged:
			if (cycle > 100) {
				System.err.println("THERE MIGHT BE AN ERROR!!!!!!!!!!!!!!!!!! CYCLES IN WAMAIL REACHED 100!!!!!!!!!!!!!");
				break;
			}

		} while (remainingRounds > 0);

		/*
		 * for(WArgumentationAgent agent:agents) { JFrame frame = WArgumentationAgentVisualizer.newWindow(agent,
		 * 800,600, sp, dp, dm, new PropertiesDistance(agent.m_examples, dm, o, dp, false), false);
		 * frame.setVisible(true);
		 * 
		 * System.out.println("Framework of " + agent.m_name + ":"); System.out.println(agent.m_af.toString(dm)); }
		 */
		/*
		 * { JFrame frame = ArgumentationGraphVisualizer.newWindow("global", 800,600, global_af, dm);
		 * frame.setVisible(true);
		 * 
		 * System.out.println("Global framework:"); System.out.println(global_af); }
		 */

		// update statistics:
		last_n_rounds = cycle;
		last_af_size = global_af.m_arguments.size();
		last_af_depth = global_af.maxDepth();

		if (last_n_rounds > max_n_rounds)
			max_n_rounds = last_n_rounds;
		if (last_af_size > max_af_size)
			max_af_size = last_af_size;
		if (last_af_depth > max_af_depth)
			max_af_depth = last_af_depth;

		return agents;
	}

	/**
	 * Gets the agents hypotheses.
	 * 
	 * @param agents
	 *            the agents
	 * @param solution
	 *            the solution
	 * @param dp
	 *            the dp
	 * @param sp
	 *            the sp
	 * @param mode
	 *            the mode
	 * @return the agents hypotheses
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public static List<RuleHypothesis> getAgentsHypotheses(List<WArgumentationAgent> agents, FeatureTerm solution, Path dp, Path sp, int mode)
			throws FeatureTermException {
		List<RuleHypothesis> result = new LinkedList<RuleHypothesis>();
		switch (mode) {
		case INDIVIDUAL_CORRECTED:
			for (WArgumentationAgent a : agents) {
				result.add(a.generateHypothesis(solution, dp, sp));
			}
			break;
		case INDIVIDUAL_EXPANDED:
			for (WArgumentationAgent a : agents) {
				result.add(a.generateHypothesis(solution, dp, sp));
			}
			for (WArgumentationAgent a : agents) {
				for (WArgumentationAgent a2 : agents) {
					if (a != a2) {
						RuleHypothesis h = result.get(agents.indexOf(a));
						RuleHypothesis h2 = result.get(agents.indexOf(a2));
						for (FeatureTerm e : a2.m_examples) {
							if (h.coveredByAnyRule(e.readPath(dp)) == null) {
								Rule r = h2.coveredByAnyRule(e.readPath(dp));
								if (r != null) {
									h.addRule(r);
								}
							}
						}
					}
				}
			}
			break;
		case UNIFIED: {
			RuleHypothesis merged = new RuleHypothesis();
			for (WArgumentationAgent a : agents) {
				RuleHypothesis tmp = a.generateHypothesis(solution, dp, sp);
				for (Rule r : tmp.getRules()) {
					merged.addRule(r);
				}
			}
			for (int j = 0; j < agents.size(); j++)
				result.add(merged);
		}
			break;
		}

		return result;
	}
}
