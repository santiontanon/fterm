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
  
 package ftl.learning.lazymethods.similarity;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import ftl.base.core.FTKBase;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Ontology;
import ftl.base.core.Path;
import ftl.base.utils.FeatureTermException;
import ftl.base.utils.Pair;
import ftl.learning.core.InformationMeasurement;

// TODO: Auto-generated Javadoc
/**
 * The Class WeightedPropertiesDistance.
 */
public class WeightedPropertiesDistance extends PropertiesDistance {

	/**
	 * Instantiates a new weighted properties distance.
	 */
	public WeightedPropertiesDistance() {
	}

	/**
	 * Instantiates a new weighted properties distance.
	 * 
	 * @param objects
	 *            the objects
	 * @param dm
	 *            the dm
	 * @param o
	 *            the o
	 * @param fast
	 *            the fast
	 * @throws Exception
	 *             the exception
	 */
	public WeightedPropertiesDistance(List<FeatureTerm> objects, FTKBase dm, Ontology o, boolean fast) throws Exception {
		super(objects, dm, o, fast);
	}

	/**
	 * Instantiates a new weighted properties distance.
	 * 
	 * @param objects
	 *            the objects
	 * @param dm
	 *            the dm
	 * @param o
	 *            the o
	 * @param dp
	 *            the dp
	 * @param fast
	 *            the fast
	 * @throws Exception
	 *             the exception
	 */
	public WeightedPropertiesDistance(List<FeatureTerm> objects, FTKBase dm, Ontology o, Path dp, boolean fast) throws Exception {
		super(objects, dm, o, dp, fast);
	}

	/**
	 * Compute weights.
	 * 
	 * @param cases
	 *            the cases
	 * @param sp
	 *            the sp
	 * @param dp
	 *            the dp
	 * @param dm
	 *            the dm
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public void computeWeights(List<FeatureTerm> cases, Path sp, Path dp, FTKBase dm) throws FeatureTermException {

		List<FeatureTerm> descriptions = new LinkedList<FeatureTerm>();
		List<FeatureTerm> solutions = new LinkedList<FeatureTerm>();
		List<FeatureTerm> different_solutions = new LinkedList<FeatureTerm>();

		for (FeatureTerm c : cases) {
			FeatureTerm solution = c.readPath(sp);
			if (!different_solutions.contains(solution))
				different_solutions.add(solution);

			descriptions.add(c.readPath(dp));
			solutions.add(solution);
		}

		for (Pair<FeatureTerm, Double> p_w : m_propertyWeight) {
			// Create the partition induced by the property:
			List<FeatureTerm> s1 = new LinkedList<FeatureTerm>();
			List<FeatureTerm> s2 = new LinkedList<FeatureTerm>();

			for (int i = 0; i < descriptions.size(); i++) {
				FeatureTerm d = descriptions.get(i);
				FeatureTerm s = solutions.get(i);

				HashSet<FeatureTerm> cache = getPropertyCache(d);
				// System.out.println("Cache has " + cache.size() + " properties.");
				if (cache.contains(p_w.m_a)) {
					s1.add(s);
				} else {
					s2.add(s);
				}
			}
			Pair<Float, Integer> tmp = InformationMeasurement.h_information_gain(solutions, s1, s2, different_solutions);
			p_w.m_b = (double) (tmp.m_a);
			// System.out.println(p_w.m_b + " [" + s1.size() + "," + s2.size() + "]" + " -> " +
			// p_w.m_a.toStringNOOS(dm));
		}

	}
}
