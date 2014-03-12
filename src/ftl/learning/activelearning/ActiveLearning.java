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
  
 package ftl.learning.activelearning;

import java.util.LinkedList;
import java.util.List;

import ftl.base.core.FTKBase;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Ontology;
import ftl.base.core.Path;
import ftl.base.utils.FeatureTermException;
import ftl.base.utils.Pair;
import ftl.learning.core.Hypothesis;
import ftl.learning.core.InductiveLearner;

// TODO: Auto-generated Javadoc
/**
 * The Class ActiveLearning.
 */
public abstract class ActiveLearning {

	/** The m_learner. */
	InductiveLearner m_learner = null;

	/**
	 * Active learning experiment.
	 * 
	 * @param a_training
	 *            the a_training
	 * @param test
	 *            the test
	 * @param differentSolutions
	 *            the different solutions
	 * @param l
	 *            the l
	 * @param amount
	 *            the amount
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
	public List<Double> activeLearningExperiment(List<FeatureTerm> a_training, List<FeatureTerm> test, List<FeatureTerm> differentSolutions,
			InductiveLearner l, int amount, Path dp, Path sp, Ontology o, FTKBase dm) throws Exception {
		List<FeatureTerm> training = new LinkedList<FeatureTerm>();
		List<FeatureTerm> available = new LinkedList<FeatureTerm>();
		available.addAll(a_training);
		List<Double> results = new LinkedList<Double>();

		m_learner = l;

		System.out.println("activeLearningExperiment for " + this + " with method " + m_learner.getClass().getSimpleName() + " started");

		while (!available.isEmpty()) {
			training = selectTrainingExamples(training, available, differentSolutions, dp, sp, o, dm, amount);
			available.removeAll(training);

			Hypothesis h = m_learner.generateHypothesis(training, dp, sp, o, dm);
			float acc = h.evaluate(test, dm, sp, dp, false);
			// System.out.println(training.size() + " / " + available.size() + " examples accuracy is " + acc);
			results.add(new Double(acc));
		}

		return results;
	}

	/**
	 * Select training examples.
	 * 
	 * @param initialSet
	 *            the initial set
	 * @param additional
	 *            the additional
	 * @param differentSolutions
	 *            the different solutions
	 * @param dp
	 *            the dp
	 * @param sp
	 *            the sp
	 * @param o
	 *            the o
	 * @param dm
	 *            the dm
	 * @param amount
	 *            the amount
	 * @return the list
	 * @throws Exception
	 *             the exception
	 */
	public List<FeatureTerm> selectTrainingExamples(List<FeatureTerm> initialSet, List<FeatureTerm> additional, List<FeatureTerm> differentSolutions, Path dp,
			Path sp, Ontology o, FTKBase dm, int amount) throws Exception {
		List<FeatureTerm> selected = new LinkedList<FeatureTerm>();
		selected.addAll(initialSet);

		List<Pair<FeatureTerm, Double>> utilities = examplesUtility(initialSet, additional, differentSolutions, dp, sp, o, dm);

		// Sort:
		{
			int l = utilities.size();
			boolean change = true;
			while (change) {
				change = false;
				for (int i = 0; i < l - 1; i++) {
					Pair<FeatureTerm, Double> u1 = utilities.get(i);
					Pair<FeatureTerm, Double> u2 = utilities.get(i + 1);

					if (u1.m_b < u2.m_b) {
						FeatureTerm tmp1 = u1.m_a;
						Double tmp2 = u1.m_b;
						u1.m_a = u2.m_a;
						u1.m_b = u2.m_b;
						u2.m_a = tmp1;
						u2.m_b = tmp2;
						change = true;
					}
				}
			}
		}

		while (utilities.size() > amount)
			utilities.remove(utilities.size() - 1);
		for (Pair<FeatureTerm, Double> u : utilities) {
			selected.add(u.m_a);
		}

		return selected;
	}

	/**
	 * Examples utility.
	 * 
	 * @param initialSet
	 *            the initial set
	 * @param examples
	 *            the examples
	 * @param differentSolutions
	 *            the different solutions
	 * @param dp
	 *            the dp
	 * @param sp
	 *            the sp
	 * @param o
	 *            the o
	 * @param dm
	 *            the dm
	 * @return the list
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws Exception
	 *             the exception
	 */
	public abstract List<Pair<FeatureTerm, Double>> examplesUtility(List<FeatureTerm> initialSet, List<FeatureTerm> examples,
			List<FeatureTerm> differentSolutions, Path dp, Path sp, Ontology o, FTKBase dm) throws FeatureTermException, Exception;
}
