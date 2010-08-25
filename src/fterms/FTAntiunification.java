package fterms;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import fterms.exceptions.FeatureTermException;

import util.Pair;


/*
 *  TODO: in the COMPLETE version, we should filter the "generalizations_to_preocess" for "equivalents"
 */
public class FTAntiunification {

    public static final int VERSION_FAST = 0;
    public static final int VERSION_STANDARD = 1;
    public static final int VERSION_COMPLETE = 2;

    public static FeatureTerm simpleAntiunification(FeatureTerm f1, FeatureTerm f2, Ontology ontology, FTKBase domain_model) throws FeatureTermException {
        List<FeatureTerm> objects = new LinkedList<FeatureTerm>(), results;

        objects.add(f1);
        objects.add(f2);

        results = antiunification(objects, 0, null, ontology, domain_model, true, VERSION_FAST);

        if (results != null && results.size() > 0) {
            return results.remove(0);
        }
        return null;
    } // simple_antiunification

    public static FeatureTerm simpleAntiunification(List<FeatureTerm> objects, Ontology ontology, FTKBase domain_model) throws FeatureTermException {
        List<FeatureTerm> results;

        results = antiunification(objects, 0, null, ontology, domain_model, true, VERSION_FAST);

        if (results != null && results.size() > 0) {
            return results.remove(0);
        }
        return null;
    } // simple_antiunification

