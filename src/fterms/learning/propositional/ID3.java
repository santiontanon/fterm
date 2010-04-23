package fterms.learning.propositional;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import util.Pair;
import fterms.FTKBase;
import fterms.FeatureTerm;
import fterms.Ontology;
import fterms.Path;
import fterms.Sort;
import fterms.Symbol;
import fterms.TermFeatureTerm;
import fterms.exceptions.FeatureTermException;
import fterms.learning.Hypothesis;
import fterms.learning.InductiveLearner;
import fterms.learning.InformationMeasurement;
import fterms.learning.Rule;
import fterms.learning.RuleHypothesis;

/*
 * ID3 with a single modification: missing values are handled as in C4.5 -> they are assumed to be a value: null
 */

public class ID3 extends InductiveLearner {
	public static int DEBUG = 0; 
	public static int handleMissingValuesMethod = 0;	// 0: ignore, 1: replicate example in all branches

	// The first element of this list is the root:
	ID3Node m_root;
	
	public Hypothesis generateHypothesis(List<FeatureTerm> examples,Path dp,Path sp,Ontology o,FTKBase dm) throws Exception {
		return learnHypothesis(examples,dp,sp,o,dm);
	}
	
	public static Hypothesis learnHypothesis(Collection<FeatureTerm> examples,Path dp,Path sp,Ontology o,FTKBase dm) throws Exception {
		ID3 tree = learn(examples,dp,sp,o,dm);
		
//		System.out.println(tree.toString(dm));
		
		// Convert to a Hypothesis object:
		if (examples.size()==0) {
			return new RuleHypothesis();
		}
		RuleHypothesis h = generateRules(tree.m_root, examples.iterator().next().readPath(dp).getSort());
		
		// Compute the default solution:
		{
			HashMap<FeatureTerm,Integer> classifiedExamples = new HashMap<FeatureTerm,Integer>(); 
			FeatureTerm best = null;
			int bestCount = 0;
			
			for(FeatureTerm example:examples) {
				FeatureTerm s = example.readPath(sp);
				Integer count = classifiedExamples.get(s);
				
				if (count==null) count = 0;
				classifiedExamples.put(s,count+1);
				
				if (best==null || count>bestCount) {
					best = s;
					bestCount = count;
				}
			}
			h.setDefaultSolution(best);
		}
			
		return h;
	}
	
	
	private static RuleHypothesis generateRules(ID3Node node, Sort descriptionSort) throws Exception {		
		if (node.leaf) {
			RuleHypothesis h = new RuleHypothesis();
			for(FeatureTerm s:node.solutions.keySet()) {
				FeatureTerm pattern = descriptionSort.createFeatureTerm();
				int n = node.solutions.get(s);
				h.addRule(pattern, s, (float)(n/((float)(1 + n))), n);
			}
			return h;
		} else {
			RuleHypothesis h = new RuleHypothesis();

			for(FeatureTerm v:node.nextNodes.keySet()) {
				ID3Node nextNode = node.nextNodes.get(v);
				RuleHypothesis tmpHypothesis = generateRules(nextNode, descriptionSort);

				for(Rule rule:tmpHypothesis.getRules()) {
					if (v!=null) ((TermFeatureTerm)(rule.pattern)).defineFeatureValue(node.feature, v);
					h.addRule(rule);
				}
				
			}
			return h;
		}
	}


