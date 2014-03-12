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
import java.io.FileWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ftl.base.utils.FeatureTermException;
import ftl.base.utils.Pair;

/**
 * This class implements the disintegration operation, which breaks a feature term into a set of properties as described
 * in: - "On Similarity Measures based on a Refinement Lattice" by Santiago Ontanon and Enric Plaza (ICCBR 2009) -
 * "Similarity over Refinement Graphs" by Santiago Ontanon and Enric Plaza (Machine Learning Journal, submitted).
 * 
 * @author santi
 */
public class Disintegration {

	/** The DEBUG. */
	public static int DEBUG = 0;

	/** The s_reminder type. */
	public static int s_reminderType = 1; // 0: unification-based (always works, but slow)
											// 1: smart (uses fast, checks for correctness, and in case of failure, uses
											// unification-based)
											// 2: fast (fast, but might not work when breaking variable equalities)

	/** The properties all table. */
	public static HashMap<FeatureTerm, List<FeatureTerm>> propertiesAllTable = new HashMap<FeatureTerm, List<FeatureTerm>>();

	/** The properties formal table. */
	public static HashMap<FeatureTerm, List<FeatureTerm>> propertiesFormalTable = new HashMap<FeatureTerm, List<FeatureTerm>>();

	/** The properties fast table. */
	public static HashMap<FeatureTerm, List<FeatureTerm>> propertiesFastTable = new HashMap<FeatureTerm, List<FeatureTerm>>();

