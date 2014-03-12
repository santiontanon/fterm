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

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ftl.base.bridges.NOOSParser;
import ftl.base.core.BaseOntology;
import ftl.base.core.FTKBase;
import ftl.base.core.FTUnification;
import ftl.base.core.FeatureTerm;
import ftl.base.core.FloatFeatureTerm;
import ftl.base.core.IntegerFeatureTerm;
import ftl.base.core.Ontology;
import ftl.base.core.Symbol;
import ftl.base.core.SymbolFeatureTerm;
import ftl.base.utils.FeatureTermException;
import ftl.base.utils.RewindableInputStream;

/**
 * The Class TestUnification.
 */
public class TestUnification {

	/** The dm. */
	FTKBase dm = new FTKBase();

	/** The o. */
	Ontology o = new Ontology();

	/** The f1. */
	FeatureTerm f1 = null;

	/** The f2. */
	FeatureTerm f2 = null;

	/** The expected. */
	int expected = 0;

	/** The unifications. */
	List<FeatureTerm> unifications;

	/** The case_base. */
	FTKBase case_base = new FTKBase();

	/**
	 * Sets the up.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Before
	public void setUp() throws Exception {
		case_base = new FTKBase();
		case_base.uses(dm);
		Ontology base_ontology = new BaseOntology();
		o.uses(base_ontology);
		FTKBase base_domain_model = new FTKBase();

		base_domain_model.create_boolean_objects(base_ontology);
		dm.importNOOS("Resources/DATA/family-ontology.noos", o);
		dm.importNOOS("Resources/DATA/family-dm.noos", o);
		dm.importNOOS("Resources/DATA/zoology-ontology.noos", o);
		dm.importNOOS("Resources/DATA/zoology-dm.noos", o);
		dm.importNOOS("Resources/DATA/sponge-ontology.noos", o);
		dm.importNOOS("Resources/DATA/sponge-dm.noos", o);
		dm.importNOOS("Resources/DATA/trains-ontology.noos", o);
		dm.importNOOS("Resources/DATA/trains-dm.noos", o);

	}

	/**
	 * Test subsumptionf1f2integer.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	@Test
	public void TestSubsumptionf1f2integer() throws FeatureTermException {
		f1 = new IntegerFeatureTerm(null, o);
		f2 = new IntegerFeatureTerm(1, o);
		expected = 1;
		unification();
	}

	/**
	 * Test subsumtpionf1f2float.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	@Test
	public void TestUnificationf1f2float() throws FeatureTermException {
		f1 = new FloatFeatureTerm(null, o);
		f2 = new FloatFeatureTerm(1.0f, o);
		expected = 1;
		unification();
	}

	/**
	 * Test subsumtpionf1f2symbol.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	@Test
	public void TestUnificationf1f2symbol() throws FeatureTermException {
		f1 = new SymbolFeatureTerm(null, o);
		f2 = new SymbolFeatureTerm(new Symbol("hola"), o);
		expected = 1;
		unification();
	}

	/**
	 * N parser.
	 * 
	 * @param v1
	 *            the v1
	 * @param v2
	 *            the v2
	 * @throws UnsupportedEncodingException
	 *             the unsupported encoding exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public void nParser(String v1, String v2) throws UnsupportedEncodingException, IOException, FeatureTermException {
		f1 = null;
		f2 = null;
		f1 = NOOSParser.parse(new RewindableInputStream(new ByteArrayInputStream(v1.getBytes("UTF-8"))), case_base, o);
		f2 = NOOSParser.parse(new RewindableInputStream(new ByteArrayInputStream(v2.getBytes("UTF-8"))), case_base, o);
	}

	/**
	 * Test subsumtpionf1f2 noos family1.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws UnsupportedEncodingException
	 *             the unsupported encoding exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void TestUnificationf1f2NoosFamily1() throws FeatureTermException, UnsupportedEncodingException, IOException {

		String var = new String("(define (person) " + "  (son (define (male)))" + ")");

		String var2 = new String("(define (female) " + "  (son (define (person) (son (define (male))))))" + ")");

		nParser(var, var2);

		expected = 1;
		unification();
	}

	/**
	 * Test subsumtpionf1f2 noos family2.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws UnsupportedEncodingException
	 *             the unsupported encoding exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void TestUnificationf1f2NoosFamily2() throws FeatureTermException, UnsupportedEncodingException, IOException {

		String var = new String("(define (person) " + "  (GRANDFATHER (define (male) " + "         (wife (define (female)) )) " + "       (define (male)))"
				+ ")");

		String var2 = new String("(define (female) " + "  (GRANDFATHER (define (person) " + "         (son (define (male)))))" + ")");

		nParser(var, var2);
		expected = 3;
		unification();
	}

	/**
	 * Test subsumtpionf1f2 noos family3.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws UnsupportedEncodingException
	 *             the unsupported encoding exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void TestUnificationf1f2NoosFamily3() throws FeatureTermException, UnsupportedEncodingException, IOException {
		String var = new String("(define ?X1 (male) " + "  (son (define (male) " + "         (father !X1)) " + "  ) " + ") ");
		String var2 = new String("(define (male)) ");
		nParser(var, var2);

		expected = 1;
		unification();
	}

	/**
	 * Test subsumtpionf1f2 noos family4.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws UnsupportedEncodingException
	 *             the unsupported encoding exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void TestUnificationf1f2NoosFamily4() throws FeatureTermException, UnsupportedEncodingException, IOException {
		String var = new String("(define ?X1 (male) " + "  (son (define (male) " + "         (wife (define (female))) " + "         (father !X1)) " + "  ) "
				+ ")");
		String var2 = "(define (male) " + "  (son (define (male) " + "         (father (define (male)))) " + "  ))";

		nParser(var, var2);
		expected = 1;
		unification();
	}

	/**
	 * Test subsumtpionf1f2 noos family5.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws UnsupportedEncodingException
	 *             the unsupported encoding exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void TestUnificationf1f2NoosFamily5() throws FeatureTermException, UnsupportedEncodingException, IOException {
		String var = new String("(define (person) " + "  (son (define ?X1 (male))) " + "  (grandfather !X1) " + ") ");

		String var2 = "(define (male) " + "  (son (define (male) " + "         (uncle (define (male))))) " + "  (grandfather (define (male) "
				+ "            (mother (define (female))))) " + ") ";

		nParser(var, var2);
		expected = 2;
		unification();
	}

	/**
	 * Test subsumtpionf1f2 noos sponge1.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws UnsupportedEncodingException
	 *             the unsupported encoding exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void TestUnificationf1f2NoosSponge1() throws FeatureTermException, UnsupportedEncodingException, IOException {
		String var = new String("(define (sponge-problem) " + " (description (define (sponge) " + " (spiculate-skeleton (define (spiculate-skeleton) "
				+ " (megascleres (define (megascleres) " + " (smooth-form style))))))))");

		String var2 = "(define (sponge-problem) " + " (description (define (sponge) " + " (spiculate-skeleton (define (spiculate-skeleton) "
				+ " (megascleres (define (megascleres) " + " (smooth-form oxea))))))))";

		nParser(var, var2);
		expected = 1;
		unification();
	}

	/**
	 * Test subsumtpionf1f2 noos family6.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws UnsupportedEncodingException
	 *             the unsupported encoding exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void TestUnificationf1f2NoosFamily6() throws FeatureTermException, UnsupportedEncodingException, IOException {
		String var = new String("(define (female) " + "                     (brother (define (male) "
				+ "                                (father (define (male))))))");

		String var2 = "(define ?X3 (female) " + "                     (brother (define ?X4 (male) " + "                                (sister !X3))) "
				+ "                     (father (define (male) " + "                               (son !X4) "
				+ "                               (daughter !X3))))";

		nParser(var, var2);
		expected = 1;
		unification();
	}

	/**
	 * Test subsumtpionf1f2 noos family7.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws UnsupportedEncodingException
	 *             the unsupported encoding exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void TestUnificationf1f2NoosFamily7() throws FeatureTermException, UnsupportedEncodingException, IOException {
		String var = new String("(define (female) " + "                 (mother (define (female) "
				+ "                           (husband (define ?X5 (male))))) " + "                 (brother (define (male) "
				+ "                            (father !X5))))");

		String var2 = "(define ?X3 (female) " + "                 (father (define ?X4 (male) " + "                           (daughter !X3) "
				+ "                           (son (define ?X5 (male) " + "                                  (sister !X3) "
				+ "                                  (father !X4) " + "                                  (mother (define ?X7 (female) "
				+ "                                            (husband (define (male))))))) " + "                           (wife !X7))) "
				+ "                 (mother !X7) " + "                 (brother !X5))";

		nParser(var, var2);

		expected = 1;
		unification();
	}

	/**
	 * Test subsumtpionf1f2 noos trains1.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws UnsupportedEncodingException
	 *             the unsupported encoding exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void TestUnificationf1f2NoosTrains1() throws FeatureTermException, UnsupportedEncodingException, IOException {
		String var = new String("(define (trains-description)" + "   (cars (define (car)" + "          (nwhl 2))))");

		String var2 = "(define (trains-description) " + "  (cars (define (set) " + "          (define (car) " + "           (infront (define ?X4 (car) "
				+ "                      (nwhl (define (integer))) " + "                      (loc 2) " + "                      (npl 3) "
				+ "                      (cshape closedrect) " + "                      (infront (define ?X8 (car) "
				+ "                                 (lcont (define (trianglod))) " + "                                 (nwhl 2) "
				+ "                                 (ln short) " + "                                 (loc 3) " + "                                 (npl 1) "
				+ "                                 (cshape openrect)))))) " + "          !X4 " + "          !X8)))";

		nParser(var, var2);

		expected = 1;
		unification();
	}

	/**
	 * Test subsumtpionf1f2 noos trains2.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws UnsupportedEncodingException
	 *             the unsupported encoding exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void TestUnificationf1f2NoosTrains2() throws FeatureTermException, UnsupportedEncodingException, IOException {
		String var = new String("(define (trains-description) " + "  (ncar 3) " + "  (cars (define (set) " + "          (define (car) "
				+ "           (infront (define ?X5 (car) " + "                      (lcont (define (set) "
				+ "                               (define (circlelod)) " + "                               (define (circlelod)) "
				+ "                               )) " + "                      (infront (define ?X7 (car) "
				+ "                                 (lcont (define (trianglod))) " + "                                 (npl 1) "
				+ "                                 (nwhl 2) " + "                                 (loc 3) "
				+ "                                 (cshape openrect) " + "                                 (ln short))) " + "                      (npl 3) "
				+ "                      (nwhl 2) " + "                      (loc 2) " + "                      (cshape closedrect) "
				+ "                      (ln long))) " + "           (npl 0) " + "           (nwhl 2) " + "           (loc 1) " + "           (cshape engine) "
				+ "           (ln long)) " + "          !X5 " + "          !X7)))");

		String var2 = "(define (trains-description) " + "  (ncar 3) " + "  (cars (define (set) " + "          (define (car) "
				+ "           (infront (define ?X5 (car) " + "                      (lcont (define (circlelod))) "
				+ "                      (infront (define ?X6 (car) " + "                                 (lcont (define (trianglod))) "
				+ "                                 (npl 1) " + "                                 (loc 3) " + "                                 (nwhl 2) "
				+ "                                 (cshape openrect) " + "                                 (ln short))) " + "                      (npl 3) "
				+ "                      (loc 2) " + "                      (nwhl 2) " + "                      (cshape closedrect) "
				+ "                      (ln long))) " + "           (npl 0) " + "           (loc 1) " + "           (nwhl 2) " + "           (cshape engine) "
				+ "           (ln long)) " + "          !X5 " + "          !X6)))";

		nParser(var, var2);

		// System.out.println(LaTeX.toLaTeXTerm(f1, dm, true));
		// System.out.println(ClausalForm.toLaTeXTerm(f1, dm, true));

		expected = 1;
		unification();
	}

	/**
	 * Test subsumtpionf1f2 noos trains3.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws UnsupportedEncodingException
	 *             the unsupported encoding exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void TestUnificationf1f2NoosTrains3() throws FeatureTermException, UnsupportedEncodingException, IOException {
		String var = new String("(define (car)" + "  (lcont (define (set)" + "    (define (load))" + "    (define (load))" + "    (define (circlelod))))" + ")");

		String var2 = "(define (car)" + "  (lcont (define (set)" + "    (define (load))" + "    (define (circlelod))" + "    (define (circlelod))))" + ")";

		nParser(var, var2);
		expected = 1;
		unification();
	}

	/**
	 * Test subsumtpionf1f2 noos trains4.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws UnsupportedEncodingException
	 *             the unsupported encoding exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void TestUnificationf1f2NoosTrains4() throws FeatureTermException, UnsupportedEncodingException, IOException {
		String var = new String("(define (trains-description) " + "  (cars (define (set) " + "          (define (car) "
				+ "           (infront (define ?X4 (car) " + "                      (infront (define ?X5 (car)))))) " + "          !X4 " + "          !X5 "
				+ "          (define (car)))))");

		String var2 = "(define (trains-description) " + "  (cars (define (set) " + "          (define (car) " + "           (infront (define ?X8 (car)))) "
				+ "          !X8)))";

		nParser(var, var2);
		expected = 1;
		unification();
	}

	/**
	 * Test subsumtpionf1f2 noos trains5.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws UnsupportedEncodingException
	 *             the unsupported encoding exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void TestUnificationf1f2NoosTrains5() throws FeatureTermException, UnsupportedEncodingException, IOException {
		String var = new String("(define (trains-description) " + "  (cars (define (set) " + "          (define (car) "
				+ "           (infront (define ?X4 (car) " + "                      (infront (define ?X5 (car) "
				+ "                                 (infront (define ?X6 (car)))))))) " + "          !X4 " + "          !X5 " + "          !X6 "
				+ "          (define (car)))))");

		String var2 = "(define (trains-description) " + "  (cars (define (set) " + "          (define (car) " + "           (infront (define ?X4 (car)))) "
				+ "          !X4 " + "          (define (car) " + "           (infront (define (car)))) " + "          (define (car) "
				+ "           (infront (define ?X8 (car)))) " + "          !X8)))";

		nParser(var, var2);
		expected = 1;
		unification();
	}

	/**
	 * Unification.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	private void unification() throws FeatureTermException {
		// System.out.println("F1:\n" + f1.toStringNOOS(dm));
		// System.out.println("F2:\n" + f2.toStringNOOS(dm));

		unifications = FTUnification.unification(f1, f2, dm);
		if (unifications != null && !unifications.isEmpty()) {
			System.out.println("Unification successful!");
			System.out.println(unifications.size() + " unifications");

			for (FeatureTerm u : unifications) {
				System.out.println(u.toStringNOOS(dm));
				if (!f1.subsumes(u)) {
					fail("F1 not subsumes U");
				}
				if (!f2.subsumes(u)) {
					fail("F2 not subsumes U");
				}
			}
			if (expected != unifications.size()) {
				fail("Unifications size unexpected");
			}
		} else {
			if (expected != 0) {
				fail("Not unifications?");
			}
		}
	}
}
