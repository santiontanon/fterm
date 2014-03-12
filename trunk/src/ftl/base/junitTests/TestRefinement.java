/**
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
  
 package ftl.base.junitTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ftl.base.bridges.NOOSParser;
import ftl.base.core.BaseOntology;
import ftl.base.core.FTKBase;
import ftl.base.core.FTRefinement;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Ontology;
import ftl.base.core.SetFeatureTerm;
import ftl.base.core.Symbol;
import ftl.base.core.TermFeatureTerm;
import ftl.base.utils.FeatureTermException;
import ftl.base.utils.RewindableInputStream;

// TODO: Auto-generated Javadoc
/**
 * The Class TestRefinement.
 * 
 * @author clopez Specialization Refinements tests
 */
public class TestRefinement {

	/** The base_ontology. */
	Ontology base_ontology = null;

	/** The o. */
	Ontology o = new Ontology();

	/** The base_domain_model. */
	FTKBase base_domain_model = new FTKBase();

	/** The domain_model_trains. */
	FTKBase domain_model_trains = new FTKBase();

	/** The domain_model_toxicology. */
	FTKBase domain_model_toxicology = new FTKBase();

	/** The domain_model_families. */
	FTKBase domain_model_families = new FTKBase();

	/** The x1. */
	TermFeatureTerm x1 = null;

	/** The x2. */
	FeatureTerm x2 = null;

	/** The x3. */
	FeatureTerm x3 = null;

	/** The x4. */
	FeatureTerm x4 = null;

	/** The set1. */
	SetFeatureTerm set1 = new SetFeatureTerm();

	/** The cb_families. */
	FTKBase cb_families = new FTKBase();

	/**
	 * Sets the up.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Before
	public void setUp() throws Exception {
		base_ontology = new BaseOntology();
		o.uses(base_ontology);
		base_domain_model.create_boolean_objects(base_ontology);
		domain_model_trains.uses(base_domain_model);
		domain_model_toxicology.uses(base_domain_model);
		domain_model_families.uses(base_domain_model);
		x1 = new TermFeatureTerm((Symbol) null, o.getSort("person"));

		base_domain_model.importNOOS("Resources/DATA/simple-family-ontology.noos", o);
		x2 = new TermFeatureTerm((Symbol) null, o.getSort("male"));
		x3 = new TermFeatureTerm((Symbol) null, o.getSort("female"));
		x4 = new TermFeatureTerm((Symbol) null, o.getSort("person"));

		set1.addSetValue(x3);
		set1.addSetValue(x4);
		x1.defineFeatureValue(new Symbol("father"), x2);
		x1.defineFeatureValue(new Symbol("children"), set1);
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
	 * Test variable size is4.
	 */
	@Test
	public void testVariableSizeIs4() {
		int expected = 4;
		List<FeatureTerm> variables = FTRefinement.variables(x1);
		assertEquals(expected, variables.size());
	}

