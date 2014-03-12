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
  
 package ftl.argumentation.visualization.amail.panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;

import ftl.argumentation.core.Argument;
import ftl.argumentation.core.ArgumentAcceptability;
import ftl.argumentation.core.ArgumentationTree;
import ftl.argumentation.visualization.amail.listeners.ArgumentStateMouseListener;
import ftl.base.core.FTKBase;
import ftl.base.core.Path;
import ftl.base.utils.FeatureTermException;
import ftl.base.utils.Pair;

// TODO: Auto-generated Javadoc
/**
 * The Class ArgumentationStatePanel.
 * 
 * @author santi
 */
public class ArgumentationStatePanel extends JPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 5160182009941056868L;

	/** The agents. */
	List<String> agents = null;

	/** The tree_arguments. */
	HashMap<ArgumentationTree, List<Argument>> tree_arguments = null;

	/** The agent_trees. */
	HashMap<String, List<ArgumentationTree>> agent_trees = null;

	/** The retracted_trees. */
	List<ArgumentationTree> retracted_trees = null;

	/** The aal. */
	List<ArgumentAcceptability> aal = new LinkedList<ArgumentAcceptability>();

	/** The argument i ds. */
	HashMap<Argument, String> argumentIDs = null;

	/** The parent. */
	public ArgumentationPanel parent = null;

	/** The m_domain model. */
	public FTKBase m_domainModel = null;

	/** The argument positions. */
	public HashMap<Argument, Point2D> argumentPositions = new HashMap<Argument, Point2D>();

	/** The highlighted argument. */
	public Argument highlightedArgument = null;

	/** The description path. */
	public Path solutionPath = null, descriptionPath = null;

	/** The size_x. */
	int size_x = 0;

	/** The size_y. */
	int size_y = 0;

	/** The border_x. */
	int border_x = 20;

	/** The border_y. */
	int border_y = 40;

	/** The argument width. */
	int argumentWidth = 40;

	/** The argument height. */
	int argumentHeight = 32;

	/** The argument h separation. */
	int argumentHSeparation = 32;

	/** The argument v separation. */
	int argumentVSeparation = 40;

	/**
	 * Instantiates a new argumentation state panel.
	 * 
	 * @param p
	 *            the p
	 * @param dm
	 *            the dm
	 * @param sp
	 *            the sp
	 * @param dp
	 *            the dp
	 * @param a_aa1
	 *            the a_aa1
	 * @param a_aa2
	 *            the a_aa2
	 * @param a_argumentIDs
	 *            the a_argument i ds
	 */
	public ArgumentationStatePanel(ArgumentationPanel p, FTKBase dm, Path sp, Path dp, ArgumentAcceptability a_aa1, ArgumentAcceptability a_aa2,
			HashMap<Argument, String> a_argumentIDs) {
		agents = new LinkedList<String>();
		for (AgentPanel ap : p.apl)
			agents.add(ap.m_name);
		tree_arguments = new HashMap<ArgumentationTree, List<Argument>>();
		agent_trees = new HashMap<String, List<ArgumentationTree>>();
		retracted_trees = new LinkedList<ArgumentationTree>();
		parent = p;
		m_domainModel = dm;
		solutionPath = sp;
		descriptionPath = dp;
		aal.add(a_aa1);
		aal.add(a_aa2);
		argumentIDs = a_argumentIDs;
		ArgumentStateMouseListener ml = new ArgumentStateMouseListener(this);
		addMouseMotionListener(ml);
		addMouseListener(ml);
	}

	/**
	 * Instantiates a new argumentation state panel.
	 * 
	 * @param p
	 *            the p
	 * @param dm
	 *            the dm
	 * @param sp
	 *            the sp
	 * @param dp
	 *            the dp
	 * @param a_aa
	 *            the a_aa
	 * @param a_argumentIDs
	 *            the a_argument i ds
	 */
	public ArgumentationStatePanel(ArgumentationPanel p, FTKBase dm, Path sp, Path dp, List<ArgumentAcceptability> a_aa, HashMap<Argument, String> a_argumentIDs) {
		agents = new LinkedList<String>();
		for (AgentPanel ap : p.apl)
			agents.add(ap.m_name);
		tree_arguments = new HashMap<ArgumentationTree, List<Argument>>();
		agent_trees = new HashMap<String, List<ArgumentationTree>>();
		retracted_trees = new LinkedList<ArgumentationTree>();
		parent = p;
		m_domainModel = dm;
		solutionPath = sp;
		descriptionPath = dp;
		aal.addAll(a_aa);
		argumentIDs = a_argumentIDs;
		ArgumentStateMouseListener ml = new ArgumentStateMouseListener(this);
		addMouseMotionListener(ml);
		addMouseListener(ml);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	public void paint(Graphics g) {
		super.paint(g);
		List<Argument> latestArguments = new LinkedList<Argument>();

		// Find arguments to highlight:
		if (parent != null && parent.m_messages != null) {
			for (Pair<String, Object> message : parent.m_messages) {
				if (message.m_b instanceof Argument) {
					latestArguments.add((Argument) (message.m_b));
				}
			}
		}

		for (String agent : agents) {
			List<ArgumentationTree> l = agent_trees.get(agent);
			if (l != null) {
				for (ArgumentationTree tree : agent_trees.get(agent)) {
					paintTree(g, tree, latestArguments);
				}
			}
		}
		for (ArgumentationTree tree : retracted_trees) {
			paintTree(g, tree, latestArguments);
		}
	}

	/**
	 * Paint tree.
	 * 
	 * @param g
	 *            the g
	 * @param tree
	 *            the tree
	 * @param latestArguments
	 *            the latest arguments
	 */
	void paintTree(Graphics g, ArgumentationTree tree, List<Argument> latestArguments) {
		for (Argument a : tree_arguments.get(tree)) {
			Argument parent = tree.getParent(a);
			// draw line:
			if (parent != null) {
				Point2D pos1 = argumentPositions.get(a);
				Point2D pos2 = argumentPositions.get(parent);
				g.setColor(Color.black);
				g.drawLine(border_x + (int) (pos1.getX() + argumentWidth / 2), border_y + (int) (pos1.getY() + argumentHeight / 2),
						border_x + (int) (pos2.getX() + argumentWidth / 2), border_y + (int) (pos2.getY() + argumentHeight / 2));
			}
		}

		for (Argument a : tree_arguments.get(tree)) {
			if (latestArguments.contains(a)) {
				paintArgument(g, tree, a, true);
			} else {
				paintArgument(g, tree, a, false);
			}
		}
	}

	/**
	 * Paint argument.
	 * 
	 * @param g
	 *            the g
	 * @param tree
	 *            the tree
	 * @param a
	 *            the a
	 * @param latest
	 *            the latest
	 */
	void paintArgument(Graphics g, ArgumentationTree tree, Argument a, boolean latest) {
		try {// acceptability:
			Point2D p = argumentPositions.get(a);
			int w = argumentWidth;
			int h = argumentHeight;
			List<Boolean> accl = new LinkedList<Boolean>();
			boolean all_acc_false = true;

			for (int n = 0; n < aal.size(); n++) {
				boolean tmp = aal.get(n).accepted(a);
				accl.add(tmp);
				if (tmp) {
					all_acc_false = false;
				}
			}

			// if latest:
			if (latest) {
				float f = (float) (0.7f + 0.3f * Math.sin(System.currentTimeMillis() * 0.01));
				g.setColor(new Color(f, f, f));
				g.fillRect((int) p.getX() + border_x - 4, (int) p.getY() + border_y - 4, w + 8, h + 8);
			}

			// draw argument:
			if (a.m_type == Argument.ARGUMENT_EXAMPLE) {
				g.setColor(Color.yellow);
			} else {
				if (all_acc_false || tree.defeatedP(a)) {
					g.setColor(Color.red);
				} else {
					g.setColor(Color.green);
				}
			}

			g.fillRect((int) p.getX() + border_x, (int) p.getY() + border_y, w, h);

			if (a.m_type == Argument.ARGUMENT_RULE) {
				int acc_width_x = (accl.size() - 1) * 20 + 12;
				int acc_startx = (int) (p.getX() + w / 2) - acc_width_x / 2;
				int incx = 20;

				for (int n = 0; n < aal.size(); n++) {
					if (accl.get(n)) {
						g.setColor(Color.green);
					} else {
						g.setColor(Color.red);
					}
					g.fillArc(border_x + acc_startx + incx * n, border_y + (int) p.getY() - 16, 12, 12, 0, 360);
					g.setColor(Color.black);
					g.drawArc(border_x + acc_startx + incx * n, border_y + (int) p.getY() - 16, 12, 12, 0, 360);
				}
			}

			// if (tree.settledP(a)) {
			g.setColor(Color.black);
			g.drawRect((int) p.getX() + border_x, (int) p.getY() + border_y, (int) w, (int) h);
			// } else {
			// g.setColor(Color.magenta);
			// g.drawRect((int)p.getX()+border_x, (int)p.getY()+border_y, (int)w, (int)h);
			// }

			String name = argumentName(a);
			int t1w = g.getFontMetrics().stringWidth(a.m_agent);
			int t2w = g.getFontMetrics().stringWidth(name);
			g.drawString(a.m_agent, (int) (p.getX() + w / 2 - t1w / 2) + border_x, (int) (p.getY() - 2 + h / 2 - g.getFontMetrics().getHeight() / 2) + border_y);
			g.drawString(name, (int) (p.getX() + w / 2 - t2w / 2) + border_x, (int) (p.getY() + 2 + h / 2 + g.getFontMetrics().getHeight() / 2) + border_y);
		} catch (FeatureTermException ex) {
			ex.printStackTrace();
		}

	}

	/**
	 * Argument name.
	 * 
	 * @param a
	 *            the a
	 * @return the string
	 */
	public String argumentName(Argument a) {
		String name = null;
		if (a.m_type == Argument.ARGUMENT_RULE) {
			name = argumentIDs.get(a);
			if (name == null) {
				name = newArgumentName(a);
				argumentIDs.put(a, name);
			}
		} else {
			name = a.m_example.getName().toString();
		}
		return name;
	}

	/**
	 * New argument name.
	 * 
	 * @param a
	 *            the a
	 * @return the string
	 */
	public String newArgumentName(Argument a) {
		int Aindex = agents.indexOf(a.m_agent);
		if (Aindex == -1) {
			Aindex = 2;
		}
		int Cindex = 0;
		for (Argument a2 : argumentIDs.keySet()) {
			if (a2.m_agent.equals(a.m_agent)) {
				Cindex++;
			}
		}
		return "" + ((char) ('A' + Aindex)) + Cindex;
	}

	/**
	 * Argument coordinates.
	 * 
	 * @param a
	 *            the a
	 * @return the rectangle2 d
	 */
	public Rectangle2D argumentCoordinates(Argument a) {
		Point2D p = argumentPositions.get(a);
		if (p != null) {
			return new Rectangle2D.Double(p.getX() + border_x, p.getY() + border_y, argumentWidth, argumentHeight);
		}
		return null;
	}

	/**
	 * Adds the tree.
	 * 
	 * @param t
	 *            the t
	 * @param agent
	 *            the agent
	 * @param retracted
	 *            the retracted
	 */
	public void addTree(ArgumentationTree t, String agent, boolean retracted) {
		if (retracted) {
			retracted_trees.add(t);
		} else {
			List<ArgumentationTree> l = agent_trees.get(agent);
			if (l == null) {
				l = new LinkedList<ArgumentationTree>();
				agent_trees.put(agent, l);
			}
			l.add(t);
		}

		{
			List<Argument> l = new LinkedList<Argument>();
			l.addAll(t.getArguments());
			for (Argument parent : t.getArgumentsWithRetractedChildren()) {
				// System.err.println("Argument with retracted children...");
				for (Argument a : t.getRetractedChildren(parent)) {
					// System.err.println("Retracted children...");
					l.add(a);
				}
			}
			tree_arguments.put(t, l);
		}

		// clear all the tree posisions:
		argumentPositions.clear();
		;
		size_x = 0;
		size_y = 0;

		// Sort them according to their order:
		List<ArgumentationTree> allTrees = new LinkedList<ArgumentationTree>();
		List<String> orderedTrees = new LinkedList<String>();
		for (String agent2 : agents) {
			if (agent_trees.get(agent2) != null) {
				for (ArgumentationTree t2 : agent_trees.get(agent2)) {
					String name = argumentName(t2.getRoot());
					orderedTrees.add(name);
					allTrees.add(t2);
				}
			}
		}
		for (ArgumentationTree t2 : retracted_trees) {
			String name = argumentName(t2.getRoot());
			orderedTrees.add(name);
			allTrees.add(t2);
		}
		Collections.sort(orderedTrees);

		for (String name : orderedTrees) {
			ArgumentationTree tree = null;
			for (ArgumentationTree t2 : allTrees) {
				if (argumentName(t2.getRoot()).equals(name)) {
					tree = t2;
					break;
				}
			}
			if (tree == null) {
				System.err.println("Can't find argument with name " + name);
				continue;
			}
			for (Argument a : tree_arguments.get(tree)) {
				Point2D pos = relativeArgumentPosition(tree, a);
				argumentPositions.put(a, new Point2D.Double(size_x + pos.getX(), pos.getY()));
			}

			Rectangle2D r = treeSize(tree, tree.getRoot());
			size_x += argumentHSeparation + r.getWidth();
			size_y = (int) Math.max(size_y, r.getHeight());
		}

		setPreferredSize(new Dimension(size_x + border_x * 2, size_y + border_y * 2));
	}

	/**
	 * Relative argument position.
	 * 
	 * @param t
	 *            the t
	 * @param a
	 *            the a
	 * @return the point2 d
	 */
	Point2D relativeArgumentPosition(ArgumentationTree t, Argument a) {
		return relativeArgumentPosition(t, t.getRoot(), a);
	}

	/**
	 * Relative argument position.
	 * 
	 * @param t
	 *            the t
	 * @param parent
	 *            the parent
	 * @param a
	 *            the a
	 * @return the point2 d
	 */
	Point2D relativeArgumentPosition(ArgumentationTree t, Argument parent, Argument a) {
		if (a == parent) {
			return new Point2D.Double(0, 0);
		} else {
			List<Argument> children = new LinkedList<Argument>();
			List<Argument> l = t.getChildren(parent);
			if (l != null) {
				children.addAll(l);
			}
			l = t.getRetractedChildren(parent);
			if (l != null) {
				children.addAll(l);
			}

			if (children.size() == 0) {
				return null;
			} else {
				double offset_x = 0;
				double offset_y = argumentHeight + argumentVSeparation;
				List<Rectangle2D> childrenSizes = new LinkedList<Rectangle2D>();
				for (Argument child : children) {
					Point2D pos = relativeArgumentPosition(t, child, a);
					if (pos != null) {
						return new Point2D.Double(pos.getX() + offset_x, pos.getY() + offset_y);
					} else {
						Rectangle2D s = treeSize(t, child);
						offset_x += s.getWidth() + 16;
					}
				}
				return null;
			}
		}
	}

	/**
	 * Tree size.
	 * 
	 * @param t
	 *            the t
	 * @param root
	 *            the root
	 * @return the rectangle2 d
	 */
	Rectangle2D treeSize(ArgumentationTree t, Argument root) {
		List<Argument> children = new LinkedList<Argument>();
		List<Argument> l = t.getChildren(root);
		if (l != null) {
			children.addAll(l);
		}
		l = t.getRetractedChildren(root);
		if (l != null) {
			children.addAll(l);
		}

		if (children.size() == 0) {
			return new Rectangle2D.Double(0, 0, argumentWidth, argumentHeight);
		} else {
			double width = 0;
			double height = 0;
			List<Rectangle2D> childrenSizes = new LinkedList<Rectangle2D>();
			for (Argument child : children) {
				Rectangle2D s = treeSize(t, child);
				if (width == 0) {
					width += s.getWidth();
				} else {
					width += 16 + s.getWidth();
				}
				height = Math.max(height, s.getHeight());
			}
			height += argumentHeight + argumentVSeparation;
			return new Rectangle2D.Double(0, 0, width, height);
		}
	}
}
