/**
 * Copyright (c) 2013, Santiago Ontañón All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. Neither the name of the IIIA-CSIC nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission. THIS SOFTWARE IS PROVIDED
 * BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ftl.base.bridges;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ftl.base.core.FTKBase;
import ftl.base.core.FeatureTerm;
import ftl.base.core.FloatFeatureTerm;
import ftl.base.core.IntegerFeatureTerm;
import ftl.base.core.Ontology;
import ftl.base.core.Path;
import ftl.base.core.SetFeatureTerm;
import ftl.base.core.Sort;
import ftl.base.core.Symbol;
import ftl.base.core.SymbolFeatureTerm;
import ftl.base.core.TermFeatureTerm;
import ftl.base.utils.FeatureTermException;
import ftl.base.utils.RewindableInputStream;

// TODO: Auto-generated Javadoc
/**
 * The Class NOOSParser.
 */
public class NOOSParser extends OntologyParser {

    /**
     * Parses the.
     *
     * @param str the str
     * @param m the m
     * @param o the o
     * @return the feature term
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws FeatureTermException the feature term exception
     */
    public static FeatureTerm parse(String str, FTKBase m, Ontology o) throws IOException, FeatureTermException {
        // return parse(new RewindableInputStream(new StringBufferInputStream(str)),m,o);
        return parse(new RewindableInputStream(new ByteArrayInputStream(str.getBytes("UTF-8"))), m, o);
    }

    /**
     * Parses the.
     *
     * @param fp the fp
     * @param m the m
     * @param o the o
     * @return the feature term
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws FeatureTermException the feature term exception
     */
    public static FeatureTerm parse(RewindableInputStream fp, FTKBase m, Ontology o) throws IOException, FeatureTermException {
        NOOSParser noosParser = new NOOSParser();
        noosParser.fp = fp;
        return noosParser.buildFeatureTermFromOntology(fp, m, null, o);
    }

    /**
     * Parses the.
     *
     * @param fp the fp
     * @param m the m
     * @param resultingSort the resulting sort
     * @param o the o
     * @return the feature term
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws FeatureTermException the feature term exception
     */
    public static FeatureTerm parse(RewindableInputStream fp, FTKBase m, Sort resultingSort, Ontology o) throws IOException, FeatureTermException {
        NOOSParser noosParser = new NOOSParser();
        noosParser.fp = fp;
        return noosParser.buildFeatureTermFromOntology(fp, m, resultingSort, o);
    }

    /**
     * Builds the feature term from ontology.
     *
     * @param fp the fp
     * @param m the m
     * @param o the o
     * @return the feature term
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws FeatureTermException the feature term exception
     */
    public FeatureTerm buildFeatureTermFromOntology(RewindableInputStream fp, FTKBase m, Ontology o) throws IOException, FeatureTermException {
        return buildFeatureTermFromOntology(fp, m, null, o);
    }

