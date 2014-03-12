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
  
 package ftl.argumentation.weighted;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import ftl.argumentation.core.Argument;
import ftl.base.core.FTKBase;
import ftl.base.core.FeatureTerm;
import ftl.base.utils.FeatureTermException;
import ftl.base.utils.Pair;

// TODO: Auto-generated Javadoc
/**
 * The Class WArgumentationFramework.
 * 
 * @author santi
 */
public class WArgumentationFramework {

	/** The DEBUG. */
	public static int DEBUG = 0;

	/** The m_arguments. */
	List<WeightedArgument> m_arguments = new LinkedList<WeightedArgument>();

	// the hashlist is indexed by the "attackee", and the value is the list of "attackers"
	/** The m_attacks. */
	HashMap<WeightedArgument, List<WeightedArgument>> m_attacks = new HashMap<WeightedArgument, List<WeightedArgument>>();

	/**
	 * Attack string.
	 * 
	 * @return the string
	 */
	public String attackString() {
		return attackString(m_attacks);
	}

	/**
	 * Attack string.
	 * 
	 * @param attacks
	 *            the attacks
	 * @return the string
	 */
	public String attackString(HashMap<WeightedArgument, List<WeightedArgument>> attacks) {
		String tmp = "";
		for (WeightedArgument a : attacks.keySet()) {
			tmp += "A" + a.m_a.m_ID + " {";
			for (WeightedArgument a1 : attacks.get(a)) {
				tmp += "A" + a1.m_a.m_ID + " ";
			}
			tmp += "} ";
		}
		return tmp;
	}

	/**
	 * Adds the attack.
	 * 
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 */
	public void addAttack(WeightedArgument a, WeightedArgument b) {
		addAttack(a, b, m_attacks);
	}

	/**
	 * Adds the attack.
	 * 
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @param attacks
	 *            the attacks
	 */
	public void addAttack(WeightedArgument a, WeightedArgument b, HashMap<WeightedArgument, List<WeightedArgument>> attacks) {
		List<WeightedArgument> l = attacks.get(b);
		if (l == null) {
			l = new LinkedList<WeightedArgument>();
			attacks.put(b, l);
		}
		l.add(a);
	}

	/**
	 * Removes the attack.
	 * 
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @param attacks
	 *            the attacks
	 */
	public void removeAttack(WeightedArgument a, WeightedArgument b, HashMap<WeightedArgument, List<WeightedArgument>> attacks) {
		List<WeightedArgument> l = attacks.get(b);
		if (l != null)
			l.remove(a);
	}

	/**
	 * Adds the argument.
	 * 
	 * @param a
	 *            the a
	 * @return the weighted argument
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public WeightedArgument addArgument(WeightedArgument a) throws FeatureTermException {
		if (a.m_a.m_type != Argument.ARGUMENT_RULE)
			return null;

		// Check for any attacks:
		for (WeightedArgument a2 : m_arguments) {
			if (a2.m_a.m_rule.pattern.subsumes(a.m_a.m_rule.pattern)) {
				if (a.m_a.m_rule.pattern.subsumes(a2.m_a.m_rule.pattern)) {
					if (DEBUG >= 1)
						System.out.println("Equivalent argument inserted into an argumentation graph:" + a.getID() + " = " + a2.getID());
					removeArgument(a);
					return a2;
				} else {
					if (!a2.m_a.m_rule.solution.equivalents(a.m_a.m_rule.solution))
						addAttack(a, a2);
				}
			} else {
				if (a.m_a.m_rule.pattern.subsumes(a2.m_a.m_rule.pattern)) {
					if (!a2.m_a.m_rule.solution.equivalents(a.m_a.m_rule.solution))
						addAttack(a2, a);
				}
			}
		}

		m_arguments.add(a);
		return a;
	}

	/**
	 * Removes the argument.
	 * 
	 * @param a
	 *            the a
	 */
	public void removeArgument(WeightedArgument a) {
		// remove the argument and attacks TOWARDS it:
		m_arguments.remove(a);
		m_attacks.remove(a);

		// remove attacks FROM it:
		for (WeightedArgument a2 : m_attacks.keySet()) {
			List<WeightedArgument> l = m_attacks.get(a2);
			l.remove(a);
		}
	}

	/**
	 * Gets the arguments.
	 * 
	 * @return the arguments
	 */
	public List<WeightedArgument> getArguments() {
		return m_arguments;
	}

	/**
	 * Gets the arguments.
	 * 
	 * @param agent
	 *            the agent
	 * @return the arguments
	 */
	public List<WeightedArgument> getArguments(String agent) {
		List<WeightedArgument> l = new LinkedList<WeightedArgument>();
		for (WeightedArgument a : m_arguments) {
			if (a.m_a.m_agent.equals(agent))
				l.add(a);
		}
		return l;
	}

