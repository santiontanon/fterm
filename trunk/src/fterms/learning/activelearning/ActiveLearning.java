package fterms.learning.activelearning;

import java.util.LinkedList;
import java.util.List;

import util.Pair;
import fterms.FTKBase;
import fterms.FeatureTerm;
import fterms.Ontology;
import fterms.Path;
import fterms.exceptions.FeatureTermException;
import fterms.learning.Hypothesis;
import fterms.learning.InductiveLearner;

public abstract class ActiveLearning {
	
	InductiveLearner m_learner = null;
	
	public List<Double> activeLearningExperiment(List<FeatureTerm> a_training,List<FeatureTerm> test,List<FeatureTerm> differentSolutions,InductiveLearner l,int amount,Path dp,Path sp, Ontology o, FTKBase dm) throws Exception {
		List<FeatureTerm> training = new LinkedList<FeatureTerm>();
		List<FeatureTerm> available = new LinkedList<FeatureTerm>();
		available.addAll(a_training);
		List<Double> results = new LinkedList<Double>();
		
		m_learner = l;
		
		System.out.println("activeLearningExperiment for " + this + " with method " + m_learner.getClass().getSimpleName() + " started");
		
		while(!available.isEmpty()) {
			training = selectTrainingExamples(training,available,differentSolutions,dp,sp,o,dm,amount);
			available.removeAll(training);
			
			Hypothesis h = m_learner.generateHypothesis(training, dp, sp, o, dm);
			float acc = h.evaluate(test, dm, sp, dp, false);
//			System.out.println(training.size() + " / " + available.size() + " examples accuracy is " + acc);
			results.add(new Double(acc));
		}
		
		return results;
	}
	
	
	public List<FeatureTerm> selectTrainingExamples(List<FeatureTerm> initialSet,List<FeatureTerm> additional,List<FeatureTerm> differentSolutions, Path dp, Path sp, Ontology o, FTKBase dm,int amount) throws Exception {
		List<FeatureTerm> selected = new LinkedList<FeatureTerm>();
		selected.addAll(initialSet);

		List<Pair<FeatureTerm,Double>> utilities = examplesUtility(initialSet,additional, differentSolutions,dp,sp,o,dm);
					
		// Sort:
		{
			int l = utilities.size();
			boolean change = true;
			while(change) {
				change = false;
				for(int i = 0;i<l-1;i++) {
					Pair<FeatureTerm,Double> u1 = utilities.get(i);
					Pair<FeatureTerm,Double> u2 = utilities.get(i+1);
					
					if (u1.m_b<u2.m_b) {
						FeatureTerm tmp1 = u1.m_a;
						Double tmp2 = u1.m_b;
						u1.m_a = u2.m_a;
						u1.m_b = u2.m_b;
						u2.m_a = tmp1;
						u2.m_b = tmp2;
						change = true;
					}
				}
			}
		}
		
		while(utilities.size()>amount) utilities.remove(utilities.size()-1);
		for(Pair<FeatureTerm,Double> u:utilities) {
			selected.add(u.m_a);
		}
		
		return selected;
	}
	
	public abstract List<Pair<FeatureTerm,Double>> examplesUtility(List<FeatureTerm> initialSet,List<FeatureTerm> examples,List<FeatureTerm> differentSolutions, Path dp, Path sp, Ontology o, FTKBase dm) throws FeatureTermException, Exception;
}
