/*
 * Creator: Santi Onta��n Villar
 */

package fterms;

import fterms.exceptions.FeatureTermException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Ontology {
	static Random s_rand = new Random();

	Symbol m_name;			// ontology name
	Symbol m_description;	// ontology description

	HashMap<String,Sort> sort_list = new HashMap<String,Sort>();
	HashMap<String,Sort> undefined_sort_list = new HashMap<String,Sort>();

	List<Ontology> m_super = new LinkedList<Ontology>(); // List of ontologies used
	

	public Ontology()
	{
		m_name=null;
		m_description=null;
	} /* Ontology::Ontology */ 

	
	public void uses(Ontology o) {
		m_super.add(o);
	}


	Sort getSortInternal(String name)
	{		
		Sort s = sort_list.get(name);
		if (s!=null) return s;
		
		for(Ontology o:m_super) {
			s = o.getSortInternal(name);
			if (s!=null) return s;
		}

		return null;
	} /* Ontology::get_sort_internal */ 


	Sort getSortInternal(Symbol name)
	{
		Sort s = sort_list.get(name.toString());
		if (s!=null) return s;
		
		for(Ontology o:m_super) {
			s = o.getSortInternal(name);
			if (s!=null) return s;
		}

		return null;

	} /* Ontology::get_sort_internal */ 


	public Sort getSort(String name) throws FeatureTermException
	{
		if (name==null) return null;
		Sort s = sort_list.get(name);
		if (s!=null) return s;
		s = undefined_sort_list.get(name);
		if (s!=null) return s;
		
		for(Ontology o:m_super) {
			s = o.getSortInternal(name);
			if (s!=null) return s;
		}

		s=new Sort(new Symbol(name),null,this);
		s.m_defined=false;
		undefined_sort_list.put(name,s);
		
		return s;
	} /* Ontology::get_sort */ 



	public Sort newSort(String name,String a_super,String []fnames,String []fsorts) throws FeatureTermException
	{
		Sort s=getSortInternal(name);
		
		if (s==null) {
			int i;

			s=undefined_sort_list.get(name);

			if (s==null) {
				s=new Sort();
				sort_list.put(name,s);
			} else {
				undefined_sort_list.remove(name);
				sort_list.put(name,s);
			} // if  
			s.m_ontology=this;
			s.m_name=new Symbol(name);
			s.m_super=getSort(a_super);
			if (s.m_super!=null) s.m_super.m_subsorts.add(s);

			for(i=0;i<fnames.length;i++) {
				s.addFeature(fnames[i],fsorts[i],null,this,false);
			} // for  

			s.m_data_type=Sort.DATATYPE_FEATURETERM;
        } else {
            System.err.println("Ontology.newSort: Sort '" + name + "' was already defined!!");
		} // if  

		return s;
	} // Ontology::new_sort  
 

	public Sort getSort(Symbol n) throws FeatureTermException
	{
		return getSort(n.get());
	} /* Ontology::get_sort */ 


	public Sort newSort(Symbol name,Sort a_super,Symbol []fnames,Sort []fsorts)
	{
		Sort s=getSortInternal(name);

		if (s==null) {
			int i;

			s=undefined_sort_list.get(name.toString());

			if (s==null) {
				s=new Sort();
				sort_list.put(name.toString(),s);
				s.m_name=name;
				s.m_ontology=this;
			} else {
				undefined_sort_list.remove(name.toString());
				sort_list.put(name.toString(),s);
			} /* if */ 
			s.m_super=a_super;
			if (s.m_super!=null) s.m_super.m_subsorts.add(s);

			if (fnames!=null) {
				for(i=0;i<fnames.length;i++) {
					s.addFeature(fnames[i],fsorts[i],null,false);
				} /* for */ 
			}

			s.m_data_type=Sort.DATATYPE_FEATURETERM;
		} /* if */ 
		
		return s;
	} /* Ontology::new_sort */ 


	public void deleteSort(Sort s)
	{
		sort_list.remove(s);
	} /* if */  


	public Sort getRandomSort()
	{
		return sort_list.get(s_rand.nextInt(sort_list.size()));
	} /* Ontology::get_random_sort */ 


	public int getNSorts()
	{
		return sort_list.values().size()+undefined_sort_list.values().size();
	} /* Ontology::get_nsorts */ 


	public Collection<Sort> getSorts()
	{
		return sort_list.values();
	} /* Ontology::get_sort_num */ 

	public Collection<Sort> getUndefinedSorts()
	{
		return undefined_sort_list.values();
	} /* Ontology::get_sort_num */ 

	public void setName(String name) throws FeatureTermException
	{
		m_name = new Symbol(name);
	} /* Ontology::set_name */ 


	public void setName(Symbol name)
	{
		m_name=name;
	} /* Ontology::set_name */  


	public void setDescription(String des) throws FeatureTermException
	{
		m_description=new Symbol(des);
	} /* Ontology::set_description */  


	public void setDescription(Symbol des)
	{
		m_description=des;
	} /* Ontology::set_description */


	public int getNUndefinedSorts() {
		
		return undefined_sort_list.size();
	} 

	public String getDescription() {
		String tmp = "Ontology:\n";
		
		for(Sort s:sort_list.values()) {
			tmp+=s.getDescription() + "\n";
		}
		
		return tmp;
	}
	
}
