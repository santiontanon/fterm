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

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ftl.base.core.FTKBase;
import ftl.base.core.FeatureTerm;
import ftl.base.core.FloatFeatureTerm;
import ftl.base.core.IntegerFeatureTerm;
import ftl.base.core.Ontology;
import ftl.base.core.Path;
import ftl.base.core.SetFeatureTerm;
import ftl.base.core.Sort;
import ftl.base.core.Symbol;
import ftl.base.core.SymbolFeatureTerm;
import ftl.base.core.TermFeatureTerm;
import ftl.base.utils.FeatureTermException;
import ftl.base.utils.RewindableInputStream;

// TODO: Auto-generated Javadoc
/**
 * The Class OntologyParser.
 */
public abstract class OntologyParser {
	/* Used to support the ">>" and "." symbols of the NOOS syntax: */
	/* assign f.feature = root.p */
	/**
	 * The Class NOOSPathRecord.
	 */
	static class NOOSPathRecord {

		/** The f. */
		public FeatureTerm f = null;

		/** The feature. */
		public Symbol feature = null;

		/** The root. */
		public FeatureTerm root = null;

		/** The p. */
		public Path p = null;
	};

	/* Used to support the "?x" and "!x" symbols of the enhanced NOOS syntax: */
	/**
	 * The Class NOOSVariableRecord.
	 */
	static class NOOSVariableRecord {

		/** The variable. */
		public Symbol variable = null;

		/** The f. */
		public FeatureTerm f = null;
	};

	/**
	 * The Class NOOSVariableLinkRecord.
	 */
	static class NOOSVariableLinkRecord {

		/** The f. */
		public FeatureTerm f = null;

		/** The feature. */
		public Symbol feature = null;

		/** The variable. */
		public Symbol variable = null;
	};

	/** The sort. */
	protected Sort sort;

	/** The token name. */
	protected String tokenName;

	/** The error message. */
	protected String errorMessage;

	/** The error. */
	protected boolean error = false;

	/** The feature_variablename. */
	protected StringBuilder feature_variablename;

	/** The num. */
	protected int[] num = new int[1];

	/** The fp. */
	protected RewindableInputStream fp;

	/**
	 * Setup string feature type.
	 * 
	 * @param o
	 *            the o
	 * @param string
	 *            the string
	 * @return the feature term
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	protected FeatureTerm setupStringFeatureType(Ontology o, String string) throws FeatureTermException {
		FeatureTerm fvalue;
		fvalue = new SymbolFeatureTerm(new Symbol(string), o);
		return fvalue;
	}

	/**
	 * Setup symbol.
	 * 
	 * @param vsort
	 *            the vsort
	 * @param m
	 *            the m
	 * @param symbolName
	 *            the symbol name
	 * @return the feature term
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	protected FeatureTerm setupSymbol(Sort vsort, FTKBase m, String symbolName) throws FeatureTermException {
		error = false;
		num = new int[1];
		FeatureTerm fvalue;
		fvalue = getFeatureValueOfSymbol(vsort, m, symbolName);
		return fvalue;
	}

	/**
	 * Adds the set values to set feature term.
	 * 
	 * @param f
	 *            the f
	 * @param fvalue
	 *            the fvalue
	 */
	protected void addSetValuesToSetFeatureTerm(FeatureTerm f, FeatureTerm fvalue) {
		if (fvalue != null)
			((SetFeatureTerm) f).addSetValue(fvalue);
		fvalue = null;
	}

