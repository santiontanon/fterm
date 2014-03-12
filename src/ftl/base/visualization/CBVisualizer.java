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
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.controls.ControlAdapter;
import prefuse.controls.DragControl;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.data.Graph;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.render.PolygonRenderer;
import prefuse.util.ColorLib;
import prefuse.util.GraphicsLib;
import prefuse.util.display.DisplayLib;
import prefuse.util.force.DragForce;
import prefuse.util.force.ForceSimulator;
import prefuse.util.force.NBodyForce;
import prefuse.util.force.SpringForce;
import prefuse.visual.AggregateItem;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;
import ftl.base.core.Disintegration;
import ftl.base.core.FTKBase;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Ontology;
import ftl.base.core.Path;
import ftl.base.utils.FeatureTermException;
import ftl.demo.FTLGUI;
import ftl.learning.core.TrainingSetProperties;
import ftl.learning.core.TrainingSetUtils;
import ftl.learning.lazymethods.similarity.RIBL;

// TODO: Auto-generated Javadoc
/**
 * The Class CBVisualizer.
 * 
 * @author santi
 */
public class CBVisualizer extends Display {

	/** The cases_max_y. */
	double cases_min_x = 0, cases_min_y = 0, cases_max_x = 0, cases_max_y = 0;

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 2317826226570557775L;

	/** The Constant GRAPH. */
	public static final String GRAPH = "graph";

	/** The Constant NODES. */
	public static final String NODES = "graph.nodes";

	/** The Constant EDGES. */
	public static final String EDGES = "graph.edges";

	/** The dm. */
	static FTKBase domain = new FTKBase();

	/** The o. */
	static Ontology o = new Ontology();

	/** The case_base. */
	static FTKBase case_base = new FTKBase();

	/** Max Deph distance. */
	static int m_visibleCount = 16;

	/** The ts. */
	private static TrainingSetProperties ts = new TrainingSetProperties();

	/** The solution colors. */
	HashMap<String, Color> solutionColors = new HashMap<String, Color>();

	/** The available colors. */
	List<Color> availableColors = null;

	/** The rand. */
	Random rand = new Random();

	/** The distances. */
	double distances[] = null;

	/** The distances computed percentage. */
	double distancesComputedPercentage = 0.0;

	/** The forces_x. */
	double forces_x[] = null;

	/** The forces_y. */
	double forces_y[] = null;

	/** The locations. */
	public Point2D locations[] = null;

	/** The ts_dy. */
	int ts_x, ts_y, ts_dx, ts_dy;

	/** The case_size. */
	public int case_size = 8;

	/** The gui. */
	FTLGUI gui;

	/** The progress bar. */
	private JProgressBar progressBar;

	/** The blinker. */
	private volatile Thread blinker;

	/** The tsolution. */
	private Path tsolution = null;

	/** The distances computed. */
	private boolean distancesComputed = false;

	/** The frame. */
	private JFrame frame;

	/** The vg. */
	private VisualGraph vg;

	/** The load file. */
	private boolean loadFile;

	/** The dy. */
	private int dx, dy;

	/**
	 * New window.
	 * 
	 * @param name
	 *            the name
	 * @param dx
	 *            the dx
	 * @param dy
	 *            the dy
	 * @param terms
	 *            the terms
	 * @param dm
	 *            the dm
	 * @param sp
	 *            the sp
	 * @param dp
	 *            the dp
	 * @param separateConstants
	 *            the separate constants
	 * @return the j frame
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static JFrame newWindow(String name, int dx, int dy, List<FeatureTerm> terms, FTKBase dm, Path sp, Path dp,
			boolean separateConstants) throws FeatureTermException, IOException {
		CBVisualizer ad = new CBVisualizer(dx, dy, terms, dm, sp, dp, separateConstants, null, null, false);
		JFrame frame = new JFrame(name);
		frame.getContentPane().add(ad);
		frame.pack();
		return frame;
	}

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void main(String[] args) throws FeatureTermException, IOException {
		int CB = TrainingSetUtils.TRAINS_82_DATASET;
		ts = TrainingSetUtils.loadTrainingSet(CB, o, domain, case_base);
		List<FeatureTerm> terms = ts.cases;
		CBVisualizer pan = new CBVisualizer(800, 800, terms, domain, ts.solution_path, ts.description_path, true, null, ts, true);

		JFrame frame = new JFrame("TEST");
		frame.setSize(400, 400);
		// ScrollableFrame frame = new ScrollableFrame("test", pan);
		// frame.getContentPane().add(pan);

		frame.add(pan);
		frame.pack();

		// 2. Optional: What happens when the frame closes?
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// 5. Show it.
		frame.setVisible(true);

		double multi = 1.2;
		frame.setSize((int) (pan.getPreferredSize().getWidth() * multi), (int) (pan.getPreferredSize().getHeight() * multi));
	}

	// /* (non-Javadoc)
	// * @see javax.swing.JComponent#getPreferredSize()
	// */
	// @Override
	// public Dimension getPreferredSize() {
	// this.s
	// // TODO Auto-generated method stub
	// return new Dimension(400,400);
	// //return super.getPreferredSize();
	// }

