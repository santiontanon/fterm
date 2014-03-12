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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ftl.base.bridges.NOOSParser;
import ftl.base.core.BaseOntology;
import ftl.base.core.FTKBase;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Ontology;
import ftl.base.utils.FeatureTermException;
import ftl.base.utils.RewindableInputStream;

// TODO: Auto-generated Javadoc
/**
 * The Class TestSubsumtion.
 * 
 * @author jcaceres
 */

public class TestSubsumtion {

	/** The base_ontology. */
	Ontology base_ontology = null;

	/** The o. */
	Ontology o = new Ontology();

	/** The o2. */
	Ontology o2 = new Ontology();

	/** The case_base. */
	FTKBase case_base;

	/** The base_domain_model. */
	FTKBase base_domain_model;

	/** The domain_model_trains. */
	FTKBase domain_model_trains;

	/** The domain_model_toxicology. */
	FTKBase domain_model_toxicology;

	/** The domain_model_families. */
	FTKBase domain_model_families;

	/** The l. */
	List<FeatureTerm> l;

	/** The s. */
	HashSet<FeatureTerm> s;

	/** The case_base2. */
	FTKBase case_base2;

	/** The dm. */
	FTKBase dm;

	/** The f1. */
	FeatureTerm f1 = null;

	/** The f2. */
	FeatureTerm f2 = null;

	/** The f3. */
	FeatureTerm f3 = null;

	/** The f4. */
	FeatureTerm f4 = null;

	/** The f5. */
	FeatureTerm f5 = null;

	/** The f6. */
	FeatureTerm f6 = null;

	/** The f7. */
	FeatureTerm f7 = null;

	/** The f8. */
	FeatureTerm f8 = null;

	/** The f9. */
	FeatureTerm f9 = null;

	/** The f10. */
	FeatureTerm f10 = null;

	/**
	 * Sets the up.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Before
	public void setUp() throws Exception {
		base_ontology = new BaseOntology();
		o = new Ontology();
		o.uses(base_ontology);

		case_base = new FTKBase();
		base_domain_model = new FTKBase();
		domain_model_trains = new FTKBase();
		domain_model_toxicology = new FTKBase();
		domain_model_families = new FTKBase();
		l = new LinkedList<FeatureTerm>();
		s = new HashSet<FeatureTerm>();
		case_base2 = new FTKBase();
		dm = new FTKBase();

		case_base.uses(base_domain_model);

		o2 = new Ontology();
		o2.uses(base_ontology);
		case_base2.uses(dm);
		domain_model_trains.importNOOS("Resources/DATA/trains-ontology.noos", o);
		f1 = NOOSParser.parse(new RewindableInputStream(new ByteArrayInputStream(("(define (symbol))").getBytes("UTF-8"))), case_base, o);
		f2 = NOOSParser.parse(new RewindableInputStream(new ByteArrayInputStream(("\"hello\"").getBytes("UTF-8"))), case_base, o);
		f3 = NOOSParser.parse(new RewindableInputStream(new ByteArrayInputStream(("\"bye\"").getBytes("UTF-8"))), case_base, o);
		f4 = NOOSParser.parse(new RewindableInputStream(new ByteArrayInputStream(("\"hello\"").getBytes("UTF-8"))), case_base, o);
		f5 = NOOSParser.parse(new RewindableInputStream(new ByteArrayInputStream(("(define (set))").getBytes("UTF-8"))), case_base, o);
		f6 = NOOSParser.parse(new RewindableInputStream(new ByteArrayInputStream(("(define (car))").getBytes("UTF-8"))), case_base, o);
		f7 = NOOSParser.parse(new RewindableInputStream(new ByteArrayInputStream(("(define (car) (infront (define (set))))").getBytes("UTF-8"))), case_base, o);
		f8 = NOOSParser.parse(new RewindableInputStream(new ByteArrayInputStream(("(define (car) (infront (define (car))))").getBytes("UTF-8"))), case_base, o);

		dm.create_boolean_objects(o2);
		dm.importNOOS("Resources/DATA/sponge-ontology.noos", o2);
		dm.importNOOS("Resources/DATA/sponge-dm.noos", o2);

		f9 = NOOSParser.parse(
				new RewindableInputStream(new ByteArrayInputStream(("(define (megascleres) (acanthose (define (acanthose))))").getBytes("UTF-8"))), case_base2,
				o2);
		f10 = NOOSParser.parse(new RewindableInputStream(new ByteArrayInputStream(("(define (megascleres) (acanthose no-acanthose))").getBytes("UTF-8"))),
				case_base2, o2);

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
	 * Test subsumtion f1subsumes f2.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testSubsumtionF1subsumesF2() throws FeatureTermException, IOException {
		assertTrue(f1.subsumes(f2));
	}

	/**
	 * Test subsumtion f2subsumes f4.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testSubsumtionF2subsumesF4() throws FeatureTermException, IOException {
		assertTrue(f2.subsumes(f4));
	}

	/**
	 * Test subsumtion f2 nosubsumes f3.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testSubsumtionF2NosubsumesF3() throws FeatureTermException, IOException {
		assertFalse(f2.subsumes(f3));
	}

	/**
	 * Test subsumtion f2equals f4.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testSubsumtionF2equalsF4() throws FeatureTermException, IOException {
		assertTrue(f2.equals(f4));
	}

	/**
	 * Test subsumtion f2equals f3.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testSubsumtionF2equalsF3() throws FeatureTermException, IOException {
		assertFalse(f2.equals(f3));
	}

	/**
	 * Test subsumtion f5subsumes f6.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testSubsumtionF5subsumesF6() throws FeatureTermException, IOException {
		assertTrue(f5.subsumes(f6));
	}

	/**
	 * Test subsumtion f6subsumes f7.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testSubsumtionF6subsumesF7() throws FeatureTermException, IOException {
		assertTrue(f6.subsumes(f7));
	}

	/**
	 * Test subsumtion f7subsumes f6.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testSubsumtionF7subsumesF6() throws FeatureTermException, IOException {
		assertTrue(f7.subsumes(f6));
	}

	/**
	 * Test subsumtion f7subsumes f8.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testSubsumtionF7subsumesF8() throws FeatureTermException, IOException {
		assertTrue(f7.subsumes(f8));
	}

	/**
	 * Test subsumtion list l sizeis3.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testSubsumtionListLSizeis3() throws FeatureTermException, IOException {
		if (!l.contains(f1))
			l.add(f1);
		if (!l.contains(f2))
			l.add(f2);
		if (!l.contains(f3))
			l.add(f3);
		if (!l.contains(f4))
			l.add(f4);
		int expected = 3;
		assertEquals(expected, l.size());
	}

	/**
	 * Test subsumtion lists sizeis3.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testSubsumtionListsSizeis3() throws FeatureTermException, IOException {
		if (!s.contains(f1))
			s.add(f1);
		if (!s.contains(f2))
			s.add(f2);
		if (!s.contains(f3))
			s.add(f3);
		if (!s.contains(f4))
			s.add(f4);
		int expected = 3;
		assertEquals(expected, s.size());
	}

	/**
	 * Test sponge subsumtion f9subsumes f10.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testSpongeSubsumtionF9subsumesF10() throws FeatureTermException, IOException {
		assertTrue(f9.subsumes(f10));

	}

	/**
	 * Test sponge subsumtion f10 nosubsumes f9.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testSpongeSubsumtionF10NosubsumesF9() throws FeatureTermException, IOException {
		assertFalse(f10.subsumes(f9));
	}

}
