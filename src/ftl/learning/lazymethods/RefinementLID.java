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
  
 package ftl.learning.lazymethods;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ftl.base.core.FTAntiunification;
import ftl.base.core.FTKBase;
import ftl.base.core.FTRefinement;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Ontology;
import ftl.base.core.Path;
import ftl.base.core.Symbol;
import ftl.base.core.TermFeatureTerm;
import ftl.base.utils.FeatureTermException;
import ftl.base.utils.Pair;
import ftl.learning.core.InformationMeasurement;
import ftl.learning.core.Prediction;

// TODO: Auto-generated Javadoc
/**
 * The Class RefinementLID.
 */
public class RefinementLID extends LID {

	/**
	 * Predict_ rli d3.
	 * 
	 * @param problem
	 *            the problem
	 * @param cases
	 *            the cases
	 * @param description_path
	 *            the description_path
	 * @param solution_path
	 *            the solution_path
	 * @param o
	 *            the o
	 * @param dm
	 *            the dm
	 * @param heuristic
	 *            the heuristic
	 * @param selection_mode
	 *            the selection_mode
	 * @param min_precedents
	 *            the min_precedents
	 * @param generalize
	 *            the generalize
	 * @return the prediction
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public static Prediction predict_RLID3(FeatureTerm problem, List<FeatureTerm> cases, Path description_path, Path solution_path, Ontology o, FTKBase dm,
			int heuristic, int selection_mode, int min_precedents, boolean generalize) throws FeatureTermException {
		Prediction prediction = null;
		List<FeatureTerm> discriminant_set, dset_tmp, discriminant_set_complementary;
		List<FeatureTerm> discriminant_solutions, dsolutions_tmp;
		List<FeatureTerm> solutions = new ArrayList<FeatureTerm>();
		FeatureTerm description, solution;
		FeatureTerm last_description = null, current_description = null;
		FeatureTerm short_description;
		List<FeatureTerm> description_refinements;
		int nrefinements, i, selected;
		float[] heuristics;
		int[] coverage;
		boolean finished;
		int language = FTRefinement.ALL_REFINEMENTS;
		int rounds = 0;

		long start_time = System.currentTimeMillis();
		// System.out.println("rLID3: start\n");

		// Create the initial description and discriminant sets:
		current_description = new TermFeatureTerm((Symbol) null, o.getSort("any"));
		short_description = current_description;
		{
			discriminant_set = new ArrayList<FeatureTerm>();
			discriminant_set_complementary = new ArrayList<FeatureTerm>();
			discriminant_solutions = new ArrayList<FeatureTerm>();

			for (FeatureTerm c : cases) {
				discriminant_set.add(c.readPath(description_path));
				discriminant_solutions.add(c.readPath(solution_path));
			} // for

			// System.out.println("rLID3: %i initial cases\n",discriminant_set.Length());
		}

		// Compute the different solutions:
		{
			// Compute the list of different solutions:
			{
				Set<FeatureTerm> tmp = new HashSet<FeatureTerm>();
				for (FeatureTerm s : discriminant_solutions) {
					tmp.add(s);
				}
				solutions.addAll(tmp);
			}
		}

		if (solutions.size() == 0) {
			System.err.println("rLID3: ERROR!! no solutions!\n");
			return null;
		} /* if */

