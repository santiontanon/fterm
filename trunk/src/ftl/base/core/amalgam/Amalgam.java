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
  
 package ftl.base.core.amalgam;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import ftl.base.core.FTAntiunification;
import ftl.base.core.FTKBase;
import ftl.base.core.FTRefinement;
import ftl.base.core.FTUnification;
import ftl.base.core.FeatureTerm;
import ftl.base.core.Ontology;
import ftl.base.utils.FeatureTermException;

/**
 *
 * @author santi
 */
public class Amalgam {
        
    public static int DEBUG = 0;
    
      /*
     * This function uses a simple greedy search algorithm to reach an amalgam that maximizes a given criterion
     */
    public static List<AmalgamResult> amalgamRefinementsGreedy(FeatureTerm f1, FeatureTerm f2, AmalgamEvaluationFunction ef, Ontology o, FTKBase dm, boolean systematicUnification) throws FeatureTermException {
        return amalgamRefinementsGreedy(f1, f2, null, null, ef, o, dm, systematicUnification, FTAntiunification.VERSION_FAST);
    }

    
    /* This is the same method, but ensuring that we never generalize t1 beyond pattern1, or t2 beyond pattern2: */
    public static List<AmalgamResult> amalgamRefinementsGreedy(FeatureTerm f1, FeatureTerm f2, FeatureTerm pattern1, FeatureTerm pattern2, AmalgamEvaluationFunction ef, Ontology o, FTKBase dm, boolean systematicUnification, int AUType) throws FeatureTermException {
        List<FeatureTerm> l1 = new LinkedList<FeatureTerm>();
        List<FeatureTerm> l2 = new LinkedList<FeatureTerm>();
        l1.add(f1);
        l2.add(f2);
        int explored = 0;
       
        // Initial results:
        List<FeatureTerm> baseAmalgams;
        {
            List<FeatureTerm> tmp = new LinkedList<FeatureTerm>();
            tmp.add(f1);
            tmp.add(f2);
            baseAmalgams = FTAntiunification.antiunification(tmp, FTRefinement.ALL_REFINEMENTS, null, o, dm, true, AUType);
        }
        
        FeatureTerm transfer1 = null;
        FeatureTerm transfer2 = null;
        List<AmalgamResult> results = new LinkedList<AmalgamResult>();       
        double bestResultsScore = 0;
        List<AmalgamResult> bestResults = new LinkedList<AmalgamResult>();
        AmalgamResult best = null;
        
        // filter among the set of base results:
        {
            List<AmalgamResult> nextResults = new LinkedList<AmalgamResult>();
            for(FeatureTerm amalgam:baseAmalgams) {
                nextResults.add(new AmalgamResult(amalgam, ef.evaluate(amalgam, amalgam, amalgam, dm, o), amalgam, amalgam));
                explored++;
                if (DEBUG>=1) {
                    System.out.println("amalgamRefinementsGreedy: base result");
                    System.out.println(amalgam.toStringNOOS(dm));
                }
            }
            best = null;
            for(AmalgamResult n:nextResults) {
                if (best==null || n.getEvaluation()>best.getEvaluation()) best = n;
            }
            if (best!=null) {
                if (DEBUG>=1) System.out.println("amalgamRefinementsGreedy: best in this iteration " + best.getEvaluation() + "  (explored so far " + explored + ")");

                transfer1 = best.getTransfer1();
                transfer2 = best.getTransfer2();
                results.clear();
                if (bestResults.isEmpty() || best.getEvaluation()>bestResultsScore) {
                    bestResults.clear();
                    bestResultsScore = best.getEvaluation()-1;
                }
                for(AmalgamResult n:nextResults) {
                    if (n.getEvaluation()>=best.getEvaluation()) results.add(n);
                    if (n.getEvaluation()>bestResultsScore) bestResults.add(n);
                }
                bestResultsScore = Math.max(bestResultsScore,best.getEvaluation());
            }      
        }
        
        do {
            List<FeatureTerm> nextTransfers1 = FTRefinement.getSpecializationsSubsumingAll(transfer1, dm, o, FTRefinement.ALL_REFINEMENTS, l1);
            List<FeatureTerm> nextTransfers2 = FTRefinement.getSpecializationsSubsumingAll(transfer2, dm, o, FTRefinement.ALL_REFINEMENTS, l2);
            List<AmalgamResult> nextResults = new LinkedList<AmalgamResult>();
            if (DEBUG>=1) System.out.println("amalgamRefinementsGreedy: nextTransfers1 " + nextTransfers1.size());
            for(FeatureTerm nextTransfer1:nextTransfers1) {
                if (DEBUG>=2) System.out.println("amalgamRefinementsGreedy: transfer1 " + nextTransfers1.indexOf(nextTransfer1) + "/" + nextTransfers1.size());
                if (systematicUnification) {
                    List<FeatureTerm> tmpl = FTUnification.unification(nextTransfer1, transfer2, dm);
                    for(FeatureTerm tmp:tmpl) {
                        explored++;
                        nextResults.add(new AmalgamResult(tmp,ef.evaluate(tmp, nextTransfer1, transfer2, dm, o), nextTransfer1, transfer2));
                    }
                } else {
                    FeatureTerm tmp = FTUnification.simpleUnification(nextTransfer1, transfer2, dm);
                    explored++;
                    nextResults.add(new AmalgamResult(tmp,ef.evaluate(tmp, nextTransfer1, transfer2, dm, o), nextTransfer1, transfer2));
                }
            }
            if (DEBUG>=1) System.out.println("amalgamRefinementsGreedy: nextTransfers2 " + nextTransfers2.size());
            for(FeatureTerm nextTransfer2:nextTransfers2) {
                if (DEBUG>=2) System.out.println("amalgamRefinementsGreedy: transfer2 " + nextTransfers2.indexOf(nextTransfer2) + "/" + nextTransfers2.size());
                if (systematicUnification) {
                    List<FeatureTerm> tmpl = FTUnification.unification(transfer1, nextTransfer2, dm);
                    for(FeatureTerm tmp:tmpl) {
                        explored++;
                        nextResults.add(new AmalgamResult(tmp,ef.evaluate(tmp, transfer1, nextTransfer2, dm, o), transfer1, nextTransfer2));
                    }
                } else {
                    FeatureTerm tmp = FTUnification.simpleUnification(transfer1, nextTransfer2, dm);
                    explored++;
                    nextResults.add(new AmalgamResult(tmp,ef.evaluate(tmp, transfer1, nextTransfer2, dm, o), transfer1, nextTransfer2));
                }
            }

            best = null;
            for(AmalgamResult n:nextResults) {
                if (best==null || n.evaluation>best.evaluation) best = n;
            }
            if (best!=null) {
                if (DEBUG>=1) System.out.println("amalgamRefinementsGreedy: best in this iteration " + best.evaluation + "  (explored so far " + explored + ")");
                
                transfer1 = best.transfer1;
                transfer2 = best.transfer2;
                results.clear();
                if (bestResults.isEmpty() || best.evaluation>bestResultsScore) {
                    bestResults.clear();
                    bestResultsScore = best.evaluation-1;
                }
                for(AmalgamResult n:nextResults) {
                    if (n.evaluation>=best.evaluation) results.add(n);
                    if (n.evaluation>bestResultsScore) bestResults.add(n);
                }
                bestResultsScore = Math.max(bestResultsScore,best.evaluation);
            }
            
        }while(best!=null);
        
        // filter duplicates:
        List<FeatureTerm> l = new LinkedList<FeatureTerm>();
        List<AmalgamResult> toDelete = new LinkedList<AmalgamResult>();
        for(AmalgamResult r:bestResults) {
            for(FeatureTerm a:l) {
                if (a.equivalents(r.amalgam)) {
                    toDelete.add(r);
                    break;
                }
            }
            l.add(r.amalgam);
        }
        bestResults.removeAll(toDelete);
        
        return bestResults;
    }    
    

    
    public static List<AmalgamResult> assymetricAmalgamRefinementsGreedy(FeatureTerm source, FeatureTerm target, AmalgamEvaluationFunction ef, Ontology o, FTKBase dm) throws FeatureTermException {
        List<FeatureTerm> l1 = new LinkedList<FeatureTerm>();
        List<FeatureTerm> l2 = new LinkedList<FeatureTerm>();
        l1.add(source);
        l2.add(target);
        int explored = 0;
        
        FeatureTerm au = FTAntiunification.simpleAntiunification(source, target, o, dm);
        if (DEBUG>=1) System.out.println("assymetricAmalgamRefinementsGreedy: antiunification\n" + au.toStringNOOS(dm));
            
        // Initial results:
        List<FeatureTerm> baseAmalgams = new LinkedList<FeatureTerm>();
//        baseAmalgams.addAll(FTUnification.unification(LUG1P.m_a, LUG2P.m_a, dm));
        baseAmalgams.add(target);
        List<AmalgamResult> results = new LinkedList<AmalgamResult>();
        for(FeatureTerm amalgam:baseAmalgams) {
            results.add(new AmalgamResult(amalgam,ef.evaluate(amalgam, au, target, dm, o), au, target));
            if (DEBUG>=1) System.out.println("assymetricAmalgamRefinementsGreedy: base result");
            if (DEBUG>=1) System.out.println(amalgam.toStringNOOS(dm));
        }
        
        // perform greedy search:
//        FeatureTerm transfer1 = LUG1P.m_a;
//        FeatureTerm transfer2 = LUG2P.m_a;
        FeatureTerm transfer1 = au;
        FeatureTerm transfer2 = target;
        double bestResultsScore = 0;
        List<AmalgamResult> bestResults = new LinkedList<AmalgamResult>();
        AmalgamResult best = null;
        do {
            List<FeatureTerm> nextTransfers1 = FTRefinement.getSpecializationsSubsumingAll(transfer1, dm, o, FTRefinement.ALL_REFINEMENTS, l1);
            List<AmalgamResult> nextResults = new LinkedList<AmalgamResult>();
            for(FeatureTerm nextTransfer1:nextTransfers1) {
                if (DEBUG>=1) System.out.println("assymetricAmalgamRefinementsGreedy: testing transfer " + nextTransfers1.indexOf(nextTransfer1) + "/" + nextTransfers1.size());
                List<FeatureTerm> tmpl = FTUnification.unification(nextTransfer1, transfer2, dm);
                for(FeatureTerm tmp:tmpl) {
                    explored++;
                    nextResults.add(new AmalgamResult(tmp,ef.evaluate(tmp, nextTransfer1, transfer2, dm, o), nextTransfer1, transfer2));
                }
            }
            best = null;
            for(AmalgamResult n:nextResults) {
                if (best==null || n.evaluation>best.evaluation) best = n;
            }
            if (best!=null) {
                if (DEBUG>=1) System.out.println("assymetricAmalgamRefinementsGreedy: best in this iteration " + best.evaluation + "  (explored so far " + explored + ")");
                
                transfer1 = best.transfer1;
                transfer2 = best.transfer2;
                results.clear();
                if (bestResults.isEmpty() || best.evaluation>bestResultsScore) {
                    bestResults.clear();
                    bestResultsScore = best.evaluation-1;
                }
                for(AmalgamResult n:nextResults) {
                    if (n.evaluation>=best.evaluation) results.add(n);
                    if (n.evaluation>bestResultsScore) bestResults.add(n);
                }
                bestResultsScore = Math.max(bestResultsScore,best.evaluation);
            }
        }while(best!=null);
        
        return bestResults;
    }    



