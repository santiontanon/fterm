package fterms;

import java.util.HashMap;
import java.util.List;

import fterms.exceptions.FeatureTermException;
import fterms.specialterms.SpecialTerm;


public class SpecialFeatureTerm extends TermFeatureTerm {
	SpecialTerm m_value = null;
	
	public SpecialFeatureTerm(Sort s) {
		super(s);
	}

	public SpecialFeatureTerm(String name,Sort s) throws FeatureTermException {
		super(name,s);
	}

	public SpecialFeatureTerm(Symbol name,Sort s) {
		super(name,s);
	}

	public SpecialFeatureTerm(String name,Sort s, SpecialTerm value) throws FeatureTermException {
		super(name,s);
		m_value = value;
	}
	
	public SpecialFeatureTerm(Symbol name,Sort s, SpecialTerm value) {
		super(name,s);
		m_value = value;
	}

	// This function is supposed to read all the features of the TermFeatureTerm, and instantiate the appropriate values of the special term
	public void takeValues() throws FeatureTermException {
		m_value.takeValues(this);
	}
	
	
	FeatureTerm cloneInternal2(HashMap<FeatureTerm,FeatureTerm> correspondences, FTKBase dm,Ontology o) throws FeatureTermException {
		SpecialFeatureTerm f = new SpecialFeatureTerm(m_name,m_sort);
		correspondences.put(this,f);
		
		f.m_value = m_value.clone(dm, o, correspondences);
		for(Symbol fn:getFeatureNames()) f.defineFeatureValue(fn, featureValue(fn));
				
		return f;
	}
	
	public SpecialTerm getValue() {
		return m_value;		
	}
	
	public List<FeatureTerm> specializations(FTKBase dm, Ontology o) throws FeatureTermException {
		return m_value.specializations(dm,o,m_sort);
	}

	public List<FeatureTerm> specializationsSubsumingAll(FTKBase dm, Ontology o, List<FeatureTerm> objects) throws FeatureTermException {
		return m_value.specializationsSubsumingAll(dm,o,m_sort,objects);
	}

	public List<FeatureTerm> specializationsSubsumingSome(FTKBase dm, Ontology o, List<FeatureTerm> objects) throws FeatureTermException {
		return m_value.specializationsSubsumingSome(dm,o,m_sort,objects);
	}

	public List<FeatureTerm> generalizations(FTKBase dm, Ontology o) throws FeatureTermException {
		return m_value.generalizations(dm,o,m_sort);
	}
}
