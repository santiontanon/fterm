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
import ftl.base.utils.Pair;

/**
 * The Class FTUnification.
 * 
 * @author santi
 */
public class FTUnification {

	/** The DEBUG. */
	public static int DEBUG = 0;

	/**
	 * The Class Restriction.
	 */
	static class Restriction {

		/** The f1. */
		FeatureTerm f1;

		/** The f2. */
		FeatureTerm f2;

		/** The soft. */
		boolean soft;

		/**
		 * Instantiates a new restriction.
		 * 
		 * @param aF1
		 *            the a_f1
		 * @param aF2
		 *            the a_f2
		 * @param aSoft
		 *            the a_soft
		 */
		public Restriction(FeatureTerm aF1, FeatureTerm aF2, boolean aSoft) {
			f1 = aF1;
			f2 = aF2;
			soft = aSoft; // soft restrictions can be broken (e.g. for sets)
		}
	}

	/**
	 * The Class Feature.
	 */
	static class Feature {

		/** The f1. */
		FeatureTerm f1;

		/** The f2. */
		FeatureTerm f2;

		/** The f. */
		Symbol f;

		/** The function. */
		boolean function; // whether it admits sets or not (functions do not admit sets)

		/** The size_restriction. */
		int size_restriction; // minimum size of the set in this feature

		/**
		 * Instantiates a new feature.
		 * 
		 * @param aF1
		 *            the a_f1
		 * @param aF2
		 *            the a_f2
		 * @param aF
		 *            the a_f
		 * @param aFunction
		 *            the a_function
		 * @param sr
		 *            the sr
		 */
		public Feature(FeatureTerm aF1, FeatureTerm aF2, Symbol aF, boolean aFunction, int sr) {
			f1 = aF1;
			f2 = aF2;
			f = aF;
			function = aFunction;
			size_restriction = sr;
		}
	}

	/**
	 * The Class UnificationNode.
	 */
	static class UnificationNode {

		/** The root. */
		FeatureTerm root;

		/** The variables. */
		List<FeatureTerm> variables = new LinkedList<FeatureTerm>();

		/** The features. */
		List<Feature> features = new LinkedList<Feature>();

		/** The restrictions. */
		List<Restriction> restrictions = new LinkedList<Restriction>(); // yet to be applied

		/** The different restrictions. */
		List<Restriction> differentRestrictions = null; // variables which cannot be equal (due to our subsumption
														// restriction on sets)

		/**
		 * Instantiates a new unification node.
		 * 
		 * @param n
		 *            the n
		 */
		public UnificationNode(UnificationNode n) {
			root = n.root;
			variables.addAll(n.variables);
			for (Feature f : n.features) {
				features.add(new Feature(f.f1, f.f2, f.f, f.function, f.size_restriction));
			}
			for (Restriction r : n.restrictions) {
				restrictions.add(new Restriction(r.f1, r.f2, r.soft));
			}
			differentRestrictions = new LinkedList<Restriction>();
			for (Restriction r : n.differentRestrictions) {
				differentRestrictions.add(new Restriction(r.f1, r.f2, r.soft));
			}
		}

		/**
		 * Instantiates a new unification node.
		 * 
		 * @param f1
		 *            the f1
		 * @param f2
		 *            the f2
		 * @param dm
		 *            the dm
		 * @throws FeatureTermException
		 *             the feature term exception
		 */
		public UnificationNode(FeatureTerm f1, FeatureTerm f2, FTKBase dm) throws FeatureTermException {
			HashMap<FeatureTerm, List<Pair<TermFeatureTerm, Symbol>>> v1 = FTRefinement.variablesWithAllParents(f1);
			HashMap<FeatureTerm, List<Pair<TermFeatureTerm, Symbol>>> v2 = FTRefinement.variablesWithAllParents(f2);

			for (FeatureTerm v : v1.keySet()) {
				variables.add(v);
				for (Pair<TermFeatureTerm, Symbol> p : v1.get(v)) {
					if (p != null && p.m_a != null) {
						FeatureTerm tmp = p.m_a.featureValue(p.m_b);
						int n = 1;
						if (tmp instanceof SetFeatureTerm) {
							n = ((SetFeatureTerm) tmp).getSetValues().size();
						}
						features.add(new Feature(p.m_a, v, p.m_b, p.m_a.getSort().featureSingleton(p.m_b), n));
					}
				}
			}
			for (FeatureTerm v : v2.keySet()) {
				variables.add(v);
				for (Pair<TermFeatureTerm, Symbol> p : v2.get(v)) {
					if (p != null && p.m_a != null) {
						FeatureTerm tmp = p.m_a.featureValue(p.m_b);
						int n = 1;
						if (tmp instanceof SetFeatureTerm) {
							n = ((SetFeatureTerm) tmp).getSetValues().size();
						}
						features.add(new Feature(p.m_a, v, p.m_b, p.m_a.getSort().featureSingleton(p.m_b), n));
					}
				}
			}
			differentRestrictions = new LinkedList<Restriction>();
			for (SetFeatureTerm s1 : FTRefinement.sets(f1)) {
				for (int i = 0; i < s1.getSetValues().size(); i++) {
					for (int j = i + 1; j < s1.getSetValues().size(); j++) {
						differentRestrictions.add(new Restriction(s1.getSetValues().get(i), s1.getSetValues().get(j), false));
					}
				}
			}
			for (SetFeatureTerm s2 : FTRefinement.sets(f2)) {
				for (int i = 0; i < s2.getSetValues().size(); i++) {
					for (int j = i + 1; j < s2.getSetValues().size(); j++) {
						differentRestrictions.add(new Restriction(s2.getSetValues().get(i), s2.getSetValues().get(j), false));
					}
				}
			}
			root = f1;
			restrictions.add(new Restriction(f1, f2, false)); // the first restriction cannot be broken
		}

