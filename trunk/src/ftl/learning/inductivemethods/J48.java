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

import java.util.List;

import ftl.base.bridges.NOOSToWeka;
import ftl.base.bridges.NOOSToWeka.ConversionRecord;
import ftl.base.core.FTKBase;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Ontology;
import ftl.base.core.Path;
import ftl.learning.core.Hypothesis;
import ftl.learning.core.InductiveLearner;

import weka.classifiers.Classifier;
import weka.core.Instances;

// TODO: Auto-generated Javadoc
/*
 * This class just wraps weka's J48 into a class usable in the AWorld project
 */

/**
 * The Class J48.
 */
public class J48 extends InductiveLearner {

	/** The record. */
	NOOSToWeka.ConversionRecord record = null;

	/**
	 * Instantiates a new j48.
	 * 
	 * @param r
	 *            the r
	 */
	public J48(NOOSToWeka.ConversionRecord r) {
		record = r;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csic.iiia.ftl.learning.core.InductiveLearner#generateHypothesis(java.util.List,
	 * csic.iiia.ftl.base.core.Path, csic.iiia.ftl.base.core.Path, csic.iiia.ftl.base.core.Ontology,
	 * csic.iiia.ftl.base.core.FTKBase)
	 */
	public Hypothesis generateHypothesis(List<FeatureTerm> examples, Path dp, Path sp, Ontology o, FTKBase dm) throws Exception {
		WekaJ48Hypothesis h = null;

		Instances wekaTrainingSet = NOOSToWeka.translateSubset(examples, record.getAllCases(), record.getAllWekaCases());

		Classifier c = new weka.classifiers.trees.J48();
		try {
			c.buildClassifier(wekaTrainingSet);
			// System.out.println(c.toString());
			h = new WekaJ48Hypothesis(c, record, true);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return h;
	}
}
