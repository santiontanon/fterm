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
import ftl.learning.core.Hypothesis;
import ftl.learning.core.TrainingSetProperties;
import ftl.learning.core.TrainingSetUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class WABUITest.
 * 
 * @author santi
 */
public class WABUITest {

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String args[]) {
		try {
			Ontology base_ontology = new BaseOntology();
			Ontology o = new Ontology();
			FTKBase dm = new FTKBase();
			FTKBase case_base = new FTKBase();
			o.uses(base_ontology);
			case_base.uses(dm);

			dm.create_boolean_objects(o);
			TrainingSetProperties ts = TrainingSetUtils.loadTrainingSet(TrainingSetUtils.DEMOSPONGIAE_120_DATASET, o, dm, case_base);

			System.out.println("Data set loaded...");
			System.out.flush();

			List<FeatureTerm> allSolutions = Hypothesis.differentSolutions(ts.cases, ts.solution_path);

			HashMap<String, int[]> training_results = new HashMap<String, int[]>();
			HashMap<String, int[]> test_results = new HashMap<String, int[]>();

			List<List<FeatureTerm>> individual_training_sets = new LinkedList<List<FeatureTerm>>();
			String split1[] = { "sp-98", "sp-124", "sp-249", "sp-214", "sp-238", "sp-243", "sp-13", "sp-130", "sp-29", "sp-252", "sp-12", "sp-116", "sp-132",
					"sp-244", "sp-223", "sp-33", "sp-36", "sp-7", "sp-25", "sp-11", "sp-16", "sp-246", "sp-219", "sp-215", "sp-32", "sp-213", "sp-104",
					"sp-226", "sp-38", "sp-1", "sp-20", "sp-232", "sp-28", "sp-106", "sp-112", "sp-230", "sp-134", "sp-6", "sp-240", "sp-35", "sp-37", "sp-3",
					"sp-228", "sp-101", "sp-234", "sp-128", "sp-108", "sp-107", "sp-217", "sp-14", "sp-239", "sp-224", "sp-121", "sp-21", "sp-220", "sp-97",
					"sp-251", "sp-235", "sp-133", "sp-19", "sp-0" };
			String split2[] = { "sp-24", "sp-39", "sp-95", "sp-30", "sp-102", "sp-231", "sp-15", "sp-9", "sp-10", "sp-34", "sp-111", "sp-122", "sp-126",
					"sp-221", "sp-120", "sp-100", "sp-225", "sp-229", "sp-8", "sp-110", "sp-119", "sp-123", "sp-216", "sp-242", "sp-26", "sp-113", "sp-105",
					"sp-218", "sp-227", "sp-27", "sp-127", "sp-245", "sp-117", "sp-103", "sp-109", "sp-118", "sp-17", "sp-241", "sp-114", "sp-115", "sp-129",
					"sp-22", "sp-4", "sp-5", "sp-18", "sp-125", "sp-2", "sp-237", "sp-23", "sp-131", "sp-248", "sp-99", "sp-233", "sp-250", "sp-31", "sp-96",
					"sp-222", "sp-236", "sp-247" };
			List<FeatureTerm> l = new LinkedList<FeatureTerm>();
			for (String id : split1)
				l.add(ts.getCaseByName(id));
			individual_training_sets.add(l);
			l = new LinkedList<FeatureTerm>();
			for (String id : split2)
				l.add(ts.getCaseByName(id));
			individual_training_sets.add(l);

			FeatureTerm solution = individual_training_sets.get(1).get(0).readPath(ts.solution_path);
			System.out.println("Target class: " + solution.toStringNOOS(dm));

			for (FeatureTerm ft : individual_training_sets.get(0))
				System.out.print("\"" + ft.getName() + "\",");
			System.out.println("");
			for (FeatureTerm ft : individual_training_sets.get(1))
				System.out.print("\"" + ft.getName() + "\",");
			System.out.println("");

			// Generate rules with WABUI for agent 1:
			WArgumentationAgent a1 = new WArgumentationAgent("Ag1", individual_training_sets.get(0), solution, ts.description_path, ts.solution_path, o, dm,
					FTRefinement.ALL_REFINEMENTS);
			WArgumentationAgent a2 = new WArgumentationAgent("Ag2", individual_training_sets.get(1), solution, ts.description_path, ts.solution_path, o, dm,
					FTRefinement.ALL_REFINEMENTS);

			for (WeightedArgument arg : a1.m_hypothesis) {
				a1.examine(arg);
				a2.examine(arg);
				List<WeightedArgument> attacks = WABUI.attack(arg, null, individual_training_sets.get(1), ts.description_path, ts.solution_path, o, dm, "Ag2",
						FTRefinement.ALL_REFINEMENTS);
				for (WeightedArgument arg2 : attacks) {
					a1.examine(arg2);
					a2.examine(arg2);
					a1.m_af.addArgument(arg2);
				}
				if (!attacks.isEmpty()) {
					System.out.println("Argument " + arg.getID() + " can be attacked!!!");
					System.out.println("Inconsistency budget of " + arg.getID() + ": " + a1.m_af.inconsistencyBudget(arg));
					System.out.println("Argumentation state:\n" + a1.m_af.toString(dm));
					List<WeightedArgument> replacements = WABUI.replacement(arg, null, a1.m_af, a1.m_af.inconsistencyBudget(arg),
							individual_training_sets.get(0), ts.description_path, ts.solution_path, o, dm, "Ag1", FTRefinement.ALL_REFINEMENTS);
					System.out.println(replacements.size() + " replacements found:");
					for (WeightedArgument arg2 : replacements) {
						a1.examine(arg2);
						a2.examine(arg2);
						System.out.print("Replacement: ");
						for (ArgumentExaminationRecord aer : arg2.getExaminations()) {
							System.out.print(" (" + aer.m_a.m_a.m_agent + ":");
							for (FeatureTerm s : aer.m_histogram.keySet())
								System.out.print(" " + aer.m_histogram.get(s));
							System.out.print(")");
						}
						System.out.println("");

						if (arg2.m_a.equivalents(arg.m_a)) {
							System.out.println("Replacement is identical to original...");
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