	/**
	 * Instantiates a new cB visualizer.
	 *
	 * @param dx1 the dx1
	 * @param dy1 the dy1
	 * @param terms the terms
	 * @param dm the dm
	 * @param sp the sp
	 * @param dp the dp
	 * @param separateConstants the separate constants
	 * @param ftlgui the ftlgui
	 * @param ts2 the ts2
	 * @param loadFromFile the load from file
	 * @throws FeatureTermException the feature term exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public CBVisualizer(int dx1, int dy1, List<FeatureTerm> terms, FTKBase dm, Path sp, Path dp, boolean separateConstants, FTLGUI ftlgui,
			TrainingSetProperties ts2, boolean loadFromFile) throws FeatureTermException, IOException {
		// initialize display and data

		super(new Visualization());
		createProgressBar();
		tsolution = sp;
		domain = dm;
		ts = ts2;
		gui = ftlgui;
		distancesComputed = false;
		distancesComputedPercentage = 0;
		loadFile = loadFromFile;
		dx = dx1;
		dy = dy1;

		if (gui != null)
			gui.setEnabled(false);

		this.setAutoscrolls(true);
		initDataGroups(terms, dm, separateConstants);
		renderCB(dx, dy, terms, dm, sp, dp, ftlgui);

	}

	/**
	 * Render cb.
	 * 
	 * @param dx
	 *            the dx
	 * @param dy
	 *            the dy
	 * @param terms
	 *            the terms
	 * @param dm
	 *            the dm
	 * @param sp
	 *            the sp
	 * @param dp
	 *            the dp
	 * @param ftlgui
	 *            the ftlgui
	 */
	private void renderCB(int dx, int dy, List<FeatureTerm> terms, FTKBase dm, Path sp, Path dp, FTLGUI ftlgui) {
		// set up the renderers
		// draw the nodes as basic shapes
		// Renderer nodeR = new ShapeRenderer(20);
		LabelRenderer nodeR = new LabelRenderer("name");
		nodeR.setHorizontalPadding(4);
		nodeR.setVerticalPadding(2);
		nodeR.setRoundedCorner(8, 8); // round the corners
		EdgeRenderer edgeR = new LabelEdgeRenderer(Constants.EDGE_TYPE_LINE, Constants.EDGE_ARROW_FORWARD);
		edgeR.setArrowHeadSize(6, 6);

		// draw aggregates as polygons with curved edges
		PolygonRenderer polyR = new PolygonRenderer(Constants.POLY_TYPE_CURVE);
		polyR.setCurveSlack(0.15f);

		DefaultRendererFactory drf = new DefaultRendererFactory(nodeR, edgeR);
		drf.add("ingroup('aggregates')", polyR);
		m_vis.setRendererFactory(drf);

		// set up the visual operators
		// first set up all the color actions
		ColorAction nFill = new ColorAction(NODES, VisualItem.FILLCOLOR);
		nFill.setDefaultColor(ColorLib.gray(255));
		nFill.add("_hover", ColorLib.gray(200));

		ColorAction nEdges = new ColorAction(EDGES, VisualItem.STROKECOLOR);
		nEdges.setDefaultColor(ColorLib.gray(100));

		// ColorAction aStroke = new ColorAction(AGGR, VisualItem.STROKECOLOR);
		// aStroke.setDefaultColor(ColorLib.gray(200));
		// aStroke.add("_hover", ColorLib.rgb(255,100,100));

		// bundle the color actions
		ActionList colors = new ActionList();
		colors.add(nFill);
		colors.add(nEdges);
		// colors.add(aStroke);
		// colors.add(aFill);

		// now create the main layout routine
		ActionList layout = new ActionList(Activity.INFINITY);

		ForceDirectedLayout fdl = new ForceDirectedLayout(GRAPH, false, false);
		ForceSimulator m_fsim = new ForceSimulator();
		float gravConstant = -10f; // the more negative, the more repelling
		float minDistance = 50; // -1 for always on, the more positive, the more space between nodes
		float theta = 0; // the lower, the more single-node repell calculation
		m_fsim.addForce(new NBodyForce(gravConstant, minDistance, theta));
		m_fsim.addForce(new SpringForce(1E-4f, 200));
		m_fsim.addForce(new DragForce());
		fdl.setForceSimulator(m_fsim);

		layout.add(colors);
		layout.add(fdl);
		// layout.add(new AggregateLayout(AGGR));
		layout.add(new RepaintAction());
		m_vis.putAction("layout", layout);

		// set up the display
		setPreferredSize(new Dimension(dx, dy));
		// pan(250, 250);

		setHighQuality(true);
		// addControlListener(new AggregateDragControl());
		addControlListener(new DragControl(true));
		addControlListener(new ZoomControl());
		addControlListener(new PanControl(false));// arrastrar para mover el conjunto
		addControlListener(new TermSelectControl(terms, dm, sp, dp, ftlgui));
		// addControlListener(new WheelZoomControl());

		ZoomToFitControl zoomListener = new ZoomToFitControl();
		zoomListener.setZoomOverItem(false);
		addControlListener(zoomListener);
		addControlListener(new WheelScrollControl(this.getHeight()));

		// ActionList draw = new ActionList();
		// draw.add(new GraphDistanceFilter(GRAPH, 50));
		// m_vis.putAction("draw", draw);

		if (distancesComputed)
			setCursor(null);

		// set things running
		m_vis.run("layout");
	}