		rounds = 0;
		do {
			finished = false;

			// sanity check
			/*
			 * { if (last_description!=null) { if (!last_description.subsumes(current_description))
			 * System.out.println("rLID3: SANITY CHECK 1 FAILRED!!!!!!!!!!!!!!!!"); if
			 * (current_description.subsumes(last_description))
			 * System.out.println("rLID3: SANITY CHECK 2 FAILRED!!!!!!!!!!!!!!!!"); }
			 * 
			 * if (rounds>25) { System.out.println(current_description.toStringNOOS(dm)); } }
			 */

			// Description = Antiunification of all the base objects:
			{
				List<FeatureTerm> tmp1 = new LinkedList<FeatureTerm>();
				List<FeatureTerm> objects = new LinkedList<FeatureTerm>();
				FeatureTerm au;

				tmp1.add(problem);
				tmp1.addAll(discriminant_set);

				au = FTAntiunification.simpleAntiunification(tmp1, current_description, o, dm);
				// System.out.println("rLID3: Antiunification complete\n");
				// System.out.println(current_description.toStringNOOS(dm));

				objects.add(problem);
				objects.addAll(discriminant_set);
				// description_refinements = DSpecializationL2(current_description,&objects,0,o,-1,domain_model);
				description_refinements = FTRefinement.getSpecializationsSubsumingSome(au, dm, o, language, objects);
			}
			// System.out.println("rLID3: " + description_refinements.size() + " refinements");

			/* Filter refinements: */
			{
				List<FeatureTerm> to_delete = new LinkedList<FeatureTerm>();
				boolean valid;

				for (FeatureTerm refinement : description_refinements) {
					valid = false;
					if (refinement.subsumes(problem) && !refinement.subsumes(current_description)) {
						valid = true;
					} // if

					if (!valid)
						to_delete.add(refinement);
				} /* while */

				description_refinements.removeAll(to_delete);
			}
			// System.out.println("rLID3: " + description_refinements.size() + " valid refinements");

			selected = -1;
			if (description_refinements.size() > 0) {

				/* Evaluate all the refinements: */
				nrefinements = description_refinements.size();
				heuristics = new float[nrefinements];
				coverage = new int[nrefinements];
				i = 0;
				// System.out.println("rLID3: Computing heuristics...\n");
				for (FeatureTerm refinement : description_refinements) {
					switch (heuristic) {
					case 0: /* Information Gain: */{
						Pair<Float, Integer> p = InformationMeasurement.h_information_gain(discriminant_set, discriminant_solutions, solutions, refinement);
						heuristics[i] = p.m_a;
						coverage[i] = p.m_b;
					}
						break;
					case 1: /* RLDM: */{
						Pair<Float, Integer> p = InformationMeasurement.h_rldm(discriminant_set, discriminant_solutions, solutions, refinement);
						heuristics[i] = p.m_a;
						coverage[i] = p.m_b;
					}
						break;
					case 2: /* Entropy: */{
						Pair<Float, Integer> p = InformationMeasurement.h_entropy(discriminant_set, discriminant_solutions, solutions, refinement);
						heuristics[i] = p.m_a;
						coverage[i] = p.m_b;
					}
						break;
					default:
						coverage[i] = 0;
						heuristics[i] = 0;
						break;
					} /* switch */

					// System.out.println("%g [%i]\n",heuristics[i],coverage[i]);

					i++;
				} /* while */
				// System.out.println("rLID3: heuristics complete\n");

				/* Choose one refinement: */
				switch (selection_mode) {
				case 0:/* MAXIMUM: */{
					float maximum = heuristics[0];
					selected = 0;

					for (i = 1; i < nrefinements; i++) {
						if (heuristics[i] > maximum || (heuristics[i] == maximum && coverage[i] > coverage[selected])) {
							maximum = heuristics[i];
							selected = i;
						} /* if */
					} /* for */
				}
					break;
				case 1:/* RANDOM: */
					selected = s_rand.nextInt(nrefinements);
					break;
				case 2:/* RANDOM PONDERATED: */{
					float h_sum = 0;
					float tmp, accum;

					// System.out.println("{ 0");
					for (i = 0; i < nrefinements; i++) {
						h_sum += heuristics[i];
						// System.out.println(" - %g",h_sum);
					} // if

					tmp = s_rand.nextFloat() * h_sum;

					selected = -1;
					accum = 0;
					for (i = 0; selected == -1 && i < nrefinements; i++) {
						if (accum + heuristics[i] > tmp) {
							selected = i;
						}
						accum += heuristics[i];
					} // for

					// System.out.println(" } . %g . %i\n",tmp,selected);

					if (selected == -1) {
						selected = s_rand.nextInt(nrefinements);
					}
				}
					break;
				case 3:/* MINIMUM: */{
					float minimum = heuristics[0];
					selected = 0;

					for (i = 1; i < nrefinements; i++) {
						if (heuristics[i] < minimum || (heuristics[i] == minimum && coverage[i] > coverage[selected])) {
							minimum = heuristics[i];
							selected = i;
						} /* if */
					} /* for */
				}
					break;

				default:
					selected = 0;
					break;
				} // while
			} // if

			if (selected >= 0) {
				last_description = current_description;
				current_description = description_refinements.get(selected);

				dset_tmp = new LinkedList<FeatureTerm>();
				dsolutions_tmp = new LinkedList<FeatureTerm>();

				for (i = 0; i < discriminant_set.size(); i++) {
					description = discriminant_set.get(i);
					solution = discriminant_solutions.get(i);
					if (current_description.subsumes(description)) {
						dset_tmp.add(description);
						dsolutions_tmp.add(solution);
					} else {
						discriminant_set_complementary.add(description);
					} // if
				} // while

				// System.out.println("rLID: |Dset| = %i, |DsetC| = %i\n",dset_tmp.Length(),discriminant_set_complementary.Length());

				if (dset_tmp.size() > 0) {
					discriminant_set = dset_tmp;
					discriminant_solutions = dsolutions_tmp;
					dset_tmp = null;
					dsolutions_tmp = null;
				} else {
					// System.out.println("rLID3: discarting all the refinements\n");
					current_description = last_description;
					dset_tmp = null;
					dsolutions_tmp = null;
					selected = -1; /* Don't select this refinement, since it makes de discriminant set to be empty */
				} /* if */
			} /* if */

			short_description = current_description;

			if (selected >= 0) {
				List<FeatureTerm> stmp = new LinkedList<FeatureTerm>();

				for (FeatureTerm sol : discriminant_solutions) {
					if (!stmp.contains(sol)) {
						stmp.add(sol);
					} // if
				} // while

				if (stmp.size() == 1) {
					finished = true;
				}
			} /* if */

			if (discriminant_set.size() < min_precedents) {
				finished = true;
			}
			rounds++;
		} while (selected >= 0 && !finished);

