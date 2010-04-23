package fterms.specialterms;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import fterms.FTKBase;
import fterms.FeatureTerm;
import fterms.FloatFeatureTerm;
import fterms.IntegerFeatureTerm;
import fterms.Ontology;
import fterms.SetFeatureTerm;
import fterms.Sort;
import fterms.SpecialFeatureTerm;
import fterms.Symbol;
import fterms.exceptions.FeatureTermException;

public class FloatInterval extends SpecialTerm {
	
	static class FloatFTComparator implements Comparator {

		public int compare(Object arg0, Object arg1) {
			double o0 = (Float)arg0;
			double o1 = (Float)arg1;

			if( o0 > o1 )
				return 1;
				else if( o0 < o1 )
				return -1;
				else
				return 0;		
			}
		
	}

	
	FeatureTerm m_min,m_max;

	public FloatInterval(FeatureTerm min, FeatureTerm max) {
		m_min = min;
		m_max = max;
//		System.out.println("NEW FloatInterval CREATED!!!!!!!!!!!!!!!!!!!!!!!");
	}
	
	public static void createSort(Ontology o) throws FeatureTermException {
		Sort s;
		s=o.newSpecialSort("float-interval","float",new String[]{"min","float"},new String[]{"max","float"});
		s.setSpecialSeed(new FloatInterval(null,null));
	}

	public void takeValues(FeatureTerm parent) throws FeatureTermException {
		m_min = (parent.featureValue("min"));
		m_max = (parent.featureValue("max"));
	}

	public SpecialTerm newSpecialTerm() {
		return new FloatInterval(null,null);
	}
	
	public SpecialTerm clone(FTKBase dm,Ontology o, HashMap<FeatureTerm,FeatureTerm> correspondences) throws FeatureTermException {
		FeatureTerm c_min = (m_min == null ? null:m_min.clone(dm, o, correspondences));
		FeatureTerm c_max = (m_max == null ? null:m_max.clone(dm, o, correspondences));
		return new FloatInterval(c_min,c_max);
	}

	public boolean subsumes(FeatureTerm f) {
		
//		System.out.println("FloatInterval.subsumption!");
//		System.out.println("[" + (m_min==null?"-":m_min.toStringNOOS()) + "," + (m_max==null?"-":m_max.toStringNOOS()) + "]");
//		System.out.println(f.toStringNOOS());
		
		if (f instanceof SetFeatureTerm) {
			for(FeatureTerm f2:((SetFeatureTerm)f).getSetValues()) {
				if (subsumes(f2)) return true;
			}
			return false;
		} else 	if (f instanceof FloatFeatureTerm || (f instanceof IntegerFeatureTerm)) {
			if (!f.hasValue()) return false;
			float v=(f instanceof FloatFeatureTerm ? ((FloatFeatureTerm)f).getValue():((IntegerFeatureTerm)f).getValue());
			if (m_min!=null && v<((FloatFeatureTerm)m_min).getValue()) return false;
			if (m_max!=null && v>((FloatFeatureTerm)m_max).getValue()) return false;
			return true;
		} else if (f instanceof SpecialFeatureTerm && ((SpecialFeatureTerm)f).getValue() instanceof FloatInterval) {
			FloatInterval st=(FloatInterval)(((SpecialFeatureTerm)f).getValue());
			if (st==null) return false;
			if (m_min!=null) {
				if (st.m_min==null) return false;
				if (((FloatFeatureTerm)m_min).getValue()>((FloatFeatureTerm)st.m_min).getValue()) return false;
			} // if 
			if (m_max!=null) {
				if (st.m_max==null) return false;
				if (((FloatFeatureTerm)m_max).getValue()<((FloatFeatureTerm)st.m_max).getValue()) return false;
			} // if 
			return true;
		} // if 
		return false;
	}
	
	
	public List<FeatureTerm> specializations(FTKBase dm, Ontology o, Sort m_sort)
	{
		// Not implemented, since the set would be infinite, just return an empty list
		return new LinkedList<FeatureTerm>();
	}
	
	
	public List<FeatureTerm> specializationsSubsumingAll(FTKBase dm, Ontology ontology, Sort sort,List<FeatureTerm> objects) throws FeatureTermException
	{
		List<FeatureTerm> refinements = new LinkedList<FeatureTerm>();		
		float objects_min = 0,objects_max = 0,v;
		boolean no_min=false,no_max=false;
		FeatureTerm f2;
		boolean first=true;

		if (objects==null || objects.size()==0) return refinements;

		for(FeatureTerm f:objects) {
			if (f.getDataType()==Sort.DATATYPE_FLOAT) {
				v=((FloatFeatureTerm)f).getValue();
				if (first) {
					objects_min=objects_max=v;
					first=false;
				} else {
					if (v<objects_min) objects_min=v;
					if (v>objects_max) objects_max=v;
				} // if
			} else if (f.getDataType()==Sort.DATATYPE_INTEGER) {
				v=((IntegerFeatureTerm)f).getValue();
				if (first) {
					objects_min=objects_max=v;
					first=false;
				} else {
					if (v<objects_min) objects_min=v;
					if (v>objects_max) objects_max=v;
				} // if
			} else {
				if (f.getSort()==sort) {
					f2=f.featureValue("min");
					if (f2!=null && (f2.getDataType()==Sort.DATATYPE_FLOAT)) {
						v=((FloatFeatureTerm)f2).getValue();
						if (first) {
							objects_min=objects_max=v;
							first=false;
						} else {
							if (v<objects_min) objects_min=v;
							if (v>objects_max) objects_max=v;
						} // if
					} else {
						no_min=true;
					} // if 
					f2=f.featureValue("max");
					if (f2!=null && (f2.getDataType()==Sort.DATATYPE_FLOAT)) {
						v=((FloatFeatureTerm)f2).getValue();
						if (first) {
							objects_min=objects_max=v;
							first=false;
						} else {
							if (v<objects_min) objects_min=v;
							if (v>objects_max) objects_max=v;
						} // if
					} else {
						no_max=true;
					} // if 
				} else {
					return refinements;
				} // if 
			} // if 
		} // while 

		if (m_min!=null && (((FloatFeatureTerm)m_min).getValue()>objects_min || no_min )) return refinements;
		if (m_max!=null && (((FloatFeatureTerm)m_max).getValue()<objects_max || no_max )) return refinements;

		if (!no_min) {
			if (m_min==null || ((FloatFeatureTerm)m_min).getValue()<objects_min) {
				FloatInterval st = new FloatInterval(new FloatFeatureTerm(objects_min,ontology),m_max);
				SpecialFeatureTerm sft = new SpecialFeatureTerm((Symbol)null,sort,st); 
				if (st.m_min!=null) sft.defineFeatureValue(new Symbol("min"), st.m_min);
				if (st.m_max!=null) sft.defineFeatureValue(new Symbol("max"), st.m_max);
				refinements.add(sft);
			}
		}
		if (!no_max) {
			if (m_max==null || ((FloatFeatureTerm)m_min).getValue()>objects_max) {
				FloatInterval st = new FloatInterval(m_min,new FloatFeatureTerm(objects_max,ontology));
				SpecialFeatureTerm sft = new SpecialFeatureTerm((Symbol)null,sort,st);
				if (st.m_min!=null) sft.defineFeatureValue(new Symbol("min"), st.m_min);
				if (st.m_max!=null) sft.defineFeatureValue(new Symbol("max"), st.m_max);
				refinements.add(sft);
			}
		}

		return refinements;
	}
		
