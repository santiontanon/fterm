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

// newPoint.java
//
// Mark F. Hulber
// May 1996
//
//
// newPoint is an extension of the Java Point class.  Additions include
//    methods for making comparisons between points including relative
//    direction, magnitude, and angular computations.
//
//

import java.awt.*;
import java.awt.geom.Point2D;

// TODO: Auto-generated Javadoc
/**
 * The Class newPoint.
 */
public class newPoint extends Point2D.Double {

	/** The mm. */
	private java.lang.Math mm;

	/**
	 * Instantiates a new new point.
	 * 
	 * @param nx
	 *            the nx
	 * @param ny
	 *            the ny
	 */
	public newPoint(double nx, double ny) {
		super(nx, ny);
	}

	/**
	 * Length.
	 * 
	 * @return the double
	 */
	public double length() {
		return mm.sqrt(x * x + y * y);
	}

	/**
	 * Classify.
	 * 
	 * @param p0
	 *            the p0
	 * @param p1
	 *            the p1
	 * @return the int
	 */
	public int classify(newPoint p0, newPoint p1) {

		newPoint a = new newPoint(p1.x - p0.x, p1.y - p0.y);
		newPoint b = new newPoint(x - p0.x, y - p0.y);

		double sa = a.x * b.y - b.x * a.y;

		if (sa > 0.0)
			return 0; // LEFT
		if (sa < 0.0)
			return 1; // RIGHT
		if ((a.x * b.x < 0.0) || (a.y * b.y < 0.0))
			return 2; // BEHIND
		if (a.length() < b.length())
			return 3; // BEYOND
		if (p0.equals(this))
			return 4; // ORIGIN
		if (p1.equals(this))
			return 5; // DESTINATION
		return 6; // BETWEEN
	}

	/**
	 * Polar angle.
	 * 
	 * @return the double
	 */
	public double polarAngle() {

		if ((x == 0.0) && (y == 0.0))
			return -1.0;
		if (x == 0.0)
			return ((y > 0.0) ? 90 : 270);
		double theta = mm.atan((double) y / x);
		theta *= 360 / (2 * mm.PI);
		if (x > 0.0)
			return ((y >= 0.0) ? theta : 360 + theta);
		else
			return (180 + theta);
	}

	/**
	 * Polar cmp.
	 * 
	 * @param p
	 *            the p
	 * @param q
	 *            the q
	 * @return the int
	 */
	public int polarCmp(newPoint p, newPoint q) {

		newPoint vp = new newPoint(p.x - this.x, p.y - this.y);
		newPoint vq = new newPoint(q.x - this.x, q.y - this.y);

		double pPolar = vp.polarAngle();
		double qPolar = vq.polarAngle();

		if (pPolar < qPolar)
			return -1;
		if (pPolar > qPolar)
			return 1;
		if (vp.length() < vq.length())
			return -1;
		if (vp.length() > vq.length())
			return 1;
		return 0;
	}

}
