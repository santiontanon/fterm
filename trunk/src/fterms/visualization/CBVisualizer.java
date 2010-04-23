/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fterms.visualization;

import fterms.Disintegration;
import fterms.FTKBase;
import fterms.FeatureTerm;
import fterms.Path;
import fterms.exceptions.FeatureTermException;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
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
import prefuse.data.Graph;
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

/**
 *
 * @author santi
 */
public class CBVisualizer extends Display {
    public static final String GRAPH = "graph";
    public static final String NODES = "graph.nodes";
    public static final String EDGES = "graph.edges";


    public static JFrame newWindow(String name,int dx,int dy,List<FeatureTerm> terms,FTKBase dm, Path sp, Path dp,boolean separateConstants) {
        CBVisualizer ad = new CBVisualizer(dx,dy,terms,dm, sp, dp,separateConstants);
        JFrame frame = new JFrame(name);
        frame.getContentPane().add(ad);
        frame.pack();
        return frame;
    }

    public CBVisualizer(int dx,int dy,List<FeatureTerm> terms,FTKBase dm, Path sp, Path dp, boolean separateConstants) {
        // initialize display and data
        super(new Visualization());
        initDataGroups(terms,dm,separateConstants);

        // set up the renderers
        // draw the nodes as basic shapes
//        Renderer nodeR = new ShapeRenderer(20);
        LabelRenderer nodeR = new LabelRenderer("name");
        nodeR.setHorizontalPadding(4);
        nodeR.setVerticalPadding(2);
     	nodeR.setRoundedCorner(8, 8); // round the corners
        EdgeRenderer edgeR = new LabelEdgeRenderer(Constants.EDGE_TYPE_LINE,Constants.EDGE_ARROW_FORWARD);
        edgeR.setArrowHeadSize(6,6);

        // draw aggregates as polygons with curved edges
        PolygonRenderer polyR = new PolygonRenderer(Constants.POLY_TYPE_CURVE);
        polyR.setCurveSlack(0.15f);

        DefaultRendererFactory drf = new DefaultRendererFactory(nodeR,edgeR);
        drf.add("ingroup('aggregates')", polyR);
        m_vis.setRendererFactory(drf);

        // set up the visual operators
        // first set up all the color actions
        ColorAction nFill = new ColorAction(NODES, VisualItem.FILLCOLOR);
        nFill.setDefaultColor(ColorLib.gray(255));
        nFill.add("_hover", ColorLib.gray(200));

        ColorAction nEdges = new ColorAction(EDGES, VisualItem.STROKECOLOR);
        nEdges.setDefaultColor(ColorLib.gray(100));

//        ColorAction aStroke = new ColorAction(AGGR, VisualItem.STROKECOLOR);
//        aStroke.setDefaultColor(ColorLib.gray(200));
//        aStroke.add("_hover", ColorLib.rgb(255,100,100));

        int[] palette = new int[] {
            ColorLib.rgba(255,200,200,150),
            ColorLib.rgba(200,255,200,150),
            ColorLib.rgba(200,200,255,150)
        };
//        ColorAction aFill = new DataColorAction(AGGR, "id",
//                Constants.NOMINAL, VisualItem.FILLCOLOR, palette);

        // bundle the color actions
        ActionList colors = new ActionList();
        colors.add(nFill);
        colors.add(nEdges);
//        colors.add(aStroke);
//        colors.add(aFill);

        // now create the main layout routine
        ActionList layout = new ActionList(Activity.INFINITY);
        ForceDirectedLayout fdl = new ForceDirectedLayout(GRAPH, true);
        ForceSimulator m_fsim = new ForceSimulator();
        m_fsim.addForce(new NBodyForce());
        m_fsim.addForce(new SpringForce(1E-4f,200));
        m_fsim.addForce(new DragForce());
        fdl.setForceSimulator(m_fsim);

        layout.add(colors);
        layout.add(fdl);
//        layout.add(new AggregateLayout(AGGR));
        layout.add(new RepaintAction());
        m_vis.putAction("layout", layout);

        // set up the display
        setSize(dx,dy);
        pan(250, 250);
        setHighQuality(true);
        addControlListener(new AggregateDragControl());
        addControlListener(new ZoomControl());
        addControlListener(new PanControl());
        addControlListener(new TermSelectControl(terms,dm,sp,dp));

//      ActionList draw = new ActionList();
//	draw.add(new GraphDistanceFilter(GRAPH, 50));
//	m_vis.putAction("draw", draw);


        // set things running
        m_vis.run("layout");

    }

