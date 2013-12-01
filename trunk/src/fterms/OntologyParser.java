package fterms;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import util.RewindableInputStream;
import fterms.exceptions.FeatureTermException;

public abstract class OntologyParser {
    /* Used to support the ">>" and "." symbols of the NOOS syntax:	*/
    /* assign f.feature = root.p							*/

    static class NOOSPathRecord {

        public FeatureTerm f = null;
        public Symbol feature = null;
        public FeatureTerm root = null;
        public Path p = null;
    };

    /* Used to support the "?x" and "!x" symbols of the enhanced NOOS syntax: */
    static class NOOSVariableRecord {

        public Symbol variable = null;
        public FeatureTerm f = null;
    };

    static class NOOSVariableLinkRecord {

        public FeatureTerm f = null;
        public Symbol feature = null;
        public Symbol variable = null;
    };
    protected Sort sort;
    protected String tokenName;
    protected String errorMessage;
    protected boolean error = false;
    protected StringBuilder feature_variablename;
    protected int[] num = new int[1];
    protected RewindableInputStream fp;

    protected FeatureTerm setupStringFeatureType(Ontology o, String string)
            throws FeatureTermException {
        FeatureTerm fvalue;
        fvalue = new SymbolFeatureTerm(new Symbol(string), o);
        return fvalue;
    }

    protected FeatureTerm setupSymbol(Sort vsort, FTKBase m, String symbolName)
            throws FeatureTermException {
        error = false;
        num = new int[1];
        FeatureTerm fvalue;
        fvalue = getFeatureValueOfSymbol(vsort, m, symbolName);
        return fvalue;
    }

    protected void addSetValuesToSetFeatureTerm(FeatureTerm f, FeatureTerm fvalue) {
        if (fvalue != null) {
            ((SetFeatureTerm) f).addSetValue(fvalue);
        }
        fvalue = null;
    }

    protected void addFeatureVariable(List<NOOSVariableRecord> nvl,
            List<FeatureTerm> values_read, FeatureTerm fvalue,
            boolean feature_variable, String feature_variablename)
            throws FeatureTermException {
        if (fvalue == null) {
            return;
        }
        if (feature_variable) {
            NOOSVariableRecord nv;
//							printf("Adding variable \"%s\"\n",variablename);
            nv = new NOOSVariableRecord();
            nv.f = fvalue;
            nv.variable = new Symbol(feature_variablename);
            nvl.add(nv);
            feature_variable = false;
        } // if
        values_read.add(fvalue);

    }

    protected FeatureTerm setupFloatFeatureType(Ontology o, String floatString)
            throws FeatureTermException {
        FeatureTerm fvalue;
        fvalue = new FloatFeatureTerm(Float.parseFloat(floatString), o);
        return fvalue;
    }

    protected FeatureTerm setupIntegerFeatureType(Ontology o, String integerString)
            throws FeatureTermException {
        FeatureTerm fvalue;
        fvalue = new IntegerFeatureTerm(Integer.parseInt(integerString), o);
        return fvalue;
    }

    protected FeatureTerm getFeatureValueOfSymbol(Sort vsort, FTKBase m,
            String tokenName) throws FeatureTermException {
        FeatureTerm res = null;
        FeatureTerm fvalue = null;

        Set<FeatureTerm> l;
        Symbol n = new Symbol(tokenName);

        l = m.SearchFT(n);
        for (FeatureTerm f2 : l) {
            if (vsort.inSort(f2)) {
                res = f2;
                num[0]++;
            } // if 						
        }

        if (num[0] == 1) {
            fvalue = res;
        } else {
            if (num[0] == 0) {
                // Create a new term of the appopiate sort and add it to the memory & undefined term list:
                System.err.println("undefined term: " + n);
                fvalue = new TermFeatureTerm(n, vsort);
                m.AddUndefinedFT(fvalue);
            }  // if 
        } // if 

        return fvalue;
    }

    protected void defineRefVariable(FeatureTerm f,
            List<NOOSVariableLinkRecord> nvll, String nameOfVariable) throws FeatureTermException {
        NOOSVariableLinkRecord nv = new NOOSVariableLinkRecord();
        nv.f = f;
        nv.feature = null;
        nv.variable = new Symbol(nameOfVariable);
        nvll.add(nv);
    }

    protected void addSymbolsOfRootToPathRecord(List<NOOSPathRecord> nprl,
            NOOSPathRecord npr) {
        nprl.add(npr);
    }

    protected void defineSymbolOfRoot(NOOSPathRecord npr, String tokenName)
            throws FeatureTermException {
        npr.p.features.add(0, new Symbol(tokenName));
    }

    protected NOOSPathRecord defineRoot(List<FeatureTerm> hierarchy, FeatureTerm f) {
        NOOSPathRecord npr = new NOOSPathRecord();
        if (hierarchy.isEmpty()) {
            npr.root = f;
        } else {
            npr.root = hierarchy.get(0);
        } // if 
        npr.feature = null;
        npr.f = f;
        npr.p = new Path();
        return npr;
    }

