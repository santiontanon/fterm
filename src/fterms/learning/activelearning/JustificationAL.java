package fterms.learning.activelearning;

import java.util.LinkedList;
import java.util.List;

import util.Pair;
import fterms.FTKBase;
import fterms.FeatureTerm;
import fterms.Path;
import fterms.exceptions.FeatureTermException;
import fterms.learning.InformationMeasurement;
import fterms.learning.Prediction;
import java.util.HashSet;

public class JustificationAL extends QueryByCommittee {

	public final static int CONFIDENCE_AVERAGE = 0;			// Averages confidences for each cluster
	public final static int CONFIDENCE_SUM = 1;				// Adds confidences for each cluster
	public final static int CONFIDENCE_BAYES = 2;			// A better estimation to turn confidences into probabilities
	
	public final static int DISAGREEMENT_FIRST_SECOND = 0;
	public final static int DISAGREEMENT_ENTROPY = 1;

	int m_confidenceMethod = 0;
	int m_disageementMethod = 0;
	
	public JustificationAL(int classifiers,int confidenceMethod,int disageementMethod) {
		super(classifiers);
		m_confidenceMethod = confidenceMethod;
		m_disageementMethod = disageementMethod;
	}
	
	public String toString() {
		String conf[]={"average","sum","Bayes"};
		String disa[]={"FS","entropy"};
		return "JustificationAL(" + m_nClassifiers + "-" + conf[m_confidenceMethod] + "-" + disa[m_disageementMethod] + ")";
	}

