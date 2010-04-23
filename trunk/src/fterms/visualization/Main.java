/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fterms.visualization;

import fterms.BaseOntology;
import fterms.FTKBase;
import fterms.FeatureTerm;
import fterms.Ontology;
import fterms.Path;
import fterms.Symbol;
import fterms.exceptions.FeatureTermException;
import fterms.learning.distance.Distance;
import fterms.learning.distance.RIBL;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.JFrame;

/**
 *
 * @author santi
 */
public class Main {
    static int DATA_SET = 0;	/* 0: sponge, 1: toxic, 2: soybean, 3: zoology, 4: trains, 5: family, 6: cars, 7: chess, 8: melanoma */
    public static int mode = 0; // 0: complete case base, 1: single case

    public static void main(String[] argv) throws FeatureTermException, IOException {

        Ontology base_ontology = new BaseOntology();
        Ontology o=new Ontology();
        FTKBase dm=new FTKBase();
        FTKBase case_base=new FTKBase();
        o.uses(base_ontology);
        case_base.uses(dm);
        Set<FeatureTerm> cases = null;
        Path dp = new Path(), sp = new Path();

        dm.create_boolean_objects(o);

        dp.features.add(new Symbol("description"));
        sp.features.add(new Symbol("solution"));

        switch(DATA_SET) {
        case 0:
                dm.ImportNOOS("NOOS/sponge-ontology.noos",o);
                dm.ImportNOOS("NOOS/sponge-dm.noos",o);
                //			case_base.ImportNOOS("NOOS/sponge-cases-503.noos",o);
                //                case_base.ImportNOOS("NOOS/sponge-cases-280.noos",o);
                case_base.ImportNOOS("NOOS/sponge-cases-120.noos",o);
                //			case_base.ImportNOOS("NOOS/sponge-cases-test.noos",o);

                sp.features.add(new Symbol("order"));

                cases=case_base.SearchFT(o.getSort("sponge-problem"));
                break;
        case 1:
                dm.ImportNOOS("NOOS/toxic-eva-ontology.noos",o);
                dm.ImportNOOS("NOOS/toxic-eva-dm.noos",o);
                case_base.ImportNOOS("NOOS/toxic-eva-cases-10.noos",o);
                //			case_base.ImportNOOS("NOOS/toxic-eva-cases-50.noos",o);
                //			case_base.ImportNOOS("NOOS/toxic-eva-cases-100.noos",o);
                //			case_base.ImportNOOS("NOOS/toxic-eva-cases-371.noos",o);
                //case_base.ImportNOOS("NOOS/toxic-eva-filtered-cases-276.noos",o);

                sp.features.add(new Symbol("m-rats"));
                //				sp.features.add(new Symbol("f-rats"));
                //				sp.features.add(new Symbol("m-mice"));
                //				sp.features.add(new Symbol("f-mice"));

                cases=case_base.SearchFT(o.getSort("toxic-problem"));
                break;
        case 2:
                dm.ImportNOOS("NOOS/soybean-ontology.noos",o);
                dm.ImportNOOS("NOOS/soybean-dm.noos",o);
                case_base.ImportNOOS("NOOS/soybean-cases-307.noos",o);

                cases=case_base.SearchFT(o.getSort("soybean-problem"));
                break;
        case 3:
                dm.ImportNOOS("NOOS/zoology-ontology.noos",o);
                dm.ImportNOOS("NOOS/zoology-dm.noos",o);
                case_base.ImportNOOS("NOOS/zoology-cases-101.noos",o);

                cases=case_base.SearchFT(o.getSort("zoo-problem"));
                break;
        case 4:
                dm.ImportNOOS("NOOS/trains-ontology.noos",o);
                dm.ImportNOOS("NOOS/trains-dm.noos",o);
                case_base.ImportNOOS("NOOS/trains-cases-10.noos",o);
                //			dm.ImportNOOS("NOOS/trains2-ontology.noos",o);
                //			dm.ImportNOOS("NOOS/trains2-dm.noos",o);
                //			case_base.ImportNOOS("NOOS/trains2-cases-20.noos",o);

                cases=case_base.SearchFT(o.getSort("trains-problem"));
                break;
        case 5:
                dm.ImportNOOS("NOOS/family-ontology.noos",o);
                dm.ImportNOOS("NOOS/family-dm.noos",o);
                case_base.ImportNOOS("NOOS/family-cases-12.noos",o);
                case_base.ImportNOOS("NOOS/family-cases-12-sets.noos",o);

                cases=case_base.SearchFT(o.getSort("uncle-problem"));
                break;
        case 6:
                dm.ImportNOOS("NOOS/car-ontology.noos",o);
                dm.ImportNOOS("NOOS/car-dm.noos",o);
                case_base.ImportNOOS("NOOS/car-1728.noos",o);

                cases=case_base.SearchFT(o.getSort("car-problem"));
                break;
        case 7:
                dm.ImportNOOS("NOOS/kr-vs-kp-ontology.noos",o);
                dm.ImportNOOS("NOOS/kr-vs-kp-dm.noos",o);
                case_base.ImportNOOS("NOOS/kr-vs-kp-3196.noos",o);

                cases=case_base.SearchFT(o.getSort("kr-vs-kp-problem"));
                break;
        case 8:
                dm.ImportNOOS("NOOS/nomes-dermatologics-ontology.noos",o);
                dm.ImportNOOS("NOOS/nomes-dermatologics-dm.noos",o);
                case_base.ImportNOOS("NOOS/nomes-dermatologics.noos",o);

                cases=case_base.SearchFT(o.getSort("melanoma-dermatologics-problem"));
                break;
        }

        if (mode==0) {
            new ScrollableFrame("test", 800,600, new FeatureTermTreePanel(cases.iterator().next(), dm));
        } else {
            List<FeatureTerm> l = new LinkedList<FeatureTerm>();
            l.addAll(cases);
            FeatureTerm f = l.get(0);
            System.out.println("NOOS term:");
            System.out.println(f.toStringNOOS(dm));
//            System.out.println("LaTeX term:");
//            System.out.println(NOOSToLaTeX.toLaTeXTerm(f,dm, true));
//            JFrame frame = FTVisualizer.newWindow("FTVisualizer demo",640,480,f,dm,true);
            JFrame frame = CBVisualizer.newWindow("Properties Visualizer demo",640,480,l,dm,sp,dp,true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        }
    }

}