	/**
	 * Adds the feature variable.
	 * 
	 * @param nvl
	 *            the nvl
	 * @param values_read
	 *            the values_read
	 * @param fvalue
	 *            the fvalue
	 * @param feature_variable
	 *            the feature_variable
	 * @param feature_variablename
	 *            the feature_variablename
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	protected void addFeatureVariable(List<NOOSVariableRecord> nvl, List<FeatureTerm> values_read, FeatureTerm fvalue, boolean feature_variable,
			String feature_variablename) throws FeatureTermException {
		if (fvalue == null)
			return;
		if (feature_variable) {
			NOOSVariableRecord nv;

                        nv = new NOOSVariableRecord();
			nv.f = fvalue;
			nv.variable = new Symbol(feature_variablename);
			nvl.add(nv);
			feature_variable = false;
		} // if
		values_read.add(fvalue);

	}

	/**
	 * Setup float feature type.
	 * 
	 * @param o
	 *            the o
	 * @param floatString
	 *            the float string
	 * @return the feature term
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	protected FeatureTerm setupFloatFeatureType(Ontology o, String floatString) throws FeatureTermException {
		FeatureTerm fvalue;
		fvalue = new FloatFeatureTerm(Float.parseFloat(floatString), o);
		return fvalue;
	}

	/**
	 * Setup integer feature type.
	 * 
	 * @param o
	 *            the o
	 * @param integerString
	 *            the integer string
	 * @return the feature term
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	protected FeatureTerm setupIntegerFeatureType(Ontology o, String integerString) throws FeatureTermException {
		FeatureTerm fvalue;
		fvalue = new IntegerFeatureTerm(Integer.parseInt(integerString), o);
		return fvalue;
	}

	/**
	 * Gets the feature value of symbol.
	 * 
	 * @param vsort
	 *            the vsort
	 * @param m
	 *            the m
	 * @param tokenName
	 *            the token name
	 * @return the feature value of symbol
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	protected FeatureTerm getFeatureValueOfSymbol(Sort vsort, FTKBase m, String tokenName) throws FeatureTermException {
		FeatureTerm res = null;
		FeatureTerm fvalue = null;

		Set<FeatureTerm> l;
		Symbol n = new Symbol(tokenName);

                if (vsort==null) {
                    throw new FeatureTermException("Cannot find symbol with name '" + tokenName + "'");
                }
                
		l = m.searchFT(n);
		for (FeatureTerm f2 : l) {
			if (vsort.inSort(f2)) {
				res = f2;
				num[0]++;
			} // if
		}

		if (num[0] == 1) {
			fvalue = res;
		} else {
			if (num[0] == 0) {
				// Create a new term of the appopiate sort and add it to the memory & undefined term list:
				System.err.println("undefined term: " + n);
				fvalue = new TermFeatureTerm(n, vsort);
				m.addUndefinedFT(fvalue);
			} // if
		} // if

		return fvalue;
	}

	/**
	 * Define ref variable.
	 * 
	 * @param f
	 *            the f
	 * @param nvll
	 *            the nvll
	 * @param nameOfVariable
	 *            the name of variable
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	protected void defineRefVariable(FeatureTerm f, List<NOOSVariableLinkRecord> nvll, String nameOfVariable) throws FeatureTermException {
		NOOSVariableLinkRecord nv = new NOOSVariableLinkRecord();
		nv.f = f;
		nv.feature = null;
		nv.variable = new Symbol(nameOfVariable);
		nvll.add(nv);
	}

	/**
	 * Adds the symbols of root to path record.
	 * 
	 * @param nprl
	 *            the nprl
	 * @param npr
	 *            the npr
	 */
	protected void addSymbolsOfRootToPathRecord(List<NOOSPathRecord> nprl, NOOSPathRecord npr) {
		nprl.add(npr);
	}

	/**
	 * Define symbol of root.
	 * 
	 * @param npr
	 *            the npr
	 * @param tokenName
	 *            the token name
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	protected void defineSymbolOfRoot(NOOSPathRecord npr, String tokenName) throws FeatureTermException {
		npr.p.features.add(0, new Symbol(tokenName));
	}

	/**
	 * Define root.
	 * 
	 * @param hierarchy
	 *            the hierarchy
	 * @param f
	 *            the f
	 * @return the nOOS path record
	 */
	protected NOOSPathRecord defineRoot(List<FeatureTerm> hierarchy, FeatureTerm f) {
		NOOSPathRecord npr = new NOOSPathRecord();
		if (hierarchy.isEmpty()) {
			npr.root = f;
		} else {
			npr.root = hierarchy.get(0);
		} // if
		npr.feature = null;
		npr.f = f;
		npr.p = new Path();
		return npr;
	}

