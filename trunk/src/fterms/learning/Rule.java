package fterms.learning;

import fterms.FTKBase;
import fterms.FeatureTerm;

public class Rule {
	public FeatureTerm pattern;
	public FeatureTerm solution;
	public float reliability;
	public int support;
	
	public Rule(FeatureTerm p,FeatureTerm s) {
		pattern = p;
		solution = s;
		reliability = 0;
		support = 0;
	}

    public Rule(FeatureTerm p,FeatureTerm s,float r,int supp) {
		pattern = p;
		solution = s;
		reliability = r;
		support = supp;
	}
	
	public String toStringNOOS(FTKBase dm) {
		String ret = "* Rule:\nPattern:\n" + pattern.toStringNOOS(dm) + "\n";
		ret += "Solution: " + solution.toStringNOOS(dm) + "\n";
		ret += "Reliability: " + reliability + "\nSupport: " + support;
		return ret;
	}
};