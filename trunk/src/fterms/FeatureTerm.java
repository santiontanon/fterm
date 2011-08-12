package fterms;

import fterms.subsumption.FTSubsumption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import util.Pair;
import fterms.exceptions.FeatureTermException;
import fterms.subsumption.CSPSubsumption;
import util.FTSubsumptionRecord;

public abstract class FeatureTerm {

    Symbol m_name = null;
    Sort m_sort;

    public Sort getSort() {
        return m_sort;
    }

    public void setSort(Sort s) {
        m_sort = s;
    }

    public Symbol getName() {
        return m_name;
    }

    public void setName(Symbol name) {
        m_name = name;
    }

    public int getDataType() {
        if (m_sort == null) {
            return Sort.DATATYPE_SET;
        }

        return m_sort.m_data_type;
    }

    public boolean equivalents(FeatureTerm f) throws FeatureTermException {
        return subsumes(f) && f.subsumes(this);
    }

    public abstract boolean hasValue();

    public abstract boolean isLeaf();

    public boolean isConstant() {
        return false;
    }

    public List<FeatureTerm> featureValues(String feature) throws FeatureTermException {
        FeatureTerm v = featureValue(feature);
        if (v==null) return new LinkedList<FeatureTerm>();
        if (v instanceof SetFeatureTerm) {
            List<FeatureTerm> l = new LinkedList<FeatureTerm>();
            l.addAll(((SetFeatureTerm)v).getSetValues());
            return l;
        } else {
            List<FeatureTerm> l = new LinkedList<FeatureTerm>();
            l.add(v);
            return l;
        }
    }

    public List<FeatureTerm> featureValues(Symbol feature) throws FeatureTermException {
        FeatureTerm v = featureValue(feature);
        if (v==null) return new LinkedList<FeatureTerm>();
        if (v instanceof SetFeatureTerm) {
            List<FeatureTerm> l = new LinkedList<FeatureTerm>();
            l.addAll(((SetFeatureTerm)v).getSetValues());
            return l;
        } else {
            List<FeatureTerm> l = new LinkedList<FeatureTerm>();
            l.add(v);
            return l;
        }
    }


    public FeatureTerm featureValue(String feature) throws FeatureTermException {
        throw new FeatureTermException("FeatureValue of non-term: " + this.getClass().getName() + " -> " + feature);
    }

    public FeatureTerm featureValue(Symbol feature) throws FeatureTermException {
        throw new FeatureTermException("FeatureValue of non-term: " + this.getClass().getName() + " -> " + feature.get());
    }

    public FeatureTerm readPath(Path path) throws FeatureTermException {
        FeatureTerm t = this;

        for (Symbol s : path.features) {
            if (t != null) {
                t = t.featureValue(s);
            }
        } // for

        return t;
    } // FeatureTerm::readPath

    public String toStringNOOS() {
        return toStringNOOS(null);
    } // FeatureTerm::toStringNOOS

    public String toStringNOOS(int tabs) {
        return toStringNOOS(null, tabs);
    } // FeatureTerm::toStringNOOS

    public String toStringNOOS(FTKBase dm) {
        String tmp;
        List<FeatureTerm> bindings = new LinkedList<FeatureTerm>();

        tmp = toStringNOOSInternal(bindings, 0, dm);

        {
            int i;
            String tmp2, tmp3;

            // Search for nonused variables, and remove them:
            for (i = 0; i < bindings.size(); i++) {
                tmp2 = "!X" + (i + 1) + ")";
                tmp3 = "!X" + (i + 1) + "\n";
                if (!tmp.contains(tmp2) && !tmp.contains(tmp3)) {
                    // delete variable:
                    tmp2 = "?X" + (i + 1) + " ";
                    tmp = tmp.replace(tmp2, "");
                } // if
            } // for
        }

        return tmp;
    } // FeatureTerm::toStringNOOS

