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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ftl.base.bridges.NOOSParser;
import ftl.base.bridges.NOOSToken;
import ftl.base.utils.FeatureTermException;
import ftl.base.utils.RewindableInputStream;

/**
 * The Class FTKBase.
 */
public class FTKBase implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 9191014604120966466L;

	/** The index. */
	List<FeatureTerm> index = new LinkedList<FeatureTerm>();

	/** The name_index. */
	HashMap<String, HashSet<FeatureTerm>> name_index = new HashMap<String, HashSet<FeatureTerm>>();

	/** The sort_index. */
	HashMap<Sort, HashSet<FeatureTerm>> sort_index = new HashMap<Sort, HashSet<FeatureTerm>>();

	/** The undefined_terms. */
	List<FeatureTerm> undefined_terms = new LinkedList<FeatureTerm>();

	/** The used_bases. */
	List<FTKBase> used_bases = new LinkedList<FTKBase>();

	/**
	 * Instantiates a new fTK base.
	 */
	public FTKBase() {
	}

	/**
	 * Create_boolean_objects.
	 * 
	 * @param o
	 *            the o
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public void create_boolean_objects(Ontology o) throws FeatureTermException {
		addFT(new TermFeatureTerm("true", o.getSort("boolean")));
		addFT(new TermFeatureTerm("false", o.getSort("boolean")));
	}

	/**
	 * Adds the ft.
	 * 
	 * @param f
	 *            the f
	 */
	public void addFT(FeatureTerm f) {
		String name;
		HashSet<FeatureTerm> ni;
		HashSet<FeatureTerm> si;

		index.add(f);

		if (f.getName() == null) {
			name = null;
		} else {
			name = f.getName().get();
		}

		ni = name_index.get(name);
		if (ni != null) {
			ni.add(f);
		} else {
			ni = new HashSet<FeatureTerm>();
			ni.add(f);
			name_index.put(name, ni);
		} // if

		si = sort_index.get(f.getSort());
		if (si != null) {
			si.add(f);
		} else {
			si = new HashSet<FeatureTerm>();
			si.add(f);
			sort_index.put(f.getSort(), si);
		} // if

	}

	/**
	 * Adds the undefined ft.
	 * 
	 * @param f
	 *            the f
	 */
	public void addUndefinedFT(FeatureTerm f) {
		addFT(f);

		undefined_terms.add(f);
	}

	/**
	 * Gets the all terms.
	 * 
	 * @return the all terms
	 */
	public List<FeatureTerm> getAllTerms() {
		return index;
	}

	/**
	 * Search undefined ft.
	 * 
	 * @param name
	 *            the name
	 * @return the feature term
	 */
	public FeatureTerm searchUndefinedFT(Symbol name) {
		Set<FeatureTerm> l;
		FeatureTerm found = null;

		l = searchFT(name);

		for (FeatureTerm f : l) {
			if (undefined_terms.contains(f)) {
				found = f;
				break;
			} // if
		}

		return found;
	}

	/**
	 * Delete ft.
	 * 
	 * @param f
	 *            the f
	 */
	public void deleteFT(FeatureTerm f) {
		Set<FeatureTerm> ni;
		Set<FeatureTerm> si;

		ni = name_index.get(f.getName().get());
		si = sort_index.get(f.getSort());

		if (ni != null) {
			ni.remove(f);
		} // if

		if (si != null) {
			si.remove(f);
		} // if

		index.remove(f);
		undefined_terms.remove(f);
	}

	/**
	 * Gets the by name.
	 * 
	 * @param name
	 *            the name
	 * @return the by name
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public FeatureTerm getByName(String name) throws FeatureTermException {
		Set<FeatureTerm> s = searchFT(new Symbol(name));
		if (s.size() < 1)
			return null;
		return s.iterator().next();
	}

	/**
	 * Gets the by name.
	 * 
	 * @param name
	 *            the name
	 * @return the by name
	 */
	public FeatureTerm getByName(Symbol name) {
		Set<FeatureTerm> s = searchFT(name);
		if (s.size() < 1)
			return null;
		return s.iterator().next();
	}

	/**
	 * Search ft.
	 * 
	 * @param name
	 *            the name
	 * @return the sets the
	 */
	public Set<FeatureTerm> searchFT(Symbol name) {
		HashSet<FeatureTerm> l;
		HashSet<FeatureTerm> ni;

		l = new HashSet<FeatureTerm>();

		ni = name_index.get(name.get());
		if (ni != null)
			l.addAll(ni);

		// Search in the used memories:
		for (FTKBase b : used_bases) {
			l.addAll(b.searchFT(name));
		}

		return l;
	}

	/**
	 * Search ft.
	 * 
	 * @param s
	 *            the s
	 * @return the sets the
	 */
	public Set<FeatureTerm> searchFT(Sort s) {
		HashSet<FeatureTerm> l, si;

		l = new HashSet<FeatureTerm>();

		si = sort_index.get(s);
		if (si != null)
			l.addAll(si);

		// Search in the used memories:
		for (FTKBase b : used_bases) {
			l.addAll(b.searchFT(s));
		}

		for (Sort s2 : s.getSubSorts()) {
			l.addAll(searchFT(s2));
		}

		return l;
	}

	/**
	 * Import noos.
	 * 
	 * @param filename
	 *            the filename
	 * @param o
	 *            the o
	 * @return true, if successful
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public boolean importNOOS(String filename, Ontology o) throws IOException, FeatureTermException {
		boolean retval = false;
		RewindableInputStream fp;

		fp = new RewindableInputStream(new FileInputStream(new File(filename)));

		retval = importNOOS(fp, o);
		fp.close();

		return retval;
	} // FTKBase::ImportNOOS

	/**
	 * Import noos.
	 * 
	 * @param fp
	 *            the fp
	 * @param o
	 *            the o
	 * @return true, if successful
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public boolean importNOOS(RewindableInputStream fp, Ontology o) throws IOException, FeatureTermException {
		NOOSToken token = null;
		boolean end;
		int state = 0;
		int pos = 0, lastpos = 0;

		end = false;
		pos = fp.position();
		do {
			lastpos = fp.position();
			token = NOOSToken.getTokenNOOS(fp);

			if (token != null) {
				switch (state) {
				case 0:
					if (token.type == NOOSToken.TOKEN_LEFT_PAR) {
						state = 1;
						pos = lastpos;
					} else {
						end = true;
					} // if
					break;

				case 1:
					if (token.type == NOOSToken.TOKEN_SYMBOL) {
						if (token.token.equals("define-ontology"))
							state = 2;
						if (token.token.equals("define-sort"))
							state = 3;
						if (token.token.equals("define-domain-model"))
							state = 4;
						if (token.token.equals("define") || token.token.equals("define-episode")) {
							fp.position(pos);
							FeatureTerm f = NOOSParser.parse(fp, this, o);
							if (f != null) {
								// System.err.println("term added " + (f.getName()!=null ? f.getName().get() : "?"));
								addFT(f);
								state = 0;
							} else {
								end = true;
							} // if
						} // if
						if (state == 1)
							end = true;
					} else {
						end = true;
					} // if
					break;

				case 2: // define-ontology:
					if (token.type == NOOSToken.TOKEN_SYMBOL) {
						int npar = 1;
						Symbol s;

						s = new Symbol(token.token);
						o.newSort(s, o.getSort("any"), null, null);

						while (npar != 0 && !end) {
							token = NOOSToken.getTokenNOOS(fp);
							if (token == null)
								end = true;
							if (token.type == NOOSToken.TOKEN_LEFT_PAR)
								npar++;
							if (token.type == NOOSToken.TOKEN_RIGHT_PAR)
								npar--;
						} // while
						state = 0;
					} else {
						end = true;
					} // if
					break;

				case 3: // define-sort:
				{
					boolean first = true;
					Symbol name = null, super_sort = null;
					FeatureTerm default_value;
					List<Symbol> fnames = new ArrayList<Symbol>();
					List<Symbol> fsorts = new ArrayList<Symbol>();
					List<FeatureTerm> fdefault = new ArrayList<FeatureTerm>();
					List<Boolean> fsingleton = new ArrayList<Boolean>();

					while (state == 3 && !end) {
						if (token.type == NOOSToken.TOKEN_LEFT_PAR) {
							if (first) {
								token = NOOSToken.getTokenNOOS(fp);

								if (token == null || token.type != NOOSToken.TOKEN_SYMBOL) {
									end = true;
								} else {
									super_sort = new Symbol(token.token);

									token = NOOSToken.getTokenNOOS(fp);
									if (token == null || token.type != NOOSToken.TOKEN_SYMBOL) {
										end = true;
									} else {
										name = new Symbol(token.token);
										token = NOOSToken.getTokenNOOS(fp);
										if (token.type != 1)
											end = true;
										first = false;
									} // if
								} // if
							} else {
								token = NOOSToken.getTokenNOOS(fp);
								if (token == null || token.type != NOOSToken.TOKEN_SYMBOL) {
									end = true;
								} else {
									boolean singleton = false;
									fnames.add(new Symbol(token.token));

									token = NOOSToken.getTokenNOOS(fp);

									if (token.type == NOOSToken.TOKEN_SINGLETON) {
										singleton = true;
										token = NOOSToken.getTokenNOOS(fp);
									} // if

									if (token == null || token.type != NOOSToken.TOKEN_SYMBOL) {
										end = true;
									} else {
										int pos2 = 0;
										fsorts.add(new Symbol(token.token));
										pos2 = fp.position();
										token = NOOSToken.getTokenNOOS(fp);
										if (token.type == NOOSToken.TOKEN_RIGHT_PAR) {
											fdefault.add(null);
										} else {
											fp.position(pos2);
											default_value = NOOSParser.parse(fp, this, o);
											if (default_value != null) {
												fdefault.add(default_value);
												token = NOOSToken.getTokenNOOS(fp);
												if (token.type != NOOSToken.TOKEN_RIGHT_PAR)
													end = true;
											} else {
												end = true;
											} // if
										} // if
										fsingleton.add(singleton);
									} // if
								} // if

							} // if
						} else if (token.type == NOOSToken.TOKEN_RIGHT_PAR) {
							// Create the sort with the corresponding features:
							Sort s = o.newSort(name, o.getSort(super_sort), null, null);
							Symbol s1, s2;
							boolean singleton;

							while (!fnames.isEmpty()) {
								s1 = fnames.remove(0);
								s2 = fsorts.remove(0);
								default_value = fdefault.remove(0);
								singleton = fsingleton.remove(0);
								s.addFeature(s1, o.getSort(s2), default_value, singleton);
							} // while

							state = 0;
						} else {
							end = true;
						} // if

						if (!end && state == 3) {
							token = NOOSToken.getTokenNOOS(fp);
							if (token == null)
								end = true;
						} // if
					} // while
				}
					break;

				case 4: // define-domain-model:
					if (token.type == NOOSToken.TOKEN_SYMBOL) {
						int npar = 1;
						while (npar != 0 && !end) {
							token = NOOSToken.getTokenNOOS(fp);
							if (token == null)
								end = true;
							if (token != null && token.type == NOOSToken.TOKEN_LEFT_PAR)
								npar++;
							if (token != null && token.type == NOOSToken.TOKEN_RIGHT_PAR)
								npar--;
						} // while
						state = 0;
					} else {
						end = true;
					} // if
					break;

				} // switch
				token = null;
			} else {
				end = true;
			} // if
		} while (!end);
		return true;
	}

	/**
	 * Gets the _n_undefined_terms.
	 * 
	 * @return the _n_undefined_terms
	 */
	public int get_n_undefined_terms() {
		return undefined_terms.size();
	} // FTKBase::get_n_undefined_terms

	/**
	 * Contains.
	 * 
	 * @param f
	 *            the f
	 * @return true, if successful
	 */
	public boolean contains(FeatureTerm f) {
		if (f.getName() != null) {
			HashSet<FeatureTerm> ni;

			ni = name_index.get(f.getName().get());
			if (ni != null)
				if (ni.contains(f))
					return true;
		} else {
			HashSet<FeatureTerm> si;

			si = sort_index.get(f.getSort());
			if (si != null)
				if (si.contains(f))
					return true;
		} // if

		// Search in the used memories:
		for (FTKBase b : used_bases) {
			if (b.contains(f))
				return true;
		}

		return false;
	}

	/**
	 * Uses.
	 * 
	 * @param base
	 *            the base
	 */
	public void uses(FTKBase base) {
		used_bases.add(base);
	}

	/**
	 * Print_status.
	 */
	public void printStatus() {
		System.out.println("Name index:");
		for (String key : name_index.keySet()) {
			System.out.print(key + " -> " + name_index.get(key).size() + " ");
			for (FeatureTerm f : name_index.get(key))
				if (undefined_terms.contains(f))
					System.out.print("U");
			System.out.println(".");
		}
		System.out.println("Sort index:");
		for (Sort key : sort_index.keySet()) {
			System.out.print(key.get() + " -> " + sort_index.get(key).size() + " ");
			for (FeatureTerm f : sort_index.get(key))
				if (undefined_terms.contains(f))
					System.out.print("U");
			System.out.println(".");
		}
	}

	/**
	 * Print_undefined_terms.
	 */
	public void printUndefinedTerms() {
		for (FeatureTerm f : undefined_terms) {
			System.out.println(f.toStringNOOS());
		} // while
	}

	/**
	 * Gets the _n_terms.
	 * 
	 * @return the _n_terms
	 */
	public int getNTerms() {
		return index.size();
	}

    public void ImportNOOS(String nooStoxicevaontologynoos, Ontology o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
