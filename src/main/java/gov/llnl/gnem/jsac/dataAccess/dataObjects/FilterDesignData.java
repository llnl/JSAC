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
package gov.llnl.gnem.jsac.dataAccess.dataObjects;

import com.oregondsp.signalProcessing.filter.iir.AnalogButterworth;
import com.oregondsp.signalProcessing.filter.iir.AnalogChebyshevI;
import com.oregondsp.signalProcessing.filter.iir.AnalogChebyshevII;
import com.oregondsp.signalProcessing.filter.iir.AnalogPrototype;
import com.oregondsp.signalProcessing.filter.iir.Complex;
import com.oregondsp.signalProcessing.filter.iir.IIRFilter;
import com.oregondsp.signalProcessing.filter.iir.PassbandType;

import gov.llnl.gnem.jsac.commands.filtering.ChebyshevParameters;
import llnl.gnem.core.signalprocessing.filter.FilterDesign;

public class FilterDesignData {

    private static final int DEFAULT_NUMBER_OF_FREQUENCY_SAMPLES = 2001;

    private static int numfreq = DEFAULT_NUMBER_OF_FREQUENCY_SAMPLES;

    private FilterType type;

    private float[] analogGroupDelay;
    private float[] analogSpectrumRealPart;
    private float[] analogSpectrumImagPart;
    private float[] digitalImpulseResponse;
    private float[] digitalGroupDelay;
    private float[] digitalSpectrumRealPart;
    private float[] digitalSpectrumImagPart;

    private PassbandType passbandType;
    private FilterDesign filterDesign;
    private int npoles;
    private int npasses;
    private double transitionBandwidth;
    private double attenuation;
    private double delta_t;
    private double f1;
    private double f2;

    private double delta_f;

    private boolean validData;
    private boolean twoPass;

    public static void setNumFreqSamples(int numfreq_) {
        numfreq = numfreq_;
    }

    public FilterDesignData(double[] predictor, double delta_t) {

        type = FilterType.PredictionError;

        this.delta_t = delta_t;

        analogGroupDelay = null;
        analogSpectrumRealPart = null;
        analogSpectrumImagPart = null;

        passbandType = null;
        filterDesign = null;

        digitalImpulseResponse = new float[predictor.length];
        for (int i = 0; i < predictor.length; i++) {
            digitalImpulseResponse[i] = (float) predictor[i];
        }

        delta_f = 1.0 / (delta_t * numfreq);

        digitalSpectrumRealPart = new float[numfreq];
        digitalSpectrumImagPart = new float[numfreq];
        digitalGroupDelay = new float[numfreq];

        // coupled form oscillator
        //
        //  e^(-j*omega*(n+1) ) = e^(-j*omega*n) * e^(-j*omega)  =  (c + j*s) * (dc + j*ds)
        //  = (c*dc - s*ds) + j*(c*ds + s*dc)

        double dOmega = Math.PI / (numfreq - 1);

        for (int k = 0; k <= numfreq / 2; k++) {
            double omega = dOmega * k;
            double c = 1.0;
            double s = 0.0;
            double dc = Math.cos(omega);
            double ds = -Math.sin(omega);
            double sr = 0.0;
            double si = 0.0;
            double dr = 0.0;
            double di = 0.0;
            for (int i = 0; i < predictor.length; i++) {
                sr += predictor[i] * c;
                si += predictor[i] * s;
                dr += predictor[i] * c * i;
                di += predictor[i] * s * i;
                double tmp = c * dc - s * ds;
                s = c * ds + s * dc;
                c = tmp;
            }
            digitalSpectrumRealPart[k] = (float) sr;
            digitalSpectrumImagPart[k] = (float) si;

            // (dr + j*di) / (sr + j*si)  =  (dr + j*di) * (sr - j*si) / ( sr*sr + si*si)
            // real part:  ( dr*sr + di*si ) / ( sr*sr + si*si )

            digitalGroupDelay[k] = (float) ((dr * sr + di * si) / (sr * sr + si * si));

        }

        validData = true;
        twoPass = false;

    }

