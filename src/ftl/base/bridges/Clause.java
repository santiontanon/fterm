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
  
 package ftl.base.bridges;

import java.util.Vector;
import ftl.base.utils.Pair;

/**
 * The Class Clause. It's used to represent a vector of clauses
 * 
 * @author santi
 */
public class Clause {

	/** The Constant TYPE_UNKNOWN. */
	public static final int TYPE_UNKNOWN = -1;

	/** The Constant TYPE_ID. */
	public static final int TYPE_ID = 0;

	/** The Constant TYPE_SYMBOL. */
	public static final int TYPE_SYMBOL = 1;

	/** The Constant TYPE_INTEGER. */
	public static final int TYPE_INTEGER = 2;

	/** The Constant TYPE_FLOAT. */
	public static final int TYPE_FLOAT = 3;

	/** The head. */
	String head;

	/** The parameters. A set of pairs Integer-String */
	Vector<Pair<Integer, String>> parameters;

	/**
	 * Instantiates a new clause.
	 * 
	 * @param h
	 *            the head name
	 */
	public Clause(String h) {
		head = h;
		parameters = new Vector<Pair<Integer, String>>();
	}

	/**
	 * Sets the parameter.
	 * 
	 * @param i
	 *            the position where it starts
	 * @param type
	 *            the type
	 * @param value
	 *            the value
	 */
	public void setParameter(int i, int type, String value) {
		while (parameters.size() <= i) {
			parameters.add(new Pair<Integer, String>(TYPE_UNKNOWN, ""));
		}
		parameters.get(i).m_a = type;
		parameters.get(i).m_b = value;
	}

	/**
	 * Gets the parameter type.
	 * 
	 * @param i
	 *            the parameter position
	 * @return the parameter type
	 */
	public int getParameterType(int i) {
		return parameters.get(i).m_a;
	}

	/**
	 * Gets the parameter value.
	 * 
	 * @param i
	 *            the i
	 * @return the parameter value
	 */
	public String getParameterValue(int i) {
		return parameters.get(i).m_b;
	}

	/**
	 * Gets the number parameters.
	 * 
	 * @return the number parameters
	 */
	public int getNumberParameters() {
		return parameters.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String tmp = head + "(";

		for (int i = 0; i < parameters.size(); i++) {
			Pair<Integer, String> p = parameters.get(i);
			if (i < parameters.size() - 1) {
				tmp += p.m_b + ",";
			} else {
				tmp += p.m_b;
			}
		}
		tmp += ")";
		return tmp;
	}

	/**
	 * Gets the head.
	 * 
	 * @return the head
	 */
	public String getHead() {
		return head;
	}
}
