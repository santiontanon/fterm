package fterms;

import fterms.exceptions.FeatureTermException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

public class TermFeatureTerm extends FeatureTerm {

    static final boolean STRICT_SECURE = false;	// If set to false, the defineFeatureValueSecure only prints warnings, but
    // does the assignment anyway.
//	List<Symbol> m_feature_name = new ArrayList<Symbol>();
//	List<FeatureTerm> m_feature_value = new ArrayList<FeatureTerm>();
    HashMap<Symbol, FeatureTerm> m_features = new HashMap<Symbol, FeatureTerm>();

    public TermFeatureTerm(Sort s) {
        m_sort = s;
    }

    public TermFeatureTerm(Symbol name, Sort s) {
        m_name = name;
        m_sort = s;
    }

    public TermFeatureTerm(String name, Sort s) throws FeatureTermException {
        m_name = new Symbol(name);
        m_sort = s;
    }

    public Set<Symbol> getFeatureNames() {
        return m_features.keySet();
    }

    public Set<Entry<Symbol, FeatureTerm>> getFeatures() {
        return m_features.entrySet();
    }

    public Collection<FeatureTerm> getFeatureValues() {
        return m_features.values();
    }

    public boolean hasValue() {
        return m_features.size() > 0;
    }

    public FeatureTerm featureValue(Symbol feature) {
        return m_features.get(feature);
    } // featureValue

    public void defineFeatureValue(Symbol feature, FeatureTerm value) {
        m_features.put(feature, value);
    } // FeatureTerm::defineFeatureValue


    public void addFeatureValue(Symbol feature, FeatureTerm value) {
        FeatureTerm t = m_features.get(feature);

        if (t==null) {
            m_features.put(feature, value);
        } else {
            if (t instanceof SetFeatureTerm) {
                ((SetFeatureTerm)t).addSetValue(value);
            } else {
                SetFeatureTerm t2 = new SetFeatureTerm();
                t2.addSetValue(t);
                t2.addSetValue(value);
                m_features.put(feature, t2);
            }
        }
    }

    public void addFeatureValueSecure(Symbol feature, FeatureTerm value) {
        FeatureTerm t = m_features.get(feature);

        if (t==null) {
            defineFeatureValueSecure(feature,value);
        } else {
            if (t instanceof SetFeatureTerm) {
                ((SetFeatureTerm)t).addSetValue(value);
            } else {
                SetFeatureTerm t2 = new SetFeatureTerm();
                t2.addSetValue(t);
                t2.addSetValue(value);
                m_features.put(feature, t2);
                defineFeatureValueSecure(feature,t2);
            }
        }
    }


    // checks the SORT to see if the feature exists
    // and if the value has the correct sort
    public boolean defineFeatureValueSecure(Symbol feature, FeatureTerm value) {
        boolean singleton = false;

        if (feature == null) {
            System.err.println("defineFeatureValueSecure: error 1, feature is null");
            if (STRICT_SECURE) {
                return false;
            }
        }

        if (!m_sort.hasFeature(feature)) {
            System.err.println("defineFeatureValueSecure: feature '" + feature.get() + "' does not belong to sort '" + m_sort.get() + "'");
            if (STRICT_SECURE) {
                return false;
            }
        } // if
        if (!m_sort.featureSort(feature).inSort(value)) {
            System.err.println("defineFeatureValueSecure: error 3");
            System.err.println(value.toStringNOOS());
            System.err.println("is not of the sort " + m_sort.featureSort(feature).get());
            if (STRICT_SECURE) {
                return false;
            }
        } // if

        singleton = m_sort.featureSingleton(feature);

        if (singleton && value instanceof SetFeatureTerm) {
            System.err.println("defineFeatureValueSecure: error 4 in feature " + feature.get() + " - " + value.toStringNOOS());
            if (STRICT_SECURE) {
                return false;
            }
        } // if

        m_features.put(feature, value);

        return true;
    } // FeatureTerm::defineFeatureValueSecure

    public FeatureTerm featureValue(String feature) throws FeatureTermException {
        return m_features.get(new Symbol(feature));
    } // FeatureTerm::featureValue

    public void removeFeatureValue(String feature) throws FeatureTermException {
        removeFeatureValue(new Symbol(feature));
    } // FeatureTerm::removeFeatureValue

    public void removeFeatureValue(Symbol feature) {
        m_features.remove(feature);
    } // FeatureTerm::removeFeatureValue

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

            FeatureTerm f;

            tmp += "(define ?X" + (ID + 1) + " (" + m_sort.get();

            if (m_name != null) {
                tmp += " :id " + m_name.get();
            } // if

            tmp += ")";

            if (!m_features.isEmpty()) {
                tmp += "\n";
                for (int i = 0; i < tabs + 2; i++) {
                    tmp += " ";
                }
            }

            int l = m_features.keySet().size();
            int i = 0;
            for (Symbol s : m_features.keySet()) {
                f = m_features.get(s);

                if (f == null) {
                    tmp += "(" + s.get() + " null)";
                } else {
                    tmp += "(" + s.get() + " " + f.toStringNOOSInternal(bindings, tabs + s.get().length() + 4, dm) + ")";
                }
                i++;

                if (i != l) {
                    tmp += "\n";
                    for (int j = 0; j < tabs + 2; j++) {
                        tmp += " ";
                    }
                } // if 
            } // while

            return tmp + ")";
        } else {
            return "!X" + (ID + 1);
        } // if
    } // FeatureTerm::toStringNOOSInternal

    FeatureTerm cloneInternal2(HashMap<FeatureTerm, FeatureTerm> correspondences, FTKBase dm, Ontology o) throws FeatureTermException {
        TermFeatureTerm f = new TermFeatureTerm(m_name, m_sort);
        correspondences.put(this, f);

        for (Symbol s : m_features.keySet()) {
            f.m_features.put(s, m_features.get(s).cloneInternal(correspondences, dm, o));
        }

        return f;
    }

    public boolean isLeaf() {
        return m_features.size() == 0;
    }
}
