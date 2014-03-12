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

import java.util.LinkedList;
import java.util.List;

import ftl.base.utils.Pair;

// TODO: Auto-generated Javadoc
/*
 * This class stores not only the feature name of a path, but also a reference to the original object
 */
/**
 * The Class AnnotatedPath.
 */
public class AnnotatedPath {

	/** The features. */
	public List<Pair<FeatureTerm, Symbol>> features = new LinkedList<Pair<FeatureTerm, Symbol>>();

	/**
	 * Instantiates a new annotated path.
	 */
	public AnnotatedPath() {
	}

	/**
	 * Instantiates a new annotated path.
	 * 
	 * @param p
	 *            the p
	 */
	public AnnotatedPath(AnnotatedPath p) {
		features.addAll(p.features);
	}

	/**
	 * To path.
	 * 
	 * @return the path
	 */
	public Path toPath() {
		Path p = new Path();

		for (Pair<FeatureTerm, Symbol> f_s : features) {
			p.features.add(f_s.m_b);
		}

		return p;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String tmp = null;
		for (Pair<FeatureTerm, Symbol> f_s : features) {
			if (tmp == null) {
				tmp = "(" + f_s.m_a.getSort().get() + ")" + f_s.m_b.toString();
			} else {
				tmp += ".(" + f_s.m_a.getSort().get() + ")" + f_s.m_b.toString();
			}
		}

		if (tmp == null)
			return "";
		return tmp;
	}
}
