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
  
 package ftl.argumentation.core;

import java.util.Collection;
import java.util.LinkedList;

import ftl.base.core.FeatureTerm;
import ftl.base.core.Path;
import ftl.base.utils.FeatureTermException;

// TODO: Auto-generated Javadoc
/**
 * The Class ArgumentAcceptability.
 * 
 * @author santi
 */
public abstract class ArgumentAcceptability {

	/** The DEBUG. */
	public static int DEBUG = 0;

	/** The m_examples. */
	protected Collection<FeatureTerm> m_examples;

	/** The m_sp. */
	protected Path m_dp, m_sp;

	/**
	 * Instantiates a new argument acceptability.
	 * 
	 * @param examples
	 *            the examples
	 * @param sp
	 *            the sp
	 * @param dp
	 *            the dp
	 */
	public ArgumentAcceptability(Collection<FeatureTerm> examples, Path sp, Path dp) {
		m_examples = new LinkedList<FeatureTerm>();
		m_examples.addAll(examples);

		m_dp = dp;
		m_sp = sp;
	}

	/**
	 * Update examples.
	 * 
	 * @param examples
	 *            the examples
	 */
	public void updateExamples(Collection<FeatureTerm> examples) {
		m_examples.clear();
		m_examples.addAll(examples);
	}

	// boolean criterion
	/**
	 * Accepted.
	 * 
	 * @param a
	 *            the a
	 * @return true, if successful
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public abstract boolean accepted(Argument a) throws FeatureTermException;

	// graded criterion
	/**
	 * Degree.
	 * 
	 * @param a
	 *            the a
	 * @return the float
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public abstract float degree(Argument a) throws FeatureTermException;
}
