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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.llnl.gnem.jsac.dataAccess.dataObjects.SpectralData.PresentationFormat;
import gov.llnl.gnem.jsac.plots.PlotPrefs;
import gov.llnl.gnem.jsac.plots.PlotDrawingData;
import gov.llnl.gnem.jsac.plots.PlotUtils;
import gov.llnl.gnem.jsac.plots.Scale;
import gov.llnl.gnem.jsac.plots.TimeMode;
import gov.llnl.gnem.jsac.plots.Xlimits;
import gov.llnl.gnem.jsac.util.PartialDataWindow;
import llnl.gnem.core.util.Epoch;

/**
 *
 * @author dodge1
 */
public class SacPlotData {
    private static final Logger log = LoggerFactory.getLogger(SacPlotData.class);

    private double referenceTime;
    private double b;
    private double e;
    private float[] xValues;
    private float[] yValues;
    private SpectralData spectralData;
    private Double delta;
    private PresentationFormat presentationFormat;
    private double xShiftForRelativeMode = 0;

    public SacPlotData(double beginTime, double delta, float[] yData, double referenceTime) {
        this.delta = delta;
        b = beginTime;
        if (yData == null || yData.length < 1) {
            throw new IllegalStateException("Empty or Null y-data cannot be plotted.");
        }
        e = b + delta * (yData.length - 1);
        yValues = yData.clone();
        xValues = new float[yValues.length];
        for (int j = 0; j < yValues.length; ++j) {
            xValues[j] = (float) (beginTime + j * delta);
        }
        spectralData = null;
        this.referenceTime = referenceTime;
        presentationFormat = null;
    }

    public SacPlotData(float[] xData, float[] yData, double referenceTime) {
        if (xData == null || xData.length < 1) {
            throw new IllegalStateException("Empty or Null x-data cannot be plotted.");
        }
        if (yData == null || yData.length < 1) {
            throw new IllegalStateException("Empty or Null y-data cannot be plotted.");
        }
        if (yData.length != xData.length) {
            throw new IllegalStateException("x-data and y-data must have same lengths.");
        }
        xValues = xData.clone();
        yValues = yData.clone();
        spectralData = null;
        this.referenceTime = referenceTime;
        b = xData[0];
        e = xData[xData.length - 1];
        delta = null;
        presentationFormat = null;
    }

    public SacPlotData(SpectralData dftResult) {
        spectralData = new SpectralData(dftResult);
        xValues = null;
        yValues = null;
        referenceTime = 0;
        delta = spectralData.getDelfreq();
        presentationFormat = dftResult.getPresentationFormat();
        b = spectralData.getMinFreq();
        e = spectralData.getMaxPositiveFreq();
    }

    private void clearData() {
        if (xValues != null) {
            xValues = new float[0];
        }
        if (yValues != null) {
            yValues = new float[0];
        }
        if (spectralData != null) {
            spectralData.clear();
        }
    }

    public double getB() {
        return b;
    }

    public double getE() {
        return e;
    }

    public PresentationFormat getPresentationFormat() {
        return presentationFormat;
    }

    public float[] getxValues() {
        return xValues.clone();
    }

    public float[] getPositiveFrequencies() {
        return spectralData.getPositiveFreqArray();
    }

    public float[] getYValues() {
        return yValues.clone();
    }

    public Double getBeginTimeAsEpochTime() {
        return referenceTime + b;
    }

    public Double getDelta() {
        return delta;
    }

    public float[] getPositiveFreqAmplitudeArray() {
        return spectralData.getPositiveFreqAmplitudeArray();
    }

    public float[] getPositiveFreqPhaseArray() {
        return spectralData.getPositiveFreqPhaseArray();
    }

    public float[] getPositiveFreqRealArray() {
        return spectralData.getPositiveFreqRealArray();
    }

    public float[] getPositiveFreqImagArray() {
        return spectralData.getPositiveFreqImagArray();
    }

    public void maybeApplyFloorToYvalues() {
        if (yValues == null) {
            return;
        }
        if (PlotPrefs.getInstance().getYscale() == Scale.LOG) {
            float minVal = (float) PlotDrawingData.getInstance().getFloorValue();
            for (int j = 0; j < yValues.length; ++j) {
                yValues[j] = yValues[j] >= minVal ? yValues[j] : minVal;
            }
        }
    }

    public void maybeTrimForLogDisplay() {
        if (PlotPrefs.getInstance().getXscale() == Scale.LOG) {
            double plotStartTime = this.xValues[0];
            double deltaT = xValues[1] - xValues[0];
            double floor = PlotDrawingData.getInstance().getFloorValue();
            if (plotStartTime < floor) {
                int numPointsToSkip = (int) Math.round((floor - plotStartTime) / deltaT);
                int remainingNpts = xValues.length - numPointsToSkip;
                float[] reducedX = new float[remainingNpts];
                System.arraycopy(xValues, numPointsToSkip, reducedX, 0, remainingNpts);
                xValues = reducedX;
                float[] reducedY = new float[remainingNpts];
                System.arraycopy(yValues, numPointsToSkip, reducedY, 0, remainingNpts);
                yValues = reducedY;
            }
        }
    }