	/**
	 * Define symbol.
	 * 
	 * @param fname
	 *            the fname
	 * @param hierarchy
	 *            the hierarchy
	 * @param f
	 *            the f
	 * @param vsort
	 *            the vsort
	 * @param nprl
	 *            the nprl
	 * @param nvll
	 *            the nvll
	 * @param m
	 *            the m
	 * @param nvl
	 *            the nvl
	 * @param o
	 *            the o
	 * @return the feature term
	 * @throws Exception
	 *             the exception
	 */
	protected FeatureTerm defineSymbol(Symbol fname, List<FeatureTerm> hierarchy, FeatureTerm f, Sort vsort, List<NOOSPathRecord> nprl,
			List<NOOSVariableLinkRecord> nvll, FTKBase m, List<NOOSVariableRecord> nvl, Ontology o) throws Exception {
		FeatureTerm fvalue;
		hierarchy.add(f);
		fvalue = getFeatureTermInternal(m, o, vsort, hierarchy, nprl, nvl, nvll);
		hierarchy.remove(f);
		return fvalue;
	}

	/**
	 * Define symbol.
	 * 
	 * @param hierarchy
	 *            the hierarchy
	 * @param f
	 *            the f
	 * @param vsort
	 *            the vsort
	 * @param nprl
	 *            the nprl
	 * @param nvll
	 *            the nvll
	 * @param m
	 *            the m
	 * @param nvl
	 *            the nvl
	 * @param o
	 *            the o
	 * @return the feature term
	 * @throws Exception
	 *             the exception
	 */
	protected FeatureTerm defineSymbol(List<FeatureTerm> hierarchy, FeatureTerm f, Sort vsort, List<NOOSPathRecord> nprl, List<NOOSVariableLinkRecord> nvll,
			FTKBase m, List<NOOSVariableRecord> nvl, Ontology o) throws Exception {
		FeatureTerm fvalue;
		Symbol fname = null;
		fvalue = this.defineSymbol(fname, hierarchy, f, vsort, nprl, nvll, m, nvl, o);

		return fvalue;
	}

	/**
	 * Gets the feature term internal.
	 * 
	 * @param m
	 *            the m
	 * @param o
	 *            the o
	 * @param vsort
	 *            the vsort
	 * @param hierarchy
	 *            the hierarchy
	 * @param nprl
	 *            the nprl
	 * @param nvl
	 *            the nvl
	 * @param nvll
	 *            the nvll
	 * @return the feature term internal
	 * @throws Exception
	 *             the exception
	 */
	protected FeatureTerm getFeatureTermInternal(FTKBase m, Ontology o, Sort vsort, List<FeatureTerm> hierarchy, List<NOOSPathRecord> nprl,
			List<NOOSVariableRecord> nvl, List<NOOSVariableLinkRecord> nvll) throws Exception {

		// Sort and name:
		prepareNextSymbol();
		FeatureTerm f = null;
		f = setupIdentifier(m, o, vsort, hierarchy, nprl, nvl, nvll, f);
		f = setupFeatures(hierarchy, f, vsort, nprl, nvll, m, nvl, o);

		return f;
	} // FeatureTer

