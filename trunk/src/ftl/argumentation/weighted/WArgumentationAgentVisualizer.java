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
  
 package ftl.argumentation.weighted;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import ftl.argumentation.visualization.amail.panels.TrainingSetPanel;
import ftl.base.core.FTKBase;
import ftl.base.core.Path;
import ftl.base.utils.FeatureTermException;
import ftl.learning.lazymethods.similarity.PropertiesDistance;

// TODO: Auto-generated Javadoc
/**
 * The Class WArgumentationAgentVisualizer.
 * 
 * @author santi
 */
public class WArgumentationAgentVisualizer extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4193794829195699797L;

	/** The agv. */
	ArgumentationGraphVisualizer agv = null;

	/** The tsp. */
	TrainingSetPanel tsp = null;

	/**
	 * New window.
	 * 
	 * @param agent
	 *            the agent
	 * @param dx
	 *            the dx
	 * @param dy
	 *            the dy
	 * @param sp
	 *            the sp
	 * @param dp
	 *            the dp
	 * @param dm
	 *            the dm
	 * @param propertiesDistance
	 *            the properties distance
	 * @param avoidPrimaryColors
	 *            the avoid primary colors
	 * @return the j frame
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	static JFrame newWindow(WArgumentationAgent agent, int dx, int dy, Path sp, Path dp, FTKBase dm, PropertiesDistance propertiesDistance,
			boolean avoidPrimaryColors) throws FeatureTermException {
		WArgumentationAgentVisualizer aav = new WArgumentationAgentVisualizer(agent, dx, dy, sp, dp, dm, propertiesDistance, avoidPrimaryColors);
		JFrame frame = new JFrame();
		frame.add(aav);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		return frame;
	}

	/**
	 * Instantiates a new w argumentation agent visualizer.
	 * 
	 * @param agent
	 *            the agent
	 * @param dx
	 *            the dx
	 * @param dy
	 *            the dy
	 * @param sp
	 *            the sp
	 * @param dp
	 *            the dp
	 * @param dm
	 *            the dm
	 * @param propertiesDistance
	 *            the properties distance
	 * @param avoidPrimaryColors
	 *            the avoid primary colors
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public WArgumentationAgentVisualizer(WArgumentationAgent agent, int dx, int dy, Path sp, Path dp, FTKBase dm, PropertiesDistance propertiesDistance,
			boolean avoidPrimaryColors) throws FeatureTermException {
		agv = new ArgumentationGraphVisualizer(dx, dy, agent.m_af, dm);
		agv.setPreferredSize(new Dimension(dx, dy / 2));
		tsp = new TrainingSetPanel(agent.m_examples, sp, dp, dm, propertiesDistance, avoidPrimaryColors);
		tsp.setPreferredSize(new Dimension(dx, dy / 2));
		tsp.setBackground(Color.white);

		BoxLayout b1 = new BoxLayout(this, BoxLayout.Y_AXIS);
		setLayout(b1);

		add(agv);
		add(tsp);

		class PeriodicUpdate extends Thread {
			WArgumentationAgentVisualizer aav;
			int period = 50;

			public PeriodicUpdate(WArgumentationAgentVisualizer t, int p) {
				aav = t;
				period = p;
			}

			public void run() {
				while (true) {
					aav.repaint();
					if (aav.agv.selectedArgument == null) {
						aav.tsp.patternToHighlight = null;
					} else {
						aav.tsp.patternToHighlight = aav.agv.selectedArgument.m_a.m_rule.pattern;
					}
					try {
						Thread.sleep(period);
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
				}
			}
		}

		PeriodicUpdate updater = new PeriodicUpdate(this, 50);
		updater.start();
	}

}
