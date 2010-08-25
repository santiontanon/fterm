/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fterms.argumentation;

import fterms.FTKBase;
import fterms.FTUnification;
import fterms.FeatureTerm;
import fterms.Ontology;
import fterms.exceptions.FeatureTermException;
import fterms.learning.Rule;

/**
 *
 * @author santi
 */
public class Attack {

    public static int DEBUG = 0;


    /*
     * An argument "attacker" (which as a whole is known to be accepted) attacks another argument "attackee", if:
     * - they support different solutions AND
     * - (attacker and attackee unify AND the unification is an acceptable argument) OR (attackee subsumes attacker)
    */
    public static boolean attacksAcceptedP(Argument attacker, Argument attackee, ArgumentAcceptability aa, Ontology o, FTKBase dm) throws FeatureTermException {
        if (!attacker.m_rule.solution.equivalents(attackee.m_rule.solution)) {
            if (attackee.m_rule.pattern.subsumes(attacker.m_rule.pattern)) {
                    if (DEBUG>=1) System.out.println("attacksAcceptedP: true (attacker subsumes by attackee)");
                return true;
            }
            FeatureTerm u = FTUnification.simpleUnification(attacker.m_rule.pattern, attackee.m_rule.pattern, dm);
            Argument a = new Argument(new Rule(u,attacker.m_rule.solution,0.0f,0));
            if (u!=null) {
                if (aa.accepted(a)) {
                    if (DEBUG>=1) System.out.println("attacksAcceptedP: true (unification is acceptable)");
                    return true;
                } else {
                    if (DEBUG>=1) {
                        System.out.println("attacksAcceptedP: false -> unification is not acceptable:");
                        System.out.println(a.toStringNOOS(dm));
                        int tmp = ArgumentAcceptability.DEBUG;
                        ArgumentAcceptability.DEBUG = 2;
                        aa.accepted(a);
                        ArgumentAcceptability.DEBUG = tmp;
                    }
                }
            } else {
                if (DEBUG>=1) System.out.println("attacksAcceptedP: false -> arguments do not unify ");
            }
        } else {
            if (DEBUG>=1) System.out.println("attacksAcceptedP: false -> same solutions " + attacker.m_rule.solution.toStringNOOS(dm) + " = " + attackee.m_rule.solution.toStringNOOS(dm));
        }
        return false;
    }

    public static boolean attacksP(Argument attacker, Argument attackee, ArgumentAcceptability aa, Ontology o, FTKBase dm) throws FeatureTermException {
        if (!attacker.m_rule.solution.equivalents(attackee.m_rule.solution)) {
            FeatureTerm u = FTUnification.simpleUnification(attacker.m_rule.pattern, attackee.m_rule.pattern, dm);
            Argument a = new Argument(new Rule(u,attacker.m_rule.solution,0.0f,0));
            if (u!=null) {
                if (aa.accepted(a)) {
                    if (DEBUG>=1) System.out.println("attacksP: true");
                    return true;
                } else {
                    if (DEBUG>=1) {
                        System.out.println("attacksP: false -> unification is not acceptable:");
                        System.out.println(a.toStringNOOS(dm));
                        int tmp = ArgumentAcceptability.DEBUG;
                        ArgumentAcceptability.DEBUG = 2;
                        aa.accepted(a);
                        ArgumentAcceptability.DEBUG = tmp;
                    }
                }
            } else {
                if (DEBUG>=1) System.out.println("attacksP: false -> arguments do not unify ");
            }
        } else {
            if (DEBUG>=1) System.out.println("attacksP: false -> same solutions " + attacker.m_rule.solution.toStringNOOS(dm) + " = " + attackee.m_rule.solution.toStringNOOS(dm));
        }
        return false;
    }

    
    public static boolean attacksSimple(Argument attacker, Argument attackee, Ontology o, FTKBase dm) throws FeatureTermException {
        if (!attacker.m_rule.solution.equivalents(attackee.m_rule.solution)) {
            if (attackee.m_rule.pattern.subsumes(attacker.m_rule.pattern)) {
                    if (DEBUG>=1) System.out.println("attacksSimple: true (attackee subsumes by attacker)");
                return true;
            }
            if (DEBUG>=1) System.out.println("attacksSimple: false -> attackee does not subsume attacker ");
        } else {
            if (DEBUG>=1) System.out.println("attacksSimple: false -> same solutions " + attacker.m_rule.solution.toStringNOOS(dm) + " = " + attackee.m_rule.solution.toStringNOOS(dm));
        }
        return false;
    }



}