		/**
		 * V name.
		 * 
		 * @param v
		 *            the v
		 * @param xl
		 *            the xl
		 * @param yl
		 *            the yl
		 * @param zl
		 *            the zl
		 * @return the string
		 */
		public String vName(FeatureTerm v, List<FeatureTerm> xl, List<FeatureTerm> yl, List<FeatureTerm> zl) {
			int pos = -1;
			if (xl != null) {
				pos = xl.indexOf(v);
				if (pos != -1)
					return "X" + pos;
			}
			if (yl != null) {
				pos = yl.indexOf(v);
				if (pos != -1)
					return "Y" + pos;
			}
			if (zl != null) {
				pos = zl.indexOf(v);
				if (pos != -1)
					return "Z" + pos;
			}
			return "_";
		}

		/**
		 * Prints the.
		 * 
		 * @param dm
		 *            the dm
		 * @param xl
		 *            the xl
		 * @param yl
		 *            the yl
		 */
		public void print(FTKBase dm, List<FeatureTerm> xl, List<FeatureTerm> yl) {
			System.out.print("V:[ ");
			for (FeatureTerm v : variables) {
				System.out.print(vName(v, xl, yl, variables) + ":" + (v.isConstant() || v.getName() != null ? v.toStringNOOS(dm) : v.getSort().get()) + " ");
			}
			System.out.println("]");
			System.out.print("F:[ ");
			for (Feature f : features) {
				System.out.print(vName(f.f1, xl, yl, variables) + "." + f.f.get() + "=" + vName(f.f2, xl, yl, variables) + " ");
			}
			System.out.println("]");
			System.out.print("R:[ ");
			for (Restriction r : restrictions) {
				if (r.soft)
					System.out.print("{");
				System.out.print(vName(r.f1, xl, yl, variables) + "=" + vName(r.f2, xl, yl, variables));
				if (r.soft)
					System.out.print("} ");
				else
					System.out.print(" ");
			}
			System.out.println("]");
			System.out.print("R:[ ");
			for (Restriction r : differentRestrictions) {
				System.out.print(vName(r.f1, xl, yl, variables) + "!=" + vName(r.f2, xl, yl, variables) + " ");
			}
			System.out.println("]");
		}

