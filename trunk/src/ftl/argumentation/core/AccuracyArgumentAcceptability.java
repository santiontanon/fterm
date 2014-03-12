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

import ftl.base.core.FeatureTerm;
import ftl.base.core.Path;
import ftl.base.utils.FeatureTermException;

// TODO: Auto-generated Javadoc
/**
 * The Class AccuracyArgumentAcceptability.
 * 
 * @author santi
 */
public class AccuracyArgumentAcceptability extends ArgumentAcceptability {

	/** The m_threshold. */
	float m_threshold;

	/**
	 * Instantiates a new accuracy argument acceptability.
	 * 
	 * @param examples
	 *            the examples
	 * @param sp
	 *            the sp
	 * @param dp
	 *            the dp
	 * @param threshold
	 *            the threshold
	 */
	public AccuracyArgumentAcceptability(Collection<FeatureTerm> examples, Path sp, Path dp, float threshold) {
		super(examples, sp, dp);

		m_threshold = threshold;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csic.iiia.ftl.argumentation.core.ArgumentAcceptability#accepted(csic.iiia.ftl.argumentation.core.Argument)
	 */
	public boolean accepted(Argument a) throws FeatureTermException {
		if (degree(a) >= m_threshold)
			return true;
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csic.iiia.ftl.argumentation.core.ArgumentAcceptability#degree(csic.iiia.ftl.argumentation.core.Argument)
	 */
	public float degree(Argument a) throws FeatureTermException {
		if (a.m_type == Argument.ARGUMENT_EXAMPLE)
			return 1.0f;
		if (a.m_type == Argument.ARGUMENT_RULE) {
			float P = 0;
			float N = 0;

			for (FeatureTerm e : m_examples) {
				FeatureTerm d = e.readPath(m_dp);

				if (a.m_rule.pattern.subsumes(d)) {
					FeatureTerm s = e.readPath(m_sp);
					if (s.equivalents(a.m_rule.solution))
						P++;
					else
						N++;
				}
			}

			float b = 0;

			if (P + N > 0) {
				b = (P) / (P + N);
			} else {
				b = 0.5f;
			}

			if (DEBUG >= 1) {
				System.out.println("AccuracyArgumentAcceptability.accepted " + b + "(" + P + "/" + N + ") with " + m_examples.size() + " examples");
				if (DEBUG >= 2) {
					for (FeatureTerm e : m_examples) {
						FeatureTerm d = e.readPath(m_dp);

						if (a.m_rule.pattern.subsumes(d)) {
							FeatureTerm s = e.readPath(m_sp);
							System.out.print(e.getName().get() + " ");
						}
					}
					System.out.println("");
				}
			}

			return b;
		}
		return 0.0f;
	}

}
