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
import ftl.base.core.Sort;
import ftl.base.utils.FeatureTermException;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;

// TODO: Auto-generated Javadoc
/**
 * The Class KashimaKernelSparse.
 * 
 * @author santi
 */
public class KashimaKernelSparse extends Distance {

	/** The DEBUG. */
	public static int DEBUG = 0;

	/** The q. */
	public double q = 0.1;

	/**
	 * Instantiates a new kashima kernel sparse.
	 */
	public KashimaKernelSparse() {

	}

	/**
	 * Instantiates a new kashima kernel sparse.
	 * 
	 * @param a_q
	 *            the a_q
	 */
	public KashimaKernelSparse(double a_q) {
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
		return 1.0 - similarity(g1, g2, q, false, dm);
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
		return 1.0 - similarity(g1, g2, pq, false, dm);
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
	 * @param root
	 *            the root
	 * @param dm
	 *            the dm
	 * @return the double
	 * @throws Exception
	 *             the exception
	 */
	public static double similarity(FeatureTerm f1, FeatureTerm f2, double pq, boolean root, FTKBase dm) throws Exception {
		Graph g1 = new Graph(f1, dm);
		Graph g2 = new Graph(f2, dm);
		return similarity(g1, g2, pq, root, dm);
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
	 * @param root
	 *            the root
	 * @param dm
	 *            the dm
	 * @return the double
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public static double similarity(Graph g1, Graph g2, double pq, boolean root, FTKBase dm) throws FeatureTermException {
		int n1 = g1.m_n_nodes, n2 = g2.m_n_nodes;
		double[][] b = new double[1][n1 * n2];
		double[][] r1 = new double[n1 * n2][1];
		// double []rinfinity = new double[n1*n2];
		double[][] t = new double[n1 * n2][n1 * n2];
		double[][] ImT = new double[n1 * n2][n1 * n2];

		// compute 'b' matrix:
		if (DEBUG >= 1)
			System.out.println("b matrix:");
		if (root) {
			b[0][0] = labelSimilarity(g1.label(g1.m_node_labels[0]), g2.label(g2.m_node_labels[0]), dm);
		} else {
			for (int i = 0; i < n1; i++) {
				for (int j = 0; j < n2; j++) {
					b[0][i * n2 + j] = labelSimilarity(g1.label(g1.m_node_labels[i]), g2.label(g2.m_node_labels[j]), dm) / (n1 * n2);
					if (DEBUG >= 1 && b[0][i * n2 + j] != 0)
						System.out.println(i + "," + j + " -> " + b[0][i * n2 + j]);
				}
			}
		}

		// compute 'q' and 'r1' matrices (they are the same):
		if (DEBUG >= 1)
			System.out.println("q matrix:");
		for (int i = 0; i < n1; i++) {
			for (int j = 0; j < n2; j++) {
				double pq1 = 1;
				double pq2 = 1;
				if (g1.successors(i) != 0)
					pq1 = pq;
				if (g2.successors(j) != 0)
					pq2 = pq;
				r1[i * n2 + j][0] = pq1 * pq2;
				if (DEBUG >= 1 && r1[i * n2 + j][0] != 0)
					System.out.println(i + "," + j + " -> " + r1[i * n2 + j][0]);
			}
		}

		// compute the 't' matrix:
		if (DEBUG >= 1)
			System.out.println("t matrix:");
		for (int i = 0; i < n1; i++) {
			for (int j = 0; j < n2; j++) {
				for (int ip = 0; ip < n1; ip++) {
					for (int jp = 0; jp < n2; jp++) {
						double pt1 = 0; // transition probability in g1
						double pt2 = 0; // transition probability in g2
						double vs = 0; // vertex similarity
						double es = 0; // edge similarity

						if (g1.m_edges[ip][i] != 0) {
							int ns1 = g1.successors(ip);
							pt1 = (1.0 - pq) / ns1;
						}
						if (g2.m_edges[jp][j] != 0) {
							int ns2 = g2.successors(jp);
							pt2 = (1.0 - pq) / ns2;
						}

						int vl1 = g1.m_node_labels[i];
						int vl2 = g2.m_node_labels[j];
						int el1 = g1.m_edges[ip][i];
						int el2 = g2.m_edges[jp][j];
						if (el1 != 0 && el2 != 0) {
							vs = labelSimilarity(g1.m_label_dictionary.get(vl1), g2.m_label_dictionary.get(vl2), dm);
							es = labelSimilarity(g1.m_label_dictionary.get(el1), g2.m_label_dictionary.get(el2), dm);
							// System.out.println(i + "," + j + "," + ip + "," + jp + " -> " + "(" + pt1 + "," + pt2 +
							// "," + vs + "," + es + ") <- " + g1.m_label_dictionary.get(el1) + "," +
							// g2.m_label_dictionary.get(el2));
						}

						t[i * n2 + j][ip * n2 + jp] = pt1 * pt2 * vs * es;
						if (DEBUG >= 1 && t[i * n2 + j][ip * n2 + jp] != 0)
							System.out.println(i + "," + j + "," + ip + "," + jp + " -> " + t[i * n2 + j][ip * n2 + jp]);
					}
				}
			}
		}

		// compute the I - t matrix:
		for (int i = 0; i < n1 * n2; i++) {
			for (int j = 0; j < n1 * n2; j++) {
				double tmp = 0;
				if (i == j)
					tmp = 1;
				ImT[i][j] = tmp - t[i][j];
			}
		}

		// invert the "I - t" matrix:
		DoubleMatrix2D ImT_matrix = new SparseDoubleMatrix2D(ImT);
		System.out.println("inverting " + ImT_matrix.rows());
		Algebra a = new Algebra();
		DoubleMatrix2D ImT_matrix_inverse = a.inverse(ImT_matrix);
		DoubleMatrix2D r1_matrix = new SparseDoubleMatrix2D(r1);
		DoubleMatrix2D b_matrix = new SparseDoubleMatrix2D(b);

		DoubleMatrix2D tmp = multiplyMatrix(b_matrix, multiplyMatrix(a.transpose(ImT_matrix_inverse), r1_matrix));

		/*
		 * Formatter fmt = new Formatter(); System.out.println("r1: " + fmt.toString(r1_matrix));
		 * System.out.println("b: " + fmt.toString(b_matrix)); System.out.println("I - t: " + fmt.toString(ImT_matrix));
		 * System.out.println("(I - t)^(-1): " + fmt.toString(ImT_matrix_inverse)); //
		 * System.out.println("verification: " + fmt.toString(multiplyMatrix(ImT_matrix,ImT_matrix_inverse)));
		 * System.out.println("tmp1: " + fmt.toString(multiplyMatrix(ImT_matrix_inverse,r1_matrix)));
		 */

		// System.out.println(tmp.get(0,0));
		return tmp.get(0, 0);
	}

	/**
	 * Multiply matrix.
	 * 
	 * @param A
	 *            the a
	 * @param B
	 *            the b
	 * @return the double matrix2 d
	 */
	static DoubleMatrix2D multiplyMatrix(DoubleMatrix2D A, DoubleMatrix2D B) {
		DoubleMatrix2D C = new SparseDoubleMatrix2D(A.rows(), B.columns());
		A.zMult(B, C);
		return C;
	}

	/**
	 * Label similarity.
	 * 
	 * @param l1
	 *            the l1
	 * @param l2
	 *            the l2
	 * @param dm
	 *            the dm
	 * @return the double
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public static double labelSimilarity(Object l1, Object l2, FTKBase dm) throws FeatureTermException {
		List<Object> sl1 = labelSuperList(l1, dm);
		List<Object> sl2 = labelSuperList(l2, dm);
		int n = 0;
		int m = Math.min(sl1.size(), sl2.size());
		while (n < m) {
			if (sl1.get(n) != sl2.get(n)) {
				if (sl1.get(n) instanceof FeatureTerm && sl2.get(n) instanceof FeatureTerm) {
					FeatureTerm t1 = (FeatureTerm) sl1.get(n);
					FeatureTerm t2 = (FeatureTerm) sl2.get(n);

					if (!t1.isConstant() || !t2.isConstant() || !t1.equivalents(t2)) {
						break;
					}
				} else {
					break;
				}
			}
			n++;
		}

		if (DEBUG >= 2) {
			System.out.println(sl1);
			System.out.println(sl2);
			System.out.println("LK(" + l1 + "," + l2 + ") = " + (((double) n) * 2) / (sl1.size() + sl2.size()));
		}

		return (((double) n) * 2) / (sl1.size() + sl2.size());
	}

	/**
	 * Label super list.
	 * 
	 * @param current
	 *            the current
	 * @param dm
	 *            the dm
	 * @return the list
	 */
	public static List<Object> labelSuperList(Object current, FTKBase dm) {
		List<Object> superList = new LinkedList<Object>();

		// if it's a generic term, then the first is already its sort:
		if (current instanceof FeatureTerm) {
			FeatureTerm f = (FeatureTerm) current;
			if (!f.isConstant() && f.getName() == null && !dm.contains(f)) {
				current = f.getSort();
			}
		}

		do {
			superList.add(0, current);

			if (current instanceof FeatureTerm) {
				current = ((FeatureTerm) current).getSort();
			} else if (current instanceof Sort) {
				current = ((Sort) current).getSuper();
				if (((Sort) current).getSuper() == null)
					current = null; // do not include "any" in the count
				if (current != null && ((Sort) current).get().endsWith("ontology"))
					current = null; // also elimiate the fake sorts at the top of ontologies
			} else {
				current = null;
			}
		} while (current != null);

		return superList;
	}

}
