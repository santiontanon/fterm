/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fterms.learning;

import fterms.BaseOntology;
import fterms.FTKBase;
import fterms.FeatureTerm;
import fterms.Ontology;
import fterms.Path;
import fterms.Symbol;
import fterms.exceptions.FeatureTermException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import util.Sampler;

/**
 *
 * @author santi
 */
public class TrainingSetUtils {
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

        System.out.println("Desired bias: " + bias);
        System.out.println("Initial bias: " + cbias);

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

        System.out.println("Adjusted bias: " + cbias);

        // Compute how many cases to distribtue according to redundancy:
        int ncases = (int)((redundancy*(examples.size())*(n-1)) + examples.size());
        System.out.println("Redundancy " + redundancy +" -> " + ncases);

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
            System.out.println("Distribution for " + s.toStringNOOS(dm) + " -> " + d);
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
    public static final int UNCLE_DATASET = 7;
    public static final int UNCLE_DATASET_SETS = 8;
    public static final int CARS_DATASET = 9;
    public static final int TOXICOLOGY_DATASET_MRATS = 10;
    public static final int TOXICOLOGY_DATASET_FRATS = 11;
    public static final int TOXICOLOGY_DATASET_MMICE = 12;
    public static final int TOXICOLOGY_DATASET_FMICE = 13;
    public static final int KR_VS_KP_DATASET = 14;
    public static final int FINANCIAL = 15;
    public static final int FINANCIAL_NO_TRANSACTIONS = 16;

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
            case CARS_DATASET:
                dm.ImportNOOS("NOOS/car-ontology.noos", o);
                dm.ImportNOOS("NOOS/car-dm.noos", o);
                case_base.ImportNOOS("NOOS/car-1728.noos", o);

                ts.name = "cars";
                ts.problem_sort = o.getSort("car-problem");
                break;
            case TOXICOLOGY_DATASET_MRATS:
                dm.ImportNOOS("NOOS/toxic-eva-ontology.noos", o);
                dm.ImportNOOS("NOOS/toxic-eva-dm.noos", o);
                case_base.ImportNOOS("NOOS/toxic-eva-filtered-cases-276.noos", o);

                ts.solution_path.features.add(new Symbol("m-rats"));

                ts.name = "toxicology";
                ts.problem_sort = o.getSort("toxic-problem");
                break;
            case TOXICOLOGY_DATASET_FRATS:
                dm.ImportNOOS("NOOS/toxic-eva-ontology.noos", o);
                dm.ImportNOOS("NOOS/toxic-eva-dm.noos", o);
                case_base.ImportNOOS("NOOS/toxic-eva-filtered-cases-276.noos", o);

                ts.solution_path.features.add(new Symbol("f-rats"));
                //				sp.features.add(new Symbol("m-mice"));
                //				sp.features.add(new Symbol("f-mice"));

                ts.name = "toxicology";
                ts.problem_sort = o.getSort("toxic-problem");
                break;
            case TOXICOLOGY_DATASET_MMICE:
                dm.ImportNOOS("NOOS/toxic-eva-ontology.noos", o);
                dm.ImportNOOS("NOOS/toxic-eva-dm.noos", o);
                case_base.ImportNOOS("NOOS/toxic-eva-filtered-cases-276.noos", o);

                ts.solution_path.features.add(new Symbol("m-mice"));
                //				sp.features.add(new Symbol("f-mice"));

                ts.name = "toxicology";
                ts.problem_sort = o.getSort("toxic-problem");
                break;
            case TOXICOLOGY_DATASET_FMICE:
                dm.ImportNOOS("NOOS/toxic-eva-ontology.noos", o);
                dm.ImportNOOS("NOOS/toxic-eva-dm.noos", o);
                case_base.ImportNOOS("NOOS/toxic-eva-filtered-cases-276.noos", o);

                ts.solution_path.features.add(new Symbol("f-mice"));

                ts.name = "toxicology";
                ts.problem_sort = o.getSort("toxic-problem");
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
            default:
                return null;
        }

        ts.cases.addAll(case_base.SearchFT(ts.problem_sort));
        return ts;
    }
}
