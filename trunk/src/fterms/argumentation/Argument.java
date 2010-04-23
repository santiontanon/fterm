package fterms.argumentation;

import fterms.FTKBase;
import fterms.FeatureTerm;
import fterms.exceptions.FeatureTermException;
import fterms.learning.Rule;

public class Argument {

    public static int next_ID = 0;
    public static final int ARGUMENT_NONE = -1;
    public static final int ARGUMENT_RULE = 0;
    public static final int ARGUMENT_EXAMPLE = 1;
    public int m_type = ARGUMENT_NONE;
    public FeatureTerm m_example;
    public Rule m_rule;
    public String m_agent = null;
    public int m_ID = next_ID++;

    public Argument(FeatureTerm e) {
        m_type = ARGUMENT_EXAMPLE;
        m_example = e;
        m_rule = null;
    }

    public Argument(Rule r) {
        m_type = ARGUMENT_RULE;
        m_rule = r;
        m_example = null;
    }

    public Argument(FeatureTerm e, String agent) {
        m_type = ARGUMENT_EXAMPLE;
        m_example = e;
        m_rule = null;
        m_agent = agent;
    }

    public Argument(Rule r, String agent) {
        m_type = ARGUMENT_RULE;
        m_rule = r;
        m_example = null;
        m_agent = agent;
    }

    public String toString() {
        if (m_type == ARGUMENT_RULE) {
            return "RuleArgument" + m_ID + (m_agent == null ? "" : "-" + m_agent);
        } else if (m_type == ARGUMENT_EXAMPLE) {
            return "ExampleArgument" + m_ID + (m_agent == null ? "" : "-" + m_agent + "-") + "(" + m_example.getName() + ")";
        } else {
            return "NoneArgument" + m_ID + (m_agent == null ? "" : "-" + m_agent);
        }
    }

    public String toStringNOOS(FTKBase dm) {
        if (m_type == ARGUMENT_RULE) {
            return "Argument" + m_ID + "(RULE):\n" + m_rule.toStringNOOS(dm);
        } else if (m_type == ARGUMENT_EXAMPLE) {
            return "Argument" + m_ID + "(EXAMPLE):\n" + m_example.toStringNOOS(dm);
        } else {
            return "Argument" + m_ID + "(NONE)";
        }
    }

    public boolean equivalents(Argument a) throws FeatureTermException {
        if (m_type != a.m_type) {
            return false;
        }
        if (m_type == ARGUMENT_EXAMPLE) {
            if (m_example == a.m_example) {
                return true;
            }
            return false;
        }
        if (m_type == ARGUMENT_RULE) {
            if (!m_rule.solution.equivalents(a.m_rule.solution)) {
                return false;
            }
            if (!m_rule.pattern.equivalents(a.m_rule.pattern)) {
                return false;
            }
            return true;
        }

        return true;
    }
    
    public int getID() {
        return m_ID;
    }
}
