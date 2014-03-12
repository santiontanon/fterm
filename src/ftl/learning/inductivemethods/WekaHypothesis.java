/*
 * 
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
  
 package ftl.learning.inductivemethods;

import ftl.base.bridges.NOOSToWeka;
import ftl.base.bridges.NOOSToWeka.ConversionRecord;
import ftl.base.core.FTKBase;
import ftl.base.core.FeatureTerm;
import ftl.learning.core.Hypothesis;
import ftl.learning.core.Prediction;
import weka.classifiers.Classifier;
import weka.core.Instance;

// TODO: Auto-generated Javadoc
/**
 * The Class WekaHypothesis.
 */
public class WekaHypothesis extends Hypothesis {

	/** The m_classifier. */
	Classifier m_classifier = null;

	/** The m_record. */
	ConversionRecord m_record = null;

	/**
	 * Instantiates a new weka hypothesis.
	 * 
	 * @param c
	 *            the c
	 * @param record
	 *            the record
	 */
	public WekaHypothesis(Classifier c, ConversionRecord record) {
		m_classifier = c;
		m_record = record;
	}

	/**
	 * Copy.
	 * 
	 * @param h
	 *            the h
	 * @throws Exception
	 *             the exception
	 */
	public void copy(WekaHypothesis h) throws Exception {
		m_classifier = ((WekaHypothesis) h).m_classifier;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csic.iiia.ftl.learning.core.Hypothesis#generatePrediction(csic.iiia.ftl.base.core.FeatureTerm,
	 * csic.iiia.ftl.base.core.FTKBase, boolean)
	 */
	public Prediction generatePrediction(FeatureTerm problem, FTKBase dm, boolean debug) throws Exception {
		Prediction p = new Prediction(problem);

		Instance inst = NOOSToWeka.translateInstance(m_record.problemsToCases.get(problem), m_record.getAllCases(), m_record.getAllWekaCases());
		double result = m_classifier.classifyInstance(inst);
		FeatureTerm solution = m_record.getSolutionMapping()[(int) result];

		p.solutions.add(solution);
		p.support.put(solution, 1);
		return p;
	} // Hypothesis::generate_prediction

	/*
	 * (non-Javadoc)
	 * 
	 * @see csic.iiia.ftl.learning.core.Hypothesis#toString(csic.iiia.ftl.base.core.FTKBase)
	 */
	public String toString(FTKBase dm) {
		return m_classifier.toString();
	} // Hypothesis::show_rule_set

	/*
	 * (non-Javadoc)
	 * 
	 * @see csic.iiia.ftl.learning.core.Hypothesis#toCompactString(csic.iiia.ftl.base.core.FTKBase)
	 */
	public String toCompactString(FTKBase dm) {
		return "WekaHypothesis(" + m_classifier.getClass().getName() + ")";
	} // Hypothesis::show_rule_set

}
