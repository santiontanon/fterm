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
  
 package ftl.planning;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import ftl.base.core.FTKBase;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Ontology;
import ftl.base.core.SetFeatureTerm;
import ftl.base.utils.FeatureTermException;

// TODO: Auto-generated Javadoc
/**
 * The Class SearchBasedPlanningNode.
 */
public class SearchBasedPlanningNode implements Comparable<SearchBasedPlanningNode> {

	/** The m_state. */
	FeatureTerm m_state;

	/** The m_op. */
	FeatureTerm m_op;

	/** The m_parent. */
	SearchBasedPlanningNode m_parent;

	/** The m_c. */
	float m_h, m_c;

	/** The g_nodes evaluated in last search. */
	static int g_nodesEvaluatedInLastSearch = 0;

	/**
	 * Instantiates a new search based planning node.
	 * 
	 * @param state
	 *            the state
	 * @param op
	 *            the op
	 * @param parent
	 *            the parent
	 * @param h
	 *            the h
	 * @param c
	 *            the c
	 */
	SearchBasedPlanningNode(FeatureTerm state, FeatureTerm op, SearchBasedPlanningNode parent, float h, float c) {
		m_state = state;
		m_op = op;
		m_parent = parent;
		m_h = h;
		m_c = c;
	} // SBPNode

