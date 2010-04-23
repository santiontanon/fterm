package fterms.learning;

import java.util.List;

import fterms.FTKBase;
import fterms.FeatureTerm;
import fterms.Ontology;
import fterms.Path;

public abstract class InductiveLearner {
	public abstract Hypothesis generateHypothesis(List<FeatureTerm> examples,Path dp,Path sp,Ontology o,FTKBase dm) throws Exception;
}
