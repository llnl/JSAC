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

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.apache.commons.math3.complex.Complex;

public class SpectralData {

    private Complex[] frequencyDomainData;
    private int blockStartIndex;
    private int blockEndIndex;
    private final double delfreq;
    private final PresentationFormat presentationFormat;
    private final int originalNsamps;
    private final int maxAllowableIndex;

    public SpectralData(Complex[] frequencyDomainData, double delfreq, PresentationFormat presentationFormat, int originalNsamps) {
        this.frequencyDomainData = frequencyDomainData;
        this.delfreq = delfreq;
        this.presentationFormat = presentationFormat;
        this.originalNsamps = originalNsamps;
        blockStartIndex = 1;//Don't use zero frequency and negative frequencies for plotting.
        blockEndIndex = frequencyDomainData.length / 2;
        maxAllowableIndex = blockEndIndex;
    }

    public SpectralData(SpectralData old) {
        frequencyDomainData = old.frequencyDomainData.clone();
        delfreq = old.delfreq;
        presentationFormat = old.presentationFormat;
        originalNsamps = old.originalNsamps;
        blockStartIndex = old.blockStartIndex;
        blockEndIndex = old.blockEndIndex;
        maxAllowableIndex = old.maxAllowableIndex;
    }

    public Complex[] getFrequencyDomainData() {
        return frequencyDomainData;
    }

    public double getDelfreq() {
        return delfreq;
    }

    public PresentationFormat getPresentationFormat() {
        return presentationFormat;
    }

    public int getOriginalNsamps() {
        return originalNsamps;
    }

    public void writeToTextFile(String filename) throws FileNotFoundException {
        try (PrintWriter pw = new PrintWriter(filename)) {
            for (int j = 0; j < frequencyDomainData.length; ++j) {
                pw.println(j * delfreq + "  " + frequencyDomainData[j].abs());
            }
        }
    }

    public Double getMinFreq() {
        return 0.0;
    }

    public Double getMaxPositiveFreq() {
        int npts = frequencyDomainData.length / 2; //Drop zero frequency and negative frequencies.
        return npts * delfreq;
    }

    public Integer getSize() {
        return frequencyDomainData.length;
    }

    public void divOmega() {
        //SAC divOmega zeroes out the zero frequency
        if (frequencyDomainData != null && frequencyDomainData.length > 0) {
            frequencyDomainData[0] = new Complex(0.0, 0.0);
            int nfreqs = frequencyDomainData.length / 2;
            int k = 0;
            for (int j = 1; j <= nfreqs - 1; ++j) {
                double omega = 2.0 * Math.PI * j * delfreq;
                Complex c = new Complex(0.0, omega);
                frequencyDomainData[j] = frequencyDomainData[j].divide(c);
                k = frequencyDomainData.length - j;
                frequencyDomainData[k] = new Complex(frequencyDomainData[j].getReal(), -1.0 * frequencyDomainData[j].getImaginary());
            }
            double omega = 2.0 * Math.PI * nfreqs * delfreq;
            Complex c = new Complex(0.0, omega);
            frequencyDomainData[nfreqs] = frequencyDomainData[nfreqs].divide(c);
        }
    }

    public void mulOmega() {
        //SAC mulOmega zeroes out the zero frequency
        if (frequencyDomainData != null && frequencyDomainData.length > 0) {
            frequencyDomainData[0] = new Complex(0.0, 0.0);
            int nfreqs = frequencyDomainData.length / 2;
            int k = 0;
            for (int j = 1; j <= nfreqs - 1; ++j) {
                double omega = 2.0 * Math.PI * j * delfreq;
                Complex c = new Complex(0.0, omega);
                frequencyDomainData[j] = frequencyDomainData[j].multiply(c);
                k = frequencyDomainData.length - j;
                frequencyDomainData[k] = new Complex(frequencyDomainData[j].getReal(), -1.0 * frequencyDomainData[j].getImaginary());
            }
            double omega = 2.0 * Math.PI * nfreqs * delfreq;
            Complex c = new Complex(0.0, omega);
            frequencyDomainData[nfreqs] = frequencyDomainData[nfreqs].multiply(c);
        }
    }

