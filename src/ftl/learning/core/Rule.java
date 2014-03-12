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

import ftl.base.core.FTKBase;
import ftl.base.core.FeatureTerm;

// TODO: Auto-generated Javadoc
/**
 * The Class Rule.
 */
public class Rule {

	/** The pattern. */
	public FeatureTerm pattern;

	/** The solution. */
	public FeatureTerm solution;

	/** The reliability. */
	public float reliability;

	/** The support. */
	public int support;

	/**
	 * Instantiates a new rule.
	 * 
	 * @param p
	 *            the p
	 * @param s
	 *            the s
	 */
	public Rule(FeatureTerm p, FeatureTerm s) {
		pattern = p;
		solution = s;
		reliability = 0;
		support = 0;
	}

	/**
	 * Instantiates a new rule.
	 * 
	 * @param p
	 *            the p
	 * @param s
	 *            the s
	 * @param r
	 *            the r
	 * @param supp
	 *            the supp
	 */
	public Rule(FeatureTerm p, FeatureTerm s, float r, int supp) {
		pattern = p;
		solution = s;
		reliability = r;
		support = supp;
	}

	/**
	 * To string noos.
	 * 
	 * @param dm
	 *            the dm
	 * @return the string
	 */
	public String toStringNOOS(FTKBase dm) {
		String ret = "* Rule:\nPattern:\n" + pattern.toStringNOOS(dm) + "\n";
		ret += "Solution: " + solution.toStringNOOS(dm) + "\n";
		ret += "Reliability: " + reliability + "\nSupport: " + support;
		return ret;
	}
};