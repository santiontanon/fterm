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

import java.util.List;

import ftl.base.core.FTKBase;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Path;
import ftl.base.utils.FeatureTermException;
import ftl.learning.core.Rule;

// TODO: Auto-generated Javadoc
/**
 * The Class Argument.
 */
public class Argument {

	/** The next_ id. */
	public static int next_ID = 0;

	/** The Constant ARGUMENT_NONE. */
	public static final int ARGUMENT_NONE = -1;

	/** The Constant ARGUMENT_RULE. */
	public static final int ARGUMENT_RULE = 0;

	/** The Constant ARGUMENT_EXAMPLE. */
	public static final int ARGUMENT_EXAMPLE = 1;

	/** The m_type. */
	public int m_type = ARGUMENT_NONE;

	/** The m_example. */
	public FeatureTerm m_example;

	/** The m_rule. */
	public Rule m_rule;

	/** The m_agent. */
	public String m_agent = null;

	/** The m_ id. */
	public int m_ID = next_ID++;

	/**
	 * Instantiates a new argument.
	 * 
	 * @param e
	 *            the e
	 */
	public Argument(FeatureTerm e) {
		m_type = ARGUMENT_EXAMPLE;
		m_example = e;
		m_rule = null;
	}

	/**
	 * Instantiates a new argument.
	 * 
	 * @param r
	 *            the r
	 */
	public Argument(Rule r) {
		m_type = ARGUMENT_RULE;
		m_rule = r;
		m_example = null;
	}

	/**
	 * Instantiates a new argument.
	 * 
	 * @param e
	 *            the e
	 * @param agent
	 *            the agent
	 */
	public Argument(FeatureTerm e, String agent) {
		m_type = ARGUMENT_EXAMPLE;
		m_example = e;
		m_rule = null;
		m_agent = agent;
	}

	/**
	 * Instantiates a new argument.
	 * 
	 * @param r
	 *            the r
	 * @param agent
	 *            the agent
	 */
	public Argument(Rule r, String agent) {
		m_type = ARGUMENT_RULE;
		m_rule = r;
		m_example = null;
		m_agent = agent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		if (m_type == ARGUMENT_RULE) {
			return "RuleArgument" + m_ID + (m_agent == null ? "" : "-" + m_agent);
		} else if (m_type == ARGUMENT_EXAMPLE) {
			return "ExampleArgument" + m_ID + (m_agent == null ? "" : "-" + m_agent + "-") + "(" + m_example.getName() + ")";
		} else {
			return "NoneArgument" + m_ID + (m_agent == null ? "" : "-" + m_agent);
		}
	}

	/**
	 * To string noos.
	 * 
	 * @param dm
	 *            the dm
	 * @return the string
	 */
	public String toStringNOOS(FTKBase dm) {
		if (m_type == ARGUMENT_RULE) {
			return "Argument" + m_ID + "(RULE):\n" + m_rule.toStringNOOS(dm);
		} else if (m_type == ARGUMENT_EXAMPLE) {
			return "Argument" + m_ID + "(EXAMPLE):\n" + m_example.toStringNOOS(dm);
		} else {
			return "Argument" + m_ID + "(NONE)";
		}
	}

	/**
	 * Equivalents.
	 * 
	 * @param a
	 *            the a
	 * @return true, if successful
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public boolean equivalents(Argument a) throws FeatureTermException {
		if (m_type != a.m_type) {
			return false;
		}
		if (m_type == ARGUMENT_EXAMPLE) {
			if (m_example == a.m_example) {
				return true;
			}
			return false;
		}
		if (m_type == ARGUMENT_RULE) {
			if (!m_rule.solution.equivalents(a.m_rule.solution)) {
				return false;
			}
			if (!m_rule.pattern.equivalents(a.m_rule.pattern)) {
				return false;
			}
			return true;
		}

		return true;
	}

	/**
	 * Gets the iD.
	 * 
	 * @return the iD
	 */
	public int getID() {
		return m_ID;
	}

	/*
	 * Returns the ratio of shared examples among two arguments. It only applies to RULE arguments, for other kinds, it
	 * returns 0
	 */
	/**
	 * Overlap.
	 * 
	 * @param a
	 *            the a
	 * @param examples
	 *            the examples
	 * @param dp
	 *            the dp
	 * @return the float
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public float overlap(Argument a, List<FeatureTerm> examples, Path dp) throws FeatureTermException {
		if (m_type == ARGUMENT_RULE) {
			if (a.m_type == ARGUMENT_RULE) {
				int covered_by_both = 0;
				for (FeatureTerm e : examples) {
					FeatureTerm d = e.readPath(dp);
					if (m_rule.pattern.subsumes(d) && a.m_rule.pattern.subsumes(d)) {
						covered_by_both++;
					}
				}
				if (examples.isEmpty())
					return 0;
				return ((float) covered_by_both) / examples.size();
			} else {
				return 0;
			}
		} else {
			return 0;
		}
	}
}
