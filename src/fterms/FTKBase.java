package fterms;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import util.RewindableInputStream;

import fterms.exceptions.FeatureTermException;


public class FTKBase {
	
	List<FeatureTerm> index = new LinkedList<FeatureTerm>();
	HashMap<String,HashSet<FeatureTerm>> name_index = new HashMap<String,HashSet<FeatureTerm>>();
	HashMap<Sort,HashSet<FeatureTerm>> sort_index = new HashMap<Sort,HashSet<FeatureTerm>>();

	List<FeatureTerm> undefined_terms = new LinkedList<FeatureTerm>();

	List<FTKBase> used_bases = new LinkedList<FTKBase>();
	

	public FTKBase()
	{
	} // FTKBase  



	FTKBase(FileInputStream fp,Ontology o)
	{
		load(fp,o);
	} // FTKBase  



	public void create_boolean_objects(Ontology o) throws FeatureTermException
	{
		AddFT(new TermFeatureTerm("true",o.getSort("boolean")));
		AddFT(new TermFeatureTerm("false",o.getSort("boolean")));
	} // FTKBase::create_boolean_objects  


	boolean save(PrintWriter fp,FTKBase domain_model)
	{
		/*
		List<FeatureTerm> l;
		FeatureTerm *f;
		FeatureTerm *f_set=FeatureTerm::SetFeatureTerm();
		char *s;

		l.Instance(index);
		l.Rewind();
		while(l.Iterate(f)) {
			f_set->addSetValue(new FeatureTerm(f));
		} // while 
		s=f_set->toStringNOOS(domain_model);
		fprintf(fp,"%s\n",s);
		delete []s;
		delete f_set;
		 */
		return true;
	} // FTKBase::save  


	boolean load(FileInputStream fp,Ontology o)
	{
	/*
		FeatureTerm *f_set,*f;
		List<FeatureTerm> *l;

		f_set=FeatureTerm::fromFileNOOS(fp,this,o);

		l=f_set->set_values_quick();

		l->Rewind();
		while(l->Iterate(f)) {
			AddFT(f);
		} // while 
		l=0;
		delete f_set;
		*/
		return true;
	} // FTKBase::load  

	public void AddFT(FeatureTerm f)
	{
		String name;
		HashSet<FeatureTerm> ni;
		HashSet<FeatureTerm> si;

		index.add(f);
		
		if (f.getName()==null) {
			name = null;
		} else {
			name = f.getName().get();
		}
		
		ni=name_index.get(name);
		if (ni!=null) {			
			ni.add(f);
		} else {
			ni=new HashSet<FeatureTerm>();
			ni.add(f);
			name_index.put(name,ni);
		} // if 

		si=sort_index.get(f.getSort());
		if (si!=null) {
			si.add(f);
		} else {
			si=new HashSet<FeatureTerm>();
			si.add(f);
			sort_index.put(f.getSort(),si);
		} // if  

	} // FTKBase::AddFT  


	public void AddUndefinedFT(FeatureTerm f)
	{
		AddFT(f);

		undefined_terms.add(f);
	} // FTKBase::AddFT  
	
	
	public FeatureTerm SearchUndefinedFT(Symbol name)
	{
		Set<FeatureTerm> l;
		FeatureTerm found=null;

		l=SearchFT(name);

		for(FeatureTerm f:l) {
			if (undefined_terms.contains(f)) {
				found=f;
				break;
			} // if   			
		}
		
		return found;
	} // FTKBase::SearchUndefinedFT  
	

	public void DeleteFT(FeatureTerm f)
	{
		Set<FeatureTerm> ni;
		Set<FeatureTerm> si;

		ni=name_index.get(f.getName().get());
		si=sort_index.get(f.getSort());

		if (ni!=null) {
			ni.remove(f);
		} // if  

		if (si!=null) {
			si.remove(f);
		} // if  

		index.remove(f);
		undefined_terms.remove(f);
	} // FTKBase::DeleteFT  


