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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ftl.base.core.FTKBase;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Ontology;
import ftl.base.core.SetFeatureTerm;
import ftl.base.core.Sort;
import ftl.base.core.SymbolFeatureTerm;
import ftl.base.core.TermFeatureTerm;
import ftl.base.utils.FeatureTermException;

// TODO: Auto-generated Javadoc
/**
 * The Class Planning.
 */
public class Planning {

	/** The operator_sort. */
	static Sort and_sort = null, or_sort = null, not_sort = null, when_sort = null, unless_sort = null, if_sort = null, operator_sort = null;

	/**
	 * Inits the planning.
	 * 
	 * @param o
	 *            the o
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public static void initPlanning(Ontology o) throws FeatureTermException {
		and_sort = o.getSort("and");
		or_sort = o.getSort("or");
		not_sort = o.getSort("not");
		when_sort = o.getSort("when");
		unless_sort = o.getSort("unless");
		if_sort = o.getSort("if");
		operator_sort = o.getSort("operator");
	} // initPlanning

	/**
	 * To string nice.
	 * 
	 * @param op
	 *            the op
	 * @param dm
	 *            the dm
	 * @return the string
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public static String toStringNice(FeatureTerm op, FTKBase dm) throws FeatureTermException {
		String out = "";
		FeatureTerm n = op.featureValue("name");
		FeatureTerm v = op.featureValue("parameters");

		if (n != null) {
			out += ((SymbolFeatureTerm) n).getValue().get() + " ";
		} else {
			out += "<operator> ";
		} // if

		if (v != null) {
			if (v instanceof SetFeatureTerm) {
				for (FeatureTerm v2 : ((SetFeatureTerm) v).getSetValues()) {
					if (v2.getName() != null) {
						out += v2.getName().get() + " ";
					} else {
						FeatureTerm tmp = (v2 instanceof TermFeatureTerm ? v2.featureValue("name") : null);
						if (tmp != null && tmp instanceof SymbolFeatureTerm) {
							out += ((SymbolFeatureTerm) tmp).getValue() + " ";
						} else {
							out += v2.toStringNOOS(dm) + " ";
						}
					} // if
				} // while
				System.out.print("");
			} else {
				if (v.getName() != null) {
					out += v.getName().get() + " ";
				} else {
					FeatureTerm tmp = (v instanceof TermFeatureTerm ? v.featureValue("name") : null);
					if (tmp != null && tmp instanceof SymbolFeatureTerm) {
						out += ((SymbolFeatureTerm) tmp).getValue() + " ";
					} else {
						out += v.toStringNOOS(dm) + " ";
					}
				} // if
			} // if
		}
		return out;
	} // printOperatorNice

	/**
	 * Evaluate predicate.
	 * 
	 * @param predicate
	 *            the predicate
	 * @param state
	 *            the state
	 * @param dm
	 *            the dm
	 * @return true, if successful
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public static boolean evaluatePredicate(FeatureTerm predicate, FeatureTerm state, FTKBase dm) throws FeatureTermException {
		if (predicate.getSort().is_a(and_sort)) {
			FeatureTerm f = predicate.featureValue("predicates");
			if (f instanceof SetFeatureTerm) {
				for (FeatureTerm f2 : ((SetFeatureTerm) f).getSetValues()) {
					if (!evaluatePredicate(f2, state, dm))
						return false;
				} // while
				return true;
			} else {
				return evaluatePredicate(f, state, dm);
			} //
		} else if (predicate.getSort().is_a(or_sort)) {
			FeatureTerm f = predicate.featureValue("predicates");
			if (f instanceof SetFeatureTerm) {
				for (FeatureTerm f2 : ((SetFeatureTerm) f).getSetValues()) {
					if (evaluatePredicate(f2, state, dm))
						return true;
				} // while
				return true;
			} else {
				return evaluatePredicate(f, state, dm);
			} //
		} else if (predicate.getSort().is_a(not_sort)) {
			FeatureTerm f = predicate.featureValue("predicate");
			return !evaluatePredicate(f, state, dm);
		} else if (predicate.getSort().is_a(when_sort)) {
			FeatureTerm f = predicate.featureValue("condition");
			if (evaluatePredicate(f, state, dm)) {
				f = predicate.featureValue("effect");
				return !evaluatePredicate(f, state, dm);
			} else {
				return true;
			} // if
		} else if (predicate.getSort().is_a(unless_sort)) {
			FeatureTerm f = predicate.featureValue("condition");
			if (!evaluatePredicate(f, state, dm)) {
				f = predicate.featureValue("effect");
				return !evaluatePredicate(f, state, dm);
			} else {
				return true;
			} // if
		} else if (predicate.getSort().is_a(if_sort)) {
			FeatureTerm f = predicate.featureValue("condition");
			if (evaluatePredicate(f, state, dm)) {
				f = predicate.featureValue("positive-effect");
				return !evaluatePredicate(f, state, dm);
			} else {
				f = predicate.featureValue("negative-effect");
				return !evaluatePredicate(f, state, dm);
			} // if
		} else {
			if (state.getSort().is_a(and_sort)) {
				FeatureTerm f1 = state.featureValue("predicates");
				return predicate.subsumes(f1);
			} else {
				// Game state is not an and of things... not supported yet...
				System.err.println("evaluatePredicate: Game State is not an 'and' construction!\n");
				return false;
			} // if
		} // if
	} // evaluatePredicate

	/**
	 * Valid instantiations.
	 * 
	 * @param dm
	 *            the dm
	 * @param objects
	 *            the objects
	 * @param state
	 *            the state
	 * @return the list
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public static List<FeatureTerm> validInstantiations(FTKBase dm, List<FeatureTerm> objects, FeatureTerm state) throws FeatureTermException {
		Set<FeatureTerm> operators = dm.searchFT(operator_sort);
		List<FeatureTerm> res = new LinkedList<FeatureTerm>();

		for (FeatureTerm f : operators) {
			res.addAll(validInstantiations(f, objects, state, dm));
		} // while

		return res;
	} // validInstantiations

	/**
	 * Valid instantiations.
	 * 
	 * @param op
	 *            the op
	 * @param objects
	 *            the objects
	 * @param state
	 *            the state
	 * @param dm
	 *            the dm
	 * @return the list
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	static List<FeatureTerm> validInstantiations(FeatureTerm op, List<FeatureTerm> objects, FeatureTerm state, FTKBase dm) throws FeatureTermException {
		List<FeatureTerm> result = new LinkedList<FeatureTerm>();

		// printf("validInstantiations\n");
		validInstantiationsAuxiliar(op, state, objects, dm, 0, result);
		// printf("found %i\n",result.Length());

		return result;
	} // validInstantiations

	/**
	 * Valid instantiations auxiliar.
	 * 
	 * @param op
	 *            the op
	 * @param state
	 *            the state
	 * @param objects
	 *            the objects
	 * @param dm
	 *            the dm
	 * @param alreadyMatched
	 *            the already matched
	 * @param result
	 *            the result
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	static void validInstantiationsAuxiliar(FeatureTerm op, FeatureTerm state, List<FeatureTerm> objects, FTKBase dm, int alreadyMatched,
			List<FeatureTerm> result) throws FeatureTermException {
		FeatureTerm parameter;
		FeatureTerm f;
		FeatureTerm clone;

		f = op.featureValue("parameters");
		if (f instanceof SetFeatureTerm) {
			if (alreadyMatched >= ((SetFeatureTerm) f).getSetValues().size()) {
				if (validOperatorInstantiation(op, state, dm))
					result.add(op);
				return;
			} // if
		} else {
			if (alreadyMatched >= 1) {
				if (validOperatorInstantiation(op, state, dm))
					result.add(op);
				return;
			} // if
		} // if

		if (f instanceof SetFeatureTerm) {
			parameter = ((SetFeatureTerm) f).getSetValues().get(alreadyMatched);
		} else {
			parameter = f;
		} // if

		for (FeatureTerm object : objects) {
			if (parameter.subsumes(object)) {
				HashMap<FeatureTerm, FeatureTerm> correspondences = new HashMap<FeatureTerm, FeatureTerm>();
				correspondences.put(parameter, object);
				clone = op.clone(correspondences);

				validInstantiationsAuxiliar(clone, state, objects, dm, alreadyMatched + 1, result);
			} // if
		} // while
	} // validInstantiationsAuxiliar

	/**
	 * Valid operator instantiation.
	 * 
	 * @param op
	 *            the op
	 * @param state
	 *            the state
	 * @param dm
	 *            the dm
	 * @return true, if successful
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	static boolean validOperatorInstantiation(FeatureTerm op, FeatureTerm state, FTKBase dm) throws FeatureTermException {
		FeatureTerm predicate = op.featureValue("precondition");

		if (predicate != null) {
			boolean retval = evaluatePredicate(predicate, state, dm);
			// printf("validOperatorInstantiation: %s\n",(retval ? "true":"false"));
			return retval;
		} // if
		System.err.println("validOperatorInstantiation: empty predicate!");
		return true;
	} // validOperatorInstantiation

	/**
	 * Apply operator.
	 * 
	 * @param op
	 *            the op
	 * @param state
	 *            the state
	 * @param dm
	 *            the dm
	 * @return the feature term
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public static FeatureTerm applyOperator(FeatureTerm op, FeatureTerm state, FTKBase dm) throws FeatureTermException {
		FeatureTerm effect = op.featureValue("effect");
		HashMap<FeatureTerm, FeatureTerm> correspondences = new HashMap<FeatureTerm, FeatureTerm>();

		FeatureTerm effect_clone = effect.clone(correspondences);
		FeatureTerm state_clone = state.clone(correspondences);
		;

		if (!evaluatePredicate(effect_clone, state_clone, dm)) {
			state_clone = executeEffect(effect_clone, state_clone, dm);
		} // if

		return state_clone;
	} /* applyOperator */

