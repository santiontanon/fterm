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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import ftl.base.utils.FeatureTermException;

// TODO: Auto-generated Javadoc
/**
 * The Class SetFeatureTerm.
 */
public class SetFeatureTerm extends FeatureTerm {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** The m_set. */
	List<FeatureTerm> m_set = new LinkedList<FeatureTerm>();

	/**
	 * Instantiates a new sets the feature term.
	 */
	public SetFeatureTerm() {
	}

	/**
	 * Instantiates a new sets the feature term.
	 * 
	 * @param name
	 *            the name
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public SetFeatureTerm(String name) throws FeatureTermException {
		m_name = new Symbol(name);
	}

	/**
	 * Instantiates a new sets the feature term.
	 * 
	 * @param name
	 *            the name
	 */
	public SetFeatureTerm(Symbol name) {
		m_name = name;
	}

	/**
	 * Adds the set value.
	 * 
	 * @param f
	 *            the f
	 */
	public void addSetValue(FeatureTerm f) {
		if (f == null) {
			System.err.println("SetFeatureTerm::addSetValue: adding a null element to a set!");
		}
		if (f instanceof SetFeatureTerm) {
			System.err.println("SetFeatureTerm::addSetValue: adding a set to a set!!! some methods do not support this...");
			if (f == this) {
				System.err.println("SetFeatureTerm::addSetValue: circular recursion in a set!!!");
			}
		}
		m_set.add(f);
	}

	/**
	 * Adds the set value secure.
	 * 
	 * @param f
	 *            the f
	 */
	public void addSetValueSecure(FeatureTerm f) {
		if (f == null) {
			System.err.println("SetFeatureTerm::addSetValue: adding a null element to a set!");
		}
		if (f instanceof SetFeatureTerm) {
			System.err.println("SetFeatureTerm::addSetValue: adding a set to a set!!! some methods do not support this...");
			if (f == this) {
				System.err.println("SetFeatureTerm::addSetValue: circular recursion in a set!!!");
			}
		}
		if (!m_set.contains(f))
			m_set.add(f);
	}

	/**
	 * Removes the set value.
	 * 
	 * @param f
	 *            the f
	 */
	public void removeSetValue(FeatureTerm f) {
		m_set.remove(f);
	}

