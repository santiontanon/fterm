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
  
 package ftl.base.bridges;

import java.util.LinkedList;
import java.util.List;

import ftl.base.core.BaseOntology;
import ftl.base.core.Disintegration;
import ftl.base.core.FTKBase;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Ontology;
import ftl.base.utils.FeatureTermException;
import ftl.learning.core.TrainingSetProperties;
import ftl.learning.core.TrainingSetUtils;

/**
 * The Class DisintegrationToWEKA.
 * 
 * @author santi
 */
public class DisintegrationToWEKA {

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String args[]) {
		try {
			Ontology base_ontology = new BaseOntology();
			Ontology o = new Ontology();
			FTKBase dm = new FTKBase();
			FTKBase case_base = new FTKBase();
			o.uses(base_ontology);
			case_base.uses(dm);
			dm.create_boolean_objects(o);

			int tsu = TrainingSetUtils.ZOOLOGY_DATASET;

			disintegrate(tsu, o, dm, case_base);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Disintegrate.
	 * 
	 * @param trainingSetUtils
	 *            the training set utils
	 * @param o
	 *            the o
	 * @param dm
	 *            the dm
	 * @param case_base
	 *            the case_base
	 * @return the string
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws Exception
	 *             the exception
	 */
	private static String disintegrate(int trainingSetUtils, Ontology o, FTKBase dm, FTKBase case_base) throws FeatureTermException, Exception {

		String disintegration = "";
		TrainingSetProperties ts = TrainingSetUtils.loadTrainingSet(trainingSetUtils, o, dm, case_base);

		long t_start = System.currentTimeMillis();
		long t_end;

		List<FeatureTerm> cases;
		case_base.printUndefinedTerms();

		t_end = System.currentTimeMillis();
		System.out.println("Time taken to load: " + (t_end - t_start));
		disintegration += "Time taken to load: " + (t_end - t_start) + "\n";

		t_start = System.currentTimeMillis();

		cases = ts.cases;

		ts.printStatistics(dm);

		List<FeatureTerm> dictionary = new LinkedList<FeatureTerm>();

		for (FeatureTerm c : cases) {
			FeatureTerm description = c.readPath(ts.description_path);
			System.out.println("Disintegrating " + c.getName());
			disintegration += "Disintegrating " + c.getName() + "\n";
			List<FeatureTerm> properties = Disintegration.disintegrate(description, dm, o, false, false); // not using
																											// cache

			// add them without replication to the dictionary:
			for (FeatureTerm property : properties) {
				boolean present = false;
				for (FeatureTerm p2 : dictionary) {
					if (property.equivalents(p2)) {
						present = true;
						break;
					}
				}
				if (!present)
					dictionary.add(property);
			}
		}

		System.out.println("Resulting dictionary has " + dictionary.size() + " properties.");
		disintegration += "Resulting dictionary has " + dictionary.size() + " properties." + "\n";
		;

		System.out.println("@relation 'dataset'");
		disintegration += "@relation 'dataset'" + "\n";
		;
		int i = 0;
		for (FeatureTerm p : dictionary) {
			System.out.println("@attribute p" + i + " real");
			disintegration += "@attribute p" + i + " real" + "\n";
			;
			i++;
		}
		System.out.print("@attribute 'class' {");
		disintegration += "@attribute 'class' {" + "\n";
		;
		boolean first = true;
		for (FeatureTerm s : ts.differentSolutions()) {
			if (first) {
				first = false;
			} else {
				System.out.print(",");
				disintegration += "," + "\n";
				;
			}
			System.out.print(s.toStringNOOS(dm));
		}
		System.out.println("}");
		disintegration += "}" + "\n";
		;
		System.out.println("@data");
		disintegration += "@data" + "\n";
		;

		for (FeatureTerm c : cases) {
			FeatureTerm description = c.readPath(ts.description_path);
			FeatureTerm solution = c.readPath(ts.solution_path);
			for (FeatureTerm property : dictionary) {
				if (property.subsumes(description)) {
					System.out.print("1,");
					disintegration += "1,";
				} else {
					System.out.print("0,");
					disintegration += "0,";
				}
			}
			System.out.println(solution.toStringNOOS(dm));
			disintegration += solution.toStringNOOS(dm) + "\n";
			;
		}
		return disintegration;
	}

	/**
	 * Disintegrate.
	 * 
	 * @param trainingSet
	 *            the training set
	 * @return the string
	 * @throws Exception
	 *             the exception
	 */
	public static String disintegrate(int trainingSet) throws Exception {
		Ontology base_ontology = new BaseOntology();
		Ontology o = new Ontology();
		FTKBase dm = new FTKBase();
		FTKBase case_base = new FTKBase();
		o.uses(base_ontology);
		case_base.uses(dm);
		dm.create_boolean_objects(o);

		return disintegrate(trainingSet, o, dm, case_base);
	}
}
