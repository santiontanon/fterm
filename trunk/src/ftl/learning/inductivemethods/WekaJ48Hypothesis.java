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
  
 package ftl.learning.inductivemethods;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import weka.classifiers.Classifier;
import weka.core.Instance;
import ftl.base.bridges.NOOSToWeka;
import ftl.base.bridges.NOOSToWeka.ConversionRecord;
import ftl.base.core.FTKBase;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Ontology;
import ftl.base.core.Sort;
import ftl.base.core.TermFeatureTerm;
import ftl.base.utils.FeatureTermException;
import ftl.base.utils.Pair;
import ftl.learning.core.Prediction;

// TODO: Auto-generated Javadoc
/**
 * The Class WekaJ48Hypothesis.
 */
public class WekaJ48Hypothesis extends WekaHypothesis {

	/** The Constant DEBUG. */
	static final int DEBUG = 0;

	/** The m_generate justifications. */
	boolean m_generateJustifications = true;

	/**
	 * Instantiates a new weka j48 hypothesis.
	 * 
	 * @param c
	 *            the c
	 * @param record
	 *            the record
	 * @param generateJustifications
	 *            the generate justifications
	 */
	public WekaJ48Hypothesis(Classifier c, ConversionRecord record, boolean generateJustifications) {
		super(c, record);
		m_generateJustifications = generateJustifications;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * csic.iiia.ftl.learning.inductivemethods.WekaHypothesis#generatePrediction(csic.iiia.ftl.base.core.FeatureTerm,
	 * csic.iiia.ftl.base.core.FTKBase, boolean)
	 */
	public Prediction generatePrediction(FeatureTerm problem, FTKBase dm, boolean debug) throws Exception {
		Prediction p = new Prediction(problem);
		Instance inst;
		
		if(m_record.problemsToCases.containsKey(problem)){
			inst = NOOSToWeka.translateInstance(m_record.problemsToCases.get(problem), m_record.getAllCases(), m_record.getAllWekaCases());
		}else{
			inst = NOOSToWeka.translateInstance(problem, m_record.getAllCases(), m_record.getAllWekaCases());
		}
		
		double result = m_classifier.classifyInstance(inst);
		FeatureTerm solution = m_record.getSolutionMapping()[(int) result];
		if (DEBUG >= 1)
			System.out.println("Predicting... J48 predicts: " + solution.toStringNOOS(dm));

		if (m_generateJustifications) {
			/*
			 * Navigate the tree in the same way Weka does, and for each leaf that matches, create one entry in the
			 * prediction
			 */

			weka.classifiers.trees.J48 j48 = (weka.classifiers.trees.J48) m_classifier;
			String tree = j48.graph();

			generateJustifications(p, problem, solution, tree, dm, false);

			if (p.solutions.size() == 0) {
				System.out.flush();
				System.err.flush();
				System.err.println("No solutions predicted!");
				System.err.println("Weka's J48 had predicted: " + solution.toStringNOOS(dm));
				System.err.println(m_classifier.toString());
				System.err.println(problem.toStringNOOS(dm));
				System.err.flush();
				if (DEBUG >= 1)
					generateJustifications(p, problem, solution, tree, dm, true);
				System.out.flush();

				p.solutions.add(solution);
				p.support.put(solution, 1);
				Sort patternSort = problem.getSort();
				p.justifications.put(solution, (TermFeatureTerm) patternSort.createFeatureTerm());
			}
		} else {
			p.solutions.add(solution);
			p.support.put(solution, 1);
		}

		return p;
	} // Hypothesis::generate_prediction