		/**
		 * Apply restriction. It applies the first hard restriction, if no hard restricitons, then applies the first
		 * soft
		 * 
		 * @param dm
		 *            the dm
		 * @return the list
		 * @throws FeatureTermException
		 *             the feature term exception
		 */
		public List<UnificationNode> applyRestriction(FTKBase dm) throws FeatureTermException {
			int rnum = 0;

			for (Restriction r : restrictions) {
				if (!r.soft) {
					rnum = restrictions.indexOf(r);
					break;
				}
			}

			Restriction r = restrictions.get(rnum);
			List<UnificationNode> results = new LinkedList<UnificationNode>();

			if (DEBUG >= 2)
				System.out.println("Applying restriction " + rnum);

			if (variables.contains(r.f1) && variables.contains(r.f2) && r.f1 != r.f2) {
				if (r.soft) {
					// clone the node:
					UnificationNode n = new UnificationNode(this);
					n.restrictions.remove(rnum);
					results.add(n);
				}

				// clone the node:
				UnificationNode n = new UnificationNode(this);
				n.restrictions.remove(rnum);

				// figure out the new restrictions
				for (Feature f : n.features) {
					if (f.f1 == r.f1) {
						for (Feature f2 : n.features) {
							if (f2.f1 == r.f2 && f2.f.equals(f.f) && f.f2 != f2.f2) {
								Restriction nr;
								if (f.f2.isConstant() && f2.f2.isConstant() && f.f2.equals(f2.f2)) {
									nr = new Restriction(f.f2, f2.f2, false);
								} else {
									nr = new Restriction(f.f2, f2.f2, !f.f1.getSort().featureSingleton(f.f) || !f2.f1.getSort().featureSingleton(f.f));
								}
								n.restrictions.add(nr);
							}
						}
					}
				}

				FeatureTerm u = variableUnification(r.f1, r.f2, dm);

				if (u != null) {
					// remove the second variable:
					n.variables.remove(r.f1);
					n.variables.remove(r.f2);
					n.variables.add(u);

					if (n.root == r.f1)
						n.root = u;
					if (n.root == r.f2)
						n.root = u;

					// substitute all the appearances of f1 and f2 by their unification:
					for (Feature f : n.features) {
						if (f.f1 == r.f1)
							f.f1 = u;
						if (f.f2 == r.f1)
							f.f2 = u;
						if (f.f1 == r.f2)
							f.f1 = u;
						if (f.f2 == r.f2)
							f.f2 = u;
					}
					for (Restriction r2 : n.restrictions) {
						if (r2.f1 == r.f1)
							r2.f1 = u;
						if (r2.f2 == r.f1)
							r2.f2 = u;
						if (r2.f1 == r.f2)
							r2.f1 = u;
						if (r2.f2 == r.f2)
							r2.f2 = u;
					}
					for (Restriction r2 : n.differentRestrictions) {
						if (r2.f1 == r.f1)
							r2.f1 = u;
						if (r2.f2 == r.f1)
							r2.f2 = u;
						if (r2.f1 == r.f2)
							r2.f1 = u;
						if (r2.f2 == r.f2)
							r2.f2 = u;
					}

					// remove repeated feature restrictions:
					{
						List<Feature> toDelete = new LinkedList<Feature>();
						for (Feature f1 : n.features) {
							for (Feature f2 : n.features) {
								if (f1 != f2) {
									if (f1.f1 == f2.f1 && f1.f2 == f2.f2 && f1.f.equals(f2.f)) {
										f1.function = f1.function && f2.function;
										f1.size_restriction = Math.max(f1.size_restriction, f2.size_restriction);
										toDelete.add(f2);
									}
								} else {
									break;
								}
							}
						}
						n.features.removeAll(toDelete);
					}

					// remove repeated variable restrictions:
					{
						List<Restriction> toDelete = new LinkedList<Restriction>();
						for (Restriction r1 : n.restrictions) {
							for (Restriction r2 : n.restrictions) {
								if (r1 != r2) {
									if (r1.f1 == r2.f1 && r1.f2 == r2.f2) {
										r1.soft = r1.soft && r2.soft;
										toDelete.add(r2);
									}
								} else {
									break;
								}
							}
						}
						n.restrictions.removeAll(toDelete);
					}

					// check for consistency:
					boolean consistent = true;
					for (Feature f1 : n.features) {
						int num1 = f1.size_restriction;
						int num2 = 0;
						for (Feature f2 : n.features) {
							if (f1.f1 == f2.f1 && f1.f.equals(f2.f))
								num2++;
						}
						if (num2 < num1) {
							// restrictions violated!!!
							if (DEBUG >= 2) {
								System.out.println("Number restriction violated for X" + n.variables.indexOf(f1.f1) + "." + f1.f.get());
								System.out.println("It should ne at least " + num1 + " and it's " + num2);
							}
							consistent = false;
							break;
						}
						if (!consistent)
							break;
					}
					if (consistent) {
						for (Restriction r2 : n.differentRestrictions) {
							if (r2.f1 == r2.f2) {
								consistent = false;
								break;
							}
						}
						if (consistent) {
							List<Restriction> toDeleteR = new LinkedList<Restriction>();
							for (Restriction r1 : n.restrictions) {
								for (Restriction r2 : n.differentRestrictions) {
									// make sure restriction does not collide with 'differentRestrictions':
									if ((r1.f1 == r2.f1 && r1.f2 == r2.f2) || (r1.f2 == r2.f1 && r1.f1 == r2.f2)) {
										// impossible restriction:
										if (r1.soft)
											toDeleteR.add(r1);
										else
											consistent = false;
										break;
									}
								}
								if (!consistent)
									break;
							}
							n.restrictions.removeAll(toDeleteR);
						}
					}

					if (consistent)
						results.add(n);
				}

			} else {
				// clone the node:
				UnificationNode n = new UnificationNode(this);
				n.restrictions.remove(rnum);
				results.add(n);
			}
			if (DEBUG >= 2)
				System.out.println(results.size() + " children");
			return results;
		}