	/**
	 * Sets the colors.
	 * 
	 * @param avoidPrimaryColors
	 *            the new colors
	 */
	private void setColors(Boolean avoidPrimaryColors) {
		availableColors = new LinkedList<Color>();
		if (!avoidPrimaryColors) {
			availableColors.add(new Color(1.0f, 0.0f, 0.0f));
			availableColors.add(new Color(0.0f, 1.0f, 0.0f));
			availableColors.add(new Color(0.0f, 0.0f, 1.0f));
		}
		// availableColors.add(new Color(1.0f, 1.0f, 0.0f));
		availableColors.add(new Color(0.0f, 1.0f, 1.0f));
		availableColors.add(new Color(1.0f, 0.0f, 1.0f));
		availableColors.add(new Color(0.0f, 0.0f, 0.0f));
		// availableColors.add(new Color(1.0f, 1.0f, 1.0f));
		availableColors.add(new Color(1.0f, 0.5f, 0.5f));
		availableColors.add(new Color(0.5f, 1.0f, 0.5f));
		availableColors.add(new Color(0.5f, 0.5f, 1.0f));
		availableColors.add(new Color(1.0f, 1.0f, 0.5f));
		availableColors.add(new Color(0.5f, 1.0f, 1.0f));
		availableColors.add(new Color(1.0f, 0.5f, 1.0f));
		availableColors.add(new Color(0.5f, 0.5f, 0.5f));

	}

	/**
	 * Assign colors.
	 * 
	 * @param m_cases
	 *            the m_cases
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public void assignColors(List<FeatureTerm> m_cases) throws FeatureTermException {
		if (solutionColors.keySet().size() != 0)
			return;
		String solution = "";
		for (FeatureTerm c : m_cases) {
			if (c.readPath(tsolution) != null && domain != null) {
				solution = c.readPath(tsolution).toStringNOOS(domain);
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

	}

	/**
	 * Update.
	 * 
	 * @param value
	 *            the value
	 */
	public void update(int value) {
		progressBar.setValue(value);
	}

