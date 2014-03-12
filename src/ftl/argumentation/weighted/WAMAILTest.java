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
 * The Class WAMAILTest.
 * 
 * @author santi
 */
public class WAMAILTest {

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String args[]) {
		int ITERATIONS = 1;
		int NAGENTS = 2;
		double BIAS = 0;
		double REDUNDANCY = 0;

		try {
			Ontology base_ontology = new BaseOntology();
			Ontology o = new Ontology();
			FTKBase dm = new FTKBase();
			FTKBase case_base = new FTKBase();
			o.uses(base_ontology);
			case_base.uses(dm);

			dm.create_boolean_objects(o);

			// TrainingSetProperties ts = TrainingSetUtils.loadTrainingSet(TrainingSetUtils.ZOOLOGY_DATASET, o, dm,
			// case_base);
			// TrainingSetProperties ts = TrainingSetUtils.loadTrainingSet(TrainingSetUtils.DEMOSPONGIAE_120_DATASET, o,
			// dm, case_base);
			TrainingSetProperties ts = TrainingSetUtils.loadTrainingSet(TrainingSetUtils.DEMOSPONGIAE_280_DATASET, o, dm, case_base);
			// ts.printStatistics(dm);

			System.out.println("Data set loaded...");
			System.out.flush();

			List<FeatureTerm> allSolutions = Hypothesis.differentSolutions(ts.cases, ts.solution_path);

			HashMap<String, int[]> training_results = new HashMap<String, int[]>();
			HashMap<String, int[]> test_results = new HashMap<String, int[]>();

			List<List<FeatureTerm>> individual_training_sets = new LinkedList<List<FeatureTerm>>();
			String split1[] = { "sp-7", "sp-280", "sp-9", "sp-179", "sp-238", "sp-145", "sp-15", "sp-13", "sp-203", "sp-228", "sp-167", "sp-152", "sp-102",
					"sp-262", "sp-80", "sp-22", "sp-114", "sp-30", "sp-261", "sp-34", "sp-157", "sp-95", "sp-77", "sp-271", "sp-231", "sp-121", "sp-119",
					"sp-73", "sp-39", "sp-1", "sp-113", "sp-217", "sp-58", "sp-134", "sp-76", "sp-209", "sp-154", "sp-182", "sp-164", "sp-43", "sp-269",
					"sp-54", "sp-75", "sp-219", "sp-195", "sp-20", "sp-60", "sp-126", "sp-57", "sp-276", "sp-94", "sp-32", "sp-66", "sp-146", "sp-82",
					"sp-266", "sp-29", "sp-186", "sp-10", "sp-96", "sp-68", "sp-260", "sp-51", "sp-149", "sp-241", "sp-0", "sp-188", "sp-127", "sp-273",
					"sp-205", "sp-174", "sp-19", "sp-183", "sp-27", "sp-251", "sp-81", "sp-108", "sp-18", "sp-151", "sp-194", "sp-150", "sp-208", "sp-111",
					"sp-161", "sp-105", "sp-199", "sp-254", "sp-63", "sp-103", "sp-116", "sp-224", "sp-232", "sp-158", "sp-143", "sp-99", "sp-170", "sp-259",
					"sp-117", "sp-245", "sp-44", "sp-185", "sp-104", "sp-52", "sp-92", "sp-138", "sp-270", "sp-256", "sp-79", "sp-35", "sp-212", "sp-176",
					"sp-171", "sp-264", "sp-124", "sp-155", "sp-229", "sp-140", "sp-234", "sp-123", "sp-272", "sp-144", "sp-11", "sp-223", "sp-250" };
			String split2[] = { "sp-36", "sp-129", "sp-136", "sp-274", "sp-139", "sp-220", "sp-197", "sp-278", "sp-41", "sp-132", "sp-42", "sp-275", "sp-69",
					"sp-120", "sp-106", "sp-86", "sp-131", "sp-267", "sp-190", "sp-163", "sp-236", "sp-221", "sp-265", "sp-101", "sp-215", "sp-125", "sp-46",
					"sp-23", "sp-277", "sp-237", "sp-24", "sp-252", "sp-130", "sp-85", "sp-91", "sp-210", "sp-181", "sp-222", "sp-213", "sp-168", "sp-263",
					"sp-100", "sp-87", "sp-6", "sp-50", "sp-253", "sp-133", "sp-93", "sp-189", "sp-89", "sp-196", "sp-33", "sp-206", "sp-178", "sp-61",
					"sp-175", "sp-177", "sp-279", "sp-257", "sp-148", "sp-244", "sp-226", "sp-268", "sp-56", "sp-67", "sp-207", "sp-202", "sp-258", "sp-49",
					"sp-214", "sp-112", "sp-78", "sp-31", "sp-162", "sp-218", "sp-5", "sp-235", "sp-169", "sp-173", "sp-211", "sp-55", "sp-227", "sp-141",
					"sp-97", "sp-21", "sp-71", "sp-180", "sp-14", "sp-225", "sp-45", "sp-191", "sp-107", "sp-4", "sp-204", "sp-37", "sp-198", "sp-246",
					"sp-88", "sp-48", "sp-109", "sp-233", "sp-135", "sp-172", "sp-159", "sp-243", "sp-3", "sp-118", "sp-216", "sp-122", "sp-2", "sp-47",
					"sp-40", "sp-8", "sp-25", "sp-28", "sp-65", "sp-247", "sp-72", "sp-201", "sp-110", "sp-192", "sp-255", "sp-142", "sp-59", "sp-115",
					"sp-53", "sp-74", "sp-240", "sp-98", "sp-165", "sp-184", "sp-156", "sp-70", "sp-38", "sp-64", "sp-62", "sp-193" };
			List<FeatureTerm> l = new LinkedList<FeatureTerm>();
			for (String id : split1)
				l.add(ts.getCaseByName(id));
			individual_training_sets.add(l);
			l = new LinkedList<FeatureTerm>();
			for (String id : split2)
				l.add(ts.getCaseByName(id));
			individual_training_sets.add(l);

			FeatureTerm solution = individual_training_sets.get(1).get(1).readPath(ts.solution_path);
			System.out.println("Target class: " + solution.toStringNOOS(dm));

			// WAMAIL:
			System.out.println(" *** WAMAIL ***");
			// List<RuleHypothesis> l_h = WAMAIL.WAMAIL(individual_training_sets, solution, o, dm, ts.description_path,
			// ts.solution_path, WAMAIL.INDIVIDUAL_CORRECTED, FTRefinement.ALL_REFINEMENTS);
			List<RuleHypothesis> l_h = WAMAIL.WAMAIL(individual_training_sets, solution, o, dm, ts.description_path, ts.solution_path,
					WAMAIL.INDIVIDUAL_EXPANDED, FTRefinement.ALL_REFINEMENTS);
			// List<RuleHypothesis> l_h = WAMAIL.WAMAIL(individual_training_sets, solution, o, dm, ts.description_path,
			// ts.solution_path, WAMAIL.UNIFIED, FTRefinement.ALL_REFINEMENTS);

			System.out.println(" --- WAMAIL STATISTICS: --------------------");
			System.out.println("Max rounds: " + WAMAIL.max_n_rounds);
			System.out.println("Max AF size: " + WAMAIL.max_af_size);
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
			current[0] += res[0];
			current[1] += res[1];
			current[2] += res[2];
			current[3] += res[3];
		}
	}

	// This method returns the confusion matrix: [tp,tn, fp, fn]
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
	 * @return the int[]
	 * @throws Exception
	 *             the exception
	 */
	static int[] evaluateHypotheses(RuleHypothesis h, FeatureTerm solution, List<FeatureTerm> test_set, FTKBase dm, Path dp, Path sp) throws Exception {
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

		return new int[] { positive_covered, negative - negative_covered, negative_covered, positive - positive_covered };
	}
}