	public Set<FeatureTerm> SearchFT(Symbol name)
	{
		HashSet<FeatureTerm> l;
		HashSet<FeatureTerm> ni;

		l=new HashSet<FeatureTerm>();

		ni=name_index.get(name.get());
		if (ni!=null) l.addAll(ni);

		// Search in the used memories:  
		for (FTKBase b:used_bases) {
			l.addAll(b.SearchFT(name));
		}
		
//		System.err.println("Found " + l.size() + " terms with name " + name);

		return l;
	} // FTKBase::SearchFT  


	public Set<FeatureTerm> SearchFT(Sort s)
	{
		HashSet<FeatureTerm> l,si;

		l=new HashSet<FeatureTerm>();

		si=sort_index.get(s);
		if (si!=null) l.addAll(si);
		
		// Search in the used memories:  
		for (FTKBase b:used_bases) {
			l.addAll(b.SearchFT(s));
		}

                for(Sort s2:s.getSubSorts()) {
                    l.addAll(SearchFT(s2));
                }
		
//		System.err.println("Found " + l.size() + " terms with sort " + (s==null ? "-":s.get()));		

		return l;
	} // FTKBase::SearchFT  

/*
	List<FeatureTerm> *FTKBase::RetrieveFT(FeatureTerm *pattern)
	{
		List<FeatureTerm> *l,l2;
		FeatureTerm *f;
		Sort *s;

		s=pattern->getSort();

		l=new List<FeatureTerm>;

		{
			List<KBSortIndex> l3;
			KBSortIndex *si;

			l3.Instance(sort_index);
			l3.Rewind();
			while(l3.Iterate(si)) {
				if (s->isSubsort(si->s)) {
					l2.Instance(si->fterms);
					l2.Rewind();
					while(l2.Iterate(f)) {
						if (pattern->subsumes(f)) l->Add(f);
					} // if  
				} // if  
			} // while  
		}	

		// Search in the used memories:  
		{
			List<FTKBase> l2;
			FTKBase *b;
			List<FeatureTerm> *res;

			l2.Instance(used_bases);
			l2.Rewind();
			while(l2.Iterate(b)) {
				res=b->RetrieveFT(pattern);
				while(!res->EmptyP()) l->Add(res->ExtractIni());
				delete res;
			} // while  
		}

		return l;
	} // FTKBase::RetrieveFT  


	List<FeatureTerm> *FTKBase::LocalSearchFT(Symbol *name)
	{
		List<FeatureTerm> *l,l2;
		FeatureTerm *f;
		KBNameIndex *ni;

		l=new List<FeatureTerm>;

		ni=SearchName(name);

		if (ni!=0) {
			l2.Instance(ni->fterms);
			l2.Rewind();
			while(l2.Iterate(f)) l->Add(f);
		} // if  

		return l;
	} // FTKBase::SearchFT  


	List<FeatureTerm> *FTKBase::LocalSearchFT(Sort *s)
	{
		List<FeatureTerm> *l,l2;
		FeatureTerm *f;

		l=new List<FeatureTerm>;

		{
			List<KBSortIndex> l3;
			KBSortIndex *si;

			l3.Instance(sort_index);
			l3.Rewind();
			while(l3.Iterate(si)) {
				if (s==0 || s->isSubsort(si->s)) {
					l2.Instance(si->fterms);
					l2.Rewind();
					while(l2.Iterate(f)) l->Add(f);
				} // if  
			} // while  
		}

		return l;
	} // FTKBase::SearchFT  


	List<FeatureTerm> *FTKBase::LocalRetrieveFT(FeatureTerm *pattern)
	{
		List<FeatureTerm> *l,l2;
		FeatureTerm *f;
		Sort *s;

		s=pattern->getSort();

		l=new List<FeatureTerm>;

		{
			List<KBSortIndex> l3;
			KBSortIndex *si;

			l3.Instance(sort_index);
			l3.Rewind();
			while(l3.Iterate(si)) {
				if (s->isSubsort(si->s)) {
					l2.Instance(si->fterms);
					l2.Rewind();
					while(l2.Iterate(f)) {
						if (pattern->subsumes(f)) l->Add(f);
					} // if  
				} // if  
			} // while  
		}	

		return l;
	} // FTKBase::RetrieveFT  
*/