    private void initDataGroups(List<FeatureTerm> orderedTerms,FTKBase dm, boolean separateConstants) {
        Graph g = new Graph(true);

        g.addColumn("id", Integer.class);
        g.addColumn("name", String.class);

        // Create nodes:
        for(FeatureTerm t:orderedTerms) {
            g.addNode();
        }

        VisualGraph vg = m_vis.addGraph(GRAPH, g);

        // Set labels:
        Iterator i = vg.nodes();
        for(FeatureTerm t:orderedTerms) {
            VisualItem vi = (VisualItem)i.next();
            vi.set("id", new Integer(orderedTerms.indexOf(t)));
            if (dm.contains(t) || t.getName()!=null) {
                vi.set("name", t.getName().get());
            } else {
                if (t.isConstant()) {
                    vi.set("name", t.toStringNOOS(dm));
                } else {
                    vi.set("name", "X" + (orderedTerms.indexOf(t)+1) + " : " + t.getSort().get());
                }
            }

            vi.setStrokeColor(ColorLib.rgb(128,128,128));
            vi.set(VisualItem.TEXTCOLOR, ColorLib.gray(0));
        }

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

    public void itemEntered(VisualItem item, MouseEvent e) {
        Display d = (Display)e.getSource();
        d.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        activeItem = item;
        setFixed(item, true);
    }

    public void itemExited(VisualItem item, MouseEvent e) {
        if ( activeItem == item ) {
            activeItem = null;
            setFixed(item, false);
        }
        Display d = (Display)e.getSource();
        d.setCursor(Cursor.getDefaultCursor());
    }

    public void itemPressed(VisualItem item, MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e) &&
            e.getClickCount() == 2 && !e.isConsumed()) {
            e.consume();
            //handle double click.
            dragged = false;
            Display d = (Display)e.getComponent();
            d.getAbsoluteCoordinate(e.getPoint(), down);

            // Display the pop up!!!
            if (item!=null) {
                try {
                    int index = (Integer)item.get("id");
                    FeatureTerm t = terms.get(index);
                    JFrame frame;
                    frame = FTVisualizer.newWindow("FTVisualizer demo", 640, 480, t, dm, true);
                    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    frame.setVisible(true);
                } catch (FeatureTermException ex) {
                    ex.printStackTrace();
                }
            }
        } else {
            if (SwingUtilities.isRightMouseButton(e)) {
                if (item!=null) {
                    menuItem = item;
                    menu.show( e.getComponent() , e.getX(), e.getY());
                }
            }
        }
    }

    protected static void setFixed(VisualItem item, boolean fixed) {
        if ( item instanceof AggregateItem ) {
            Iterator items = ((AggregateItem)item).items();
            while ( items.hasNext() ) {
                setFixed((VisualItem)items.next(), fixed);
            }
        } else {
            item.setFixed(fixed);
        }
    }
} // end of class TermSelectControl


class TermVisualizerActionListener implements ActionListener {
    private TermSelectControl control;

    public TermVisualizerActionListener(TermSelectControl c) {
        super();
        control = c;
    }
    public void actionPerformed(ActionEvent e) {
        try {
            // Display the pop up!!!
            int index = (Integer) control.menuItem.get("id");
            FeatureTerm t = control.terms.get(index);
            JFrame frame = FTVisualizer.newWindow("Term Visualizer", 640, 480, t, control.dm, true);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setVisible(true);
        } catch (FeatureTermException ex) {
            Logger.getLogger(TermVisualizerActionListener.class.getName()).log(Level.SEVERE, null, ex);
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
        int index = (Integer)control.menuItem.get("id");
        FeatureTerm t = control.terms.get(index);
        try {
            List<FeatureTerm> properties = Disintegration.disintegrate(t.readPath(control.dp), control.dm, t.getSort().getOntology());
            JFrame frame = PropertiesVisualizer.newWindow("Properties",640,480,properties,control.dm,control.sp,control.dp,true);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
        int index = (Integer)control.menuItem.get("id");
        FeatureTerm t = control.terms.get(index);
        try {
            List<FeatureTerm> properties = Disintegration.disintegrateFast(t.readPath(control.dp), control.dm, t.getSort().getOntology());
            JFrame frame = PropertiesVisualizer.newWindow("Properties",640,480,properties,control.dm,control.sp,control.dp,true);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setVisible(true);

        } catch (FeatureTermException ex) {
            ex.printStackTrace();
            System.err.println("Exception generating properties!");
        }
    }
}
