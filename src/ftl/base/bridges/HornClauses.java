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
import java.util.Set;

import ftl.base.core.FTKBase;
import ftl.base.core.FTRefinement;
import ftl.base.core.FeatureTerm;
import ftl.base.core.FloatFeatureTerm;
import ftl.base.core.IntegerFeatureTerm;
import ftl.base.core.Ontology;
import ftl.base.core.SetFeatureTerm;
import ftl.base.core.Symbol;
import ftl.base.utils.FeatureTermException;

/**
 * 
 * @author santi
 */
public class HornClauses {

	/**
	 * To clauses.
	 * 
	 * @param f
	 *            the f
	 * @param dm
	 *            the dm
	 * @param prefix
	 *            the prefix
	 * @return the list
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public static List<Clause> toClauses(FeatureTerm f, FTKBase dm, String prefix) throws FeatureTermException { // Quitado
																													// par�metro
																													// Ontology
		List<Clause> clauses = new LinkedList<Clause>();
		List<FeatureTerm> vs_tmp = FTRefinement.variables(f);
		List<FeatureTerm> vs = new LinkedList<FeatureTerm>();
		int count = 0;

		// Make sure that the root is the first element:
		vs_tmp.remove(f);
		vs.add(f);
		vs.addAll(vs_tmp);

		for (FeatureTerm v : vs) {

			if (!dm.contains(v) && !v.isConstant()) {
				// sorts:
				{
					Clause c = new Clause(v.getSort().get());
					c.setParameter(0, Clause.TYPE_ID, prefix + count);
					clauses.add(c);
				}

				// Features:
				for (Symbol feature : v.getSort().getFeatures()) {
					FeatureTerm tmpvalue = v.featureValue(feature);

					if (tmpvalue != null) {
						List<FeatureTerm> values = new LinkedList<FeatureTerm>();
						if (tmpvalue instanceof SetFeatureTerm) {
							values.addAll(((SetFeatureTerm) tmpvalue).getSetValues());
						} else {
							values.add(tmpvalue);
						}

						for (FeatureTerm value : values) {
							Clause c = new Clause(feature.get());
							c.setParameter(0, Clause.TYPE_ID, prefix + count);
							if (dm.contains(value)) {
								c.setParameter(1, Clause.TYPE_SYMBOL, value.toStringNOOS(dm));
							} else if (value.isConstant()) {
								if (value instanceof IntegerFeatureTerm) {
									c.setParameter(1, Clause.TYPE_INTEGER, value.toStringNOOS(dm));
								} else if (value instanceof FloatFeatureTerm) {
									c.setParameter(1, Clause.TYPE_FLOAT, value.toStringNOOS(dm));
								} else {
									c.setParameter(1, Clause.TYPE_SYMBOL, value.toStringNOOS(dm));
								}
							} else {
								c.setParameter(1, Clause.TYPE_ID, prefix + vs.indexOf(value));
							}
							clauses.add(c);
						}
					}
				}
			}

			count++;
		}

		return clauses;
	}
}
