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

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
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
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.render.PolygonRenderer;
import prefuse.util.ColorLib;
import prefuse.util.force.DragForce;
import prefuse.util.force.ForceSimulator;
import prefuse.util.force.NBodyForce;
import prefuse.util.force.SpringForce;
import prefuse.visual.AggregateItem;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;
import ftl.base.core.FTKBase;
import ftl.base.core.FeatureTerm;
import ftl.base.utils.FeatureTermException;

// TODO: Auto-generated Javadoc
/**
 * The Class PropertiesVisualizer.
 * 
 * @author santi
 */
public class PropertiesVisualizer extends Display {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7907896770435115487L;

	/** The Constant GRAPH. */
	public static final String GRAPH = "graph";

	/** The Constant NODES. */
	public static final String NODES = "graph.nodes";

	/** The Constant EDGES. */
	public static final String EDGES = "graph.edges";

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
	 * @param separateConstants
	 *            the separate constants
	 * @return the j frame
	 */
	public static JFrame newWindow(String name, int dx, int dy, List<FeatureTerm> terms, FTKBase dm, boolean separateConstants) {
		PropertiesVisualizer ad = new PropertiesVisualizer(dx, dy, terms, dm, separateConstants);
		JFrame frame = new JFrame(name);
		frame.getContentPane().add(ad);
		frame.pack();
		return frame;
	}

	/**
	 * Instantiates a new properties visualizer.
	 * 
	 * @param dx
	 *            the dx
	 * @param dy
	 *            the dy
	 * @param terms
	 *            the terms
	 * @param dm
	 *            the dm
	 * @param separateConstants
	 *            the separate constants
	 */
	public PropertiesVisualizer(int dx, int dy, List<FeatureTerm> terms, FTKBase dm, boolean separateConstants) {
		// initialize display and data
		super(new Visualization());
		try {
			initDataGroups(terms, dm);
		} catch (FeatureTermException ex) {
			ex.printStackTrace();
		}

		// set up the renderers
		// draw the nodes as basic shapes
		// Renderer nodeR = new ShapeRenderer(20);
		LabelRenderer nodeR = new LabelRenderer("name");
		nodeR.setHorizontalPadding(4);
		nodeR.setVerticalPadding(2);
		nodeR.setRoundedCorner(8, 8); // round the corners
		EdgeRenderer edgeR = new EdgeRenderer(Constants.EDGE_TYPE_LINE, Constants.EDGE_ARROW_FORWARD);
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

		int[] palette = new int[] { ColorLib.rgba(255, 200, 200, 150), ColorLib.rgba(200, 255, 200, 150), ColorLib.rgba(200, 200, 255, 150) };
		// ColorAction aFill = new DataColorAction(AGGR, "id",
		// Constants.NOMINAL, VisualItem.FILLCOLOR, palette);

		// bundle the color actions
		ActionList colors = new ActionList();
		colors.add(nFill);
		colors.add(nEdges);
		// colors.add(aStroke);
		// colors.add(aFill);

		// now create the main layout routine
		ActionList layout = new ActionList(Activity.INFINITY);
		ForceDirectedLayout fdl = new ForceDirectedLayout(GRAPH, true);
		ForceSimulator m_fsim = new ForceSimulator();
		m_fsim.addForce(new NBodyForce());
		m_fsim.addForce(new SpringForce(1E-4f, 50));
		m_fsim.addForce(new DragForce());
		fdl.setForceSimulator(m_fsim);

		layout.add(colors);
		layout.add(fdl);
		// layout.add(new AggregateLayout(AGGR));
		layout.add(new RepaintAction());
		m_vis.putAction("layout", layout);

		// set up the display
		setSize(dx, dy);
		pan(250, 250);
		setHighQuality(true);
		addControlListener(new AggregateDragControl());
		addControlListener(new ZoomControl());
		addControlListener(new PanControl());
		addControlListener(new PropertiesSelectControl(terms, dm));
		addControlListener(new WheelScrollControl(this.getHeight()));

		// ActionList draw = new ActionList();
		// draw.add(new GraphDistanceFilter(GRAPH, 50));
		// m_vis.putAction("draw", draw);

		// set things running
		m_vis.run("layout");

	}

