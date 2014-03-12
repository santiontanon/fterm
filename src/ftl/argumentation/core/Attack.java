/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Copyright (c) 2013, Santiago Ontañón All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution. Neither the name of
 * the IIIA-CSIC nor the names of its contributors may be used to endorse or promote products derived from this software
 * without specific prior written permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
  
 package ftl.argumentation.core;

import ftl.base.core.FTKBase;
import ftl.base.core.FTUnification;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Ontology;
import ftl.base.utils.FeatureTermException;
import ftl.learning.core.Rule;

// TODO: Auto-generated Javadoc
/**
 * The Class Attack.
 *
 * @author santi
 */
public class Attack {

    /** The DEBUG. */
    public static int DEBUG = 0;

    /**
     * Attacks accepted p.
     * 
     * An argument "attacker" (which as a whole is known to be accepted) attacks another argument "attackee", if:
     * - they support different solutions AND
     * - (attacker and attackee unify AND the unification is an acceptable argument) OR (attackee subsumes attacker)
     *
     * @param attacker the attacker
     * @param attackee the attackee
     * @param aa the aa
     * @param o the o
     * @param dm the dm
     * @return true, if successful
     * @throws FeatureTermException the feature term exception
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

    /**
     * Attacks p.
     *
     * @param attacker the attacker
     * @param attackee the attackee
     * @param aa the aa
     * @param o the o
     * @param dm the dm
     * @return true, if successful
     * @throws FeatureTermException the feature term exception
     */
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

    
    /**
     * Attacks simple.
     *
     * @param attacker the attacker
     * @param attackee the attackee
     * @param o the o
     * @param dm the dm
     * @return true, if successful
     * @throws FeatureTermException the feature term exception
     */
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
