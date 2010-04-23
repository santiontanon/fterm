package fterms.learning;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import fterms.FTAntiunification;
import fterms.FTKBase;
import fterms.FTRefinement;
import fterms.FeatureTerm;
import fterms.Ontology;
import fterms.Path;


public class RefinementINDIE extends InductiveLearner {
	
	public static int DEBUG = 0;

	public Hypothesis generateHypothesis(List<FeatureTerm> examples,Path dp,Path sp,Ontology o,FTKBase dm) throws Exception {
		return learn(examples,dp,sp,o,dm,0);
	}

	public RuleHypothesis learn(Collection<FeatureTerm> cases,Path description_path,Path solution_path,Ontology o,FTKBase domain_model,int heuristic) throws Exception {
		RuleHypothesis h = new RuleHypothesis(false);
		HashMap<FeatureTerm, List<FeatureTerm> > casesBySolution = new HashMap<FeatureTerm, List<FeatureTerm> >();
		List<FeatureTerm> positive = new LinkedList<FeatureTerm>();
		List<FeatureTerm> negative = new LinkedList<FeatureTerm>();

		// Compute the different solutions:  
		{
//			int pos;
			FeatureTerm s,d;
			List<FeatureTerm> descriptions;

			for(FeatureTerm c:cases) {
				d=c.readPath(description_path);
				s=c.readPath(solution_path);
				
				descriptions = casesBySolution.get(s);
				if (descriptions == null) {
					descriptions = new LinkedList<FeatureTerm>();
					descriptions.add(d);
					casesBySolution.put(s,descriptions);
				} else {
					descriptions.add(d);
				} // if 
			} // while 
		}

		// Compute the default solution:  
		{
			int max=-1;
			List<FeatureTerm> tmp;

			h.m_default_solution=null;

			for(FeatureTerm s:casesBySolution.keySet()) {
				tmp = casesBySolution.get(s);

				if (max==-1 || tmp.size()>max) {
					max=tmp.size();
					h.m_default_solution = s;
				} // if  
			} // while 
		}

		for(FeatureTerm solution:casesBySolution.keySet()) {
			// Learn a set of patterns for the current solution:  
			if (DEBUG>=1) System.out.println("\nRefinementINDIE: building model for " + solution.toStringNOOS(domain_model) + " --------------------------------------------------\n");

			// Create the positive and negative examples lists:  
			for(FeatureTerm sol:casesBySolution.keySet()) {
				if (sol.equals(solution)) {
					positive.addAll(casesBySolution.get(sol));
				} else {
					negative.addAll(casesBySolution.get(sol));						
				}
			}

			FeatureTerm description = o.getSort("any").createFeatureTerm();
			
			INDIE(h,description,solution,positive,negative,heuristic,o,domain_model);

			positive.clear();
			negative.clear();
		} // while 

		return h;		
	}
	

