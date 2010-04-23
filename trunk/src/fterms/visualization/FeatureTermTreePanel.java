/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fterms.visualization;

import fterms.FTKBase;
import fterms.FeatureTerm;
import fterms.SetFeatureTerm;
import fterms.Symbol;
import fterms.exceptions.FeatureTermException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.font.LineMetrics;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import util.Pair;

/**
 *
 * @author santi
 *
 * This class draws feature terms assuming they are almost a tree. 
 * It's known to have issues properly computing the location of nodes, when the terms are complex
 * graphs. So, if your term resembles more a graph,
 * then it's better to visualize it using fterms.visualization.FTVisualizer instead
 */

public class FeatureTermTreePanel extends JPanel {
    public static final int HSEPARATION = 64;
    public static final int VSEPARATION = 12;
    public static final int FEATUREVSEPARATION = 4;
    protected static Color ftBackgroundColor = new Color(240,240,240);

    protected FeatureTerm m_ft = null;
    protected FTKBase m_dm = null;
    protected HashMap<FeatureTerm,Rectangle2D> m_positions = null;

    public FeatureTermTreePanel(FeatureTerm f,FTKBase dm) {
        m_ft = f;
        m_dm = dm;
    }

    public void paint(Graphics g) {
		super.paint(g);

        if (m_positions==null && m_ft!=null) {
            m_positions = new HashMap<FeatureTerm,Rectangle2D>();
            computeNodePositions(m_ft,g,20,20);
            Rectangle2D size = subtreeSize(m_ft, g, new LinkedList<FeatureTerm>());
            setPreferredSize(new Dimension((int)size.getWidth()+40,(int)size.getHeight()+40));
        }

        if (m_positions!=null) {
            for(FeatureTerm f:m_positions.keySet()) {
                Rectangle2D p = m_positions.get(f);
                paintNode(f, g, (int)p.getX(),(int)p.getY());
            }
        }
    }

    protected void computeNodePositions(FeatureTerm t,Graphics g,int x,int y) {
        class PositionRecord {
            public int x,y;
            public FeatureTerm f;

            public PositionRecord(int ax,int ay,FeatureTerm af) {
                x = ax;
                y = ay;
                f = af;
            }
        }
        List<PositionRecord> open = new LinkedList<PositionRecord>();
        List<FeatureTerm> closed = new LinkedList<FeatureTerm>();
        open.add(new PositionRecord(x,y,t));
        closed.add(t);

        while(!open.isEmpty()) {
            PositionRecord r = open.remove(0);
            x = r.x;
            y = r.y;
            List<FeatureTerm> tmp = new LinkedList<FeatureTerm>();
            tmp.addAll(closed);
            Rectangle2D Tsize = subtreeSize(r.f, g, tmp);
            Rectangle2D Nsize = nodeSize(r.f, g);

            Rectangle2D position = new Rectangle2D.Double(r.x,r.y+(Tsize.getHeight()/2)-(Nsize.getHeight()/2),Nsize.getWidth(),Nsize.getHeight());
//            Point2D position = new Point2D.Double(r.x,r.y);
            m_positions.put(r.f,position);

            x+=Nsize.getWidth() + HSEPARATION;
            for(Pair<String,FeatureTerm> f2:children(r.f)) {
                if (f2.m_b!=null && !closed.contains(f2.m_b)) {
                    open.add(new PositionRecord(x,y,f2.m_b));
                    closed.add(f2.m_b);
                    List<FeatureTerm> tmp2 = new LinkedList<FeatureTerm>();
                    tmp2.addAll(closed);
                    y += subtreeSize(f2.m_b,g,tmp2).getHeight()+VSEPARATION;
                }
            }
        }
    }

    String nodeHeader(FeatureTerm f)
    {
        String header = null;

        if (f==null) {
            System.err.println("null term!!!");
        }

        if (f instanceof SetFeatureTerm) return "set";

        if (m_dm.contains(f) || f.isConstant()) return f.toStringNOOS(m_dm);

        if (f.getName()!=null) {
            header = f.getSort().get() + ": " + f.getName();
        } else {
            header = f.getSort().get();
        }

        return header;
    }

