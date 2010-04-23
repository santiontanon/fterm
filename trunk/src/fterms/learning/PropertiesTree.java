/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fterms.learning;

import fterms.Disintegration;
import fterms.FTKBase;
import fterms.FTRefinement;
import fterms.FeatureTerm;
import fterms.Ontology;
import fterms.Path;
import fterms.exceptions.FeatureTermException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author santi
 */
public class PropertiesTree extends InductiveLearner {

    boolean m_fast = false;   // Fast disintegration or formal disintegration

    public PropertiesTree(boolean fast) {
        m_fast = fast;
    }

    public Hypothesis generateHypothesis(List<FeatureTerm> examples, Path dp, Path sp, Ontology o, FTKBase dm) throws Exception {
        PatternTreeHypothesis h = new PatternTreeHypothesis();

        // Generate properties:
        List<FeatureTerm> descriptions = new LinkedList<FeatureTerm>();
        for(FeatureTerm e:examples) descriptions.add(e.readPath(dp));
        List<FeatureTerm> properties = generateAllProperties(descriptions,dm,o);

        // Build the tree:
        h.m_root = generateTree(examples,dp,sp,properties,h, dm, o);

        return h;
    }

    private PatternTreeHypothesis.PatternTreeNode generateTree(List<FeatureTerm> examples, Path dp, Path sp, List<FeatureTerm> properties, PatternTreeHypothesis h, FTKBase dm, Ontology o) throws FeatureTermException {
        System.out.println("PropertiesTree.generateTree: " + examples.size() + " examples and " + properties.size() + " properties.");
        HashMap<FeatureTerm,Integer> distribution = Hypothesis.distribution(examples, sp);

        // Find the best property:
        FeatureTerm best_property = null;
        float best_heuristic = 0;

        if (distribution.size()>1) {
            for(FeatureTerm p:properties) {
                float heuristic = evaluateProperty(p,examples,dp,sp);
                if (best_property==null || heuristic>=best_heuristic) {
                    if (heuristic==best_heuristic) {
                        // Resolve ties by generality (biased towards using general patterns):
                        int s1 = FTRefinement.depth(best_property,dm,o);
                        int s2 = FTRefinement.depth(p,dm,o);

                        if (s2<s1) {
                            best_property = p;
                            best_heuristic = heuristic;
                        }
                    } else {
                        best_property = p;
                        best_heuristic = heuristic;
                    }
                }
            }
        }

        // If any, create node and call recursively:
        if (best_property!=null) {
            PatternTreeHypothesis.PatternTreeNode n = h.new PatternTreeNode(best_property);
            n.m_distribution = distribution;

            List<FeatureTerm> remainingExamplesPositive = new LinkedList<FeatureTerm>();
            List<FeatureTerm> remainingExamplesNegative = new LinkedList<FeatureTerm>();
            List<FeatureTerm> remainingPropertiesPositive = new LinkedList<FeatureTerm>();
            List<FeatureTerm> remainingPropertiesNegative = new LinkedList<FeatureTerm>();

            for(FeatureTerm e:examples) {
                FeatureTerm d = e.readPath(dp);
                if (best_property.subsumes(d)) remainingExamplesPositive.add(e);
                                          else remainingExamplesNegative.add(e);
            }

            for(FeatureTerm p:properties) {
                if (p.subsumes(best_property)) {
                    if (best_property.subsumes(p)) {
//                      remainingPropertiesPositive.add(p);
//                      remainingPropertiesNegative.add(p);
                    } else {
//                      remainingPropertiesPositive.add(p);
                        remainingPropertiesNegative.add(p);
                    }
                } else {
                    if (best_property.subsumes(p)) {
                        remainingPropertiesPositive.add(p);
//                      remainingPropertiesNegative.add(p);
                    } else {
                        remainingPropertiesPositive.add(p);
                        remainingPropertiesNegative.add(p);
                    }
                }
            }

            if (remainingExamplesPositive.size()>0)
                n.m_positiveChild = generateTree(remainingExamplesPositive,dp,sp,remainingPropertiesPositive,h, dm , o);
            if (remainingExamplesNegative.size()>0)
                n.m_negativeChild = generateTree(remainingExamplesNegative,dp,sp,remainingPropertiesNegative,h, dm , o);
            return n;
        } else {
            PatternTreeHypothesis.PatternTreeNode n = h.new PatternTreeNode(best_property);
            n.m_distribution = distribution;
            return n;
        }
    }

    private List<FeatureTerm> generateAllProperties(List<FeatureTerm> objects, FTKBase dm, Ontology o) throws FeatureTermException {
        int count = 0;
        List<FeatureTerm> properties = new LinkedList<FeatureTerm>();

        // Generate all the properties
        for (FeatureTerm object : objects) {
            System.out.println("PropertiesTree.generateAllProperties: processing " + count + " -> " + object.getName());
            //	System.out.println(object.toStringNOOS(dm));

            List<FeatureTerm> properties_tmp = null;
            if (m_fast) {
                properties_tmp = Disintegration.disintegrateFast(object, dm, o);
            } else {
                properties_tmp = Disintegration.disintegrate(object, dm, o);
            }

            System.out.println(properties_tmp.size() + " found, now filtering... (previous total: " + properties.size());

            for (FeatureTerm property : properties_tmp) {
                boolean duplicate = false;

                for (FeatureTerm p : properties) {
                    if (property.equivalents(p)) {
                        duplicate = true;
                        break;
                    }
                }

                if (!duplicate) {
                    properties.add(property);
                }
            }

            count++;
        }
        return properties;
    }

    private float evaluateProperty(FeatureTerm p, List<FeatureTerm> examples, Path dp, Path sp) throws FeatureTermException {
        HashMap<FeatureTerm,Integer> distribution = Hypothesis.distribution(examples, sp);

        List<FeatureTerm> positive = new LinkedList<FeatureTerm>();
        List<FeatureTerm> negative = new LinkedList<FeatureTerm>();

        for(FeatureTerm e:examples) {
            FeatureTerm d = e.readPath(dp);
            if (p.subsumes(d)) positive.add(e);
                          else negative.add(e);
        }
        HashMap<FeatureTerm,Integer> positiveDistribution = Hypothesis.distribution(positive, sp);
        HashMap<FeatureTerm,Integer> negativeDistribution = Hypothesis.distribution(negative, sp);

        float h1 = InformationMeasurement.entropyHash(distribution);
        float h2 = InformationMeasurement.entropyHash(positiveDistribution);
        float h3 = InformationMeasurement.entropyHash(negativeDistribution);

        float gain = h1 - ((((float) (positive.size())) / ((float) (examples.size()))) * h2 +
                           (((float) (negative.size())) / ((float) (examples.size()))) * h3);

//        System.out.println("EvaluateProperty: " + gain + " = " + h1 + " - ( " + (((float) (positive.size())) / ((float) (examples.size()))) + " * "+ h2 + " ) + ( " +
//                           (((float) (negative.size())) / ((float) (examples.size()))) + " * "+ h3 + " )");
//        System.out.println(distribution);
//        System.out.println(positiveDistribution);
//        System.out.println(negativeDistribution);

        return gain;
    }
    
}
