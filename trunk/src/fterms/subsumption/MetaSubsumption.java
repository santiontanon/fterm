/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fterms.subsumption;

import fterms.FTKBase;
import fterms.FeatureTerm;
import fterms.SetFeatureTerm;
import fterms.exceptions.FeatureTermException;
import fterms.exceptions.SubsumptionTimeOutException;

/**
 *
 * @author santi
 */
public class MetaSubsumption {

    // give half a second to regular subsumption, and otherwise, switch to CSP subsumption:
    public static boolean subsumes(FeatureTerm f1, FeatureTerm f2, FTKBase dm) throws FeatureTermException {
        try {
            if (f1 instanceof SetFeatureTerm || f2 instanceof SetFeatureTerm) {
                return FTSubsumption.subsumes(f1,f2);
            } else {
                return FTSubsumption.subsumes(f1, f2, 200);
            }
        }catch(SubsumptionTimeOutException e) {
            System.out.println("Timed out!");
            return CSPSubsumptionSymmetry.subsumes(f1,f2,dm);
        }
    }

    

    // give half a second to regular subsumption, and otherwise, switch to CSP subsumption:
    public static boolean subsumes(FeatureTerm f1, FeatureTerm f2) throws FeatureTermException {
        try {
            if (f1 instanceof SetFeatureTerm || f2 instanceof SetFeatureTerm) {
                return FTSubsumption.subsumes(f1,f2);
            } else {
                return FTSubsumption.subsumes(f1, f2, 200);
            }
        }catch(SubsumptionTimeOutException e) {
            System.out.println("Timed out!");
            return CSPSubsumptionSymmetry.subsumes(f1,f2);
        }
    }    
}