    public String toStringNOOS(FTKBase dm, int tabs) {
        String tmp;
        List<FeatureTerm> bindings = new LinkedList<FeatureTerm>();

        tmp = toStringNOOSInternal(bindings, tabs, dm);

        {
            int i;
            String tmp2, tmp3;

            // Search for nonused variables, and remove them:
            for (i = 0; i < bindings.size(); i++) {
                tmp2 = "!X" + (i + 1) + ")";
                tmp3 = "!X" + (i + 1) + "\n";
                if (!tmp.contains(tmp2) && !tmp.contains(tmp3)) {
                    // delete variable:
                    tmp2 = "?X" + (i + 1) + " ";
                    tmp = tmp.replace(tmp2, "");
                } // if
            } // for
        }

        return tmp;
    } // FeatureTerm::toStringNOOS

    String toStringNOOSInternal(List<FeatureTerm> bindings, int tabs, FTKBase dm) {
        String tmp = "";
        int ID = -1;

        if (m_name != null && dm != null && dm.contains(this)) {
            return tmp + m_name.get();
        }

        ID = bindings.indexOf(this);
        if (ID == -1) {
            bindings.add(this);
            ID = bindings.indexOf(this);

            tmp += "(define ?X" + (ID + 1) + " (" + m_sort.get();

            if (m_name != null) {
                tmp += " :id " + m_name.get();
            } // if

            return tmp + "))";
        } else {
            return "!X" + (ID + 1);
        } // if
    } // FeatureTerm::toStringNOOSInternal

    //	 Makes an independent copy of the Feature Term
    public FeatureTerm clone(Ontology o) throws FeatureTermException {
        return clone((FTKBase) null, o);
    } // FeatureTerm::clone

    public FeatureTerm clone(FTKBase dm, Ontology o) throws FeatureTermException {
        HashMap<FeatureTerm, FeatureTerm> correspondences = new HashMap<FeatureTerm, FeatureTerm>();

        return cloneInternal(correspondences, dm, o);
    } // FeatureTerm::clone

    public FeatureTerm clone(HashMap<FeatureTerm, FeatureTerm> correspondences) throws FeatureTermException {
        return clone(null, correspondences);
    } // FeatureTerm::clone

    public FeatureTerm clone(FTKBase dm, HashMap<FeatureTerm, FeatureTerm> correspondences) throws FeatureTermException {
        return cloneInternal(correspondences, dm, m_sort.getOntology());
    } // FeatureTerm::clone

    public FeatureTerm clone(FTKBase dm, Ontology o, HashMap<FeatureTerm, FeatureTerm> correspondences) throws FeatureTermException {
        return cloneInternal(correspondences, dm, o);
    } // FeatureTerm::clone

    FeatureTerm cloneInternal(HashMap<FeatureTerm, FeatureTerm> correspondences, FTKBase dm, Ontology o) throws FeatureTermException {
        FeatureTerm correspondence = correspondences.get(this);

        if (correspondence == null) {
            if (dm == null || !dm.contains(this)) {

                //				if (m_name!=null) {
                //					System.err.println("Warning(dm)! cloning a named feature term: " + m_name.get());
                //				} // if

                return cloneInternal2(correspondences, dm, o);
            } else {
                return this;
            } // if
        } else {
            return correspondence;
        } // if
    } // FeatureTerm::cloneInternal

