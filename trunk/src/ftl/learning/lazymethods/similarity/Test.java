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

import java.util.List;

import ftl.base.core.BaseOntology;
import ftl.base.core.FTKBase;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Ontology;
import ftl.base.utils.FeatureTermException;
import ftl.learning.core.TrainingSetProperties;
import ftl.learning.core.TrainingSetUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class Test.
 * 
 * @author santi
 */
public class Test {

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

			// TrainingSetProperties ts = TrainingSetUtils.loadTrainingSet(TrainingSetUtils.ZOOLOGY_DATASET, o, dm,
			// case_base);
			// TrainingSetProperties ts = TrainingSetUtils.loadTrainingSet(TrainingSetUtils.SOYBEAN_DATASET, o, dm,
			// case_base);
			// TrainingSetProperties ts = TrainingSetUtils.loadTrainingSet(TrainingSetUtils.TRAINS_DATASET, o, dm,
			// case_base);
			// TrainingSetProperties ts = TrainingSetUtils.loadTrainingSet(TrainingSetUtils.UNCLE_DATASET_SETS, o, dm,
			// case_base);
			TrainingSetProperties ts = TrainingSetUtils.loadTrainingSet(TrainingSetUtils.MUTAGENESIS_NOL_DISCRETIZED, o, dm, case_base);

			List<FeatureTerm> cases = ts.cases;

			for (FeatureTerm c1 : ts.cases) {
				// System.out.println(c1.readPath(ts.description_path).toStringNOOS(dm));
				Graph g1 = new Graph(c1.readPath(ts.description_path), dm);
				for (FeatureTerm c2 : ts.cases) {
					Graph g2 = new Graph(c2.readPath(ts.description_path), dm);

					System.out.print("K(" + c1.getName() + "," + c2.getName() + ") = ");
					double s1 = KashimaKernel.similarity(g1, g2, 0.1, true, dm);
					System.out.print(s1 + " | ");
					double s2 = KashimaKernelSparse.similarity(g1, g2, 0.1, true, dm);
					System.out.println(s2 + " ");
					System.exit(1);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
