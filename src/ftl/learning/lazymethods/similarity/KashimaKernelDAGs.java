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
  
 package ftl.learning.lazymethods.similarity;

import java.util.LinkedList;
import java.util.List;

import ftl.base.core.FTKBase;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Ontology;
import ftl.base.utils.FeatureTermException;
import ftl.base.utils.Pair;

// TODO: Auto-generated Javadoc
/**
 * The Class KashimaKernelDAGs.
 * 
 * @author santi
 */
public class KashimaKernelDAGs extends Distance {

	/** The DEBUG. */
	static int DEBUG = 0;

	/** The q. */
	double q = 0.1;

	/**
	 * Instantiates a new kashima kernel da gs.
	 */
	public KashimaKernelDAGs() {

	}

	/**
	 * Instantiates a new kashima kernel da gs.
	 * 
	 * @param a_q
	 *            the a_q
	 */
	public KashimaKernelDAGs(double a_q) {
		q = a_q;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csic.iiia.ftl.learning.lazymethods.similarity.Distance#distance(csic.iiia.ftl.base.core.FeatureTerm,
	 * csic.iiia.ftl.base.core.FeatureTerm, csic.iiia.ftl.base.core.Ontology, csic.iiia.ftl.base.core.FTKBase)
	 */
	public double distance(FeatureTerm f1, FeatureTerm f2, Ontology o, FTKBase dm) throws Exception {
		Graph g1 = new Graph(f1, dm);
		Graph g2 = new Graph(f2, dm);
		return 1.0 - similarity(g1, g2, q, dm);
	}

	/**
	 * Distance.
	 * 
	 * @param f1
	 *            the f1
	 * @param f2
	 *            the f2
	 * @param dm
	 *            the dm
	 * @param pq
	 *            the pq
	 * @return the double
	 * @throws Exception
	 *             the exception
	 */
	public static double distance(FeatureTerm f1, FeatureTerm f2, FTKBase dm, double pq) throws Exception {
		Graph g1 = new Graph(f1, dm);
		Graph g2 = new Graph(f2, dm);
		return 1.0 - similarity(g1, g2, pq, dm);
	}

	/**
	 * Similarity.
	 * 
	 * @param f1
	 *            the f1
	 * @param f2
	 *            the f2
	 * @param pq
	 *            the pq
	 * @param dm
	 *            the dm
	 * @return the double
	 * @throws Exception
	 *             the exception
	 */
	public static double similarity(FeatureTerm f1, FeatureTerm f2, double pq, FTKBase dm) throws Exception {
		Graph g1 = new Graph(f1, dm);
		Graph g2 = new Graph(f2, dm);
		return similarity(g1, g2, pq, dm);
	}

	// root = true means that all the random walks start from the root
	/**
	 * Similarity.
	 * 
	 * @param g1
	 *            the g1
	 * @param g2
	 *            the g2
	 * @param pq
	 *            the pq
	 * @param dm
	 *            the dm
	 * @return the double
	 * @throws Exception
	 *             the exception
	 */
	public static double similarity(Graph g1, Graph g2, double pq, FTKBase dm) throws Exception {
		List<Pair<List<Object>, Double>> walks1 = new LinkedList<Pair<List<Object>, Double>>();
		List<Pair<List<Object>, Double>> walks2 = new LinkedList<Pair<List<Object>, Double>>();

		for (int i = 0; i < g1.m_n_nodes; i++)
			walks1.addAll(walksStartingFrom(i, g1, pq));
		for (int i = 0; i < g2.m_n_nodes; i++)
			walks2.addAll(walksStartingFrom(i, g2, pq));
		for (Pair<List<Object>, Double> walk : walks1)
			walk.m_b /= g1.m_n_nodes;
		for (Pair<List<Object>, Double> walk : walks2)
			walk.m_b /= g2.m_n_nodes;

		/*
		 * System.out.println("walks: " + walks1.size() + " , " + walks2.size()); System.out.println("G1:");
		 * for(Pair<List<Integer>,Double> walk:walks1) { System.out.println(walk.m_b + " : " + walk.m_a); }
		 * System.out.println("G2:"); for(Pair<List<Integer>,Double> walk:walks2) { System.out.println(walk.m_b + " : "
		 * + walk.m_a); }
		 */

		double accum = 0;

		for (Pair<List<Object>, Double> walk1 : walks1) {
			for (Pair<List<Object>, Double> walk2 : walks2) {
				accum += walk1.m_b * walk2.m_b * walkSimilarity(walk1.m_a, walk2.m_a, g1, g2, dm);
			}
		}

		return accum;
	}

	/**
	 * Walk similarity.
	 * 
	 * @param walk1
	 *            the walk1
	 * @param walk2
	 *            the walk2
	 * @param g1
	 *            the g1
	 * @param g2
	 *            the g2
	 * @param dm
	 *            the dm
	 * @return the double
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public static double walkSimilarity(List<Object> walk1, List<Object> walk2, Graph g1, Graph g2, FTKBase dm) throws FeatureTermException {
		int n = walk1.size();
		if (n != walk2.size())
			return 0;

		double sym = 1;
		double tmp = 0;
		for (int i = 0; i < n; i++) {
			tmp = KashimaKernel.labelSimilarity(walk1.get(i), walk2.get(i), dm);
			if (tmp == 0)
				return 0;
			sym *= tmp;
		}

		return sym;
	}

	/**
	 * Walks starting from.
	 * 
	 * @param node
	 *            the node
	 * @param g1
	 *            the g1
	 * @param q
	 *            the q
	 * @return the list
	 * @throws Exception
	 *             the exception
	 */
	public static List<Pair<List<Object>, Double>> walksStartingFrom(int node, Graph g1, double q) throws Exception {
		int ns = g1.successors(node);
		double ps = 1;

		if (ns > 0)
			ps = (1.0 - q) / ns;

		List<Pair<List<Object>, Double>> walks = new LinkedList<Pair<List<Object>, Double>>();
		for (int i = 0; i < ns; i++) {
			int next = g1.ithSuccessor(node, i);
			List<Pair<List<Object>, Double>> l = walksStartingFrom(next, g1, q);
			for (Pair<List<Object>, Double> walk : l) {
				walk.m_a.add(0, g1.m_label_dictionary.get(g1.m_edges[node][next]));
				walk.m_a.add(0, g1.m_label_dictionary.get(g1.m_node_labels[node]));
				walk.m_b *= ps;
			}
			walks.addAll(l);
		}
		List<Object> walk = new LinkedList<Object>();
		walk.add(g1.m_label_dictionary.get(g1.m_node_labels[node]));
		if (ns == 0) {
			walks.add(new Pair<List<Object>, Double>(walk, 1.0));
		} else {
			walks.add(new Pair<List<Object>, Double>(walk, q));
		}

		return walks;
	}
}
