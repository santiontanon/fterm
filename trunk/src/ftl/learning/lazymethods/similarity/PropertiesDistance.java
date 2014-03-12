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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import ftl.base.core.Disintegration;
import ftl.base.core.FTKBase;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Ontology;
import ftl.base.core.Path;
import ftl.base.utils.FeatureTermException;
import ftl.base.utils.Pair;

// TODO: Auto-generated Javadoc
/**
 * The Class PropertiesDistance.
 */
public class PropertiesDistance extends Distance {

	/** The DEBUG. */
	public static int DEBUG = 0;

	/** The s_cache. */
	static boolean s_cache = true;

	/** The m_fast. */
	boolean m_fast = false;

	/** The descriptions. */
	List<FeatureTerm> descriptions = new LinkedList<FeatureTerm>();

	/** The m_property weight. */
	protected List<Pair<FeatureTerm, Double>> m_propertyWeight = null;;

	/** The property_cache. */
	HashMap<FeatureTerm, HashSet<FeatureTerm>> property_cache = new HashMap<FeatureTerm, HashSet<FeatureTerm>>();

	/**
	 * Instantiates a new properties distance.
	 */
	public PropertiesDistance() {
		m_propertyWeight = new LinkedList<Pair<FeatureTerm, Double>>();
	}

	/**
	 * Instantiates a new properties distance.
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
	public PropertiesDistance(Collection<FeatureTerm> objects, FTKBase dm, Ontology o, Path dp, boolean fast) throws Exception {
		m_fast = fast;
		for (FeatureTerm obj : objects) {
			descriptions.add(obj.readPath(dp));
		}
		generateAllProperties(descriptions, dm, o);
	}

	/**
	 * Instantiates a new properties distance.
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
	public PropertiesDistance(List<FeatureTerm> objects, FTKBase dm, Ontology o, boolean fast) throws Exception {
		m_fast = fast;
		generateAllProperties(objects, dm, o);
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
	 * @throws Exception
	 *             the exception
	 */
	void generateAllProperties(List<FeatureTerm> objects, FTKBase dm, Ontology o) throws Exception {
		int count = 0;
		long start = System.currentTimeMillis();
		Integer max_properties = null;
		Integer min_properties = null;
		m_propertyWeight = new LinkedList<Pair<FeatureTerm, Double>>();

		// Generate all the properties
		for (FeatureTerm object : objects) {

			if (DEBUG >= 1)
				System.out.println("processing " + object.getName() + " (" + count + ")");
			List<FeatureTerm> properties_tmp = Disintegration.disintegrate(object, dm, o, s_cache, m_fast);
			long start_time = System.currentTimeMillis();

			if (max_properties == null || properties_tmp.size() > max_properties)
				max_properties = properties_tmp.size();
			if (min_properties == null || properties_tmp.size() < min_properties)
				min_properties = properties_tmp.size();

			for (FeatureTerm property : properties_tmp) {
				boolean duplicate = false;

				for (Pair<FeatureTerm, Double> p_w : m_propertyWeight) {
					if (property.equivalents(p_w.m_a)) {
						duplicate = true;
						break;
					}
				}

				if (!duplicate) {
					m_propertyWeight.add(new Pair<FeatureTerm, Double>(property, 1.0));
				}
			}

			long time = System.currentTimeMillis();
			if (DEBUG >= 1)
				System.out.println("Filtering time: " + (time - start_time));

			count++;

			// if (count>=10) break;
		}

		// The weights will be all 1 in this distance:
		if (DEBUG >= 1)
			System.out.println("Properties per term: [" + min_properties + " - " + max_properties + "]");
		if (DEBUG >= 1)
			System.out.println(m_propertyWeight.size() + " properties (in " + (System.currentTimeMillis() - start) + "ms)");

	}

	/**
	 * Gets the property cache.
	 * 
	 * @param f1
	 *            the f1
	 * @return the property cache
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public HashSet<FeatureTerm> getPropertyCache(FeatureTerm f1) throws FeatureTermException {
		HashSet<FeatureTerm> cache1 = property_cache.get(f1);

		if (cache1 == null) {
			if (DEBUG >= 1)
				System.out.println("getPropertyCache: new case, testinc against " + m_propertyWeight.size() + " properties.");
			cache1 = new HashSet<FeatureTerm>();
			for (Pair<FeatureTerm, Double> p_w : m_propertyWeight)
				if (p_w.m_a.subsumes(f1))
					cache1.add(p_w.m_a);
			property_cache.put(f1, cache1);
		}

		return cache1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csic.iiia.ftl.learning.lazymethods.similarity.Distance#distance(csic.iiia.ftl.base.core.FeatureTerm,
	 * csic.iiia.ftl.base.core.FeatureTerm, csic.iiia.ftl.base.core.Ontology, csic.iiia.ftl.base.core.FTKBase)
	 */
	public double distance(FeatureTerm f1, FeatureTerm f2, Ontology o, FTKBase dm) throws Exception {
		double shared = 0;
		double f1_not_shared = 0;
		double f2_not_shared = 0;

		if (m_propertyWeight == null || m_propertyWeight.size() == 0) {
			generateAllProperties(descriptions, dm, o);
		}

		HashSet<FeatureTerm> cache1 = getPropertyCache(f1);
		HashSet<FeatureTerm> cache2 = getPropertyCache(f2);

		for (Pair<FeatureTerm, Double> p_w : m_propertyWeight) {
			if (p_w.m_b > 0) {
				if (cache1.contains(p_w.m_a)) {
					if (cache2.contains(p_w.m_a)) {
						shared += p_w.m_b;
					} else {
						f1_not_shared += p_w.m_b;
					}
				} else {
					if (cache2.contains(p_w.m_a)) {
						f2_not_shared += p_w.m_b;
					} else {
						// none of them have it!
						// should we count it as a similarity???
					}
				}
			}
		}

		double tmp = ((double) (shared * 2 + f1_not_shared + f2_not_shared));
		double distance = (tmp > 0 ? 1.0f - (((double) (shared * 2)) / tmp) : 1.0);

		return distance;
	}
}
