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

import java.util.LinkedList;
import java.util.List;

import ftl.base.core.FTAntiunification;
import ftl.base.core.FTKBase;
import ftl.base.core.FTRefinement;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Ontology;
import ftl.base.utils.FeatureTermException;
import ftl.learning.core.Hypothesis;
import ftl.learning.core.RuleHypothesis;

// TODO: Auto-generated Javadoc
/**
 * The Class RefinementINDIE2H. This class is identical to RefinementINDIE, but instead of randomly selecting one
 * antiunification, it uses a heuristic
 * 
 */
public class RefinementINDIE2H extends RefinementINDIE {

	/** The DEBUG. */
	public static int DEBUG = 0;

	/*
	 * (non-Javadoc)
	 * 
	 * @see csic.iiia.ftl.learning.inductivemethods.RefinementINDIE#INDIE(csic.iiia.ftl.learning.core.RuleHypothesis,
	 * csic.iiia.ftl.base.core.FeatureTerm, csic.iiia.ftl.base.core.FeatureTerm, java.util.List, java.util.List, int,
	 * csic.iiia.ftl.base.core.Ontology, csic.iiia.ftl.base.core.FTKBase)
	 */
	void INDIE(RuleHypothesis h, FeatureTerm description, FeatureTerm solution, List<FeatureTerm> positive, List<FeatureTerm> negative, int heuristic,
			Ontology o, FTKBase domain_model) throws Exception {

		List<FeatureTerm> initial_l = new LinkedList<FeatureTerm>();
		List<FeatureTerm> au_l;
		FeatureTerm au = null;
		List<FeatureTerm> au_negative_covered = new LinkedList<FeatureTerm>(), au_negative_uncovered = new LinkedList<FeatureTerm>();
		List<FeatureTerm> au_positive_covered = new LinkedList<FeatureTerm>(), au_positive_uncovered = new LinkedList<FeatureTerm>();
		List<FeatureTerm> positive_covered = new LinkedList<FeatureTerm>(), positive_uncovered = new LinkedList<FeatureTerm>();

		initial_l.add(description);

		if (DEBUG >= 1)
			System.out.println("RefinementINDIE2H: computing antiunification with " + positive.size() + " objects...");
		au_l = FTAntiunification.antiunification(positive, 0, initial_l, o, domain_model, true, FTAntiunification.VERSION_FAST);

		/* Choose just one antiunification: */
		{
			int min_negative = 0;
			int n_negative_covered;

			for (FeatureTerm au1 : au_l) {
				n_negative_covered = 0;
				for (FeatureTerm e : negative) {
					if (au1.subsumes(e))
						n_negative_covered++;
				} // while

				if (au == null || n_negative_covered < min_negative) {
					au = au1;
					min_negative = n_negative_covered;
				} // if
			} // while
		}

		if (DEBUG >= 1)
			System.out.println("Antiunification:\n" + au.toStringNOOS(domain_model));

		if (au != null) {
			for (FeatureTerm example : negative) {
				if (au.subsumes(example)) {
					au_negative_covered.add(example);
				} else {
					au_negative_uncovered.add(example);
				} // if
			} // while

			if (au_negative_covered.isEmpty()) {
				// Rule found!!!
				au = Hypothesis.generalizePattern(au, positive, negative, o, domain_model);
				h.addRule(au, solution, ((float) positive.size() + 1) / ((float) positive.size() + 2), positive.size());

				if (DEBUG >= 1)
					System.out.println("RefinementINDIE2H: new rule found , covers " + positive.size() + " positive examples and 0 negative examples");
				if (DEBUG >= 1)
					System.out.println("RefinementINDIE2H: rule is for class " + solution.toStringNOOS(domain_model));
				if (DEBUG >= 1)
					System.out.println(au.toStringNOOS(domain_model));
			} else {
				// Rule is too general, the space of problems has to be partitioned:
				List<FeatureTerm> refinements;
				int selected, nrefinements;
				float heuristics[];
				int i;

				for (FeatureTerm example : positive) {
					if (au.subsumes(example)) {
						au_positive_covered.add(example);
					} else {
						au_positive_uncovered.add(example);
					} // if
				} // while

				refinements = FTRefinement.getSpecializationsSubsumingSome(au, domain_model, o, FTRefinement.ALL_REFINEMENTS, positive);
				if (DEBUG >= 1)
					System.out.println("Refinements: " + refinements.size());

				// Choose one refinement according to the heuristic:
				selected = -1;
				if (refinements.size() > 0) {

					analyzeRefinements(h, description, solution, positive, negative, heuristic, o, domain_model, au_negative_covered, au_positive_covered,
							positive_covered, positive_uncovered, refinements);

				} // if
			} // if
		} else {
			System.err.println("RefinementINDIE2H: error computing antiunification!");
		} // if
	}

	/**
	 * Analyze refinements.
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
	 * @param au_negative_covered
	 *            the au_negative_covered
	 * @param au_positive_covered
	 *            the au_positive_covered
	 * @param positive_covered
	 *            the positive_covered
	 * @param positive_uncovered
	 *            the positive_uncovered
	 * @param refinements
	 *            the refinements
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws Exception
	 *             the exception
	 */
	private void analyzeRefinements(RuleHypothesis h, FeatureTerm description, FeatureTerm solution, List<FeatureTerm> positive, List<FeatureTerm> negative,
			int heuristic, Ontology o, FTKBase domain_model, List<FeatureTerm> au_negative_covered, List<FeatureTerm> au_positive_covered,
			List<FeatureTerm> positive_covered, List<FeatureTerm> positive_uncovered, List<FeatureTerm> refinements) throws FeatureTermException, Exception {
		int selected;
		int nrefinements;
		float[] heuristics;
		int i;
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
					if (refinement.subsumes(f))
						after_p++;
				} // for
				for (FeatureTerm f : au_negative_covered) {
					if (refinement.subsumes(f))
						after_n++;
				} // for

				before_p = au_positive_covered.size();
				before_n = au_negative_covered.size();
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
					System.out.printf("%d -> %g {%g,%g} [%d,%d] . [%d,%d]/[%d,%d] (%d . %d)\n", i, heuristics[i], after_i1, after_i2, before_p, before_n,
							after_p, after_n, before_p - after_p, before_n - after_n, before, after);
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

		if (DEBUG >= 1)
			System.out.println("Refinement selected: " + selected + "/" + nrefinements);
		FeatureTerm refinement = refinements.get(selected);

		for (FeatureTerm example : positive) {
			if (refinement.subsumes(example)) {
				positive_covered.add(example);
			} else {
				positive_uncovered.add(example);
			} // if
		} // while

		if (DEBUG >= 1)
			System.out.println("(" + selected + " - " + heuristics[selected] + ") " + positive_covered.size() + " covered, " + positive_uncovered.size()
					+ " uncovered");

		if (!positive_covered.isEmpty()) {
			if (DEBUG >= 1)
				System.out.println("continuing with positive_covered (" + positive_covered.size() + ") and negative (" + negative.size() + ")");
			INDIE(h, refinement, solution, positive_covered, negative, heuristic, o, domain_model);
		}
		if (!positive_uncovered.isEmpty() && positive_uncovered.size() < positive.size()) {
			negative.add(refinement);
			if (DEBUG >= 1)
				System.out.println("continuing with positive_uncovered (" + positive_uncovered.size() + ") and negative (" + negative.size() + ")");
			INDIE(h, description, solution, positive_uncovered, negative, heuristic, o, domain_model);
			negative.remove(refinement);
		} // if
	}

}
