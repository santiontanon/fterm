/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
package ftl.learning.core;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import ftl.base.core.FTKBase;
import ftl.base.core.FeatureTerm;
import ftl.base.core.FloatFeatureTerm;
import ftl.base.core.IntegerFeatureTerm;
import ftl.base.core.Ontology;
import ftl.base.core.Path;
import ftl.base.core.Symbol;
import ftl.base.utils.FeatureTermException;
import ftl.base.utils.Pair;
import ftl.base.utils.Sampler;

// TODO: Auto-generated Javadoc
/**
 * The Class TrainingSetUtils.
 *
 * @author santi
 */
public class TrainingSetUtils {

    /**
     * The DEBUG.
     */
    public static int DEBUG = 0;

    /**
     * Split training set.
     *
     * @param examples the examples
     * @param n the n
     * @param dp the dp
     * @param sp the sp
     * @param dm the dm
     * @param bias the bias
     * @param redundancy the redundancy
     * @return the list
     * @throws FeatureTermException the feature term exception
     * @throws Exception the exception
     */
    public static List<List<FeatureTerm>> splitTrainingSet(Collection<FeatureTerm> examples, int n, Path dp, Path sp, FTKBase dm, double bias, double redundancy)
            throws FeatureTermException, Exception {
        double matrix[] = null;
        double cbias = 0;
        List<FeatureTerm> differentSolutions = Hypothesis.differentSolutions(examples, sp);
        int ns = differentSolutions.size();
        Random r = new Random();

		// Generate the bias matrix:
        // generate an initial matrix as close as possible to the desired bias:
        for (int i = 0; i < 100; i++) {
            double m[] = new double[n * ns];
            double mbias = 0;
            for (int j = 0; j < n * ns; j++) {
                m[j] = r.nextFloat();
            }
            for (int j = 0; j < n; j++) {
                double t = 0;
                for (int k = 0; k < ns; k++) {
                    t += m[k * n + j];
                }
                for (int k = 0; k < ns; k++) {
                    m[k * n + j] /= t;
                }
            }
            for (int j = 0; j < n; j++) {
                double t = 0;
                for (int k = 0; k < ns; k++) {
                    t = (m[k * n + j] - (1.0 / ns)) * (m[k * n + j] - (1.0 / ns));
                }
                mbias += Math.sqrt(t);
            }
            mbias /= n;

            if (matrix == null || Math.abs(bias - mbias) < Math.abs(bias - cbias)) {
                matrix = m;
                cbias = mbias;
            }
        }

        if (DEBUG >= 1) {
            System.out.println("Desired bias: " + bias);
        }
        if (DEBUG >= 1) {
            System.out.println("Initial bias: " + cbias);
        }

        // Adjust matrix to get closer to desired bias:
        for (int i = 0; i < 1000; i++) {
            boolean stop = true;
            double modifiers[] = {0.5, 0.75, 0.8, 0.9, 1.1, 1.25, 1.5, 2.0};

            for (double modifier : modifiers) {
                double m[] = new double[n * ns];
                for (int j = 0; j < n * ns; j++) {
                    m[j] = matrix[j];
                }
                double mbias = 0;
                for (int j = 0; j < n; j++) {
                    for (int k = 0; k < ns; k++) {
                        m[k * n + j] = ((m[k * n + j] - (1.0 / n)) * modifier) + (1.0 / n);
                        if (m[k * n + j] < 0) {
                            m[k * n + j] = 0;
                        }
                        if (m[k * n + j] > 1) {
                            m[k * n + j] = 1;
                        }
                    }
                    double t = 0;
                    for (int k = 0; k < ns; k++) {
                        t += m[k * n + j];
                    }
                    if (t > 0) {
                        for (int k = 0; k < ns; k++) {
                            m[k * n + j] /= t;
                        }
                    }
                }
                for (int j = 0; j < n; j++) {
                    double t = 0;
                    for (int k = 0; k < ns; k++) {
                        t = (m[k * n + j] - (1.0 / ns)) * (m[k * n + j] - (1.0 / ns));
                    }
                    mbias += Math.sqrt(t);
                }
                mbias /= n;
                if (Math.abs(bias - mbias) < Math.abs(bias - cbias)) {
                    // System.out.println(modifier + " -> " + mbias);
                    for (int j = 0; j < n * ns; j++) {
                        matrix[j] = m[j];
                    }
                    cbias = mbias;
                    stop = false;
                    /*
                     * for(FeatureTerm s:differentSolutions) { int j = differentSolutions.indexOf(s); List<Double> d =
                     * new LinkedList<Double>(); for(int k = 0;k<n;k++) d.add(matrix[k*ns+j]);
                     * System.out.println("D for " + s.toStringNOOS(dm) + " -> " + d); }
                     */
                }
            }
            if (stop) {
                break;
            }
        }

        if (DEBUG >= 1) {
            System.out.println("Adjusted bias: " + cbias);
        }

        // Compute how many cases to distribtue according to redundancy:
        int ncases = (int) ((redundancy * (examples.size()) * (n - 1)) + examples.size());
        if (DEBUG >= 1) {
            System.out.println("Redundancy " + redundancy + " -> " + ncases);
        }

        // Sample:
        List<List<FeatureTerm>> training_sets = new LinkedList<List<FeatureTerm>>();
        List<Hypothesis> hypotheses = new LinkedList<Hypothesis>();
        List<FeatureTerm> casesToDistribute = new LinkedList<FeatureTerm>();
        HashMap<FeatureTerm, List<Double>> distributions = new HashMap<FeatureTerm, List<Double>>();

        for (FeatureTerm s : differentSolutions) {
            int i = differentSolutions.indexOf(s);
            List<Double> d = new LinkedList<Double>();
            for (int k = 0; k < n; k++) {
                d.add(matrix[k * ns + i]);
            }
            distributions.put(s, d);
            if (DEBUG >= 1) {
                System.out.println("Distribution for " + s.toStringNOOS(dm) + " -> " + d);
            }
        }

        for (int i = 0; i < n; i++) {
            training_sets.add(new LinkedList<FeatureTerm>());
        }
        for (int i = 0; i < ncases; i++) {
            if (casesToDistribute.isEmpty()) {
                casesToDistribute.addAll(examples);
            }

            FeatureTerm e = casesToDistribute.get(r.nextInt(casesToDistribute.size()));
            FeatureTerm s = e.readPath(sp);

            boolean found = false;
            // First try to assign it to an agent according to the bias:
            for (int j = 0; j < 10; j++) {
                int a = Sampler.weighted(distributions.get(s));
                if (!training_sets.get(a).contains(e)) {
                    training_sets.get(a).add(e);
                    found = true;
                    break;
                }
            }
            // If not possible give it to a random agent which does not have the case:
            if (!found) {
                while (true) {
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
    public static final int TRAINS_100_DATASET = 63;
    public static final int TRAINS_1000_DATASET = 64;
    public static final int TRAINS_10000_DATASET = 65;
    public static final int TRAINS_100000_DATASET = 66;
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
    public static final int MUSHROOM_DATASET = 29;
    public static final int RIU_STORIES_EVENING_TIDE = 30;
    public static final int PAIRS_50_DATASET = 31;
    public static final int STRAIGHT_50_DATASET = 32;
    public static final int LYMPHOGRAPHY_148_DATASET = 33;
    public static final int PIZZA_DATASET = 34;
    public static final int BREASTCANCER_DATASET = 35;
    public static final int SPECT_DATASET = 36;

    
    /**
     * Load training set.
     *
     * @param DATASET the dATASET
     * @param o the o
     * @param dm the dm
     * @param case_base the case_base
     * @return the training set properties
     * @throws FeatureTermException the feature term exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static TrainingSetProperties loadTrainingSet(int DATASET, Ontology o, FTKBase dm, FTKBase case_base) throws FeatureTermException, IOException {
        String NOOSpath = "NOOS";
        TrainingSetProperties ts = new TrainingSetProperties();
        ts.description_path = new Path();
        ts.solution_path = new Path();
        ts.cases = new LinkedList<FeatureTerm>();

        ts.description_path.features.add(new Symbol("description"));
        ts.solution_path.features.add(new Symbol("solution"));

        switch (DATASET) {
            case ARTIFICIAL_DATASET:
                dm.importNOOS(NOOSpath + "/artificial-ontology.noos", o);
                dm.importNOOS(NOOSpath + "/artificial-dm.noos", o);
                case_base.importNOOS(NOOSpath + "/artificial-512.noos", o);

                ts.name = "artificial";
                ts.problem_sort = o.getSort("artificial-data-problem");
                break;
            case ZOOLOGY_DATASET:
                dm.importNOOS(NOOSpath + "/zoology-ontology.noos", o);
                dm.importNOOS(NOOSpath + "/zoology-dm.noos", o);
                case_base.importNOOS(NOOSpath + "/zoology-cases-101.noos", o);

                ts.name = "zoology";
                ts.problem_sort = o.getSort("zoo-problem");
                break;
            case SOYBEAN_DATASET:
                dm.importNOOS(NOOSpath + "/soybean-ontology.noos", o);
                dm.importNOOS(NOOSpath + "/soybean-dm.noos", o);
                case_base.importNOOS(NOOSpath + "/soybean-cases-307.noos", o);

                ts.name = "soybean";
                ts.problem_sort = o.getSort("soybean-problem");
                break;
            case DEMOSPONGIAE_503_DATASET:
                dm.importNOOS(NOOSpath + "/sponge-ontology.noos", o);
                dm.importNOOS(NOOSpath + "/sponge-dm.noos", o);
                case_base.importNOOS(NOOSpath + "/sponge-cases-503.noos", o);

                ts.solution_path.features.add(new Symbol("order"));

                ts.name = "demospongiae";
                ts.problem_sort = o.getSort("sponge-problem");
                break;
            case DEMOSPONGIAE_280_DATASET:
                dm.importNOOS(NOOSpath + "/sponge-ontology.noos", o);
                dm.importNOOS(NOOSpath + "/sponge-dm.noos", o);
                case_base.importNOOS(NOOSpath + "/sponge-cases-280.noos", o);

                ts.solution_path.features.add(new Symbol("order"));

                ts.name = "demospongiae";
                ts.problem_sort = o.getSort("sponge-problem");
                break;
            case DEMOSPONGIAE_120_DATASET:
                dm.importNOOS(NOOSpath + "/sponge-ontology.noos", o);
                dm.importNOOS(NOOSpath + "/sponge-dm.noos", o);
                case_base.importNOOS(NOOSpath + "/sponge-cases-120.noos", o);

                ts.solution_path.features.add(new Symbol("order"));

                ts.name = "demospongiae";
                ts.problem_sort = o.getSort("sponge-problem");
                break;
            case TRAINS_DATASET:
                dm.importNOOS(NOOSpath + "/trains-ontology.noos", o);
                dm.importNOOS(NOOSpath + "/trains-dm.noos", o);
                case_base.importNOOS(NOOSpath + "/trains-cases-10.noos", o);

                ts.name = "trains";
                ts.problem_sort = o.getSort("trains-problem");
                break;
            case TRAINS_82_DATASET:
                dm.importNOOS(NOOSpath + "/trains-ontology.noos", o);
                dm.importNOOS(NOOSpath + "/trains-dm.noos", o);
                case_base.importNOOS(NOOSpath + "/trains-cases-82.noos", o);

                ts.name = "trains";
                ts.problem_sort = o.getSort("trains-problem");
                break;
            case TRAINS_900_DATASET:
                dm.importNOOS(NOOSpath + "/trains-ontology.noos", o);
                dm.importNOOS(NOOSpath + "/trains-dm.noos", o);
                case_base.importNOOS(NOOSpath + "/trains-cases-900.noos", o);

                ts.name = "trains";
                ts.problem_sort = o.getSort("trains-problem");
                break;
            case TRAINS_100_DATASET:
                dm.importNOOS(NOOSpath + "/trains-ontology.noos", o);
                dm.importNOOS(NOOSpath + "/trains-dm.noos", o);
                case_base.importNOOS(NOOSpath + "/trains-cases-100.noos", o);

                ts.name = "trains";
                ts.problem_sort = o.getSort("trains-problem");
                break;
            case TRAINS_1000_DATASET:
                dm.importNOOS(NOOSpath + "/trains-ontology.noos", o);
                dm.importNOOS(NOOSpath + "/trains-dm.noos", o);
                case_base.importNOOS(NOOSpath + "/trains-cases-1000.noos", o);

                ts.name = "trains";
                ts.problem_sort = o.getSort("trains-problem");
                break;
            case TRAINS_10000_DATASET:
                dm.importNOOS(NOOSpath + "/trains-ontology.noos", o);
                dm.importNOOS(NOOSpath + "/trains-dm.noos", o);
                case_base.importNOOS(NOOSpath + "/trains-cases-10000.noos", o);

                ts.name = "trains";
                ts.problem_sort = o.getSort("trains-problem");
                break;
            case TRAINS_100000_DATASET:
                dm.importNOOS(NOOSpath + "/trains-ontology.noos", o);
                dm.importNOOS(NOOSpath + "/trains-dm.noos", o);
                case_base.importNOOS(NOOSpath + "/trains-cases-100000.noos", o);

                ts.name = "trains";
                ts.problem_sort = o.getSort("trains-problem");
                break;
            case UNCLE_DATASET:
                dm.importNOOS(NOOSpath + "/family-ontology.noos", o);
                dm.importNOOS(NOOSpath + "/family-dm.noos", o);
                case_base.importNOOS(NOOSpath + "/family-cases-12.noos", o);

                ts.name = "uncle";
                ts.problem_sort = o.getSort("uncle-problem");
                break;
            case UNCLE_DATASET_SETS:
                dm.importNOOS(NOOSpath + "/family-ontology.noos", o);
                dm.importNOOS(NOOSpath + "/family-dm.noos", o);
                case_base.importNOOS(NOOSpath + "/family-cases-12-sets.noos", o);

                ts.name = "uncle";
                ts.problem_sort = o.getSort("uncle-problem");
                break;
            case UNCLE_DATASET_BOTH:
                dm.importNOOS(NOOSpath + "/family-ontology.noos", o);
                dm.importNOOS(NOOSpath + "/family-dm.noos", o);
                case_base.importNOOS(NOOSpath + "/family-cases-12.noos", o);
                case_base.importNOOS(NOOSpath + "/family-cases-12-sets.noos", o);

                ts.name = "uncle";
                ts.problem_sort = o.getSort("uncle-problem");
                break;
            case CARS_DATASET:
                dm.importNOOS(NOOSpath + "/car-ontology.noos", o);
                dm.importNOOS(NOOSpath + "/car-dm.noos", o);
                case_base.importNOOS(NOOSpath + "/car-1728.noos", o);

                ts.name = "cars";
                ts.problem_sort = o.getSort("car-problem");
                break;
            case MUSHROOM_DATASET:
                dm.importNOOS("NOOS/mushroom-ontology.noos", o);
                dm.importNOOS("NOOS/mushroom-dm.noos", o);
                case_base.importNOOS("NOOS/mushroom-cases-8124.noos", o);

                ts.name = "mushroom";
                ts.problem_sort = o.getSort("mushroom-problem");
                break;
            case LYMPHOGRAPHY_148_DATASET:
                dm.importNOOS("NOOS/lymphography-ontology.noos", o);
                dm.importNOOS("NOOS/lymphography-dm.noos", o);
                case_base.importNOOS("NOOS/lymphography-cases148.noos", o);

                ts.name = "lymphography";
                ts.problem_sort = o.getSort("lymphography-problem");
                break;
            case PIZZA_DATASET:
                dm.importNOOS("NOOS/pizza-ontology.noos", o);
                case_base.importNOOS("NOOS/pizza-instances.noos", o);

                ts.name = "pizza";
                ts.problem_sort = o.getSort("pizza-problem");

                ts.description_path.features.clear();
                ts.description_path.features.add(new Symbol("problem"));
                break;

            case TOXICOLOGY_DATASET_MRATS:
            case TOXICOLOGY_DATASET_FRATS:
            case TOXICOLOGY_DATASET_MMICE:
            case TOXICOLOGY_DATASET_FMICE:
                dm.importNOOS(NOOSpath + "/toxic-eva-ontology.noos", o);
                dm.importNOOS(NOOSpath + "/toxic-eva-dm.noos", o);
			// case_base.ImportNOOS(NOOSpath + "/toxic-eva-filtered-cases-276.noos", o);
                // case_base.ImportNOOS(NOOSpath + "/toxic-eva-cases-371.noos", o);
                // case_base.ImportNOOS(NOOSpath + "/toxic-eva-fixed-cases-371.noos", o);
                case_base.importNOOS(NOOSpath + "/toxic-santi-cases-353.noos", o);

                switch (DATASET) {
                    case TOXICOLOGY_DATASET_MRATS:
                        ts.solution_path.features.add(new Symbol("m-rats"));
                        break;
                    case TOXICOLOGY_DATASET_FRATS:
                        ts.solution_path.features.add(new Symbol("f-rats"));
                        break;
                    case TOXICOLOGY_DATASET_MMICE:
                        ts.solution_path.features.add(new Symbol("m-mice"));
                        break;
                    case TOXICOLOGY_DATASET_FMICE:
                        ts.solution_path.features.add(new Symbol("f-mice"));
                        break;
                }

                ts.name = "toxicology";
                ts.problem_sort = o.getSort("toxic-problem");

                 {
                    List<FeatureTerm> cs = new LinkedList<FeatureTerm>();
                    List<FeatureTerm> toDelete = new LinkedList<FeatureTerm>();
                    cs.addAll(case_base.searchFT(ts.problem_sort));

                    for (FeatureTerm c : cs) {
                        FeatureTerm s = c.readPath(ts.solution_path);
                        String ss = s.toStringNOOS(dm);
                        if (!ss.equals("positive") && !ss.equals("negative")) {
                            // remove example, inqdequate!
                            case_base.deleteFT(c);
                        }
                    }

                }
                break;

            case TOXICOLOGY_OLD_DATASET_MRATS:
            case TOXICOLOGY_OLD_DATASET_FRATS:
            case TOXICOLOGY_OLD_DATASET_MMICE:
            case TOXICOLOGY_OLD_DATASET_FMICE:
                dm.importNOOS(NOOSpath + "/toxic-eva-old-ontology.noos", o);
                dm.importNOOS(NOOSpath + "/toxic-eva-old-dm.noos", o);
                case_base.importNOOS(NOOSpath + "/toxic-eva-old-cases.noos", o);

                switch (DATASET) {
                    case TOXICOLOGY_OLD_DATASET_MRATS:
                        ts.solution_path.features.add(new Symbol("m-rats"));
                        break;
                    case TOXICOLOGY_OLD_DATASET_FRATS:
                        ts.solution_path.features.add(new Symbol("f-rats"));
                        break;
                    case TOXICOLOGY_OLD_DATASET_MMICE:
                        ts.solution_path.features.add(new Symbol("m-mice"));
                        break;
                    case TOXICOLOGY_OLD_DATASET_FMICE:
                        ts.solution_path.features.add(new Symbol("f-mice"));
                        break;
                }

                ts.name = "toxicology-old";
                ts.problem_sort = o.getSort("toxic-problem");

                 {
                    List<FeatureTerm> cs = new LinkedList<FeatureTerm>();
                    List<FeatureTerm> toDelete = new LinkedList<FeatureTerm>();
                    cs.addAll(case_base.searchFT(ts.problem_sort));

                    for (FeatureTerm c : cs) {
                        FeatureTerm s = c.readPath(ts.solution_path);
                        if (s != null) {
                            String ss = s.toStringNOOS(dm);
                            if (!ss.equals("positive") && !ss.equals("negative")) {
                                // remove example, inqdequate!
                                case_base.deleteFT(c);
                            }
                        }
                    }

                }
                break;

            case KR_VS_KP_DATASET:
                dm.importNOOS(NOOSpath + "/kr-vs-kp-ontology.noos", o);
                dm.importNOOS(NOOSpath + "/kr-vs-kp-dm.noos", o);
                case_base.importNOOS(NOOSpath + "/kr-vs-kp-3196.noos", o);

                ts.name = "kr-vs-kp";
                ts.problem_sort = o.getSort("kr-vs-kp-problem");
                break;
            case FINANCIAL_NO_TRANSACTIONS:
                dm.importNOOS(NOOSpath + "/financial-ontology.noos", o);
                dm.importNOOS(NOOSpath + "/financial-dm.noos", o);
                // case_base.ImportNOOS(NOOSpath + "/financial-cases-682-no-transactions.noos", o);
                case_base.importNOOS(NOOSpath + "/financial-cases-10-no-transactions.noos", o);

                ts.name = "financial-no-t";
                ts.problem_sort = o.getSort("loan-problem");

                ts.description_path.features.clear();
                ts.solution_path.features.clear();
                ts.description_path.features.add(new Symbol("loan"));
                ts.solution_path.features.add(new Symbol("status"));
                break;
            case FINANCIAL:
                dm.importNOOS(NOOSpath + "/financial-ontology.noos", o);
                dm.importNOOS(NOOSpath + "/financial-dm.noos", o);
                case_base.importNOOS(NOOSpath + "/financial-cases-10.noos", o);
                // case_base.ImportNOOS(NOOSpath + "/financial-cases-682.noos", o);

                ts.name = "financial-no-t";
                ts.problem_sort = o.getSort("loan-problem");

                ts.description_path.features.clear();
                ts.solution_path.features.clear();
                ts.description_path.features.add(new Symbol("loan"));
                ts.solution_path.features.add(new Symbol("status"));
                break;
            case MUTAGENESIS:
                dm.importNOOS(NOOSpath + "/mutagenesis-ontology.noos", o);
                dm.importNOOS(NOOSpath + "/mutagenesis-dm.noos", o);
                case_base.importNOOS(NOOSpath + "/mutagenesis-b4-230-cases.noos", o);
                //case_base.importNOOS(NOOSpath + "/mutagenesis-b4-25-cases.noos", o);

                ts.name = "mutagenesis-b4";
                ts.problem_sort = o.getSort("mutagenesis-problem");

                ts.description_path.features.clear();
                ts.solution_path.features.clear();
                ts.description_path.features.add(new Symbol("problem"));
                ts.solution_path.features.add(new Symbol("solution"));
                break;
            case MUTAGENESIS_EASY:
                dm.importNOOS(NOOSpath + "/mutagenesis-ontology.noos", o);
                dm.importNOOS(NOOSpath + "/mutagenesis-dm.noos", o);
                case_base.importNOOS(NOOSpath + "/mutagenesis-b4-188-cases.noos", o);

                ts.name = "mutagenesis-b4";
                ts.problem_sort = o.getSort("mutagenesis-problem");

                ts.description_path.features.clear();
                ts.solution_path.features.clear();
                ts.description_path.features.add(new Symbol("problem"));
                ts.solution_path.features.add(new Symbol("solution"));
                break;
            case MUTAGENESIS_DISCRETIZED:
                dm.importNOOS(NOOSpath + "/mutagenesis-ontology.noos", o);
                dm.importNOOS(NOOSpath + "/mutagenesis-dm.noos", o);
                // case_base.ImportNOOS(NOOSpath + "/mutagenesis-b4-230-cases.noos", o);
                case_base.importNOOS(NOOSpath + "/mutagenesis-b4-noH-230-cases.noos", o);
                // case_base.ImportNOOS(NOOSpath + "/mutagenesis-b4-noH-25-cases.noos", o);

                ts.name = "mutagenesis-b4-discretized";
                ts.problem_sort = o.getSort("mutagenesis-problem");

                ts.description_path.features.clear();
                ts.solution_path.features.clear();
                ts.description_path.features.add(new Symbol("problem"));
                ts.solution_path.features.add(new Symbol("solution"));

                // discretize:
                 {
                    Set<FeatureTerm> cases = case_base.searchFT(ts.problem_sort);
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
                dm.importNOOS(NOOSpath + "/mutagenesis-ontology.noos", o);
                dm.importNOOS(NOOSpath + "/mutagenesis-dm.noos", o);
                case_base.importNOOS(NOOSpath + "/mutagenesis-b4-188-cases.noos", o);

                ts.name = "mutagenesis-b4-discretized";
                ts.problem_sort = o.getSort("mutagenesis-problem");

                ts.description_path.features.clear();
                ts.solution_path.features.clear();
                ts.description_path.features.add(new Symbol("problem"));
                ts.solution_path.features.add(new Symbol("solution"));

                // discretize:
                 {
                    Set<FeatureTerm> cases = case_base.searchFT(ts.problem_sort);
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
                dm.importNOOS(NOOSpath + "/mutagenesis-ontology.noos", o);
                dm.importNOOS(NOOSpath + "/mutagenesis-dm.noos", o);
                case_base.importNOOS(NOOSpath + "/mutagenesis-b4-noH-noL-188-cases.noos", o);

                ts.name = "mutagenesis-b4-nol-discretized";
                ts.problem_sort = o.getSort("mutagenesis-problem");

                ts.description_path.features.clear();
                ts.solution_path.features.clear();
                ts.description_path.features.add(new Symbol("problem"));
                ts.solution_path.features.add(new Symbol("solution"));

                // discretize:
                 {
                    Set<FeatureTerm> cases = case_base.searchFT(ts.problem_sort);
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
                dm.importNOOS(NOOSpath + "/mutagenesis-ontology.noos", o);
                dm.importNOOS(NOOSpath + "/mutagenesis-dm.noos", o);
                case_base.importNOOS(NOOSpath + "/mutagenesis-b4-noH-noL-230-cases.noos", o);

                ts.name = "mutagenesis-b4-nol-discretized";
                ts.problem_sort = o.getSort("mutagenesis-problem");

                ts.description_path.features.clear();
                ts.solution_path.features.clear();
                ts.description_path.features.add(new Symbol("problem"));
                ts.solution_path.features.add(new Symbol("solution"));

                // discretize:
                 {
                    Set<FeatureTerm> cases = case_base.searchFT(ts.problem_sort);
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
                dm.importNOOS(NOOSpath + "/story-ontology.noos", o);
                case_base.importNOOS(NOOSpath + "/story-cases-2.noos", o);

                ts.name = "riu-stories";
                ts.problem_sort = o.getSort("scene");

                ts.description_path.features.clear();
                ts.solution_path.features.clear();
                break;
            case RIU_STORIES_EVENING_TIDE:
                dm.importNOOS("NOOS/story-ontology.noos", o);
                case_base.importNOOS("NOOS/story-cases-EveningTide-20.noos", o);

                ts.name = "riu-stories-eveningtide";
                ts.problem_sort = o.getSort("scene");

                ts.description_path.features.clear();
                ts.solution_path.features.clear();
                break;
            case PAIRS_50_DATASET:
                dm.importNOOS("NOOS/pair50-ontology.noos", o);
                case_base.importNOOS("NOOS/pair50-instances.noos", o);

                ts.name = "pairs_converted_fromDL";
                ts.problem_sort = o.getSort("pairs-problem");
                ts.description_path.features.clear();
                ts.description_path.features.add(new Symbol("problem"));
                break;
            case STRAIGHT_50_DATASET:
                dm.importNOOS("NOOS/straight-ontology.noos", o);
                case_base.importNOOS("NOOS/straight-instances.noos", o);

                ts.name = "straight_converted_fromDL";
                ts.problem_sort = o.getSort("straight-problem");
                ts.description_path.features.clear();
                ts.description_path.features.add(new Symbol("problem"));
                break;
            case BREASTCANCER_DATASET:
                dm.importNOOS(NOOSpath + "/breast-cancer-ontology.noos", o);
                dm.importNOOS(NOOSpath + "/breast-cancer-dm.noos", o);
                case_base.importNOOS(NOOSpath + "/breast-cancer-cases-286.noos", o);

                ts.name = "breast-cancer";
                ts.problem_sort = o.getSort("breast-cancer-problem");
                break;
            case SPECT_DATASET:
                dm.importNOOS(NOOSpath + "/spect-ontology.noos", o);
                dm.importNOOS(NOOSpath + "/spect-dm.noos", o);
                case_base.importNOOS(NOOSpath + "/spect-cases-267.noos", o);

                ts.name = "spect";
                ts.problem_sort = o.getSort("spect-problem");
                break;
            default:
                return null;
        }

        ts.cases.addAll(case_base.searchFT(ts.problem_sort));
        return ts;
    }

    /**
     * Discretize feature.
     *
     * @param cases the cases
     * @param featurePath the feature path
     * @param solutionPath the solution path
     * @param ncuts the ncuts
     * @throws FeatureTermException the feature term exception
     */
    public static void discretizeFeature(Collection<FeatureTerm> cases, Path featurePath, Path solutionPath, int ncuts) throws FeatureTermException {
        List<Float> cuts = findDiscretizationIntervals(cases, featurePath, solutionPath, ncuts);

        // change the values by the discretized ones:
        for (FeatureTerm c : cases) {
            FeatureTerm v = c.readPath(featurePath);
            float fv = 0;
            boolean integer = true;
            if (v != null) {
                if (v instanceof IntegerFeatureTerm) {
                    fv = ((IntegerFeatureTerm) v).getValue().floatValue();
                } else {
                    fv = ((FloatFeatureTerm) v).getValue();
                    integer = false;
                }

                int newV = 0;
                for (Float cut : cuts) {
                    if (cut < fv) {
                        newV++;
                    } else {
                        break;
                    }
                }
                if (integer) {
                    ((IntegerFeatureTerm) v).setValue(newV);
                } else {
                    ((FloatFeatureTerm) v).setValue((float) newV);
                }
            } else {
                System.out.println(c.getName() + " has no value in " + featurePath);
            }
        }
    }

    // this method will split the feature range in 2^uts intervals, and return the cut points:
    /**
     * Find discretization intervals.
     *
     * @param cases the cases
     * @param featurePath the feature path
     * @param solutionPath the solution path
     * @param cuts the cuts
     * @return the list
     * @throws FeatureTermException the feature term exception
     */
    public static List<Float> findDiscretizationIntervals(Collection<FeatureTerm> cases, Path featurePath, Path solutionPath, int cuts)
            throws FeatureTermException {
        List<Pair<Float, Integer>> values = new LinkedList<Pair<Float, Integer>>();
        Vector<FeatureTerm> solutions = new Vector<FeatureTerm>();

        for (FeatureTerm c : cases) {
            FeatureTerm s = c.readPath(solutionPath);
            if (!solutions.contains(s)) {
                solutions.add(s);
            }
        }

        // get all the values:
        for (FeatureTerm c : cases) {
            FeatureTerm v = c.readPath(featurePath);
            FeatureTerm s = c.readPath(solutionPath);
            Float fv = null;

            if (v != null) {
                if (v instanceof IntegerFeatureTerm) {
                    fv = (((IntegerFeatureTerm) v).getValue()).floatValue();
                } else if (v instanceof FloatFeatureTerm) {
                    fv = ((FloatFeatureTerm) v).getValue();
                } else {
                    throw new FeatureTermException("The feature has a non numeric value!");
                }
                values.add(new Pair<Float, Integer>(fv, solutions.indexOf(s)));
            }
        }

        // sort them:
        {
            boolean change = false;
            int len = values.size();
            do {
                change = false;
                for (int i = 0; i < len - 1; i++) {
                    if (values.get(i).m_a > values.get(i + 1).m_a) {
                        Pair<Float, Integer> tmp = values.get(i);
                        values.set(i, values.get(i + 1));
                        values.set(i + 1, tmp);
                        change = true;
                    }
                }
            } while (change);
        }

		// for(Pair<Float,Integer> v:values) {
        // System.out.println(v.m_b + " - " + v.m_a);
        // }
        return discretizeFeatureInternal(values, solutions.size(), cuts);
    }

    /**
     * Discretize feature internal.
     *
     * @param values the values
     * @param nSolutions the n solutions
     * @param cuts the cuts
     * @return the list
     */
    static List<Float> discretizeFeatureInternal(List<Pair<Float, Integer>> values, int nSolutions, int cuts) {
        if (cuts == 0) {
            return new LinkedList<Float>();
        } else {
            boolean first = true;
            float bestCut = 0, bestE = 0;

			// System.out.println("discretizing " + values.size() + " values");
            int gDistribution[] = new int[nSolutions];
            float gE = 0;
            for (int i = 0; i < values.size() - 1; i++) {
                float cut = (values.get(i).m_a + values.get(i + 1).m_a) / 2;

                int d1[] = new int[nSolutions];
                int d2[] = new int[nSolutions];
                int n1 = 0;
                int n2 = 0;

                for (int j = 0; j < values.size(); j++) {
                    Pair<Float, Integer> v = values.get(j);
                    if (first) {
                        gDistribution[v.m_b]++;
                    }
                    if (v.m_a < cut) {
                        d1[v.m_b]++;
                        n1++;
                    } else {
                        d2[v.m_b]++;
                        n2++;
                    }
                }

                // compute entropy:
                if (first) {
                    gE = entropy(gDistribution);
                }
                float e1 = entropy(d1);
                float e2 = entropy(d2);
                float e = (e1 * n1 + e2 * n2) / (float) (n1 + n2);

                if (first || e < bestE) {
                    first = false;
                    bestE = e;
                    bestCut = cut;

                    // System.out.println("next best: " + cut + " (" + e + ")");
                }
            }

            if (bestE < gE) {
                List<Float> l = new LinkedList<Float>();

                List<Pair<Float, Integer>> vl1 = new LinkedList<Pair<Float, Integer>>();
                List<Pair<Float, Integer>> vl2 = new LinkedList<Pair<Float, Integer>>();

                for (Pair<Float, Integer> v : values) {
                    if (v.m_a < bestCut) {
                        vl1.add(v);
                    } else {
                        vl2.add(v);
                    }
                }

                l.addAll(discretizeFeatureInternal(vl1, nSolutions, cuts - 1));
                l.add(new Float(bestCut));
                l.addAll(discretizeFeatureInternal(vl2, nSolutions, cuts - 1));
                return l;
            } else {
                return new LinkedList<Float>();
            }
        }
    }

    /**
     * Entropy.
     *
     * @param hist the hist
     * @return the float
     */
    static float entropy(int hist[]) {
        int n = hist.length;
        int t = 0;

        for (int i = 0; i < n; i++) {
            t += hist[i];
        }

        float h = 0;

		// System.out.print("[ " + hist[0] + "," + hist[1] + "] -> ");
        for (int i = 0; i < n; i++) {
            if (hist[i] != 0) {
                float f = (float) hist[i] / (float) (t);
                h -= Math.log(f) * f;
            }
        }
        // System.out.println("" + h);
        return h;
    }
}
