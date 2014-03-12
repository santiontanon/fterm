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

import java.awt.Dimension;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import ftl.argumentation.core.Argument;
import ftl.argumentation.core.ArgumentAcceptability;
import ftl.argumentation.core.ArgumentationTree;
import ftl.base.core.FTKBase;
import ftl.base.core.Path;

// TODO: Auto-generated Javadoc
/**
 * The Class ArgumentationPanelMultiScrolled.
 * 
 * @author santi
 */
public class ArgumentationPanelMultiScrolled extends ArgumentationPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8863242484616901152L;

	/** The agent pane list. */
	List<JScrollPane> agentPaneList = new LinkedList<JScrollPane>();

	/** The agent pane content. */
	List<ArgumentationStatePanel> agentPaneContent = new LinkedList<ArgumentationStatePanel>();

	/**
	 * Instantiates a new argumentation panel multi scrolled.
	 * 
	 * @param a
	 *            the a
	 * @param a_aa
	 *            the a_aa
	 * @param dm
	 *            the dm
	 * @param sp
	 *            the sp
	 * @param dp
	 *            the dp
	 * @param argumentIDs
	 *            the argument i ds
	 */
	public ArgumentationPanelMultiScrolled(List<AgentPanel> a, List<ArgumentAcceptability> a_aa, FTKBase dm, Path sp, Path dp,
			HashMap<Argument, String> argumentIDs) {
		apl.addAll(a);
		aal.addAll(a_aa);
		m_domainModel = dm;
		m_solutionPath = sp;
		m_descriptionPath = dp;

		// Set up the view, divided in tree (argumentation in the middle, agents on the sides):
		removeAll();
		BoxLayout b1 = new BoxLayout(this, BoxLayout.Y_AXIS);
		setLayout(b1);

		int NAGENTS = apl.size();
		for (int n = 0; n < NAGENTS; n++) {
			AgentPanel ap = apl.get(n);
			ap.setPreferredSize(new Dimension(getSize().width / (NAGENTS + 1), getSize().height));
			ArgumentationStatePanel apc = (agentPaneContent.size() > n ? agentPaneContent.get(n) : null);
			if (apc == null) {
				apc = new ArgumentationStatePanel(this, dm, sp, dp, aal, argumentIDs);
				JScrollPane agentPane = new JScrollPane(apc);
				agentPane.setPreferredSize(new Dimension(getSize().width / (NAGENTS + 1), getSize().height));
				agentPaneList.add(agentPane);
				agentPaneContent.add(apc);
			}
		}

		JPanel tmp1 = new JPanel();
		add(tmp1);
		BoxLayout b2 = new BoxLayout(tmp1, BoxLayout.X_AXIS);
		tmp1.setLayout(b2);
		for (JScrollPane p : agentPaneList)
			tmp1.add(p);

		JPanel tmp2 = new JPanel();
		add(tmp2);
		BoxLayout b3 = new BoxLayout(tmp2, BoxLayout.X_AXIS);
		tmp2.setLayout(b3);
		for (AgentPanel p : apl)
			tmp2.add(p);
	}

	/*
	 * public ArgumentationPanelMultiScrolled(AgentPanel a1, AgentPanel a2, ArgumentAcceptability a_aa1,
	 * ArgumentAcceptability a_aa2, FTKBase dm, Path sp, Path dp, HashMap<Argument,String> argumentIDs) { apl.add(a1);
	 * apl.add(a2); aal.add(a_aa1); aal.add(a_aa2); m_domainModel = dm; m_solutionPath = sp; m_descriptionPath = dp;
	 * 
	 * // Set up the view, divided in tree (argumentation in the middle, agents on the sides): removeAll(); BoxLayout b1
	 * = new BoxLayout(this,BoxLayout.Y_AXIS); setLayout(b1);
	 * 
	 * int NAGENTS = apl.size(); for(int n = 0;n<NAGENTS;n++) { AgentPanel ap = apl.get(n); ap.setPreferredSize(new
	 * Dimension(getSize().width/(NAGENTS+1),getSize().height)); ArgumentationStatePanel apc =
	 * (agentPaneContent.size()>n ? agentPaneContent.get(n):null); if (apc==null) { apc = new
	 * ArgumentationStatePanel(this, dm, sp, dp, aal,argumentIDs); JScrollPane agentPane = new JScrollPane(apc);
	 * agentPane.setPreferredSize(new Dimension(getSize().width/(NAGENTS+1),getSize().height));
	 * agentPaneList.add(agentPane); agentPaneContent.add(apc); } }
	 * 
	 * JPanel tmp1 = new JPanel(); add(tmp1); BoxLayout b2 = new BoxLayout(tmp1,BoxLayout.X_AXIS); tmp1.setLayout(b2);
	 * for(JScrollPane p:agentPaneList) tmp1.add(p);
	 * 
	 * JPanel tmp2 = new JPanel(); add(tmp2); BoxLayout b3 = new BoxLayout(tmp2,BoxLayout.X_AXIS); tmp2.setLayout(b3);
	 * for(AgentPanel p:apl) tmp2.add(p); }
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * csic.iiia.ftl.argumentation.visualization.amail.panels.ArgumentationPanel#addTree(csic.iiia.ftl.argumentation
	 * .core.ArgumentationTree, java.lang.String, boolean)
	 */
	public void addTree(ArgumentationTree at, String agent, boolean retracted) {

		trees.add(at);
		for (int n = 0; n < apl.size(); n++) {
			AgentPanel ap = apl.get(n);
			if (at.getRoot().m_agent.equals(ap.m_name)) {
				agentPaneContent.get(n).addTree(at, agent, retracted);
				break;
			}
		}
	}

}
