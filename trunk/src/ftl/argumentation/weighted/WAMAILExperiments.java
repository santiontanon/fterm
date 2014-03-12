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

import ftl.base.core.BaseOntology;
import ftl.base.core.FTKBase;
import ftl.base.core.FTRefinement;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Ontology;
import ftl.base.core.Path;
import ftl.base.utils.FeatureTermException;
import ftl.learning.core.Hypothesis;
import ftl.learning.core.Prediction;
import ftl.learning.core.RuleHypothesis;
import ftl.learning.core.TrainingSetProperties;
import ftl.learning.core.TrainingSetUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class WAMAILExperiments.
 * 
 * @author santi
 */
public class WAMAILExperiments {

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String args[]) {
		int ITERATIONS = 5;
		int NAGENTS = 2;
		double BIAS = 0;
		double REDUNDANCY = 0;
		int language = FTRefinement.ALL_REFINEMENTS;
		// int language = FTRefinement.NO_EQUALITIES;

		try {
			Ontology base_ontology = new BaseOntology();
			Ontology o = new Ontology();
			FTKBase dm = new FTKBase();
			FTKBase case_base = new FTKBase();
			o.uses(base_ontology);
			case_base.uses(dm);

			dm.create_boolean_objects(o);

			TrainingSetProperties ts = TrainingSetUtils.loadTrainingSet(TrainingSetUtils.ZOOLOGY_DATASET, o, dm, case_base);

			System.out.println("Data set loaded...");
			System.out.flush();

			List<FeatureTerm> allSolutions = Hypothesis.differentSolutions(ts.cases, ts.solution_path);

			HashMap<String, int[]> training_results = new HashMap<String, int[]>();
			HashMap<String, int[]> test_results = new HashMap<String, int[]>();

			for (FeatureTerm solution : allSolutions) {
				System.out.println("Target class: " + solution.toStringNOOS(dm));

				for (int iteration = 0; iteration < ITERATIONS; iteration++) {
					System.out.println("ITERATION " + iteration + ": ----------------------- ");
					List<List<FeatureTerm>> folds = TrainingSetUtils.splitTrainingSet(ts.cases, 10, ts.description_path, ts.solution_path, dm, 0.0, 0.0);

					for (List<FeatureTerm> test_set : folds) {
						List<FeatureTerm> training_set = new LinkedList<FeatureTerm>();
						for (List<FeatureTerm> tmp : folds)
							if (tmp != test_set)
								training_set.addAll(tmp);

						List<List<FeatureTerm>> individual_training_sets = TrainingSetUtils.splitTrainingSet(training_set, NAGENTS, ts.description_path,
								ts.solution_path, dm, BIAS, REDUNDANCY);

						// INDIVIDUAL:
						System.out.println(" *** INDIVIDUAL ***");
						for (int i = 0; i < NAGENTS; i++) {
							long start = System.currentTimeMillis();
							RuleHypothesis h_tmp = WABUI.learnConceptHypothesis(individual_training_sets.get(i), solution, null, ts.description_path,
									ts.solution_path, o, dm, "Individual", language);
							long end = System.currentTimeMillis();
							System.out.println("WABUI found a hypotheses with " + h_tmp.getRules().size() + " rules");
							addResults(test_results, "Individual",
									evaluateHypotheses(h_tmp, solution, test_set, dm, ts.description_path, ts.solution_path, (int) (end - start), 0, 0));
							addResults(
									training_results,
									"Individual",
									evaluateHypotheses(h_tmp, solution, individual_training_sets.get(i), dm, ts.description_path, ts.solution_path,
											(int) (end - start), 0, 0));
							addResults(training_results, "Individual (all training set)",
									evaluateHypotheses(h_tmp, solution, training_set, dm, ts.description_path, ts.solution_path, (int) (end - start), 0, 0));
						}

						// WAMAIL:
						System.out.println(" *** WAMAIL ***");
						long start = System.currentTimeMillis();
						List<WArgumentationAgent> l_a = WAMAIL.WAMAIL(individual_training_sets, solution, o, dm, ts.description_path, ts.solution_path,
								language);
						long end = System.currentTimeMillis();
						List<RuleHypothesis> l_h1 = WAMAIL.getAgentsHypotheses(l_a, solution, ts.description_path, ts.solution_path,
								WAMAIL.INDIVIDUAL_CORRECTED);
						List<RuleHypothesis> l_h2 = WAMAIL
								.getAgentsHypotheses(l_a, solution, ts.description_path, ts.solution_path, WAMAIL.INDIVIDUAL_EXPANDED);
						List<RuleHypothesis> l_h3 = WAMAIL.getAgentsHypotheses(l_a, solution, ts.description_path, ts.solution_path, WAMAIL.UNIFIED);
						for (int i = 0; i < NAGENTS; i++) {
							RuleHypothesis h_tmp = l_h1.get(i);
							System.out.println("WMAIL found a hypotheses with " + h_tmp.getRules().size() + " rules");
							addResults(
									test_results,
									"WAMAIL-CORRECTED",
									evaluateHypotheses(h_tmp, solution, test_set, dm, ts.description_path, ts.solution_path, (int) (end - start),
											WAMAIL.last_af_size, WAMAIL.last_n_rounds));
							addResults(
									training_results,
									"WAMAIL-CORRECTED",
									evaluateHypotheses(h_tmp, solution, individual_training_sets.get(i), dm, ts.description_path, ts.solution_path,
											(int) (end - start), WAMAIL.last_af_size, WAMAIL.last_n_rounds));
							addResults(
									training_results,
									"WAMAIL-CORRECTED (all training set)",
									evaluateHypotheses(h_tmp, solution, training_set, dm, ts.description_path, ts.solution_path, (int) (end - start),
											WAMAIL.last_af_size, WAMAIL.last_n_rounds));
							h_tmp = l_h2.get(i);
							addResults(
									test_results,
									"WAMAIL-EXPANDED",
									evaluateHypotheses(h_tmp, solution, test_set, dm, ts.description_path, ts.solution_path, (int) (end - start),
											WAMAIL.last_af_size, WAMAIL.last_n_rounds));
							addResults(
									training_results,
									"WAMAIL-EXPANDED",
									evaluateHypotheses(h_tmp, solution, individual_training_sets.get(i), dm, ts.description_path, ts.solution_path,
											(int) (end - start), WAMAIL.last_af_size, WAMAIL.last_n_rounds));
							addResults(
									training_results,
									"WAMAIL-EXPANDED (all training set)",
									evaluateHypotheses(h_tmp, solution, training_set, dm, ts.description_path, ts.solution_path, (int) (end - start),
											WAMAIL.last_af_size, WAMAIL.last_n_rounds));
							h_tmp = l_h3.get(i);
							addResults(
									test_results,
									"WAMAIL-UNIFIED",
									evaluateHypotheses(h_tmp, solution, test_set, dm, ts.description_path, ts.solution_path, (int) (end - start),
											WAMAIL.last_af_size, WAMAIL.last_n_rounds));
							addResults(
									training_results,
									"WAMAIL-UNIFIED",
									evaluateHypotheses(h_tmp, solution, individual_training_sets.get(i), dm, ts.description_path, ts.solution_path,
											(int) (end - start), WAMAIL.last_af_size, WAMAIL.last_n_rounds));
							addResults(
									training_results,
									"WAMAIL-UNIFIED (all training set)",
									evaluateHypotheses(h_tmp, solution, training_set, dm, ts.description_path, ts.solution_path, (int) (end - start),
											WAMAIL.last_af_size, WAMAIL.last_n_rounds));
						}

						// break;
					}
					// break;
				}
				// break;
			}

			// Print results:
			System.out.println(" --- FINAL RESULTS: --------------------");
			int n_results = ITERATIONS * 10 * NAGENTS * allSolutions.size();
			for (String id : test_results.keySet()) {
				System.out.println("Results for: " + id);
				int[] res = test_results.get(id);
				System.out.println("* Test: " + res[0] + " , " + res[1] + "," + res[2] + "," + res[3]);
				System.out.println("  Accuracy: " + ((res[0] + res[1]) / (float) (res[0] + res[1] + res[2] + res[3])));
				System.out.println("  Precision: " + ((res[0]) / (float) (res[0] + res[2])));
				System.out.println("  Recall: " + ((res[0]) / (float) (res[0] + res[3])));
				System.out.println("  Average time: " + (res[4] / (float) n_results));
				System.out.println("  Hypothesis size: " + (res[5] / (float) n_results));
				System.out.println("  N args: " + (res[6] / (float) n_results));
				System.out.println("  N rounds: " + (res[7] / (float) n_results));
			}
			for (String id : training_results.keySet()) {
				int[] res = test_results.get(id);
				res = training_results.get(id);
				System.out.println("Results for: " + id);
				System.out.println("* Training: " + res[0] + " , " + res[1] + "," + res[2] + "," + res[3]);
				System.out.println("  Accuracy: " + ((res[0] + res[1]) / (float) (res[0] + res[1] + res[2] + res[3])));
				System.out.println("  Precision: " + ((res[0]) / (float) (res[0] + res[2])));
				System.out.println("  Recall: " + ((res[0]) / (float) (res[0] + res[3])));
				System.out.println("  Average time: " + (res[4] / (float) n_results));
				System.out.println("  Hypothesis size: " + (res[5] / (float) n_results));
				System.out.println("  N args: " + (res[6] / (float) n_results));
				System.out.println("  N rounds: " + (res[7] / (float) n_results));
			}
			System.out.println(" --- WAMAIL STATISTICS: --------------------");
			System.out.println("Max rounds: " + WAMAIL.max_n_rounds);
			System.out.println("Max AF size (number of rules exchanged): " + WAMAIL.max_af_size);
			System.out.println("Max AF depth: " + WAMAIL.max_af_depth);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Adds the results.
	 * 
	 * @param hash
	 *            the hash
	 * @param id
	 *            the id
	 * @param res
	 *            the res
	 */
	static void addResults(HashMap<String, int[]> hash, String id, int[] res) {
		int[] current = hash.get(id);

		if (current == null) {
			hash.put(id, res);
		} else {
			current[0] += res[0]; // tp
			current[1] += res[1]; // tn
			current[2] += res[2]; // fp
			current[3] += res[3]; // fn
			current[4] += res[4]; // time
			current[5] += res[5]; // number of rules
			current[6] += res[6]; // number of args
			current[7] += res[7]; // number of rounds
		}
	}

	// This method returns the confusion matrix: [tp,tn, fp, fn] plus some additional statistics: "time taken" and
	// "number of rules"
	/**
	 * Evaluate hypotheses.
	 * 
	 * @param h
	 *            the h
	 * @param solution
	 *            the solution
	 * @param test_set
	 *            the test_set
	 * @param dm
	 *            the dm
	 * @param dp
	 *            the dp
	 * @param sp
	 *            the sp
	 * @param time
	 *            the time
	 * @param args
	 *            the args
	 * @param rounds
	 *            the rounds
	 * @return the int[]
	 * @throws Exception
	 *             the exception
	 */
	static int[] evaluateHypotheses(RuleHypothesis h, FeatureTerm solution, List<FeatureTerm> test_set, FTKBase dm, Path dp, Path sp, int time, int args,
			int rounds) throws Exception {
		// Evaluate the hypothesis:

		int positive = 0;
		int positive_covered = 0;
		int negative = 0;
		int negative_covered = 0;
		int covered = 0;
		int total = 0;

		for (FeatureTerm c : test_set) {
			Prediction p;
			try {
				p = h.generatePrediction(c.readPath(dp), dm, false);
				if (c.readPath(sp).equivalents(solution)) {
					positive++;
					if (p == null) {
					} else {
						if (p.getSolution() != null && p.getSolution().equivalents(solution)) {
							covered++;
							positive_covered++;
						}
					}
				} else {
					negative++;
					if (p == null) {
					} else {
						if (p.getSolution() != null && p.getSolution().equivalents(solution)) {
							covered++;
							negative_covered++;
						}
					}
				}
			} catch (FeatureTermException e) {
				e.printStackTrace();
			}
			total++;
		}

		System.out.println("Covered " + positive_covered + "/" + positive + " positive and " + negative_covered + "/" + negative + " negative");

		return new int[] { positive_covered, negative - negative_covered, negative_covered, positive - positive_covered, time, h.getRules().size(), args,
				rounds };
	}
}
