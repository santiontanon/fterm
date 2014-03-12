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
import java.util.LinkedList;
import java.util.List;

import ftl.base.core.FTAntiunification;
import ftl.base.core.FTKBase;
import ftl.base.core.FTRefinement;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Ontology;
import ftl.base.core.Path;
import ftl.learning.core.Hypothesis;
import ftl.learning.core.InductiveLearner;
import ftl.learning.core.RuleHypothesis;

// TODO: Auto-generated Javadoc
/**
 * The Class RefinementHYDRA.
 */
public class RefinementHYDRA extends InductiveLearner {

	/** The DEBUG. */
	int DEBUG = 0;

	/**
	 * Since FOIL is basically a concept learning method, we implement HYDRA. HYDRA is a relational learning algorithm
	 * based on FOIL that learns a different rule for each solution class. Then, it computes a reliability measure for
	 * each rule, that is use to resolve ties during problem solving. Reliability is computed using the Laplace
	 * estimator of the accuracy of a rule, i.e.: (p+1)/(p+n+2) If a problem is not covered by any rule, it is assigned
	 * the most frequent solution class.
	 */
	public Hypothesis generateHypothesis(List<FeatureTerm> examples, Path dp, Path sp, Ontology o, FTKBase dm) throws Exception {
		return learn(examples, dp, sp, o, dm, 0, true);
	}

	/**
	 * Learn.
	 * 
	 * @param cases
	 *            the cases
	 * @param description_path
	 *            the description_path
	 * @param solution_path
	 *            the solution_path
	 * @param o
	 *            the o
	 * @param domain_model
	 *            the domain_model
	 * @param heuristic
	 *            the heuristic
	 * @param generalize
	 *            the generalize
	 * @return the rule hypothesis
	 * @throws Exception
	 *             the exception
	 */
	private RuleHypothesis learn(Collection<FeatureTerm> cases, Path description_path, Path solution_path, Ontology o, FTKBase domain_model, int heuristic,
			boolean generalize) throws Exception {
		RuleHypothesis h = new RuleHypothesis(false);
		List<FeatureTerm> solutions = new LinkedList<FeatureTerm>();
		List<List<FeatureTerm>> descriptions = new LinkedList<List<FeatureTerm>>();

		/* Compute the different solutions: */
		{
			int pos;
			FeatureTerm s, d;

			for (FeatureTerm c : cases) {

				d = c.readPath(description_path);
				s = c.readPath(solution_path);

				pos = solutions.indexOf(s);
				if (pos == -1) {
					solutions.add(s);
					List<FeatureTerm> tmp = new LinkedList<FeatureTerm>();
					tmp.add(d);
					descriptions.add(tmp);
				} else {
					descriptions.get(pos).add(d);
				} /* if */
			} /* while */
		}

		/* Compute the default solution: */
		{
			int max = -1;
			FeatureTerm s;
			List<FeatureTerm> tmp;

			h.setM_default_solution(null);

			for (int i = 0; i < solutions.size(); i++) {
				s = solutions.get(i);
				tmp = descriptions.get(i);

				if (max == -1 || tmp.size() > max) {
					max = tmp.size();
					h.setM_default_solution(s);
				} /* if */
			} /* while */
		}

		for (FeatureTerm solution : solutions) {
			learn_single_class(h, solution, cases, description_path, solution_path, o, domain_model, heuristic, generalize);
		} /* while */

		return h;
	} /* rFOILHypothesis::learn */