    public boolean hasNoData() {
        return (xValues == null || xValues.length < 2) && spectralData == null;
    }

    public boolean isFrequencyDomainData() {
        return spectralData != null && presentationFormat != null;
    }

    public float[] getShiftedXValues( double shift) {
        float[] result = new float[xValues.length];
        for (int j = 0; j < xValues.length; ++j) {
            result[j] = (float) (xValues[j] + shift);
        }
        return result;
    }

    public float[] getMaybeShiftedXValues(TimeMode timeMode, double referenceTime, double markerOffset) {
        double offset = this.referenceTime - referenceTime-markerOffset;
        float[] result = new float[xValues.length];
        xShiftForRelativeMode = timeMode == TimeMode.RELATIVE ? -xValues[0] : offset;
        for (int j = 0; j < xValues.length; ++j) {
            result[j] = (float) (xValues[j] + xShiftForRelativeMode);
        }
        return result;
    }

    public void maybeApplyXLimits(SacTraceData std) {
        Xlimits limits = PlotPrefs.getInstance().getXlimits();
        if (limits.isLimitsOn()) {
            int lastSample = getLastSampleIndex();
            double minVal = b;
            int intStartOffset = 0;
            double maxVal = e;
            int intEndOffset = lastSample;
            PartialDataWindow pdw = limits.getPdw();
            minVal = PlotUtils.maybeSetMinVal(pdw, std, minVal);
            if (minVal > e) { //Nothing to plot
                b = minVal;
                e = minVal;
                clearData();
                return;
            }
            if (minVal > b) {
                intStartOffset = getStartOffset(minVal);
            }

            maxVal = PlotUtils.maybeSetMaxVal(pdw, std, maxVal);
            if (maxVal < b) {//Nothing to plot
                b = maxVal;
                e = maxVal;
                clearData();
                return;
            }
            if (maxVal > b && maxVal < e) {
                intEndOffset = getEndOffset(maxVal);

            }
            if (intStartOffset > 0 || intEndOffset < lastSample) {
                b = intStartOffset > 0 ? minVal : b;
                e = intEndOffset < lastSample ? maxVal : e;

                if (xValues != null && yValues != null) {
                    int npts = intEndOffset - intStartOffset + 1;
                    float[] v1 = new float[npts];
                    float[] v2 = new float[npts];
                    System.arraycopy(xValues, intStartOffset, v1, 0, npts);
                    System.arraycopy(yValues, intStartOffset, v2, 0, npts);
                    xValues = v1;
                    yValues = v2;
                } else if (spectralData != null) {
                    spectralData.trimTo(intStartOffset, intEndOffset);
                }
            }
        }
    }

    private int getEndOffset(double maxVal) {
        int intOffset = 0;
        double offset = maxVal - b;
        if (spectralData != null) {
            return (int) Math.round(offset / delta);
        } else {
            if (delta != null) {
                intOffset = (int) Math.round(offset / delta);
            } else if (xValues == null) {
                log.warn("Cannot set limits because both DELTA and XVALUES are null!");
                return intOffset;
            } else {
                for (int j = xValues.length - 1; j > 0; --j) {
                    if (j <= 1) { // did not find an X less than max value.
                        log.warn("Cannot set limits because no XVALUE is less than than requested maximum.!");
                        return intOffset;
                    }
                    if (xValues[j] <= maxVal) {
                        return j;
                    }
                }
            }
            return intOffset;
        }
    }

    private int getStartOffset(double minVal) {
        int intOffset = 0;
        double offset = minVal - b;
        if (this.spectralData != null) {
            return (int) Math.round(offset / delta);
        } else {
            if (delta != null) {
                return (int) Math.round(offset / delta);
            } else if (this.xValues == null) {
                log.warn("Cannot set limits because both DELTA and XVALUES are null!");
                return intOffset;
            } else {
                for (int j = 0; j < xValues.length; ++j) {
                    if (j >= xValues.length) { // did not find an X greater than min value.
                        log.warn("Cannot set limits because no XVALUE is greater than requested minimum.!");
                        return intOffset;
                    }
                    if (xValues[j] > minVal) {// First sample where x value is greater than requested min value.
                        return j;
                    }
                }
            }
            return intOffset;
        }
    }

    private int getLastSampleIndex() {
        if (xValues != null) {
            return xValues.length - 1;
        } else if (this.spectralData != null) {
            return spectralData.getNumPositiveFrequencies() - 1;
        } else {
            throw new IllegalStateException("Neither xValues nor dft are non-null!");
        }
    }

    public Epoch getEpoch() {
        return new Epoch(referenceTime + b, referenceTime + e);
    }

}
