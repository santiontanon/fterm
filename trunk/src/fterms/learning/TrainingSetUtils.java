/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fterms.learning;

import fterms.BaseOntology;
import fterms.FTKBase;
import fterms.FeatureTerm;
import fterms.FloatFeatureTerm;
import fterms.IntegerFeatureTerm;
import fterms.Ontology;
import fterms.Path;
import fterms.Symbol;
import fterms.exceptions.FeatureTermException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import util.Pair;
import util.Sampler;

/**
 *
 * @author santi
 */
public class TrainingSetUtils {
    public static int DEBUG = 0;
    
    public static List<List<FeatureTerm>> splitTrainingSet(Collection<FeatureTerm> examples, int n, Path dp, Path sp, FTKBase dm, double bias, double redundancy) throws FeatureTermException, Exception {
        double matrix[] = null;
        double cbias = 0;
        List<FeatureTerm> differentSolutions = Hypothesis.differentSolutions(examples, sp);
        int ns = differentSolutions.size();
        Random r = new Random();

        // Generate the bias matrix:
        // generate an initial matrix as close as possible to the desired bias:
        for(int i = 0;i<100;i++) {
            double m[] = new double[n*ns];
            double mbias = 0;
            for(int j = 0;j<n*ns;j++) m[j]=r.nextFloat();
            for(int j = 0;j<n;j++) {
                double t = 0;
                for(int k = 0;k<ns;k++) t+=m[k*n+j];
                for(int k = 0;k<ns;k++) m[k*n+j]/=t;
            }
            for(int j = 0;j<n;j++) {
                double t = 0;
                for(int k = 0;k<ns;k++) t = (m[k*n+j]-(1.0/ns))*(m[k*n+j]-(1.0/ns));
                mbias+=Math.sqrt(t);
            }
            mbias/=n;

            if (matrix==null || Math.abs(bias-mbias)<Math.abs(bias-cbias)) {
                matrix = m;
                cbias = mbias;
            }
        }

        if (DEBUG>=1) System.out.println("Desired bias: " + bias);
        if (DEBUG>=1) System.out.println("Initial bias: " + cbias);

        // Adjust matrix to get closer to desired bias:
        for(int i = 0;i<1000;i++) {
            boolean stop = true;
            double modifiers[]={0.5, 0.75, 0.8, 0.9, 1.1, 1.25, 1.5, 2.0};

            for(double modifier:modifiers) {
                double m[] = new double[n*ns];
                for(int j = 0;j<n*ns;j++) m[j] = matrix[j];
                double mbias = 0;
                for(int j = 0;j<n;j++) {
                    for(int k = 0;k<ns;k++) {
                        m[k*n+j] = ((m[k*n+j]-(1.0/n))*modifier)+(1.0/n);
                        if (m[k*n+j]<0) m[k*n+j]=0;
                        if (m[k*n+j]>1) m[k*n+j]=1;
                    }
                    double t = 0;
                    for(int k = 0;k<ns;k++) t+=m[k*n+j];
                    if (t>0) for(int k = 0;k<ns;k++) m[k*n+j]/=t;
                }
                for(int j = 0;j<n;j++) {
                    double t = 0;
                    for(int k = 0;k<ns;k++) t = (m[k*n+j]-(1.0/ns))*(m[k*n+j]-(1.0/ns));
                    mbias+=Math.sqrt(t);
                }
                mbias/=n;
                if (Math.abs(bias-mbias)<Math.abs(bias-cbias)) {
//                    System.out.println(modifier + " -> " + mbias);
                    for(int j = 0;j<n*ns;j++) matrix[j] = m[j];
                    cbias = mbias;
                    stop = false;
/*
                    for(FeatureTerm s:differentSolutions) {
                        int j = differentSolutions.indexOf(s);
                        List<Double> d = new LinkedList<Double>();
                        for(int k = 0;k<n;k++) d.add(matrix[k*ns+j]);
                        System.out.println("D for " + s.toStringNOOS(dm) + " -> " + d);
                    }
*/
                }
            }
            if (stop) break;
        }

        if (DEBUG>=1) System.out.println("Adjusted bias: " + cbias);

        // Compute how many cases to distribtue according to redundancy:
        int ncases = (int)((redundancy*(examples.size())*(n-1)) + examples.size());
        if (DEBUG>=1) System.out.println("Redundancy " + redundancy +" -> " + ncases);

        // Sample:
        List<List<FeatureTerm>> training_sets = new LinkedList<List<FeatureTerm>>();
        List<Hypothesis> hypotheses = new LinkedList<Hypothesis>();
        List<FeatureTerm> casesToDistribute = new LinkedList<FeatureTerm>();
        HashMap<FeatureTerm,List<Double>> distributions = new HashMap<FeatureTerm,List<Double>>();

        for(FeatureTerm s:differentSolutions) {
            int i = differentSolutions.indexOf(s);
            List<Double> d = new LinkedList<Double>();
            for(int k = 0;k<n;k++) d.add(matrix[k*ns+i]);
            distributions.put(s,d);
            if (DEBUG>=1) System.out.println("Distribution for " + s.toStringNOOS(dm) + " -> " + d);
        }

        for(int i = 0;i<n;i++) training_sets.add(new LinkedList<FeatureTerm>());
        for(int i = 0;i<ncases;i++) {
            if (casesToDistribute.isEmpty()) casesToDistribute.addAll(examples);

            FeatureTerm e = casesToDistribute.get(r.nextInt(casesToDistribute.size()));
            FeatureTerm s = e.readPath(sp);

            boolean found = false;
            // First try to assign it to an agent according to the bias:
            for(int j = 0;j<10;j++) {
                int a = Sampler.weighted(distributions.get(s));
                if (!training_sets.get(a).contains(e)) {
                    training_sets.get(a).add(e);
                    found = true;
                    break;
                }
            }
            // If not possible give it to a random agent which does not have the case:
            if (!found) {
                while(true) {
                    int a = Sampler.random(distributions.get(s));
                    if (training_sets.get(a).contains(e)) {
                        training_sets.get(a).add(e);
                        break;
                    }
                }
            }
            casesToDistribute.remove(e);
        }
        return training_sets;
    }


