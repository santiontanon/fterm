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
  
 package ftl.argumentation.visualization.amail.helpers;

import java.awt.Polygon;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

// TODO: Auto-generated Javadoc
/**
 * The Class newPolygon.
 * 
 * @author santi
 */
public class newPolygon extends Polygon {

	/** The Constant serialVersionUID. */
	static final long serialVersionUID = 1;

	/**
	 * Instantiates a new new polygon.
	 */
	public newPolygon() {
		super();
	}

	/**
	 * Instantiates a new new polygon.
	 * 
	 * @param xCoords
	 *            the x coords
	 * @param yCoords
	 *            the y coords
	 * @param size
	 *            the size
	 */
	public newPolygon(int[] xCoords, int[] yCoords, int size) {
		super(xCoords, yCoords, size);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Polygon#contains(java.awt.geom.Point2D)
	 */
	public boolean contains(Point2D point) {
		boolean retval = false;
		if (super.contains(point)) {
			retval = true;
		} else {
			int[] xCoords = new int[npoints];
			int[] yCoords = new int[npoints];
			double[] coords = new double[6];
			int index = 0;
			PathIterator iter = getPathIterator(null);
			while (!iter.isDone()) {
				int type = iter.currentSegment(coords);
				if (type == PathIterator.SEG_MOVETO) {
					xCoords[index] = (int) coords[0];
					yCoords[index] = (int) coords[1];
					index++;
				} else if (type == PathIterator.SEG_LINETO || type == PathIterator.SEG_CLOSE) {
					Line2D segment = new Line2D.Double(xCoords[index - 1], yCoords[index - 1], coords[0], coords[1]);
					if (segment.ptSegDist(point) == 0) {
						retval = true;
					}
					if (type != PathIterator.SEG_CLOSE) {
						xCoords[index] = (int) coords[0];
						yCoords[index] = (int) coords[1];
						index++;
					}
				}
				iter.next();
			}
		}
		return retval;
	}

}
