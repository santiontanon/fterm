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
  
 package ftl.argumentation.weighted;

import java.util.HashMap;
import java.util.List;

import ftl.argumentation.core.Argument;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Path;
import ftl.base.utils.FeatureTermException;

// TODO: Auto-generated Javadoc
/**
 * The Class ArgumentExaminationRecord.
 * 
 * @author santi
 */
public class ArgumentExaminationRecord {

	/** The m_a. */
	public WeightedArgument m_a;

	/** The m_agent_name. */
	public String m_agent_name;

	/** The m_histogram. */
	public HashMap<FeatureTerm, Integer> m_histogram;

	/**
	 * Instantiates a new argument examination record.
	 * 
	 * @param a
	 *            the a
	 * @param name
	 *            the name
	 */
	public ArgumentExaminationRecord(WeightedArgument a, String name) {
		m_a = a;
		m_agent_name = name;
		m_histogram = new HashMap<FeatureTerm, Integer>();
	}

	/**
	 * Instantiates a new argument examination record.
	 * 
	 * @param a
	 *            the a
	 * @param name
	 *            the name
	 * @param examples
	 *            the examples
	 * @param dp
	 *            the dp
	 * @param sp
	 *            the sp
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public ArgumentExaminationRecord(WeightedArgument a, String name, List<FeatureTerm> examples, Path dp, Path sp) throws FeatureTermException {
		m_a = a;
		m_agent_name = name;
		m_histogram = new HashMap<FeatureTerm, Integer>();

		for (FeatureTerm example : examples) {
			if (a.m_a.m_type == Argument.ARGUMENT_RULE) {
				FeatureTerm d = example.readPath(dp);
				if (a.m_a.m_rule.pattern.subsumes(d)) {
					FeatureTerm s = example.readPath(sp);
					Integer c = m_histogram.get(s);
					if (c == null)
						c = 0;
					m_histogram.put(s, c + 1);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String tmp = "AER(" + m_agent_name + "): (";
		for (FeatureTerm s : m_histogram.keySet())
			tmp += " " + m_histogram.get(s);
		return tmp + ")";
	}

}
