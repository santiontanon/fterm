/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fterms.subsumption;

import fterms.FeatureTerm;
import fterms.exceptions.FeatureTermException;
import fterms.exceptions.SubsumptionTimeOutException;

/**
 *
 * @author santi
 */
public class MetaSubsumption {

    // give half a second to regular subsumption, and otherwise, switch to CSP subsumption:
    public static boolean subsumes(FeatureTerm f1, FeatureTerm f2) throws FeatureTermException {
        try {
            return FTSubsumption.subsumes(f1, f2, 500);
        }catch(SubsumptionTimeOutException e) {
            return CSPSubsumption.subsumes(f1,f2);
        }
    }

}