	/**
	 * Contains.
	 * 
	 * @param arg
	 *            the arg
	 * @return true, if successful
	 */
	public boolean contains(WeightedArgument arg) {
		return m_arguments.contains(arg);
	}

	/**
	 * Arguments under.
	 * 
	 * @param a
	 *            the a
	 * @return the list
	 */
	public List<WeightedArgument> argumentsUnder(WeightedArgument a) {
		List<WeightedArgument> res = new LinkedList<WeightedArgument>();
		List<WeightedArgument> open = new LinkedList<WeightedArgument>();

		open.add(a);
		while (!open.isEmpty()) {
			WeightedArgument arg = open.remove(0);
			if (!res.contains(arg) && !open.contains(arg)) {
				res.add(arg);
				List<WeightedArgument> tmp = m_attacks.get(arg);
				if (tmp != null)
					for (WeightedArgument arg2 : tmp) {
						if (!res.contains(arg2) && !open.contains(arg2))
							open.add(arg2);
					}
			}
		}

		return res;
	}

	/**
	 * Accepted.
	 * 
	 * @param a
	 *            the a
	 * @return true, if successful
	 */
	public boolean accepted(WeightedArgument a) {
		List<WeightedArgument> l = m_attacks.get(a);
		if (l != null)
			for (WeightedArgument a2 : l) {
				if (accepted(a2))
					return false;
			}
		return true;
	}

	/**
	 * Accepted ignoring.
	 * 
	 * @param a
	 *            the a
	 * @param ignoredAttacks
	 *            the ignored attacks
	 * @return true, if successful
	 */
	public boolean acceptedIgnoring(WeightedArgument a, HashMap<WeightedArgument, List<WeightedArgument>> ignoredAttacks) {
		List<WeightedArgument> l = m_attacks.get(a);
		List<WeightedArgument> li = ignoredAttacks.get(a);
		if (l != null)
			for (WeightedArgument a2 : l) {
				if ((li == null || !li.contains(a2)) && acceptedIgnoring(a2, ignoredAttacks))
					return false;
			}
		return true;
	}

