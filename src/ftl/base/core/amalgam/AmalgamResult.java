/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * Copyright (c) 2013, Santiago Ontañón All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. Neither the name of the IIIA-CSIC nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission. THIS SOFTWARE IS PROVIDED
 * BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ftl.base.core.amalgam;

import ftl.base.core.FeatureTerm;

/**
 *
 * @author santi
 */
public class AmalgamResult {

    public AmalgamResult(FeatureTerm a, double e, FeatureTerm t1, FeatureTerm t2) {
        amalgam = a;
        evaluation = e;
        transfer1 = t1;
        transfer2 = t2;
    }

    FeatureTerm amalgam;
    double evaluation;
    FeatureTerm transfer1;
    FeatureTerm transfer2;

    /**
     * @return the amalgam
     */
    public FeatureTerm getAmalgam() {
        return amalgam;
    }

    /**
     * @param amalgam the amalgam to set
     */
    public void setAmalgam(FeatureTerm amalgam) {
        this.amalgam = amalgam;
    }

    /**
     * @return the evaluation
     */
    public double getEvaluation() {
        return evaluation;
    }

    /**
     * @param evaluation the evaluation to set
     */
    public void setEvaluation(int evaluation) {
        this.evaluation = evaluation;
    }

    /**
     * @return the transfer1
     */
    public FeatureTerm getTransfer1() {
        return transfer1;
    }

    /**
     * @param transfer1 the transfer1 to set
     */
    public void setTransfer1(FeatureTerm transfer1) {
        this.transfer1 = transfer1;
    }

    /**
     * @return the transfer2
     */
    public FeatureTerm getTransfer2() {
        return transfer2;
    }

    /**
     * @param transfer2 the transfer2 to set
     */
    public void setTransfer2(FeatureTerm transfer2) {
        this.transfer2 = transfer2;
    }

}
