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
import java.util.Random;

import ftl.base.core.FTKBase;
import ftl.base.core.FTRefinement;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Symbol;
import ftl.base.utils.FeatureTermException;

// TODO: Auto-generated Javadoc
/**
 * The Class Graph.
 * 
 * @author santi
 */
public class Graph {

	/** The r. */
	static Random r = new Random();

	/** The m_n_nodes. */
	int m_n_nodes;

	/** The m_node_labels. */
	int m_node_labels[];

	/** The m_edges. */
	int m_edges[][]; // an edge label 0 is impossible, thus, in edges, 0 means no label (0 is always the label of a
						// vertex)

	/** The m_n_labels. */
	int m_n_labels;

	/** The m_label_dictionary. */
	List<Object> m_label_dictionary = new LinkedList<Object>();

	/**
	 * Instantiates a new graph.
	 * 
	 * @param f
	 *            the f
	 * @param dm
	 *            the dm
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public Graph(FeatureTerm f, FTKBase dm) throws FeatureTermException {
		List<FeatureTerm> vl = FTRefinement.variables(f);

		m_n_nodes = vl.size();
		m_node_labels = new int[m_n_nodes];
		m_edges = new int[m_n_nodes][m_n_nodes];
		for (int n = 0; n < m_n_nodes; n++) {
			FeatureTerm v = vl.get(n);
			m_node_labels[n] = label(v);

			for (Symbol fname : v.getSort().getFeatures()) {
				List<FeatureTerm> fvalues = v.featureValues(fname);
				for (FeatureTerm fvalue : fvalues) {
					m_edges[n][vl.indexOf(fvalue)] = label(fname);
				}
			}
		}
	}

	/**
	 * Label.
	 * 
	 * @param l
	 *            the l
	 * @return the int
	 */
	int label(FeatureTerm l) {
		int index = m_label_dictionary.indexOf(l);
		if (index != -1)
			return index;
		m_label_dictionary.add(l);
		return m_label_dictionary.size() - 1;
	}

	/**
	 * Label.
	 * 
	 * @param l
	 *            the l
	 * @return the int
	 */
	int label(Symbol l) {
		int index = m_label_dictionary.indexOf(l);
		if (index != -1)
			return index;
		m_label_dictionary.add(l);
		return m_label_dictionary.size() - 1;
	}

	/**
	 * Label.
	 * 
	 * @param l
	 *            the l
	 * @return the object
	 */
	Object label(int l) {
		return m_label_dictionary.get(l);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String tmp = "[ ";

		for (int n = 0; n < m_n_nodes; n++) {
			tmp += m_node_labels[n] + " ";
		}

		tmp += "]\n[ ";
		for (int n = 0; n < m_n_nodes; n++) {
			for (int n2 = 0; n2 < m_n_nodes; n2++) {
				tmp += "  " + m_edges[n][n2];
			}
			if (n == m_n_nodes - 1) {
				tmp += " ]";
			} else {
				tmp += "\n   ";
			}
		}

		return tmp;
	}

	/**
	 * Prints the dictionary.
	 * 
	 * @param dm
	 *            the dm
	 */
	public void printDictionary(FTKBase dm) {
		int n = 0;
		System.out.println("Dictionary:");
		for (Object l : m_label_dictionary) {
			if (l instanceof Symbol) {
				System.out.println(n + " - " + l);
			} else {
				FeatureTerm v = (FeatureTerm) l;
				if (dm.contains(v)) {
					System.out.println(n + " - " + v.toStringNOOS(dm));
				} else {
					if (v.getName() != null) {
						System.out.println(n + " - " + v.getName());
					} else {
						if (v.isConstant()) {
							System.out.println(n + " - " + v.toStringNOOS(dm));
						} else {
							System.out.println(n + " - " + v.getSort().get());
						}
					}
				}
			}
			n++;
		}
	}

	/**
	 * Successors.
	 * 
	 * @param node
	 *            the node
	 * @return the int
	 */
	int successors(int node) {
		int n = 0;
		for (int i = 0; i < m_n_nodes; i++) {
			if (m_edges[node][i] != 0)
				n++;
		}
		return n;
	}

	/**
	 * Ith successor.
	 * 
	 * @param node
	 *            the node
	 * @param ith
	 *            the ith
	 * @return the int
	 * @throws Exception
	 *             the exception
	 */
	int ithSuccessor(int node, int ith) throws Exception {
		for (int i = 0; i < m_n_nodes; i++) {
			if (m_edges[node][i] != 0) {
				if (ith == 0)
					return i;
				ith--;
			}
		}
		throw new Exception("Graph: There is no " + ith + "th successor to node " + node);
	}

	/**
	 * Random walk.
	 * 
	 * @param termination
	 *            the termination
	 * @return the list
	 * @throws Exception
	 *             the exception
	 */
	public List<Integer> randomWalk(double termination) throws Exception {
		List<Integer> walk = new LinkedList<Integer>();

		int node = r.nextInt(m_n_nodes);
		walk.add(m_node_labels[node]);

		do {
			int nsucc = successors(node);
			if (nsucc == 0)
				return walk;
			if (r.nextDouble() < termination)
				return walk;
			int next = ithSuccessor(node, r.nextInt(nsucc));
			walk.add(m_edges[node][next]);
			walk.add(m_node_labels[next]);
			node = next;
		} while (true);
	}
}
