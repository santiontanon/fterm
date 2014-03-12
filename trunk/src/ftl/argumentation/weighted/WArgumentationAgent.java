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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import ftl.base.core.FTKBase;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Ontology;
import ftl.base.core.Path;
import ftl.base.utils.FeatureTermException;
import ftl.learning.core.RuleHypothesis;

// TODO: Auto-generated Javadoc
/**
 * The Class WArgumentationAgent.
 * 
 * @author santi
 */
public class WArgumentationAgent {

	/** The m_name. */
	public String m_name;

	/** The m_examples. */
	public List<FeatureTerm> m_examples;

	/** The m_hypothesis. */
	public List<WeightedArgument> m_hypothesis;

	/** The m_sp. */
	public Path m_dp, m_sp;

	/** The budget cache. */
	public HashMap<WeightedArgument, Float> budgetCache = new HashMap<WeightedArgument, Float>();

	/** The m_af. */
	public WArgumentationFramework m_af;

	/**
	 * Instantiates a new w argumentation agent.
	 * 
	 * @param name
	 *            the name
	 * @param examples
	 *            the examples
	 * @param solution
	 *            the solution
	 * @param dp
	 *            the dp
	 * @param sp
	 *            the sp
	 * @param o
	 *            the o
	 * @param dm
	 *            the dm
	 * @param language
	 *            the language
	 * @throws Exception
	 *             the exception
	 */
	public WArgumentationAgent(String name, Collection<FeatureTerm> examples, FeatureTerm solution, Path dp, Path sp, Ontology o, FTKBase dm, int language)
			throws Exception {
		m_name = name;
		m_dp = dp;
		m_sp = sp;
		m_examples = new LinkedList<FeatureTerm>();
		m_examples.addAll(examples);
		m_af = new WArgumentationFramework();
		m_hypothesis = WABUI.learnConcept(m_examples, solution, m_af, dp, sp, o, dm, m_name, language);
		for (WeightedArgument arg : m_hypothesis) {
			examine(arg);
			m_af.addArgument(arg);
		}
	}

	// This method assumes that agents do not change their sets of examples:
	/**
	 * Examine.
	 * 
	 * @param arg
	 *            the arg
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public void examine(WeightedArgument arg) throws FeatureTermException {
		if (!arg.examined(m_name)) {
			arg.addExaminationRecord(new ArgumentExaminationRecord(arg, m_name, m_examples, m_dp, m_sp));
		}
	}

	/**
	 * Cache inconsistency budget.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public void cacheInconsistencyBudget() throws FeatureTermException {
		for (WeightedArgument arg : m_af.getArguments())
			budgetCache.put(arg, m_af.inconsistencyBudget(arg));
	}

	/**
	 * Generate hypothesis.
	 * 
	 * @param solution
	 *            the solution
	 * @param dp
	 *            the dp
	 * @param sp
	 *            the sp
	 * @return the rule hypothesis
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public RuleHypothesis generateHypothesis(FeatureTerm solution, Path dp, Path sp) throws FeatureTermException {
		RuleHypothesis h = new RuleHypothesis();

		System.out.println("generateHypothesis: start");

		List<FeatureTerm> uncovered = new LinkedList<FeatureTerm>();
		for (FeatureTerm e : m_examples) {
			FeatureTerm s = e.readPath(sp);
			if (s.equivalents(solution)) {
				uncovered.add(e.readPath(dp));
			}
		}
		while (!uncovered.isEmpty()) {
			FeatureTerm d = uncovered.remove(0);
			float best_b = 0;
			WeightedArgument best = null;
			for (WeightedArgument arg : m_hypothesis) {
				if (arg.m_a.m_rule.pattern.subsumes(d)) {
					Float b = budgetCache.get(arg);
					if (b == null) {
						b = m_af.inconsistencyBudget(arg);
						budgetCache.put(arg, b);
					}
					if (best == null || b < best_b) {
						best = arg;
						best_b = b;
					}
				}
			}
			if (best != null) {
				System.out.println("Argument " + best.getID() + " added to hypothesis");
				h.addRule(best.m_a.m_rule);
				List<FeatureTerm> toDelete = new LinkedList<FeatureTerm>();
				for (FeatureTerm d2 : uncovered) {
					if (best.m_a.m_rule.pattern.subsumes(d2))
						toDelete.add(d2);
				}
				uncovered.removeAll(toDelete);
			}
		}
		return h;
	}
}
