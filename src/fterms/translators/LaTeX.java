/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fterms.translators;

import fterms.FTKBase;
import fterms.FeatureTerm;
import fterms.FloatFeatureTerm;
import fterms.IntegerFeatureTerm;
import fterms.SetFeatureTerm;
import fterms.Symbol;
import fterms.SymbolFeatureTerm;
import fterms.TermFeatureTerm;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author santi
 */
public class LaTeX {

    public static String toLaTeXTerm(FeatureTerm f, FTKBase dm, boolean separateConstants) {
        String tmp;
        List<FeatureTerm> bindings = new LinkedList<FeatureTerm>();

        tmp = toLaTeXTermInternal(f,bindings,dm, separateConstants);

        tmp = tmp.replaceAll("-", "{\\text -}");

        return "\\[\n \\psi ::= " + tmp + "\n\\]";
    }

    private static String toLaTeXTermInternal(FeatureTerm f,List<FeatureTerm> bindings,FTKBase dm,  boolean separateConstants)
    {
        if (f instanceof TermFeatureTerm) {
            return toLaTeXTermInternalTerm((TermFeatureTerm)f, bindings, dm, separateConstants);
        } else if (f instanceof SetFeatureTerm) {
            return toLaTeXTermInternalSet((SetFeatureTerm)f, bindings, dm, separateConstants);
        } else if (f instanceof SymbolFeatureTerm) {
            return toLaTeXTermInternalConstant(f,bindings,dm, separateConstants);
        } else if (f instanceof IntegerFeatureTerm) {
            return toLaTeXTermInternalConstant(f,bindings,dm, separateConstants);
        } else if (f instanceof FloatFeatureTerm) {
            return toLaTeXTermInternalConstant(f,bindings,dm, separateConstants);
        } else {
            return toLaTeXTermInternalAbstract(f, bindings, dm, separateConstants);
        }

    }
    
    private static String toLaTeXTermInternalAbstract(FeatureTerm f,List<FeatureTerm> bindings,FTKBase dm, boolean separateConstants)
    {
            String tmp = "";
            int ID=-1;

            if (separateConstants && (f.isConstant() || dm.contains(f))) {
                ID=-1;
            } else {
                ID=bindings.indexOf(f);
            }
            if (ID==-1) {
                    bindings.add(f);
                    ID=bindings.indexOf(f);

                    if (f.getName()!=null && dm!=null && dm.contains(f)) {
                        return tmp +="X_{" + (ID+1) + "} : " + f.getName().get();
                    }

                    tmp +="X_{" + (ID+1) + "} : " + f.getSort().get();

                    return tmp;
            } else {
                    return "X_{" + (ID+1) + "}";
            } // if
    }

    private static String toLaTeXTermInternalConstant(FeatureTerm f,List<FeatureTerm> bindings,FTKBase dm, boolean separateConstants)
    {
            String tmp = "";
            int ID=-1;

            if (separateConstants && (f.isConstant() || dm.contains(f))) {
                ID=-1;
            } else {
                ID=bindings.indexOf(f);
            }
            if (ID==-1) {
                    bindings.add(f);
                    ID=bindings.indexOf(f);

                    tmp +="X_{" + (ID+1) + "} : " + f.toStringNOOS(dm);

                    return tmp;
            } else {
                    return "X_{" + (ID+1) + "}";
            } // if
    }


    private static String toLaTeXTermInternalSet(SetFeatureTerm f,List<FeatureTerm> bindings,FTKBase dm, boolean separateConstants)
    {
        String tmp = "";

        List<FeatureTerm> values = f.getSetValues();

        if (values.size()>0) {
            if (values.size()==1) {
                return toLaTeXTermInternal(values.get(0), bindings, dm, separateConstants);
            } else {
                tmp += "\\left\\{\n";
                tmp += "\\begin{array}{l}\n";
                for(FeatureTerm value:values) {
                    tmp += toLaTeXTermInternal(value, bindings, dm, separateConstants);
                    tmp += "\\\\\n";
                }
                tmp += "\\end{array}\n";
                tmp += "\\right.";                
            }
        } else {
            tmp += "\\emptyset\\n";
        }


        return tmp;
    }


    private static String toLaTeXTermInternalTerm(TermFeatureTerm f,List<FeatureTerm> bindings,FTKBase dm, boolean separateConstants)
    {
            String tmp = "";
            int ID=-1;

            if (separateConstants && (f.isConstant() || dm.contains(f))) {
                ID=-1;
            } else {
                ID=bindings.indexOf(f);
            }
            if (ID==-1) {
                    bindings.add(f);
                    ID=bindings.indexOf(f);

                    if (f.getName()!=null && dm!=null && dm.contains(f)) {
                        return tmp +="X_{" + (ID+1) + "} : " + f.getName().get();
                    }

                    tmp +="X_{" + (ID+1) + "} : " + f.getSort().get();

                    // features:
                    Set<Symbol> features = f.getFeatureNames();

                    if (features.size()>0) {
                        tmp += "\\left[\n";

                        if (features.size()>1) {
                            tmp += "\\begin{array}{l}\n";
                        }

                        for(Symbol feature:features) {
                            FeatureTerm fv = f.featureValue(feature);

                            tmp += feature.get() + " \\doteq " + toLaTeXTermInternal(fv,bindings, dm, separateConstants);

                            if (features.size()>1) {
                                tmp += "\\\\\n";
                            }
                        }

                        if (features.size()>1) {
                            tmp += "\\end{array}\n";
                        }

                        tmp += "\\right]";
                    }

                    return tmp;
            } else {
                    return "X_{" + (ID+1) + "}";
            } // if
    }


}
