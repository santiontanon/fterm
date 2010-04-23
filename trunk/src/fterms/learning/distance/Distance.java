package fterms.learning.distance;

import fterms.FTKBase;
import fterms.FeatureTerm;
import fterms.Ontology;
import fterms.exceptions.FeatureTermException;

public abstract class Distance {
	public abstract double distance(FeatureTerm f1,FeatureTerm f2,Ontology o,FTKBase dm) throws FeatureTermException;
}
