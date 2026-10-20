/*-
 * #%L
 * Java Seismic Analysis Code (JSAC)
 *  LLNL-CODE-855505
 *  This work was performed under the auspices of the U.S. Department of Energy
 *  by Lawrence Livermore National Laboratory under Contract DE-AC52-07NA27344.
 * %%
 * Copyright (C) 2022 - 2023 Lawrence Livermore National Laboratory
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package gov.llnl.gnem.jsac.commands.filtering;

/*
*  Author:  Dave Harris
*           Deschutes Signal Processing LLC
*           (541) 993-7665
*
*
*  CHEBPARM - Calculates Chebyshev type I and II design parameters
*
*
*  INPUT ARGUMENTS
*  ---------------
*
*       attenuation      double   Desired stopband attenuation
*                                     i.e. max stopband amplitude is 1/attenuation
*
*       transitionBW     double   Transition bandwidth between stop and passbands
*                                     as a fraction of the passband width
*
*       order            int      Filter order (number of poles and degree of Chebyshev polynomial)
*
*
*  RETURNS
*  ----------------
*
*      double[]          retval[0]   the Chebyshev design parameter
*                        retval[1]   the minimum passband gain (nominal 1.0)
*
* */

public class ChebyshevParameters {

    public static double[] calculateEpsilon(double attenuation, double transitionBW, int order) {

        double omegaR = 1.0 + transitionBW;
        double alpha = Math.pow(omegaR + Math.sqrt(omegaR * omegaR - 1.), order);
        double g = (alpha * alpha + 1.0) / (2.0 * alpha);

        double[] retval = new double[2];
        double eps = Math.sqrt(attenuation * attenuation - 1.0) / g;
        retval[0] = eps;
        retval[1] = 1.0 / Math.sqrt(1.0 + eps * eps);

        return retval;
    }

    public static double[] requiredOrder(double attenuation, double transitionBW) {

        double omegaR = 1.0 + transitionBW;
        double eps = 1.0 / Math.sqrt(attenuation * attenuation - 1.0);
        double r = 1.0 - 1.0 / Math.sqrt(1 + eps * eps);
        double g = Math.sqrt((attenuation * attenuation - 1) / (eps * eps));
        double n = Math.log10(g + Math.sqrt(g * g - 1.0)) / Math.log10(omegaR + Math.sqrt(omegaR * omegaR - 1.0));

        double[] retval = new double[3];
        retval[0] = n;
        retval[1] = r;
        retval[2] = eps;

        return retval;
    }

    public static double recurrence(double x, int order) {

        double[] T = new double[order + 1];

        T[0] = 1.0;
        T[1] = x;
        for (int i = 2; i <= order; i++) {
            T[i] = 2.0 * x * T[i - 1] - T[i - 2];
        }

        return T[order];
    }

    public static double c1(int order, double epsilon, double omega) {

        double T = recurrence(omega, order);
        double retval = 1.0 / (1.0 + epsilon * epsilon * T * T);

        return Math.sqrt(retval);
    }

    public static double c2(int order, double epsilon, double omegaR, double omega) {

        double TnOr = recurrence(omegaR, order);
        double TnOs = recurrence(omegaR / omega, order);

        double ratio = TnOr / TnOs;
        double retval = 1.0 / (1.0 + epsilon * epsilon * ratio * ratio);

        return Math.sqrt(retval);
    }

}
