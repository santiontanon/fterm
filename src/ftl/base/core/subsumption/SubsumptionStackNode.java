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

import ftl.base.core.FeatureTerm;

/**
 * The Class SubsumptionStackNode.
 */
public class SubsumptionStackNode {

	/** The m_f1. */
	FeatureTerm mF1;

	/** The m_f2. */
	FeatureTerm mF2;

	/** The m_state. */
	int mState;

	/** The m_assignment. */
	int[] mAssignment;

	/** The m_set2. */
	FeatureTerm mSet1[], mSet2[];

	/** The m_set2l. */
	int mSet1l, mSet2l;

	/** The m_assignment_pos. */
	int mAssignmentPos;

	/**
	 * Instantiates a new subsumption stack node.
	 * 
	 * @param f1
	 *            the f1
	 * @param f2
	 *            the f2
	 * @param state
	 *            the state
	 * @param assignment
	 *            the assignment
	 * @param set1
	 *            the set1
	 * @param set1l
	 *            the set1l
	 * @param set2
	 *            the set2
	 * @param set2l
	 *            the set2l
	 * @param assignment_pos
	 *            the assignment_pos
	 */
	SubsumptionStackNode(FeatureTerm f1, FeatureTerm f2, int state, int[] assignment, FeatureTerm set1[], int set1l, FeatureTerm set2[], int set2l,
			int assignment_pos) {

		assert (f1 != null || f2 != null);

		mF1 = f1;
		mF2 = f2;
		mState = state;
		mAssignment = assignment;
		mSet1 = set1;
		mSet2 = set2;
		mSet1l = set1l;
		mSet2l = set2l;
		mAssignmentPos = assignment_pos;
	}
}
