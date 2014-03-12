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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import ftl.base.core.FTKBase;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Ontology;
import ftl.base.core.Path;
import ftl.base.utils.Pair;
import ftl.learning.core.Prediction;
import ftl.learning.lazymethods.similarity.Distance;

// TODO: Auto-generated Javadoc
/**
 * The Class KNNCSA.
 */
public class KNNCSA {

	/**
	 * The Class SDComparator.
	 */
	static class SDComparator implements Comparator {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object arg0, Object arg1) {
			double o0 = ((Pair<Pair<FeatureTerm, FeatureTerm>, Double>) arg0).m_b;
			double o1 = ((Pair<Pair<FeatureTerm, FeatureTerm>, Double>) arg1).m_b;

			if (o0 > o1)
				return 1;
			else if (o0 < o1)
				return -1;
			else
				return 0;
		}

	}

	/**
	 * Predict.
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
	 * @param K
	 *            the k
	 * @param d
	 *            the d
	 * @return the prediction
	 * @throws Exception
	 *             the exception
	 */
	public static Prediction predict(FeatureTerm problem, List<FeatureTerm> cases, Path description_path, Path solution_path, Ontology o, FTKBase dm, int K,
			Distance d) throws Exception {
		List<Pair<Pair<FeatureTerm, FeatureTerm>, Double>> solutions_distances = new LinkedList<Pair<Pair<FeatureTerm, FeatureTerm>, Double>>();
		HashMap<FeatureTerm, List<Double>> K_closest_distances = new HashMap<FeatureTerm, List<Double>>();

		for (FeatureTerm c : cases) {
			FeatureTerm description = c.readPath(description_path);
			FeatureTerm solution = c.readPath(solution_path);
			double distance = d.distance(problem, description, o, dm);

			solutions_distances.add(new Pair<Pair<FeatureTerm, FeatureTerm>, Double>(new Pair<FeatureTerm, FeatureTerm>(c, solution), distance));
		}

		Collections.sort(solutions_distances, new KNNCSA.SDComparator());

		Prediction p = new Prediction();
		p.problem = problem;

		for (int i = 0; i < K; i++) {
			Pair<Pair<FeatureTerm, FeatureTerm>, Double> solution_distance = solutions_distances.get(i);

			System.out.println(solution_distance.m_a.m_a.getName() + " - " + solution_distance.m_a.m_b.toStringNOOS(dm) + " -> " + solution_distance.m_b);

			if (K_closest_distances.get(solution_distance.m_a.m_b) == null) {
				List<Double> tmp = new LinkedList<Double>();
				tmp.add(solution_distance.m_b);
				K_closest_distances.put(solution_distance.m_a.m_b, tmp);
			} else {
				K_closest_distances.get(solution_distance.m_a.m_b).add(solution_distance.m_b);
			}
		}

		FeatureTerm best_solution = null;
		double best_distance = 0.0;

		for (FeatureTerm solution : K_closest_distances.keySet()) {
			double average_distance = 0.0;

			for (Double dist : K_closest_distances.get(solution))
				average_distance += dist;
			average_distance /= K_closest_distances.get(solution).size();

			System.out.println(solution + " - " + average_distance);

			if (best_solution == null || average_distance < best_distance) {
				best_solution = solution;
				best_distance = average_distance;
			}
		}

		p.solutions.add(best_solution);
		p.justifications.put(best_solution, null);
		p.support.put(best_solution, 1);

		return p;
	}

	/*
	 * This method returns different predictions for different values of K at the same time
	 */
	/**
	 * Multiple predictions.
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
	 * @param Kl
	 *            the kl
	 * @param d
	 *            the d
	 * @return the list
	 * @throws Exception
	 *             the exception
	 */
	public static List<Prediction> multiplePredictions(FeatureTerm problem, List<FeatureTerm> cases, Path description_path, Path solution_path, Ontology o,
			FTKBase dm, List<Integer> Kl, Distance d) throws Exception {
		List<Pair<Pair<FeatureTerm, FeatureTerm>, Double>> solutions_distances = new LinkedList<Pair<Pair<FeatureTerm, FeatureTerm>, Double>>();

		for (FeatureTerm c : cases) {
			FeatureTerm description = c.readPath(description_path);
			FeatureTerm solution = c.readPath(solution_path);
			double distance = d.distance(problem, description, o, dm);

			solutions_distances.add(new Pair<Pair<FeatureTerm, FeatureTerm>, Double>(new Pair<FeatureTerm, FeatureTerm>(c, solution), distance));
		}

		Collections.sort(solutions_distances, new KNNCSA.SDComparator());
		List<Prediction> pl = new LinkedList<Prediction>();

		for (Integer K : Kl) {
			HashMap<FeatureTerm, List<Double>> K_closest_distances = new HashMap<FeatureTerm, List<Double>>();
			Prediction p = new Prediction();
			p.problem = problem;
			System.out.println(" K = " + K + " ------------------------- ");

			for (int i = 0; i < K; i++) {
				Pair<Pair<FeatureTerm, FeatureTerm>, Double> solution_distance = solutions_distances.get(i);

				System.out.println(solution_distance.m_a.m_a.getName() + " - " + solution_distance.m_a.m_b.toStringNOOS(dm) + " -> " + solution_distance.m_b);

				if (K_closest_distances.get(solution_distance.m_a.m_b) == null) {
					List<Double> tmp = new LinkedList<Double>();
					tmp.add(solution_distance.m_b);
					K_closest_distances.put(solution_distance.m_a.m_b, tmp);
				} else {
					K_closest_distances.get(solution_distance.m_a.m_b).add(solution_distance.m_b);
				}
			}

			FeatureTerm best_solution = null;
			double best_distance = 0.0;

			for (FeatureTerm solution : K_closest_distances.keySet()) {
				double average_distance = 0.0;

				for (Double dist : K_closest_distances.get(solution))
					average_distance += dist;
				average_distance /= K_closest_distances.get(solution).size();

				System.out.println(solution + " - " + average_distance);

				if (best_solution == null || average_distance < best_distance) {
					best_solution = solution;
					best_distance = average_distance;
				}
			}

			p.solutions.add(best_solution);
			p.justifications.put(best_solution, null);
			p.support.put(best_solution, 1);

			pl.add(p);
		}

		return pl;
	}

}
