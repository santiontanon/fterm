/*
 * Creator: Santi Onta��n Villar
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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import ftl.base.utils.FeatureTermException;

// TODO: Auto-generated Javadoc
/**
 * The Class Ontology.
 */
public class Ontology implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 4565482885159803106L;

	/** The s_rand. */
	static Random s_rand = new Random();

	/** The m_name. */
	Symbol m_name; // ontology name

	/** The m_description. */
	Symbol m_description; // ontology description

	/** The sort_list. */
	HashMap<String, Sort> sort_list = new HashMap<String, Sort>();

	/** The undefined_sort_list. */
	HashMap<String, Sort> undefined_sort_list = new HashMap<String, Sort>();

	/** The m_super. */
	List<Ontology> m_super = new LinkedList<Ontology>(); // List of ontologies used

	/**
	 * Instantiates a new ontology.
	 */
	public Ontology() {
		m_name = null;
		m_description = null;
	} /* Ontology::Ontology */

	/**
	 * Uses.
	 * 
	 * @param o
	 *            the o
	 */
	public void uses(Ontology o) {
		m_super.add(o);
	}

	/**
	 * Gets the sort internal.
	 * 
	 * @param name
	 *            the name
	 * @return the sort internal
	 */
	Sort getSortInternal(String name) {
		Sort s = sort_list.get(name);
		if (s != null)
			return s;

		for (Ontology o : m_super) {
			s = o.getSortInternal(name);
			if (s != null)
				return s;
		}

		return null;
	} /* Ontology::get_sort_internal */

	/**
	 * Gets the sort internal.
	 * 
	 * @param name
	 *            the name
	 * @return the sort internal
	 */
	Sort getSortInternal(Symbol name) {
		Sort s = sort_list.get(name.toString());
		if (s != null)
			return s;

		for (Ontology o : m_super) {
			s = o.getSortInternal(name);
			if (s != null)
				return s;
		}

		return null;

	} /* Ontology::get_sort_internal */

	/**
	 * Gets the sort.
	 * 
	 * @param name
	 *            the name
	 * @return the sort
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public Sort getSort(String name) throws FeatureTermException {
		if (name == null)
			return null;
		Sort s = sort_list.get(name);
		if (s != null)
			return s;
		s = undefined_sort_list.get(name);
		if (s != null)
			return s;

		for (Ontology o : m_super) {
			s = o.getSortInternal(name);
			if (s != null)
				return s;
		}

		s = new Sort(new Symbol(name), null, this);
		s.m_defined = false;
		undefined_sort_list.put(name, s);

		return s;
	} /* Ontology::get_sort */

	/**
	 * New sort.
	 * 
	 * @param name
	 *            the name
	 * @param a_super
	 *            the a_super
	 * @param fnames
	 *            the fnames
	 * @param fsorts
	 *            the fsorts
	 * @return the sort
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public Sort newSort(String name, String a_super, String[] fnames, String[] fsorts) throws FeatureTermException {
		Sort s = getSortInternal(name);

		if (s == null) {
			int i;

			s = undefined_sort_list.get(name);

			if (s == null) {
				s = new Sort();
				sort_list.put(name, s);
			} else {
				undefined_sort_list.remove(name);
				sort_list.put(name, s);
			} // if
			s.m_ontology = this;
			s.m_name = new Symbol(name);
			s.m_super = getSort(a_super);
			if (s.m_super != null)
				s.m_super.m_subsorts.add(s);

			for (i = 0; i < fnames.length; i++) {
				s.addFeature(fnames[i], fsorts[i], null, this, false);
			} // for

			s.mDataType = Sort.DATATYPE_FEATURETERM;
		} else {
			System.err.println("Ontology.newSort: Sort '" + name + "' was already defined!!");
		} // if

		return s;
	} // Ontology::new_sort

	/**
	 * Gets the sort.
	 * 
	 * @param n
	 *            the n
	 * @return the sort
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public Sort getSort(Symbol n) throws FeatureTermException {
		return getSort(n.get());
	} /* Ontology::get_sort */

	/**
	 * New sort.
	 * 
	 * @param name
	 *            the name
	 * @param a_super
	 *            the a_super
	 * @param fnames
	 *            the fnames
	 * @param fsorts
	 *            the fsorts
	 * @return the sort
	 */
	public Sort newSort(Symbol name, Sort a_super, Symbol[] fnames, Sort[] fsorts) {
		Sort s = getSortInternal(name);

		if (s == null) {
			int i;

			s = undefined_sort_list.get(name.toString());

			if (s == null) {
				s = new Sort();
				sort_list.put(name.toString(), s);
				s.m_name = name;
				s.m_ontology = this;
			} else {
				undefined_sort_list.remove(name.toString());
				sort_list.put(name.toString(), s);
			} /* if */
			s.m_super = a_super;
			if (s.m_super != null)
				s.m_super.m_subsorts.add(s);

			if (fnames != null) {
				for (i = 0; i < fnames.length; i++) {
					s.addFeature(fnames[i], fsorts[i], null, false);
				} /* for */
			}

			s.mDataType = Sort.DATATYPE_FEATURETERM;
		} /* if */

		return s;
	} /* Ontology::new_sort */

	/**
	 * Delete sort.
	 * 
	 * @param s
	 *            the s
	 */
	public void deleteSort(Sort s) {
		sort_list.remove(s);
	} /* if */

	/**
	 * Gets the random sort.
	 * 
	 * @return the random sort
	 */
	public Sort getRandomSort() {
		return sort_list.get(s_rand.nextInt(sort_list.size()));
	} /* Ontology::get_random_sort */

	/**
	 * Gets the n sorts.
	 * 
	 * @return the n sorts
	 */
	public int getNSorts() {
		return sort_list.values().size() + undefined_sort_list.values().size();
	} /* Ontology::get_nsorts */

	/**
	 * Gets the sorts.
	 * 
	 * @return the sorts
	 */
	public Collection<Sort> getSorts() {
		return sort_list.values();
	} /* Ontology::get_sort_num */

	/**
	 * Gets the undefined sorts.
	 * 
	 * @return the undefined sorts
	 */
	public Collection<Sort> getUndefinedSorts() {
		return undefined_sort_list.values();
	} /* Ontology::get_sort_num */

	/**
	 * Sets the name.
	 * 
	 * @param name
	 *            the new name
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public void setName(String name) throws FeatureTermException {
		m_name = new Symbol(name);
	} /* Ontology::set_name */

	/**
	 * Sets the name.
	 * 
	 * @param name
	 *            the new name
	 */
	public void setName(Symbol name) {
		m_name = name;
	} /* Ontology::set_name */

	/**
	 * Sets the description.
	 * 
	 * @param des
	 *            the new description
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public void setDescription(String des) throws FeatureTermException {
		m_description = new Symbol(des);
	} /* Ontology::set_description */

	/**
	 * Sets the description.
	 * 
	 * @param des
	 *            the new description
	 */
	public void setDescription(Symbol des) {
		m_description = des;
	} /* Ontology::set_description */

	/**
	 * Gets the n undefined sorts.
	 * 
	 * @return the n undefined sorts
	 */
	public int getNUndefinedSorts() {

		return undefined_sort_list.size();
	}

	/**
	 * Gets the description.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		String tmp = "Ontology:\n";

		for (Sort s : sort_list.values()) {
			tmp += s.getDescription() + "\n";
		}

		return tmp;
	}

}
