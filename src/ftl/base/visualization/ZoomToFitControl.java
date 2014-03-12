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

import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.controls.ControlAdapter;
import prefuse.util.GraphicsLib;
import prefuse.util.display.DisplayLib;
import prefuse.util.ui.UILib;
import prefuse.visual.VisualItem;


/**
 * Zooms a display such that all items within a given group will fit within
 * the display bounds. By default, this achieved by clicking the right
 * mouse button once, with no dragging.
 * 
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class ZoomToFitControl extends ControlAdapter {

    private long m_duration = 2000;
    private int m_margin = 50;
    private int m_button = RIGHT_MOUSE_BUTTON;
    private boolean m_zoomOverItem = true;
    private String m_group = Visualization.ALL_ITEMS;
    
    /**
     * Create a new ZoomToFitControl.
     */
    public ZoomToFitControl() {
    	
    }
    
    /**
     * Create a new ZoomToFitControl.
     * @param group the data group that should fit the Display
     */
    public ZoomToFitControl(String group) {
        this.m_group = group;
    }
    
    /**
     * Create a new ZoomToFitControl.
     * @param button the mouse button used to initiate the zoom-to-fit. One of
     * {@link Control#LEFT_MOUSE_BUTTON}, {@link Control#MIDDLE_MOUSE_BUTTON},
     * or {@link Control#RIGHT_MOUSE_BUTTON}.
     */
    public ZoomToFitControl(int button) {
        this.m_button = button;
    }

    /**
     * Create a new ZoomToFitControl.
     * @param group the data group that should fit the Display
     * @param button the mouse button used to initiate the zoom-to-fit. One of
     * {@link Control#LEFT_MOUSE_BUTTON}, {@link Control#MIDDLE_MOUSE_BUTTON},
     * or {@link Control#RIGHT_MOUSE_BUTTON}.
     */
    public ZoomToFitControl(String group, int button) {
        this.m_group = group;
        this.m_button = button;
    }
    
    /**
     * Create a new ZoomToFitControl.
     * @param group the data group that should fit the Display
     * @param margin the margin, in pixels, desired between the group 
     * and the edge of the display
     * @param duration the duration of the animated zoom
     * @param button the mouse button used to initiate the zoom-to-fit. One of
     * {@link Control#LEFT_MOUSE_BUTTON}, {@link Control#MIDDLE_MOUSE_BUTTON},
     * or {@link Control#RIGHT_MOUSE_BUTTON}.
     */
    public ZoomToFitControl(String group, int margin, 
                            long duration, int button)
    {
        this.m_group = group;
        this.m_margin = margin;
        this.m_duration = duration;
        this.m_button = button;
    }
    
    /**
     * @see prefuse.controls.Control#itemClicked(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
     */
    public void itemClicked(VisualItem item, MouseEvent e) {
        if ( m_zoomOverItem )
            mouseClicked(e);
    }
    

    /**
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(MouseEvent e) {
    	Display display = (Display)e.getComponent();
    	if ( !display.isTranformInProgress() && 
    			e.getClickCount()==2 )
        {
    	    Visualization vis = display.getVisualization();
            Rectangle2D bounds = vis.getBounds(m_group);
            
            //System.out.println("ZTO: " + bounds.getHeight());
            GraphicsLib.expand(bounds, m_margin + (int)(1/display.getScale()));
            DisplayLib.fitViewToBounds(display, bounds, m_duration);
        }
    }
    
    /**
     * Indicates if the zoom control will work while the mouse is
     * over a VisualItem.
     * @return true if the control still operates over a VisualItem
     */
    public boolean isZoomOverItem() {
        return m_zoomOverItem;
    }

    /**
     * Determines if the zoom control will work while the mouse is
     * over a VisualItem
     * @param zoomOverItem true to indicate the control operates
     * over VisualItems, false otherwise
     */
    public void setZoomOverItem(boolean zoomOverItem) {
        this.m_zoomOverItem = zoomOverItem;
    }
    
    /**
     * Get the display margin to include within the "zoomed-to-fit" bounds.
     * @return Display margin currently in use
     */
    public int getMargin() {
        return m_margin;
    }
    
    /**
     * Set the display margin to include within the "zoomed-to-fit" bounds.
     * @param margin Display margin to use
     */
    public void setMargin(int margin) {
        this.m_margin = margin;
    }
    
} // end of class ZoomToFitControl
