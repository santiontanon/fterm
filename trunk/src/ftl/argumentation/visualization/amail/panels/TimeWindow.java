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
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

// TODO: Auto-generated Javadoc
/**
 * The Class TimeWindow.
 * 
 * @author santi
 */
public class TimeWindow extends JFrame {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 5702243342785523192L;

	/** The tabs. */
	JTabbedPane tabs = null;

	/** The WINDO w_ width. */
	public int WINDOW_WIDTH = 1024;

	/** The WINDO w_ height. */
	public int WINDOW_HEIGHT = 768;

	/**
	 * Instantiates a new time window.
	 * 
	 * @param name
	 *            the name
	 * @param periodicUpdate
	 *            the periodic update
	 */
	public TimeWindow(String name, int periodicUpdate) {
		super(name);
		setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
		setSize(WINDOW_WIDTH, WINDOW_HEIGHT);

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.out.println("Error setting native LAF: " + e);
		}

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);

		setBackground(Color.WHITE);

		getContentPane().removeAll();
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		tabs = new JTabbedPane();
		panel.add(tabs);
		getContentPane().add(panel);
		pack();

		class PeriodicUpdate extends Thread {
			TimeWindow tw;
			int period = 50;

			public PeriodicUpdate(TimeWindow t, int p) {
				tw = t;
				period = p;
			}

			public void run() {
				while (true) {
					tw.repaint();
					try {
						Thread.sleep(period);
					} catch (InterruptedException ex) {
						Logger.getLogger(TimeWindow.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			}
		}

		PeriodicUpdate updater = new PeriodicUpdate(this, periodicUpdate);
		updater.start();

	}

	/*
	 * public void setCurrent(JPanel p) { getContentPane().removeAll(); JPanel panel = new JPanel(); panel.setLayout(new
	 * BoxLayout(panel,BoxLayout.Y_AXIS));
	 * 
	 * if (tabs==null) { tabs = new JTabbedPane(); panel.add(tabs); } getContentPane().add(panel); pack(); }
	 */
	/**
	 * Adds the step.
	 * 
	 * @param name
	 *            the name
	 * @param p
	 *            the p
	 */
	public void addStep(String name, JPanel p) {
		tabs.add(name, p);
	}

}