	void INDIE(RuleHypothesis h,FeatureTerm description,FeatureTerm solution,List<FeatureTerm> positive,List<FeatureTerm> negative,int heuristic,Ontology o,FTKBase domain_model) throws Exception
	{

		List<FeatureTerm> initial_l = new LinkedList<FeatureTerm>();
		List<FeatureTerm> au_l;
		FeatureTerm au;
		List<FeatureTerm> negative_covered = new LinkedList<FeatureTerm>(),negative_uncovered = new LinkedList<FeatureTerm>();
		List<FeatureTerm> positive_covered = new LinkedList<FeatureTerm>(),positive_uncovered = new LinkedList<FeatureTerm>();

		initial_l.add(description);

		if (DEBUG>=1) System.out.println("RefinementINDIE: computing antiunification with " + positive.size() + " objects...");
		au_l = FTAntiunification.antiunification(positive, 0, initial_l, o, domain_model, true, FTAntiunification.VERSION_FAST);

		au=au_l.remove(0);

		if (au!=null) {
			for(FeatureTerm example:negative) {
				if (au.subsumes(example)) {
					negative_covered.add(example);
				} else {
					negative_uncovered.add(example);
				} // if  
			} // while  
			
			if (negative_covered.isEmpty()) {
				// Rule found!!!  
				au=Hypothesis.generalizePattern(au,positive,negative,o,domain_model);
				h.addRule(au,solution,((float)positive.size()+1)/((float)positive.size()+2),positive.size());

				if (DEBUG>=1) System.out.println("RefinementINDIE: new rule found , covers " + positive.size() + " positive examples and 0 negative examples");
				if (DEBUG>=1) System.out.println("RefinementINDIE: rule is for class " + solution.toStringNOOS(domain_model));
				if (DEBUG>=1) System.out.println(au.toStringNOOS(domain_model));
			} else {				
				// Rule is too general, the space of problems has to be partitioned:  
				List<FeatureTerm> refinements;
				int selected,nrefinements;
				float heuristics[];
				int i;

				refinements = FTRefinement.getSpecializationsSubsumingSome(au, domain_model, o, FTRefinement.ALL_REFINEMENTS, positive);

				// Choose one refinement according to the heuristic:  			
				selected=-1;
				if (refinements.size()>0) {
					
					// Evaluate all the refinements:  
					nrefinements=refinements.size();
					heuristics=new float[nrefinements];
					i=0;
					for(FeatureTerm refinement:refinements) {
						switch(heuristic) {
						case 0:	// Information Gain:  
								{
									int before_p=0,before_n=0,before=0;
									int after_p=0,after_n=0,after=0;
									float before_i,after_i1,after_i2;
									double LOG2E=Math.log(2.0);

									for(FeatureTerm f:positive) {
										if (au.subsumes(f)) before_p++;
										if (refinement.subsumes(f)) after_p++;
									} // if  
									for(FeatureTerm f:negative) {
										if (au.subsumes(f)) before_n++;
										if (refinement.subsumes(f)) after_n++;
									} // if 

									before=before_p+before_n;
									after=after_p+after_n;
									before_i=((float)-(Math.log(((float)before_p)/((float)before_p+before_n))/LOG2E));
									if (after==0) {
										after_i1=0;
									} else {
										if (after_p==0) {
											after_i1=1;
										} else {
											after_i1=((float)-(Math.log(((float)after_p)/((float)after))/LOG2E));
										} // if 
									} // if 
									if (before-after==0) after_i2=0;
											 	    else after_i2=((float)-(Math.log(((float)before_p-after_p)/((float)before-after))/LOG2E));

									heuristics[i]=-(before_i-(after*after_i1+(before-after)*after_i2)/before);
//									System.out.printf("%d -> %g {%g,%g} [%d,%d] . [%d,%d]/[%d,%d] (%d . %d)\n",i,heuristics[i], after_i1,after_i2, before_p,before_n, after_p,after_n,before_p-after_p,before_n-after_n,before,after);
								}
								break;
						case 1: // RLDM:  
								heuristics[i]=0;
								break;
						default:heuristics[i]=0;
								break;
						} // switch  
							

						i++;
					} // while 

					// Choose one refinement:  
					{
						float maximum=heuristics[0];
						selected=0;

						for(i=0;i<nrefinements;i++) {
							if (heuristics[i]>maximum) {
								maximum=heuristics[i];
								selected=i;
							} // if 
						} // for   
					}

//					printf("Refinement selected: %i\n",selected);
					FeatureTerm refinement=refinements.get(selected);

					for(FeatureTerm example:positive) {
						if (refinement.subsumes(example)) {
							positive_covered.add(example);
						} else {
							positive_uncovered.add(example);
						} // if 
					} // while 

//					System.out.println("(" + selected + " - " + heuristics[selected] + ") " + positive_covered.size() + " covered, " + positive_uncovered.size() + " uncovered");

					if (!positive_covered.isEmpty()) INDIE(h,refinement,solution,positive_covered,negative,heuristic,o,domain_model);
					if (!positive_uncovered.isEmpty() && positive_uncovered.size()<positive.size()) {
						negative.add(refinement);
						INDIE(h,description,solution,positive_uncovered,negative,heuristic,o,domain_model);
						negative.remove(refinement);
					} // if  
					
				} // if  		
			} // if  			
		} else {
			System.err.println("RefinementINDIE: error computing antiunification!");
		} // if  
	}  	

}
