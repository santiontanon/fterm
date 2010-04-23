package fterms.learning.propositional;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import util.Pair;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import fterms.FTKBase;
import fterms.FeatureTerm;
import fterms.FloatFeatureTerm;
import fterms.IntegerFeatureTerm;
import fterms.Ontology;
import fterms.Path;
import fterms.SetFeatureTerm;
import fterms.Sort;
import fterms.Symbol;
import fterms.exceptions.FeatureTermException;

public class NOOSToWeka {
	
	public static class ConversionRecord {
		List<FeatureTerm> allCases;
		Instances allWekaCases;
		FeatureTerm []solutionMapping = null;	// This array stores the mapping from weka solutions to FeatureTerms for
													// the last time that the method 'toInstances' was called.
		public HashMap<FeatureTerm,FeatureTerm> problemsToCases;
	}

	static List<ConversionRecord> s_conversionRecords = new LinkedList<ConversionRecord>();
	
	
	/*
	 * This method converts a set of examples to the WEKA classes. The set of examples has to have been converted 
	 * first to a propositional representation using the fterms.learning.propositional.Conversor class
	 */
	public static ConversionRecord toInstances(List<FeatureTerm> examples,Path dp,Path sp, FTKBase dm, Ontology o) throws FeatureTermException {
		String name = null;
		FastVector attributes = null;
		Instances dataSet = null;
		long start_t, end_t;
		Sort descriptionSort = null;
		Sort exampleSort = null;
		Sort solutionSort = null;
		List<Pair<Path,Sort>> exampleFeatures = new LinkedList<Pair<Path,Sort>>();
		Sort integerSort = o.getSort("integer"); 
		Sort floatSort = o.getSort("float"); 
		attributes = new FastVector();
		
		ConversionRecord record = new ConversionRecord();
		
		start_t = System.currentTimeMillis();

		// Data set name and sort:
		{
			for(FeatureTerm e:examples) {
				FeatureTerm d = e.readPath(dp);
				FeatureTerm s = e.readPath(sp);
				descriptionSort = d.getSort();
				exampleSort = e.getSort();
				solutionSort = s.getSort();
				name = descriptionSort.get() + "-Instances";
				break;
			}
		}

		// Compute the set of attributes:
		{		
			int index = 0;
			
			exampleFeatures.add(new Pair<Path,Sort>(sp,solutionSort));
			for(Symbol fname:descriptionSort.getFeatures()) {
				Path fpath = new Path(dp);
				fpath.features.add(fname);
				exampleFeatures.add(new Pair<Path,Sort>(fpath,descriptionSort.featureSort(fname)));
			}
			
//			System.out.println("Example sort " + descriptionSort.get() + " with " + exampleFeatures + " features");
			for(Pair<Path,Sort> feature:exampleFeatures) {
				Attribute a;
				String featureName = feature.m_a.toString();
				Sort featureSort = feature.m_b;
				
				// The solution class cannot be numeric, so the first attributed (index = 0) is treated specially:
				if (index!=0 && 
					(integerSort.isSubsort(featureSort) ||
					 floatSort.isSubsort(featureSort))) {
					a = new Attribute(featureName);
//					System.out.println("Numeric feature: " + featureName);
				} else {
					FastVector possibleValues = new FastVector();
					List<FeatureTerm> allSolutionValues = null;
					
					if (index==0) allSolutionValues = new LinkedList<FeatureTerm>();
					
					// Find the possible values:
					{
						for(FeatureTerm example:examples) {
							FeatureTerm v = example.readPath(feature.m_a);
						    while(v!=null && v instanceof SetFeatureTerm) {
								SetFeatureTerm sv = (SetFeatureTerm)v;
								if (!sv.getSetValues().isEmpty()) {
									v = sv.getSetValues().get(0);
								} else {
									v = null;
								}
							}
							if (v!=null) {
								String sv = v.toStringNOOS(dm);
								sv = sv.replaceAll(" ", "-");
								if (!possibleValues.contains(sv)) {
									possibleValues.addElement(sv);
									if (index==0) allSolutionValues.add(v);
								}
							}
						}
					}
					
					if (index==0) {
						record.solutionMapping = new FeatureTerm[possibleValues.size()];
						for(int i = 0;i<allSolutionValues.size();i++) {
							record.solutionMapping[i]=allSolutionValues.get(i);
						}
					}
										
					a = new Attribute(featureName,possibleValues);
//					System.out.println("Symbolic feature: " + featureName);
//					System.out.print("Possible values: ");
//					for(int i=0;i<possibleValues.size();i++) System.out.print("'" + possibleValues.elementAt(i) + "' ");
//					System.out.println("");
				}
				
//				System.out.println(a.type());
				
				attributes.addElement(a);
				index++;
			}			
		}
		
		dataSet = new Instances(name,attributes,examples.size());
		dataSet.setClassIndex(0);
		
		for(FeatureTerm example:examples) {
			FeatureTerm description = example.readPath(dp);
			FeatureTerm solution = example.readPath(sp);
			
			Instance i = new Instance(attributes.size());

			for(Pair<Path,Sort> feature:exampleFeatures) {
				int index = exampleFeatures.indexOf(feature);
				String featureName = feature.m_a.toString();
				Sort featureSort = feature.m_b;
				
				FeatureTerm v = example.readPath(feature.m_a);
			    while(v!=null && v instanceof SetFeatureTerm) {
					SetFeatureTerm sv = (SetFeatureTerm)v;
					if (!sv.getSetValues().isEmpty()) {
						v = sv.getSetValues().get(0);
					} else {
						v = null;
					}
				}
				if (v!=null) {
					// The solution class cannot be numeric, so the first attributed (index = 0) is treated specially:
					if (index!=0 && v instanceof IntegerFeatureTerm) {
						i.setValue((Attribute)(attributes.elementAt(index)), (double)(((IntegerFeatureTerm)v).getValue()));						
					} else if (index!=0 && v instanceof FloatFeatureTerm) {
						i.setValue((Attribute)(attributes.elementAt(index)), (double)(((FloatFeatureTerm)v).getValue()));
					} else {
						String sv = v.toStringNOOS(dm);
						sv = sv.replaceAll(" ", "-");
						i.setValue((Attribute)(attributes.elementAt(index)), sv);
					} // if										
				}
			}	
			dataSet.add(i);
		}
		
		record.allCases = examples;
		record.allWekaCases = dataSet;
		
		record.problemsToCases = new HashMap<FeatureTerm,FeatureTerm>();
		for(FeatureTerm c:record.allCases) {
			FeatureTerm d = c.readPath(dp);
			record.problemsToCases.put(d,c);
		}

		
		end_t = System.currentTimeMillis();
		
//		System.out.println(dataSet);		
//		System.out.println("NOOSToWeka.toInstances took " + (end_t-start_t));

		return record;
	}
	
	
	public static Instances translateSubset(List<FeatureTerm> subset,List<FeatureTerm> originalSet, Instances completeTranslation) {
		Instances wekaSubset = new Instances(completeTranslation,subset.size());
		
		for(FeatureTerm example:subset) {
			int i = originalSet.indexOf(example);
			wekaSubset.add(completeTranslation.instance(i));
		}

//		System.out.println(wekaSubset);		
		
		return wekaSubset;
	}
	
	public static Instance translateInstance(FeatureTerm example,List<FeatureTerm> originalSet,Instances completeTranslation) {
		return completeTranslation.instance(originalSet.indexOf(example));
	}
	
}
