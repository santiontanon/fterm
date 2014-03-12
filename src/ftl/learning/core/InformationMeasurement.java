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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import java.util.HashMap;

import ftl.base.core.FeatureTerm;
import ftl.base.utils.FeatureTermException;
import ftl.base.utils.Pair;

// TODO: Auto-generated Javadoc
/**
 * The Class InformationMeasurement.
 */
public class InformationMeasurement {

	/**
	 * H_information_gain.
	 * 
	 * @param dset
	 *            the dset
	 * @param dsolutions
	 *            the dsolutions
	 * @param solutions
	 *            the solutions
	 * @param current_description
	 *            the current_description
	 * @return the pair
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public static Pair<Float, Integer> h_information_gain(List<FeatureTerm> dset, List<FeatureTerm> dsolutions, List<FeatureTerm> solutions,
			FeatureTerm current_description) throws FeatureTermException {
		List<FeatureTerm> dset_tmp;
		List<FeatureTerm> dsolutions_tmp;
		FeatureTerm description, s1;
		int nsols = solutions.size();
		int[] distributiona = new int[nsols];
		int[] distributionb = new int[nsols];
		int[] distributionc = new int[nsols];
		int na, nb, nc;
		int i;
		float gain;

		// System.out.println(current_description.toStringNOOS());

		na = dset.size();

		for (i = 0; i < nsols; i++) {
			s1 = solutions.get(i);

			distributiona[i] = 0;
			for (FeatureTerm s2 : dsolutions) {
				if (s1.equals(s2)) {
					distributiona[i]++;
				}
			}
		} // for

		dset_tmp = new LinkedList<FeatureTerm>();
		dsolutions_tmp = new LinkedList<FeatureTerm>();

		Iterator<FeatureTerm> i1 = dset.iterator();
		Iterator<FeatureTerm> i2 = dsolutions.iterator();
		while (i1.hasNext()) {
			description = i1.next();
			s1 = i2.next();
			if (current_description.subsumes(description)) {
				dset_tmp.add(description);
				dsolutions_tmp.add(s1);
			} // if
		} // while

		for (i = 0; i < nsols; i++) {
			s1 = solutions.get(i);
			distributionb[i] = 0;

			for (FeatureTerm s2 : dsolutions_tmp) {
				if (s1.equals(s2)) {
					distributionb[i]++;
				}
			}
			distributionc[i] = distributiona[i] - distributionb[i];
		} // for

		nb = dset_tmp.size();
		nc = na - nb;

		// Compute information gain:
		{
			float e1, e2, e3;

			e1 = entropy(nsols, distributiona);
			e2 = entropy(nsols, distributionb);
			e3 = entropy(nsols, distributionc);

			// System.out.println(e1 + " - " + e2 + " - " + e3 + "[" + na + "," + nb + "," + nc + "]");

			// gain=e1-(nb/na)*e2 + (nc/na)*e3);
			gain = e1 - ((((float) (nb)) / ((float) (na))) * e2 + (((float) (nc)) / ((float) (na))) * e3);

		}

		return new Pair<Float, Integer>(gain, nb);
	} // h_information_gain

	/**
	 * H_information_gain.
	 * 
	 * @param s
	 *            the s
	 * @param s1
	 *            the s1
	 * @param s2
	 *            the s2
	 * @param solutions
	 *            the solutions
	 * @return the pair
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public static Pair<Float, Integer> h_information_gain(List<FeatureTerm> s, List<FeatureTerm> s1, List<FeatureTerm> s2, List<FeatureTerm> solutions)
			throws FeatureTermException {
		int nsols = solutions.size();
		int[] distributiona = new int[nsols];
		int[] distributionb = new int[nsols];
		int[] distributionc = new int[nsols];
		int na, nb, nc;
		int i;
		float gain;

		// System.out.println(current_description.toStringNOOS());

		na = s.size();
		nb = s1.size();
		nc = s2.size();

		for (FeatureTerm solution : s)
			distributiona[solutions.indexOf(solution)]++;
		for (FeatureTerm solution : s1)
			distributionb[solutions.indexOf(solution)]++;
		for (FeatureTerm solution : s2)
			distributionc[solutions.indexOf(solution)]++;

		// Compute information gain:
		{
			float e1, e2, e3;

			e1 = entropy(nsols, distributiona);
			e2 = entropy(nsols, distributionb);
			e3 = entropy(nsols, distributionc);

			// System.out.println(e1 + " - " + e2 + " - " + e3 + "[" + na + "," + nb + "," + nc + "]");

			// gain=e1-(nb/na)*e2 + (nc/na)*e3);
			gain = e1 - ((((float) (nb)) / ((float) (na))) * e2 + (((float) (nc)) / ((float) (na))) * e3);

		}

		return new Pair<Float, Integer>(gain, nb);
	} // h_information_gain

	/** The Constant M_LOG2E. */
	static final double M_LOG2E = Math.log(2.0);

	/**
	 * Entropy hash.
	 * 
	 * @param distribution
	 *            the distribution
	 * @return the float
	 */
	public static float entropyHash(HashMap<FeatureTerm, Integer> distribution) {
		int i;
		int total = 0;
		float entropy = 0;
		float tmp;

		for (FeatureTerm f : distribution.keySet()) {
			total += (distribution.get(f));
		}

		if (total == 0) {
			return 0;
		}

		for (FeatureTerm f : distribution.keySet()) {
			tmp = ((float) distribution.get(f)) / ((float) total);
			if (tmp != 0.0) {
				entropy += ((float) tmp * (Math.log(tmp) / M_LOG2E));
			} // if
		} // for

		return -entropy;
	} // entropy

