/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fterms.visualization;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

/**
 *
 * @author santi
 */
public class ScrollableFrame extends JFrame {
    public int WINDOW_WIDTH = 800;
    public int WINDOW_HEIGHT = 600;

    public ScrollableFrame(String name, int w,int h,JPanel p) {
		super(name);

        WINDOW_WIDTH = w;
        WINDOW_HEIGHT = h;

        setPreferredSize(new Dimension(WINDOW_WIDTH,WINDOW_HEIGHT));
		setSize(WINDOW_WIDTH,WINDOW_HEIGHT);

        try {
          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception e) {
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

    public ScrollableFrame(String name, JPanel p) {
		super(name);
		setPreferredSize(new Dimension(WINDOW_WIDTH,WINDOW_HEIGHT));
		setSize(WINDOW_WIDTH,WINDOW_HEIGHT);

        try {
          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception e) {
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

}