	/**
	 * Gets the name.
	 * 
	 * @param nameIdentifier
	 *            the name identifier
	 * @return the name
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	protected Symbol getName(String nameIdentifier) throws FeatureTermException, IOException {
		Symbol name;
		name = new Symbol(nameIdentifier);
		return name;
	}

	/**
	 * Gets the sort.
	 * 
	 * @param o
	 *            the o
	 * @param sortIdentifier
	 *            the sort identifier
	 * @return the sort
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	protected Sort getSort(Ontology o, String sortIdentifier) throws FeatureTermException, IOException {
		Sort sortInstance = null;
		if (!sortIdentifier.equals("set")) {
			sortInstance = o.getSort(sortIdentifier);
		} // if
		return sortInstance;

	}

	/**
	 * Setup variable.
	 * 
	 * @param nvl
	 *            the nvl
	 * @param f
	 *            the f
	 * @param variable
	 *            the variable
	 * @param variablename
	 *            the variablename
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	protected void setupVariable(List<NOOSVariableRecord> nvl, FeatureTerm f, boolean variable, String variablename) throws FeatureTermException {
		if (variable) {
			NOOSVariableRecord nv;
			// printf("Adding variable \"%s\"\n",variablename);
			nv = new NOOSVariableRecord();
			nv.f = f;
			nv.variable = new Symbol(variablename);
			nvl.add(nv);
		} // if
	}

	/**
	 * Initialize_ symbol.
	 * 
	 * @param sort
	 *            the sort
	 * @param m
	 *            the m
	 * @param fname
	 *            the fname
	 * @param name
	 *            the name
	 * @return the feature term
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	protected FeatureTerm initialize_Symbol(Sort sort, FTKBase m, Symbol fname, String name) throws FeatureTermException {
		FeatureTerm fvalue = null;
		Sort s;
		Symbol n = new Symbol(name);
		s = sort.featureSort(fname);

		if (s != null) {
			fvalue = setupSymbol(s, m, name);
			if (num[0] != 0 && num[0] != 1) {
				System.err.println(errorMessage);
				fvalue = null;
			}
		} else {
                    throw new FeatureTermException("OntologyParser: cannot find sort of feature '" + fname + "' of sort '" + sort + "'");
                }

		return fvalue;
	}

	/**
	 * Initialize_ref variable.
	 * 
	 * @param variables_read
	 *            the variables_read
	 * @param name
	 *            the name
	 * @return true, if successful
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	protected boolean initialize_refVariable(List<Symbol> variables_read, String name) throws FeatureTermException {
		boolean delayed;
		variables_read.add(new Symbol(name));
		delayed = true;
		return delayed;
	}

	/**
	 * Initialize_initial variable.
	 * 
	 * @param feature_variablename
	 *            the feature_variablename
	 * @param tokenName
	 *            the token name
	 * @return true, if successful
	 */
	protected boolean initialize_initialVariable(StringBuilder feature_variablename, String tokenName) {
		boolean feature_variable = true;
		feature_variablename = feature_variablename.append(tokenName);
		return feature_variable;
	}

	/**
	 * Read all feature values.
	 * 
	 * @param f
	 *            the f
	 * @param nvll
	 *            the nvll
	 * @param fname
	 *            the fname
	 * @param values_read
	 *            the values_read
	 * @param variables_read
	 *            the variables_read
	 */
	protected void readAllFeatureValues(FeatureTerm f, List<NOOSVariableLinkRecord> nvll, Symbol fname, List<FeatureTerm> values_read,
			List<Symbol> variables_read) {
		SetFeatureTerm ft_tmp;

		// printf("(2) Creating a set with %i+%i values\n",values_read.Length(),variables_read.Length());

		ft_tmp = new SetFeatureTerm();
		checkReadAllFeatureValuesRequirements(f, fname, ft_tmp);
		while (!values_read.isEmpty())
			ft_tmp.addSetValue(values_read.remove(0));

		while (!variables_read.isEmpty()) {
			NOOSVariableLinkRecord nv = new NOOSVariableLinkRecord();
			nv.f = ft_tmp;
			nv.feature = null;
			nv.variable = variables_read.remove(0);
			nvll.add(nv);
			// printf("Added a variable link record to fature - for variable %s\n",nv.variable.get());
		} // while
	}

	/**
	 * Check read all feature values requirements.
	 * 
	 * @param f
	 *            the f
	 * @param fname
	 *            the fname
	 * @param ft_tmp
	 *            the ft_tmp
	 */
	protected void checkReadAllFeatureValuesRequirements(FeatureTerm f, Symbol fname, SetFeatureTerm ft_tmp) {

	}

	/**
	 * Read one feature value.
	 * 
	 * @param f
	 *            the f
	 * @param fname
	 *            the fname
	 * @param values_read
	 *            the values_read
	 */
	protected void readOneFeatureValue(FeatureTerm f, Symbol fname, List<FeatureTerm> values_read) {
		FeatureTerm f2;
		f2 = values_read.remove(0);
		checkReadOneFeatureValueRequirements(f, fname, f2); // if
	}

