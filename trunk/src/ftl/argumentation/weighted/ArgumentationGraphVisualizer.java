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

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import ftl.argumentation.visualization.amail.panels.RulePanel;
import ftl.base.core.FTKBase;
import ftl.base.core.FeatureTerm;
import ftl.base.utils.FeatureTermException;
import ftl.base.visualization.LabelEdgeRenderer;
import ftl.base.visualization.ScrollableFrame;

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
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.force.DragForce;
import prefuse.util.force.ForceSimulator;
import prefuse.util.force.NBodyForce;
import prefuse.util.force.SpringForce;
import prefuse.visual.AggregateItem;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;

// TODO: Auto-generated Javadoc
/**
 * The Class ArgumentationGraphVisualizer.
 * 
 * @author santi
 */
public class ArgumentationGraphVisualizer extends Display {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** The Constant GRAPH. */
	public static final String GRAPH = "graph";

	/** The Constant NODES. */
	public static final String NODES = "graph.nodes";

	/** The Constant EDGES. */
	public static final String EDGES = "graph.edges";

	/** The selected argument. */
	public WeightedArgument selectedArgument = null;

	/**
	 * New window.
	 * 
	 * @param name
	 *            the name
	 * @param dx
	 *            the dx
	 * @param dy
	 *            the dy
	 * @param ag
	 *            the ag
	 * @param dm
	 *            the dm
	 * @return the j frame
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public static JFrame newWindow(String name, int dx, int dy, WArgumentationFramework ag, FTKBase dm) throws FeatureTermException {
		ArgumentationGraphVisualizer ad = new ArgumentationGraphVisualizer(dx, dy, ag, dm);
		JFrame frame = new JFrame(name);
		frame.getContentPane().add(ad);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		return frame;
	}

	/**
	 * Instantiates a new argumentation graph visualizer.
	 * 
	 * @param dx
	 *            the dx
	 * @param dy
	 *            the dy
	 * @param ag
	 *            the ag
	 * @param dm
	 *            the dm
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public ArgumentationGraphVisualizer(int dx, int dy, WArgumentationFramework ag, FTKBase dm) throws FeatureTermException {
		// initialize display and data
		super(new Visualization());
		initDataGroups(ag);

		// set up the renderers
		// draw the nodes as basic shapes
		// Renderer nodeR = new ShapeRenderer(20);
		LabelRenderer nodeR = new LabelRenderer("name");
		nodeR.setHorizontalPadding(4);
		nodeR.setVerticalPadding(2);
		nodeR.setRoundedCorner(8, 8); // round the corners
		EdgeRenderer edgeR = new LabelEdgeRenderer(Constants.EDGE_TYPE_LINE, Constants.EDGE_ARROW_FORWARD);
		edgeR.setArrowHeadSize(6, 6);

		DefaultRendererFactory drf = new DefaultRendererFactory(nodeR, edgeR);
		m_vis.setRendererFactory(drf);

		// set up the visual operators
		// first set up all the color actions
		ColorAction nFill = new ColorAction(NODES, VisualItem.FILLCOLOR);
		nFill.setDefaultColor(ColorLib.gray(255));
		nFill.add("_hover", ColorLib.gray(200));

		ColorAction nEdges = new ColorAction(EDGES, VisualItem.STROKECOLOR);
		nEdges.setDefaultColor(ColorLib.gray(100));

		int[] palette = new int[] { ColorLib.rgba(255, 200, 200, 150), ColorLib.rgba(200, 255, 200, 150), ColorLib.rgba(200, 200, 255, 150) };

		// bundle the color actions
		ActionList colors = new ActionList();
		colors.add(nFill);
		colors.add(nEdges);

		// now create the main layout routine
		ActionList layout = new ActionList(Activity.INFINITY);
		ForceDirectedLayout fdl = new ForceDirectedLayout(GRAPH, true);
		ForceSimulator m_fsim = new ForceSimulator();
		m_fsim.addForce(new NBodyForce());
		m_fsim.addForce(new SpringForce(1E-4f, 200));
		m_fsim.addForce(new DragForce());
		fdl.setForceSimulator(m_fsim);

		layout.add(colors);
		layout.add(fdl);
		layout.add(new RepaintAction());
		m_vis.putAction("layout", layout);

		// set up the display
		setSize(dx, dy);
		pan(250, 250);
		setHighQuality(true);
		addControlListener(new DragControl());
		addControlListener(new ZoomControl());
		addControlListener(new PanControl());
		addControlListener(new ArgumentSelectControl(ag.getArguments(), dm, this));

		// ActionList draw = new ActionList();
		// draw.add(new GraphDistanceFilter(GRAPH, 50));
		// m_vis.putAction("draw", draw);

		// set things running
		m_vis.run("layout");
	}

	/**
	 * Inits the data groups.
	 * 
	 * @param ag
	 *            the ag
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	private void initDataGroups(WArgumentationFramework ag) throws FeatureTermException {
		Graph g = new Graph(true);

		g.addColumn("id", Integer.class);
		g.addColumn("name", String.class);

		// Create nodes:
		for (WeightedArgument a : ag.m_arguments) {
			g.addNode();
		}

		// Create links:
		for (WeightedArgument a : ag.m_arguments) {
			List<WeightedArgument> l = ag.m_attacks.get(a);

			if (l != null) {
				for (WeightedArgument a2 : l) {
					Node n1 = g.getNode(ag.m_arguments.indexOf(a2));
					Node n2 = g.getNode(ag.m_arguments.indexOf(a));

					Edge e = g.addEdge(n1, n2);
					e.set("name", "" + a2.attackStrength(a));
				}
			}
		}

		VisualGraph vg = m_vis.addGraph(GRAPH, g);

		Iterator i = vg.nodes();
		for (WeightedArgument a : ag.m_arguments) {
			VisualItem vi = (VisualItem) i.next();
			String tmp = "A" + a.getID();
			for (ArgumentExaminationRecord aer : a.getExaminations()) {
				tmp += " (" + aer.m_agent_name + ":";
				for (FeatureTerm s : aer.m_histogram.keySet())
					tmp += " " + aer.m_histogram.get(s);
				tmp += ")";
			}
			vi.set("name", tmp);
			vi.set("id", new Integer(ag.m_arguments.indexOf(a)));

			if (ag.accepted(a))
				vi.setStrokeColor(ColorLib.rgb(0, 128, 0));
			else
				vi.setStrokeColor(ColorLib.rgb(128, 0, 0));
			vi.set(VisualItem.TEXTCOLOR, ColorLib.gray(0));
		}

		// Set colors:
		i = vg.edges();
		while (i.hasNext()) {
			VisualItem vi = (VisualItem) i.next();
			vi.setFillColor(ColorLib.gray(0));
			vi.setTextColor(ColorLib.rgb(0, 128, 0));
		}

		m_vis.setInteractive(EDGES, null, false);

	}
}

class ArgumentSelectControl extends ControlAdapter {

	public VisualItem menuItem = null;
	public List<WeightedArgument> args = null;
	public FTKBase dm = null;
	public ArgumentationGraphVisualizer vis = null;

	protected VisualItem activeItem;
	protected Point2D down = new Point2D.Double();
	protected Point2D temp = new Point2D.Double();
	protected boolean dragged;
	final JPopupMenu menu = new JPopupMenu();

	public ArgumentSelectControl(List<WeightedArgument> a, FTKBase a_dm, ArgumentationGraphVisualizer v) {
		args = a;
		dm = a_dm;
		vis = v;

		// Create and add a menu item
		// JMenuItem menuItem1 = new JMenuItem("View Argument");
		// menuItem1.addActionListener(new TermVisualizerActionListener(this));
		// menu.add(menuItem1);

	}

	public void itemEntered(VisualItem item, MouseEvent e) {
		Display d = (Display) e.getSource();
		d.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		activeItem = item;
		setFixed(item, true);

		int index = (Integer) item.get("id");
		vis.selectedArgument = args.get(index);
	}

	public void itemExited(VisualItem item, MouseEvent e) {
		if (activeItem == item) {
			activeItem = null;
			setFixed(item, false);
		}
		Display d = (Display) e.getSource();
		d.setCursor(Cursor.getDefaultCursor());

		vis.selectedArgument = null;
	}

	public void itemPressed(VisualItem item, MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2 && !e.isConsumed()) {
			e.consume();
			// handle double click.
			dragged = false;
			Display d = (Display) e.getComponent();
			d.getAbsoluteCoordinate(e.getPoint(), down);

			// Display the pop up!!!
			if (item != null) {
				try {
					int index = (Integer) item.get("id");
					WeightedArgument a = args.get(index);
					// JFrame frame;
					// frame = FTVisualizer.newWindow("Argument Pattern", 640, 480, a.m_a.m_rule.pattern, dm, true,
					// true);
					// frame.setVisible(true);

					ScrollableFrame f = new ScrollableFrame("Rule Argument", 1024, 480, new RulePanel(a.m_a.m_rule.pattern, a.m_a.m_rule.solution, dm));
					f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				} catch (Exception ex) {
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
			Iterator items = ((AggregateItem) item).items();
			while (items.hasNext()) {
				setFixed((VisualItem) items.next(), fixed);
			}
		} else {
			item.setFixed(fixed);
		}
	}
} // end of class TermSelectControl
