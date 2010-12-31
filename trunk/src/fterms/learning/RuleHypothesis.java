package fterms.learning;

import java.util.LinkedList;
import java.util.List;

import fterms.FTKBase;
import fterms.FeatureTerm;
import fterms.exceptions.FeatureTermException;


public class RuleHypothesis extends Hypothesis {
	boolean m_ordered = false;
	protected List<Rule> m_rules = new LinkedList<Rule>();
	protected FeatureTerm m_default_solution = null;


	public RuleHypothesis() {
		
	}
	
	public RuleHypothesis(boolean ordered) {
		m_ordered = ordered;
	}

	public RuleHypothesis(RuleHypothesis h) {
		m_ordered = h.m_ordered;
		m_rules.addAll(h.m_rules);
		m_default_solution = h.m_default_solution;
	}

        public void setOrdered(boolean ordered) {
            m_ordered = ordered;
        }

        public boolean isOrdered() {
            return m_ordered;
        }

        public int size() {
            return m_rules.size();
        }
	
	public void copy(RuleHypothesis h) throws Exception {
		m_ordered = h.m_ordered;
		m_rules.clear();
		m_rules.addAll(h.m_rules);
		m_default_solution = h.m_default_solution;
	}

	public void addRule(FeatureTerm p, FeatureTerm s,float reliability,int support) throws Exception {
		m_rules.add(new Rule(p,s,reliability, support));
	}
	
	public void addRule(Rule r) {
		m_rules.add(r);
	}

        public void removeRule(Rule r) {
            m_rules.remove(r);
        }

	public List<Rule> getRules() {
		return m_rules;
	}
	
	public FeatureTerm getDefaultSolution() {
		return m_default_solution;
	}
	
	public void setDefaultSolution(FeatureTerm s) {
		m_default_solution = s;
	}

	public boolean subsumedByRule(FeatureTerm problem,Rule r) throws FeatureTermException, Exception {
		
		if (m_ordered) {
			int pos = m_rules.indexOf(r);
			if (pos==-1) {
				System.err.println("That rule is not part of this hypothesis!");
				return false;
			}
			
			// A rule can only subsume a pattern when m_ordered = true if all the previous rules do not:
			for(Rule r2:m_rules) {
				if (r2==r) {
					return r.pattern.subsumes(problem);
				} else {
					if (r2.pattern.subsumes(problem)) return false;
				}
			}
		} else {
			return r.pattern.subsumes(problem);
		}
		
		return false;
	}

	public Prediction generatePrediction(FeatureTerm problem,FTKBase dm, boolean debug) throws FeatureTermException, Exception
	{
		Prediction p;
		Rule candidate=null;
		int fired = 0;
		
		if (m_ordered) {
			for(Rule rule:m_rules) {
				if (rule.pattern.subsumes(problem)) {
					if (debug) System.out.println("Fired rule " + m_rules.indexOf(rule) + " -> " + rule.solution.toStringNOOS(dm));
					candidate = rule;
					fired++;
					break;
				} // if 
			} // while  			
		} else {
			for(Rule rule:m_rules) {
				if (rule.pattern.subsumes(problem)) {
					if (debug) System.out.println("Fired rule " + m_rules.indexOf(rule) + " -> " + rule.solution.toStringNOOS(dm));
					if ((candidate==null || 
						(candidate.pattern.subsumes(rule.pattern) && !rule.pattern.subsumes(candidate.pattern)) || 
						(!candidate.pattern.subsumes(rule.pattern) && rule.reliability>candidate.reliability))) {
						candidate=rule;
						if (debug) System.out.println("Candidate rule " + m_rules.indexOf(rule));
						fired++;
					} // if
				} // if 
			} // while  
		}
		
//		System.out.println("Fired " + fired + " rules with ordered " + m_ordered);

		if (candidate!=null) {
			if (debug) System.out.println("Hypothesis: " + candidate.solution.toStringNOOS(dm) + " with reliability " + candidate.reliability);

			p = new Prediction();
			p.problem = problem;
			p.solutions.add(candidate.solution);
			p.justifications.put(candidate.solution,candidate.pattern);
			p.support.put(candidate.solution,candidate.support);
			return p;
		} // if  

		if (m_default_solution!=null) {
			if (debug) System.out.println("Hypothesis: not covered by any pattern. Using default solution...");
			if (debug) System.out.println("Hypothesis: " + m_default_solution.toStringNOOS(dm));

			p = new Prediction();
			p.problem = problem;
			p.solutions.add(m_default_solution);
			p.justifications.put(m_default_solution,problem.getSort().createFeatureTerm());
			p.support.put(m_default_solution,1);
			return p;
		} else {
			if (debug) System.out.println("Hypothesis: not covered by any pattern and no default solution...");
			return new Prediction();
		} // if  
	} // Hypothesis::generate_prediction


    public Rule coveredByAnyRule(FeatureTerm problem) throws FeatureTermException {
        for(Rule rule:m_rules) {
            if (rule.pattern.subsumes(problem)) return rule;
        } // while
        return null;
    }


	public String  toString(FTKBase dm)
	{
		String tmp;

		tmp = ("Hypothesis: rules learned (" + m_rules.size() + "): ----------------------\n");
		if (m_default_solution!=null) {
			tmp+="Default Solution: " + m_default_solution.toStringNOOS(dm) + "\n";
		} // if 

		for(Rule rule:m_rules) {
			tmp+= m_rules.indexOf(rule) + " - Rule for " + rule.solution.toStringNOOS(dm) + " " + rule.reliability + "\n";
			tmp+= rule.pattern.toStringNOOS(dm) + "\n";
		} // while  

		return tmp;
	} // Hypothesis::show_rule_set 

	public String  toCompactString(FTKBase dm)
	{
		String tmp;

		tmp = ("Hypothesis: rules learned (" + m_rules.size() + "): ----------------------\n");
		if (m_default_solution!=null) {
			tmp+="Default Solution: " + m_default_solution.toStringNOOS(dm) + "\n";
		} // if 

		for(Rule rule:m_rules) {
			tmp+= "- Rule for " + rule.solution.toStringNOOS(dm) + " - " + rule.reliability + "\n";
		} // while  

		return tmp;
	} // Hypothesis::show_rule_set 
}