    public static List<FeatureTerm> antiunification(List<FeatureTerm> objects, int language, List<FeatureTerm> initial_terms, Ontology o, FTKBase dm, boolean separation_test, int version) throws FeatureTermException {
        List<FeatureTerm> todelete_g = new LinkedList<FeatureTerm>(), todelete_gtp = new LinkedList<FeatureTerm>(), generalizations_to_process = new LinkedList<FeatureTerm>();
        List<FeatureTerm> next_nodes = new LinkedList<FeatureTerm>(), tmp = null;
        FeatureTerm current, node;
        boolean subsumes_all;
        int iterations = 0, nodes_checked = 0;
        List<FeatureTerm> generalizations = new LinkedList<FeatureTerm>();

//		System.out.println("Starting AU");

        if (objects.isEmpty()) {
            return null;
        }

        if (language == 0) {
            language = FTRefinement.ALL_REFINEMENTS;
        }

        if (initial_terms == null || initial_terms.isEmpty()) {
            // Create initial term:
            Sort s = null;

            for (FeatureTerm object : objects) {
                if (s == null) {
                    if (!(object instanceof SetFeatureTerm)) {
                        s = object.getSort();
                    }
                } else {
                    s = s.Antiunification(object.getSort());
                } // if
            } // while
            if (s == null) {
                current = new SetFeatureTerm();
            } else {
                current = s.createFeatureTerm();
            } // if
            generalizations_to_process.add(current);
        } else {
            boolean more_specific;

            for (FeatureTerm f : initial_terms) {
                more_specific = true;

                for (FeatureTerm generalization : generalizations_to_process) {
                    if (generalization.subsumes(f)) {
                        if (f.subsumes(generalization)) {
                            more_specific = false;
                            break;
                        } else {
                            todelete_g.add(generalization);
                        } // if
                    } else {
                        if (more_specific && f.subsumes(generalization)) {
                            more_specific = false;
                        }
                    } // if
                } // while

                if (more_specific) {
                    if (version != VERSION_COMPLETE) {
                        while (!todelete_g.isEmpty()) {
                            FeatureTerm generalization = todelete_g.remove(0);
                            generalizations_to_process.remove(generalization);
                        } // while
                    } // if

                    generalizations_to_process.add(f);
                } // if
            } // while
        } // if


        // Check for separability:
        if (separation_test) { // separation_test
            Sort s = null;
            boolean common;

            for (FeatureTerm object : objects) {
                if (s == null) {
                    if (!(object instanceof SetFeatureTerm)) {
                        s = object.getSort();
                    }
                } else {
                    s = s.Antiunification(object.getSort());
                } // if
            } // while

            if (s != null && s.getDataType() == Sort.DATATYPE_FEATURETERM) {
                FeatureTerm ft_tmp;
                for (Symbol fn : s.getFeatures()) {
                    common = true;
                    for (FeatureTerm object : objects) {
                        ft_tmp = object.featureValue(fn);
                        if (ft_tmp == null) {
                            common = false;
                            break;
                        } // if
                    } // while

                    if (common) {
                        if (separableFeature(fn, objects, dm)) {
                            List<FeatureTerm> new_objects = new LinkedList<FeatureTerm>();
                            List<FeatureTerm> new_initial_terms = new LinkedList<FeatureTerm>();
                            List<FeatureTerm> result;
                            List<FeatureTerm> buffer = new LinkedList<FeatureTerm>();
                            FeatureTerm ft, ft2;

                            // Perform the antiunification of just the separable feature:
                            //  - construct the list of new objects
                            for (FeatureTerm object : objects) {
                                new_objects.add(object.featureValue(fn));
                            } // while

                            //	- construct the list of new initial terms
                            while (!generalizations_to_process.isEmpty()) {
                                buffer.add(generalizations_to_process.remove(0));
                            } // while
                            while (!buffer.isEmpty()) {
                                new_initial_terms.clear();
                                FeatureTerm object = buffer.remove(0);
                                ft = object.featureValue(fn);
                                if (ft != null) {
                                    new_initial_terms.add(ft);
                                }

                                // - if the list of initial terms is empty, construct an initial term of the sort required for the
                                //   feature at hand:
                                if (new_initial_terms.isEmpty()) {
                                    ft = s.featureSort(fn).createFeatureTerm();
                                    new_initial_terms.add(ft);
                                } // if

                                // - antiunification
                                result = antiunification(new_objects, language, new_initial_terms, o, dm, separation_test, version);

                                for (FeatureTerm ft3 : result) {
                                    ft2 = object.clone(dm, o);
                                    if (ft2.getSort() != s && ft2.getSort().isSubsort(s)) {
                                        ft2.setSort(s);
                                    }
                                    ((TermFeatureTerm) ft2).defineFeatureValue(fn, ft3);
                                    generalizations_to_process.add(ft2);
                                } // while
                            } // while
                        } // if
                    } // if

                } // while
            } // if
        } // if check_separability



        nodes_checked = generalizations_to_process.size();

        // Start search:
        while (!generalizations_to_process.isEmpty()) {

//			System.out.println("AU: " + iterations + " - Generalizations: " + generalizations.size() + ", " + generalizations_to_process.size());
	/*
            {
            char *s;
            FeatureTerm *f;

            printf("Generalizations: \n");
            generalizations.Rewind();
            while(generalizations.Iterate(f)) {
            s=f.toStringNOOS(domain_model);
            printf("%s\n",s);
            delete []s;
            } // while
            printf("Generalizations to process: \n");
            generalizations_to_process.Rewind();
            while(generalizations_to_process.Iterate(f)) {
            s=f.toStringNOOS(domain_model);
            printf("%s\n",s);
            delete []s;
            } // while
            }
             */


            {
                current = generalizations_to_process.remove(0);

                if (version == VERSION_FAST) {
                    tmp = FTRefinement.getSomeSpecializationSubsumingAll(current, dm, o, language, objects);
                } else {
                    tmp = FTRefinement.getSpecializationsSubsumingAll(current, dm, o, language, objects);
                }

//				System.out.println("AU: " + tmp.size() + " refinements...");

                next_nodes.addAll(0, tmp);

                if (version == VERSION_FAST) {
                    generalizations.clear();
                }

                /* In the COMPLETE version, since the "generalizations_to_process" list is not filtered */
                /* we have to test whether "current" is a new antiunification or not					*/
                if (version == VERSION_COMPLETE) {
                    List<FeatureTerm> todelete = new LinkedList<FeatureTerm>();
                    boolean advances = true;

                    for (FeatureTerm f : generalizations) {
                        if (f.subsumes(current)) {
                            todelete.add(f);
                        }
                        if (current.subsumes(f)) {
                            advances = false;
                            break;
                        }
                    } // while

                    if (advances) {
                        while (!todelete.isEmpty()) {
                            FeatureTerm f = todelete.remove(0);
                            generalizations.remove(f);
                        } // while

                        generalizations.add(current);
                    } // if
                } else {
                    generalizations.add(current);
                } // if
            }
            /*
            {
            char *s;
            s=current.toStringNOOS();
            printf("Current:\n%s\n",s);
            delete []s;
            printf(", Next: %i (total: %i) \n",next_nodes.Length(),nodes_checked);
            fflush(0);
            }
             */
            while (!next_nodes.isEmpty()) {
                node = next_nodes.remove(0);
                /*
                {
                char *s;
                s=node.toStringNOOS();
                printf("considering:\n%s\n",s);
                delete []s;
                }
                 */
                nodes_checked++;

                subsumes_all = true;

                /*
                for(FeatureTerm object:objects) {
                if (!node.subsumes(object)) {
                subsumes_all=false;
                break;
                } // if
                } // while
                 */
                if (subsumes_all) {
                    boolean more_specific = true;

                    // manage the generalizations:
                    for (FeatureTerm generalization : generalizations) {
                        if (generalization.subsumes(node)) {
                            if (node.subsumes(generalization)) {
                                more_specific = false;
                                break;
                            } else {
                                todelete_g.add(generalization);
                            } // if
                        } else {
                            if (more_specific && node.subsumes(generalization)) {
                                more_specific = false;
                                break;
                            } // if
                        } // if
                    } // while

                    if (more_specific) {
                        for (FeatureTerm generalization : generalizations_to_process) {
                            if (generalization.subsumes(node)) {
                                if (node.subsumes(generalization)) {
                                    more_specific = false;
                                    break;
                                } else {
                                    todelete_gtp.add(generalization);
                                } // if
                            } else {
                                if (more_specific && node.subsumes(generalization)) {
                                    more_specific = false;
                                    break;
                                } // if
                            } // if
                        } // while
                    }

                    if (more_specific) {
                        while (!todelete_g.isEmpty()) {
                            FeatureTerm generalization = todelete_g.remove(0);
                            generalizations.remove(generalization);
                        } // while
                        if (version != VERSION_COMPLETE) {
                            while (!todelete_gtp.isEmpty()) {
                                FeatureTerm generalization = todelete_gtp.remove(0);
                                generalizations_to_process.remove(generalization);
                            } // while
                        } // if

                        if (version == VERSION_FAST) {
                            generalizations_to_process.clear();
                            next_nodes.clear();
                        } // if

                        generalizations_to_process.add(0, node);
                    } // if

                } else {
//						printf("Does not subsume all examples...\n");
                } // if
            } // while

            // In the FAST mode, only one possible antiunification is to be found, but no search is required:
            if (version == VERSION_FAST) {
                if (generalizations.size() > 0) {
                    node = generalizations.remove(0);
                    generalizations.clear();
                    generalizations.add(node);
                } // if
                if (generalizations_to_process.size() > 0) {
                    node = generalizations_to_process.remove(0);
                    generalizations_to_process.clear();
                    generalizations_to_process.add(node);
                } // if
            } // if

            iterations++;
        } // while
	/*
        {
        FeatureTerm *f;
        char *s;

        printf("----------------------------------------\n");
        printf("Base objects:\n");
        objects.Rewind();
        while(objects.Iterate(f)) {
        s=f.toStringNOOS();
        printf("%s\n",s);
        delete s;
        } // while
        printf("Initial terms:\n");
        if (initial_terms!=0) {
        initial_terms.Rewind();
        while(initial_terms.Iterate(f)) {
        s=f.toStringNOOS();
        printf("%s\n",s);
        delete s;
        } // while
        } // if
        printf("Result:\n");
        generalizations.Rewind();
        while(generalizations.Iterate(f)) {
        s=f.toStringNOOS();
        printf("%s\n",s);
        delete s;
        } // while
        }
         */
//		System.out.println("Finishing AU: " + generalizations.size());

        return generalizations;

    } /* antiunification */


