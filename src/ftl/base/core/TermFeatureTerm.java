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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import ftl.base.utils.FeatureTermException;
import ftl.base.utils.SingletonFeatureTermException;
import java.util.LinkedHashMap;

// TODO: Auto-generated Javadoc
/**
 * The Class TermFeatureTerm.
 */
public class TermFeatureTerm extends FeatureTerm {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1715159979844047122L;

	/** The Constant STRICT_SECURE. */
	static final boolean STRICT_SECURE = false; // If set to false, the defineFeatureValueSecure only prints warnings,
												// but
	// does the assignment anyway.
	// List<Symbol> m_feature_name = new ArrayList<Symbol>();
	// List<FeatureTerm> m_feature_value = new ArrayList<FeatureTerm>();
	/** The m_features. */
	LinkedHashMap<Symbol, FeatureTerm> m_features = new LinkedHashMap<Symbol, FeatureTerm>();

	/**
	 * Instantiates a new term feature term.
	 * 
	 * @param s
	 *            the s
	 */
	public TermFeatureTerm(Sort s) {
		m_sort = s;
	}

	/**
	 * Instantiates a new term feature term.
	 * 
	 * @param name
	 *            the name
	 * @param s
	 *            the s
	 */
	public TermFeatureTerm(Symbol name, Sort s) {
		m_name = name;
		m_sort = s;
	}

	/**
	 * Instantiates a new term feature term.
	 * 
	 * @param name
	 *            the name
	 * @param s
	 *            the s
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public TermFeatureTerm(String name, Sort s) throws FeatureTermException {
		m_name = new Symbol(name);
		m_sort = s;
	}

	/**
	 * Gets the feature names.
	 * 
	 * @return the feature names
	 */
	public Set<Symbol> getFeatureNames() {
		return m_features.keySet();
	}

	/**
	 * Gets the features.
	 * 
	 * @return the features
	 */
	public Set<Entry<Symbol, FeatureTerm>> getFeatures() {
		return m_features.entrySet();
	}