    public static final int ARTIFICIAL_DATASET = 0;
    public static final int ZOOLOGY_DATASET = 1;
    public static final int SOYBEAN_DATASET = 2;
    public static final int DEMOSPONGIAE_503_DATASET = 3;
    public static final int DEMOSPONGIAE_280_DATASET = 4;
    public static final int DEMOSPONGIAE_120_DATASET = 5;
    public static final int TRAINS_DATASET = 6;
    public static final int TRAINS_82_DATASET = 61;
    public static final int TRAINS_900_DATASET = 62;
    public static final int UNCLE_DATASET = 7;
    public static final int UNCLE_DATASET_SETS = 8;
    public static final int UNCLE_DATASET_BOTH = 9;
    public static final int CARS_DATASET = 10;
    public static final int TOXICOLOGY_DATASET_MRATS = 11;
    public static final int TOXICOLOGY_DATASET_FRATS = 12;
    public static final int TOXICOLOGY_DATASET_MMICE = 13;
    public static final int TOXICOLOGY_DATASET_FMICE = 14;
    public static final int TOXICOLOGY_OLD_DATASET_MRATS = 15;
    public static final int TOXICOLOGY_OLD_DATASET_FRATS = 16;
    public static final int TOXICOLOGY_OLD_DATASET_MMICE = 17;
    public static final int TOXICOLOGY_OLD_DATASET_FMICE = 18;
    public static final int KR_VS_KP_DATASET = 19;
    public static final int FINANCIAL = 20;
    public static final int FINANCIAL_NO_TRANSACTIONS = 21;
    public static final int MUTAGENESIS = 22;
    public static final int MUTAGENESIS_EASY = 23;
    public static final int MUTAGENESIS_DISCRETIZED = 24;
    public static final int MUTAGENESIS_EASY_DISCRETIZED = 25;
    public static final int MUTAGENESIS_NOL_DISCRETIZED = 26;
    public static final int MUTAGENESIS_EASY_NOL_DISCRETIZED = 27;
    public static final int RIU_STORIES = 28;