		/**
		 * Consistent. Checks for consistency:
		 * 
		 * @return true, if successful
		 */
		public boolean consistent() {

			return true;
		}

		/**
		 * Generate result.
		 * 
		 * @param dm
		 *            the dm
		 * @return the feature term
		 * @throws FeatureTermException
		 *             the feature term exception
		 */
		public FeatureTerm generateResult(FTKBase dm) throws FeatureTermException {
			HashMap<FeatureTerm, FeatureTerm> variablesMap = new HashMap<FeatureTerm, FeatureTerm>();

			// recreate variables:
			for (FeatureTerm v : variables) {
				if (v.isConstant() || dm.contains(v)) {
					variablesMap.put(v, v);
				} else {
					variablesMap.put(v, v.getSort().createFeatureTerm());
				}
			}

			// add features:
			for (Feature f : features) {
				TermFeatureTerm v1 = (TermFeatureTerm) variablesMap.get(f.f1);
				FeatureTerm v2 = variablesMap.get(f.f2);
				if (v1 == null || v2 == null) {
					System.err.println("FTUnification2.generateResult: variable mapping does not exist!!");
				}
				v1.addFeatureValue(f.f, v2);
			}

			return variablesMap.get(root);
		}
	}

	/**
	 * Variable unification.
	 * 
	 * @param f1
	 *            the f1
	 * @param f2
	 *            the f2
	 * @param dm
	 *            the dm
	 * @return the feature term
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	private static FeatureTerm variableUnification(FeatureTerm f1, FeatureTerm f2, FTKBase dm) throws FeatureTermException {
		Sort s1 = f1.getSort();
		Sort s2 = f2.getSort();

		if (s1 == null || s2 == null)
			return null;

		Sort su = s1.Unification(s2);

		if (su == null)
			return null;

		FeatureTerm u = su.createFeatureTerm();

		if (f1.isConstant() || dm.contains(f1)) {
			if (f2.isConstant() || dm.contains(f2)) {
				if (f1.equivalents(f2))
					return f1;
				return null;
			} else {
				return f1;
			}
		} else {
			if (f2.isConstant() || dm.contains(f2)) {
				return f2;
			}
		}

		return u;
	}

	/**
	 * Unification.
	 * 
	 * @param f1
	 *            the f1
	 * @param f2
	 *            the f2
	 * @param dm
	 *            the dm
	 * @return the list
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public static List<FeatureTerm> unification(FeatureTerm f1, FeatureTerm f2, FTKBase dm) throws FeatureTermException {

		if (DEBUG >= 1)
			System.out.println("UnificationDuplicates started...");
		List<FeatureTerm> unifications = unificationDuplicates(f1, f2, dm, false);

		if (DEBUG >= 1)
			System.out.println("UnificationDuplicates returned " + unifications.size() + " results, now filtering...");

		List<FeatureTerm> unificationsFiltered = new LinkedList<FeatureTerm>();
		List<FeatureTerm> toDelete = new LinkedList<FeatureTerm>();

		for (FeatureTerm u : unifications) {
			boolean found = false;
			toDelete.clear();
			for (FeatureTerm u2 : unificationsFiltered) {
				if (u2.subsumes(u)) {
					// the one in the filtered list is more general or equal
					if (u.subsumes(u2)) {
						// they are equal, just don't add
						found = true;
					} else {
						// the one in the filtered list is more general, remove it and add the new one
						found = true;
					}
				} else {
					// the one in the filtered list is more specific, or different
					if (u.subsumes(u2)) {
						// the one in the filtered list is more specific:
						toDelete.add(u2);
					}
				}
			}
			unificationsFiltered.removeAll(toDelete);
			if (!found)
				unificationsFiltered.add(u);
		}

		if (DEBUG >= 1)
			System.out.println("Unification filter reduced from " + unifications.size() + " to " + unificationsFiltered.size());

		return unificationsFiltered;
	}

	/**
	 * Simple unification.
	 * 
	 * @param f1
	 *            the f1
	 * @param f2
	 *            the f2
	 * @param dm
	 *            the dm
	 * @return the feature term
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public static FeatureTerm simpleUnification(FeatureTerm f1, FeatureTerm f2, FTKBase dm) throws FeatureTermException {
		List<FeatureTerm> unifications = unificationDuplicates(f1, f2, dm, true);

                FeatureTerm candidate = null;
                
                for(FeatureTerm u:unifications) {
                    if (candidate==null) {
                        candidate = u;
                    } else {
                        if (!u.subsumes(candidate)) {
                            candidate = u;
                        }
                    }
                }
                
		return candidate;
	}
        
        
	/**
	 * This method is very fast, but does not ensure that the term that is returned is the most general possible
	 * 
	 * @param f1
	 *            the f1
	 * @param f2
	 *            the f2
	 * @param dm
	 *            the dm
	 * @return the feature term
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public static FeatureTerm simpleUnificationUnsafe(FeatureTerm f1, FeatureTerm f2, FTKBase dm) throws FeatureTermException {
		List<FeatureTerm> unifications = unificationDuplicates(f1, f2, dm, true);

                if (unifications!=null && unifications.size()>0) return unifications.get(0);
                return null;
	}
        
	/**
	 * Unification duplicates.
	 * 
	 * @param f1
	 *            the f1
	 * @param f2
	 *            the f2
	 * @param dm
	 *            the dm
	 * @param firstResult
	 *            the first result
	 * @return the list
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public static List<FeatureTerm> unificationDuplicates(FeatureTerm f1, FeatureTerm f2, FTKBase dm, boolean firstResult) throws FeatureTermException {
		List<FeatureTerm> results = new LinkedList<FeatureTerm>();
		UnificationNode start = new UnificationNode(f1, f2, dm);
		List<UnificationNode> stack = new LinkedList<UnificationNode>();
		stack.add(start);

		if (DEBUG >= 2) {
			System.out.println("unificationDuplicates:");
			System.out.println(f1.toStringNOOS(dm));
			System.out.println(f2.toStringNOOS(dm));
		}

		// for debuging purposes:
		List<FeatureTerm> xl = null;
		List<FeatureTerm> yl = null;

		if (DEBUG >= 2) {
			xl = FTRefinement.variables(f1);
			yl = FTRefinement.variables(f2);
		}

		while (!stack.isEmpty()) {
			if (DEBUG >= 1) {
				System.out.println("stack: " + stack.size() + ", results: " + results.size());
			}
			UnificationNode n = stack.remove(0);
			if (DEBUG >= 2) {
				System.out.println("Current:");
				n.print(dm, xl, yl);
			}

			List<UnificationNode> r = n.applyRestriction(dm);
			List<UnificationNode> toDelete = new LinkedList<UnificationNode>();

			// check for result:
			for (UnificationNode n2 : r) {
				if (n2.restrictions.isEmpty()) {
					// it's a result!!!
					FeatureTerm res = n2.generateResult(dm);
					results.add(res);
					if (firstResult)
						return results;
					toDelete.add(n2);

					if (DEBUG >= 2) {
						System.out.println("We've got a result:");
						n2.print(dm, xl, yl);
						System.out.println(res.toStringNOOS(dm));
					}
				}
			}
			r.removeAll(toDelete);

			if (r != null) {
				stack.addAll(0, r);
			}
		}

		return results;
	}

	/**
	 * Checks if is unification. Note: this method has the problem that the generalization operator is not complete, and
	 * thus, it might not be accurate....
	 * 
	 * @param u
	 *            the u
	 * @param f1
	 *            the f1
	 * @param f2
	 *            the f2
	 * @param dm
	 *            the dm
	 * @param o
	 *            the o
	 * @return true, if is unification
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public static boolean isUnification(FeatureTerm u, FeatureTerm f1, FeatureTerm f2, FTKBase dm, Ontology o) throws FeatureTermException {
		if (!f1.subsumes(u))
			return false;
		if (!f2.subsumes(u))
			return false;

		List<FeatureTerm> gs = FTRefinement.getGeneralizationsAggressive(u, dm, o);
		for (FeatureTerm g : gs) {
			if (u.subsumes(g)) {
				System.err.println("isUnification: term subsumes generalization refinement!!!!!");
			} else {
				if (f1.subsumes(g) && f2.subsumes(g))
					return false;
			}
		}
		return true;
	}
}