	/**
	 * Entropy.
	 * 
	 * @param l
	 *            the l
	 * @param d
	 *            the d
	 * @return the float
	 */
	public static float entropy(int l, int[] d) {
		int i;
		int total = 0;
		float entropy = 0;
		float tmp;

		for (i = 0; i < l; i++) {
			total += d[i];
		}

		if (total == 0) {
			return 0;
		}

		for (i = 0; i < l; i++) {
			tmp = ((float) d[i]) / ((float) total);
			if (tmp != 0.0) {
				entropy += ((float) tmp * (Math.log(tmp) / M_LOG2E));
			} // if
		} // for

		return -entropy;
	} // entropy

	/**
	 * Entropy d.
	 * 
	 * @param l
	 *            the l
	 * @param d
	 *            the d
	 * @return the double
	 */
	public static double entropyD(int l, double[] d) {
		int i;
		double total = 0;
		double entropy = 0;
		double tmp;

		for (i = 0; i < l; i++) {
			total += d[i];
		}

		if (total == 0) {
			return 0;
		}

		for (i = 0; i < l; i++) {
			tmp = d[i] / total;
			if (tmp != 0.0) {
				entropy += (tmp * (Math.log(tmp) / M_LOG2E));
			} // if
		} // for

		return -entropy;
	} // entropy

	/**
	 * H_rldm.
	 * 
	 * @param dset
	 *            the dset
	 * @param dsolutions
	 *            the dsolutions
	 * @param solutions
	 *            the solutions
	 * @param current_description
	 *            the current_description
	 * @return the pair
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public static Pair<Float, Integer> h_rldm(List<FeatureTerm> dset, List<FeatureTerm> dsolutions, List<FeatureTerm> solutions, FeatureTerm current_description)
			throws FeatureTermException {
		FeatureTerm description, solution;
		int nsols = solutions.size();
		int sol_pos;
		float rldm;
		int d_correcta[] = new int[nsols];
		int d_b[] = new int[2];
		int d_intersection[] = new int[nsols * 2];
		float e1, e2, e3;
		int i;

		for (i = 0; i < nsols; i++) {
			d_correcta[i] = 0;
		}
		for (i = 0; i < 2; i++) {
			d_b[i] = 0;
		}
		for (i = 0; i < 2 * nsols; i++) {
			d_intersection[i] = 0;
		}

		Iterator<FeatureTerm> i1 = dset.iterator();
		Iterator<FeatureTerm> i2 = dsolutions.iterator();
		while (i1.hasNext()) {
			description = i1.next();
			solution = i2.next();
			sol_pos = solutions.indexOf(solution);

			d_correcta[sol_pos]++;

			if (current_description.subsumes(description)) {
				d_b[0]++;
				d_intersection[sol_pos]++;
			} else {
				d_b[1]++;
				d_intersection[sol_pos + nsols]++;
			} // if
		} // while

		// Compute rldm:
		{
			e1 = entropy(nsols, d_correcta);
			e2 = entropy(2, d_b);
			e3 = entropy(2 * nsols, d_intersection);

			// printf("A: ");for(i=0;i<nsols;i++) printf("%i ",d_correcta[i]);printf("\n");
			// printf("B: ");for(i=0;i<2;i++) printf("%i ",d_b[i]);printf("\n");
			// printf("C: ");for(i=0;i<2*nsols;i++) printf("%i ",d_intersection[i]);printf("\n");

			// printf("[%i] {%g %g %g} ",dsolutions.size(),e1,e2,e3);

			if (e2 == 0) {
				rldm = 1;
			} else {
				rldm = 2.0F - ((e1 + e2) / e3);
			}
		}

		return new Pair<Float, Integer>(rldm, d_b[0]);
	} // h_rldm

	/**
	 * H_entropy.
	 * 
	 * @param dset
	 *            the dset
	 * @param dsolutions
	 *            the dsolutions
	 * @param solutions
	 *            the solutions
	 * @param current_description
	 *            the current_description
	 * @return the pair
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public static Pair<Float, Integer> h_entropy(List<FeatureTerm> dset, List<FeatureTerm> dsolutions, List<FeatureTerm> solutions,
			FeatureTerm current_description) throws FeatureTermException {
		List<FeatureTerm> dset_tmp;
		List<FeatureTerm> dsolutions_tmp;
		FeatureTerm description, s1;
		int nsols = solutions.size();
		;
		int distributionb[] = new int[nsols];
		// int na,nc;
		int nb;
		int i;
		float e;

		// na=dset.size();

		dset_tmp = new LinkedList<FeatureTerm>();
		dsolutions_tmp = new LinkedList<FeatureTerm>();

		Iterator<FeatureTerm> i1 = dset.iterator();
		Iterator<FeatureTerm> i2 = dsolutions.iterator();
		while (i1.hasNext()) {
			description = i1.next();
			s1 = i2.next();
			if (current_description.subsumes(description)) {
				dset_tmp.add(description);
				dsolutions_tmp.add(s1);
			} // if
		} // while

		for (i = 0; i < nsols; i++) {
			s1 = solutions.get(i);
			distributionb[i] = 0;
			for (FeatureTerm s2 : dsolutions_tmp) {
				if (s1.equals(s2)) {
					distributionb[i]++;
				}
			}
		} // for

		nb = dset_tmp.size();
		// nc=na-nb;

		// Compute information gain:
		{
			float e2;

			e2 = entropy(nsols, distributionb);

			e = e2;
		}

		return new Pair<Float, Integer>(e, nb);
	} // h_entropy
}
