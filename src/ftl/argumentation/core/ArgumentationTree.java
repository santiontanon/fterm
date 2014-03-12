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
  
 package ftl.argumentation.core;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ftl.base.core.FeatureTerm;
import ftl.base.utils.FeatureTermException;

// TODO: Auto-generated Javadoc
/**
 * The Class ArgumentationTree.
 * 
 * @author santi
 */
public class ArgumentationTree {

	/** The DEBUG. */
	static public int DEBUG = 0;

	/** The m_root. */
	Argument m_root;

	/** The m_children. */
	HashMap<Argument, List<Argument>> m_children = new HashMap<Argument, List<Argument>>();

	/** The m_parent. */
	HashMap<Argument, Argument> m_parent = new HashMap<Argument, Argument>();

	/** The m_arguments. */
	List<Argument> m_arguments = new LinkedList<Argument>();

	// This list contains the set of agents who have settled for a particular argument,
	// (the agent who generated the argument might not be in the list)
	/** The m_partially_settled. */
	HashMap<Argument, List<String>> m_partially_settled = new HashMap<Argument, List<String>>();

	// This hash stores the retracted arguments. For each argument A, it stores a list with
	// all the arguments that used to attack A but were retracted.
	/** The m_retracted. */
	HashMap<Argument, List<Argument>> m_retracted = new HashMap<Argument, List<Argument>>();