	/**
	 * Inits the data groups.
	 * 
	 * @param orderedTerms
	 *            the ordered terms
	 * @param dm
	 *            the dm
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	private void initDataGroups(List<FeatureTerm> orderedTerms, FTKBase dm) throws FeatureTermException {
		Graph g = new Graph(true);

		g.addColumn("id", Integer.class);
		g.addColumn("name", String.class);

		// Create nodes:
		for (FeatureTerm t : orderedTerms) {
			g.addNode();
		}

		// Create links:
		// Compute the properties tree:
		int l = orderedTerms.size();
		boolean[] subsumtionMatrix = new boolean[l * l];
		for (int i = 0; i < l * l; i++)
			subsumtionMatrix[i] = false;
		for (int i = 0; i < l; i++) {
			for (int j = 0; j < l; j++) {
				if (i != j) {
					if (orderedTerms.get(i).subsumes(orderedTerms.get(j))) {
						subsumtionMatrix[i * l + j] = true;
						System.out.print("1 ");
					} else {
						System.out.print("0 ");
					}
				}
			}
			System.out.println("");
		}

		// Preprocess the matrix:
		{
			for (int i = 0; i < l; i++) {
				for (int j = 0; j < l; j++) {
					if (subsumtionMatrix[i * l + j]) {
						for (int k = 0; k < l; k++) {
							if (k != j && subsumtionMatrix[k * l + j]) {
								if (subsumtionMatrix[i * l + k]) {
									subsumtionMatrix[i * l + j] = false;
								}
							}
						}
					}
				}
			}
		}

		// Create the actual links:
		for (int i = 0; i < l; i++) {
			for (int j = 0; j < l; j++) {
				if (i != j) {
					if (subsumtionMatrix[i * l + j]) {
						Node n1 = g.getNode(i);
						Node n2 = g.getNode(j);

						Edge e = g.addEdge(n1, n2);
						System.out.print("1 ");
					} else {
						System.out.print("0 ");
					}
				}
			}
			System.out.println("");
		}

		VisualGraph vg = m_vis.addGraph(GRAPH, g);

		// Set labels:
		Iterator i = vg.nodes();
		for (FeatureTerm t : orderedTerms) {
			VisualItem vi = (VisualItem) i.next();
			vi.set("id", new Integer(orderedTerms.indexOf(t)));
			vi.set("name", "P" + (orderedTerms.indexOf(t)));
			vi.setStrokeColor(ColorLib.rgb(128, 128, 128));
			vi.set(VisualItem.TEXTCOLOR, ColorLib.gray(0));
		}

		// Set colors:
		i = vg.edges();
		while (i.hasNext()) {
			VisualItem vi = (VisualItem) i.next();
			vi.setFillColor(ColorLib.gray(0));
			vi.setTextColor(ColorLib.rgb(0, 128, 0));
		}

	}

}

class PropertiesSelectControl extends ControlAdapter {

	public VisualItem menuItem = null;
	public List<FeatureTerm> terms = null;
	public FTKBase dm = null;

	protected VisualItem activeItem;
	protected Point2D down = new Point2D.Double();
	protected Point2D temp = new Point2D.Double();
	protected boolean dragged;
	final JPopupMenu menu = new JPopupMenu();

	public PropertiesSelectControl(List<FeatureTerm> t, FTKBase a_dm) {
		terms = t;
		dm = a_dm;
	}

	public void itemEntered(VisualItem item, MouseEvent e) {
		Display d = (Display) e.getSource();
		d.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		activeItem = item;
		setFixed(item, true);
	}

	public void itemExited(VisualItem item, MouseEvent e) {
		if (activeItem == item) {
			activeItem = null;
			setFixed(item, false);
		}
		Display d = (Display) e.getSource();
		d.setCursor(Cursor.getDefaultCursor());
	}

	public void itemPressed(final VisualItem item, MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2 && !e.isConsumed()) {
			e.consume();
			// handle double click.
			dragged = false;
			Display d = (Display) e.getComponent();
			d.getAbsoluteCoordinate(e.getPoint(), down);

			// Display the pop up!!!
			if (item != null) {
				final Integer index = (Integer) item.get("id");
				final FeatureTerm t = terms.get(index);
				
				javax.swing.SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						try {
							FTVisualizer ad = new FTVisualizer(1000, 1000, t, dm, true, true);
							JFrame frame = new JFrame(index.toString());
							frame.getContentPane().add(ad);
							frame.pack();
							frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
							frame.setVisible(true);

							double multi = 1.2;
							double max = Math.max((ad.getPreferredSize().getHeight())*multi, 150);
							frame.setSize((int)(ad.getPreferredSize().getWidth()*multi), (int)max);
														
						} catch (FeatureTermException e) {
							e.printStackTrace();
						}
						
					}
				});
				
			}
		}
	}

	protected static void setFixed(VisualItem item, boolean fixed) {
		if (item instanceof AggregateItem) {
			Iterator items = ((AggregateItem) item).items();
			while (items.hasNext()) {
				setFixed((VisualItem) items.next(), fixed);
			}
		} else {
			item.setFixed(fixed);
		}
	}
} // end of class TermSelectControl