    public FilterDesignData(PassbandType passbandType, FilterDesign filterDesign, int npoles, int npasses, double transitionBandwidth, double attenuation, double delta_t, double f1, double f2) {

        type = FilterType.IIR;

        this.passbandType = passbandType;
        this.filterDesign = filterDesign;
        this.npoles = npoles;
        this.npasses = npasses;
        this.transitionBandwidth = transitionBandwidth;
        this.attenuation = attenuation;
        this.delta_t = delta_t;
        this.f1 = f1;
        this.f2 = f2;

        if (npasses == 2) {
            twoPass = true;
        } else {
            twoPass = false;
        }
        delta_f = 1.0 / (2.0 * delta_t * (numfreq - 1));

        AnalogPrototype prototype = null;
        double[] cp;
        double epsilon;

        validData = true;

        switch (filterDesign) {
        case Butterworth:
            prototype = new AnalogButterworth(npoles);
            break;
        case Chebyshev1:
            cp = ChebyshevParameters.calculateEpsilon(attenuation, transitionBandwidth, npoles);
            epsilon = cp[0];
            prototype = new AnalogChebyshevI(npoles, epsilon);
            break;
        case Chebyshev2:
            epsilon = 1.0 / Math.sqrt(attenuation * attenuation - 1.0);
            prototype = new AnalogChebyshevII(npoles, epsilon);
            break;
        default:
            System.out.println("Filter type not implemented");
            validData = false;
            break;
        }

        if (validData) {

            AnalogPrototype transformedPrototype = null;

            switch (passbandType) {
            case LOWPASS:
                transformedPrototype = prototype.lptolp(2.0 * Math.PI * f2);
                break;
            case HIGHPASS:
                transformedPrototype = prototype.lptohp(2.0 * Math.PI * f1);
                break;
            case BANDPASS:
                transformedPrototype = prototype.lptobp(2.0 * Math.PI * f1, 2.0 * Math.PI * f2);
                break;
            case BANDREJECT:
                transformedPrototype = prototype.lptobr(2.0 * Math.PI * f1, 2.0 * Math.PI * f2);
                break;
            }

            IIRFilter digitalFilter = new IIRFilter(prototype, passbandType, f1, f2, delta_t);

            digitalSpectrumRealPart = new float[numfreq];
            digitalSpectrumImagPart = new float[numfreq];
            digitalGroupDelay = new float[numfreq];

            analogSpectrumRealPart = new float[numfreq];
            analogSpectrumImagPart = new float[numfreq];
            analogGroupDelay = new float[numfreq];

            for (int ifreq = 0; ifreq < numfreq; ifreq++) {

                double OmegaA = 2.0 * Math.PI * ifreq * delta_f;
                double OmegaD = Math.PI / (numfreq - 1) * ifreq;

                Complex H = transformedPrototype.evaluate(OmegaA);
                analogSpectrumRealPart[ifreq] = (float) H.real();
                analogSpectrumImagPart[ifreq] = (float) H.imag();
                analogGroupDelay[ifreq] = (float) transformedPrototype.groupDelay(OmegaA);

                H = digitalFilter.evaluate(OmegaD);
                digitalSpectrumRealPart[ifreq] = (float) H.real();
                digitalSpectrumImagPart[ifreq] = (float) H.imag();
                digitalGroupDelay[ifreq] = (float) digitalFilter.groupDelay(OmegaD);

                if (twoPass) {
                    digitalSpectrumRealPart[ifreq] = digitalSpectrumRealPart[ifreq] * digitalSpectrumRealPart[ifreq] + digitalSpectrumImagPart[ifreq] * digitalSpectrumImagPart[ifreq];
                    digitalGroupDelay[ifreq] = 0.0f;
                }

            }

            digitalImpulseResponse = new float[numfreq / 2];

            digitalImpulseResponse[numfreq / 20] = 1.0f;

            digitalFilter.filter(digitalImpulseResponse);

        }

    }

    public boolean isValid() {
        return validData;
    }

    public FilterType getType() {
        return type;
    }

    public float[] getAnalogSpectrumRealPart() {
        return analogSpectrumRealPart.clone();
    }

    public float[] getAnalogSpectrumImagPart() {
        return analogSpectrumImagPart.clone();
    }

    public float[] getAnalogGroupDelay() {
        return analogGroupDelay;
    }

    public float[] getDigitalImpulseResponse() {
        return digitalImpulseResponse.clone();
    }

    public float[] getDigitalSpectrumRealPart() {
        return digitalSpectrumRealPart.clone();
    }

    public float[] getDigitalSpectrumImagPart() {
        return digitalSpectrumImagPart.clone();
    }

    public float[] getDigitalGroupDelay() {
        return digitalGroupDelay.clone();
    }

    public double getDeltaF() {
        return delta_f;
    }

    public double[] getCutoffs() {
        double[] retval = new double[2];
        retval[0] = f1;
        retval[1] = f2;

        return retval;
    }

    public double getDeltaT() {
        return delta_t;
    }

    public PassbandType getPassbandType() {
        return passbandType;
    }

    public FilterDesign getFilterDesign() {
        return filterDesign;
    }

    public int getNPoles() {
        return npoles;
    }

    public int getNPasses() {
        return npasses;
    }

    public double getTransitionBandwidth() {
        return transitionBandwidth;
    }

    public double getAttenuation() {
        return attenuation;
    }

}