	/**
	 * Distances.
	 * 
	 * @param terms
	 *            the terms
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	private void distances(final List<FeatureTerm> terms) throws FeatureTermException, IOException, InterruptedException {
		// List<FeatureTerm> m_cases = terms;
		final String m_name = ts.name + "-" + terms.size();
		final RIBL rbDistance = new RIBL(terms, m_visibleCount);

		if (!distancesComputed) {
			if (distances == null) {
				// check if the similarity was precomputed before:
				System.gc();
				distances = new double[terms.size() * terms.size()];

				class SimilarityComputer extends Thread {
					CBVisualizer ap;

					public SimilarityComputer(CBVisualizer p) {
						ap = p;
						if (gui != null)
							gui.setEnabled(false);
					}

					public void run() {
						Thread thisThread = Thread.currentThread();

						progressBar.setValue(0);
						setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						if (gui != null)
							gui.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						if (blinker == thisThread) {
							double d = 0;
							int i, j, l;
							l = terms.size();
							BufferedWriter bw = null;

							// String fileName = ("cache/Precomputed/Sim-" + m_name +
							// rbDistance.getClass().getSimpleName()).replaceAll(" ", "_");
							String fileName = ("cache/Precomputed/Sim-" + m_name + rbDistance.getClass().getSimpleName() + ts
									.printStatistics(domain)).replaceAll(" ", "_");
							System.out.println(fileName);
							boolean exists = (new File(fileName)).exists();

							if (exists && loadFile) { // Carga de fichero precalculado
								File f = new File(fileName);
								try {
									BufferedReader br = new BufferedReader(new FileReader(f));

									HashMap<String, Integer> namesMap = new HashMap<String, Integer>();
									String line = null;
									int index = 0;

									for (FeatureTerm c : terms) {
										namesMap.put(c.getName().get(), index);
										index++;
									}

									distances = new double[terms.size() * terms.size()];

									line = br.readLine();
									int actualLine = 0;
									System.out.println("Load from file");
									while (line != null) {

										StringTokenizer st = new StringTokenizer(line);
										String c1n = st.nextToken();
										String c2n = st.nextToken();
										double d1 = Double.parseDouble(st.nextToken());
										int j1 = 0, k = 0, l1 = terms.size();

										j1 = namesMap.get(c1n);
										k = namesMap.get(c2n);
										distances[j1 + k * l1] = d1;
										distances[k + j1 * l1] = d1;

										distancesComputedPercentage = (double) ((double) ++actualLine / (double) (l1 * l1))
												* (double) 200.0;

										javax.swing.SwingUtilities.invokeLater(new Runnable() {
											public void run() {
												progressBar.setValue((int) distancesComputedPercentage);
											}
										});

										// System.out.println(distancesComputedPercentage);

										try {
											line = br.readLine();
										} catch (IOException e) {
											FTLGUI.writeConsole(e.toString());
											e.printStackTrace();
										}
									}
									progressBar.setValue(100);
									// frame.dispose();

									if (gui != null)
										gui.setEnabled(true);
								} catch (IOException e) {
									FTLGUI.writeConsole(e.toString());
									e.printStackTrace();
								}
							} else { // Cálculo de datos

								try {
									if (loadFile)
										bw = new BufferedWriter(new FileWriter(fileName));
									// bw = null;
									System.out.println("Calculating...");
									for (i = 0; i < l; i++) {
										for (j = 0; j < l; j++) {
											if (blinker == thisThread) {
												if (i < j) {
													FeatureTerm c1 = terms.get(i);
													FeatureTerm c2 = terms.get(j);

													d = rbDistance.distance(c1, c2, o, domain);
													distances[j + i * l] = d;
													distances[i + j * l] = d;

													if (loadFile)
														bw.write(c1.getName() + " " + c2.getName() + " " + distances[j + i * l] + "\n");
													distancesComputedPercentage = ((double) (i * l + j)) / (l * l) * 100.0;

													javax.swing.SwingUtilities.invokeLater(new Runnable() {
														public void run() {
															progressBar.setValue((int) distancesComputedPercentage);
														}
													});
												}

											} else {
												// Thread stopped
												progressBar.setValue(0);
												frame.dispose();
												// delete file
												if (loadFile)
													bw.close();
												File fileToDelete = new File(fileName);
												fileToDelete.delete();
												distancesComputed = false;
												distances = null;
												if (gui != null)
													gui.setCursor(null);
												setCursor(null);
												// Thread.currentThread().interrupt();
												return;
											}
											// System.out.println(distancesComputedPercentage);
										}

									}

									if (loadFile)
										bw.close();
								} catch (IOException e) {
									FTLGUI.writeConsole(e.toString());
									e.printStackTrace();
								} catch (FeatureTermException e) {
									FTLGUI.writeConsole(e.toString());
									e.printStackTrace();
								}
							}// Finished loading or computing files

							try {
								updateItemsData(terms); // Items positioning and labeling
							} catch (FeatureTermException e) {
								FTLGUI.writeConsole(e.toString());
								e.printStackTrace();
							}

							javax.swing.SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									if (gui != null) {
										gui.getPan().revalidate();
										gui.getPan().repaint();
										gui.setCursor(null);
										gui.setEnabled(true);
										gui.invalidate();
										gui.validate();
										gui.repaint();
										// gui.pack();

										// CBVisualizer.this.setSize(CBVisualizer.this.dx,CBVisualizer.this.dy);

										// Component comp = CBVisualizer.this.getComponent(0);
										// CBVisualizer.this.removeAll();
										// JScrollPane termPane = new JScrollPane(comp);
										// CBVisualizer.this.add(termPane);
										// termPane.revalidate();
										// termPane.repaint();

										Container parent = CBVisualizer.this.getParent();
										parent.removeAll();
										JScrollPane termPane = new JScrollPane(CBVisualizer.this);
										parent.add(termPane);
										termPane.revalidate();
										termPane.repaint();

										zoomToFit(CBVisualizer.this);

									}
								}
							});

							setCursor(null);
							frame.dispose();

						}

					}
				}// End subclass SimilarityComputer

				SimilarityComputer computer = new SimilarityComputer(this);
				blinker = new Thread(computer);
				blinker.start();

				// new Thread(computer).start();
			}

		}
	}

	/**
	 * Creates the progress bar.
	 */
	private void createProgressBar() {
		// ftlgui.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		// Create and set up the window.
		frame = new JFrame("Loading Case Base");
		frame.setResizable(false);
		frame.setSize(500, 200);
		frame.setLocationRelativeTo(gui);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// Create the demo's UI.
		JButton startButton = new JButton("Stop");
		startButton.setActionCommand("start");

		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);

