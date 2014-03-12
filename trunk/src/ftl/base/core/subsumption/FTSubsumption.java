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
  
 package ftl.base.core.subsumption;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import ftl.base.core.FeatureTerm;
import ftl.base.core.FloatFeatureTerm;
import ftl.base.core.IntegerFeatureTerm;
import ftl.base.core.SetFeatureTerm;
import ftl.base.core.Symbol;
import ftl.base.core.SymbolFeatureTerm;
import ftl.base.core.TermFeatureTerm;
import ftl.base.utils.FeatureTermException;
import ftl.base.utils.SubsumptionTimeOutException;

/**
 * The Class FTSubsumption.
 * 
 * @author santi
 */
public class FTSubsumption {

	/** The Constant STATE_NORMAL. */
	public static final int STATE_NORMAL = 0;

	/** The Constant STATE_FALSE. */
	public static final int STATE_FALSE = 1;

	/** The Constant STATE_TRUE. */
	public static final int STATE_TRUE = 2;

	/** The Constant STATE_CONTINUING. */
	public static final int STATE_CONTINUING = 3;

	/**
	 * Subsumes.
	 * 
	 * @param f1
	 *            the f1
	 * @param f2
	 *            the f2
	 * @return true, if successful
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public static boolean subsumes(FeatureTerm f1, FeatureTerm f2) throws FeatureTermException {
		List<FeatureTerm> bindings_a = new ArrayList<FeatureTerm>();
		List<FeatureTerm> bindings_b = new ArrayList<FeatureTerm>();
		return FTSubsumption.subsumptionWithBindings(f1, f2, bindings_a, bindings_b, 0);
	}

	/**
	 * Subsumes.
	 * 
	 * @param f1
	 *            the f1
	 * @param f2
	 *            the f2
	 * @param maxTime
	 *            the max time
	 * @return true, if successful
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public static boolean subsumes(FeatureTerm f1, FeatureTerm f2, int maxTime) throws FeatureTermException {
		List<FeatureTerm> bindings_a = new ArrayList<FeatureTerm>();
		List<FeatureTerm> bindings_b = new ArrayList<FeatureTerm>();
		return FTSubsumption.subsumptionWithBindings(f1, f2, bindings_a, bindings_b, maxTime);
	}

	/**
	 * Subsumption with bindings. You can provide a "maxTime" parameter to this method, after which it will generate a
	 * time out exception. If a time out of 0 or lower is specified, then this parameter is ignored, and the method runs
	 * for as long as needed.
	 * 
	 * 
	 * @param t1
	 *            the t1
	 * @param t2
	 *            the t2
	 * @param bindings_a
	 *            the bindings_a
	 * @param bindings_b
	 *            the bindings_b
	 * @param maxTime
	 *            the max time
	 * @return true, if successful
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	private static boolean subsumptionWithBindings(FeatureTerm t1, FeatureTerm t2, List<FeatureTerm> bindings_a, List<FeatureTerm> bindings_b, int maxTime)
			throws FeatureTermException {

		List<SubsumptionStackNode> stack = new LinkedList<SubsumptionStackNode>();
		SubsumptionStackNode stack_node;
		List<SubsumptionBackTrackNode> stack_backtracking = new LinkedList<SubsumptionBackTrackNode>();
		FeatureTerm f1 = null, f2 = null;
		FeatureTerm set1[] = null, set2[] = null;
		int set1l = 0, set2l = 0;
		int[] assignment = null;
		int assignment_pos = 0;
		int state;
		boolean res = true;
		int i;
		long start = System.currentTimeMillis();

		if (t1 == t2) {
			return true;
		}
		if (t1 == null || t2 == null) {
			return false;
		}

		stack.add(0, new SubsumptionStackNode(t1, t2, 0, null, null, 0, null, 0, -1));
		while (!stack.isEmpty()) {

			if (maxTime > 0) {
				long current = System.currentTimeMillis();
				if ((current - start) > maxTime)
					throw new SubsumptionTimeOutException("");
			}

			stack_node = stack.remove(0);
			f1 = stack_node.mF1;
			f2 = stack_node.mF2;
			state = stack_node.mState;
			assignment = stack_node.mAssignment;
			set1 = stack_node.mSet1;
			set2 = stack_node.mSet2;
			set1l = stack_node.mSet1l;
			set2l = stack_node.mSet2l;
			assignment_pos = stack_node.mAssignmentPos;

			// Initial subsumption tests: name,sort, etc.
			if (state == STATE_NORMAL) {
				state = stateNormal1(bindings_a, bindings_b, f1, f2, state);
			} // if

			if (state == STATE_NORMAL) {
				state = stateNormal2(f1, f2, state);
			} // if

			if (state == STATE_NORMAL) {
				state = stateNormal3(f1, f2, state);
			} // if

			if (state == STATE_NORMAL || state == STATE_TRUE) {
				stateNormal4(bindings_a, bindings_b, f1, f2);
			} // if

			if (state == STATE_CONTINUING) {
				state = STATE_NORMAL;
			}

			// Subsumption:
			if (state == STATE_NORMAL) {
				if (f1 instanceof SetFeatureTerm || f2 instanceof SetFeatureTerm) {
					boolean interrupted = false;

					// Prepare the sets:
					if (set1 == null || set2 == null) {
						if (f1 instanceof SetFeatureTerm) {
							if (((SetFeatureTerm) f1).getSetValues().size() > 0) {
								set1 = new FeatureTerm[((SetFeatureTerm) f1).getSetValues().size()];
							} else {
								set1 = null;
							} // if
							set1l = 0;
							for (FeatureTerm ft : ((SetFeatureTerm) f1).getSetValues()) {
								set1[set1l++] = ft;
							}
						} else {
							set1 = new FeatureTerm[1];
							set1l = 0;
							set1[set1l++] = f1;
						} // if
						if (f2 instanceof SetFeatureTerm) {
							if (((SetFeatureTerm) f2).getSetValues().size() > 0) {
								set2 = new FeatureTerm[((SetFeatureTerm) f2).getSetValues().size()];
							} else {
								set2 = null;
							} // if
							set2l = 0;
							for (FeatureTerm ft : ((SetFeatureTerm) f2).getSetValues()) {
								set2[set2l++] = ft;
							}
						} else {
							set2 = new FeatureTerm[1];
							set2l = 0;
							set2[set2l++] = f2;
						} // if

						if (set1l == 0) {
							state = STATE_TRUE;
						}
						if (set1l > set2l) {
							state = STATE_FALSE;
						}
					} // if

					// Assign an element of the set2 to each element of the set1:
					if (state == STATE_NORMAL) {

						if (assignment == null) {
							assignment = new int[set1l];
							for (i = 0; i < set1l; i++) {
								assignment[i] = i;
							}
							assignment_pos = 0;
						} // if

						if (assignment_pos >= set1l) {
							state = STATE_TRUE;
						} else {
							// create a backtracking node:
							{
								SubsumptionBackTrackNode b_node;

								b_node = new SubsumptionBackTrackNode();
								b_node.n_bindings = bindings_a.size();
								{
									int assignment_copy[] = new int[set1l];
									for (i = 0; i < set1l; i++) {
										assignment_copy[i] = assignment[i];
									}
									b_node.m_node = new SubsumptionStackNode(f1, f2, 3, assignment_copy, set1, set1l, set2, set2l, assignment_pos);
								}
								b_node.m_stack = new LinkedList<SubsumptionStackNode>();

								for (SubsumptionStackNode s_node : stack) {
									if (s_node.mAssignment != null) {
										int assignment_copy[] = new int[s_node.mSet1l];
										for (i = 0; i < s_node.mSet1l; i++) {
											assignment_copy[i] = s_node.mAssignment[i];
										}
										b_node.m_stack.add(new SubsumptionStackNode(s_node.mF1, s_node.mF2, s_node.mState, assignment_copy, set1,
												s_node.mSet1l, set2, s_node.mSet2l, s_node.mAssignmentPos));
									} else {
										b_node.m_stack.add(new SubsumptionStackNode(s_node.mF1, s_node.mF2, s_node.mState, null, null, 0, null, 0, -1));
									} // if
								} // while

								stack_backtracking.add(0, b_node);
							}

							stack.add(0, new SubsumptionStackNode(f1, f2, 3, assignment, set1, set1l, set2, set2l, assignment_pos + 1));
							stack.add(0, new SubsumptionStackNode(set1[assignment_pos], set2[assignment[assignment_pos]], 0, null, null, 0, null, 0, -1));
							interrupted = true;
						} // if
					} // if

					if (!interrupted) {
						set1 = null;
						set2 = null;
					} // if
				} else {
					state = singleObjectSubsumption(stack, f1, f2, state);
				} // if

			} // if

			if (state == STATE_FALSE) {
				if (!stack_backtracking.isEmpty()) {
					// backtracking:
					SubsumptionBackTrackNode b_node;
					SubsumptionStackNode s_node;
					boolean found = false;

					do {
						b_node = stack_backtracking.remove(0);
						s_node = b_node.m_node;
						// printf("bt: [%p,%p] ",s_node.m_f1,s_node.m_f2);
						if (get_set_assignment(s_node.mSet1l, s_node.mSet2l, s_node.mAssignment, s_node.mAssignmentPos)) {
							found = true;
						}

					} while (!found && !stack_backtracking.isEmpty());

					if (found) {
						// Reconstruct the stack and bindings:
						// stack:
						stack.clear();

						for (SubsumptionStackNode s_node2 : b_node.m_stack) {
							if (s_node2.mAssignment != null) {
								int assignment_copy[] = new int[s_node2.mSet1l];
								for (i = 0; i < s_node2.mSet1l; i++) {
									assignment_copy[i] = s_node2.mAssignment[i];
								}
								stack.add(new SubsumptionStackNode(s_node2.mF1, s_node2.mF2, s_node2.mState, assignment_copy, set1, s_node2.mSet1l, set2,
										s_node2.mSet2l, s_node2.mAssignmentPos));
							} else {
								stack.add(new SubsumptionStackNode(s_node2.mF1, s_node2.mF2, s_node2.mState, null, null, 0, null, 0, -1));
							} // if
						} // while
						if (b_node.m_node.mAssignment != null) {
							int assignment_copy[] = new int[b_node.m_node.mSet1l];
							for (i = 0; i < b_node.m_node.mSet1l; i++) {
								assignment_copy[i] = b_node.m_node.mAssignment[i];
							}
							stack.add(0, new SubsumptionStackNode(b_node.m_node.mF1, b_node.m_node.mF2, b_node.m_node.mState, assignment_copy, set1,
									b_node.m_node.mSet1l, set2, b_node.m_node.mSet2l, b_node.m_node.mAssignmentPos));
						} else {
							stack.add(0, new SubsumptionStackNode(b_node.m_node.mF1, b_node.m_node.mF2, b_node.m_node.mState, null, null, 0, null, 0, -1));
						} // if

						while (bindings_a.size() > b_node.n_bindings) {
							bindings_a.remove(0);
							bindings_b.remove(0);
						} // while
					} else {
						stack.clear();
						res = false;
					} // if
				} else {
					stack.clear();
					res = false;
				} // if
			}
		}

		return res;
	}

	/**
	 * Single object subsumption.
	 * 
	 * @param stack
	 *            the stack
	 * @param f1
	 *            the f1
	 * @param f2
	 *            the f2
	 * @param state
	 *            the state
	 * @return the int
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	private static int singleObjectSubsumption(List<SubsumptionStackNode> stack, FeatureTerm f1, FeatureTerm f2, int state) throws FeatureTermException {
		// Single object subsumption:

		if (!f1.hasValue()) {
			if (!f1.getSort().subsumes(f2.getSort())) {
				state = STATE_FALSE;
			}
		} else {
			if (f1 instanceof IntegerFeatureTerm) {
				if (((IntegerFeatureTerm) f1).getValue() == null) {
					if (!(f2 instanceof IntegerFeatureTerm)) {
						state = STATE_FALSE;
					}
				} else {
					if (!(f2 instanceof IntegerFeatureTerm) || ((IntegerFeatureTerm) f2).getValue() == null
							|| !((IntegerFeatureTerm) f2).getValue().equals(((IntegerFeatureTerm) f1).getValue())) {
						state = STATE_FALSE;
					}
				}
			} else if (f1 instanceof FloatFeatureTerm) {
				Float f_value = null;

				if (f2 instanceof IntegerFeatureTerm) {
					f_value = new Float((float) ((IntegerFeatureTerm) f2).getValue());
				} // if
				if (f2 instanceof FloatFeatureTerm) {
					f_value = ((FloatFeatureTerm) f2).getValue();
				} // if

				if (((FloatFeatureTerm) f1).getValue() == null) {
				} else {
					if (f_value == null || !f_value.equals(((FloatFeatureTerm) f1).getValue())) {
						state = STATE_FALSE;
					}
				}
			} else if (f1 instanceof SymbolFeatureTerm) {
				if (((SymbolFeatureTerm) f1).getValue() != null) {
					if (!(f2 instanceof SymbolFeatureTerm) || ((SymbolFeatureTerm) f2).getValue() == null) {
						state = STATE_FALSE;
					} else {
						if (!((SymbolFeatureTerm) f2).getValue().equals(((SymbolFeatureTerm) f1).getValue())) {
							state = STATE_FALSE;
						}
					} // if
				} // if
			} else if (f1 instanceof TermFeatureTerm) {
				for (Entry<Symbol, FeatureTerm> feature : ((TermFeatureTerm) f1).getFeatures()) {
					FeatureTerm fv = feature.getValue();
					Symbol fn = feature.getKey();
					FeatureTerm fv2 = null;
					if (f2 instanceof TermFeatureTerm)
						fv2 = f2.featureValue(fn);
					if (fv2 == null) {
						if (!(fv instanceof SetFeatureTerm) || ((SetFeatureTerm) fv).getSetValues().size() != 0) {
							state = STATE_FALSE;
							break;
						}
					} // if
					stack.add(0, new SubsumptionStackNode(fv, fv2, 0, null, null, 0, null, 0, -1));
				}
			} else {
				System.err.println("Warning! weird FeatureTerm class '" + f1.getClass().getName() + "' in subsumption");
			}
		} // if
		return state;
	}

	/**
	 * State normal4.
	 * 
	 * @param bindings_a
	 *            the bindings_a
	 * @param bindings_b
	 *            the bindings_b
	 * @param f1
	 *            the f1
	 * @param f2
	 *            the f2
	 */
	private static void stateNormal4(List<FeatureTerm> bindings_a, List<FeatureTerm> bindings_b, FeatureTerm f1, FeatureTerm f2) {
		if (f2 != null && (f1.getDataType() == f2.getDataType())) {
			bindings_a.add(0, f1);
			bindings_b.add(0, f2);
		} // if
	}

