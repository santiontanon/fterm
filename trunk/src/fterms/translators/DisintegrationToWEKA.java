/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fterms.translators;

import fterms.BaseOntology;
import fterms.Disintegration;
import fterms.FTKBase;
import fterms.FeatureTerm;
import fterms.Ontology;
import fterms.exceptions.FeatureTermException;
import fterms.learning.TrainingSetProperties;
import fterms.learning.TrainingSetUtils;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author santi
 */
public class DisintegrationToWEKA {
    public static void main(String args[]) {
        try {
            long t_start, t_end, t_last, t_current;
            Ontology base_ontology = new BaseOntology();
            Ontology o=new Ontology();
            FTKBase dm=new FTKBase();
            FTKBase case_base=new FTKBase();
            o.uses(base_ontology);
            case_base.uses(dm);

            dm.create_boolean_objects(o);

            List<FeatureTerm> cases = null;

			t_start=System.currentTimeMillis();
//            TrainingSetProperties ts = TrainingSetUtils.loadTrainingSet(TrainingSetUtils.ZOOLOGY_DATASET, o, dm, case_base);
//            TrainingSetProperties ts = TrainingSetUtils.loadTrainingSet(TrainingSetUtils.SOYBEAN_DATASET, o, dm, case_base);
            TrainingSetProperties ts = TrainingSetUtils.loadTrainingSet(TrainingSetUtils.TRAINS_DATASET, o, dm, case_base);
//            TrainingSetProperties ts = TrainingSetUtils.loadTrainingSet(TrainingSetUtils.UNCLE_DATASET_BOTH, o, dm, case_base);
//            TrainingSetProperties ts = TrainingSetUtils.loadTrainingSet(TrainingSetUtils.DEMOSPONGIAE_503_DATASET, o, dm, case_base);
//            TrainingSetProperties ts = TrainingSetUtils.loadTrainingSet(TrainingSetUtils.FINANCIAL_NO_TRANSACTIONS, o, dm, case_base);
//            TrainingSetProperties ts = TrainingSetUtils.loadTrainingSet(TrainingSetUtils.FINANCIAL, o, dm, case_base);

//            TrainingSetProperties ts = TrainingSetUtils.loadTrainingSet(TrainingSetUtils.TOXICOLOGY_DATASET_MRATS, o, dm, case_base);
//            TrainingSetProperties ts = TrainingSetUtils.loadTrainingSet(TrainingSetUtils.TOXICOLOGY_DATASET_FRATS, o, dm, case_base);
//            TrainingSetProperties ts = TrainingSetUtils.loadTrainingSet(TrainingSetUtils.TOXICOLOGY_DATASET_MMICE, o, dm, case_base);
//            TrainingSetProperties ts = TrainingSetUtils.loadTrainingSet(TrainingSetUtils.TOXICOLOGY_DATASET_FMICE, o, dm, case_base);

//            TrainingSetProperties ts = TrainingSetUtils.loadTrainingSet(TrainingSetUtils.TOXICOLOGY_OLD_DATASET_MRATS, o, dm, case_base);

//            TrainingSetProperties ts = TrainingSetUtils.loadTrainingSet(TrainingSetUtils.MUTAGENESIS_EASY, o, dm, case_base);
//            TrainingSetProperties ts = TrainingSetUtils.loadTrainingSet(TrainingSetUtils.MUTAGENESIS, o, dm, case_base);
//            TrainingSetProperties ts = TrainingSetUtils.loadTrainingSet(TrainingSetUtils.MUTAGENESIS_EASY_DISCRETIZED, o, dm, case_base);
//            TrainingSetProperties ts = TrainingSetUtils.loadTrainingSet(TrainingSetUtils.MUTAGENESIS_DISCRETIZED, o, dm, case_base);
//            TrainingSetProperties ts = TrainingSetUtils.loadTrainingSet(TrainingSetUtils.MUTAGENESIS_EASY_NOL_DISCRETIZED, o, dm, case_base);

            case_base.print_undefined_terms();


			t_end=System.currentTimeMillis();
			System.out.println("Time taken to load: " + (t_end-t_start));
			t_start=System.currentTimeMillis();

            cases=ts.cases;

            ts.printStatistics(dm);

            List<FeatureTerm> dictionary = new LinkedList<FeatureTerm>();

            for(FeatureTerm c:cases) {
                FeatureTerm description = c.readPath(ts.description_path);
                List<FeatureTerm> properties = Disintegration.disintegrate(description, dm, o, true, false);

                // add them without replication to the dictionary:
                for(FeatureTerm property:properties) {
                    boolean present = false;
                    for(FeatureTerm p2:dictionary) {
                        if (property.equivalents(p2)) {
                            present = true;
                            break;
                        }
                    }
                    if (!present) dictionary.add(property);
                }
            }

            System.out.println("Resulting dictionary has " + dictionary.size() + " properties.");

            System.out.println("@relation 'dataset'");
            int i = 0;
            for(FeatureTerm p:dictionary) {
                System.out.println("@attribute p" + i + " real");
                i++;
            }
            System.out.print("@attribute 'class' {");
            boolean first = true;
            for(FeatureTerm s:ts.differentSolutions()) {
                if (first) {
                    first = false;
                } else {
                    System.out.print(",");
                }
                System.out.print(s.toStringNOOS(dm));
            }
            System.out.println("}");
            System.out.println("@data");
            
            for(FeatureTerm c:cases) {
                FeatureTerm description = c.readPath(ts.description_path);
                FeatureTerm solution = c.readPath(ts.solution_path);
                for(FeatureTerm property:dictionary) {
                    if (property.subsumes(description)) {
                        System.out.print("1,");
                    } else {
                        System.out.print("0,");
                    }
                }
                System.out.println(solution.toStringNOOS(dm));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