		JPanel panel = new JPanel();
		panel.add(progressBar);
		panel.add(startButton);

		frame.getContentPane().add(panel, BorderLayout.PAGE_START);
		// add(new JScrollPane(taskOutput), BorderLayout.CENTER);
		setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		panel.setOpaque(true); // content panes must be opaque
		frame.setContentPane(panel);

		// Focus
		frame.setAlwaysOnTop(true);
		frame.setLocationByPlatform(true);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
		progressBar.repaint();
		frame.repaint();

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				frame.dispose();
				blinker = null;
				gui.setEnabled(true);
				setCursor(null);
				gui.setCursor(null);
				System.out.println("STOPPED");
				FTLGUI.writeConsole("STOP: Loading Case Base");
			}
		});

		startButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
				blinker = null;
				gui.setEnabled(true);
				setCursor(null);
				gui.setCursor(null);
				System.out.println("STOPPED");
				FTLGUI.writeConsole("STOP: Loading Case Base");
			}
		});
	}

	/**
	 * Apply forces.
	 * 
	 * @param orderedTerms
	 *            the ordered terms
	 */
	private void applyForces(List<FeatureTerm> orderedTerms) {

		int i, j, l;
		// double totalForce = 0.0;
		l = orderedTerms.size();
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

			// totalForce += Math.abs(forces_x[i]) + Math.abs(forces_y[i]);
		}

		double speed = 0.5;
		for (i = 0; i < l; i++) {
			locations[i].setLocation(locations[i].getX() + forces_x[i] * speed, locations[i].getY() + forces_y[i] * speed);
		}
		// System.out.println("Update cycle for " + m_name + " -> " + totalForce);
	}

	/**
	 * Inits the data groups.
	 * 
	 * @param orderedTerms
	 *            the ordered terms
	 * @param dm
	 *            the dm
	 * @param separateConstants
	 *            the separate constants
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void initDataGroups(final List<FeatureTerm> orderedTerms, final FTKBase dm, boolean separateConstants)
			throws FeatureTermException, IOException {
		Graph g = new Graph(true);

		g.addColumn("id", Integer.class);
		g.addColumn("name", String.class);

		// Create nodes:
		for (@SuppressWarnings("unused")
		FeatureTerm t : orderedTerms) {
			g.addNode();
		}

		setColors(true);
		assignColors(orderedTerms);

		if (locations == null) {
			locations = new Point2D[orderedTerms.size()];
			for (int i = 0; i < orderedTerms.size(); i++) {
				locations[i] = new Point2D.Double(rand.nextDouble(), rand.nextDouble());
			}
		}
		vg = m_vis.addGraph(GRAPH, g);
		try {
			distances(orderedTerms);
		} catch (InterruptedException e) {
			FTLGUI.writeConsole(e.toString());
			e.printStackTrace();
		}
		applyForces(orderedTerms);
		drawCaseBase(orderedTerms);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#getPreferredSize()
	 */
	@Override
	public Dimension getPreferredSize() {
		try {
			String m_group = Visualization.ALL_ITEMS;
			Visualization vis = this.getVisualization();
			Rectangle2D bounds = vis.getBounds(m_group);
			this.revalidate();
			this.setVisible(true);
			// zoomToFit((Display) this);
			return new Dimension((int) bounds.getWidth(), (int) bounds.getHeight());
		} catch (Exception e) {
			FTLGUI.writeConsole(e.toString());
			e.printStackTrace();
			return new Dimension(1000, 800);
		}
	}

	/**
	 * Zoom to fit.
	 * 
	 * @param display
	 *            the display
	 */
	private void zoomToFit(Display display) {
		if (!display.isTranformInProgress()) {
			Visualization vis = display.getVisualization();
			Rectangle2D bounds = vis.getBounds(Visualization.ALL_ITEMS);

			GraphicsLib.expand(bounds, 300 + (int) (1 / display.getScale()));
			DisplayLib.fitViewToBounds(display, bounds, 1000);

			m_vis.run("layout");
		}
	}

	/**
	 * Update items position.
	 * 
	 * @param orderedTerms
	 *            the ordered terms
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public void updateItemsData(List<FeatureTerm> orderedTerms) throws FeatureTermException {
		// Set labels:
		Iterator<?> i = vg.nodes();

		for (FeatureTerm t : orderedTerms) {
			VisualItem vi = (VisualItem) i.next();
			vi.set("id", new Integer(orderedTerms.indexOf(t)));
			if (domain.contains(t) || t.getName() != null) {
				vi.set("name", t.getName().get().toString());
			} else {
				if (t.isConstant()) {
					vi.set("name", t.toStringNOOS(domain));

				} else {
					vi.set("name", "X" + (orderedTerms.indexOf(t) + 1) + " : " + t.getSort().get());
				}
			}
			// colocar elemento actual
			String solution = t.readPath(tsolution).toStringNOOS(domain);
			Color color = solutionColors.get(solution);
			vi.set(VisualItem.TEXTCOLOR, ColorLib.gray(0));
			vi.setStrokeColor(color.getRGB());

		}

		updateItemsPosition(orderedTerms);
		distancesComputed = true;
		System.out.println("DISTANCE COMPUTED!!");
	}

	/**
	 * Update items position.
	 * 
	 * @param orderedTerms
	 *            the ordered terms
	 */
	private void updateItemsPosition(List<FeatureTerm> orderedTerms) {
		Iterator<?> i;
		System.out.println("Updating positions...");
		i = vg.nodes();
		int xy = 0;
		for (FeatureTerm t : orderedTerms) {
			System.out.print(".");
			VisualItem vi = (VisualItem) i.next();
			vi.set("id", new Integer(orderedTerms.indexOf(t)));
			// set positions
			int x = caseX(xy);
			int y = caseY(xy);

			// System.out.println(this.getSize().getHeight());
			// System.out.println(orderedTerms.size());
			// System.out.println(this.getSize().getHeight() / orderedTerms.size());

			int multiplier = Math.min(Math.max(orderedTerms.size(), 20), 30);

			vi.setX(x * multiplier);
			vi.setY(y * multiplier);
			xy++;
		}
		// if (gui != null)
		// gui.panelGeneralCB.invalidate();
	}

	/**
	 * Draw case base.
	 * 
	 * @param m_cases
	 *            the m_cases
	 */
	public void drawCaseBase(List<FeatureTerm> m_cases) {
		boolean first = true;

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

	/**
	 * Gets the percentage.
	 * 
	 * @return the percentage
	 */
	public String getPercentage() {
		String s = "" + distancesComputedPercentage;
		return (s);
	}

}

