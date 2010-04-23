package fterms.learning.propositional;

import weka.classifiers.Classifier;
import weka.core.Instance;
import fterms.FTKBase;
import fterms.FeatureTerm;
import fterms.learning.Hypothesis;
import fterms.learning.Prediction;
import fterms.learning.propositional.NOOSToWeka.ConversionRecord;

public class WekaHypothesis extends Hypothesis {
	Classifier m_classifier = null;
	ConversionRecord m_record = null;
	
	public WekaHypothesis(Classifier c,ConversionRecord record) {
		m_classifier = c;
		m_record = record;
	}

	
	public void copy(WekaHypothesis h) throws Exception {
		m_classifier = ((WekaHypothesis)h).m_classifier;
	}

	public Prediction generatePrediction(FeatureTerm problem,FTKBase dm, boolean debug) throws Exception
	{
		Prediction p = new Prediction(problem);
	
		Instance inst = NOOSToWeka.translateInstance(m_record.problemsToCases.get(problem), m_record.allCases, m_record.allWekaCases);
		double result = m_classifier.classifyInstance(inst);
		FeatureTerm solution = m_record.solutionMapping[(int)result];
		
		p.solutions.add(solution);
		p.support.put(solution,1);
		return p;
	} // Hypothesis::generate_prediction  


	public String  toString(FTKBase dm)
	{		
		return m_classifier.toString();
	} // Hypothesis::show_rule_set 

	public String  toCompactString(FTKBase dm)
	{
		return "WekaHypothesis(" + m_classifier.getClass().getName() + ")";
	} // Hypothesis::show_rule_set 
	
}
