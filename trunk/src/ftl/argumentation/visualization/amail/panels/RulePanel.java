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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.LinkedList;

import ftl.base.core.FTKBase;
import ftl.base.core.FeatureTerm;
import ftl.base.visualization.FeatureTermTreePanel;

// TODO: Auto-generated Javadoc
/**
 * The Class RulePanel.
 * 
 * @author santi
 */
public class RulePanel extends FeatureTermTreePanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8300019668322105834L;

	/** The m_solution. */
	FeatureTerm m_head, m_solution;

	/** The size_head. */
	Rectangle2D size_head;

	/** The size_solution. */
	Rectangle2D size_solution;

	/**
	 * Instantiates a new rule panel.
	 * 
	 * @param head
	 *            the head
	 * @param solution
	 *            the solution
	 * @param dm
	 *            the dm
	 */
	public RulePanel(FeatureTerm head, FeatureTerm solution, FTKBase dm) {
		super(null, dm);
		m_head = head;
		m_solution = solution;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csic.iiia.ftl.base.visualization.FeatureTermTreePanel#paint(java.awt.Graphics)
	 */
	public void paint(Graphics g) {
		g.clearRect(0, 0, getWidth(), getHeight());
		// super.paint(g);

		if (m_positions == null) {
			m_positions = new HashMap<FeatureTerm, Rectangle2D>();
			size_head = subtreeSize(m_head, g, new LinkedList<FeatureTerm>());
			size_solution = subtreeSize(m_solution, g, new LinkedList<FeatureTerm>());
			int middle_y = (int) Math.max(size_head.getHeight(), size_solution.getHeight()) / 2;
			setPreferredSize(new Dimension((int) (size_head.getWidth() + size_solution.getWidth() + 80 + 40), (int) (Math.max(size_head.getHeight(),
					size_solution.getHeight()) + 40)));

			size_head.setFrame(20, 20 + middle_y - (int) (size_head.getHeight() / 2), size_head.getWidth(), size_head.getHeight());
			size_solution.setFrame((int) (20 + size_head.getWidth() + 100), 20 + middle_y - (int) (size_solution.getHeight() / 2), size_solution.getWidth(),
					size_solution.getHeight());

			computeNodePositions(m_head, g, 20, 20 + middle_y - (int) (size_head.getHeight() / 2));
			computeNodePositions(m_solution, g, (int) (20 + size_head.getWidth() + 100), 20 + middle_y - (int) (size_solution.getHeight() / 2));

		}

		g.setColor(Color.lightGray);
		g.fillRect((int) size_head.getX() - 10, (int) size_head.getY() - 10, (int) size_head.getWidth() + 20, (int) size_head.getHeight() + 20);
		g.fillRect((int) size_solution.getX() - 10, (int) size_solution.getY() - 10, (int) size_solution.getWidth() + 20, (int) size_solution.getHeight() + 20);

		int px[] = { -30, 0, 0, 30, 0, 0, -30 };
		int py[] = { -10, -10, -30, 0, 30, 10, 10 };
		for (int i = 0; i < px.length; i++) {
			px[i] += (int) (20 + size_head.getWidth() + 50);
			py[i] += (int) (20 + Math.max(size_head.getHeight(), size_solution.getHeight()) / 2);
		}
		g.setColor(Color.black);
		g.fillPolygon(px, py, 7);

		if (m_positions != null) {
			for (FeatureTerm f : m_positions.keySet()) {
				Rectangle2D p = m_positions.get(f);
				paintNode(f, g, (int) p.getX(), (int) p.getY());
			}
		}

	}

}
