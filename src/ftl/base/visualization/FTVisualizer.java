/**
 * Copyright (c) 2004-2006 Regents of the University of California.
 * See "license-prefuse.txt" for licensing terms.
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

import ftl.base.bridges.NOOSParser;
import ftl.base.core.BaseOntology;
import ftl.base.core.FTKBase;
import ftl.base.core.FTRefinement;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Ontology;
import ftl.base.core.SetFeatureTerm;
import ftl.base.core.Symbol;
import ftl.base.core.TermFeatureTerm;
import ftl.base.utils.FeatureTermException;
import ftl.base.utils.Pair;
import ftl.base.utils.RewindableInputStream;
import ftl.base.visualization.LabelEdgeRenderer;
import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.layout.Layout;
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
import prefuse.util.GraphicsLib;
import prefuse.util.force.DragForce;
import prefuse.util.force.ForceSimulator;
import prefuse.util.force.NBodyForce;
import prefuse.util.force.SpringForce;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;


public class FTVisualizer extends Display {

    public static final String GRAPH = "graph";
    public static final String NODES = "graph.nodes";
    public static final String EDGES = "graph.edges";
    public static final String AGGR = "aggregates";


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
            Ontology o = new Ontology();
            FTKBase dm = new FTKBase();
            FTKBase case_base = new FTKBase();
            case_base = new FTKBase();

            Ontology base_ontology = new BaseOntology();
            o.uses(base_ontology);
            FTKBase base_domain_model = new FTKBase();

            base_domain_model.create_boolean_objects(base_ontology);
            dm.importNOOS("NOOS/family-ontology.noos", o);
            dm.importNOOS("NOOS/family-dm.noos", o);
            dm.importNOOS("NOOS/zoology-ontology.noos", o);
            dm.importNOOS("NOOS/zoology-dm.noos", o);
            dm.importNOOS("NOOS/sponge-ontology.noos", o);
            dm.importNOOS("NOOS/sponge-dm.noos", o);
            dm.importNOOS("NOOS/trains-ontology.noos", o);
            dm.importNOOS("NOOS/trains-dm.noos", o);
            case_base.uses(dm);
            // String var = "(define (person)    (son (define ?X1 (male)))   " + "	(grandfather !X1)   )";

            String var = "(define (trains-description)     " + 
                         "  (ncar 3) " + 
                         "  (cars (define (set)             " +
                         "    (define (car) " +
                         "      (infront (define ?X5 (car)" + 
                         "                 (lcont (define (set) " + 
                         "	                        (define (circlelod)) " +
                         "                          (define (circlelod)) ))  " + 
                         "                 (infront (define ?X7 (car) " + 
                         "	                          (lcont (define (trianglod))) " +
                         "                            (npl 1) " + 
                         "	                          (nwhl 2) " + 
                         "                            (loc 3) " + 
                         "	                          (cshape openrect) " +
                         "                            (ln short)))" + 
                         "                 (npl 3) " + 
                         "	               (nwhl 2)   " + 
                         "                 (loc 2) " +
                         "                 (cshape closedrect) " + 
                         "	               (ln long)))    " + 
                         "      (npl 0)  " + 
                         "      (nwhl 2)" + 
                         "      (loc 1) " + 
                         "      (cshape engine) " +
                         "      (ln long))  " + 
                         "    !X5 " + 
                         "    !X7)))";

            FeatureTerm f = NOOSParser.parse(new RewindableInputStream(new ByteArrayInputStream(var.getBytes("UTF-8"))), case_base, o);

            FTVisualizer ad = new FTVisualizer(800,600, f, dm, true, true);
            JFrame frame = new JFrame("TEST");
            frame.getContentPane().add(ad);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setVisible(true);
    }    
    
    public static JFrame newWindow(String name,int dx,int dy,FeatureTerm f,FTKBase dm, boolean separateConstants, boolean groupSets) throws FeatureTermException {
        FTVisualizer ad = new FTVisualizer(dx,dy,f,dm, separateConstants, groupSets);
        JFrame frame = new JFrame(name);
        frame.getContentPane().add(ad);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        return frame;
    }


    public FTVisualizer(int dx,int dy,FeatureTerm f,FTKBase dm, boolean separateConstants, boolean groupSets) throws FeatureTermException {
        // initialize display and data
        super(new Visualization());
        initDataGroups(f,dm,separateConstants, groupSets);

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

        ColorAction aStroke = new ColorAction(AGGR, VisualItem.STROKECOLOR);
        aStroke.setDefaultColor(ColorLib.gray(200));
        aStroke.add("_hover", ColorLib.rgb(255,100,100));

        int[] palette = new int[] {
            ColorLib.rgba(255,200,200,150),
            ColorLib.rgba(200,255,200,150),
            ColorLib.rgba(200,200,255,150)
        };
        ColorAction aFill = new DataColorAction(AGGR, "id",
                Constants.NOMINAL, VisualItem.FILLCOLOR, palette);

        // bundle the color actions
        ActionList colors = new ActionList();
        colors.add(nFill);
        colors.add(nEdges);
        colors.add(aStroke);
        colors.add(aFill);

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
        layout.add(new AggregateLayout(AGGR));
        layout.add(new RepaintAction());
        m_vis.putAction("layout", layout);

        // set up the display
        setSize(dx,dy);
        pan(250, 250);
        setHighQuality(true);
        addControlListener(new AggregateDragControl());
        addControlListener(new ZoomControl());
        addControlListener(new PanControl());

//      ActionList draw = new ActionList();
//      draw.add(new GraphDistanceFilter(GRAPH, 50));
//      m_vis.putAction("draw", draw);


        // set things running
        m_vis.run("layout");
    }

    private void initDataGroups(FeatureTerm f,FTKBase dm, boolean separateConstants, boolean groupSets) throws FeatureTermException {
        Graph g = new Graph(true);
        
        g.addColumn("name", String.class);

        HashMap<FeatureTerm, List<Pair<TermFeatureTerm,Symbol> > > variables = FTRefinement.variablesWithAllParents(f);
        List<FeatureTerm> orderedVariables = new LinkedList<FeatureTerm>();
        List<String> orderedFeatures = new LinkedList<String>();

        // Create nodes:
        for(FeatureTerm v:variables.keySet()) {
            if (separateConstants && (v.isConstant() || dm.contains(v))) {
                for(Pair<TermFeatureTerm,Symbol> parent:variables.get(v)) {
                    orderedVariables.add(v);
                    g.addNode();
                }
            } else {
                orderedVariables.add(v);
                g.addNode();
            }
        }

        // Create links:
        for(FeatureTerm v:variables.keySet()) {
            List<Pair<TermFeatureTerm,Symbol> > allParents = variables.get(v);

            int parentIndex = 0;
            for(Pair<TermFeatureTerm,Symbol> parent:allParents) {
                if (separateConstants && (v.isConstant() || dm.contains(v))) {
                    if (parent!=null) {
                        int i1 = orderedVariables.indexOf(v)+parentIndex;
                        int i2 = orderedVariables.indexOf(parent.m_a);
                        Node n1 = g.getNode(i1);
                        Node n2 = g.getNode(i2);

                        Edge e = g.addEdge(n2, n1);
                        e.set("name",parent.m_b.get());
                    }
                    parentIndex++;
                } else {
                    if (parent!=null) {
                        int i1 = orderedVariables.indexOf(v);
                        int i2 = orderedVariables.indexOf(parent.m_a);
                        Node n1 = g.getNode(i1);
                        Node n2 = g.getNode(i2);

                        Edge e = g.addEdge(n2, n1);
                        e.set("name",parent.m_b.get());
                    }
                }
            }
        }

        VisualGraph vg = m_vis.addGraph(GRAPH, g);

        AggregateTable at = m_vis.addAggregates(AGGR);
        at.addColumn(VisualItem.POLYGON, float[].class);
        at.addColumn("id", int.class);

        // Create sets:
        if (groupSets) {
            int setID = 0;
            List<SetFeatureTerm> sets = FTRefinement.sets(f);
            for(SetFeatureTerm set:sets) {
                AggregateItem aitem = (AggregateItem)at.addItem();
                aitem.setInt("id", setID++);
                for(FeatureTerm v:set.getSetValues()) {
                    int index = orderedVariables.indexOf(v);
                    aitem.addItem((VisualItem)vg.getNode(index));
                }
            }
        }

        // Set labels:
        Iterator i = vg.nodes();
        for(FeatureTerm v:orderedVariables) {
            VisualItem vi = (VisualItem)i.next();
            if (dm.contains(v) || v.getName()!=null) {
                vi.set("name", v.getName().get());
            } else {
                if (v.isConstant()) {
                    vi.set("name", v.toStringNOOS(dm));
                } else {
                    vi.set("name", "X" + (orderedVariables.indexOf(v)+1) + " : " + v.getSort().get());
                }
            }
            if (v == f) {
                vi.setStroke(new BasicStroke(2.0f));
                vi.setStrokeColor(ColorLib.rgb(128,0,0));
                vi.set(VisualItem.TEXTCOLOR, ColorLib.rgb(128,0,0));
            } else {
                vi.setStrokeColor(ColorLib.rgb(128,128,128));
                vi.set(VisualItem.TEXTCOLOR, ColorLib.gray(0));
            }
        }
        for(String fId:orderedFeatures) {
            VisualItem vi = (VisualItem)i.next();
            String fName = fId.substring(fId.indexOf(":")+1);
            vi.set("name", fName);
        }

        // Set colors:
        i = vg.edges();
        while(i.hasNext()) {
            VisualItem vi = (VisualItem)i.next();
            vi.setFillColor(ColorLib.gray(0));
            vi.setTextColor(ColorLib.rgb(0,128,0));
        }

        m_vis.setInteractive(EDGES, null, false);

    }


} // end of class FTVisualizer


class AggregateLayout extends Layout {

    private int m_margin = 5; // convex hull pixel margin
    private double[] m_pts;   // buffer for computing convex hulls

    public AggregateLayout(String aggrGroup) {
        super(aggrGroup);
    }

    public void run(double frac) {

        AggregateTable aggr = (AggregateTable)m_vis.getGroup(m_group);
        // do we have any  to process?
        int num = aggr.getTupleCount();
        if ( num == 0 ) return;

        // update buffers
        int maxsz = 0;
        for ( Iterator aggrs = aggr.tuples(); aggrs.hasNext();  )
            maxsz = Math.max(maxsz, 4*2*
                    ((AggregateItem)aggrs.next()).getAggregateSize());
        if ( m_pts == null || maxsz > m_pts.length ) {
            m_pts = new double[maxsz];
        }

        // compute and assign convex hull for each aggregate
        Iterator aggrs = m_vis.visibleItems(m_group);
        while ( aggrs.hasNext() ) {
            AggregateItem aitem = (AggregateItem)aggrs.next();

            int idx = 0;
            if ( aitem.getAggregateSize() == 0 ) continue;
            VisualItem item = null;
            Iterator iter = aitem.items();
            while ( iter.hasNext() ) {
                item = (VisualItem)iter.next();
                if ( item.isVisible() ) {
                    addPoint(m_pts, idx, item, m_margin);
                    idx += 2*4;
                }
            }
            // if no aggregates are visible, do nothing
            if ( idx == 0 ) continue;

            // compute convex hull
            double[] nhull = GraphicsLib.convexHull(m_pts, idx);

            // prepare viz attribute array
            float[]  fhull = (float[])aitem.get(VisualItem.POLYGON);
            if ( fhull == null || fhull.length < nhull.length )
                fhull = new float[nhull.length];
            else if ( fhull.length > nhull.length )
                fhull[nhull.length] = Float.NaN;

            // copy hull values
            for ( int j=0; j<nhull.length; j++ )
                fhull[j] = (float)nhull[j];
            aitem.set(VisualItem.POLYGON, fhull);
            aitem.setValidated(false); // force invalidation
        }
    }

    private static void addPoint(double[] pts, int idx,
                                 VisualItem item, int growth)
    {
        Rectangle2D b = item.getBounds();
        double minX = (b.getMinX())-growth, minY = (b.getMinY())-growth;
        double maxX = (b.getMaxX())+growth, maxY = (b.getMaxY())+growth;
        pts[idx]   = minX; pts[idx+1] = minY;
        pts[idx+2] = minX; pts[idx+3] = maxY;
        pts[idx+4] = maxX; pts[idx+5] = minY;
        pts[idx+6] = maxX; pts[idx+7] = maxY;
    }

} // end of class AggregateLayout


class AggregateDragControl extends ControlAdapter {

    private VisualItem activeItem;
    protected Point2D down = new Point2D.Double();
    protected Point2D temp = new Point2D.Double();
    protected boolean dragged;

    public AggregateDragControl() {
    }

    public void itemEntered(VisualItem item, MouseEvent e) {
        Display d = (Display)e.getSource();
        d.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        activeItem = item;
        if ( !(item instanceof AggregateItem) )
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
        if (!SwingUtilities.isLeftMouseButton(e)) return;
        dragged = false;
        Display d = (Display)e.getComponent();
        d.getAbsoluteCoordinate(e.getPoint(), down);
        if ( item instanceof AggregateItem )
            setFixed(item, true);
    }

    public void itemReleased(VisualItem item, MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e)) return;
        if ( dragged ) {
            activeItem = null;
            setFixed(item, false);
            dragged = false;
        }
    }

    public void itemDragged(VisualItem item, MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e)) return;
        dragged = true;
        Display d = (Display)e.getComponent();
        d.getAbsoluteCoordinate(e.getPoint(), temp);
        double dx = temp.getX()-down.getX();
        double dy = temp.getY()-down.getY();

        move(item, dx, dy);

        down.setLocation(temp);
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

    protected static void move(VisualItem item, double dx, double dy) {
        if ( item instanceof AggregateItem ) {
            Iterator items = ((AggregateItem)item).items();
            while ( items.hasNext() ) {
                move((VisualItem)items.next(), dx, dy);
            }
        } else {
            double x = item.getX();
            double y = item.getY();
            item.setStartX(x);  item.setStartY(y);
            item.setX(x+dx);    item.setY(y+dy);
            item.setEndX(x+dx); item.setEndY(y+dy);
        }
    }
} // end of class AggregateDragControl