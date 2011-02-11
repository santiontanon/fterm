package fterms.subsumption;

import fterms.FeatureTerm;

public class SubsumptionStackNode {
	FeatureTerm m_f1;
	FeatureTerm m_f2;
	int m_state;
//	int m_feature_pos;

	int []m_assignment;
	FeatureTerm m_set1[],m_set2[];
	int m_set1l,m_set2l;
	int m_assignment_pos;
	
	
	SubsumptionStackNode(FeatureTerm f1,FeatureTerm f2,int state,
//			 int feature_pos,
			 int []assignment,FeatureTerm set1[],int set1l,FeatureTerm set2[],int set2l,int assignment_pos) {
		
		assert(f1!=null || f2!=null);
		
		m_f1=f1;
		m_f2=f2;
		m_state=state;
//		m_feature_pos=feature_pos;
		
		m_assignment=assignment;
		m_set1=set1;
		m_set2=set2;
		m_set1l=set1l;
		m_set2l=set2l;
		m_assignment_pos=assignment_pos;
	} // SubsumptionStackNode  
}