class TermSelectControl extends ControlAdapter {

	public VisualItem menuItem = null;
	public List<FeatureTerm> terms = null;
	public FTKBase dm = null;
	public Path sp = null, dp = null;

	protected VisualItem activeItem;
	protected Point2D down = new Point2D.Double();
	protected Point2D temp = new Point2D.Double();
	protected boolean dragged;
	final JPopupMenu menu = new JPopupMenu();
	private FTLGUI ftlgui;
	private JPopupMenu popupMenu;

	public TermSelectControl(List<FeatureTerm> t, FTKBase a_dm, Path a_sp, Path a_dp) {
		terms = t;
		dm = a_dm;
		sp = a_sp;
		dp = a_dp;

		// Create and add a menu item
		JMenuItem menuItem1 = new JMenuItem("View Term");
		JMenuItem menuItem2 = new JMenuItem("View Properties (formal)");
		JMenuItem menuItem3 = new JMenuItem("View Properties (fast)");

		menuItem1.addActionListener(new TermVisualizerActionListener(this));
		menuItem2.addActionListener(new PropertiesFormalVisualizerActionListener(this));
		menuItem3.addActionListener(new PropertiesFastVisualizerActionListener(this));

		menu.add(menuItem1);
		menu.add(menuItem2);
		menu.add(menuItem3);

	}

