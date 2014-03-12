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
  
 package ftl.argumentation.visualization.amail.listeners;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JFrame;

import ftl.argumentation.visualization.amail.panels.AgentPanel;
import ftl.base.utils.FeatureTermException;
import ftl.base.visualization.FTVisualizer;

// TODO: Auto-generated Javadoc
/**
 * The listener interface for receiving agentPMouse events. The class that is interested in processing a agentPMouse
 * event implements this interface, and the object created with that class is registered with a component using the
 * component's <code>addAgentPMouseListener<code> method. When
 * the agentPMouse event occurs, that object's appropriate
 * method is invoked.
 * 
 * @see AgentPMouseEvent
 */
public class AgentPMouseListener implements MouseMotionListener, MouseListener {

	/** The ap. */
	AgentPanel ap;

	/**
	 * Instantiates a new agent p mouse listener.
	 * 
	 * @param p
	 *            the p
	 */
	public AgentPMouseListener(AgentPanel p) {
		ap = p;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	public void mouseDragged(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	public void mouseMoved(MouseEvent e) {
		int l = ap.m_cases.size();
		int x = e.getX();
		int y = e.getY();

		ap.highlightedCase = -1;
		for (int i = 0; i < l; i++) {
			if (x >= ap.caseX(i) && x < ap.caseX(i) + ap.case_size && y >= ap.caseY(i) && y < ap.caseY(i) + ap.case_size) {
				// button highlighted
				ap.highlightedCase = i;
				break;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
		int l = ap.m_cases.size();
		int x = e.getX();
		int y = e.getY();

		ap.highlightedCase = -1;
		for (int i = 0; i < l; i++) {
			if (x >= ap.caseX(i) && x < ap.caseX(i) + ap.case_size && y >= ap.caseY(i) && y < ap.caseY(i) + ap.case_size) {
				try {
					// clicked on a case:
					JFrame frame;
					frame = FTVisualizer.newWindow("Case Visualizer", 640, 480, ap.m_cases.get(i), ap.domainModel, true, true);
					frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					frame.setVisible(true);
					// ScrollableFrame f = new ScrollableFrame("Case Visualizer",640,480,new
					// FeatureTermTreePanel(ap.m_cases.get(i),ap.domainModel));
					// f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					break;
				} catch (FeatureTermException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
	}

}