    public float[] getAmplitudeArray() {
        float[] result = new float[frequencyDomainData.length];
        for (int j = 0; j < result.length; ++j) {
            result[j] = (float) frequencyDomainData[j].abs();
        }
        return result;
    }

    public float[] getPhaseArray() {
        float[] result = new float[frequencyDomainData.length];
        for (int j = 0; j < result.length; ++j) {
            Complex c = frequencyDomainData[j];
            result[j] = (float) Math.atan2(c.getImaginary(), c.getReal());
        }
        return result;

    }

    public float[] getRealArray() {
        float[] result = new float[frequencyDomainData.length];
        for (int j = 0; j < result.length; ++j) {
            result[j] = (float) frequencyDomainData[j].getReal();
        }
        return result;
    }

    public float[] getImagArray() {
        float[] result = new float[frequencyDomainData.length];
        for (int j = 0; j < result.length; ++j) {
            result[j] = (float) frequencyDomainData[j].getImaginary();
        }
        return result;

    }

    public int getNumPositiveFrequencies() {
        return frequencyDomainData.length / 2;
    }

    public float[] getPositiveFreqAmplitudeArray() {
        int npts = getNumPositiveFrequencies();
        float[] result = new float[npts];
        for (int j = 0; j < npts; ++j) {
            result[j] = (float) frequencyDomainData[j + blockStartIndex].abs();
        }
        return result;
    }

    public float[] getPositiveFreqArray() {
        int npts = getNumPositiveFrequencies();
        float[] result = new float[npts];
        int k = 0;
        for (int j = blockStartIndex; j <= blockEndIndex; ++j) {
            result[k++] = (float) ((j) * delfreq);
        }
        return result;
    }

    public float[] getPositiveFreqPhaseArray() {
        int npts = getNumPositiveFrequencies();
        float[] result = new float[npts];
        int k = 0;
        for (int j = blockStartIndex; j <= blockEndIndex; ++j) {
            Complex c = frequencyDomainData[j];
            result[k++] = (float) Math.atan2(c.getImaginary(), c.getReal());
        }
        return result;
    }

    public float[] getPositiveFreqRealArray() {
        int npts = getNumPositiveFrequencies();
        float[] result = new float[npts];
        int k = 0;
        for (int j = blockStartIndex; j <= blockEndIndex; ++j) {
            Complex c = frequencyDomainData[j];
            result[k++] = (float) c.getReal();
        }
        return result;
    }

    public float[] getPositiveFreqImagArray() {
        int npts = getNumPositiveFrequencies();
        float[] result = new float[npts];
        int k = 0;
        for (int j = blockStartIndex; j <= blockEndIndex; ++j) {
            Complex c = frequencyDomainData[j];
            result[k++] = (float) c.getImaginary();
        }
        return result;
    }

    public void trimTo(int intStartOffset, int intEndOffset) {
        if (intEndOffset <= intStartOffset) {
            return;
        }
        blockStartIndex = Math.max(1, intStartOffset);
        blockEndIndex = Math.min(maxAllowableIndex, intEndOffset);
    }

    public void clear() {
        frequencyDomainData = new Complex[0];
    }

    public boolean isPlottable() {
        return frequencyDomainData != null && frequencyDomainData.length > 1;
    }

    public void rq(double q, double r, double c) {
        for (int j = 0; j < frequencyDomainData.length; ++j) {
            double f = j * delfreq;
            double factor = Math.exp(Math.PI * r * f / q / c);
            Complex v = frequencyDomainData[j];
            double amp = v.abs() * factor;
            double phase = Math.atan2(v.getImaginary(), v.getReal());
            double x = amp * Math.cos(phase);
            double y = amp * Math.sin(phase);
            frequencyDomainData[j] = new Complex(x, y);
        }
    }

    public static enum PresentationFormat {
        RealImaginary, AmplitudePhase
    }

}
