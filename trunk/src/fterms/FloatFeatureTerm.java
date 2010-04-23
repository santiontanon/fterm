package fterms;

import fterms.exceptions.FeatureTermException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

public class FloatFeatureTerm extends FeatureTerm {
	Float m_value = null;
	
	public FloatFeatureTerm(Symbol name,Float value,Ontology o) throws FeatureTermException {
		m_name = name;
		m_value = value;
		m_sort = o.getSort("float");
	}	

	public FloatFeatureTerm(Float value,Ontology o) throws FeatureTermException {
		m_value = value;
		m_sort = o.getSort("float");
	}	
	
	public Float getValue() {
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
				return "(define ?X" + (ID+1) + " (" + m_sort.get() + "))";
			} else {
				return "" + m_value;
			} // if 
		} else {
			if (m_value==null) {
				return "!X" + (ID+1);
			} else {
				return "" + m_value;				
			}
		} // if  
	} // FeatureTerm::toStringNOOSInternal 

	
	FeatureTerm cloneInternal2(HashMap<FeatureTerm,FeatureTerm> correspondences, FTKBase dm,Ontology o) throws FeatureTermException {
		FeatureTerm f = new FloatFeatureTerm(m_name,m_value,o);
		correspondences.put(this,f);
		return f;
	}

	public boolean isLeaf() {
		return true;
	}
	
	public boolean equals(Object o) {
		if (o instanceof FloatFeatureTerm) {
			if ((((FloatFeatureTerm)o).m_value!=null && ((FloatFeatureTerm)o).m_value.equals(m_value))) return true;
		}
		if (o==this) return true;
		return false;
	}
	
    @Override
    public int hashCode() {
    	if (m_value==null) return 0;
    	return (int)(m_value*1000);
    }


}
