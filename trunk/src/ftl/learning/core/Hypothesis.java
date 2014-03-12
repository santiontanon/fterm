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
  
 package ftl.learning.core;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import java.util.HashMap;

import ftl.base.core.FTKBase;
import ftl.base.core.FTRefinement;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Ontology;
import ftl.base.core.Path;
import ftl.base.utils.FeatureTermException;

// TODO: Auto-generated Javadoc
/**
 * The Class Hypothesis.
 */
public abstract class Hypothesis {

	/**
	 * Instantiates a new hypothesis.
	 */
	public Hypothesis() {
	}

	/**
	 * Generate prediction.
	 * 
	 * @param problem
	 *            the problem
	 * @param dm
	 *            the dm
	 * @param debug
	 *            the debug
	 * @return the prediction
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws Exception
	 *             the exception
	 */
	public abstract Prediction generatePrediction(FeatureTerm problem, FTKBase dm, boolean debug) throws FeatureTermException, Exception;

	/**
	 * Size.
	 * 
	 * @return the int
	 */
	public int size() {
		return 0;
	}

	/**
	 * To string.
	 * 
	 * @param dm
	 *            the dm
	 * @return the string
	 */
	public String toString(FTKBase dm) {
		return "Hypothesis\n";
	}

	/**
	 * To compact string.
	 * 
	 * @param dm
	 *            the dm
	 * @return the string
	 */
	public String toCompactString(FTKBase dm) {
		return "Hypothesis\n";
	}

	/**
	 * Different solutions.
	 * 
	 * @param examples
	 *            the examples
	 * @param sp
	 *            the sp
	 * @return the list
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public static List<FeatureTerm> differentSolutions(Collection<FeatureTerm> examples, Path sp) throws FeatureTermException {
		List<FeatureTerm> solutions = new LinkedList<FeatureTerm>();

		for (FeatureTerm ex : examples) {
			FeatureTerm s = ex.readPath(sp);
			if (!solutions.contains(s)) {
				solutions.add(s);
			}
		}

		return solutions;
	}

	/**
	 * Distribution.
	 * 
	 * @param examples
	 *            the examples
	 * @param sp
	 *            the sp
	 * @return the hash map
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public static HashMap<FeatureTerm, Integer> distribution(Collection<FeatureTerm> examples, Path sp) throws FeatureTermException {
		HashMap<FeatureTerm, Integer> solutions = new HashMap<FeatureTerm, Integer>();

		for (FeatureTerm ex : examples) {
			FeatureTerm s = ex.readPath(sp);
			if (solutions.get(s) == null) {
				solutions.put(s, 1);
			} else {
				solutions.put(s, solutions.get(s) + 1);
			}
		}

		return solutions;
	}

	/**
	 * Most common solution.
	 * 
	 * @param examples
	 *            the examples
	 * @param sp
	 *            the sp
	 * @return the feature term
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public static FeatureTerm mostCommonSolution(Collection<FeatureTerm> examples, Path sp) throws FeatureTermException {
		HashMap<FeatureTerm, Integer> solutions = new HashMap<FeatureTerm, Integer>();
		int max = 0;
		FeatureTerm maxSol = null;

		for (FeatureTerm ex : examples) {
			FeatureTerm s = ex.readPath(sp);
			if (solutions.get(s) == null) {
				solutions.put(s, 1);
			} else {
				solutions.put(s, solutions.get(s) + 1);
			}

			if (maxSol == null || solutions.get(s) > max) {
				maxSol = s;
				max = solutions.get(s);
			}
		}

		return maxSol;
	}

	/**
	 * Evaluate.
	 * 
	 * @param cases
	 *            the cases
	 * @param dm
	 *            the dm
	 * @param sp
	 *            the sp
	 * @param dp
	 *            the dp
	 * @param debug
	 *            the debug
	 * @return the float
	 * @throws Exception
	 *             the exception
	 */
	public float evaluate(Collection<FeatureTerm> cases, FTKBase dm, Path sp, Path dp, boolean debug) throws Exception {
		// Evaluate the hypothesis:
		float score = 0;
		int problems = 0;
		int max = 10000;

		for (FeatureTerm c : cases) {
			if (problems < max) {
				Prediction p;
				try {
					p = generatePrediction(c.readPath(dp), dm, debug);
					if (p != null) {
						// System.out.println("Problem " + problems);
						// System.out.println("Prediction: " + p.toString(dm));
					} else {
						System.err.println("Problem " + problems);
						System.err.println("No prediction found.");
					}

					if (debug) {
						System.out.println("Real Solution: " + c.readPath(sp).toStringNOOS(dm));
					}
					float localScore = p.getScore(c.readPath(sp));

					if (debug) {
						System.out.println("Score: " + localScore);
					}

					if (localScore == 0) {
						if (debug) {
							System.out.println("Problem that was failed:");
						}
						if (debug) {
							System.out.println(c.toStringNOOS(dm));
						}
					}

					score += localScore;
					problems++;
				} catch (FeatureTermException e) {
					e.printStackTrace();
				}
			}
		}

		// System.out.println("Final Score: " + ((score/problems)*100) + "% in " + problems + " problems");
		return ((score / problems) * 100);
	}

	/**
	 * Generalize pattern.
	 * 
	 * @param initialPattern
	 *            the initial pattern
	 * @param positive
	 *            the positive
	 * @param negative
	 *            the negative
	 * @param o
	 *            the o
	 * @param domain_model
	 *            the domain_model
	 * @return the feature term
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public static FeatureTerm generalizePattern(FeatureTerm initialPattern, List<FeatureTerm> positive, List<FeatureTerm> negative, Ontology o,
			FTKBase domain_model) throws FeatureTermException {
		List<FeatureTerm> l;
		boolean end, c1, c2;
		FeatureTerm pattern = initialPattern;

		// System.out.println("Generalizing: - " + positive.size() + " - " + negative.size());
		// System.out.println(pattern.toStringNOOS(domain_model));

		do {
			end = true;
			l = FTRefinement.getSomeGeneralizationsAggressive(pattern, domain_model, o);
			// System.out.println(l.size() + " generalizations... ");

			for (FeatureTerm f2 : l) {

				// if (!pattern.subsumes(f2)) {
				c1 = true;
				c2 = true;
				for (FeatureTerm d : positive) {
					if (!f2.subsumes(d)) {
						c1 = false;

						// System.out.println(f2.toStringNOOS(domain_model));
						// System.out.println("does not subsume");
						// System.out.println(d.toStringNOOS(domain_model));
						break;
					} else {
						// System.out.println("ok");
					}
				}
				// System.out.println("C1: " + c1);

				if (c1) {
					for (FeatureTerm d : negative) {
						if (f2.subsumes(d)) {
							c2 = false;
							break;
						} else {
							// System.out.println("ok");
						}
					}
					// System.out.println("C2: " + c2);
				}

				if (c1 && c2) {
					pattern = f2;
					end = false;
					// System.out.println("ok");
					// System.out.println(pattern.toStringNOOS(domain_model));
					// System.out.flush();
					break;
				} else {
					// System.out.println("fail");
				}
				// } else {
				// System.err.println("pattern:");
				// System.err.println(pattern.toStringNOOS(domain_model));
				// System.err.println("f2:");
				// System.err.println(f2.toStringNOOS(domain_model));
				// System.err.flush();
				// }
			}

			// System.out.println("Generalization:");
			// System.out.println(pattern.toStringNOOS(domain_model));
			// System.out.flush();
		} while (!end);

		// System.out.println("Generalization: done... (" + positive.size() + "," + negative.size() + ")");

		return pattern;
	}
}
