package fterms;

import java.util.LinkedList;
import java.util.List;

public class Path {
	public List<Symbol> features = new LinkedList<Symbol>();
	
	public Path() {
	}
	
	public Path(Path p) {
		features.addAll(p.features);
	}
	
	public int size() {
		return features.size();
	}
	
	public String toString() {
		String tmp = null;
		for(Symbol s:features) {
			if (tmp==null) {
				tmp = s.toString();
			} else {
				tmp+="." + s.toString();
			}
		}
		
		if (tmp==null) return "";
		return tmp;
	}
	
	public boolean equals(Object o) {
		if (o instanceof Path) {
			Path p2 = (Path)o;			
			if (features.size()!= p2.features.size()) return false;
			for(int i=0;i<features.size();i++) {
				if (!features.get(i).equals(p2.features.get(i))) return false;
			}
			return true;
		}
		return false;
	}
	
    public int hashCode() {
    	int hc = 0;
    	for(Symbol f:features) {
    		hc+=f.hashCode();
    	}
    	return hc;
    }
}
