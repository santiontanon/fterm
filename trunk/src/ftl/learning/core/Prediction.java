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
package ftl.learning.core;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import ftl.base.core.FTKBase;
import ftl.base.core.FeatureTerm;

// TODO: Auto-generated Javadoc
/**
 * The Class Prediction.
 */
public class Prediction {

    /**
     * The problem.
     */
    public FeatureTerm problem = null;

    /**
     * The solutions.
     */
    public List<FeatureTerm> solutions = new LinkedList<FeatureTerm>();

    /**
     * The justifications.
     */
    public HashMap<FeatureTerm, FeatureTerm> justifications = new HashMap<FeatureTerm, FeatureTerm>();

    /**
     * The support.
     */
    public HashMap<FeatureTerm, Integer> support = new HashMap<FeatureTerm, Integer>();

    /**
     * Instantiates a new prediction.
     */
    public Prediction() {

    }

    /**
     * Instantiates a new prediction.
     *
     * @param p the p
     */
    public Prediction(FeatureTerm p) {
        problem = p;
    }

    /**
     * To string.
     *
     * @param dm the dm
     * @return the string
     */
    public String toString(FTKBase dm) {
        String tmp;

        tmp = ("Prediction: number of possible solutions (" + solutions.size() + "): ----------------------\n");
        for (FeatureTerm solution : solutions) {
            FeatureTerm justification = justifications.get(solution);
            tmp += "- Justification for " + solution.toStringNOOS(dm) + "(support:" + support.get(solution) + ")\n";
            tmp += (justification != null ? justification.toStringNOOS(dm) : "-") + "\n";
        } // while

        return tmp;
    }

    /**
     * Gets the score.
     *
     * @param realSolution the real solution
     * @return the score
     */
    public float getScore(FeatureTerm realSolution) {
        float total = 0, correct = 0;

        if (support.get(realSolution) != null) {
            correct += support.get(realSolution);
        }
        for (FeatureTerm solution : solutions) {
            if (support.get(solution) != null) {
                total += support.get(solution);
            }
        }

        if (total > 0) {
            return correct / total;
        }
        return 1.0f / (float) solutions.size();
    }

    /**
     * Gets the solution.
     *
     * @return the solution
     */
    public FeatureTerm getSolution() {
        HashMap<FeatureTerm, Integer> votes = new HashMap<FeatureTerm, Integer>();

        for (FeatureTerm solution : solutions) {
            if (votes.get(solution) == null) {
                votes.put(solution, support.get(solution));
            } else {
                votes.put(solution, votes.get(solution) + support.get(solution));
            }
        }

        {
            FeatureTerm max = null;
            int max_votes = 0;

            for (FeatureTerm solution : votes.keySet()) {
                if (max == null || votes.get(solution) > max_votes) {
                    max = solution;
                    max_votes = votes.get(solution);
                }
            }

            return max;
        }
    }
}