	/**
	 * ID search based plan.
	 * 
	 * @param objects
	 *            the objects
	 * @param initialState
	 *            the initial state
	 * @param goalState
	 *            the goal state
	 * @param domain_model
	 *            the domain_model
	 * @param o
	 *            the o
	 * @return the list
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public static List<FeatureTerm> IDSearchBasedPlan(List<FeatureTerm> objects, FeatureTerm initialState, FeatureTerm goalState, FTKBase domain_model,
			Ontology o) throws FeatureTermException {
		List<FeatureTerm> plan = null;
		int depth = 1;

		do {
			g_nodesEvaluatedInLastSearch = 0;
			System.out.println("Trying Depth " + depth);
			plan = IDSearchBasedPlanAuxiliar(objects, initialState, goalState, domain_model, o, depth);
			depth++;
			System.out.println("Expanded " + g_nodesEvaluatedInLastSearch + " nodes");
		} while (plan == null);

		return plan;
	} // IDSearchBasedPlan

	/**
	 * ID search based plan auxiliar.
	 * 
	 * @param objects
	 *            the objects
	 * @param initialState
	 *            the initial state
	 * @param goalState
	 *            the goal state
	 * @param domain_model
	 *            the domain_model
	 * @param o
	 *            the o
	 * @param maxDepth
	 *            the max depth
	 * @return the list
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	static List<FeatureTerm> IDSearchBasedPlanAuxiliar(List<FeatureTerm> objects, FeatureTerm initialState, FeatureTerm goalState, FTKBase domain_model,
			Ontology o, int maxDepth) throws FeatureTermException {
		List<FeatureTerm> operators;

		if (maxDepth <= 0) {
			g_nodesEvaluatedInLastSearch++;
			if (Planning.evaluatePredicate(goalState, initialState, domain_model)) {
				return new LinkedList<FeatureTerm>();
			} else {
				return null;
			} // if
		} else {
			operators = Planning.validInstantiations(domain_model, objects, initialState);

			for (FeatureTerm op : operators) {
				FeatureTerm newState = Planning.applyOperator(op, initialState, domain_model);
				/*
				 * { char *s1 = initialState.toStringNOOS(domain_model); char *s2 = newState.toStringNOOS(domain_model);
				 * printf("Applying Operator:\n"); printOperatorNice(op,domain_model);
				 * printf("Initial State:\n%s\n",s1); printf("Final State:\n%s\n",s2); delete []s1; delete []s2; }
				 */
				List<FeatureTerm> plan = IDSearchBasedPlanAuxiliar(objects, newState, goalState, domain_model, o, maxDepth - 1);
				if (plan != null) {
					plan.add(0, op);
					return plan;
				} // if
			} // while
			return null;
		} // if
	} // IDSearchBasedPlanAuxiliar

	/**
	 * H search based plan.
	 * 
	 * @param objects
	 *            the objects
	 * @param initialState
	 *            the initial state
	 * @param goalState
	 *            the goal state
	 * @param dm
	 *            the dm
	 * @param o
	 *            the o
	 * @return the list
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public static List<FeatureTerm> HSearchBasedPlan(List<FeatureTerm> objects, FeatureTerm initialState, FeatureTerm goalState, FTKBase dm, Ontology o)
			throws FeatureTermException {
		List<SearchBasedPlanningNode> m_open = new LinkedList<SearchBasedPlanningNode>();
		List<SearchBasedPlanningNode> m_closed = new LinkedList<SearchBasedPlanningNode>();
		SearchBasedPlanningNode node, node2;
		g_nodesEvaluatedInLastSearch = 0;

		node = new SearchBasedPlanningNode(initialState, null, null, heuristic(goalState, initialState, dm, o), 0);
		m_open.add(node);

		while (!m_open.isEmpty()) {
			node = m_open.remove(0);
			m_closed.add(node);

			System.out.println(g_nodesEvaluatedInLastSearch + " -> Node: " + node.m_c + " + " + node.m_h + " [" + m_open.size() + "|" + m_closed.size() + "]");

			List<FeatureTerm> operators = Planning.validInstantiations(dm, objects, node.m_state);

			for (FeatureTerm op : operators) {

				FeatureTerm newState = Planning.applyOperator(op, node.m_state, dm);
				/*
				 * { char *s1 = initialState.toStringNOOS(domain_model); char *s2 = newState.toStringNOOS(domain_model);
				 * printf("Applying Operator:\n"); printOperatorNice(op,domain_model);
				 * printf("Initial State:\n%s\n",s1); printf("Final State:\n%s\n",s2); delete []s1; delete []s2; }
				 */
				g_nodesEvaluatedInLastSearch++;
				if (Planning.evaluatePredicate(goalState, newState, dm)) {
					// Found plan!!!
					System.out.println("Success with " + g_nodesEvaluatedInLastSearch + " expanded nodes\n");

					List<FeatureTerm> res = new LinkedList<FeatureTerm>();

					res.add(op);
					node2 = node;
					while (node2 != null) {
						if (node2.m_op != null)
							res.add(0, node2.m_op);
						node2 = node2.m_parent;
					} // while
					return res;
				} // if
				node2 = new SearchBasedPlanningNode(newState, op, node, heuristic(goalState, newState, dm, o), node.m_c + 1);

				if (!HIDSBPAlreadyExpanded(node2, m_open, m_closed))
					m_open.add(node2);
			} // while

			Collections.sort(m_open);
		} // while

		System.out.println("Failure with " + g_nodesEvaluatedInLastSearch + " expanded nodes");

		return null;
	} // HIDSearchBasedPlan

	/**
	 * HIDSBP already expanded.
	 * 
	 * @param node
	 *            the node
	 * @param m_open
	 *            the m_open
	 * @param m_closed
	 *            the m_closed
	 * @return true, if successful
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	static boolean HIDSBPAlreadyExpanded(SearchBasedPlanningNode node, List<SearchBasedPlanningNode> m_open, List<SearchBasedPlanningNode> m_closed)
			throws FeatureTermException {
		for (SearchBasedPlanningNode node2 : m_closed) {
			if (node2.m_state.subsumes(node.m_state) && node.m_state.subsumes(node2.m_state))
				return true;
		}

		for (SearchBasedPlanningNode node2 : m_open) {
			if (node2.m_state.subsumes(node.m_state) && node.m_state.subsumes(node2.m_state))
				return true;
		}

		return false;
	}

	/**
	 * Heuristic.
	 * 
	 * @param predicate
	 *            the predicate
	 * @param state
	 *            the state
	 * @param dm
	 *            the dm
	 * @param o
	 *            the o
	 * @return the float
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	static float heuristic(FeatureTerm predicate, FeatureTerm state, FTKBase dm, Ontology o) throws FeatureTermException {
		float h = 0;

		if (predicate.getSort().is_a(Planning.and_sort)) {
			FeatureTerm f = predicate.featureValue("predicates");
			if (f instanceof SetFeatureTerm) {
				for (FeatureTerm f2 : ((SetFeatureTerm) f).getSetValues()) {
					h += heuristic(f2, state, dm, o);
				}
				return h;
			} else {
				return heuristic(f, state, dm, o);
			} //
		} else if (predicate.getSort().is_a(Planning.or_sort)) {
			FeatureTerm f = predicate.featureValue("predicates");
			if (f instanceof SetFeatureTerm) {
				for (FeatureTerm f2 : ((SetFeatureTerm) f).getSetValues()) {
					float htmp = heuristic(f2, state, dm, o);
					if (htmp > h)
						h = htmp;
				} // while
				return h;
			} else {
				return heuristic(f, state, dm, o);
			} //
		} else if (predicate.getSort().is_a(Planning.not_sort)) {
			FeatureTerm f = predicate.featureValue("predicate");
			return heuristicNegated(f, state, dm, o);
		} else if (predicate.getSort().is_a(Planning.when_sort)) {
			FeatureTerm f = predicate.featureValue("condition");
			if (Planning.evaluatePredicate(f, state, dm)) {
				f = predicate.featureValue("effect");
				return heuristic(f, state, dm, o);
			} else {
				return 0;
			} // if
		} else if (predicate.getSort().is_a(Planning.unless_sort)) {
			FeatureTerm f = predicate.featureValue("condition");
			if (!Planning.evaluatePredicate(f, state, dm)) {
				f = predicate.featureValue("effect");
				return heuristic(f, state, dm, o);
			} else {
				return 0;
			} // if
		} else if (predicate.getSort().is_a(Planning.if_sort)) {
			FeatureTerm f = predicate.featureValue("condition");
			if (Planning.evaluatePredicate(f, state, dm)) {
				f = predicate.featureValue("positive-effect");
				return heuristic(f, state, dm, o);
			} else {
				f = predicate.featureValue("negative-effect");
				return heuristic(f, state, dm, o);
			} // if
		} else {
			if (state.getSort().is_a(Planning.and_sort)) {
				FeatureTerm f1 = state.featureValue("predicates");
				if (!predicate.subsumes(f1))
					h++;
				return h;
			} else {
				// Game state is not an and of things... not supported yet...
				System.err.println("heuristic: Game State is not an 'and' construction!;");
				System.err.println(state.toStringNOOS(dm));
				return h;
			} // if
		} // if
	} /* heuristic */

	/**
	 * Heuristic negated.
	 * 
	 * @param predicate
	 *            the predicate
	 * @param state
	 *            the state
	 * @param dm
	 *            the dm
	 * @param o
	 *            the o
	 * @return the float
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	static float heuristicNegated(FeatureTerm predicate, FeatureTerm state, FTKBase dm, Ontology o) throws FeatureTermException {
		float h = 0;

		if (predicate.getSort().is_a(Planning.and_sort)) {
			FeatureTerm f = predicate.featureValue("predicates");
			if (f instanceof SetFeatureTerm) {
				for (FeatureTerm f2 : ((SetFeatureTerm) f).getSetValues()) {
					h += heuristicNegated(f2, state, dm, o);
				} // while
				return h;
			} else {
				return heuristicNegated(f, state, dm, o);
			} //
		} else if (predicate.getSort().is_a(Planning.or_sort)) {
			FeatureTerm f = predicate.featureValue("predicates");
			if (f instanceof SetFeatureTerm) {
				for (FeatureTerm f2 : ((SetFeatureTerm) f).getSetValues()) {
					float htmp = heuristicNegated(f2, state, dm, o);
					if (htmp > h)
						h = htmp;
				} // while
				return h;
			} else {
				return heuristicNegated(f, state, dm, o);
			} //
		} else if (predicate.getSort().is_a(Planning.not_sort)) {
			FeatureTerm f = predicate.featureValue("predicate");
			return heuristicNegated(f, state, dm, o);
		} else if (predicate.getSort().is_a(Planning.when_sort)) {
			FeatureTerm f = predicate.featureValue("condition");
			if (Planning.evaluatePredicate(f, state, dm)) {
				f = predicate.featureValue("effect");
				return heuristicNegated(f, state, dm, o);
			} else {
				return 0;
			} // if
		} else if (predicate.getSort().is_a(Planning.unless_sort)) {
			FeatureTerm f = predicate.featureValue("condition");
			if (!Planning.evaluatePredicate(f, state, dm)) {
				f = predicate.featureValue("effect");
				return heuristicNegated(f, state, dm, o);
			} else {
				return 0;
			} // if
		} else if (predicate.getSort().is_a(Planning.if_sort)) {
			FeatureTerm f = predicate.featureValue("condition");
			if (Planning.evaluatePredicate(f, state, dm)) {
				f = predicate.featureValue("positive-effect");
				return heuristicNegated(f, state, dm, o);
			} else {
				f = predicate.featureValue("negative-effect");
				return heuristicNegated(f, state, dm, o);
			} // if
		} else {
			if (state.getSort().is_a(Planning.and_sort)) {
				FeatureTerm f1 = state.featureValue("predicates");
				if (predicate.subsumes(f1))
					h++;
				return h;
			} else {
				// Game state is not an and of things... not supported yet...
				System.err.println("heuristicNegated: Game State is not an 'and' construction!");
				return h;
			} // if
		} // if
	} // heuristicNegated

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(SearchBasedPlanningNode o) {
		SearchBasedPlanningNode n = (SearchBasedPlanningNode) o;
		float f = (m_c + m_h) - (n.m_c + n.m_h);
		if (f < 0)
			return -1;
		if (f > 0)
			return 1;
		return 0;
	}

}