		if (generalize) {
			List<FeatureTerm> l, l2 = new LinkedList<FeatureTerm>();
			FeatureTerm f = null;
			boolean end = true, c1, c2;

			l2.add(short_description);
			f = current_description;

			do {
				end = true;
				l = FTRefinement.getGeneralizations(f, dm, o);
				// System.out.println(f.toStringNOOS(dm));
				// System.out.println("Generalizing Justification: " + l.size() + " refinements.");

				for (FeatureTerm f2 : l) {
					c1 = true;
					for (FeatureTerm d : discriminant_set) {
						if (!f2.subsumes(d)) {
							c1 = false;
							break;
						}
					} // while

					c2 = true;
					for (FeatureTerm d : discriminant_set_complementary) {
						if (f2.subsumes(d)) {
							c2 = false;
							break;
						}
					} // while

					if (c1 && c2) {
						f = f2;
						end = false;
					} // if
				} // if
			} while (!end);
			short_description = f;
		} else {
			short_description = current_description;
		}

		/* Determine final solution: */
		prediction = new Prediction(problem);
		{
			int[] distribution = new int[solutions.size()];

			for (i = 0; i < solutions.size(); i++) {
				distribution[i] = 0;
			}

			for (FeatureTerm sol : discriminant_solutions) {
				distribution[solutions.indexOf(sol)]++;
			} /* while */

			for (i = 0; i < solutions.size(); i++) {
				if (distribution[i] > 0) {
					prediction.solutions.add(solutions.get(i));
					prediction.justifications.put(solutions.get(i), short_description);
					prediction.support.put(solutions.get(i), distribution[i]);
				} /* if */
			} /* for */

			// Sort the solutions according to their support:
			{
				boolean change;
				FeatureTerm ftptr;
				do {
					change = false;
					for (i = 0; i < prediction.solutions.size() - 1; i++) {
						if (prediction.support.get(prediction.solutions.get(i)) < prediction.support.get(prediction.solutions.get(i + 1))) {
							ftptr = prediction.solutions.get(i);
							prediction.solutions.set(i, prediction.solutions.get(i + 1));
							prediction.solutions.set(i + 1, ftptr);
							change = true;
						} // if
					} // for
				} while (change);
			}
		} /* if */

		/* Show result: */
		{
			System.out.println("/- rLID3 --------------------------------------------------------\\");
			System.out.println("* Time taken: " + (System.currentTimeMillis() - start_time) + "ms");
			System.out.println("* Problem:");
			if (prediction.problem.getName() != null) {
				System.out.println("  " + prediction.problem.getName().get());
			} else {
				System.out.println("?");
			} // if
			System.out.println("* " + prediction.justifications.size() + " different solutions");

			for (FeatureTerm sol : prediction.solutions) {
				System.out.println("* Solution:");
				System.out.println("  " + sol.toStringNOOS(dm));
				System.out.println("* Justification:");
				System.out.println("  " + (prediction.justifications.get(sol) != null ? prediction.justifications.get(sol).toStringNOOS(dm) : "-"));
				System.out.println("* Support: " + prediction.support.get(sol));
			} // while
			System.out.println("\\----------------------------------------------------------------/");
		}

		return prediction;
	} /* refinement_LID3 */

}