	/**
	 * Execute effect.
	 * 
	 * @param effect
	 *            the effect
	 * @param s
	 *            the s
	 * @param dm
	 *            the dm
	 * @return the feature term
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	static FeatureTerm executeEffect(FeatureTerm effect, FeatureTerm s, FTKBase dm) throws FeatureTermException {
		if (effect.getSort().is_a(and_sort)) {
			for (FeatureTerm f : ((SetFeatureTerm) effect.featureValue("predicates")).getSetValues()) {
				if (!evaluatePredicate(f, s, dm)) {
					s = executeEffect(f, s, dm);
				} // if
			}
		} else if (effect.getSort().is_a(or_sort)) {
			System.err.println("executeNegatedEffect: effects of an operator has an 'or' construct! Not suppoerted...\n");
		} else if (effect.getSort().is_a(not_sort)) {
			FeatureTerm f1 = effect.featureValue("predicate");
			s = executeNegatedEffect(f1, s, dm);
		} else if (effect.getSort().is_a(when_sort)) {
			FeatureTerm f = effect.featureValue("condition");
			if (evaluatePredicate(f, s, dm)) {
				f = effect.featureValue("effect");
				s = executeEffect(f, s, dm);
			} // if
		} else if (effect.getSort().is_a(unless_sort)) {
			FeatureTerm f = effect.featureValue("condition");
			if (evaluatePredicate(f, s, dm)) {
				f = effect.featureValue("effect");
				s = executeEffect(f, s, dm);
			} // if
		} else if (effect.getSort().is_a(if_sort)) {
			FeatureTerm f = effect.featureValue("condition");
			if (evaluatePredicate(f, s, dm)) {
				f = effect.featureValue("positive-effect");
				s = executeEffect(f, s, dm);
			} else {
				f = effect.featureValue("negative-effect");
				s = executeEffect(f, s, dm);
			} // if
		} else {
			if (s.getSort().is_a(and_sort)) {
				FeatureTerm f1 = s.featureValue("predicates");
				((SetFeatureTerm) f1).addSetValue(effect);
			} else {
				// Game state is not an and of things... not supported yet...
				System.err.println("executeEffect: Game State is not an 'and' construction, cannot add a new predicate!\n");
			} // if
		} // if

		return s;
	} /* executeEffect */