    public static TrainingSetProperties loadTrainingSet(int DATASET, Ontology o, FTKBase dm, FTKBase case_base) throws FeatureTermException, IOException
    {
        TrainingSetProperties ts = new TrainingSetProperties();
        ts.description_path = new Path();
        ts.solution_path = new Path();
        ts.cases = new LinkedList<FeatureTerm>();

        ts.description_path.features.add(new Symbol("description"));
        ts.solution_path.features.add(new Symbol("solution"));

        switch(DATASET) {
            case ARTIFICIAL_DATASET:
                dm.ImportNOOS("NOOS/artificial-ontology.noos", o);
                dm.ImportNOOS("NOOS/artificial-dm.noos", o);
                case_base.ImportNOOS("NOOS/artificial-512.noos", o);

                ts.name = "artificial";
                ts.problem_sort = o.getSort("artificial-data-problem");
                break;
            case ZOOLOGY_DATASET:
                dm.ImportNOOS("NOOS/zoology-ontology.noos", o);
                dm.ImportNOOS("NOOS/zoology-dm.noos", o);
                case_base.ImportNOOS("NOOS/zoology-cases-101.noos", o);

                ts.name = "zoology";
                ts.problem_sort = o.getSort("zoo-problem");
                break;
            case SOYBEAN_DATASET:
                dm.ImportNOOS("NOOS/soybean-ontology.noos", o);
                dm.ImportNOOS("NOOS/soybean-dm.noos", o);
                case_base.ImportNOOS("NOOS/soybean-cases-307.noos", o);

                ts.name = "soybean";
                ts.problem_sort = o.getSort("soybean-problem");
                break;
            case DEMOSPONGIAE_503_DATASET:
                dm.ImportNOOS("NOOS/sponge-ontology.noos", o);
                dm.ImportNOOS("NOOS/sponge-dm.noos", o);
                case_base.ImportNOOS("NOOS/sponge-cases-503.noos",o);

                ts.solution_path.features.add(new Symbol("order"));

                ts.name = "demospongiae";
                ts.problem_sort = o.getSort("sponge-problem");
                break;
            case DEMOSPONGIAE_280_DATASET:
                dm.ImportNOOS("NOOS/sponge-ontology.noos", o);
                dm.ImportNOOS("NOOS/sponge-dm.noos", o);
                case_base.ImportNOOS("NOOS/sponge-cases-280.noos",o);

                ts.solution_path.features.add(new Symbol("order"));

                ts.name = "demospongiae";
                ts.problem_sort = o.getSort("sponge-problem");
                break;
            case DEMOSPONGIAE_120_DATASET:
                dm.ImportNOOS("NOOS/sponge-ontology.noos", o);
                dm.ImportNOOS("NOOS/sponge-dm.noos", o);
                case_base.ImportNOOS("NOOS/sponge-cases-120.noos",o);

                ts.solution_path.features.add(new Symbol("order"));

                ts.name = "demospongiae";
                ts.problem_sort = o.getSort("sponge-problem");
                break;
            case TRAINS_DATASET:
                dm.ImportNOOS("NOOS/trains-ontology.noos", o);
                dm.ImportNOOS("NOOS/trains-dm.noos", o);
                case_base.ImportNOOS("NOOS/trains-cases-10.noos", o);

                ts.name = "trains";
                ts.problem_sort = o.getSort("trains-problem");
                break;
            case TRAINS_82_DATASET:
                dm.ImportNOOS("NOOS/trains-ontology.noos", o);
                dm.ImportNOOS("NOOS/trains-dm.noos", o);
                case_base.ImportNOOS("NOOS/trains-cases-82.noos", o);

                ts.name = "trains";
                ts.problem_sort = o.getSort("trains-problem");
                break;
            case TRAINS_900_DATASET:
                dm.ImportNOOS("NOOS/trains-ontology.noos", o);
                dm.ImportNOOS("NOOS/trains-dm.noos", o);
                case_base.ImportNOOS("NOOS/trains-cases-900.noos", o);

                ts.name = "trains";
                ts.problem_sort = o.getSort("trains-problem");
                break;
            case UNCLE_DATASET:
                dm.ImportNOOS("NOOS/family-ontology.noos", o);
                dm.ImportNOOS("NOOS/family-dm.noos", o);
                case_base.ImportNOOS("NOOS/family-cases-12.noos", o);

                ts.name = "uncle";
                ts.problem_sort = o.getSort("uncle-problem");
                break;
            case UNCLE_DATASET_SETS:
                dm.ImportNOOS("NOOS/family-ontology.noos", o);
                dm.ImportNOOS("NOOS/family-dm.noos", o);
                case_base.ImportNOOS("NOOS/family-cases-12-sets.noos",o);

                ts.name = "uncle";
                ts.problem_sort = o.getSort("uncle-problem");
                break;
            case UNCLE_DATASET_BOTH:
                dm.ImportNOOS("NOOS/family-ontology.noos", o);
                dm.ImportNOOS("NOOS/family-dm.noos", o);
                case_base.ImportNOOS("NOOS/family-cases-12.noos",o);
                case_base.ImportNOOS("NOOS/family-cases-12-sets.noos",o);

                ts.name = "uncle";
                ts.problem_sort = o.getSort("uncle-problem");
                break;
            case CARS_DATASET:
                dm.ImportNOOS("NOOS/car-ontology.noos", o);
                dm.ImportNOOS("NOOS/car-dm.noos", o);
                case_base.ImportNOOS("NOOS/car-1728.noos", o);

                ts.name = "cars";
                ts.problem_sort = o.getSort("car-problem");
                break;

            case TOXICOLOGY_DATASET_MRATS:
            case TOXICOLOGY_DATASET_FRATS:
            case TOXICOLOGY_DATASET_MMICE:
            case TOXICOLOGY_DATASET_FMICE:
                dm.ImportNOOS("NOOS/toxic-eva-ontology.noos", o);
                dm.ImportNOOS("NOOS/toxic-eva-dm.noos", o);
//                case_base.ImportNOOS("NOOS/toxic-eva-filtered-cases-276.noos", o);
//                case_base.ImportNOOS("NOOS/toxic-eva-cases-371.noos", o);
//                case_base.ImportNOOS("NOOS/toxic-eva-fixed-cases-371.noos", o);
                case_base.ImportNOOS("NOOS/toxic-santi-cases-353.noos", o);

                switch(DATASET) {
                case TOXICOLOGY_DATASET_MRATS:
                    ts.solution_path.features.add(new Symbol("m-rats")); break;
                case TOXICOLOGY_DATASET_FRATS:
                    ts.solution_path.features.add(new Symbol("f-rats")); break;
                case TOXICOLOGY_DATASET_MMICE:
                    ts.solution_path.features.add(new Symbol("m-mice")); break;
                case TOXICOLOGY_DATASET_FMICE:
                    ts.solution_path.features.add(new Symbol("f-mice")); break;
                }

                ts.name = "toxicology";
                ts.problem_sort = o.getSort("toxic-problem");

                {
                    List<FeatureTerm> cs = new LinkedList<FeatureTerm>();
                    List<FeatureTerm> toDelete = new LinkedList<FeatureTerm>();
                    cs.addAll(case_base.SearchFT(ts.problem_sort));

                    for(FeatureTerm c:cs) {
                        FeatureTerm s = c.readPath(ts.solution_path);
                        String ss = s.toStringNOOS(dm);
                        if (!ss.equals("positive") && !ss.equals("negative")) {
                            // remove example, inqdequate!
                            case_base.DeleteFT(c);
                        }
                    }

                }
                break;

            case TOXICOLOGY_OLD_DATASET_MRATS:
            case TOXICOLOGY_OLD_DATASET_FRATS:
            case TOXICOLOGY_OLD_DATASET_MMICE:
            case TOXICOLOGY_OLD_DATASET_FMICE:
                dm.ImportNOOS("NOOS/toxic-eva-old-ontology.noos", o);
                dm.ImportNOOS("NOOS/toxic-eva-old-dm.noos", o);
                case_base.ImportNOOS("NOOS/toxic-eva-old-cases.noos", o);

                switch(DATASET) {
                case TOXICOLOGY_OLD_DATASET_MRATS:
                    ts.solution_path.features.add(new Symbol("m-rats")); break;
                case TOXICOLOGY_OLD_DATASET_FRATS:
                    ts.solution_path.features.add(new Symbol("f-rats")); break;
                case TOXICOLOGY_OLD_DATASET_MMICE:
                    ts.solution_path.features.add(new Symbol("m-mice")); break;
                case TOXICOLOGY_OLD_DATASET_FMICE:
                    ts.solution_path.features.add(new Symbol("f-mice")); break;
                }

                ts.name = "toxicology-old";
                ts.problem_sort = o.getSort("toxic-problem");

                {
                    List<FeatureTerm> cs = new LinkedList<FeatureTerm>();
                    List<FeatureTerm> toDelete = new LinkedList<FeatureTerm>();
                    cs.addAll(case_base.SearchFT(ts.problem_sort));

                    for(FeatureTerm c:cs) {
                        FeatureTerm s = c.readPath(ts.solution_path);
                        if (s!=null) {
                            String ss = s.toStringNOOS(dm);
                            if (!ss.equals("positive") && !ss.equals("negative")) {
                                // remove example, inqdequate!
                                case_base.DeleteFT(c);
                            }
                        }
                    }

                }
                break;

            case KR_VS_KP_DATASET:
                dm.ImportNOOS("NOOS/kr-vs-kp-ontology.noos", o);
                dm.ImportNOOS("NOOS/kr-vs-kp-dm.noos", o);
                case_base.ImportNOOS("NOOS/kr-vs-kp-3196.noos", o);

                ts.name = "kr-vs-kp";
                ts.problem_sort = o.getSort("kr-vs-kp-problem");
                break;
            case FINANCIAL_NO_TRANSACTIONS:
                dm.ImportNOOS("NOOS/financial-ontology.noos", o);
                dm.ImportNOOS("NOOS/financial-dm.noos", o);
//                case_base.ImportNOOS("NOOS/financial-cases-682-no-transactions.noos", o);
                case_base.ImportNOOS("NOOS/financial-cases-10-no-transactions.noos", o);

                ts.name = "financial-no-t";
                ts.problem_sort = o.getSort("loan-problem");

                ts.description_path.features.clear();
                ts.solution_path.features.clear();
                ts.description_path.features.add(new Symbol("loan"));
                ts.solution_path.features.add(new Symbol("status"));
                break;
            case FINANCIAL:
                dm.ImportNOOS("NOOS/financial-ontology.noos", o);
                dm.ImportNOOS("NOOS/financial-dm.noos", o);
                case_base.ImportNOOS("NOOS/financial-cases-10.noos", o);
//                case_base.ImportNOOS("NOOS/financial-cases-682.noos", o);

                ts.name = "financial-no-t";
                ts.problem_sort = o.getSort("loan-problem");

                ts.description_path.features.clear();
                ts.solution_path.features.clear();
                ts.description_path.features.add(new Symbol("loan"));
                ts.solution_path.features.add(new Symbol("status"));
                break;
            case MUTAGENESIS:
                dm.ImportNOOS("NOOS/mutagenesis-ontology.noos", o);
                dm.ImportNOOS("NOOS/mutagenesis-dm.noos", o);
//                case_base.ImportNOOS("NOOS/mutagenesis-b4-230-cases.noos", o);
                case_base.ImportNOOS("NOOS/mutagenesis-b4-25-cases.noos", o);

                ts.name = "mutagenesis-b4";
                ts.problem_sort = o.getSort("mutagenesis-problem");

                ts.description_path.features.clear();
                ts.solution_path.features.clear();
                ts.description_path.features.add(new Symbol("problem"));
                ts.solution_path.features.add(new Symbol("solution"));
                break;
            case MUTAGENESIS_EASY:
                dm.ImportNOOS("NOOS/mutagenesis-ontology.noos", o);
                dm.ImportNOOS("NOOS/mutagenesis-dm.noos", o);
                case_base.ImportNOOS("NOOS/mutagenesis-b4-188-cases.noos", o);

                ts.name = "mutagenesis-b4";
                ts.problem_sort = o.getSort("mutagenesis-problem");

                ts.description_path.features.clear();
                ts.solution_path.features.clear();
                ts.description_path.features.add(new Symbol("problem"));
                ts.solution_path.features.add(new Symbol("solution"));
                break;
            case MUTAGENESIS_DISCRETIZED:
                dm.ImportNOOS("NOOS/mutagenesis-ontology.noos", o);
                dm.ImportNOOS("NOOS/mutagenesis-dm.noos", o);
//                case_base.ImportNOOS("NOOS/mutagenesis-b4-230-cases.noos", o);
                case_base.ImportNOOS("NOOS/mutagenesis-b4-noH-230-cases.noos", o);
//                case_base.ImportNOOS("NOOS/mutagenesis-b4-noH-25-cases.noos", o);

                ts.name = "mutagenesis-b4-discretized";
                ts.problem_sort = o.getSort("mutagenesis-problem");

                ts.description_path.features.clear();
                ts.solution_path.features.clear();
                ts.description_path.features.add(new Symbol("problem"));
                ts.solution_path.features.add(new Symbol("solution"));

                // discretize:
                {
                    Set<FeatureTerm> cases=case_base.SearchFT(ts.problem_sort);
                    Path fp = new Path();
                    fp.features.add(new Symbol("problem"));
                    fp.features.add(new Symbol("lumo"));
                    TrainingSetUtils.discretizeFeature(cases, fp, ts.solution_path, 2);

                    fp.features.clear();
                    fp.features.add(new Symbol("problem"));
                    fp.features.add(new Symbol("logp"));
                    TrainingSetUtils.discretizeFeature(cases, fp, ts.solution_path, 2);
                }

                break;
            case MUTAGENESIS_EASY_DISCRETIZED:
                dm.ImportNOOS("NOOS/mutagenesis-ontology.noos", o);
                dm.ImportNOOS("NOOS/mutagenesis-dm.noos", o);
                case_base.ImportNOOS("NOOS/mutagenesis-b4-188-cases.noos", o);

                ts.name = "mutagenesis-b4-discretized";
                ts.problem_sort = o.getSort("mutagenesis-problem");

                ts.description_path.features.clear();
                ts.solution_path.features.clear();
                ts.description_path.features.add(new Symbol("problem"));
                ts.solution_path.features.add(new Symbol("solution"));

                // discretize:
                {
                    Set<FeatureTerm> cases=case_base.SearchFT(ts.problem_sort);
                    Path fp = new Path();
                    fp.features.add(new Symbol("problem"));
                    fp.features.add(new Symbol("lumo"));
                    TrainingSetUtils.discretizeFeature(cases, fp, ts.solution_path, 2);

                    fp.features.clear();
                    fp.features.add(new Symbol("problem"));
                    fp.features.add(new Symbol("logp"));
                    TrainingSetUtils.discretizeFeature(cases, fp, ts.solution_path, 2);
                }

                break;
            case MUTAGENESIS_EASY_NOL_DISCRETIZED:
                dm.ImportNOOS("NOOS/mutagenesis-ontology.noos", o);
                dm.ImportNOOS("NOOS/mutagenesis-dm.noos", o);
                case_base.ImportNOOS("NOOS/mutagenesis-b4-noH-noL-188-cases.noos", o);

                ts.name = "mutagenesis-b4-nol-discretized";
                ts.problem_sort = o.getSort("mutagenesis-problem");

                ts.description_path.features.clear();
                ts.solution_path.features.clear();
                ts.description_path.features.add(new Symbol("problem"));
                ts.solution_path.features.add(new Symbol("solution"));

                // discretize:
                {
                    Set<FeatureTerm> cases=case_base.SearchFT(ts.problem_sort);
                    Path fp = new Path();
                    fp.features.add(new Symbol("problem"));
                    fp.features.add(new Symbol("lumo"));
                    TrainingSetUtils.discretizeFeature(cases, fp, ts.solution_path, 2);

                    fp.features.clear();
                    fp.features.add(new Symbol("problem"));
                    fp.features.add(new Symbol("logp"));
                    TrainingSetUtils.discretizeFeature(cases, fp, ts.solution_path, 2);
                }
                break;
            case MUTAGENESIS_NOL_DISCRETIZED:
                dm.ImportNOOS("NOOS/mutagenesis-ontology.noos", o);
                dm.ImportNOOS("NOOS/mutagenesis-dm.noos", o);
                case_base.ImportNOOS("NOOS/mutagenesis-b4-noH-noL-230-cases.noos", o);

                ts.name = "mutagenesis-b4-nol-discretized";
                ts.problem_sort = o.getSort("mutagenesis-problem");

                ts.description_path.features.clear();
                ts.solution_path.features.clear();
                ts.description_path.features.add(new Symbol("problem"));
                ts.solution_path.features.add(new Symbol("solution"));

                // discretize:
                {
                    Set<FeatureTerm> cases=case_base.SearchFT(ts.problem_sort);
                    Path fp = new Path();
                    fp.features.add(new Symbol("problem"));
                    fp.features.add(new Symbol("lumo"));
                    TrainingSetUtils.discretizeFeature(cases, fp, ts.solution_path, 2);

                    fp.features.clear();
                    fp.features.add(new Symbol("problem"));
                    fp.features.add(new Symbol("logp"));
                    TrainingSetUtils.discretizeFeature(cases, fp, ts.solution_path, 2);
                }
                break;
            case RIU_STORIES:
                dm.ImportNOOS("NOOS/story-ontology.noos", o);
                case_base.ImportNOOS("NOOS/story-cases-2.noos", o);

                ts.name = "riu-stories";
                ts.problem_sort = o.getSort("scene");

                ts.description_path.features.clear();
                ts.solution_path.features.clear();
                break;
            default:
                return null;
        }

        ts.cases.addAll(case_base.SearchFT(ts.problem_sort));
        return ts;
    }


