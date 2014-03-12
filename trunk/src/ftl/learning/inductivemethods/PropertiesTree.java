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
  
 package ftl.learning.inductivemethods;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import ftl.base.core.Disintegration;
import ftl.base.core.FTKBase;
import ftl.base.core.FTRefinement;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Ontology;
import ftl.base.core.Path;
import ftl.base.utils.FeatureTermException;
import ftl.learning.core.Hypothesis;
import ftl.learning.core.InductiveLearner;
import ftl.learning.core.InformationMeasurement;

// TODO: Auto-generated Javadoc
/**
 * The Class PropertiesTree.
 * 
 * @author santi
 */
public class PropertiesTree extends InductiveLearner {

        /** Amount of debug messages printed by this class. */
        public static int DEBUG = 0;
    
	/** Fast disintegration or formal disintegration. */
	boolean m_fast = false; // 

	/** The last property set. */
	public List<FeatureTerm> lastPropertySet = null;

	/**
	 * Instantiates a new properties tree.
	 * 
	 * @param fast
	 *            the fast
	 */
	public PropertiesTree(boolean fast) {
		m_fast = fast;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csic.iiia.ftl.learning.core.InductiveLearner#generateHypothesis(java.util.List,
	 * csic.iiia.ftl.base.core.Path, csic.iiia.ftl.base.core.Path, csic.iiia.ftl.base.core.Ontology,
	 * csic.iiia.ftl.base.core.FTKBase)
	 */
	public Hypothesis generateHypothesis(List<FeatureTerm> examples, Path dp, Path sp, Ontology o, FTKBase dm) throws Exception {
		PatternTreeHypothesis h = new PatternTreeHypothesis();

		// Generate properties:
		List<FeatureTerm> descriptions = new LinkedList<FeatureTerm>();
		for (FeatureTerm e : examples)
			descriptions.add(e.readPath(dp));
		List<FeatureTerm> properties = generateAllProperties(descriptions, dm, o);
		lastPropertySet = properties;

		// Build the tree:
                long time1 = System.currentTimeMillis();
		h.m_root = generateTree(examples, dp, sp, properties, h, dm, o);
                long time2 = System.currentTimeMillis();
                if (DEBUG>=1) System.out.println("Time to learn: " + (time2 - time1));
                
		return h;
	}

	/**
	 * Generate hypothesis.
	 * 
	 * @param examples
	 *            the examples
	 * @param percetageToDisintegrate
	 *            the percetage to disintegrate
	 * @param dp
	 *            the dp
	 * @param sp
	 *            the sp
	 * @param o
	 *            the o
	 * @param dm
	 *            the dm
	 * @return the hypothesis
	 * @throws Exception
	 *             the exception
	 */
	public Hypothesis generateHypothesis(List<FeatureTerm> examples, float percetageToDisintegrate, Path dp, Path sp, Ontology o, FTKBase dm) throws Exception {
		PatternTreeHypothesis h = new PatternTreeHypothesis();

		// Generate properties:
		List<FeatureTerm> descriptions = new LinkedList<FeatureTerm>();

		Random r = new Random();
		int n = Math.max((int) (examples.size() * percetageToDisintegrate), 1);
		List<FeatureTerm> toDisintegrate = new LinkedList<FeatureTerm>();

		toDisintegrate.addAll(examples);
		while (toDisintegrate.size() > n)
			toDisintegrate.remove(r.nextInt(toDisintegrate.size()));

		for (FeatureTerm e : toDisintegrate)
			descriptions.add(e.readPath(dp));

		List<FeatureTerm> properties = generateAllProperties(descriptions, dm, o);
		lastPropertySet = properties;

		// Build the tree:
		h.m_root = generateTree(examples, dp, sp, properties, h, dm, o);

		return h;
	}