	public boolean ImportNOOS(String filename,Ontology o) throws IOException, FeatureTermException
	{
		boolean retval=false;
		RewindableInputStream fp;
		
		fp = new RewindableInputStream(new FileInputStream(new File(filename)));

		retval=ImportNOOS(fp,o);
		fp.close();

		return retval;
	} // FTKBase::ImportNOOS  


	public boolean ImportNOOS(RewindableInputStream fp,Ontology o) throws IOException, FeatureTermException
	{
		NOOSToken token=null;
		boolean end;
		int state=0;
		int pos=0,lastpos=0;
		
		end=false;
		pos = fp.position();
		do{
			lastpos = fp.position();
			token=NOOSToken.getTokenNOOS(fp);

			if (token!=null) {
				switch(state) {
				case 0: if (token.type==NOOSToken.TOKEN_LEFT_PAR) {
							state=1;
							pos=lastpos;
						} else {
							end=true;
						} // if
						break;

				case 1: if (token.type==NOOSToken.TOKEN_SYMBOL) {
							if (token.token.equals("define-ontology")) state=2;
							if (token.token.equals("define-sort")) state=3;
							if (token.token.equals("define-domain-model")) state=4;
							if (token.token.equals("define") ||
								token.token.equals("define-episode")) {
								fp.position(pos);
								FeatureTerm f=NOOSParser.parse(fp,this,o);
								if (f!=null) {
//									printf("term added %s\n",(f->getName()!=0 ? f->getName()->get() : "?"));
									AddFT(f);
									state=0;
								} else {
									end=true;
								} // if  
							} // if  
							if (state==1) end=true;
						} else {
							end=true;
						} // if  
						break;

				case 2: // define-ontology:  
						if (token.type==NOOSToken.TOKEN_SYMBOL) {
							int npar=1;
							Symbol s;

							s=new Symbol(token.token);
							o.newSort(s,o.getSort("any"),null,null);

							while(npar!=0 && !end) {
								token=NOOSToken.getTokenNOOS(fp);
								if (token==null) end=true;
								if (token.type==NOOSToken.TOKEN_LEFT_PAR) npar++;
								if (token.type==NOOSToken.TOKEN_RIGHT_PAR) npar--;
							} // while  
							state=0;
						} else {
							end=true;
						} // if  
						break;

				case 3: // define-sort:  
						{
							boolean first=true;
							Symbol name=null,super_sort=null;
							FeatureTerm default_value;
							List<Symbol> fnames = new ArrayList<Symbol>();
							List<Symbol> fsorts = new ArrayList<Symbol>();
							List<FeatureTerm> fdefault = new ArrayList<FeatureTerm>();
							List<Boolean> fsingleton = new ArrayList<Boolean>();

							while(state==3 && !end) {
								if (token.type==NOOSToken.TOKEN_LEFT_PAR) {
									if (first) {
										token=NOOSToken.getTokenNOOS(fp);

										if (token==null || token.type!=NOOSToken.TOKEN_SYMBOL) {
											end=true;							
										} else {
											super_sort=new Symbol(token.token);

											token=NOOSToken.getTokenNOOS(fp);
											if (token==null || token.type!=NOOSToken.TOKEN_SYMBOL) {
												end=true;
											} else {
												name=new Symbol(token.token);
												token=NOOSToken.getTokenNOOS(fp);
												if (token.type!=1) end=true;
												first=false;
											} // if  
										} // if  
									} else {
										token=NOOSToken.getTokenNOOS(fp);
										if (token==null || token.type!=NOOSToken.TOKEN_SYMBOL) {
											end=true;							
										} else {
											boolean singleton=false;
											fnames.add(new Symbol(token.token));

											token=NOOSToken.getTokenNOOS(fp);

											if (token.type==NOOSToken.TOKEN_SINGLETON) {
												singleton=true;
												token=NOOSToken.getTokenNOOS(fp);
											} // if 

											if (token==null || token.type!=NOOSToken.TOKEN_SYMBOL) {
												end=true;
											} else {
												int pos2=0;
												fsorts.add(new Symbol(token.token));
												pos2=fp.position();
												token=NOOSToken.getTokenNOOS(fp);
												if (token.type==NOOSToken.TOKEN_RIGHT_PAR) {
													fdefault.add(null);
												} else {
													fp.position(pos2);
													default_value=NOOSParser.parse(fp,this,o);
													if (default_value!=null) {
														fdefault.add(default_value);
														token=NOOSToken.getTokenNOOS(fp);
														if (token.type!=NOOSToken.TOKEN_RIGHT_PAR) end=true;
													} else {
														end=true;
													} // if 
												} // if 
												fsingleton.add(singleton);
											} // if  
										} // if 

									} // if  
								} else if (token.type==NOOSToken.TOKEN_RIGHT_PAR) {
									// Create the sort with the corresponding features:  
									Sort s=o.newSort(name,o.getSort(super_sort),null,null);
									Symbol s1,s2;
									boolean singleton;

									while(!fnames.isEmpty()) {
										s1=fnames.remove(0);
										s2=fsorts.remove(0);
										default_value=fdefault.remove(0);
										singleton=fsingleton.remove(0);
										s.addFeature(s1,o.getSort(s2),default_value,singleton);
									} // while  

									state=0;							
								} else {
									end=true;
								} // if  	
								
								if (!end && state==3) {
									token=NOOSToken.getTokenNOOS(fp);
									if (token==null) end=true;
								} // if  
							} // while  
						}
						break;

				case 4: // define-domain-model:  
						if (token.type==NOOSToken.TOKEN_SYMBOL) {
							int npar=1;
							while(npar!=0 && !end) {
								token=NOOSToken.getTokenNOOS(fp);
								if (token==null) end=true;
								if (token!=null && token.type==NOOSToken.TOKEN_LEFT_PAR) npar++;
								if (token!=null && token.type==NOOSToken.TOKEN_RIGHT_PAR) npar--;
							} // while  
							state=0;
						} else {
							end=true;
						} // if  
						break;

				} // switch  
				token=null;
			} else {
				end=true;
			} // if  
		}while(!end);
		return true;
	} // FTKBase::ImportNOOS  