	// Returns an upper bound in the inconsistency budged required so that "a" is accepted:
	// This method uses the simple bound of removing all the attacks of accepted arguments to 'a':
	/**
	 * Inconsistency budget upper bound.
	 * 
	 * @param a
	 *            the a
	 * @return the float
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public float inconsistencyBudgetUpperBound(WeightedArgument a) throws FeatureTermException {
		if (accepted(a))
			return 0;

		List<WeightedArgument> l = m_attacks.get(a);
		float budget = 0;
		if (l != null) {
			for (WeightedArgument a2 : l) {
				if (accepted(a2)) {
					budget += a2.attackStrength(a);
				}
			}
		}
		return budget;
	}

	/*
	 * public float inconsistencyBudget(WeightedArgument a) throws FeatureTermException { float bound =
	 * inconsistencyBudgetUpperBound(a);
	 * 
	 * if (maxDepth(a)<=2) return bound; // when de depth is 2 or less, the UpperBound is exactly the budget
	 * 
	 * System.out.print("[");System.out.flush();
	 * 
	 * float currentBudget = 0; int state = 0; // 0 : normal, 1 : backtracking Pair<Integer,Integer> current = null;
	 * List<WeightedArgument> argumentsUnderA = argumentsUnder(a);
	 * 
	 * List<Pair<Integer,Integer>> stack = new LinkedList<Pair<Integer,Integer>>();
	 * HashMap<WeightedArgument,List<WeightedArgument>> ignoredAttacks = new
	 * HashMap<WeightedArgument,List<WeightedArgument>>();
	 * 
	 * // determine the first state: for(WeightedArgument a2:argumentsUnderA) { List<WeightedArgument> l =
	 * m_attacks.get(a2); if (l!=null && !l.isEmpty()) { current = new
	 * Pair<Integer,Integer>(argumentsUnderA.indexOf(a2),0); break; } }
	 * 
	 * if (current==null) { System.out.print("]");System.out.flush(); return bound; }
	 * 
	 * do{ // System.out.println("Current: " + current.m_a + " - " + current.m_b + " stack size: " + stack.size() +
	 * " attacks: " + attackString(ignoredAttacks));
	 * 
	 * if (state==0) { // System.out.println("Normal"); // add the attack to the ignored list: WeightedArgument a1 =
	 * argumentsUnderA.get(current.m_a); WeightedArgument a2 = m_attacks.get(a1).get(current.m_b); if (a1==null ||
	 * a2==null) System.err.println("Current state is incorrect...");
	 * 
	 * addAttack(a2, a1, ignoredAttacks); currentBudget += a2.attackStrength(a1);
	 * 
	 * if (currentBudget<bound) { // System.out.println("Testing for acceptance..."); if (acceptedIgnoring(a,
	 * ignoredAttacks)) { // better solution found!!! bound = currentBudget;
	 * 
	 * // backtrack!!! stack.add(0,current); state = 1; continue; } else { stack.add(0,current);
	 * 
	 * // determine the next state: boolean found = false; List<WeightedArgument> l = m_attacks.get(a1); if
	 * (l.size()>current.m_b+1) { current = new Pair<Integer,Integer>(current.m_a,current.m_b+1); found = true; } else {
	 * for(int i = current.m_a+1;i<argumentsUnderA.size();i++) { l = m_attacks.get(argumentsUnderA.get(i)); if (l!=null
	 * && !l.isEmpty()) { current = new Pair<Integer,Integer>(i,0); found = true; break; } } } if (!found) { // no need
	 * to keep searching: // System.out.println("Search is over"); System.out.print("]");System.out.flush(); if (bound
	 * != inconsistencyBudgetFast(a)) System.err.println("(DIFFERENT: " + bound + "!=" + inconsistencyBudgetFast(a) +
	 * ")"); return bound; } } } else { // backtrack!!! stack.add(0,current); state = 1; continue; }
	 * 
	 * } else { // backtracking: if (stack.isEmpty()) { System.out.print("]");System.out.flush(); if (bound !=
	 * inconsistencyBudgetFast(a)) System.err.println("(DIFFERENT: " + bound + "!=" + inconsistencyBudgetFast(a) + ")");
	 * return bound; }
	 * 
	 * Pair<Integer,Integer> last = stack.remove(0); // System.out.println("Backtrack to (" + last.m_a + "," + last.m_b
	 * + ")"); WeightedArgument a1 = argumentsUnderA.get(last.m_a); WeightedArgument a2 =
	 * m_attacks.get(a1).get(last.m_b); if (a1==null || a2==null) System.err.println("Last state is incorrect...");
	 * 
	 * removeAttack(a2, a1, ignoredAttacks);
	 * 
	 * // reassess the budget: currentBudget = 0; for(WeightedArgument b1:ignoredAttacks.keySet()) {
	 * List<WeightedArgument> l = ignoredAttacks.get(b1); for(WeightedArgument b2:l)
	 * currentBudget+=b2.attackStrength(b1); }
	 * 
	 * // determine the next state: List<WeightedArgument> l = m_attacks.get(a1); boolean found = false; if
	 * (l.size()>last.m_b+1) { current = new Pair<Integer,Integer>(last.m_a,last.m_b+1); found = true; } else { for(int
	 * i = last.m_a+1;i<argumentsUnderA.size();i++) { l = m_attacks.get(argumentsUnderA.get(i)); if (l!=null &&
	 * !l.isEmpty()) { current = new Pair<Integer,Integer>(i,0); found = true; break; } } } if (found) state = 0; }
	 * }while(!stack.isEmpty() || state == 0);
	 * 
	 * System.out.print("]");System.out.flush(); if (bound != inconsistencyBudgetFast(a))
	 * System.err.println("(DIFFERENT: " + bound + "!=" + inconsistencyBudgetFast(a) + ")"); return bound; }
	 */

