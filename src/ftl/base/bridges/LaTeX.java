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
import ftl.base.core.FeatureTerm;
import ftl.base.core.FloatFeatureTerm;
import ftl.base.core.IntegerFeatureTerm;
import ftl.base.core.SetFeatureTerm;
import ftl.base.core.Symbol;
import ftl.base.core.SymbolFeatureTerm;
import ftl.base.core.TermFeatureTerm;

// TODO: Auto-generated Javadoc
/**
 * The Class LaTeX.
 * 
 * @author santi
 */
public class LaTeX {

	/**
	 * To la te x term.
	 * 
	 * @param f
	 *            the f
	 * @param dm
	 *            the dm
	 * @param separateConstants
	 *            the separate constants
	 * @return the string
	 * @throws Exception 
	 */
	public static String toLaTeXTerm(FeatureTerm f, FTKBase dm, boolean separateConstants) throws Exception {
		String tmp;
		List<FeatureTerm> bindings = new LinkedList<FeatureTerm>();

		tmp = toLaTeXTermInternal(f, bindings, dm, separateConstants);
		tmp = tmp.replaceAll("-", "{\\text -}");

		return "\\[\n \\psi ::= " + tmp + "\n\\]";
	}

	/**
	 * To la te x term internal.
	 * 
	 * @param f
	 *            the f
	 * @param bindings
	 *            the bindings
	 * @param dm
	 *            the dm
	 * @param separateConstants
	 *            the separate constants
	 * @return the string
	 * @throws Exception 
	 */
	private static String toLaTeXTermInternal(FeatureTerm f, List<FeatureTerm> bindings, FTKBase dm, boolean separateConstants) throws Exception {
		if (f instanceof TermFeatureTerm) {
			return toLaTeXTermInternalTerm((TermFeatureTerm) f, bindings, dm, separateConstants);
		} else if (f instanceof SetFeatureTerm) {
			return toLaTeXTermInternalSet((SetFeatureTerm) f, bindings, dm, separateConstants);
		} else if (f instanceof SymbolFeatureTerm) {
			return toLaTeXTermInternalConstant(f, bindings, dm, separateConstants);
		} else if (f instanceof IntegerFeatureTerm) {
			return toLaTeXTermInternalConstant(f, bindings, dm, separateConstants);
		} else if (f instanceof FloatFeatureTerm) {
			return toLaTeXTermInternalConstant(f, bindings, dm, separateConstants);
		} else {
			return toLaTeXTermInternalAbstract(f, bindings, dm, separateConstants);
		}

	}

	/**
	 * To la te x term internal abstract.
	 * 
	 * @param f
	 *            the f
	 * @param bindings
	 *            the bindings
	 * @param dm
	 *            the dm
	 * @param separateConstants
	 *            the separate constants
	 * @return the string
	 */
	private static String toLaTeXTermInternalAbstract(FeatureTerm f, List<FeatureTerm> bindings, FTKBase dm, boolean separateConstants) throws Exception  {
		String tmp = "";
		int ID = -1;

		if (separateConstants && (f.isConstant() || dm.contains(f))) {
			ID = -1;
		} else {
			ID = bindings.indexOf(f);
		}
		if (ID == -1) {
			bindings.add(f);
			ID = bindings.indexOf(f);

			if (f.getName() != null && dm != null && dm.contains(f)) {
				return tmp += "X_{" + (ID + 1) + "} : " + f.getName().get();
			}

			tmp += "X_{" + (ID + 1) + "} : " + f.getSort().get();

			return tmp;
		} else {
			return "X_{" + (ID + 1) + "}";
		} // if
	}

	/**
	 * To la te x term internal constant.
	 * 
	 * @param f
	 *            the f
	 * @param bindings
	 *            the bindings
	 * @param dm
	 *            the dm
	 * @param separateConstants
	 *            the separate constants
	 * @return the string
	 */
	private static String toLaTeXTermInternalConstant(FeatureTerm f, List<FeatureTerm> bindings, FTKBase dm, boolean separateConstants) {
		String tmp = "";
		int ID = -1;

		if (separateConstants && (f.isConstant() || dm.contains(f))) {
			ID = -1;
		} else {
			ID = bindings.indexOf(f);
		}
		if (ID == -1) {
			bindings.add(f);
			ID = bindings.indexOf(f);

			tmp += "X_{" + (ID + 1) + "} : " + f.toStringNOOS(dm);

			return tmp;
		} else {
			return "X_{" + (ID + 1) + "}";
		} // if
	}

	/**
	 * To la te x term internal set.
	 * 
	 * @param f
	 *            the f
	 * @param bindings
	 *            the bindings
	 * @param dm
	 *            the dm
	 * @param separateConstants
	 *            the separate constants
	 * @return the string
	 * @throws Exception 
	 */
	private static String toLaTeXTermInternalSet(SetFeatureTerm f, List<FeatureTerm> bindings, FTKBase dm, boolean separateConstants) throws Exception {
		String tmp = "";

		List<FeatureTerm> values = f.getSetValues();

		if (values.size() > 0) {
			if (values.size() == 1) {
				return toLaTeXTermInternal(values.get(0), bindings, dm, separateConstants);
			} else {
				tmp += "\\left\\{\n";
				tmp += "\\begin{array}{l}\n";
				for (FeatureTerm value : values) {
					tmp += toLaTeXTermInternal(value, bindings, dm, separateConstants);
					tmp += "\\\\\n";
				}
				tmp += "\\end{array}\n";
				tmp += "\\right.";
			}
		} else {
			tmp += "\\emptyset\\n";
		}

		return tmp;
	}

	/**
	 * To la te x term internal term.
	 * 
	 * @param f
	 *            the f
	 * @param bindings
	 *            the bindings
	 * @param dm
	 *            the dm
	 * @param separateConstants
	 *            the separate constants
	 * @return the string
	 * @throws Exception 
	 */
	private static String toLaTeXTermInternalTerm(TermFeatureTerm f, List<FeatureTerm> bindings, FTKBase dm, boolean separateConstants) throws Exception {
		String tmp = "";
		int ID = -1;

		if (separateConstants && (f.isConstant() || dm.contains(f))) {
			ID = -1;
		} else {
			ID = bindings.indexOf(f);
		}
		if (ID == -1) {
			bindings.add(f);
			ID = bindings.indexOf(f);

			if (f.getName() != null && dm != null && dm.contains(f)) {
				return tmp += "X_{" + (ID + 1) + "} : " + f.getName().get();
			}

			tmp += "X_{" + (ID + 1) + "} : " + f.getSort().get();

			// features:
			Set<Symbol> features = f.getFeatureNames();

			if (features.size() > 0) {
				tmp += "\\left[\n";

				if (features.size() > 1) {
					tmp += "\\begin{array}{l}\n";
				}

				for (Symbol feature : features) {
					FeatureTerm fv = f.featureValue(feature);

					tmp += feature.get() + " \\doteq " + toLaTeXTermInternal(fv, bindings, dm, separateConstants);

					if (features.size() > 1) {
						tmp += "\\\\\n";
					}
				}

				if (features.size() > 1) {
					tmp += "\\end{array}\n";
				}

				tmp += "\\right]";
			}

			return tmp;
		} else {
			return "X_{" + (ID + 1) + "}";
		} // if
	}

}
