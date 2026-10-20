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

import java.text.DecimalFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.llnl.gnem.jsac.dataAccess.dataObjects.SacTraceData;

public class Whitener {

    private static final Logger log = LoggerFactory.getLogger(Whitener.class);

    private int N;
    private final double[] reflectionCoefficients;

    public Whitener(int N) {
        this.N = N;
        reflectionCoefficients = new double[N];
    }

    public double[] correlate(SacTraceData std) {

        double[] retval = new double[N + 1];
        float[] x = std.getData();
        int n = x.length;

        for (int lag = 0; lag <= N; lag++) {

            double tmp = 0.0;
            for (int i = 0; i < n - lag; i++) {
                tmp += x[i] * x[i + lag];
            }

            retval[lag] = tmp;
        }

        return retval;
    }

    public double[] whiten(SacTraceData std, boolean common) {

        double[] retval = correlate(std);

        if (!common) {
            applyPredictor(std, generatePredictor(retval, reflectionCoefficients));
        }

        return retval;
    }

    public double[] getReflectionCoefficients() {
        return reflectionCoefficients;
    }

    public static void applyPredictor(SacTraceData std, double[] predictor) {

        int n = predictor.length - 1;
        float[] x = std.getData();
        int m = x.length - n;
        float[] y = new float[m];

        for (int i = 0; i < m; i++) {
            double tmp = x[i + n];
            for (int k = 1; k <= n; k++) {
                tmp += predictor[k] * x[i + n - k];
            }
            y[i] = (float) tmp;
        }

        std.setData(y);

    }

    public static double[] generatePredictor(double[] correlationFunction, double[] reflect) {

        int n = correlationFunction.length;
        double[] predictor = new double[n];

        levinsonsRecursion(correlationFunction, predictor, reflect, n);

        DecimalFormat F = new DecimalFormat("0.00000");
        log.trace("reflection coefficients: ");
        for (double element : reflect) {
            log.trace(F.format(element));
        }

        return predictor;
    }

    /*                                                            LEVIN
     *   Method to solve Durbin's problem - Toeplitz
     *   normal equations, right hand vector of autocorrelations
     *
     *
     *  Input Arguments:
     *  ----------------
     *
     *    R                            Vector of autocorrelations
     *
     *    N                            Number of autocorrelations
     *                                 also the filter length (includes
     *                                 the first coefficient, which is
     *                                 always one).  Number of zeros is N-1.
     *
     *  Output Arguments:
     *  -----------------
     *
     *    A                            Vector of filter coefficients
     *    REFLCT                       Vector of reflection coefficients
     *                                 there are n-1 of these
     *
     *
     * */
    public static void levinsonsRecursion(double[] R, double[] A, double[] REFLECT, int N) {

        int idx, im1, jdx;
        double rhoDenom, rhoNum, rho;
        double[] tmp = new double[N + 1];

        // | R[0]  R[1]  ...  R[N-1] |  |  A[1]  |        | R[1] |
        // | R[1]  R[0]  ...  R[N-2] |  |  A[2]  |        | R[2] |
        // |  .     .           .    |  |   .    |   =  - |   .  |
        // |  .     .           .    |  |   .    |        |   .  |
        // |  .     .           .    |  |   .    |        |   .  |
        // | R[N] R[N-1] ...   R[0]  |  | A[N-1] |        | R[N] |
        //  Initialize first two coefficients
        A[0] = 1.;
        A[1] = -R[1] / R[0];
        REFLECT[0] = A[1];

        /*  Using Levinson's recursion, determine the rest of the coefficients.
         *  It is assumed that the filter is of length N, including the lead
         *  coefficient which is always one.
         * */
        if (N >= 3) {

            for (idx = 3; idx <= N; idx++) {

                im1 = idx - 1;
                rhoNum = R[idx - 1];
                rhoDenom = R[0];

                for (jdx = 2; jdx <= im1; jdx++) {
                    rhoNum += A[jdx - 1] * R[idx - jdx];
                    rhoDenom += A[jdx - 1] * R[jdx - 1];
                }

                rho = -rhoNum / rhoDenom;
                REFLECT[im1 - 1] = rho;

                for (jdx = 2; jdx <= im1; jdx++) {
                    tmp[jdx - 1] = A[jdx - 1] + rho * (A[idx - jdx]);
                }

                for (jdx = 2; jdx <= im1; jdx++) {
                    A[jdx - 1] = tmp[jdx - 1];
                }

                A[idx - 1] = rho;

            }
        }
    }

}