	public List<FeatureTerm> specializationsSubsumingSome(FTKBase dm, Ontology ontology, Sort sort,List<FeatureTerm> objects) throws FeatureTermException
	{
		List<FeatureTerm> refinements = new LinkedList<FeatureTerm>();	
		List<Float> values = new LinkedList<Float>();
		int i;
		float v;
		FeatureTerm f2;
		
//		System.out.println("FloatInterval.specializationsSubsumingSome");

		if (objects==null || objects.size()==0) return refinements;

		for(FeatureTerm f:objects) {
			if (f.getDataType()==Sort.DATATYPE_FLOAT) {
				v=((FloatFeatureTerm)f).getValue();
				values.add(v);
			} // if 
			if (f.getSort()==sort) {
				f2=f.featureValue("min");
				if (f2!=null && f2.getDataType()==Sort.DATATYPE_FLOAT) {
					v=((FloatFeatureTerm)f2).getValue();
					values.add(v);
				} // if 
				f2=f.featureValue("max");
				if (f2!=null && f2.getDataType()==Sort.DATATYPE_FLOAT) {
					v=((FloatFeatureTerm)f2).getValue();
					values.add(v);
				} // if 
			} // if 

		} // while 
		
//		System.out.println("FloatInterval.specializationsSubsumingSome, " + values.size() + " values");

		Collections.sort(values,new FloatInterval.FloatFTComparator());
		
//		for(Float value:values) System.out.print(value + " ");
//		System.out.println("");

		for(i=0;i<values.size()-1;i++) {
			v=(values.get(i)+values.get(i+1))/2;
			if (values.get(i)!=values.get(i+1)) {
				if ((m_min==null || (((FloatFeatureTerm)m_min).getValue()<v)) &&
					(m_max==null || (((FloatFeatureTerm)m_max).getValue()>=v))) {
					FloatInterval st = new FloatInterval(new FloatFeatureTerm(v,ontology),m_max);
					SpecialFeatureTerm sft = new SpecialFeatureTerm((Symbol)null,sort,st); 
					if (st.m_min!=null) sft.defineFeatureValue(new Symbol("min"), st.m_min);
					if (st.m_max!=null) sft.defineFeatureValue(new Symbol("max"), st.m_max);
					refinements.add(sft);
				}
				if ((m_max==null || (((FloatFeatureTerm)m_max).getValue()>v)) &&
					(m_min==null || (((FloatFeatureTerm)m_min).getValue()<=v))) {
					FloatInterval st = new FloatInterval(m_min,new FloatFeatureTerm(v,ontology));
					SpecialFeatureTerm sft = new SpecialFeatureTerm((Symbol)null,sort,st); 
					if (st.m_min!=null) sft.defineFeatureValue(new Symbol("min"), st.m_min);
					if (st.m_max!=null) sft.defineFeatureValue(new Symbol("max"), st.m_max);
					refinements.add(sft);
				}
			} // if
		} // for 
		
		return refinements;
	}
	
	
	public List<FeatureTerm> generalizations(FTKBase dm, Ontology o, Sort m_sort)
	{
		List<FeatureTerm> refinements = new LinkedList<FeatureTerm>();		
		
		// ...
		
		return refinements;
	}
	
}
