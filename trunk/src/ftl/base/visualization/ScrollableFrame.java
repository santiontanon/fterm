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
  
 package ftl.base.visualization;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import ftl.base.core.FTKBase;
import ftl.base.core.FeatureTerm;

// TODO: Auto-generated Javadoc
/**
 * The Class ScrollableFrame.
 * 
 * @author santi
 */
public class ScrollableFrame extends JFrame {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The WINDO w_ width. */
	public int WINDOW_WIDTH = 800;

	/** The WINDO w_ height. */
	public int WINDOW_HEIGHT = 600;

	/**
	 * Instantiates a new scrollable frame.
	 * 
	 * @param name
	 *            the name
	 * @param w
	 *            the w
	 * @param h
	 *            the h
	 * @param p
	 *            the p
	 */
	public ScrollableFrame(String name, int w, int h, JPanel p) {
		super(name);

		WINDOW_WIDTH = w;
		WINDOW_HEIGHT = h;

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
		panel.setLayout(new BorderLayout());
		JScrollPane termPane = new JScrollPane(p);
		panel.add(termPane);
		getContentPane().add(panel);
		pack();
	}

	/**
	 * Instantiates a new scrollable frame.
	 * 
	 * @param name
	 *            the name
	 * @param pan
	 *            the p
	 */
	public ScrollableFrame(String name, CBVisualizer pan) {
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
		panel.setLayout(new BorderLayout());
		JScrollPane termPane = new JScrollPane(pan);
		panel.add(termPane);
		getContentPane().add(panel);
		pack();
	}

}
