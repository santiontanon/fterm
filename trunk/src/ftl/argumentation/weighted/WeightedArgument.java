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

import java.util.LinkedList;
import java.util.List;

import ftl.argumentation.core.Argument;
import ftl.base.core.FTKBase;
import ftl.base.core.FeatureTerm;
import ftl.base.utils.FeatureTermException;

// TODO: Auto-generated Javadoc
/**
 * The Class WeightedArgument.
 * 
 * @author santi
 */
public class WeightedArgument {

	/** The m_a. */
	Argument m_a;

	/** The m_examinations. */
	List<ArgumentExaminationRecord> m_examinations = new LinkedList<ArgumentExaminationRecord>();

	/**
	 * Instantiates a new weighted argument.
	 * 
	 * @param a
	 *            the a
	 */
	public WeightedArgument(Argument a) {
		m_a = a;
	}

	/**
	 * Adds the examination record.
	 * 
	 * @param aer
	 *            the aer
	 */
	public void addExaminationRecord(ArgumentExaminationRecord aer) {
		if (examined(aer.m_agent_name))
			System.err.println("Already examined by " + aer.m_agent_name + "!!!!!!!!!");
		m_examinations.add(aer);
	}

	/**
	 * Laplace confidence.
	 * 
	 * @return the float
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public float LaplaceConfidence() throws FeatureTermException {
		float p = 0;
		float n = 0;

		for (ArgumentExaminationRecord aer : m_examinations) {
			for (FeatureTerm s : aer.m_histogram.keySet()) {
				if (s.equivalents(m_a.m_rule.solution)) {
					p += aer.m_histogram.get(s);
				} else {
					n += aer.m_histogram.get(s);
				}
			}
		}

		return (p + 1) / (p + n + 2);
	}

	// This method assumes that one argument is more specific than another
	/**
	 * Overlap when subsumption.
	 * 
	 * @param a
	 *            the a
	 * @return the float
	 */
	public float overlapWhenSubsumption(WeightedArgument a) {
		int union = 0;
		int intersection = 0;

		for (ArgumentExaminationRecord aer1 : m_examinations) {
			for (ArgumentExaminationRecord aer2 : a.m_examinations) {
				if (aer1.m_agent_name.equals(aer2.m_agent_name)) {
					int n1 = 0, n2 = 0;
					for (FeatureTerm s : aer1.m_histogram.keySet())
						n1 += aer1.m_histogram.get(s);
					for (FeatureTerm s : aer2.m_histogram.keySet())
						n2 += aer2.m_histogram.get(s);

					union += Math.max(n1, n2);
					intersection += Math.min(n1, n2);
					break;
				}
			}
		}

		if (union == 0)
			return 0;
		return ((float) intersection) / union;
	}

	// Strength of the attack of "this" to "a":
	/**
	 * Attack strength.
	 * 
	 * @param a
	 *            the a
	 * @return the float
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public float attackStrength(WeightedArgument a) throws FeatureTermException {
		return LaplaceConfidence() * overlapWhenSubsumption(a);
	}

	/**
	 * Examined.
	 * 
	 * @param agent
	 *            the agent
	 * @return true, if successful
	 */
	public boolean examined(String agent) {
		for (ArgumentExaminationRecord aer : m_examinations) {
			if (aer.m_agent_name.equals(agent))
				return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "W(Weighted) " + m_a.toString();
	}

	/**
	 * To string noos.
	 * 
	 * @param dm
	 *            the dm
	 * @return the string
	 */
	public String toStringNOOS(FTKBase dm) {
		return "W(Weighted) " + m_a.toStringNOOS(dm);
	}

	/**
	 * Gets the examinations.
	 * 
	 * @return the examinations
	 */
	public List<ArgumentExaminationRecord> getExaminations() {
		return m_examinations;
	}

	/**
	 * Gets the agent.
	 * 
	 * @return the agent
	 */
	public String getAgent() {
		return m_a.m_agent;
	}

	/**
	 * Gets the iD.
	 * 
	 * @return the iD
	 */
	public int getID() {
		return m_a.m_ID;
	}

	/**
	 * Gets the examination.
	 * 
	 * @param agent
	 *            the agent
	 * @return the examination
	 */
	ArgumentExaminationRecord getExamination(String agent) {
		for (ArgumentExaminationRecord aer : m_examinations) {
			if (aer.m_agent_name.equals(agent))
				return aer;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public WeightedArgument clone() {
		WeightedArgument c = new WeightedArgument(m_a);
		c.m_examinations.addAll(m_examinations);
		return c;
	}
}