    public static void discretizeFeature(Collection<FeatureTerm> cases, Path featurePath, Path solutionPath, int ncuts) throws FeatureTermException {
        List<Float> cuts = findDiscretizationIntervals(cases,featurePath,solutionPath,ncuts);

        // change the values by the discretized ones:
        for(FeatureTerm c:cases) {
            FeatureTerm v = c.readPath(featurePath);
            float fv = 0;
            boolean integer = true;
            if (v!=null) {
                if (v instanceof IntegerFeatureTerm) {
                    fv = ((IntegerFeatureTerm)v).getValue().floatValue();
                } else {
                    fv = ((FloatFeatureTerm)v).getValue();
                    integer = false;
                }

                int newV = 0;
                for(Float cut:cuts) {
                    if (cut<fv) newV++;
                           else break;
                }
                if (integer) {
                    ((IntegerFeatureTerm)v).setValue(newV);
                } else {
                    ((FloatFeatureTerm)v).setValue((float)newV);
                }
            } else {
                System.out.println(c.getName() + " has no value in " + featurePath);
            }
        }
    }

    // this method will split the feature range in 2^uts intervals, and return the cut points:
    public static List<Float> findDiscretizationIntervals(Collection<FeatureTerm> cases, Path featurePath, Path solutionPath, int cuts) throws FeatureTermException {
        List<Pair<Float,Integer>> values = new LinkedList<Pair<Float,Integer>>();
        Vector<FeatureTerm> solutions = new Vector<FeatureTerm>();

        for(FeatureTerm c:cases) {
            FeatureTerm s = c.readPath(solutionPath);
            if (!solutions.contains(s)) solutions.add(s);
        }

        // get all the values:
        for(FeatureTerm c:cases) {
            FeatureTerm v = c.readPath(featurePath);
            FeatureTerm s = c.readPath(solutionPath);
            Float fv = null;

            if (v!=null) {
                if (v instanceof IntegerFeatureTerm) {
                    fv = (((IntegerFeatureTerm)v).getValue()).floatValue();
                } else if (v instanceof FloatFeatureTerm) {
                    fv = ((FloatFeatureTerm)v).getValue();
                } else {
                    throw new FeatureTermException("The feature has a non numeric value!");
                }
                values.add(new Pair<Float,Integer>(fv,solutions.indexOf(s)));
            }
        }

        // sort them:
        {
            boolean change = false;
            int len = values.size();
            do{
                change = false;
                for(int i = 0;i<len-1;i++) {
                    if (values.get(i).m_a>values.get(i+1).m_a) {
                        Pair<Float,Integer> tmp = values.get(i);
                        values.set(i,values.get(i+1));
                        values.set(i+1, tmp);
                        change = true;
                    }
                }
            }while(change);
        }

//        for(Pair<Float,Integer> v:values) {
//            System.out.println(v.m_b + " - " + v.m_a);
//        }

        return discretizeFeatureInternal(values,solutions.size(),cuts);
    }