	/**
	 * State normal3.
	 * 
	 * @param f1
	 *            the f1
	 * @param f2
	 *            the f2
	 * @param state
	 *            the state
	 * @return the int
	 */
	private static int stateNormal3(FeatureTerm f1, FeatureTerm f2, int state) {
		// Test sorts:
		if (!(f1 instanceof SetFeatureTerm) && !(f2 instanceof SetFeatureTerm) && f1.getSort() != null && f2.getSort() != null
				&& !f1.getSort().subsumes(f2.getSort())) {
			state = STATE_FALSE;
		}
		return state;
	}

	/**
	 * State normal2.
	 * 
	 * @param f1
	 *            the f1
	 * @param f2
	 *            the f2
	 * @param state
	 *            the state
	 * @return the int
	 */
	private static int stateNormal2(FeatureTerm f1, FeatureTerm f2, int state) {
		// Test names:
		if (f1.getName() != null) {
			if (f2.getName() == null) {
				if (!(f2 instanceof SetFeatureTerm)) {
					state = STATE_FALSE;
				} // if
			} else {
				if (f2.getName().equals(f1.getName())) {
					state = STATE_TRUE;
				} else {
					state = STATE_FALSE;
				}
			} // if
		} // if
		return state;
	}

	/**
	 * State normal1.
	 * 
	 * @param bindings_a
	 *            the bindings_a
	 * @param bindings_b
	 *            the bindings_b
	 * @param f1
	 *            the f1
	 * @param f2
	 *            the f2
	 * @param state
	 *            the state
	 * @return the int
	 */
	private static int stateNormal1(List<FeatureTerm> bindings_a, List<FeatureTerm> bindings_b, FeatureTerm f1, FeatureTerm f2, int state) {
		// Test for path equalties:
		if (f2 != null && (f1.getDataType() == f2.getDataType())) {
			int pos;

			pos = bindings_a.indexOf(f1);
			if (pos != -1) {
				if (bindings_b.get(pos).equals(f2)) {
					// already known to be true:
					state = STATE_TRUE;
				} else {
					FeatureTerm ftc = bindings_b.get(pos);
					if (f1 instanceof IntegerFeatureTerm && f2 instanceof IntegerFeatureTerm) {
						if (ftc instanceof IntegerFeatureTerm && ((IntegerFeatureTerm) f2).getValue() != null
								&& ((IntegerFeatureTerm) f2).getValue() == ((IntegerFeatureTerm) f1).getValue()) {
							state = STATE_TRUE;
						} else {
							state = STATE_FALSE;
						} // if
					} else {
						if (f1 instanceof FloatFeatureTerm && f2 instanceof FloatFeatureTerm) {
							if (ftc instanceof FloatFeatureTerm && ((FloatFeatureTerm) f2).getValue() != null
									&& ((FloatFeatureTerm) f2).getValue() == ((FloatFeatureTerm) f1).getValue()) {
								state = STATE_TRUE;
							} else {
								state = STATE_FALSE;
							} // if
						} else {
							state = STATE_FALSE;
						}
					}
				} // if
			} // if
		} // if
		return state;
	}

	/**
	 * Gets the _set_assignment.
	 * 
	 * @param l1
	 *            the l1
	 * @param l2
	 *            the l2
	 * @param assignment
	 *            the assignment
	 * @param failure_position
	 *            the failure_position
	 * @return the _set_assignment
	 */
	static boolean get_set_assignment(int l1, int l2, int assignment[], int failure_position) {
		int i = l1 - 1, j;
		int start_pos = 0;
		boolean collision;

		if (failure_position >= 0 && failure_position <= l1 - 1) {
			i = failure_position;
			start_pos = failure_position;
		}
		for (j = failure_position + 1; j < l1; j++) {
			assignment[j] = -1;
		}
		do {
			do {
				assignment[i]++;
				collision = false;
				for (j = 0; j < i; j++) {
					if (assignment[i] == assignment[j]) {
						collision = true;
					}
				} // for
			} while (collision && assignment[i] < l2);
			if (assignment[i] < l2) {
				i++;
			} else {
				if (i < l1) {
					assignment[i] = -1;
				}
				i--;
			} // if

			if (i == l1) {
				return true;
			} /* if */
		} while (i >= start_pos);

		return false;
	} /* get_set_assignment */

}
