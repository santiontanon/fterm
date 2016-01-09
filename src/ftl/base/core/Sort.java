/*
 * Creator: Santi Ontanon Villar
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
  
 package ftl.base.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import ftl.base.utils.FeatureTermException;

// TODO: Auto-generated Javadoc
/**
 * The Class Sort.
 */
public class Sort implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 2327229309614395538L;

	/** The Constant DATATYPE_ABSTRACT. */
	public final static int DATATYPE_ABSTRACT = -1;

	/** The Constant DATATYPE_INTEGER. */
	public final static int DATATYPE_INTEGER = 0;

	/** The Constant DATATYPE_FLOAT. */
	public final static int DATATYPE_FLOAT = 1;

	/** The Constant DATATYPE_SYMBOL. */
	public final static int DATATYPE_SYMBOL = 2;

	/** The Constant DATATYPE_FEATURETERM. */
	public final static int DATATYPE_FEATURETERM = 3;

	/** The Constant DATATYPE_SET. */
	public final static int DATATYPE_SET = 4;

	/** The s_rand. */
	static Random s_rand = new Random();

	/** The m_name. */
	Symbol m_name;

	/** The m_super. */
	Sort m_super;

	/** The m_ontology. */
	Ontology m_ontology;

	/** The m_subsorts. */
	HashSet<Sort> m_subsorts = new HashSet<Sort>();

	/** The m_defined. */
	boolean m_defined; // when a sort has been used but not defined, this variable takes 'true' as value

	/** The m data type. */
	int mDataType;

	/** The m_feature_position. */
	HashMap<String, Integer> m_feature_position = new HashMap<String, Integer>();

	/** The m_feature_names. */
	List<Symbol> m_feature_names = new ArrayList<Symbol>();

	/** The m_feature_sorts. */
	List<Sort> m_feature_sorts = new ArrayList<Sort>();

	/** The m_feature_default. */
	List<FeatureTerm> m_feature_default = new ArrayList<FeatureTerm>();

	/** The m_feature_singleton. */
	List<Boolean> m_feature_singleton = new ArrayList<Boolean>(); // Determines whether the feature can have a single
																	// value or if it can have many values

	/**
	 * Instantiates a new sort.
	 */
	Sort() {
		m_defined = true;

		m_name = null;
		m_super = null;
		m_ontology = null;

		mDataType = -1;

	} // Sort::Sort

	/**
	 * Instantiates a new sort.
	 * 
	 * @param name
	 *            the name
	 * @param a_super
	 *            the a_super
	 * @param o
	 *            the o
	 */
	public Sort(Symbol name, Sort a_super, Ontology o) {
		m_defined = true;

		m_name = name;
		m_super = a_super;
		if (m_super != null) {
			m_super.m_subsorts.add(this);
		} // if
		m_ontology = o;

		mDataType = -1;
		if (m_super != null)
			mDataType = m_super.mDataType;
	} // Sort::Sort

	/**
	 * Instantiates a new sort.
	 * 
	 * @param name
	 *            the name
	 * @param a_super
	 *            the a_super
	 * @param o
	 *            the o
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public Sort(String name, String a_super, Ontology o) throws FeatureTermException {
		m_defined = true;

		m_name = new Symbol(name);
		m_super = o.getSort(a_super);
		if (m_super != null) {
			m_super.m_subsorts.add(this);
			// printf("Adding %s to %s\n",name,m_super->get());
		} // if
		m_ontology = o;

		mDataType = -1;
		if (m_super != null)
			mDataType = m_super.mDataType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return m_name.get();
	}

	/**
	 * In sort.
	 * 
	 * @param i
	 *            the i
	 * @return true, if successful
	 */
	public boolean inSort(int i) {
		if (mDataType == -1)
			return true;
		if (mDataType == 0)
			return true;
		return false;
	} // Sort::inSort

	/**
	 * In sort.
	 * 
	 * @param f
	 *            the f
	 * @return true, if successful
	 */
	public boolean inSort(float f) {
		if (mDataType == -1)
			return true;
		if (mDataType == 0)
			return true;
		return false;
	} // Sort::inSort

	/**
	 * In sort.
	 * 
	 * @param s
	 *            the s
	 * @return true, if successful
	 */
	public boolean inSort(Symbol s) {
		if (mDataType == -1)
			return true;
		if (mDataType == 2)
			return true;
		return false;
	} // Sort:inSort

	/**
	 * In sort.
	 * 
	 * @param f
	 *            the f
	 * @return true, if successful
	 */
	public boolean inSort(FeatureTerm f) {
		Sort s;

		if (f instanceof SetFeatureTerm) {
			for (FeatureTerm v : ((SetFeatureTerm) f).getSetValues()) {
				if (!inSort(v))
					return false;
			}
			return true;
		}

		s = f.getSort();
		if (s == null)
			return true;
		if (s == this)
			return true;

		while (s.m_super != null) {
			s = s.m_super;
			if (s == this)
				return true;
		} // while
		return false;
	} // Sort::inSort

	/**
	 * Random.
	 * 
	 * @return the feature term
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public FeatureTerm random() throws FeatureTermException {
		switch (mDataType) {
		case DATATYPE_ABSTRACT: {
			Sort s = m_ontology.getRandomSort();
			if (s != null)
				return s.random();
			return null;
		}
		case DATATYPE_INTEGER:
			return new IntegerFeatureTerm(s_rand.nextInt(), m_ontology);
		case DATATYPE_FLOAT:
			return new FloatFeatureTerm(s_rand.nextFloat(), m_ontology);
		case DATATYPE_SYMBOL: {
			String tmp = "";
			int i;

			tmp = tmp + ('a' + (s_rand.nextInt(26)));

			for (i = 1; i < 255 && (s_rand.nextInt(16)) != 0; i++) {
				tmp = tmp + ('a' + (s_rand.nextInt(26)));
			} // for

			return new SymbolFeatureTerm(new Symbol(tmp), m_ontology);
		}

		case DATATYPE_FEATURETERM: // Generate a random Feature Term:
		}

		return null;
	} // Sort::random

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public Symbol getName() {
		return m_name;
	}

	/**
	 * Gets the.
	 * 
	 * @return the string
	 */
	public String get() {
		return m_name.get();
	}

	/**
	 * Gets the data type.
	 * 
	 * @return the data type
	 */
	public int getDataType() {
		return mDataType;
	}

	/**
	 * Sets the data type.
	 * 
	 * @param dt
	 *            the new data type
	 */
	public void setDataType(int dt) {
		mDataType = dt;
	}

	/**
	 * Gets the ontology.
	 * 
	 * @return the ontology
	 */
	public Ontology getOntology() {
		return m_ontology;
	}

	/**
	 * Checks for feature.
	 * 
	 * @param name
	 *            the name
	 * @return true, if successful
	 */
	public boolean hasFeature(Symbol name) {
		if (m_feature_position.get(name.get()) != null)
			return true;
		// if (m_feature_names.contains(name)) return true;
		if (m_super != null)
			return m_super.hasFeature(name);
		return false;
	} // hasFeature

	/**
	 * Checks for feature.
	 * 
	 * @param name
	 *            the name
	 * @return true, if successful
	 */
	public boolean hasFeature(String name) {
		// Symbol n = new Symbol(name);
		if (m_feature_position.get(name) != null)
			return true;
		// if (m_feature_names.contains(n)) return true;
		if (m_super != null)
			return m_super.hasFeature(name);
		return false;
	} // hasFeature

	/**
	 * Feature sort.
	 * 
	 * @param name
	 *            the name
	 * @return the sort
	 */
	public Sort featureSort(Symbol name) {
		Integer pos = -1;
		pos = m_feature_position.get(name.get());
		if (pos != null) {
			return m_feature_sorts.get(pos);
		} else {
			if (m_super != null)
				return m_super.featureSort(name);
			return null;
		} // if
	} // featureSort

	/**
	 * Feature sort.
	 * 
	 * @param name
	 *            the name
	 * @return the sort
	 */
	public Sort featureSort(String name) {
		Integer pos = -1;
		pos = m_feature_position.get(name);
		if (pos != null) {
			return m_feature_sorts.get(pos);
		} else {
			if (m_super != null)
				return m_super.featureSort(name);
			return null;
		} // if
	} // Sort::featureSort

        
	public void setFeatureSort(Symbol name, Sort s) {
		Integer pos = -1;
		pos = m_feature_position.get(name.get());
		if (pos != null) {
                    m_feature_sorts.set(pos, s);
		} else {
                    if (m_super != null) m_super.setFeatureSort(name, s);
		} // if
	} // featureSort

        
	/**
	 * Feature default value.
	 * 
	 * @param name
	 *            the name
	 * @return the feature term
	 */
	public FeatureTerm featureDefaultValue(Symbol name) {
		Integer pos = -1;
		pos = m_feature_position.get(name.get());
		if (pos != null) {
			return m_feature_default.get(pos);
		} else {
			if (m_super != null)
				return m_super.featureDefaultValue(name);
			return null;
		} // if
	} // Sort::featureDefaultValue

	/**
	 * Feature default value.
	 * 
	 * @param name
	 *            the name
	 * @return the feature term
	 */
	public FeatureTerm featureDefaultValue(String name) {
		Integer pos = -1;
		pos = m_feature_position.get(name);
		if (pos != null) {
			return m_feature_default.get(pos);
		} else {
			if (m_super != null)
				return m_super.featureDefaultValue(name);
			return null;
		} // if
	} // Sort::featureDefaultValue

	/**
	 * Feature singleton.
	 * 
	 * @param name
	 *            the name
	 * @return true, if successful
	 */
	public boolean featureSingleton(Symbol name) {
		Integer pos = -1;
		pos = m_feature_position.get(name.get());
		if (pos != null) {
			return m_feature_singleton.get(pos);
		} else {
			if (m_super != null)
				return m_super.featureSingleton(name);
			return false;
		} // if
	} // Sort::featureSingleton

	/**
	 * Feature singleton.
	 * 
	 * @param name
	 *            the name
	 * @return true, if successful
	 */
	public boolean featureSingleton(String name) {
		Integer pos = -1;
		pos = m_feature_position.get(name);
		if (pos != null) {
			return m_feature_singleton.get(pos);
		} else {
			if (m_super != null)
				return m_super.featureSingleton(name);
			return false;
		} // if
	} // Sort::featureSingleton

	/**
	 * Antiunification.
	 * 
	 * @param s
	 *            the s
	 * @return the sort
	 */
	public Sort Antiunification(Sort s) {
		Sort au = this;
		if (s == null)
			return null;

		while (au != null) {
			if (au.subsumes(s))
				return au;
			au = au.m_super;
		} // while
		return null;
	} // Sort::Antiunification

	/**
	 * Unification.
	 * 
	 * @param s
	 *            the s
	 * @return the sort
	 */
	public Sort Unification(Sort s) {
		if (this.subsumes(s))
			return s;
		if (s.subsumes(this))
			return this;

		return null;
	} // Sort::Unification

	/**
	 * Checks if is subsort.
	 * 
	 * Returns true if 's' is subsort of 'this'
	 * 
	 * @param s
	 *            the s
	 * @return true, if is subsort
	 */
	public boolean subsumes(Sort s) {
		if (s == null)
			return false;
		if (s == this)
			return true;
		if (s.m_super != null)
			return subsumes(s.m_super);

		return false;
	} // Sort::isSubsort

	/**
	 * Checks if is _a.
	 * 
	 * @param s
	 *            the s
	 * @return true, if is _a
	 */
	public boolean is_a(Sort s) {

		if (s == this)
			return true;

		for (Sort s2 : s.m_subsorts)
			if (is_a(s2))
				return true;

		return false;
	} // Sort::is_a

	/**
	 * Refinement steps.
	 * 
	 * returns the distance in the sort hierarchy (only if this is a subsort of s)
	 * 
	 * @param s
	 *            the s
	 * @return the int
	 */
	public int refinementSteps(Sort s) {
		int tmp;

		if (s == this)
			return 0;

		for (Sort s2 : m_subsorts) {
			tmp = s2.refinementSteps(s);
			if (tmp >= 0)
				return tmp + 1;
		} // for

		return -1;
	} // Sort::isSubsort

	/**
	 * Gets the sub sorts.
	 * 
	 * @return the sub sorts
	 */
	public Set<Sort> getSubSorts() {
		return m_subsorts;
	} // Sort::getSubSorts

	/**
	 * Adds the feature.
	 * 
	 * @param name
	 *            the name
	 * @param sort
	 *            the sort
	 * @param defaultvalue
	 *            the defaultvalue
	 * @param singleton
	 *            the singleton
	 */
	public void addFeature(Symbol name, Sort sort, FeatureTerm defaultvalue, boolean singleton) {
		m_feature_position.put(name.get(), m_feature_names.size());
		m_feature_names.add(name);
		m_feature_sorts.add(sort);
		m_feature_default.add(defaultvalue);
		m_feature_singleton.add(singleton);
	} // Sort::addFeature

	/**
	 * Adds the feature.
	 * 
	 * @param name
	 *            the name
	 * @param sort
	 *            the sort
	 * @param defaultvalue
	 *            the defaultvalue
	 * @param o
	 *            the o
	 * @param singleton
	 *            the singleton
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public void addFeature(String name, String sort, FeatureTerm defaultvalue, Ontology o, boolean singleton) throws FeatureTermException {
		m_feature_position.put(name, m_feature_names.size());
		m_feature_names.add(new Symbol(name));
		m_feature_sorts.add(o.getSort(sort));
		m_feature_default.add(defaultvalue);
		m_feature_singleton.add(singleton);
	} // Sort::addFeature

	/**
	 * Gets the features.
	 * 
	 * @return the features
	 */
	public List<Symbol> getFeatures() {
		if (m_super == null) {
			return m_feature_names;
		} else {
			List<Symbol> l = new LinkedList<Symbol>();
			l.addAll(m_super.getFeatures());
			for (Symbol f : m_feature_names)
				if (!l.contains(f))
					l.add(f);
			return l;
		} // if
	} // Sort::getFeatures

	/**
	 * Gets the super.
	 * 
	 * @return the super
	 */
	public Sort getSuper() {
		return m_super;
	} // Sort::getSuper

	/**
	 * Creates the feature term.
	 * 
	 * @return the feature term
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public FeatureTerm createFeatureTerm() throws FeatureTermException {
		switch (mDataType) {
		case DATATYPE_INTEGER: {
			FeatureTerm f = new IntegerFeatureTerm(null, m_ontology);
			f.setSort(this);
			return f;
		}
		case DATATYPE_FLOAT: {
			FeatureTerm f = new FloatFeatureTerm(null, m_ontology);
			f.setSort(this);
			return f;
		}
		case DATATYPE_SYMBOL: {
			FeatureTerm f = new SymbolFeatureTerm(null, m_ontology);
			f.setSort(this);
			return f;
		}
		case DATATYPE_FEATURETERM: {
			FeatureTerm f = new TermFeatureTerm((Symbol) null, this);
			return f;
		}
		case DATATYPE_ABSTRACT: {
			FeatureTerm f = new TermFeatureTerm((Symbol) null, this);
			return f;
		}
		}
		return null;
	}

	/**
	 * Gets the description.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		String tmp = "[sort: name = " + m_name.get() + ", datatype = " + mDataType + ", super = " + (m_super == null ? "-" : m_super.get()) + ", features = {";
		for (int i = 0; i < m_feature_names.size(); i++) {
			tmp += "(" + m_feature_names.get(i).get() + "," + m_feature_sorts.get(i).get() + ")";
		}

		tmp += "} ]";
		return tmp;
	}

}