    public void substitute(FeatureTerm f1, FeatureTerm f2) {
        HashSet<FeatureTerm> visited = new HashSet<FeatureTerm>();
        List<FeatureTerm> open_nodes = new LinkedList<FeatureTerm>();
        FeatureTerm node;
        boolean beautifySets = false;

        visited.add(this);
        open_nodes.add(this);
        open_nodes.add(f2);

        while (!open_nodes.isEmpty()) {
            node = open_nodes.remove(0);

            if (node instanceof TermFeatureTerm) {
                for (Entry<Symbol, FeatureTerm> feature2 : ((TermFeatureTerm) node).getFeatures()) {
                    FeatureTerm ft2 = feature2.getValue();
                    if (ft2.equals(f1)) {
                        ((TermFeatureTerm) node).defineFeatureValue(feature2.getKey(), f2);
                    } else {
                        if (!visited.contains(ft2)) {
                            visited.add(ft2);
                            open_nodes.add(ft2);
                        }
                    }
                }
            } // if

            if (node instanceof SetFeatureTerm) {
                List<FeatureTerm> setValues = ((SetFeatureTerm) node).getSetValues();
                if (setValues.contains(f2)) {
                    setValues.remove(f1);
                    if (setValues.size()==1) beautifySets = true;
                    for (FeatureTerm ft2 : setValues) {
                        if (!visited.contains(ft2)) {
                            visited.add(ft2);
                            open_nodes.add(ft2);
                        }
                    } // for
                } else {
                    for (FeatureTerm ft2 : setValues) {
                        if (ft2.equals(f1)) {
                            ((SetFeatureTerm) node).getSetValues().set(((SetFeatureTerm) node).getSetValues().indexOf(ft2), f2);
                        } else {
                            if (!visited.contains(ft2)) {
                                visited.add(ft2);
                                open_nodes.add(ft2);
                            }
                        }
                    } // for
                }
            } // if/
        } // while

        if (beautifySets) {
            HashMap<SetFeatureTerm, Set<Pair<TermFeatureTerm, Symbol>>> m = FTRefinement.setsWithAllParents(this);
            for(SetFeatureTerm set:m.keySet()) {
                if (set.getSetValues().size()==1) {
                    Set<Pair<TermFeatureTerm, Symbol>> parents = m.get(set);
                    for(Pair<TermFeatureTerm, Symbol> parent:parents) {
                        parent.m_a.defineFeatureValue(parent.m_b, set.getSetValues().get(0));
                    }
                }
            }
        }

    } // substitute

    abstract FeatureTerm cloneInternal2(HashMap<FeatureTerm, FeatureTerm> correspondences, FTKBase dm, Ontology o) throws FeatureTermException;

    
    public boolean subsumes(FeatureTerm f) throws FeatureTermException {
        List<FeatureTerm> bindings_a = new ArrayList<FeatureTerm>();
        List<FeatureTerm> bindings_b = new ArrayList<FeatureTerm>();

//        long start = System.currentTimeMillis();
        boolean res = FTSubsumption.subsumptionWithBindings(this,f,bindings_a,bindings_b,0);
//        long end = System.currentTimeMillis();
//        long startCSP = System.currentTimeMillis();
//        CSPSubsumption.subsumes(this,f);
//        long endCSP = System.currentTimeMillis();

//        if (res!=resCSP) System.err.println("subsumption different!!!!!!!!!!!!!!!!!!");

//        FTSubsumptionRecord.register(this,f,end-start,endCSP-startCSP,res);
        return res;
    }



    public boolean checkDataType() {

        if (m_sort == null) {
            if (!(this instanceof SetFeatureTerm)) {
                System.err.println("Feature term has data type " + m_sort.m_data_type + " and has class " + this.getClass().getSimpleName());
                return false;
            }
            return true;
        }
        if (m_sort.m_data_type == Sort.DATATYPE_INTEGER && !(this instanceof IntegerFeatureTerm) ||
            m_sort.m_data_type == Sort.DATATYPE_FLOAT && !(this instanceof FloatFeatureTerm) ||
            m_sort.m_data_type == Sort.DATATYPE_SYMBOL && !(this instanceof SymbolFeatureTerm) ||
            m_sort.m_data_type == Sort.DATATYPE_FEATURETERM && !(this instanceof TermFeatureTerm) ||
            m_sort.m_data_type == Sort.DATATYPE_SET && !(this instanceof SetFeatureTerm)) {
            System.err.println("Feature term has data type " + m_sort.m_data_type + " for sort " + m_sort.get() + " and has class " + this.getClass().getSimpleName());
            return false;
        }

        return true;
    }

    public boolean consistencyCheck(FTKBase dm) {
        List<Pair<FeatureTerm, Path>> nodes = FTRefinement.variablesWithPaths(this, dm);

        for (Pair<FeatureTerm, Path> node : nodes) {
            if (!node.m_a.checkDataType()) {
                System.err.println("Feature Term not properly formed! error in path: " + node.m_b);
                return false;
            }
        }

        return true;
    }
}