    static List<Float> discretizeFeatureInternal(List<Pair<Float,Integer>> values, int nSolutions, int cuts) {
        if (cuts==0) {
            return new LinkedList<Float>();
        } else {
            boolean first = true;
            float bestCut = 0, bestE = 0;

//            System.out.println("discretizing " + values.size() + " values");

            int gDistribution[] = new int[nSolutions];
            float gE = 0;
            for(int i = 0 ;i<values.size()-1;i++) {
                float cut = (values.get(i).m_a + values.get(i+1).m_a)/2;

                int d1[] = new int[nSolutions];
                int d2[] = new int[nSolutions];
                int n1 = 0;
                int n2 = 0;

                for(int j = 0;j<values.size();j++) {
                    Pair<Float,Integer> v = values.get(j);
                    if (first) gDistribution[v.m_b]++;
                    if (v.m_a<cut) {
                        d1[v.m_b]++;
                        n1++;
                    } else {
                        d2[v.m_b]++;
                        n2++;
                    }
                }

                // compute entropy:
                if (first) gE = entropy(gDistribution);
                float e1 = entropy(d1);
                float e2 = entropy(d2);
                float e = (e1*n1+e2*n2)/(float)(n1+n2);

                if (first || e<bestE) {
                    first = false;
                    bestE = e;
                    bestCut = cut;

//                    System.out.println("next best: " + cut + " (" + e + ")");
                }
            }

            if (bestE<gE) {
                List<Float> l = new LinkedList<Float>();

                List<Pair<Float,Integer>> vl1 = new LinkedList<Pair<Float,Integer>>();
                List<Pair<Float,Integer>> vl2 = new LinkedList<Pair<Float,Integer>>();

                for(Pair<Float,Integer> v:values) {
                    if (v.m_a<bestCut) {
                        vl1.add(v);
                    } else {
                        vl2.add(v);
                    }
                }

                l.addAll(discretizeFeatureInternal(vl1,nSolutions,cuts-1));
                l.add(new Float(bestCut));
                l.addAll(discretizeFeatureInternal(vl2,nSolutions,cuts-1));
                return l;
            } else {
                return new LinkedList<Float>();
            }
        }
    }

    static float entropy(int hist[]) {
        int n = hist.length;
        int t = 0;

        for(int i = 0;i<n;i++) t+=hist[i];

        float h = 0;

//        System.out.print("[ " + hist[0] + "," + hist[1] + "] -> ");

        for(int i = 0;i<n;i++) {
            if (hist[i]!=0) {
                float f = (float)hist[i]/(float)(t);
                h-=Math.log(f)*f;
            }
        }
//        System.out.println("" + h);
        return h;
    }
}
