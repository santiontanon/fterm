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

import ftl.base.core.FTKBase;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Ontology;

// TODO: Auto-generated Javadoc
/**
 * The Class KashimaKernelWithRoot.
 * 
 * @author santi
 */
public class KashimaKernelWithRoot extends KashimaKernel {

	/** The DEBUG. */
	public static int DEBUG = 0;

	/**
	 * Instantiates a new kashima kernel with root.
	 */
	public KashimaKernelWithRoot() {

	}

	/**
	 * Instantiates a new kashima kernel with root.
	 * 
	 * @param a_q
	 *            the a_q
	 */
	public KashimaKernelWithRoot(double a_q) {
		super(a_q);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csic.iiia.ftl.learning.lazymethods.similarity.KashimaKernel#distance(csic.iiia.ftl.base.core.FeatureTerm,
	 * csic.iiia.ftl.base.core.FeatureTerm, csic.iiia.ftl.base.core.Ontology, csic.iiia.ftl.base.core.FTKBase)
	 */
	public double distance(FeatureTerm f1, FeatureTerm f2, Ontology o, FTKBase dm) throws Exception {
		Graph g1 = new Graph(f1, dm);
		Graph g2 = new Graph(f2, dm);
		return 1.0 - similarity(g1, g2, q, true, dm);
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
		return 1.0 - similarity(g1, g2, pq, true, dm);
	}

}
