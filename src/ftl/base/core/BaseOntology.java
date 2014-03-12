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

import ftl.base.utils.FeatureTermException;

// TODO: Auto-generated Javadoc
/**
 * The Class BaseOntology.
 */
public class BaseOntology extends Ontology {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 5348414900036743640L;

	/**
	 * Instantiates a new base ontology.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public BaseOntology() throws FeatureTermException {
		Sort s;
		s = new Sort("any", null, this);
		s.mDataType = -1;
		sort_list.put(s.get(), s);
		s = new Sort("number", "any", this);
		s.mDataType = -1;
		sort_list.put(s.get(), s);
		s = new Sort("float", "number", this);
		s.mDataType = 1;
		sort_list.put(s.get(), s);
		s = new Sort("integer", "number", this);
		s.mDataType = 0;
		sort_list.put(s.get(), s);
		s = new Sort("symbol", "any", this);
		s.mDataType = 2;
		sort_list.put(s.get(), s);
		s = new Sort("boolean", "any", this);
		s.mDataType = -1;
		sort_list.put(s.get(), s);

		m_name = new Symbol("NOOS Base Ontology");
		m_description = new Symbol("By Santiago Ontanon");
	} /* Ontology::Ontology */
}