	public static ID3 learn(Collection<FeatureTerm> examples,Path dp,Path sp,Ontology o,FTKBase dm) throws FeatureTermException {
		HashMap<FeatureTerm,List<FeatureTerm>> classifiedExamples = new HashMap<FeatureTerm,List<FeatureTerm>>(); 
		Sort ds = null;
		
		// Divide examples by classes:
		for(FeatureTerm example:examples) {
			FeatureTerm s = example.readPath(sp);
			if (ds==null) {
				FeatureTerm d = example.readPath(dp);
				ds = d.getSort();
			}
			List<FeatureTerm> l = classifiedExamples.get(s);
			
			if (l==null) {
				l = new LinkedList<FeatureTerm>();
				classifiedExamples.put(s,l);
			}
			l.add(example);
		}

		ID3 tree = new ID3();
		tree.m_root = tree.new ID3Node();	
		
		if (examples.size()==0) return tree;
		
		if (DEBUG>=2) {
			for(FeatureTerm s:classifiedExamples.keySet()) {
				System.out.print(s.toStringNOOS(dm) + " -> ");
				for(FeatureTerm ex:classifiedExamples.get(s)) {
					System.out.print(ex.getName() + " ");
				}
				System.out.println("");
			}
		}
		
		learnInternal(tree.m_root,tree,new LinkedList<Symbol>(),ds,classifiedExamples,dp,sp,o,dm);
		
		if (DEBUG>=1) System.out.println(tree.toString(dm));
		
		return tree;
	}

	
	static void learnInternal(ID3Node node, ID3 tree, LinkedList<Symbol> usedFeatures, Sort ds, HashMap<FeatureTerm,List<FeatureTerm>> classifiedExamples,Path dp,Path sp,Ontology o,FTKBase dm) throws FeatureTermException {

		/*
		System.out.println("Distribution:");
		for(FeatureTerm solution:classifiedExamples.keySet()) {
			System.out.println(solution.toStringNOOS(dm) + " -> " + classifiedExamples.get(solution).size());
		}
		*/
		
		// Check if we are done:
		{
			boolean createLeafNode = true;
			FeatureTerm onlyOneClass = null;
			for(FeatureTerm solution:classifiedExamples.keySet()) {
				List<FeatureTerm> l = classifiedExamples.get(solution);
				
				if (l!=null && l.size()>0) {
					if (onlyOneClass == null) {
						onlyOneClass = solution;
					} else {
						createLeafNode = false;
						break;
					}
				}
			}
			
			if (createLeafNode) {
				node.leaf = true;
				node.solutions = new HashMap<FeatureTerm,Integer>();
				node.solutions.put(onlyOneClass,classifiedExamples.get(onlyOneClass).size());
				return;
			}
		}
		
		// List available features:
		List<Symbol> availableFeatures = new LinkedList<Symbol>();
		for(Symbol feature:ds.getFeatures()) {
			if (!usedFeatures.contains(feature)) {
				availableFeatures.add(feature);
			}
		}
		
		if (availableFeatures.size()==0) {
			// If no more features, just create a leaf node:
			node.leaf = true;
			node.solutions = new HashMap<FeatureTerm,Integer>();
			for(FeatureTerm solution:classifiedExamples.keySet()) {
				List<FeatureTerm> l = classifiedExamples.get(solution);
				if (l!=null && l.size()>0) {
					node.solutions.put(solution,l.size());
				}
			}
			return;
		} else {
			// Select best feature:
			Symbol bestFeature = null;
			double bestHeuristic = 0.0;
			HashMap<FeatureTerm, HashMap<FeatureTerm,List<FeatureTerm>>> best_split = null;
			
			for(Symbol feature:availableFeatures) {
				Pair<Float,HashMap<FeatureTerm, HashMap<FeatureTerm,List<FeatureTerm>>>> heuristic_p = informationGain(feature,classifiedExamples,dp);
				if (bestFeature==null || heuristic_p.m_a>bestHeuristic) {
					bestFeature = feature;
					bestHeuristic = heuristic_p.m_a;
					best_split = heuristic_p.m_b;
				}
			}
			
			if (DEBUG>=1) System.out.println("Selected: " + bestFeature);
			
			node.feature = bestFeature;
			node.nextNodes = new HashMap<FeatureTerm,ID3Node>();
			for(FeatureTerm v:best_split.keySet()) {
				ID3Node newNode = tree.new ID3Node();
				node.nextNodes.put(v,newNode);
				
				usedFeatures.add(bestFeature);
				learnInternal(newNode, tree, usedFeatures, ds, best_split.get(v), dp, sp, o, dm);
				usedFeatures.remove(bestFeature);
			}
			
		}
	}
	
