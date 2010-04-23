package fterms.learning.activelearning;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import util.Pair;
import fterms.FTKBase;
import fterms.FeatureTerm;
import fterms.Ontology;
import fterms.Path;
import fterms.exceptions.FeatureTermException;
import fterms.learning.Hypothesis;
import fterms.learning.Prediction;

public class QueryByCommittee extends ActiveLearning {

	protected int m_nClassifiers = 3; 
	
	public QueryByCommittee(int nClassifiers) {
		m_nClassifiers = nClassifiers;
	}
	
	public String toString() {
		return "QueryByCommittee(" + m_nClassifiers + ")";
	}
	
	public List<Pair<FeatureTerm,Double>> examplesUtility(List<FeatureTerm> allTraining,
			List<FeatureTerm> examples,List<FeatureTerm> differentSolutions, Path dp, Path sp, Ontology o, FTKBase dm) throws Exception {
		
		// Use bagging to create a committee:
		List<List<FeatureTerm>> trainingSets = new LinkedList<List<FeatureTerm>>();
		List<Hypothesis> hypotheses = new LinkedList<Hypothesis>();
		Random r = new Random();
		
		for(int i = 0;i<m_nClassifiers;i++) {
			List<FeatureTerm> trainingSet = new LinkedList<FeatureTerm>();
			trainingSet.addAll(allTraining);
			
			int toRemove = (int)(allTraining.size()*0.33);
			
			for(int j = 0;j<toRemove;j++) {
				trainingSet.remove(r.nextInt(trainingSet.size()));
			}
			trainingSets.add(trainingSet);
		}
		
		// Train the classifiers:
		for(List<FeatureTerm> trainingSet:trainingSets) {
			Hypothesis h = m_learner.generateHypothesis(trainingSet,dp,sp,o,dm);
			hypotheses.add(h);
		}
		
		// Measure disagreement:
		{
			List<Pair<FeatureTerm,Double>> ret = new LinkedList<Pair<FeatureTerm,Double>>();
			
			for(FeatureTerm example:examples) {
				List<Prediction> predictions = new LinkedList<Prediction>();
				for(Hypothesis h:hypotheses) {
					Prediction p = h.generatePrediction(example.readPath(dp), dm, false);
					if (p!=null) predictions.add(p);
				}
				double d = disagreement(predictions,trainingSets,differentSolutions,dp,sp,dm);
				ret.add(new Pair<FeatureTerm,Double>(example,d));
//				System.out.println(d);
			}
			

			return ret;
		}		
	}

	
	public double disagreement(List<Prediction> predictions,List<List<FeatureTerm>> trainingSets,List<FeatureTerm> differentSolutions,Path dp,Path sp,FTKBase dm) throws FeatureTermException {
		List<FeatureTerm> solutions = new LinkedList<FeatureTerm>();
		List<Pair<FeatureTerm,Integer>> solutionCount = new LinkedList<Pair<FeatureTerm,Integer>>();
		
		for(Prediction p:predictions) solutions.add(p.getSolution());
		
		for(FeatureTerm solution:solutions) {
			Pair<FeatureTerm,Integer> count = null;
			for(Pair<FeatureTerm,Integer> tmp:solutionCount) {
				if (tmp.m_a.equals(solution)) {
					count = tmp;
					break;
				}
			}
			if (count==null) {
				count = new Pair<FeatureTerm,Integer>(solution,0);
				solutionCount.add(count);
			}
			count.m_b+=1;
		}
		
		// Sort them:
		{
			int l = solutionCount.size();
			boolean change = true;
			while(change) {
				change = false;
				for(int i = 0;i<l-1;i++) {
					Pair<FeatureTerm,Integer> u1 = solutionCount.get(i);
					Pair<FeatureTerm,Integer> u2 = solutionCount.get(i+1);
					
					if (u1.m_b<u2.m_b) {
						FeatureTerm tmp1 = u1.m_a;
						Integer tmp2 = u1.m_b;
						u1.m_a = u2.m_a;
						u1.m_b = u2.m_b;
						u2.m_a = tmp1;
						u2.m_b = tmp2;
						change = true;
					}
				}
			}
		}
		
		int c1 = 0;
		int c2 = 0;
		
		if (solutionCount.size()>=1) c1 = solutionCount.get(0).m_b; 
		if (solutionCount.size()>=2) c2 = solutionCount.get(1).m_b; 
				
		return solutions.size()-(c1-c2);
	}
}
