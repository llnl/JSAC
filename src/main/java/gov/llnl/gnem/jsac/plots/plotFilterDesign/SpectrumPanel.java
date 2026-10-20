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
package gov.llnl.gnem.jsac.plots.plotFilterDesign;

import java.awt.Color;

import org.apache.commons.math3.complex.Complex;

import gov.llnl.gnem.jsac.plots.PlotColors;
import gov.llnl.gnem.jsac.plots.PlotUtils;
import llnl.gnem.core.gui.plotting.AxisScale;
import llnl.gnem.core.gui.plotting.HorizPinEdge;
import llnl.gnem.core.gui.plotting.Legend;
import llnl.gnem.core.gui.plotting.PenStyle;
import llnl.gnem.core.gui.plotting.VertPinEdge;
import llnl.gnem.core.gui.plotting.jmultiaxisplot.JMultiAxisPlot;
import llnl.gnem.core.gui.plotting.jmultiaxisplot.JSubplot;
import llnl.gnem.core.gui.plotting.plotobject.Line;

public class SpectrumPanel extends JMultiAxisPlot {

    public SpectrumPanel() {
        setplotSpacing(2.0);
    }

    void plot(double deltaF, float[] analogReals, float[] analogImags, float[] digitalReals, float[] digitalImags, float[] analogGroupDelayData, float[] digitalGroupDelay) {
        clear();
        float[] frequencies = buildFreqArray(deltaF, analogReals.length);
        float[] analogAmp = new float[analogReals.length];
        float[] analogPhase = new float[analogReals.length];
        for (int j = 0; j < analogReals.length; ++j) {
            Complex c = new Complex(analogReals[j], analogImags[j]);
            analogAmp[j] = (float) c.abs();
            analogPhase[j] = (float) Math.atan2(c.getImaginary(), c.getReal());
        }

        float[] digitalAmp = new float[digitalReals.length];
        float[] digitalPhase = new float[digitalImags.length];
        for (int j = 0; j < digitalReals.length; ++j) {
            Complex c = new Complex(digitalReals[j], digitalImags[j]);
            digitalAmp[j] = (float) c.abs();
            digitalPhase[j] = (float) Math.atan2(c.getImaginary(), c.getReal());
        }

        JSubplot ampSubplot = addSubplot();
        Line analogLine = (Line) ampSubplot.Plot(frequencies, analogAmp);
        analogLine.setPenStyle(PenStyle.DASH);
        PlotUtils.setYAxisScale(ampSubplot);
        analogLine.setColor(Color.ORANGE);

        Line digitalLine = new Line(frequencies, digitalAmp);
        ampSubplot.AddPlotObject(digitalLine);
        digitalLine.setPenStyle(PenStyle.SOLID);
        digitalLine.setColor(Color.BLUE);

        Legend legend = new Legend(getTitle().getFontName(), getTitle().getFontSize(), HorizPinEdge.RIGHT, VertPinEdge.TOP, 5, 5);
        legend.addLabeledLine("Analog Amplitude", analogLine);
        legend.addLabeledLine("Digital Amplitude", digitalLine);
        ampSubplot.AddPlotObject(legend);

        ampSubplot.getYaxis().setLabelText("Amplitude");
        ampSubplot.getPlotRegion().setBackgroundColor(PlotColors.getInstance().getBackGroundColor());
        PlotUtils.setYAxisScale(ampSubplot);
        PlotUtils.maybeSetYlimits(ampSubplot);
        PlotUtils.setYlabelAttributes(ampSubplot);
        PlotUtils.setSubplotAttributes(ampSubplot);
        PlotUtils.setXlabelAttributes(this);
        PlotUtils.setXscale(this);
        PlotUtils.setXaxisColor(this);
        PlotUtils.renderTitle(this);
        PlotUtils.renderBorder(this);
        this.getXaxis().setLabelText("Frequency (Hz)");

        JSubplot phaseSubplot = addSubplot();
        analogLine = (Line) phaseSubplot.Plot(frequencies, analogPhase);
        analogLine.setPenStyle(PenStyle.DASH);
        analogLine.setColor(Color.ORANGE);

        digitalLine = new Line(frequencies, digitalPhase);
        phaseSubplot.AddPlotObject(digitalLine);
        digitalLine.setPenStyle(PenStyle.SOLID);
        digitalLine.setColor(Color.BLUE);
        legend = new Legend(getTitle().getFontName(), getTitle().getFontSize(), HorizPinEdge.RIGHT, VertPinEdge.TOP, 5, 5);
        legend.addLabeledLine("Analog Phase", analogLine);
        legend.addLabeledLine("Digital Phase", digitalLine);
        phaseSubplot.AddPlotObject(legend);

        phaseSubplot.getPlotRegion().setBackgroundColor(PlotColors.getInstance().getBackGroundColor());
        phaseSubplot.getYaxis().setLabelText("Phase (Radians)");
        PlotUtils.maybeSetYlimits(phaseSubplot);
        PlotUtils.setYlabelAttributes(phaseSubplot);
        PlotUtils.setSubplotAttributes(phaseSubplot);

        JSubplot groupDelaySubplot = addSubplot();
        analogLine = (Line) groupDelaySubplot.Plot(0, deltaF, analogGroupDelayData);
        analogLine.setPenStyle(PenStyle.DASH);
        analogLine.setColor(Color.ORANGE);
        digitalLine = new Line(frequencies, digitalGroupDelay);
        groupDelaySubplot.AddPlotObject(digitalLine);
        digitalLine.setPenStyle(PenStyle.SOLID);
        digitalLine.setColor(Color.BLUE);

        legend = new Legend(getTitle().getFontName(), getTitle().getFontSize(), HorizPinEdge.RIGHT, VertPinEdge.TOP, 5, 5);
        legend.addLabeledLine("Analog Group Delay", analogLine);
        legend.addLabeledLine("Digital Group Delay", digitalLine);
        groupDelaySubplot.AddPlotObject(legend);

        PlotUtils.setYAxisScale(groupDelaySubplot);
        groupDelaySubplot.getPlotRegion().setBackgroundColor(PlotColors.getInstance().getBackGroundColor());
        groupDelaySubplot.getYaxis().setLabelText("Seconds");

        PlotUtils.setXlabelAttributes(this);
        PlotUtils.setXscale(this);
        PlotUtils.setXaxisColor(this);
        PlotUtils.renderTitle(this);
        PlotUtils.renderBorder(this);
        this.getXaxis().setLabelText("Frequency (Hz)");

        AxisScale scale = getCoordinateTransform().getXScale();
        if (scale == AxisScale.LOG) {
            setXscale(frequencies);
        }
        scale = getCoordinateTransform().getYScale();
        if (scale == AxisScale.LOG) {
            setYscale(analogAmp, ampSubplot);
        }
        //        setplotSpacing(1.0);
        repaint();
    }

    private float[] buildFreqArray(double deltaF, int length) {
        float[] result = new float[length];
        for (int j = 1; j < length; ++j) {
            result[j] = (float) (j * deltaF);
        }
        return result;
    }

    private void setXscale(float[] frequencies) {
        float minFreq = frequencies[0];
        if (minFreq <= 0) {
            minFreq = frequencies[1];
        }
        float maxFreq = frequencies[frequencies.length - 1];
        setAllXlimits(minFreq, maxFreq);
    }

    private void setYscale(float[] amp, JSubplot ampPlot) {
        double min = Double.MAX_VALUE;
        double max = -min;
        for (float v : amp) {
            if (v < min) {
                min = v;
            }
            if (v > max) {
                max = v;
            }
        }
        if (min < 1.0e-10) {
            min = 1.0e-10;
        }
        if (max < 1) {
            max = 1;
        }
        ampPlot.getYaxis().setMin(min);
        ampPlot.getYaxis().setMax(max);
    }

}