	/**
	 * Learn_single_class.
	 * 
	 * @param h
	 *            the h
	 * @param solution
	 *            the solution
	 * @param cases
	 *            the cases
	 * @param description_path
	 *            the description_path
	 * @param solution_path
	 *            the solution_path
	 * @param o
	 *            the o
	 * @param domain_model
	 *            the domain_model
	 * @param heuristic
	 *            the heuristic
	 * @param generalize
	 *            the generalize
	 * @return true, if successful
	 * @throws Exception
	 *             the exception
	 */
	private boolean learn_single_class(RuleHypothesis h, FeatureTerm solution, Collection<FeatureTerm> cases, Path description_path, Path solution_path,
			Ontology o, FTKBase domain_model, int heuristic, boolean generalize) throws Exception {
		List<FeatureTerm> positive = new LinkedList<FeatureTerm>();
		List<FeatureTerm> negative = new LinkedList<FeatureTerm>();

		/* Learn a set of patterns for the current solution: */
		if (DEBUG >= 1)
			System.out.println("\nrHYDRA: building model for " + solution.toStringNOOS(domain_model) + "--------------------------------------------------\n");

		/* Create the positive and negative examples lists: */
		{
			FeatureTerm s, d;

			for (FeatureTerm c : cases) {
				d = c.readPath(description_path);
				s = c.readPath(solution_path);

				if (solution.equivalents(s)) {
					positive.add(d);
				} else {
					negative.add(d);
				} /* if */
			} /* while */
		}

		if (DEBUG >= 1)
			System.out.println(positive.size() + " positives and " + negative.size() + " negatives");

		if (negative.isEmpty()) {
			FeatureTerm pattern = o.getSort("any").createFeatureTerm();

			if (DEBUG >= 1)
				System.out.println("rHYDRA: rule found, covers " + positive.size() + " positive examples");
			if (DEBUG >= 1)
				System.out.println("rHYDRA: rule for class " + solution.toStringNOOS(domain_model) + ":");
			if (DEBUG >= 1)
				System.out.println(pattern.toStringNOOS(domain_model));

			h.addRule(pattern, solution, 1.0f, positive.size());
		} else {
			FeatureTerm pattern = null;
			List<FeatureTerm> refinements;
			int i, selected, nrefinements;
			float[] heuristics;
			int[] coverage;

			/* Build patterns until all the positive examples have been covered: */
			while (!positive.isEmpty()) {

				/* Description = Antiunification of all the positive and negative objects: */
				if (pattern == null) {

					List<FeatureTerm> tmp1 = new LinkedList<FeatureTerm>(), tmp2 = new LinkedList<FeatureTerm>(), tmp3;

					for (FeatureTerm f : positive)
						tmp1.add(f);
					for (FeatureTerm f : negative)
						tmp1.add(f);

					tmp2.add(o.getSort("any").createFeatureTerm());

					if (DEBUG >= 1)
						System.out.println("\nAntiunification with " + tmp1.size() + " objects");

					tmp3 = FTAntiunification.antiunification(tmp1, 0, tmp2, o, domain_model, true, FTAntiunification.VERSION_FAST);

					pattern = tmp3.remove(0);
				} /* if */

				refinements = FTRefinement.getSpecializationsSubsumingSome(pattern, domain_model, o, FTRefinement.ALL_REFINEMENTS, positive);
				if (DEBUG >= 1)
					System.out.println("rHYDRA: " + refinements.size() + " refinements, p/n: " + positive.size() + "/" + negative.size());

				if (refinements.isEmpty()) {
					int positive_covered = 0;
					int negative_covered = 0;
					float reliability = 0;

					for (FeatureTerm f : positive)
						if (pattern.subsumes(f))
							positive_covered++;
					for (FeatureTerm f : negative)
						if (pattern.subsumes(f))
							negative_covered++;

					reliability = ((float) positive_covered + 1) / ((float) positive_covered + negative_covered + 2);

					if (DEBUG >= 1)
						System.out.println("rHYDRA: rule cannot be specified further, covers " + positive_covered + " positive examples and "
								+ negative_covered + " negative examples");
					if (DEBUG >= 1)
						System.out.println("rHYDRA: rule for class " + solution.toStringNOOS(domain_model) + ":");
					if (DEBUG >= 1)
						System.out.println(pattern.toStringNOOS(domain_model));
					positive.clear();
					h.addRule(pattern, solution, reliability, positive_covered);
				} else {
					nrefinements = refinements.size();
					heuristics = new float[nrefinements];
					coverage = new int[nrefinements];

					/* Compute heuristic: */
					i = 0;
					for (FeatureTerm refinement : refinements) {
						switch (heuristic) {
						case 0: /* Information Gain: */
						{
							float before_p = 0, before_n = 0;
							float after_p = 0, after_n = 0;
							float before_i, after_i;
							double LOG2E = Math.log(2.0);

							for (FeatureTerm f : positive) {
								if (pattern.subsumes(f))
									before_p++;
								if (refinement.subsumes(f))
									after_p++;
							} /* if */
							for (FeatureTerm f : negative) {
								if (pattern.subsumes(f))
									before_n++;
								if (refinement.subsumes(f))
									after_n++;
							} /* if */

							before_i = (float) (-(Math.log(before_p / (before_p + before_n)) / LOG2E));
							if (after_p + after_n == 0)
								after_i = 0;
							else
								after_i = (float) (-(Math.log(after_p / (after_p + after_n)) / LOG2E));

							heuristics[i] = (after_p) * (before_i - after_i);
							coverage[i] = (int) after_p;
						}
							break;
						case 1: /* RLDM: */
							heuristics[i] = 0;
							coverage[i] = 0;
							break;
						default:
							heuristics[i] = 0;
							coverage[i] = 0;
							break;
						} /* switch */
						i++;
					} /* while */

					{
						float maximum = 0;
						selected = -1;

						for (i = 0; i < nrefinements; i++) {
							if (coverage[i] > 0 && (selected == -1 || heuristics[i] > maximum)) {
								maximum = heuristics[i];
								selected = i;
							} /* if */
						} /* for */

						if (DEBUG >= 1)
							System.out.println("Selected: " + selected + " (" + maximum + ")");

						if (selected >= 0) {
							int positive_covered = 0;
							int negative_covered = 0;
							float reliability = 0;
							List<FeatureTerm> to_delete = new LinkedList<FeatureTerm>();

							pattern = refinements.get(selected);

							for (FeatureTerm f : positive) {
								if (pattern.subsumes(f)) {
									positive_covered++;
									to_delete.add(f);
								} /* if */
							} // while
							for (FeatureTerm f : negative) {
								if (pattern.subsumes(f))
									negative_covered++;
							}
							reliability = ((float) positive_covered + 1) / ((float) positive_covered + negative_covered + 2);

							if (negative_covered == 0) {

								pattern = Hypothesis.generalizePattern(pattern, to_delete, negative, o, domain_model);

								if (DEBUG >= 1)
									System.out.println("rHYDRA: rule cannot be specified further, covers " + positive_covered + " positive examples");
								if (DEBUG >= 1)
									System.out.println("rHYDRA: rule for class " + solution.toStringNOOS(domain_model) + ":");
								if (DEBUG >= 1)
									System.out.println(pattern.toStringNOOS(domain_model));

								positive.removeAll(to_delete);
								to_delete.clear();
								h.addRule(pattern, solution, reliability, positive_covered);
								pattern = null;
							} else {
								to_delete.clear();
							} /* if */
						} else {
							int positive_covered = 0;
							int negative_covered = 0;
							float reliability = 0;

							for (FeatureTerm f : positive)
								if (pattern.subsumes(f))
									positive_covered++;
							for (FeatureTerm f : negative)
								if (pattern.subsumes(f))
									negative_covered++;

							reliability = ((float) positive_covered + 1) / ((float) positive_covered + negative_covered + 2);

							if (DEBUG >= 1)
								System.out.println("rHYDRA: : rule cannot be specified further covering some positive, covers " + positive_covered
										+ " positive examples and " + negative_covered + " negative examples\n");
							if (DEBUG >= 1)
								System.out.println("rHYDRA: rule for class " + solution.toStringNOOS(domain_model) + ":");
							if (DEBUG >= 1)
								System.out.println(pattern.toStringNOOS(domain_model));

							positive.clear();
							h.addRule(pattern, solution, reliability, positive_covered);
						} /* if */
					}
				} /* if */
			} /* while */
		}

		return true;
	}

}
