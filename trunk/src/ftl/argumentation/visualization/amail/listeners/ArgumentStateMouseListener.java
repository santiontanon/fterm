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
import java.awt.geom.Rectangle2D;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;

import ftl.argumentation.core.Argument;
import ftl.argumentation.visualization.amail.panels.AgentPanel;
import ftl.argumentation.visualization.amail.panels.ArgumentationStatePanel;
import ftl.argumentation.visualization.amail.panels.RulePanel;
import ftl.base.utils.FeatureTermException;
import ftl.base.visualization.FeatureTermTreePanel;
import ftl.base.visualization.ScrollableFrame;

// TODO: Auto-generated Javadoc
/**
 * The listener interface for receiving argumentStateMouse events. The class that is interested in processing a
 * argumentStateMouse event implements this interface, and the object created with that class is registered with a
 * component using the component's <code>addArgumentStateMouseListener<code> method. When
 * the argumentStateMouse event occurs, that object's appropriate
 * method is invoked.
 * 
 * @see ArgumentStateMouseEvent
 */
public class ArgumentStateMouseListener implements MouseMotionListener, MouseListener {

	/** The ap. */
	ArgumentationStatePanel ap;

	/**
	 * Instantiates a new argument state mouse listener.
	 * 
	 * @param p
	 *            the p
	 */
	public ArgumentStateMouseListener(ArgumentationStatePanel p) {
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
		int x = e.getX();
		int y = e.getY();

		for (AgentPanel apl : ap.parent.apl) {
			apl.patternToHighlight = null;
		}

		ap.highlightedArgument = null;
		for (Argument a : ap.argumentPositions.keySet()) {
			Rectangle2D coor = ap.argumentCoordinates(a);

			if (x >= coor.getX() && x < coor.getX() + coor.getWidth() && y >= coor.getY() && y < coor.getY() + coor.getHeight()) {
				ap.highlightedArgument = a;
				if (a.m_type == Argument.ARGUMENT_RULE) {
					for (AgentPanel apl : ap.parent.apl) {
						apl.patternToHighlight = a.m_rule.pattern;
					}
				} else {
					try {
						for (AgentPanel apl : ap.parent.apl) {
							apl.patternToHighlight = a.m_example.readPath(ap.descriptionPath);
						}
					} catch (FeatureTermException ex) {
						Logger.getLogger(ArgumentStateMouseListener.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
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
		int x = e.getX();
		int y = e.getY();

		ap.highlightedArgument = null;
		for (Argument a : ap.argumentPositions.keySet()) {
			Rectangle2D coor = ap.argumentCoordinates(a);

			if (x >= coor.getX() && x < coor.getX() + coor.getWidth() && y >= coor.getY() && y < coor.getY() + coor.getHeight()) {
				// clicked on an argument:
				if (a.m_type == Argument.ARGUMENT_RULE) {
					ScrollableFrame f = new ScrollableFrame("Rule Argument", 1024, 480, new RulePanel(a.m_rule.pattern, a.m_rule.solution, ap.m_domainModel));
					f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				} else if (a.m_type == Argument.ARGUMENT_EXAMPLE) {
					ScrollableFrame f = new ScrollableFrame("Example Argument", 640, 480, new FeatureTermTreePanel(a.m_example, ap.m_domainModel));
					f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				}
				break;
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