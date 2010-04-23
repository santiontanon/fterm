package fterms.learning.distance;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import fterms.FTAntiunification;
import fterms.FTKBase;
import fterms.FTRefinement;
import fterms.FeatureTerm;
import fterms.FloatFeatureTerm;
import fterms.IntegerFeatureTerm;
import fterms.Ontology;
import fterms.SetFeatureTerm;
import fterms.Sort;
import fterms.Symbol;
import fterms.TermFeatureTerm;
import fterms.exceptions.FeatureTermException;

import util.Pair;

/*
 * This is SHAUD as explained in Eva's and Enric's paper (it has some inconsistencies)
 */

public class SHAUD extends Distance {
	
	boolean debugSHAUD = false;
	boolean debugSETS = false;
	boolean debugStructure = false;

	HashMap<String,Pair<Double,Double>> m_numericFeatureRanges = new HashMap<String,Pair<Double,Double>>();
	
	public SHAUD(List<FeatureTerm> cases) throws FeatureTermException {
		for(FeatureTerm c:cases) {
			Set<FeatureTerm> v = FTRefinement.variables(c);
			
			for(FeatureTerm ft:v) {
				if (ft instanceof TermFeatureTerm) {
					Sort s = ft.getSort();
					for(Symbol feature:s.getFeatures()) {
						String name = s.get()+"-"+feature.get();
						FeatureTerm value = ft.featureValue(feature);
						Double dvalue = null;
						
						if (value instanceof IntegerFeatureTerm) {
							Integer ivalue = ((IntegerFeatureTerm)value).getValue();
							if (ivalue!=null) {
								dvalue = (double)((int)ivalue);
							}
						} else if (value instanceof FloatFeatureTerm) {
							Float fvalue = ((FloatFeatureTerm)value).getValue();
							if (fvalue!=null) {
								dvalue = (double)((float)fvalue);
							}
						}
						
						if (dvalue!=null) {
							Pair<Double,Double> range = m_numericFeatureRanges.get(name);
							if (range==null) {
								range = new Pair<Double,Double>(dvalue,dvalue);
								m_numericFeatureRanges.put(name,range);
							} else {
								range.m_a = Math.min(range.m_a, dvalue);
								range.m_b = Math.max(range.m_b, dvalue);
							}
						}
					}
				}
			}
		}
		
		for(String name:m_numericFeatureRanges.keySet()) {
			System.out.println("Range for " + name + ": " + m_numericFeatureRanges.get(name).m_a + " - " + m_numericFeatureRanges.get(name).m_b);
		}
	}
	
	public double distance(FeatureTerm f1, FeatureTerm f2, Ontology o,
			FTKBase dm) throws FeatureTermException {

		return 1 - SHAUDSimilarity(o.getSort("any"),null,null,f1,f2,o,dm);
	}
	
	private double SHAUDSimilarity(Sort mostGeneral,String rangeName,FeatureTerm au,FeatureTerm f1, FeatureTerm f2, Ontology o,
			FTKBase dm) throws FeatureTermException {

		if (f1 instanceof SetFeatureTerm || f2 instanceof SetFeatureTerm) {
			List<FeatureTerm> s1 = new LinkedList<FeatureTerm>();
			List<FeatureTerm> s2 = new LinkedList<FeatureTerm>();
			double []similarities;
			int i;
			
			if (f1 instanceof SetFeatureTerm) {
				for(FeatureTerm f:((SetFeatureTerm)f1).getSetValues()) {
					s1.add(f);
				}
			} else {
				s1.add(f1);
			}
			if (f2 instanceof SetFeatureTerm) {
				for(FeatureTerm f:((SetFeatureTerm)f2).getSetValues()) {
					s2.add(f);
				}
			} else {
				s2.add(f2);
			}
						
			similarities = new double[s1.size()*s2.size()];

			if (s1.size()>s2.size()) {
				List<FeatureTerm> tmp = s1;
				s1 = s2;
				s2 = tmp;
			}
			
			i = 0;
			for(int n1 = 0;n1<s1.size();n1++) {
				for(int n2 = 0;n2<s2.size();n2++) {
					similarities[i] = SHAUDSimilarity(mostGeneral,rangeName,null,s1.get(n1),s2.get(n2),o,dm);
					i++;
				}				
			}
			
			double similarity =  bestSetPairing(s1,s2,similarities,dm);
			if (debugSHAUD) {
				System.out.println("SHAUD(sets):");
				System.out.println(f1.toStringNOOS(dm));
				System.out.println(f2.toStringNOOS(dm));
				System.out.println("- similarity: " + similarity);
			}
			return similarity;
		} else 
		if (f1.getDataType()==Sort.DATATYPE_INTEGER && f2.getDataType()==Sort.DATATYPE_INTEGER) {
			Pair<Double,Double> range = m_numericFeatureRanges.get(rangeName);
			
			if (range==null) {
				if (((IntegerFeatureTerm)f1).getValue().equals(((IntegerFeatureTerm)f2).getValue())) return 1.0;
				return 0.0;
			} else {
				double v1 = ((IntegerFeatureTerm)f1).getValue();
				double v2 = ((IntegerFeatureTerm)f2).getValue();
				
				return 1.0 - (Math.abs(v1-v2))/(range.m_b - range.m_a);
			}
		} else 
		if (f1.getDataType()==Sort.DATATYPE_FLOAT && f2.getDataType()==Sort.DATATYPE_FLOAT) {
			Pair<Double,Double> range = m_numericFeatureRanges.get(rangeName);
			
			if (range==null) {
				if (((FloatFeatureTerm)f1).getValue().equals(((FloatFeatureTerm)f2).getValue())) return 1.0;
				return 0.0;
			} else {
				double v1 = ((FloatFeatureTerm)f1).getValue();
				double v2 = ((FloatFeatureTerm)f2).getValue();
				
				return 1.0 - (Math.abs(v1-v2))/(range.m_b - range.m_a);
			}
		} else {
			double SimE = SortSimilarity(mostGeneral,f1,f2,o,dm);
			double SimS = StructuralSimilarity(f1,f2,au,o,dm);
			double O = Omega(f1,f2,o,dm);
			
			double similarity = (SimE + SimS)/(1 + O); 
			
			if (debugSHAUD) {
				System.out.println("SHAUD:");
				System.out.println(f1.toStringNOOS(dm));
				System.out.println(f2.toStringNOOS(dm));
				System.out.println("- SimE: " + f1.getSort().get() + "," + f2.getSort().get() + "(" +mostGeneral.get() + ") -> " + SimE);
				System.out.println("- SimS: " + SimS);
				System.out.println("- O: " + O);
				System.out.println("- similarity: " + similarity);
			}
			
			return similarity;
		}
	}
	