    /**
     * Builds the feature term from ontology. resultingSort hints at the sort
     * expected in the output
     *
     * @param fpRewindable the fp rewindable
     * @param m the m
     * @param resultingSort the resulting sort
     * @param o the o
     * @return the feature term
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws FeatureTermException the feature term exception
     */
    public FeatureTerm buildFeatureTermFromOntology(RewindableInputStream fpRewindable, FTKBase m, Sort resultingSort, Ontology o) throws IOException,
            FeatureTermException {

        try {
            return parseFile(m, resultingSort, o);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Parses the file.
     *
     * @param m the m
     * @param resultingSort the resulting sort
     * @param o the o
     * @return the feature term
     * @throws Exception the exception
     */
    private FeatureTerm parseFile(FTKBase m, Sort resultingSort, Ontology o) throws Exception {
        NOOSToken t;
        FeatureTerm f;

        t = NOOSToken.getTokenNOOS(fp);
        if (t.type == NOOSToken.TOKEN_LEFT_PAR) {
            t = NOOSToken.getTokenNOOS(fp);
            if (t.type != NOOSToken.TOKEN_SYMBOL || (!t.token.equals("define") && !t.token.equals("define-episode"))) {
                return null;
            } // if
        } else if (t.type == NOOSToken.TOKEN_SYMBOL) {
            int num = 0;
            FeatureTerm res = null;
            Set<FeatureTerm> l;
            Symbol n = new Symbol(t.token);

            l = m.searchFT(n);
            for (FeatureTerm f2 : l) {
                if (resultingSort == null || resultingSort.inSort(f2)) {
                    res = f2;
                    num++;
                } // if
            }

            if (num == 1) {
                return res;
            } else {
                System.err.println("NOOS Importer: Error 6: several objects share the same id '" + t.token + "', their sorts are:");
                for (FeatureTerm f2 : l) {
                    System.err.println(f2.getSort().get());
                }
                return null;
            } // if

        } else if (t.type == NOOSToken.TOKEN_INTEGER) {
            /* integer: */
            return new IntegerFeatureTerm(null, Integer.parseInt(t.token), o);
        } else if (t.type == NOOSToken.TOKEN_FLOAT) {
            /* float: */
            return new FloatFeatureTerm(null, Float.parseFloat(t.token), o);
        } else if (t.type == NOOSToken.TOKEN_STRING) {
            /* symbol: */
            return new SymbolFeatureTerm(null, new Symbol(t.token), o);
        } else {
            return null;
        } // if

        {
            List<FeatureTerm> hierarchy = new LinkedList<FeatureTerm>();
            List<NOOSPathRecord> nprl = new LinkedList<NOOSPathRecord>();
            List<NOOSVariableRecord> nvl = new LinkedList<NOOSVariableRecord>();
            List<NOOSVariableLinkRecord> nvll = new LinkedList<NOOSVariableLinkRecord>();
            f = getFeatureTermInternal(m, o, o.getSort("any"), hierarchy, nprl, nvl, nvll);

            if (f != null) {

                /* process nprl list: */
                NOOSPathRecord npr;
                FeatureTerm f2;
                while (!nprl.isEmpty()) {
                    npr = nprl.remove(0);
                    if (npr.root != null && npr.p != null) {
                        f2 = npr.root.readPath(npr.p);
                        if (f2 != null && npr.f != null) {
                            if (npr.feature == null && npr.f instanceof SetFeatureTerm) {
                                ((SetFeatureTerm) npr.f).addSetValue(f2);
                            } else if (npr.feature != null && npr.f instanceof TermFeatureTerm) {
                                FeatureTerm f3;
                                f3 = npr.f.featureValue(npr.feature);
                                if (f3 != null) {
                                    if (f3 instanceof SetFeatureTerm) {
                                        ((SetFeatureTerm) f3).addSetValue(f2);
                                    } else {
                                        SetFeatureTerm f4;
                                        f4 = new SetFeatureTerm();

                                        f4.addSetValue(f3);
                                        f4.addSetValue(f2);
                                        ((TermFeatureTerm) npr.f).removeFeatureValue(npr.feature);
                                        if (!((TermFeatureTerm) npr.f).defineFeatureValueSecure(npr.feature, f4)) {
                                            System.err.println("NOOS parser(6): wrong feature in sort! setting " + npr.feature.get() + " in "
                                                    + npr.f.getSort().get());
                                        } // if
                                    } // if
                                } else {
                                    if (!((TermFeatureTerm) npr.f).defineFeatureValueSecure(npr.feature, f2)) {
                                        System.err.println("NOOS parser(7): wrong feature in sort! setting " + npr.feature.get() + " in "
                                                + npr.f.getSort().get());
                                    } // if
                                } // if
                            } // if
                        } // if
                    } // if
                } // while

                /* process nvll: */
                NOOSVariableRecord vr;
                NOOSVariableLinkRecord vl;
                while (!nvll.isEmpty()) {
                    vl = nvll.remove(0);
                    if (vl.f != null && vl.variable != null) {
                        vr = null;

                        for (NOOSVariableRecord vrtmp : nvl) {
                            if (vrtmp.variable.equals(vl.variable)) {
                                vr = vrtmp;
                                break;
                            }
                        }

                        if (vr != null) {
                            if (vl.feature == null && vl.f instanceof SetFeatureTerm) {
                                ((SetFeatureTerm) vl.f).addSetValue(vr.f);
                            } else if (vl.feature != null && vl.f instanceof TermFeatureTerm) {
                                if (!((TermFeatureTerm) vl.f).defineFeatureValueSecure(vl.feature, vr.f)) {
                                    System.err.println("NOOS parser(8): wrong feature in sort! setting " + vl.feature.get() + " in " + vl.f.getSort().get());
                                } // if
                            } else {
                                System.err.println("Variable: " + vl.variable.get());
                                System.err.println("failure!\n");
                                System.err.println("Variable.f: " + vl.f.getSort().get() + " Variable.feature: " + vl.feature.get());
                            } // if
                        } // if
                    } // if
                } // while

            } // if

            return f;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * csic.iiia.ftl.base.bridges.OntologyParser#setupOValuesAndFeatureValuesOfSpecificSort(csic.iiia.ftl.base.core.
     * Sort, java.util.List, csic.iiia.ftl.base.core.FeatureTerm, java.util.List, java.util.List,
     * csic.iiia.ftl.base.core.FTKBase, java.util.List, csic.iiia.ftl.base.core.Ontology, java.lang.String)
     */
    protected boolean setupOValuesAndFeatureValuesOfSpecificSort(Sort sort, List<FeatureTerm> hierarchy, FeatureTerm f, List<NOOSPathRecord> nprl,
            List<NOOSVariableLinkRecord> nvll, FTKBase m, List<NOOSVariableRecord> nvl, Ontology o, String featureName) throws Exception {
        if (t.type == NOOSToken.TOKEN_LEFT_PAR) {
            // Feature Name:
            t = NOOSToken.getTokenNOOS(fp);
            if (t.type != NOOSToken.TOKEN_SYMBOL) {
                System.err.println("NOOS Importer: Error 8");
                error = true;
                return error;
            } // if
            if (t.token.equals("define")) {
                System.err.println("NOOS Importer: Error 8a");
                error = true;
                return error;
            } // if
            tokenName = t.token;
            t = NOOSToken.getTokenNOOS(fp);
            return super.setupOValuesAndFeatureValuesOfSpecificSort(sort, hierarchy, f, nprl, nvll, m, nvl, o, tokenName);

        } else if (t.type == NOOSToken.TOKEN_RIGHT_PAR) {
            return true;
        } else {
            System.err.println("NOOS Importer: Error 17 when creating term of sort " + sort);
            error = true;
            return error;

        } // if
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * csic.iiia.ftl.base.bridges.OntologyParser#checkReadOneFeatureValueRequirements(csic.iiia.ftl.base.core.FeatureTerm
     * , csic.iiia.ftl.base.core.Symbol, csic.iiia.ftl.base.core.FeatureTerm)
     */
    protected void checkReadOneFeatureValueRequirements(FeatureTerm f, Symbol fname, FeatureTerm f2) {
        if (!((TermFeatureTerm) f).defineFeatureValueSecure(fname, f2)) {
            System.err.println("NOOS parser(10): wrong feature in sort! setting " + fname.get() + " in " + f.getSort().get());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * csic.iiia.ftl.base.bridges.OntologyParser#checkReadAllFeatureValuesRequirements(csic.iiia.ftl.base.core.FeatureTerm
     * , csic.iiia.ftl.base.core.Symbol, csic.iiia.ftl.base.core.SetFeatureTerm)
     */
    protected void checkReadAllFeatureValuesRequirements(FeatureTerm f, Symbol fname, SetFeatureTerm ft_tmp) {
        if (!((TermFeatureTerm) f).defineFeatureValueSecure(fname, ft_tmp)) {
            error = true;
            System.err.println("NOOS parser(10): wrong feature in sort! setting " + fname.get() + " in " + f.getSort().get());
        } // if
    }

    /*
     * (non-Javadoc)
     * 
     * @see csic.iiia.ftl.base.bridges.OntologyParser#setupFeatureValuesOfSpecificFeature(csic.iiia.ftl.base.utils.
     * RewindableInputStream, csic.iiia.ftl.base.core.Sort, java.util.List, csic.iiia.ftl.base.core.FeatureTerm,
     * java.util.List, java.util.List, csic.iiia.ftl.base.core.FTKBase, java.util.List,
     * csic.iiia.ftl.base.core.Ontology, csic.iiia.ftl.base.core.Symbol, java.util.List, java.util.List)
     */
    protected void setupFeatureValuesOfSpecificFeature(RewindableInputStream fp, Sort sort, List<FeatureTerm> hierarchy, FeatureTerm f,
            List<NOOSPathRecord> nprl, List<NOOSVariableLinkRecord> nvll, FTKBase m, List<NOOSVariableRecord> nvl, Ontology o, Symbol fname,
            List<FeatureTerm> values_read, List<Symbol> variables_read) throws Exception {
        FeatureTerm fvalue;
        boolean feature_variable = false;
        boolean delayed = false;
        boolean end = isEndOfSetOfFeaturesValues();
        do {
            fvalue = null;
            feature_variablename = new StringBuilder();

            if (t.type == NOOSToken.TOKEN_LEFT_PAR) {
                if (feature_variable) {
                    return;
                } // i

                prepareNextSymbol();
                if (t.type == NOOSToken.TOKEN_SYMBOL && t.token.equals("define")) {
                    Sort fsort = sort.featureSort(fname);
                    if (fsort==null) {
                        throw new FeatureTermException("NOOSParser: cannot find the sort of feature '" + fname + "' in sort " + sort);
                    }
                    fvalue = super.defineSymbol(hierarchy, fvalue, fsort, nprl, nvll, m, nvl, o);
                    if (fvalue == null) {
                        System.err.println("NOOS Importer: Error 9 (token: " + t.token + "): cannot parse the value of feature '" + fname + "'");
                        error = true;
                    } // if
                } else if (t.type == NOOSToken.TOKEN_ROOT) {
                    fvalue = setupRoot(hierarchy, f, nprl, fvalue);

                } else if (t.type == NOOSToken.TOKEN_PARENT) {
                    fvalue = setupParent(hierarchy, f, nprl, fvalue);

                } else {
                    System.err.println("NOOS Importer: Error 13. Expected [SYMBOL|ROOM|PARENT] and found " + t.type + " - " + t.token);
                    error = true;
                } // if

            } else if (t.type == NOOSToken.TOKEN_INIT_VARIABLE) {

                tokenName = t.token;
                feature_variable = initialize_initialVariable(feature_variablename, tokenName);

            } else if (t.type == NOOSToken.TOKEN_REF_VARIABLE) {
                delayed = initialize_refVariable(variables_read, t.token);

            } else if (t.type == NOOSToken.TOKEN_SYMBOL) {

                String name;
                name = t.token;
                fvalue = initialize_Symbol(sort, m, fname, name);
                error = fvalue == null;
                if (error) {
                    System.err.println("NOOS Importer: Error 18, possible incorrect feature name '" + fname.get() + "' in sort " + sort.get());

                }

            } else if (t.type == NOOSToken.TOKEN_INTEGER) {
                fvalue = setupIntegerFeatureType(o, t.token);

            } else if (t.type == NOOSToken.TOKEN_FLOAT) {
                fvalue = setupFloatFeatureType(o, t.token);

            } else if (t.type == NOOSToken.TOKEN_STRING) {
                fvalue = setupStringFeatureType(o, t.token);

            } else {
                System.err.println("NOOS Importer: Error 15 (token: " + t.type + " - " + t.token + ")");
                error = true;
            } // if

            if (!error) {

                addFeatureVariable(nvl, values_read, fvalue, feature_variable, feature_variablename.toString());
                if (fvalue == null && !delayed && !feature_variable) {
                    System.err.println("NOOS Importer: Error 16");
                    error = true;
                } // if
            }

            if (error) {
                return;
            }
            prepareNextSymbol();
            end = isEndOfSetOfFeaturesValues() || error;

        } while (end);

    }

    /*
     * (non-Javadoc)
     * 
     * @see csic.iiia.ftl.base.bridges.OntologyParser#isEndOfSetOfFeaturesValues()
     */
    protected boolean isEndOfSetOfFeaturesValues() {
        return t.type != NOOSToken.TOKEN_RIGHT_PAR;
    }

    /*
     * (non-Javadoc)
     * 
     * @see csic.iiia.ftl.base.bridges.OntologyParser#setupSetOfElements(java.util.List,
     * csic.iiia.ftl.base.core.FeatureTerm, csic.iiia.ftl.base.core.Sort, java.util.List, java.util.List,
     * csic.iiia.ftl.base.core.FTKBase, java.util.List, csic.iiia.ftl.base.core.Ontology,
     * csic.iiia.ftl.base.core.FeatureTerm)
     */
    protected boolean setupSetOfElements(List<FeatureTerm> hierarchy, FeatureTerm f, Sort vsort, List<NOOSPathRecord> nprl, List<NOOSVariableLinkRecord> nvll,
            FTKBase m, List<NOOSVariableRecord> nvl, Ontology o, FeatureTerm fvalue) throws Exception {
        // Set elements:

        if (t.type == NOOSToken.TOKEN_LEFT_PAR) {
            t = NOOSToken.getTokenNOOS(fp);
            if (t.type == NOOSToken.TOKEN_SYMBOL && t.token.equals("define")) {
                fvalue = defineSymbol(hierarchy, f, vsort, nprl, nvll, m, nvl, o);
                if (fvalue == null) {
                    System.err.println("NOOS Importer: Error 1: cannot parse the first element of a set!");
                    error = true;

                } // if
            } else if (t.type == NOOSToken.TOKEN_ROOT) {
                fvalue = setupRoot(hierarchy, f, nprl, fvalue);

            } else if (t.type == NOOSToken.TOKEN_PARENT) {
                fvalue = setupParent(hierarchy, f, nprl, fvalue); // if

            } else {
                System.err.println("NOOS Importer: Error 5");
                error = true;
            } // if
            if (error) {
                return error;
            }

        } else if (t.type == NOOSToken.TOKEN_REF_VARIABLE) {
            defineRefVariable(f, nvll, t.token);
            // printf("Added a variable reference to \"%s\" (to a SET)\n",token);
        } else if (t.type == NOOSToken.TOKEN_SYMBOL) {

            fvalue = setupSymbol(vsort, m);

        } else if (t.type == NOOSToken.TOKEN_INTEGER) {
            // integer:
            fvalue = setupIntegerFeatureType(o, t.token);
        } else if (t.type == NOOSToken.TOKEN_FLOAT) {
            // float:
            fvalue = setupFloatFeatureType(o, t.token);
        } else if (t.type == NOOSToken.TOKEN_STRING) {
            // symbol:
            fvalue = setupStringFeatureType(o, t.token);
        } else if (t.type == NOOSToken.TOKEN_RIGHT_PAR) {
            return true;
        } else {
            System.err.println("NOOS Importer: Error 7");
            error = true;
        } // if
        if (error) {
            return true;
        }
        addSetValuesToSetFeatureTerm(f, fvalue);
        return false;
    }

    /**
     * Setup symbol.
     *
     * @param vsort the vsort
     * @param m the m
     * @return the feature term
     * @throws FeatureTermException the feature term exception
     */
    protected FeatureTerm setupSymbol(Sort vsort, FTKBase m) throws FeatureTermException {
        FeatureTerm fvalue = null;
        if (vsort != null) {
            fvalue = super.setupSymbol(vsort, m, t.token);
            if (num[0] != 0 && num[0] != 1) {
                System.err.println(errorMessage);
                fvalue = null;
            }
        }

        return fvalue;
    }

    /**
     * Setup parent.
     *
     * @param hierarchy the hierarchy
     * @param f the f
     * @param nprl the nprl
     * @param fvalue the fvalue
     * @return the feature term
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws FeatureTermException the feature term exception
     */
    private FeatureTerm setupParent(List<FeatureTerm> hierarchy, FeatureTerm f, List<NOOSPathRecord> nprl, FeatureTerm fvalue) throws IOException,
            FeatureTermException {
        int ndots = t.token.length();
        NOOSPathRecord npr = new NOOSPathRecord();
        error = false;
        if (ndots == 1) {
            npr.root = f;
        } else {
            if (hierarchy.isEmpty()) {
                System.err.println("NOOS Importer: Error 3");
                error = true;
                return null;
            } else {
                int pos = hierarchy.size() - (ndots - 1);
                npr.root = hierarchy.get(pos);
            } // if
        } // if
        npr.feature = null;
        npr.f = f;
        npr.p = new Path();

        // printf("PARENT (%i): ",ndots);
        t = NOOSToken.getTokenNOOS(fp);
        while (t.type == NOOSToken.TOKEN_SYMBOL) {
            npr.p.features.add(0, new Symbol(t.token));
            // printf("%s ",token);
            t = NOOSToken.getTokenNOOS(fp);
        } // while
        // printf("\n");
        if (t.type == NOOSToken.TOKEN_RIGHT_PAR) {
            addSymbolsOfRootToPathRecord(nprl, npr);
            fvalue = null;
        } else {
            System.err.println("NOOS Importer: Error 4");
            error = true;
            return null;
        }
        return fvalue;
    }

    /**
     * Setup root.
     *
     * @param hierarchy the hierarchy
     * @param f the f
     * @param nprl the nprl
     * @param fvalue the fvalue
     * @return the feature term
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws FeatureTermException the feature term exception
     */
    private FeatureTerm setupRoot(List<FeatureTerm> hierarchy, FeatureTerm f, List<NOOSPathRecord> nprl, FeatureTerm fvalue) throws IOException,
            FeatureTermException {
        error = false;
        NOOSPathRecord npr = defineRoot(hierarchy, f);

        // printf("ROOT: ");
        t = NOOSToken.getTokenNOOS(fp);

        while (t.type == NOOSToken.TOKEN_SYMBOL) {
            tokenName = t.token;
            defineSymbolOfRoot(npr, tokenName);
            // printf("%s ",token);
            t = NOOSToken.getTokenNOOS(fp);
        } // while
        // printf("\n");
        if (t.type == NOOSToken.TOKEN_RIGHT_PAR) {
            addSymbolsOfRootToPathRecord(nprl, npr);
            fvalue = null;
        } else {
            System.err.println("NOOS Importer: Error 2");
            error = true;
            return null;
        } // if
        return fvalue;
    }

    /**
     * The t.
     */
    private NOOSToken t;

    /*
     * (non-Javadoc)
     * 
     * @see csic.iiia.ftl.base.bridges.OntologyParser#setupIdentifier(csic.iiia.ftl.base.core.FTKBase,
     * csic.iiia.ftl.base.core.Ontology, csic.iiia.ftl.base.core.Sort, java.util.List, java.util.List, java.util.List,
     * java.util.List, csic.iiia.ftl.base.core.FeatureTerm)
     */
    protected FeatureTerm setupIdentifier(FTKBase m, Ontology o, Sort vsort, List<FeatureTerm> hierarchy, List<NOOSPathRecord> nprl,
            List<NOOSVariableRecord> nvl, List<NOOSVariableLinkRecord> nvll, FeatureTerm f) throws IOException, FeatureTermException {
        boolean variable = false;
        String variablename = null;
        Symbol name = null;

        if (t.type == NOOSToken.TOKEN_INIT_VARIABLE) {

            variablename = t.token;
            variable = true;
            t = NOOSToken.getTokenNOOS(fp);
        } // if

        if (t.type != NOOSToken.TOKEN_LEFT_PAR) {
            return f;
        }

        t = NOOSToken.getTokenNOOS(fp);
        if (t.type != NOOSToken.TOKEN_SYMBOL) {
            return f;
        }
        sort = getSort(o, t.token);
        t = NOOSToken.getTokenNOOS(fp);
        if (t.type == NOOSToken.TOKEN_SYMBOL) {
            if (!t.token.equals(":id")) {
                return f;
            }

            t = NOOSToken.getTokenNOOS(fp);
            if (t.type == NOOSToken.TOKEN_SYMBOL) {
                name = getName(t.token);
                t = NOOSToken.getTokenNOOS(fp);
                if (isEndOfSetOfFeaturesValues()) {
                    return f;
                }
            } else {
                return f;
            } // if

        }
        if (isEndOfSetOfFeaturesValues()) {
            return f;
        } // if

		// Check if there is some undefined term with the appropiate name, use it and remove it from the undefined term
        // list:
        if (name != null) {
            FeatureTerm found = null;
            found = m.searchUndefinedFT(name);

            if (found != null) {

                // Delete it first, since it will be added again when returning from this function:
                m.deleteFT(found);
                f = found;
                f.setSort(sort);

            } else {
                if (sort == null) {
                    f = new SetFeatureTerm(name);
                } else {
                    f = new TermFeatureTerm(name, sort);
                } // if
            } // if

        } else {
            if (sort == null) {
                f = new SetFeatureTerm(name);
            } else {
                f = new TermFeatureTerm(name, sort);
            } // if
        } // if

        setupVariable(nvl, f, variable, variablename);

        return f;
    }

    /*
     * (non-Javadoc)
     * 
     * @see csic.iiia.ftl.base.bridges.OntologyParser#prepareNextSymbol()
     */
    protected NOOSToken prepareNextSymbol() throws IOException {

        t = NOOSToken.getTokenNOOS(fp);
        return t;
    }

}