    public static List<Pair<FeatureTerm, Integer>> antiunificationCountingSteps(List<FeatureTerm> objects, int language, List<Pair<FeatureTerm, Integer>> initial_terms, Ontology o, FTKBase dm, boolean separation_test, int version) throws FeatureTermException {
        List<Pair<FeatureTerm, Integer>> todelete_g = new LinkedList<Pair<FeatureTerm, Integer>>(), todelete_gtp = new LinkedList<Pair<FeatureTerm, Integer>>();
        List<Pair<FeatureTerm, Integer>> generalizations_to_process = new LinkedList<Pair<FeatureTerm, Integer>>();
        List<FeatureTerm> next_nodes = new LinkedList<FeatureTerm>(), tmp = null;
        FeatureTerm node;
        Pair<FeatureTerm, Integer> current;
        boolean subsumes_all;
        int iterations = 0, nodes_checked = 0;
        List<Pair<FeatureTerm, Integer>> generalizations = new LinkedList<Pair<FeatureTerm, Integer>>();

//		System.out.println("Starting AU");

        if (objects.isEmpty()) {
            return null;
        }

        if (language == 0) {
            language = FTRefinement.ALL_REFINEMENTS;
        }

        if (initial_terms == null || initial_terms.isEmpty()) {
            // Create initial term:
            Sort s = null;

            for (FeatureTerm object : objects) {
                if (s == null) {
                    if (!(object instanceof SetFeatureTerm)) {
                        s = object.getSort();
                    }
                } else {
                    s = s.Antiunification(object.getSort());
                } // if
            } // while
            if (s == null) {
                current = new Pair<FeatureTerm, Integer>(new SetFeatureTerm(), 0);
            } else {
                current = new Pair<FeatureTerm, Integer>(s.createFeatureTerm(), 0);
                while (s.getSuper() != null) {
                    s = s.getSuper();
                    current.m_b++;
                } /* while */
            } // if
            generalizations_to_process.add(current);
        } else {
            boolean more_specific;

            for (Pair<FeatureTerm, Integer> f_steps : initial_terms) {
                more_specific = true;

                for (Pair<FeatureTerm, Integer> generalization_steps : generalizations_to_process) {
                    if (generalization_steps.m_a.subsumes(f_steps.m_a)) {
                        if (f_steps.m_a.subsumes(generalization_steps.m_a)) {
                            more_specific = false;
                            break;
                        } else {
                            todelete_g.add(generalization_steps);
                        } // if
                    } else {
                        if (more_specific && f_steps.m_a.subsumes(generalization_steps.m_a)) {
                            more_specific = false;
                        }
                    } // if
                } // while

                if (more_specific) {
                    if (version != VERSION_COMPLETE) {
                        while (!todelete_g.isEmpty()) {
                            Pair<FeatureTerm, Integer> generalization_steps = todelete_g.remove(0);
                            generalizations_to_process.remove(generalization_steps);
                        } // while
                    } // if

                    generalizations_to_process.add(f_steps);
                } // if
            } // while
        } // if


        // Check for separability:
        if (separation_test) { // separation_test
            Sort s = null;
            boolean common;

            for (FeatureTerm object : objects) {
                if (s == null) {
                    if (!(object instanceof SetFeatureTerm)) {
                        s = object.getSort();
                    }
                } else {
                    s = s.Antiunification(object.getSort());
                } // if
            } // while

            if (s != null && s.getDataType() == Sort.DATATYPE_FEATURETERM) {
                FeatureTerm ft_tmp;
                for (Symbol fn : s.getFeatures()) {
                    common = true;
                    for (FeatureTerm object : objects) {
                        ft_tmp = object.featureValue(fn);
                        if (ft_tmp == null) {
                            common = false;
                            break;
                        } // if
                    } // while

                    if (common) {
                        if (separableFeature(fn, objects, dm)) {
                            List<FeatureTerm> new_objects = new LinkedList<FeatureTerm>();
                            List<Pair<FeatureTerm, Integer>> new_initial_terms = new LinkedList<Pair<FeatureTerm, Integer>>();
                            List<Pair<FeatureTerm, Integer>> result;
                            List<Pair<FeatureTerm, Integer>> buffer = new LinkedList<Pair<FeatureTerm, Integer>>();
                            FeatureTerm ft, ft2;

                            // Perform the antiunification of just the separable feature:
                            //  - construct the list of new objects
                            for (FeatureTerm object : objects) {
                                new_objects.add(object.featureValue(fn));
                            } // while

                            //	- construct the list of new initial terms
                            while (!generalizations_to_process.isEmpty()) {
                                buffer.add(generalizations_to_process.remove(0));
                            } // while
                            while (!buffer.isEmpty()) {
                                new_initial_terms.clear();
                                Pair<FeatureTerm, Integer> object_steps = buffer.remove(0);
                                ft = object_steps.m_a.featureValue(fn);
                                if (ft != null) {
                                    new_initial_terms.add(new Pair<FeatureTerm, Integer>(ft, 0));
                                }

                                // - if the list of initial terms is empty, construct an initial term of the sort required for the
                                //   feature at hand:
                                if (new_initial_terms.isEmpty()) {
                                    ft = s.featureSort(fn).createFeatureTerm();
                                    new_initial_terms.add(new Pair<FeatureTerm, Integer>(ft, 0));
                                } // if

                                // - antiunification
                                result = antiunificationCountingSteps(new_objects, language, new_initial_terms, o, dm, separation_test, version);

                                for (Pair<FeatureTerm, Integer> ft3_steps : result) {
                                    ft2 = object_steps.m_a.clone(dm, o);
                                    if (ft2.getSort() != s && ft2.getSort().isSubsort(s)) {
                                        ft2.setSort(s);
                                    }
                                    ((TermFeatureTerm) ft2).defineFeatureValue(fn, ft3_steps.m_a);
                                    generalizations_to_process.add(new Pair<FeatureTerm, Integer>(ft2, object_steps.m_b + ft3_steps.m_b + 1));
                                } // while
                            } // while
                        } // if
                    } // if

                } // while
            } // if
        } // if check_separability



        nodes_checked = generalizations_to_process.size();

        // Start search:
        while (!generalizations_to_process.isEmpty()) {

//			System.out.println("AU: " + iterations + " - Generalizations: " + generalizations.size() + ", " + generalizations_to_process.size());
	/*
            {
            char *s;
            FeatureTerm *f;

            printf("Generalizations: \n");
            generalizations.Rewind();
            while(generalizations.Iterate(f)) {
            s=f.toStringNOOS(domain_model);
            printf("%s\n",s);
            delete []s;
            } // while
            printf("Generalizations to process: \n");
            generalizations_to_process.Rewind();
            while(generalizations_to_process.Iterate(f)) {
            s=f.toStringNOOS(domain_model);
            printf("%s\n",s);
            delete []s;
            } // while
            }
             */


            {
                current = generalizations_to_process.remove(0);
                if (version == VERSION_FAST) {
                    tmp = FTRefinement.getSomeSpecializationSubsumingAll(current.m_a, dm, o, language, objects);
                } else {
                    tmp = FTRefinement.getSpecializationsSubsumingAll(current.m_a, dm, o, language, objects);
                }

//				System.out.println("AU: " + tmp.size() + " refinements...");

                next_nodes.addAll(0, tmp);

                if (version == VERSION_FAST) {
                    generalizations.clear();
                }

                /* In the COMPLETE version, since the "generalizations_to_process" list is not filtered */
                /* we have to test whether "current" is a new antiunification or not					*/
                if (version == VERSION_COMPLETE) {
                    List<Pair<FeatureTerm, Integer>> todelete = new LinkedList<Pair<FeatureTerm, Integer>>();
                    boolean advances = true;

                    for (Pair<FeatureTerm, Integer> f_steps : generalizations) {
                        if (f_steps.m_a.subsumes(current.m_a)) {
                            todelete.add(f_steps);
                        }
                        if (current.m_a.subsumes(f_steps.m_a)) {
                            advances = false;
                            break;
                        }
                    } // while

                    if (advances) {
                        while (!todelete.isEmpty()) {
                            Pair<FeatureTerm, Integer> f_steps = todelete.remove(0);
                            generalizations.remove(f_steps);
                        } // while

                        generalizations.add(current);
                    } // if
                } else {
                    generalizations.add(current);
                } // if
            }
            /*
            {
            char *s;
            s=current.toStringNOOS();
            printf("Current:\n%s\n",s);
            delete []s;
            printf(", Next: %i (total: %i) \n",next_nodes.Length(),nodes_checked);
            fflush(0);
            }
             */
            while (!next_nodes.isEmpty()) {
                node = next_nodes.remove(0);
                /*
                {
                char *s;
                s=node.toStringNOOS();
                printf("considering:\n%s\n",s);
                delete []s;
                }
                 */
                nodes_checked++;

                subsumes_all = true;

                /*
                for(FeatureTerm object:objects) {
                if (!node.subsumes(object)) {

                System.out.println("Does not subsume(node):\n" + node.toStringNOOS(dm));
                System.out.println("Does not subsume(object):\n" + object.toStringNOOS(dm));

                subsumes_all=false;
                break;
                } // if
                } // while
                 */
                if (subsumes_all) {
                    boolean more_specific = true;

                    // manage the generalizations:
                    for (Pair<FeatureTerm, Integer> generalization_steps : generalizations) {
                        if (generalization_steps.m_a.subsumes(node)) {
                            if (node.subsumes(generalization_steps.m_a)) {
                                more_specific = false;
                                break;
                            } else {
                                todelete_g.add(generalization_steps);
                            } // if
                        } else {
                            if (more_specific && node.subsumes(generalization_steps.m_a)) {
                                more_specific = false;
                                break;
                            } // if
                        } // if
                    } // while

                    if (more_specific) {
                        for (Pair<FeatureTerm, Integer> generalization_steps : generalizations_to_process) {
                            if (generalization_steps.m_a.subsumes(node)) {
                                if (node.subsumes(generalization_steps.m_a)) {
                                    more_specific = false;
                                    break;
                                } else {
                                    todelete_gtp.add(generalization_steps);
                                } // if
                            } else {
                                if (more_specific && node.subsumes(generalization_steps.m_a)) {
                                    more_specific = false;
                                    break;
                                } // if
                            } // if
                        } // while
                    }

                    if (more_specific) {
                        while (!todelete_g.isEmpty()) {
                            Pair<FeatureTerm, Integer> generalization_steps = todelete_g.remove(0);
                            generalizations.remove(generalization_steps);
                        } // while
                        if (version != VERSION_COMPLETE) {
                            while (!todelete_gtp.isEmpty()) {
                                Pair<FeatureTerm, Integer> generalization_steps = todelete_gtp.remove(0);
                                generalizations_to_process.remove(generalization_steps);
                            } // while
                        } // if

                        if (version == VERSION_FAST) {
                            generalizations_to_process.clear();
                            next_nodes.clear();
                        } // if

                        generalizations_to_process.add(0, new Pair<FeatureTerm, Integer>(node, current.m_b + 1));

                    } else {
                        System.err.println("Generalization is not more specific!!");
                        System.err.println(current.m_a.toStringNOOS(dm));
                        System.err.println(node.toStringNOOS(dm));
                    } // if

                } else {
//						printf("Does not subsume all examples...\n");
                } // if
            } // while

            // In the FAST mode, only one possible antiunification is likely to be found, but no search is required:
            if (version == VERSION_FAST) {
                if (generalizations.size() > 0) {
                    Pair<FeatureTerm, Integer> tmp2 = generalizations.remove(0);
                    generalizations.clear();
                    generalizations.add(tmp2);
                } // if
                if (generalizations_to_process.size() > 0) {
                    Pair<FeatureTerm, Integer> tmp2 = generalizations_to_process.remove(0);
                    generalizations_to_process.clear();
                    generalizations_to_process.add(tmp2);
                } // if
            } // if

            iterations++;
        } // while
	/*
        {
        FeatureTerm *f;
        char *s;

        printf("----------------------------------------\n");
        printf("Base objects:\n");
        objects.Rewind();
        while(objects.Iterate(f)) {
        s=f.toStringNOOS();
        printf("%s\n",s);
        delete s;
        } // while
        printf("Initial terms:\n");
        if (initial_terms!=0) {
        initial_terms.Rewind();
        while(initial_terms.Iterate(f)) {
        s=f.toStringNOOS();
        printf("%s\n",s);
        delete s;
        } // while
        } // if
        printf("Result:\n");
        generalizations.Rewind();
        while(generalizations.Iterate(f)) {
        s=f.toStringNOOS();
        printf("%s\n",s);
        delete s;
        } // while
        }
         */
//		System.out.println("Finishing AU: " + generalizations.size());

        return generalizations;

    } /* antiunification */


