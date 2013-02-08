/*
 * Creator: Santi Ontanon Villar
 */

package fterms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import fterms.exceptions.FeatureTermException;


public class Sort {
	public final static int DATATYPE_ABSTRACT = -1;
	public final static int DATATYPE_INTEGER = 0;
	public final static int DATATYPE_FLOAT = 1;
	public final static int DATATYPE_SYMBOL = 2;
	public final static int DATATYPE_FEATURETERM = 3;
	public final static int DATATYPE_SET = 4;

	static Random s_rand = new Random();

	Symbol m_name;
	Sort m_super;
	Ontology m_ontology;
	HashSet<Sort> m_subsorts = new HashSet<Sort>();

	boolean m_defined;	// when a sort has been used but not defined, this variable takes 'true' as value 

	int m_data_type;	

	HashMap<String,Integer> m_feature_position = new HashMap<String,Integer>();
	
	List<Symbol> m_feature_names = new ArrayList<Symbol>();
	List<Sort> m_feature_sorts = new ArrayList<Sort>();
	List<FeatureTerm> m_feature_default = new ArrayList<FeatureTerm>();
	List<Boolean> m_feature_singleton = new ArrayList<Boolean>();		// Determines whether the feature can have a single value or if it can have many values

	Sort()
	{
		m_defined=true;

		m_name=null;
		m_super=null;
		m_ontology=null;

		m_data_type=-1;

	} // Sort::Sort  


	public Sort(Symbol name,Sort a_super,Ontology o)
	{
		m_defined=true;

		m_name=name;
		m_super=a_super;
		if (m_super!=null) {
			m_super.m_subsorts.add(this);
		} // if
		m_ontology=o;

		m_data_type=-1;
		if (m_super!=null) m_data_type=m_super.m_data_type;
	} // Sort::Sort  



	public Sort(String name,String a_super,Ontology o) throws FeatureTermException
	{
		m_defined=true;

		m_name=new Symbol(name);
		m_super=o.getSort(a_super);
		if (m_super!=null) {
			m_super.m_subsorts.add(this);
//			printf("Adding %s to %s\n",name,m_super->get());
		} // if
		m_ontology=o;

		m_data_type=-1;
		if (m_super!=null) m_data_type=m_super.m_data_type;
	}


    public String toString() {
        return m_name.get();
    }


	public boolean inSort(int i)
	{
		if (m_data_type==-1) return true;
		if (m_data_type==0) return true;
		return false;
	} // Sort::inSort  


	public boolean inSort(float f)
	{
		if (m_data_type==-1) return true;
		if (m_data_type==0) return true;
		return false;
	} // Sort::inSort  


	public boolean inSort(Symbol s)
	{	
		if (m_data_type==-1) return true;
		if (m_data_type==2) return true;
		return false;
	} // Sort:inSort  


	public boolean inSort(FeatureTerm f)
	{
		Sort s;

        if (f instanceof SetFeatureTerm) {
            for(FeatureTerm v:((SetFeatureTerm)f).getSetValues()) {
                if (!inSort(v)) return false;
            }
            return true;
        }

		s=f.getSort();
		if (s==null) return true;
		if (s==this) return true;

		while(s.m_super!=null) {
			s=s.m_super;
			if (s==this) return true;
		} // while  
		return false;
	} // Sort::inSort  


	public FeatureTerm random() throws FeatureTermException
	{
		switch(m_data_type) {
		case DATATYPE_ABSTRACT:
		{
			Sort s=m_ontology.getRandomSort();
			if (s!=null) return s.random();
			return null;
		}
		case DATATYPE_INTEGER: return new IntegerFeatureTerm(s_rand.nextInt(),m_ontology);
		case DATATYPE_FLOAT: return new FloatFeatureTerm(s_rand.nextFloat(), m_ontology);
		case DATATYPE_SYMBOL:
		{
			String tmp="";
			int i;

			tmp=tmp + ('a'+(s_rand.nextInt(26)));

			for(i=1;i<255 && (s_rand.nextInt(16))!=0;i++) {
				tmp=tmp + ('a'+(s_rand.nextInt(26)));
			} // for  

			return new SymbolFeatureTerm(new Symbol(tmp),m_ontology);
		}

		case DATATYPE_FEATURETERM: // Generate a random Feature Term:  
		}

		return null;
	} // Sort::random  

	public Symbol getName() {
		return m_name;
	}

    public String get() {
		return m_name.get();
	}

	public int getDataType() {
		return m_data_type;
	}

	public void setDataType(int dt) {
		m_data_type = dt;
	}

	public Ontology getOntology() {
		return m_ontology;
	}


	public boolean hasFeature(Symbol name)
	{
		if (m_feature_position.get(name.get())!=null) return true;
//		if (m_feature_names.contains(name)) return true;
		if (m_super!=null) return m_super.hasFeature(name);
		return false;
	} // hasFeature  


	public boolean hasFeature(String name)
	{
//		Symbol n = new Symbol(name);
		if (m_feature_position.get(name)!=null) return true;
//		if (m_feature_names.contains(n)) return true;
		if (m_super!=null) return m_super.hasFeature(name);
		return false;		
	} // hasFeature  


	public Sort featureSort(Symbol name)
	{
		Integer pos=-1;
		pos = m_feature_position.get(name.get());
		if (pos!=null) {
			return m_feature_sorts.get(pos);
		} else {
			if (m_super!=null) return m_super.featureSort(name);
			return null;
		} // if  
	} // featureSort  


	public Sort featureSort(String name)
	{
		Integer pos=-1;
		pos = m_feature_position.get(name);
		if (pos!=null) {
			return m_feature_sorts.get(pos);
		} else {
			if (m_super!=null) return m_super.featureSort(name);
			return null;
		} // if  
	} // Sort::featureSort  


