package fterms;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import fterms.exceptions.FeatureTermException;


public class SetFeatureTerm extends FeatureTerm {
	List<FeatureTerm> m_set = new LinkedList<FeatureTerm>();
	
	public SetFeatureTerm() {
	}
	
	public SetFeatureTerm(String name) throws FeatureTermException {
		m_name = new Symbol(name);
	}

	public SetFeatureTerm(Symbol name) {
		m_name = name;
	}

	public void addSetValue(FeatureTerm f) {
		if (f == null) {
			System.err.println("SetFeatureTerm::addSetValue: adding a null element to a set!");
		}
		if (f instanceof SetFeatureTerm) {
			System.err.println("SetFeatureTerm::addSetValue: adding a set to a set!!! some methods do not support this...");
			if (f==this) {
				System.err.println("SetFeatureTerm::addSetValue: circular recursion in a set!!!");
			}
		}
		m_set.add(f);
	}
	
	public void addSetValueSecure(FeatureTerm f) {
		if (f == null) {
			System.err.println("SetFeatureTerm::addSetValue: adding a null element to a set!");
		}
		if (f instanceof SetFeatureTerm) {
			System.err.println("SetFeatureTerm::addSetValue: adding a set to a set!!! some methods do not support this...");
			if (f==this) {
				System.err.println("SetFeatureTerm::addSetValue: circular recursion in a set!!!");
			}
		}
                if (!m_set.contains(f)) m_set.add(f);
	}

        public void removeSetValue(FeatureTerm f) {
		m_set.remove(f);
	}

	public List<FeatureTerm> getSetValues() {
		return m_set;
	}
	
	public boolean hasValue() {
		return m_set.size()>0;
	}
	
	public FeatureTerm featureValue(Symbol feature) throws FeatureTermException
	{
		SetFeatureTerm result = new SetFeatureTerm();
		FeatureTerm f2;
		int nvalues = 0;
		FeatureTerm lastValue = null;

		for(FeatureTerm f:m_set) {
			f2=f.featureValue(feature);
			if (f2!=null) {
				if (f2 instanceof SetFeatureTerm) {
					for(FeatureTerm f3:((SetFeatureTerm) f2).getSetValues()) {
						result.addSetValue(f3);
					}
				} else {
					result.addSetValue(f2);					
				}
				lastValue = f2;
				nvalues++;
			}
		} /* while */ 
		
		if (nvalues==1) return lastValue;

		return result;
	} /* FeatureTerm::featureValue */ 
	
	
	public FeatureTerm featureValue(String feature) throws FeatureTermException
	{
		SetFeatureTerm result = new SetFeatureTerm();
		FeatureTerm f2;

		for(FeatureTerm f:m_set) {			
			f2=f.featureValue(feature);
			if (f2!=null) result.addSetValue(f2);
		} /* while */ 

		return result;
	} /* FeatureTerm::featureValue */ 
	
		
	String toStringNOOSInternal(List<FeatureTerm> bindings,int tabs,FTKBase dm)
	{
		int i;
		String tmp = "";
		int ID=-1;
		

		if (m_name!=null && dm!=null && dm.contains(this)) return m_name.get();

		ID=bindings.indexOf(this);

		if (ID==-1) {
			bindings.add(this);
			ID=bindings.indexOf(this);

			tmp += "(define ?X" + (ID +1) + " (set)";

			if (!m_set.isEmpty()) {
				tmp +="\n";
				for(i=0;i<tabs+2;i++) tmp+=" ";
			}
			
			for(FeatureTerm f:m_set) {
				tmp += f.toStringNOOSInternal(bindings,tabs+1,dm);

				if (m_set.indexOf(f)!=m_set.size()-1) {
					tmp+="\n";
					for(i=0;i<tabs+2;i++) tmp+=" ";
				} // if 
			} // for

			return tmp+")";			
		} else {
			if (m_set.isEmpty()) {
				return "(define (set))";
			} else {
				return "!X" + (ID+1);				
			}
		} // if  
		
	} // FeatureTerm::toStringNOOSInternal 
	
	
	FeatureTerm cloneInternal2(HashMap<FeatureTerm,FeatureTerm> correspondences, FTKBase dm,Ontology o) throws FeatureTermException {
		SetFeatureTerm f = new SetFeatureTerm(m_name);
		correspondences.put(this,f);
	
		for(FeatureTerm f2:m_set) {
			f.m_set.add(f2.cloneInternal(correspondences,dm,o));
		} // while  
		return f;
	}
	
	public boolean isLeaf() {
		return false;
	}

	public void substituteSetValue(int i, FeatureTerm f) {
		m_set.set(i,f);
	}

	
	public FeatureTerm clone(FTKBase dm,HashMap<FeatureTerm,FeatureTerm> correspondences) throws FeatureTermException
	{    
		SetFeatureTerm ret = new SetFeatureTerm(m_name);
		
		for(FeatureTerm f:m_set) {
			ret.addSetValue(f.clone(dm,correspondences));
		}
		return ret;
	} // FeatureTerm::clone  
	
	public boolean equals(Object o) {
		if (o instanceof SetFeatureTerm) {
			if (m_set.size()==0 && ((SetFeatureTerm)o).m_set.size()==0) return true;
			return super.equals(o);
		}
		return false;
	}

    public int hashCode() {
    	if (m_set.size()==0) return 0;
    	return super.hashCode();
    }

}
