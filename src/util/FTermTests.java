/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package util;

/**
 *
 * @author santi
 */
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import util.RewindableInputStream;

import fterms.BaseOntology;
import fterms.Disintegration;
import fterms.FTKBase;
import fterms.FTRefinement;
import fterms.FTUnification;
import fterms.FeatureTerm;
import fterms.FloatFeatureTerm;
import fterms.IntegerFeatureTerm;
import fterms.NOOSParser;
import fterms.Ontology;
import fterms.SetFeatureTerm;
import fterms.Sort;
import fterms.Symbol;
import fterms.SymbolFeatureTerm;
import fterms.TermFeatureTerm;
import fterms.exceptions.FeatureTermException;
import fterms.learning.TrainingSetProperties;
import fterms.learning.TrainingSetUtils;


public class FTermTests {
    private static FTKBase case_base;
	public static void main(String args[]) {
		long t_start,t_end;
		int errors = 0;

		System.out.println("Feature Terms Unit Tests...");

		try {
			t_start=System.currentTimeMillis();
			errors+=basicTests();
			t_end=System.currentTimeMillis();
			System.out.println("Time: " + (t_end - t_start));
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Feature Terms Unit Tests finished: " + errors + " tests failed.");
	}

	static int basicTests() throws IOException, FeatureTermException
	{
		int errors = 0;

		Ontology base_ontology = new BaseOntology();
		Ontology o = new Ontology();
		o.uses(base_ontology);
		FTKBase base_domain_model = new FTKBase();
		FTKBase domain_model_trains = new FTKBase();
		FTKBase domain_model_toxicology = new FTKBase();
		FTKBase domain_model_families = new FTKBase();

		/*
		 * ---------------------------------------------------------------------------------
		 */

		System.out.println("Initialization tests...");
		if (base_ontology.getNSorts()!=6) {
			int n = base_ontology.getNSorts();
			System.out.println("Initialization Test 1 failed: N sorts in base_ontology is " + n);
			for(Sort s:base_ontology.getSorts()) {
				System.out.println(s.get());
			}
			errors++;
		}
		if (base_ontology.getNUndefinedSorts()!=0) {
			int n = base_ontology.getNUndefinedSorts();
			System.out.println("Initialization Test 2 failed: N undefined sorts in base_ontology is " + n);
			for(Sort s:base_ontology.getSorts()) {
				System.out.println(s.get());
			}
			errors++;
		}

		base_domain_model.create_boolean_objects(base_ontology);
		domain_model_trains.uses(base_domain_model);
		domain_model_toxicology.uses(base_domain_model);
		domain_model_families.uses(base_domain_model);

		if (base_ontology.getNSorts()!=6) {
			int n = base_ontology.getNSorts();
			System.out.println("Initialization Test 3 failed: N sorts in base_ontology is " + n);
			for(Sort s:base_ontology.getSorts()) {
				System.out.println(s.get());
			}
			errors++;
		}
		if (base_domain_model.get_n_terms()!=2) {
			System.out.println("Initialization Test 4 failed: N terms in base_domain_model is " + base_domain_model.get_n_terms());
			errors++;
		}
		if (base_domain_model.get_n_undefined_terms()!=0) {
			System.out.println("Initialization Test 5 failed: N undefined terms in base_domain_model is " + base_domain_model.get_n_undefined_terms());
			errors++;
		}

		/*
		 * ---------------------------------------------------------------------------------
		 */

		System.out.println("Ontology Load tests...");
		domain_model_trains.ImportNOOS("NOOS/trains-ontology.noos",o);
		if (o.getNSorts()!=16) {
			int n = o.getNSorts();
			System.out.println("Ontology Test 1 failed: N sorts in o is " + n);
			for(Sort s:o.getSorts()) {
				System.out.println(s.get());
			}
			errors++;
		}
		domain_model_toxicology.ImportNOOS("NOOS/toxic-eva-ontology.noos",o);
		if (o.getNSorts()!=235) {
			int n = o.getNSorts();
			System.out.println("Ontology Test 2 failed: N sorts in o is " + n);
			System.out.println(o.getDescription());
			errors++;
		}

		/*
		 * ---------------------------------------------------------------------------------
		 */

		System.out.println("Case Base Load tests...");
		FTKBase cb_trains = new FTKBase();
		domain_model_trains.ImportNOOS("NOOS/trains-dm.noos", o);
		cb_trains.uses(domain_model_trains);
		cb_trains.ImportNOOS("NOOS/trains-cases-10.noos", o);

		cb_trains.print_status();

		if (cb_trains.SearchFT(o.getSort("trains-description")).size()!=10) {
			int n = cb_trains.SearchFT(o.getSort("trains-description")).size();
			System.out.println("Case Base Load Test 1 failed: N trains-description objects is " + n);
			for(FeatureTerm obj:cb_trains.SearchFT(o.getSort("trains-description"))) {
				System.out.println(obj.toStringNOOS(domain_model_trains));
			}
			errors++;
		}

		FTKBase cb_toxicology = new FTKBase();
		cb_toxicology.uses(domain_model_toxicology);
		domain_model_toxicology.ImportNOOS("NOOS/toxic-eva-dm.noos", o);
		cb_toxicology.ImportNOOS("NOOS/toxic-eva-cases-50.noos", o);

		cb_toxicology.print_status();

		if (cb_toxicology.SearchFT(o.getSort("toxic-problem")).size()!=47) {
			int n = cb_toxicology.SearchFT(o.getSort("toxic-problem")).size();
			System.out.println("Case Base Load Test 2 failed: N toxic-problem objects is " + n);
			for(FeatureTerm f:cb_toxicology.SearchFT(o.getSort("toxic-problem"))) {
				if (f.getName()!=null) {
					System.out.println(f.getName().get());
				} else {
					System.out.println(f.toStringNOOS(domain_model_toxicology));
				}
			}
			errors++;
		}

		/*
		 * ---------------------------------------------------------------------------------
		 */

		System.out.println("Subsumtion tests...");
		{
			FTKBase case_base = new FTKBase();
			case_base.uses(base_domain_model);
			FeatureTerm f1 = NOOSParser.parse(new RewindableInputStream(new StringBufferInputStream("(define (symbol))")),case_base,o);
			FeatureTerm f2 = NOOSParser.parse(new RewindableInputStream(new StringBufferInputStream("\"hello\"")),case_base,o);
			FeatureTerm f3 = NOOSParser.parse(new RewindableInputStream(new StringBufferInputStream("\"bye\"")),case_base,o);
			FeatureTerm f4 = NOOSParser.parse(new RewindableInputStream(new StringBufferInputStream("\"hello\"")),case_base,o);

            FeatureTerm f5 = NOOSParser.parse(new RewindableInputStream(new StringBufferInputStream("(define (set))")),case_base,o);
            FeatureTerm f6 = NOOSParser.parse(new RewindableInputStream(new StringBufferInputStream("(define (car))")),case_base,o);
            FeatureTerm f7 = NOOSParser.parse(new RewindableInputStream(new StringBufferInputStream("(define (car) (infront (define (set))))")),case_base,o);
			FeatureTerm f8 = NOOSParser.parse(new RewindableInputStream(new StringBufferInputStream("(define (car) (infront (define (car))))")),case_base,o);

            System.out.println("f5: " + f5.toStringNOOS(case_base));
            System.out.println("f6: " + f6.toStringNOOS(case_base));
            System.out.println("f7: " + f7.toStringNOOS(case_base));
            System.out.println("f8: " + f8.toStringNOOS(case_base));

			if (!f1.subsumes(f2)) {
				errors++;
				System.out.println("Failed: a generic Symbol does not subsume a constant symbol!");
			}

			if (!f2.subsumes(f4)) {
				errors++;
				System.out.println("Failed: Two identical constant symbols do not subsume each other!");
			}

			if (f2.subsumes(f3)) {
				errors++;
				System.out.println("Failed: Two different constant symbols subsume each other!");
			}

			if (!f2.equals(f4)) {
				errors++;
				System.out.println("Failed: Two identical constant symbols are not equals!");
			}

			if (f2.equals(f3)) {
				errors++;
				System.out.println("Failed: Two different constant symbols are equals!");
			}

			if (!f5.subsumes(f6)) {
				errors++;
				System.out.println("Failed: An empty set does not subsume a feature term!");
			}

			if (!f6.subsumes(f7)) {
				errors++;
				System.out.println("Failed: An empty feature does not subsume an empty set!");
			}

            if (!f7.subsumes(f6)) {
				errors++;
				System.out.println("Failed: Defining a feature with an empty set is different than not having a value!");
			}

            if (!f7.subsumes(f8)) {
				errors++;
				System.out.println("Failed: Defining a feature with an empty set does not subsume something with a value!");
			}

            List<FeatureTerm> l = new LinkedList<FeatureTerm>();
			HashSet<FeatureTerm> s = new HashSet<FeatureTerm>();

			if (!l.contains(f1)) l.add(f1);
			if (!l.contains(f2)) l.add(f2);
			if (!l.contains(f3)) l.add(f3);
			if (!l.contains(f4)) l.add(f4);
			if (l.size()!=3) {
				errors++;
				System.out.println("Failed: List containment failure!");
			}

			if (!s.contains(f1)) s.add(f1);
			if (!s.contains(f2)) s.add(f2);
			if (!s.contains(f3)) s.add(f3);
			if (!s.contains(f4)) s.add(f4);
			if (s.size()!=3) {
				errors++;
				System.out.println("Failed: HashSet containment failure!");
			}


			{
				// Problem in sponge subsumption:
				Ontology o2=new Ontology();
				FTKBase dm=new FTKBase();
				o2.uses(base_ontology);
				FTKBase case_base2=new FTKBase();
				case_base2.uses(dm);

				dm.create_boolean_objects(o2);

				dm.ImportNOOS("NOOS/sponge-ontology.noos",o2);
				dm.ImportNOOS("NOOS/sponge-dm.noos",o2);

				f1 = NOOSParser.parse(new RewindableInputStream(new StringBufferInputStream("(define (megascleres) (acanthose (define (acanthose))))")),case_base2,o2);
				f2 = NOOSParser.parse(new RewindableInputStream(new StringBufferInputStream("(define (megascleres) (acanthose no-acanthose))")),case_base2,o2);

				if (!f1.subsumes(f2)) {
					errors++;
					System.out.println("Failed: Sponge subsumption failure 1!!");
				}
				if (f2.subsumes(f1)) {
					errors++;
					System.out.println("Failed: Sponge subsumption failure 2!!");
				}

			}
		}

		/*
		 * ---------------------------------------------------------------------------------
		 */

        errors += refinementTests(o,base_domain_model,domain_model_trains,domain_model_families, cb_trains);

		errors += antiunificationTests(o,domain_model_families);

		/*
		 * ---------------------------------------------------------------------------------
		 */

		errors += generalizationTests();

		errors += unificationTests(o,domain_model_families);

        errors += disintegrationTests();

		return errors;
	}


    static int refinementTests(Ontology o,FTKBase base_domain_model, FTKBase domain_model_families,
                                          FTKBase domain_model_trains, FTKBase cb_trains) throws IOException, FeatureTermException {
        int errors = 0;
		System.out.println("Specialization Refinements tests...");
		{
			base_domain_model.ImportNOOS("NOOS/simple-family-ontology.noos",o);

			TermFeatureTerm x1 = new TermFeatureTerm((Symbol)null,o.getSort("person"));
			FeatureTerm x2 = new TermFeatureTerm((Symbol)null,o.getSort("male"));
			FeatureTerm x3 = new TermFeatureTerm((Symbol)null,o.getSort("female"));
			FeatureTerm x4 = new TermFeatureTerm((Symbol)null,o.getSort("person"));
			SetFeatureTerm set1 = new SetFeatureTerm();
			set1.addSetValue(x3);
			set1.addSetValue(x4);
			x1.defineFeatureValue(new Symbol("father"),x2);
			x1.defineFeatureValue(new Symbol("children"),set1);

			System.out.println("Generated Term:\n" + x1.toStringNOOS());

			List<FeatureTerm> variables = FTRefinement.variables(x1);
			if (variables.size()!=4) {
				System.out.println("Failed: Specialization Refinements Test 1 failed: N variables is " + variables.size());
				for(FeatureTerm f:variables) {
					if (f.getName()!=null) {
						System.out.println(f.getName().get());
					} else {
						System.out.println(f.toStringNOOS(base_domain_model));
					}
				}
				errors++;
			}

			List<FeatureTerm> sortRefinements = FTRefinement.sortSpecialization(x1,base_domain_model,null);
			if (sortRefinements.size()!=4) {
				System.out.println("Failed: Specialization Refinements Test 2 failed: N sort refinements is " + sortRefinements.size());
				for(FeatureTerm f:sortRefinements) {
					System.out.println(f.toStringNOOS(base_domain_model));
				}
				errors++;
			}

			List<FeatureTerm> featureRefinements = FTRefinement.featureIntroduction(x1,base_domain_model,null);
			if (featureRefinements.size()!=20) {
				System.out.println("Failed: Specialization Refinements Test 3 failed: N feature refinements is " + featureRefinements.size());
				for(FeatureTerm f:featureRefinements) {
					System.out.println(f.toStringNOOS(base_domain_model));
				}
				errors++;
			}

			List<FeatureTerm> equalityRefinements = FTRefinement.variableEqualityAddition(x1,base_domain_model,null);
			if (equalityRefinements.size()!=4) {
				System.out.println("Failed: Specialization Refinements Test 4 failed: N equality refinements is " + equalityRefinements.size());
				for(FeatureTerm f:equalityRefinements) {
					System.out.println(f.toStringNOOS(base_domain_model));
				}
				errors++;
			}

			List<FeatureTerm> setRefinements = FTRefinement.setExpansion(x1,base_domain_model,null);
			if (setRefinements.size()!=2) {
				System.out.println("Failed: Specialization Refinements Test 5 failed: N set refinements is " + setRefinements.size());
				for(FeatureTerm f:setRefinements) {
					System.out.println(f.toStringNOOS(base_domain_model));
				}
				errors++;
			}

            {
                o.newSort("s", "any", new String[]{"f","g"}, new String[]{"s","s"});
                
                FeatureTerm f1 = NOOSParser.parse(new RewindableInputStream(new StringBufferInputStream(
                        "(define ?X1 (s) " +
                        "  (f (define ?X2 (s) " +
                        "       (f (define (s) " +
                        "            (f (define (set) " +
                        "                 !X1 " +
                        "                 !X2 " +
                        "                 (define (s) " +
                        "                  (f (define (s)))))))))))")),base_domain_model,o);

                List<FeatureTerm> veRefinements = FTRefinement.variableEqualityAddition(f1, base_domain_model, null);
                for(FeatureTerm r:veRefinements) {
                    if (!f1.subsumes(r)) {
    					System.out.println("Error in variable Equality Addition refinement!\nOriginal term:\n" + f1.toStringNOOS());
    					System.out.println("Refinement:\n" + r.toStringNOOS());
                        errors++;
                    }
                }


                System.out.println("Trying to generate a loop of 2 out of a loop of 4 by variable equality refinement...");
                FeatureTerm f2 = NOOSParser.parse(new RewindableInputStream(new StringBufferInputStream(
                        "(define ?X1 (s) " +
                        "  (f (define (s) " +
                        "    (f (define (s) " +
                        "      (f (define (s) " +
                        "        (f !X1))))))))")),base_domain_model,o);
/*
                FeatureTerm f2 = NOOSParser.parse(new RewindableInputStream(new StringBufferInputStream(
                        "(define ?X1 (s) " +
                        "  (f (define (set) " +
                        "    (define (s) " +
                        "      (f !X1)) " +
                        "    (define (s) " +
                        "      (f !X1)))))")),base_domain_model,o);
*/
                List<FeatureTerm> open = new LinkedList<FeatureTerm>();
                List<FeatureTerm> closed = new LinkedList<FeatureTerm>();
                open.add(f2);
                while(!open.isEmpty()) {
                    FeatureTerm f = open.remove(0);
                    closed.add(f);
//                    System.out.println("original:" + f.toStringNOOS());
                    List<FeatureTerm> l = FTRefinement.variableEqualityAddition(f, base_domain_model, null);
//                    List<FeatureTerm> l = FTRefinement.getSpecializations(f, base_domain_model, FTRefinement.ALL_REFINEMENTS);
                    for(FeatureTerm r:l) {
//                        System.out.println("refinement:" + r.toStringNOOS());
                        boolean found = false;
                        for(FeatureTerm tmp:open) {
                            if (tmp.equivalents(r)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found)
                            for(FeatureTerm tmp:closed) {
                                if (tmp.equivalents(r)) {
                                    found = true;
                                    break;
                                }
                            }
                        if (!found) open.add(r);
                    }
                }
                System.out.println("Generating variable equality refinements of a 4 loop, found " + closed.size());
                for(FeatureTerm f:closed) {
                    System.out.println(f.toStringNOOS());
                }

            }



		}

		/*
		 * ---------------------------------------------------------------------------------
		 */

		System.out.println("Generalization Refinements tests...");
		{
			domain_model_families.ImportNOOS("NOOS/family-ontology.noos", o);
			domain_model_families.ImportNOOS("NOOS/family-dm.noos", o);
			FTKBase cb_families = new FTKBase();
			cb_families.uses(domain_model_families);
			cb_families.ImportNOOS("NOOS/family-cases-test.noos", o);

			FeatureTerm root1,root2;
			{
				Set<FeatureTerm> l = cb_families.SearchFT(new Symbol("victoria"));
				if (l.size()!=1) {
					System.out.println("Failed: Generalization Refinements Test 1 failed: There is more than 1 roberto in the case base!");
					errors++;
				}
				root1 = l.iterator().next();
			}
			{
				Set<FeatureTerm> l = cb_trains.SearchFT(new Symbol("t1"));
				if (l.size()!=1) {
					System.out.println("Failed: Generalization Refinements Test 2 failed: There is more than 1 roberto in the case base!");
					errors++;
				}
				root2 = l.iterator().next();
			}

			System.out.println("Generated Term Root 1:\n" + root1.toStringNOOS());
			List<FeatureTerm> generalizations1 = FTRefinement.getGeneralizations(root1, domain_model_families, o);
			System.out.println(generalizations1.size() + " generalizations");
			if (generalizations1.size()!=9) {
				System.out.println("Failed: Generalization Refinements Test 3 failed: not 9 generalizations of victoria!");
				errors++;
			}
			for(FeatureTerm g:generalizations1) {
				if (!g.subsumes(root1)) {
					System.out.println("Failed: Generalization Refinements Test 4 failed: Generalization does not subsume original!");
					System.out.println(g.toStringNOOS());
					errors++;
				}
			}

			System.out.println("Generated Term Root 2:\n" + root2.toStringNOOS());
			List<FeatureTerm> generalizations2 = FTRefinement.getGeneralizations(root2, domain_model_trains, o);
			System.out.println(generalizations2.size() + " generalizations");
			if (generalizations1.size()!=9) {
				System.out.println("Failed: Generalization Refinements Test 5 failed: not 9 generalizations of T1!");
				errors++;
			}
			for(FeatureTerm g:generalizations2) {
				if (!g.subsumes(root2)) {
					System.out.println("Failed: Generalization Refinements Test 6 failed: Generalization does not subsume original!");
					System.out.println(g.toStringNOOS());
					errors++;
				}
			}


		}
        return errors;
    }

    static int generalizationTests() throws IOException,FeatureTermException {
        int errors = 0;
        System.out.println("Generalization tests...");
        FeatureTerm f = null;
        Ontology base_ontology = new BaseOntology();
        Ontology o=new Ontology();
        FTKBase dm=new FTKBase();
        FTKBase cb=new FTKBase();
        o.uses(base_ontology);
        cb.uses(dm);

        TrainingSetProperties ts = TrainingSetUtils.loadTrainingSet(TrainingSetUtils.TRAINS_DATASET, o, dm, cb);

        f = NOOSParser.parse(new RewindableInputStream(new StringBufferInputStream(
            "(define (trains-description) " +
            "  (cars (define (set) " +
            "          (define (car) " +
            "           (infront (define ?X4 (car)))) " +
            "          !X4)))")),case_base,o);

        List<FeatureTerm> l = FTRefinement.getGeneralizationsAggressive(f, dm, o);
        for(FeatureTerm r:l) {
            boolean investigate = false;
            if (!r.subsumes(f)) {
                errors++;
                System.out.println("Generalization does not subsume original!!!");
                System.out.println("Original:");
                System.out.println(f.toStringNOOS(dm));
                System.out.println("Generalization:");
                System.out.println(r.toStringNOOS(dm));
            } else {
                if (f.subsumes(r)) {
                    errors++;
                    System.out.println("Original, more general than generalization!!!");
                    System.out.println("Original:");
                    System.out.println(f.toStringNOOS(dm));
                    System.out.println("Generalization:");
                    System.out.println(r.toStringNOOS(dm));
                }
            }
        }

        return errors;
    }

    static int disintegrationTests() throws FeatureTermException, IOException
	{
        int datasets[]= {TrainingSetUtils.ZOOLOGY_DATASET,TrainingSetUtils.UNCLE_DATASET,TrainingSetUtils.TRAINS_DATASET};
        String message[]={"Language L0, zoology","Language Lc, uncle, no sets","Language L, trains"};
        String examples[]={"zp-1680","e1","tr6"};
		int errors = 0;
		System.out.println("Disintegration tests...");

        for(int i = 0;i<datasets.length;i++) {
            Ontology base_ontology = new BaseOntology();
            Ontology o=new Ontology();
            FTKBase dm=new FTKBase();
            FTKBase cb=new FTKBase();
            o.uses(base_ontology);
            cb.uses(dm);

            System.out.println(message[i]);
            TrainingSetProperties ts = TrainingSetUtils.loadTrainingSet(datasets[i], o, dm, cb);

            FeatureTerm c = ts.getCaseByName(examples[i]);
            FeatureTerm description = c.readPath(ts.description_path);
            System.out.println("Disintegrating " + c.getName().get());
            List<Pair<FeatureTerm,FeatureTerm>> disintegrationTrace = Disintegration.disintegrateWithTrace(description, dm, o);

            FeatureTerm last = description.clone(dm, o);
            {
                List<FeatureTerm> variables = FTRefinement.variables(last);
                for(FeatureTerm v:variables) {
                    if (!dm.contains(v)) v.setName(null);
                }
            }

            for(Pair<FeatureTerm,FeatureTerm> property_term:disintegrationTrace) {
                boolean anyproblem = false;
                // 1st, test whether the remainder is correct:
                if (FTUnification.isUnification(last,property_term.m_a,property_term.m_b,dm,o)) {
                    System.out.println("remainder is correct!");
                } else {
                    System.out.println("Failed: remainder is incorrect!!!!!");
                    errors++;
                    anyproblem = true;
                }


                // 2nd, test whether unification works:
                List<FeatureTerm> unifications = FTUnification.unification(property_term.m_a, property_term.m_b, dm);
                boolean found = false;
                boolean fine = true;
                if (unifications!=null && unifications.size()>0) {
                    System.out.println("Unification yields " + unifications.size() + " results.");
                    for(FeatureTerm u:unifications) {
                        if (last.equivalents(u)) {
                            found = true;
                            System.out.println("ok!");
                            break;
                        }
                    }
                } else {
                    System.out.println("Property and rest do not unify!!!");
                    if (!property_term.m_a.subsumes(last)) {
                        System.out.println("The property does not subsume the original term!!!!!");
                        fine = false;
                    }
                    if (!property_term.m_b.subsumes(last)) {
                        System.out.println("The rest does not subsume the original term!!!!!");
                        fine = false;
                    }
                    if (fine) {
                        System.out.println("There is an error in the unification method...");
                    }
                }
                if (!found || !fine) anyproblem = true;
 
                if (anyproblem) {
                    errors++;
                    System.out.println("Disintegration error, unifying property with rest does not recover original term!!!!");
                    System.out.println("Property is:");
                    System.out.println(property_term.m_a.toStringNOOS(dm));
                    System.out.println("Rest is:");
                    System.out.println(property_term.m_b.toStringNOOS(dm));
                    System.out.println("original is:");
                    System.out.println(last.toStringNOOS(dm));

                    System.out.println("Unifications are:");
                    if (unifications!=null) {
                        for(FeatureTerm g:unifications) {
                            System.out.println(g.toStringNOOS(dm));
                        }
                    }
                    List<FeatureTerm> gs = FTRefinement.variableEqualityEliminationAggressive(last, dm);
                    System.out.println("Generalizations of original:");
                    for(FeatureTerm g:gs) {
                        System.out.println(g.toStringNOOS(dm));
                    }
                }
                last = property_term.m_b;
            }
        }



        return errors;
    }


    static int antiunificationTests(Ontology o,FTKBase dm) throws FeatureTermException, IOException
	{
		int errors = 0;
		System.out.println("Antiunification tests...");

        // ...

        return errors;
    }


	static int unificationTests(Ontology o,FTKBase dm) throws FeatureTermException, IOException
	{
		int errors = 0;
		int i = 0;
		FTKBase case_base = new FTKBase();

		case_base.uses(dm);

        dm.ImportNOOS("NOOS/zoology-ontology.noos",o);
        dm.ImportNOOS("NOOS/zoology-dm.noos",o);
        dm.ImportNOOS("NOOS/sponge-ontology.noos",o);
        dm.ImportNOOS("NOOS/sponge-dm.noos",o);
        dm.ImportNOOS("NOOS/trains-ontology.noos",o);
        dm.ImportNOOS("NOOS/trains-dm.noos",o);

        System.out.println("Unification tests...");

		FeatureTerm f1 = null;
		FeatureTerm f2 = null;
		int expected_result = 0;
		List<FeatureTerm> unifications;

		//	Integers:
		do {
			switch(i) {
			case 0:
				f1 = new IntegerFeatureTerm(null,o);
				f2 = new IntegerFeatureTerm(1,o);
				expected_result = 1;
				break;
			case 1:
				f1 = new FloatFeatureTerm(null,o);
				f2 = new FloatFeatureTerm(1.0f,o);
				expected_result = 1;
				break;
			case 2:
				f1 = new SymbolFeatureTerm(null,o);
				f2 = new SymbolFeatureTerm(new Symbol("hola"),o);
				expected_result = 1;
				break;
			case 3:
				f1 = NOOSParser.parse(new RewindableInputStream(new StringBufferInputStream(
						"(define (person) " +
						"  (son (define (male)))" +
				")")),case_base,o);
				f2 = NOOSParser.parse(new RewindableInputStream(new StringBufferInputStream(
						"(define (female) " +
						"  (son (define (person) (son (define (male))))))" +
				")")),case_base,o);
				expected_result = 1;
				break;
			case 4:
				f1 = NOOSParser.parse(new RewindableInputStream(new StringBufferInputStream(
						"(define (person) " +
						"  (GRANDFATHER (define (male) " +
						"         (wife (define (female)) )) " +
						"       (define (male)))" +
				")")),case_base,o);
				f2 = NOOSParser.parse(new RewindableInputStream(new StringBufferInputStream(
						"(define (female) " +
						"  (GRANDFATHER (define (person) " +
						"         (son (define (male)))))" +
				")")),case_base,o);
				expected_result = 3;
				break;
			case 5:
				f1 = NOOSParser.parse(new RewindableInputStream(new StringBufferInputStream(
						"(define ?X1 (male) " +
									   "  (son (define (male) " +
									   "         (father !X1)) " +
									   "  ) " +
									   ") ")),case_base,o);
				f2 = NOOSParser.parse(new RewindableInputStream(new StringBufferInputStream(
						"(define (male) " +
									   ") ")),case_base,o);
				expected_result = 1;
				break;
			case 6:
				f1 = NOOSParser.parse(new RewindableInputStream(new StringBufferInputStream(
						"(define ?X1 (male) " +
									   "  (son (define (male) " +
									   "         (wife (define (female))) " +
									   "         (father !X1)) " +
									   "  ) " +
									   ") ")),case_base,o);
				f2 = NOOSParser.parse(new RewindableInputStream(new StringBufferInputStream(
						"(define (male) " +
									   "  (son (define (male) " +
									   "         (father (define (male)))) " +
									   "  ) " +
									   ") ")),case_base,o);
				expected_result = 1;
				break;
			case 7:
				f1 = NOOSParser.parse(new RewindableInputStream(new StringBufferInputStream(
						"(define (person) " +
									   "  (son (define ?X1 (male))) " +
									   "  (grandfather !X1) " +
									   ") ")),case_base,o);
					f2 = NOOSParser.parse(new RewindableInputStream(new StringBufferInputStream(
							"(define (male) " +
									   "  (son (define (male) " +
									   "         (uncle (define (male))))) " +
									   "  (grandfather (define (male) " +
									   "            (mother (define (female))))) " +
									   ") ")),case_base,o);
				expected_result = 2;
				break;
            case 8:
                f1 = NOOSParser.parse(new RewindableInputStream(new StringBufferInputStream(
                        "(define (sponge-problem) " +
                        " (description (define (sponge) " +
                        " (spiculate-skeleton (define (spiculate-skeleton) " +
                        " (megascleres (define (megascleres) " +
                        " (smooth-form style))))))))")),case_base,o);
                f2 = NOOSParser.parse(new RewindableInputStream(new StringBufferInputStream(
                        "(define (sponge-problem) " +
                        " (description (define (sponge) " +
                        " (spiculate-skeleton (define (spiculate-skeleton) " +
                        " (megascleres (define (megascleres) " +
                        " (smooth-form oxea))))))))")),case_base,o);
				expected_result = 1;
                break;
            case 9:
                f1 = NOOSParser.parse(new RewindableInputStream(new StringBufferInputStream(
                        "(define (female) " +
                        "                     (brother (define (male) " +
                        "                                (father (define (male))))))")),case_base,o);
                f2 = NOOSParser.parse(new RewindableInputStream(new StringBufferInputStream(
                        "(define ?X3 (female) " +
                        "                     (brother (define ?X4 (male) " +
                        "                                (sister !X3))) " +
                        "                     (father (define (male) " +
                        "                               (son !X4) " +
                        "                               (daughter !X3))))")),case_base,o);
                 
                expected_result = 1;
                break;
            case 10:
                f1 = NOOSParser.parse(new RewindableInputStream(new StringBufferInputStream(
                        "(define (female) " +
                        "                 (mother (define (female) " +
                        "                           (husband (define ?X5 (male))))) " +
                        "                 (brother (define (male) " +
                        "                            (father !X5))))")),case_base,o);
                f2 = NOOSParser.parse(new RewindableInputStream(new StringBufferInputStream(
                        "(define ?X3 (female) " +
                        "                 (father (define ?X4 (male) " +
                        "                           (daughter !X3) " +
                        "                           (son (define ?X5 (male) " +
                        "                                  (sister !X3) " +
                        "                                  (father !X4) " +
                        "                                  (mother (define ?X7 (female) " +
                        "                                            (husband (define (male))))))) " +
                        "                           (wife !X7))) " +
                        "                 (mother !X7) " +
                        "                 (brother !X5))")),case_base,o);
                expected_result = 1;
                break;
            case 11:
                f1 = NOOSParser.parse(new RewindableInputStream(new StringBufferInputStream(
                    " (define (trains-description)" +
                    "   (cars (define (car)" +
                    "          (nwhl 2))))")),case_base,o);
                f2 = NOOSParser.parse(new RewindableInputStream(new StringBufferInputStream(
                    " (define (trains-description) " +
                    "  (cars (define (set) " +
                    "          (define (car) " +
                    "           (infront (define ?X4 (car) " +
                    "                      (nwhl (define (integer))) " +
                    "                      (loc 2) " +
                    "                      (npl 3) " +
                    "                      (cshape closedrect) " +
                    "                      (infront (define ?X8 (car) " +
                    "                                 (lcont (define (trianglod))) " +
                    "                                 (nwhl 2) " +
                    "                                 (ln short) " +
                    "                                 (loc 3) " +
                    "                                 (npl 1) " +
                    "                                 (cshape openrect)))))) " +
                    "          !X4 " +
                    "          !X8)))")),case_base,o);
                expected_result = 1;
                break;
            case 12:
                f1 = NOOSParser.parse(new RewindableInputStream(new StringBufferInputStream(
                    "(define (trains-description) " +
                    "  (ncar 3) " +
                    "  (cars (define (set) " +
                    "          (define (car) " +
                    "           (infront (define ?X5 (car) " +
                    "                      (lcont (define (set) " +
                    "                               (define (circlelod)) " +
                    "                               (define (circlelod)) " +
                    "                               )) " +
                    "                      (infront (define ?X7 (car) " +
                    "                                 (lcont (define (trianglod))) " +
                    "                                 (npl 1) " +
                    "                                 (nwhl 2) " +
                    "                                 (loc 3) " +
                    "                                 (cshape openrect) " +
                    "                                 (ln short))) " +
                    "                      (npl 3) " +
                    "                      (nwhl 2) " +
                    "                      (loc 2) " +
                    "                      (cshape closedrect) " +
                    "                      (ln long))) " +
                    "           (npl 0) " +
                    "           (nwhl 2) " +
                    "           (loc 1) " +
                    "           (cshape engine) " +
                    "           (ln long)) " +
                    "          !X5 " +
                    "          !X7)))")),case_base,o);
                f2 = NOOSParser.parse(new RewindableInputStream(new StringBufferInputStream(
                    "(define (trains-description) " +
                    "  (ncar 3) " +
                    "  (cars (define (set) " +
                    "          (define (car) " +
                    "           (infront (define ?X5 (car) " +
                    "                      (lcont (define (circlelod))) " +
                    "                      (infront (define ?X6 (car) " +
                    "                                 (lcont (define (trianglod))) " +
                    "                                 (npl 1) " +
                    "                                 (loc 3) " +
                    "                                 (nwhl 2) " +
                    "                                 (cshape openrect) " +
                    "                                 (ln short))) " +
                    "                      (npl 3) " +
                    "                      (loc 2) " +
                    "                      (nwhl 2) " +
                    "                      (cshape closedrect) " +
                    "                      (ln long))) " +
                    "           (npl 0) " +
                    "           (loc 1) " +
                    "           (nwhl 2) " +
                    "           (cshape engine) " +
                    "           (ln long)) " +
                    "          !X5 " +
                    "          !X6)))")),case_base,o);
                expected_result = 1;
                break;

            case 13:
                f1 = NOOSParser.parse(new RewindableInputStream(new StringBufferInputStream(
                    "(define (car)" +
                    "  (lcont (define (set)" +
                    "    (define (load))" +
                    "    (define (load))" +
                    "    (define (circlelod))))" +
                    ")")),case_base,o);
                f2 = NOOSParser.parse(new RewindableInputStream(new StringBufferInputStream(
                    "(define (car)" +
                    "  (lcont (define (set)" +
                    "    (define (load))" +
                    "    (define (circlelod))" +
                    "    (define (circlelod))))" +
                    ")")),case_base,o);
                expected_result = 1;
                break;

            case 14:
                f1 = NOOSParser.parse(new RewindableInputStream(new StringBufferInputStream(
                    "(define (trains-description) " +
                    "  (cars (define (set) " +
                    "          (define (car) " +
                    "           (infront (define ?X4 (car) " +
                    "                      (infront (define ?X5 (car)))))) " +
                    "          !X4 " +
                    "          !X5 " +
                    "          (define (car)))))")),case_base,o);
                f2 = NOOSParser.parse(new RewindableInputStream(new StringBufferInputStream(
                    "(define (trains-description) " +
                    "  (cars (define (set) " +
                    "          (define (car) " +
                    "           (infront (define ?X8 (car)))) " +
                    "          !X8)))")),case_base,o);
                expected_result = 1;
                break;
            case 15:
                f1 = NOOSParser.parse(new RewindableInputStream(new StringBufferInputStream(
                    "(define (trains-description) " +
                    "  (cars (define (set) " +
                    "          (define (car) " +
                    "           (infront (define ?X4 (car) " +
                    "                      (infront (define ?X5 (car) " +
                    "                                 (infront (define ?X6 (car)))))))) " +
                    "          !X4 " +
                    "          !X5 " +
                    "          !X6 " +
                    "          (define (car)))))")),case_base,o);
                f2 = NOOSParser.parse(new RewindableInputStream(new StringBufferInputStream(
                    "(define (trains-description) " +
                    "  (cars (define (set) " +
                    "          (define (car) " +
                    "           (infront (define ?X4 (car)))) " +
                    "          !X4 " +
                    "          (define (car) " +
                    "           (infront (define (car)))) " +
                    "          (define (car) " +
                    "           (infront (define ?X8 (car)))) " +
                    "          !X8)))")),case_base,o);
                expected_result = 1;
                break;
            default:
				f1 = null;
                f2 = null;
			break;
			}

			if (f1!=null) {
				System.out.println("F1:\n" + f1.toStringNOOS(dm));
				System.out.println("F2:\n" + f2.toStringNOOS(dm));

//				unifications=FTUnification.unification(f1,f2,o,dm, true);
				unifications=FTUnification.unification(f1,f2,dm);
				if (unifications!=null && !unifications.isEmpty()) {
					System.out.println("Unification successful!");
					System.out.println(unifications.size() + " unifications");
					for(FeatureTerm u:unifications) {
						System.out.println(u.toStringNOOS(dm));
						if (!f1.subsumes(u)) {
							System.out.println("ERROR! f1 does not subsume u!");
							errors++;
						}
						if (!f2.subsumes(u)) {
							System.out.println("ERROR! f2 does not subsume u!");
							errors++;
						}
					}
					if (expected_result!=unifications.size()) {
						errors++;
						System.out.println("ERROR! " + expected_result + " expected unifications!");
					}
				} else {
					System.out.println("0 unifications");
					if (expected_result!=0) {
						errors++;
						System.out.println("ERROR! " + expected_result + " expected unifications!");
					}
				} // if
			}
			i++;
		} while (f1!=null);

		return errors;
	}
}