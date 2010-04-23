package fterms.learning.propositional;

import java.io.IOException;
import java.io.StringBufferInputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import util.Pair;
import util.RewindableInputStream;
import weka.classifiers.Classifier;
import weka.core.Instance;
import fterms.FTKBase;
import fterms.FeatureTerm;
import fterms.FloatFeatureTerm;
import fterms.NOOSParser;
import fterms.Ontology;
import fterms.Sort;
import fterms.SpecialFeatureTerm;
import fterms.Symbol;
import fterms.TermFeatureTerm;
import fterms.exceptions.FeatureTermException;
import fterms.learning.Prediction;
import fterms.learning.propositional.NOOSToWeka.ConversionRecord;
import fterms.specialterms.FloatInterval;
import fterms.specialterms.IFPresent;

public class WekaJ48Hypothesis extends WekaHypothesis {	
	static final int DEBUG = 0; 
	boolean m_generateJustifications = true;
	
	public WekaJ48Hypothesis(Classifier c,ConversionRecord record,boolean generateJustifications) {
		super(c,record);
		m_generateJustifications = generateJustifications;
	}

	public Prediction generatePrediction(FeatureTerm problem,FTKBase dm, boolean debug) throws Exception
	{
		Prediction p = new Prediction(problem);
		
		Instance inst = NOOSToWeka.translateInstance(m_record.problemsToCases.get(problem), m_record.allCases, m_record.allWekaCases);
		double result = m_classifier.classifyInstance(inst);
		FeatureTerm solution = m_record.solutionMapping[(int)result];	
		if (DEBUG>=1) System.out.println("Predicting... J48 predicts: " + solution.toStringNOOS(dm));
		
		if (m_generateJustifications) {
			/*
			 * Navigate the tree in the same way Weka does, and for each leaf that matches, create one entry in the prediction
			 */
			
			weka.classifiers.trees.J48 j48 = (weka.classifiers.trees.J48)m_classifier;
			String tree = j48.graph();

			generateJustifications(p,problem,solution,tree,dm,false);
			
			if (p.solutions.size()==0) {
				System.out.flush();
				System.err.flush();
				System.err.println("No solutions predicted!");
				System.err.println("Weka's J48 had predicted: " + solution.toStringNOOS(dm));
				System.err.println(m_classifier.toString());
				System.err.println(problem.toStringNOOS(dm));
				System.err.flush();
				if (DEBUG>=1) generateJustifications(p,problem,solution,tree,dm,true);
				System.out.flush();

				p.solutions.add(solution);
				p.support.put(solution,1);			
				Sort patternSort = problem.getSort();
				p.justifications.put(solution,(TermFeatureTerm)patternSort.createFeatureTerm());			
			}
		} else {
			p.solutions.add(solution);
			p.support.put(solution,1);			
		}
	
		
		return p;
	} // Hypothesis::generate_prediction  
	
	
	private void generateJustifications(Prediction p,FeatureTerm problem,FeatureTerm solution,String tree,FTKBase dm,boolean debug) throws IOException, FeatureTermException {
//		System.out.println(tree);
		Pair<String,TermFeatureTerm> current = null, next = null;
		List<Pair<String,TermFeatureTerm>> stack = new LinkedList<Pair<String,TermFeatureTerm>>();
		Sort patternSort = problem.getSort();
		Ontology o = patternSort.getOntology();
		
		current = new Pair<String,TermFeatureTerm>("N0",(TermFeatureTerm)patternSort.createFeatureTerm());
		stack.add(current);

		while(!stack.isEmpty()) {
			current = stack.remove(0);
			String nodeString = searchNodeString(current.m_a,tree);
			
			// If the current pattern does not subsume the problem, do not continue looking:
			{
				if (isLeaf(nodeString)) {
					// find the solution confidence and support, and create a rule!
					StringTokenizer st = new StringTokenizer(nodeString," \"");
					st.nextToken();
					st.nextToken();
					String solutionString = st.nextToken();
					
//					System.out.println("Leaf reached! '" + nodeString + "' -> '" + solutionString + "'");
					FeatureTerm solutionValue = NOOSParser.parse(new RewindableInputStream(new StringBufferInputStream(solutionString)), dm, o);
					
					if (solution.equals(solutionValue)) {
						p.solutions.add(solutionValue);
						p.support.put(solutionValue,1);
						p.justifications.put(solutionValue, current.m_b);
						if (DEBUG>=1 || debug) System.out.println("Predicted: " + solutionString);

					//						} else {
//						System.err.println("Solution mismatch!");
//						System.err.println("according to weka it is '" + solution.toStringNOOS(dm) + "'");
//						System.err.println("according to AWorld it is '" + solutionValue.toStringNOOS(dm) + "'");
//						System.out.println("pattern is:" + current.m_b.toStringNOOS(dm));
//						System.err.println("term is:" + problem.toStringNOOS(dm));
					} else {
						if (DEBUG>=1 || debug) System.out.println("Predicted: " + solutionString + " but not accounted, since it is different from what weka's J48 predicted...");
					}
					
				} else {
					String feature = nodeFeature(nodeString);
					
					if (feature.startsWith("description.")) feature = feature.substring(12);
					
//					System.out.println("Feature '" + feature + "'");
					
					List<String> nextNodes = nextNodes(current.m_a,tree);
					
					for(String nextNode:nextNodes) {
						int i1 = nextNode.indexOf(">");
						int i2 = nextNode.indexOf(" ");
						String nodeID = nextNode.substring(i1+1, i2);
						String test = nodeFeature(nextNode);
						
						FeatureTerm valueToAdd = null;

//						System.out.println("next node '" + nodeID + "' for '" + feature + " " + test + "'");
						
						if (test.startsWith("<= ")) {
							Sort fs = patternSort.featureSort(feature); 
							if (fs.get().equals("float") || fs.get().equals("integer")) {
								Sort specialSort = o.getSort("float-interval");
								valueToAdd = new SpecialFeatureTerm((Symbol)null,specialSort,new FloatInterval(null,null));
								((SpecialFeatureTerm)valueToAdd).defineFeatureValue(new Symbol("max"), new FloatFeatureTerm(Float.parseFloat(test.substring(3)),o));
								((SpecialFeatureTerm)valueToAdd).takeValues();
							} else {
								System.err.println("Interval special sort for '" + fs.get() + "' does not exist!");
							}
						} else if (test.startsWith("> ")) {
							Sort fs = patternSort.featureSort(feature); 
							if (fs.get().equals("float") || fs.get().equals("integer")) {
								Sort specialSort = o.getSort("float-interval");
								valueToAdd = new SpecialFeatureTerm((Symbol)null,specialSort,new FloatInterval(null,null));
								((SpecialFeatureTerm)valueToAdd).defineFeatureValue(new Symbol("min"), new FloatFeatureTerm(Float.parseFloat(test.substring(2)),o));
								((SpecialFeatureTerm)valueToAdd).takeValues();
							} else {
								System.err.println("Interval special sort for '" + fs.get() + "' does not exist!");
							}
						} if (test.startsWith("= ")) {
							valueToAdd = NOOSParser.parse(new RewindableInputStream(new StringBufferInputStream(test.substring(2))), dm, patternSort.featureSort(feature), o);
//							System.out.println("'" + test.substring(2) + "' -> '" + value.toStringNOOS(dm) + "'");
						}

						if (valueToAdd!=null) {
							Sort specialSort = o.getSort("if-present");
							SpecialFeatureTerm wrapperValue = new SpecialFeatureTerm((Symbol)null,specialSort,new IFPresent(null));
							wrapperValue.defineFeatureValue(new Symbol("value"), valueToAdd);
							wrapperValue.takeValues();
							next = new Pair<String,TermFeatureTerm>(nodeID,(TermFeatureTerm)current.m_b.clone(dm, o));
							next.m_b.defineFeatureValue(new Symbol(feature), wrapperValue);
							
							if (debug) 	System.out.println("generateJustifications: new pattern generated: \n" + next.m_b.toStringNOOS(dm));
							
							if (next.m_b.subsumes(problem)) {
								if (debug) 	System.out.println("generateJustifications: subsumes problem");
								stack.add(0,next);
							} else {
								if (debug) 	System.out.println("generateJustifications: does not subsume problem");								
							}
						}
					}
				}
			}
		}		
	}
	
	
	private String searchNodeString(String node, String tree) {
		StringTokenizer st = new StringTokenizer(tree,"\n");
		while(st.hasMoreTokens()) {
			String token = st.nextToken();
			if (token.startsWith(node + " ")) return token;
		}
		return null;
	}
	
	private boolean isLeaf(String nodeString) {
		if (nodeString.indexOf("shape=box style=filled")!=-1) return true;
		return false;
	}
	
	private String nodeFeature(String nodeString) {
		StringTokenizer st = new StringTokenizer(nodeString,"\"");
		st.nextToken();
		return st.nextToken();
	}
	
	private List<String> nextNodes(String node, String tree) {
		List<String> nextNodes = new LinkedList<String>();
		StringTokenizer st = new StringTokenizer(tree,"\n");
		while(st.hasMoreTokens()) {
			String token = st.nextToken();
			if (token.startsWith(node + "->")) {
				nextNodes.add(token);
			}
		}
		return nextNodes;
	}
}
