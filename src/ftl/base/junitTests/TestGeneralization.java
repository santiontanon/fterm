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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ftl.base.bridges.NOOSParser;
import ftl.base.core.BaseOntology;
import ftl.base.core.FTKBase;
import ftl.base.core.FTRefinement;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Ontology;
import ftl.base.utils.FeatureTermException;
import ftl.base.utils.RewindableInputStream;
import ftl.learning.core.TrainingSetProperties;
import ftl.learning.core.TrainingSetUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class TestGeneralization.
 */
public class TestGeneralization {

	/** The f. */
	FeatureTerm f = null;

	/** The ts. */
	TrainingSetProperties ts = null;

	/** The l. */
	List<FeatureTerm> l = null;

	/**
	 * Sets the up.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Before
	public void setUp() throws Exception {
		Ontology base_ontology = new BaseOntology();
		Ontology o = new Ontology();
		FTKBase dm = new FTKBase();
		FTKBase cb = new FTKBase();
		o.uses(base_ontology);
		cb.uses(dm);
		FTKBase case_base = new FTKBase();
		ts = TrainingSetUtils.loadTrainingSet(TrainingSetUtils.TRAINS_DATASET, o, dm, cb);
		f = NOOSParser.parse(new RewindableInputStream(new ByteArrayInputStream(("(define (trains-description) " + "  (cars (define (set) "
				+ "          (define (car) " + "           (infront (define ?X4 (car)))) " + "          !X4)))").getBytes("UTF-8"))), case_base, o);

		l = FTRefinement.getGeneralizationsAggressive(f, dm, o);
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
	 * Test generalization.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testGeneralization() throws FeatureTermException, IOException {
		for (FeatureTerm r : l) {
			if (!r.subsumes(f)) {
				fail();
			} else {
				if (f.subsumes(r)) {
					fail();
				}
			}
		}
	}
}
