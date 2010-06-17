/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fterms.translators;

import fterms.FTKBase;
import fterms.FTRefinement;
import fterms.FeatureTerm;
import fterms.FloatFeatureTerm;
import fterms.IntegerFeatureTerm;
import fterms.Ontology;
import fterms.SetFeatureTerm;
import fterms.Symbol;
import fterms.exceptions.FeatureTermException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author santi
 */
public class HornClauses {

    public static List<Clause> toClauses(FeatureTerm f, Ontology o, FTKBase dm, String prefix) throws FeatureTermException {
        List<Clause> clauses = new LinkedList<Clause>();
        List<FeatureTerm> vs_tmp = FTRefinement.variables(f);
        List<FeatureTerm> vs = new LinkedList<FeatureTerm>();
        int count = 0;

        // Make sure that the root is the first element:
        vs_tmp.remove(f);
        vs.add(f);
        vs.addAll(vs_tmp);

        for (FeatureTerm v : vs) {

            if (!dm.contains(v) && !v.isConstant()) {
                // sorts:
                {
                    Clause c = new Clause(v.getSort().get());
                    c.setParameter(0, Clause.TYPE_ID, prefix + count);
                    clauses.add(c);
                }

                // Features:
                for (Symbol feature : v.getSort().getFeatures()) {
                    FeatureTerm tmpvalue = v.featureValue(feature);

                    if (tmpvalue != null) {
                        List<FeatureTerm> values = new LinkedList<FeatureTerm>();
                        if (tmpvalue instanceof SetFeatureTerm) {
                            values.addAll(((SetFeatureTerm) tmpvalue).getSetValues());
                        } else {
                            values.add(tmpvalue);
                        }

                        for (FeatureTerm value : values) {
                            Clause c = new Clause(feature.get());
                            c.setParameter(0, Clause.TYPE_ID, prefix + count);
                            if (dm.contains(value)) {
                                c.setParameter(1, Clause.TYPE_SYMBOL, value.toStringNOOS(dm));
                            } else if (value.isConstant()) {
                                if (value instanceof IntegerFeatureTerm) {
                                    c.setParameter(1, Clause.TYPE_INTEGER, value.toStringNOOS(dm));
                                } else if (value instanceof FloatFeatureTerm) {
                                    c.setParameter(1, Clause.TYPE_FLOAT, value.toStringNOOS(dm));
                                } else {
                                    c.setParameter(1, Clause.TYPE_SYMBOL, value.toStringNOOS(dm));
                                }
                            } else {
                                c.setParameter(1, Clause.TYPE_ID, prefix + vs.indexOf(value));
                            }
                            clauses.add(c);
                        }
                    }
                }
            }

            count++;
        }

        return clauses;
    }
}
