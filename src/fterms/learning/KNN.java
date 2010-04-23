package fterms.learning;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import fterms.FTKBase;
import fterms.FeatureTerm;
import fterms.Ontology;
import fterms.Path;
import fterms.exceptions.FeatureTermException;
import fterms.learning.distance.Distance;

import util.Pair;

public class KNN {
	
	static class SDComparator implements Comparator {

		public int compare(Object arg0, Object arg1) {
			double o0 = ((Pair<Pair<FeatureTerm,FeatureTerm>,Double>)arg0).m_b;
			double o1 = ((Pair<Pair<FeatureTerm,FeatureTerm>,Double>)arg1).m_b;

			if( o0 > o1 )
				return 1;
				else if( o0 < o1 )
				return -1;
				else
				return 0;		
			}
		
	}

	public static Prediction predict(FeatureTerm problem,List<FeatureTerm> cases,Path description_path,Path solution_path,Ontology o,FTKBase dm,int K, Distance d) throws FeatureTermException
	{
		List<Pair<Pair<FeatureTerm,FeatureTerm>,Double>> solutions_distances = new LinkedList<Pair<Pair<FeatureTerm,FeatureTerm>,Double>>();
		
		for(FeatureTerm c:cases) {
			FeatureTerm description = c.readPath(description_path);
			FeatureTerm solution = c.readPath(solution_path);
			double distance = d.distance(problem, description, o, dm);
			
			solutions_distances.add(new Pair<Pair<FeatureTerm,FeatureTerm>,Double>(new Pair<FeatureTerm,FeatureTerm>(c,solution),distance));
		}
		
		Collections.sort(solutions_distances, new KNN.SDComparator());
		
		Prediction p = new Prediction();
		p.problem = problem;
		
		for(int i = 0 ; i<K ; i++) {
			Pair<Pair<FeatureTerm,FeatureTerm>,Double> solution_distance = solutions_distances.remove(0);
			
			System.out.println(solution_distance.m_a.m_a.getName() + " - " + solution_distance.m_a.m_b.toStringNOOS(dm) + " -> " + solution_distance.m_b);
			
			if (!p.solutions.contains(solution_distance.m_a.m_b)) {
				p.solutions.add(solution_distance.m_a.m_b);
				p.justifications.put(solution_distance.m_a.m_b,null);
				p.support.put(solution_distance.m_a.m_b,0);
			}
			
			p.support.put(solution_distance.m_a.m_b, 1 + p.support.get(solution_distance.m_a.m_b));
		}
		
		return p;
	}
	
	
	/*
	 * This method returns different predictions for different values of K at the same time
	 */
	public static List<Prediction> multiplePredictions(FeatureTerm problem,List<FeatureTerm> cases,Path description_path,Path solution_path,Ontology o,FTKBase dm,List<Integer> Kl, Distance d) throws FeatureTermException
	{
		List<Pair<Pair<FeatureTerm,FeatureTerm>,Double>> solutions_distances = new LinkedList<Pair<Pair<FeatureTerm,FeatureTerm>,Double>>();
		
		for(FeatureTerm c:cases) {
			FeatureTerm description = c.readPath(description_path);
			FeatureTerm solution = c.readPath(solution_path);
			double distance = d.distance(problem, description, o, dm);
			
			solutions_distances.add(new Pair<Pair<FeatureTerm,FeatureTerm>,Double>(new Pair<FeatureTerm,FeatureTerm>(c,solution),distance));
		}
		
		Collections.sort(solutions_distances, new KNN.SDComparator());
		List<Prediction> pl = new LinkedList<Prediction>();
		
		for(Integer K:Kl) {
			Prediction p = new Prediction();
			p.problem = problem;
			System.out.println(" K = " + K + " ------------------------- ");
			for(int i = 0 ; i<K ; i++) {
				Pair<Pair<FeatureTerm,FeatureTerm>,Double> solution_distance = solutions_distances.get(i);
				
				System.out.println(solution_distance.m_a.m_a.getName() + " - " + solution_distance.m_a.m_b.toStringNOOS(dm) + " -> " + solution_distance.m_b);
				
				if (!p.solutions.contains(solution_distance.m_a.m_b)) {
					p.solutions.add(solution_distance.m_a.m_b);
					p.justifications.put(solution_distance.m_a.m_b,null);
					p.support.put(solution_distance.m_a.m_b,0);
				}
				
				p.support.put(solution_distance.m_a.m_b, 1 + p.support.get(solution_distance.m_a.m_b));
			}
			pl.add(p);
		}
		
		return pl;
	}
	
}