	/**
	 * Inconsistency budget.
	 * 
	 * @param a
	 *            the a
	 * @return the float
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public float inconsistencyBudget(WeightedArgument a) throws FeatureTermException {
		float bound = inconsistencyBudgetUpperBound(a);

		if (maxDepth(a) <= 2)
			return bound; // when de depth is 2 or less, the UpperBound is exactly the budget

		System.out.print("{");
		System.out.flush();

		float currentBudget = 0;
		int state = 0; // 0 : normal, 1 : backtracking
		Pair<Integer, Integer> current = null;
		List<WeightedArgument> attacksToA = m_attacks.get(a);
		List<WeightedArgument> argumentsUnderA = argumentsUnder(a);
		List<WeightedArgument> argumentsThatMustBeIgnored = new LinkedList<WeightedArgument>();

		List<Pair<Integer, Integer>> stack = new LinkedList<Pair<Integer, Integer>>();
		HashMap<WeightedArgument, List<WeightedArgument>> ignoredAttacks = new HashMap<WeightedArgument, List<WeightedArgument>>();

		// Check how many of those "argumentsUnderA" are direct attacks to A which are not attacked by anyone:
		for (WeightedArgument a2 : argumentsUnderA) {
			if (attacksToA.contains(a2)) {
				List<WeightedArgument> l = m_attacks.get(a2);
				if (l == null || l.isEmpty()) {
					// this one MUST be removed:
					argumentsThatMustBeIgnored.add(a2);
					currentBudget += a2.attackStrength(a);
					addAttack(a2, a, ignoredAttacks);
				}
			}
		}

		argumentsUnderA.removeAll(argumentsThatMustBeIgnored);

		// determine the first state:
		for (WeightedArgument a2 : argumentsUnderA) {
			List<WeightedArgument> l = m_attacks.get(a2);
			if (l != null && !l.isEmpty()) {
				current = new Pair<Integer, Integer>(argumentsUnderA.indexOf(a2), 0);
				break;
			}
		}

		if (current == null) {
			System.out.print("}");
			System.out.flush();
			return bound;
		}

		do {
			// System.out.println("Current: " + current.m_a + " - " + current.m_b + " stack size: " + stack.size() +
			// " attacks: " + attackString(ignoredAttacks));

			if (state == 0) {
				// System.out.println("Normal");
				// add the attack to the ignored list:
				WeightedArgument a1 = argumentsUnderA.get(current.m_a);
				WeightedArgument a2 = m_attacks.get(a1).get(current.m_b);
				if (a1 == null || a2 == null)
					System.err.println("Current state is incorrect...");

				addAttack(a2, a1, ignoredAttacks);
				currentBudget += a2.attackStrength(a1);

				if (currentBudget < bound) {
					// System.out.println("Testing for acceptance...");
					if (acceptedIgnoring(a, ignoredAttacks)) {
						// better solution found!!!
						bound = currentBudget;

						// System.out.println("Better solution found!!!!!! -> " + bound);

						// backtrack!!!
						stack.add(0, current);
						state = 1;
						continue;
					} else {
						stack.add(0, current);

						// determine the next state:
						boolean found = false;
						List<WeightedArgument> l = m_attacks.get(a1);
						if (l.size() > current.m_b + 1) {
							current = new Pair<Integer, Integer>(current.m_a, current.m_b + 1);
							found = true;
						} else {
							for (int i = current.m_a + 1; i < argumentsUnderA.size(); i++) {
								l = m_attacks.get(argumentsUnderA.get(i));
								if (l != null && !l.isEmpty()) {
									current = new Pair<Integer, Integer>(i, 0);
									found = true;
									break;
								}
							}
						}
						if (!found) {
							// no need to keep searching:
							// System.out.println("Search is over");
							System.out.print("}");
							System.out.flush();
							return bound;
						}
					}
				} else {
					// backtrack!!!
					stack.add(0, current);
					state = 1;
					continue;
				}

			} else {
				// backtracking:
				if (stack.isEmpty()) {
					System.out.print("}");
					System.out.flush();
					return bound;
				}

				Pair<Integer, Integer> last = stack.remove(0);
				// System.out.println("Backtrack to (" + last.m_a + "," + last.m_b + ")");
				WeightedArgument a1 = argumentsUnderA.get(last.m_a);
				WeightedArgument a2 = m_attacks.get(a1).get(last.m_b);
				if (a1 == null || a2 == null)
					System.err.println("Last state is incorrect...");

				removeAttack(a2, a1, ignoredAttacks);

				// reassess the budget:
				currentBudget = 0;
				for (WeightedArgument b1 : ignoredAttacks.keySet()) {
					List<WeightedArgument> l = ignoredAttacks.get(b1);
					for (WeightedArgument b2 : l)
						currentBudget += b2.attackStrength(b1);
				}

				// determine the next state:
				List<WeightedArgument> l = m_attacks.get(a1);
				boolean found = false;
				if (l.size() > last.m_b + 1) {
					current = new Pair<Integer, Integer>(last.m_a, last.m_b + 1);
					found = true;
				} else {
					for (int i = last.m_a + 1; i < argumentsUnderA.size(); i++) {
						l = m_attacks.get(argumentsUnderA.get(i));
						if (l != null && !l.isEmpty()) {
							current = new Pair<Integer, Integer>(i, 0);
							found = true;
							break;
						}
					}
				}
				if (found)
					state = 0;
			}
		} while (!stack.isEmpty() || state == 0);

		System.out.print("}");
		System.out.flush();
		return bound;
	}

	/**
	 * Gets the attacks of.
	 * 
	 * @param arg
	 *            the arg
	 * @return the attacks of
	 */
	public List<WeightedArgument> getAttacksOf(WeightedArgument arg) {
		return m_attacks.get(arg);
	}