	/**
	 * Check read one feature value requirements.
	 * 
	 * @param f
	 *            the f
	 * @param fname
	 *            the fname
	 * @param f2
	 *            the f2
	 */
	protected void checkReadOneFeatureValueRequirements(FeatureTerm f, Symbol fname, FeatureTerm f2) {

	}

	/**
	 * Setup o values and feature values of specific sort.
	 * 
	 * @param sort
	 *            the sort
	 * @param hierarchy
	 *            the hierarchy
	 * @param f
	 *            the f
	 * @param nprl
	 *            the nprl
	 * @param nvll
	 *            the nvll
	 * @param m
	 *            the m
	 * @param nvl
	 *            the nvl
	 * @param o
	 *            the o
	 * @param featureName
	 *            the feature name
	 * @return true, if successful
	 * @throws Exception
	 *             the exception
	 */
	protected boolean setupOValuesAndFeatureValuesOfSpecificSort(Sort sort, List<FeatureTerm> hierarchy, FeatureTerm f, List<NOOSPathRecord> nprl,
			List<NOOSVariableLinkRecord> nvll, FTKBase m, List<NOOSVariableRecord> nvl, Ontology o, String featureName) throws Exception {
		Symbol fname;
		fname = new Symbol(featureName);
		// Feature Value: (may have several values to form a SET)
		readValuesAndFeatureValuesOfSpecificFeature(fp, sort, hierarchy, f, nprl, nvll, m, nvl, o, fname);
		return false;
	}

	/**
	 * Read values and feature values of specific feature.
	 * 
	 * @param fp
	 *            the fp
	 * @param sort
	 *            the sort
	 * @param hierarchy
	 *            the hierarchy
	 * @param f
	 *            the f
	 * @param nprl
	 *            the nprl
	 * @param nvll
	 *            the nvll
	 * @param m
	 *            the m
	 * @param nvl
	 *            the nvl
	 * @param o
	 *            the o
	 * @param fname
	 *            the fname
	 * @throws Exception
	 *             the exception
	 */
	protected void readValuesAndFeatureValuesOfSpecificFeature(RewindableInputStream fp, Sort sort, List<FeatureTerm> hierarchy, FeatureTerm f,
			List<NOOSPathRecord> nprl, List<NOOSVariableLinkRecord> nvll, FTKBase m, List<NOOSVariableRecord> nvl, Ontology o, Symbol fname) throws Exception {
		List<FeatureTerm> values_read = new LinkedList<FeatureTerm>();
		List<Symbol> variables_read = new LinkedList<Symbol>();

		setupFeatureValuesOfSpecificFeature(fp, sort, hierarchy, f, nprl, nvll, m, nvl, o, fname, values_read, variables_read);
		if (error)
			return;

		// Add read values to feature value:
		addReadValuesToFeatureValue(f, nvll, fname, values_read, variables_read);
	}

	/**
	 * Adds the read values to feature value.
	 * 
	 * @param f
	 *            the f
	 * @param nvll
	 *            the nvll
	 * @param fname
	 *            the fname
	 * @param values_read
	 *            the values_read
	 * @param variables_read
	 *            the variables_read
	 */
	protected void addReadValuesToFeatureValue(FeatureTerm f, List<NOOSVariableLinkRecord> nvll, Symbol fname, List<FeatureTerm> values_read,
			List<Symbol> variables_read) {
		if (values_read.size() + variables_read.size() == 1) {
			if (values_read.size() == 1) {
				readOneFeatureValue(f, fname, values_read);
			} else {
				NOOSVariableLinkRecord nv = new NOOSVariableLinkRecord();
				nv.f = f;
				nv.feature = fname;
				nv.variable = variables_read.remove(0);
				nvll.add(nv);
				// printf("Added a variable link record to fature %s for variable %s\n",nv.feature.get(),nv.variable.get());
			} // if
		} else if (values_read.size() + variables_read.size() > 1) {
			readAllFeatureValues(f, nvll, fname, values_read, variables_read);

		} // if
	}

