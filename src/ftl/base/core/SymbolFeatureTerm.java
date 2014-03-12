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
import java.util.List;

import ftl.base.utils.FeatureTermException;

// TODO: Auto-generated Javadoc
/**
 * The Class SymbolFeatureTerm.
 */
public class SymbolFeatureTerm extends FeatureTerm {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8292956438553951470L;

	/** The m_value. */
	Symbol m_value = null;

	/**
	 * Instantiates a new symbol feature term.
	 * 
	 * @param name
	 *            the name
	 * @param value
	 *            the value
	 * @param o
	 *            the o
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public SymbolFeatureTerm(Symbol name, Symbol value, Ontology o) throws FeatureTermException {
		m_name = name;
		m_value = value;
		m_sort = o.getSort("symbol");
	}

	/**
	 * Instantiates a new symbol feature term.
	 * 
	 * @param value
	 *            the value
	 * @param o
	 *            the o
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public SymbolFeatureTerm(Symbol value, Ontology o) throws FeatureTermException {
		m_value = value;
		m_sort = o.getSort("symbol");
	}

	/**
	 * Gets the value.
	 * 
	 * @return the value
	 */
	public Symbol getValue() {
		return m_value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csic.iiia.ftl.base.core.FeatureTerm#hasValue()
	 */
	public boolean hasValue() {
		return m_value != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csic.iiia.ftl.base.core.FeatureTerm#isConstant()
	 */
	public boolean isConstant() {
		return m_value != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csic.iiia.ftl.base.core.FeatureTerm#toStringNOOSInternal(java.util.List, int,
	 * csic.iiia.ftl.base.core.FTKBase)
	 */
	String toStringNOOSInternal(List<FeatureTerm> bindings, int tabs, FTKBase dm) {
		int ID = -1;

		if (m_name != null && dm != null && dm.contains(this))
			return m_name.get();

		ID = bindings.indexOf(this);

		if (ID == -1) {
			bindings.add(this);
			ID = bindings.indexOf(this);

			if (m_value == null) {
				return "(define ?X" + (ID + 1) + " (symbol))";
			} else {
				return "?X" + (ID + 1) + " \"" + m_value + "\"";
			} // if
		} else {
			if (m_value == null) {
				return "!X" + (ID + 1);
			} else {
				return "\"" + m_value + "\"";
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
		FeatureTerm f = new SymbolFeatureTerm(m_name, m_value, o);
		correspondences.put(this, f);
		return f;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csic.iiia.ftl.base.core.FeatureTerm#isLeaf()
	 */
	public boolean isLeaf() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (o instanceof SymbolFeatureTerm) {
			if (m_value != null && (((SymbolFeatureTerm) o).m_value != null && ((SymbolFeatureTerm) o).m_value.equals(m_value)))
				return true;
		}
		if (o == this)
			return true;
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (m_value == null)
			return 0;
		return m_value.hashCode();
	}

}