	public FeatureTerm featureDefaultValue(Symbol name)
	{
		Integer pos=-1;
		pos = m_feature_position.get(name.get());
		if (pos!=null) {
			return m_feature_default.get(pos);
		} else {
			if (m_super!=null) return m_super.featureDefaultValue(name);
			return null;
		} // if  
	} // Sort::featureDefaultValue  


	public FeatureTerm featureDefaultValue(String name)
	{
		Integer pos=-1;
		pos = m_feature_position.get(name);
		if (pos!=null) {
			return m_feature_default.get(pos);
		} else {
			if (m_super!=null) return m_super.featureDefaultValue(name);
			return null;
		} // if  
	} // Sort::featureDefaultValue  


	public boolean featureSingleton(Symbol name)
	{
		Integer pos=-1;
		pos = m_feature_position.get(name.get());
		if (pos!=null) {
			return m_feature_singleton.get(pos);
		} else {
			if (m_super!=null) return m_super.featureSingleton(name);
			return false;
		} // if  
	} // Sort::featureSingleton  


	public boolean featureSingleton(String name)
	{
		Integer pos=-1;
		pos = m_feature_position.get(name);
		if (pos!=null) {
			return m_feature_singleton.get(pos);
		} else {
			if (m_super!=null) return m_super.featureSingleton(name);
			return false;
		} // if  
	} // Sort::featureSingleton  

	public Sort Antiunification(Sort s) {
		Sort au=this;
		if (s==null) return null;

		while(au!=null) {
			if (au.isSubsort(s)) return au;
			au=au.m_super;
		} // while  
		return null;
	} // Sort::Antiunification  


	public Sort Unification(Sort s)
	{
		if (this.isSubsort(s)) return s;
		if (s.isSubsort(this)) return this;

		return null;
	} // Sort::Unification  


        public boolean subsumes(Sort s) {
            if (s==this || this.isSubsort(s)) return true;
            
            return false;
        }
        
	/*
	 * Returns true if 's' is subsort of 'this'
	 */
	public boolean isSubsort(Sort s)
	{
		if (s==null) return false;
		if (s==this) return true;
		if (s.m_super!=null) return isSubsort(s.m_super);
		
		return false;
	} // Sort::isSubsort  


	public boolean is_a(Sort s)
	{

		if (s==this) return true;

		for(Sort s2:s.m_subsorts) if (is_a(s2)) return true;		

		return false;
	} // Sort::is_a  


	// returns the distance in the sort hierarchy (only if this is a subsort of s)
	public int refinementSteps(Sort s)
	{
		int tmp;

		if (s==this) return 0;

		for(Sort s2:m_subsorts) {
			tmp=s2.refinementSteps(s);
			if (tmp>=0) return tmp+1;
		} // for

		return -1;
	} // Sort::isSubsort  


	public Set<Sort> getSubSorts()
	{
		return m_subsorts;
	} // Sort::getSubSorts  


	public void addFeature(Symbol name,Sort sort,FeatureTerm defaultvalue,boolean singleton)
	{
		m_feature_position.put(name.get(), m_feature_names.size());
		m_feature_names.add(name);
		m_feature_sorts.add(sort);
		m_feature_default.add(defaultvalue);
		m_feature_singleton.add(singleton);
	} // Sort::addFeature  



	public void addFeature(String name,String sort,FeatureTerm defaultvalue,Ontology o,boolean singleton) throws FeatureTermException
	{
		m_feature_position.put(name, m_feature_names.size());
		m_feature_names.add(new Symbol(name));
		m_feature_sorts.add(o.getSort(sort));
		m_feature_default.add(defaultvalue);
		m_feature_singleton.add(singleton);
	} // Sort::addFeature  

	public List<Symbol> getFeatures()
	{
		if (m_super==null) {	
			return m_feature_names;
		} else {
			List<Symbol> l=new LinkedList<Symbol>();
			l.addAll(m_super.getFeatures());
            for(Symbol f:m_feature_names)
                if (!l.contains(f)) l.add(f);
			return l;
		} // if 
	} // Sort::getFeatures  


	public Sort getSuper()
	{
		return m_super;
	} // Sort::getSuper 


	public FeatureTerm createFeatureTerm() throws FeatureTermException {
		switch(m_data_type) {
		case DATATYPE_INTEGER:	
			{
				FeatureTerm f = new IntegerFeatureTerm(null,m_ontology);
				f.setSort(this);
				return f;
			}
		case DATATYPE_FLOAT:	
			{
				FeatureTerm f = new FloatFeatureTerm(null,m_ontology);
				f.setSort(this);
				return f;
			}
		case DATATYPE_SYMBOL:	
			{
				FeatureTerm f = new SymbolFeatureTerm(null,m_ontology);
				f.setSort(this);
				return f;
			}
		case DATATYPE_FEATURETERM:	
			{
				FeatureTerm f = new TermFeatureTerm((Symbol)null,this);
				return f;
			}
		case DATATYPE_ABSTRACT:	
			{
				FeatureTerm f = new TermFeatureTerm((Symbol)null,this);
				return f;				
			}
		}
		return null;
	}
	
	
	public String getDescription() {
		String tmp = "[sort: name = " + m_name.get() + ", datatype = " + m_data_type + ", super = " + (m_super==null ? "-":m_super.get()) + ", features = {";
		for(int i=0;i<m_feature_names.size();i++) {
			tmp += "(" + m_feature_names.get(i).get() + "," + m_feature_sorts.get(i).get() + ")";
		}
		
		tmp +="} ]";
		return tmp;
	}

}

