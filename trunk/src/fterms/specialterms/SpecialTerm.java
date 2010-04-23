package fterms.specialterms;

import java.util.HashMap;
import java.util.List;

import fterms.FTKBase;
import fterms.FeatureTerm;
import fterms.Ontology;
import fterms.Sort;
import fterms.exceptions.FeatureTermException;


public abstract class SpecialTerm {
	
	public static void createSort(Ontology o) throws FeatureTermException {
		
	}
	
	public abstract SpecialTerm newSpecialTerm();
	public abstract void takeValues(FeatureTerm parent) throws FeatureTermException;
	
	public abstract SpecialTerm clone(FTKBase dm,Ontology o, HashMap<FeatureTerm,FeatureTerm> correspondences) throws FeatureTermException;
	
	public abstract boolean subsumes(FeatureTerm t) throws FeatureTermException;

	public abstract List<FeatureTerm> specializations(FTKBase dm, Ontology o, Sort m_sort) throws FeatureTermException;
	public abstract List<FeatureTerm> specializationsSubsumingAll(FTKBase dm, Ontology o, Sort m_sort,List<FeatureTerm> objects) throws FeatureTermException;
	public abstract List<FeatureTerm> specializationsSubsumingSome(FTKBase dm, Ontology o, Sort m_sort,List<FeatureTerm> objects) throws FeatureTermException;
	public abstract List<FeatureTerm> generalizations(FTKBase dm, Ontology o, Sort m_sort) throws FeatureTermException;
}
