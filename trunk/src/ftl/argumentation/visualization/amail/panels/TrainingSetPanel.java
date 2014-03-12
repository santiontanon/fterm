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
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

import ftl.argumentation.visualization.amail.helpers.GiftWrap;
import ftl.argumentation.visualization.amail.helpers.GrahamScan;
import ftl.argumentation.visualization.amail.helpers.newPolygon;
import ftl.argumentation.visualization.amail.listeners.TrainingSetPMouseListener;
import ftl.base.core.FTKBase;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Path;
import ftl.base.utils.FeatureTermException;
import ftl.learning.lazymethods.similarity.Distance;

// TODO: Auto-generated Javadoc
/**
 * The Class TrainingSetPanel.
 * 
 * @author santi
 */
public class TrainingSetPanel extends JPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1035118161975403167L;

	/** The m_cases. */
	public List<FeatureTerm> m_cases = null;

	/** The solution path. */
	public Path solutionPath = null;

	/** The problem path. */
	public Path problemPath = null;

	/** The domain model. */
	public FTKBase domainModel = null;

	/** The highlighted case. */
	public int highlightedCase = -1;

	/** The locations. */
	public Point2D locations[] = null;

	/** The forces_x. */
	double forces_x[] = null;

	/** The forces_y. */
	double forces_y[] = null;

	/** The solution colors. */
	HashMap<String, Color> solutionColors = new HashMap<String, Color>();

	/** The available colors. */
	List<Color> availableColors = null;

	/** The distances. */
	double distances[] = null;

	/** The distances computed. */
	boolean distancesComputed = false;

	/** The distances computed percentage. */
	double distancesComputedPercentage = 0.0;

	/** The rand. */
	Random rand = new Random();

	/** The distance metric. */
	Distance distanceMetric = null;

	/** The ts_dy. */
	int ts_x, ts_y, ts_dx, ts_dy;

	/** The case_size. */
	public int case_size = 8;

	/** The cases_max_y. */
	double cases_min_x = 0, cases_min_y = 0, cases_max_x = 0, cases_max_y = 0;

	/** The pattern to highlight. */
	public FeatureTerm patternToHighlight = null;

	/**
	 * New window.
	 * 
	 * @param name
	 *            the name
	 * @param dx
	 *            the dx
	 * @param dy
	 *            the dy
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
	 * @param avoidPrimaryColors
	 *            the avoid primary colors
	 * @return the j frame
	 */
	public static JFrame newWindow(String name, int dx, int dy, List<FeatureTerm> cases, Path sp, Path dp, FTKBase dm, Distance d, boolean avoidPrimaryColors) {
		TrainingSetPanel tsp = new TrainingSetPanel(cases, sp, dp, dm, d, avoidPrimaryColors);
		tsp.setPreferredSize(new Dimension(dy, dy));

		JFrame frame = new JFrame(name);
		frame.getContentPane().add(tsp);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		class PeriodicUpdate extends Thread {
			JFrame tsp;
			int period = 50;

			public PeriodicUpdate(JFrame t, int p) {
				tsp = t;
				period = p;
			}

			public void run() {
				while (true) {
					tsp.repaint();
					try {
						Thread.sleep(period);
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
				}
			}
		}

		PeriodicUpdate updater = new PeriodicUpdate(frame, 50);
		updater.start();

		return frame;
	}

	/**
	 * Instantiates a new training set panel.
	 * 
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
	 * @param avoidPrimaryColors
	 *            the avoid primary colors
	 */
	public TrainingSetPanel(List<FeatureTerm> cases, Path sp, Path dp, FTKBase dm, Distance d, boolean avoidPrimaryColors) {
		m_cases = new ArrayList<FeatureTerm>();
		m_cases.addAll(cases);
		solutionPath = sp;
		problemPath = dp;
		domainModel = dm;
		distanceMetric = d;

		availableColors = new LinkedList<Color>();
		if (!avoidPrimaryColors) {
			availableColors.add(new Color(1.0f, 0.0f, 0.0f));
			availableColors.add(new Color(0.0f, 1.0f, 0.0f));
			availableColors.add(new Color(0.0f, 0.0f, 1.0f));
		}
		availableColors.add(new Color(1.0f, 1.0f, 0.0f));
		availableColors.add(new Color(0.0f, 1.0f, 1.0f));
		availableColors.add(new Color(1.0f, 0.0f, 1.0f));
		availableColors.add(new Color(0.0f, 0.0f, 0.0f));
		availableColors.add(new Color(1.0f, 1.0f, 1.0f));
		availableColors.add(new Color(1.0f, 0.5f, 0.5f));
		availableColors.add(new Color(0.5f, 1.0f, 0.5f));
		availableColors.add(new Color(0.5f, 0.5f, 1.0f));
		availableColors.add(new Color(1.0f, 1.0f, 0.5f));
		availableColors.add(new Color(0.5f, 1.0f, 1.0f));
		availableColors.add(new Color(1.0f, 0.5f, 1.0f));
		availableColors.add(new Color(0.5f, 0.5f, 0.5f));

		TrainingSetPMouseListener ml = new TrainingSetPMouseListener(this);
		addMouseMotionListener(ml);
		addMouseListener(ml);

		try {
			assignColors();
		} catch (FeatureTermException ex) {
			ex.printStackTrace();
		}

		if (locations == null) {
			locations = new Point2D[m_cases.size()];
			for (int i = 0; i < m_cases.size(); i++) {
				locations[i] = new Point2D.Double(rand.nextDouble(), rand.nextDouble());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	public void paint(Graphics g) {
		super.paint(g);

		List<Integer> toHighlight = new LinkedList<Integer>();
		newPolygon highlightHull = null;
		Point2D highlightHullCenter = null;
		double highLightHullDiameter = 0;

		if (patternToHighlight != null) {
			for (int i = 0; i < m_cases.size(); i++) {
				try {
					FeatureTerm c = m_cases.get(i);
					if (patternToHighlight.subsumes(c.readPath(problemPath))) {
						toHighlight.add(i);
					}
				} catch (FeatureTermException ex) {
					ex.printStackTrace();
				}
			}

			if (!toHighlight.isEmpty()) {
				List<Point2D> points = new LinkedList<Point2D>();
				for (Integer i : toHighlight) {
					int x = caseX(i);
					int y = caseY(i);

					points.add(new Point2D.Double(x - case_size, y - case_size));
					points.add(new Point2D.Double(x + case_size * 2, y - case_size));
					points.add(new Point2D.Double(x - case_size, y + case_size * 2));
					points.add(new Point2D.Double(x + case_size * 2, y + case_size * 2));
				}

				List<Point2D> highlightHullPoints = GrahamScan.convexHull(points);
				if (highlightHullPoints == null)
					highlightHullPoints = GiftWrap.convexHull(points);

				int xs[] = new int[highlightHullPoints.size()];
				int ys[] = new int[highlightHullPoints.size()];
				double cx = 0, cy = 0;
				for (int i = 0; i < highlightHullPoints.size(); i++) {
					xs[i] = (int) highlightHullPoints.get(i).getX();
					ys[i] = (int) highlightHullPoints.get(i).getY();
					cx += xs[i];
					cy += ys[i];
					highlightHull = new newPolygon(xs, ys, xs.length);
				}
				cx /= xs.length;
				cy /= ys.length;
				highlightHullCenter = new Point2D.Double(cx, cy);
				highLightHullDiameter = 0;
				for (int i = 0; i < highlightHullPoints.size(); i++) {
					xs[i] = (int) highlightHullPoints.get(i).getX();
					ys[i] = (int) highlightHullPoints.get(i).getY();
					highLightHullDiameter += Math.sqrt((xs[i] - cx) * (xs[i] - cx) + (ys[i] - cy) * (ys[i] - cy));
				}
				highLightHullDiameter /= xs.length;
			}
		}

		updatePositions();

		// Determine agent and training set location:
		int a_x, a_y, a_dx;
		int max_agent_size = 100;
		Rectangle r = getBounds();
		if (r.height > max_agent_size * 2 + 40) {
			a_dx = max_agent_size;
		} else {
			a_dx = (r.height - 40) / 2;
		}
		a_x = r.width / 2;
		a_y = r.height - (max_agent_size + 20);
		ts_x = 20;
		ts_y = 10;
		ts_dx = r.width - 40;

		// draw agent:
		ts_dy = r.height - 40;

		// draw training set
		g.drawRect(ts_x, ts_y, ts_dx, ts_dy);
		if (!distancesComputed) {
			g.drawString("Percentage computed: " + distancesComputedPercentage + "%", ts_x + 5, ts_y + 15);
		}
		drawCaseBase(g, ts_x + 10, ts_y + 10, ts_dx - 20, ts_dy - 20, highlightHull, toHighlight);
	}

	/**
	 * Assign colors.
	 * 
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public void assignColors() throws FeatureTermException {
		if (solutionColors.keySet().size() != 0)
			return;

		for (FeatureTerm c : m_cases) {
			String solution = c.readPath(solutionPath).toStringNOOS(domainModel);
			Color color = solutionColors.get(solution);
			if (color == null) {
				if (!availableColors.isEmpty()) {
					color = availableColors.remove(0);
					solutionColors.put(solution, color);
				} else {
					color = new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
					solutionColors.put(solution, color);
				}
			}
		}
	}

	/**
	 * Update positions.
	 */
	public void updatePositions() {
		if (!distancesComputed) {
			if (distances == null) {
				// compute similarities:
				distances = new double[m_cases.size() * m_cases.size()];
				class SimilarityComputer extends Thread {
					TrainingSetPanel ap;

					public SimilarityComputer(TrainingSetPanel p) {
						ap = p;
					}

					public void run() {
						try {
							int i, j, l;
							l = ap.m_cases.size();
							for (i = 0; i < l; i++) {
								for (j = 0; j < l; j++) {
									if (i < j) {
										FeatureTerm c1 = ap.m_cases.get(i);
										FeatureTerm c2 = ap.m_cases.get(j);

										double d = distanceMetric.distance(c1.readPath(problemPath), c2.readPath(problemPath), c1.getSort().getOntology(),
												domainModel);
										ap.distances[j + i * l] = d;
										ap.distances[i + j * l] = d;
									}
									distancesComputedPercentage = ((double) (i * l + j)) / (l * l) * 100.0;
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

						distancesComputed = true;
					}
				}

				SimilarityComputer computer = new SimilarityComputer(this);
				new Thread(computer).start();
			}
		} else {
			applyForces();
		}
	}

	/**
	 * Apply forces.
	 */
	public void applyForces() {
		int i, j, l;
		double totalForce = 0.0;
		l = m_cases.size();
		if (forces_x == null) {
			forces_x = new double[l];
			forces_y = new double[l];
		}
		for (i = 0; i < l; i++) {
			double f;
			double dx, dy, d;
			forces_x[i] = 0.0;
			forces_y[i] = 0.0;
			int l2;
			for (j = 0; j < l; j++) {
				if (i != j) {
					dx = locations[j].getX() - locations[i].getX();
					dy = locations[j].getY() - locations[i].getY();
					d = Math.sqrt(dx * dx + dy * dy);
					if (d > 0.000001) {
						f = d - distances[i + j * l];
						forces_x[i] += (dx / d) * f;
						forces_y[i] += (dy / d) * f;
					}
				}
			}
			l2 = l;
			forces_x[i] /= l2;
			forces_y[i] /= l2;

			totalForce += Math.abs(forces_x[i]) + Math.abs(forces_y[i]);
		}

		double speed = 0.5;
		for (i = 0; i < l; i++) {
			locations[i].setLocation(locations[i].getX() + forces_x[i] * speed, locations[i].getY() + forces_y[i] * speed);
		}
		// System.out.println("Update cycle for " + m_name + " -> " + totalForce);

	}

	/**
	 * Draw case base.
	 * 
	 * @param g
	 *            the g
	 * @param sx
	 *            the sx
	 * @param sy
	 *            the sy
	 * @param dx
	 *            the dx
	 * @param dy
	 *            the dy
	 * @param highlightHull
	 *            the highlight hull
	 * @param toHighlight
	 *            the to highlight
	 */
	public void drawCaseBase(Graphics g, int sx, int sy, int dx, int dy, newPolygon highlightHull, List<Integer> toHighlight) {
		boolean first = true;

		{
			double s = Math.sqrt(((dx * dy) / m_cases.size()) / 50);
			if (s < 8)
				s = 8;
			if (s > 16)
				s = 16;
			case_size = (int) s;
		}

		cases_min_x = cases_min_y = cases_max_x = cases_max_y = 0;
		for (int i = 0; i < m_cases.size(); i++) {
			Point2D l = locations[i];
			if (first) {
				cases_min_x = cases_max_x = l.getX();
				cases_min_y = cases_max_y = l.getY();
				first = false;
			} else {
				if (l.getX() < cases_min_x)
					cases_min_x = l.getX();
				if (l.getX() > cases_max_x)
					cases_max_x = l.getX();
				if (l.getY() < cases_min_y)
					cases_min_y = l.getY();
				if (l.getY() > cases_max_y)
					cases_max_y = l.getY();
			}
		}

		// System.out.println("[" + min_x + "," + max_x + "] - [" + min_y + "," + max_y + "]");

		// draw the cases:
		int i = 0;
		for (FeatureTerm c : m_cases) {
			String solution;
			int x = caseX(i);
			int y = caseY(i);
			if (toHighlight.contains(i)) {
				g.setColor(Color.black);
				g.drawArc(x - 8, y - 8, case_size + 16, case_size + 16, 0, 360);
				g.setColor(Color.lightGray);
				g.fillArc(x - 8, y - 8, case_size + 16, case_size + 16, 0, 360);
			}

			i++;
		}

		// draw the cases:
		i = 0;
		for (FeatureTerm c : m_cases) {
			String solution;
			int x = caseX(i);
			int y = caseY(i);
			try {
				solution = c.readPath(solutionPath).toStringNOOS(domainModel);
				Color color = solutionColors.get(solution);
				g.setColor(color);
				g.fillArc(x, y, case_size, case_size, 0, 360);
			} catch (FeatureTermException ex) {
				ex.printStackTrace();
			}
			g.setColor(Color.black);
			g.drawArc(x, y, case_size, case_size, 0, 360);
			i++;
		}

		if (highlightedCase != -1) {
			FeatureTerm c = m_cases.get(highlightedCase);
			int x = caseX(highlightedCase);
			int y = caseY(highlightedCase);

			String solution;
			try {
				solution = c.readPath(solutionPath).toStringNOOS(domainModel);
				Color color = solutionColors.get(solution);
				if (color == null)
					color = Color.black;
				g.setColor(color);
				g.drawArc(x - 4, y - 4, case_size + 8, case_size + 8, 0, 360);

				// draw text box:
				{
					String s1 = c.getName().get();
					String s2 = c.readPath(problemPath).getName().get();
					String s3 = c.readPath(solutionPath).toStringNOOS(domainModel);

					Rectangle2D r1 = g.getFontMetrics().getStringBounds(s1, g);
					Rectangle2D r2 = g.getFontMetrics().getStringBounds(s2, g);
					Rectangle2D r3 = g.getFontMetrics().getStringBounds(s3, g);
					int padding = 4;
					int tdx = (int) Math.max(r1.getWidth(), Math.max(r2.getWidth(), r3.getWidth())) + padding * 2;
					int tdy = (int) (r1.getHeight() + r2.getHeight() + r3.getHeight()) + padding * 4;

					g.setColor(Color.lightGray);
					g.fillRect(x + case_size / 2 - tdx / 2, y + case_size + 2, tdx, tdy);
					g.setColor(Color.black);
					g.drawRect(x + case_size / 2 - tdx / 2, y + case_size + 2, tdx, tdy);
					g.drawString(s1, x + case_size / 2 - tdx / 2 + padding, (int) (y + case_size + 2 + r1.getHeight()) + padding);
					g.drawString(s2, x + case_size / 2 - tdx / 2 + padding, (int) (y + case_size + 2 + r1.getHeight() + r2.getHeight()) + padding * 2);
					g.drawString(s3, x + case_size / 2 - tdx / 2 + padding,
							(int) (y + case_size + 2 + r1.getHeight() + r2.getHeight() + r3.getHeight() + padding * 3));
				}
			} catch (FeatureTermException ex) {
				ex.printStackTrace();
			}
		}

	}

	/**
	 * Case x.
	 * 
	 * @param i
	 *            the i
	 * @return the int
	 */
	public int caseX(int i) {
		Point2D l = locations[i];
		double x = (l.getX() - cases_min_x) / Math.max(cases_max_x - cases_min_x, cases_max_y - cases_min_y);
		double divisor = Math.min(ts_dx, ts_dy) - case_size * 3;
		return (int) (ts_x + ((ts_dx - case_size * 3) - divisor) / 2 + case_size + x * divisor);
	}

	/**
	 * Case y.
	 * 
	 * @param i
	 *            the i
	 * @return the int
	 */
	public int caseY(int i) {
		Point2D l = locations[i];
		double y = (l.getY() - cases_min_y) / Math.max(cases_max_x - cases_min_x, cases_max_y - cases_min_y);
		double divisor = Math.min(ts_dx, ts_dy) - case_size * 3;
		return (int) (ts_y + ((ts_dy - case_size * 3) - divisor) / 2 + case_size + y * divisor);
	}
}