	/**
	 * Disintegrate.
	 * 
	 * @param f
	 *            the f
	 * @param dm
	 *            the dm
	 * @param o
	 *            the o
	 * @return the list
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public static List<FeatureTerm> disintegrate(FeatureTerm f, FTKBase dm, Ontology o) throws FeatureTermException {
		List<FeatureTerm> properties = null;
		Pair<FeatureTerm, FeatureTerm> property_rest;

		// First check cache:
		properties = propertiesFormalTable.get(f);
		if (properties != null)
			return properties;

		properties = new LinkedList<FeatureTerm>();

		FeatureTerm unnamed = f.clone(dm, o);
		List<FeatureTerm> variables = FTRefinement.variables(unnamed);
		for (FeatureTerm v : variables) {
			if (!dm.contains(v))
				v.setName(null);
		}
		if (DEBUG >= 2) {
			System.out.println("Unnamed term to disintegrate:");
			System.out.println(unnamed.toStringNOOS(dm));
		}

		do {
			property_rest = extractProperty(unnamed, dm, o, s_reminderType);

			if (property_rest != null) {
				if (property_rest.m_a != null)
					properties.add(property_rest.m_a);
				unnamed = property_rest.m_b;

				System.out.println(properties.size() + " properties (term now has " + FTRefinement.variables(unnamed).size() + " variables)");

				if (DEBUG >= 4)
					System.out.println("--------------------");
				if (DEBUG >= 4)
					System.out.println(property_rest.m_a.toStringNOOS(dm));
				if (DEBUG >= 4)
					System.out.println(f.toStringNOOS(dm));
			}
		} while (property_rest != null);

		if (DEBUG >= 4)
			System.out.println("--------------------");
		if (DEBUG >= 4)
			System.out.println(unnamed.toStringNOOS(dm));

		propertiesFormalTable.put(f, properties);
		return properties;
	}

	/**
	 * Disintegrate all remainders.
	 * 
	 * @param f
	 *            the f
	 * @param dm
	 *            the dm
	 * @param o
	 *            the o
	 * @return the list
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public static List<FeatureTerm> disintegrateAllRemainders(FeatureTerm f, FTKBase dm, Ontology o) throws FeatureTermException {
		List<FeatureTerm> properties = null;
		Pair<List<FeatureTerm>, FeatureTerm> property_rest;

		// First check cache:
		properties = propertiesAllTable.get(f);
		if (properties != null)
			return properties;

		properties = new LinkedList<FeatureTerm>();

		FeatureTerm unnamed = f.clone(dm, o);
		List<FeatureTerm> variables = FTRefinement.variables(unnamed);
		for (FeatureTerm v : variables) {
			if (!dm.contains(v))
				v.setName(null);
		}
		if (DEBUG >= 2) {
			System.out.println("Unnamed term to disintegrate:");
			System.out.println(unnamed.toStringNOOS(dm));
		}

		do {
			property_rest = extractAllProperties(unnamed, dm, o);

			if (property_rest != null) {
				if (property_rest.m_a != null)
					properties.addAll(property_rest.m_a);
				unnamed = property_rest.m_b;

				System.out.println(properties.size() + " properties (term now has " + FTRefinement.variables(unnamed).size() + " variables");

				if (DEBUG >= 4)
					System.out.println("--------------------");
				// if (DEBUG>=4) System.out.println(property_rest.m_a.toStringNOOS(dm));
				if (DEBUG >= 4)
					System.out.println(f.toStringNOOS(dm));
			}
		} while (property_rest != null);

		if (DEBUG >= 4)
			System.out.println("--------------------");
		if (DEBUG >= 4)
			System.out.println(unnamed.toStringNOOS(dm));

		propertiesAllTable.put(f, properties);
		return properties;
	}

	/**
	 * Disintegrate with trace.
	 * 
	 * @param f
	 *            the f
	 * @param dm
	 *            the dm
	 * @param o
	 *            the o
	 * @return the list
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public static List<Pair<FeatureTerm, FeatureTerm>> disintegrateWithTrace(FeatureTerm f, FTKBase dm, Ontology o) throws FeatureTermException {
		List<Pair<FeatureTerm, FeatureTerm>> trace = null;
		Pair<FeatureTerm, FeatureTerm> property_rest;

		trace = new LinkedList<Pair<FeatureTerm, FeatureTerm>>();

		FeatureTerm unnamed = f.clone(dm, o);
		List<FeatureTerm> variables = FTRefinement.variables(unnamed);
		for (FeatureTerm v : variables) {
			if (!dm.contains(v))
				v.setName(null);
		}

		if (DEBUG >= 2) {
			System.out.println("Unnamed term to disintegrate:");
			System.out.println(unnamed.toStringNOOS(dm));
		}

		do {
			property_rest = extractProperty(unnamed, dm, o, s_reminderType);

			if (property_rest != null) {
				if (property_rest.m_a != null)
					trace.add(property_rest);
				unnamed = property_rest.m_b;

				System.out.println(trace.size() + " properties (term now has " + FTRefinement.variables(unnamed).size() + " variables");
			}
		} while (property_rest != null);

		return trace;
	}

	/**
	 * Disintegrate.
	 * 
	 * @param object
	 *            the object
	 * @param dm
	 *            the dm
	 * @param o
	 *            the o
	 * @param cache
	 *            the cache
	 * @param fast
	 *            the fast
	 * @return the list
	 * @throws Exception
	 *             the exception
	 */
	public static List<FeatureTerm> disintegrate(FeatureTerm object, FTKBase dm, Ontology o, boolean cache, boolean fast) throws Exception {
		long start_time = System.currentTimeMillis();
		List<FeatureTerm> properties_tmp = null;

		properties_tmp = propertiesFormalTable.get(object);
		if (properties_tmp != null)
			return properties_tmp;

		if (object.getName() != null && cache) {
			String fname;
			String fname_state;
			FeatureTerm current_state = null;
			if (fast)
				fname = "cache/disintegration-cache/fast-" + object.getName();
			else
				fname = "cache/disintegration-cache/formal-" + object.getName();
			if (fast)
				fname_state = "cache/disintegration-cache/fast-" + object.getName() + "-state";
			else
				fname_state = "cache/disintegration-cache/formal-" + object.getName() + "-state";
			
			File theDir = new File("cache/disintegration-cache");

			  // if the directory does not exist, create it
			  if (!theDir.exists())
			  {
			    boolean result = theDir.mkdir();  
			  }
			
			File tmp_state = new File(fname_state);
			File tmp = new File(fname);
			// set up the current state:
			if (tmp_state.exists() && tmp.exists()) {
				// disintegration was abandoned in the middle
				// load properties
				FTKBase tmpBase = new FTKBase();
				tmpBase.uses(dm);
				tmpBase.importNOOS(fname, o);
				properties_tmp = new LinkedList<FeatureTerm>();
				properties_tmp.addAll(tmpBase.getAllTerms());
				// load the last state:
				FTKBase tmpBase_state = new FTKBase();
				tmpBase_state.uses(dm);
				tmpBase_state.importNOOS(fname_state, o);
				current_state = tmpBase_state.getAllTerms().get(0);
				System.out.println(properties_tmp.size() + " properties were already extracted. Continuing...");
			} else {
				// disintegration didn't start or it's complete:
				if (tmp.exists()) {
					// load properties
					FTKBase tmpBase = new FTKBase();
					tmpBase.uses(dm);
					tmpBase.importNOOS(fname, o);
					properties_tmp = new LinkedList<FeatureTerm>();
					properties_tmp.addAll(tmpBase.getAllTerms());
					if (DEBUG >= 1)
						System.out.println(properties_tmp.size() + " properties were already extracted. Complete.");
				} else {
					properties_tmp = new LinkedList<FeatureTerm>();
					current_state = object.clone(dm, o);
					List<FeatureTerm> variables = FTRefinement.variables(current_state);
					for (FeatureTerm v : variables) {
						if (!dm.contains(v))
							v.setName(null);
					}
					System.out.println("Disintegrating from scratch...");
				}
			}

			while (current_state != null) {
				// extract a property:
				Pair<FeatureTerm, FeatureTerm> property_rest;
				if (fast) {
					property_rest = Disintegration.extractPropertyFast(current_state, dm, o);
				} else {
					property_rest = Disintegration.extractProperty(current_state, dm, o, s_reminderType);
				}
				if (property_rest != null) {
					current_state = property_rest.m_b;
					properties_tmp.add(property_rest.m_a);

					System.out.println(properties_tmp.size() + " properties (term now has " + FTRefinement.variables(current_state).size() + " variables)");

					// save the property
					try {
						
							FileWriter fw = new FileWriter(fname, true);
							fw.write(property_rest.m_a.toStringNOOS(dm) + "\n");
							fw.flush();
							fw.close();
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// save the state
					if (current_state != null) {
						FileWriter fw = new FileWriter(fname_state);
						fw.write(current_state.toStringNOOS(dm) + "\n");
						fw.flush();
						fw.close();
					} else {
						tmp_state.delete();
					}
				} else {
					current_state = null;
					tmp_state.delete();
				}
			}

			propertiesFormalTable.put(object, properties_tmp);

		} else {
			if (fast)
				properties_tmp = Disintegration.disintegrateFast(object, dm, o);
			else
				properties_tmp = Disintegration.disintegrate(object, dm, o);
		}

		long disintegration_time = System.currentTimeMillis();
		if (DEBUG >= 1)
			System.out.println("Disintegration time: " + (disintegration_time - start_time));
		return properties_tmp;
	}

	/**
	 * Extract property. This method is like the one below, but follows the exat formulation used in out journal paper
	 * 
	 * @param f
	 *            the f
	 * @param dm
	 *            the dm
	 * @param o
	 *            the o
	 * @param reminderType
	 *            the reminder type
	 * @return the pair
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	private static Pair<FeatureTerm, FeatureTerm> extractProperty(FeatureTerm f, FTKBase dm, Ontology o, int reminderType) throws FeatureTermException {

		if (DEBUG >= 1)
			System.out.println("extractPropertyFormal started...");
		if (DEBUG >= 2) {
			System.out.println("Original term:");
			System.out.println(f.toStringNOOS(dm));
		}

		List<FeatureTerm> refinements = FTRefinement.getSomeGeneralizationsAggressive(f, dm, o);

		if (refinements.size() > 0) {
			FeatureTerm refinement = refinements.get(0);
			Pair<FeatureTerm, FeatureTerm> tmp = null;
			switch (reminderType) {
			case 0:
				tmp = new Pair<FeatureTerm, FeatureTerm>(remainderUnification(f, refinement, dm, o), refinement);
				break;
			case 1:
				tmp = new Pair<FeatureTerm, FeatureTerm>(remainderSmart(f, refinement, dm, o), refinement);
				break;
			default:
				tmp = new Pair<FeatureTerm, FeatureTerm>(remainderFaster(f, refinement, dm, o), refinement);
				break;
			}

			if (DEBUG >= 2) {
				System.out.println("Property:");
				System.out.println(tmp.m_a.toStringNOOS(dm));
			}
			if (DEBUG >= 1)
				System.out.println("extractPropertyFormal finished...");
			return tmp;
		} else {
			if (DEBUG >= 1)
				System.out.println("extractPropertyFormal finished... (null)");
			return null;
		}
	}

	/**
	 * Extract all properties. This method is like the one below, but follows the exat formulation used in out journal
	 * paper
	 * 
	 * @param f
	 *            the f
	 * @param dm
	 *            the dm
	 * @param o
	 *            the o
	 * @return the pair
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	private static Pair<List<FeatureTerm>, FeatureTerm> extractAllProperties(FeatureTerm f, FTKBase dm, Ontology o) throws FeatureTermException {

		if (DEBUG >= 1)
			System.out.println("extractAllProperties started...");
		if (DEBUG >= 2) {
			System.out.println("Original term:");
			System.out.println(f.toStringNOOS(dm));
		}

		List<FeatureTerm> refinements = FTRefinement.getSomeGeneralizationsAggressive(f, dm, o);

		if (refinements.size() > 0) {
			FeatureTerm refinement = refinements.get(0);
			Pair<List<FeatureTerm>, FeatureTerm> tmp = null;
			tmp = new Pair<List<FeatureTerm>, FeatureTerm>(allRemaindersUnification(f, refinement, dm, o), refinement);

			if (DEBUG >= 2) {
				for (FeatureTerm p : tmp.m_a) {
					System.out.println("Property:");
					System.out.println(p.toStringNOOS(dm));
				}
			}
			if (DEBUG >= 1)
				System.out.println("extractPropertyFormal finished...");
			return tmp;
		} else {
			if (DEBUG >= 1)
				System.out.println("extractPropertyFormal finished... (null)");
			return null;
		}
	}

	/**
	 * Remainder unification.
	 * 
	 * This method computes the remainder by using the "unification" method. It returns the correct result, but it's
	 * very, very slow. 
	 * 
	 * @param f
	 *            the f
	 * @param refinement
	 *            the refinement
	 * @param dm
	 *            the dm
	 * @param o
	 *            the o
	 * @return the feature term
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	private static FeatureTerm remainderUnification(FeatureTerm f, FeatureTerm refinement, FTKBase dm, Ontology o) throws FeatureTermException {
		FeatureTerm oldRemainder = null;
		FeatureTerm remainder = f;
		do {
			oldRemainder = remainder;
			if (DEBUG >= 2)
				System.out.println("remainder: cycle starts");
			if (DEBUG >= 3) {
				System.out.println("refinement: ");
				System.out.println(remainder.toStringNOOS(dm));
			}
			List<FeatureTerm> refinements = FTRefinement.getGeneralizationsAggressive(remainder, dm, o);
			if (DEBUG >= 3)
				System.out.println("remainder: " + refinements.size() + " refinements.");

			remainder = null;
			for (FeatureTerm r : refinements) {
				if (DEBUG >= 1) {
					if (oldRemainder.subsumes(r)) {
						System.err.println("A generalization refinement is more specific than the orignal term!!!!!");
						System.err.println("Original:");
						System.err.println(oldRemainder.toStringNOOS(dm));
						System.err.println("Generalization:");
						System.err.println(r.toStringNOOS(dm));
					}
				}

				// If the refinement subsumes 'refinement', then their unification will never be 'f':
				if (r.subsumes(refinement))
					continue;

				if (DEBUG >= 3)
					System.out.println("remainder: starting unification...");
				List<FeatureTerm> unifications = FTUnification.unification(refinement, r, dm);
				if (unifications == null) {
					if (DEBUG >= 3)
						System.out.println("remainder: 0 unifications");
					continue;
				}
				if (DEBUG >= 3)
					System.out.println("remainder: " + unifications.size() + " unifications");

				for (FeatureTerm u : unifications) {
					if (u.equivalents(f)) {
						remainder = r;
						break;
					}
				}

				if (remainder != null)
					break;
			}
		} while (remainder != null);

		if (DEBUG >= 4)
			System.out.println("f: ");
		if (DEBUG >= 4)
			System.out.println(f.toStringNOOS(dm));
		if (DEBUG >= 4)
			System.out.println("refinement: ");
		if (DEBUG >= 4)
			System.out.println(refinement.toStringNOOS(dm));
		if (DEBUG >= 4)
			System.out.println("Remainder: ");
		if (DEBUG >= 4)
			System.out.println(oldRemainder.toStringNOOS(dm));

		return oldRemainder;
	}

	/**
	 * All remainders unification.
	 * 
	 * This method computes all the possible remainders by using the "unification" method. It returns the correct
	 * result, but it's very, very slow.
	 * 
	 * @param f
	 *            the f
	 * @param refinement
	 *            the refinement
	 * @param dm
	 *            the dm
	 * @param o
	 *            the o
	 * @return the list
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	private static List<FeatureTerm> allRemaindersUnification(FeatureTerm f, FeatureTerm refinement, FTKBase dm, Ontology o) throws FeatureTermException {
		List<FeatureTerm> stack = new LinkedList<FeatureTerm>();
		List<FeatureTerm> remainders = new LinkedList<FeatureTerm>();

		stack.add(f);
		do {
			FeatureTerm c = stack.remove(0);
			if (DEBUG >= 2)
				System.out.println("allRemaindersUnification: cycle starts (stack,rem " + stack.size() + "," + remainders.size() + ")");
			if (DEBUG >= 3) {
				System.out.println("current: ");
				System.out.println(c.toStringNOOS(dm));
			}

			// check for: 1) unification recovers f, 2) not subsumed by any remainder already computed
			boolean found = false;
			for (FeatureTerm remainder : remainders) {
				if (remainder.subsumes(c)) {
					found = true;
					if (DEBUG >= 2)
						System.out.println("allRemaindersUnification: subsumed by a previous remainder...");
					break;
				}
			}
			if (found)
				continue;
			if (DEBUG >= 3)
				System.out.println("allRemaindersUnification: starting unification...");
			List<FeatureTerm> unifications = FTUnification.unification(refinement, c, dm);
			if (unifications == null) {
				if (DEBUG >= 3)
					System.out.println("remainder: 0 unifications");
				continue;
			}
			if (DEBUG >= 3)
				System.out.println("remainder: " + unifications.size() + " unifications");

			found = false;
			for (FeatureTerm u : unifications) {
				if (u.equivalents(f)) {
					// add to remainders:
					if (DEBUG >= 3) {
						System.out.println("New remainder: ");
						System.out.println(c.toStringNOOS(dm));
					}

					List<FeatureTerm> toDelete = new LinkedList<FeatureTerm>();
					for (FeatureTerm remainder : remainders) {
						if (c.subsumes(remainder))
							toDelete.add(remainder);
					}
					remainders.removeAll(toDelete);
					toDelete.clear();
					for (FeatureTerm t : stack) {
						if (c.subsumes(t))
							toDelete.add(t);
					}
					stack.removeAll(toDelete);
					remainders.add(c);
					found = true;
					break;
				}
			}

			if (found) {
				List<FeatureTerm> refinements = FTRefinement.getGeneralizationsAggressive(c, dm, o);
				stack.addAll(0, refinements);
				if (DEBUG >= 2)
					System.out.println("allRemaindersUnification: " + refinements.size() + " refinements.");
			}
		} while (!stack.isEmpty());

		return remainders;
	}

	/**
	 * Remainder faster.
	 * 
	 * 'refinement' is a generlization of 'f':
	 * 
	 * @param f
	 *            the f
	 * @param refinement
	 *            the refinement
	 * @param dm
	 *            the dm
	 * @param o
	 *            the o
	 * @return the feature term
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	private static FeatureTerm remainderFaster(FeatureTerm f, FeatureTerm refinement, FTKBase dm, Ontology o) throws FeatureTermException {
		FeatureTerm oldRemainder = null;
		FeatureTerm remainder = f;

		// If any of these terms are candidate unifications, then the property cannot recover the original term:
		List<FeatureTerm> originalGeneralizations = FTRefinement.getGeneralizationsAggressive(f, dm, o);

		// Only those generalizations of 'f', which are subsumed by 'refinement'
		List<FeatureTerm> originalGeneralizationsFiltered = new LinkedList<FeatureTerm>();
		for (FeatureTerm g : originalGeneralizations) {
			if (refinement.subsumes(g))
				originalGeneralizationsFiltered.add(g);
		}

		do {
			oldRemainder = remainder;
			if (DEBUG >= 3) {
				System.out.println("remainder: cycle starts");
				System.out.println("refinement: ");
				System.out.println(remainder.toStringNOOS(dm));
			}
			List<FeatureTerm> refinements = FTRefinement.getGeneralizationsAggressive(remainder, dm, o);
			if (DEBUG >= 3)
				System.out.println("remainder: " + refinements.size() + " refinements.");

			remainder = null;
			for (FeatureTerm r : refinements) {
				boolean canRecover = true;

				// Sanity test:
				/*
				 * if (!(refinement.subsumes(f) && r.subsumes(f))) {
				 * System.err.println("remainderFast: sanity check failed!"); System.exit(1); }
				 */
				canRecover = true;
				for (FeatureTerm candidate : originalGeneralizationsFiltered) {
					if (refinement.subsumes(candidate) && r.subsumes(candidate)) {
						canRecover = false;
					}
				}

				if (canRecover) {
					remainder = r;
					break;
				}
			}
		} while (remainder != null);

		if (DEBUG >= 4)
			System.out.println("f: ");
		if (DEBUG >= 4)
			System.out.println(f.toStringNOOS(dm));
		if (DEBUG >= 4)
			System.out.println("refinement: ");
		if (DEBUG >= 4)
			System.out.println(refinement.toStringNOOS(dm));
		if (DEBUG >= 4)
			System.out.println("Remainder: ");
		if (DEBUG >= 4)
			System.out.println(oldRemainder.toStringNOOS(dm));

		return oldRemainder;
	}

	/**
	 * Remainder smart.
	 * 
	 * @param f
	 *            the f
	 * @param refinement
	 *            the refinement
	 * @param dm
	 *            the dm
	 * @param o
	 *            the o
	 * @return the feature term
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	private static FeatureTerm remainderSmart(FeatureTerm f, FeatureTerm refinement, FTKBase dm, Ontology o) throws FeatureTermException {
		if (DEBUG >= 2)
			System.out.println("ReminderSmart started...");
		FeatureTerm result = remainderFaster(f, refinement, dm, o);
		List<FeatureTerm> l = FTUnification.unification(result, refinement, dm);
		boolean found = false;
		for (FeatureTerm tmp : l) {
			if (f.equivalents(tmp)) {
				if (DEBUG >= 2)
					System.out.println("ReminderSmart, fast succeeded!");
				return result;
			}
		}
		// if (DEBUG>=2)
		System.out.println("ReminderSmart, fast failed, starting slow remainder...");
		result = remainderUnification(f, refinement, dm, o);
		return result;
	}

	/**
	 * Disintegrate fast.
	 * 
	 * @param f
	 *            the f
	 * @param dm
	 *            the dm
	 * @param o
	 *            the o
	 * @return the list
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public static List<FeatureTerm> disintegrateFast(FeatureTerm f, FTKBase dm, Ontology o) throws FeatureTermException {
		List<FeatureTerm> properties = null;
		Pair<FeatureTerm, FeatureTerm> property_rest;

		// First check cache:
		properties = propertiesFastTable.get(f);
		if (properties != null)
			return properties;
		properties = new LinkedList<FeatureTerm>();

		FeatureTerm unnamed = f.clone(dm, o);
		for (FeatureTerm f2 : FTRefinement.variables(unnamed)) {
			if (!dm.contains(f2))
				f2.setName(null);
		}

		do {
			property_rest = extractPropertyFast(unnamed, dm, o);

			if (property_rest != null) {
				properties.add(property_rest.m_a);
				unnamed = property_rest.m_b;

				if (DEBUG >= 4)
					System.out.println("-------------------- " + properties.size());
				if (DEBUG >= 4)
					System.out.println(property_rest.m_a.toStringNOOS(dm));
				if (DEBUG >= 4)
					System.out.println(f.toStringNOOS(dm));
			}
		} while (property_rest != null);

		if (DEBUG >= 4)
			System.out.println("--------------------");
		if (DEBUG >= 4)
			System.out.println(unnamed.toStringNOOS(dm));

		propertiesFastTable.put(f, properties);
		return properties;
	}

	/**
	 * Extract property fast.
	 * 
	 * This method takes a feature term and generates a property and generalizes the term to remove that property from
	 * it The unification of the property and the generalized term should result exactly in the original term
	 * 
	 * @param f
	 *            the f
	 * @param dm
	 *            the dm
	 * @param o
	 *            the o
	 * @return the pair
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	private static Pair<FeatureTerm, FeatureTerm> extractPropertyFast(FeatureTerm f, FTKBase dm, Ontology o) throws FeatureTermException {
		HashMap<FeatureTerm, AnnotatedPath> vp = FTRefinement.variablesWithAnnotatedPaths(f);
		HashMap<FeatureTerm, List<Pair<TermFeatureTerm, Symbol>>> vap = FTRefinement.variablesWithAllParents(f);

		// Check for a sort property:
		for (FeatureTerm X : vap.keySet()) {
			if (!X.isConstant() && !dm.contains(X)) {
				Sort most_general = o.getSort("any");
				Sort to_generalize_to = X.getSort().getSuper();

				if (to_generalize_to != null) {
					for (Pair<TermFeatureTerm, Symbol> parent : vap.get(X)) {
						if (parent != null) {
							Sort s = parent.m_a.getSort().featureSort(parent.m_b);
							if (most_general.subsumes(s)) {
								most_general = s;
							}
						}
					}

					if (to_generalize_to.is_a(most_general)) {
						boolean canGeneralize = true;

						if (X instanceof TermFeatureTerm) {
							for (Symbol fn : ((TermFeatureTerm) X).getFeatureNames()) {
								if (!to_generalize_to.hasFeature(fn)) {
									canGeneralize = false;
									break;
								}
							}
						}

						if (canGeneralize) {
							// Build Property:
							FeatureTerm property = null;
							{
								AnnotatedPath ap = vp.get(X);
								FeatureTerm lastNode = null, tmp;
								Symbol lastFeature = null;

								// Reconstruct the path:
								for (Pair<FeatureTerm, Symbol> n_f : ap.features) {
									if (lastNode == null) {
										Sort s = n_f.m_a.getSort();
										while (s.getSuper().hasFeature(n_f.m_b)) {
											s = s.getSuper();
										}

										property = lastNode = s.createFeatureTerm();
									} else {
										Sort s = n_f.m_a.getSort();
										while (s.getSuper().hasFeature(n_f.m_b)) {
											s = s.getSuper();
										}

										tmp = s.createFeatureTerm();
										((TermFeatureTerm) lastNode).defineFeatureValue(lastFeature, tmp);
										lastNode = tmp;
									}
									lastFeature = n_f.m_b;
								}

								if (lastNode == null) {
									property = lastNode = X.getSort().createFeatureTerm();
								} else {
									tmp = X.getSort().createFeatureTerm();
									((TermFeatureTerm) lastNode).defineFeatureValue(lastFeature, tmp);
								}
							}

							// Generalize:
							FeatureTerm generalization = FTRefinement.sortSubstitution(f, X, to_generalize_to, dm);

							return new Pair<FeatureTerm, FeatureTerm>(property, generalization);
						}
					}
				}
			}
		}

		// Otherwise check for a constant property:
		{
			for (FeatureTerm node : vap.keySet()) {
				if (node.isConstant() || dm.contains(node)) {
					List<Pair<TermFeatureTerm, Symbol>> parents = vap.get(node);

					for (Pair<TermFeatureTerm, Symbol> p_f : parents) {
						FeatureTerm container = p_f.m_a.featureValue(p_f.m_b);

						FeatureTerm property = null;
						{
							AnnotatedPath ap = vp.get(node);
							FeatureTerm lastNode = null, tmp;
							Symbol lastFeature = null;

							// Reconstruct the path:
							for (Pair<FeatureTerm, Symbol> n_f : ap.features) {
								if (lastNode == null) {
									Sort s = n_f.m_a.getSort();
									while (s.getSuper().hasFeature(n_f.m_b)) {
										s = s.getSuper();
									}

									property = lastNode = s.createFeatureTerm();
								} else {
									Sort s = n_f.m_a.getSort();
									while (s.getSuper().hasFeature(n_f.m_b)) {
										s = s.getSuper();
									}

									tmp = s.createFeatureTerm();
									((TermFeatureTerm) lastNode).defineFeatureValue(lastFeature, tmp);
									lastNode = tmp;
								}
								lastFeature = n_f.m_b;
							}

							if (lastNode == null) {
								property = lastNode = node;
							} else {
								((TermFeatureTerm) lastNode).defineFeatureValue(lastFeature, node);
							}
						}

						if (container.equals(node)) {
							HashMap<FeatureTerm, FeatureTerm> correspondences = new HashMap<FeatureTerm, FeatureTerm>();
							FeatureTerm clone = f.clone(dm, correspondences);
							((TermFeatureTerm) correspondences.get(p_f.m_a)).defineFeatureValue(p_f.m_b, node.getSort().createFeatureTerm());

							/*
							 * if (clone.equivalents(f)) { System.err.println("ERROR 2!!!");
							 * System.err.println(f.toStringNOOS(dm)); System.err.println(clone.toStringNOOS(dm));
							 * System.err.println(property.toStringNOOS(dm)); System.exit(1); }
							 */
							return new Pair<FeatureTerm, FeatureTerm>(property, clone);
						} else {
							int l = ((SetFeatureTerm) container).getSetValues().size();
							for (int i = 0; i < l; i++) {
								FeatureTerm ft2 = ((SetFeatureTerm) container).getSetValues().get(i);
								if (ft2.equals(node)) {
									HashMap<FeatureTerm, FeatureTerm> correspondences = new HashMap<FeatureTerm, FeatureTerm>();
									FeatureTerm clone = f.clone(dm, correspondences);
									((SetFeatureTerm) correspondences.get(container)).substituteSetValue(i, ft2.getSort().createFeatureTerm());

									return new Pair<FeatureTerm, FeatureTerm>(property, clone);
								}
							} // for
						}
					}
				}
			}
		}

		// Otherwise check for a set property:
		{
			HashMap<SetFeatureTerm, Set<Pair<TermFeatureTerm, Symbol>>> sp = FTRefinement.setsWithAllParents(f);
			for (SetFeatureTerm S : sp.keySet()) {
				Sort most_general = o.getSort("any");

				for (Pair<TermFeatureTerm, Symbol> parent : sp.get(S)) {
					Sort s = parent.m_a.getSort().featureSort(parent.m_b);
					if (most_general.subsumes(s)) {
						most_general = s;
					}
				}

				for (FeatureTerm X : S.getSetValues()) {
					if (X.getSort() == most_general && X.isLeaf()) {
						HashMap<FeatureTerm, FeatureTerm> correspondences = new HashMap<FeatureTerm, FeatureTerm>();
						FeatureTerm clone = f.clone(dm, correspondences);

						if (S.getSetValues().size() == 2) {
							SetFeatureTerm setClone = (SetFeatureTerm) correspondences.get(S);
							setClone.removeSetValue(correspondences.get(X));
							FeatureTerm toRemoveClone = setClone.getSetValues().get(0);
							clone.substitute(correspondences.get(S), toRemoveClone);
						} else {
							FeatureTerm Xclone = correspondences.get(X);
							if (Xclone != null) {
								((SetFeatureTerm) correspondences.get(S)).removeSetValue(Xclone);
							} else {
								// X is in the domain model
								((SetFeatureTerm) correspondences.get(S)).removeSetValue(X);
							}
						}

						FeatureTerm property = null;
						{
							AnnotatedPath ap = vp.get(X);
							FeatureTerm lastNode = null, tmp;
							Symbol lastFeature = null;

							// Reconstruct the path:
							for (Pair<FeatureTerm, Symbol> n_f : ap.features) {
								if (lastNode == null) {
									Sort s = n_f.m_a.getSort();
									while (s.getSuper().hasFeature(n_f.m_b)) {
										s = s.getSuper();
									}

									property = lastNode = s.createFeatureTerm();
								} else {
									Sort s = n_f.m_a.getSort();
									while (s.getSuper().hasFeature(n_f.m_b)) {
										s = s.getSuper();
									}

									tmp = s.createFeatureTerm();
									((TermFeatureTerm) lastNode).defineFeatureValue(lastFeature, tmp);
									lastNode = tmp;
								}
								lastFeature = n_f.m_b;
							}

							tmp = new SetFeatureTerm();
							for (int i = 0; i < S.getSetValues().size(); i++) {
								((SetFeatureTerm) tmp).addSetValue(most_general.createFeatureTerm());
							}

							if (lastNode == null) {
								property = lastNode = tmp;
							} else {
								((TermFeatureTerm) lastNode).defineFeatureValue(lastFeature, tmp);
							}
						}

						return new Pair<FeatureTerm, FeatureTerm>(property, clone);
					}
				}
			}

		}

		// Otherwise check for a feature property:
		for (FeatureTerm X : vap.keySet()) {
			if (X instanceof TermFeatureTerm) {
				TermFeatureTerm TX = (TermFeatureTerm) X;

				for (Symbol fname : TX.getFeatureNames()) {
					FeatureTerm fvalue = TX.featureValue(fname);

					if (fvalue.getSort() == X.getSort().featureSort(fname) && fvalue.isLeaf() && !fvalue.isConstant() && !dm.contains(fvalue)) {
						FeatureTerm property = null;

						HashMap<FeatureTerm, FeatureTerm> correspondences = new HashMap<FeatureTerm, FeatureTerm>();
						FeatureTerm clone = f.clone(dm, correspondences);

						TermFeatureTerm f1 = (TermFeatureTerm) correspondences.get(X);
						f1.removeFeatureValue(fname);

						{
							AnnotatedPath ap = vp.get(fvalue);
							FeatureTerm lastNode = null, tmp;
							Symbol lastFeature = null;

							// Reconstruct the path:
							for (Pair<FeatureTerm, Symbol> n_f : ap.features) {
								if (lastNode == null) {
									Sort s = n_f.m_a.getSort();
									while (s.getSuper().hasFeature(n_f.m_b)) {
										s = s.getSuper();
									}

									property = lastNode = s.createFeatureTerm();
								} else {
									Sort s = n_f.m_a.getSort();
									while (s.getSuper().hasFeature(n_f.m_b)) {
										s = s.getSuper();
									}

									tmp = s.createFeatureTerm();
									((TermFeatureTerm) lastNode).defineFeatureValue(lastFeature, tmp);
									lastNode = tmp;
								}
								lastFeature = n_f.m_b;
							}

							if (lastNode == null) {
								property = lastNode = fvalue.getSort().createFeatureTerm();
							} else {
								((TermFeatureTerm) lastNode).defineFeatureValue(lastFeature, fvalue.getSort().createFeatureTerm());
							}
						}

						return new Pair<FeatureTerm, FeatureTerm>(property, clone);
					}
				}
			}
		}

		// Otherwise check for a variable equality property:
		{
			for (FeatureTerm X : vap.keySet()) {
				List<Pair<TermFeatureTerm, Symbol>> parents = vap.get(X);
				/*
				 * System.out.println("Parents of X:"); for(Pair<TermFeatureTerm,Symbol> parent:parents) { if
				 * (parent==null) { System.out.println("null"); } else { System.out.println(parent.m_a.getSort().get() +
				 * "." + parent.m_b.get()); } }
				 */
				if (parents.size() >= 2) {
					FeatureTerm property = null;
					Pair<TermFeatureTerm, Symbol> parent1 = null;
					Pair<TermFeatureTerm, Symbol> parent2 = null;
					boolean first = true;

					// Get 2 parents of the set:
					for (Pair<TermFeatureTerm, Symbol> parent : parents) {
						if (first) {
							parent1 = parent;
							first = false;
						} else {
							parent2 = parent;
							break;
						}
					}

					if (parent2 == null) {
						parent2 = parent1;
						parent1 = null;
					}
					/*
					 * if (parent1==null) { System.out.println("parent1: null"); } else { System.out.println("parent1: "
					 * + parent1.m_a.getSort().get() + "." + parent1.m_b.get()); } if (parent2==null) {
					 * System.out.println("parent2: null"); } else { System.out.println("parent2: " +
					 * parent2.m_a.getSort().get() + "." + parent2.m_b.get()); }
					 */

					HashMap<FeatureTerm, FeatureTerm> correspondences = new HashMap<FeatureTerm, FeatureTerm>();
					FeatureTerm clone = f.clone(dm, correspondences);
					if (parent1 == null) {
						FeatureTerm ft2Clone = X.getSort().createFeatureTerm();
						((TermFeatureTerm) (correspondences.get(parent2.m_a))).defineFeatureValue(parent2.m_b, ft2Clone);
					} else {
						FeatureTerm ft2Clone = X.getSort().createFeatureTerm();
						((TermFeatureTerm) (correspondences.get(parent1.m_a))).defineFeatureValue(parent1.m_b, ft2Clone);
					}

					// Create property:
					{
						AnnotatedPath ap1 = (parent1 == null ? new AnnotatedPath() : vp.get(parent1.m_a));
						AnnotatedPath ap2 = vp.get(parent2.m_a);

						FeatureTerm lastNode1 = null, lastNode2 = null, tmp, trail = null;
						Symbol lastFeature1 = null, lastFeature2 = null;

						// Reconstruct the first path:
						for (Pair<FeatureTerm, Symbol> n_f : ap1.features) {
							if (lastNode1 == null) {
								Sort s = n_f.m_a.getSort();
								while (s.getSuper().hasFeature(n_f.m_b)) {
									s = s.getSuper();
								}

								property = lastNode1 = s.createFeatureTerm();
							} else {
								Sort s = n_f.m_a.getSort();
								while (s.getSuper().hasFeature(n_f.m_b)) {
									s = s.getSuper();
								}

								tmp = s.createFeatureTerm();
								((TermFeatureTerm) lastNode1).defineFeatureValue(lastFeature1, tmp);
								lastNode1 = tmp;
							}
							lastFeature1 = n_f.m_b;
						}

						// Reconstruct the second path:
						trail = property;
						for (Pair<FeatureTerm, Symbol> n_f : ap2.features) {
							if (lastNode2 == null) {
								Sort s = n_f.m_a.getSort();
								while (s.getSuper().hasFeature(n_f.m_b)) {
									s = s.getSuper();
								}

								if (trail == null) {
									property = lastNode2 = s.createFeatureTerm();
								} else {
									if (!s.inSort(trail)) {
										trail.setSort(s);
									}
									lastNode2 = trail;
									trail = trail.featureValue(n_f.m_b);
								}
							} else {
								Sort s = n_f.m_a.getSort();
								while (s.getSuper().hasFeature(n_f.m_b)) {
									s = s.getSuper();
								}

								if (trail == null) {
									tmp = s.createFeatureTerm();
									((TermFeatureTerm) lastNode2).defineFeatureValue(lastFeature2, tmp);
									lastNode2 = tmp;
								} else {
									if (!s.inSort(trail)) {
										trail.setSort(s);
									}
									lastNode2 = trail;
									trail = trail.featureValue(n_f.m_b);
								}
							}
							lastFeature2 = n_f.m_b;
						}

						// Add the parents and the common node
						{
							if (lastNode1 == null) {
								if (parent1 == null) {
									// the variable equality refers to the root
								} else {
									Sort s = parent1.m_a.getSort();
									while (s.getSuper().hasFeature(parent1.m_b)) {
										s = s.getSuper();
									}

									if (property == null) {
										// both paths refer to the root
										lastNode1 = property = s.createFeatureTerm();
									} else {
										if (!s.inSort(property)) {
											property.setSort(s);
										}
										lastNode1 = property;
									}
								}
							} else {
								Sort s = parent1.m_a.getSort();
								while (s.getSuper().hasFeature(parent1.m_b)) {
									s = s.getSuper();
								}

								tmp = lastNode1.featureValue(lastFeature1);
								if (tmp == null) {
									tmp = s.createFeatureTerm();
									((TermFeatureTerm) lastNode1).defineFeatureValue(lastFeature1, tmp);
									lastNode1 = tmp;
								} else {
									if (!s.inSort(tmp)) {
										tmp.setSort(s);
									}
									lastNode1 = tmp;
								}
							}

							if (lastNode2 == null) {
								Sort s = parent2.m_a.getSort();
								while (s.getSuper().hasFeature(parent2.m_b)) {
									s = s.getSuper();
								}

								if (property == null) {
									// both paths refer to the root
									lastNode2 = property = s.createFeatureTerm();
								} else {
									if (!s.inSort(property)) {
										property.setSort(s);
									}
									lastNode2 = property;
								}
							} else {
								Sort s = parent2.m_a.getSort();
								while (s.getSuper().hasFeature(parent2.m_b)) {
									s = s.getSuper();
								}

								tmp = lastNode2.featureValue(lastFeature2);
								if (tmp == null) {
									tmp = s.createFeatureTerm();
									((TermFeatureTerm) lastNode2).defineFeatureValue(lastFeature2, tmp);
									lastNode2 = tmp;
								} else {
									if (!s.inSort(tmp)) {
										tmp.setSort(s);
									}
									lastNode2 = tmp;
								}
							}
						}

						if (DEBUG >= 4)
							System.out.println(" -------------------------- ");
						if (DEBUG >= 4)
							System.out.println(ap1 + " -> " + parent1.m_b);
						if (DEBUG >= 4)
							System.out.println(ap2 + " -> " + parent2.m_b);
						if (DEBUG >= 4)
							System.out.println(property.toStringNOOS(dm));

						// Add the common term:
						{
							Sort s = X.getSort();
							FeatureTerm common = null;

							if (parent1 == null) {
								// parent1 is the root
								((TermFeatureTerm) lastNode2).defineFeatureValue(parent2.m_b, property);
							} else {
								common = lastNode1.featureValue(parent1.m_b);
								if (common == null) {
									common = lastNode2.featureValue(parent2.m_b);
									if (common == null) {
										common = s.createFeatureTerm();
									}
								}

								tmp = lastNode1.featureValue(parent1.m_b);
								if (tmp == null) {
									((TermFeatureTerm) lastNode1).defineFeatureValue(parent1.m_b, common);
								} else {
									common = tmp;
									if (!s.inSort(common)) {
										tmp.setSort(s);
									}
								}

								tmp = lastNode2.featureValue(parent2.m_b);
								if (tmp == null) {
									((TermFeatureTerm) lastNode2).defineFeatureValue(parent2.m_b, common);
								} else {
									if (!s.inSort(common)) {
										tmp.setSort(s);
									}
								}
							}
						}

						if (DEBUG >= 4)
							System.out.println(property.toStringNOOS(dm));

					}

					return new Pair<FeatureTerm, FeatureTerm>(property, clone);
				}
			}
		}

		return null;
	}
}