	/**
	 * Instantiates a new argumentation tree.
	 * 
	 * @param root
	 *            the root
	 */
	public ArgumentationTree(Argument root) {
		m_root = root;
		m_arguments.add(root);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public ArgumentationTree clone() {
		ArgumentationTree t = new ArgumentationTree(m_root);
		t.m_arguments.clear();
		for (Argument a : m_children.keySet()) {
			List<Argument> l = m_children.get(a);
			List<Argument> l2 = new LinkedList<Argument>();
			l2.addAll(l);
			t.m_children.put(a, l2);
		}
		t.m_parent.putAll(m_parent);
		t.m_arguments.addAll(m_arguments);

		for (Argument a : m_partially_settled.keySet()) {
			List<String> l = m_partially_settled.get(a);
			List<String> l2 = new LinkedList<String>();
			l2.addAll(l);
			t.m_partially_settled.put(a, l2);
		}
		t.m_retracted = new HashMap<Argument, List<Argument>>();
		for (Argument a : m_retracted.keySet()) {
			List<Argument> l = m_retracted.get(a);
			List<Argument> l2 = new LinkedList<Argument>();
			l2.addAll(l);
			t.m_retracted.put(a, l2);
		}
		return t;
	}

	/**
	 * Adds the attack.
	 * 
	 * @param attacked
	 *            the attacked
	 * @param attacker
	 *            the attacker
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public void addAttack(Argument attacked, Argument attacker) throws FeatureTermException {
		if (m_arguments.contains(attacked)) {
			List<Argument> children = m_children.get(attacked);
			if (children == null) {
				children = new LinkedList<Argument>();
				m_children.put(attacked, children);
			}
			if (!children.contains(attacker)) {
				if (DEBUG >= 1) {
					FeatureTerm a1 = attacked.m_rule.pattern;
					FeatureTerm a2 = (attacker.m_type == Argument.ARGUMENT_EXAMPLE ? attacker.m_example : attacker.m_rule.pattern);

					if (attacked.m_type == Argument.ARGUMENT_EXAMPLE)
						System.err.println("addAttack: adding an attack against an example!!!!!!");
					if (a2.subsumes(a1))
						System.err.println("addAttack: attack equal or more general than oririnal argument!!!!");
				}
				children.add(attacker);
				m_arguments.add(attacker);
				m_parent.put(attacker, attacked);
			} else {
				System.err.println("Adding a repeated attack to a tree!!!");
			}
		} else {
			System.err.println("Adding attack to argument not in the tree!!!");
		}
	}

	/**
	 * Gets the root.
	 * 
	 * @return the root
	 */
	public Argument getRoot() {
		return m_root;
	}

	/**
	 * Gets the arguments.
	 * 
	 * @return the arguments
	 */
	public List<Argument> getArguments() {
		return m_arguments;
	}

	// The difference of this method and the previous, is that this one also returns retracted
	// arguments:
	/**
	 * Gets the all arguments.
	 * 
	 * @return the all arguments
	 */
	public List<Argument> getAllArguments() {
		List<Argument> args = new LinkedList<Argument>();
		args.addAll(m_arguments);
		for (Argument a : getArgumentsWithRetractedChildren()) {
			for (Argument a2 : m_retracted.get(a)) {
				if (!args.contains(a2))
					args.add(a2);
			}
		}
		return args;
	}

	/**
	 * Gets the size.
	 * 
	 * @return the size
	 */
	public int getSize() {
		return m_arguments.size();
	}

	/**
	 * Contains.
	 * 
	 * @param a
	 *            the a
	 * @return true, if successful
	 */
	public boolean contains(Argument a) {
		return m_arguments.contains(a);
	}

	/**
	 * Contains equivalent.
	 * 
	 * @param a
	 *            the a
	 * @return true, if successful
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public boolean containsEquivalent(Argument a) throws FeatureTermException {
		for (Argument a2 : m_arguments) {
			if (a2.equivalents(a))
				return true;
		}
		return false;
	}

	/**
	 * Defeated p.
	 * 
	 * @return true, if successful
	 */
	public boolean defeatedP() {
		return defeatedP(m_root);
	}

	/**
	 * Defeated p.
	 * 
	 * @param a
	 *            the a
	 * @return true, if successful
	 */
	public boolean defeatedP(Argument a) {
		List<Argument> args = m_children.get(a);
		if (args == null)
			return false;
		for (Argument a2 : args) {
			if (a2.m_type == Argument.ARGUMENT_RULE && !defeatedP(a2))
				return true;
		}
		return false;
	}

	/**
	 * Gets the examples.
	 * 
	 * @return the examples
	 */
	public List<FeatureTerm> getExamples() {
		List<FeatureTerm> examples = new LinkedList<FeatureTerm>();

		for (Argument a : m_arguments) {
			if (a.m_type == Argument.ARGUMENT_EXAMPLE) {
				examples.add(a.m_example);
			}
		}

		return examples;
	}

	/**
	 * Gets the examples sent to agent.
	 * 
	 * @param name
	 *            the name
	 * @return the examples sent to agent
	 */
	public List<FeatureTerm> getExamplesSentToAgent(String name) {
		List<FeatureTerm> examples = new LinkedList<FeatureTerm>();

		for (Argument a : m_arguments) {
			if (a.m_type == Argument.ARGUMENT_EXAMPLE) {
				Argument p = m_parent.get(a);
				if (p != null && p.m_agent.equals(name))
					examples.add(a.m_example);
			}
		}

		return examples;
	}

	/**
	 * Gets the rule arguments.
	 * 
	 * @return the rule arguments
	 */
	public List<Argument> getRuleArguments() {
		List<Argument> args = new LinkedList<Argument>();

		for (Argument a : m_arguments) {
			if (a.m_type == Argument.ARGUMENT_RULE) {
				args.add(a);
			}
		}

		return args;
	}

	// Get all the arguments that if defeated, the root would be warranted:
	// It does not take into account whether they are settled or not
	/**
	 * Gets the challengers.
	 * 
	 * @return the challengers
	 */
	public List<Argument> getChallengers() {
		return getChallengers(m_root);
	}

	/**
	 * Gets the challengers.
	 * 
	 * @param a
	 *            the a
	 * @return the challengers
	 */
	public List<Argument> getChallengers(Argument a) {
		List<Argument> args = new LinkedList<Argument>();
		if (defeatedP(a)) {
			for (Argument b : m_children.get(a)) {
				if (b.m_type == Argument.ARGUMENT_RULE && !defeatedP(b)) {
					List<Argument> children = m_children.get(b);
					if (children == null) {
						args.add(b);
					} else {
						List<Argument> args2 = new LinkedList<Argument>();
						for (Argument a2 : children) {
							if (a2.m_type == Argument.ARGUMENT_RULE) {
								args2.addAll(getChallengers(a2));
							}
						}
						if (args2.size() == 0) {
							args.add(b);
						} else {
							args.addAll(args2);
						}
					}
				}
			}
		}
		return args;
	}

	// Get all the arguments that if attacked, could make the root defeated:
	// They are returned in a leaves-to-root order (i.e. leaves first, and root node last)
	/**
	 * Gets the defenders.
	 * 
	 * @param agents
	 *            the agents
	 * @return the defenders
	 */
	public List<Argument> getDefenders(List<ArgumentationAgent> agents) {
		return getDefenders(m_root, agents);
	}

	/**
	 * Gets the defenders.
	 * 
	 * @param a
	 *            the a
	 * @param agents
	 *            the agents
	 * @return the defenders
	 */
	public List<Argument> getDefenders(Argument a, List<ArgumentationAgent> agents) {
		List<Argument> args = new LinkedList<Argument>();
		if (!defeatedP(a) || !settledP(a, agents)) {
			List<Argument> children = m_children.get(a);
			if (children == null) {
				args.add(a);
				return args;
			}
			for (Argument b : children) {
				if (b.m_type == Argument.ARGUMENT_RULE && defeatedP(b)) {
					List<Argument> args2 = new LinkedList<Argument>();
					for (Argument a2 : m_children.get(b)) {
						if (a2.m_type == Argument.ARGUMENT_RULE) {
							args2.addAll(getDefenders(a2, agents));
						}
					}
					args.addAll(args2);
				}
			}
			args.add(a);
		}
		return args;
	}

        
        // I've removed the concept of "settled arguments"
        // TODO: remove the function altogether, and all the things that refer to it
	/**
	 * Settle.
	 * 
	 * @param a
	 *            the a
	 * @param agent
	 *            the agent
	 */
	public void settle(Argument a, String agent) {
            /*
		if (m_arguments.contains(a)) {
			List<String> l = m_partially_settled.get(a);
			if (l == null) {
				l = new LinkedList<String>();
				m_partially_settled.put(a, l);
			}
			l.add(agent);
		}
            */
	}

	/**
	 * Un settle.
	 * 
	 * @param a
	 *            the a
	 * @param agent
	 *            the agent
	 */
	public void unSettle(Argument a, String agent) {
		List<String> l = m_partially_settled.get(a);
		if (!a.m_agent.equals(agent) && l != null && l.contains(agent)) {
			l.remove(agent);
			Argument p = m_parent.get(a);
			if (p != null)
				unSettle(p, agent);
		}
	}

	/**
	 * Un settle.
	 */
	public void unSettle() {
		m_partially_settled.clear();
	}

	/**
	 * Settled p.
	 * 
	 * @param a
	 *            the a
	 * @param agents
	 *            the agents
	 * @return true, if successful
	 */
	public boolean settledP(Argument a, List<ArgumentationAgent> agents) {
		List<String> l = m_partially_settled.get(a);
		for (ArgumentationAgent agent : agents) {
			if (!agent.m_name.equals(a.m_agent) && (l == null || !l.contains(agent.m_name))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Settled p.
	 * 
	 * @param a
	 *            the a
	 * @param m_agent_name
	 *            the m_agent_name
	 * @return true, if successful
	 */
	public boolean settledP(Argument a, String m_agent_name) {
		List<String> l = m_partially_settled.get(a);
		if (!m_agent_name.equals(a.m_agent) && (l == null || !l.contains(m_agent_name))) {
			return false;
		}
		return true;
	}
        
	/**
	 * Settled p.
	 * 
	 * @param a
	 *            the a
	 * @return true, if successful
	 */        
        public boolean settledP(Argument a) {
	        List<String> l = m_partially_settled.get(a);
	        if (l==null || l.isEmpty()) {
	            return false;
	        }
	        return true;
        }        

	/**
	 * Retract unacceptable.
	 * 
	 * @param agent
	 *            the agent
	 * @param aa
	 *            the aa
	 * @return true, if successful
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public boolean retractUnacceptable(String agent, ArgumentAcceptability aa) throws FeatureTermException {
		List<Argument> toDelete = new LinkedList<Argument>();
		boolean retractedRoot = false;
		for (Argument a : m_arguments) {
			if (a.m_agent != null && a.m_agent.equals(agent) && !aa.accepted(a))
				toDelete.add(a);
		}

		for (Argument a : toDelete) {
			if (a == m_root) {
				retractedRoot = true;
				retractArgument(a);
			} else {
				retractArgument(a);
			}
		}
		return retractedRoot;
	}

	/**
	 * Retract argument.
	 * 
	 * @param a
	 *            the a
	 */
	void retractArgument(Argument a) {
		List<Argument> children = m_children.get(a);

		{
			Argument parent = m_parent.get(a);
			List<Argument> l = m_retracted.get(parent);
			if (l == null) {
				l = new LinkedList<Argument>();
				m_retracted.put(parent, l);
			}
			l.add(a);
		}

		if (children != null) {
			List<Argument> la = new LinkedList<Argument>();
			la.addAll(children);
			for (Argument a1 : la)
				retractArgument(a1);
			m_children.remove(a);
		}

		Argument p = m_parent.get(a);
		if (p != null) {
			m_children.get(p).remove(a);
			unSettle(p, a.m_agent);
		}
		m_parent.remove(a);
		m_arguments.remove(a);
		m_partially_settled.remove(a);
	}

	/**
	 * Gets the arguments with retracted children.
	 * 
	 * @return the arguments with retracted children
	 */
	public Set<Argument> getArgumentsWithRetractedChildren() {
		return m_retracted.keySet();
	}

	/**
	 * Gets the retracted children.
	 * 
	 * @param a
	 *            the a
	 * @return the retracted children
	 */
	public List<Argument> getRetractedChildren(Argument a) {
		return m_retracted.get(a);
	}

	/**
	 * Gets the depth.
	 * 
	 * @param a
	 *            the a
	 * @return the depth
	 */
	public int getDepth(Argument a) {
		if (!m_arguments.contains(a))
			return -1;

		int d = 0;
		while (a != m_root && a != null) {
			a = m_parent.get(a);
			d++;
		}
		return d;
	}

	/**
	 * Gets the parent.
	 * 
	 * @param a
	 *            the a
	 * @return the parent
	 */
	public Argument getParent(Argument a) {
		Argument ret = m_parent.get(a);
		if (ret == null) {
			if (a == m_root)
				return null;
			for (Argument a2 : m_retracted.keySet()) {
				if (m_retracted.get(a2).contains(a))
					return a2;
			}
		}
		return ret;
	}

	/**
	 * Gets the children.
	 * 
	 * @param a
	 *            the a
	 * @return the children
	 */
	public List<Argument> getChildren(Argument a) {
		return m_children.get(a);
	}

	/**
	 * To string.
	 * 
	 * @param agents
	 *            the agents
	 * @return the string
	 */
	public String toString(List<ArgumentationAgent> agents) {
		return toString(m_root, 0, agents);
	}

	/**
	 * To string.
	 * 
	 * @param a
	 *            the a
	 * @param tabs
	 *            the tabs
	 * @param agents
	 *            the agents
	 * @return the string
	 */
	public String toString(Argument a, int tabs, List<ArgumentationAgent> agents) {
		String tmp = "";
		for (int i = 0; i < tabs; i++)
			tmp += "  ";
		tmp += a;
		if (defeatedP(a))
			tmp += " (defeated) ";
		if (settledP(a, agents))
			tmp += " (settled) ";
		tmp += "\n";
		List<Argument> children = m_children.get(a);
		if (children == null)
			return tmp;
		for (Argument a2 : children) {
			if (m_parent.get(a2) != a) {
				System.err.println("Inconsistency in the tree!!!! " + a.m_ID + " is not the parent of " + a2.m_ID);
			}
			tmp += toString(a2, tabs + 1, agents);
		}
		return tmp;
	}

}