	/**
	 * Execute negated effect.
	 * 
	 * @param effect
	 *            the effect
	 * @param s
	 *            the s
	 * @param dm
	 *            the dm
	 * @return the feature term
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	static FeatureTerm executeNegatedEffect(FeatureTerm effect, FeatureTerm s, FTKBase dm) throws FeatureTermException {
		if (effect.getSort().is_a(and_sort)) {
			for (FeatureTerm f : ((SetFeatureTerm) effect.featureValue("predicates")).getSetValues()) {
				if (!evaluatePredicate(f, s, dm)) {
					s = executeNegatedEffect(f, s, dm);
				} // if
			}
		} else if (effect.getSort().is_a(or_sort)) {
			System.err.println("executeNegatedEffect: effects of an operator has an 'or' construct! Not suppoerted...\n");
		} else if (effect.getSort().is_a(not_sort)) {
			FeatureTerm f1 = effect.featureValue("predicate");
			s = executeNegatedEffect(f1, s, dm);
		} else if (effect.getSort().is_a(when_sort)) {
			FeatureTerm f = effect.featureValue("condition");
			if (evaluatePredicate(f, s, dm)) {
				f = effect.featureValue("effect");
				s = executeNegatedEffect(f, s, dm);
			} // if
		} else if (effect.getSort().is_a(unless_sort)) {
			FeatureTerm f = effect.featureValue("condition");
			if (evaluatePredicate(f, s, dm)) {
				f = effect.featureValue("effect");
				s = executeNegatedEffect(f, s, dm);
			} // if
		} else if (effect.getSort().is_a(if_sort)) {
			FeatureTerm f = effect.featureValue("condition");
			if (evaluatePredicate(f, s, dm)) {
				f = effect.featureValue("positive-effect");
				s = executeNegatedEffect(f, s, dm);
			} else {
				f = effect.featureValue("negative-effect");
				s = executeNegatedEffect(f, s, dm);
			} // if
		} else {
			if (s.getSort().is_a(and_sort)) {
				FeatureTerm f1 = s.featureValue("predicates");
				List<FeatureTerm> to_delete = new LinkedList<FeatureTerm>();

				for (FeatureTerm f : ((SetFeatureTerm) f1).getSetValues()) {
					if (effect.subsumes(f)) {
						to_delete.add(f);
					} // if
				} // while

				while (!to_delete.isEmpty()) {
					FeatureTerm f = to_delete.remove(0);
					((SetFeatureTerm) f1).getSetValues().remove(f);
				} // while
			} else {
				// Game state is not an and of things... not supported yet...
				System.err.println("executeNegatedEffect: Game State is not an 'and' construction, cannot add a new predicate!\n");
			} // if
		} // if

		return s;
	} /* executeNegatedEffect */

}