	/**
	 * Gets the feature values.
	 * 
	 * @return the feature values
	 */
	public Collection<FeatureTerm> getFeatureValues() {
		return m_features.values();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csic.iiia.ftl.base.core.FeatureTerm#hasValue()
	 */
	public boolean hasValue() {
		return m_features.size() > 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csic.iiia.ftl.base.core.FeatureTerm#featureValue(csic.iiia.ftl.base.core.Symbol)
	 */
	public FeatureTerm featureValue(Symbol feature) {
		return m_features.get(feature);
	} // featureValue

	/**
	 * Define feature value.
	 * 
	 * @param feature
	 *            the feature
	 * @param value
	 *            the value
	 */
	public void defineFeatureValue(Symbol feature, FeatureTerm value) {
		m_features.put(feature, value);
	} // FeatureTerm::defineFeatureValue

        
	/**
	 * Adds the feature value.
	 * 
	 * @param feature
	 *            the feature
	 * @param value
	 *            the value
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
        public void defineFeatureValue(String feature, FeatureTerm value) throws FeatureTermException {
                m_features.put(new Symbol(feature), value);
        } // FeatureTerm::defineFeatureValue
 
 
	/**
	 * Adds the feature value.
	 * 
	 * @param feature
	 *            the feature
	 * @param value
	 *            the value
	 * @throws SingletonFeatureTermException
	 *             the singleton feature term exception
	 */
	public void addFeatureValue(Symbol feature, FeatureTerm value) throws SingletonFeatureTermException {
		FeatureTerm t = m_features.get(feature);

		if (value == null) {
			System.err.println("Warning: TermFeatureTerm.addFeatureValue -> value is null");
		}

		if (t == null) {
			m_features.put(feature, value);
		} else {
			if (t instanceof SetFeatureTerm) {
				if (((SetFeatureTerm) t).getSetValues().contains(value))
					return;
				if (m_sort.featureSingleton(feature))
					throw new SingletonFeatureTermException("in addFeatureValue in feature " + feature);
				((SetFeatureTerm) t).addSetValue(value);
			} else {
				if (t == value)
					return;
				if (m_sort.featureSingleton(feature))
					throw new SingletonFeatureTermException("in addFeatureValue in feature " + feature);
				SetFeatureTerm t2 = new SetFeatureTerm();
				t2.addSetValue(t);
				t2.addSetValue(value);
				m_features.put(feature, t2);
			}
		}
	}

	/**
	 * Adds the feature value secure.
	 * 
	 * @param feature
	 *            the feature
	 * @param value
	 *            the value
	 * @throws SingletonFeatureTermException
	 *             the singleton feature term exception
	 */
	public void addFeatureValueSecure(Symbol feature, FeatureTerm value) throws SingletonFeatureTermException {
		FeatureTerm t = m_features.get(feature);

		if (t == null) {
			defineFeatureValueSecure(feature, value);
		} else {
			if (t instanceof SetFeatureTerm) {
				if (((SetFeatureTerm) t).getSetValues().contains(value))
					return;
				if (m_sort.featureSingleton(feature))
					throw new SingletonFeatureTermException("in addFeatureValue");
				((SetFeatureTerm) t).addSetValueSecure(value);
			} else {
				if (t == value)
					return;
				if (m_sort.featureSingleton(feature))
					throw new SingletonFeatureTermException("in addFeatureValue");
				SetFeatureTerm t2 = new SetFeatureTerm();
				t2.addSetValue(t);
				t2.addSetValue(value);
				m_features.put(feature, t2);
				defineFeatureValueSecure(feature, t2);
			}
		}
	}

	// checks the SORT to see if the feature exists
	// and if the value has the correct sort
	/**
	 * Define feature value secure.
	 * 
	 * @param feature
	 *            the feature
	 * @param value
	 *            the value
	 * @return true, if successful
	 */
	public boolean defineFeatureValueSecure(Symbol feature, FeatureTerm value) {
		boolean singleton = false;

		if (feature == null) {
			System.err.println("defineFeatureValueSecure: error 1, feature is null");
			if (STRICT_SECURE) {
				return false;
			}
		}

		if (!m_sort.hasFeature(feature)) {
			System.err.println("defineFeatureValueSecure: feature '" + feature.get() + "' does not belong to sort '" + m_sort.get() + "'");
			if (STRICT_SECURE) {
				return false;
			}
		} else	if (!m_sort.featureSort(feature).inSort(value)) {
			System.err.println("defineFeatureValueSecure: error 3 setting feature '" + feature + "' of a term of sort '" + m_sort + "'");
			System.err.println(value.toStringNOOS());
			System.err.println("is not of the sort " + m_sort.featureSort(feature).get());
			if (STRICT_SECURE) {
				return false;
			}
		} // if

		singleton = m_sort.featureSingleton(feature);

		if (singleton && (value instanceof SetFeatureTerm) && ((SetFeatureTerm) value).getSetValues().size() > 1) {
			System.err.println("defineFeatureValueSecure: error 4 in feature " + feature.get() + " - " + value.toStringNOOS());
			if (STRICT_SECURE) {
				return false;
			}
		} // if

		m_features.put(feature, value);

		return true;
	} // FeatureTerm::defineFeatureValueSecure

	/*
	 * (non-Javadoc)
	 * 
	 * @see csic.iiia.ftl.base.core.FeatureTerm#featureValue(java.lang.String)
	 */
	public FeatureTerm featureValue(String feature) throws FeatureTermException {
		return m_features.get(new Symbol(feature));
	} // FeatureTerm::featureValue

	/**
	 * Removes the feature value.
	 * 
	 * @param feature
	 *            the feature
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public void removeFeatureValue(String feature) throws FeatureTermException {
		removeFeatureValue(new Symbol(feature));
	} // FeatureTerm::removeFeatureValue

	/**
	 * Removes the feature value.
	 * 
	 * @param feature
	 *            the feature
	 */
	public void removeFeatureValue(Symbol feature) {
		m_features.remove(feature);
	} // FeatureTerm::removeFeatureValue

	/**
	 * Removes the feature value.
	 * 
	 * @param feature
	 *            the feature to remove from
	 * @param value
	 *            the value to remove
	 */
	public void removeFeatureValue(Symbol feature, FeatureTerm value) {
            FeatureTerm t = m_features.get(feature);
            if (t instanceof SetFeatureTerm) {
                SetFeatureTerm st = (SetFeatureTerm)t;
                st.m_set.remove(value);
                if (st.m_set.isEmpty()) {
                    m_features.remove(feature);
                }
            } else {
                if (t==value) {
                    m_features.remove(feature);
                }
            }
	} // FeatureTerm::removeFeatureValue
        
        
	/*
	 * (non-Javadoc)
	 * 
	 * @see csic.iiia.ftl.base.core.FeatureTerm#toStringNOOSInternal(java.util.List, int,
	 * csic.iiia.ftl.base.core.FTKBase)
	 */
	void toStringNOOSInternal(StringBuffer tmp, List<FeatureTerm> bindings, int tabs, FTKBase dm) {
		int ID = -1;

		if (m_name != null && dm != null && dm.contains(this)) {
			tmp.append(m_name.get());
                        return;
		}
		ID = bindings.indexOf(this);

		if (ID == -1) {
			bindings.add(this);
			ID = bindings.indexOf(this);

			FeatureTerm f;

			tmp.append("(define ?X" + (ID + 1) + " (" + m_sort.get());

			if (m_name != null) {
				tmp.append(" :id " + m_name.get());
			} // if

			tmp.append(")");

			if (!m_features.isEmpty()) {
				tmp.append("\n");
				for (int i = 0; i < tabs + 2; i++) {
					tmp.append(" ");
				}
			}

			int l = m_features.keySet().size();
			int i = 0;
			for (Symbol s : m_features.keySet()) {
				f = m_features.get(s);

				if (f == null) {
					tmp.append("(" + s.get() + " null)");
				} else {
					tmp.append("(" + s.get() + " ");
                                        f.toStringNOOSInternal(tmp, bindings, tabs + s.get().length() + 4, dm);
                                        tmp.append(")");
				}
				i++;

				if (i != l) {
					tmp.append("\n");
					for (int j = 0; j < tabs + 2; j++) {
						tmp.append(" ");
					}
				} // if
			} // while

			tmp.append(")");
		} else {
			tmp.append("!X" + (ID + 1));
		} // if
	} // FeatureTerm::toStringNOOSInternal

	/*
	 * (non-Javadoc)
	 * 
	 * @see csic.iiia.ftl.base.core.FeatureTerm#cloneInternal2(java.util.HashMap, csic.iiia.ftl.base.core.FTKBase,
	 * csic.iiia.ftl.base.core.Ontology)
	 */
	FeatureTerm cloneInternal2(HashMap<FeatureTerm, FeatureTerm> correspondences, FTKBase dm, Ontology o) throws FeatureTermException {
		TermFeatureTerm f = new TermFeatureTerm(m_name, m_sort);
		correspondences.put(this, f);

		for (Symbol s : m_features.keySet()) {
			FeatureTerm v = m_features.get(s);
			if (v != null)
				f.m_features.put(s, v.cloneInternal(correspondences, dm, o));
		}

		return f;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csic.iiia.ftl.base.core.FeatureTerm#isLeaf()
	 */
	public boolean isLeaf() {
		return m_features.size() == 0;
	}
}
