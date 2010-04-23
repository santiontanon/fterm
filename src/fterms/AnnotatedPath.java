package fterms;

import java.util.LinkedList;
import java.util.List;

import util.Pair;

/*
 * This class stores not only the feature name of a path, but also a reference to the original object
 */
public class AnnotatedPath {
	public List<Pair<FeatureTerm,Symbol>> features = new LinkedList<Pair<FeatureTerm,Symbol>>();
	
	public AnnotatedPath() {
	}
	
	public AnnotatedPath(AnnotatedPath p) {
		features.addAll(p.features);
	}
	
	public Path toPath() {
		Path p = new Path();
		
		for(Pair<FeatureTerm,Symbol> f_s:features) {
			p.features.add(f_s.m_b);
		}
		
		return p;
	}
	
	public String toString() {
		String tmp = null;
		for(Pair<FeatureTerm,Symbol> f_s:features) {
			if (tmp==null) {
				tmp = "(" + f_s.m_a.getSort().get() + ")" + f_s.m_b.toString();
			} else {
				tmp+=".(" + f_s.m_a.getSort().get() + ")" + f_s.m_b.toString();
			}
		}
		
		if (tmp==null) return "";
		return tmp;
	}
}