	/**
	 * Generate justifications.
	 * 
	 * @param p
	 *            the p
	 * @param problem
	 *            the problem
	 * @param solution
	 *            the solution
	 * @param tree
	 *            the tree
	 * @param dm
	 *            the dm
	 * @param debug
	 *            the debug
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	private void generateJustifications(Prediction p, FeatureTerm problem, FeatureTerm solution, String tree, FTKBase dm, boolean debug) throws IOException,
			FeatureTermException {
		// System.out.println(tree);
		Pair<String, TermFeatureTerm> current = null, next = null;
		List<Pair<String, TermFeatureTerm>> stack = new LinkedList<Pair<String, TermFeatureTerm>>();
		Sort patternSort = problem.getSort();
		Ontology o = patternSort.getOntology();

		current = new Pair<String, TermFeatureTerm>("N0", (TermFeatureTerm) patternSort.createFeatureTerm());
		stack.add(current);

		while (!stack.isEmpty()) {
			current = stack.remove(0);
			String nodeString = searchNodeString(current.m_a, tree);

			/*
			 * // If the current pattern does not subsume the problem, do not continue looking: { if
			 * (isLeaf(nodeString)) { // find the solution confidence and support, and create a rule! StringTokenizer st
			 * = new StringTokenizer(nodeString," \""); st.nextToken(); st.nextToken(); String solutionString =
			 * st.nextToken();
			 * 
			 * // System.out.println("Leaf reached! '" + nodeString + "' -> '" + solutionString + "'"); FeatureTerm
			 * solutionValue = NOOSParser.parse(new RewindableInputStream(new StringBufferInputStream(solutionString)),
			 * dm, o);
			 * 
			 * if (solution.equals(solutionValue)) { p.solutions.add(solutionValue); p.support.put(solutionValue,1);
			 * p.justifications.put(solutionValue, current.m_b); if (DEBUG>=1 || debug) System.out.println("Predicted: "
			 * + solutionString);
			 * 
			 * // } else { // System.err.println("Solution mismatch!"); //
			 * System.err.println("according to weka it is '" + solution.toStringNOOS(dm) + "'"); //
			 * System.err.println("according to AWorld it is '" + solutionValue.toStringNOOS(dm) + "'"); //
			 * System.out.println("pattern is:" + current.m_b.toStringNOOS(dm)); // System.err.println("term is:" +
			 * problem.toStringNOOS(dm)); } else { if (DEBUG>=1 || debug) System.out.println("Predicted: " +
			 * solutionString + " but not accounted, since it is different from what weka's J48 predicted..."); }
			 * 
			 * } else { String feature = nodeFeature(nodeString);
			 * 
			 * if (feature.startsWith("description.")) feature = feature.substring(12);
			 * 
			 * // System.out.println("Feature '" + feature + "'");
			 * 
			 * List<String> nextNodes = nextNodes(current.m_a,tree);
			 * 
			 * for(String nextNode:nextNodes) { int i1 = nextNode.indexOf(">"); int i2 = nextNode.indexOf(" "); String
			 * nodeID = nextNode.substring(i1+1, i2); String test = nodeFeature(nextNode);
			 * 
			 * FeatureTerm valueToAdd = null;
			 * 
			 * // System.out.println("next node '" + nodeID + "' for '" + feature + " " + test + "'");
			 * 
			 * if (test.startsWith("<= ")) { Sort fs = patternSort.featureSort(feature); if (fs.get().equals("float") ||
			 * fs.get().equals("integer")) { Sort specialSort = o.getSort("float-interval"); valueToAdd = new
			 * SpecialFeatureTerm((Symbol)null,specialSort,new FloatInterval(null,null));
			 * ((SpecialFeatureTerm)valueToAdd).defineFeatureValue(new Symbol("max"), new
			 * FloatFeatureTerm(Float.parseFloat(test.substring(3)),o)); ((SpecialFeatureTerm)valueToAdd).takeValues();
			 * } else { System.err.println("Interval special sort for '" + fs.get() + "' does not exist!"); } } else if
			 * (test.startsWith("> ")) { Sort fs = patternSort.featureSort(feature); if (fs.get().equals("float") ||
			 * fs.get().equals("integer")) { Sort specialSort = o.getSort("float-interval"); valueToAdd = new
			 * SpecialFeatureTerm((Symbol)null,specialSort,new FloatInterval(null,null));
			 * ((SpecialFeatureTerm)valueToAdd).defineFeatureValue(new Symbol("min"), new
			 * FloatFeatureTerm(Float.parseFloat(test.substring(2)),o)); ((SpecialFeatureTerm)valueToAdd).takeValues();
			 * } else { System.err.println("Interval special sort for '" + fs.get() + "' does not exist!"); } } if
			 * (test.startsWith("= ")) { valueToAdd = NOOSParser.parse(new RewindableInputStream(new
			 * StringBufferInputStream(test.substring(2))), dm, patternSort.featureSort(feature), o); //
			 * System.out.println("'" + test.substring(2) + "' -> '" + value.toStringNOOS(dm) + "'"); }
			 * 
			 * if (valueToAdd!=null) { Sort specialSort = o.getSort("if-present"); SpecialFeatureTerm wrapperValue = new
			 * SpecialFeatureTerm((Symbol)null,specialSort,new IFPresent(null)); wrapperValue.defineFeatureValue(new
			 * Symbol("value"), valueToAdd); wrapperValue.takeValues(); next = new
			 * Pair<String,TermFeatureTerm>(nodeID,(TermFeatureTerm)current.m_b.clone(dm, o));
			 * next.m_b.defineFeatureValue(new Symbol(feature), wrapperValue);
			 * 
			 * if (debug) System.out.println("generateJustifications: new pattern generated: \n" +
			 * next.m_b.toStringNOOS(dm));
			 * 
			 * if (next.m_b.subsumes(problem)) { if (debug)
			 * System.out.println("generateJustifications: subsumes problem"); stack.add(0,next); } else { if (debug)
			 * System.out.println("generateJustifications: does not subsume problem"); } } } } }
			 */
		}
	}

	/**
	 * Search node string.
	 * 
	 * @param node
	 *            the node
	 * @param tree
	 *            the tree
	 * @return the string
	 */
	private String searchNodeString(String node, String tree) {
		StringTokenizer st = new StringTokenizer(tree, "\n");
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (token.startsWith(node + " "))
				return token;
		}
		return null;
	}

	/**
	 * Checks if is leaf.
	 * 
	 * @param nodeString
	 *            the node string
	 * @return true, if is leaf
	 */
	private boolean isLeaf(String nodeString) {
		if (nodeString.indexOf("shape=box style=filled") != -1)
			return true;
		return false;
	}

	/**
	 * Node feature.
	 * 
	 * @param nodeString
	 *            the node string
	 * @return the string
	 */
	private String nodeFeature(String nodeString) {
		StringTokenizer st = new StringTokenizer(nodeString, "\"");
		st.nextToken();
		return st.nextToken();
	}

	/**
	 * Next nodes.
	 * 
	 * @param node
	 *            the node
	 * @param tree
	 *            the tree
	 * @return the list
	 */
	private List<String> nextNodes(String node, String tree) {
		List<String> nextNodes = new LinkedList<String>();
		StringTokenizer st = new StringTokenizer(tree, "\n");
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (token.startsWith(node + "->")) {
				nextNodes.add(token);
			}
		}
		return nextNodes;
	}
}
