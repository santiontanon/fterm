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
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
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
import prefuse.action.assignment.DataColorAction;
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

/**
 *
 * @author santi
 */
public class PropertiesVisualizer extends Display {
    public static final String GRAPH = "graph";
    public static final String NODES = "graph.nodes";
    public static final String EDGES = "graph.edges";


    public static JFrame newWindow(String name,int dx,int dy,List<FeatureTerm> terms,FTKBase dm,boolean separateConstants) {
        PropertiesVisualizer ad = new PropertiesVisualizer(dx,dy,terms,dm,separateConstants);
        JFrame frame = new JFrame(name);
        frame.getContentPane().add(ad);
        frame.pack();
        return frame;
    }

    public PropertiesVisualizer(int dx,int dy,List<FeatureTerm> terms,FTKBase dm, boolean separateConstants) {
        // initialize display and data
        super(new Visualization());
        try {
            initDataGroups(terms, dm);
        } catch (FeatureTermException ex) {
            ex.printStackTrace();
        }

        // set up the renderers
        // draw the nodes as basic shapes
//        Renderer nodeR = new ShapeRenderer(20);
        LabelRenderer nodeR = new LabelRenderer("name");
        nodeR.setHorizontalPadding(4);
        nodeR.setVerticalPadding(2);
     	nodeR.setRoundedCorner(8, 8); // round the corners
        EdgeRenderer edgeR = new EdgeRenderer(Constants.EDGE_TYPE_LINE,Constants.EDGE_ARROW_FORWARD);
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
        m_fsim.addForce(new SpringForce(1E-4f,100));
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
        addControlListener(new PropertiesSelectControl(terms,dm));

//      ActionList draw = new ActionList();
//	draw.add(new GraphDistanceFilter(GRAPH, 50));
//	m_vis.putAction("draw", draw);


        // set things running
        m_vis.run("layout");

    }

    private void initDataGroups(List<FeatureTerm> orderedTerms,FTKBase dm) throws FeatureTermException {
        Graph g = new Graph(true);

        g.addColumn("id", Integer.class);
        g.addColumn("name", String.class);

        // Create nodes:
        for(FeatureTerm t:orderedTerms) {
            g.addNode();
        }

        // Create links:
        // Compute the properties tree:
        int l = orderedTerms.size();
        boolean []subsumtionMatrix = new boolean[l*l];
        for(int i = 0;i<l*l;i++) subsumtionMatrix[i] = false;
        for(int i = 0;i<l;i++) {
            for(int j = 0;j<l;j++) {
                if (i!=j) {
                    if (orderedTerms.get(i).subsumes(orderedTerms.get(j))) {
                        subsumtionMatrix[i*l+j] = true;
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
            for(int i = 0;i<l;i++) {
                for(int j = 0;j<l;j++) {
                    if (subsumtionMatrix[i*l+j]) {
                        for(int k = 0;k<l;k++) {
                            if (k!=j && subsumtionMatrix[k*l+j]) {
                                if (subsumtionMatrix[i*l+k]) {
                                    subsumtionMatrix[i*l+j] = false;
                                }
                            }
                        }
                    }
                }
            }
        }

        // Create the actual links:
        for(int i = 0;i<l;i++) {
            for(int j = 0;j<l;j++) {
                if (i!=j) {
                    if (subsumtionMatrix[i*l+j]) {
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
        for(FeatureTerm t:orderedTerms) {
            VisualItem vi = (VisualItem)i.next();
            vi.set("id", new Integer(orderedTerms.indexOf(t)));
            vi.set("name", "P" + (orderedTerms.indexOf(t)));
            vi.setStrokeColor(ColorLib.rgb(128,128,128));
            vi.set(VisualItem.TEXTCOLOR, ColorLib.gray(0));
        }

        // Set colors:
        i = vg.edges();
        while(i.hasNext()) {
            VisualItem vi = (VisualItem)i.next();
            vi.setFillColor(ColorLib.gray(0));
            vi.setTextColor(ColorLib.rgb(0,128,0));
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
                int index = (Integer)item.get("id");
                FeatureTerm t = terms.get(index);
                JFrame frame = null;
                try {
                    frame = FTVisualizer.newWindow("FTVisualizer: " + (String) item.get("name"), 640, 480, t, dm, true);
                } catch (FeatureTermException ex) {
                    Logger.getLogger(PropertiesSelectControl.class.getName()).log(Level.SEVERE, null, ex);
                }
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setVisible(true);
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



