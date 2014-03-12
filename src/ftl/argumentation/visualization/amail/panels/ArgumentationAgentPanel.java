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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;

import ftl.argumentation.visualization.amail.helpers.newPolygon;
import ftl.base.core.FTKBase;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Path;
import ftl.base.utils.Pair;
import ftl.learning.lazymethods.similarity.Distance;

// TODO: Auto-generated Javadoc
/**
 * The Class ArgumentationAgentPanel.
 * 
 * @author santi
 */
public class ArgumentationAgentPanel extends AgentPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	/** The m_parent. */
	public ArgumentationPanel m_parent = null;

	/**
	 * Instantiates a new argumentation agent panel.
	 * 
	 * @param name
	 *            the name
	 * @param cases
	 *            the cases
	 * @param sp
	 *            the sp
	 * @param dp
	 *            the dp
	 * @param dm
	 *            the dm
	 * @param d
	 *            the d
	 * @param cache
	 *            the cache
	 * @param a_drawAgent
	 *            the a_draw agent
	 * @param a_parent
	 *            the a_parent
	 */
	public ArgumentationAgentPanel(String name, List<FeatureTerm> cases, Path sp, Path dp, FTKBase dm, Distance d, boolean cache, boolean a_drawAgent,
			ArgumentationPanel a_parent) {
		super(name, cases, sp, dp, dm, d, cache, a_drawAgent, true);
		m_parent = a_parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csic.iiia.ftl.argumentation.visualization.amail.panels.AgentPanel#drawCaseBase(java.awt.Graphics, int, int,
	 * int, int, csic.iiia.ftl.argumentation.visualization.amail.helpers.newPolygon, java.util.List)
	 */
	public void drawCaseBase(Graphics g, int sx, int sy, int dx, int dy, newPolygon highlightHull, List<Integer> toHighlight) {
		if (m_parent != null && m_parent.token != null && m_parent.token.equals(m_name)) {
			String s = "TOKEN";
			Rectangle2D r1 = g.getFontMetrics().getStringBounds(s, g);
			g.setColor(Color.lightGray);
			g.fillRect(sx + 2, sy + 2, (int) r1.getWidth() + 16, (int) r1.getHeight() + 16);
			g.setColor(Color.white);
			g.drawString(s, sx + 10, sy + 20);
		}

		List<FeatureTerm> latestExamples = new LinkedList<FeatureTerm>();
		if (m_parent != null && m_parent.m_messages != null) {
			for (Pair<String, Object> message : m_parent.m_messages) {
				if (message.m_b instanceof FeatureTerm && !message.m_a.equals(m_name)) {
					latestExamples.add((FeatureTerm) (message.m_b));
				}
			}
		}

		int i = 0;
		for (FeatureTerm c : m_cases) {
			if (latestExamples.contains(c)) {
				int x = caseX(i);
				int y = caseY(i);
				float f = (float) (0.7f + 0.3f * Math.sin(System.currentTimeMillis() * 0.01));
				g.setColor(new Color(f, f, f));
				g.fillArc(x - 8, y - 8, case_size + 16, case_size + 16, 0, 360);
			}
			i++;
		}
		super.drawCaseBase(g, sx, sy, dx, dy, highlightHull, toHighlight);
	}
}
