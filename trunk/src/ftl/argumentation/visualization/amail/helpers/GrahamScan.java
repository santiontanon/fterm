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
 * The Class GrahamScan.
 * 
 * @author santi
 */
public class GrahamScan {

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

		Vector<newPoint> lines = doGraham(q);
		if (lines == null)
			return null;

		for (newPoint p : lines) {
			result.add(new Point2D.Double(p.x, p.y));
		}
		return result;
	}

	/**
	 * Do graham.
	 * 
	 * @param q
	 *            the q
	 * @return the java.util. vector
	 */
	static java.util.Vector<newPoint> doGraham(java.util.Vector<newPoint> q) {
		java.util.Vector<newPoint> lines = new java.util.Vector<newPoint>(100, 100);
		java.util.Stack<newPoint> stk = new java.util.Stack<newPoint>();
		java.util.Vector<newPoint> s = new java.util.Vector<newPoint>(100, 100);

		int m = 0;
		newPoint temp, temp2;
		int n = q.size();
		int a, i;
		if (n > 2) {
			s.removeAllElements();
			s = (java.util.Vector) q.clone();
			for (i = 1; i < n; i++) {
				if ((s.elementAt(i)).y < (s.elementAt(m)).y || ((s.elementAt(i)).y == (s.elementAt(m)).y) && ((s.elementAt(i)).x < (s.elementAt(m)).x)) {
					m = i;
				}
			}
			temp = (newPoint) s.elementAt(0);
			s.setElementAt((newPoint) s.elementAt(m), 0);
			s.setElementAt(temp, m);

			// stage 2
			temp2 = (newPoint) s.elementAt(0);
			for (i = 2; i < n; i++) {
				for (int j = n - 1; j >= i; j--) {
					if (temp2.polarCmp(s.elementAt(j - 1), s.elementAt(j)) == 1) {
						temp = s.elementAt(j - 1);
						s.setElementAt((newPoint) s.elementAt(j), j - 1);
						s.setElementAt(temp, j);
					}
				}
			}
			for (i = 1; (s.elementAt(i + 1)).classify(s.elementAt(0), s.elementAt(i)) == 3; i++)
				;
			stk.removeAllElements();
			stk.push(s.elementAt(0));
			stk.push(s.elementAt(i));

			boolean blah;
			for (i = i + 1; i < n; i++) {
				blah = true;
				while (blah && !stk.isEmpty()) {
					temp2 = stk.pop();
					if (stk.isEmpty())
						return null;
					if ((s.elementAt(i)).classify(stk.peek(), temp2) == 0) {
						stk.push(temp2);
						blah = false;
					}
				}
				stk.push(s.elementAt(i));
			}

			lines.removeAllElements();

			while (!stk.empty()) {
				lines.addElement(stk.pop());
			}
			return lines;
		}
		return null;
	}
}
