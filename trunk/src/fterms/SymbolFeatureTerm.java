package fterms;

import fterms.exceptions.FeatureTermException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

public class SymbolFeatureTerm extends FeatureTerm {
	Symbol m_value = null;
	
	SymbolFeatureTerm(Symbol name,Symbol value,Ontology o) throws FeatureTermException {
		m_name = name;
		m_value = value;
		m_sort = o.getSort("symbol");
	}	

	public SymbolFeatureTerm(Symbol value,Ontology o) throws FeatureTermException {
		m_value = value;
		m_sort = o.getSort("symbol");
	}		
	
	public Symbol getValue() {
		return m_value;
	}
	
	public boolean hasValue() {
		return m_value!=null;
	}
	
	public boolean isConstant() {
		return m_value!=null;
	}

	String toStringNOOSInternal(List<FeatureTerm> bindings,int tabs,FTKBase dm)
	{
		int ID=-1;

		if (m_name!=null && dm!=null && dm.contains(this)) return m_name.get();

		ID=bindings.indexOf(this);

		if (ID==-1) {
			bindings.add(this);
			ID=bindings.indexOf(this);

			if (m_value==null) {
				return "(define ?X" + (ID+1) + " (symbol))";
			} else {
				return "?X" + (ID+1) + " \"" + m_value + "\"";
			} // if 
		} else {
			if (m_value==null) {
				return "!X" + (ID+1);
			} else {
				return "\"" + m_value + "\"";
			}
		} // if  
	} // FeatureTerm::toStringNOOSInternal 
	
	
	FeatureTerm cloneInternal2(HashMap<FeatureTerm,FeatureTerm> correspondences, FTKBase dm,Ontology o) throws FeatureTermException {
		FeatureTerm f = new SymbolFeatureTerm(m_name,m_value,o);
		correspondences.put(this,f);
		return f;
	}

	public boolean isLeaf() {
		return true;
	}
	
	public boolean equals(Object o) {
		if (o instanceof SymbolFeatureTerm) {
			if (m_value!=null && (((SymbolFeatureTerm)o).m_value!=null && ((SymbolFeatureTerm)o).m_value.equals(m_value))) return true;
		}
		if (o==this) return true;
		return false;
	}
	
    public int hashCode() {
    	if (m_value==null) return 0;
    	return m_value.hashCode();
    }


}