	/**
	 * Gets the sets the values.
	 * 
	 * @return the sets the values
	 */
	public List<FeatureTerm> getSetValues() {
		return m_set;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csic.iiia.ftl.base.core.FeatureTerm#hasValue()
	 */
	public boolean hasValue() {
		return m_set.size() > 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csic.iiia.ftl.base.core.FeatureTerm#featureValue(csic.iiia.ftl.base.core.Symbol)
	 */
	public FeatureTerm featureValue(Symbol feature) throws FeatureTermException {
		SetFeatureTerm result = new SetFeatureTerm();
		FeatureTerm f2;
		int nvalues = 0;
		FeatureTerm lastValue = null;

		for (FeatureTerm f : m_set) {
			f2 = f.featureValue(feature);
			if (f2 != null) {
				if (f2 instanceof SetFeatureTerm) {
					for (FeatureTerm f3 : ((SetFeatureTerm) f2).getSetValues()) {
						result.addSetValue(f3);
					}
				} else {
					result.addSetValue(f2);
				}
				lastValue = f2;
				nvalues++;
			}
		} /* while */

		if (nvalues == 1) return lastValue;
                if (nvalues == 0) return null;

		return result;
	} /* FeatureTerm::featureValue */

	/*
	 * (non-Javadoc)
	 * 
	 * @see csic.iiia.ftl.base.core.FeatureTerm#featureValue(java.lang.String)
	 */
	public FeatureTerm featureValue(String feature) throws FeatureTermException {
		SetFeatureTerm result = new SetFeatureTerm();
		FeatureTerm f2;
                int nvalues = 0;
                FeatureTerm lastValue = null;

		for (FeatureTerm f : m_set) {
			f2 = f.featureValue(feature);
			if (f2 != null) {
                            if (f2 instanceof SetFeatureTerm) {
                                for(FeatureTerm f3:((SetFeatureTerm) f2).getSetValues()) {
                                    result.addSetValue(f3);
                                }
                            } else {
                                result.addSetValue(f2);
                            }
                            lastValue = f2;
                            nvalues++;                            
                        }
		} 

                if (nvalues == 1) return lastValue;
                if (nvalues == 0) return null;
                
		return result;
	} /* FeatureTerm::featureValue */

	/*
	 * (non-Javadoc)
	 * 
	 * @see csic.iiia.ftl.base.core.FeatureTerm#toStringNOOSInternal(java.util.List, int,
	 * csic.iiia.ftl.base.core.FTKBase)
	 */
	void toStringNOOSInternal(StringBuffer tmp, List<FeatureTerm> bindings, int tabs, FTKBase dm) {
		int i;
		int ID = -1;

		if (m_name != null && dm != null && dm.contains(this)) {
			tmp.append(m_name.get());
                        return;
                }

		ID = bindings.indexOf(this);

		if (ID == -1) {
			bindings.add(this);
			ID = bindings.indexOf(this);

			tmp.append("(define ?X" + (ID + 1) + " (set)");

			if (!m_set.isEmpty()) {
				tmp.append("\n");
				for (i = 0; i < tabs + 2; i++)
					tmp.append(" ");
			}

			for (FeatureTerm f : m_set) {
				tmp.append(f.toStringNOOSInternal(bindings, tabs + 1, dm));

				if (m_set.indexOf(f) != m_set.size() - 1) {
					tmp.append("\n");
					for (i = 0; i < tabs + 2; i++)
						tmp.append(" ");
				} // if
			} // for

			tmp.append(")");
		} else {
			if (m_set.isEmpty()) {
				tmp.append("(define (set))");
			} else {
				tmp.append("!X" + (ID + 1));
			}
		} // if

	} // FeatureTerm::toStringNOOSInternal

	/*
	 * (non-Javadoc)
	 * 
	 * @see csic.iiia.ftl.base.core.FeatureTerm#cloneInternal2(java.util.HashMap, csic.iiia.ftl.base.core.FTKBase,
	 * csic.iiia.ftl.base.core.Ontology)
	 */
	FeatureTerm cloneInternal2(HashMap<FeatureTerm, FeatureTerm> correspondences, FTKBase dm, Ontology o) throws FeatureTermException {
		SetFeatureTerm f = new SetFeatureTerm(m_name);
		correspondences.put(this, f);

		for (FeatureTerm f2 : m_set) {
			f.m_set.add(f2.cloneInternal(correspondences, dm, o));
		} // while
		return f;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csic.iiia.ftl.base.core.FeatureTerm#isLeaf()
	 */
	public boolean isLeaf() {
		return false;
	}

	/**
	 * Substitute set value.
	 * 
	 * @param i
	 *            the i
	 * @param f
	 *            the f
	 */
	public void substituteSetValue(int i, FeatureTerm f) {
		m_set.set(i, f);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csic.iiia.ftl.base.core.FeatureTerm#clone(csic.iiia.ftl.base.core.FTKBase, java.util.HashMap)
	 */
	public FeatureTerm clone(FTKBase dm, HashMap<FeatureTerm, FeatureTerm> correspondences) throws FeatureTermException {
		SetFeatureTerm ret = new SetFeatureTerm(m_name);

		for (FeatureTerm f : m_set) {
			ret.addSetValue(f.clone(dm, correspondences));
		}
		return ret;
	} // FeatureTerm::clone

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (o instanceof SetFeatureTerm) {
			if (m_set.size() == 0 && ((SetFeatureTerm) o).m_set.size() == 0)
				return true;
			return super.equals(o);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (m_set.size() == 0)
			return 0;
		return super.hashCode();
	}

}
