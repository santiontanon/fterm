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

import java.util.LinkedList;
import java.util.List;

import ftl.base.core.FTAntiunification;
import ftl.base.core.FTKBase;
import ftl.base.core.FTRefinement;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Ontology;
import ftl.base.utils.FeatureTermException;
import ftl.base.utils.Pair;

// TODO: Auto-generated Javadoc
/**
 * The Class AUDistance.
 */
public class AUDistance extends Distance {

	/** The Constant DEBUG. */
	static public boolean DEBUG = false;
        
        int language = FTRefinement.ALL_REFINEMENTS;
        
        public AUDistance() {
            
        }
        
        public AUDistance(int a_language) {
            language = a_language;
        }

	/*
	 * (non-Javadoc)
	 * 
	 * @see csic.iiia.ftl.learning.lazymethods.similarity.Distance#distance(csic.iiia.ftl.base.core.FeatureTerm,
	 * csic.iiia.ftl.base.core.FeatureTerm, csic.iiia.ftl.base.core.Ontology, csic.iiia.ftl.base.core.FTKBase)
	 */
	public double distance(FeatureTerm f1, FeatureTerm f2, Ontology o, FTKBase dm) throws FeatureTermException {
		FeatureTerm au;

		int steps_au = 0;
		int additional_steps_f1 = 0;
		int additional_steps_f2 = 0;

		List<FeatureTerm> objects = new LinkedList<FeatureTerm>();

		if (DEBUG) {
			System.out.print(f1.getName() + " - " + f2.getName() + " [");
			System.out.flush();
		}
		// Count antiunification steps:
		{
			List<FeatureTerm> results;
			objects.add(f1);
			objects.add(f2);
			results = FTAntiunification.antiunification(objects, language, null, o, dm, true, FTAntiunification.VERSION_FAST);
			objects.clear();
			au = results.get(0);

			if (DEBUG) {
				System.out.print("* ");
				System.out.flush();
			}

			steps_au = termSize(au, dm, o);
		}
		if (DEBUG) {
			System.out.print("a = " + steps_au + " ");
			System.out.flush();
		}

		// Count additional f1 steps:

		{
			List<Pair<FeatureTerm, Integer>> results;
			List<Pair<FeatureTerm, Integer>> startl = new LinkedList<Pair<FeatureTerm, Integer>>();
			objects.add(f1);
			startl.add(new Pair<FeatureTerm, Integer>(au, 0));
			results = FTAntiunification.antiunificationCountingSteps(objects, language, startl, o, dm, false,
					FTAntiunification.VERSION_FAST);
			objects.clear();
			startl.clear();
			additional_steps_f1 = results.get(0).m_b;
		}

		// additional_steps_f1 = Math.max(termSize(f1,dm,o) - steps_au,0);

		if (DEBUG) {
			System.out.print("b = " + additional_steps_f1 + " ");
			System.out.flush();
		}

		// Count additional f2 steps:
		{
			List<Pair<FeatureTerm, Integer>> results;
			List<Pair<FeatureTerm, Integer>> startl = new LinkedList<Pair<FeatureTerm, Integer>>();
			objects.add(f2);
			startl.add(new Pair<FeatureTerm, Integer>(au, 0));
			results = FTAntiunification.antiunificationCountingSteps(objects, language, startl, o, dm, false,
					FTAntiunification.VERSION_FAST);
			objects.clear();
			startl.clear();
			additional_steps_f2 = results.get(0).m_b;
		}
		// additional_steps_f2 = Math.max(termSize(f2,dm,o) - steps_au,0);

		if (DEBUG) {
			System.out.print("c = " + additional_steps_f2 + "] -> ");
			System.out.flush();
		}

		double distance = 1.0f - (((double) (steps_au * 2)) / ((double) (steps_au * 2 + additional_steps_f1 + additional_steps_f2)));

		if (DEBUG) {
			System.out.println(distance);
			System.out.flush();
			// System.out.println(au.toStringNOOS(dm));
		}
		/*
		 * System.out.println(f1.toStringNOOS(dm)); System.out.flush(); System.out.println(f2.toStringNOOS(dm));
		 * System.out.flush(); System.out.println(au.toStringNOOS(dm)); System.out.flush();
		 */
		return distance;
	}

	/**
	 * Term size.
	 * 
	 * @param f
	 *            the f
	 * @param dm
	 *            the dm
	 * @param o
	 *            the o
	 * @return the int
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	int termSize(FeatureTerm f, FTKBase dm, Ontology o) throws FeatureTermException {
		int steps = 0;
		boolean done = false;

		while (!done) {
			List<FeatureTerm> generalizations = FTRefinement.getSomeGeneralizationsAggressive(f, dm, o);

			if (generalizations.isEmpty()) {
				// System.out.println(f.toStringNOOS(dm));
				done = true;
			} else {
				steps++;
				f = generalizations.get(0);
			}

			// System.out.println(steps + " - " + generalizations.size());
			// if ((steps%100==0)) System.out.println(f.toStringNOOS(dm));
		}

		return steps;
	}

}