    protected FeatureTerm defineSymbol(Symbol fname, List<FeatureTerm> hierarchy, FeatureTerm f,
            Sort vsort,
            List<NOOSPathRecord> nprl, List<NOOSVariableLinkRecord> nvll,
            FTKBase m, List<NOOSVariableRecord> nvl, Ontology o)
            throws Exception {
        FeatureTerm fvalue;
        hierarchy.add(f);
        fvalue = getFeatureTermInternal(m, o, vsort, hierarchy, nprl, nvl, nvll);
        hierarchy.remove(f);
        return fvalue;
    }

    protected FeatureTerm defineSymbol(List<FeatureTerm> hierarchy, FeatureTerm f,
            Sort vsort,
            List<NOOSPathRecord> nprl, List<NOOSVariableLinkRecord> nvll,
            FTKBase m, List<NOOSVariableRecord> nvl, Ontology o)
            throws Exception {
        FeatureTerm fvalue;
        Symbol fname = null;
        fvalue = this.defineSymbol(fname, hierarchy, f, vsort, nprl, nvll, m, nvl, o);

        return fvalue;
    }

    protected FeatureTerm getFeatureTermInternal(FTKBase m, Ontology o, Sort vsort, List<FeatureTerm> hierarchy, List<NOOSPathRecord> nprl, List<NOOSVariableRecord> nvl, List<NOOSVariableLinkRecord> nvll) throws Exception {

        // Sort and name:  
        prepareNextSymbol();
        FeatureTerm f = null;
        f = setupIdentifier(m, o, vsort, hierarchy, nprl, nvl, nvll, f);
        f = setupFeatures(hierarchy, f, vsort, nprl, nvll, m, nvl, o);

        return f;
    } // FeatureTer

    protected Symbol getName(String nameIdentifier)
            throws FeatureTermException, IOException {
        Symbol name;
        name = new Symbol(nameIdentifier);
        return name;
    }

    protected Sort getSort(Ontology o, String sortIdentifier)
            throws FeatureTermException, IOException {
        Sort sortInstance = null;
        if (!sortIdentifier.equals("set")) {
            sortInstance = o.getSort(sortIdentifier);
        } // if 
        return sortInstance;

    }

    protected void setupVariable(List<NOOSVariableRecord> nvl, FeatureTerm f,
            boolean variable, String variablename) throws FeatureTermException {
        if (variable) {
            NOOSVariableRecord nv;
            //		printf("Adding variable \"%s\"\n",variablename);
            nv = new NOOSVariableRecord();
            nv.f = f;
            nv.variable = new Symbol(variablename);
            nvl.add(nv);
        } // if 
    }

    protected FeatureTerm initialize_Symbol(Sort sort, FTKBase m, Symbol fname,
            String name) throws FeatureTermException {
        FeatureTerm fvalue = null;
        Sort s;
        Symbol n = new Symbol(name);
        s = sort.featureSort(fname);

        if (sort != null) {
            fvalue = setupSymbol(s, m, name);
            if (num[0] != 0 && num[0] != 1) {
                System.err.println("NOOSParser: Error 14, more than one named term of the proper sort, with name: '" + n.get() + "'");
                fvalue = null;
            }
        }

        return fvalue;
    }

    protected boolean initialize_refVariable(List<Symbol> variables_read, String name)
            throws FeatureTermException {
        boolean delayed;
        variables_read.add(new Symbol(name));
        delayed = true;
        return delayed;
    }

    protected boolean initialize_initialVariable(StringBuilder feature_variablename, String tokenName) {
        boolean feature_variable = true;
        feature_variablename = feature_variablename.append(tokenName);
        return feature_variable;
    }

    protected void readAllFeatureValues(FeatureTerm f,
            List<NOOSVariableLinkRecord> nvll, Symbol fname,
            List<FeatureTerm> values_read, List<Symbol> variables_read) {
        SetFeatureTerm ft_tmp;

//					printf("(2) Creating a set with %i+%i values\n",values_read.Length(),variables_read.Length());
        ft_tmp = new SetFeatureTerm();
        checkReadAllFeatureValuesRequirements(f, fname, ft_tmp);
        while (!values_read.isEmpty()) {
            ft_tmp.addSetValue(values_read.remove(0));
        }

        while (!variables_read.isEmpty()) {
            NOOSVariableLinkRecord nv = new NOOSVariableLinkRecord();
            nv.f = ft_tmp;
            nv.feature = null;
            nv.variable = variables_read.remove(0);
            nvll.add(nv);
//						printf("Added a variable link record to fature - for variable %s\n",nv.variable.get());
        } // while 
    }

    protected void checkReadAllFeatureValuesRequirements(FeatureTerm f,
            Symbol fname, SetFeatureTerm ft_tmp) {

    }

