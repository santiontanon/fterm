package fterms.learning;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import fterms.FTKBase;
import fterms.FeatureTerm;


public class Prediction {
	public FeatureTerm problem = null;
	public List<FeatureTerm> solutions = new LinkedList<FeatureTerm>();
	public HashMap<FeatureTerm,FeatureTerm> justifications = new HashMap<FeatureTerm,FeatureTerm>();
	public HashMap<FeatureTerm,Integer> support = new HashMap<FeatureTerm,Integer>();
	
	public Prediction() {
		
	}
	
	public Prediction(FeatureTerm p) {
		problem = p;
	}
	
	public String toString(FTKBase dm) {
		String tmp;

		tmp = ("Prediction: number of possible solutions (" + solutions.size() + "): ----------------------\n");
		for(FeatureTerm solution:solutions) {
			FeatureTerm justification = justifications.get(solution);
			tmp+= "- Justification for " + solution.toStringNOOS(dm) + "(support:" + support.get(solution) + ")\n";
			tmp+= (justification!=null ? justification.toStringNOOS(dm):"-") + "\n";
		} // while  
		
		return tmp;
	}
	
	public float getScore(FeatureTerm realSolution) {
		float total = 0,correct = 0;
		
		if (support.get(realSolution)!=null) correct+=support.get(realSolution);
		for(FeatureTerm solution:solutions) {
			if (support.get(solution)!=null) total+=support.get(solution);
		}
	
		if (total>0) return correct/total;
		return 1.0f/(float)solutions.size();
	}
	
	public FeatureTerm getSolution() {
		HashMap<FeatureTerm,Integer> votes = new HashMap<FeatureTerm,Integer>();
		
		for(FeatureTerm solution:solutions) {
			if (votes.get(solution)==null) {
				votes.put(solution,support.get(solution));
			} else {
				votes.put(solution,votes.get(solution)+support.get(solution));
			}
		}
		
		{
			FeatureTerm max = null;
			int max_votes =0;
			
			for(FeatureTerm solution:votes.keySet()) {
				if (max==null || votes.get(solution)>max_votes) {
					max = solution;
					max_votes = votes.get(solution);
				}
			}
			
			return max;
		}
	}
}