    public static List<AmalgamResult> amalgamRefinementsGreedy(List<FeatureTerm> lf, AmalgamEvaluationFunction ef, Ontology o, FTKBase dm, boolean systematicUnification) throws FeatureTermException {
        List<AmalgamResult> results = new LinkedList<AmalgamResult>();
        
        if (lf.size()==1) {
            results.add(new AmalgamResult(lf.get(0),ef.evaluate(lf.get(0), lf.get(0), lf.get(0), dm, o), lf.get(0), lf.get(0)));
            return results;
        }
        
        FeatureTerm f1 = lf.get(0);
        FeatureTerm f2 = lf.get(1);
        results = amalgamRefinementsGreedy(f1,f2,ef,o,dm, systematicUnification);
        for(int i = 0;i<lf.size()-2;i++) {
            AmalgamResult best = null;
            List<AmalgamResult> nextResults = new LinkedList<AmalgamResult>();
            for(AmalgamResult result:results) {
                List<AmalgamResult> tmpResults = amalgamRefinementsGreedy(result.amalgam,lf.get(i+2),ef,o,dm, systematicUnification);
                for(AmalgamResult result2:tmpResults) {
                    if (best==null || result2.evaluation>best.evaluation) best = result2;
                }
                nextResults.addAll(tmpResults);
            }
            List<AmalgamResult> toDelete = new LinkedList<AmalgamResult>();
            for(AmalgamResult result:nextResults) {
                if (result.evaluation<best.evaluation) toDelete.add(result);
            }
            nextResults.removeAll(toDelete);
            results = nextResults;
        }
        return results;
    }

    
    public static List<AmalgamResult> assymetricAmalgamRefinementsGreedy(List<FeatureTerm> lf, FeatureTerm target, AmalgamEvaluationFunction ef, Ontology o, FTKBase dm, boolean systematicUnification) throws FeatureTermException {
        List<AmalgamResult> leftAmalgams = amalgamRefinementsGreedy(lf,ef,o, dm, systematicUnification);
        List<AmalgamResult> amalgams = new LinkedList<AmalgamResult>();
        
        for(AmalgamResult leftAmalgam:leftAmalgams) {
            amalgams.addAll(assymetricAmalgamRefinementsGreedy(leftAmalgam.amalgam, target, ef, o, dm));
        }        
        
        AmalgamResult best = null;
        for(AmalgamResult n:amalgams) {
            if (best==null || n.evaluation>best.evaluation) best = n;
        }
        if (best!=null) {
            List<AmalgamResult> toDelete = new LinkedList<AmalgamResult>();
            for(AmalgamResult n:amalgams) {
                if (n.evaluation<best.evaluation) toDelete.add(n);
            }
            amalgams.removeAll(toDelete);
        }        
        return amalgams;
    }
}