	/**
	 * Generate tree.
	 * 
	 * @param examples
	 *            the examples
	 * @param dp
	 *            the dp
	 * @param sp
	 *            the sp
	 * @param properties
	 *            the properties
	 * @param h
	 *            the h
	 * @param dm
	 *            the dm
	 * @param o
	 *            the o
	 * @return the pattern tree hypothesis. pattern tree node
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	private PatternTreeHypothesis.PatternTreeNode generateTree(List<FeatureTerm> examples, Path dp, Path sp, List<FeatureTerm> properties,
			PatternTreeHypothesis h, FTKBase dm, Ontology o) throws FeatureTermException {
		System.out.println("PropertiesTree.generateTree: " + examples.size() + " examples and " + properties.size() + " properties.");
		HashMap<FeatureTerm, Integer> distribution = Hypothesis.distribution(examples, sp);

		// Find the best property:
		FeatureTerm best_property = null;
		float best_heuristic = 0;

		if (distribution.size() > 1) {
			for (FeatureTerm p : properties) {
				float heuristic = evaluateProperty(p, examples, dp, sp);

				if (best_property == null || heuristic >= best_heuristic) {
					if (heuristic == best_heuristic) {
						// Resolve ties by generality (biased towards using general patterns):
						int s1 = FTRefinement.depth(best_property, dm, o);
						int s2 = FTRefinement.depth(p, dm, o);

						if (s2 < s1) {
							best_property = p;
							best_heuristic = heuristic;

							// System.out.println(heuristic + " : ");
							// System.out.println(p.toStringNOOS(dm));

						}
					} else {
						best_property = p;
						best_heuristic = heuristic;

						// System.out.println(heuristic + " : ");
						// System.out.println(p.toStringNOOS(dm));
					}
				}
			}
		}

		// If any, create node and call recursively:
		if (best_property != null) {
			PatternTreeHypothesis.PatternTreeNode n = h.new PatternTreeNode(best_property);
			n.m_distribution = distribution;

			List<FeatureTerm> remainingExamplesPositive = new LinkedList<FeatureTerm>();
			List<FeatureTerm> remainingExamplesNegative = new LinkedList<FeatureTerm>();
			List<FeatureTerm> remainingPropertiesPositive = new LinkedList<FeatureTerm>();
			List<FeatureTerm> remainingPropertiesNegative = new LinkedList<FeatureTerm>();

			for (FeatureTerm e : examples) {
				FeatureTerm d = e.readPath(dp);
				if (best_property.subsumes(d))
					remainingExamplesPositive.add(e);
				else
					remainingExamplesNegative.add(e);
			}

			for (FeatureTerm p : properties) {
				if (p.subsumes(best_property)) {
					if (best_property.subsumes(p)) {
						// remainingPropertiesPositive.add(p);
						// remainingPropertiesNegative.add(p);
					} else {
						// remainingPropertiesPositive.add(p);
						remainingPropertiesNegative.add(p);
					}
				} else {
					if (best_property.subsumes(p)) {
						remainingPropertiesPositive.add(p);
						// remainingPropertiesNegative.add(p);
					} else {
						remainingPropertiesPositive.add(p);
						remainingPropertiesNegative.add(p);
					}
				}
			}

			if (remainingExamplesPositive.size() > 0)
				n.m_positiveChild = generateTree(remainingExamplesPositive, dp, sp, remainingPropertiesPositive, h, dm, o);
			if (remainingExamplesNegative.size() > 0)
				n.m_negativeChild = generateTree(remainingExamplesNegative, dp, sp, remainingPropertiesNegative, h, dm, o);
			return n;
		} else {
			PatternTreeHypothesis.PatternTreeNode n = h.new PatternTreeNode(best_property);
			n.m_distribution = distribution;
			return n;
		}
	}

	/**
	 * Generate all properties.
	 * 
	 * @param objects
	 *            the objects
	 * @param dm
	 *            the dm
	 * @param o
	 *            the o
	 * @return the list
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws Exception
	 *             the exception
	 */
	private List<FeatureTerm> generateAllProperties(List<FeatureTerm> objects, FTKBase dm, Ontology o) throws FeatureTermException, Exception {
		int count = 0;
		List<FeatureTerm> properties = new LinkedList<FeatureTerm>();

		// Generate all the properties
		for (FeatureTerm object : objects) {
			System.out.println("PropertiesTree.generateAllProperties: processing " + count + " -> " + object.getName());
			// System.out.println(object.toStringNOOS(dm));

			List<FeatureTerm> properties_tmp = null;
			properties_tmp = Disintegration.disintegrate(object, dm, o, true, m_fast);

			System.out.println(properties_tmp.size() + " found, now filtering... (previous total: " + properties.size());

			for (FeatureTerm property : properties_tmp) {
				boolean duplicate = false;

				for (FeatureTerm p : properties) {
					if (property.equivalents(p)) {
						duplicate = true;
						break;
					}
				}

				if (!duplicate) {
					properties.add(property);
				}
			}

			count++;
		}
		return properties;
	}

	/**
	 * Evaluate property.
	 * 
	 * @param p
	 *            the p
	 * @param examples
	 *            the examples
	 * @param dp
	 *            the dp
	 * @param sp
	 *            the sp
	 * @return the float
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	private float evaluateProperty(FeatureTerm p, List<FeatureTerm> examples, Path dp, Path sp) throws FeatureTermException {
		HashMap<FeatureTerm, Integer> distribution = Hypothesis.distribution(examples, sp);

		List<FeatureTerm> positive = new LinkedList<FeatureTerm>();
		List<FeatureTerm> negative = new LinkedList<FeatureTerm>();

		for (FeatureTerm e : examples) {
			FeatureTerm d = e.readPath(dp);
			if (p.subsumes(d))
				positive.add(e);
			else
				negative.add(e);
		}
		HashMap<FeatureTerm, Integer> positiveDistribution = Hypothesis.distribution(positive, sp);
		HashMap<FeatureTerm, Integer> negativeDistribution = Hypothesis.distribution(negative, sp);

		float h1 = InformationMeasurement.entropyHash(distribution);
		float h2 = InformationMeasurement.entropyHash(positiveDistribution);
		float h3 = InformationMeasurement.entropyHash(negativeDistribution);

		float gain = h1 - ((((float) (positive.size())) / ((float) (examples.size()))) * h2 + (((float) (negative.size())) / ((float) (examples.size()))) * h3);

		// System.out.println("EvaluateProperty: " + gain + " = " + h1 + " - ( " + (((float) (positive.size())) /
		// ((float) (examples.size()))) + " * "+ h2 + " ) + ( " +
		// (((float) (negative.size())) / ((float) (examples.size()))) + " * "+ h3 + " )");
		// System.out.println(distribution);
		// System.out.println(positiveDistribution);
		// System.out.println(negativeDistribution);

		return gain;
	}

}