	public TermSelectControl(final List<FeatureTerm> terms2, FTKBase dm2, final Path solution, final Path descriptionPath,
			final FTLGUI ftlgui) {
		this(terms2, dm2, solution, descriptionPath);
		this.ftlgui = ftlgui;
		if (ftlgui != null && (ftlgui.lazyTestSetPanel != null) && (ftlgui.lazyTestSetPanel.isShowing())) {
			JMenuItem menuItem4 = new JMenuItem("Hypothesis");
			menuItem4.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					int index = (Integer) TermSelectControl.this.menuItem.get("id");
					loadLazyHypothesis(terms2, solution, descriptionPath, ftlgui, index);
				}
			});
			menu.add(menuItem4);
		}
	}

	/**
	 * @param terms2
	 * @param sp2
	 * @param dp2
	 * @param ftlgui
	 */
	private void loadLazyHypothesis(final List<FeatureTerm> terms2, final Path sp2, final Path dp2, final FTLGUI ftlgui, int index) {
		ftlgui.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		FeatureTerm t = TermSelectControl.this.terms.get(index);
		ftlgui.lazyClassify(t, terms2, sp2, dp2);
		ftlgui.setCursor(null);
	}

	public void itemEntered(VisualItem item, MouseEvent e) {
		Display d = (Display) e.getSource();
		d.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		activeItem = item;
		setFixed(item, true);

		int index = (Integer) TermSelectControl.this.activeItem.get("id");
		FeatureTerm t = TermSelectControl.this.terms.get(index);

		popupMenu = new JPopupMenu();
		popupMenu.setEnabled(false);
		popupMenu.setFocusable(false);
		String g = null;
		try {
			if (t.readPath(sp).getName() != null) {
				g = t.readPath(sp).getName().toString();
				popupMenu.add(g);
				popupMenu.show(e.getComponent(), e.getX() + 30, e.getY() - 20);
			} else {
				popupMenu.setVisible(false);
				popupMenu = null;
			}
		} catch (FeatureTermException e2) {
			e2.printStackTrace();
		}

	}

	public void itemExited(VisualItem item, MouseEvent e) {
		if (activeItem == item) {
			activeItem = null;
			setFixed(item, false);
		}
		Display d = (Display) e.getSource();
		d.setCursor(Cursor.getDefaultCursor());
		if (popupMenu != null)
			popupMenu.setVisible(false);
		popupMenu = null;
	}

	public void itemPressed(VisualItem item, MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1 && !e.isConsumed()) {
			e.consume();
			// handle double click.
			dragged = false;
			Display d = (Display) e.getComponent();
			d.getAbsoluteCoordinate(e.getPoint(), down);

			// Display the pop up!!!
			if (item != null) {
				try {
					int index = (Integer) item.get("id");

					if (ftlgui != null && (ftlgui.lazyTestSetPanel != null) && (ftlgui.lazyTestSetPanel.isShowing())) {
						loadLazyHypothesis(terms, sp, dp, ftlgui, index);
					}

					FeatureTerm t = terms.get(index);

					if (ftlgui == null) {
						JFrame frame;
						frame = FTVisualizer.newWindow("FTVisualizer demo", 640, 480, t, dm, true, true);
						frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
						CloseMacAction.addMacCloseBinding(frame);

						frame.setVisible(true);
						frame.pack();

						// double multi = 1.2;
						// frame.setSize((int)(ad.getPreferredSize().getWidth()*multi),
						// (int)(ad.getPreferredSize().getHeight()*multi));
					} else {
						if (ftlgui.panelGeneralCB.isShowing())
							ftlgui.generalReloadFT(t);
					}

				} catch (FeatureTermException ex) {
					ex.printStackTrace();
				}
			}
		} else {
			if (SwingUtilities.isRightMouseButton(e)) {
				if (item != null) {
					menuItem = item;
					menu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		}
	}

	protected static void setFixed(VisualItem item, boolean fixed) {
		if (item instanceof AggregateItem) {
			Iterator<?> items = ((AggregateItem) item).items();
			while (items.hasNext()) {
				setFixed((VisualItem) items.next(), fixed);
			}
		} else {
			item.setFixed(fixed);
		}
	}
} // end of class TermSelectControl