	// This method estimates the examination of a given argument by all the agents, given the examinations in the
	// arguments of the AF:
	/**
	 * Examination estimation.
	 * 
	 * @param arg
	 *            the arg
	 * @return the list
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	List<ArgumentExaminationRecord> examinationEstimation(WeightedArgument arg) throws FeatureTermException {
		List<ArgumentExaminationRecord> result = new LinkedList<ArgumentExaminationRecord>();
		HashMap<String, List<WeightedArgument>> covered = new HashMap<String, List<WeightedArgument>>();

		for (WeightedArgument arg2 : m_arguments) {
			if (arg.m_a.m_rule.pattern.subsumes(arg2.m_a.m_rule.pattern)) {
				for (ArgumentExaminationRecord aer : arg2.getExaminations()) {
					List<WeightedArgument> l = covered.get(aer.m_agent_name);
					if (l == null) {
						l = new LinkedList<WeightedArgument>();
						covered.put(aer.m_agent_name, l);
					}

					boolean mostGeneral = true;
					List<WeightedArgument> toDelete = new LinkedList<WeightedArgument>();
					for (WeightedArgument arg3 : l) {
						if (arg3.m_a.m_rule.pattern.subsumes(arg2.m_a.m_rule.pattern)) {
							mostGeneral = false;
							break;
						} else {
							if (arg2.m_a.m_rule.pattern.subsumes(arg3.m_a.m_rule.pattern)) {
								toDelete.add(arg3);
							}
						}
					}
					if (mostGeneral) {
						l.removeAll(toDelete);
						l.add(arg2);
					}
				}
			}
		}

		for (String agent : covered.keySet()) {
			ArgumentExaminationRecord aer = new ArgumentExaminationRecord(arg, agent);
			List<WeightedArgument> l = covered.get(agent);
			for (WeightedArgument arg2 : l) {
				ArgumentExaminationRecord aer2 = arg2.getExamination(agent);

				for (FeatureTerm s : aer2.m_histogram.keySet()) {
					Integer n = aer.m_histogram.get(s);
					if (n == null)
						n = 0;
					aer.m_histogram.put(s, n + aer2.m_histogram.get(s));
				}
			}
			result.add(aer);
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public WArgumentationFramework clone() {
		WArgumentationFramework c = new WArgumentationFramework();
		HashMap<WeightedArgument, WeightedArgument> clone_table = new HashMap<WeightedArgument, WeightedArgument>();

		for (WeightedArgument a : m_arguments) {
			WeightedArgument ca = a.clone();
			clone_table.put(a, ca);
			c.m_arguments.add(ca);
		}

		for (WeightedArgument a : m_attacks.keySet()) {
			WeightedArgument ca = clone_table.get(a);
			List<WeightedArgument> l = m_attacks.get(a);
			List<WeightedArgument> cl = new LinkedList<WeightedArgument>();
			for (WeightedArgument a2 : l) {
				WeightedArgument ca2 = clone_table.get(a2);
				cl.add(ca2);
			}
			c.m_attacks.put(ca, cl);
		}

		return c;
	}

	/**
	 * To string.
	 * 
	 * @param dm
	 *            the dm
	 * @return the string
	 */
	public String toString(FTKBase dm) {
		String tmp = "WAF = (A,R)\n  A = {";
		for (WeightedArgument a : m_arguments)
			tmp += a.getID() + ":" + a.m_a.m_rule.solution.toStringNOOS(dm) + " ";
		tmp += "}\n  R = {";
		for (WeightedArgument a : m_attacks.keySet()) {
			List<WeightedArgument> l = m_attacks.get(a);
			for (WeightedArgument a2 : l) {
				tmp += a2.getID() + "->" + a.getID() + " ";
			}
		}
		tmp += "}";
		return tmp;
	}

	// Finds the longest chain of arguments:
	/**
	 * Max depth.
	 * 
	 * @return the int
	 */
	public int maxDepth() {
		int res = 0;
		for (WeightedArgument a : m_arguments) {
			int r = maxDepth(a);
			if (r > res)
				res = r;
		}
		return res;
	}

	/**
	 * Max depth.
	 * 
	 * @param a
	 *            the a
	 * @return the int
	 */
	public int maxDepth(WeightedArgument a) {
		int res = 0;
		List<WeightedArgument> l = m_attacks.get(a);
		if (l == null)
			return 1;
		for (WeightedArgument a2 : l) {
			int r = maxDepth(a2);
			if (r > res)
				res = r;
		}
		return res + 1;
	}

}