	private double SortSimilarity(Sort mostGeneral,FeatureTerm f1, FeatureTerm f2, Ontology o, FTKBase dm) {
		Sort sort = f1.getSort();
		Sort sort2 = f2.getSort();
		
		if (f1.equals(f2)) return 1.0;
		
		Sort au = sort.Antiunification(sort2);
		double d_mg = 0;
		double d_au = 0;
		double d_max = 0;
			
	//	System.out.print("S(" + sort.get() + "," + sort2.get() + ") -> " +au.get() + ":");
		
		d_max = sortTreeDepth(mostGeneral)+1; // we add 1 here to denote that the values in the DM also count in the hierarchy
		
		while(mostGeneral!=null) {
			mostGeneral = mostGeneral.getSuper();
			d_mg++;
		}
		
		while(au!=null) {
			au = au.getSuper();
			d_au++;
		}
				
		d_au -=d_mg;
		
		double similarity = (d_max!=0 ? (1.0 - ((d_max - d_au)/d_max)):1.0);

//		System.out.println((d_max - d_au) + " / " + d_max + " -> " + similarity);
		
		return similarity;
	}

	private int sortTreeDepth(Sort mostGeneral) {
		int max = 0;
		
		for(Sort s:mostGeneral.getSubSorts()) {
			int tmp = 1 + sortTreeDepth(s);
			if (tmp>max) max = tmp;
		}
		return max;
	}

	private double Omega(FeatureTerm f1, FeatureTerm f2,Ontology o, FTKBase dm) throws FeatureTermException {
		FeatureTerm au = FTAntiunification.simpleAntiunification(f1, f2, o, dm);
		
		return (W(f1)+W(f2)-W(au));
	}

	private double StructuralSimilarity(FeatureTerm f1, FeatureTerm f2, FeatureTerm au,Ontology o,FTKBase dm) throws FeatureTermException {
		if (au==null) au = FTAntiunification.simpleAntiunification(f1, f2, o, dm);
		double similarity = 0.0;

		if (au.getSort()==null) {
			System.out.println(f1.toStringNOOS(dm));
			System.out.println(f2.toStringNOOS(dm));
		}
		
		if (debugStructure) {
			System.out.println("StructuralSimilarity:");
			System.out.println(f1.toStringNOOS(dm));
			System.out.println(f2.toStringNOOS(dm));
		}
		
		for(Symbol f:au.getSort().getFeatures()) {
			FeatureTerm auf = au.featureValue(f);
			
			if (auf!=null) {
				FeatureTerm f1f = f1.featureValue(f);
				FeatureTerm f2f = f2.featureValue(f);

				double shaud = SHAUDSimilarity(au.getSort().featureSort(f),au.getSort().get()+"-"+f.get(),au.featureValue(f),f1f,f2f,o,dm);
				double w = 1 + W(auf);
				if (debugStructure) System.out.println("+ (" + shaud + " * " + w + ")" );
				similarity+=shaud*w;
			}
		}

		if (debugStructure) System.out.println("total: " + similarity);
		
		return similarity;
	}
	
	private double W(FeatureTerm ft) {
		return FTRefinement.variables(ft).size()-1;
	}
	
	
	double bestSetPairing(List<FeatureTerm> set1,List<FeatureTerm> set2,double similarities[],FTKBase dm) {
		int s1 = set1.size();
		int s2 = set2.size();
		double max_sim = 0.0;
		int max_i = 0;

		for(int i=0;i<Math.pow(s2, s1);i++) {
			int tmp = i;
			double sim = 0.0;
			for(int j = 0;j<s1;j++) {
				int correspondence = tmp%s2;
				tmp = tmp/s2;
				
				sim +=similarities[j*s2+correspondence];
			}
			
			if (sim>max_sim) {
				max_sim = sim;
				max_i = i;
			}
		}
		
		max_sim/=s2;
		
		if (debugSETS) {
			System.out.println("bestSetPairing");
			System.out.print("s1: [ ");
			for(FeatureTerm f:set1) {
				System.out.print(f.toStringNOOS(dm)+ " ");
			}
			System.out.println("]");
			System.out.print("s2: [ ");
			for(FeatureTerm f:set2) {
				System.out.print(f.toStringNOOS(dm)+ " ");
			}
			System.out.println("]");

			int tmp = max_i;
			for(int j = 0;j<s1;j++) {
				int correspondence = tmp%s2;
				tmp = tmp/s2;
				System.out.println(j + " - " + correspondence + " -> " + similarities[j*s2+correspondence]);
			}
			System.out.println("Sim: " + max_sim);
		}
		
		return max_sim;
	}

}
