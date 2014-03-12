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

import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

// TODO: Auto-generated Javadoc
/**
 * The Class GiftWrap.
 * 
 * @author santi
 */
public class GiftWrap {

	/**
	 * Convex hull.
	 * 
	 * @param points
	 *            the points
	 * @return the list
	 */
	public static List<Point2D> convexHull(List<Point2D> points) {
		List<Point2D> result = new LinkedList<Point2D>();
		Vector<newPoint> q = new Vector<newPoint>();
		for (Point2D p : points) {
			q.add(new newPoint(p.getX(), p.getY()));
		}

		Vector<newPoint> lines = doWrap(q);

		for (newPoint p : lines) {
			result.add(new Point2D.Double(p.x, p.y));
		}
		return result;
	}

	/**
	 * Do wrap.
	 * 
	 * @param q
	 *            the q
	 * @return the java.util. vector
	 */
	static java.util.Vector doWrap(java.util.Vector q) {
		java.util.Vector lines = new java.util.Vector(100, 100);
		java.util.Vector s = new java.util.Vector(100, 100);

		newPoint temp;
		int n = q.size();
		int a, i;
		if (n > 0) {
			s.removeAllElements();
			s = (java.util.Vector) q.clone();
			for (a = 0, i = 1; i < n; i++) {
				if (((newPoint) s.elementAt(i)).x < ((newPoint) s.elementAt(a)).x
						|| ((((newPoint) s.elementAt(i)).x == ((newPoint) s.elementAt(a)).x) && (((newPoint) s.elementAt(i)).y < ((newPoint) s.elementAt(a)).y))) {
					a = i;
				}
			}
			s.addElement((newPoint) s.elementAt(a));
			lines.removeAllElements();

			for (int m = 0; m < n; m++) {
				temp = (newPoint) s.elementAt(a);
				s.setElementAt((newPoint) s.elementAt(m), a);
				s.setElementAt(temp, m);
				lines.addElement(new newPoint(((newPoint) s.elementAt(m)).x, ((newPoint) s.elementAt(m)).y));

				a = m + 1;

				for (i = m + 2; i <= n; i++) {
					int c = ((newPoint) s.elementAt(i)).classify((newPoint) s.elementAt(m), (newPoint) s.elementAt(a));
					if (c == 1 || c == 3) {
						a = i;
					}
				}

				if (a == n) {
					return lines;
				}

			}
		}
		return null;
	}
}
