package fterms;

import fterms.exceptions.FeatureTermException;

public class BaseOntology extends Ontology {
	
	public BaseOntology() throws FeatureTermException
	{
		Sort s;
		s=new Sort("any",null,this);
		s.m_data_type=-1;
		sort_list.put(s.get(),s);
		s=new Sort("number","any",this);
		s.m_data_type=-1;
		sort_list.put(s.get(),s);
		s=new Sort("float","number",this);
		s.m_data_type=1;
		sort_list.put(s.get(),s);
		s=new Sort("integer","number",this);
		s.m_data_type=0;
		sort_list.put(s.get(),s);
		s=new Sort("symbol","any",this);
		s.m_data_type=2;
		sort_list.put(s.get(),s);
		s=new Sort("boolean","any",this);
		s.m_data_type=-1;
		sort_list.put(s.get(),s);

		m_name=new Symbol("NOOS Base Ontology");
		m_description=new Symbol("By Santiago Ontanon");
	} /* Ontology::Ontology */ 
}
