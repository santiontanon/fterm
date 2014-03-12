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

import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import prefuse.render.EdgeRenderer;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;

// TODO: Auto-generated Javadoc
/**
 * The Class LabelEdgeRenderer.
 * 
 * @author santi
 */
public class LabelEdgeRenderer extends EdgeRenderer {

	/** The m_font. */
	protected Font m_font = null; // temp font holder
	private FTVisualizer ftObject;

	/**
	 * Instantiates a new label edge renderer.
	 * 
	 * @param labelColor
	 *            the label color
	 */
	public LabelEdgeRenderer(int labelColor) {
		super();
	}

	/**
	 * Instantiates a new label edge renderer.
	 * 
	 * @param lt
	 *            the lt
	 * @param labelColor
	 *            the label color
	 */
	public LabelEdgeRenderer(int lt, int labelColor) {
		super(lt);
	}

	/**
	 * Instantiates a new label edge renderer.
	 * 
	 * @param lt
	 *            the lt
	 * @param at
	 *            the at
	 * @param labelColor
	 *            the label color
	 */
	public LabelEdgeRenderer(int lt, int at, int labelColor) {
		super(lt, at);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see prefuse.render.EdgeRenderer#render(java.awt.Graphics2D, prefuse.visual.VisualItem)
	 */
	public void render(Graphics2D g, VisualItem item) {
		// render the edge line
		super.render(g, item);
		
		// Add label:
		Shape s = getShape(item);
		if (s != null) {
			Rectangle2D r = s.getBounds2D();
			boolean useInt = 1.5 > Math.max(g.getTransform().getScaleX(), g.getTransform().getScaleY());
			if (m_font == null)
				m_font = item.getFont();
			// scale the font as needed
			FontMetrics fm = DEFAULT_GRAPHICS.getFontMetrics(m_font);
			if (item.canGetString("name")) {
				g.setFont(m_font);
				g.setPaint(ColorLib.getColor(item.getTextColor()));
				drawString(g, fm, item.getString("name"), useInt, r.getX(), r.getY(), r.getWidth(), r.getHeight());
			}

		}
	}

	/**
	 * Draw string.
	 * 
	 * @param g
	 *            the g
	 * @param fm
	 *            the fm
	 * @param text
	 *            the text
	 * @param useInt
	 *            the use int
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param w
	 *            the w
	 * @param h
	 *            the h
	 */
	private final void drawString(Graphics2D g, FontMetrics fm, String text, boolean useInt, double x, double y, double w, double h) {
		// compute the x-coordinate
		double tx;
		double ty;
		

		tx = x + (w - fm.stringWidth(text))/2;
		ty = y + (h / 2) - fm.getHeight()/2;
		// use integer precision unless zoomed-in
		// results in more stable drawing

		if (useInt) {
			g.drawString(text, (int) tx, (int) ty);
		} else {
			g.drawString(text, (float) tx, (float) ty);
		}
		
	}

}