	static Pair<Float,HashMap<FeatureTerm, HashMap<FeatureTerm,List<FeatureTerm>>>> informationGain(Symbol feature,HashMap<FeatureTerm,List<FeatureTerm>> classifiedExamples, Path dp) throws FeatureTermException {
		double initial_e = entropy(classifiedExamples);
		HashMap<FeatureTerm, HashMap<FeatureTerm,List<FeatureTerm>>> split = new HashMap<FeatureTerm, HashMap<FeatureTerm,List<FeatureTerm>>>();
		List<Pair<FeatureTerm,FeatureTerm>> examplesWithMissingValue = null;
		
		for(FeatureTerm solution:classifiedExamples.keySet()) {
			for(FeatureTerm example:classifiedExamples.get(solution)) {
				FeatureTerm d = example.readPath(dp);
				FeatureTerm v = d.featureValue(feature);
				HashMap<FeatureTerm,List<FeatureTerm>> classifiedExamplesForValue = null;

				if (v==null && handleMissingValuesMethod!=0) {
					switch(handleMissingValuesMethod) {
					case 1:
						if (examplesWithMissingValue==null) {
							examplesWithMissingValue = new LinkedList<Pair<FeatureTerm,FeatureTerm>>();
						}
						examplesWithMissingValue.add(new Pair<FeatureTerm,FeatureTerm>(example,solution));
						break;
					default:
						System.err.println("ID3.informationGain: handleMissingValuesMethod has an incorrect value!");
					}
					
				} else {
					classifiedExamplesForValue = split.get(v);
					
					if (classifiedExamplesForValue==null) {
						classifiedExamplesForValue = new HashMap<FeatureTerm,List<FeatureTerm>>();
						split.put(v, classifiedExamplesForValue);
					}
	
					List<FeatureTerm> examplesForsolution = classifiedExamplesForValue.get(solution);
					if (examplesForsolution==null) {
						examplesForsolution = new LinkedList<FeatureTerm>();
						classifiedExamplesForValue.put(solution,examplesForsolution);
					}
					examplesForsolution.add(example);
				}
			}
		}
		
		switch(handleMissingValuesMethod) {
		case 0:
			break;
		case 1:
			if (examplesWithMissingValue!=null) {
				for(Pair<FeatureTerm,FeatureTerm> example_solution:examplesWithMissingValue) {
					for(FeatureTerm v:split.keySet()) {						
						List<FeatureTerm> examplesForsolution = split.get(v).get(example_solution.m_b);
						if (examplesForsolution==null) {
							examplesForsolution = new LinkedList<FeatureTerm>();
							split.get(v).put(example_solution.m_b,examplesForsolution);
						}
						examplesForsolution.add(example_solution.m_a);
					}
				}
			}
			break;
		default:
			System.err.println("ID3.informationGain: handleMissingValuesMethod has an incorrect value!");
		}
				
		float total_e = 0.0f;
		int total_examples = 0;
		float average_e = 0.0f;
		
		for(FeatureTerm v:split.keySet()) {				
			int nex = 0;
			for(FeatureTerm solution:split.get(v).keySet()) nex+=split.get(v).get(solution).size();
			total_e += entropy(split.get(v))*nex;
			total_examples += nex;
		}
		
		if (total_examples>0) {
			average_e = total_e/total_examples;
		} else {
			average_e = 0;
		}
		
		if (DEBUG>=2) System.out.println("informationGain: " + feature.get() + " -> " + (initial_e - average_e));
		
		return new Pair<Float,HashMap<FeatureTerm, HashMap<FeatureTerm,List<FeatureTerm>>>>((float)(initial_e - total_e/total_examples),split);
	}
	
	static double entropy(HashMap<FeatureTerm, List<FeatureTerm>> classifiedExamples) {
		List<FeatureTerm> solutions = new LinkedList<FeatureTerm>();
		solutions.addAll(classifiedExamples.keySet());
		int distribution[]=new int[solutions.size()];
		for(FeatureTerm solution:solutions) {
			distribution[solutions.indexOf(solution)] = classifiedExamples.get(solution).size();
		}
		
		return InformationMeasurement.entropy(solutions.size(), distribution);
	}
	
	public String toString(FTKBase dm) {
		return toStringInternal(m_root,0,dm);
	}
	
	public String toStringInternal(ID3Node n,int tabs,FTKBase dm) {
		String out = "";
		
		if (n.leaf) {
			for(int i = 0;i<tabs;i++) out+=" ";
			out+="Leaf -> ";
			for(FeatureTerm s:n.solutions.keySet()) {
				out+=s.toStringNOOS(dm) + ":" + n.solutions.get(s) + " ";
			}
			out+="\n";
		} else {
			for(FeatureTerm v:n.nextNodes.keySet()) {
				for(int i = 0;i<tabs;i++) out+=" ";
				if (v==null) {
					out+="< " + n.feature.get() + " = null >\n";
				} else {
					out+="< " + n.feature.get() + " = " + v.toStringNOOS(dm) + " >\n";					
				}
				out+=toStringInternal(n.nextNodes.get(v),tabs+2,dm);
			}
		}
		
		
		return out;
	}

	class ID3Node {
		boolean leaf;
		
		// For leaf nodes:
		HashMap<FeatureTerm,Integer> solutions;
		
		// For intermediate nodes:
		Symbol feature;
		HashMap<FeatureTerm,ID3Node> nextNodes;
	}		
}