    List<Pair<String,FeatureTerm>> children(FeatureTerm f)
    {
        List<Pair<String,FeatureTerm>> ret = new LinkedList<Pair<String,FeatureTerm>>();

        if (f instanceof SetFeatureTerm) {
            for(FeatureTerm f2:((SetFeatureTerm)f).getSetValues()) {
                ret.add(new Pair<String,FeatureTerm>("",f2));
            }
            return ret;
        }

        for(Symbol fn:f.getSort().getFeatures()) {
            try {
                FeatureTerm c = f.featureValue(fn);
                if (c == null || (c instanceof SetFeatureTerm && ((SetFeatureTerm)c).getSetValues().size()==0)) {
                } else {
                    if (m_dm.contains(c) || c.isConstant()) {
                        ret.add(new Pair<String, FeatureTerm>(fn.get() + ": " + c.toStringNOOS(m_dm), null));
                    } else {
                        ret.add(new Pair<String, FeatureTerm>(fn.get(), c));
                    }
                }
            } catch (FeatureTermException ex) {
                ex.printStackTrace();
            }
        }
        return ret;

    }

    
    protected Rectangle2D subtreeSize(FeatureTerm f,Graphics g,List<FeatureTerm> visited) {
        Rectangle2D baseSize = nodeSize(f,g);
        double width = 0;
        double height = 0;
        boolean first = true;
        List<Pair<String,FeatureTerm>> children = children(f);
        List<FeatureTerm> newChildren = new LinkedList<FeatureTerm>();

        visited.add(f);

        for(Pair<String,FeatureTerm> f2:children) {
            if (f2.m_b!=null && !visited.contains(f2.m_b)) newChildren.add(f2.m_b);
        }

        visited.addAll(newChildren);

        for(Pair<String,FeatureTerm> f2:children) {
            if (f2.m_b!=null) {
                if (!visited.contains(f2.m_b) || newChildren.contains(f2.m_b)) {
                    Rectangle2D subSize = subtreeSize(f2.m_b,g,visited);
                    width = Math.max(width,subSize.getWidth());
                    height+=subSize.getHeight();
                    if (first) {
                        first = false;
                    } else {
                        height+=VSEPARATION;
                    }
                }
            }
        }

        width = width + (first ? 0:HSEPARATION) + baseSize.getWidth();
        height = Math.max(height,baseSize.getHeight());

        return new Rectangle2D.Double(0,0,width,height);
    }
    

    protected Rectangle2D nodeSize(FeatureTerm f, Graphics g) {
        String header = nodeHeader(f);
        List<Pair<String,FeatureTerm>> children = children(f);

        double w = 0.0,h = 0.0;
        h = (g.getFontMetrics().getHeight()+FEATUREVSEPARATION)*(children.size()+1)+FEATUREVSEPARATION*2;

        Rectangle2D sb = g.getFontMetrics().getStringBounds(header, g);
        w = Math.max(sb.getWidth(), w);
        for(Pair<String,FeatureTerm> s:children) {
            sb = g.getFontMetrics().getStringBounds(s.m_a, g);
            w = Math.max(sb.getWidth()+8, w);
        }

        return new Rectangle2D.Double(0,0,w+16,h);
    }

    protected void paintNode(FeatureTerm f,Graphics g,int x,int y)
    {
        Rectangle2D size = nodeSize(f, g);
        int textHeight = g.getFontMetrics().getHeight();
        g.setColor(ftBackgroundColor);
        g.fillRect(x, y, (int)size.getWidth(), (int)size.getHeight());
        g.setColor(Color.black);
        g.drawRect(x, y, (int)size.getWidth(), (int)size.getHeight());
        g.drawLine(x, y+FEATUREVSEPARATION*2+textHeight, x+(int)size.getWidth(), y+FEATUREVSEPARATION*2+textHeight);

        g.drawString(nodeHeader(f), x+8, y+FEATUREVSEPARATION+textHeight);

        int y2 = y+FEATUREVSEPARATION*3+textHeight*2;
        for(Pair<String,FeatureTerm> s:children(f)) {
            g.drawString(s.m_a, x + 8, y2);

            // Draw lines:
            if (s.m_b!=null) {
                Rectangle2D p = m_positions.get(s.m_b);
                if (p!=null) {
                    int lx1 = x+(int)size.getWidth()-8;
                    int ly1 = y2-textHeight/2+4;
                    int lx2 = (int)p.getX();
//                    int ly2 = (int)p.getY()+textHeight/2+FEATUREVSEPARATION;
                    int ly2 = (int)(p.getY()+p.getHeight()/2);
                    g.drawLine(lx1, ly1, lx2, ly2);
                    g.fillArc(lx1-4, ly1-4, 8, 8, 0, 360);
                    drawArrowTip(g,lx2,ly2,(lx2-lx1),(ly2-ly1),10,10);
                }

            }

            y2 += FEATUREVSEPARATION + textHeight;
        }
    }

    void drawArrowTip(Graphics g,int start_x,int start_y,float vx,float vy,float width,float length)
    {
        double n = Math.sqrt(vx*vx+vy*vy);
        if (n!=0) {
            vx/=n;
            vy/=n;
            double cvx = -vy;
            double cvy = vx;
            int px[]={start_x,(int)(start_x-vx*length+cvx*width/2),(int)(start_x-vx*length-cvx*width/2)};
            int py[]={start_y,(int)(start_y-vy*length+cvy*width/2),(int)(start_y-vy*length-cvy*width/2)};

            g.fillPolygon(px,py, 3);
        }
    }
}