class TermVisualizerActionListener implements ActionListener {
	private TermSelectControl control;
	private FeatureTerm term;

	private FeatureTerm getTerm() {
		int index = (Integer) control.menuItem.get("id");
		return control.terms.get(index);
	}

	public TermVisualizerActionListener(TermSelectControl c) {
		super();
		control = c;
	}

	public void actionPerformed(ActionEvent e) {
		// Display the pop up!!!
		// setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		int index = (Integer) control.menuItem.get("id");
		FeatureTerm t = control.terms.get(index);
		try {
			// JFrame frame = new JFrame(t.getName().toString());
			// frame = FTVisualizer.newWindow("Term Visualizer", 640, 480, t, control.dm, true, true);

			FTVisualizer ad = new FTVisualizer(1000, 1000, t, control.dm, true, true);
			JFrame frame = new JFrame(t.getName().toString());
			frame.getContentPane().add(ad);
			frame.pack();
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

			CloseMacAction.addMacCloseBinding(frame);
			frame.setVisible(true);

			double multi = 1.2;
			frame.setSize((int) (ad.getPreferredSize().getWidth() * multi), (int) (ad.getPreferredSize().getHeight() * multi));

			this.term = t;
		} catch (FeatureTermException e1) {
			e1.printStackTrace();
		}

	}

}

class PropertiesFormalVisualizerActionListener implements ActionListener {
	private TermSelectControl control;

	public PropertiesFormalVisualizerActionListener(TermSelectControl c) {
		super();
		control = c;
	}

	public void actionPerformed(ActionEvent evt) {
		int index = (Integer) control.menuItem.get("id");
		FeatureTerm t = control.terms.get(index);
		try {
			List<FeatureTerm> properties = Disintegration.disintegrate(t.readPath(control.dp), control.dm, t.getSort().getOntology());
			JFrame frame = PropertiesVisualizer.newWindow("Properties", 640, 480, properties, control.dm, true);
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			CloseMacAction.addMacCloseBinding(frame);

			frame.setVisible(true);

		} catch (FeatureTermException ex) {
			ex.printStackTrace();
			System.err.println("Exception generating properties!");
		}
	}
}

class PropertiesFastVisualizerActionListener implements ActionListener {
	private TermSelectControl control;

	public PropertiesFastVisualizerActionListener(TermSelectControl c) {
		super();
		control = c;
	}

	public void actionPerformed(ActionEvent evt) {
		int index = (Integer) control.menuItem.get("id");
		FeatureTerm t = control.terms.get(index);
		try {
			List<FeatureTerm> properties = Disintegration.disintegrateFast(t.readPath(control.dp), control.dm, t.getSort().getOntology());
			JFrame frame = PropertiesVisualizer.newWindow("Properties", 640, 480, properties, control.dm, true);
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			CloseMacAction.addMacCloseBinding(frame);

			frame.setVisible(true);

		} catch (FeatureTermException ex) {
			ex.printStackTrace();
			System.err.println("Exception generating properties!");
		}
	}
}
