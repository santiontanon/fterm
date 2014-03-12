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
 * The Class RefinementINDIE.
 */
public class RefinementINDIE extends InductiveLearner {

	/** The DEBUG. */
	public static int DEBUG = 0;

	/*
	 * (non-Javadoc)
	 * 
	 * @see csic.iiia.ftl.learning.core.InductiveLearner#generateHypothesis(java.util.List,
	 * csic.iiia.ftl.base.core.Path, csic.iiia.ftl.base.core.Path, csic.iiia.ftl.base.core.Ontology,
	 * csic.iiia.ftl.base.core.FTKBase)
	 */
	public Hypothesis generateHypothesis(List<FeatureTerm> examples, Path dp, Path sp, Ontology o, FTKBase dm) throws Exception {
		return learn(examples, dp, sp, o, dm, 0);
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
	 * @return the rule hypothesis
	 * @throws Exception
	 *             the exception
	 */
	private RuleHypothesis learn(Collection<FeatureTerm> cases, Path description_path, Path solution_path, Ontology o, FTKBase domain_model, int heuristic)
			throws Exception {
		RuleHypothesis h = new RuleHypothesis(false);
		HashMap<FeatureTerm, List<FeatureTerm>> casesBySolution = new HashMap<FeatureTerm, List<FeatureTerm>>();
		List<FeatureTerm> positive = new LinkedList<FeatureTerm>();
		List<FeatureTerm> negative = new LinkedList<FeatureTerm>();

		// Compute the different solutions:
		{
			// int pos;
			FeatureTerm s, d;
			List<FeatureTerm> descriptions;

			for (FeatureTerm c : cases) {
				d = c.readPath(description_path);
				s = c.readPath(solution_path);

				descriptions = casesBySolution.get(s);
				if (descriptions == null) {
					descriptions = new LinkedList<FeatureTerm>();
					descriptions.add(d);
					casesBySolution.put(s, descriptions);
				} else {
					descriptions.add(d);
				} // if
			} // while
		}

		// Compute the default solution:
		{
			int max = -1;
			List<FeatureTerm> tmp;

			h.setM_default_solution(null);

			for (FeatureTerm s : casesBySolution.keySet()) {
				tmp = casesBySolution.get(s);

				if (max == -1 || tmp.size() > max) {
					max = tmp.size();
					h.setM_default_solution(s);
				} // if
			} // while
		}

		for (FeatureTerm solution : casesBySolution.keySet()) {
			// Learn a set of patterns for the current solution:
			if (DEBUG >= 1)
				System.out.println("\nRefinementINDIE: building model for " + solution.toStringNOOS(domain_model)
						+ " --------------------------------------------------\n");

			// Create the positive and negative examples lists:
			for (FeatureTerm sol : casesBySolution.keySet()) {
				if (sol.equals(solution)) {
					positive.addAll(casesBySolution.get(sol));
				} else {
					negative.addAll(casesBySolution.get(sol));
				}
			}

			FeatureTerm description = o.getSort("any").createFeatureTerm();

			INDIE(h, description, solution, positive, negative, heuristic, o, domain_model);

			positive.clear();
			negative.clear();
		} // while

		return h;
	}

	/**
	 * INDIE.
	 * 
	 * @param h
	 *            the h
	 * @param description
	 *            the description
	 * @param solution
	 *            the solution
	 * @param positive
	 *            the positive
	 * @param negative
	 *            the negative
	 * @param heuristic
	 *            the heuristic
	 * @param o
	 *            the o
	 * @param domain_model
	 *            the domain_model
	 * @throws Exception
	 *             the exception
	 */
	void INDIE(RuleHypothesis h, FeatureTerm description, FeatureTerm solution, List<FeatureTerm> positive, List<FeatureTerm> negative, int heuristic,
			Ontology o, FTKBase domain_model) throws Exception {

		List<FeatureTerm> initial_l = new LinkedList<FeatureTerm>();
		List<FeatureTerm> au_l;
		FeatureTerm au;
		List<FeatureTerm> negative_covered = new LinkedList<FeatureTerm>(), negative_uncovered = new LinkedList<FeatureTerm>();
		List<FeatureTerm> positive_covered = new LinkedList<FeatureTerm>(), positive_uncovered = new LinkedList<FeatureTerm>();

		initial_l.add(description);

		if (DEBUG >= 1)
			System.out.println("RefinementINDIE: computing antiunification with " + positive.size() + " objects...");
		au_l = FTAntiunification.antiunification(positive, 0, initial_l, o, domain_model, true, FTAntiunification.VERSION_FAST);

		au = au_l.remove(0);

		if (au != null) {
			for (FeatureTerm example : negative) {
				if (au.subsumes(example)) {
					negative_covered.add(example);
				} else {
					negative_uncovered.add(example);
				} // if
			} // while

			if (negative_covered.isEmpty()) {
				// Rule found!!!
				au = Hypothesis.generalizePattern(au, positive, negative, o, domain_model);
				h.addRule(au, solution, ((float) positive.size() + 1) / ((float) positive.size() + 2), positive.size());

				if (DEBUG >= 1)
					System.out.println("RefinementINDIE: new rule found , covers " + positive.size() + " positive examples and 0 negative examples");
				if (DEBUG >= 1)
					System.out.println("RefinementINDIE: rule is for class " + solution.toStringNOOS(domain_model));
				if (DEBUG >= 1)
					System.out.println(au.toStringNOOS(domain_model));
			} else {
				// Rule is too general, the space of problems has to be partitioned:
				List<FeatureTerm> refinements;
				int selected, nrefinements;
				float heuristics[];
				int i;

				refinements = FTRefinement.getSpecializationsSubsumingSome(au, domain_model, o, FTRefinement.ALL_REFINEMENTS, positive);

				// Choose one refinement according to the heuristic:
				selected = -1;
				if (refinements.size() > 0) {

					// Evaluate all the refinements:
					nrefinements = refinements.size();
					heuristics = new float[nrefinements];
					i = 0;
					for (FeatureTerm refinement : refinements) {
						switch (heuristic) {
						case 0: // Information Gain:
						{
							int before_p = 0, before_n = 0, before = 0;
							int after_p = 0, after_n = 0, after = 0;
							float before_i, after_i1, after_i2;
							double LOG2E = Math.log(2.0);

							for (FeatureTerm f : positive) {
								if (au.subsumes(f))
									before_p++;
								if (refinement.subsumes(f))
									after_p++;
							} // if
							for (FeatureTerm f : negative) {
								if (au.subsumes(f))
									before_n++;
								if (refinement.subsumes(f))
									after_n++;
							} // if

							before = before_p + before_n;
							after = after_p + after_n;
							before_i = ((float) -(Math.log(((float) before_p) / ((float) before_p + before_n)) / LOG2E));
							if (after == 0) {
								after_i1 = 0;
							} else {
								if (after_p == 0) {
									after_i1 = 1;
								} else {
									after_i1 = ((float) -(Math.log(((float) after_p) / ((float) after)) / LOG2E));
								} // if
							} // if
							if (before - after == 0)
								after_i2 = 0;
							else
								after_i2 = ((float) -(Math.log(((float) before_p - after_p) / ((float) before - after)) / LOG2E));

							heuristics[i] = -(before_i - (after * after_i1 + (before - after) * after_i2) / before);
							if (DEBUG >= 1)
								System.out.printf("%d -> %g {%g,%g} [%d,%d] . [%d,%d]/[%d,%d] (%d . %d)\n", i, heuristics[i], after_i1, after_i2, before_p,
										before_n, after_p, after_n, before_p - after_p, before_n - after_n, before, after);
						}
							break;
						case 1: // RLDM:
							heuristics[i] = 0;
							break;
						default:
							heuristics[i] = 0;
							break;
						} // switch

						i++;
					} // while

					// Choose one refinement:
					{
						float maximum = heuristics[0];
						selected = 0;

						for (i = 0; i < nrefinements; i++) {
							if (heuristics[i] > maximum) {
								maximum = heuristics[i];
								selected = i;
							} // if
						} // for
					}

					FeatureTerm refinement = refinements.get(selected);

					for (FeatureTerm example : positive) {
						if (refinement.subsumes(example)) {
							positive_covered.add(example);
						} else {
							positive_uncovered.add(example);
						} // if
					} // while

					if (DEBUG >= 1)
						System.out.println("(" + selected + " - " + heuristics[selected] + ") " + positive_covered.size() + " covered, "
								+ positive_uncovered.size() + " uncovered");

					if (!positive_covered.isEmpty())
						INDIE(h, refinement, solution, positive_covered, negative, heuristic, o, domain_model);
					if (!positive_uncovered.isEmpty() && positive_uncovered.size() < positive.size()) {
						negative.add(refinement);
						INDIE(h, description, solution, positive_uncovered, negative, heuristic, o, domain_model);
						negative.remove(refinement);
					} // if

				} // if
			} // if
		} else {
			System.err.println("RefinementINDIE: error computing antiunification!");
		} // if
	}

}