	/**
	 * Test sort refinements is4.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	@Test
	public void testSortRefinementsIs4() throws FeatureTermException {
		List<FeatureTerm> sortRefinements = FTRefinement.sortSpecialization(x1, base_domain_model, null);
		int expected = 4;
		assertEquals(expected, sortRefinements.size());
	}

	/**
	 * Test feature refinements is20.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	@Test
	public void testFeatureRefinementsIs20() throws FeatureTermException {
		List<FeatureTerm> featureRefinements = FTRefinement.featureIntroduction(x1, base_domain_model, null);
		int expected = 20;
		assertEquals(expected, featureRefinements.size());
	}

	/**
	 * Test equality refinements is4.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	@Test
	public void testEqualityRefinementsIs4() throws FeatureTermException {
		List<FeatureTerm> equalityRefinements = FTRefinement.variableEqualityAddition(x1, base_domain_model, null);
		int expected = 4;
		assertEquals(expected, equalityRefinements.size());
	}

	/**
	 * Test set refinements is2.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	@Test
	public void testSetRefinementsIs2() throws FeatureTermException {
		List<FeatureTerm> setRefinements = FTRefinement.setExpansion(x1, base_domain_model, null);
		int expected = 2;
		assertEquals(expected, setRefinements.size());
	}

	/**
	 * Test refinements subsumes.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testRefinementsSubsumes() throws FeatureTermException, IOException {
		o.newSort("s", "any", new String[] { "f", "g" }, new String[] { "s", "s" });

		String var = new String("(define ?X1 (s) " + "  (f (define ?X2 (s) " + "       (f (define (s) " + "            (f (define (set) "
				+ "                 !X1 " + "                 !X2 " + "                 (define (s) " + "                  (f (define (s)))))))))))");

		FeatureTerm f1 = NOOSParser.parse(new RewindableInputStream(new ByteArrayInputStream(var.getBytes("UTF-8"))), base_domain_model, o);
		List<FeatureTerm> veRefinements = FTRefinement.variableEqualityAddition(f1, base_domain_model, null);

		for (FeatureTerm r : veRefinements) {
			if (!f1.subsumes(r)) {
				fail();
			}
		}
	}

	/**
	 * Trying to generate a loop of 2 out of a loop of 4 by variable equality refinement.
	 * 
	 * @throws UnsupportedEncodingException
	 *             the unsupported encoding exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	@Test
	public void testRefinement() throws UnsupportedEncodingException, IOException, FeatureTermException {
		testRefinementsSubsumes();

		String var = new String("(define ?X1 (s) " + "  (f (define (s) " + "    (f (define (s) " + "      (f (define (s) " + "        (f !X1))))))))");

		FeatureTerm f2 = NOOSParser.parse(new RewindableInputStream(new ByteArrayInputStream(var.getBytes("UTF-8"))), base_domain_model, o);

		List<FeatureTerm> open = new LinkedList<FeatureTerm>();
		List<FeatureTerm> closed = new LinkedList<FeatureTerm>();
		open.add(f2);
		while (!open.isEmpty()) {
			FeatureTerm f = open.remove(0);
			closed.add(f);
			List<FeatureTerm> l = FTRefinement.variableEqualityAddition(f, base_domain_model, null);
			for (FeatureTerm r : l) {
				boolean found = false;
				for (FeatureTerm tmp : open) {
					if (tmp.equivalents(r)) {
						found = true;
						break;
					}
				}
				if (!found)
					for (FeatureTerm tmp : closed) {
						if (tmp.equivalents(r)) {
							found = true;
							break;
						}
					}
				if (!found)
					open.add(r);
			}
		}
		System.out.println("Generating variable equality refinements of a 4 loop, found " + closed.size());
		// String ftext = new String(
		// "(define ?X1 (s) " +
		// "  (f (define (s) " +
		// "    (f !X1))))");
		String ftext = new String("(define ?X1 (s) " + "  (f (define (s) " + "    (f (define (s) " + "      (f (define (s) " + "        (f !X1))))))))");

		FeatureTerm f3 = NOOSParser.parse(new RewindableInputStream(new ByteArrayInputStream(ftext.getBytes("UTF-8"))), base_domain_model, o);

		boolean cond = false;
		for (FeatureTerm f : closed) {
			if (f.equivalents(f3))
				cond = true;
			System.out.println(f.toStringNOOS());
		}

		assertEquals(true, cond);
	}

	/**
	 * Generalization refinement.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	private void GeneralizationRefinement() throws IOException, FeatureTermException {
		domain_model_families.importNOOS("Resources/DATA/family-ontology.noos", o);
		domain_model_families.importNOOS("Resources/DATA/family-dm.noos", o);
		cb_families.uses(domain_model_families);
		cb_families.importNOOS("Resources/DATA/family-cases-test.noos", o);
	}

	/**
	 * Test find victoria.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	@Test
	public void testFindVictoria() throws IOException, FeatureTermException {

		GeneralizationRefinement();
		FeatureTerm root1, root2;
		Set<FeatureTerm> l = cb_families.searchFT(new Symbol("victoria"));
		int expected = 1;
		// root1 = l.iterator().next();
		assertEquals(expected, l.size());
	}

	/**
	 * Test train find t1.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	@Test
	public void testTrainFindT1() throws IOException, FeatureTermException {
		GeneralizationRefinement();
		domain_model_trains.importNOOS("Resources/DATA/trains-ontology.noos", o);
		domain_model_toxicology.importNOOS("Resources/DATA/toxic-eva-ontology.noos", o);
		FTKBase cb_trains = new FTKBase();
		domain_model_trains.importNOOS("Resources/DATA/trains-dm.noos", o);
		cb_trains.uses(domain_model_trains);
		cb_trains.importNOOS("Resources/DATA/trains-cases-10.noos", o);
		Set<FeatureTerm> l = cb_trains.searchFT(new Symbol("t1"));
		int expected = 1;
		assertEquals(expected, l.size());
	}

}
