package fterms.learning;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import fterms.FTKBase;
import fterms.FTRefinement;
import fterms.FeatureTerm;
import fterms.Ontology;
import fterms.Path;
import fterms.exceptions.FeatureTermException;
import java.util.HashMap;

public abstract class Hypothesis {

    public Hypothesis() {
    }

    public abstract Prediction generatePrediction(FeatureTerm problem, FTKBase dm, boolean debug) throws FeatureTermException, Exception;

    public int size() {
        return 0;
    }

    public String toString(FTKBase dm) {
        return "Hypothesis\n";
    }

    public String toCompactString(FTKBase dm) {
        return "Hypothesis\n";
    }

    public static List<FeatureTerm> differentSolutions(Collection<FeatureTerm> examples, Path sp) throws FeatureTermException {
        List<FeatureTerm> solutions = new LinkedList<FeatureTerm>();

        for (FeatureTerm ex : examples) {
            FeatureTerm s = ex.readPath(sp);
            if (!solutions.contains(s)) {
                solutions.add(s);
            }
        }

        return solutions;
    }

    public static HashMap<FeatureTerm, Integer> distribution(Collection<FeatureTerm> examples, Path sp) throws FeatureTermException {
        HashMap<FeatureTerm, Integer> solutions = new HashMap<FeatureTerm, Integer>();

        for (FeatureTerm ex : examples) {
            FeatureTerm s = ex.readPath(sp);
            if (solutions.get(s) == null) {
                solutions.put(s, 1);
            } else {
                solutions.put(s, solutions.get(s) + 1);
            }
        }

        return solutions;
    }


    public static FeatureTerm mostCommonSolution(Collection<FeatureTerm> examples, Path sp) throws FeatureTermException {
        HashMap<FeatureTerm, Integer> solutions = new HashMap<FeatureTerm, Integer>();
        int max = 0;
        FeatureTerm maxSol = null;

        for (FeatureTerm ex : examples) {
            FeatureTerm s = ex.readPath(sp);
            if (solutions.get(s) == null) {
                solutions.put(s, 1);
            } else {
                solutions.put(s, solutions.get(s) + 1);
            }

            if (maxSol == null || solutions.get(s) > max) {
                maxSol = s;
                max = solutions.get(s);
            }
        }

        return maxSol;
    }

    public float evaluate(Collection<FeatureTerm> cases, FTKBase dm, Path sp, Path dp, boolean debug) throws Exception {
        // Evaluate the hypothesis:
        float score = 0;
        int problems = 0;
        int max = 10000;

        for (FeatureTerm c : cases) {
            if (problems < max) {
                Prediction p;
                try {
                    p = generatePrediction(c.readPath(dp), dm, debug);
                    if (p != null) {
                        //							System.out.println("Problem " + problems);
                        //							System.out.println("Prediction: " + p.toString(dm));
                    } else {
                        System.err.println("Problem " + problems);
                        System.err.println("No prediction found.");
                    }

                    if (debug) {
                        System.out.println("Real Solution: " + c.readPath(sp).toStringNOOS(dm));
                    }
                    float localScore = p.getScore(c.readPath(sp));

                    if (debug) {
                        System.out.println("Score: " + localScore);
                    }

                    if (localScore == 0) {
                        if (debug) {
                            System.out.println("Problem that was failed:");
                        }
                        if (debug) {
                            System.out.println(c.toStringNOOS(dm));
                        }
                    }

                    score += localScore;
                    problems++;
                } catch (FeatureTermException e) {
                    e.printStackTrace();
                }
            }
        }

//		System.out.println("Final Score: " + ((score/problems)*100) + "% in " + problems + " problems");
        return ((score / problems) * 100);
    }

    public static FeatureTerm generalizePattern(FeatureTerm initialPattern, List<FeatureTerm> positive, List<FeatureTerm> negative, Ontology o, FTKBase domain_model) throws FeatureTermException {
        List<FeatureTerm> l;
        boolean end, c1, c2;
        FeatureTerm pattern = initialPattern;

//		System.out.println("Generalizing: - " + positive.size() + " - " + negative.size());
        //		System.out.println(pattern.toStringNOOS(domain_model));

        do {
            end = true;
            l = FTRefinement.getGeneralizations(pattern, domain_model, o);
//			System.out.println(l.size() + " generalizations... ");

            for (FeatureTerm f2 : l) {

                //				if (!pattern.subsumes(f2)) {
                c1 = true;
                c2 = true;
                for (FeatureTerm d : positive) {
                    if (!f2.subsumes(d)) {
                        c1 = false;

                        //						System.out.println(f2.toStringNOOS(domain_model));
                        //						System.out.println("does not subsume");
                        //						System.out.println(d.toStringNOOS(domain_model));
                        break;
                    } else {
                        // System.out.println("ok");
                    }
                }
//                System.out.println("C1: " + c1);

                if (c1) {
                    for (FeatureTerm d : negative) {
                        if (f2.subsumes(d)) {
                            c2 = false;
                            break;
                        } else {
                            //							System.out.println("ok");
                        }
                    }
//                    System.out.println("C2: " + c2);
                }

                if (c1 && c2) {
                    pattern = f2;
                    end = false;
                    //						System.out.println("ok");
                    //						System.out.println(pattern.toStringNOOS(domain_model));
                    //						System.out.flush();
                    break;
                } else {
                    //						System.out.println("fail");
                }
                //				} else {
                //					System.err.println("pattern:");
                //					System.err.println(pattern.toStringNOOS(domain_model));
                //					System.err.println("f2:");
                //					System.err.println(f2.toStringNOOS(domain_model));
                //					System.err.flush();
                //				}
            }

            //			System.out.println("Generalization:");
            //			System.out.println(pattern.toStringNOOS(domain_model));
            //			System.out.flush();
        } while (!end);

//	System.out.println("Generalization: done... (" + positive.size() + "," + negative.size() + ")");

        return pattern;
    }
}
