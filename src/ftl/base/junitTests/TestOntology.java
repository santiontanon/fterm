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

import static org.junit.Assert.*;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ftl.base.core.BaseOntology;
import ftl.base.core.FTKBase;
import ftl.base.core.Ontology;
import ftl.base.utils.FeatureTermException;

// TODO: Auto-generated Javadoc
/**
 * The Class TestOntology.
 * 
 * @author clopez
 */
public class TestOntology {

	/** The o. */
	Ontology o = new Ontology();

	/** The base_ontology. */
	Ontology base_ontology = null;

	/** The base_domain_model. */
	FTKBase base_domain_model;

	/** The domain_model_trains. */
	FTKBase domain_model_trains;

	/** The domain_model_toxicology. */
	FTKBase domain_model_toxicology;

	/** The domain_model_families. */
	FTKBase domain_model_families;

	/** The cb_trains. */
	FTKBase cb_trains;

	/** The cb_toxicology. */
	FTKBase cb_toxicology;

	/**
	 * Sets the up.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Before
	public void setUp() throws Exception {
		base_domain_model = new FTKBase();
		domain_model_trains = new FTKBase();
		domain_model_toxicology = new FTKBase();
		domain_model_families = new FTKBase();
		cb_trains = new FTKBase();
		cb_toxicology = new FTKBase();
		base_ontology = new BaseOntology();
		o = new Ontology();
		o.uses(base_ontology);

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
	 * Procedimiento recurrente llamada por varios JUnitTest @throws FeatureTermException.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public void domain() throws FeatureTermException {
		base_domain_model.create_boolean_objects(base_ontology);
		domain_model_trains.uses(base_domain_model);
		domain_model_toxicology.uses(base_domain_model);
		domain_model_families.uses(base_domain_model);
	}

	/**
	 * Test ontology initialization test n sorts is6.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	@Test
	public void testOntologyInitializationTestNSortsIs6() throws FeatureTermException {
		int expectedValue = 6;
		assertEquals(expectedValue, base_ontology.getNSorts());
	}

	/**
	 * Test ontology initialization test n undefined sorts is0.
	 */
	@Test
	public void testOntologyInitializationTestNUndefinedSortsIs0() {
		int expected = 0;
		assertEquals(expected, base_ontology.getNUndefinedSorts());
	}

	/**
	 * Test ontology initialization test after domain nsorts is6.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	@Test
	public void testOntologyInitializationTestAfterDomainNsortsIs6() throws FeatureTermException {
		domain();
		int expected = 6;
		assertEquals(expected, base_ontology.getNSorts());

	}

	/**
	 * Test ontology initialization test after domain n terms is2.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	@Test
	public void testOntologyInitializationTestAfterDomainNTermsIs2() throws FeatureTermException {
		domain();
		int expected = 2;
		assertEquals(expected, base_domain_model.getNTerms());
	}

	/**
	 * Test ontology initialization test after noo sn nundefine terms is0.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	@Test
	public void testOntologyInitializationTestAfterNOOSnNundefineTermsIs0() throws FeatureTermException {
		domain();
		int expected = 0;
		assertEquals(expected, base_domain_model.get_n_undefined_terms());
	}

	/**
	 * Test ontology initialization test after noos nsort is16.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testOntologyInitializationTestAfterNOOSNsortIs16() throws FeatureTermException, IOException {
		domain();
		domain_model_trains.importNOOS("Resources/DATA/trains-ontology.noos", o);
		int expected = 16;
		assertEquals(expected, o.getNSorts());
	}

	/**
	 * Test ontology initialization test after noo stoxicology nsort is235.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testOntologyInitializationTestAfterNOOStoxicologyNsortIs235() throws FeatureTermException, IOException {
		domain();
		domain_model_trains.importNOOS("Resources/DATA/trains-ontology.noos", o);
		domain_model_toxicology.importNOOS("Resources/DATA/toxic-eva-ontology.noos", o);
		int expected = 235;
		assertEquals(expected, o.getNSorts());
	}

	/**
	 * Test ontology initialization test after noo strains get sort is10.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testOntologyInitializationTestAfterNOOStrainsGetSortIs10() throws FeatureTermException, IOException {
		domain();

		domain_model_trains.importNOOS("Resources/DATA/trains-ontology.noos", o);
		domain_model_trains.importNOOS("Resources/DATA/trains-dm.noos", o);
		cb_trains.uses(domain_model_trains);
		cb_trains.importNOOS("Resources/DATA/trains-cases-10.noos", o);
		int expected = 10;
		assertEquals(expected, cb_trains.searchFT(o.getSort("trains-description")).size());
	}

	// Este test no encuentra el archivo toxic-eva-cases-50.noos
	// @Test
	// public void testOntologyInitializationTestAfterNOOStoxicGetSortIs47() throws FeatureTermException, IOException{
	// cb_toxicology.uses(domain_model_toxicology);
	// domain_model_toxicology.ImportNOOS("Resources/DATA/toxic-eva-dm.noos", o);
	// cb_toxicology.ImportNOOS("Resources/DATA/toxic-eva-cases-50.noos", o);
	// cb_toxicology.print_status();
	// int expected = 47;
	// assertEquals(expected,cb_toxicology.SearchFT(o.getSort("toxic-problem")).size());
	// }

}
