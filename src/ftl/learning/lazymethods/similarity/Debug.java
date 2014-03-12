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

import ftl.base.bridges.NOOSParser;
import ftl.base.core.BaseOntology;
import ftl.base.core.FTKBase;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Ontology;
import ftl.base.utils.FeatureTermException;
import ftl.learning.core.TrainingSetProperties;
import ftl.learning.core.TrainingSetUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class Debug.
 * 
 * @author santi
 */
public class Debug {

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public static void main(String[] args) throws FeatureTermException {
		Ontology base_ontology = new BaseOntology();
		Ontology o = new Ontology();
		FTKBase dm = new FTKBase();
		FTKBase case_base = new FTKBase();
		o.uses(base_ontology);
		case_base.uses(dm);

		dm.create_boolean_objects(o);

		try {
			TrainingSetProperties ts = TrainingSetUtils.loadTrainingSet(TrainingSetUtils.TRAINS_DATASET, o, dm, case_base);

			FeatureTerm f1 = NOOSParser.parse("(define (car))", dm, o);
			FeatureTerm f2 = NOOSParser.parse("(define (car) (loc 1) (nwhl 2))", dm, o);
			FeatureTerm f3 = NOOSParser.parse("(define (car) (loc 2))", dm, o);
			FeatureTerm f4 = NOOSParser.parse("(define (car) (nwhl 2))", dm, o);

			// KashimaKernelSparse d = new KashimaKernelSparse();
			KashimaKernelDAGs d = new KashimaKernelDAGs();

			System.out.println("1 - 2 : " + d.distance(f1, f2, o, dm));
			System.out.println("1 - 3 : " + d.distance(f1, f3, o, dm));
			System.out.println("1 - 4 : " + d.distance(f1, f4, o, dm));
			System.out.println("2 - 3 : " + d.distance(f2, f3, o, dm));
			System.out.println("2 - 4 : " + d.distance(f2, f4, o, dm));
			System.out.println("3 - 4 : " + d.distance(f3, f4, o, dm));

			// System.out.println("" + KashimaKernel.labelSimilarity(NOOSParser.parse("(define (car) (nwhl 2))", dm, o),
			// NOOSParser.parse("(define (car))", dm, o), dm));
			// System.out.println("" + KashimaKernel.labelSimilarity(f2,f3, dm));
			// System.out.println("" + (0.05*0.03333 + 0.45*0.15*0.66666 + 0.5*(0.33333*0.66666 + 0.33333)));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