	/**
	 * Setup features.
	 * 
	 * @param hierarchy
	 *            the hierarchy
	 * @param f
	 *            the f
	 * @param vsort
	 *            the vsort
	 * @param nprl
	 *            the nprl
	 * @param nvll
	 *            the nvll
	 * @param m
	 *            the m
	 * @param nvl
	 *            the nvl
	 * @param o
	 *            the o
	 * @return the feature term
	 * @throws Exception
	 *             the exception
	 */
	protected FeatureTerm setupFeatures(List<FeatureTerm> hierarchy, FeatureTerm f, Sort vsort, List<NOOSPathRecord> nprl, List<NOOSVariableLinkRecord> nvll,
			FTKBase m, List<NOOSVariableRecord> nvl, Ontology o) throws Exception {
		boolean end = false;
		Symbol fname;
		Sort sort = this.sort;
		// Features:
		do {

			prepareNextSymbol();

			// System.out.println("(" + t.token + "," + t.type + ")");

			FeatureTerm fvalue = null;
			if (sort == null) {
				end = setupSetOfElements(hierarchy, f, vsort, nprl, nvll, m, nvl, o, fvalue);
			} else {

				end = error || setupOValuesAndFeatureValuesOfSpecificSort(sort, hierarchy, f, nprl, nvll, m, nvl, o, tokenName);
			}
			// if
		} while (!end);
		return f;
	}

	/**
	 * Setup identifier.
	 * 
	 * @param m
	 *            the m
	 * @param o
	 *            the o
	 * @param vsort
	 *            the vsort
	 * @param hierarchy
	 *            the hierarchy
	 * @param nprl
	 *            the nprl
	 * @param nvl
	 *            the nvl
	 * @param nvll
	 *            the nvll
	 * @param f
	 *            the f
	 * @return the feature term
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	protected abstract FeatureTerm setupIdentifier(FTKBase m, Ontology o, Sort vsort, List<FeatureTerm> hierarchy, List<NOOSPathRecord> nprl,
			List<NOOSVariableRecord> nvl, List<NOOSVariableLinkRecord> nvll, FeatureTerm f) throws IOException, FeatureTermException;

	/**
	 * Prepare next symbol.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	protected abstract void prepareNextSymbol() throws Exception;

	/**
	 * Setup feature values of specific feature.
	 * 
	 * @param fp
	 *            the fp
	 * @param sort
	 *            the sort
	 * @param hierarchy
	 *            the hierarchy
	 * @param f
	 *            the f
	 * @param nprl
	 *            the nprl
	 * @param nvll
	 *            the nvll
	 * @param m
	 *            the m
	 * @param nvl
	 *            the nvl
	 * @param o
	 *            the o
	 * @param fname
	 *            the fname
	 * @param values_read
	 *            the values_read
	 * @param variables_read
	 *            the variables_read
	 * @throws Exception
	 *             the exception
	 */
	protected abstract void setupFeatureValuesOfSpecificFeature(RewindableInputStream fp, Sort sort, List<FeatureTerm> hierarchy, FeatureTerm f,
			List<NOOSPathRecord> nprl, List<NOOSVariableLinkRecord> nvll, FTKBase m, List<NOOSVariableRecord> nvl, Ontology o, Symbol fname,
			List<FeatureTerm> values_read, List<Symbol> variables_read) throws Exception;

	/**
	 * Setup set of elements.
	 * 
	 * @param hierarchy
	 *            the hierarchy
	 * @param f
	 *            the f
	 * @param vsort
	 *            the vsort
	 * @param nprl
	 *            the nprl
	 * @param nvll
	 *            the nvll
	 * @param m
	 *            the m
	 * @param nvl
	 *            the nvl
	 * @param o
	 *            the o
	 * @param fvalue
	 *            the fvalue
	 * @return true, if successful
	 * @throws Exception
	 *             the exception
	 */
	protected abstract boolean setupSetOfElements(List<FeatureTerm> hierarchy, FeatureTerm f, Sort vsort, List<NOOSPathRecord> nprl,
			List<NOOSVariableLinkRecord> nvll, FTKBase m, List<NOOSVariableRecord> nvl, Ontology o, FeatureTerm fvalue) throws Exception;

	/**
	 * Checks if is end of set of features values.
	 * 
	 * @return true, if is end of set of features values
	 */
	protected abstract boolean isEndOfSetOfFeaturesValues();

}