	public int get_n_undefined_terms()
	{
		return undefined_terms.size();
	} // FTKBase::get_n_undefined_terms  


	public boolean contains(FeatureTerm f)
	{
		if (f.getName()!=null) {
                    HashSet<FeatureTerm> ni;

                    ni=name_index.get(f.getName().get());
                    if (ni!=null) if (ni.contains(f)) return true;
		} else {
                    HashSet<FeatureTerm> si;

                    si = sort_index.get(f.getSort());
                    if (si!=null) if (si.contains(f)) return true;
		} // if

                // Search in the used memories:
                for (FTKBase b:used_bases) {
                    if (b.contains(f)) return true;
                }

		return false;
	} // FTKBase::memberP  


	public void uses(FTKBase base)
	{
		used_bases.add(base);
	} // FTKBase::uses  


	public void print_status() {
		System.out.println("Name index:");
		for(String key:name_index.keySet()) {
			System.out.print(key + " -> " + name_index.get(key).size() + " ");
			for(FeatureTerm f:name_index.get(key)) if (undefined_terms.contains(f)) System.out.print("U");
			System.out.println(".");
		}
		System.out.println("Sort index:");
		for(Sort key:sort_index.keySet()) {
			System.out.print(key.get() + " -> " + sort_index.get(key).size() + " ");
			for(FeatureTerm f:sort_index.get(key)) if (undefined_terms.contains(f)) System.out.print("U");
			System.out.println(".");
		}
	}
	
	public void print_undefined_terms()
	{
		for(FeatureTerm f:undefined_terms) {
			System.out.println(f.toStringNOOS());
		} // while  
	} // FTKBase::print_undefined_terms  


	public int get_n_terms()
	{
		return index.size();
	} // FTKBase::get_n_terms  


}
