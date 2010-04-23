package fterms.translators;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import util.Pair;

import fterms.FTKBase;
import fterms.FTRefinement;
import fterms.FeatureTerm;
import fterms.Ontology;
import fterms.Path;
import fterms.SetFeatureTerm;
import fterms.Sort;
import fterms.Symbol;
import fterms.TermFeatureTerm;
import fterms.exceptions.FeatureTermException;

public class FeatureValue {
	public static List<FeatureTerm> toFeatureValue(Collection<FeatureTerm> examples,Path sp,Path dp,Ontology o,FTKBase dm, Ontology targetOntology, boolean fillMissing) throws FeatureTermException {
		List<FeatureTerm> propositionalExamples = new LinkedList<FeatureTerm>();
		List<Path> paths = new LinkedList<Path>();
		Sort problem_sort = null;
		Sort description_sort = null;
		Sort solution_sort = null;
		FeatureTerm unknown = null;

		Sort pes;
		Sort pds;

		// Get all paths:
		{
			for(FeatureTerm example:examples) {
				FeatureTerm description = example.readPath(dp);
				FeatureTerm solution = example.readPath(sp);
//				System.out.println(example.toStringNOOS(dm));
				{
					Sort es = example.getSort();
					Sort ds = description.getSort();
					Sort ss = solution.getSort();
					
					if (problem_sort==null) {
						problem_sort = es;
						description_sort = ds;
						solution_sort = ss;
					} else {
						problem_sort = problem_sort.Antiunification(es);
						description_sort = description_sort.Antiunification(ds);
						solution_sort = solution_sort.Antiunification(ss);
					}
				}
				
				for(Pair<FeatureTerm,Path> pair:FTRefinement.variablesWithPaths(description,dm)) {
					if (!paths.contains(pair.m_b)) {
						paths.add(pair.m_b);
//					} else {
//						System.out.println(pair.m_b + " not added");
					}
				}
//				break;
			}
		}
				
		// Filter by removing paths that are subpaths of others (we only want the leaves):
		{
			boolean subPath = false;
			List<Path> to_delete = new LinkedList<Path>();
			
			for(Path p1:paths) {
				for(Path p2:paths) {
					if (p1!=p2) {
						if (p1.size()<p2.size()) {
							subPath = true;
							for(int i=0;i<p1.size();i++) {
								if (!p1.features.get(i).equals(p2.features.get(i))) {
									subPath = false;
									break;
								}
							}
							if (subPath) to_delete.add(p1);
						}
					}
				}
			}
			
			paths.removeAll(to_delete);
		}
		
		System.out.println("Total number of paths after filtering: " + paths.size());
		for(Path p:paths) {
			System.out.println(p);
		}

		// Create a new sort in the target ontology with the new flatteened example:
		{
			pes = new Sort("Propositional" + problem_sort.get(),null,targetOntology);
			pds = new Sort("Propositional" + description_sort.get(),null,targetOntology);
			
			pes.setDataType(Sort.DATATYPE_FEATURETERM);
			pds.setDataType(Sort.DATATYPE_FEATURETERM);
			
			pes.addFeature("description", pds.get(), null, targetOntology, true);
			pes.addFeature("solution", solution_sort.get(), null, targetOntology, true);
			
			for(Path p:paths) {
				Sort fs = null;
				
				for(FeatureTerm example:examples) {
					FeatureTerm d = example.readPath(dp);
					FeatureTerm v = d.readPath(p);
					
					if (v!=null) {
						if (v instanceof SetFeatureTerm) {
							for(FeatureTerm v2:((SetFeatureTerm)v).getSetValues()) {
								Sort s = v2.getSort();
								if (fs == null) {
									fs = s; 
								} else {
									fs = fs.Antiunification(s);
								}								
							}
						} else {
							Sort s = v.getSort();
							if (fs == null) {
								fs = s; 
							} else {
								fs = fs.Antiunification(s);
							}
						}
					} // if
				}
				
				pds.addFeature(p.toString(), fs.get(), null, targetOntology, true);
			}
			
			System.out.println("Sorts created:");
			System.out.println(pes.getDescription());
			System.out.println(pds.getDescription());
		}
		
		// Convert all the examples to that ontology:
		for(FeatureTerm example:examples) {
			FeatureTerm d = example.readPath(dp);
			FeatureTerm s = example.readPath(sp);
			
			TermFeatureTerm pd,pe;
			
			if (example.getName()!=null) {
				pe = new TermFeatureTerm("propositional"+example.getName().get(),pes);
			} else {
				pe = new TermFeatureTerm((String)null,pes);
			}
			
			if (d.getName()!=null) {
				pd = new TermFeatureTerm("propositional"+d.getName().get(),pds);
			} else {
				pd = new TermFeatureTerm((String)null,pds);				
			}
			
			for(Path p:paths) {
				FeatureTerm v = d.readPath(p);
				if (v!=null) {
					pd.defineFeatureValue(new Symbol(p.toString()), v);
				} else {
					if (fillMissing) {
						if (unknown==null) {
							unknown = new TermFeatureTerm("unknown", targetOntology.getSort("any"));
							dm.AddFT(unknown);
						}
						pd.defineFeatureValue(new Symbol(p.toString()), unknown);
					}
				}
			}
			
			pe.defineFeatureValue(new Symbol("description"), pd);
			pe.defineFeatureValue(new Symbol("solution"), s);
			
			propositionalExamples.add(pe);
		}
		return propositionalExamples;
	}	
}
