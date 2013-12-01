package fterms.learning.distance;

import java.util.LinkedList;
import java.util.List;

import fterms.FTAntiunification;
import fterms.FTKBase;
import fterms.FTRefinement;
import fterms.FeatureTerm;
import fterms.Ontology;
import fterms.exceptions.FeatureTermException;
import util.Pair;

public class AUDistance extends Distance {

    static final boolean DEBUG = false;

    public double distance(FeatureTerm f1, FeatureTerm f2, Ontology o, FTKBase dm) throws FeatureTermException {
        FeatureTerm au;

        int steps_au = 0;
        int additional_steps_f1 = 0;
        int additional_steps_f2 = 0;

        List<FeatureTerm> objects = new LinkedList<FeatureTerm>();

        if (DEBUG) {
            System.out.print(f1.getName() + " - " + f2.getName() + " [");
            System.out.flush();
        }
        // Count antiunification steps:
        {
            List<FeatureTerm> results;
            objects.add(f1);
            objects.add(f2);
            results = FTAntiunification.antiunification(objects, FTRefinement.ALL_REFINEMENTS, null, o, dm, true, FTAntiunification.VERSION_FAST);
            objects.clear();
            au = results.get(0);

            if (DEBUG) {
                System.out.print("* ");
                System.out.flush();
            }

            steps_au = termSize(au, dm, o);
        }
        if (DEBUG) {
            System.out.print("a = " + steps_au + " ");
            System.out.flush();
        }

		// Count additional f1 steps:		
        {
            List<Pair<FeatureTerm, Integer>> results;
            List<Pair<FeatureTerm, Integer>> startl = new LinkedList<Pair<FeatureTerm, Integer>>();
            objects.add(f1);
            startl.add(new Pair<FeatureTerm, Integer>(au, 0));
            results = FTAntiunification.antiunificationCountingSteps(objects, FTRefinement.ALL_REFINEMENTS, startl, o, dm, false, FTAntiunification.VERSION_FAST);
            objects.clear();
            startl.clear();
            additional_steps_f1 = results.get(0).m_b;
        }

//		additional_steps_f1 = Math.max(termSize(f1,dm,o) - steps_au,0);
        if (DEBUG) {
            System.out.print("b = " + additional_steps_f1 + " ");
            System.out.flush();
        }

        // Count additional f2 steps:
        {
            List<Pair<FeatureTerm, Integer>> results;
            List<Pair<FeatureTerm, Integer>> startl = new LinkedList<Pair<FeatureTerm, Integer>>();
            objects.add(f2);
            startl.add(new Pair<FeatureTerm, Integer>(au, 0));
            results = FTAntiunification.antiunificationCountingSteps(objects, FTRefinement.ALL_REFINEMENTS, startl, o, dm, false, FTAntiunification.VERSION_FAST);
            objects.clear();
            startl.clear();
            additional_steps_f2 = results.get(0).m_b;
        }
//		additional_steps_f2 = Math.max(termSize(f2,dm,o) - steps_au,0);

        if (DEBUG) {
            System.out.print("c = " + additional_steps_f2 + "] -> ");
            System.out.flush();
        }

        double distance = 1.0f - (((double) (steps_au * 2)) / ((double) (steps_au * 2 + additional_steps_f1 + additional_steps_f2)));

        if (DEBUG) {
            System.out.println(distance);
            System.out.flush();
//			System.out.println(au.toStringNOOS(dm));
        }
        /*		
         System.out.println(f1.toStringNOOS(dm));
         System.out.flush();
         System.out.println(f2.toStringNOOS(dm));
         System.out.flush();
         System.out.println(au.toStringNOOS(dm));
         System.out.flush();
         */
        return distance;
    }

    int termSize(FeatureTerm f, FTKBase dm, Ontology o) throws FeatureTermException {
        int steps = 0;
        boolean done = false;

        while (!done) {
            List<FeatureTerm> generalizations = FTRefinement.getSomeGeneralizationsAggressive(f, dm, o);

            if (generalizations.isEmpty()) {
//				System.out.println(f.toStringNOOS(dm));
                done = true;
            } else {
                steps++;
                f = generalizations.get(0);
            }

//			System.out.println(steps + " - " + generalizations.size());
//			if ((steps%100==0)) System.out.println(f.toStringNOOS(dm));
        }

        return steps;
    }

}
