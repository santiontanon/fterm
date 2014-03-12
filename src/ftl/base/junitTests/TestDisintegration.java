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
  
 package ftl.base.junitTests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ftl.base.core.BaseOntology;
import ftl.base.core.Disintegration;
import ftl.base.core.FTKBase;
import ftl.base.core.FTRefinement;
import ftl.base.core.FTUnification;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Ontology;
import ftl.base.utils.FeatureTermException;
import ftl.base.utils.Pair;
import ftl.learning.core.TrainingSetProperties;
import ftl.learning.core.TrainingSetUtils;

public class TestDisintegration {

	TrainingSetProperties ts = null;
	Ontology o = new Ontology();

	/** The dm. */
	FTKBase dm = new FTKBase();
	FTKBase cb = new FTKBase();
	boolean anyproblem = false;
	boolean found = false;
	boolean fine = true;

	@Before
	public void setUp() throws Exception {
		Ontology base_ontology = new BaseOntology();
		o.uses(base_ontology);
		cb.uses(dm);

	}

	/**
	 * Tear down.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test disintegration.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testDisintegration() throws FeatureTermException, IOException {
		int datasets[] = { TrainingSetUtils.ZOOLOGY_DATASET, TrainingSetUtils.UNCLE_DATASET, TrainingSetUtils.TRAINS_DATASET };
		String message[] = { "Language L0, zoology", "Language Lc, uncle, no sets", "Language L, trains" };
		String examples[] = { "zp-1680", "e1", "tr6" };

		for (int i = 0; i < datasets.length; i++) {
			System.out.println(message[i]);
			ts = TrainingSetUtils.loadTrainingSet(datasets[i], o, dm, cb);

			FeatureTerm c = ts.getCaseByName(examples[i]);
			FeatureTerm description = c.readPath(ts.description_path);
			System.out.println("Disintegrating " + c.getName().get());
			List<Pair<FeatureTerm, FeatureTerm>> disintegrationTrace = Disintegration.disintegrateWithTrace(description, dm, o);

			FeatureTerm last = description.clone(dm, o);
			{
				List<FeatureTerm> variables = FTRefinement.variables(last);
				for (FeatureTerm v : variables) {
					if (!dm.contains(v))
						v.setName(null);
				}
			}

			for (Pair<FeatureTerm, FeatureTerm> property_term : disintegrationTrace) {
				// boolean anyproblem = false;
				// 1st, test whether the remainder is correct:
				if (FTUnification.isUnification(last, property_term.m_a, property_term.m_b, dm, o)) {
					System.out.println("remainder is correct!");
				} else {
					System.out.println("Failed: remainder is incorrect!!!!!");
					fail();
					anyproblem = true;
				}

				// 2nd, test whether unification works:
				List<FeatureTerm> unifications = FTUnification.unification(property_term.m_a, property_term.m_b, dm);
				// boolean found = false;
				// boolean fine = true;
				if (unifications != null && unifications.size() > 0) {
					System.out.println("Unification yields " + unifications.size() + " results.");
					for (FeatureTerm u : unifications) {
						if (last.equivalents(u)) {
							found = true;
							System.out.println("ok!");
							break;
						}
					}
				} else {
					System.out.println("Property and rest do not unify!!!");
					if (!property_term.m_a.subsumes(last)) {
						System.out.println("The property does not subsume the original term!!!!!");
						fine = false;
						fail();
					}
					if (!property_term.m_b.subsumes(last)) {
						System.out.println("The rest does not subsume the original term!!!!!");
						fine = false;
						fail();
					}
					if (fine) {
						System.out.println("There is an error in the unification method...");
						fail();
					}
				}
				if (!found || !fine)
					anyproblem = true;

				if (anyproblem) {

					System.out.println("Disintegration error, unifying property with rest does not recover original term!!!!");
					System.out.println("Property is:");
					System.out.println(property_term.m_a.toStringNOOS(dm));
					System.out.println("Rest is:");
					System.out.println(property_term.m_b.toStringNOOS(dm));
					System.out.println("original is:");
					System.out.println(last.toStringNOOS(dm));

					System.out.println("Unifications are:");
					if (unifications != null) {
						for (FeatureTerm g : unifications) {
							System.out.println(g.toStringNOOS(dm));
						}
					}
					List<FeatureTerm> gs = FTRefinement.variableEqualityEliminationAggressive(last, dm);
					System.out.println("Generalizations of original:");
					for (FeatureTerm g : gs) {
						System.out.println(g.toStringNOOS(dm));
						fail();
					}
				}
				last = property_term.m_b;
			}
		}
	}
}