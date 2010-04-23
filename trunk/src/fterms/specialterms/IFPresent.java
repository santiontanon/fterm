package fterms.specialterms;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import fterms.FTKBase;
import fterms.FTRefinement;
import fterms.FeatureTerm;
import fterms.Ontology;
import fterms.Sort;
import fterms.SpecialFeatureTerm;
import fterms.Symbol;
import fterms.exceptions.FeatureTermException;

/*
 * This special term has the property that it always subsumes a null term. So, it can be used
 * to simulate the conditions in decision trees (where when an attribute is missing, that conditions is
 * skipped).
 */

public class IFPresent extends SpecialTerm {
		
	FeatureTerm m_value;
	
	public IFPresent(FeatureTerm value) {
		m_value = value;
//		System.out.println("NEW FloatInterval CREATED!!!!!!!!!!!!!!!!!!!!!!!");
	}
	
	public static void createSort(Ontology o) throws FeatureTermException {
		Sort s;
		s=o.newSpecialSort("if-present","float",new String[]{"min","float"},new String[]{"max","float"});
		s.setSpecialSeed(new IFPresent(null));
	}

	public void takeValues(FeatureTerm parent) throws FeatureTermException {
		m_value = (parent.featureValue("value"));
	}

	public SpecialTerm newSpecialTerm() {
		return new FloatInterval(null,null);
	}
	
	public SpecialTerm clone(FTKBase dm,Ontology o, HashMap<FeatureTerm,FeatureTerm> correspondences) throws FeatureTermException {
		FeatureTerm c_value = (m_value == null ? null:m_value.clone(dm, o, correspondences));
		return new IFPresent(c_value);
	}

	public boolean subsumes(FeatureTerm f) throws FeatureTermException 
	{
		if (f==null) return true;
		return m_value.subsumes(f);
	}
	
	
	public List<FeatureTerm> specializations(FTKBase dm, Ontology o, Sort m_sort) throws FeatureTermException
	{
		List<FeatureTerm> l = FTRefinement.getSpecializations(m_value, dm, FTRefinement.ALL_REFINEMENTS);
		List<FeatureTerm> ret = new LinkedList<FeatureTerm>();
		Symbol fName = new Symbol("value");
		
		for(FeatureTerm f:l) {
			SpecialFeatureTerm sft = new SpecialFeatureTerm((Symbol)null,o.getSort(new Symbol("if-present")),new IFPresent(null));
			sft.defineFeatureValue(fName, f);
			sft.takeValues();
			ret.add(sft);
		}
		return ret;
	}
	
	
	public List<FeatureTerm> specializationsSubsumingAll(FTKBase dm, Ontology o, Sort sort,List<FeatureTerm> objects) throws FeatureTermException
	{
		List<FeatureTerm> l = FTRefinement.getSpecializationsSubsumingAll(m_value, dm, o, FTRefinement.ALL_REFINEMENTS,objects);
		List<FeatureTerm> ret = new LinkedList<FeatureTerm>();
		Symbol fName = new Symbol("value");
		
		for(FeatureTerm f:l) {
			SpecialFeatureTerm sft = new SpecialFeatureTerm((Symbol)null,o.getSort(new Symbol("if-present")),new IFPresent(null));
			sft.defineFeatureValue(fName, f);
			sft.takeValues();
			ret.add(sft);
		}

		return ret;
	}
		
	public List<FeatureTerm> specializationsSubsumingSome(FTKBase dm, Ontology o, Sort sort,List<FeatureTerm> objects) throws FeatureTermException
	{
		List<FeatureTerm> l = FTRefinement.getSpecializationsSubsumingSome(m_value, dm, o, FTRefinement.ALL_REFINEMENTS,objects);
		List<FeatureTerm> ret = new LinkedList<FeatureTerm>();
		Symbol fName = new Symbol("value");
		
		for(FeatureTerm f:l) {
			SpecialFeatureTerm sft = new SpecialFeatureTerm((Symbol)null,o.getSort(new Symbol("if-present")),new IFPresent(null));
			sft.defineFeatureValue(fName, f);
			sft.takeValues();
			ret.add(sft);
		}

		return ret;
	}
	
	
	public List<FeatureTerm> generalizations(FTKBase dm, Ontology o, Sort m_sort) throws FeatureTermException
	{
		List<FeatureTerm> l = FTRefinement.getGeneralizations(m_value, dm, o);
		List<FeatureTerm> ret = new LinkedList<FeatureTerm>();
		Symbol fName = new Symbol("value");
		
		for(FeatureTerm f:l) {
			SpecialFeatureTerm sft = new SpecialFeatureTerm((Symbol)null,o.getSort(new Symbol("if-present")),new IFPresent(null));
			sft.defineFeatureValue(fName, f);
			sft.takeValues();
			ret.add(sft);
		}

		return ret;
	}
	
}