    protected void readOneFeatureValue(FeatureTerm f, Symbol fname,
            List<FeatureTerm> values_read) {
        FeatureTerm f2;
        f2 = values_read.remove(0);
        checkReadOneFeatureValueRequirements(f, fname, f2); // if 
    }

    protected void checkReadOneFeatureValueRequirements(FeatureTerm f,
            Symbol fname, FeatureTerm f2) {

    }

    protected boolean setupOValuesAndFeatureValuesOfSpecificSort(Sort sort, List<FeatureTerm> hierarchy, FeatureTerm f,
            List<NOOSPathRecord> nprl,
            List<NOOSVariableLinkRecord> nvll, FTKBase m,
            List<NOOSVariableRecord> nvl, Ontology o, String featureName)
            throws Exception {
        Symbol fname;
        fname = new Symbol(featureName);
        // Feature Value: (may have several values to form a SET)  
        readValuesAndFeatureValuesOfSpecificFeature(fp, sort, hierarchy, f, nprl, nvll, m, nvl, o, fname);
        return false;
    }

    protected void readValuesAndFeatureValuesOfSpecificFeature(RewindableInputStream fp,
            Sort sort, List<FeatureTerm> hierarchy, FeatureTerm f,
            List<NOOSPathRecord> nprl,
            List<NOOSVariableLinkRecord> nvll, FTKBase m,
            List<NOOSVariableRecord> nvl, Ontology o, Symbol fname)
            throws Exception {
        List<FeatureTerm> values_read = new LinkedList<FeatureTerm>();
        List<Symbol> variables_read = new LinkedList<Symbol>();

        setupFeatureValuesOfSpecificFeature(fp, sort, hierarchy,
                f, nprl, nvll, m, nvl, o, fname,
                values_read,
                variables_read);
        if (error) {
            return;
        }

        // Add read values to feature value:  
        addReadValuesToFeatureValue(f, nvll, fname, values_read, variables_read);
    }

    protected void addReadValuesToFeatureValue(FeatureTerm f,
            List<NOOSVariableLinkRecord> nvll, Symbol fname,
            List<FeatureTerm> values_read, List<Symbol> variables_read) {
        if (values_read.size() + variables_read.size() == 1) {
            if (values_read.size() == 1) {
                readOneFeatureValue(f, fname, values_read);
            } else {
                NOOSVariableLinkRecord nv = new NOOSVariableLinkRecord();
                nv.f = f;
                nv.feature = fname;
                nv.variable = variables_read.remove(0);
                nvll.add(nv);
//						printf("Added a variable link record to fature %s for variable %s\n",nv.feature.get(),nv.variable.get());
            } // if 
        } else if (values_read.size() + variables_read.size() > 1) {
            readAllFeatureValues(f, nvll, fname, values_read, variables_read);

        } // if 
    }

    protected FeatureTerm setupFeatures(List<FeatureTerm> hierarchy, FeatureTerm f, Sort vsort, List<NOOSPathRecord> nprl, List<NOOSVariableLinkRecord> nvll, FTKBase m, List<NOOSVariableRecord> nvl, Ontology o) throws Exception {
        boolean end = false;
        Symbol fname;
        Sort sort = this.sort;
        // Features:  
        do {

            prepareNextSymbol();

//			System.out.println("(" + t.token + "," + t.type + ")");
            FeatureTerm fvalue = null;
            if (sort == null) {
                end = setupSetOfElements(hierarchy, f, vsort, nprl, nvll, m, nvl, o, fvalue);
            } else {

                end = error || setupOValuesAndFeatureValuesOfSpecificSort(sort, hierarchy, f, nprl, nvll, m, nvl, o, tokenName);
            }
            // if 
        } while (!end);
        return f;
    }

    protected abstract FeatureTerm setupIdentifier(FTKBase m, Ontology o, Sort vsort,
            List<FeatureTerm> hierarchy, List<NOOSPathRecord> nprl,
            List<NOOSVariableRecord> nvl, List<NOOSVariableLinkRecord> nvll,
            FeatureTerm f) throws IOException, FeatureTermException;

    protected abstract void prepareNextSymbol() throws Exception;

    protected abstract void setupFeatureValuesOfSpecificFeature(RewindableInputStream fp, Sort sort,
            List<FeatureTerm> hierarchy, FeatureTerm f,
            List<NOOSPathRecord> nprl,
            List<NOOSVariableLinkRecord> nvll, FTKBase m,
            List<NOOSVariableRecord> nvl, Ontology o, Symbol fname,
            List<FeatureTerm> values_read, List<Symbol> variables_read)
            throws Exception;

    protected abstract boolean setupSetOfElements(List<FeatureTerm> hierarchy, FeatureTerm f,
            Sort vsort,
            List<NOOSPathRecord> nprl, List<NOOSVariableLinkRecord> nvll,
            FTKBase m, List<NOOSVariableRecord> nvl, Ontology o, FeatureTerm fvalue) throws Exception;

    protected abstract boolean isEndOfSetOfFeaturesValues();

}