    public static boolean separableFeature(Symbol fname, List<FeatureTerm> objects, FTKBase dm) throws FeatureTermException {
        Sort s;
        List<FeatureTerm> l1, l2;
        FeatureTerm o1, o2;

        for (FeatureTerm object : objects) {
            List<FeatureTerm> os = new LinkedList<FeatureTerm>();

            if (object instanceof SetFeatureTerm) {
                os.addAll(((SetFeatureTerm) object).getSetValues());
            } else {
                os.add(object);
            }

            for (FeatureTerm o : os) {
                o1 = o.featureValue(fname);
                if (o1 != null) {
                    l1 = FTRefinement.variables(o1);

                    if (l1.contains(o)) {
                        return false;
                    }

                    s = o.getSort();
                    for (Symbol fname2 : s.getFeatures()) {
                        if (!fname2.equals(fname)) {
                            o2 = o.featureValue(fname2);
                            if (o2 != null) {
                                l2 = FTRefinement.variables(o2);

                                for (FeatureTerm tmp : l1) {
                                    if (!tmp.isConstant() &&
                                        (dm == null || !dm.contains(tmp))) {
                                        if (l2.contains(tmp)) {
                                            return false;
                                        }
                                    } // if
                                } // while
                            } // if
                        } // if
                    } // for
                } // if
            } // for
        } // while

        return true;
    } // separable_feature
}
