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
  
 package ftl.argumentation.visualization.amail.panels;

import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;

import ftl.argumentation.core.ArgumentAcceptability;
import ftl.argumentation.core.ArgumentationTree;
import ftl.base.core.FTKBase;
import ftl.base.core.Path;
import ftl.base.utils.Pair;

// TODO: Auto-generated Javadoc
/**
 * The Class ArgumentationPanel.
 * 
 * @author santi
 */
public abstract class ArgumentationPanel extends JPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 2029805867440704769L;

	/** The apl. */
	public List<AgentPanel> apl = new LinkedList<AgentPanel>();

	/** The token. */
	public String token = null;

	/** The aal. */
	List<ArgumentAcceptability> aal = new LinkedList<ArgumentAcceptability>();

	/** The trees. */
	List<ArgumentationTree> trees = new LinkedList<ArgumentationTree>();

	/** The m_domain model. */
	FTKBase m_domainModel = null;

	/** The m_description path. */
	Path m_solutionPath = null, m_descriptionPath = null;

	/** The m_messages. */
	public List<Pair<String, Object>> m_messages = null;

	/**
	 * Adds the tree.
	 * 
	 * @param at
	 *            the at
	 * @param agent
	 *            the agent
	 * @param retracted
	 *            the retracted
	 */
	public abstract void addTree(ArgumentationTree at, String agent, boolean retracted);

}