	public double disagreement(List<Prediction> predictions,List<List<FeatureTerm>> trainingSets,List<FeatureTerm> differentSolutions,Path dp, Path sp,FTKBase dm) throws FeatureTermException {
		List<Pair<FeatureTerm,Double>> solutions = new LinkedList<Pair<FeatureTerm,Double>>();
		List<Pair<FeatureTerm,Pair<Double,Integer>>> solutionClusters = new LinkedList<Pair<FeatureTerm,Pair<Double,Integer>>>();
		
		// Compute solution confidence:
		for(Prediction p:predictions) {
			FeatureTerm solution = p.getSolution();

            solutions.add(new Pair<FeatureTerm,Double>(solution,justificationConfidenceQuick(p.justifications.get(solution),solution,trainingSets,dp,sp)));
		}

		for(FeatureTerm s:differentSolutions) {
			Pair<FeatureTerm,Pair<Double,Integer>> cluster = new Pair<FeatureTerm,Pair<Double,Integer>>(s,new Pair<Double,Integer>(0.0,0));
			solutionClusters.add(cluster);				
		}
		
		switch(m_confidenceMethod) {
		case CONFIDENCE_AVERAGE:
				for(Pair<FeatureTerm,Double> solution:solutions) {
					Pair<FeatureTerm,Pair<Double,Integer>> cluster = null;
					for(Pair<FeatureTerm,Pair<Double,Integer>> tmp:solutionClusters) {
						if (tmp.m_a.equals(solution.m_a)) {
							cluster = tmp;
							break;
						}
					}
					if (cluster==null) {
						cluster = new Pair<FeatureTerm,Pair<Double,Integer>>(solution.m_a,new Pair<Double,Integer>(0.0,0));
						solutionClusters.add(cluster);
					}
					cluster.m_b.m_a+=solution.m_b;
					cluster.m_b.m_b++;
				}
				
				// average:
				for(Pair<FeatureTerm,Pair<Double,Integer>> tmp:solutionClusters) {
					tmp.m_b.m_a/=tmp.m_b.m_b;
				}
				break;
		case CONFIDENCE_SUM:
				for(Pair<FeatureTerm,Double> solution:solutions) {
					Pair<FeatureTerm,Pair<Double,Integer>> cluster = null;
					for(Pair<FeatureTerm,Pair<Double,Integer>> tmp:solutionClusters) {
						if (tmp.m_a.equals(solution.m_a)) {
							cluster = tmp;
							break;
						}
					}
					if (cluster==null) {
						cluster = new Pair<FeatureTerm,Pair<Double,Integer>>(solution.m_a,new Pair<Double,Integer>(0.0,0));
						solutionClusters.add(cluster);
					}
					cluster.m_b.m_a+=solution.m_b;
					cluster.m_b.m_b++;
				}
				break;
		case CONFIDENCE_BAYES:
			for(Pair<FeatureTerm,Double> solution:solutions) {
				for(Pair<FeatureTerm,Pair<Double,Integer>> cluster:solutionClusters) {
					if (cluster.m_a.equals(solution.m_a)) {
						cluster.m_b.m_a+=solution.m_b;
					} else {
						cluster.m_b.m_a+=(1-solution.m_b)/(differentSolutions.size()-1);
					}
				}
			}
			
			for(Pair<FeatureTerm,Pair<Double,Integer>> cluster:solutionClusters) {
				cluster.m_b.m_a/=solutions.size();
			}
			break;
		}
		
		
		// Sort them:
		{
			int l = solutionClusters.size();
			boolean change = true;
			while(change) {
				change = false;
				for(int i = 0;i<l-1;i++) {
					Pair<FeatureTerm,Pair<Double,Integer>> u1 = solutionClusters.get(i);
					Pair<FeatureTerm,Pair<Double,Integer>> u2 = solutionClusters.get(i+1);
					
					if (u1.m_b.m_a<u2.m_b.m_a) {
						FeatureTerm tmp1 = u1.m_a;
						Pair<Double,Integer> tmp2 = u1.m_b;
						u1.m_a = u2.m_a;
						u1.m_b = u2.m_b;
						u2.m_a = tmp1;
						u2.m_b = tmp2;
						change = true;
					}
				}
			}
		}
		
		switch(m_disageementMethod) {
		case DISAGREEMENT_FIRST_SECOND:
			{
				double c1 = 0;
				double c2 = 0;
				double max = 1.0;
				
				if (m_confidenceMethod == CONFIDENCE_SUM) max = trainingSets.size();
				
				if (solutionClusters.size()>=1) c1 = solutionClusters.get(0).m_b.m_a; 
				if (solutionClusters.size()>=2) c2 = solutionClusters.get(1).m_b.m_a; 
				
	//			for(Pair<FeatureTerm,Pair<Double,Integer>> c:solutionClusters) {
	//				System.out.print("<" + c.m_a.toStringNOOS(dm) + ", " + c.m_b.m_a + "> ");
	//			}
	//			System.out.println(" -->> " + c1 + " - " + c2 + " -> " + (1.0-(c1-c2)));
				
				return max - (c1-c2);
			}
		
		case DISAGREEMENT_ENTROPY:
			{
				double []p = new double[differentSolutions.size()];
				int i = 0;
				for(Pair<FeatureTerm,Pair<Double,Integer>> cluster:solutionClusters) {
					p[i++]=cluster.m_b.m_a;
				}
				double h = InformationMeasurement.entropyD(i, p);
				
//				System.out.print("Entropy: [");
//				for(double tmp:p) System.out.print(tmp + " ");
//				System.out.println("] -> " + h);
				return h;
			}
		}
		
		return 0;
	}


    private double justificationConfidenceQuick(FeatureTerm pattern,FeatureTerm solution,List<List<FeatureTerm>> trainingSets,Path dp,Path sp) throws FeatureTermException {
        HashSet<FeatureTerm> all = new HashSet<FeatureTerm>();
        HashSet<FeatureTerm> subsumed = new HashSet<FeatureTerm>();
        double aye = 0.0;
        double nay = 0.0;

        if (pattern==null) return 0.5;

        for(List<FeatureTerm> ts:trainingSets) all.addAll(ts);

        for(FeatureTerm c:all) {
            FeatureTerm d = c.readPath(dp);
            if (pattern.subsumes(d)) subsumed.add(c);
        }

        for(List<FeatureTerm> ts:trainingSets) {

            for(FeatureTerm c:ts) {
                if (subsumed.contains(c)) {
				FeatureTerm s = c.readPath(sp);
                    if (s.equals(solution)) aye++;
                                       else nay++;
                }
            }
        }

		return (1.0+aye)/(2.0+aye+nay);
    }
}
